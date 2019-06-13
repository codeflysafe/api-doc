package com.hsjfans.api.demo.models;

import java.io.Serializable;

/**
 * @name 书籍
 * 书籍实体
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class Book implements Serializable {

    /**
     * @nullable 忽略
     */
    private String name;

    // isbn 号
    private String isbn;

    // 价格
    private int price;

}
