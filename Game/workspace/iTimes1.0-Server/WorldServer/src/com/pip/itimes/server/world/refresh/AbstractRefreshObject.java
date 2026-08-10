package com.pip.itimes.server.world.refresh;


import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffery
 * @version 1.0
 */
public abstract class AbstractRefreshObject implements IRefreshObject {

    private boolean visible;
    private int refreshSecond;
    private int id;
    private boolean needLock;
    private int lockTimeOut;
    private byte type;

    public AbstractRefreshObject(int id) {
        this.id = id;
    }

    public void setNeedLock(boolean needLock){
        this.needLock = needLock;
    }

    public boolean needLock() {
        return needLock;
    }


    public int getId() {
        return id;
    }

    public void setRefreshSecond(int refreshSecond){
        this.refreshSecond = refreshSecond;
    }

    public int getRefreshSecond() {
        return refreshSecond;
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }





    abstract public Lock lock(PlayerData owner) throws LockException;


    abstract public void release(Lock lock,boolean completed) throws LockException;

    public void setLockTimeOut(int timeOut){
        this.lockTimeOut = timeOut;
    }

    public int getLockTimeOut() {
        return lockTimeOut;
    }


    public void setType(byte type){
        this.type = type;
    }

    public byte getType(){
        return type;
    }
}
