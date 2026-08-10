package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class AbilityNpc extends TaskNpc{

    private byte clazz;

    public AbilityNpc() {
        super();
    }

    public void setClazz(byte clazz){
        this.clazz = clazz;
    }

    public byte getClazz(){
        return clazz;
    }

}
