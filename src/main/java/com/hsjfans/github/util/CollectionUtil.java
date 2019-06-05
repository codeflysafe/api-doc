package com.hsjfans.github.util;

import com.google.common.collect.Sets;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class CollectionUtil {

    /**
     *
     * @param annotations annotation
     * @return
     */
    public static Set<String> annotationsToSet(Annotation[] annotations){
        Set<String> strings = Sets.newHashSet();
        for (Annotation a :annotations
                ) {
            strings.add(a.annotationType().getSimpleName());
        }
        return strings;
    }


    public static boolean contain(Annotation[] annotations,Class<?> a){
        for(Annotation annotation:annotations){
            if(annotation.getClass().equals(a)){
                return true;
            }
        }
        return false;
    }
}