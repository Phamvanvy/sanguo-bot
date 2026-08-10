package com.pip.itimes.server.world.game;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface ILock {
    public void lock(ILockOwner owner) throws LockException;
    public void release(ILockOwner owner,boolean complete) throws LockException;
    public void releaseAll() throws LockException;
    public void cancel(ILockOwner owner);
    public void cnacelAll();
}
