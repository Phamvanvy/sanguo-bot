package com.pip.itimes.utils.action;

import com.pip.itimes.utils.*;
import java.util.Date;

/**
 * 采集
 * @author Jeffrey
 * @version 1.0
 */
public class GatherAction
    extends AbstractAction {

    private IAward[] award;

    public GatherAction(int source,IAward[] award,Date date) {
        this.source = source;
        this.award = award;
        this.time = date;
    }

    //采集到的物品
    public IAward[] getAwards(){
        return award;
    }

    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
