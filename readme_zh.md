Welcome to the api-doc wiki!

![](https://raw.githubusercontent.com/hsjfans/git_resource/master/20190609204505.png)

[English](./readme.md) [简体中文](./readme_zh.md)

## 初衷

原因很简单，最主要是 

- 简化接口文档的编写
- 对接口的修改更加快速的可见


## 主要作用

`api-doc` 主要功能是生成 `java api` 文档, 它拓展了`JavaDoc` 标准注释，无需使用注解，减少了入侵。同时，可以规范你的`java` 文档规范.

## 原理

首先感谢，[javaparser](https://github.com/javaparser/javaparser)，本项目采用`javaparser` 进行 `java`源码扫描，同时，对其源码进行简单拓展使其支持了自定义的`javaDocTag`,简化开发，修改版的`javaparser`详见[这里](https://github.com/hsjfans/javaparser)。 

其次，除了使用源码扫描工具之外，为了对项目代码的入侵，还自定义了一个简单的类加载器，用户只需自定义`web`项目路径以及预输出路径即可启动。
使用类加载主要是为了，更加准确对源码进行解析(利用反射)。

## 使用文档

- [Get Start](https://github.com/hsjfans/api-doc/wiki/Get-Start)
- [Java Doc Tag](https://github.com/hsjfans/api-doc/wiki/Java-Doc_Tag)
- [JavaParser](https://github.com/hsjfans/api-doc/wiki/JavaParser)

## TODO LIST

由于是端午节这三天写出来的草图，很多功能还不够完善，我这里有一些已知的问题:
- [ ] `@ResponseBody` 的支持，
- [ ] `Set,List`等集合的支持
- [ ] 热加载问题(目前每需要全面进行扫描，存在很大的浪费)
- [ ] 自动加载的问题，这个其实与热加载一样，与`git`集成
- [ ] `Mock`，前后端mock 数据，
- [ ] 修改问题，解析出来的东西也只是草图，也需要进行调整。
- [ ] 更多持久化方案，如`markdown`,`json`,`mongodb` 等支持


## Reference 

dependencies:
- [javaParser](https://github.com/javaparser/javaparser)

- [yapi](https://github.com/YMFE/yapi)

- git hook

- [java doc](https://docs.orac