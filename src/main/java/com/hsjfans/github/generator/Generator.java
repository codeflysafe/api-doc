package com.hsjfans.github.generator;

import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.ApiTree;

/**
 *
 * 文档生成器
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public interface Generator {

   void from(ApiTree apiTree, Config config);

}

