package com.hsjfans.github.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.ApiTree;
import com.hsjfans.github.model.ClassField;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.model.ControllerMethod;
import com.hsjfans.github.util.*;

import java.io.File;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */

public abstract class AbstractParser implements Parser  {

    public static ClassLoader classLoader  ;

    protected final Config config;

    private final ApiTree apiTree;

    public AbstractParser(Config config){
        this.config = config;
        classLoader = new ApiClassLoader(config);
        this.apiTree = new ApiTree();
    }

    protected abstract Set<String> supportClassAnnotations();


    protected abstract void parseControllerClassDoc(Class<?> cl, ControllerClass controllerClass);


    /**
     *  解析单个 controllerClass 文件
     * @param cl not null
     * @return the controllerMethod
     * @see ControllerClass
     */
    protected ControllerClass parseControllerClass(Class<?> cl){

        ControllerClass controllerClass =ControllerClass.of(cl);

        //第一步 解析 controller 注释
        parseControllerClassDoc(cl,controllerClass);

        //第二步 解析 设置 method
        controllerClass.setControllerMethod(parseControllerMethods(cl));

        return controllerClass;

    }


    /**
     *  解析 class 的 methods
     * @param cl not null
     * @return ControllerMethods
     */
    protected  List<ControllerMethod> parseControllerMethods(Class<?> cl){

        Method[] methods = cl.getDeclaredMethods();
        TypeDeclaration<?> typeDeclaration = ClassCache.getTypeDeclaration(cl.getName());
        List<ControllerMethod> controllerMethods = Lists.newArrayListWithCapacity(methods.length);
        // just public method
        // and has PostMapping GetMapping ...
        Arrays.stream(methods).filter(SpringUtil::isSpringMethods).forEach(method -> {
            List<MethodDeclaration> methodDeclarations = typeDeclaration.getMethodsBySignature(method.getName(),ClassUtils.methodSignature(method));
            if(methodDeclarations.size()>0){
                Optional.ofNullable(parseControllerMethod(methodDeclarations.get(0),method)).ifPresent(controllerMethods::add);
            }
        });
        return controllerMethods;
    }

    protected abstract List<ClassField> parseParameterClassField(Parameter parameter);


    protected  abstract ControllerMethod parseControllerMethod(MethodDeclaration methodDeclaration, Method method);


    /**
     *  过滤 java 文件
     */
    protected Set<Class<?>> parseJavaFiles(Set<File> javaFiles){

        final Set<Class<?>> controllerClasses = Sets.newHashSet();
        javaFiles.forEach(file->{
            if(!FileUtil.filterTest(config.getPackageName(),file)){
                Optional.ofNullable(ClassUtils.parseJavaFile(file)).ifPresent(compilationUnit -> {
                    compilationUnit.getPackageDeclaration().ifPresent(packageDeclaration -> {
                        String packageName = packageDeclaration.getNameAsString();
                        compilationUnit.getPrimaryType().ifPresent(typeDeclaration -> {
                            String className = packageName+"."+typeDeclaration.getName();
                            Class<?> cl ;
                            try {
                                cl = classLoader.loadClass(className);
                                ClassCache.putTypeDeclaration(className,typeDeclaration);
                                ClassCache.putClass(className,cl);
                                controllerClasses.add(cl);
                            } catch (ClassNotFoundException e) {
                                LogUtil.error(" 加载类失败 e={} ",e);
                            }
                        });
                    });
                });
            }
        });
        return controllerClasses;

    }

    protected abstract Set<Class<?>> getAllControllerClass(Set<File> javaFiles);


    @Override
    public ApiTree parse(String projectPath, boolean recursive) throws ParserException {
        LogUtil.info("开始解析 projectPath = %s ",projectPath);
        Set<File> javaFiles =  ClassUtils.scan(projectPath,true);
        Set<ControllerClass> controllerClasses = Sets.newHashSet();
        getAllControllerClass(javaFiles).forEach(cl-> controllerClasses.add(this.parseControllerClass(cl)));
        this.apiTree.insertAll(controllerClasses);
        return apiTree;
    }


    @Override
    public ApiTree parse(List<String> projectPaths, boolean recursive) throws ParserException {
        projectPaths.forEach(path->{
            try {
                this.apiTree.union(this.parse(path,recursive));
            } catch (ParserException e) {
                e.printStackTrace();
            }
        });
        return this.apiTree;
    }



