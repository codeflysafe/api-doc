package com.hsjfans.github.model;


import com.google.common.collect.Sets;
import lombok.Data;


import java.io.Serializable;
import java.util.Collection;
import java.util.Set;


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
