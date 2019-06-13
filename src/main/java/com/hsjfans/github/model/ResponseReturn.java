package com.hsjfans.github.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sun.reflect.generics.tree.ReturnType;

import java.util.List;

/**
 *
 * response return is the return of the method
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseReturn {

    /**
     *  the return item
     */
    private List<ClassField> returnItem;

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


    /**
     *  the enum values
     */
    private Object[] enumValues;


    /**
     *  the return of method
     */
    private ReturnType returnType;



}
