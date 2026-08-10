package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.Title;
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
import com.pip.sanguo.editor.property.TitlePropertyDescriptor;

/**
 * 表达式模板：是否拥有某个称号。
 * @author lighthu
 */
public class C_HasTitle extends AbstractExpr {
    public int titleID;
    
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_HasTitle() {
	    titleID = 1;
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_HasTitle();
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "拥有称号...";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
	    Title t = (Title)EditorApplication.getProj().findObject(Title.class, titleID);
	    return "拥有称号 " + (t == null ? "未知" : t.title);
	}
	
	/**
     * 取得属性描述符。这个模板有1个参数：参数。
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] { 
                new TitlePropertyDescriptor("title", "称号")
        };
    }

    /**
     * 取得属性当前值。
     */
    public Object getPropertyValue(Object id) {
        if ("title".equals(id)) {
            return new Integer(titleID);
        }
        return null;
    }

    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(Object id, Object value) {
        if ("title".equals(id)) {
            int newValue = ((Integer)value).intValue();
            if (newValue != titleID) {
                titleID = newValue;
                fireValueChanged();
            }
        }
    }

    public String getExpression() {
        return "HasTitle(" + titleID + ")";
    }

    public boolean isCondition() {
        return true;
    }

    /**
     * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
     */
    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("HasTitle") && expr.getRightExpr() == null) {
            if (expr.getLeftExpr().getFunctionCall().getParamCount() != 1) {
                return null;
            }
            Expression param1 = expr.getLeftExpr().getFunctionCall().getParam(0);
            if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER) {
                C_HasTitle ret = (C_HasTitle)createNew(qinfo);
                ret.titleID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
                return ret;
            }
        }
        return null;
    }
    
    
}
