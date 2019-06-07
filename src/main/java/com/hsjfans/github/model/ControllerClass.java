package com.hsjfans.github.model;

import java.io.Serializable;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class ControllerClass implements Serializable {

    /**
     *  the class name `@name`
     */
    private String name;

    /**
     *  the description
     */
    private String description;

    /**
     * the base url `@RequestMapping(value="/books")`
     */
    private String url;


    /**
     *  the methods
     */
    @Deprecated
    private Constant.RequestMethod[] methods;


}
