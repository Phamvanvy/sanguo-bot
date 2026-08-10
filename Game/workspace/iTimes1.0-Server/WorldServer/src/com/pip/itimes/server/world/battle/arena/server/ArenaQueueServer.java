package com.pip.itimes.server.world.battle.arena.server;

import java.util.concurrent.atomic.AtomicInteger;

import com.pip.itimes.server.world.ArenaServer;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.arena.ArenaConstants;

public class ArenaQueueServer implements Comparable<ArenaQueueServer>{
    protected int type;
    protected int id; //Queue Id

    protected int serverId; //服务器id
    protected int ownerId; //队长id
    protected String ownerName; //队长名
    protected String serverName; //服务器名，记录用
    protected int[] playerId; //角色id
    protected String[] playername; //角色名字
    protected int[] playerLevel; //角色级别
    protected int[] playerArenaLevel; //角色个人等级
    protected int arenaLevel; //战队等级
    protected String arenaName; //战队名称
    protected int state;
    protected ArenaQueueServer opponent = null;

    protected int waitRound = 0; //已等待的轮次

    protected BattleSprite[] sprite = null;
    protected BattleSprite[] pet = null;
    protected int battleId;
    protected int netWin; //净胜场次
    protected int netWinWeighting; //净胜场次加权

    private static AtomicInteger ids = new AtomicInteger(1);

    public static final int STATE_WAIT = 0;
    public static final int STATE_SYNC = 1;
    public static final int STATE_SYNC_OK = 2;
    public static final int STATE_BATTLE = 3;

    //worldwar add mengjie 
    protected int arenaLevel_worldwar;
    
    public ArenaQueueServer(int type){
        this.type = type;
        state = STATE_WAIT;
        id = ids.getAndIncrement();

        switch(type){
            case ArenaConstants.ARENA_TYPE_ONE: {
                playerId = new int[1];
                playername = new String[1];
                playerLevel = new int[1];
                playerArenaLevel = new int[1];
                sprite = new BattleSprite[1];
                pet = new BattleSprite[1];
            }
                break;
            case ArenaConstants.ARENA_TYPE_TWO: {
                playerId = new int[2];
                playername = new String[2];
                playerLevel = new int[2];
                playerArenaLevel = new int[2];
                sprite = new BattleSprite[2];
                pet = new BattleSprite[2];
            }
                break;
            case ArenaConstants.ARENA_TYPE_THREE: {
                playerId = new int[3];
                playername = new String[3];
                playerLevel = new int[3];
                playerArenaLevel = new int[3];
                sprite = new BattleSprite[3];
                pet = new BattleSprite[3];
            }
                break;
        }
    }

    public int getType(){
        return type;
    }

    public int getServerId(){
        return serverId;
    }

    public void setServerId(int serverId){
        this.serverId = serverId;
    }

    public int getOwnerId(){
        return ownerId;
    }

    public void setOwnerId(int ownerId){
        this.ownerId = ownerId;
    }

    public String getOwnerName(){
        return ownerName;
    }

    public void setOwnerName(String ownerName){
        this.ownerName = ownerName;
    }

    public int[] getPlayerId(){
        return playerId;
    }

    public void setPlayerId(int[] playerId){
        this.playerId = playerId;
    }

    public int[] getPlayerLevel(){
        return playerLevel;
    }

    public void setPlayerLevel(int[] playerLevel){
        this.playerLevel = playerLevel;
    }

    public int[] getPlayerArenaLevel(){
        return playerArenaLevel;
    }

    public void setPlayerArenaLevel(int[] playerArenaLevel){
        this.playerArenaLevel = playerArenaLevel;
    }

    public String getArenaName(){
        return arenaName;
    }

    public void setArenaName(String arenaName){
        this.arenaName = arenaName;
    }

    public int getArenaLevel(){
        return arenaLevel;
    }

    public void setArenaLevel(int arenaLevel){
        this.arenaLevel = arenaLevel;
    }

    public int getState(){
        return state;
    }

    public void setState(int state){
        this.state = state;
    }

    public int getWaitRound(){
        return waitRound;
    }

    public void setWaitRound(int waitRound){
        this.waitRound = waitRound;
    }

    public void addWaitRound(){
        this.waitRound++;
    }

    public ArenaQueueServer getOpponent(){
        return opponent;
    }

    public void setOpponent(ArenaQueueServer opponent){
        this.opponent = opponent;
    }

    public BattleSprite[] getSprite(){
        return sprite;
    }

    public int getArenaLevel_worldwar() {
		return arenaLevel_worldwar;
	}

	public void setArenaLevel_worldwar(int arenaLevelWorldwar) {
		arenaLevel_worldwar = arenaLevelWorldwar;
	}

	public void setSprite(int playerId, BattleSprite sprite){
        for(int i = 0; i < this.playerId.length; i++){
            if(this.playerId[i] == playerId){
                this.sprite[i] = sprite;
                
                break;
            }
        }
    }

    public BattleSprite[] getPet(){
        return pet;
    }

    public void setPet(int playerId, BattleSprite pet){
        for(int i = 0; i < this.playerId.length; i++){
            if(this.playerId[i] == playerId){
                this.pet[i] = pet;
                
                break;
            }
        }
    }

    public int getBattleId(){
        return battleId;
    }

    public void setBattleId(int battleId){
        this.battleId = battleId;
    }

    public String getServerName(){
        return serverName;
    }

    public void setServerName(String serverName){
        this.serverName = serverName;
    }

    public String[] getPlayername(){
        return playername;
    }

    public void setPlayername(String[] playername){
        this.playername = playername;
    }
    
    public boolean syncOK(){
        boolean result = true;
        
        for(int i = 0; i < sprite.length; i++){
            if(sprite[i] == null){
                result = false;
                
                break;
            }
        }
        
        return result;
    }
    
    public void clearSyncData(){
        for(int i = 0; i < sprite.length; i++){
            sprite[i] = null;
        }
    }

    public int getNetWin(){
        return netWin;
    }
    
    public int getAveragePlayerLevel(){
        int result = 0;
        
        for(int i = 0; i < playerLevel.length; i++){
            result += playerLevel[i];
        }
        
        return result / playerLevel.length;
    }

    public boolean canPair(ArenaQueueServer other){
        int arenaLevelGap = Math.abs(arenaLevel - other.arenaLevel);
        int playerLevelGap = Math.abs(getAveragePlayerLevel() - other.getAveragePlayerLevel());
        int waitRoundAdd = waitRound + other.waitRound;

        return (arenaLevelGap + playerLevelGap) < (waitRoundAdd + waitRoundAdd) * (type + 1);
    }

    public int compareTo(ArenaQueueServer other){
        int ourNumber = arenaLevel / 2 + getAveragePlayerLevel() + netWinWeighting;
        int otherNumber = other.arenaLevel / 2 + other.getAveragePlayerLevel() + other.netWinWeighting;

        if(ourNumber > otherNumber){
            return 1;
        }else if(ourNumber < otherNumber){
            return -1;
        }else{
            if(id > other.id){
                return 1;
            }else if(id < other.id){
                return -1;
            }
        }

        return 0;
    }

    public void setNetWin(int netWin){
        this.netWin = netWin;
        this.netWinWeighting = ArenaServer.instance.arenaServerService.getNetWinWeighting(netWin);
    }

    public static final Long getQueueKey(int serverId, int ownerId){
        return new Long((long) serverId << 32 | (long) ownerId);
    }

    public static final int getServerIdFromKey(Long key){
        return (int) (key >> 32);
    }

    public static final int getOwnerIdFromKey(Long key){
        return (int) (key & 0xFFFFFFFF);
    }
}
