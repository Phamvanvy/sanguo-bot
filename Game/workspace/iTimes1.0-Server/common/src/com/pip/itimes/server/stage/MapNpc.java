package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class MapNpc{

    private byte x;
    private byte y;
    private byte id;

    public MapNpc() {
    }
    public void setX(byte x){
        this.x = x;
    }

    public byte getX(){
        return x;
    }

    public void setY(byte y){
        this.y = y;
    }

    public byte getY(){
        return y;
    }

    public void setId(byte id){
        this.id = id;
    }

    public byte getId(){
        return id;
    }
}