    /**
     *
     * 解析 field 参数
     * 支持`request`请求以及`response`返回值
     * @param request the request param class
     */
    protected static List<ClassField> parserClassFields(Type type,Class<?> request, boolean response){

        List<ClassField> classFields = Lists.newArrayList();

        if(request.isEnum()){
            ClassField classField = new ClassField();
            Object[] enumValues = ClassUtils.getEnumValues(request);
            classField.setType("String");
            classField.setEnumValues(enumValues);
            classField.setEnumType(true);
            classFields.add(classField);
            return classFields;
        } else if(request.isArray()){
            ClassField classField = new ClassField();
            classField.setType(request.getTypeName());
            classField.setName(request.getSimpleName());
            classField.setArray(true);
            classField.setFields(parserClassFields(type,request.getComponentType(),response));
            classFields.add(classField);
            return classFields;
        }else{
            // 如果是标准库的集合类型
            Class<?>  c = ClassUtils.isCollection(type);
            if(c!=null){
                ClassField classField = new ClassField();
                classField.setType(request.getTypeName());
                classField.setName(request.getSimpleName());
                classField.setArray(true);
                classField.setFields(parserClassFields(c.getComponentType(),c,response));
                classFields.add(classField);
                return classFields;
            }

        }

        TypeDeclaration typeDeclaration = ClassCache.getTypeDeclaration(request.getName());
        if (typeDeclaration==null){
            LogUtil.error(" 没有找到对应的 typeDeclaration key= %s ",request.getName());
            return classFields;
        }

        // 结构字段解析
        Arrays.stream(request.getDeclaredFields()).filter(
                field -> !field.isSynthetic()&&
                        (field.getModifiers() & Modifier.FINAL) == 0
                        && (field.getModifiers() & Modifier.STATIC)==0
                        && (field.getModifiers() & Modifier.NATIVE)==0
                        && (field.getModifiers() & Modifier.ABSTRACT)==0
                        && (field.getModifiers() & Modifier.INTERFACE)==0
                        && (field.getModifiers() & Modifier.TRANSIENT)==0
        ).forEach(field -> {
            // 类型信息，这里填充
            ClassField classField = new ClassField();
            classField.setType(field.getType().getTypeName());
            classField.setName(field.getName());
            // 先填充注释信息
            typeDeclaration.getFieldByName(field.getName()).ifPresent(fieldDeclaration -> {
                if(((FieldDeclaration)fieldDeclaration).getComment().isPresent()){
                    Javadoc javadoc = ((FieldDeclaration)fieldDeclaration).getComment().get().parse();

                    Optional<JavadocBlockTag> ignoreOpt = CollectionUtil.contains(javadoc.getBlockTags(),JavadocBlockTag.Type.IGNORE);
                    ignoreOpt.ifPresent(javadocBlockTag -> {
                        classField.setIgnore(true);
                        classField.setDescription(javadocBlockTag.getContent().toText());
                    });
                    if(classField.isIgnore()){
                        return;
                    }
                    // if contains `@name`
                    CollectionUtil.contains(javadoc.getBlockTags(),JavadocBlockTag.Type.NAME).ifPresent(javadocBlockTag -> {
                        classField.setName(javadocBlockTag.getContent().toText());
                        classField.setDescription(javadocBlockTag.getContent().toText());
                    });
                    // if contains `@fuzzy`
                    CollectionUtil.contains(javadoc.getBlockTags(),JavadocBlockTag.Type.FUZZY).ifPresent(javadocBlockTag -> {
                        classField.setFuzzy(true);
                        classField.setDescription(javadocBlockTag.getContent().toText());
                    });
                    // if contains `@nullable`
                    CollectionUtil.contains(javadoc.getBlockTags(),JavadocBlockTag.Type.NULLABLE).ifPresent(javadocBlockTag -> {
                        classField.setNullable(true);
                        classField.setDescription(javadocBlockTag.getContent().toText());
                    });
                    if(!javadoc.getDescription().toText().isEmpty()){
                        classField.setDescription(javadoc.getDescription().toText());
                    }
                }
            });

            // 如果 参数被忽略，跳过
            if(classField.isIgnore()&&!response){
                return;
            }
            // 如果是基本类型，这里直接进行解析
            if(ClassUtils.isFieldPrimitive(field)||field.getType().equals(String.class)|ClassUtils.isTime(field.getType())){
                // nothing to do
                classFields.add(classField);
                return;
            }
            else {
                classField.setFields(parserClassFields(field.getGenericType(),field.getType(),response));
                if(field.getType().isEnum()){
                    classField.setEnumType(true);
                    classField.setEnumValues(classField.getFields().get(0).getEnumValues());
                }
            }
            classFields.add(classField);

        });
        return classFields;
    }


}
