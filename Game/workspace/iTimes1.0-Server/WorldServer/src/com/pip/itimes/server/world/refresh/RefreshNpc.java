package com.pip.itimes.server.world.refresh;


import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffery
 * @version 1.0
 */
public class RefreshNpc extends AbstractRefreshObject{

    public RefreshNpc(int id) {
        super(id);
        setType(IRefreshObject.NPC);
        setNeedLock(false);
    }

    public Lock lock(PlayerData owner) throws LockException{
        return null;
    }

    public void release(Lock lock,boolean completed) throws LockException{
    }

    public boolean isEmpty(){
        return false;
    }
}
