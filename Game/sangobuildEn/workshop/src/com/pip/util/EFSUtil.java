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
}
