package com.hsjfans.github.model;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class ApiTree implements Serializable {

    private static  final ConcurrentMap<Class<?>,ControllerClass> controllerClassMap
            = Maps.newConcurrentMap();

    /**
     *  insert the controller cl
     * @param cl
     */
    public void insert(Class<?> cl,ControllerClass controllerClass){
        controllerClassMap.putIfAbsent(cl,controllerClass);
    }

    public static ConcurrentMap<Class<?>, ControllerClass> getControllerClassMap() {
        return controllerClassMap;
    }
}
