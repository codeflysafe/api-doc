package com.hsjfans.github.util;


import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ClassUtils {

    private static final ClassLoader loader = ClassUtils.class.getClassLoader();

    /**
     *
     * @param packageName the package name
     * @return
     */
	public static Set<String> scan(String packageName){
        Set<String> classNames = new HashSet<>();
	    URL url = loader.getResource(packageName.replace(".","/"));
	    if(url!=null&&url.getProtocol().equals("file")){
            classNames.addAll(scanDir(url.getPath(),packageName));
        }
	    return classNames;
    }


    /**
     *
     * @param filePath the filePath
     * @param packageName the packageName
     */
    private static Set<String> scanDir(String filePath,String packageName){
        Set<String> classNames = new HashSet<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if(files==null){return classNames;}
        for(File f:files){
            if(f.isDirectory()){
                classNames.addAll(scanDir(f.getPath(),packageName+"."+f.getName()));
            }else {
                if(f.getName().endsWith(".class")){
                    classNames.add(packageName+"."+f.getName().replace(".class",""));
                }
            }
        }
        return classNames;
    }


    public static void main(String[] args) throws ClassNotFoundException {
        for(String className:scan("com.hsjfans.github")){
            Class<?> cl = loader.loadClass(className);
            System.out.println(cl.getName());
        }
    }
}