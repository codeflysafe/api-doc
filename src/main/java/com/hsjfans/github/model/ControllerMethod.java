package com.hsjfans.github.model;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class ControllerMethod implements Serializable {


    /**
     *  is true will be ignored
     */
    private boolean ignore;

    /**
     *
     *  the method url
     */
    private String url;

    /**
     *  the method name `@name`
     **/
    private String name;


    /**
     *  the method
     */
    private Method method;

    /**
     *  the description
     */
    private String description;

    /**
     *  the class
     */
    private Class<?> aClass;

    /**
     *  the args
     */
    private RequestParam[] params;


    /**
     *  the responseReturn
     */
    private ResponseReturn responseReturn;


    /**
     *  the request that api support
     */
    private Constant.RequestMethod[] methods;


}
