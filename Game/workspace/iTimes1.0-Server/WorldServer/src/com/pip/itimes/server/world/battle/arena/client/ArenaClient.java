package com.pip.itimes.server.world.battle.arena.client;

import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.arena.ArenaConstants;


public class ArenaClient{
    protected long beginTime;
    protected long endTime;
    
    //ownerId ArenaBattle
    protected ConcurrentHashMap<Integer, ArenaBattleClient> ownerId2battles = new ConcurrentHashMap<Integer, ArenaBattleClient>();
    //playerId ArenaBattle
    protected ConcurrentHashMap<Integer, ArenaBattleClient> playerId2battles = new ConcurrentHashMap<Integer, ArenaBattleClient>();

    public ArenaClient(){
    }

    public long getBeginTime(){
        return beginTime;
    }

    public void setBeginTime(long beginTime){
        this.beginTime = beginTime;
    }

    public long getEndTime(){
        return endTime;
    }

    public void setEndTime(long endTime){
        this.endTime = endTime;
    }
    
    public boolean isValid(){
        long now = System.currentTimeMillis();
        
        if(now >= beginTime && now <= endTime){
            return true;
        }else{
            return false;
        }
    }

    public int addToQueue(int type, int ownerId, String ownerName, int queuePlayerId, WorldPlayer[] players){
        synchronized(ownerId2battles){
            boolean ownerQueue = false;
            boolean otherQueue = false;
            boolean otherPlayerQueue = false;
            
            if(ownerId2battles.containsKey(ownerId)){
                ownerQueue = true;
            }
            
            for(int i = 0; i < players.length; i++){
                if(queuePlayerId != players[i].getId() && playerId2battles.containsKey(players[i].getId())){
                    otherPlayerQueue = true;
                    
                    break;
                }
            }
            
            if(playerId2battles.containsKey(queuePlayerId)){
                otherQueue = true;
            }
            
            if(ownerQueue || otherQueue || otherPlayerQueue){
                if(ownerQueue){
                    boolean found = false;
                    
                    for(int i = 0; i < players.length; i++){
                        if(queuePlayerId == players[i].getId()){
                            found = true;
                            
                            break;
                        }
                    }
                    
                    if(found){
                        return ArenaConstants.ARENA_QUEUE_DUPLICATE;
                    }else{
                        return ArenaConstants.ARENA_QUEUE_OTHER;
                    }
                }
                
                if(otherQueue){
                    return ArenaConstants.ARENA_QUEUE_DUPLICATE_OTHER;
                }
                
                if(otherPlayerQueue){
                    return ArenaConstants.ARENA_QUEUE_DUPLICATE_PLAYER;
                }
            }else{
                ArenaBattleClient battle = new ArenaBattleClient(type, ownerId, ownerName, players);
                
                ownerId2battles.put(ownerId, battle);
                
                for(int i = 0; i < players.length; i++){
                    playerId2battles.put(players[i].getId(), battle);
                }

                return ArenaConstants.ARENA_QUEUE_SUCCESSFUL;
            }
        }
        
        return ArenaConstants.ARENA_QUEUE_ERROR;
    }

    public int cancelQueue(int type, int ownerId, int playerId){
        synchronized(ownerId2battles){
            ArenaBattleClient battle = ownerId2battles.get(ownerId);
    
            if(battle == null){
                return ArenaConstants.ARENA_CANCEL_QUEUE_ERROR;
            }if(battle.getType() != type ){
                return ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE_OTHER;
            }else{
                boolean found = false;
                int[] playerIds = battle.getPlayerIds();
                
                for(int i = 0; i < playerIds.length; i++){
                    if(playerId == playerIds[i]){
                        found = true;
                        
                        break;
                    }
                }
                
                if(found){
                    if(battle.getBattleStarted()){
                        return ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE;
                    }
                }else{
                    return ArenaConstants.ARENA_CANCEL_QUEUE_OTHER;
                }
            }
    
            return ArenaConstants.ARENA_CANCEL_QUEUE_SUCCESSFUL;
        }
    }

    public boolean removeQueue(int ownerId){
        synchronized(ownerId2battles){
            ArenaBattleClient battle = ownerId2battles.remove(ownerId);
            
            if(battle != null){
                int[] playerIds = battle.getPlayerIds();
                
                for(int i = 0; i < playerIds.length; i++){
                    playerId2battles.remove(playerIds[i]);
                }
                
                return true;
            }
            
            return false;
        }
    }

    public ArenaBattleClient getBattleByOwner(int ownerId){
        synchronized(ownerId2battles){
            return ownerId2battles.get(ownerId);
        }
    }
    
    public ArenaBattleClient getBattleByPlayer(int playerId){
        synchronized(ownerId2battles){
            return playerId2battles.get(playerId);
        }
    }
}
