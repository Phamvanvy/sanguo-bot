package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorldMap {
	
	protected int Id;
    protected String name;
    protected int pointid;//始发场景ID（可多个）
    protected int npcid;//地图传送点NPC ID
    protected int levelmin;//最低级别
    protected int levelmax;//最高级别
    protected int WorldMapid;//目标地 地图id
    protected int endx;//目的地坐标x
    protected int endy;//目的地坐标y
    protected int x;//从地图外跳转到地图对应的坐标x
    protected int y;//从地图外跳转到地图对应的坐标y
    protected String info;//地区特色
    
    public int getNewMapId() {
		return newMapId;
	}

	public int getNewEndX() {
		return newEndX;
	}

	public int getNewEndY() {
		return newEndY;
	}

	protected int newMapId;     //新版本跳转地图id
    protected int newEndX;     //新版本跳转地图id
    protected int newEndY;     //新版本跳转地图id
    
    protected static Map<Integer,WorldMap> Mapbypointid = new HashMap<Integer,WorldMap>();
    protected static Map<Integer,WorldMap> Mapbynpcid = new HashMap<Integer,WorldMap>();
    protected static Map<Integer,WorldMap> MapbyId = new HashMap<Integer,WorldMap>();
    protected static int countnpc = -1;
    
    public static  void addWorldMap(WorldMap worldmap){

    	Mapbypointid.put(worldmap.getPointid(), worldmap);
    	Mapbynpcid.put(worldmap.getNpcid(), worldmap);
    	MapbyId.put(worldmap.getId(), worldmap);
    }
    
    public static  void addCountNPC(int Countnpc){
    	countnpc = Countnpc;
    }
    
    public static WorldMap getWorldMapbypointid(int pointid){
        return Mapbypointid.get(pointid);
    }
    
    public static WorldMap getWorldMapbynpcid(int npcid){
        return Mapbynpcid.get(npcid);
    }
    
    public static WorldMap[] getWorldMapbylevel(int level){
    	ArrayList npcid = new ArrayList();
    	for(int k = 1;k<countnpc + 1;k++){
    		if((MapbyId.get(k).levelmin <= level) && (MapbyId.get(k).levelmax >= level)){
    			npcid.add(MapbyId.get(k)); 
    		}
    	}
    	WorldMap[] worldmaparr = new WorldMap[npcid.size()];
    	for(int k = 0;k<npcid.size();k++){
    		worldmaparr[k] = (WorldMap) npcid.get(k);
    	}
        return worldmaparr;
    }
    
    /*public WorldMap(int Id,String name,int pointid,int npcid,int levelmin,int levelmax,int WorldMapid,
    				int endx,int endy,int x,int y,String info) {
    	this.Id = Id;
    	this.name = name;
        this.pointid = pointid;//始发场景ID（可多个）
    	this.npcid = npcid;//地图传送点NPC ID
        this.levelmin = levelmin;//最低级别
        this.levelmax = levelmax;//最高级别
        this.WorldMapid = WorldMapid;//目标地 地图id
        this.endx = endx;//目的地坐标x
        this.endy = endy;//目的地坐标y
        this.x = x;//从地图外跳转到地图对应的坐标x
        this.y = y;//从地图外跳转到地图对应的坐标y
        this.info = info;
    	
    }
*/
    
    public WorldMap(int Id,String name,int pointid,int npcid,int levelmin,int levelmax,int WorldMapid,
			int endx,int endy,int x,int y,String info, int newMapId, int newEndX, int newEndY) {
		this.Id = Id;
		this.name = name;
		this.pointid = pointid;//始发场景ID（可多个）
		this.npcid = npcid;//地图传送点NPC ID
		this.levelmin = levelmin;//最低级别
		this.levelmax = levelmax;//最高级别
		this.WorldMapid = WorldMapid;//目标地 地图id
		this.endx = endx;//目的地坐标x
		this.endy = endy;//目的地坐标y
		this.x = x;//从地图外跳转到地图对应的坐标x
		this.y = y;//从地图外跳转到地图对应的坐标y
		this.info = info;
		this.newMapId = newMapId;
		this.newEndX = newEndX;
		this.newEndY = newEndY;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPointid() {
		return pointid;
	}

	public void setPointid(int pointid) {
		this.pointid = pointid;
	}

	public int getNpcid() {
		return npcid;
	}

	public void setNpcid(int npcid) {
		this.npcid = npcid;
	}

	public int getLevelmin() {
		return levelmin;
	}

	public void setLevelmin(int levelmin) {
		this.levelmin = levelmin;
	}

	public int getLevelmax() {
		return levelmax;
	}

	public void setLevelmax(int levelmax) {
		this.levelmax = levelmax;
	}

	public int getWorldMapid() {
		return WorldMapid;
	}

	public void setWorldMapid(int worldMapid) {
		WorldMapid = worldMapid;
	}

	public int getEndx() {
		return endx;
	}

	public void setEndx(int endx) {
		this.endx = endx;
	}

	public int getEndy() {
		return endy;
	}

	public void setEndy(int endy) {
		this.endy = endy;
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

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public static Map<Integer, WorldMap> getMapbypointid() {
		return Mapbypointid;
	}

	public static void setMapbypointid(Map<Integer, WorldMap> mapbypointid) {
		Mapbypointid = mapbypointid;
	}

	public static Map<Integer, WorldMap> getMapbynpcid() {
		return Mapbynpcid;
	}

	public static void setMapbynpcid(Map<Integer, WorldMap> mapbynpcid) {
		Mapbynpcid = mapbynpcid;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

    
}
