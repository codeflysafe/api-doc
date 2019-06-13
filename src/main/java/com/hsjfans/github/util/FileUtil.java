package com.hsjfans.github.util;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class FileUtil {

    private static final Map<String,String> fileCache = new HashMap<>();


    public static String from(String path){
        if(fileCache.containsKey(path)){
            return fileCache.get(path);
        }
        File file = new File(path);
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String str;
            while ((str=reader.readLine())!=null){
                builder.append(str).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileCache.put(path,builder.toString());
        return builder.toString();
    }


    public static void to(String path,String content)  {

        FileWriter fileWritter = null;
        try {
            File file = new File(path);
            if(file.isDirectory()){
                file.mkdirs();
            }
            fileWritter = new FileWriter(path,false);
            fileWritter.write(content);
            fileWritter.flush();
            fileWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static boolean filterTest(String suffix,File file){
        return file.getPath().startsWith(suffix+"/src/test/");
    }

}

