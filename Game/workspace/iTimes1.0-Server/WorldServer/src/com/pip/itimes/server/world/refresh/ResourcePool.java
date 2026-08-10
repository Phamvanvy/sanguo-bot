package com.pip.itimes.server.world.refresh;

import com.pip.itimes.server.stage.Resource;
import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffery
 * @version 1.0
 */
public class ResourcePool extends LockRefreshPool{

    public ResourcePool() {

    }

    public void addResource(Resource resource){
        RefreshResource r = new RefreshResource(resource,3);
        r.setRefreshSecond(resource.getRefreshSecond());
        addRefreshObject(r);
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
            return ret;
        }
    }

    public void release(Lock lock, boolean completed) throws LockException {
        synchronized (this) {
            IRefreshObject o = lock.getObject();
            boolean v1 = o.isVisible();
            o.release(lock,completed);
            boolean v2 = o.isVisible();
            if (o.isEmpty() && !v2) {
                schedule(o);
            }
            if(!v2&&v1)
                objectDisappeared(o);
        }
    }

}
