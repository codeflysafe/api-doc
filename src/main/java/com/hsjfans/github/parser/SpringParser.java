package com.hsjfans.github.parser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.model.ControllerMethod;
import com.hsjfans.github.util.ClassUtils;
import com.hsjfans.github.util.LogUtil;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class SpringParser extends AbstractParser{


    private static final ClassLoader classLoader   = AbstractParser.class.getClassLoader();

    // 支持的 Controller 类注解
    private static final Set<String> supportClassAnnotations = Sets.newHashSet("RestController","Controller");


    @Override
    protected Set<String> supportClassAnnotations() {
        return supportClassAnnotations;
    }

    @Override
    protected Set<CompilationUnit> getAllControllerClass(Set<File> javaFiles) {

        Set<CompilationUnit> compilationUnits = Sets.newHashSet();
        javaFiles.forEach(file->{
           CompilationUnit compilationUnit = ClassUtils.parseJavaFile(file);
           if (compilationUnit!=null){
               Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
               if(packageDeclaration.isPresent()){
                   String packageName = packageDeclaration.get().getNameAsString();
                   Optional<TypeDeclaration<?>> typeDeclaration = compilationUnit.getPrimaryType();
                   if(typeDeclaration.isPresent()){
                       String className = packageName+"."+typeDeclaration.get().getName();
                       Class<?> cl ;
                       try {
                           cl = classLoader.loadClass(className);
                           ClassCache.putCompilationUnit(className,compilationUnit);
                           ClassCache.putClass(className,cl);
                           compilationUnits.add(compilationUnit);
                       }catch (Exception e){
                           LogUtil.warn(e.getMessage());
                       }
                   }
               }
           }
        });

        return compilationUnits;
    }

    @Override
    protected ControllerClass parseCompilationUnit(CompilationUnit compilationUnit) {
        ControllerClass controllerClass = null;
        Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
        if(!packageDeclaration.isPresent()){
           return null;
        }
        String packageName = packageDeclaration.get().getNameAsString();
        Optional<TypeDeclaration<?>> typeDeclaration = compilationUnit.getPrimaryType();
        if(!typeDeclaration.isPresent()){
            return null;
        }
        String className = packageName+"."+typeDeclaration.get().getName();
        Class<?> cl ;
        try {
             cl = classLoader.loadClass(className);
        }catch (Exception e){
            LogUtil.warn(e.getMessage());
            return null;
        }

        controllerClass =  ClassUtils.parseClassComment(typeDeclaration.get().getComment().orElse(null),cl);
        if(controllerClass==null||controllerClass.isIgnore()){return null;}

        final List<ControllerMethod> controllerMethods = Lists.newArrayListWithCapacity(cl.getMethods().length);

        typeDeclaration.get().getMethods().forEach(
                methodDeclaration -> {
                    ControllerMethod controllerMethod = ClassUtils.parseMethodComment(methodDeclaration.getComment().orElse(null),methodDeclaration);
                    if(controllerMethod!=null){
                        controllerMethods.add(controllerMethod);
                    }
                }
        );
        controllerClass.setControllerMethod(controllerMethods);

        return controllerClass;


    }


}
