package com.pip.itimes.server.world.battle;

import java.util.HashMap;
import java.util.Map;

import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.Monster;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.suit.SuitEffect;
import com.pip.itimes.server.suit.Suits;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.StageService;
import com.pip.itimes.server.world.TongService;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.boss.BossService;
import com.pip.itimes.server.world.game.FallService2;

public class BattleReplayerService {
	
	
	  /**
     * 尝试进行没有序列的回合数试探
     */
    public static final byte nomal = 0;
    
	   /**
     * 尝试进行没有序列的回合数试探
     */
    public static final byte tryAutoBattleCountNoSerial = 1;
    
    /**
     * 随机数上传错误
     */
    public static final byte tryAutoBattleSeedFinal = 2;
    
    
    /**
     * 使用固定协议
     */
    public static final byte tryAutoBattleFinal = 3;
    
/*	BattleService2 battleService = null;

	public BattleService2 getBattleService() {
		return battleService;
	}

	public void setBattleService(BattleService2 battleService) {
		this.battleService = battleService;
	}*/
	
	public ConnectService getConnectService() {
		return connectService;
	}

	public void setConnectService(ConnectService connectService) {
		this.connectService = connectService;
	}

	public PlayerService getPlayerService() {
		return playerService;
	}

	public void setPlayerService(PlayerService playerService) {
		this.playerService = playerService;
	}

	public StageService getStageService() {
		return stageService;
	}

	public void setStageService(StageService stageService) {
		this.stageService = stageService;
	}

	public ChatService getChatService() {
		return chatService;
	}

	public void setChatService(ChatService chatService) {
		this.chatService = chatService;
	}

	private ConnectService connectService;

	private PlayerService playerService;


	private StageService stageService;

	private ChatService chatService;
	
	/**
	 * 当前播放数量
	 */
	private int id;
	

	/**
	 * 当前战斗策略
	 */
	private static final BattleStrategy normalClientStrategy = new BattleStrategy();
	
	
	public static BattleStrategy getNormalClientStrategy() {
		return normalClientStrategy;
	}

	/**
	 * 当前战斗存放。。没一个玩家和他里面的战斗，，这里只限于一个回合打不死才有用
	 */
	public static Map<Integer,BattleReplayer> player_battleReplayer = new HashMap<Integer,BattleReplayer>();
	
	/**
	 * @param playerId
	 * @param battleReplayer
	 * 将每次的战斗都放入战斗表里
	 */
	public void addBattleReplayer(int playerId, BattleReplayer battleReplayer){
		player_battleReplayer.put(playerId, battleReplayer);
	}
	
	/**
	 * @param playerId
	 * 去掉一个玩家的战斗回放
	 */
	public void removerBattleReplayer(int playerId){
		player_battleReplayer.remove(playerId);
	}
	/**
	 * @param playerId
	 * @return 当前战斗是否存放了该玩家
	 */
	public boolean hasBattleReplayer(int playerId){
		return player_battleReplayer.containsKey(playerId);
	}
	
	
	/**
	 * @param playerId
	 * @return获取战斗对象
	 */
	public BattleReplayer getBattleReplayer(int playerId){
		return player_battleReplayer.get(playerId);
	}
	public BattleReplayer CreateBattleReplayer(int boundCount, int monsterId, int playerSkillId,
			byte playerTarget, int playerPetSkillId, byte playerPetTarget,
			int bountSeed, int[][] monsterProperty, WorldPlayer player){
		//获取地图上的怪物
		Monster[] monster = stageService.getMonsters(monsterId);
		
		BattleReplayer battleReplayer = new BattleReplayer(this, normalClientStrategy, boundCount, monsterId, monster, playerSkillId, playerTarget
				, playerPetSkillId, playerPetTarget, bountSeed, monsterProperty, player);
		return battleReplayer;
		
	}
	

}
