package com.pip.itimes.server.world.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.CampBattlefieldService;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.PositionSprite;
import com.pip.itimes.server.world.Team;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.Battle2;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.battle.ClientBattle2;
import com.pip.itimes.server.world.battle.ResourcesBattlefieldPKBattle;

/**
 * 阵营战场：资源争夺战模型
 * @author hchen
 *
 */
public class BattleForResourcesInstanceModel implements CampBattlefieldInstanceModel {
	private static final Logger log = Logger.getLogger(BattleForResourcesInstanceModel.class);
	
	private ChatService chatService;
    private MailService mailService;
    private WorldService worldService;
    private InstanceService instanceService;
    private CampBattlefieldService campBattlefieldService;
    private BattleService2 battleService;
    
	private Set<ResourcesBattlefieldPKBattle> battles = new HashSet<ResourcesBattlefieldPKBattle>();
    private CampBattlefieldInstance ID_instances = null;
    private Map<Integer, Integer> playerID_instances = new HashMap<Integer, Integer>();
    private Map<Integer, Map> playerID_maps = new HashMap<Integer, Map>();
    
    public void setChatService (ChatService chatService) {
    	this.chatService = chatService;
    }
    
    public void setMailService (MailService mailService) {
    	this.mailService = mailService;
    }
    
    public void setWorldService (WorldService worldService) {
    	this.worldService = worldService;
    }
    
    public void setInstanceService (InstanceService instanceService) {
    	this.instanceService = instanceService;
    }
    
    public void setCampBattlefieldService(CampBattlefieldService campBattlefieldService) {
    	this.campBattlefieldService = campBattlefieldService;
    }
    
    public void setBattleService (BattleService2 battleService) {
    	this.battleService = battleService;
    }
    
    /**
     * 战场开启
     */
    public int start (int instanceID, String name, String type, int levelType, long forbidEnterTime, CampBattlefieldPlayer[] players)
		throws CampBattlefieldException {
		CampBattlefieldInstance instance = createInstance(instanceID, name, type, levelType, players);
		return instance.getId();
	}
    
    /**
     * 尝试进入战场
     */
    public synchronized Instance tryGotoInstance (int ID, WorldPlayer player, int battleID)
		throws InstanceException {
    	GameMap map = player.getMap();
        if (map == null) {
        	campBattlefieldService.getConnectService().sendMessage(player.getId(), "位置错误，不能进入");
//            throw new InstanceException("位置错误，不能进入");
        	log.info("Battle Resources tryGotoInstance Position Error");
        	return null;
        }
        if (map.getMapId() == CampBattlefieldConfig.WORLD_MAP) {
        	campBattlefieldService.getConnectService().sendMessage(player.getId(), "您所在的区域无法进入战场。");
        	log.info("Battle Resources tryGotoInstance in WORLD_MAP");
//        	throw new InstanceException("您所在的区域无法进入战场。");
        	return null;
        }
        if (map.getInstance() != null) {
        	campBattlefieldService.getConnectService().sendMessage(player.getId(), "您所在的区域无法进入战场。");
        	log.info("Battle Resources tryGotoInstance map Instance null");
//        	throw new InstanceException("您所在的区域无法进入战场。");
        	return null;
        }
		CampBattlefieldInstance instance = getInstance(player, ID);
		if (instance != null) {
			if (instance.isClosed()) {
				if (campBattlefieldService.containsDraggedPlayers(player.getId())) {
					CampBattlefieldPlayer cbPlayer = campBattlefieldService.getDraggedPlayer().get(player.getId());
					campBattlefieldService.restoreQueuePositionPlayer(instance, cbPlayer);
					campBattlefieldService.getDraggedPlayer().remove(player.getId());
				}
				campBattlefieldService.getConnectService().sendMessage(player.getId(), "你所传送的战场已经关闭已经将你排入到队列首名等待！");
				log.info("Battle Resources tryGotoInstance closed");
//				throw new InstanceException("你所传送的战场已经关闭已经将你排入到队列首名等待！");
				return null;
			}
			int brightPlayerCount = getCampBattlefieldPlayer(instance, Utils.CAMP_BRIGHT);
	    	int darkPlayerCount = getCampBattlefieldPlayer(instance, Utils.CAMP_DARK);
	    	int maxDark = CampBattlefieldConfig.battlefields.get(instance.getName()).getCampbattlefieldWarrior(instance.getLevelType()).getDarkplayers();
			int maxBright = CampBattlefieldConfig.battlefields.get(instance.getName()).getCampbattlefieldWarrior(instance.getLevelType()).getBrightplayers();
			
			if (brightPlayerCount + darkPlayerCount >= maxBright + maxDark) {
				if (campBattlefieldService.containsDraggedPlayers(player.getId())) {
					CampBattlefieldPlayer cbPlayer = campBattlefieldService.getDraggedPlayer().get(player.getId());
					campBattlefieldService.restoreQueuePositionPlayer(instance, cbPlayer);
					campBattlefieldService.getDraggedPlayer().remove(player.getId());
				}
				campBattlefieldService.getConnectService().sendMessage(player.getId(), "你所传送的战场已经满员，已经将你排入到队列首名等待！");
				log.info("Battle Resources tryGotoInstance full players");
//				throw new InstanceException("你所传送的战场已经满员，已经将你排入到队列首名等待！");
				return null;
			}
			
			
			CampBattlefield campBattlefield = CampBattlefieldConfig.battlefields.get(instance.getName());
			if(campBattlefield == null){
				return null;
			}
			
			CampBattlefieldPlayer cbp = instance.getBattlefieldPlayer(player.getId());
			
			if(battleService.getBattleByPlayer(player.getId()) != null || battleID >= 0){
				if (campBattlefieldService.containsDraggedPlayers(player.getId())) {
					CampBattlefieldPlayer cbPlayer = campBattlefieldService.getDraggedPlayer().get(player.getId());
					campBattlefieldService.restoreQueuePositionPlayer(instance, cbPlayer);
					campBattlefieldService.getDraggedPlayer().remove(player.getId());
				}else{
					campBattlefieldService.addWaitingPlayers(instance.getName(), instance.getLevelType(), player, cbp.getJoinTime(), cbp.random);
					instance.removeActive(player.getId());
					instance.removeBattlefieldPlayer(player.getId());
					instance.removePlayer(player.getId());
				}
				campBattlefieldService.getConnectService().sendMessage(player.getId(), "由于传送时您正在战斗中，不能进行传送，已经将你排入到队列首名等待！");
				chatService.sendPrivateMessage(-1, "系统", player.getId(), "由于传送时您正在战斗中，不能进行传送，已经将你排入到队列首名等待！");
				log.info("Battle Resources tryGotoInstance player in battle playerID[" + player.getId() + "] battlefieldID[" + instance.getId() + "]");
				return null;
			}
			
			int campTeam = cbp.getCampTeam();
			//需要对角色进行选择性分配进战场
			if(campTeam == Utils.CAMP_RANDOM){
				int brightDragCount = maxBright - brightPlayerCount;
				int darkDragCount = maxDark - darkPlayerCount;
				//黑龙少人
				if(darkDragCount > 0 && darkDragCount > brightDragCount){
					campTeam = Utils.CAMP_DARK;
				}
				//元素人少
				else if(brightDragCount > 0 && brightDragCount > darkDragCount){
					campTeam = Utils.CAMP_BRIGHT;
				}
				//两队差一样的人数 随机分配队伍
				else if(brightDragCount > 0 && darkDragCount > 0){
					if(Utils.hit(50, 100)){
						campTeam = Utils.CAMP_DARK;
					}else{
						campTeam = Utils.CAMP_BRIGHT;
					}
				}else{
					if(brightDragCount > 0){
						campTeam = Utils.CAMP_BRIGHT;
					}else if(darkDragCount > 0){
						campTeam = Utils.CAMP_DARK;
					}
				}
				cbp.setCampTeam(campTeam);
			}
			instance.preAdd(new WorldPlayer[] { player }, (byte)campBattlefield.getModel(), (byte)campTeam);
			
			if (campBattlefieldService.containsDraggedPlayers(player.getId())) {
				campBattlefieldService.getDraggedPlayer().remove(player.getId());
				campBattlefieldService.removeWaitingPlayer(instance.getName(), instance.getLevelType(), player.getId(), player.getCamp());
			}
			player.setJumpMapId(map.getMapId());
            player.setJumpX(player.getX());
            player.setJumpY(player.getY());
            player.setShowCampMessage(false);
            
            //删除角色身上在战场才能获得的物品
            int itemID = instance.getDefinition().getCompetingGoodsID();
            Changed changed = new Changed();
            if(player.hasItem(itemID)){
            	int count = player.getItemCount(itemID);
            	player.completeRemoveItem(itemID, count, changed);
            }
            
            if(campTeam != Utils.NO_CAMP){
	    		player.setTitle(campTeam == Utils.CAMP_BRIGHT ? Utils.CAMP_TEAM_BRIGHT : Utils.CAMP_TEAM_DARK);
				changed.setProperty(Changed.TITLE_STRING, player.getTitle());
            }
            worldService.getConnectService().sendGetItem(changed, player.getId(), (byte)20);
            
            long now = System.currentTimeMillis();
            long min = (now - instance.getCreateTime()) / 60000;
            long allTime = (instance.getEndTime() - instance.getCreateTime()) / 60000;
            chatService.sendPrivateMessage(-1, "系统", player.getId(), "这场战斗还有" + (allTime - min) + "分钟结束!");
            
			return instance;
		}
		log.info("Battle Resources tryGotoInstance error");
		return null;
    }
    
