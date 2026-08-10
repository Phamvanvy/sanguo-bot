package com.pip.itimes.utils.action;

import com.pip.itimes.utils.IAward;
import java.util.Date;
import com.pip.itimes.utils.IVisitor;

/**
 * 出卖物品
 * @author Jeffrey
 * @version 1.0
 */
public class SellItemAction
    extends AbstractAction {

    private int price;
    private IAward award;

    public SellItemAction(int source,int price,IAward award,Date date) {
        this.source = source;
        this.price = price;
        this.award = award;
        this.time = date;
    }

    //物品价格
    public int getPrice(){
        return price;
    }

    //出卖的物品
    public IAward getAward(){
        return award;
    }

    public void accept(IVisitor visitor){
        visitor.visit(this);
    }
}
