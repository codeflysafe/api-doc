package com.hsjfans.github.model;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class ControllerClass implements Serializable {


    private Class<?> aClass;

    /**
     *  the class name `@name`
     */
    private String name;



    /**
     * the base url `@RequestMapping(value="/books")`
     */
    private String[] url = {""};


    private boolean ignore ;

    /**
     *  the methods
     */
    @Deprecated
    private RequestMethod[] methods;

    /**
     *
     */
    private List<ControllerMethod> controllerMethod;

    /**
     *  author
     */
    private String author = "";


    private String description;

    @Override
    public String toString() {
        return "ControllerClass{" +
                "aClass=" + aClass +
                ", name='" + name + '\'' +
                ", url=" + Arrays.toString(url) +
                ", ignore=" + ignore +
                ", methods=" + Arrays.toString(methods) +
                ", controllerMethod=" + controllerMethod +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
