package com.hsjfans.github.parser;

import com.google.common.collect.Sets;
import com.hsjfans.github.annotation.Api;
import com.hsjfans.github.annotation.ApiDoc;
import com.hsjfans.github.annotation.Ignore;
import com.hsjfans.github.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Slf4j
public abstract class AbstractParser implements Parser  {

    protected abstract Set<String> supportClassAnnotations();

    @Override
    public void parse(Class<?> cl) throws ParserException {

        Annotation[] annotations = cl.getAnnotations();
        Set<String> annotationStrs = CollectionUtil.annotationsToSet(annotations);
        if(Sets.intersection(CollectionUtil.annotationsToSet(annotations),supportClassAnnotations()).size()==0){

            // todo add log
            throw new ParserException(" 不支持的类型"+cl.getName());
        }

        boolean contain = CollectionUtil.contain(annotations,ApiDoc.class);

        Method[] methods =  cl.getMethods();
        for(Method method:methods){
           if(contain){
               parseApiDocMethod(method);
           }else {
               parseApiMethod(method);
           }
        }

    }

    /**
     * parse the method with @ApiDoc
     * @param method method
     */
    private void parseApiDocMethod(Method method){
       Annotation[] annotations =  method.getAnnotations();
       if(CollectionUtil.contain(annotations, Ignore.class)){
           return;
       }
       parseMethod(method);
    }


    /**
     *
     * parse the method no @ApiDoc
     *
     * @param method method
     */
    private void parseApiMethod(Method method){

        Annotation[] annotations =  method.getAnnotations();
        if(!CollectionUtil.contain(annotations, Api.class)){
           return;
        }
        parseMethod(method);
    }


    /**
     * parse method
     * @param method method
     */
    private void parseMethod(Method method){



    }

}
