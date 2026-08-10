package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.FunctionCall;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.editor.property.NPCPropertyDescriptor;
import com.pip.sanguo.editor.property.RichTextPropertyDescriptor;

/**
 * 表达式模板：NPC冒泡。
 * @author zlguo
 */
public class A_Bubble extends AbstractExpr{
    public int npcID;
    public String content;
    public int lastTime;
    
    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public A_Bubble(){
        npcID = -1;
        content = "";
        lastTime = -1;
    }

    /**
     * 用模板创建新的表达式片段。
     */
    public IExpr createNew(QuestInfo qinfo) {
        return new A_Bubble();
    }

    public String getExpression() {
        return "Bubble(" + npcID + ", \"" + PQEUtils.reverseConv(content) + "\"," + lastTime + ")";
    }

    /**
     * 取得模板名称。
     */
    public String getName() {
        return "NPC冒泡...";
    }

    /**
     * 判断这个模板是一个条件还是一个动作。
     */
    public boolean isCondition() {
        return false;
    }

    /**
     * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
     */
    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("Bubble") && expr.getRightExpr() == null) {
            FunctionCall fc = expr.getLeftExpr().getFunctionCall();
            if (fc.getParamCount() != 3) {
                return null;
            }
            Expression param1 = fc.getParam(0);
            Expression param2 = fc.getParam(1);
            Expression param3 = fc.getParam(2);
            if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_STRING &&
                param3.getRightExpr() == null && param3.getLeftExpr().type == Expr0.TYPE_NUMBER) {
                A_Bubble ret = (A_Bubble)createNew(qinfo);
                ret.npcID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
                ret.content = PQEUtils.translateStringConstant(param2.getLeftExpr().value);
                ret.lastTime = PQEUtils.translateNumberConstant(param3.getLeftExpr().value);
                return ret;
            }
        }
        return null;
    }
    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        return "NPC冒泡...";
    }
    
 // 下面是IPropertySource接口的实现

    /**
     * 取得属性描述符。这个模板有2个参数：字符串参数和整数参数。
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] { 
                new NPCPropertyDescriptor("npcID", "NPC"),
                new RichTextPropertyDescriptor("content", "气泡内容", questInfo),
                new TextPropertyDescriptor("lastTime", "持续时间")
        };
    }

    public Object getPropertyValue(Object id) {
        if ("npcID".equals(id)) {
            return new Integer(npcID);
        }
        else if ("content".equals(id)) {
            return content;
        }
        else if ("lastTime".equals(id)) {
            return "" + lastTime;
        }
        return null;
}

    public void setPropertyValue(Object id, Object value) {
        if ("npcID".equals(id)) {
            int newValue = ((Integer)value).intValue();
            if (newValue != npcID) {
                npcID = newValue;
                fireValueChanged();
            }
        } else if ("content".equals(id)) {
            String newValue = (String)value;
            if (!newValue.equals(content)) {
                content = newValue;
                fireValueChanged();
            }
        } else if ("lastTime".equals(id)) {
            int newValue = Integer.parseInt((String)value);
            if (newValue != lastTime) {
                lastTime = newValue;
                fireValueChanged();
            }
        }
    }

}
