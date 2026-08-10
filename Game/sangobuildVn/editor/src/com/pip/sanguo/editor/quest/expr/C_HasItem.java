package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.ItemPropertyDescriptor;

/**
 * 表达式模板：判断是否有某件物品。
 * @author lighthu
 */
public class C_HasItem extends AbstractExpr {
    public boolean checkTrue = true;
    public int itemID;
    public int checkCount = 1;
    
    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public C_HasItem() {
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
            return "HasItem(" + itemID + ", " + checkCount + ")";
        } else {
            return "HasItem(" + itemID + ", " + checkCount + ") == 0";
        }
    }

    /**
     * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
     */
    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("HasItem") && 
                expr.getLeftExpr().getFunctionCall().getParamCount() == 2) {
            Expression param0 = expr.getLeftExpr().getFunctionCall().getParam(0);
            Expression param1 = expr.getLeftExpr().getFunctionCall().getParam(1);
            if (param0.getRightExpr() == null && param0.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                    param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER) {
                C_HasItem ret = (C_HasItem)createNew(qinfo);
                ret.itemID = PQEUtils.translateNumberConstant(param0.getLeftExpr().value);
                ret.checkCount = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
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

    /**
     * 用模板创建新的表达式片段。
     */
    public IExpr createNew(QuestInfo qinfo) {
        return new C_HasItem();
    }

    /**
     * 取得模板名称。
     */
    public String getName() {
        return "是否拥有物品...";
    }

    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        if (checkTrue) {
            return "拥有物品  " + EditorApplication.getProj().findItemOrEquipment(itemID) + " 至少 " + checkCount + " 个";
        } else {
            return "拥有物品  " + EditorApplication.getProj().findItemOrEquipment(itemID) + " 不足 " + checkCount + " 个";
        }
    }

    // 下面是IPropertySource接口的实现

    /**
     * 取得属性描述符。这个模板有1个参数：参数。
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new ItemPropertyDescriptor("itemID", "物品"),
                new TextPropertyDescriptor("checkCount", "数量"),
                new ComboBoxPropertyDescriptor("checkTrue", "条件成立", new String[] { "是", "否" })
        };
    }

    /**
     * 取得属性当前值。
     */
    public Object getPropertyValue(Object id) {
        if ("itemID".equals(id)) {
            return itemID;
        } else if ("checkCount".equals(id)) {
            return String.valueOf(checkCount);
        } else if ("checkTrue".equals(id)) {
            return checkTrue ? 0 : 1;
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
        } else if ("checkCount".equals(id)) {
            int newValue = Integer.parseInt((String)value);
            if (newValue != checkCount) {
                checkCount = newValue;
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
