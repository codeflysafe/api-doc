package com.hsjfans.github.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class RequestMapping implements Serializable {

    /**
     *  the url path or value
     */
    private String[] value;


    /**
     *  the request methods
     */
    private RequestMethod[] methods;

}
