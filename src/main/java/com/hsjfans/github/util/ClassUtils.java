package com.hsjfans.github.util;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.hsjfans.github.model.Param;
import com.hsjfans.github.model.Return;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * @param filePath | @ignore | the filePath
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
     * @param parameters
     * @return
     */
    public static Param parseMethodComment(Comment comment, NodeList<Parameter> parameters){

        //  todo
        return null;
    }


    public static Return parseMethodReturn(){
        //  todo
        return null;
    }


    /**
     *  parse the method  comment to Param
     * @param comment
     * @return
     */
    public static Param parseFieldComment(Comment comment){

        //  todo
        return null;
    }

    /**
     *  parse the method  comment to Param
     * @param comment
     * @return
     */
    public static Param parseClassComment(Comment comment){

       //  todo
        return null;
    }


    public static Class<?> loader(String packageName) throws ClassNotFoundException {
        return loader.loadClass(packageName);
    }



    public static void main(String[] args) throws ClassNotFoundException {
        for(File file:scan("/Volumes/doc/projects/java/api",true)){
            CompilationUnit compilationUnit = parseJavaFile(file);
            System.out.println( compilationUnit.getPackageDeclaration().get().getName());
            if(compilationUnit.getPrimaryType().isPresent()){
               System.out.println(compilationUnit.getPrimaryType().get().getName());
//                compilationUnit.getPrimaryType().get().getMethods().forEach(a->{
//                   if(a.getComment().isPresent()){
//                      Comment comment = a.getComment().get();
////                      System.out.println(comment.getContent());
////                      System.out.println(a.getParameters());
//                      if(a.getType().isClassOrInterfaceType()){
//                          System.out.println( a.getType().asClassOrInterfaceType().removeScope());
//                      }
//
//                   }
//                });
            }
        }



    }



}