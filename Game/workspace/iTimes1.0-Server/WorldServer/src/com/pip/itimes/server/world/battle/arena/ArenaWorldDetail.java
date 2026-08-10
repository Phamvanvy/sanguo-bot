package com.pip.itimes.server.world.battle.arena;

public class ArenaWorldDetail{
    private String id;
    private String passWord;
    private int address;

    public ArenaWorldDetail(String id, String password, int address){
        this.id = id;
        this.passWord = password;
        this.address = address;
    }

    public String getId(){
        return id;
    }

    public String getPassWord(){
        return passWord;
    }

    public int getAddress(){
        return address;
    }

    public void setPassWord(String password){
        this.passWord = password;
    }

    public void setAddress(int address){
        this.address = address;
    }
}
