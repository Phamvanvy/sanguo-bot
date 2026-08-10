package com.pip.itimes.utils.action;

import com.pip.itimes.utils.*;
import java.util.Date;

/**
 * PK开始
 * @author Jeffrey
 * @version 1.0
 */
public class PkBeginAction
    extends AbstractAction {

    private int dest;
    private int sourceMoney;
    private int destMoney;
    private int wager;

    public PkBeginAction(int source,int dest,int sourceMoney,int destMoney,int wager,Date date) {
        this.source = source;
        this.dest = dest;
        this.sourceMoney = sourceMoney;
        this.destMoney = destMoney;
        this.wager = wager;
        this.time = date;
    }

    //对方的ID
    public int getDest(){
        return dest;
    }

    //发起方当时拥有的金钱
    public int getSourceMoney(){
        return sourceMoney;
    }

    //对方当时拥有的金钱
    public int getDestMoney(){
        return destMoney;
    }

    //赌注
    public int getWager(){
        return wager;
    }

    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
