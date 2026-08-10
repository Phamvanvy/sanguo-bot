package com.pip.itimes.server.world.refresh;

import com.pip.itimes.server.stage.Npc;
import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffery
 * @version 1.0
 */
public class NpcPool extends LockRefreshPool{
    public NpcPool() {
    }

    public void addNpc(Npc npc){
        RefreshNpc n = new RefreshNpc(npc.getId());
        n.setRefreshSecond(npc.getRefreshSecond());
        addRefreshObject(n);
    }

    public Lock lock(int id, PlayerData player) throws LockException {
        return null;
    }


    public void release(Lock lock, boolean complete) throws LockException {
    }


}
