package com.pip.itimes.server.world.refresh;


import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffery
 * @version 1.0
 */
public class Lock {

    public static final Lock NO_LOCK = new Lock(null,null);;

    private static int lockId = 0;

    private PlayerData owner;
    private IRefreshObject object;

    private int id;

    public Lock(PlayerData owner,IRefreshObject object) {
        synchronized(Lock.class){
            id = lockId++;
        }
        this.owner = owner;
        this.object = object;
    }

    public int getId(){
        return id;
    }

    public PlayerData getOwner(){
        return owner;
    }

    public IRefreshObject getObject(){
        return object;
    }

    public int getResourceId(){
        return object.getId();
    }
}
