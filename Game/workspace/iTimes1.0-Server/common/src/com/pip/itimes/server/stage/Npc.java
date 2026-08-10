package com.pip.itimes.server.stage;


/**
 * @author Jeffrey
 * @version 1.0
 */
public class Npc{

    private int id;
    private short refreshSecond;
    private byte x;
    private byte y;
    private byte type;
    private short pngId;
    private byte flag;
    private String name;

    public Npc() {
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setRefreshSecond(short refreshSecond){
        this.refreshSecond = refreshSecond;
    }

    public short getRefreshSecond() {
        return refreshSecond;
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

    public void setType(byte type){
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public void setPngId(short pngId){
        this.pngId = pngId;
    }

    public short getPngId(){
        return pngId;
    }

    public void setFlag(byte flag){
        this.flag = flag;
    }

    public byte getFlag(){
        return flag;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
