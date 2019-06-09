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
     *
     * default is src/static/
     */
    private String outPath = "src/static/";

    /**
     *  the path of classes files
     */
    private String classPath;


    private boolean gradle;


    private String gradlePath;


    private String mvnPath;


    /**
     *  api doc name  -  xxx 接口文档
     */
    private String docName;



}
