package com.hsjfans.github.util;

import com.google.common.collect.Maps;
import com.hsjfans.github.config.Config;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
    protected String projectPath;

    /**
     *  加载 jar 包内
     */
    protected Map<String,byte[]> jarMap;

    protected Config config;

    public ApiClassLoader(String projectPath){
        this.projectPath = projectPath;
        jarMap = Maps.newHashMap();
    }

    public ApiClassLoader(Config config){
        this(config.getClassPath());
        this.config = config;
        if(config.getClassPath()==null){
            this.projectPath= config.getPackageName()+"/out/production/classes";
        }
    }



    protected byte[] loadByte(String name) throws Exception {
        name = name.replaceAll("\\.", "/");
        Path path = Paths.get(projectPath + "/" + name
                + ".class");
        return  Files.readAllBytes(path);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = loadByte(name);
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            LogUtil.error(" e ={} ",e);
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
