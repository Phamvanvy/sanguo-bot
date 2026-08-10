package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.PQEUtils;

public class C_KillPlayersLimited extends AbstractExpr {

    public int maxKills0fOnePlayer;
    public int maxKillsOfPlayers;
    public int rewardRatio;
    
    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public C_KillPlayersLimited() {
        this.maxKills0fOnePlayer = 0;
        this.maxKillsOfPlayers = 0;
        this.rewardRatio = 0;
    }
    
    /**
     * 用模板创建新的表达式片段。
     */
    public IExpr createNew(QuestInfo qinfo) {
        return new C_KillPlayersLimited();
    }

    /**
     * 取得模板名称。
     */
    public String getName() {
        return "防刷机制杀死玩家";
    }

    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        return "防刷机制杀死玩家 ";
    }

    public String getExpression() {
        return "E_KillPlayersLimited(" + maxKills0fOnePlayer + "," + maxKillsOfPlayers + "," + rewardRatio + ")";
    }

    public boolean isCondition() {
        return true;
    }

    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("E_KillPlayersLimited") && expr.getRightExpr() == null) {
            if (expr.getLeftExpr().getFunctionCall().getParamCount() != 3) {
                return null;
            }
            Expression param1 = expr.getLeftExpr().getFunctionCall().getParam(0);
            Expression param2 = expr.getLeftExpr().getFunctionCall().getParam(1);
            Expression param3 = expr.getLeftExpr().getFunctionCall().getParam(2);
            if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param3.getRightExpr() == null && param3.getLeftExpr().type == Expr0.TYPE_NUMBER) {
                C_KillPlayersLimited ret = (C_KillPlayersLimited)createNew(qinfo);
                ret.maxKills0fOnePlayer = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
                ret.maxKillsOfPlayers = PQEUtils.translateNumberConstant(param2.getLeftExpr().value);
                ret.rewardRatio = PQEUtils.translateNumberConstant(param3.getLeftExpr().value);
                return ret;
            }
        }
        return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] { 
                new TextPropertyDescriptor("maxkillsofoneplayer", "个人上限"),
                new TextPropertyDescriptor("maxkillsofplayers", "总上限"),
                new TextPropertyDescriptor("rewardratio", "几率(万分之)")
        };
    }

    public Object getPropertyValue(Object id) {
        if ("maxkillsofoneplayer".equals(id)) {
            return String.valueOf(maxKills0fOnePlayer);
        } else if("maxkillsofplayers".equals(id)){
            return String.valueOf(maxKillsOfPlayers);
        } else if("rewardratio".equals(id)){
            return String.valueOf(rewardRatio);
        }
        return null;
    }

    public void setPropertyValue(Object id, Object value) {
        if("maxkillsofoneplayer".equals(id)) {
            int newValue = Integer.parseInt((String)value);
            if(newValue!=maxKills0fOnePlayer){
                maxKills0fOnePlayer = newValue;
                fireValueChanged();
            }
        }else if("maxkillsofplayers".equals(id)) {
            int newValue = Integer.parseInt((String)value);
            if(newValue!=maxKillsOfPlayers){
                maxKillsOfPlayers = newValue;
                fireValueChanged();
            }
        }else if("rewardratio".equals(id)) {
            int newValue = Integer.parseInt((String)value);
            if(newValue!=rewardRatio){
                rewardRatio = newValue;
                fireValueChanged();
            }
        }
    }

}
