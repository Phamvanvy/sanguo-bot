package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.ItemCellEditor;
import com.pip.sanguo.editor.property.ItemPropertyDescriptor;
import com.pip.sanguo.editor.property.NPCPropertyDescriptor;

/**
 * 表达式模板：判断是否使用了某物品。
 * @author lighthu
 */
public class C_UseItem extends AbstractExpr {
    
    public int itemID;
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_UseItem() {
	    itemID = -1;
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_UseItem();
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "玩家使用物品...";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
	    return "玩家使用物品 " + EditorApplication.getProj().findItemOrEquipment(itemID);
	}
	
	/**
     * 取得属性描述符。这个模板有1个参数：参数。
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] { 
                new ItemPropertyDescriptor("itemID", "使用物品")
        };
    }

    /**
     * 取得属性当前值。
     */
    public Object getPropertyValue(Object id) {
        if ("itemID".equals(id)) {
            return new Integer(itemID);
        }
        return null;
    }

    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(Object id, Object value) {
        if ("itemID".equals(id)) {
            int newValue = ((Integer)value).intValue();
            if (newValue != itemID) {
                itemID = newValue;
                fireValueChanged();
            }
        }
    }

    public String getExpression() {
        return "E_UseItem(" + itemID + ")";
    }

    public boolean isCondition() {
        return true;
    }

    /**
     * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
     */
    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("E_UseItem") && expr.getRightExpr() == null) {
            if (expr.getLeftExpr().getFunctionCall().getParamCount() != 1) {
                return null;
            }
            Expression param1 = expr.getLeftExpr().getFunctionCall().getParam(0);
            if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER) {
                C_UseItem ret = (C_UseItem)createNew(qinfo);
                ret.itemID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
                return ret;
            }
        }
        return null;
    }
    
    
}
