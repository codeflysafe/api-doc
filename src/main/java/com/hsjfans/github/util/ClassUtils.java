package com.hsjfans.github.util;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.Lists;
import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.*;
import com.hsjfans.github.model.RequestParam;
import com.hsjfans.github.parser.ClassCache;
import com.hsjfans.github.parser.Parser;
import com.hsjfans.github.parser.ParserException;
import com.hsjfans.github.parser.SpringParser;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;


public class ClassUtils {

    /**
     *
     * find all java source from project
     *
     * @param packageName the java source path name
     * @param recursion scan recursion or not
     * @return
     */
	public static Set<File> scan(String packageName,boolean recursion){
        Set<File> javaFiles = new HashSet<>();
	    File file = new File(packageName);

	    if(file.isDirectory()){
            javaFiles.addAll(scanDir(file.getPath(),recursion));
        }
	    return javaFiles;
    }

    /**
     *
     * @param packageNames the package names
     * @param recursion scan recursion or not
     * @return
     */
    public static Set<File> scan(Collection<String> packageNames, boolean recursion){
        Set<File> javaFiles = new HashSet<>();
	    for(String packageName:packageNames){
            javaFiles.addAll(scan(packageName,recursion));
        }
	    return javaFiles;
    }


    /**
     *
     * 扫描项目
     *
     * @param filePath filePath
     * @param recursion scan recursion or not
     */
    private static Set<File> scanDir(String filePath,boolean recursion){
        Set<File> javaFiles = new HashSet<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if(files==null){return javaFiles;}
        for(File f:files){
            if(f.isDirectory()&&recursion){
                javaFiles.addAll(scanDir(f.getPath(),recursion));
            }else {
                if(f.getName().endsWith(".java")){
                    javaFiles.add(f);
                }
            }
        }
        return javaFiles;
    }


