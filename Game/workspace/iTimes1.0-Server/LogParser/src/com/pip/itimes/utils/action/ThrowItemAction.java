package com.pip.itimes.utils.action;

import com.pip.itimes.utils.*;
import java.util.Date;

/**
 * 扔掉物品
 * @author Jeffrey
 * @version 1.0
 */
public class ThrowItemAction
    extends AbstractAction {

    private IAward award;

    public ThrowItemAction(int source,IAward award,Date date) {
        this.source = source;
        this.award = award;
        this.time = date;
    }

    //扔掉的物品
    public IAward getAward(){
        return award;
    }


    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
