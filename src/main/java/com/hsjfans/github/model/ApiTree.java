package com.hsjfans.github.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */

@Data
public class ApiTree implements Serializable {

    private final Set<ControllerClass> set
            = Sets.newHashSet();

    /**
     * insert the controller cl
     *
     * @param controllerClass
     */
    public void insert(ControllerClass controllerClass) {
        set.add(controllerClass);
    }


    public void insertAll(Collection<? extends ControllerClass> controllerClasses) {
        set.addAll(controllerClasses);
    }


    public void union(ApiTree apiTree) {
        set.addAll(apiTree.getSet());
    }
}
