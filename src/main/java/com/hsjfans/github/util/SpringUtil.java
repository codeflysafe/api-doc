package com.hsjfans.github.util;

import com.google.common.collect.Lists;
import com.hsjfans.github.model.RequestMapping;
import com.hsjfans.github.model.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class SpringUtil {



    private static final List<String> SPRING_CONTROLLERS = Lists.newArrayList("Controller","RestController");


    /**
     *  * @see GetMapping
     *  * @see PutMapping
     *  * @see DeleteMapping
     *  * @see PatchMapping
     *  * @see RequestMapping
     */
    private static final List<String> SUPPORT_REQUEST_MAPPING = Lists.newArrayList(
            "PostMapping,GetMapping,DeleteMapping,PatchMapping,PutMapping"
    );


    private static final String REQUEST_MAPPING = "RequestMapping";

    public static boolean isControllerClass(Annotation[] annotations){

        for (Annotation a:annotations
             ) {
            if(SPRING_CONTROLLERS.contains(a.annotationType().getSimpleName())){
                return true;
            }
        }
        return false;
    }



    public static RequestMapping parseRequestMapping(Annotation annotation){

        RequestMapping requestMapping = new RequestMapping();
        String name = annotation.annotationType().getSimpleName();
        try {
            if(name.equals(REQUEST_MAPPING)){
                Method method = annotation.getClass().getMethod("method");
                Object requestMethods = method.invoke(annotation);
                if(requestMethods.getClass().isArray()){
                    RequestMethod[] ms = new RequestMethod[((Object[]) requestMethods).length];
                    for (int i = 0; i <ms.length ; i++) {
                        ms[i] = RequestMethod.valueOf(((Enum)(((Object[]) requestMethods)[i])).name());
                    }
                    requestMapping.setMethods(ms);
                }
            }

            if(SUPPORT_REQUEST_MAPPING.contains(name)||name.equals(REQUEST_MAPPING)){

                Method valueMethod = annotation.getClass().getMethod("value");
                String[] values = (String[]) valueMethod.invoke(annotation);
                if(values!=null){
                    requestMapping.setValue(values);
                }
                Method pathMethod = annotation.getClass().getMethod("path");
                String[] paths = (String[]) pathMethod.invoke(annotation);
                if(paths.length>0){
                    requestMapping.setValue(paths);
                }
            }



        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // nothing to do
        }

        System.out.println(requestMapping);

        return requestMapping;

    }


    public static boolean isSpringRequestAnnotation(Annotation annotation){
        String name = annotation.annotationType().getSimpleName().trim();
//        System.out.println(name+"  "+SUPPORT_REQUEST_MAPPING);
//        return name.equals(REQUEST_MAPPING)||SUPPORT_REQUEST_MAPPING.contains(name);
        return name.equals("GetMapping");
    }


    public static boolean isSpringMethods(Method method){
        System.out.println(" method is"+method.getName());
        boolean s = Arrays.stream(method.getAnnotations()).anyMatch(SpringUtil::isSpringRequestAnnotation);
        System.out.println(s);
        return s;
    }


}

