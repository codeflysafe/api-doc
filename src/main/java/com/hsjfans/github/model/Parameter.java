package com.hsjfans.github.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class Parameter implements Serializable {

    /**
     *  参数名称
     */
    private String name;


    /**
     *
     * 参数类型
     *
     */
    private String type;


    /**
     *  是否必须 默认为 false
     */
    private boolean fuzzy;


    /**
     *  说明
     */
    private String comment;



}
