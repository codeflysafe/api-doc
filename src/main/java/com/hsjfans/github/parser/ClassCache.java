package com.hsjfans.github.parser;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * todo 优化
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class ClassCache {

    private static final ReadWriteLock classReadWriteLock = new ReentrantReadWriteLock();

    private static final ReadWriteLock typeDeclarationReadWriteLock = new ReentrantReadWriteLock();

    private static final Map<String, Class<?>> classCache;

    private static final Map<String, TypeDeclaration<?>> typeDeclarationCache;

    private static final ConcurrentMap<String, Javadoc> javaDocMap;

    static {
        classCache = Maps.newHashMap();
        typeDeclarationCache = Maps.newHashMap();
        javaDocMap = Maps.newConcurrentMap();
    }


    public static void putJavadoc(String packageName, Javadoc javadoc) {
        javaDocMap.putIfAbsent(packageName, javadoc);
    }

    public static Javadoc getJavadoc(String packageName) {
        return javaDocMap.get(packageName);
    }


    public static void putTypeDeclaration(String filePath, TypeDeclaration compilationUnit) {
        boolean contain;
        typeDeclarationReadWriteLock.readLock().lock();
        contain = typeDeclarationCache.containsKey(filePath);
        typeDeclarationReadWriteLock.readLock().unlock();
        if (contain) {
            return;
        }
        typeDeclarationReadWriteLock.writeLock().lock();
        typeDeclarationCache.put(filePath, compilationUnit);
        typeDeclarationReadWriteLock.writeLock().unlock();
    }

    public static TypeDeclaration<?> getTypeDeclaration(String filePath) {
        TypeDeclaration compilationUnit;
        typeDeclarationReadWriteLock.readLock().lock();
        compilationUnit = typeDeclarationCache.get(filePath);
        typeDeclarationReadWriteLock.readLock().unlock();
        return compilationUnit;
    }


    public static Class<?> getClass(String packageName) {
        Class<?> c;
        classReadWriteLock.readLock().lock();
        c = classCache.get(packageName);
        classReadWriteLock.readLock().unlock();
        return c;
    }


    public static void putClass(String packageName, Class<?> c) {
        boolean contain;
        classReadWriteLock.readLock().lock();
        contain = classCache.containsKey(packageName);
        classReadWriteLock.readLock().unlock();
        if (contain) {
            return;
        }
        classReadWriteLock.writeLock().lock();
        classCache.put(packageName, c);
        classReadWriteLock.writeLock().unlock();
    }

    public static Map<String, Class<?>> getClassCache() {
        return classCache;
    }

    public static Map<String, TypeDeclaration<?>> getCompilationUnitCache() {
        return getCompilationUnitCache();
    }
}
