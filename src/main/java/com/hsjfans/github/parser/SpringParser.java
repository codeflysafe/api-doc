package com.hsjfans.github.parser;


import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Slf4j
public class SpringParser extends AbstractParser{

    // 支持的 Controller 类注解
    private static final Set<String> supportClassAnnotations = Sets.newHashSet("RestController","Controller");


    @Override
    protected Set<String> supportClassAnnotations() {
        return supportClassAnnotations;
    }




}
