package com.hsjfans.github.util;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.Lists;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.model.ControllerMethod;
import com.hsjfans.github.model.RequestParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;



public class ClassUtils {

    private static final ClassLoader loader = ClassUtils.class.getClassLoader();

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
    public static ControllerMethod parseMethodComment(Comment comment, MethodDeclaration method){
        if(comment==null){return null;}
        // contain `Controller` or `RestController`;

        ControllerMethod controllerMethod = new ControllerMethod();
        method.getAnnotations().stream().filter(AnnotationExpr::isNormalAnnotationExpr).forEach(annotationExpr -> {
            if(SpringUtil.map.containsKey(annotationExpr.getNameAsString())){
                controllerMethod.setMethods(SpringUtil.map.get(annotationExpr.getNameAsString()));
                annotationExpr.asNormalAnnotationExpr().getPairs().forEach(
                        memberValuePair -> {
                            if(memberValuePair.getNameAsString().equals("value")){
                                controllerMethod.setUrl(StringUtil.parseUrls(memberValuePair.getValue().toString()));
                            }
                            if(memberValuePair.getNameAsString().equals("method")){
                                controllerMethod.setMethods(StringUtil.parseUrls(memberValuePair.getValue().toString()));
                            }
                        }
                );
            }

        });

//        controllerMethod.setMethod(method);
//        if(annotationMap.containsKey(PostMapping.class.getName())){
//            controllerMethod.addRequestMethod(RequestMethod.POST);
//            controllerMethod.setUrl(((PostMapping)annotationMap.get(PostMapping.class.getName())).value());
//        }
//        else if(annotationMap.containsKey(GetMapping.class.getName())){
//            controllerMethod.addRequestMethod(RequestMethod.GET);
//            controllerMethod.setUrl(((GetMapping)annotationMap.get(GetMapping.class.getName())).value());
//        }
//        else if(annotationMap.containsKey(PutMapping.class.getName())){
//            controllerMethod.addRequestMethod(RequestMethod.PUT);
//            controllerMethod.setUrl(((PutMapping)annotationMap.get(PutMapping.class.getName())).value());
//        }
//       else if(annotationMap.containsKey(DeleteMapping.class.getName())){
//            controllerMethod.addRequestMethod(RequestMethod.DELETE);
//            controllerMethod.setUrl(((DeleteMapping)annotationMap.get(DeleteMapping.class.getName())).value());
//        }
//       else if(annotationMap.containsKey(PatchMapping.class.getName())){
//            controllerMethod.addRequestMethod(RequestMethod.PATCH);
//            controllerMethod.setUrl(((PatchMapping)annotationMap.get(PatchMapping.class.getName())).value());
//        }
//
//       else if(annotationMap.containsKey(RequestMapping.class.getName())){
//            RequestMapping mapping = (RequestMapping)annotationMap.get(RequestMapping.class.getName());
//            controllerMethod.setMethods(mapping.method());
//            if(mapping.value().length>0){
//                controllerMethod.setUrl(mapping.value());
//            }else {
//                controllerMethod.setUrl(mapping.path());
//            }
//        }
//        else {
//            return null;
//        }

        // start handle comment
        Javadoc javadoc = comment.parse();

        final List<RequestParam> requestParams = Lists.newArrayListWithCapacity(javadoc.getBlockTags().size());

        javadoc.getBlockTags().forEach(javadocBlockTag ->
        {

            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.IGNORE)){
               controllerMethod.setIgnore(true);
               return;
            }

            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.NAME)){
                controllerMethod.setName(javadocBlockTag.getContent().toString());
            }

            if(javadocBlockTag.getType().equals(JavadocBlockTag.Type.PARAM)){
                RequestParam requestParam = new RequestParam();
                if(!javadocBlockTag.isInlineIgnore()){
                    requestParam.setFuzzy(javadocBlockTag.isInlineFuzzy());
                    requestParam.setNecessary(javadocBlockTag.isInlineNecessary());
                    requestParam.setName(javadocBlockTag.getContent().toText());
                    requestParams.add(requestParam);
                }

            }

        });

        if(controllerMethod.isIgnore()){return null;}

        System.out.println(controllerMethod);
        return controllerMethod;
    }


    /**
     *  parse the method  comment to Param
     * @param comment
     * @param returnType
     * @return
     */
    public static void parseMethodReturn(Comment comment,Class<?> returnType){
        //  todo

    }


    /**
     *  parse the method  comment to Param
     * @param comment comment {@ignore}
     * @param field filed
     * @return
     */
    public static void parseFieldComment(Comment comment, Field field){

        //  todo

    }

    /**
     * @name parse the method  comment to Param
     * @param comment @Ignore
     * @param cl @Ignore
     * @return
     */
    public static ControllerClass parseClassComment(Comment comment, Class<?> cl){
        if(comment==null){return null;}


        // contain `Controller` or `RestController`
        Map<String, Annotation> annotationMap = CollectionUtil.convertToMap(
                cl.getAnnotations()
        );

        if((!annotationMap.containsKey(Controller.class.getName()))&&(!annotationMap.containsKey(RestController.class.getName()))){
            return null;
        }

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
                controllerClass.setName(javadocBlockTag.toText());
            }

        });


        if(controllerClass.isIgnore()){return null;}

        if(controllerClass.getName()==null){
            controllerClass.setName(cl.getSimpleName());
        }

//        System.out.println(annotationMap);
//        System.out.println(RequestMapping.class.getName());
        // handle url
        if(annotationMap.containsKey(RequestMapping.class.getName())){
            RequestMapping mapping = ((RequestMapping)annotationMap.get(RequestMapping.class.getName()));
            controllerClass.setMethods(mapping.method());
            if(mapping.path().length>0){
                controllerClass.setUrl(mapping.path());
            } else if (mapping.value().length>0){
                controllerClass.setUrl(mapping.value());
            }
        }

//        System.out.println(controllerClass);

        return controllerClass;

    }


    public static Class<?> loader(String packageName) throws ClassNotFoundException {
        return loader.loadClass(packageName);
    }


    /**
     * @see #toString()
     * @param args args
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws ClassNotFoundException {
        String testPath = "/Volumes/doc/projects/java/java-api-doc/src/main/java/com/hsjfans/github";
        String realPath = "/Volumes/doc/projects/java/api";
        String test2Path = "/Volumes/doc/projects/java/java-api-doc/src/main/java/com/hsjfans/github/model";
        for(File file:scan(test2Path,true)){
            CompilationUnit compilationUnit = parseJavaFile(file);
            Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
            if(!packageDeclaration.isPresent()){
                return;
            }
            String packageName = packageDeclaration.get().getNameAsString();
            Optional<TypeDeclaration<?>> typeDeclaration = compilationUnit.getPrimaryType();
            if(!typeDeclaration.isPresent()){
                return;
            }
            String className = packageName+"."+typeDeclaration.get().getName();

            Class<?> c = loader(className);
//            c.getMethods()[0].getParameters();
//            System.out.println(typeDeclaration.get().getComment());
//            System.out.println(compilationUnit.getPrimaryType().get());
//            parseClassComment(typeDeclaration.get().getComment().orElse(null),c);

            // parse method
            typeDeclaration.get().getMethods().forEach(m->{
                parseMethodComment( m.getComment().orElse(null),m);
            });


//            typeDeclaration.get().getFields().forEach(a->{
//                parseClassComment(a.getComment().orElse(null),c);
//            });

        }



    }



}