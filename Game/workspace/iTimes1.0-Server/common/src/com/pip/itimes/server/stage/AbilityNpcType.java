package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class AbilityNpcType extends TaskNpcType{

    public Ability[] abilities;
    public int clazz;

    public AbilityNpcType(int id,String name,int type) {
        super(id,name,type);
    }

    public Ability[] getAbilities(){
        return abilities;
    }

    public void setAbilitites(Ability[] abilities){
        this.abilities = abilities;
    }

    public int getClazz(){
        return clazz;
    }

    public void setClazz(int clazz){
        this.clazz = clazz;
    }
}