    /**
     *  parse java source
     * @param javaFile file
     * @return CompilationUnit
     * @see CompilationUnit
     */
    public static CompilationUnit parseJavaFile(File javaFile){
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = StaticJavaParser.parse(javaFile);
        } catch (FileNotFoundException e) {
            LogUtil.warn(" parseJavaFile javaFile "+javaFile.getName()+" failed");
        }
        return compilationUnit;
    }


    /**
     *  parse the method  comment to Param
     * @param methodDeclaration
     * @param method
     * @return
     */
    public static ControllerMethod parseMethodComment(MethodDeclaration methodDeclaration, Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        ControllerMethod controllerMethod = new ControllerMethod();
        List<Annotation> annotations = Arrays.stream(method.getAnnotations()).filter(
                annotation ->
                        SpringUtil.map.containsKey(annotation.annotationType().getSimpleName())).collect(Collectors.toList());
        if(annotations.size()>0){
            Annotation annotation = annotations.get(0);
            if(annotation.annotationType().getSimpleName().equals(RequestMapping.class.getSimpleName())){
                RequestMapping mapping = (RequestMapping)annotation;
                controllerMethod.setMethods(mapping.method());
                if(mapping.value().length>0){
                    controllerMethod.setUrl(mapping.value());
                }else {
                    controllerMethod.setUrl(mapping.path());
                }
            }else {
                RequestMapping requestMapping = annotation.annotationType().getAnnotation(RequestMapping.class);
                controllerMethod.setMethods(requestMapping.method());
                Method value = annotation.annotationType().getMethod("value");
                controllerMethod.setUrl((String[]) value.invoke(annotation));
                if(controllerMethod.getUrl()==null){
                    Method path = annotation.annotationType().getMethod("path");
                    controllerMethod.setUrl((String[]) path.invoke(annotation));
                }
            }
        }

        if(controllerMethod.getName()==null){
            controllerMethod.setName(method.getName());
        }


        final List<RequestParam> requestParams = Lists.newArrayListWithCapacity(methodDeclaration.getParameters().size());
        final ResponseReturn responseReturn = new ResponseReturn();

        // start handle comment
        Javadoc javadoc ;
        if(methodDeclaration.getJavadoc().isPresent()){
            javadoc = methodDeclaration.getJavadoc().get();
            controllerMethod.setDescription(javadoc.getDescription().toText());

            for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
                com.github.javaparser.ast.body.Parameter parameter = methodDeclaration.getParameter(i);
                Parameter nativeParameter = method.getParameters()[i];
                List<JavadocBlockTag> javadocBlockTags = javadoc.getBlockTags().stream().filter(javadocBlockTag -> javadocBlockTag.getName().isPresent()&&javadocBlockTag.getType().equals(JavadocBlockTag.Type.PARAM)&&javadocBlockTag.getName().get().equals(parameter.getNameAsString()))
                        .collect(Collectors.toList());

                if(javadocBlockTags.size()>0){
                    JavadocBlockTag javadocBlockTag = javadocBlockTags.get(0);
                    RequestParam requestParam = new RequestParam();
                    requestParam.setFuzzy(javadocBlockTag.isInlineFuzzy());
                    requestParam.setNecessary(!javadocBlockTag.isInlineIgnore());
                    requestParam.setName(javadocBlockTag.getName().orElse(null));
                    requestParam.setDescription(javadocBlockTag.getContent().toText());
                    requestParam.setType(nativeParameter.getType().getTypeName());
                    // 这里只解析 @param 参数
                    // 判断请求参数是否为结构体，如果是 则进行解析
                    if (!nativeParameter.getType().isPrimitive()){
                        List<RequestParam> requestParams1 = parseRequestParam(nativeParameter.getType());
//                        System.out.println(" requestParams1 is "+requestParams1);
                        requestParam.setParams(requestParams1);
                    }

                    if(requestParam.getName()==null){
                        requestParam.setName(nativeParameter.getName());
                    }

                    requestParams.add(requestParam);
                }

            }

            List<JavadocBlockTag> javadocBlockTags = javadoc.getBlockTags().stream().filter(javadocBlockTag -> javadocBlockTag.getName().isPresent()&&javadocBlockTag.getType().equals(JavadocBlockTag.Type.RETURN))
                    .collect(Collectors.toList());
            if(javadocBlockTags.size()>0){
                JavadocBlockTag javadocBlockTag = javadocBlockTags.get(0);
                responseReturn.setDescription(javadocBlockTag.getContent().toText());
                responseReturn.setName(javadocBlockTag.getName().orElse(null));
                //  ReturnItem returnItem = new ReturnItem();

                // 返回值只解析 description
            }

            List<JavadocBlockTag> authors = javadoc.getBlockTags().stream().filter(javadocBlockTag -> javadocBlockTag.getName().isPresent()&&javadocBlockTag.getType().equals(JavadocBlockTag.Type.AUTHOR))
                    .collect(Collectors.toList());
            if(authors.size()>0){
                controllerMethod.setAuthor(authors.get(0).getName().get());
            }
            // parse method return
            if(!method.getReturnType().isPrimitive()){
                responseReturn.setReturnItem(parseRequestParam(method.getReturnType()));
            }

            controllerMethod.setParams(requestParams);
            responseReturn.setType(method.getReturnType().getTypeName());
            if(responseReturn.getName()==null){
                responseReturn.setName(method.getReturnType().getName());
            }
            controllerMethod.setResponseReturn(responseReturn);
            if(controllerMethod.isIgnore()){return null;}
            return controllerMethod;
        }

        return null;
    }



    /**
     *
     * @param request the request param class
     */
    private static List<RequestParam> parseRequestParam(Class<?> request){
        List<RequestParam> requestParams = Lists.newLinkedList();
        TypeDeclaration typeDeclaration = ClassCache.getCompilationUnit(request.getName());
//
        if (typeDeclaration==null){
            return requestParams;
        }
//        System.out.println(typeDeclaration.getName());
        Arrays.stream(request.getDeclaredFields()).filter(field -> !field.isSynthetic()&&
                (field.getModifiers() & Modifier.FINAL) == 0
                && (field.getModifiers() & Modifier.STATIC)==0
                && (field.getModifiers() & Modifier.NATIVE)==0
                && (field.getModifiers() & Modifier.ABSTRACT)==0
                && (field.getModifiers() & Modifier.INTERFACE)==0
                && (field.getModifiers() & Modifier.TRANSIENT)==0
        ).
                forEach(field -> {

                    System.out.println(" filed = "+field.getType());
            RequestParam requestParam = new RequestParam();
            requestParam.setType(field.getType().getTypeName());
            if(field.getType().isPrimitive()){
               typeDeclaration.getFieldByName(field.getName()).ifPresent(fieldDeclaration -> {
                    parseFiledComment(((FieldDeclaration)fieldDeclaration).getComment().orElse(null),requestParam);
                    if(requestParam.getName()==null){
                        requestParam.setName(field.getName());
                        requestParam.setType(field.getType().getSimpleName());
                    }
                });

            }else if(field.getType().isArray()) {

                Field[] fields = field.getType().getFields();
                requestParam.setName(field.getName());
                requestParam.setType(field.getType().getTypeName());
                if(fields.length>0){
                    requestParam.setParams(parseRequestParam(fields[0].getType()));
                }
            }
            else if(field.getType().isEnum()){
//                System.out.println(" enum is"+field);
                Object[] enumValues = new Object[field.getType().getEnumConstants().length];

                for (int i = 0; i <field.getType().getEnumConstants().length ; i++) {
                    enumValues[i] = field.getType().getEnumConstants()[i];
                }

                requestParam.setEnumValues(enumValues);

            }else if(!field.getType().isInterface()) {
                requestParam.setParams(parseRequestParam(field.getType()));
            }

            requestParam.setName(field.getName());
            requestParams.add(requestParam);

        });

        return requestParams;
    }



    // todo handle array
    private static void parseFiledComment(Comment comment,RequestParam requestParam){
        if(comment==null){return;}
        Javadoc javadoc = comment.parse();
        javadoc.getBlockTags().forEach(javadocBlockTag -> {
            // if contains `@ignore`
            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.IGNORE)){
                requestParam.setNecessary(false);
            }
            // if contains `@name`
            if (javadocBlockTag.getType().equals(JavadocBlockTag.Type.NAME)){
                requestParam.setName(javadocBlockTag.getContent().toText());
            }

            // if contains `@fuzzy`
            if (javadocBlockTag.getType().equals(JavadocBlockTag.Type.FUZZY)){
                requestParam.setFuzzy(true);
            }


        });

        requestParam.setDescription(javadoc.getDescription().toText());

    }



    /**
     * @name parse the method  comment to Param
     * @param comment @Ignore
     * @param cl @Ignore
     * @return
     */
    public static ControllerClass parseClassComment(Comment comment, Class<?> cl){


        if(comment==null){return null;}
        final ControllerClass controllerClass = new ControllerClass();
        controllerClass.setAClass(cl);
        Javadoc javadoc = comment.parse();
        javadoc.getBlockTags().forEach(javadocBlockTag ->
        {
            // if contains `@ignore`
            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.IGNORE)){
                controllerClass.setIgnore(true);
            }

            // if contains `@name`
            if (javadocBlockTag.getType().equals(JavadocBlockTag.Type.NAME)){
                controllerClass.setName(javadocBlockTag.getContent().toText());
            }

            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.AUTHOR)){
                controllerClass.setAuthor(javadocBlockTag.getContent().toText());
            }

        });

        if(controllerClass.isIgnore()){return null;}
        if(controllerClass.getName()==null){
            controllerClass.setName(cl.getSimpleName());
        }
        controllerClass.setDescription(javadoc.getDescription().toText());

        // handle url
        Arrays.stream(cl.getAnnotations()).filter(annotation -> annotation.annotationType().getSimpleName().equals("RequestMapping"))
                .forEach(annotation->{
                    RequestMapping mapping = (RequestMapping) annotation;
                    if(mapping.path().length>0){
                        controllerClass.setUrl(mapping.path());
                    } else if (mapping.value().length>0){
                        controllerClass.setUrl(mapping.value());
                    }else {
                        controllerClass.setUrl(new String[]{""});
                    }
                });

        return controllerClass;

    }





    /**
     * @see #toString()
     * @param args args
     */
    public static void main(String[] args) throws  ParserException {

        String testPath = "/Volumes/doc/projects/java/java-api-doc/src/main/java/com/hsjfans/github";
        String realPath = "/Volumes/doc/projects/java/api";
        Config config = new Config();
        config.setPackageName(realPath);
        config.setGradle(true);
        config.setGradlePath("");
        Parser parser = new SpringParser(config);
        parser.parse(config.getPackageName(),true);

    }



}