package com.pip.itimes.server.world.game;

import java.util.ArrayList;
import java.util.List;

import com.pip.itimes.server.util.IntHashSet;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Instance {

    //所有有此副本进度的角色的id
    protected IntHashSet playerIds = new IntHashSet();

    //当前激活的角色的id，激活表明角色在此副本中
    protected IntHashSet activeIds = new IntHashSet();

    protected List maps = new ArrayList();

    protected int id;

    //副本入口，不属于副本
    protected GameMap entrance = null;
    protected InstanceDefinition definition;

    protected InstanceService service;



    public Instance(int id,InstanceDefinition idf,InstanceService service) {
        this.id = id;
        definition = idf;
        this.service = service;
    }

    public InstanceDefinition getDefinition(){
        return definition;
    }

    public int getId(){
        return id;
    }

    public int getInstanceId(){
        return definition.getId();
    }

    public void setEntrance(GameMap entrance){
        this.entrance = entrance;
    }

    public GameMap getEntrance(){
        return entrance;
    }

    public void addMap(GameMap map){
        maps.add(map);
    }

    public GameMap[] getMaps(){
        GameMap[] ret = new GameMap[maps.size()];
        maps.toArray(ret);
        return ret;
    }

    public GameMap getMap(short id){
        for(int i=0;i<maps.size();i++){
            GameMap map = (GameMap)maps.get(i);
            if(map.getMapId()==id){
                return map;
            }
        }
        return null;
    }

    public int getMaxPlayer(){
        return definition.getMaxPlayer();
    }

    public int getRefreshSecond(){
        return definition.getRefreshSecond();
    }

    public void addPlayer(IPlayerData player){
        if(!playerIds.contains(player.getId())){
            playerIds.add(player.getId());
            definition.getModel().playerAddedToInstance(player,this);
        }
    }

//    public void addPlayer(Integer id){
//        playerIds.add(id);
//    }

    public int[] getPlayers(){
        return playerIds.getValues();
    }

    public boolean contains(int playerId){
        return playerIds.contains(playerId);
    }

    public boolean activeContains(int playerId){
        return activeIds.contains(playerId);
    }

    public void removePlayer(int playerId){
        playerIds.remove(playerId);
//        playerIds.removeElement(playerId);
    }




   public boolean setActive(int id){
       if(!activeIds.contains(id)){
           activeIds.add(id);
           return true;
       }
       return false;
   }

//   public void removeActive(int playerId){
//       removeActive(new Integer(playerId));
//   }

   public boolean removeActive(int id){
       return activeIds.remove(id);
   }

   public boolean canAdd(WorldPlayer player){
       if(activeIds.contains(player.getId())){
           return true;
       }
       if(activeIds.size()>=getMaxPlayer()){
           return false;
       }
       return true;
   }

   public void preAdd(IPlayerData[] players) throws InstanceException{
       int count = players.length;
       for(int i=0;i<players.length;i++){
           if(activeIds.contains(players[i].getId()))
               count--;
       }
       if(activeIds.size()+count<=getMaxPlayer()){
           for(int i=0;i<players.length;i++){
               addPlayer(players[i]);
           }
       }else{
           throw new InstantiationError("副本已满");
       }

   }

   public void kickPlayer(int playerId){
       activeIds.remove(playerId);
       playerIds.remove(playerId);
//       activeIds.removeElement(playerId);
//       playerIds.removeElement(playerId);
       if(playerIds.size()==0){
           service.instanceEmpty(this);
       }
    }

    public int[] getActives(){
        return activeIds.getValues();
    }

    public int getPlayerCount(){
        return playerIds.size();
    }

}
