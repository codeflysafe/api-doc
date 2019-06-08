package com.hsjfans.github.model;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Controller
@RequestMapping(value = "/123/",method = {RequestMethod.POST,RequestMethod.GET})
public class ApiTree implements Serializable {

    private static  final ConcurrentMap<Class<?>,ControllerClass> controllerClassMap
            = Maps.newConcurrentMap();

    /**
     *  insert the controller cl
     * @param cl
     */
    @PatchMapping(value ={ "/123","/456"})
    public void insert(Class<?> cl,ControllerClass controllerClass){
        controllerClassMap.putIfAbsent(cl,controllerClass);
    }


    /**
     * @name 测试get
     */
    @RequestMapping(value = "/789")
    public void get(){
        return;
    }


    /**
     * @name 测试set
     */
    @RequestMapping("/123-12312")
    public void set(){
        return;
    }

    public static ConcurrentMap<Class<?>, ControllerClass> getControllerClassMap() {
        return controllerClassMap;
    }
}
