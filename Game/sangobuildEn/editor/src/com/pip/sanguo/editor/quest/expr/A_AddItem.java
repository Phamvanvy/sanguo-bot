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
import com.pip.sanguo.editor.property.ItemPropertyDescriptor;

/**
 * 表达式模板：为用户添加物品。
 * @author lighthu
 */
public class A_AddItem extends AbstractExpr {
	public int itemID;
	public int count;
	
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public A_AddItem() {
		itemID = 1;
		count = 1;
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
		return new A_AddItem();
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "添加物品";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		return "添加物品 " + itemID + " " + count + " 个";
	}
	
	/**
	 * 取得生成的表达式。
	 */
	public String getExpression() {
		return "AddItem(" + itemID + ", " + count + ")";
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
		if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("AddItem") && expr.getRightExpr() == null) {
			FunctionCall fc = expr.getLeftExpr().getFunctionCall();
			if (fc.getParamCount() != 2) {
				return null;
			}
			Expression param1 = fc.getParam(0);
			Expression param2 = fc.getParam(1);
			if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER &&
				param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_NUMBER) {
				A_AddItem ret = (A_AddItem)createNew(null);
				ret.itemID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
				ret.count = PQEUtils.translateNumberConstant(param2.getLeftExpr().value);
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
		        new ItemPropertyDescriptor("itemID", "物品"),
				new TextPropertyDescriptor("count", "数量")
		};
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		if ("itemID".equals(id)) {
			return itemID;
		} else if ("count".equals(id)) {
			return String.valueOf(count);
		}
		return null;
	}

	/**
	 * 设置属性当前值。
	 */
	public void setPropertyValue(Object id, Object value) {
	    if ("itemID".equals(id)) {
            int newValue = (Integer)value;
            if (newValue != itemID) {
                itemID = newValue;
                fireValueChanged();
            }
		} else if ("count".equals(id)) {
			int newValue = Integer.parseInt((String)value);
			if (newValue != count) {
				count = newValue;
				fireValueChanged();
			}
		}
	}
}
