package com.pip.itimes.server.world.refresh;


/**
 * @author Jeffery
 * @version 1.0
 */
public abstract class RefreshObject {
    private Object object;
    private int id;
    private boolean visible;
    private int refreshSecond;
    private int lockTime;

    public RefreshObject(int id,Object o) {
        this.id = id;
        this.object = o;
    }

    public Object getObject(){
        return object;
    }

    public int getId(){
        return id;
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }

    public boolean isVisible(){
        return visible;
    }

    public int getRefreshSecond(){
        return refreshSecond;
    }

    public void setRefreshSecond(int refreshSecond){
        this.refreshSecond = refreshSecond;
    }

    public int getLockTime(){
        return lockTime;
    }

    public void setLockTime(int lockTime){
        this.lockTime = lockTime;
    }

    abstract public boolean lock();
}
