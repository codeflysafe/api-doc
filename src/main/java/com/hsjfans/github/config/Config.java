package com.hsjfans.github.config;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class Config {

    /**
     *  package name
     */
    private String packageName;

    /**
     * the path to store the generated docs
     */
    private String outPath;


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }
}
