package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;

public class AnniversaryEnhance {
	
	protected int equItemId;//精炼前装备id
    protected int newequItemId;//精炼后装备id
    protected int count;//星级
    protected int probability;//概率
    public int getAnniversary() {
		return anniversary;
	}

	public void setAnniversary(int anniversary) {
		this.anniversary = anniversary;
	}

	public int getEquipType() {
		return equipType;
	}

	public void setEquipType(int equipType) {
		this.equipType = equipType;
	}
	protected int anniversary; //星级品质
    protected int equipType; //星级品质
    
    protected static Map<Integer,AnniversaryEnhance> MapAnniversary = new HashMap<Integer,AnniversaryEnhance>();
    //jwp add
    protected static Map<Integer,AnniversaryEnhance> MapUnhenceYearEquip = new HashMap<Integer,AnniversaryEnhance>();//周年装分解；
    
   // protected static Map<Integer,AnniversaryEnhance> MapAnniversaryYearEquip = new HashMap<Integer,AnniversaryEnhance>();//3星二周年链；
	//protected static Map<Integer,AnniversaryEnhance> MapAnniversaryTwoYearEquip = new HashMap<Integer,AnniversaryEnhance>();//3星二周年戒指；
    
	/*public static  void addMapAnniversaryYearEquip(AnniversaryEnhance anniversary){

		MapAnniversaryYearEquip.put(anniversary.equItemId, anniversary);
    }
	public static  void addMapAnniversaryTwoYearEquip(AnniversaryEnhance anniversary){

		MapAnniversaryTwoYearEquip.put(anniversary.equItemId, anniversary);
    }*/
	public static  void addAnniversaryEnhance(AnniversaryEnhance anniversary){

    	MapAnniversary.put(anniversary.equItemId, anniversary);
    }
	
    public static  void addUnhenceYearEquip(AnniversaryEnhance anniversary){

    	MapUnhenceYearEquip.put(anniversary.newequItemId, anniversary);
    }
	public static AnniversaryEnhance getUnhenceYearEquip(int newequItemId){
        return MapUnhenceYearEquip.get(newequItemId);
    }
	
	public static boolean isAnniversaryEqu(int itemId){
		boolean anniversaryEquFlag = false;
		if(MapAnniversary.containsKey(itemId) || MapUnhenceYearEquip.containsKey(itemId)){
			anniversaryEquFlag = true;
		}
		return anniversaryEquFlag;
	}
     //jwp add end 
    public AnniversaryEnhance(int equItemId,int newequItemId,int count,int probability ,int anniversary,int equipType) {
    	this.equItemId = equItemId;//精炼前装备id
    	this.newequItemId = newequItemId;//精炼后装备id
    	this.count = count;//星级
    	this.probability = probability;
    	//jwp add
    	this.anniversary = anniversary;
    	this.equipType = equipType;
    }


	public int getEquItemId() {
		return equItemId;
	}


	public void setEquItemId(int equItemId) {
		this.equItemId = equItemId;
	}


	public int getNewequItemId() {
		return newequItemId;
	}


	public void setNewequItemId(int newequItemId) {
		this.newequItemId = newequItemId;
	}


	public int getCount() {
		return count;
	}


	public void setCount(int count) {
		this.count = count;
	}


	public int getProbability() {
		return probability;
	}


	public void setProbability(int probability) {
		this.probability = probability;
	}
	public static AnniversaryEnhance getAnniversaryEnhance(int equItemId){
        return MapAnniversary.get(equItemId);
    }
	/*public static void addAnniversaryYearEquip(int equipType ,int equipCount,int anniversary){
		for(Map.Entry<Integer,AnniversaryEnhance> temp : MapUnhenceYearEquip.entrySet()){ 
			AnniversaryEnhance anniversaryEnhance = temp.getValue();
			 if(anniversaryEnhance.anniversary ==anniversary && anniversaryEnhance.equipType == 1 && anniversaryEnhance.count >=equipCount){//二周年链
		        	addMapAnniversaryYearEquip(anniversaryEnhance);
		        }else if(anniversaryEnhance.anniversary ==anniversary && anniversaryEnhance.equipType == 2 && anniversaryEnhance.count >=equipCount){//二周年戒
		        	addMapAnniversaryTwoYearEquip(anniversaryEnhance);
		        }
		} 
    
    }*/
}
