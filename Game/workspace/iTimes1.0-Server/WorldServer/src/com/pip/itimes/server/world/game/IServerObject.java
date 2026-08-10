package com.pip.itimes.server.world.game;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface IServerObject extends ILock,IRefresh{

    public static final int STATUS_VISIBLE = 0;
    public static final int STATUS_INVISIBLE = 1;

    public int getId();
    public short getX();
    public short getY();
    public RefreshService getRefreshService();
    public int getStatus();
    public IRefreshCallback getCallback();
    public int getRefreshSecond();
    public GameMap getMap();
}
