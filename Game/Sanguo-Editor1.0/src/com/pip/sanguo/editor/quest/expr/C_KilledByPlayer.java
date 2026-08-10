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
 * 表达式模板：判断玩家是否被其他玩家杀死。
 * @author lighthu
 */
public class C_KilledByPlayer extends AbstractFunctionCheck3 {
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_KilledByPlayer() {
		super("E_KilledByPlayer");
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_KilledByPlayer();
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "被其他玩家杀死";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		return "被其他玩家杀死";
	}
}
