package com.pip.itimes.utils.action;

import com.pip.itimes.utils.IVisitor;
import com.pip.itimes.utils.IAward;
import java.util.Date;

/**
 * 使用物品
 * @author Jeffrey
 * @version 1.0
 */
public class UseItemAction extends AbstractAction{

    private IAward award;

    public UseItemAction(int source,IAward award,Date date) {
        this.source = source;
        this.award = award;
        this.time = date;
    }

    //使用的物品
    public IAward getAward(){
        return award;
    }

    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
