package com.hsjfans.github.util;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
     * @param comment
     * @param method
     * @return
     */
    public static ControllerMethod parseMethodComment(Comment comment, Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(comment==null){return null;}

        LogUtil.info("comment = %s ,method = %s",comment.toString(),method.getName());

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


        // start handle comment
        Javadoc javadoc = comment.parse();
//        System.out.println(javadoc);
        LogUtil.info(" javaDoc is  %s",javadoc);
        final List<RequestParam> requestParams = Lists.newArrayListWithCapacity(javadoc.getBlockTags().size());
        final ResponseReturn responseReturn = new ResponseReturn();
        javadoc.getBlockTags().forEach(javadocBlockTag ->
        {
            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.IGNORE)){
               controllerMethod.setIgnore(true);
               return;
            }
            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.NAME)){
                controllerMethod.setName(javadocBlockTag.getContent().toText());
            }
            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.PARAM)){
                RequestParam requestParam = new RequestParam();
                requestParam.setFuzzy(javadocBlockTag.isInlineFuzzy());
                requestParam.setNecessary(!javadocBlockTag.isInlineIgnore());
                requestParam.setName(javadocBlockTag.getName().orElse(""));
                requestParam.setDescription(javadocBlockTag.getContent().toText());

                // 这里只解析 @param 参数
                javadocBlockTag.getName().ifPresent(
                        name->{
                            LogUtil.info("  javadocBlockTag name is %s  ",name);
                            LogUtil.info("  method.getParameters() are %s",method.getParameters()[0].getName());
                            // 判断请求参数是否为结构体，如果是 则进行解析
                           List<Parameter> parameters =  Arrays.stream(method.getParameters()).filter(parameter->parameter.getName().equals(name)&&!parameter.getType().isPrimitive())
                                    .collect(Collectors.toList());
                           if(parameters.size()>0){
                               LogUtil.info("  parameter is %s  ",parameters.get(0).getName());
                              requestParam.setParams(parseRequestParam(parameters.get(0).getType()));
                           }
                        }
                );

                requestParams.add(requestParam);
            }

            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.RETURN)){
                responseReturn.setDescription(javadocBlockTag.getContent().toText());
                responseReturn.setDescription(javadocBlockTag.getName().orElse(""));
               //  ReturnItem returnItem = new ReturnItem();

                // 返回值只解析 description
            }

        });


        // parse method return
        parseResponseReturn(method.getReturnType(),responseReturn);



        controllerMethod.setParams(requestParams);
        controllerMethod.setResponseReturn(responseReturn);
        if(controllerMethod.isIgnore()){return null;}
        return controllerMethod;
    }



    /**
     *  parse the method  comment to Param
     * @param returnClass comment {@ignore}
     * @param responseReturn responseReturn
     * @return
     */
    private static List<ReturnItem> parseResponseReturn(Class<?> returnClass,ResponseReturn responseReturn){

        if(returnClass.isPrimitive()){

        }else if(returnClass.isArray()){

        }else if(returnClass.isEnum()){

        }else {

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
        if (typeDeclaration==null){
            return requestParams;
        }
        Arrays.stream(request.getFields()).forEach(field -> {
            RequestParam requestParam = new RequestParam();
            requestParam.setType(field.getType().getTypeName());
            if(field.getType().isPrimitive()){
               typeDeclaration.getFieldByName(field.getName()).ifPresent(fieldDeclaration -> {
                    parseFiledComment(((FieldDeclaration)fieldDeclaration).getComment().orElse(null),requestParam);
                    if(requestParam.getName()==null){
                        requestParam.setName(field.getName());
                    }
                });

            }else if(field.getType().isArray()) {
                // todo

            }else if(!field.getType().isInterface()) {
                requestParam.setParams(parseRequestParam(field.getType()));
            }

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



    private static void parseReturnComment(Comment comment,ReturnItem returnItem){
        if(comment==null){return;}


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



//    public static Class<?> parseImportClass(CompilationUnit compilationUnit){
//        compilationUnit.getImports().forEach(
//                importDeclaration->{
//                    return;
//                }
//        );
//        return null;
//    }
//
//    /**
//     * get file name without extension
//     * @param javaFile
//     * @return string
//     */
//    public static String getJavaFileName(File javaFile){
//        String fileName = javaFile.getName();
//        return fileName.substring(0, fileName.lastIndexOf("."));
//    }


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