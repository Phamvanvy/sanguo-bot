package com.pip.itimes.utils.action;

import com.pip.itimes.utils.IAward;
import java.util.Date;
import com.pip.itimes.utils.IVisitor;

/**
 * 发送信件
 * @author Jeffrey
 * @version 1.0
 */
public class SendAttachmentAction
    extends AbstractAction {

    private int dest;
    private IAward award;
    private int price;

    public SendAttachmentAction(int source,int dest,int price,Date date,IAward award) {
        this.source = source;
        this.dest = dest;
        this.time = date;
        this.award = award;
        this.price = price;
    }

    //信件的附件
    public IAward getAward(){
        return award;
    }

    //收件人
    public int getDest(){
        return dest;
    }

    //价格
    public int getPrice(){
        return price;
    }

    public void accept(IVisitor visitor){
        visitor.visit(this);
    }
}
