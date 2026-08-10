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
 * 表达式模板：性别判断。
 * @author lighthu
 */
public class C_Sex extends AbstractExpr {
	public int sex;
	
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_Sex() {
		sex = 0;
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_Sex();
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
		return "_SEX == " + sex;
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "性别为...";
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
		if (expr.getLeftExpr().type == Expr0.TYPE_IDENTIFIER && expr.getRightExpr() != null &&
				expr.getRightExpr().type == Expr0.TYPE_NUMBER) {
			if (!"_SEX".equals(expr.getLeftExpr().value) || expr.op != ParserConstants.EQ) {
				return null;
			}
			C_Sex ret = new C_Sex();
			ret.sex = PQEUtils.translateNumberConstant(expr.getRightExpr().value);
			return ret;
		}
		return null;
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		switch (sex) {
		case 0:
			return "角色为男性";
		case 1:
			return "角色为女性";
		default:
			return "角色为阴阳人";
		}
	}

	// 下面是IPropertySource接口的实现

	/**
	 * 取得属性描述符。这个模板只有2个参数：比较性别。
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { 
				new ComboBoxPropertyDescriptor("value", "性别", new String[] { "男性", "女性", "阴阳人" })
		};
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		if ("value".equals(id)) {
			return sex;
		}
		return null;
	}

	/**
	 * 设置属性当前值。
	 */
	public void setPropertyValue(Object id, Object value) {
		if ("value".equals(id)) {
			int newSex = ((Integer)value).intValue();
			if (newSex != sex) {
				sex = newSex;
				fireValueChanged();
			}
		}
	}
}
