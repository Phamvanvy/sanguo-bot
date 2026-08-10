package com.pip.itimes.utils.action;

import com.pip.itimes.utils.*;
import java.util.Date;

/**
 * 给宠物喂食
 * @author Jeffrey
 * @version 1.0
 */
public class FeedAction
    extends AbstractAction {

    private IAward award;

    public FeedAction(int source,IAward award,Date date) {
        this.source = source;
        this.award = award;
        this.time = date;
    }

    //给宠物喂的物品
    public IAward getAward(){
        return award;
    }

    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
