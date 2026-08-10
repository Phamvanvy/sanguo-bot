package com.pip.itimes.server.world.refresh;


/**
 * @author Jeffrey
 * @version 1.0
 */
public interface IRefreshCallback {
    public void objectCreated(IRefreshObject object);
    public void objectDisappeared(IRefreshObject object);
    public void objectLocked(IRefreshObject object);
    public void objectReleased(IRefreshObject object);
}
