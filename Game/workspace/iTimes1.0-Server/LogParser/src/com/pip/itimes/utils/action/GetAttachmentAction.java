package com.pip.itimes.utils.action;

import com.pip.itimes.utils.IAward;
import java.util.Date;
import com.pip.itimes.utils.IVisitor;

/**
 * 收取信件
 * @author Jeffrey
 * @version 1.0
 */
public class GetAttachmentAction extends AbstractAction{

    private IAward award;

    public GetAttachmentAction(int id,Date time,IAward award) {
        this.source = id;
        this.time = time;
        this.award = award;
    }

    //新建的附件
    public IAward getAward(){
        return award;
    }

    /**
     * accept
     *
     * @param visitor IVisitor
     * @todo Implement this com.pip.itimes.utils.IAction method
     */
    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }

}
