package com.pip.itimes.server.stage;

public class ArenaSingupNpcType extends TaskNpcType{
    private int arenaType;

    public int getArenaType(){
        return arenaType;
    }

    public void setArenaType(int arenaType){
        this.arenaType = arenaType;
    }

    public ArenaSingupNpcType(int id, String name, int type){
        super(id, name, type);
    }
}
