package com.hsjfans.api.demo.controllers;

import com.hsjfans.api.demo.models.Book;
import org.junit.Test;

import java.lang.reflect.Parameter;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class BookControllerTest {


    @Test
    public void createBoolTest(){


        Parameter parameter = BookController.class.getMethods()[0].getParameters()[0];

        System.out.println(parameter);

    }

}
