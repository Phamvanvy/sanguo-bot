package com.pip.itimes.server.world.refresh;

import com.pip.itimes.server.stage.MonsterGroup;
import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffery
 * @version 1.0
 */
public class MGPool extends LockRefreshPool{
    public MGPool() {
    }

    public void addMG(MonsterGroup mg){
        RefreshMG m = new RefreshMG(mg.getId());
        m.setRefreshSecond(mg.getRefreshSecond());
        m.setVisible(mg.isVisible());
        addRefreshObject(m);
    }


    public Lock lock(int id, PlayerData player) throws LockException {
        synchronized (this) {
            IRefreshObject o = (IRefreshObject) objects.get(new Integer(id));
            if (o == null)
                throw new LockException(LockException.INEXISTENCE);
            if (!o.isVisible()) {
                throw new LockException(LockException.INVISIBLE);
            }
            Lock ret = o.lock(player);
            objectLocked(o);
            o.setVisible(false);
            objectDisappeared(o);
            return ret;
        }
    }

    public void release(Lock lock, boolean completed) throws LockException {
        synchronized (this) {
            IRefreshObject o = lock.getObject();
            o.release(lock,completed);
            boolean v2 = o.isVisible();
            if(v2){
                objectCreated(o);
            }else{
                schedule(o);
                objectDisappeared(o);
            }
        }
    }


}
