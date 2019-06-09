package com.hsjfans.github.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
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

}
