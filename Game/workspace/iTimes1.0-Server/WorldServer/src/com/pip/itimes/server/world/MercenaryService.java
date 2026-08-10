package com.pip.itimes.server.world;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Mercenary;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.MercenaryDao;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.EquipmentHelper;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.PlayerDataException;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.Battle2;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.game.GameMap;

public class MercenaryService implements Runnable{
	private static final Logger log = Logger.getLogger(MercenaryService.class);
	
	private PositionService positionService;
	private PlayerService playerService;
	private TeamService teamService;
	private BattleService2 battleService;
	private ConnectService connectService;
	private MercenaryDao dao;
	
	private static SimpleDateFormat format = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 玩家携带着的佣兵
	 * KEY:佣兵ID
	 * VALUE:佣兵Player化数据 可参与Position模拟
	 */
	private ConcurrentHashMap<Integer, MercenaryPlayer> mapMercenaryPlayer = new ConcurrentHashMap<Integer, MercenaryPlayer>();
	
	/**
	 * 当前佣兵市场商店中玩家卖身表
	 * KEY:佣兵id
	 * VALUE:佣兵数据
	 */
	private ConcurrentHashMap<Integer, MercenaryShop> mapMercenaryShop = new ConcurrentHashMap<Integer, MercenaryShop>();
	
	/**
	 * KEY:Playerid
	 * Value:佣兵ID
	 */
	private ConcurrentHashMap<Integer, Integer> mapMercenaryShopPlayerid = new ConcurrentHashMap<Integer, Integer>();
	
	
	public void setPositionService(PositionService positionService){
		this.positionService = positionService;
	}
	
	public void setPlayerService(PlayerService playerService){
		this.playerService = playerService;
	}
	
	public void setTeamService(TeamService teamService){
		this.teamService = teamService;
	}
	
	public void setBattleService(BattleService2 battleService){
		this.battleService = battleService;
	}
	
	public void setConnectService(ConnectService connectService){
		this.connectService = connectService;
	}
	
	public MercenaryService(MercenaryDao dao){
		this.dao = dao;
	}
	
	public void init(){
		try {
			List<Mercenary> list = dao.getPlayerShopMercenary();
			mapMercenaryShop.clear();
			for(int i=0; i<list.size(); i++){
				Mercenary m = list.get(i);
				MercenaryShop ms = new MercenaryShop();
				ms.setMercenary(m);
				ms.setPlayerid(m.getMasterid());
				ms.setName(m.getPlayername());
				ms.setPrice(m.getPrice());
				ms.setProfession(0);
				ms.setSex(m.getSex());
				BattleSprite bs = new BattleSprite();
				bs.initBattleData(BattleSprite.TYPE_PLAYER, m.getLevel(), m.getVitality(), m.getStrength(), m.getIntelligence(), m.getAgility(), 0, 0, 0, m.getViany(), 0, 0, null,null,null,null,null);
				byte[] bytes = m.getUsedequipments();
				bs.initEquipData(getUsedEquipment(bytes));
				int[] attributes = bs.attributes;
				ms.setFire(MercenaryConstants.calcFire(attributes[BattleSprite.ATTR_HPMAX], 
						attributes[BattleSprite.ATTR_MPMAX], attributes[BattleSprite.ATTR_PMAX], 
						attributes[BattleSprite.ATTR_MMAX], bs.getShowAttribute(BattleSprite.ATTR_MHIT), 
						attributes[BattleSprite.ATTR_FLEE], attributes[BattleSprite.ATTR_PCRI], 
						attributes[BattleSprite.ATTR_MCRI], attributes[BattleSprite.ATTR_NOCRI], 
						bs.getDefence()));
				mapMercenaryShop.put(m.getId(), ms);
				mapMercenaryShopPlayerid.put(m.getMasterid(), m.getId());
			}
		} catch (Exception e) {
			log.info(e, e);
		}
	}
	
