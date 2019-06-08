package com.hsjfans.github.util;

import com.hsjfans.github.config.Config;

import java.io.FileInputStream;

/**
 *
 * api 文档的类加载器
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class ApiClassLoader extends ClassLoader {

    /**
     *  the dir path of class
     *  default is project+ /out/production/classes
     */
    private String projectPath;

    public ApiClassLoader(Config config){
        this.projectPath= config.getPackageName()+"/out/production/classes";
    }

    public ApiClassLoader(String projectPath){
        this.projectPath = projectPath;
    }

    private byte[] loadByte(String name) throws Exception {
        name = name.replaceAll("\\.", "/");
        FileInputStream fis = new FileInputStream(projectPath + "/" + name
                + ".class");
        int len = fis.available();
        byte[] data = new byte[len];
        fis.read(data);
        fis.close();
        return data;

    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = loadByte(name);
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
    }


    public static void main(String[] args) throws ClassNotFoundException {
        Config config = new Config();
        config.setPackageName("/Volumes/doc/projects/java/api");
        ApiClassLoader apiClassLoader = new ApiClassLoader(config);
        Class c = apiClassLoader.loadClass("com.autozooo.api.application.autoshow.AutoShowApplicationService");
        System.out.println(c.getName());
        System.out.println(c.getSimpleName());
    }




}
