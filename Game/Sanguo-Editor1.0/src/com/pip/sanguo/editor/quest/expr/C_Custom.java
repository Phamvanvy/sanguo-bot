package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.ExpressionList;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;

/**
 * 用户自定义的无法用模板处理的复杂表达式。
 * @author lighthu
 */
public class C_Custom extends AbstractExpr {
	public String exprStr;
	
	/**
	 * 构造方法。
	 */
	public C_Custom() {
		exprStr = "a() == 1";
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_Custom();
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
        return exprStr;
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "自定义条件";
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
		if (expr.getRightExpr() != null) {
			C_Custom ret = new C_Custom();
			ret.exprStr = expr.toString();
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		return ExpressionList.toNatureString(exprStr);
	}

	// 下面是IPropertySource接口的实现

	/**
	 * 取得属性描述符。这个模板有1个参数：表达式。
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { 
				new TextPropertyDescriptor("value", "表达式")
		};
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		if ("value".equals(id)) {
			return exprStr;
		}
		return null;
	}

	/**
	 * 设置属性当前值。
	 */
	public void setPropertyValue(Object id, Object value) {
		if ("value".equals(id)) {
			String newValue = (String)value;
			if (!newValue.equals(exprStr)) {
				exprStr = newValue;
				fireValueChanged();
			}
		}
	}
}