	public static IEquipment[] getUsedEquipment(byte[] bytes){
		IEquipment equipments[] = new IEquipment[9];
		if (bytes != null && bytes.length > 2) {
			try{
	            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	            DataInputStream dis = new DataInputStream(bis);
	            byte version = dis.readByte();
	            short size = dis.readShort();
	            for (int e = 0; e < size; e++) {
	                IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
	                if (equ != null){
	                	equ.setDataVersion(version);
	                	equipments[equ.getPart()] = equ;
	                }
	            }
			}catch(Exception e){
				log.info(e, e);
			}
        }
		return equipments;
	}
	
	public void start(){
		new Thread(this).start();
	}
	
	public void addMercenaryPlayer(MercenaryPlayer mp){
		if(!mapMercenaryPlayer.containsKey(mp.getId())){
			mapMercenaryPlayer.put(mp.getId(), mp);
		}
	}
	
	public void removeMercenaryPlayer(int id){
		if(mapMercenaryPlayer.containsKey(id)){
			MercenaryPlayer mp = mapMercenaryPlayer.remove(id);
			positionService.unRegistry(mp);
		}
	}
	
	public Mercenary createMercenary(WorldPlayer player, MercenaryShop ms, MercenaryData md){
		Date date = new Date();
		Mercenary m = new Mercenary();
		m.setMasterid(-1);
		m.setAccountid(-1);
		m.setBuyplayerid(player.getId());
		m.setProfession((byte)md.getProfession());
		m.setPlayername(ms.getName());
		m.setLevel(100);
		m.setSex(ms.getSex());
		m.setCamp((byte)Utils.NO_CAMP);
		m.setPrice(ms.getPrice());
		m.setCreatetime(date);
		m.setBuytime(date);
		m.setLeavetime(date);
		m.setUsetime(0);
		m.setBattletime(0);
		m.setFace(ms.getFace());
		m.setViany(0);
		m.setStrength(md.getAttrStr());
		m.setAgility(md.getAttrAgi());
		m.setVitality(md.getAttrVit());
		m.setIntelligence(md.getAttrInt());
		short[] skillid = md.getSkillID();
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream dos = new DataOutputStream(bos);
	        dos.writeShort((short)skillid.length);
	        for (int i = 0; i < skillid.length; i++) {
	            dos.writeShort(skillid[i]);
	        }
	        m.setAbilities(bos.toByteArray());
		}catch(Exception e){
			log.info(e, e);
		}
		m.setUsedequipments(new byte[0]);
		m.setState(Mercenary.STATE_SLEEP);
		m.setValid(true);
		saveMercenary(m);
		return m;
	}
	
	public void saveMercenary(Mercenary mercenary){
		try {
			dao.makePersistent(mercenary);
		} catch (Exception e) {
			log.info(e, e);
		}
	}
	
	public MercenaryPlayer createMercenaryPlayer(WorldPlayer player, Mercenary mercenary){
		if(mercenary == null) return null;
		MercenaryPlayer mp = new MercenaryPlayer();
		MercenaryShop ms = new MercenaryShop();
		ms.setMercenary(mercenary);
		ms.setProfession(mercenary.getProfession());
		ms.setName(mercenary.getPlayername());
		ms.setPlayerid(mercenary.getId());
		ms.setPrice(mercenary.getPrice());
		ms.setSex(mercenary.getSex());
		
		BattleSprite bs = new BattleSprite();
		bs.initBattleData(BattleSprite.TYPE_PLAYER, mercenary.getLevel(), mercenary.getVitality(), mercenary.getStrength(), mercenary.getIntelligence(), mercenary.getAgility(), 0, 0, 0, mercenary.getViany(), 0, 0, null,null,null,null,null);
		byte[] bytes = mercenary.getUsedequipments();
		bs.initEquipData(getUsedEquipment(bytes));
		int[] attributes = bs.attributes;
		ms.setFire(MercenaryConstants.calcFire(attributes[BattleSprite.ATTR_HPMAX], 
				attributes[BattleSprite.ATTR_MPMAX], attributes[BattleSprite.ATTR_PMAX], 
				attributes[BattleSprite.ATTR_MMAX], bs.getShowAttribute(BattleSprite.ATTR_MHIT), 
				attributes[BattleSprite.ATTR_FLEE], attributes[BattleSprite.ATTR_PCRI], 
				attributes[BattleSprite.ATTR_MCRI], attributes[BattleSprite.ATTR_NOCRI], 
				bs.getDefence()));
		
		mp.setId(mercenary.getId());
		mp.setMercenaryShop(ms);
		mp.setCamp(mercenary.getCamp());
		mp.setHp(attributes[BattleSprite.ATTR_HPMAX]);
		mp.setMp(attributes[BattleSprite.ATTR_MPMAX]);
		mp.setMaxHp(attributes[BattleSprite.ATTR_HPMAX]);
		mp.setMaxMp(attributes[BattleSprite.ATTR_MPMAX]);
		mp.setPlayerName(mercenary.getPlayername());
		mp.setClient(player.getClient());
		mp.setSex(mercenary.getSex());
		mp.setLevel(mercenary.getLevel());
		mp.setTongName("");
		mp.setTitle("");
		return mp;
	}
	
	public MercenaryShop[] getPlayerShop(){
		if(mapMercenaryShop.size() == 0){
			return new MercenaryShop[0];
		}
		MercenaryShop[] ms = new MercenaryShop[mapMercenaryShop.size()];
		mapMercenaryShop.values().toArray(ms);
		return ms;
	}
	
	public MercenaryShop getPlayerShopPlayerid(int playerid){
		if(mapMercenaryShopPlayerid.containsKey(playerid)){
			return mapMercenaryShop.get(mapMercenaryShopPlayerid.get(playerid));
		}else{
			return null;
		}
	}
	
	public MercenaryShop getPlayerShop(int id){
		if(mapMercenaryShop.containsKey(id)){
			return mapMercenaryShop.get(id);
		}else{
			return null;
		}
	}
	
	public boolean inPlayerShopPlayerid(int playerid){
		if(mapMercenaryShopPlayerid.containsKey(playerid)){
			return true;
		}
		return false;
	}
	
	public Mercenary removePlayerInShopPlayerid(int playerid){
		if(mapMercenaryShopPlayerid.containsKey(playerid)){
			return removePlayerInShop(mapMercenaryShopPlayerid.get(playerid), playerid);
		}
		return null;
	}
	
	public Mercenary removePlayerInShop(int id, int playerid){
		if(mapMercenaryShop.containsKey(id)){
			MercenaryShop ms = mapMercenaryShop.remove(id);
			mapMercenaryShopPlayerid.remove(playerid);
			return ms.getMercenary();
		}
		return null;
	}
	
	/**
	 * 添加玩家当前属性到商店中
	 * @param player
	 * @param price
	 * @return
	 */
	public int add2PlayerShop(WorldPlayer player, int price){
		if(player != null && !mapMercenaryShop.containsKey(player.getId())){
			try{
				Date date = new Date();
				Mercenary m = new Mercenary();
				player.reset();
				m.setMasterid(player.getId());
				Client client = player.getClient();
				if(client != null){
					m.setAccountid(client.accountId);
				}else{
					m.setAccountid(-1);
				}
				m.setBuyplayerid(-1);
				m.setProfession((byte)0);
				m.setPlayername(player.getPlayerName());
				m.setLevel(player.getLevel());
				m.setSex(player.getSex());
				m.setCamp(player.getCamp());
				m.setPrice(price);
				m.setCreatetime(date);
				m.setBuytime(date);
				m.setLeavetime(date);
				m.setUsetime(0);
				m.setBattletime(0);
				m.setFace(player.getFace());
				m.setViany(player.getVianyType());
				m.setStrength(player.getRealStrength());
				m.setAgility(player.getRealAgility());
				m.setVitality(player.getRealVitality());
				m.setIntelligence(player.getRealIntelligence());
				m.setAbilities(player.getPlayer().getAbilities());
				m.setUsedequipments(player.getPlayer().getUsedEquipments());
				m.setState((byte)0);
				m.setValid(true);
				
				MercenaryShop ms = new MercenaryShop();
				ms.setName(player.getPlayerName());
				ms.setPlayerid(player.getId());
				ms.setPrice(price);
				ms.setProfession(0);
				ms.setSex(player.getSex());
				ms.setMercenary(m);
				BattleSprite bs = new BattleSprite();
				bs.initBattleData(BattleSprite.TYPE_PLAYER, m.getLevel(), m.getVitality(), m.getStrength(), m.getIntelligence(), m.getAgility(), 0, 0, 0, m.getViany(), 0, 0, null,null,null,null,null);
				byte[] bytes = m.getUsedequipments();
				bs.initEquipData(getUsedEquipment(bytes));
				int[] attributes = bs.attributes;
				ms.setFire(MercenaryConstants.calcFire(attributes[BattleSprite.ATTR_HPMAX], 
						attributes[BattleSprite.ATTR_MPMAX], attributes[BattleSprite.ATTR_PMAX], 
						attributes[BattleSprite.ATTR_MMAX], bs.getShowAttribute(BattleSprite.ATTR_MHIT), 
						attributes[BattleSprite.ATTR_FLEE], attributes[BattleSprite.ATTR_PCRI], 
						attributes[BattleSprite.ATTR_MCRI], attributes[BattleSprite.ATTR_NOCRI], 
						bs.getDefence()));
				dao.makePersistent(m);
				mapMercenaryShop.put(m.getId(), ms);
				mapMercenaryShopPlayerid.put(player.getId(), m.getId());
				return m.getId();
			}catch(Exception e){
				log.info(e, e);
			}
		}
		return 0;
	}
	
	public Mercenary loadMercenary(int id){
		try{
			return dao.getMercenaryById(id);
		}catch(Exception e){
			log.info(e, e);
		}
		return null;
	}
	
	public void run(){
		final long hour4 = 4 * 60 * 60 * 1000L;
		final long hour24 = 24 * 60 * 60 * 1000L;
		final long sleep = 10000L;
		long tmpNow = new Date().getTime();
		long startDay = Utils.getTodayStart() + hour4;
		if(startDay > tmpNow){
			startDay -= hour24;
		}
		long checkTimer = System.currentTimeMillis();
		long check2Timer = checkTimer;
		while(true){
			try{
				if(mapMercenaryPlayer.size() > 0){
					long checkNow = System.currentTimeMillis();
					Iterator<MercenaryPlayer> iter = mapMercenaryPlayer.values().iterator();
					boolean oneCut = checkNow - checkTimer >= 1000L * 60;
					boolean twoCut = checkNow - check2Timer >= 1000L * 60 * 2;
					while(iter.hasNext()){
						MercenaryPlayer mp  = (MercenaryPlayer)iter.next();
						if(mp != null){
							WorldPlayer buyPlayer = playerService.getWorldPlayer(mp.getMercenaryShop().getMercenary().getBuyplayerid());
							if(buyPlayer != null){
								GameMap newmap = buyPlayer.getMap();
								GameMap oldmap = mp.getMap();
								if(oldmap == null || oldmap != newmap){
									positionService.unRegistry(mp);
									mp.setNeedRefreshPosition(true);
									mp.setMap(newmap);
								}
								if(newmap != null){
									mp.setX((short)(buyPlayer.getX() - 16));
									mp.setY((short)(buyPlayer.getY() - 8));
									positionService.positionChanged(mp, newmap, mp.getX(), mp.getY());
								}
								//同一个帐号 2分钟扣除一次统御值
								if(buyPlayer.getAccountId() == mp.getAccountId()){
									if(twoCut){
										buyPlayer.setLeaderShip(buyPlayer.getLeaderShip() - 1);
										log.info("playerID[" + buyPlayer.getId() + "] Leadership cut same accountID[" + mp.getAccountId() + "]");
									}
								}else{
									//非同一帐号 每分钟扣除一次统御值
									if(oneCut){
										buyPlayer.setLeaderShip(buyPlayer.getLeaderShip() - 1);
										log.info("playerID[" + buyPlayer.getId() + "] Leadership cut");
									}
								}
								if(buyPlayer.getLeaderShip() == 5 && !buyPlayer.showLeadershipMessage){
									buyPlayer.showLeadershipMessage = true;
									playerService.chatService.sendPrivateMessage(-1, "系统", buyPlayer.getId(), "统御力即将不足佣兵即将离开请购买统御魔能来补充你的统御力。");
								}
								if(buyPlayer.getLeaderShip() <= 0){
									buyPlayer.setLeaderShip(0);
									if(mapMercenaryPlayer.containsKey(mp.getId())){
										removeMercenaryPlayer(mp.getId());
										Mercenary m = mp.getMercenaryShop().getMercenary();
										m.setState(Mercenary.STATE_SLEEP);
										saveMercenary(m);
										Team team = buyPlayer.getTeam();
										if(team != null){
											PositionSprite[] members = team.getPlayers();
								            teamService.leaveTeam(team, mp);
								            UWAPSegment seg = new UWAPSegment(ClientConstants.
								                                              TEAM_LEAVE);
								            seg.writeInt(team.getId());
								            seg.writeInt(mp.getId());
								            seg.write((byte) 2); //离开
								            for (int i = 0; i < members.length; i++) {
								                if (members[i] != mp && members[i] instanceof WorldPlayer) {
								                    connectService.writeTo(seg, members[i].getId());
								                }
								            }
										}
									}
									log.info("playerID[" + buyPlayer.getId() + "] Leadership zero");
								}
							}else{
								positionService.unRegistry(mp);
								iter.remove();
							}
						}
					}
					if(oneCut){
						checkTimer = checkNow;
					}
					if(twoCut){
						check2Timer = checkNow;
					}
				}
				
				long now = System.currentTimeMillis();
				if(startDay + hour24 < now){
					//开始清除所有玩家身上的佣兵
					List<Mercenary> list = dao.getPlayerMercenary();
					boolean removeInDB = false;
					for(int i=0; i<list.size(); i++){
						Mercenary m = list.get(i);
						int buyplayerid = m.getBuyplayerid();
						WorldPlayer player = playerService.getWorldPlayer(buyplayerid);
						if(player != null){
							removeInDB = false;
							HashMap<Integer, MercenaryPlayer> mapmp = player.getMercenary();
							MercenaryPlayer mp = mapmp.get(m.getId());
							if(mp != null){
								player.removeMercenary(mp);
								if(mapMercenaryPlayer.containsKey(mp.getId())){
									Team team = player.getTeam();
									if(team != null){
										PositionSprite[] members = team.getPlayers();
							            teamService.leaveTeam(team, mp);
							            UWAPSegment seg = new UWAPSegment(ClientConstants.
							                                              TEAM_LEAVE);
							            seg.writeInt(team.getId());
							            seg.writeInt(mp.getId());
							            seg.write((byte) 2); //离开
							            for (int j = 0; j < members.length; j++) {
							                if (members[j] != mp && members[j] instanceof WorldPlayer) {
							                    connectService.writeTo(seg, members[j].getId());
							                }
							            }
									}
									removeMercenaryPlayer(mp.getId());
								}
							}
						}else{
							removeInDB = true;
//							player = playerService.loadWorldPlayer(m.getBuyplayerid());
							player = playerService.getWorldPlayerAndCatch(m.getBuyplayerid());
							if(player != null){
								player.removeMercenaryId(m.getId());
//								player.reset();
//								playerService.savePlayer(player);
//								playerService.unRegistry(player);
							}
							playerService.releasePlayer(player);
						}
						m.setState(Mercenary.STATE_AUTOREMOVE);
						saveMercenary(m);
						log.info("Remove Mercenary playerID[" + m.getBuyplayerid() + "] MercenaryID[" + m.getId() + "] removeInDB[" + removeInDB + "]");
					}
					
					//开始清除商店中的玩家的佣兵
					if(mapMercenaryShop.size() > 0){
						synchronized (mapMercenaryShop) {
							Iterator<MercenaryShop> iter = mapMercenaryShop.values().iterator();
							while(iter.hasNext()){
								MercenaryShop ms = iter.next();
								if(ms != null){
									Mercenary m = ms.getMercenary();
									m.setState(Mercenary.STATE_AUTOREMOVE);
									saveMercenary(m);
								}
								iter.remove();
							}
							mapMercenaryShop.clear();
							mapMercenaryShopPlayerid.clear();
						}
					}
					
					startDay += hour24;
				}
				
			}catch(Exception e){
				log.info(e, e);
			}finally{
				try{
					Thread.sleep(sleep);
				}catch (Exception e) {
				}
			}
		}
	}
}
