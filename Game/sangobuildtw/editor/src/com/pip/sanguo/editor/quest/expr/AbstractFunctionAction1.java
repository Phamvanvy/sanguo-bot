package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.FunctionCall;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;
import com.pip.sanguo.editor.property.VariablePropertyDescriptor;

/**
 * 表达式模板：带两个参数的函数动作，第一个参数是变量名，第二个参数是整数。
 * @author lighthu
 */
public abstract class AbstractFunctionAction1 extends AbstractExpr {
	protected String funcName;
	public String param1;
	public int param2;
	protected String paramName1;
	protected String paramName2;
	
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public AbstractFunctionAction1(String name, String param1, String paramName1, int param2, String paramName2) {
		funcName = name;
		this.param1 = param1;
		this.paramName1 = paramName1;
		this.param2 = param2;
		this.paramName2 = paramName2;
	}
	
	/**
	 * 判断这个模板是一个条件还是一个动作。
	 */
	public boolean isCondition() {
		return false;
	}

	/**
	 * 取得生成的表达式。
	 */
	public String getExpression() {
		return funcName + "(\"" + PQEUtils.reverseConv(param1) + "\", " + param2 + ")";
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
		if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals(funcName) && expr.getRightExpr() == null) {
			FunctionCall fc = expr.getLeftExpr().getFunctionCall();
			if (fc.getParamCount() != 2) {
				return null;
			}
			Expression param1 = fc.getParam(0);
			Expression param2 = fc.getParam(1);
			if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_STRING &&
				param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_NUMBER) {
				AbstractFunctionAction1 ret = (AbstractFunctionAction1)createNew(qinfo);
				ret.param1 = PQEUtils.translateStringConstant(param1.getLeftExpr().value);
				ret.param2 = PQEUtils.translateNumberConstant(param2.getLeftExpr().value);
				return ret;
			}
		}
		return null;
	}
	
	/**
	 * 判断是否只允许使用任务变量。
	 */
	protected boolean isLocalOnly() {
	    return true;
	}

	// 下面是IPropertySource接口的实现

	/**
	 * 取得属性描述符。这个模板有2个参数：字符串参数和整数参数。
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { 
				new VariablePropertyDescriptor("param1", paramName1, questInfo, isLocalOnly()),
				new TextPropertyDescriptor("param2", paramName2)
		};
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		if ("param1".equals(id)) {
			return param1;
		} else if ("param2".equals(id)) {
			return String.valueOf(param2);
		}
		return null;
	}

	/**
	 * 设置属性当前值。
	 */
	public void setPropertyValue(Object id, Object value) {
		if ("param1".equals(id)) {
			String newValue = (String)value;
			if (!newValue.equals(param1)) {
				param1 = newValue;
				fireValueChanged();
			}
		} else if ("param2".equals(id)) {
			int newValue = Integer.parseInt((String)value);
			if (newValue != param2) {
				param2 = newValue;
				fireValueChanged();
			}
		}
	}
}
