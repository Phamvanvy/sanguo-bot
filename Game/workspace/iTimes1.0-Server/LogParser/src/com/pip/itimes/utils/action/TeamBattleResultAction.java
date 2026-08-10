package com.pip.itimes.utils.action;

import com.pip.itimes.utils.*;
import java.util.Date;

/**
 * 组队战斗结束
 * @author Jeffrey
 * @version 1.0
 */
public class TeamBattleResultAction
    extends AbstractAction {

    private int monsterId;
    private IAward[] awards;

    public TeamBattleResultAction(int source,int monsterId,IAward[] awards,Date date) {
        this.source = source;
        this.monsterId = monsterId;
        this.awards = awards;
        this.time = date;
    }

    //战胜的怪物ID
    public int getMonsterId(){
        return monsterId;
    }

    //获得的物品
    public IAward[] getAwards(){
        return awards;
    }


    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
