/**
 * @author leo
 */
package com.pip.itimes.server.world.boss;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.BossTips;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.stage.WorldMap;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.PositionService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.StageService;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * @author leo
 *
 */
public class BossService implements Runnable{
    private static final Logger log = Logger.getLogger(BossService.class);

    private PlayerService playerService;
    private StageService stageService;
    private ConnectService connectService;
    private PositionService positionService;
    private ChatService chatService;
    
    public static final int[] bossHpMpWeight = new int[]{
        10001,
        10008,
        10027,
        10064,
        10125,
        10216,
        10343,
        10512,
        10729,
        11000,
        11331,
        11728,
        12197,
        12744,
        13375,
        14096,
        14913,
        15832,
        16859,
        18000,
        19261,
        20648,
        22167,
        23824,
        25625,
        27576,
        29683,
        31952,
        34389,
        37000,
        39791,
        42768,
        45937,
        49304,
        52875,
        56656,
        60653,
        64872,
        69319,
        74000,
        78921,
        84088,
        89507,
        95184,
        101125,
        107336,
        113823,
        120592,
        127649,
        135000,
        142651,
        150608,
        158877,
        167464,
        176375,
        185616,
        195193,
        205112,
        215379,
        226000,
        236981,
        248328,
        260047,
        272144,
        284625,
        297496,
        310763,
        324432,
        338509,
        353000,
        367911,
        383248,
        399017,
        415224,
        431875,
        448976,
        466533,
        484552,
        503039,
        522000,
        541441,
        561368,
        581787,
        602704,
        624125,
        646056,
        668503,
        691472,
        714969,
        739000,
        763571,
        788688,
        814357,
        840584,
        867375,
        894736,
        922673,
        951192,
        980299,
        1010000
    };
    
    public void setChatService(ChatService chatService){
    	this.chatService = chatService;
    }
    //清空数据循环器
    private int trick = 0;
    /**
     * 玩家对应的boss战斗表
     */
    private  ConcurrentHashMap<Integer, WorldBoss> player2Boss = new ConcurrentHashMap<Integer, WorldBoss>();
    
    public WorldBoss getPlayerBoss(int playerId){
    	return player2Boss.get(playerId);
    }
    public ConcurrentHashMap<Integer, WorldBoss> getPlayer2Boss() {
		return player2Boss;
	}
    
    /**
     * @param worldBossId
     * @return血倍数
     */
    public int getWorldBossHp(int worldBossId, int originalHp, int maxLevel){
        int result = originalHp;

        BossDefine bossDefine = BossDefineLoader.bossDefineMap.get(worldBossId);
        
        if(bossDefine != null){
            result = (int)((long)originalHp * bossHpMpWeight[maxLevel - 1] / bossHpMpWeight[bossDefine.getMaxLevel() - 1] * bossDefine.getHpMax() / 100) + originalHp;
        }
        
        return result;
    }
    
    
    /**
     * @param worldBossId
     * @return蓝倍数
     */
    public int getWorldBossMp(int worldBossId, int originalMp, int maxLevel){
        int result = originalMp;

        BossDefine bossDefine = BossDefineLoader.bossDefineMap.get(worldBossId);
        
        if(bossDefine != null){
            result = (int)((long)originalMp * bossHpMpWeight[maxLevel - 1] / bossHpMpWeight[bossDefine.getMaxLevel() - 1] * bossDefine.getMpMax() / 100) + originalMp;
        }
        
        return result;
    }
    
	/**
     * @param playerId
     * @param worldBoss
     * 将玩家放入世界boss对应表
     */
    public void addPlayerBoss(int playerId, WorldBoss worldBoss){
    	player2Boss.put(playerId, worldBoss);
//    	removeWaitingBoss(worldBoss);
    }
    //所有产生等待战斗的worldboss
    private Map<Integer, WorldBoss> waitingBossMap = new HashMap<Integer, WorldBoss>();
    
    public BossService(){
    	//player2Boss.clear();
    }

    public void start(){
        new Thread(this).start();
    }
    
