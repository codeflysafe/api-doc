package com.hsjfans.github.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 *
 * the controller class of the project
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControllerClass implements Serializable {


    /**
     *  the true class
     */
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
    private RequestMethod[] methods;

    /**
     * the method list of this class
     */
    private List<ControllerMethod> controllerMethod;

    /**
     *  author
     */
    private String author = "";


    /**
     *  the description of this class
     */
    private String description;


    public static ControllerClass of(Class<?> aClass){
        ControllerClass controllerClass = new ControllerClass();
        controllerClass.setName(aClass.getSimpleName());
        controllerClass.setAClass(aClass);
        return controllerClass;
    }

    public void fulfillRequestMapping(RequestMapping requestMapping){
        this.url = requestMapping.getValue();
        this.methods = requestMapping.getMethods();
        return;
    }

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
