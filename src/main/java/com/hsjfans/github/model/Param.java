package com.hsjfans.github.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class Param {


    /**
     *  the name of request param
     */
    private String name;

    /**
     *  contain "@ignore"
     *
     *  necessary param or not default necessary
     */
    private boolean ignore = true;


    /**
     *  contain "@fuzzy"
     *
     *  default false
     */
    private boolean fuzzy = false;



    /**
     *  the description
     */
    private String description;


    /**
     *  the support request methods
     */
    private Constant.RequestMethod[] requestMethods;


    /**
     *  Collection  class
     */
    private Param[] params;



}
