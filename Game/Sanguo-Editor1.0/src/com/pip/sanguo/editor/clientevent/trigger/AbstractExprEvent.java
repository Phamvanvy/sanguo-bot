package com.pip.sanguo.editor.clientevent.trigger;

import com.pip.sanguo.data.clientEvent.ClientEvent;


/**
 * 表达式模板的基本抽象实现。
 * @author lighthu
 */
public abstract class AbstractExprEvent implements IExprEvent {
    protected ClientEvent clientEvent;

    protected AbstractExprEvent() {
    }
    
    public Object getEditableValue() {
        return this;
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {}
    
    public String toString() {
        return getName();
    }
    
    public ClientEvent getClientEvent() {
        return clientEvent;
    }
}
