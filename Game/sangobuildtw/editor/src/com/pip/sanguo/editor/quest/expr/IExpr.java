package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.IPropertySource;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expression;

/**
 * 任务设计器中用到的表达式片段/模板接口。一个表达式片段可以通过表达式模板创建出来，并且
 * 具有自己的属性集合供设计器修改。
 * @author lighthu
 */
public interface IExpr extends IPropertySource {
	/**
	 * 识别一个表达式对象是否能够被这个模板识别。
	 * @param expr 表达式对象
	 * @return 如果能够识别，返回识别出来的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr);
	
	/**
	 * 通过模板创建一个新的空白表达式片段。
	 * @return 新的表达式片段对象
	 */
	public IExpr createNew(QuestInfo qinfo);
	
	/**
	 * 判断这个模板是一个条件还是一个动作。
	 */
	public boolean isCondition();
	
	/**
	 * 取得模板的显示名称。
	 */
	public String getName();
	
	/**
	 * 取得表达式片段对应的表达式字符串。
	 */
	public String getExpression();
	
	/**
	 * 得到表达式片段的自然表达字符串。
	 */
	public String toNatureString();
	
	/**
	 * 添加监听者。
	 */
	public void addListener(IExprListener l);
	
	/**
	 * 取得表达式所属的任务（用于获取任务变量列表）。
	 */
	public QuestInfo getQuestInfo();
}
