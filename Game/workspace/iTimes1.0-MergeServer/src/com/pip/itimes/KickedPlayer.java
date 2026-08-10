package com.pip.itimes;

public class KickedPlayer{
    private int id;
    private long time;

    public KickedPlayer(int id, long time){
        this.id = id;
        this.time = time;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public long getTime(){
        return time;
    }

    public void setTime(long time){
        this.time = time;
    }
}
