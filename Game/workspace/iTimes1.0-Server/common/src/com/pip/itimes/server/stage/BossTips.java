package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

public class BossTips {
	
    /**
     * 里面对应的是怪物全局id和要发送的战斗结束聊天
     */
    public static final Map<Integer,String> tips = new HashMap<Integer,String>();

    /**
     * 世界boss发送的聊天
     */
    public static final Map<Integer, String> worldBossTip = new HashMap<Integer, String>();
    
    /**
     * 世界boss开始交战是的私聊
     */
    public static final Map<Integer, String> bossTip = new HashMap<Integer,String>();
    
    
    /**
     * 世界boss提前2分钟喊话
     */
    public static final Map<Integer, String> bossPreTip = new HashMap<Integer,String>();
    
    public static void clearBossPreTip(){
    	bossPreTip.clear();
    }
    
    public static void addBossPreTip(int mgId, String tip){
    	bossPreTip.put(mgId, tip);
    	
    }
    
    public static String getPreBossTip(int mgId){
    	return bossPreTip.get(mgId);
    }
    public static void addBossTip(int mgId, String message){
    	bossTip.put(mgId, message);
    }
    
    public static String getBossTip(int mgId){
    	return bossTip.get(mgId);
    }
    public static void bossTipClear(){
    	bossTip.clear();
    }
    
    public static  void addWorldBossTip(int mgId,String message){
    	worldBossTip.put(mgId, message);
    }
    
    public static String getWorldBossTip(int mgId){
        return worldBossTip.get(mgId);
    }
    
    public static void worldBossTipClear(){
    	worldBossTip.clear();
    }
    public static void addTip(int level,String message){
        tips.put(level,message);
    }

    public static String getTip(int level){
        return tips.get(level);
    }

    public static void clear(){
        tips.clear();
    }

}