    public void setPositionService(PositionService positionService){
    	this.positionService = positionService;
    }
    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }

    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }
    
    /**
     * @param worldBoss
     * 加入等待对列里面
     */
    public void addWaitingBoss(WorldBoss worldBoss){
    	waitingBossMap.put(worldBoss.getGroupId(), worldBoss);;
    }
    
    /**
     * @param worldBossId
     * 清除所有等待战斗状态的boss
     */
    public void removeWaitingBoss(WorldBoss worldBoss){
    	waitingBossMap.remove(worldBoss.getGroupId());
    }
    /**
     * @param worldBossId
     * 清除所有等待战斗状态的boss
     *//*
    public void removeWaitingBoss(int worldBossId){
    	for(int i = 0; i < waitingBossVector.size(); i++){
    		WorldBoss worldBoss = (WorldBoss) waitingBossVector.get(i);
    		if(worldBoss.getGroupId() == worldBossId){
    			waitingBossVector.remove(i);
    			break;
    		}
    	}
    }*/
    
    /**
     * @param worldBossId
     * @return是否是世界boss
     */
    public boolean isWorldBoss(int worldBossId){
    	boolean worldBossFlag = false;
    	HashMap<Integer, BossDefine> bossDefineMap = BossDefineLoader.bossDefineMap;
    	if(bossDefineMap.containsKey(worldBossId)){
    		worldBossFlag = true;
    	}
    	return worldBossFlag;
    }
    
    
    /**
     * @param worldBossId
     * @return获得对应id的boss
     */
    public WorldBoss findWaitingBoss(int worldBossId){
    	WorldBoss worldBoss = null;
    	if(waitingBossMap.containsKey(worldBossId)){
    		worldBoss = (WorldBoss) waitingBossMap.get(worldBossId);
    	}
    	return worldBoss;
    }
    
    /**
     * @param mapId
     * @return返回该地图上的世界boss并下发创建消息
     */
    public void findMapWaitingBoss(int mapId, int playerId){
    	WorldBoss worldBoss = null;
    	for(Map.Entry<Integer, WorldBoss> boss: waitingBossMap.entrySet()){
    		if(boss.getValue().getMapId() == mapId){
    			worldBoss = boss.getValue();
    		}
    	}
    	if(worldBoss != null){
    		sendBossInfo(worldBoss, playerId);
    	}
    }
    /**
     * 清除所有等待状态的boss
     */
    public void clearWaitngBoss(BossDefine bossDefine){
    	if(waitingBossMap.containsKey(bossDefine.getGroupId())){
    		WorldBoss worldBoss = waitingBossMap.get(bossDefine.getGroupId());
    		refreshWorldBossDisable(worldBoss, WorldBoss.STATE_DESTROY);
    		if(worldBoss.getState() == WorldBoss.STATE_DESTROY){
    			bossDefine.setNeedRecreate(true);
    		}
    		waitingBossMap.remove(bossDefine.getGroupId());
    	}

    }
    /**
     * @param bossDefine
     * 创建boss 并下发给所有玩家
     */
    public void createBoss(BossDefine bossDefine){

    	WorldBoss worldBoss = new WorldBoss(stageService, bossDefine);
    	refeshWorldBoss(worldBoss);
    }
    
    public void createBossChat (BossDefine bossDefine) {
    	WorldBoss worldBoss = new WorldBoss(stageService, bossDefine);
    }
    
    /**
     * @param worldBoss
     * 将boss重新信息重新下发并发送聊天
     */
    
    public void refeshWorldBoss(WorldBoss worldBoss){
    	sendWorldBossInfo(worldBoss, worldBoss.getMapId());
    	sendWorldBossChatMessage(worldBoss);
    	addWaitingBoss(worldBoss);
    }
    
/*    *//**
     * @param worldBoss
     * 将boss重新信息重新下发并发送聊天
     *//*
    
    public void recreateWorldBoss(WorldBoss worldBoss, WorldPlayer[] player){
    	sendRecreateWorldBossInfo(worldBoss, worldBoss.getMapId(), player);
    	sendWorldBossChatMessage(worldBoss);
    	addWaitingBoss(worldBoss);
    }
    */
