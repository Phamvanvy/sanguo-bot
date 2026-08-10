package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;

/**
 * 表达式模板：设置变量的值。
 * @author lighthu
 */
public class A_Inc extends AbstractFunctionAction1 {
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public A_Inc(QuestInfo qinfo) {
		super("Inc", "var", "变量", 1, "增加值");
		questInfo = qinfo;
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new A_Inc(qinfo);
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "增加变量的值";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		return "变量 " + param1 + " 的值增加 " + param2;
	}
}
