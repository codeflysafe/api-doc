package com.hsjfans.github.model;

import com.hsjfans.github.util.Constant;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * the api Info
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Data
public class Api implements Serializable {

    /**
     *  api 名称
     */
    private String title;

    /**
     * api method
     */
    private Constant.RequestMethod method;


    /**
     *
     * the api url
     *
     */
    private String url;


    /**
     *  返回数据
     */
    private ResponseEntity responseEntity;


    /**
     *  请求参数
     */
    private RequestEntity requestEntity;




}
