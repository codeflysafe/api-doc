package com.hsjfans.github.util;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.regex.Pattern;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class ParseUtil {

    private static String BLOCK_TAG_PREFIX = "@";
    private static Pattern BLOCK_PATTERN = Pattern.compile("^\\s*" + BLOCK_TAG_PREFIX, Pattern.MULTILINE);


    public static int getParameterIndexViaJavaDocTagName(String tagName, MethodDeclaration methodDeclaration) {

        for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
            if (methodDeclaration.getParameter(i).getNameAsString().equals(tagName)) {
                return i;
            }
        }

        return -1;

    }


}
