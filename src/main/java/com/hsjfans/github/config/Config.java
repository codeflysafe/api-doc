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


    /**
     *  the repository of gradle
     */
    private String gradlePath;


    /**
     *  the repository of maven
     */
    private String mvnPath;


    /**
     *  api doc name  -  xxx 接口文档
     */
    private String docName;


    private String apiName;


    public String getOutPath(){
        return this.outPath+this.apiName+"/";
    }



}
