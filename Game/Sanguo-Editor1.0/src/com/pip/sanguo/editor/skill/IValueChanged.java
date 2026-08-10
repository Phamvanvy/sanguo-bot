package com.pip.sanguo.editor.skill;

/**
 * PropertySheetEntry控件属性修改事件消息时间处理
 * 
 * @author Administrator
 */
public interface IValueChanged {
    
    /**
     * 监听PropertySheet中的值发生变化时，触发该消息
     */
    public void valueChanged(String id);
    
    /**
     * 监听PropertySheet中的值产生错误时，触发该消息
     */
    public void valueError(String errorMessage);
}
