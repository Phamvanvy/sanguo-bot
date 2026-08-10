package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.NPCPropertyDescriptor;
import com.pip.sanguo.editor.property.VariablePropertyDescriptor;

/**
 * 表达式模板：查找一个NPC是否在附近。
 * @author lighthu
 */
public class C_FindNPCByType extends AbstractExpr {
    public int templateID;
    public int distance;
    public String varName;
    public boolean checkTrue = true;
    
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_FindNPCByType(QuestInfo qinfo) {
	    templateID = -1;
	    distance = 24;
	    varName = "新变量";
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
        if (checkTrue) {
            return "FindNPCByType(" + templateID + ", " + distance + ", \"" + varName + "\")";
        } else {
            return "FindNPCByType(" + templateID + ", " + distance  + ", \"" + varName + "\") == 0";
        }
    }

    /**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_FindNPCByType(qinfo);
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "附近有指定类型的NPC...";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
	    if (checkTrue) {
	        return "附近" + (distance / 8.00) + "码内存在类型为" + templateID + "的NPC";
	    } else {
            return "附近" + (distance / 8.00) + "码内不存在类型为" + templateID + "的NPC";
	    }
	}

    /**
     * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
     */
    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("FindNPCByType")) {
            if (expr.getLeftExpr().getFunctionCall().getParamCount() != 3) {
                return null;
            }
            Expression param1 = expr.getLeftExpr().getFunctionCall().getParam(0);
            Expression param2 = expr.getLeftExpr().getFunctionCall().getParam(1);
            Expression param3 = expr.getLeftExpr().getFunctionCall().getParam(2);
            if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param3.getRightExpr() == null && param3.getLeftExpr().type == Expr0.TYPE_STRING) {
                C_FindNPCByType ret = (C_FindNPCByType)createNew(qinfo);
                ret.templateID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
                ret.distance = PQEUtils.translateNumberConstant(param2.getLeftExpr().value);
                ret.varName = PQEUtils.translateStringConstant(param3.getLeftExpr().value);
                if (expr.getRightExpr() == null) {
                    return ret;
                } else if (expr.getRightExpr().type == Expr0.TYPE_NUMBER && PQEUtils.translateNumberConstant(expr.getRightExpr().value) == 0) {
                    ret.checkTrue = false;
                    return ret;
                }
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
                new TextPropertyDescriptor("templateID", "模板ID"),
                new TextPropertyDescriptor("distance", "有效距离(码)"),
                new VariablePropertyDescriptor("varName", "保存到变量", questInfo, true),
                new ComboBoxPropertyDescriptor("checkTrue", "条件成立", new String[] { "是", "否" })
        };
    }

    /**
     * 取得属性当前值。
     */
    public Object getPropertyValue(Object id) {
        if ("templateID".equals(id)) {
            return String.valueOf(templateID);
        } else if ("distance".equals(id)) {
            return String.valueOf(distance / 8.00);
        } else if ("varName".equals(id)) {
            return varName;
        } else if ("checkTrue".equals(id)) {
            return checkTrue ? 0 : 1;
        }
        return null;
    }

    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(Object id, Object value) {
        if ("templateID".equals(id)) {
            int newValue = Integer.parseInt((String)value);
            if (newValue != templateID) {
                templateID = newValue;
                fireValueChanged();
            }
        } else if ("distance".equals(id)) {
            int newValue = (int)(Double.parseDouble((String)value) * 8);
            if (newValue != distance) {
                distance = newValue;
                fireValueChanged();
            }
        } else if ("varName".equals(id)) {
            String newValue = (String)value;
            if (!newValue.equals(varName)) {
                varName = newValue;
                fireValueChanged();
            }
        } else if ("checkTrue".equals(id)) {
            boolean newValue = ((Integer)value).intValue() == 0;
            if (newValue != checkTrue) {
                checkTrue = newValue;
                fireValueChanged();
            }
        }
    }
}
