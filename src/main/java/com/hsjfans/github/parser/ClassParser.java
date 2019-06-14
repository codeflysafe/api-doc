package com.hsjfans.github.parser;


import com.hsjfans.github.model.ClassField;

import java.util.List;

/**
 * 解析 filed
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public interface ClassParser {


    /**
     * 解析
     *
     * @param cl cl
     * @return
     */
    List<ClassField> parseClassFiled(Class<?> cl);

}

