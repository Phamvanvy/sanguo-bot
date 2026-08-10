package com.pip.itimes.server.world;


public class TongIslandDef {

    protected int id;
    protected short[] mapIds;
    protected int bbsId;
    protected String name;
    protected short entrance;
    protected short entrancex;
    protected short entrancey;
    protected int dropGroup;

    public static final int OPEN = 1;
    public static final int CLOSED = 2;

    public TongIslandDef(int id,String name,short[] mapIds,int bbsId,short entrance,short entrancex,short entrancey,int dropGroup) {
        this.id = id;
        this.name = name;
        this.mapIds = mapIds;
        this.bbsId = bbsId;
        this.entrance = entrance;
        this.entrancex = entrancex;
        this.entrancey = entrancey;
        this.dropGroup = dropGroup;
    }

    public int getDropGroup(){
        return dropGroup;
    }

    public short[] getMapIds() {
        return mapIds;
    }

    public int getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    public short getEntrance(){
        return entrance;
    }

    public short getEntrancex(){
        return entrancex;
    }

    public short getEntrancey(){
        return entrancey;
    }

    public int getBbsId(){
        return bbsId;
    }
}
