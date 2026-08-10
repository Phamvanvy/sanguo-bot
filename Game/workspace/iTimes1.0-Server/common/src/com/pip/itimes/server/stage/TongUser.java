package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class TongUser{
    public int id;
    public String name;
    public int level;
    public int tongDuty;
    public int contribute;
    public String tongTitle;
    public boolean online;

    public TongUser(int id, String name, int level, int tongDuty,
                    String tongTitle, boolean online,int contribute) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.tongDuty = tongDuty;
        this.tongTitle = tongTitle==null?"":tongTitle;
        this.online = online;
        this.contribute = contribute;
    }

    public int hashCode(){
        return id;
    }

    public boolean equals(Object o){
        return ((TongUser)o).id == id;
    }
}
