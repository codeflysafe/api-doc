package com.hsjfans.api.demo.controllers;

import com.hsjfans.api.demo.models.Book;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    /**
     * @name 通过id查询数据
     * @param bookId the bookId
     * @return the book
     */
    @GetMapping(value = "/{bookId}")
    public Book queryBook(@PathVariable int bookId){
        return null;
    }


    /**
     * @name 获取全部书籍
     * @return list
     */
    @GetMapping
    public List<Book> queryBooks(){
        return null;
    }

}
