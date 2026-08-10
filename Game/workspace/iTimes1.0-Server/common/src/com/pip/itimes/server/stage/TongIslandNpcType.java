package com.pip.itimes.server.stage;

public class TongIslandNpcType extends TaskNpcType {

    protected int islandId;

    public TongIslandNpcType(int id, String name, int type) {
        super(id, name, type);
    }

    public void setIslandId(int islandId){
        this.islandId = islandId;
    }

    public int getIslandId(){
        return islandId;
    }
}
