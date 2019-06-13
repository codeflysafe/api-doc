package com.hsjfans.github.util;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.Lists;
import com.hsjfans.github.model.*;
import com.hsjfans.github.model.ClassField;
import com.hsjfans.github.parser.AbstractParser;
import com.hsjfans.github.parser.ClassCache;
import com.hsjfans.github.parser.ParserException;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        final List<ClassField> requestParams = Lists.newArrayListWithCapacity(methodDeclaration.getParameters().size());

        // start handle comment
        Javadoc javadoc ;
        if(methodDeclaration.getJavadoc().isPresent()){
            javadoc = methodDeclaration.getJavadoc().get();
            controllerMethod.setDescription(javadoc.getDescription().toText());
            for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
                com.github.javaparser.ast.body.Parameter parameter = methodDeclaration.getParameter(i);
                Parameter nativeParameter = method.getParameters()[i];
                // 查处对应的 @Param 注解
                List<JavadocBlockTag> javadocBlockTags = javadoc.getBlockTags().stream().filter(javadocBlockTag ->
                        javadocBlockTag.getName().isPresent()&&javadocBlockTag.is(JavadocBlockTag.Type.PARAM)&&!javadocBlockTag.isInlineIgnore()
                                &&javadocBlockTag.getName().get().equals(parameter.getNameAsString()))
                        .collect(Collectors.toList());
                if(javadocBlockTags.size()>0){
                    // 这里只取第一个同名的 param
                    JavadocBlockTag javadocBlockTag = javadocBlockTags.get(0);
                    ClassField requestParam = new ClassField();
                    requestParam.setFuzzy(javadocBlockTag.isInlineFuzzy());
                    requestParam.setNullable(javadocBlockTag.isInlineNullable());
                    requestParam.setName(javadocBlockTag.getName().get());
                    requestParam.setDescription(javadocBlockTag.getContent().toText());
                    requestParam.setType(nativeParameter.getType().getSimpleName());
                    // 开始解析 基本类型
                   if(isParameterPrimitive(nativeParameter)||nativeParameter.getType().equals(String.class)||nativeParameter.getType().isEnum()||isTime(nativeParameter.getType())){
                       // nothing to do
                    } else {
                        // 去解析以下，看结果再加吧
                        List<ClassField> requestParams1 = parseRequestParam(nativeParameter.getType(),false);
//                        System.out.println(" requestParams1 is "+requestParams1);
                        requestParam.setFields(requestParams1);
                    }

                    if(requestParam.getName()==null){
                        requestParam.setName(nativeParameter.getName());
                    }

                    requestParams.add(requestParam);
                }

            }
            List<JavadocBlockTag> javadocBlockTags = javadoc.getBlockTags().stream().filter(javadocBlockTag -> javadocBlockTag.getType().equals(JavadocBlockTag.Type.RETURN))
                    .collect(Collectors.toList());


            final ResponseReturn responseReturn = new ResponseReturn();

            if(javadocBlockTags.size()>0){
                JavadocBlockTag javadocBlockTag = javadocBlockTags.get(0);
                responseReturn.setDescription(javadocBlockTag.getContent().toText());
                responseReturn.setName(javadocBlockTag.getName().orElse(null));
                //  ReturnItem returnItem = new ReturnItem();

                // 返回值只解析 description
            }
            List<JavadocBlockTag> authors = javadoc.getBlockTags().stream().filter(javadocBlockTag -> javadocBlockTag.getType().equals(JavadocBlockTag.Type.AUTHOR))
                    .collect(Collectors.toList());
//            System.out.println(authors);
            if(authors.size()>0){
                controllerMethod.setAuthor(authors.get(0).getContent().toText());
            }
            responseReturn.setType(method.getReturnType().getSimpleName());
            // name
            List<JavadocBlockTag> names = javadoc.getBlockTags().stream().filter(javadocBlockTag -> javadocBlockTag.getType().equals(JavadocBlockTag.Type.NAME))
                    .collect(Collectors.toList());
//            System.out.println(names);
            if(names.size()>0){
                controllerMethod.setName(names.get(0).getContent().toText());
            }
            // parse method return
            if(!isPrimitive(method.getReturnType())&&!method.getReturnType().isEnum()&&!method.getReturnType().getSimpleName().equals("String")){
                responseReturn.setReturnItem(parseRequestParam(method.getReturnType(),true));
            }else if(method.getReturnType().isEnum()){
                responseReturn.setEnumValues(method.getReturnType().getEnumConstants());
                responseReturn.setType("String");
            }
            controllerMethod.setParams(requestParams);

            if(responseReturn.getName()==null){
                responseReturn.setName(method.getReturnType().getSimpleName());
            }
            controllerMethod.setResponseReturn(responseReturn);
            if(controllerMethod.isIgnore()){return null;}
