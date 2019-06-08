package com.hsjfans.github.config;

import lombok.Data;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class Config {

    /**
     *  package name
     */
    private String packageName;

    /**
     * the path to store the generated docs
     */
    private String outPath;

    /**
     *  the path of classes files
     */
    private String classPath;


    private boolean gradle;


    private String gradlePath;


    private String mvnPath;



}
