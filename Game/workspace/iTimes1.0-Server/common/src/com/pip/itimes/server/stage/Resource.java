package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class Resource{

    private int id;
    private byte type;
    private short refreshSecond;
    private byte tileX;
    private byte tileY;
    private short x,y;
    private byte itemId;
    private byte level;
    private boolean playgame;

    public Resource() {
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setType(byte type){
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public void setRefreshSecond(short refreshSecond){
        this.refreshSecond = refreshSecond;
    }

    public short getRefreshSecond() {
        return refreshSecond;
    }

    public void setTileX(byte x){
        this.tileX = x;
    }

    public byte getTileX(){
        return tileX;
    }

    public void setTileY(byte y){
        this.tileY = y;
    }

    public byte getTileY(){
        return tileY;
    }

    public void setItemId(byte itemId){
        this.itemId = itemId;
    }

    public byte getItemId(){
        return itemId;
    }

    public void setLevel(byte level){
        this.level = level;
    }

    public byte getLevel(){
        return level;
    }

    public boolean getPlaygame(){
        return playgame;
    }

    public short getY() {
        return y;
    }

    public short getX() {
        return x;
    }

    public void setPlaygame(boolean playgame){
        this.playgame = playgame;
    }

    public void setY(short y) {
        this.y = y;
    }

    public void setX(short x) {
        this.x = x;
    }
}
