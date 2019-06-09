package com.hsjfans.github.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Sets;
import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.ApiTree;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.util.ApiClassLoader;
import com.hsjfans.github.util.ClassUtils;
import com.hsjfans.github.util.LogUtil;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */

public abstract class AbstractParser implements Parser  {

    protected final ClassLoader classLoader  ;

    protected final Config config;

    private final ApiTree apiTree;

    public AbstractParser(Config config){
        this.config = config;
        this.classLoader = new ApiClassLoader(config);
        this.apiTree = new ApiTree();
    }


    protected abstract Set<String> supportClassAnnotations();

    protected abstract Set<CompilationUnit> getAllControllerClass(Set<File> javaFiles);

    protected abstract void parseCompilationUnit(CompilationUnit compilationUnit, Set<ControllerClass> controllerClasses);


    @Override
    public ApiTree parse(String projectPath, boolean recursive) throws ParserException {
        LogUtil.info("开始解析 projectPath# "+projectPath);
        Set<File> javaFiles =  ClassUtils.scan(projectPath,true);
        Set<CompilationUnit> compilationUnits =  getAllControllerClass(javaFiles);
        Set<ControllerClass> controllerClasses = Sets.newHashSet();
        compilationUnits.forEach(compilationUnit -> parseCompilationUnit(compilationUnit,controllerClasses));
        this.apiTree.insertAll(controllerClasses);
        return apiTree;
    }


    @Override
    public ApiTree parse(List<String> projectPaths, boolean recursive) throws ParserException {
        projectPaths.forEach(path->{
            try {
                this.apiTree.union(this.parse(path,recursive));
            } catch (ParserException e) {
                e.printStackTrace();
            }
        });
        return this.apiTree;
    }


}
