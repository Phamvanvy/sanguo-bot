package com.pip.servermgr;

import java.io.*;
import java.util.*;

/**
 * 载入单个Class文件的ClassLoader
 * @author lighthu
 */
public class SingleClassLoader extends ClassLoader {
	private Class cls;
	private byte[] clsData;
	private String clsName;

    public SingleClassLoader(ClassLoader parent, String name, byte[] data) throws Exception {
    	super(parent);
    	clsName = name;
    	clsData = data;
    }
    
    @Override
	public synchronized Class loadClass(String className, boolean resolve) throws ClassNotFoundException {
    	// 如果类已经载入过了，那么直接返回
    	if (className.equals(clsName) && cls != null) {
    		if (resolve) {
    			resolveClass(cls);
    			return cls;
    		}
    	}
    	
        // 首先试图从本地系统类组中载入指定类。这是必须的，因为虚拟机将这个类载入后，在解析
        // 和执行它时所用到的任何其他类，如java.lang.System类等，均不再使用虚拟机的类载入器，
        // 而是调用我们自制的类载入器来加载
        try {
            Class newClass = findSystemClass(className);
            return newClass;
        } catch (ClassNotFoundException e) {
        }
        try {
        	Class newClass = getParent().loadClass(className);
        	return newClass;
        } catch (ClassNotFoundException e) {
        }
        
        // 如果不是系统类，那么这个类必须是指定的名字
        if (className.equals(clsName)) {
        	Class newClass = defineClass(className, clsData, 0, clsData.length);
        	if (newClass == null) {
        		throw new ClassNotFoundException(className);
        	}
        	if (resolve) {
        		resolveClass(newClass);
        	}
        	return newClass;
        } else {
        	throw new ClassNotFoundException(className);
        }
    }
}
