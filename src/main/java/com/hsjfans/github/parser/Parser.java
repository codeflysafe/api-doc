package com.hsjfans.github.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.hsjfans.github.model.ApiTree;

import java.util.List;

/**
 *
 * 解析器
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public interface Parser {

   /**
    *
    * parse the project
    *
    *
    * @param projectPath project path
    * @param recursive recursive scan or not
    * @throws ParserException
    */
   ApiTree parse(String projectPath, boolean recursive) throws ParserException;


   /**
    *
    *
    * parse the multi-project
    *
    * @param projectPaths multi-project path
    * @param recursive recursive scan or not
    * @throws ParserException
    */
   ApiTree parse(List<String> projectPaths,boolean recursive) throws ParserException;



}
