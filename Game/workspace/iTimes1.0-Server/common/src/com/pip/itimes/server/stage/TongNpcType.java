package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class TongNpcType extends TaskNpcType{

    private int clazz;

    public TongNpcType(int id,String name,int type) {
        super(id,name,type);
    }

    public int getClazz(){
        return clazz;
    }

    public void setClazz(int clazz){
        this.clazz = clazz;
    }
}
