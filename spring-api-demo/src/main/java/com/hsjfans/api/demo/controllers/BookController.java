package com.hsjfans.api.demo.controllers;

import com.hsjfans.api.demo.models.Book;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @name 书籍相关接口
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@RestController
@RequestMapping(value = "/books")
public class BookController {


    /**
     * @name 创建书籍
     * @param book book
     * @return new book
     */
    @PostMapping(value = "")
    public Book createBook(Book book){
        return null;
    }

}
