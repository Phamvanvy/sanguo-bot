package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.FunctionCall;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.NPCPropertyDescriptor;
import com.pip.sanguo.editor.property.VariablePropertyDescriptor;

/**
 * 表达式模板：在指定位置强制刷新NPC。
 * @author lighthu
 */
public class A_RefreshNPCAt extends AbstractExpr {
	public int npcID;
	public String targetNpcVar;
	public String varName;
	
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public A_RefreshNPCAt(QuestInfo qinfo) {
		npcID = -1;
		targetNpcVar = "新变量";
		varName = "新变量";
		questInfo = qinfo;
	}
	
	/**
	 * 判断这个模板是一个条件还是一个动作。
	 */
	public boolean isCondition() {
		return false;
	}

	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new A_RefreshNPCAt(qinfo);
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "在指定位置强制刷新NPC";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		return "在指定位置强制刷新NPC " + GameMapNPC.toStringShort(EditorApplication.getProj(), npcID);
	}
	
	/**
	 * 取得生成的表达式。
	 */
	public String getExpression() {
		return "RefreshNPCAt(" + npcID + ", " + targetNpcVar + ", \"" + varName + "\")";
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
		if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("RefreshNPCAt") && expr.getRightExpr() == null) {
			FunctionCall fc = expr.getLeftExpr().getFunctionCall();
			if (fc.getParamCount() != 3) {
				return null;
			}
			Expression param1 = fc.getParam(0);
			Expression param2 = fc.getParam(1);
			Expression param3 = fc.getParam(2);
			if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER &&
				param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_IDENTIFIER &&
				param3.getRightExpr() == null && param3.getLeftExpr().type == Expr0.TYPE_STRING) {
				A_RefreshNPCAt ret = (A_RefreshNPCAt)createNew(qinfo);
				ret.npcID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
				ret.targetNpcVar = param2.getLeftExpr().value;
				ret.varName = PQEUtils.translateStringConstant(param3.getLeftExpr().value);
				return ret;
			}
		}
		return null;
	}

	// 下面是IPropertySource接口的实现

	/**
	 * 取得属性描述符。这个模板有2个参数：字符串参数和整数参数。
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { 
				new NPCPropertyDescriptor("npcID", "刷新NPC"),
				new VariablePropertyDescriptor("targetNpcVar", "目标NPC变量", questInfo, true),
				new VariablePropertyDescriptor("varName", "保存到变量", questInfo, true)
		};
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		if ("npcID".equals(id)) {
			return new Integer(npcID);
		} else if ("targetNpcVar".equals(id)) {
			return targetNpcVar;
		} else if ("varName".equals(id)) {
		    return varName;
		}
		return null;
	}

	/**
	 * 设置属性当前值。
	 */
	public void setPropertyValue(Object id, Object value) {
		if ("npcID".equals(id)) {
			int newValue = ((Integer)value).intValue();
			if (newValue != npcID) {
				npcID = newValue;
				fireValueChanged();
			}
		} else if ("targetNpcVar".equals(id)) {
		    String newValue = (String)value;
            if (!newValue.equals(targetNpcVar)) {
                targetNpcVar = newValue;
                fireValueChanged();
            }
		} else if ("varName".equals(id)) {
            String newValue = (String)value;
            if (!newValue.equals(varName)) {
                varName = newValue;
                fireValueChanged();
            }
		}
	}
}
