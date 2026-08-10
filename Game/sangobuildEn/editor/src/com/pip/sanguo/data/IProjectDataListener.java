package com.pip.sanguo.data;

/**
 * 用于监视项目数据的保存，以更新文件时间。
 * @author lighthu
 */
public interface IProjectDataListener {
    void saveStart(Class cls);
    void saveEnd(Class cls);
}
