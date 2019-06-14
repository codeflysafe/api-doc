package com.hsjfans.github.util;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class JavaDocUtil {


    /**
     * 这里同时将 doc 加入到缓存中
     * <p>
     * 判断是否含有 `@ignore` 注释
     *
     * @param typeDeclaration typeDeclaration
     * @return true
     */
    public static boolean isIgnore(TypeDeclaration typeDeclaration) {

        Javadoc javadoc = (Javadoc) typeDeclaration.getJavadoc().orElse(null);
        if (javadoc == null) {
            return false;
        }

        return javadoc.getBlockTags().stream().anyMatch(javadocBlockTag -> javadocBlockTag.is(JavadocBlockTag.Type.IGNORE));
    }

}
