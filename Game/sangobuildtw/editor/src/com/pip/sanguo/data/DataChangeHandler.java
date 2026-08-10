package com.pip.sanguo.data;

/**
 * 数据修改监听器，用于服务器重载数据时处理修改的数据。
 * @author lighthu
 */
public interface DataChangeHandler {
    /**
     * 添加新对象通知。
     * @param obj 新添加的对象
     */
    void dataObjectAdded(DataObject obj);
    /**
     * 对象被删除通知。
     * @param obj 被删除的老对象
     */
    void dataObjectRemoved(DataObject obj);
    /**
     * 对象即将被修改通知。
     * @param obj 修改前的对象
     */
    void dataObjectChanging(DataObject obj);
    /**
     * 对象被修改通知。
     * @param obj 修改后的新对象
     */
    void dataObjectChanged(DataObject obj);
}
