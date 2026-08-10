package com.pip.itimes.server.world.game;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface IRefreshCallback {
    public void objectCreated(IServerObject object);
    public void objectDisappeared(IServerObject object);
}
