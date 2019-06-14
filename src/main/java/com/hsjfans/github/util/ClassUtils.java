package com.hsjfans.github.util;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.hsjfans.github.parser.AbstractParser;

import java.io.*;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ClassUtils {



    /**
     * 从 jar 内加载  类
     * @param name name
     * @return
     */
    private static void findFromJar(String name){



    }


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



    public static boolean isFieldPrimitive(Field field){
        return isPrimitive(field.getType());
    }

    public static boolean isParameterPrimitive(Parameter parameter){
        return isPrimitive(parameter.getType());
    }


    public static boolean isPrimitive(Class<?> cl){
        if(cl.isPrimitive()){
            return true;
        }
        try {
            return  ((Class)(cl.getField("TYPE").get(null))).isPrimitive();
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        return false;

    }


    public static Class<?> isCollection(Type t){
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



    public static boolean isTime(Class<?> c){
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


    public static Object[] getEnumValues(Class<?> cl){
        if(cl.isEnum()){
            Object[] enumValues = new Object[cl.getEnumConstants().length];
            for (int i = 0; i <enumValues.length ; i++) {
                enumValues[i] = cl.getEnumConstants();
            }
            return enumValues;
        }
        return null;
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String path = "file:/Volumes/doc/projects/java/java-api-doc/build/libs/java-api-doc-1.0-SNAPSHOT.jar";
        URL url = new URL(path);
        System.out.println(url);
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url},Thread.currentThread().getContextClassLoader());

        InputStream inputStream = urlClassLoader.getResourceAsStream("com.hsjfans.github.parser.ClassFieldParser");

        System.out.println(inputStream);

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }

        System.out.println(buffer.toString());

    }




}