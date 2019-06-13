package com.hsjfans.github.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.ApiTree;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.model.ControllerMethod;
import com.hsjfans.github.util.ApiClassLoader;
import com.hsjfans.github.util.ClassUtils;
import com.hsjfans.github.util.LogUtil;
import com.hsjfans.github.util.SpringUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */

public abstract class AbstractParser implements Parser  {

    public static ClassLoader classLoader  ;

    protected final Config config;

    private final ApiTree apiTree;

    public AbstractParser(Config config){
        this.config = config;
        classLoader = new ApiClassLoader(config);
        this.apiTree = new ApiTree();
    }

    protected abstract Set<String> supportClassAnnotations();


    protected abstract void parseControllerClassDoc(Class<?> cl, ControllerClass controllerClass);


    /**
     *  解析单个 controllerClass 文件
     * @param cl not null
     * @return the controllerMethod
     * @see ControllerClass
     */
    protected ControllerClass parseControllerClass(Class<?> cl){

        ControllerClass controllerClass =ControllerClass.of(cl);

        //第一步 解析 controller 注释
        parseControllerClassDoc(cl,controllerClass);

        //第二步 解析 设置 method
        controllerClass.setControllerMethod(parseControllerMethods(cl));




    }


    /**
     *  解析 class 的 methods
     * @param cl not null
     * @return ControllerMethods
     */
    protected  List<ControllerMethod> parseControllerMethods(Class<?> cl){

        Method[] methods = cl.getMethods();
        TypeDeclaration<?> typeDeclaration = ClassCache.getTypeDeclaration(cl.getName());
        List<ControllerMethod> controllerMethods = Lists.newArrayListWithCapacity(methods.length);
        // just public method
        // and has PostMapping GetMapping ...
        Arrays.stream(methods).filter(SpringUtil::isSpringMethods).forEach(method -> {
            List<MethodDeclaration> methodDeclarations = typeDeclaration.getMethodsBySignature(method.getName(),ClassUtils.methodSignature(method));
            if(methodDeclarations.size()>0){
                Optional.ofNullable(parseControllerMethod(methodDeclarations.get(0),method)).ifPresent(controllerMethods::add);
            }
        });
        return controllerMethods;
    }


    protected  abstract ControllerMethod parseControllerMethod(MethodDeclaration methodDeclaration, Method method);


    /**
     *  过滤 java 文件
     */
    protected Set<Class<?>> parseJavaFiles(Set<File> javaFiles){

        final Set<Class<?>> controllerClasses = Sets.newHashSet();
        javaFiles.forEach(file->{
            Optional.ofNullable(ClassUtils.parseJavaFile(file)).ifPresent(compilationUnit -> {
                compilationUnit.getPackageDeclaration().ifPresent(packageDeclaration -> {
                    String packageName = packageDeclaration.getNameAsString();
                    compilationUnit.getPrimaryType().ifPresent(typeDeclaration -> {
                        String className = packageName+"."+typeDeclaration.getName();
                        Class<?> cl ;
                        try {
                            cl = classLoader.loadClass(className);
                            ClassCache.putTypeDeclaration(className,typeDeclaration);
                            ClassCache.putClass(className,cl);
                            controllerClasses.add(cl);
                        } catch (ClassNotFoundException e) {
                            LogUtil.error(" 加载类失败 e={} ",e);
                        }
                    });
                });
            });
        });
        return controllerClasses;

    }

    protected abstract Set<Class<?>> getAllControllerClass(Set<File> javaFiles);

    protected abstract void parseCompilationUnit(CompilationUnit compilationUnit, Set<ControllerClass> controllerClasses);

    @Override
    public ApiTree parse(String projectPath, boolean recursive) throws ParserException {
        LogUtil.info("开始解析 projectPath# "+projectPath);
        Set<File> javaFiles =  ClassUtils.scan(projectPath,true);
        Set<ControllerClass> controllerClasses = Sets.newHashSet();
        getAllControllerClass(javaFiles).forEach(cl-> controllerClasses.add(this.parseControllerClass(cl)));
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