//            System.out.println(controllerMethod);
            return controllerMethod;
        }

        return null;
    }


    /**
     *
     * 解析 field 参数
     * 支持`request`请求以及`response`返回值
     * @param request the request param class
     */
    private static List<ClassField> parseRequestParam(Class<?> request,boolean response){
        final List<ClassField> requestParams = Lists.newLinkedList();
        TypeDeclaration typeDeclaration = ClassCache.getCompilationUnit(request.getName());
        if (typeDeclaration==null){
            return requestParams;
        }
        Arrays.stream(request.getDeclaredFields()).filter(
                field -> !field.isSynthetic()&&
                (field.getModifiers() & Modifier.FINAL) == 0
                && (field.getModifiers() & Modifier.STATIC)==0
                && (field.getModifiers() & Modifier.NATIVE)==0
                && (field.getModifiers() & Modifier.ABSTRACT)==0
                && (field.getModifiers() & Modifier.INTERFACE)==0
                && (field.getModifiers() & Modifier.TRANSIENT)==0
        ).forEach(field -> {
            final ClassField requestParam = new ClassField();
            // 类型信息，这里填充
            requestParam.setType(field.getType().getSimpleName());
            // 先填充注释信息
            typeDeclaration.getFieldByName(field.getName()).ifPresent(fieldDeclaration -> {
                if(((FieldDeclaration)fieldDeclaration).getComment().isPresent()){
                    Javadoc javadoc = ((FieldDeclaration)fieldDeclaration).getComment().get().parse();
                    System.out.println(" javadoc is =========> "+javadoc);
                    Optional<JavadocBlockTag> ignoreOpt = CollectionUtil.contains(javadoc.getBlockTags(),JavadocBlockTag.Type.IGNORE);
                    ignoreOpt.ifPresent(javadocBlockTag -> {
                        requestParam.setIgnore(true);
                        requestParam.setDescription(javadocBlockTag.getContent().toText());
                    });
                    if(requestParam.isIgnore()){
                        return;
                    }
                    // if contains `@name`
                    CollectionUtil.contains(javadoc.getBlockTags(),JavadocBlockTag.Type.NAME).ifPresent(javadocBlockTag -> {
                        requestParam.setName(javadocBlockTag.getContent().toText());
                        requestParam.setDescription(javadocBlockTag.getContent().toText());
                    });
                    // if contains `@fuzzy`
                    CollectionUtil.contains(javadoc.getBlockTags(),JavadocBlockTag.Type.FUZZY).ifPresent(javadocBlockTag -> {
                        requestParam.setFuzzy(true);
                        requestParam.setDescription(javadocBlockTag.getContent().toText());
                    });
                    // if contains `@nullable`
                    CollectionUtil.contains(javadoc.getBlockTags(),JavadocBlockTag.Type.NULLABLE).ifPresent(javadocBlockTag -> {
                        requestParam.setNullable(true);
                        requestParam.setDescription(javadocBlockTag.getContent().toText());
                    });
                    if(!javadoc.getDescription().toText().isEmpty()){
                        requestParam.setDescription(javadoc.getDescription().toText());
                    }
                }
            });

            // 如果 参数被忽略，跳过
            if(requestParam.isIgnore()&&!response){
                return;
            }
//             如果是基本类型，这里直接进行解析
            if(isFieldPrimitive(field)||field.getType().equals(String.class)||isTime(field.getType())){
               // nothing to do
            }
            // 如果是个枚举，伪装成 字符串 处理
            else if(field.getType().isEnum()){
                Object[] enumValues = new Object[field.getType().getEnumConstants().length];
                for (int i = 0; i <field.getType().getEnumConstants().length ; i++) {
                    enumValues[i] = field.getType().getEnumConstants()[i];
                }
                requestParam.setEnumValues(enumValues);
                requestParam.setType("String");
            }
            // 如果是个数组，解析数组内的元素
            else if(field.getType().isArray()) {
                Field[] fields = field.getType().getFields();
                requestParam.setName(field.getName());
                if(fields.length>0){
                    requestParam.setFields(parseRequestParam(fields[0].getType(),response));
                }
            }
            else {
                // 如果是标准库的集合类型
                Class<?> c = isFieldCollection(field);
                if(c!=null){
                    requestParam.setFields(parseRequestParam(c,response));
                }
                // todo 其它类型暂不支持
            }
            if(requestParam.getName()==null){requestParam.setName(field.getName());}
            requestParams.add(requestParam);

        });
        return requestParams;
    }





    private static boolean isFieldPrimitive(Field field){
        return isPrimitive(field.getType());
    }

    private static boolean isParameterPrimitive(Parameter parameter){
        return isPrimitive(parameter.getType());
    }


    private static boolean isPrimitive(Class<?> cl){
        if(cl.isPrimitive()){
            return true;
        }
        try {
//            System.out.println(cl.getField("TYPE"));
            return  ((Class)(cl.getField("TYPE").get(null))).isPrimitive();
        } catch (NoSuchFieldException | IllegalAccessException e) {
//            System.out.println(e);
        }
        return false;

    }

    private static Class<?> isFieldCollection(Field field){

       Type t = field.getGenericType();
       if(t instanceof ParameterizedType){
           ParameterizedType pt = (ParameterizedType) t;
           try {
               return AbstractParser.classLoader.loadClass(pt.getActualTypeArguments()[0].getTypeName())  ;//得到对象list中实例的类型
           }catch (Exception e){
               return null;
           }
       }
       return null;
    }


    private static Class<?> isParameterCollection(Parameter parameter){
        return isCollection(parameter.getType());
    }

    private static Class<?> isCollection(Class<?> c){
        Type t = c.getGenericSuperclass();
        if(t instanceof ParameterizedType){
            ParameterizedType pt = (ParameterizedType) t;
            return  (Class) pt.getActualTypeArguments()[0];//得到对象list中实例的类型
        }
        return null;
    }






    private static boolean isTime(Class<?> c){
        if(c.equals(LocalDateTime.class)){
            return true;
        }
        if(c.equals(LocalDate.class)){
            return true;
        }
        return false;
    }


    public static String[] methodSignature(Method method){
        String[] strings = new String[method.getParameters().length];
        for (int i = 0; i < method.getParameters().length; i++) {
            strings[i] = method.getParameters()[i].getType().getSimpleName();
        }
        return strings;
    }



    /**
     * @see #toString()
     * @param args args
     */
    public static void main(String[] args) throws  ParserException {

       System.out.println(isPrimitive(Integer.class));

    }




}