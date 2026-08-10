package com.pip.itimes.server.stage;


/**
 * @author Jeffery
 * @version 1.0
 */
public class Door{

    private byte x;
    private byte y;
    private short destMapId;
    private byte destX;
    private byte destY;
    private String name;

    public Door() {
    }

    public void setX(byte x){
        this.x = x;
    }

    public byte getX() {
        return x;
    }

    public void setY(byte y){
        this.y = y;
    }

    public byte getY() {
        return y;
    }

    public void setDestMapId(short mapId){
        this.destMapId = mapId;
    }

    public short getDestMapId() {
        return destMapId;
    }

    public void setDestX(byte x){
        this.destX = x;
    }

    public byte getDestX() {
        return destX;
    }

    public void setDestY(byte y){
        this.destY = y;
    }

    public byte getDestY() {
        return destY;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
}
