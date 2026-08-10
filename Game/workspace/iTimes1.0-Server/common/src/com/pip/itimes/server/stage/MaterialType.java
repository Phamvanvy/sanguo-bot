package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class MaterialType {

    private String name;
    private byte type;
    private int id;
    private byte level;

    public MaterialType(String name,byte type,int id,byte level) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.level = level;
    }

    public String getName(){
        return name;
    }

    public byte getType(){
        return type;
    }

    public int getId(){
        return id;
    }

    public byte getLevel(){
        return level;
    }
}
