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
 * 表达式模板：判断任务目标是否已全部达成。
 * @author lighthu
 */
public class C_CanFinish extends AbstractExpr {
    public boolean checkTrue = true;
    
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_CanFinish() {
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
        if (checkTrue) {
            return "CanFinish()";
        } else {
            return "CanFinish() == 0";
        }
    }

    /**
     * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
     */
    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("CanFinish") && 
                expr.getLeftExpr().getFunctionCall().getParamCount() == 0) {
            if (expr.getRightExpr() == null) {
                return createNew(qinfo);
            } else if (expr.getRightExpr().type == Expr0.TYPE_NUMBER && PQEUtils.translateNumberConstant(expr.getRightExpr().value) == 0) {
                C_CanFinish ret = (C_CanFinish)createNew(qinfo);
                ret.checkTrue = false;
                return ret;
            }
        }
        return null;
    }
    
    /**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_CanFinish();
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "任务目标是否已达成";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
	    if (checkTrue) {
	        return "任务目标已达成";
	    } else {
	        return "任务目标未达成";
	    }
	}

    // 下面是IPropertySource接口的实现

    /**
     * 取得属性描述符。这个模板有1个参数：参数。
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new ComboBoxPropertyDescriptor("checkTrue", "条件成立", new String[] { "是", "否" })
        };
    }

    /**
     * 取得属性当前值。
     */
    public Object getPropertyValue(Object id) {
        if ("checkTrue".equals(id)) {
            return checkTrue ? 0 : 1;
        }
        return null;
    }

    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(Object id, Object value) {
        if ("checkTrue".equals(id)) {
            boolean newValue = ((Integer)value).intValue() == 0;
            if (newValue != checkTrue) {
                checkTrue = newValue;
                fireValueChanged();
            }
        }   
    }
}
