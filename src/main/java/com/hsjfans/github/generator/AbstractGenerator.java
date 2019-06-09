package com.hsjfans.github.generator;

import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.model.ControllerMethod;

/**
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public abstract class AbstractGenerator implements Generator {

    protected Config config;

    protected abstract void buildControllerDoc(ControllerClass controllerClass);

    protected abstract void buildApiDoc(ControllerClass controllerClass, ControllerMethod controllerMethod);

    protected abstract void buildExtraDoc();

}
