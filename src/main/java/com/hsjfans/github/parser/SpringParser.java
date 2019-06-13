package com.hsjfans.github.parser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.model.ControllerMethod;
import com.hsjfans.github.model.RequestMapping;
import com.hsjfans.github.util.ClassUtils;
import com.hsjfans.github.util.JavaDocUtil;
import com.hsjfans.github.util.LogUtil;
import com.hsjfans.github.util.SpringUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class SpringParser extends AbstractParser{


    // 支持的 Controller 类注解
    private static final Set<String> supportClassAnnotations = Sets.newHashSet("RestController","Controller");

    public SpringParser(Config config) {
        super(config);
    }


    @Override
    protected Set<String> supportClassAnnotations() {
        return supportClassAnnotations;
    }

    @Override
    protected void parseControllerClassDoc(Class<?> cl,final ControllerClass controllerClass) {

        TypeDeclaration<?> typeDeclaration = ClassCache.getTypeDeclaration(cl.getName());
        typeDeclaration.getJavadoc().ifPresent(javadoc -> {
            javadoc.getBlockTags().forEach(javadocBlockTag ->
            {
                // if contains `@name`
                if (javadocBlockTag.getType().equals(JavadocBlockTag.Type.NAME)){
                    controllerClass.setName(javadocBlockTag.getContent().toText());
                }
                // if contains `@author`
                if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.AUTHOR)){
                    controllerClass.setAuthor(javadocBlockTag.getContent().toText());
                }
            });
            controllerClass.setDescription(javadoc.getDescription().toText());
        });

        Arrays.stream(cl.getAnnotations()).filter(SpringUtil::isSpringRequestAnnotation).forEach(annotation -> {
            controllerClass.fulfillRequestMapping(SpringUtil.parseRequestMapping(annotation));
        });
    }

    @Override
    protected  ControllerMethod parseControllerMethod(MethodDeclaration methodDeclaration, Method method){
        ControllerMethod controllerMethod = new ControllerMethod();
        Arrays.stream(method.getAnnotations()).filter(SpringUtil::isSpringRequestAnnotation).forEach(annotation->
                controllerMethod.fulfillReqestMapping(SpringUtil.parseRequestMapping(annotation)));

        return controllerMethod;
    }


    /**
     *  从 java 文件中过滤调
     * @param javaFiles javaFiles
     * @return
     */
    @Override
    protected Set<Class<?>> getAllControllerClass(Set<File> javaFiles) {

        Set<Class<?>> classes =  this.parseJavaFiles(javaFiles);
        Iterator<Class<?>> iterator = classes.iterator();
        while (iterator.hasNext()){
            Class<?> next = iterator.next();
            // 首先从缓存中查处对应的 typeDeclaration 没有则移除
            TypeDeclaration<?> typeDeclaration = ClassCache.getTypeDeclaration(next.getName());
            if(typeDeclaration==null){
                iterator.remove();
                continue;
            }
            // 然后判断这个类是不是含有 SpringMVC 的 controller or restController 注解
            // 没有就移除掉
            if(!SpringUtil.isControllerClass(next.getAnnotations())){
               iterator.remove();
               continue;
            }
            // 最后，判断这个类是不是 `@ignore` 注释，是的话也去除掉
            if(JavaDocUtil.isIgnore(typeDeclaration)){
                iterator.remove();
            }
        }

        return classes;

    }

    @Override
    protected void  parseCompilationUnit(CompilationUnit compilationUnit,final Set<ControllerClass> controllerClasses) {
        compilationUnit.getPrimaryType().ifPresent(typeDeclaration ->
        {
            LogUtil.info(" start parse typeDeclaration %s ",typeDeclaration.getName());
            ControllerClass controllerClass ;
            List<AnnotationExpr> annotationExprs =  typeDeclaration.getAnnotations().stream().filter(annotationExpr -> annotationExpr.getNameAsString().equals("Controller") ||
                    annotationExpr.getNameAsString().equals("RestController")).collect(Collectors.toList());
           if(annotationExprs.size()>0){
               Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
               if(!packageDeclaration.isPresent()){
                   return;
               }
               String packageName = packageDeclaration.get().getNameAsString();
               String className = packageName+"."+typeDeclaration.getName();
               Class<?> cl ;
               try {
                   LogUtil.info("开始加载 controller 类  name %s  ",className);
                   cl = classLoader.loadClass(className);
               }catch (Exception e){
                   LogUtil.warn(" controller 类 className = %s 加载失败 err = %s ",className,e.getMessage());
                   return;
               }

               controllerClass =  ClassUtils.parseClassComment(typeDeclaration.getComment().orElse(null),cl);
               if(controllerClass==null||controllerClass.isIgnore()){return;}

               LogUtil.info(" className = %s 加载完毕 %s",className,controllerClass);
               final List<ControllerMethod> controllerMethods = Lists.newArrayListWithCapacity(cl.getMethods().length);

               // just public method
               // PostMapping GetMapping ...
               Arrays.stream(cl.getMethods())
                       .filter(method ->
                       Arrays.stream(method.getAnnotations()).anyMatch(annotation ->
                               SpringUtil.map.containsKey(annotation.annotationType().getSimpleName())))
               .forEach(method -> {
                   List<MethodDeclaration> methodDeclarations = typeDeclaration.getMethodsBySignature(method.getName(),methodSignature(method));
                   LogUtil.info( " methodDeclarations is %s and method is %s ",methodDeclarations.toString(),method.getName());
                   if(methodDeclarations.size()>0){
                       ControllerMethod controllerMethod = null;
                       try {
//                           System.out.println(methodDeclarations.get(0));
                           controllerMethod = ClassUtils.parseMethodComment(methodDeclarations.get(0),method);
                           if(controllerMethod!=null){
                               controllerMethod.setAClass(cl);
                               controllerMethod.setMethod(method);
                               controllerMethods.add(controllerMethod);
                               LogUtil.info( " methodDeclarations %s 解析完毕",methodDeclarations.toString());
                           }
                       } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                           LogUtil.error(" err = %s ",e.getMessage());
                       }
                   }
               });

               controllerClass.setControllerMethod(controllerMethods);
               LogUtil.info("controllerClass is %s ",controllerClass.toString());
               controllerClasses.add(controllerClass);
           }
        });


    }

    private static String[] methodSignature(Method method){
        String[] strings = new String[method.getParameters().length];
        for (int i = 0; i < method.getParameters().length; i++) {
            strings[i] = method.getParameters()[i].getType().getSimpleName();
        }
        return strings;
    }


}
