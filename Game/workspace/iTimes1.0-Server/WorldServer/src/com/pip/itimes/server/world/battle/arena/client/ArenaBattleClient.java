package com.pip.itimes.server.world.battle.arena.client;

import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.battle.arena.ArenaConstants;

public class ArenaBattleClient{
    private int type;
    private int ownerId;
    private String ownerName;
    private int id;
    private int round = 0;
    private boolean battleStarted = false;
    private int[] playerIds;
    private String[] playerNames;
    private int[] playerLevels;
    private int[] playerArenaLevels;

    public ArenaBattleClient(int type, int ownerId, String ownerName, WorldPlayer[] players){
        this.type = type;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        
        switch(type){
            case ArenaConstants.ARENA_TYPE_ONE:
                playerIds = new int[1];
                playerNames = new String[1];
                playerLevels = new int[1];
                playerArenaLevels = new int[1];
                
                break;
            case ArenaConstants.ARENA_TYPE_TWO:
                playerIds = new int[2];
                playerNames = new String[2];
                playerLevels = new int[2];
                playerArenaLevels = new int[2];
                
                break;
            case ArenaConstants.ARENA_TYPE_THREE:
                playerIds = new int[3];
                playerNames = new String[3];
                playerLevels = new int[3];
                playerArenaLevels = new int[3];
                
                break;
        }
        
        for(int i = 0; i < players.length; i++){
            playerIds[i] = players[i].getId();
            playerNames[i] = players[i].getPlayerName();
            playerLevels[i] = players[i].getLevel();
            playerArenaLevels[i] = players[i].getArenaLevel();
        }
    }
    
    public int getType(){
        return type;
    }
    
    public int getOwnerId(){
        return ownerId;
    }
    
    public String getOwnerName(){
        return ownerName;
    }
    
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }
    
    public int[] getPlayerIds(){
        return playerIds;
    }
    
    public String[] getPlayerNames(){
        return playerNames;
    }
    
    public int[] getPlayerLevels(){
        return playerLevels;
    }
    
    public int[] getPlayerArenaLevels(){
        return playerArenaLevels;
    }

    public int getRound(){
        return round;
    }

    public void setRound(int round){
        this.round = round;
    }

    public boolean getBattleStarted(){
        return battleStarted;
    }

    public void setBattleStarted(boolean battleStarted){
        this.battleStarted = battleStarted;
    }

    public UWAPSegment fight(UWAPData data, int ownerId, int playerId) throws Exception{
        short roundId = data.readShort();
        int action = data.readInt();
        byte target = data.readByte();
        int petAction = data.readInt();
        byte petTarget = data.readByte();

        return fight(ownerId, playerId, roundId, action, target, petAction, petTarget);
    }

    public UWAPSegment fight(int owenrId, int playerId, short roundId, int action, byte target, int petAction, byte petTarget){
        if(roundId != round + 1){
            return null;
        }

        int realAction = (short) (action & 0xFFFF);

        if(realAction == Skill.SKILL_ITEM){
            action = Skill.ATTACK_SKILL.id & 0xFFFF;
        }

        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_BATTLE_FIGHT);
        seg.writeInt(type);
        seg.writeInt(owenrId);
        seg.writeInt(playerId);

        seg.writeShort(roundId);
        seg.writeInt(action);
        seg.write(target);
        seg.writeInt(petAction);
        seg.write(petTarget);

        return seg;
    }
}
