package com.hsjfans.github.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * An atomic class to mapped the params who are the fields of an entity ( contains the warp ) or
 *
 * the primitive type and etc.
 *
 * Just like an tree
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestParam implements Serializable {


    /**
     *  the name of request param
     */
    private String name;


    /**
     *  the regex pattern `@regex '^1{3,4,5,6,7,8}[0-9]{9}$'`
     */
    private String regex;


    /**
     *  the type of param
     */
    private String type;


    /**
     *  contain "@fuzzy"
     *
     *  default false
     */
    private boolean fuzzy = false;


    /**
     *  is necessary or not
     */
    private boolean necessary = true;



    /**
     *  the description
     */
    private String description;


    /**
     *  Collection  class
     */
    private RequestParam[] params;



}
