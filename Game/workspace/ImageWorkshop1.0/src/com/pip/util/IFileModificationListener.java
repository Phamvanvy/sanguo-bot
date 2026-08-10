package com.pip.util;

import java.io.File;

/**
 * 监听文件修改的接口。
 * @author lighthu
 */
public interface IFileModificationListener {
    void fileModified(File f);
}
