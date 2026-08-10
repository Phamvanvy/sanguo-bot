package com.pip.util;

import java.io.*;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;

public class EFSUtil {
    public static void copyFile(File src, File dest) throws Exception {
        IFileSystem fs = EFS.getLocalFileSystem();
        IFileStore srcStore = fs.fromLocalFile(src);
        IFileStore destStore = fs.fromLocalFile(dest);
        srcStore.copy(destStore, EFS.OVERWRITE, null);
    }
    
    public static void copyDir(File src, File dest) throws Exception {
    	dest.mkdirs();
    	File[] children = src.listFiles();
    	for (File child : children) {
    		if (child.isDirectory()) {
    			if (child.getName().equals("CVS") || child.getName().equals(".svn")) {
    				continue;
    			}
    			copyDir(child, new File(dest, child.getName()));
    		} else {
    			copyFile(child, new File(dest, child.getName()));
    		}
    	}
    }
    
    public static void moveFile(File src, File dest) throws Exception {
        IFileSystem fs = EFS.getLocalFileSystem();
        IFileStore srcStore = fs.fromLocalFile(src);
        IFileStore destStore = fs.fromLocalFile(dest);
        srcStore.move(destStore, EFS.OVERWRITE, null);
    }
}
