package com.pip.itimes.server.world.refresh;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.Resource;
import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffery
 * @version 1.0
 */
public class RefreshResource extends AbstractRefreshObject{

    private int lockCount;
    private Map lock2player = new HashMap(3);
    private Resource resource;
    private boolean isEmpty;

    public RefreshResource(Resource resource,int lockCount) {
        super(resource.getId());
        this.resource = resource;
        this.lockCount = lockCount;
        setType(IRefreshObject.RESOURCE);
        setVisible(true);
        setNeedLock(true);
    }

    public Lock lock(PlayerData owner) throws LockException{
        if(lock2player.containsValue(owner)){
            Iterator ite = lock2player.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry entry = (Map.Entry)ite.next();
                if(entry.getValue()==owner){
                    return (Lock) entry.getKey();
                }
            }
        }
        if(!isVisible()){
            throw new LockException(LockException.INVISIBLE);
        }
        if(lock2player.size()<lockCount){
            Lock lock = new Lock(owner,this);
            lock2player.put(lock,owner);
            return lock;
        }else{
            throw new LockException(LockException.LOCK_FULL);
        }
    }

    public void release(Lock lock,boolean completed) throws LockException{
        if(lock2player.containsKey(lock)){
            lock2player.remove(lock);
            setVisible(false);
            if(lock2player.isEmpty()){
                isEmpty = true;
            }
        }else{
            throw new LockException(LockException.NO_LOCKED);
        }
    }

    public boolean isEmpty(){
        return isEmpty;
    }

    public Resource getResource(){
        return resource;
    }
}
