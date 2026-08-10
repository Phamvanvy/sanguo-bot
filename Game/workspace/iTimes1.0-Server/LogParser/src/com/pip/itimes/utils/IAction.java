package com.pip.itimes.utils;

import java.util.Date;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface IAction {
    public int getSource();
    public Date getTime();
    public void accept(IVisitor visitor);
}
