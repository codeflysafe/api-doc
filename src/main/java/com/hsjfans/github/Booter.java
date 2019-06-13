package com.hsjfans.github;

import com.hsjfans.github.config.Config;
import com.hsjfans.github.generator.Generator;
import com.hsjfans.github.generator.HtmlGenerator;
import com.hsjfans.github.model.ApiTree;
import com.hsjfans.github.parser.Parser;
import com.hsjfans.github.parser.ParserException;
import com.hsjfans.github.parser.SpringParser;

/**
 *
 *  Booter 启动器
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class Booter {

    public static void main(String[] args) throws ParserException {

        String realPath = "/Volumes/doc/projects/java/java-api-doc/spring-api-demo";
        Config config = new Config();
        config.setPackageName(realPath);
        config.setDocName("demo接口文档");
        config.setApiName("demo");
        Parser parser = new SpringParser(config);
        ApiTree apiTree = parser.parse(config.getPackageName(),true);
        Generator generator = new HtmlGenerator();
        generator.from(apiTree,config);

    }
}

