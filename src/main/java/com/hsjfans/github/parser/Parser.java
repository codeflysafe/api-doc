package com.hsjfans.github.parser;

/**
 *
 * 解析器
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public interface Parser {

   void parser(Class<?> cl) throws ParserException;

}
