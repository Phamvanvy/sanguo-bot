package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;

public class Instanceadd {
	
    protected int Mapid;//地图id
    protected int x;//坐标x
    protected int y;//坐标y
    protected String name;
    protected int type;
    protected int npccount;
    protected int instanceid;
    
    protected static String[] npcname = new String [10];
    protected static int[] npcsex = new int [10];
    
    protected static Map<Integer,Instanceadd> MapbyId = new HashMap<Integer,Instanceadd>();
    protected static Map<Integer,Instanceadd> MapbyType = new HashMap<Integer,Instanceadd>();
    
    public static  void addInstanceadd(Instanceadd instanceadd){

    	MapbyId.put(instanceadd.getMapid(), instanceadd);
    	MapbyType.put(instanceadd.getType(), instanceadd);
    }
    
    public static Instanceadd getInstanceaddbymapid(int mapid){
        return MapbyId.get(mapid);
    }
    public static Instanceadd getInstanceaddbytype(int type){
        return MapbyType.get(type);
    }
    public Instanceadd( int Mapid,int x,int y,String name,int type,int npccount,int instanceid) {
    	this.instanceid = instanceid;
    	this.Mapid = Mapid;
    	this.name = name;
        this.x = x;//从地图外跳转到地图对应的坐标x
        this.y = y;//从地图外跳转到地图对应的坐标y
        this.type = type;
        this.npccount = npccount;
    }

	public int getMapid() {
		return Mapid;
	}

	public void setMapid(int mapid) {
		Mapid = mapid;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getNpccount() {
		return npccount;
	}

	public void setNpccount(int npccount) {
		this.npccount = npccount;
	}

	public static String getNpcnameby(int count) {
		return npcname[count];
	}

	public static void setNpcnameby(String npcname,int count) {
		Instanceadd.npcname[count] = npcname;
	}
	
	public static int getNpcsexby(int count) {
		return npcsex[count];
	}

	public static void setNpcsexby(int npcsex,int count) {
		Instanceadd.npcsex[count] = npcsex;
	}
	public int getInstanceid() {
		return instanceid;
	}

	public void setInstanceid(int instanceid) {
		this.instanceid = instanceid;
	}	
}
