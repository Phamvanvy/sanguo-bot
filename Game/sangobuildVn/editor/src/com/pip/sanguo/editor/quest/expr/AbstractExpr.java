package com.pip.sanguo.editor.quest.expr;

import java.util.*;

import com.pip.sanguo.data.quest.QuestInfo;

/**
 * 表达式模板的基本抽象实现。
 * @author lighthu
 */
public abstract class AbstractExpr implements IExpr {
	protected List<IExprListener> listeners = new ArrayList<IExprListener>();
	protected QuestInfo questInfo;

    protected AbstractExpr() {
    }

	public void addListener(IExprListener l) {
		listeners.add(l);
	}
	
	protected void fireValueChanged() {
		for (IExprListener l : listeners) {
			l.valueChanged(this);
		}
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
	
	public QuestInfo getQuestInfo() {
	    return questInfo;
	}
}
