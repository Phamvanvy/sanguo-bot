package com.pip.itimes.server.bean;

import java.util.Date;


public class Gift implements java.io.Serializable {
    private int id;
    private int groupid;
    private int playerid;
    private Date createtime;
    private Date modifytime;
    private int rcount;
    private int count;

    public Gift() {
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getGroupid(){
        return groupid;
    }

    public void setGroupid(int groupid){
        this.groupid = groupid;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public Date getModifytime(){
        return modifytime;
    }

    public void setModifytime(Date modifytime){
        this.modifytime = modifytime;
    }
    
    public int getRcount(){
        return rcount;
    }

    public void setRcount(int rcount){
        this.rcount = rcount;
    }

    public int getCount(){
        return count;
    }

    public void setCount(int count){
        this.count = count;
    }
}
