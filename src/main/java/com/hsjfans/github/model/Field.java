package com.hsjfans.github.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 * the return class's field
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class Field implements Serializable {


    /**
     *  名称 book
     */
    private String name;


    /**
     *  类型 string
     *
     *  枚举默认取其 name()
     *
     */
    private String type;


    /**
     *  说明
     */
    private String comment;


    /**
     *  枚举类型的取值范围
     */
    private String[] enumStrValues;


    /**
     *  内嵌的返回的结构体
     */
    private ResponseEntity responseEntity;


    /**
     *
     * 内嵌的返回结构体
     *
     */
    private List<ResponseEntity> responseEntities;



}
