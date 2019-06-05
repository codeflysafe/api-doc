package com.hsjfans.github.util;


import com.hsjfans.github.annotation.Api;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ClassUtils {

    private static final ClassLoader loader = ClassUtils.class.getClassLoader();

    /**
     *
     * @param packageName the package name
     * @param recursion scan recursion or not
     * @return
     */
	public static Set<String> scan(String packageName,boolean recursion){
        Set<String> classNames = new HashSet<>();
	    URL url = loader.getResource(packageName.replace(".","/"));
	    if(url!=null&&url.getProtocol().equals("file")){
            classNames.addAll(scanDir(url.getPath(),packageName,recursion));
        }
	    return classNames;
    }

    /**
     *
     * @param packageNames the package names
     * @param recursion scan recursion or not
     * @return
     */
    public static Set<String> scan(Collection<String> packageNames, boolean recursion){
        Set<String> classNames = new HashSet<>();
	    for(String packageName:packageNames){
	        classNames.addAll(scan(packageName,recursion));
        }
	    return classNames;
    }


    /**
     *
     * @param filePath the filePath
     * @param packageName the packageName
     * @param recursion scan recursion or not
     */
    private static Set<String> scanDir(String filePath,String packageName,boolean recursion){
        Set<String> classNames = new HashSet<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if(files==null){return classNames;}
        for(File f:files){
            if(f.isDirectory()&&recursion){
                classNames.addAll(scanDir(f.getPath(),packageName+"."+f.getName(),recursion));
            }else {
                if(f.getName().endsWith(".class")){
                    classNames.add(packageName+"."+f.getName().replace(".class",""));
                }
            }
        }
        return classNames;
    }


    public static void main(String[] args) throws ClassNotFoundException {
        for(String className:scan("com.hsjfans.github.annotation",false)){
            Class<?> cl = loader.loadClass(className);
            System.out.println(cl.getSimpleName());
        }
    }





}