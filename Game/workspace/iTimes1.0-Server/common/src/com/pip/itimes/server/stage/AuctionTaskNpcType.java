package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class AuctionTaskNpcType extends TaskNpcType{

    private int clazz;
    private short areaId;

    public AuctionTaskNpcType(int id,String name,int type) {
            super(id,name,type);
    }

    public int getClazz(){
        return clazz;
    }

    public void setClazz(int clazz){
        this.clazz = clazz;
    }

    public short getAreaId(){
        return areaId;
    }

    public void setAreaId(short areaId){
        this.areaId = areaId;
    }
}
