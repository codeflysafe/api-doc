package com.hsjfans.github.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class SpringUtil {

    public static final Map<String,RequestMethod[]> map;



    public static final List<String> SPRING_CONTROLLER_METHOD_NAMES= Lists.newArrayList(
            PostMapping.class.getSimpleName(), DeleteMapping.class.getSimpleName(),
            PutMapping.class.getSimpleName(), GetMapping.class.getSimpleName(),
            PatchMapping.class.getSimpleName(), RequestMapping.class.getSimpleName()
    );

    static {
        map = Maps.newHashMap();
        map.put( PostMapping.class.getSimpleName(),new RequestMethod[]{RequestMethod.POST});
        map.put( DeleteMapping.class.getSimpleName(),new RequestMethod[]{RequestMethod.DELETE});
        map.put( GetMapping.class.getSimpleName(),new RequestMethod[]{RequestMethod.GET});
        map.put( PutMapping.class.getSimpleName(),new RequestMethod[]{RequestMethod.PUT});
        map.put( PatchMapping.class.getSimpleName(),new RequestMethod[]{RequestMethod.PATCH});
        map.put( RequestMapping.class.getSimpleName(),new RequestMethod[]{});


    }





}

