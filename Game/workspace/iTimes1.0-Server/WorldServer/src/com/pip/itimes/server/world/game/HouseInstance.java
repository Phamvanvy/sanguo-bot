package com.pip.itimes.server.world.game;

import com.pip.itimes.server.world.InstanceDefinition;

public class HouseInstance extends Instance {

    private int ownerId;

    public HouseInstance(int id, InstanceDefinition idf, InstanceService service) {
        super(id, idf, service);
    }

    public int getOwnerId(){
        return ownerId;
    }

    public void setOwnerId(int ownerId){
        this.ownerId = ownerId;
    }

}
