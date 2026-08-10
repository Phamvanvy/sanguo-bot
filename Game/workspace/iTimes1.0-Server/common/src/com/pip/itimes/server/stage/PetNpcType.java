package com.pip.itimes.server.stage;

/**
 * @author JEffery
 * @version 1.0
 */
public class PetNpcType extends TaskNpcType{

    private int clazz;

    public PetNpcType(int id,String name,int type) {
        super(id,name,type);
    }

    public int getClazz(){
        return clazz;
    }

    public void setClazz(int clazz){
        this.clazz = clazz;
    }
}