    /**
     * 获得游戏地图
     */
	public GameMap getGameMap (WorldPlayer player, short mapId) {
		Map m = (Map)playerID_maps.get(new Integer(player.getId()));
        if (m == null) {
        	return null;
        }
        return (GameMap)m.get(new Short(mapId));
	}

	/**
	 * 进入游戏时获得玩家所在的地图
	 */
	public synchronized GameMap getLoginMap (WorldPlayer player, short mapID) {
		GameMap map = getGameMap(player, mapID);
        if (map != null) {
        	CampBattlefieldInstance instance = (CampBattlefieldInstance)map.getInstance();
            if (instance == null) {
                return map;
            } else {
            	long currentTime = System.currentTimeMillis();
            	if (instance.hasOfflinePlayer(player.getId())) {
            		instance.removeOfflinePlayer(player.getId());
            	}
            	if (instance.getBattlefieldPlayer(player.getId()) == null || !playerID_instances.containsKey(player.getId())) {
            		if (player.getJumpMapId() != 0) {
            			return worldService.getNoInstanceMap(player.getJumpMapId());
            		} else {
            			return worldService.getNoInstanceMap((short)353);
            		}
            	} else {
            		if (instance.isTimeOut(currentTime) || instance.isClosed()) {
                    	if (player.getJumpMapId() != 0) {
                            return worldService.getNoInstanceMap(player.getJumpMapId());
                        } else {
                            return worldService.getNoInstanceMap((short)353);
                        }
                    } else {
                        if (map.canAdd(player)) {
                            return map;
                        } else {
                        	if (player.getJumpMapId() != 0) {
                                return worldService.getNoInstanceMap(player.getJumpMapId());
                            } else {
                                return worldService.getNoInstanceMap((short)353);
                            }
                        }
                    }
            	}
            }
        } else {
            if (player.getJumpMapId() != 0) {
                return worldService.getNoInstanceMap(player.getJumpMapId());
            } else {
                return worldService.getNoInstanceMap((short)353);
            }
        }
	}

	/**
	 * 根据玩家，战场ID，获得战场副本
	 */
	public CampBattlefieldInstance getInstance (IPlayerData player, int ID) {
//		Team team = player.getTeam();
//        if (team != null) {
//            PositionSprite[] players = team.getPlayers();
//            for (int i = 0; i < players.length; i++) {
//                if (players[i].getId() != player.getId() && players[i].getMap() != null) {
//                    Instance instance = players[i].getMap().getInstance();
//                    if (instance != null && instance.getId() == ID) {
//                        return (CampBattlefieldInstance) instance;
//                    }
//                }
//            }
//        }
//        HashSet<CampBattlefieldInstance> set = playerID_instances.get(player.getId());
//        if (set != null) {
//            Iterator<CampBattlefieldInstance> ite = set.iterator();
//            while (ite.hasNext()) {
//            	CampBattlefieldInstance instance = ite.next();
//                if (instance.getId() == ID) {
//                	return instance;
//                }
//            }
//        }
//		return null;
		if(!playerID_instances.containsKey(player.getId())){
			return null;
		}
		return ID_instances;
	}

	/**
	 * 将玩家加入战场副本
	 */
	public void playerAddedToInstance (IPlayerData player, Instance instance) {
        GameMap[] maps = instance.getMaps();
        for (int i = 0; i < maps.length; i++) {
            addToMap(player.getId(), maps[i]);
        }
	}
	
