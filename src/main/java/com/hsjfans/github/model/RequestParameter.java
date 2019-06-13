package com.hsjfans.github.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 *
 * the request params
 *
 * just like `@param`
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestParameter implements Serializable {


    /**
     *  the parameter of related method
     */
    private Parameter parameter;

    /**
     *  参数名称
     */
    private String name;


    /**
     *  参数描述
     */
    private String description;


    /**
     *  参数类型
     */
    private String typeName;


    /**
     *  是否支持模糊搜索
     */
    private boolean fuzzy;


    /**
     *  是否忽略此参数(即请求一定不包含此参数)
     */
    private boolean ignore;


    /**
     *  是否为必须的参数
     */
    private boolean nullable;

    /**
     *  如果是复杂对象会存在一个列表
     * @see ClassField
     */
    private List<ClassField> fields;



    /**
     *  the enum values
     */
    private Object[] enumValues;


}
