package com.pip.itimes.server.world.refresh;


/**
 * @author Jeffery
 * @version 1.0
 */
public interface IRefreshObject extends IRefresh,ILockable{

    public static final byte NPC = 0;
    public static final byte MG = 1;
    public static final byte RESOURCE = 2;

    public boolean needLock();
    public byte getType();
}
