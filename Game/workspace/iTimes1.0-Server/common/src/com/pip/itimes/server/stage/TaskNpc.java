package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class TaskNpc {
    private int id;
    private int type;

    public TaskNpc() {
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setType(int type){
        this.type = type;
    }

    public int getType(){
        return type;
    }

}
