package com.pip.itimes.utils.action;

import com.pip.itimes.utils.*;
import java.util.Date;

/**
 * µÇÂ¼
 * @author Jeffrey
 * @version 1.0
 */
public class LoginAction
    extends AbstractAction {

    private int level;

    public LoginAction(int source,int level,Date date) {
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
