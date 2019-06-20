package com.hsjfans.github.util;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.hsjfans.github.parser.AbstractParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {


    /**
     * 从 jar 内加载  类
     *
     * @param path jar path
     * @param name name
     * @return
     */
    private static void findFromJar(String path, String name) throws IOException {

        JarFile jarFile = new JarFile(path + name);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().endsWith(".jar")) {
//                findFromJar();
                System.out.println(jarEntry);
                findFromJar(path + name, jarEntry.getName());
            }
//            System.out.println(jarEntry.getName());
        }

    }


    /**
     * find all java source from project
     *
     * @param packageName the java source path name
     * @param recursion   scan recursion or not
     * @return
     */
    public static Set<File> scan(String packageName, boolean recursion) {
        Set<File> javaFiles = new HashSet<>();
        File file = new File(packageName);

        if (file.isDirectory()) {
            javaFiles.addAll(scanDir(file.getPath(), recursion));
        }
        return javaFiles;
    }

    /**
     * @param packageNames the package names
     * @param recursion    scan recursion or not
     * @return
     */
    public static Set<File> scan(Collection<String> packageNames, boolean recursion) {
        Set<File> javaFiles = new HashSet<>();
        for (String packageName : packageNames) {
            javaFiles.addAll(scan(packageName, recursion));
        }
        return javaFiles;
    }


    /**
     * 扫描项目
     *
     * @param filePath  filePath
     * @param recursion scan recursion or not
     */
    private static Set<File> scanDir(String filePath, boolean recursion) {
        Set<File> javaFiles = new HashSet<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (files == null) {
            return javaFiles;
        }
        for (File f : files) {
            if (f.isDirectory() && recursion) {
                javaFiles.addAll(scanDir(f.getPath(), recursion));
            } else {
                if (f.getName().endsWith(".java")) {
                    javaFiles.add(f);
                }
            }
        }
        return javaFiles;
    }


    /**
     * parse java source
     *
     * @param javaFile file
     * @return CompilationUnit
     * @see CompilationUnit
     */
    public static CompilationUnit parseJavaFile(File javaFile) {
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = StaticJavaParser.parse(javaFile);
        } catch (FileNotFoundException e) {
            LogUtil.warn(" parseJavaFile javaFile " + javaFile.getName() + " failed");
        }
        return compilationUnit;
    }


    public static boolean isFieldPrimitive(Field field) {
        return isPrimitive(field.getType());
    }

    public static boolean isParameterPrimitive(Parameter parameter) {
        return isPrimitive(parameter.getType());
    }


    public static boolean isPrimitive(Class<?> cl) {
        if (cl.isPrimitive()) {
            return true;
        }
        try {
            return ((Class) (cl.getField("TYPE").get(null))).isPrimitive();
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        return false;

    }


    public static Class<?> isCollection(Type t) {
        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            try {
                return AbstractParser.classLoader.loadClass(pt.getActualTypeArguments()[0].getTypeName());//得到对象list中实例的类型
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }


    public static boolean isTime(Class<?> c) {
        if (c.equals(LocalDateTime.class)) {
            return true;
        }
        if (c.equals(LocalDate.class)) {
            return true;
        }
        return false;
    }


    public static String[] methodSignature(Method method) {
        String[] strings = new String[method.getParameters().length];
        for (int i = 0; i < method.getParameters().length; i++) {
            strings[i] = method.getParameters()[i].getType().getSimpleName();
        }
        return strings;
    }


    public static Object[] getEnumValues(Class<?> cl) {
        if (cl.isEnum()) {
            Object[] enumValues = new Object[cl.getEnumConstants().length];
            for (int i = 0; i < enumValues.length; i++) {
                enumValues[i] = cl.getEnumConstants();
            }
            return enumValues;
        }
        return null;
    }


    // jar:file:/C:/Users/Jon/Test/foo.jar!/com/whatever/Foo.class
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException {

        findFromJar("/Volumes/doc/projects/java/api/build/libs/", "api-0.0.1-SNAPSHOT.jar");
    }


}