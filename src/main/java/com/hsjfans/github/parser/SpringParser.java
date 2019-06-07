package com.hsjfans.github.parser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Sets;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.util.ClassUtils;
import com.hsjfans.github.util.CollectionUtil;
import com.hsjfans.github.util.Constant;
import com.hsjfans.github.util.LogUtil;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Map;
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
           if(compilationUnit!=null){
               if(compilationUnit.getAnnotationDeclarationByName(Constant.SPRING_CONTROLLER).isPresent()){
                   compilationUnits.add(compilationUnit);
               }else if(compilationUnit.getAnnotationDeclarationByName(Constant.SPRING_REST_CONTROLLER).isPresent()){
                   compilationUnits.add(compilationUnit);
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

        Map<Class<?>, Annotation> annotationMap = CollectionUtil.convertToMap(
                cl.getAnnotations()
        );

        if(!annotationMap.containsKey(Controller.class)&&!annotationMap.containsKey(RestController.class)){
            return null;
        }

        // contain `RequestMapping(Value="")`
        if(annotationMap.containsKey(RequestMapping.class)){
            ((RequestMapping)annotationMap.get(RequestMapping.class))
        }

        controllerClass =  ClassUtils.parseClassComment(typeDeclaration.get().getComment().orElse(null),cl);



    }


}
