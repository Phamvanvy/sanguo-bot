package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class StoreNpcType extends TaskNpcType{

    public short areaId;
    public int group;
    public int discount;
    public int clazz;

    public StoreNpcType(int id,String name,int type) {
        super(id,name,type);
        this.clazz = clazz;
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

    public int getClazz() {
        return clazz;
    }

    public void setDiscount(int discount){
        this.discount = discount;
    }

    public void setClazz(int clazz) {
        this.clazz = clazz;
    }
}