	/**
	 * 处理逻辑
	 */
	public synchronized void process (long now) {
    	CampBattlefieldInstance instances = ID_instances;
    	if (instances != null && !instances.isClosed()) {
			if (instances.isTimeOut(now)) {
				cancel(instances, now);
				campBattlefieldService.reduceCurrentCount(instances.getType(), instances.getLevelType());
				ID_instances = null;
				return;
			}
			if (now > instances.getCreateTime() + CampBattlefieldConfig.TIME_LIMIT_START &&
					now < instances.getCreateTime() + CampBattlefieldConfig.TIME_LIMIT_PRIZES * Utils.UNIT_OF_SECOND * 50 &&
					(instances.getPlayerCountByCampType(Utils.CAMP_BRIGHT) == 0
							|| instances.getPlayerCountByCampType(Utils.CAMP_DARK) == 0)) {
				stopBattle(instances);
				int[] ids = instances.getActives();
				for (int j = 0; j < ids.length; j++) {
					CampBattlefieldPlayer bfplayer = instances.getBattlefieldPlayer(ids[j]);
					if (bfplayer != null) {
						sendRoarMessage(0, bfplayer, false, Utils.NO_CAMP);
					}
				}
				exitCampBattlefield(instances, -1, now);
				cancel(instances, now);
				campBattlefieldService.reduceCurrentCount(instances.getType(), instances.getLevelType());
				ID_instances = null;
				return;
			}
			sendNotice(instances, now);
			checkOfflinePlayer(now, instances);
			if (!instances.isClosed() && (now > instances.getCreateTime()
					+ CampBattlefieldConfig.TIME_LIMIT_PRIZES && now < instances.getCreateTime()
					+ 1000L * instances.getRefreshSecond() - CampBattlefieldConfig.TIME_LIMIT_JOIN)) {
				checkCampBattlefieldPlayer(instances);
			}
    	}
    }
	
