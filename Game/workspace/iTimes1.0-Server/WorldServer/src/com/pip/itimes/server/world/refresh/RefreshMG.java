package com.pip.itimes.server.world.refresh;


import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.PlayerData;
import java.util.Map;
import java.util.Iterator;

/**
 * @author Jeffery
 * @version 1.0
 */
public class RefreshMG extends AbstractRefreshObject{

    private PlayerData player;
    private Lock lock;

    public RefreshMG(int id) {
        super(id);
        setType(IRefreshObject.MG);
    }

    public void release(Lock lock,boolean completed) throws LockException{
        if(this.lock==lock){
            if(completed){
                setVisible(false);
            }else{
                setVisible(true);
            }
            player = null;
            lock = null;
        }else{
            throw new LockException(LockException.NO_LOCKED);
        }
    }

    public Lock lock(PlayerData owner) throws LockException {
        synchronized (this) {
            if (player == owner) {
                return lock;
            }
            if (!isVisible()) {
                throw new LockException(LockException.INVISIBLE);
            }
            if (player == null) {
                Lock lock = new Lock(owner, this);
                this.lock = lock;
                this.player = owner;
                setVisible(false);
                return lock;
            }
            return null;
        }
    }

    public boolean isEmpty(){
        return true;
    }
}
