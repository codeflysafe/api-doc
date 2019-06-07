package com.hsjfans.github.util;

import java.util.regex.Pattern;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class StringUtil {

    public static String[] parseUrls(String value) {
      return   value.replaceAll("[\\{|\\|\"|\"}]+","").trim().split(",");
    }

    public static void main(String[] args) {
        for (String s:parseUrls("{ \"/123\", \"/456\" }")
             ) {
            System.out.println(s);
        }

    }


}

