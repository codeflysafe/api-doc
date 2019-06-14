package com.hsjfans.github.util;

import com.hsjfans.github.config.Config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class GradleClassLoader  extends ApiClassLoader {


    public GradleClassLoader(Config config) {
        super(config);
    }

    @Override
    protected byte[] loadByte(String name) throws Exception {

        name = name.replaceAll("\\.", "/");
        Path path = Paths.get(projectPath + "/" + name
                + ".class");
        if(Files.exists(path)){
            return   Files.readAllBytes(path);
        }

        return loadByteFromJar(name);
    }


    private byte[] loadByteFromJar(String name) throws Exception{


        return null;

    }

}
