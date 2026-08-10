package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.NPCTemplatePropertyDescriptor;
import com.pip.sanguo.editor.property.VariablePropertyDescriptor;

/**
 * 表达式模板：检测玩家和配偶一起杀死某类怪物。
 * @author lighthu
 */
public class C_KillWithMate extends AbstractExpr {
	public int templateID;
	public String varName;
	public int max;
	
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_KillWithMate(QuestInfo qinfo) {
		templateID = 0;
		if (qinfo != null && qinfo.variables.size() > 0) {
		    varName = qinfo.variables.get(0).name;
		} else {
		    varName = "killCount";
		}
		max = 10;
		questInfo = qinfo;
	}
	
	/**
	 * 判断这个模板是一个条件还是一个动作。
	 */
	public boolean isCondition() {
		return true;
	}

	/**
	 * 取得生成的表达式。
	 */
	public String getExpression() {
		return "E_KillWithMate(" + templateID + ", \"" + PQEUtils.reverseConv(varName) + "\", " + max + ")";
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_KillWithMate(qinfo);
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "玩家和配偶一起杀死怪物...";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
	    String name = NPCTemplate.toString(EditorApplication.getProj(), templateID);
		return "玩家和配偶一起杀死怪物 " + name;
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
		if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("E_KillWithMate") && expr.getRightExpr() == null) {
			if (expr.getLeftExpr().getFunctionCall().getParamCount() != 3) {
				return null;
			}
			Expression param1 = expr.getLeftExpr().getFunctionCall().getParam(0);
			Expression param2 = expr.getLeftExpr().getFunctionCall().getParam(1);
			Expression param3 = expr.getLeftExpr().getFunctionCall().getParam(2);
			if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER &&
				param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_STRING &&
				param3.getRightExpr() == null && param3.getLeftExpr().type == Expr0.TYPE_NUMBER) {
				C_KillWithMate ret = (C_KillWithMate)createNew(qinfo);
				ret.templateID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
				ret.varName = PQEUtils.translateStringConstant(param2.getLeftExpr().value);
				ret.max = PQEUtils.translateNumberConstant(param3.getLeftExpr().value);
				return ret;
			}
		}
		return null;
	}

	// 下面是IPropertySource接口的实现

	/**
	 * 取得属性描述符。这个模板有3个参数：mapID，x，y。
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { 
				new NPCTemplatePropertyDescriptor("templateID", "怪物"),
				new VariablePropertyDescriptor("varName", "变量名", questInfo, true),
				new TextPropertyDescriptor("max", "最大数量")
		};
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		if ("templateID".equals(id)) {
			return new Integer(templateID);
		} else if ("varName".equals(id)) {
			return varName;
		} else if ("max".equals(id)) {
			return String.valueOf(max);
		}
		return null;
	}

	/**
	 * 设置属性当前值。
	 */
	public void setPropertyValue(Object id, Object value) {
		if ("templateID".equals(id)) {
			int newValue = ((Integer)value).intValue();
			if (newValue != templateID) {
				templateID = newValue;
				fireValueChanged();
			}
		} else if ("varName".equals(id)) {
			String newValue = (String)value;
			if (!newValue.equals(varName)) {
				varName = newValue;
				fireValueChanged();
			}
		} else if ("max".equals(id)) {
			int newValue = Integer.parseInt((String)value);
			if (newValue != max) {
				max = newValue;
				fireValueChanged();
			}
		}
	}
}
