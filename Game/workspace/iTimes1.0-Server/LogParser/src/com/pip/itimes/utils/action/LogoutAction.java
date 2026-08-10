package com.pip.itimes.utils.action;

import com.pip.itimes.utils.*;
import java.util.Date;

/**
 * µÇ³ö
 * @author Jeffrey
 * @version 1.0
 */
public class LogoutAction
    extends AbstractAction {

    private int level;

    public LogoutAction(int source,int level,Date date) {
        this.source = source;
        this.level = level;
        this.time = date;
    }

    //µÈ¼¶
    public int getLevel(){
        return level;
    }


    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
