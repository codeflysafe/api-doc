package com.hsjfans.github.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class ClassCache {

    private static final ReadWriteLock classReadWriteLock = new ReentrantReadWriteLock();

    private static final ReadWriteLock compilationUnitReadWriteLock = new ReentrantReadWriteLock();

    private static final Map<String,Class<?>>  classCache ;

    private static final Map<String, CompilationUnit> compilationUnitCache;

    static {
        classCache = Maps.newHashMap();
        compilationUnitCache = Maps.newHashMap();
    }


    public static void putCompilationUnit(String filePath,CompilationUnit compilationUnit){
        boolean contain;
        compilationUnitReadWriteLock.readLock().lock();
        contain = compilationUnitCache.containsKey(filePath);
        compilationUnitReadWriteLock.readLock().unlock();
        if(contain){
            return;
        }
        compilationUnitReadWriteLock.writeLock().lock();
        compilationUnitCache.put(filePath,compilationUnit);
        compilationUnitReadWriteLock.writeLock().unlock();
    }

    public static CompilationUnit getCompilationUnit(String filePath){
        CompilationUnit compilationUnit;
        compilationUnitReadWriteLock.readLock().lock();
        compilationUnit = compilationUnitCache.get(filePath);
        compilationUnitReadWriteLock.readLock().unlock();
        return compilationUnit;
    }


    public static Class<?> getClass(String filePath){
        Class<?> c;
        classReadWriteLock.readLock().lock();
        c = classCache.get(filePath);
        classReadWriteLock.readLock().unlock();
        return c;
    }



    public static void putClass(String filePath,Class<?> c){
        boolean contain;
        classReadWriteLock.readLock().lock();
        contain = classCache.containsKey(filePath);
        classReadWriteLock.readLock().unlock();
        if(contain){
            return;
        }
        classReadWriteLock.writeLock().lock();
        classCache.put(filePath,c);
        classReadWriteLock.writeLock().unlock();
    }

    public static Map<String, Class<?>> getClassCache() {
        return classCache;
    }

    public static Map<String, CompilationUnit> getCompilationUnitCache() {
        return compilationUnitCache;
    }
}
