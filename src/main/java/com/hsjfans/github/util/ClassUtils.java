package com.hsjfans.github.util;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.model.Param;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    public static Param parseMethodComment(Comment comment, Method method){

        //  todo
        return null;
    }


    /**
     *  parse the method  comment to Param
     * @param comment
     * @param returnType
     * @return
     */
    public static Return parseMethodReturn(Comment comment,Class<?> returnType){
        //  todo
        return null;
    }


    /**
     *  parse the method  comment to Param
     * @param comment comment {@ignore}
     * @param field filed
     * @return
     */
    public static Param parseFieldComment(Comment comment, Field field){

        //  todo
        return null;
    }

    /**
     * @name parse the method  comment to Param
     * @param comment @Ignore
     * @param cl @Ignore
     * @return
     */
    public static ControllerClass parseClassComment(Comment comment, Class<?> cl){
        if(comment==null){return null;}
        Javadoc javadoc = comment.parse();
        javadoc.getBlockTags().forEach(
                a->{
                    if(!a.getTagName().equals(JavadocBlockTag.Type.IGNORE
                    &&a.getTagName().equals())){

                    }
                }
        );

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
        String tets2Path = "/Volumes/doc/projects/java/java-api-doc/src/main/java/com/hsjfans/github/model";
        for(File file:scan(tets2Path,true)){
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
//            parseClassComment(typeDeclaration.get().getComment().orElse(null),null);

            // parse method
//            typeDeclaration.get().getMethods().forEach(m->{
//               parseClassComment( m.getComment().orElse(null),null);
//            });


            typeDeclaration.get().getFields().forEach(a->{
                parseClassComment(a.getComment().orElse(null),null);
            });

        }



    }



}