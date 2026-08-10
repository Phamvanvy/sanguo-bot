package com.pip.itimes.server.bean;

/**
 * @author Jeffery
 * @version 1.0
 */
public class TaskData {
    private int id;
    private byte[] saveData;
    private byte[] current;
    private byte[] finished;
    private Player player;

    public TaskData() {
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setSaveData(byte[] saveData){
        this.saveData = saveData;
    }

    public byte[] getSaveData(){
        return saveData;
    }

    public void setCurrent(byte[] current){
        this.current = current;
    }

    public byte[] getCurrent(){
        return current;
    }

    public void setFinished(byte[] finished){
        this.finished = finished;
    }

    public byte[] getFinished(){
        return finished;
    }

    public Player getPlayer(){
        return player;
    }

    public void setPlayer(Player player){
        this.player = player;
    }
}
