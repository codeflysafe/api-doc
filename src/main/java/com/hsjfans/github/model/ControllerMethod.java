package com.hsjfans.github.model;

import com.google.common.collect.Lists;
import com.hsjfans.github.util.StringUtil;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class ControllerMethod implements Serializable {


    /**
     *  is true will be ignored
     */
    private boolean ignore;

    /**
     *
     *  the method url
     */
    private String[] url =new String[]{""};

    /**
     *  the method name `@name`
     **/
    private String name;


    /**
     *  the method
     */
    private Method method;


    /**
     *  the class
     */
    private Class<?> aClass;

    /**
     *  the args
     */
    private List<RequestParam> params;


    /**
     *  the responseReturn
     */
    private ResponseReturn responseReturn;


    /**
     *  the request that api support
     */
    private RequestMethod[] methods;

    /**
     *  author
     */
    private String author;

    private String description;



}
