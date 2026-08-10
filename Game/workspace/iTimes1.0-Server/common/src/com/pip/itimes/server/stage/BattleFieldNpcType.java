package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class BattleFieldNpcType extends TaskNpcType {

    private int clazz;

    public BattleFieldNpcType(int id, String name, int type) {
        super(id, name, type);
    }

    public int getClazz(){
        return clazz;
    }

    public void setClazz(int clazz){
        this.clazz = clazz;
    }
}