	/**
	 * 是否战场召唤时间
	 */
	public boolean isSummon (int levelType, CampBattlefieldInstance instance) {
		int hour = instance.getHour();
		int minute = instance.getMinute();
		CampBattlefieldAward campBattlefieldAward = instance.getDefinition().getCampBattlefield().getCampBattlefieldTypeAward(levelType);
		int[] timePeriods = campBattlefieldAward.getTimePeriods();
		for (int i = 0; i < timePeriods.length; i += 4) {
			if (hour > timePeriods[i] && hour < timePeriods[i + 2]) {
				return true;
			} else if (hour == timePeriods[i] && minute >= timePeriods[i + 1]) {
				return true;
			} else if (hour == timePeriods[i + 2] && minute <= timePeriods[i + 3]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 是否超过离线时间
	 */
	public boolean moreThanOffLineTimeLimit (WorldPlayer player, long now, InstanceDefinition idf) {
		if (player.getLastlogoutTime() != null) {
			long logoutTime = player.getLastlogoutTime().getTime();
			if (now - logoutTime >= Utils.UNIT_OF_SECOND * idf.getTimeout()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * 战场获胜，发送奖励
	 */
	public void sendPrizes (CampBattlefieldInstance instance, long currentTime) {
		int[] ids = instance.getActives();
		int campType = getVictoryCampType(instance);
		if (currentTime < instance.getCreateTime() + CampBattlefieldConfig.TIME_LIMIT_PRIZES) {
			for (int i = 0; i < ids.length; i++) {
				CampBattlefieldPlayer bfplayer = instance.getBattlefieldPlayer(ids[i]);
				if (bfplayer != null) {
					sendRoarMessage(campType, bfplayer, false, bfplayer.getCampTeam() != Utils.NO_CAMP ? CampBattlefield.MODEL_CHAOS : CampBattlefield.MODEL_NORMOL);
				}
			}
			return;
		} else {
			for (int i = 0; i < ids.length; i++) {
				CampBattlefieldPlayer bfplayer = instance.getBattlefieldPlayer(ids[i]);
				if (bfplayer != null) {
					sendRoarMessage(campType, bfplayer, true, bfplayer.getCampTeam() != Utils.NO_CAMP ? CampBattlefield.MODEL_CHAOS : CampBattlefield.MODEL_NORMOL);
				}
			}
		}
		int levelType = instance.getLevelType();
		int randomRate = 0;
		int winnerPoint = 0;
		int winnerExpRate = 0;
		int loserExpRate = 0;
		int loserPoint = 0;
		int giftID = -1;
		boolean isSummon = false;
		CampBattlefieldAward campBattlefieldAward = instance.getDefinition().getCampBattlefield().getCampBattlefieldTypeAward(levelType);
		giftID = campBattlefieldAward.getGiftID();
		randomRate = campBattlefieldAward.getRate();
		if (isSummon(levelType, instance)) {
			isSummon = true;
			randomRate = campBattlefieldAward.getSummonRate();
			winnerPoint = campBattlefieldAward.getSummonWinnerPoint();
			winnerExpRate = campBattlefieldAward.getSummonWinnerExpRate();
			loserPoint = campBattlefieldAward.getSummonLoserPoint();
			loserExpRate = campBattlefieldAward.getSummonLoserExpRate();
		} else {
			winnerPoint = campBattlefieldAward.getWinnerPoint();
			winnerExpRate = campBattlefieldAward.getWinnerExpRate();
			loserPoint = campBattlefieldAward.getLoserPoint();
			loserExpRate = campBattlefieldAward.getLoserExpRate();
		}
		for (int i = 0; i < ids.length; i++) {
			int getExp = 0;
			int killPoints = 0;
			CampBattlefieldPlayer bfplayer = instance.getBattlefieldPlayer(ids[i]);
			WorldPlayer player = campBattlefieldService.getPlayerService().getWorldPlayer(ids[i]);
			if (bfplayer == null) {
				continue;
			}
			if (player != null) {
				if(player.getMap() != null && player.getMap().getInstance() != null && player.getMap().getInstance() instanceof CampBattlefieldInstance){
				}else{
					log.info("BattleForResources playerID[" + player.getId() + "] is not in battleMap");
					continue;
				}
				synchronized (player) {
					Changed changed = new Changed();
					if (player.getLevel() == 100 && giftID > -1) {
						IItem iit = Items.getTemplate(giftID).newInstance();
						IItem nItem = player.completeAddItem(iit, 1, changed, player.getClientDataVersion());
						if (nItem == null) {
							byte[] att = ItemUtils.item2dbAttachment(iit, 1);
							mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
									iit.getName() + "*" + 1, "", att, 0, true);
						}
					}
					boolean winner = true;
					if(bfplayer.getCampTeam() == Utils.NO_CAMP){
						if(player.getCamp() != campType){
							winner = false;
						}
					}else{
						if(bfplayer.getCampTeam() != campType){
							winner = false;
						}
					}
					if (winner) {
						if (bfplayer.isRandom()) {
							getExp = (Utils.getUpLevelExp(player.getLevel()) * winnerExpRate / 100) * randomRate + CampBattlefieldConfig.AWARD_BASE;
							int getPoints = winnerPoint * randomRate + CampBattlefieldConfig.AWARD_BASE;
							killPoints = player.getCampBattlefieldKillPoints() + getPoints;
							//取消满级后不给经验限制
							//if (player.getMaxLevel() > player.getLevel()) {
								player.addExp(getExp, changed);
							//}
							player.setCampBattlefieldKillingPoints(killPoints);
							changed.addProperty(Changed.KILL_POINT, getPoints);
						} else {
							getExp = Utils.getUpLevelExp(player.getLevel()) * winnerExpRate / 100;
							killPoints = player.getCampBattlefieldKillPoints() + winnerPoint;
							//if (player.getMaxLevel() > player.getLevel()) {
								player.addExp(getExp, changed);
							//}
							player.setCampBattlefieldKillingPoints(killPoints);
							changed.addProperty(Changed.KILL_POINT, winnerPoint);
						}
					} else {
						if (bfplayer.isRandom()) {
							getExp = (Utils.getUpLevelExp(player.getLevel()) * loserExpRate / 100) * randomRate + CampBattlefieldConfig.AWARD_BASE;
							int backupPoints = player.getCampBattlefieldKillPoints();
							killPoints = backupPoints - loserPoint * randomRate - CampBattlefieldConfig.AWARD_BASE;
							if (killPoints < 0) {
								killPoints = 0;
							}
							//if (player.getMaxLevel() > player.getLevel()) {
								player.addExp(getExp, changed);
							//}
							player.setCampBattlefieldKillingPoints(killPoints);
							if (killPoints <= 0) {
								changed.addProperty(Changed.KILL_POINT,  - backupPoints);
							} else {
								changed.addProperty(Changed.KILL_POINT,  - loserPoint * randomRate - CampBattlefieldConfig.AWARD_BASE);
							}
						} else {
							getExp = Utils.getUpLevelExp(player.getLevel()) * loserExpRate / 100;
							int backupPoints = player.getCampBattlefieldKillPoints();
							killPoints = backupPoints - loserPoint;
							if (killPoints < 0) {
								killPoints = 0;
							}
							//if (player.getMaxLevel() > player.getLevel()) {
								player.addExp(getExp, changed);
							//}
							player.setCampBattlefieldKillingPoints(killPoints);
							if (killPoints <= 0) {
								changed.addProperty(Changed.KILL_POINT,  - backupPoints);
							} else {
								changed.addProperty(Changed.KILL_POINT,  - loserPoint);
							}
						}
					}
					campBattlefieldService.getConnectService().sendGetItem(changed, player.getId(), (byte) 22);
				}
			} else {
				try {
					player = campBattlefieldService.getPlayerService().getWorldPlayerAndCatch(ids[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(player == null){
					log.info("CampBattle LoadWorldPlayer fail ID[" + ids[i] + "]");
				}else{
					if(player.getMap() != null && player.getMap().getInstance() != null && player.getMap().getInstance() instanceof CampBattlefieldInstance){
					}else{
						campBattlefieldService.getPlayerService().releasePlayer(player);
						log.info("BattleForResources playerID[" + player.getId() + "] is not in battleMap");
						continue;
					}
//					campBattlefieldService.getPlayerService().acquire(player);
					synchronized (player) {
						boolean winner = true;
						if(bfplayer.getCampTeam() == Utils.NO_CAMP){
							if(player.getCamp() != campType){
								winner = false;
							}
						}else{
							if(bfplayer.getCampTeam() != campType){
								winner = false;
							}
						}
						if (winner) {	// 胜利
							if (bfplayer.isRandom()) {
								getExp = (Utils.getUpLevelExp(player.getLevel()) * winnerExpRate / 100) * randomRate + CampBattlefieldConfig.AWARD_BASE;
								killPoints = player.getCampBattlefieldKillPoints() + winnerPoint * randomRate + CampBattlefieldConfig.AWARD_BASE;
								//if (player.getMaxLevel() > player.getLevel()) {
									player.addExp(getExp, null);
								//}
								player.setCampBattlefieldKillingPoints(killPoints);
							} else {
								getExp = (Utils.getUpLevelExp(player.getLevel()) * winnerExpRate / 100);
								killPoints = player.getCampBattlefieldKillPoints() + winnerPoint;
								//if (player.getMaxLevel() > player.getLevel()) {
									player.addExp(getExp, null);
								//}
								player.setCampBattlefieldKillingPoints(killPoints);
							}
						} else {	// 失败
							if (bfplayer.isRandom()) {
								getExp = (Utils.getUpLevelExp(player.getLevel()) * loserExpRate / 100) * randomRate + CampBattlefieldConfig.AWARD_BASE;
								killPoints = player.getCampBattlefieldKillPoints() - loserPoint * randomRate - CampBattlefieldConfig.AWARD_BASE;
								//if (player.getMaxLevel() > player.getLevel()) {
									player.addExp(getExp, null);
								//}
								if (killPoints < 0) {
									killPoints = 0;
								}
								player.setCampBattlefieldKillingPoints(killPoints);
							} else {
								getExp = (Utils.getUpLevelExp(player.getLevel()) * loserExpRate / 100);
								killPoints = player.getCampBattlefieldKillPoints() - loserPoint;
								//if (player.getMaxLevel() > player.getLevel()) {
									player.addExp(getExp, null);
								//}
								if (killPoints < 0) {
									killPoints = 0;
								}
								player.setCampBattlefieldKillingPoints(killPoints);
							}
						}
//						player.reset();
//						campBattlefieldService.getPlayerService().unRegistry(player);
//	                    campBattlefieldService.getPlayerService().savePlayer(player);
					}
					campBattlefieldService.getPlayerService().releasePlayer(player);
				}
			}
			String campBattleID = instance.getId() + "-" + CampBattlefieldConfig.formatter.format(instance.getCreateTime());
			try {
				campBattlefieldService.addCampBattlefieldData(instance.getType(), campBattleID, player.getId(), (byte)campType,
						killPoints, bfplayer.isRandom(), isSummon);
			} catch (DataAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获得胜利方的阵营类型
	 * @param instance
	 * @return
	 */
	public int getVictoryCampType (CampBattlefieldInstance instance) {
		int brightItemCount = instance.getBrightItemCount();
		int darkItemCount = instance.getDarkItemCount();
		if (brightItemCount > darkItemCount) {
			return Utils.CAMP_BRIGHT;
		} else if (darkItemCount > brightItemCount) {
			return Utils.CAMP_DARK;
		} else {
			return -1;
		}
	}
	
	/**
	 * 删除战场中的争夺物
	 * @param idf
	 * @param player
	 * @param changed
	 */
	public void removeBattlefieldItem (InstanceDefinition idf, WorldPlayer player, Changed changed) {
		synchronized (player) {
			int itemID = idf.getCompetingGoodsID();
			int count = player.getItemCount(itemID);
			if (count > 0) {
				IItem item = Items.getTemplate(itemID).newInstance();
				player.completeRemoveItem(item, count, changed);
			}
		}
	}
	
	/**
	 * 自动销毁
	 */
	public synchronized void cancel (CampBattlefieldInstance instance, long currentTime) {
		int id = instance.getId();
		stopBattle(instance);
		sendPrizes(instance, currentTime);
		exitCampBattlefield(instance, -1, currentTime);
		removeInstance(instance);
		campBattlefieldService.addRemoveBattlefieldID(id);
		log.info("CampBattleField cancel ID[" + id + "]");
	}
	
	/**
	 * 停止战斗
	 */
	public synchronized void stopBattle (CampBattlefieldInstance instance) {
		int[] ids = instance.getActives();
		for (int i = 0; i < ids.length; i++) {
			int playerID = ids[i];
			WorldPlayer player = campBattlefieldService.getPlayerService().getWorldPlayer(playerID);
			if (player != null) {
				if (battleService.inBattle(player)) {
					Battle2 battle = battleService.getBattleByPlayer(playerID);
					if (battle instanceof ClientBattle2) {	// 打怪
						ClientBattle2 clientBattle2 = (ClientBattle2) battleService.getBattleByPlayer(playerID);
						clientBattle2.setBattleOver();
						clientBattle2.roundEnd();
						clientBattle2.clearBourt();
					} else if (battle instanceof ResourcesBattlefieldPKBattle) {	// 宣战
						ResourcesBattlefieldPKBattle pkBattle = (ResourcesBattlefieldPKBattle) battleService.getBattleByPlayer(playerID);
						pkBattle.cancel();
					}
				}
			}
		}
	}
	
	/**
     * 删除战场
     */
	public void removeInstance (CampBattlefieldInstance instance) {
        worldService.instanceRemoved(instance);
        instanceService.instanceEmpty(instance);
        instance = null;
	}
	
	/**
	 * 玩家退出给予的惩罚
	 * @param player
	 * @param instance
	 * @param cbPlayer
	 * @param changed
	 * @param currentTime
	 */
	public void exitPenalties (WorldPlayer player, CampBattlefieldInstance instance, CampBattlefieldPlayer cbPlayer, Changed changed, long currentTime) {
		player.decCredit(player.getLevel(), changed);
		removeBattlefieldItem(instance.getDefinition(), player, changed);
		int levelType = instance.getLevelType();
		int hasPoints = player.getCampBattlefieldKillPoints();
		CampBattlefieldAward campBattlefieldAward = instance.getDefinition().getCampBattlefield().getCampBattlefieldTypeAward(levelType);
		int randomRate = campBattlefieldAward.getRate();
		int loserPoint;
		if (isSummon(levelType, instance)) {
			randomRate = campBattlefieldAward.getSummonRate();
			loserPoint = campBattlefieldAward.getSummonLoserPoint();
		} else {
			loserPoint = campBattlefieldAward.getLoserPoint();
		}
		int reducPoints = 0;
		if (cbPlayer != null && cbPlayer.isRandom()) {
			reducPoints = loserPoint * randomRate + CampBattlefieldConfig.AWARD_BASE;
		} else {
			reducPoints = loserPoint;
		}
		if (hasPoints > reducPoints) {
			player.setCampBattlefieldKillingPoints(hasPoints - reducPoints);
			if (changed != null) {
				changed.addProperty(Changed.KILL_POINT, -reducPoints);
			}
		} else {
			player.setCampBattlefieldKillingPoints(0);
			if (changed != null) {
				changed.addProperty(Changed.KILL_POINT, -hasPoints);
			}
		}
		if (changed != null) {
			campBattlefieldService.getConnectService().sendGetItem(changed, player.getId(), (byte) 22);
		}
		campBattlefieldService.putCooldown(player.getId(), currentTime + Utils.UNIT_OF_SECOND
				* instance.getDefinition().getClearance());
	}
	
	/**
	 * 玩家退出战场
	 * playerID = -1所有玩家全部退出战场。playerID > 0只有此玩家退出战场
	 */
	public synchronized void exitCampBattlefield (CampBattlefieldInstance instance, int playerID, long currentTime) {
		if (playerID > 0) {
			if (instance.activeContains(playerID)) {
				CampBattlefieldPlayer cbPlayer = instance.getBattlefieldPlayer(playerID);
				WorldPlayer player = campBattlefieldService.getPlayerService().getWorldPlayer(playerID);
				if (player != null) {
					synchronized (player) {
						if (player.getCredit() < player.getLevel()) {
							campBattlefieldService.getConnectService().sendMessage(playerID, "您的荣誉不足无法传出战场。");
							return;
						}
						Changed changed = new Changed();
						if(cbPlayer.getCampTeam() != Utils.NO_CAMP){
							player.setTitle("");
							changed.setProperty(Changed.TITLE_STRING, "");
						}
						exitPenalties(player, instance, cbPlayer, changed, currentTime);
						GameMap map = player.getMap();
						if (map != null && map.getInstance() != null && map.getInstance() == instance) {
							player.getMap().removePlayer(player, true);
							if (player.getJumpMapId() != 0) {
								GameMap toMap = worldService.getNoInstanceMap(player.getJumpMapId());
								if (toMap != null) {
									campBattlefieldService.sendGotoMap(player.getId(), player.getJumpMapId(),
											(short) (player.getJumpX() / toMap.getTileWidth()),
											(short) (player.getJumpY() / toMap.getTileHeight()));
								} else {
									campBattlefieldService.sendGotoMap(player.getId(), (short) 353, (short) 4, (short) 41);
								}
							} else {
								campBattlefieldService.sendGotoMap(player.getId(), (short) 353, (short) 4, (short) 41);
							}
						} else {
							campBattlefieldService.sendGotoMap(player.getId(), (short) 353, (short) 4, (short) 41);
						}
					}
				} else {
					try {
						player = campBattlefieldService.getPlayerService().getWorldPlayerAndCatch(playerID);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(player == null){
						log.info("CampBattle exitCampBattlefield LoadWorldPlayer fail ID[" + playerID + "]");
					}else{
						campBattlefieldService.getPlayerService().acquire(player);
		                synchronized (player) {
		                	exitPenalties(player, instance, cbPlayer, null, currentTime);
		                	if(cbPlayer.getCampTeam() != Utils.NO_CAMP){
								player.setTitle("");
							}
//		                	player.reset();
//		                    campBattlefieldService.getPlayerService().unRegistry(player);
//		                    campBattlefieldService.getPlayerService().savePlayer(player);
		                }
		                campBattlefieldService.getPlayerService().releasePlayer(player);
					}
				}
				
				instance.removeActive(playerID);
				instance.removeBattlefieldPlayer(playerID);
				playerID_instances.remove(playerID);
				int campTeam = cbPlayer.getCampTeam();
				if(campTeam == Utils.NO_CAMP && player != null){
					campTeam = player.getCamp();
				}
				if (!instance.reducePlayer(campTeam)) {
					int id = instance.getId();
					campBattlefieldService.reduceCurrentCount(instance.getType(), instance.getLevelType());
					instance.setClosed(true);
					stopBattle(instance);
					sendPrizes(instance, currentTime);
					specialCircumstances(instance, player, currentTime);
					campBattlefieldService.addRemoveBattlefieldID(id);
					log.info("CampBattleField remove camp[" + campTeam + "] NoPlayer ID[" + id + "]");
				}
			}
		} else {
			int[] ids = instance.getActives();
			for (int i = 0; i < ids.length; i++) {
				WorldPlayer player = campBattlefieldService.getPlayerService().getWorldPlayer(ids[i]);
				if (player != null) {
					CampBattlefieldPlayer cbPlayer = instance.getBattlefieldPlayer(player.getId());
					Changed changed = new Changed();
					if(cbPlayer != null && cbPlayer.getCampTeam() != Utils.NO_CAMP){
						player.setTitle("");
						changed.setProperty(Changed.TITLE_STRING, "");
					}
					removeBattlefieldItem(instance.getDefinition(), player, changed);
					campBattlefieldService.getConnectService().sendGetItem(changed, player.getId(), (byte) 22);
					GameMap map = player.getMap();
					if (map != null && map.getInstance() == instance) {
						player.getMap().removePlayer(player, true);
						if (player.getJumpMapId() != 0) {
		                    GameMap toMap = worldService.getNoInstanceMap(player.getJumpMapId());
		                    if (toMap != null) {
		                    	campBattlefieldService.sendGotoMap(player.getId(), player.getJumpMapId(),
		                                    (short) (player.getJumpX() / toMap.getTileWidth()),
		                                    (short) (player.getJumpY() / toMap.getTileHeight()));
		                    } else {
		                    	campBattlefieldService.sendGotoMap(player.getId(), (short) 353, (short) 4, (short) 41);
		                    }
		                } else {
		                	campBattlefieldService.sendGotoMap(player.getId(), (short) 353, (short) 4, (short) 41);
		                }
					} else {
						campBattlefieldService.sendGotoMap(player.getId(), (short) 353, (short) 4, (short) 41);
					}
				}
				playerID_instances.remove(ids[i]);
				instance.removeActive(ids[i]);
				instance.removeBattlefieldPlayer(ids[i]);
			}
		}
	}
	
	/**
	 * 关闭战场。
	 */
	public synchronized void shutDown () {
        cancel(ID_instances, -1);
	}
	
	/**
	 * 处理5分钟内玩家不足的特殊情况，此类情况不给任何奖励
	 * @param instance
	 * @param player
	 * @param currentTime
	 */
	protected void specialCircumstances (CampBattlefieldInstance instance, WorldPlayer player, long currentTime) {
		int[] ids = instance.getActives();
		for (int i = 0; i < ids.length; i++) {
			player = campBattlefieldService.getPlayerService().getWorldPlayer(ids[i]);
			if (player != null) {
				Changed changed = new Changed();
				removeBattlefieldItem(instance.getDefinition(), player, changed);
				campBattlefieldService.getConnectService().sendGetItem(changed, player.getId(), (byte) 22);
				GameMap map = player.getMap();
				if (map != null && map.getInstance() == instance) {
					player.getMap().removePlayer(player, true);
					if (player.getJumpMapId() != 0) {
	                    GameMap toMap = worldService.getNoInstanceMap(player.getJumpMapId());
	                    if (toMap != null) {
	                    	campBattlefieldService.sendGotoMap(player.getId(), player.getJumpMapId(),
	                                    (short) (player.getJumpX() / toMap.getTileWidth()),
	                                    (short) (player.getJumpY() / toMap.getTileHeight()));
	                    }
	                } else {
	                	campBattlefieldService.sendGotoMap(player.getId(), (short) 353, (short) 4, (short) 41);
	                }
				}
			} else {
				try {
					player = campBattlefieldService.getPlayerService().getWorldPlayerAndCatch(ids[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(player == null){
					log.info("CampBattle specialCircumstances LoadWorldPlayer fail ID[" + ids[i] + "]");
				}else{
					campBattlefieldService.getPlayerService().acquire(player);
					synchronized (player) {
						GameMap map = player.getMap();
						if (map != null && map.getInstance() == instance) {
							player.getMap().removePlayer(player, true);
						}
//						campBattlefieldService.getPlayerService().unRegistry(player);
//	                    campBattlefieldService.getPlayerService().savePlayer(player);
					}
					campBattlefieldService.getPlayerService().releasePlayer(player);
				}
			}
			playerID_instances.remove(ids[i]);
			instance.removeActive(ids[i]);
			instance.removeBattlefieldPlayer(ids[i]);
		}
	}
	
	/**
	 * 加入战场副本
	 * @param players
	 * @param instance
	 */
	protected void addToInstances (CampBattlefieldPlayer[] players) {
		for (int i = 0; i < players.length; i++) {
			int playerID = players[i].getPlayerID();
			playerID_instances.put(playerID, null);
		}
    }
	
	/**
	 * 创建战场
	 * @param instanceID
	 * @param name
	 * @param type
	 * @param levelType
	 * @param players
	 * @return
	 */
	protected CampBattlefieldInstance createInstance (int instanceID, String name, String type, int levelType, CampBattlefieldPlayer[] players) {
    	InstanceDefinition idf = instanceService.getInstanceDefinition(instanceID);
    	CampBattlefieldInstance instance = createInstance(idf, name, type, levelType, players);
    	addToInstances(players);
    	campBattlefieldService.addCurrentCount(type, levelType, 1);
    	return instance;
    }
    
	/**
	 * 创建战场
	 * @param idf
	 * @param name
	 * @param type
	 * @param levelType
	 * @param players
	 * @return
	 */
    protected CampBattlefieldInstance createInstance (InstanceDefinition idf, String name, String type, int levelType, CampBattlefieldPlayer[] players) {
        GameMap entrance = worldService.getNoInstanceMap(idf.getEntrance());
        CampBattlefieldInstance ret = new CampBattlefieldInstance(InstanceService.getNewInstanceId(), name, type, levelType, idf, instanceService);
        idf.setModel(this);
        ret.setBattlefieldPlayer(players);
        ret.setEntrance(entrance);
        short[] maps = idf.getMaps();
        for (int j = 0; j < maps.length; j++) {
            Scene scene = worldService.getInstanceScene(maps[j]);
            GameMap map = new GameMap(worldService, scene, (short)0, (short)0);
            map.setCanCreateTeam(false);
            map.setCanPk(false);
            ret.addMap(map);
            map.setInstance(ret);
        }
        ID_instances = ret;
        worldService.instanceCreated(ret);
        instanceService.addInstance(ret);
        return ret;
    }
	
	protected void addToMap (int playerId, GameMap gameMap) {
        Map<Short, GameMap> m = (Map) playerID_maps.get(new Integer(playerId));
        if (m == null) {
            m = new HashMap<Short, GameMap>();
            playerID_maps.put(new Integer(playerId), m);
        }
        m.put(new Short(gameMap.getMapId()), gameMap);
    }
	
	/**
	 * 删除一个资源争夺战的PK战斗。
	 * @param battle
	 */
	public synchronized void battleEnded (ResourcesBattlefieldPKBattle battle) {
		battles.remove(battle);
	}
	
	/**
	 * 添加一个资源争夺战的PK战斗。
	 * @param battle
	 */
	public synchronized void addBattle (ResourcesBattlefieldPKBattle battle) {
		battles.add(battle);
	}
	
	/**
	 * 发送战场狮子吼
	 * @param campType
	 * @param player
	 * @param isComplete
	 */
	public void sendRoarMessage (int campType, CampBattlefieldPlayer player, boolean isComplete, int model) {
		if (isComplete) {
			if (campType == Utils.CAMP_BRIGHT) {
				chatService.sendPrivateRoarMessage(-1, "狮子吼", "战场结束了~" + Utils.CAMP_TEAM_BRIGHT + "上缴的资源比" + Utils.CAMP_TEAM_DARK + "上缴的资源多！" +
						Utils.CAMP_TEAM_BRIGHT + "的玩家获得了战场的胜利！勇士的荣耀属于他们！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD,
						(short)0, player.getPlayerID(), "系统");
			} else if (campType == Utils.CAMP_DARK) {
				chatService.sendPrivateRoarMessage(-1, "狮子吼", "战场结束了~" + Utils.CAMP_TEAM_DARK + "上缴的资源比" + Utils.CAMP_TEAM_BRIGHT + "上缴的资源多！" +
						Utils.CAMP_TEAM_DARK + "的玩家获得了战场的胜利！勇士的荣耀属于他们！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD,
						(short)0, player.getPlayerID(), "系统");
			} else {
				chatService.sendPrivateRoarMessage(-1, "狮子吼", "战场结束了~双方资源相等！全都失败了！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD,
						(short)0, player.getPlayerID(), "系统");
			}
		} else {
			chatService.sendPrivateRoarMessage(-1, "狮子吼", "对方玩家不足无法完成该战场。", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD,
					(short)0, player.getPlayerID(), "系统");
		}
	}
	
	/**
	 * 发送战场定时私聊公告
	 * @param instance
	 * @param currentTime
	 */
	public void sendNotice (CampBattlefieldInstance instance, long currentTime) {
		int[] ids = instance.getActives();
		int times = instance.getMessageTimes(currentTime);
		if (times > 0) {
			for (int j = 0; j < ids.length; j++) {
				CampBattlefieldPlayer bfplayer = instance.getBattlefieldPlayer(ids[j]);
				if (bfplayer != null) {
					if (times == 2) {
						chatService.sendPrivateRoarMessage(-1, "狮子吼", "15分钟后战场就要结束了，勇士们加油搜集资源吧！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD,
								(short)0, bfplayer.getPlayerID(), "系统");
					} else if (times == 3) {
						chatService.sendPrivateRoarMessage(-1, "狮子吼", "时间已经过半，还有10分钟战场就要结束了，坚持到最后我们就是最强的！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD,
								(short)0, bfplayer.getPlayerID(), "系统");
					} else if (times == 4) {
						chatService.sendPrivateRoarMessage(-1, "狮子吼", "5分钟后战场即将结束，胜利的曙光已经照在了勇士们的脸上！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD,
								(short)0, bfplayer.getPlayerID(), "系统");
					}
				}
			}
		}
	}
	
	/**
	 * 检查在战场中离线的玩家时间，如果超过限制则从战场中踢出次玩家
	 * @param currentTime
	 * @param instance
	 */
    public synchronized void checkOfflinePlayer (long currentTime, CampBattlefieldInstance instance) {
    	Iterator<OfflinePlayer> ite = instance.getOfflinePlayers();
    	while (ite.hasNext()) {
    		OfflinePlayer offlinePlayer = (OfflinePlayer) ite.next();
    		if (currentTime - offlinePlayer.getOfflineTime() >= Utils.UNIT_OF_SECOND * instance.getDefinition().getTimeout()) {
//    			boolean needSave = false;
//    			WorldPlayer player = campBattlefieldService.getPlayerService().getWorldPlayer(offlinePlayer.getPlayerID());
//				try {
//					if (player == null) {
//						player = campBattlefieldService.getPlayerService().loadWorldPlayer(offlinePlayer.getPlayerID());
//						needSave = true;
//					}
//				} catch (Exception e) {
//				}
    			WorldPlayer player = campBattlefieldService.getPlayerService().getWorldPlayerAndCatch(offlinePlayer.getPlayerID());
				if(player == null){
					log.info("CampBattle checkOfflinePlayer LoadWorldPlayer fail ID[" + offlinePlayer.getPlayerID() + "]");
					CampBattlefieldPlayer cbPlayer = instance.getBattlefieldPlayer(offlinePlayer.getPlayerID());
					if(cbPlayer != null && cbPlayer.getCampTeam() != Utils.NO_CAMP){
						instance.reducePlayer(cbPlayer.getCampTeam());
					}
				}else{
					synchronized (player) {
						CampBattlefieldPlayer cbPlayer = instance.getBattlefieldPlayer(offlinePlayer.getPlayerID());
//						if (needSave) {
						if(!player.online()){
							exitPenalties(player, instance, cbPlayer, null, currentTime);
//							player.reset();
//							campBattlefieldService.getPlayerService().unRegistry(player);
//							campBattlefieldService.getPlayerService().savePlayer(player);
						} else {
							Changed changed = new Changed();
							exitPenalties(player, instance, cbPlayer, changed, currentTime);
						}
						if(cbPlayer != null && cbPlayer.getCampTeam() != Utils.NO_CAMP){
							instance.reducePlayer(cbPlayer.getCampTeam());
						}else{
							instance.reducePlayer(player.getCamp());
						}
					}
				}
				campBattlefieldService.getPlayerService().releasePlayer(player);
    			instance.removeBattlefieldPlayer(offlinePlayer.getPlayerID());
    			instance.removeActive(offlinePlayer.getPlayerID());
    			playerID_instances.remove(offlinePlayer.getPlayerID());
    			ite.remove();
    		}
    	}
	}
    
    /**
     * 根据阵营获得战场中的玩家个数
     * @param instance
     * @param campType
     * @return
     */
    public int getCampBattlefieldPlayer (CampBattlefieldInstance instance, int campType) {
    	Map<Integer, Integer> campType_PlayerCount = instance.getPlayerCountMap();
    	if (campType_PlayerCount.get(campType) == null) {
			return 0;
		}
		return campType_PlayerCount.get(campType).intValue();
    }
    
    /**
     * 检查战场中的玩家个数如果不足，则添加玩家进入战场
     * @param instance
     */
    public synchronized void checkCampBattlefieldPlayer (CampBattlefieldInstance instance) {
    	int brightPlayerCount = getCampBattlefieldPlayer(instance, Utils.CAMP_BRIGHT);
    	int darkPlayerCount = getCampBattlefieldPlayer(instance, Utils.CAMP_DARK);
    	int maxDark = CampBattlefieldConfig.battlefields.get(instance.getName()).getCampbattlefieldWarrior(instance.getLevelType()).getDarkplayers();
		int maxBright = CampBattlefieldConfig.battlefields.get(instance.getName()).getCampbattlefieldWarrior(instance.getLevelType()).getBrightplayers();
		
		int brightDragCount = maxBright - brightPlayerCount;
		int darkDragCount = maxDark - darkPlayerCount;
		
		if(brightDragCount > 0 || darkDragCount > 0){
			CampBattlefield campBattlefield = CampBattlefieldConfig.battlefields.get(instance.getName());
			if(campBattlefield == null){
				return;
			}
			if(campBattlefield.getModel() == CampBattlefield.MODEL_NORMOL){
		    	if (brightPlayerCount < maxBright) {
		    		addPlayerToBattlefield(instance, maxBright - brightPlayerCount,
		    				campBattlefieldService.getWaitingBrightPlayer(instance.getName()), campBattlefieldService.getDraggedPlayer(),
		    				Utils.NO_CAMP);
		    	}
		    	if (darkPlayerCount < maxDark) {
		    		addPlayerToBattlefield(instance, maxDark - darkPlayerCount,
		    				campBattlefieldService.getWaitingDarkPlayer(instance.getName()), campBattlefieldService.getDraggedPlayer(),
		    				Utils.NO_CAMP);
		    	}
			}else if(campBattlefield.getModel() == CampBattlefield.MODEL_CHAOS){
				int needCount = brightDragCount + darkDragCount;
				ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>> waitingPlayer = campBattlefieldService.getWaitingBrightPlayer(instance.getName());
				int waitingPlayerCount = campBattlefieldService.processWaitingPlayer(instance.getLevelType(), campBattlefieldService.getPlayerService(),
						-1, waitingPlayer.get(instance.getLevelType()));
				if(waitingPlayerCount > 0){
					addPlayerToBattlefield(instance, waitingPlayerCount,
						waitingPlayer, campBattlefieldService.getDraggedPlayer(),
						Utils.CAMP_RANDOM);
				}
			}
		}
    }
    
    /**
     * 战场开始后，在缺少玩家的情况下添加玩家
     * @param instance
     * @param needCount
     * @param waitingPlayer
     * @param draggedPlayers
     */
    public synchronized void addPlayerToBattlefield (CampBattlefieldInstance instance, int needCount,
    		ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>> waitingPlayer,
    			LinkedHashMap<Integer, CampBattlefieldPlayer> draggedPlayers,
    			int campTeam) {
    	int levelType = instance.getLevelType();
		if (waitingPlayer != null && waitingPlayer.get(levelType) != null
				&& waitingPlayer.get(levelType).size() > 0) {
			campBattlefieldService.processWaitingPlayer(levelType, campBattlefieldService.getPlayerService(),
															needCount, waitingPlayer.get(levelType));
			List<CampBattlefieldPlayer> playerList = campBattlefieldService.getOnlinePlayer(levelType, needCount,
																							waitingPlayer.get(levelType));
			if (playerList != null && playerList.size() > 0) {
				CampBattlefieldPlayer[] players = new CampBattlefieldPlayer[playerList.size()];
				playerList.toArray(players);
				for (int i = 0; i < players.length; i++) {
					CampBattlefieldPlayer tmp = playerList.get(i);
					tmp.setCampTeam(campTeam);
					log.info("draggedPlayer campTeam[" + campTeam + "]");
					players[i] = tmp;
					waitingPlayer.get(levelType).put(tmp.getPlayerID(), null);
					draggedPlayers.put(tmp.getPlayerID(), tmp);
				}
				instance.setBattlefieldPlayer(players);
				addToInstances(players);
				campBattlefieldService.sendToBattlefield(players, instance.getId(), instance.getName());
			}
		}
    }
}
