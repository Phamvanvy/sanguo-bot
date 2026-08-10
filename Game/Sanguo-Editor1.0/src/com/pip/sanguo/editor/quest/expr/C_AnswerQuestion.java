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
 * 表达式模板：检测用户是否回答了某个提问的某个选项。
 * @author lighthu
 */
public class C_AnswerQuestion extends AbstractFunctionCheck4 {
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_AnswerQuestion() {
		super("E_AnswerQuestion", 1, "通知ID", 0, "选项ID");
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_AnswerQuestion();
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "玩家回答提问...";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		return "提问" + param1 + "：玩家选择 " + (param2 + 1);
	}
}
