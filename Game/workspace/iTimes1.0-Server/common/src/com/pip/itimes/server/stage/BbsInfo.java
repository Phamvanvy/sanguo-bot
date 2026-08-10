package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;

public class BbsInfo {
	
	protected int id;//ID
    protected String name;
    protected int npcid;//NPC ID
    protected int Mapid;// 地图id
    protected String info;//地区特色
    
    protected static Map<Integer,BbsInfo> BbsInfobynpcid = new HashMap<Integer,BbsInfo>();
    protected static Map<Integer,BbsInfo> BbsInfobyid = new HashMap<Integer,BbsInfo>();
    protected static int bbscount = 0;
    public static  void addBbsInfo(BbsInfo bbsinfo){
    	BbsInfobyid.put(bbsinfo.getId(), bbsinfo);
    	BbsInfobynpcid.put(bbsinfo.getNpcid(), bbsinfo);
    }
    
    public BbsInfo(int id,String name,int npcid,int Mapid,String info) {
    	this.id = id;
        this.name = name;
        this.npcid = npcid;//NPC ID
        this.Mapid = Mapid;// 地图id
        this.info = info;
    	
    }
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNpcid() {
		return npcid;
	}

	public void setNpcid(int npcid) {
		this.npcid = npcid;
	}

	public int getMapid() {
		return Mapid;
	}

	public void setMapid(int Mapid) {
		this.Mapid = Mapid;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public static BbsInfo getBbsInfobyid(int id){
        return BbsInfobyid.get(id);
    }
    
    public static BbsInfo getBbsInfobynpcid(int npcid){
        return BbsInfobynpcid.get(npcid);
    }

	public static int getBbscount() {
		return bbscount;
	}

	public static void setBbscount(int bbscount) {
		BbsInfo.bbscount = bbscount;
	} 
	
}
