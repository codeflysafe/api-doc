package com.hsjfans.api.demo.controllers;

import com.hsjfans.api.demo.models.Book;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class BookControllerTest {


    @Test
    public void createBoolTest(){


        Method method = BookController.class.getMethods()[2];

       if(Collection.class.isAssignableFrom(method.getReturnType())){

           System.out.println(method.getGenericReturnType());


       }
    }

}
