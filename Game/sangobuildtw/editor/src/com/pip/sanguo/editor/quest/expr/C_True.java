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
 * 总是为真的判断。
 * @author lighthu
 */
public class C_True extends AbstractExpr {
	/**
	 * 构造方法。
	 */
	public C_True() {
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_True();
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
        return "1 == 1";
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "真";
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
	    if (expr.getLeftExpr().type == Expr0.TYPE_NUMBER && expr.getOp() == ParserConstants.EQ && 
	            expr.getRightExpr().type == Expr0.TYPE_NUMBER && "1".equals(expr.getLeftExpr().value) &&
	            "1".equals(expr.getRightExpr().value)) {
	        return createNew(qinfo);
	    } else {
	        return null;
	    }
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		return "真";
	}

	// 下面是IPropertySource接口的实现

	/**
	 * 取得属性描述符。这个模板有1个参数：表达式。
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[0];
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		return null;
	}

	/**
	 * 设置属性当前值。
	 */
	public void setPropertyValue(Object id, Object value) {
	}
}
