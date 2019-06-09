package com.hsjfans.github.model;

import lombok.Data;

import java.util.List;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class ResponseReturn {

    /**
     *  the return item
     */
    private List<RequestParam> returnItem;

    /**
     *  the description
     */
    private String description;


    /**
     *  the name
     */
    private String name;

    /**
     *  the type
     */
    private String type;



}
