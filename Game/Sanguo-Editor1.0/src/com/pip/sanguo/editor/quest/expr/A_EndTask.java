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
import com.pip.sanguo.editor.property.RewardSetPropertyDescriptor;

/**
 * 表达式模板：强制交任务。
 * @author lighthu
 */
public class A_EndTask extends AbstractExpr {
    public int rewardID;
    
    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public A_EndTask(QuestInfo qinfo) {
        questInfo = qinfo;
        if (qinfo != null && qinfo.owner.rewards.size() > 0) {
            rewardID = qinfo.owner.rewards.get(0).id;
        }
    }
    
    /**
     * 判断这个模板是一个条件还是一个动作。
     */
    public boolean isCondition() {
        return false;
    }

    /**
     * 取得生成的表达式。
     */
    public String getExpression() {
        return "EndTask(" + rewardID + ")";
    }

    /**
     * 用模板创建新的表达式片段。
     */
    public IExpr createNew(QuestInfo qinfo) {
        return new A_EndTask(qinfo);
    }

    /**
     * 取得模板名称。
     */
    public String getName() {
        return "强制结束任务";
    }

    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        return "强制结束任务，奖励分支 " + rewardID;
    }

    /**
     * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
     */
    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("EndTask") && expr.getRightExpr() == null) {
            FunctionCall fc = expr.getLeftExpr().getFunctionCall();
            if (fc.getParamCount() != 1) {
                return null;
            }
            Expression param1 = fc.getParam(0);
            if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER) {
                A_EndTask ret = (A_EndTask)createNew(qinfo);
                ret.rewardID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
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
                new RewardSetPropertyDescriptor("rewardID", "奖励分支", questInfo)
        };
    }

    /**
     * 取得属性当前值。
     */
    public Object getPropertyValue(Object id) {
        if ("rewardID".equals(id)) {
            for (int i = 0; i < questInfo.owner.rewards.size(); i++) {
                if (questInfo.owner.rewards.get(i).id == rewardID) {
                    return i;
                }
            }
            return 0;
        }
        return null;
    }

    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(Object id, Object value) {
        if ("rewardID".equals(id)) {
            int newValue = ((Integer)value).intValue();
            if (newValue >= 0 && newValue < questInfo.owner.rewards.size()) {
                newValue = questInfo.owner.rewards.get(newValue).id;
            } else {
                newValue = 0;
            }
            if (newValue != rewardID) {
                rewardID = newValue;
                fireValueChanged();
            }
        }
    }
}