/*    *//**
     * @param worldBoss
     * @param MapId
     * 所有人发boss信息
     *//*
    public void sendRecreateWorldBossInfo(WorldBoss worldBoss , int MapId, WorldPlayer[] player){
    	//获取该地图上的所有玩家
    	Vector playerMap = positionService.getPlayer2Position(MapId);
    	boolean sendFlag = true;
    	for(int i = 0; i < playerMap.size(); i++){
	    		sendFlag = true;
	    		for(int k = 0; k < player.length; k++){
	    			int id = player[k].getId();
	    			if(id == (Integer)playerMap.get(i)){
	    				sendFlag = false;
	    				break;
	    			}
	    		}
    		if(sendFlag){
    			sendBossInfo(worldBoss, (Integer) playerMap.get(i));
    		}
    		
    	}
    	
    }*/
    
    /**
     * @param bossDefine
     * 发世界boss的产生聊天
     */
    public void sendWorldBossChatMessage(WorldBoss worldBoss){
    	String message = BossTips.getWorldBossTip(worldBoss.getGroupId());
    	short mapID = (short) worldBoss.getMapId();
    	Scene map = stageService.getScene(mapID);
    	if(message.length() > 0){
    		message = message.replaceAll("mapid", map.getName());
    		chatService.sendWorldMessage(-1,"系统",message);
    	}
    	
    	
    }
    
    /**
     * @param bossDefine
     * 发世界boss的产生聊天
     */
    public void sendWorldBossPreChatMessage(int mgId,short mapID){
    	String message = BossTips.getPreBossTip(mgId);
    	Scene map = stageService.getScene(mapID);
    	if(message.length() > 0){
    		message = message.replaceAll("mapid", map.getName());
    		chatService.sendWorldMessage(-1,"系统",message);
    	}
    	
    	
    }
    
    /**
     * @param worldBoss
     * @param MapId
     * 所有人发boss信息
     */
    public void sendWorldBossInfo(WorldBoss worldBoss , int MapId){
    	//获取该地图上的所有玩家
    	Vector playerMap = positionService.getPlayer2Position(MapId);
    	for(int i = 0; i < playerMap.size(); i++){
    		sendBossInfo(worldBoss, (Integer) playerMap.get(i));
    	}
    	
    }
    

    /**
     * 丫的，发消息的时候不能瞬间发刷新，这样会让客户端频繁刷新
     * @param worldBoss
     * @param playerId
     * 某一个玩家发boss信息
     */
    public void sendBossInfo(WorldBoss worldBoss, int playerId){
    	byte[] bossData = worldBoss.toClientBytes();

        UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
        seg.writeShort(ClientConstants.EXTEND_PROTOCOL_BOSS_DATA);
        seg.write((byte) 1);
        seg.write(bossData);

        connectService.writeTo(seg, playerId);

       /* byte[] refreshData = worldBoss.getRefreshData();

        UWAPSegment seg1 = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
        seg1.writeShort(ClientConstants.EXTEND_PROTOCOL_BOSS_REFRESH);
        seg1.write((byte) 1);
        seg1.write(refreshData);

        connectService.writeTo(seg1, playerId);*/
    }
    
    public void addPlayerBoss(){
    	
    }
   /* public void createBoss(WorldPlayer player){
        synchronized(player2Boss){
            if(!player2Boss.containsKey(player.getId())){
                WorldBoss worldBoss = new WorldBoss(stageService, player);
                player2Boss.put(player.getId(), worldBoss);

                byte[] bossData = worldBoss.toClientBytes();

                UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                seg.writeShort(ClientConstants.EXTEND_PROTOCOL_BOSS_DATA);
                seg.write((byte) 1);
                seg.write(bossData);

                connectService.writeTo(seg, player.getId());

                byte[] refreshData = worldBoss.getRefreshData();

                UWAPSegment seg1 = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                seg1.writeShort(ClientConstants.EXTEND_PROTOCOL_BOSS_REFRESH);
                seg1.write((byte) 1);
                seg1.write(refreshData);

                connectService.writeTo(seg1, player.getId());
            }
        }
    }*/

    public void deleteBoss(IPlayerData player){
        //synchronized(player2Boss){
            WorldBoss worldBoss = player2Boss.remove(player.getId());
            
           /* if(worldBoss != null){
                worldBoss.destroy();
                byte[] refreshData = worldBoss.getRefreshData();
                
                UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                seg.writeShort(ClientConstants.EXTEND_PROTOCOL_BOSS_REFRESH);
                seg.write((byte) 1);
                seg.write(refreshData);

                connectService.writeTo(seg, player.getId());
            }*/
        //}
    }
    
    /**
     * 将boss所在地图上的玩家发送boss消失消息 并发送聊天
     * @param player TODO
     */
    public void refreshWorldBossDisable(WorldBoss worldBoss, byte disable/*, WorldPlayer[] player*/){
    	if(disable == WorldBoss.STATE_SHOW){
    		//获取boss加载器，检查时间，进行销毁
    		boolean avilib = true;
    		HashMap<Integer, BossDefine> bossDefineMap = BossDefineLoader.bossDefineMap;
        	for(Map.Entry<Integer, BossDefine> boss: bossDefineMap.entrySet()){
        		BossDefine bossDefine = boss.getValue();
        		if(!bossDefine.inTime() || bossDefine.getTrick() != worldBoss.getTrick()){
        			avilib = false;
        			break;
        		}
        	}
    		if(avilib){
    			worldBoss.setState(WorldBoss.STATE_SHOW);
    			addWaitingBoss(worldBoss);
    		}else{
    			worldBoss.setState(WorldBoss.STATE_DESTROY);
    		}
    	}else if(disable == WorldBoss.STATE_HIDE){
    		worldBoss.setState(WorldBoss.STATE_HIDE);
    	}else if(disable == WorldBoss.STATE_DESTROY){
    		worldBoss.setState(WorldBoss.STATE_DESTROY);
    	}
    	
    	Vector playerMap = positionService.getPlayer2Position(worldBoss.getMapId());
    	byte[] refreshData = worldBoss.getRefreshData(worldBoss);
		
	    UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
	    seg.writeShort(ClientConstants.EXTEND_PROTOCOL_BOSS_REFRESH);
	    seg.write((byte) 1);
	    seg.write(refreshData);
    	for(int i = 0; i < playerMap.size(); i++){
			connectService.writeTo(seg, (Integer)playerMap.get(i));
    	}
    }
    
	/**
	 * @param itemId
	 * 充值
	 */
	public void resetWorldBossRefresh(int itemId){
		if(BossDefineLoader.bossEquMap.containsKey(itemId)){
			BossDefine bossDefine = BossDefineLoader.bossEquMap.get(itemId);
			clearWaitngBoss(bossDefine);
			bossDefine.setNeedRecreate(true);
			log.info("worldBoss refresh Id[" + itemId + "]");
		}else{
			log.info("worldBoss refresh error Id[" + itemId + "]");
		}
	}
	
    public void run(){
        while(true){
        	
            try{
            Thread.sleep(10 * 1000L);
            trick++;
            
            HashMap<Integer, BossDefine> bossDefineMap = BossDefineLoader.bossDefineMap;
        	for(Map.Entry<Integer, BossDefine> boss: bossDefineMap.entrySet()){
        		BossDefine bossDefine = boss.getValue();
        		if(bossDefine.inTime()){
//        			if (bossDefine.firstTime == true) {	// 修复BUG： 先计算下刷新时间。然后在发送刷BOSS前2分钟的公告。
//        				bossDefine.setNextFreshTime();	// 否则这里发的公告总是上一个BOSS的公告，而且第一个BOSS永远不会发公告
//        				bossDefine.firstTime = false;
//        			}
        			if(bossDefine.needPreChat()){
        				if ((short)bossDefine.getMapId() == 0) {	// 修复BUG：不能先获得地图ID，还没有createBoss(bossDefine);
        					createBossChat(bossDefine);
        				}
        				sendWorldBossPreChatMessage(bossDefine.getGroupId(), (short)bossDefine.getMapId());
        			}
        			if(bossDefine.needFresh()){
            			//销毁已经加载过的没有在战斗状态的世界boss
            			if(bossDefine.getLoad() == BossDefine.BOSS_LOAD){
            				clearWaitngBoss(bossDefine);
            				bossDefine.setLoad(BossDefine.BOSS_UNLOAD);
            			}else if(bossDefine.getLoad() == BossDefine.BOSS_DETROY){
            				bossDefine.setLoad(BossDefine.BOSS_UNLOAD);
            			}
            			//创建boss，并设置bossDefine的加载状态为已经加载
            			if(bossDefine.getLoad() == BossDefine.BOSS_UNLOAD){
            				bossDefine.resetRefreshTime();
            				createBoss(bossDefine);
            				bossDefine.setLoad(BossDefine.BOSS_LOAD);
            			}
            			bossDefine.setNextFreshTime();
            		}
        		}else{
        			clearWaitngBoss(bossDefine);
        		}
        		
        	}
        	if(trick >= 60){//10分钟扫描一次重置所有世界boss时间
        		trick = 0;
        		//又忘记=号了，，太汗了
        		if(Utils.getTodayStart() >= BossDefineLoader.lastMakeTime + BossDefineLoader.period){
        			BossDefineLoader.reSet();
        			// add Jeremy:清除限制副本进入次数Map
        	    	Server.player_InstanceForbid.clear();
    				log.info("clear all players InstanceForbid instances");
        		}
        		
        	}
            	
            }catch(Exception e){
                log.error(e, e);
            }
        }
    }
    
}
