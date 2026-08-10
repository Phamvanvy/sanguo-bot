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
 * 表达式模板：检测玩家关闭某个对话。
 * @author lighthu
 */
public class C_CloseChat extends AbstractFunctionCheck2 {
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_CloseChat() {
		super("E_CloseChat", 1, "通知ID");
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_CloseChat();
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "玩家关闭对话...";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		return "玩家关闭对话 " + constant;
	}
}
