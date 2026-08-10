package com.pip.sanguo.editor.clientevent.trigger;

import org.eclipse.swt.widgets.Composite;
import com.pip.sanguo.data.clientEvent.ClientEvent;

public class G_IsAssociationMemeber extends AbstractExprEvent {

    public String[][] params = {
    };
    
    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public G_IsAssociationMemeber(ClientEvent event) {
        clientEvent = event;
    }
    
    /**
     * 判断这个模板是一个条件还是一个动作。
     */
    public boolean isCondition() {
        return true;
    }
    
    /**
     * 用模板创建新的表达式片段。
     */
    public IExprEvent createNew(ClientEvent event) {
        return new G_IsAssociationMemeber(event);
    }
    
    /**
     * 取得模板名称。
     */
    public String getName() {
        return "玩家是血盟成员";
    }

    /**
     * 取得生成的表达式。
     */
    public String getExpression() {
        return "g_isAssociationMember";
    }

    /**
     * 取得模板参数基本类型。
     */
    public String getParamsType() {
        return "";
    }
    
    /**
     * 取得模板参数值。
     */
    public String getParamsValue() {
        return "";
    }
    
    /**
     * 取得模板的所有参数的值(String)。
     */
    public void initParamsValue(String str){
        params[PARAM_VALUE][0] = str;
    }
    
    /**
     * 设置模板参数值。
     */
    public void setParamsValue(){
    }
    
    /**
     * 取得改模板的界面
     */
    public Composite getProperty(Composite _con) {
        return null;
    }
    
    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(String str) {
    }

}
