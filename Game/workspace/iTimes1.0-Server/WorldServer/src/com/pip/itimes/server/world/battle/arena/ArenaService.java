package com.pip.itimes.server.world.battle.arena;

import java.io.UnsupportedEncodingException;
import java.util.*;

import com.pip.itimes.net.*;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.ArenaTeam;
import com.pip.itimes.server.bean.ArenaTeam2Player;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.bean.Tong;
import com.pip.itimes.server.dao.ArenaTeamDao;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.dao.TongDao;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.TongUser;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.server.util.Utils;
import org.apache.log4j.Logger;
import com.pip.itimes.server.bean.TongIsland;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.world.CreatePlayerException;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.SchedulerManager;
import com.pip.itimes.server.world.StartIslandJob;
import com.pip.itimes.server.world.TongException;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.game.WorldService;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.dao.TongIslandDao;
import java.text.SimpleDateFormat;
import org.quartz.*;

/**
 * @author SKY
 * @version 1.0
 */
public class ArenaService{

    private static final Logger log = Logger.getLogger(ArenaService.class);

    private ArenaTeamDao arenateamDao;
    private PlayerDao playerDao;
    private PlayerService playerService;
    
    private Map<Integer,ArenaTeam> id2arena = new HashMap<Integer,ArenaTeam>();
    private Map<Integer,ArenaTeam> id2arena2 = new HashMap<Integer,ArenaTeam>();
    private Map<Integer,ArenaTeam> id2arena3 = new HashMap<Integer,ArenaTeam>();
    private Map<Integer,ArenaTeam> arenaid2arena = new HashMap<Integer,ArenaTeam>();
    
    private Map<Integer,ArrayList<ArenaTeam2Player>> id2player2 = new HashMap<Integer,ArrayList<ArenaTeam2Player>>();
    private Map<Integer,ArrayList<ArenaTeam2Player>> id2player3 = new HashMap<Integer,ArrayList<ArenaTeam2Player>>();
    public void setPlayerService(PlayerService playerService) {
		this.playerService = playerService;
	}
    
	public ArenaService(ArenaTeamDao arenateamDao,PlayerDao playerDao) {
        this.arenateamDao = arenateamDao;
        this.playerDao = playerDao;
    }
    
