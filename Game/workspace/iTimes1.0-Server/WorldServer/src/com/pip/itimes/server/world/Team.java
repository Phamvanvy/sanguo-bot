package com.pip.itimes.server.world;

import java.util.ArrayList;
import java.util.List;

import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Team {

    private int id;
    private List players = new ArrayList();
    private PositionSprite leader = null;

    public Team(int id,PositionSprite leader) {
        this.id = id;
        players.add(leader);
        this.leader = leader;
        leader.setTeam(this);
    }

    public int getId(){
        return id;
    }

    public PositionSprite getLeader(){
        return leader;
    }

    public PositionSprite[] getPlayers(){
    	PositionSprite[] ret = new PositionSprite[players.size()];
        players.toArray(ret);
        return ret;
    }

    public void addPlayer(PositionSprite player){
        players.add(player);
        player.setTeam(this);
    }

    public void removePlayer(PositionSprite player){
        for(int i=0;i<players.size();i++){
        	PositionSprite member = (PositionSprite)players.get(i);
            if(member.getId()==player.getId()){
                players.remove(i);
                member.setTeam(null);
                return;
            }
        }
    }

    public boolean contains(PositionSprite player){
        for(int i=0;i<players.size();i++){
        	PositionSprite member = (PositionSprite)players.get(i);
            if(member.getId()==player.getId()){
                return true;
            }
        }
        return false;
    }

    public boolean contains(int playerId){
        for(int i=0;i<players.size();i++){
        	PositionSprite member = (PositionSprite)players.get(i);
           if(member.getId()==playerId){
               return true;
           }
       }
       return false;

    }

    public int getCount(){
        return players.size();
    }

    public IPlayerData[] getMembers(int state){
        List l = new ArrayList(3);
        for(int i=0;i<players.size();i++){
        	IPlayerData player = (IPlayerData)players.get(i);
            if(player.getTeamState()==state){
                l.add(player);
            }
        }
        IPlayerData[] ret = new IPlayerData[l.size()];
        l.toArray(ret);
        return ret;
    }
}
