package com.hsjfans.github.util;

import java.util.regex.Pattern;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class StringUtil {

    public static String[] parseUrls(String value) {
      return   value.replaceAll("[\\{|\\|\"|\"}]+","").trim().split(",");
    }


    public static String join(String[] strings,String sep){

        StringBuilder builder = new StringBuilder(strings[0]);
        for(int i=1;i<strings.length;i++){
            builder.append(sep).append(strings[i]);
        }
        return builder.toString();
    }

    public static String enumToStrs(Object[] objects){
        if(objects==null||objects.length==0){return "";}
        String[] strings = new String[objects.length];
        int i=0;
        for (Object object:objects){
            if (object instanceof Enum){
                strings[i++] = ((Enum)object).name();
            }
        }
        return join(strings,",");
    }

    public static void main(String[] args) {
        for (String s:parseUrls("{ \"/123\", \"/456\" }")
             ) {
            System.out.println(s);
        }

    }


}

