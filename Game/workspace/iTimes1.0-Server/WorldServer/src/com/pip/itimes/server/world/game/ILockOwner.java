package com.pip.itimes.server.world.game;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface ILockOwner {
    public void addLock(ILock lock);
    public void removeLock(ILock lock);
}
