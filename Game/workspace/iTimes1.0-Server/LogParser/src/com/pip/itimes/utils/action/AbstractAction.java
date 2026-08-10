package com.pip.itimes.utils.action;

import com.pip.itimes.utils.IAction;
import java.util.Date;
import com.pip.itimes.utils.IVisitor;

/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class AbstractAction
    implements IAction {

    protected int source;
    protected Date time;

    public AbstractAction() {
    }

    public int getSource() {
        return source;
    }

    public Date getTime() {
        return time;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setTime(Date time) {
        this.time = time;
    }

}
