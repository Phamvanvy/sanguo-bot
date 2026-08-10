package com.pip.sanguo.editor.quest.expr;

/**
 * 可以带有通知的表达式模板。
 * @author lighthu
 */
public abstract class AbstractNotifyAction extends AbstractExpr {
	public int notifyID = -1;
	
	/**
	 * 判断这个模板是一个条件还是一个动作。
	 */
	public boolean isCondition() {
		return false;
	}
}
