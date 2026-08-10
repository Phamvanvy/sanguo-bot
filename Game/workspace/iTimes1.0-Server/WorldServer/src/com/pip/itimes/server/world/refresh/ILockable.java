package com.pip.itimes.server.world.refresh;


import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffery
 * @version 1.0
 */
public interface ILockable {
    public int getId();
    public Lock lock(PlayerData owner) throws LockException;
    public void release(Lock lock,boolean completed) throws LockException;
    public int getLockTimeOut();
    public boolean isEmpty();
}
