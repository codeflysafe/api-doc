package com.hsjfans.github.model;

import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

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
     *  the parameters of this method
     */
    private List<RequestParameter> requestParameters;


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


    /**
     *  the description of this method
     */
    private String description;



    public void fulfillReqestMapping(RequestMapping requestMapping){
        this.url = requestMapping.getValue();
        this.methods = requestMapping.getMethods();
    }


}
