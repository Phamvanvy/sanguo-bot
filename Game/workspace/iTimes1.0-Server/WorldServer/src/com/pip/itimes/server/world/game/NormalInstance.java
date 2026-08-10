package com.pip.itimes.server.world.game;

import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.WorldPlayer;

/**

 * @author Jeffrey
 * @version 1.0
 */
public class NormalInstance extends Instance {

    private long lastTime;

	public NormalInstance(int id,InstanceDefinition idf,InstanceService service) {
        super(id,idf,service);
        lastTime = System.currentTimeMillis();
    }

	public boolean setActive(int id) {
        boolean b = super.setActive(id);
        if(b){
            lastTime = 0;
            return true;
        }
        return false;
    }

    public boolean removeActive(int id){
        boolean b = super.removeActive(id);
        if(b&&activeIds.size()==0){
            lastTime = System.currentTimeMillis();
        }
        return b;
    }

    public boolean isTimeOut(){
        return lastTime!=0&&((System.currentTimeMillis()-lastTime)/1000>getRefreshSecond());
    }

    public int getMinLevel(){
        return definition.getMinLevel();
    }
}
