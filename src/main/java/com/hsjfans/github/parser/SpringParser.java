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
import com.hsjfans.github.model.*;
import com.hsjfans.github.util.*;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;


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
        // 先解析 doc 文件
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

        // 后填充 requestMapping 的一些属性
        Arrays.stream(cl.getAnnotations()).filter(SpringUtil::isSpringRequestAnnotation).forEach(annotation -> {
            controllerClass.fulfillRequestMapping(SpringUtil.parseRequestMapping(annotation));
        });
    }

    @Override
    protected  ControllerMethod parseControllerMethod(MethodDeclaration methodDeclaration, Method method){
        ControllerMethod controllerMethod = new ControllerMethod();

        // 首先填充 requestMapping 属性
        Arrays.stream(method.getAnnotations()).filter(SpringUtil::isSpringRequestAnnotation).forEach(annotation->
                controllerMethod.fulfillReqestMapping(SpringUtil.parseRequestMapping(annotation)));


        // 解析 doc 内容
        methodDeclaration.getJavadoc().ifPresent(javadoc ->{
            if(javadoc.getBlockTags().stream().anyMatch(javadocBlockTag->javadocBlockTag.is(JavadocBlockTag.Type.IGNORE))){
                controllerMethod.setIgnore(true);
            }
        });

        if(controllerMethod.isIgnore()){return null;}

        List<RequestParameter> requestParameters = Lists.newArrayListWithCapacity(method.getParameterCount());

        // 开始解析 parameter 参数
        methodDeclaration.getJavadoc().ifPresent(javadoc -> {
            javadoc.getBlockTags().stream().filter(javadocBlockTag ->
                    javadocBlockTag.getName().isPresent()&&javadocBlockTag.is(JavadocBlockTag.Type.PARAM)&&!javadocBlockTag.isInlineIgnore())
                    .forEach(javadocBlockTag -> {
                        int idx = ParseUtil.getParameterIndexViaJavaDocTagName(javadocBlockTag.getName().get(),methodDeclaration);
                        if(idx<0){
                            return;
                        }
                        Parameter parameter = method.getParameters()[idx];
                        RequestParameter requestParam = new RequestParameter();
                        requestParam.setFuzzy(javadocBlockTag.isInlineFuzzy());
                        requestParam.setNullable(javadocBlockTag.isInlineNullable());
                        requestParam.setName(javadocBlockTag.getName().get());
                        requestParam.setDescription(javadocBlockTag.getContent().toText());
                        requestParam.setTypeName(parameter.getType().getSimpleName());
                        requestParam.setFields(parseParameterClassField(parameter));
                        // 如果是基本类型，这里直接进行解析
                        if(ClassUtils.isParameterPrimitive(parameter)||parameter.getType().equals(String.class)|ClassUtils.isTime(parameter.getType())){
                            // nothing to do
                        }
                        else {
                            requestParam.setFields(parseParameterClassField(parameter));
                            if(parameter.getType().isEnum()){
                                requestParam.setEnumValues(requestParam.getFields().get(0).getEnumValues());
                            }
                        }
                        requestParameters.add(requestParam);
                    });
        });

        controllerMethod.setRequestParameters(requestParameters);

        // 最后填充 return 参数
        ResponseReturn responseReturn = new ResponseReturn();

        methodDeclaration.getJavadoc().ifPresent(javadoc -> {javadoc.getBlockTags().stream().filter(javadocBlockTag -> javadocBlockTag.is(JavadocBlockTag.Type.RETURN))
                .forEach(javadocBlockTag -> {responseReturn.setDescription(javadocBlockTag.getContent().toText());});
        });

        // 如果是基本类型，这里直接进行解析
        if(ClassUtils.isPrimitive(method.getReturnType())||method.getReturnType().equals(String.class)|ClassUtils.isTime(method.getReturnType())){
            // nothing to do
        }// 如果是个枚举，伪装成 字符串 处理
        else {
            responseReturn.setReturnItem(parseReturnClassField(method.getReturnType()));
            if(method.getReturnType().isEnum()){
                responseReturn.setEnumValues(responseReturn.getReturnItem().get(0).getEnumValues());
            }
        }

        controllerMethod.setResponseReturn(responseReturn);


        return controllerMethod;
    }


    protected List<ClassField> parseParameterClassField(Parameter parameter){
        return parserClassFields(parameter.getParameterizedType(),parameter.getType(),false);
    }



    protected List<ClassField> parseReturnClassField(Class<?> cl){

        System.out.println("cl is "+cl);




        return parserClassFields(cl.getGenericSuperclass(),cl,true);
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



}
