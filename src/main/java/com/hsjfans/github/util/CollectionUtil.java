package com.hsjfans.github.util;

import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hsjfans.github.model.RequestMethod;
import java.lang.annotation.Annotation;
import java.util.*;

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


    public static Map<String,Annotation> convertToMap(Annotation[] annotations){
        Map<String,Annotation> annotationMap = Maps.newConcurrentMap();
        for(Annotation annotation:annotations){
           annotationMap.put(annotation.annotationType().getName(),annotation);
        }
        return annotationMap;
    }


    public static String requestMethodsToString(RequestMethod[] requestMethods){
        StringBuilder builder = new StringBuilder();
        Arrays.stream(requestMethods).forEach(requestMethod->{
            builder.append(requestMethod.name());
        });
        return builder.toString();
    }


    public static Optional<JavadocBlockTag> contains(List<JavadocBlockTag> javadocBlockTags, JavadocBlockTag.Type type){
        for (JavadocBlockTag docTag:javadocBlockTags
             ) {
            if(docTag.is(type)){
                return Optional.of(docTag);
            }
        }
        return Optional.empty();
    }
}
