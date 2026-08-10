package com.pip.itimes.server.stage;

/**
 * @author Lelonte
 * @version 1.0
 */
public class CStoreNpcType extends TaskNpcType{

    public short areaId;
    public int group;
    public int discount;

    public CStoreNpcType(int id, String name, int type) {
        super(id,name,type);
    }

    public void setAreaId(short areaId){
        this.areaId = areaId;
    }

    public short getAreaId(){
        return areaId;
    }

    public void setGroup(int group){
        this.group = group;
    }

    public int getGroup(){
        return group;
    }

    public int getDiscount(){
        return discount;
    }

    public void setDiscount(int discount){
        this.discount = discount;
    }
}
