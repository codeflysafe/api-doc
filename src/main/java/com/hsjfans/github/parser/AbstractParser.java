package com.hsjfans.github.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.hsjfans.github.model.ApiTree;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.util.ClassUtils;
import com.hsjfans.github.util.LogUtil;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */

public abstract class AbstractParser implements Parser  {


    protected abstract Set<String> supportClassAnnotations();

    protected abstract Set<CompilationUnit> getAllControllerClass(Set<File> javaFiles);

    protected abstract ControllerClass parseCompilationUnit(CompilationUnit compilationUnit);


    @Override
    public ApiTree parse(String projectPath, boolean recursive) throws ParserException {
        LogUtil.info("开始解析 projectPath# "+projectPath);
        Set<File> javaFiles =  ClassUtils.scan(projectPath,true);
        Set<CompilationUnit> compilationUnits =  getAllControllerClass(javaFiles);
        compilationUnits.forEach(this::parseCompilationUnit);
        return null;
    }


    @Override
    public ApiTree parse(List<String> projectPaths, boolean recursive) throws ParserException {
       // todo
        return null;
    }


}
