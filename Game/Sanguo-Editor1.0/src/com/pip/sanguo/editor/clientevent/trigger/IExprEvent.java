package com.pip.sanguo.editor.clientevent.trigger;

import org.eclipse.swt.widgets.Composite;

import com.pip.sanguo.data.clientEvent.ClientEvent;

/**
 * 任务设计器中用到的表达式片段/模板接口。一个表达式片段可以通过表达式模板创建出来，并且
 * 具有自己的属性集合供设计器修改。
 * @author lighthu
 */
public interface IExprEvent {
    /**
     * 通过模板创建一个新的空白表达式片段。
     * @return 新的表达式片段对象
     */
    public static final int VARIABLE_INT    = 0;    //int
    public static final int VARIABLE_STRING = 1;    //string
    
    public static final int PARAM_TYPE      = 0;    //参数基本类型
    public static final int PARAM_VALUE     = 1;    //参数的值
    
    public IExprEvent createNew(ClientEvent event);
    
    /**
     * 判断这个模板是一个条件还是一个动作。
     */
    public boolean isCondition();
    
    /**
     * 取得模板的显示名称。
     */
    public String getName();
    
//    /**
//     * 取得模板的参数名称。
//     */
//    public String[] getParamsName();
    
    /**
     * 取得模板的所有参数的基本类型(String)。
     */
    public String getParamsType();
    
    /**
     * 取得模板的所有参数的值(String)。
     */
    public String getParamsValue();
    
    /**
     * 取得模板的所有参数的值(String)。
     */
    public void initParamsValue(String str);
    
    /**
     * 取得模板的所有参数的值(String)。
     */
    public void setParamsValue();
    
    /**
     * 取得表达式片段对应的表达式字符串。
     */
    public String getExpression();
    
    /**
     * 取得表达式所属的事件。
     */
    public ClientEvent getClientEvent();
    
    /**
     * 取得改模板的界面
     */
    public Composite getProperty(Composite container);
    
    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(String str);
}
