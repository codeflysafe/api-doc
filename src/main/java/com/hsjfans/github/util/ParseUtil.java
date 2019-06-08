package com.hsjfans.github.util;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class ParseUtil {

    private static String BLOCK_TAG_PREFIX = "@";
    private static Pattern BLOCK_PATTERN = Pattern.compile("^\\s*" + BLOCK_TAG_PREFIX, Pattern.MULTILINE);


    public static void main(String[] args) {

        String tagBlock = "// A simple line of text";
        List<String>  blockLines = BLOCK_PATTERN
                .splitAsStream(tagBlock)
                .filter(s1 -> !s1.isEmpty())
                .map(s -> BLOCK_TAG_PREFIX + s)
                .collect(Collectors.toList());

        System.out.println(blockLines);
    }

}
