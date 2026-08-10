package com.pip.itimes.server.world.game;

import com.pip.itimes.server.stage.BossLocalTips;
import com.pip.itimes.server.stage.MonsterGroup;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ChatService;


/**
 * @author Jeffrey
 * @version 1.0
 */
public class MonsterObject extends AbstractServerObject{

    private MonsterGroup mg;
    private ILockOwner owner = null;
    private WorldService service;
    
    public MonsterObject(GameMap map, MonsterGroup mg,WorldService service) {
        super(map, mg.getId(), mg.getRefreshSecond());
        this.service = service;
        this.mg = mg;
        setStatus(IServerObject.STATUS_VISIBLE);
    }
    public MonsterGroup getMonsterGroup(){
        return mg;
    }

    public void lock(ILockOwner owner) throws LockException {
        synchronized(this){
            if (getStatus() != IServerObject.STATUS_VISIBLE) {
                throw new IllegalStatusException();
            }
            if(this.owner==null){
                owner.addLock(this);
                this.owner = owner;
                setStatus(IServerObject.STATUS_INVISIBLE);
            }else{
                throw new TooMuchLockException();
            }
        }
    }

    public void release(ILockOwner owner, boolean complete) throws
            LockException {
        synchronized(this){
            if(this.owner!=owner)
                throw new NoLockedException();
            else{
                this.owner = null;
                owner.removeLock(this);
                setStatus(IServerObject.STATUS_INVISIBLE);
                if(getRefreshSecond()>0)
                    getRefreshService().queue(this,getRefreshSecond());
            }
        }
    }

    public void releaseAll() throws LockException {
        if(owner!=null){
            release(owner,true);
        }
    }

    public void cancel(ILockOwner owner) {
        synchronized(this){
            if(this.owner==owner){
                this.owner = null;
                owner.removeLock(this);
                setStatus(IServerObject.STATUS_VISIBLE);
            }
        }
    }

    public void cnacelAll() {
        synchronized(this){
            if(owner!=null)
                cancel(owner);
        }
    }

    public void refresh() {
        setStatus(IServerObject.STATUS_VISIBLE);
        //mengjie add 服务器怪发地区聊
    	String bosslocalTip[] = BossLocalTips.getTip(mg.getId());
        if(bosslocalTip!=null){
        	String mapid[] = Utils.splitString(bosslocalTip[0], ',');
        	for (int i = 0; i < mapid.length; i++) {
        			service.getChatService().sendMapMessage(
        					Short.valueOf(mapid[i]).shortValue(), -1,"系统",
        					bosslocalTip[1]);	
        	}
        }
    }

    public int getRefreshSecond(){
        return mg.getRefreshSecond();
    }

    public RefreshService getRefreshService(){
        return service.getRefreshService();
    }

    public IRefreshCallback getCallback(){
        return service;
    }

    public short getX(){
        return mg.getX();
    }

    public short getY(){
        return mg.getY();
    }
}
