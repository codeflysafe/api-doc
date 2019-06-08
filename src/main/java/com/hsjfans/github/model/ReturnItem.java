package com.hsjfans.github.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class ReturnItem implements Serializable {

    /**
     *  名称
     */
    private String name;

    /**
     *  类型
     */
    private String type;


    /**
     *  描述
     */
    private String description;

}
