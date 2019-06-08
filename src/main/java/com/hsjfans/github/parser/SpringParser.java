package com.hsjfans.github.parser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.model.ControllerMethod;
import com.hsjfans.github.util.ClassUtils;
import com.hsjfans.github.util.LogUtil;
import com.hsjfans.github.util.SpringUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    protected Set<CompilationUnit> getAllControllerClass(Set<File> javaFiles) {

        Set<CompilationUnit> compilationUnits = Sets.newHashSet();
        javaFiles.forEach(file->{
           CompilationUnit compilationUnit = ClassUtils.parseJavaFile(file);
//           compilationUnit.getPrimaryType().filter(TypeDeclaration::isClassOrInterfaceDeclaration)
//                  .get();
           if (compilationUnit!=null){
               Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
               if(packageDeclaration.isPresent()){
                   String packageName = packageDeclaration.get().getNameAsString();
                   Optional<TypeDeclaration<?>> typeDeclaration = compilationUnit.getPrimaryType();
                   if(typeDeclaration.isPresent()){
                       String className = packageName+"."+typeDeclaration.get().getName();
                       Class<?> cl ;
                       try {
                           LogUtil.info(" packageName = %s and typeDeclaration.get().getName() = %s "
                           ,packageName,typeDeclaration.get().getName());
//                           cl = classLoader.loadClass(className);
                           ClassCache.putCompilationUnit(className,typeDeclaration.get());
//                           ClassCache.putClass(className,cl);
                           compilationUnits.add(compilationUnit);
                       }catch (Exception e){
                           LogUtil.warn(e.getMessage());
                       }
                   }
               }
           }
        });

        return compilationUnits;
    }

    @Override
    protected void   parseCompilationUnit(CompilationUnit compilationUnit,final Set<ControllerClass> controllerClasses) {
        compilationUnit.getPrimaryType().ifPresent(typeDeclaration ->
        {
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
                   LogUtil.warn(" className = %s 加载失败 err = %s ",className,e.getMessage());
                   return;
               }

               controllerClass =  ClassUtils.parseClassComment(typeDeclaration.getComment().orElse(null),cl);
               if(controllerClass==null||controllerClass.isIgnore()){return;}

               LogUtil.info(" className = %s 加载完毕 %s",className,controllerClass);
               final List<ControllerMethod> controllerMethods = Lists.newArrayListWithCapacity(cl.getMethods().length);
               // just public method
               Arrays.stream(cl.getMethods())
                       .filter(method ->
                       Arrays.stream(method.getAnnotations()).anyMatch(annotation ->
                               SpringUtil.map.containsKey(annotation.annotationType().getSimpleName())))
               .forEach(method -> {
//                   LogUtil.info(" methodName is %s annotations len = %s ",method.getName(),method.getAnnotations()[0].annotationType().getSimpleName());
                   List<MethodDeclaration> methodDeclarations = typeDeclaration.getMethodsByName(method.getName());
//                   LogUtil.info( " methodDeclarations is %s",methodDeclarations.toString());
                   if(methodDeclarations.size()>0){
                       ControllerMethod controllerMethod = null;
                       try {
                           controllerMethod = ClassUtils.parseMethodComment(methodDeclarations.get(0).getComment().orElse(null),method);
                           if(controllerMethod!=null){
                               controllerMethod.setAClass(cl);
                               controllerMethods.add(controllerMethod);
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

    private static String methodSignature(Method method){
        StringBuilder stringBuilder = new StringBuilder(method.getName());
        Arrays.stream(method.getParameters()).forEach(
                parameter -> {
                    stringBuilder.append(parameter.getType().getSimpleName());
                }
        );
        return stringBuilder.toString();
    }


}
