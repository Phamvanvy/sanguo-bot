package com.pip.itimes.server.stage;

import java.util.LinkedHashMap;

public class BossRush {
	public final static short BOSSRUSH_MAXTIME = 10;//每天最多挑战10次
	private static LinkedHashMap<Integer,BossRush> bossRushMap = new LinkedHashMap<Integer,BossRush>();
	private LinkedHashMap<Integer,Boss> bossList ;
	private static int maxStage;//当前开放的最大层数
	private int monsterGroupID;
	private short stage;
	
	public BossRush(short stage, int monsterGroupID){
		this.monsterGroupID = monsterGroupID;
		this.stage = stage;
		bossList = new LinkedHashMap<Integer,Boss>();
	}
	
	public int getMonsterGroupID(){
		return monsterGroupID;
	}
	
	public short getStage(){
		return stage;
	}
	
	public void addBoss(byte monsterID, int hp, int mp, int pAttack, int mAttack){
		Boss bo = new Boss(monsterID,hp,mp, pAttack, mAttack);
		addBoss(monsterID,bo);
	}
	
	public void addBoss(int key,Boss bo){
		if(!bossList.containsKey(key)){
			bossList.put(key,bo);
		}
	}
	
	public int getHP(int monsterID){
		Boss bo = bossList.get(new Integer(monsterID));
		if(bo!=null){
			return bo.getHP();
		}
		return 0;
	}
	
	public int getMP(int monsterID){
		Boss bo = bossList.get(new Integer(monsterID));
		if(bo!=null){
			return bo.getMP();
		}
		return 0;
	}
	
	public int getPA(int monsterID){
		Boss bo = bossList.get(new Integer(monsterID));
		if(bo!=null){
			return bo.getPAttack();
		}
		return 0;
	}
	
	public int getMA(int monsterID){
		Boss bo = bossList.get(new Integer(monsterID));
		if(bo!=null){
			return bo.getMAttack();
		}
		return 0;
	}
	
	public static BossRush getBossRush(int stage){
    	return bossRushMap.get(new Integer(stage));
    }
    
    public static void addBossRushList(int stage,BossRush boss){
    	bossRushMap.put(stage, boss);
    }
    
    public static int getBossHP(int stage,int monsterID){
    	BossRush br = getBossRush(stage);
    	if(br!=null){
    		return br.getHP(monsterID);
    	}
    	return 0;
    }
    
    public static int getBossMP(int stage,int monsterID){
    	BossRush br = getBossRush(stage);
    	if(br!=null){
    		return br.getMP(monsterID);
    	}
    	return 0;
    }
    
    public static int getBossPA(int stage,int monsterID){
    	BossRush br = getBossRush(stage);
    	if(br!=null){
    		return br.getPA(monsterID);
    	}
    	return 0;
    }
    
    public static int getBossMA(int stage,int monsterID){
    	BossRush br = getBossRush(stage);
    	if(br!=null){
    		return br.getMA(monsterID);
    	}
    	return 0;
    }
    
    public static int getMonsterGroupID(int stage){
    	BossRush br = getBossRush(stage);
    	if(br!=null){
    		return br.getMonsterGroupID();
    	}
    	return 0;
    }
    
    public static int getMaxStage(){
    	return maxStage;
    }
    
    public static void setMaxStage(int max){
    	maxStage = max;
    }
    
    class Boss{
    	private int hp;
    	private int mp;
    	private byte monsterID;
    	private int mAttack;
    	private int pAttack;
    	
    	public Boss(byte monsterID, int hp, int mp, int pAttack, int mAttack){
    		this.monsterID = monsterID; 
    		this.hp = hp;
    		this.mp = mp;
    		this.pAttack = pAttack;
    		this.mAttack = mAttack;
    	}
    	public int getHP(){
    		return hp;
    	}
    	
    	public int getMP(){
    		return mp;
    	}
    	
    	public byte getMonsterID(){
    		return monsterID;
    	}
    	
    	public int getPAttack(){
    		return pAttack;
    	}
    	
    	public int getMAttack(){
    		return mAttack;
    	}
    }
}
