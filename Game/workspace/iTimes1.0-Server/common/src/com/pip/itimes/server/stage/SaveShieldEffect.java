package com.pip.itimes.server.stage;

public class SaveShieldEffect extends Effect {

    private int value;
    private byte unit;
    private byte property;
    private int time;

    public SaveShieldEffect() {   
    }
    public byte getType() {
        return 46;
    }
    public void setValue(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public void setTime(int time){
        this.time = time;
    }

    public int getTime(){
        return time;
    }

    public byte getUnit(){
        return unit;
    }

    public void setUnit(byte unit){
        this.unit = unit;
    }

    public void setProperty(byte property){
        this.property = property;
    }

    public byte getProperty(){
        return property;
    }
    
}
