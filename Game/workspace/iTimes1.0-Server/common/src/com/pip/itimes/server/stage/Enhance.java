package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;

public class Enhance {

    protected String name;
    protected int property;
    protected int[] point;
    protected int ratio;
    protected int itemId;
    protected int quality;//品质
    
    protected int percentage;//分子
    protected int additional;//附加值
    protected int bottom;//至少用精炼石数量

    protected static Map<Integer,Enhance[]> enhances = new HashMap<Integer,Enhance[]>();
    //提升部分地区的精炼几率
    public static Map<Integer, Integer> upEnhancePercent= new HashMap<Integer,Integer>();
    
    
    /**
     * 添加刻字默认表
     */
    public static Map<Integer, String> letteringString = new HashMap<Integer, String>();
    
    public static void addLettering(int itemId, String desc){
    	letteringString.put(itemId, desc);
    }
    
    public static String getLettering(int itemId){
    	String returnString = null;
    	if(letteringString.containsKey(itemId)){
    		returnString = letteringString.get(itemId);
    	}
    	return returnString;
    	
    }
    
    public static int getLetteringLength(int itemId){
    	int returnFlag = 0;
    	if(letteringString.containsKey(itemId)){
    		returnFlag = letteringString.get(itemId).length();
    	}
    	return returnFlag;
    }
    public static void addUpEnhancePercent(int mapId, int percent){
    	upEnhancePercent.put(mapId, percent);
    }
    public static int getUpMapPercent(int mapId){
    	int upPercent = 0;
    	if(upEnhancePercent.containsKey(mapId)){
    		upPercent = upEnhancePercent.get(mapId);
    	}
    	return upPercent;
    }
    public static  void addEnhance(Enhance enhance){
    	//mengjie modify
    	//将精华按照等级段分为4个数组
    	Enhance[] enhancear = enhances.get(enhance.getProperty());
    	if(enhancear==null){
    		enhancear = new Enhance[4];
    		enhances.put(enhance.getProperty(), enhancear);
    	}
    	enhancear[enhance.getQuality()] = enhance;
    }

    public static Enhance getEnhance(int property,int level){
        return enhances.get(property)[level_quality[level]];
    }
    //jwp add
    public static Enhance getUpEnhance(int property,int level){
        return enhances.get(property)[level];
    }
    //jwp add end
    
    public Enhance(String name,int property,int[] point,int ratio,int itemId,int quality,int percentage,int additional,int bottom) {
        this.property = property;
        this.name = name;
        this.point = point;
        this.ratio = ratio;
        this.itemId = itemId;
        this.quality = quality;
        this.percentage = percentage;//分子
        this.additional = additional;//附加值
        this.bottom = bottom;
    }

    public int getRatio() {
        return ratio;
    }

    public int getProperty() {
        return property;
    }

    public int getPoint(int count) {
    	if (count>0){
    		return point[count-1];
    	}else{
    		return point[0];
    	}
    }

    public String getName() {
        return name;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public void setProperty(int property) {
        this.property = property;
    }

    public void setPoint(int[] point) {
        this.point = point;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getItemId() {
        return itemId;
    }

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}
	
	public int getPercentage() {
		return percentage;
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	public int getAdditional() {
		return additional;
	}

	public void setAdditional(int additional) {
		this.additional = additional;
	}

	public int getBottom() {
		return bottom;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}

	//level=1-40 --> quality=0
	//level=41-70 --> quality=1
	//level=71-99 --> quality=2
	//level=100 --> quality=3
	public static int[] level_quality = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,//0-40
    1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,//41-70
    2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,//71-99
    	3 };//100
    //mengjie modify end
	public static int getEnhanceMaxPointProLevel(){
		return 3;
	}
}
