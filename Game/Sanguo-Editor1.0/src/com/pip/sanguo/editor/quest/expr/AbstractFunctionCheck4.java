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
 * 表达式模板：判断带两个参数的函数的返回值是否为true。
 * @author lighthu
 */
public abstract class AbstractFunctionCheck4 extends AbstractExpr {
	protected String funcName;
	public int param1;
	public int param2;
	protected String paramName1;
	protected String paramName2;
	
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public AbstractFunctionCheck4(String name, int p1, String n1, int p2, String n2) {
		funcName = name;
		param1 = p1;
		param2 = p2;
		paramName1 = n1;
		paramName2 = n2;
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
		return funcName + "(" + param1 + ", " + param2 + ")";
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
		if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals(funcName) && expr.getRightExpr() == null) {
			if (expr.getLeftExpr().getFunctionCall().getParamCount() != 2) {
				return null;
			}
			Expression param1 = expr.getLeftExpr().getFunctionCall().getParam(0);
			Expression param2 = expr.getLeftExpr().getFunctionCall().getParam(1);
			if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER &&
				param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_NUMBER) {
				AbstractFunctionCheck4 ret = (AbstractFunctionCheck4)createNew(qinfo);
				ret.param1 = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
				ret.param2 = PQEUtils.translateNumberConstant(param2.getLeftExpr().value);
				return ret;
			}
		}
		return null;
	}

	// 下面是IPropertySource接口的实现

	/**
	 * 取得属性描述符。这个模板有1个参数：参数。
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { 
				new TextPropertyDescriptor("param1", paramName1),
				new TextPropertyDescriptor("param2", paramName2)
		};
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		if ("param1".equals(id)) {
			return String.valueOf(param1);
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
			int newValue = Integer.parseInt((String)value);
			if (newValue != param1) {
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