	public ArenaTeam createArena(IPlayerData player,String name,int type,Changed changed) throws ArenaException{
        synchronized(player){
        	switch(type){
	            case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队创建
	            	if (player.getArenaV1Id() != -1)
	                    throw new ArenaException("已经有所属战队");
	                if (player.getLevel() < getCreateArenaLevel())
	                    throw new ArenaException("只有达到90级才能创建战队");
	                if (player.getMoeny() < getCreateArena1Money())
	                    throw new ArenaException("建1v1战队需要160000J的资金");
	                if (name == null || name.length() == 0)
	                    throw new ArenaException("战队名字错误");
	                if(KeywordsUtil.isInvalidName(name.toLowerCase()))
	                    throw new ArenaException("战队名出现非法字符");
	                if(!Utils.checkString(name,false))
	                    throw new ArenaException("战队名出现非法字符");
	                try {
	                    if (name.getBytes("GBK").length > 12) {
	                        throw new ArenaException("战队名字太长");
	                    }
	                }  catch (UnsupportedEncodingException ex1) {
	                }
	                ArenaTeam arenateam = new ArenaTeam();
					try {
						arenateam = (ArenaTeam) arenateamDao.getArenaTeamByName(name);
						if(arenateam != null){
		                    throw new ArenaException("存在同名战队角色");
		                }
					} catch (DataAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					arenateam = new ArenaTeam();
	                arenateam.setArenaname(name);
	                arenateam.setArenalevel(1000);
	                arenateam.setCreatetime(new Date());
	                arenateam.setLastrepairtime(new Date());
	                arenateam.setMemebercount(1);
	                arenateam.setOwner(player.getId());
	                arenateam.setSlogan("");
	                arenateam.setType(type);
	                arenateam.setValid(true);
	                int playerId = player.getId();
	                try {
	                	arenateamDao.addArenaRecord(arenateam);
	                    player.decMoney(getCreateArena1Money(),changed);
	                    player.setArenaV1Id(arenateam.getId());
	                    if(player.getArenaLevel() == 0){
	                    	player.setArenaLevel(1000);
	                    }
	                    if (id2arena.containsKey(playerId)){
	                    	id2arena.remove(playerId);
		                	id2arena.put(playerId, arenateam);
		                }else{
		                	id2arena.put(playerId, arenateam);
		                }
	                    playerService.savePlayer(player);
	                    return arenateam;
	                } catch (DataAccessException ex) {
	                    throw new ArenaException("建立1v1战队错误.");
	                }
	            }

	            case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队创建
	            	if (player.getArenaV2Id() != -1)
	                    throw new ArenaException("已经有所属战队");
	                if (player.getLevel() < getCreateArenaLevel())
	                    throw new ArenaException("只有达到90级才能创建战队");
	                if (player.getMoeny() < getCreateArena2Money())
	                    throw new ArenaException("建2v2战队需要320000J的资金");
	                if (name == null || name.length() == 0)
	                    throw new ArenaException("战队名字错误");
	                if(KeywordsUtil.isInvalidName(name.toLowerCase()))
	                    throw new ArenaException("战队名出现非法字符");
	                if(!Utils.checkString(name,false))
	                    throw new ArenaException("战队名出现非法字符");
	                try {
	                    if (name.getBytes("GBK").length > 12) {
	                        throw new ArenaException("战队名字太长");
	                    }
	                }  catch (UnsupportedEncodingException ex1) {
	                }
	                ArenaTeam arenateam = new ArenaTeam();
					try {
						arenateam = (ArenaTeam) arenateamDao.getArenaTeamByName(name);
						if(arenateam != null){
		                    throw new ArenaException("存在同名战队角色");
		                }
					} catch (DataAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					arenateam = new ArenaTeam();
	                arenateam.setArenaname(name);
	                arenateam.setArenalevel(1000);
	                arenateam.setCreatetime(new Date());
	                arenateam.setLastrepairtime(new Date());
	                arenateam.setMemebercount(1);
	                arenateam.setOwner(player.getId());
	                arenateam.setSlogan("");
	                arenateam.setType(type);
	                arenateam.setValid(true);
	                int playerId = player.getId();
	                try {
	                	arenateamDao.addArenaRecord(arenateam);
	                    player.decMoney(getCreateArena2Money(),changed);
	                    player.setArenaV2Id(arenateam.getId());
	                    if(player.getArenaLevel() == 0){
	                    	player.setArenaLevel(1000);
	                    }
	                    if (id2arena2.containsKey(playerId)){
	                    	id2arena2.remove(playerId);
		                	id2arena2.put(playerId, arenateam);
		                }else{
		                	id2arena2.put(playerId, arenateam);
		                }
	                    //N v N 战队内成员
	                    ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
	                    arenaTeam2Player.setArenaId(arenateam.getId());
	                    arenaTeam2Player.setArenaLevel(arenateam.getArenalevel());
	                    arenaTeam2Player.setIsowner(true);
	                    arenaTeam2Player.setPlayerarenaLevel(player.getArenaLevel());
	                    arenaTeam2Player.setPlayerId(player.getId());
	                    arenaTeam2Player.setType(type);
	                    arenaTeam2Player.setPlayername(player.getPlayerName());
	                    ArrayList arenaTeam2Playertmp = new ArrayList();
	                    arenaTeam2Playertmp.add(arenaTeam2Player);
	                    if (id2player2.containsKey(arenateam.getId())){
	                    	id2player2.remove(arenateam.getId());
	                    	id2player2.put(arenateam.getId(), arenaTeam2Playertmp);
	                    }else{
	                    	id2player2.put(arenateam.getId(), arenaTeam2Playertmp);
		                }
	                    playerService.savePlayer(player);
	                    return arenateam;
	                } catch (DataAccessException ex) {
	                    throw new ArenaException("建立2v2战队错误.");
	                }
	            }

	            case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队创建
	            	if (player.getArenaV3Id() != -1)
	                    throw new ArenaException("已经有所属战队");
	                if (player.getLevel() < getCreateArenaLevel())
	                    throw new ArenaException("只有达到90级才能创建战队");
	                if (player.getMoeny() < getCreateArena3Money())
	                    throw new ArenaException("建3v3战队需要480000J的资金");
	                if (name == null || name.length() == 0)
	                    throw new ArenaException("战队名字错误");
	                if(KeywordsUtil.isInvalidName(name.toLowerCase()))
	                    throw new ArenaException("战队名出现非法字符");
	                if(!Utils.checkString(name,false))
	                    throw new ArenaException("战队名出现非法字符");
	                try {
	                    if (name.getBytes("GBK").length > 12) {
	                        throw new ArenaException("战队名字太长");
	                    }
	                }  catch (UnsupportedEncodingException ex1) {
	                }
	                ArenaTeam arenateam = new ArenaTeam();
					try {
						arenateam = (ArenaTeam) arenateamDao.getArenaTeamByName(name);
						if(arenateam != null){
		                    throw new ArenaException("存在同名战队角色");
		                }
					} catch (DataAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					arenateam = new ArenaTeam();
	                arenateam.setArenaname(name);
	                arenateam.setArenalevel(1000);
	                arenateam.setCreatetime(new Date());
	                arenateam.setLastrepairtime(new Date());
	                arenateam.setMemebercount(1);
	                arenateam.setOwner(player.getId());
	                arenateam.setSlogan("");
	                arenateam.setType(type);
	                arenateam.setValid(true);
	                int playerId = player.getId();
	                try {
	                	arenateamDao.addArenaRecord(arenateam);
	                    player.decMoney(getCreateArena3Money(),changed);
	                    player.setArenaV3Id(arenateam.getId());
	                    if(player.getArenaLevel() == 0){
	                    	player.setArenaLevel(1000);
	                    }
	                    if (id2arena3.containsKey(playerId)){
	                    	id2arena3.remove(playerId);
		                	id2arena3.put(playerId, arenateam);
		                }else{
		                	id2arena3.put(playerId, arenateam);
		                }
	                    //N v N 战队内成员
	                    ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
	                    arenaTeam2Player.setArenaId(arenateam.getId());
	                    arenaTeam2Player.setArenaLevel(arenateam.getArenalevel());
	                    arenaTeam2Player.setIsowner(true);
	                    arenaTeam2Player.setPlayerarenaLevel(player.getArenaLevel());
	                    arenaTeam2Player.setPlayerId(player.getId());
	                    arenaTeam2Player.setType(type);
	                    arenaTeam2Player.setPlayername(player.getPlayerName());
	                    ArrayList arenaTeam2Playertmp = new ArrayList();
	                    arenaTeam2Playertmp.add(arenaTeam2Player);
	                    if (id2player3.containsKey(arenateam.getId())){
	                    	id2player3.remove(arenateam.getId());
	                    	id2player3.put(arenateam.getId(), arenaTeam2Playertmp);
	                    }else{
	                    	id2player3.put(arenateam.getId(), arenaTeam2Playertmp);
		                }
	                    playerService.savePlayer(player);
	                    return arenateam;
	                } catch (DataAccessException ex) {
	                    throw new ArenaException("建立3v3战队错误.");
	                }
	            }
	            
        	}
        	return null;
        }
    }
	public void addArenaplayer(IPlayerData player,int arenaId,int arenalevle,int ownerId,int type) throws ArenaException{
        synchronized(player){
        	switch(type){
	            case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队
	            }
	            break;
	            case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队
	            	if (player.getArenaV2Id() != -1)
	                    throw new ArenaException("已经有所属战队");
	                if (player.getLevel() < getCreateArenaLevel())
	                    throw new ArenaException("只有达到90级才能加入战队");
	                
	                int playerId = player.getId();
	                player.setArenaV2Id(arenaId);
	                if(player.getArenaLevel() == 0){
	                	player.setArenaLevel(1000);
	                }
//					player.setArenaLevel2(1000);
					//N v N 战队内成员
					ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
					arenaTeam2Player.setArenaId(arenaId);
					arenaTeam2Player.setArenaLevel(arenalevle);
					arenaTeam2Player.setIsowner(false);
					arenaTeam2Player.setPlayerarenaLevel(player.getArenaLevel());
					arenaTeam2Player.setPlayerId(player.getId());
					arenaTeam2Player.setType(type);
					arenaTeam2Player.setPlayername(player.getPlayerName());
					ArrayList arenaTeam2Playertmp = new ArrayList();
					
					if (id2player2.containsKey(arenaId)){
						arenaTeam2Playertmp = id2player2.get(arenaId);
						arenaTeam2Playertmp.add(arenaTeam2Player);
						id2player2.remove(arenaId);
						id2player2.put(arenaId, arenaTeam2Playertmp);
					}else{
						arenaTeam2Playertmp = findArenaTeamPlayer(arenaId,type,ownerId,arenalevle);
						arenaTeam2Playertmp.add(arenaTeam2Player);
						id2player2.put(arenaId, arenaTeam2Playertmp);
					}
					playerService.savePlayer(player);
	            }
	            break;
	            case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队创建
	            	if (player.getArenaV3Id() != -1)
	                    throw new ArenaException("已经有所属战队");
	                if (player.getLevel() < getCreateArenaLevel())
	                    throw new ArenaException("只有达到90级才能加入战队");
	                
	                int playerId = player.getId();
	                player.setArenaV3Id(arenaId);
	                if(player.getArenaLevel() == 0){
                    	player.setArenaLevel(1000);
                    }
					//N v N 战队内成员
					ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
					arenaTeam2Player.setArenaId(arenaId);
					arenaTeam2Player.setArenaLevel(arenalevle);
					arenaTeam2Player.setIsowner(false);
					arenaTeam2Player.setPlayerarenaLevel(player.getArenaLevel());
					arenaTeam2Player.setPlayerId(player.getId());
					arenaTeam2Player.setType(type);
					arenaTeam2Player.setPlayername(player.getPlayerName());
					ArrayList arenaTeam2Playertmp = new ArrayList();
					
					if (id2player3.containsKey(arenaId)){
						arenaTeam2Playertmp = id2player3.get(arenaId);
						arenaTeam2Playertmp.add(arenaTeam2Player);
						id2player3.remove(arenaId);
						id2player3.put(arenaId, arenaTeam2Playertmp);
					}else{
						arenaTeam2Playertmp = findArenaTeamPlayer(arenaId,type,ownerId,arenalevle);
						arenaTeam2Playertmp.add(arenaTeam2Player);
						id2player3.put(arenaId, arenaTeam2Playertmp);
					}
					playerService.savePlayer(player);
	            }
	            
        	}
        }
    }
    public static int getCreateArenaLevel(){
        return 90;
    }
    public static int getCreateArena1Money(){
        return 160000;
    }
    public static int getCreateArena2Money(){
        return 320000;
    }
    public static int getCreateArena3Money(){
        return 480000;
    }
    public static int getCreateArena2count(){
        return 4;
    }
    public static int getCreateArena3count(){
        return 6;
    }
    public ArenaTeam findArenaTeam(int ownerid,int type) {
        try {
        	switch(type){
	        	case ArenaConstants.ARENA_TYPE_ONE: { //1v1
	        		if (id2arena.containsKey(ownerid)){
	            		return id2arena.get(ownerid);
	                }else{
	                	ArenaTeam arenaTeam = (ArenaTeam) arenateamDao.findArenaTeam(ownerid , type);
	                	if (arenaTeam != null){
	                		id2arena.put(arenaTeam.getOwner(), arenaTeam);
	                	}
	                	return arenaTeam;
	                }
	        	}
	        	case ArenaConstants.ARENA_TYPE_TWO: { //2v2
	        		if (id2arena2.containsKey(ownerid)){
	            		return id2arena2.get(ownerid);
	                }else{
	                	ArenaTeam arenaTeam = (ArenaTeam) arenateamDao.findArenaTeam(ownerid , type);
	                	if (arenaTeam != null){
	                		id2arena2.put(arenaTeam.getOwner(), arenaTeam);
	                	}
	                	return arenaTeam;
	                }
	        	}
	        	case ArenaConstants.ARENA_TYPE_THREE: { //3v3
	        		if (id2arena3.containsKey(ownerid)){
	            		return id2arena3.get(ownerid);
	                }else{
	                	ArenaTeam arenaTeam = (ArenaTeam) arenateamDao.findArenaTeam(ownerid , type);
	                	if (arenaTeam != null){
	                		id2arena3.put(arenaTeam.getOwner(), arenaTeam);
	                	}
	                	return arenaTeam;
	                }
	        	}
        	}
        	return null;
        } catch (DataAccessException ex) {
            return null;
        }
    }
    public ArenaTeam findArenaTeamByarenaId(int arenaId) {
        try {
        	if (arenaid2arena.containsKey(arenaId)){
        		return arenaid2arena.get(arenaId);
            }else{
            	ArenaTeam arenaTeam = (ArenaTeam) arenateamDao.findArenaTeamByArenaId(arenaId);
            	if (arenaTeam != null){
            		arenaid2arena.put(arenaTeam.getId(), arenaTeam);
            	}
            	return arenaTeam;
            }
        } catch (DataAccessException ex) {
            return null;
        }
    }
    public ArrayList findArenaTeamPlayer(int arenaid,int type,int ownerId,int arenalevle) {
        try {
        	ArrayList result = new ArrayList();
        	if (type == 2){
        		if (id2player2.containsKey(arenaid)){
        			return id2player2.get(arenaid);
        		}else{
        			List list_tmp = playerDao.getPlayerList_arena(arenaid,type);
        			if ((list_tmp!=null) && (list_tmp.size() > 0)){
        				ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
        				for (int i = 0; i < list_tmp.size(); i++){
        					Object[] list_tmp2 = (Object[]) list_tmp.get(i);
        					if ((list_tmp2!=null) && (list_tmp2.length > 0)){
        						int playerId = (Integer)list_tmp2[0];
        						String playername = (String) list_tmp2[1];
        						int playerarenaLevel = (Integer)list_tmp2[2];
        						arenaTeam2Player = new ArenaTeam2Player();
        						arenaTeam2Player.setArenaId(arenaid);
        						arenaTeam2Player.setArenaLevel(arenalevle);
        						arenaTeam2Player.setPlayerId(playerId);
        						arenaTeam2Player.setPlayerarenaLevel(playerarenaLevel);
        						arenaTeam2Player.setPlayername(playername);
        						arenaTeam2Player.setType(type);
        						if (ownerId == playerId){
        							arenaTeam2Player.setIsowner(true);
        						}else{
        							arenaTeam2Player.setIsowner(false);
        						}
        						result.add(arenaTeam2Player);
        					}
        				}
        			}
        			id2player2.put(arenaid, result);
        		}
        	}else if (type == 3){
        		if (id2player3.containsKey(arenaid)){
        			return id2player3.get(arenaid);
        		}else{
        			List list_tmp = playerDao.getPlayerList_arena(arenaid,type);
        			if ((list_tmp!=null) && (list_tmp.size() > 0)){
        				ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
        				for (int i = 0; i < list_tmp.size(); i++){
        					Object[] list_tmp2 = (Object[]) list_tmp.get(i);
        					if ((list_tmp2!=null) && (list_tmp2.length > 0)){
        						int playerId = (Integer)list_tmp2[0];
        						String playername = (String) list_tmp2[1];
        						int playerarenaLevel = (Integer)list_tmp2[2];
        						arenaTeam2Player = new ArenaTeam2Player();
        						arenaTeam2Player.setArenaId(arenaid);
        						arenaTeam2Player.setArenaLevel(arenalevle);
        						arenaTeam2Player.setPlayerId(playerId);
        						arenaTeam2Player.setPlayerarenaLevel(playerarenaLevel);
        						arenaTeam2Player.setPlayername(playername);
        						arenaTeam2Player.setType(type);
        						if (ownerId == playerId){
        							arenaTeam2Player.setIsowner(true);
        						}else{
        							arenaTeam2Player.setIsowner(false);
        						}
        						result.add(arenaTeam2Player);
        					}
        				}
        			}
        			id2player3.put(arenaid, result);
        		}
        	}
        	return result;
        } catch (DataAccessException ex) {
            return null;
        }
    }
    public void killArenaTeamPlayercatch(int arenaid,int type,int ownerId,int arenalevle,int killplayerId) throws ArenaException {
        try {
        	ArrayList result = new ArrayList();
        	if (type == 2){
        		if (id2player2.containsKey(arenaid)){
        			result = id2player2.get(arenaid);
        			for (int i = 0; i < result.size(); i++){
        				ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
        				arenaTeam2Player = (ArenaTeam2Player) result.get(i);
        				if (arenaTeam2Player.getPlayerId() == killplayerId){
        					result.remove(i);
        					break;
        				}
        			}
        			id2player2.remove(arenaid);
        			id2player2.put(arenaid,result);
        		}else{
        			List list_tmp = playerDao.getPlayerList_arena(arenaid,type);
        			if ((list_tmp!=null) && (list_tmp.size() > 0)){
        				ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
        				for (int i = 0; i < list_tmp.size(); i++){
        					Object[] list_tmp2 = (Object[]) list_tmp.get(i);
        					if ((list_tmp2!=null) && (list_tmp2.length > 0)){
        						int playerId = (Integer)list_tmp2[0];
        						String playername = (String) list_tmp2[1];
        						int playerarenaLevel = (Integer)list_tmp2[2];
        						arenaTeam2Player = new ArenaTeam2Player();
        						arenaTeam2Player.setArenaId(arenaid);
        						arenaTeam2Player.setArenaLevel(arenalevle);
        						arenaTeam2Player.setPlayerId(playerId);
        						arenaTeam2Player.setPlayerarenaLevel(playerarenaLevel);
        						arenaTeam2Player.setPlayername(playername);
        						arenaTeam2Player.setType(type);
        						if (ownerId == playerId){
        							arenaTeam2Player.setIsowner(true);
        						}else{
        							arenaTeam2Player.setIsowner(false);
        						}
        						if (arenaTeam2Player.getPlayerId() != killplayerId){
        							result.add(arenaTeam2Player);
        						}
        					}
        				}
        			}
        			id2player2.put(arenaid, result);
        		}
        	}else if (type == 3){
        		if (id2player3.containsKey(arenaid)){
        			result = id2player3.get(arenaid);
        			for (int i = 0; i < result.size(); i++){
        				ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
        				arenaTeam2Player = (ArenaTeam2Player) result.get(i);
        				if (arenaTeam2Player.getPlayerId() == killplayerId){
        					result.remove(i);
        					break;
        				}
        			}
        			id2player3.remove(arenaid);
        			id2player3.put(arenaid,result);
        		}else{
        			List list_tmp = playerDao.getPlayerList_arena(arenaid,type);
        			if ((list_tmp!=null) && (list_tmp.size() > 0)){
        				ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
        				for (int i = 0; i < list_tmp.size(); i++){
        					Object[] list_tmp2 = (Object[]) list_tmp.get(i);
        					if ((list_tmp2!=null) && (list_tmp2.length > 0)){
        						int playerId = (Integer)list_tmp2[0];
        						String playername = (String) list_tmp2[1];
        						int playerarenaLevel = (Integer)list_tmp2[2];
        						arenaTeam2Player = new ArenaTeam2Player();
        						arenaTeam2Player.setArenaId(arenaid);
        						arenaTeam2Player.setArenaLevel(arenalevle);
        						arenaTeam2Player.setPlayerId(playerId);
        						arenaTeam2Player.setPlayerarenaLevel(playerarenaLevel);
        						arenaTeam2Player.setPlayername(playername);
        						arenaTeam2Player.setType(type);
        						if (ownerId == playerId){
        							arenaTeam2Player.setIsowner(true);
        						}else{
        							arenaTeam2Player.setIsowner(false);
        						}
        						if (arenaTeam2Player.getPlayerId() != killplayerId){
        							result.add(arenaTeam2Player);
        						}
        					}
        				}
        			}
        			id2player3.put(arenaid, result);
        		}
        	}
        } catch (DataAccessException ex) {
        	throw new ArenaException("离开2v2战队错误");
        }
    }
    public void killArenaTeamPlayer(int arenaid,int type,int playerId) throws ArenaException {
        try {
        	ArrayList result = new ArrayList();
        	if (type == 2){
        		if ((playerDao.getPlayerArenaAll(playerId, 1) == -1) && 
						(playerDao.getPlayerArenaAll(playerId, 3) == -1)){
        			playerDao.setPlayerArenalevel0(playerId);
				}
        		playerDao.killArenateamPlayer(arenaid,type);
        	}else if (type == 3){
        		if ((playerDao.getPlayerArenaAll(playerId, 1) == -1) && 
						(playerDao.getPlayerArenaAll(playerId, 2) == -1)){
        			playerDao.setPlayerArenalevel0(playerId);
				}
        		playerDao.killArenateamPlayer(arenaid,type);
        	}
        } catch (DataAccessException ex) {
        	throw new ArenaException("解散2v2战队错误");
        }
    }
    public void updateArenaTeamPlayercatch(int arenaid,int type,int ownerId,int playerarenalevle,int playerId) throws ArenaException {
        try {
        	ArrayList result = new ArrayList();
        	if (type == 2){
        		if (id2player2.containsKey(arenaid)){
        			result = id2player2.get(arenaid);
        			for (int i = 0; i < result.size(); i++){
        				ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
        				arenaTeam2Player = (ArenaTeam2Player) result.get(i);
        				if (arenaTeam2Player.getPlayerId() == playerId){
        					result.remove(i);
        					arenaTeam2Player.setPlayerarenaLevel(playerarenalevle);
        					result.add(arenaTeam2Player);
        					break;
        				}
        			}
        			id2player2.remove(arenaid);
        			id2player2.put(arenaid,result);
        		}
        	}else if (type == 3){
        		if (id2player3.containsKey(arenaid)){
        			result = id2player3.get(arenaid);
        			for (int i = 0; i < result.size(); i++){
        				ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
        				arenaTeam2Player = (ArenaTeam2Player) result.get(i);
        				if (arenaTeam2Player.getPlayerId() == playerId){
        					result.remove(i);
        					arenaTeam2Player.setPlayerarenaLevel(playerarenalevle);
        					result.add(arenaTeam2Player);
        					break;
        				}
        			}
        			id2player3.remove(arenaid);
        			id2player3.put(arenaid,result);
        		}
        	}
        } catch (Exception ex) {
        	throw new ArenaException("离开2v2战队错误");
        }
    }
    
    public void quit(ArenaTeam arenateam,IPlayerData player,int type) throws ArenaException{
        synchronized(player){
        	switch(type){
            case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队解散
            	if(player.getArenaV1Id()==-1)
                    return;
            	int playerId = player.getId();
                try {
					arenateamDao.deleteArenateam(arenateam.getOwner(),arenateam.getType());
					player.setArenaV1Id(-1);
					if((player.getArenaV2Id() == -1) && (player.getArenaV3Id() == -1)){
						player.setArenaLevel(0);
					}
	                if (id2arena.containsKey(playerId)){
	                	id2arena.remove(playerId);
	                }
	                playerService.savePlayer(player);
				} catch (DataAccessException e) {
					throw new ArenaException("解散1v1战队错误");
				}
            }
            break;
            case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队解散
            	if(player.getArenaV2Id()==-1)
                    return;
            	int playerId = player.getId();
                try {
					arenateamDao.deleteArenateam(arenateam.getOwner(),arenateam.getType());
					player.setArenaV2Id(-1);
					if((player.getArenaV1Id() == -1) && (player.getArenaV3Id() == -1)){
						player.setArenaLevel(0);
					}
	                if (id2arena2.containsKey(playerId)){
	                	id2arena2.remove(playerId);
	                }
	                playerService.savePlayer(player);
				} catch (DataAccessException e) {
					throw new ArenaException("解散2v2战队错误");
				}
            }
            break;
            case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队解散
            	if(player.getArenaV3Id()==-1)
                    return;
            	int playerId = player.getId();
                try {
					arenateamDao.deleteArenateam(arenateam.getOwner(),arenateam.getType());
					player.setArenaV3Id(-1);
					if((player.getArenaV1Id() == -1) && (player.getArenaV2Id() == -1)){
						player.setArenaLevel(0);
					}
	                if (id2arena3.containsKey(playerId)){
	                	id2arena3.remove(playerId);
	                }
	                playerService.savePlayer(player);
				} catch (DataAccessException e) {
					throw new ArenaException("解散3v3战队错误");
				}
            }
            break;
        	}
        }
    }
    
    public void addArenaLevel(int playerID,int point,int type) throws ArenaException{
        
    	switch(type){
	        case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队-战队等级变化
	            if (id2arena.containsKey(playerID)){
	            	ArenaTeam arenateam = id2arena.get(playerID);
	            	arenateam.setArenalevel(arenateam.getArenalevel() + point);
	            	id2arena.remove(playerID);
	            	id2arena.put(playerID, arenateam);
	            	arenateamDao.updateArenaLevel(playerID,point,type);
	            	if (arenaid2arena.containsKey(arenateam.getId())){
	            		arenaid2arena.remove(arenateam.getId());
	            		arenaid2arena.put(arenateam.getId(), arenateam);
	            	}else{
	            		arenaid2arena.put(arenateam.getId(), arenateam);
	            	}
	            }else{
	            	try {
						ArenaTeam arenateam = arenateamDao.findArenaTeam(playerID,type);
						if(arenateam!=null){
							arenateam.setArenalevel(arenateam.getArenalevel() + point);
			            	id2arena.put(playerID, arenateam);
			            	arenateamDao.updateArenaLevel(playerID,point,type);
			            	if (arenaid2arena.containsKey(arenateam.getId())){
			            		arenaid2arena.remove(arenateam.getId());
			            		arenaid2arena.put(arenateam.getId(), arenateam);
			            	}else{
			            		arenaid2arena.put(arenateam.getId(), arenateam);
			            	}
						}
					} catch (DataAccessException e) {
						throw new ArenaException("1v1战队不存在");
					}
	            }
	        }
	        break;
	        case ArenaConstants.ARENA_TYPE_TWO: {
	        	if (id2arena2.containsKey(playerID)){
	            	ArenaTeam arenateam = id2arena2.get(playerID);
	            	arenateam.setArenalevel(arenateam.getArenalevel() + point);
	            	id2arena2.remove(playerID);
	            	id2arena2.put(playerID, arenateam);
	            	arenateamDao.updateArenaLevel(playerID,point,type);
	            	if (arenaid2arena.containsKey(arenateam.getId())){
	            		arenaid2arena.remove(arenateam.getId());
	            		arenaid2arena.put(arenateam.getId(), arenateam);
	            	}else{
	            		arenaid2arena.put(arenateam.getId(), arenateam);
	            	}
	            }else{
	            	try {
						ArenaTeam arenateam = arenateamDao.findArenaTeam(playerID,type);
						if(arenateam!=null){
							arenateam.setArenalevel(arenateam.getArenalevel() + point);
			            	id2arena2.put(playerID, arenateam);
			            	arenateamDao.updateArenaLevel(playerID,point,type);
			            	if (arenaid2arena.containsKey(arenateam.getId())){
			            		arenaid2arena.remove(arenateam.getId());
			            		arenaid2arena.put(arenateam.getId(), arenateam);
			            	}else{
			            		arenaid2arena.put(arenateam.getId(), arenateam);
			            	}
						}
					} catch (DataAccessException e) {
						throw new ArenaException("2v2战队不存在");
					}
	            }
	        }
	        break;
	        case ArenaConstants.ARENA_TYPE_THREE: {
	        	if (id2arena3.containsKey(playerID)){
	            	ArenaTeam arenateam = id2arena3.get(playerID);
	            	arenateam.setArenalevel(arenateam.getArenalevel() + point);
	            	id2arena3.remove(playerID);
	            	id2arena3.put(playerID, arenateam);
	            	arenateamDao.updateArenaLevel(playerID,point,type);
	            	if (arenaid2arena.containsKey(arenateam.getId())){
	            		arenaid2arena.remove(arenateam.getId());
	            		arenaid2arena.put(arenateam.getId(), arenateam);
	            	}else{
	            		arenaid2arena.put(arenateam.getId(), arenateam);
	            	}
	            }else{
	            	try {
						ArenaTeam arenateam = arenateamDao.findArenaTeam(playerID,type);
						if(arenateam!=null){
							arenateam.setArenalevel(arenateam.getArenalevel() + point);
							id2arena3.put(playerID, arenateam);
			            	arenateamDao.updateArenaLevel(playerID,point,type);
			            	if (arenaid2arena.containsKey(arenateam.getId())){
			            		arenaid2arena.remove(arenateam.getId());
			            		arenaid2arena.put(arenateam.getId(), arenateam);
			            	}else{
			            		arenaid2arena.put(arenateam.getId(), arenateam);
			            	}
						}
					} catch (DataAccessException e) {
						throw new ArenaException("3v3战队不存在");
					}
	            }
	        }
    	}
    }

	public String getArenaName(int playerID,int type) throws ArenaException{
        
    	switch(type){
	        case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队-战队等级变化
	            if (id2arena.containsKey(playerID)){
	            	ArenaTeam arenateam = id2arena.get(playerID);
	            	return arenateam.getArenaname();
	            }else{
	            	try {
						ArenaTeam arenateam = arenateamDao.findArenaTeam(playerID,type);
						if(arenateam!=null){
							id2arena.put(playerID, arenateam);
							return arenateam.getArenaname();
						}
					} catch (DataAccessException e) {
						throw new ArenaException("1v1战队不存在");
					}
	            }
	        }
	        break;
	        case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队-战队等级变化
	            if (id2arena2.containsKey(playerID)){
	            	ArenaTeam arenateam = id2arena2.get(playerID);
	            	return arenateam.getArenaname();
	            }else{
	            	try {
						ArenaTeam arenateam = arenateamDao.findArenaTeam(playerID,type);
						if(arenateam!=null){
							id2arena2.put(playerID, arenateam);
							return arenateam.getArenaname();
						}
					} catch (DataAccessException e) {
						throw new ArenaException("2v2战队不存在");
					}
	            }
	        }
	        break;
	        case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队-战队等级变化
	            if (id2arena3.containsKey(playerID)){
	            	ArenaTeam arenateam = id2arena3.get(playerID);
	            	return arenateam.getArenaname();
	            }else{
	            	try {
						ArenaTeam arenateam = arenateamDao.findArenaTeam(playerID,type);
						if(arenateam!=null){
							id2arena3.put(playerID, arenateam);
							return arenateam.getArenaname();
						}
					} catch (DataAccessException e) {
						throw new ArenaException("3v3战队不存在");
					}
	            }
	        }
    	}
    	return "";
    }
}

