package com.pip.itimes.utils.action;

import com.pip.itimes.utils.*;
import java.util.Date;

/**
 * PK½áÊø
 * @author Jeffrey
 * @version 1.0
 */
public class PkEndAction
    extends AbstractAction {
    private int dest;
    private int sourceMoney;
    private int destMoney;
    private int wager;

    public PkEndAction(int source,int dest,int sourceMoney,int destMoney,int wager,Date date) {
        this.source = source;
        this.dest = dest;
        this.sourceMoney = sourceMoney;
        this.destMoney = destMoney;
        this.wager = wager;
        this.time = date;
    }

    public int getDest(){
        return dest;
    }

    public int getSourceMoney(){
        return sourceMoney;
    }

    public int getDestMoney(){
        return destMoney;
    }

    public int getWager(){
        return wager;
    }

    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
