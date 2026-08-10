package com.pip.itimes.server.world.game;

/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class AbstractServerObject implements IServerObject {

    private int id;
    private int second;
    private GameMap map;
    private int status;

    public AbstractServerObject(GameMap map,int id, int second) {
        this.map = map;
        this.id = id;
        this.second = second;
    }

    public int getId() {
        return id;
    }




    public int getRefreshSecond() {
        return second;
    }

    public GameMap getMap(){
        return map;
    }

    public void setStatus(int status){
        this.status = status;
        if(status==IServerObject.STATUS_INVISIBLE){
            if(getCallback()!=null){
                getCallback().objectDisappeared(this);
            }
        }
        else if(status==IServerObject.STATUS_VISIBLE){
            if(getCallback()!=null){
                getCallback().objectCreated(this);
            }
        }
    }

    public int getStatus(){
        return status;
    }
}
