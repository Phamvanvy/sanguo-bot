package com.pip.itimes.server.world.refresh;


/**
 * @author Jeffery
 * @version 1.0
 */
public interface IRefresh {
    public int getId();
    public int getRefreshSecond();
    public boolean isVisible();
    public void setVisible(boolean visible);
}
