package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class Effect {

    private String desc;

    public abstract byte getType();
    public void setDesc(String desc){
        this.desc = desc;
    }
    public String getDesc(){
        return desc;
    }
}
