package com.pip.itimes.server.stage;

/**
 * @author not attributable
 * @version 1.0
 */
public class TaskNpcType {

    protected int id;
    protected String name;
    protected int type;

    public TaskNpcType(int id,String name,int type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getType(){
        return type;
    }

}
