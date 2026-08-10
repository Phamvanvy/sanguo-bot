package com.pip.itimes.server.world.game;

import java.util.ArrayList;
import java.util.List;

import com.pip.itimes.server.stage.Resource;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ResourceObject extends AbstractServerObject {

    private Resource resource;
    private List owners = new ArrayList(3);
    private WorldService service;

    public ResourceObject(GameMap map, Resource resource,WorldService service) {
        super(map, resource.getId(), resource.getRefreshSecond());
        this.resource = resource;
        this.service = service;
        setStatus(IServerObject.STATUS_VISIBLE);
    }


    public void lock(ILockOwner owner) throws LockException {
        synchronized(this){
            if (getStatus() != IServerObject.STATUS_VISIBLE) {
                throw new IllegalStatusException();
            }
            if (!owners.contains(owner)) {
                if (owners.size() >= 3)
                    throw new TooMuchLockException();
                owner.addLock(this);
                owners.add(owner);
                if(owners.size()>=3){
                    setStatus(IServerObject.STATUS_INVISIBLE);
                }
            }
        }
    }

    public void release(ILockOwner owner, boolean complete) throws
            LockException {
        synchronized(this){
            if (!owners.contains(owner))
                throw new NoLockedException();
            owners.remove(owner);
            owner.removeLock(this);
            if(owners.size()==0){
                setStatus(IServerObject.STATUS_INVISIBLE);
                if(getRefreshSecond()>0)
                    getRefreshService().queue(this,getRefreshSecond());
            }
        }
    }

    public void releaseAll() throws LockException {
        synchronized(this){
            for(int i=0;i<owners.size();i++){
                ILockOwner owner = (ILockOwner)owners.get(i);
                release(owner,true);
            }
        }
    }

    public void cancel(ILockOwner owner) {
        synchronized(this){
            if(owners.contains(owner)){
                owners.remove(owner);
                owner.removeLock(this);
                if(owners.size()==0&&getStatus()==IServerObject.STATUS_INVISIBLE){
                    setStatus(IServerObject.STATUS_VISIBLE);
                }
            }
        }
    }

    public void cnacelAll() {
        synchronized(this){
            for(int i=0;i<owners.size();i++){
                ILockOwner owner = (ILockOwner)owners.get(i);
                cancel(owner);
            }
        }
    }

    public void refresh() {
        setStatus(IServerObject.STATUS_VISIBLE);
    }

    public int getRefreshSecond(){
        return resource.getRefreshSecond();
    }


    public RefreshService getRefreshService(){
        return service.getRefreshService();
    }

    public IRefreshCallback getCallback(){
        return service;
    }

    public byte getType() {
        return resource.getType();
    }

    public byte getLevel(){
        return resource.getLevel();
    }

    public int getItemId(){
        return resource.getItemId();
    }

    public short getX(){
        return resource.getX();
    }

    public short getY(){
        return resource.getY();
    }
}
