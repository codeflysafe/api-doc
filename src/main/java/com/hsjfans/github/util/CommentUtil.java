package com.hsjfans.github.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class CommentUtil {

    private static String BLOCK_TAG_PREFIX = "@";
    private static Pattern BLOCK_PATTERN = Pattern.compile("^\\s*" + BLOCK_TAG_PREFIX, Pattern.MULTILINE);

    /**
     *  解析
     * @param comment comment
     * @return
     */
    public static List<Map<String,String>> parseParam(Comment comment){

        Javadoc javadoc = comment.parse();


        return null;

    }


    public static void main(String[] args) {

        String tagBlock = "  /**\n" +
                "     *  parse the method  comment to Param\n" +
                "     * @param comment {@Ignore}\n" +
                "     * @param cl {@Ignore}\n" +
                "     * @return\n" +
                "     */";
        Javadoc javadoc = StaticJavaParser.parseJavadoc(tagBlock);
        System.out.println(javadoc.getBlockTags());
    }

}
