package com.pip.itimes.server.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.world.PositionService.GridManager;
import com.pip.itimes.server.world.PositionService.Group;
import com.pip.itimes.server.world.PositionService.Position;
import com.pip.itimes.server.world.game.GameMap;

public class PhizService implements Runnable{
	public final static byte PHIZ_TYPE_BATTLE = 0;
	public final static byte PHIZ_TYPE_PHIZTITLE = 1;
	
	public final static byte PHIZ_STATE_DEFAULT = 0;//战斗图标使用
	public final static byte PHIZ_STATE_PHIZLIST = 1;//表情称号列表
	public final static byte PHIZ_STATE_CHANGEPHIZ = 2;//更换表情称号
	
	/**
	 * 每次处理表情的个数
	 */
	public final static int PHIZ_DISPOSE_COUNT = 20;
	
	private static final Logger log = Logger.getLogger(PhizService.class);
	private ConnectService connectService;
	private PositionService positionService;
	private PlayerService playerService;
	private Map<Integer, ConcurrentHashMap<Byte, Phiz>> player2phiz = new HashMap<Integer,ConcurrentHashMap<Byte, Phiz>>();//目前没用到
	private Map<Integer, ConcurrentHashMap<Byte, Phiz>> changePlayer = new ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, Phiz>>();
	private Map<Integer, ConcurrentHashMap<Byte, Phiz>> sendFail = new ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, Phiz>>();
	
	public PhizService(){
		new Thread(this).start();
	}
	
	public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }
	
	public void setPositionService(PositionService positionService){
		this.positionService = positionService;
	}
	
	public void setPlayerService(PlayerService playerService){
		this.playerService = playerService;
	}
	
	/**
	 * 添加改变表情列表 将其放进改变列表中 该列表将会每2秒被处理一批
	 * @param player
	 * @param newPhizType
	 * @param newPhizIndex
	 */
	public void addChangePhiz(PositionSprite player, byte newPhizType, short newPhizIndex){
		if(player != null){
			Integer playerid = player.getId();
			if(changePlayer.containsKey(playerid)){
//				log.info("Change Phiz PlayerName[" + player.getPlayerName() + "]" + " PlayerID[" + playerid + "]");
				ConcurrentHashMap<Byte, Phiz> map = changePlayer.get(playerid);
				if(map.containsKey(newPhizType)){
					Phiz phiz = map.get(newPhizType);
					phiz.setType(newPhizType);
					phiz.setIndex(newPhizIndex);
				}else{
					map.put(newPhizType, new Phiz(newPhizType,newPhizIndex));
				}
			}else{
				ConcurrentHashMap<Byte, Phiz> map = new ConcurrentHashMap<Byte, Phiz>();
				map.put(newPhizType, new Phiz(newPhizType,newPhizIndex));
				changePlayer.put(playerid, map);
//				log.info("Add Phiz PlayerName[" + player.getPlayerName() + "]" + " PlayerID[" + playerid + "]");
			}
		}
	}
	
	public void addSendFailPhiz(WorldPlayer player, Phiz _phiz){
		if(player != null && _phiz != null){
			Integer playerid = player.getId();
			if(sendFail.containsKey(playerid)){
				ConcurrentHashMap<Byte, Phiz> map = sendFail.get(playerid);
				if(map.containsKey(_phiz.getType())){
					Phiz phiz = map.get(_phiz.getType());
					phiz.setType(_phiz.getType());
					phiz.setIndex(_phiz.getIndex());
				}else{
					map.put(_phiz.getType(), _phiz);
				}
			}else{
				ConcurrentHashMap<Byte, Phiz> map = new ConcurrentHashMap<Byte, Phiz>();
				map.put(_phiz.getType(), _phiz);
				sendFail.put(playerid, map);
			}
		}
	}
	
	/**
	 * 改变玩家的表情 并将改变的表情发送给其附近的玩家
	 * @param player
	 * @param newPhizType
	 * @param newPhizIndex
	 */
	public void changePhiz(WorldPlayer player, byte newPhizType, short newPhizIndex){
		if(player == null) return;
		Integer playerid = player.getId();
		if(player2phiz.containsKey(playerid)){
			ConcurrentHashMap<Byte, Phiz> map = player2phiz.get(playerid);
			if(map.containsKey(newPhizType)){
				Phiz phiz = (Phiz)map.get(newPhizType);
				if(phiz.getType() != newPhizType || phiz.getIndex() != newPhizIndex){
					phiz.setType(newPhizType);
					phiz.setIndex(newPhizIndex);
					sendPhiz(player, phiz);
				}
			}else{
				Phiz phiz = new Phiz(newPhizType, newPhizIndex);
				map.put(newPhizType, phiz);
				sendPhiz(player, phiz);
			}
		}else{
			ConcurrentHashMap<Byte, Phiz> map = new ConcurrentHashMap<Byte, Phiz>();
			Phiz phiz = new Phiz(newPhizType, newPhizIndex);
			map.put(newPhizType, phiz);
			player2phiz.put(playerid, map);
			sendPhiz(player, phiz);
		}
	}
	
	/**
	 * 将玩家的表情发送给其周围的玩家
	 * @param player
	 * @param phiz
	 */
	public void sendPhiz(WorldPlayer player, Phiz phiz){
		if(player != null && phiz != null){
			Position position = positionService.getPlayerPosition(player.getId());
			if(position != null){
				GameMap newMap = player.getMap();
				int newX = player.getX();
				int newY = player.getY();
				GridManager[] grids = positionService.getGridManagers(newMap, newX,
                        newY); //获得附近正方形内的4个gridManage
				if(grids == null){
					addSendFailPhiz(player, phiz);
					return;
				}
                Group[] newGroups = positionService.getNewGroups(grids, player); //获得人物所在的 4个人物的group
                Group[] groups = new Group[4];
                System.arraycopy(position.groups,0,groups,0,4);
                Map map = new HashMap();
                for (int i = 0; i < groups.length; i++) {
                    if(groups[i] != null){
                        for (Iterator ite=groups[i].players.values().iterator(); ite.hasNext();) {
                            PositionSprite p = (PositionSprite) ite.next();
                            if(p!=player){
                                Object o = map.get(p);
                                if(o == null){
                                    map.put(p,phiz);
                                }
                            }
                        }
                    }
                }
                if(player.getTeam()!=null){
                	PositionSprite[] players = player.getTeam().getPlayers();
                    if(players.length>1){
                        for(int i=0;i<players.length;i++){
                            if(players[i]!=player && players[i] instanceof WorldPlayer){
                                Phiz tmp = (Phiz)map.get(new Integer(players[i].getId()));
                                if(tmp == null){
                                	map.put(players[i], phiz);
                                }
                            }
                        }
                    }
                }
                Iterator ite = map.entrySet().iterator();
                while(ite.hasNext()){
                    Map.Entry entry = (Map.Entry)ite.next();
                    PositionSprite  p = (PositionSprite)entry.getKey();
//                    log.info("Send Phiz PlayerName[" + p.getPlayerName() + "]" + " PlayerID[" + p.getId() + "] to PlayerName[" + player.getPlayerName() + "]");
                    sendPhiz(p, player,phiz.getType());
//                    log.info("Send Phiz PlayerName[" + player.getPlayerName() + "]" + " PlayerID[" + player.getId() + "] to PlayerName[" + p.getPlayerName() + "]");
                    sendPhiz(player, p,phiz.getType());
                }
			}
		}
	}
	
	/**
	 * 将src的表情发送给desc
	 * @param src
	 * @param desc
	 */
	public void sendPhiz(WorldPlayer src, WorldPlayer desc, Phiz phiz){
//		Phiz phiz = player2phiz.get(new Integer(src.getId()));
		if(phiz != null){
			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
			seg.writeShort(ClientConstants.EXTEND_FACE);
			seg.writeInt(src.getId());
			seg.writeString(src.getPlayerName());
			seg.write((byte)phiz.getType());
			seg.write(PHIZ_STATE_DEFAULT);//统一发送0
			switch(phiz.getType()){
			case PHIZ_TYPE_BATTLE:
				//seg.write(PHIZ_STATE_DEFAULT);
				seg.write(src.hasBattle() ? (byte) 1 : (byte)0);
				break;
			case PHIZ_TYPE_PHIZTITLE://更换表情称号
				//seg.write(PHIZ_STATE_CHANGEPHIZ);
				seg.writeShort(phiz.getIndex());
				break;
			}
			connectService.writeTo(seg, desc.getId());
		}
	}
	
	/**
	 * 获取指定角色的表情 目前只有战斗表情
	 * @param src
	 * @return
	 */
	public Phiz getPhiz(PositionSprite src,byte phizType){
		if(src != null){
			switch(phizType){
			case PHIZ_TYPE_BATTLE:
				return new Phiz(PHIZ_TYPE_BATTLE, PHIZ_STATE_DEFAULT);
			case PHIZ_TYPE_PHIZTITLE:
				return new Phiz(PHIZ_TYPE_PHIZTITLE,src.getPhizTitleIndex());
			}
		}
		return null;
	}
	
	public void sendPhiz(PositionSprite src, PositionSprite desc, byte phizType){
		Phiz phiz = getPhiz(src,phizType);
		if(phiz != null){
			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
			seg.writeShort(ClientConstants.EXTEND_FACE);
			seg.writeInt(src.getId());
			seg.writeString(src.getPlayerName());
			seg.write((byte)phiz.getType());
			seg.write(PHIZ_STATE_DEFAULT);//统一发送0
			switch(phiz.getType()){
			case PHIZ_TYPE_BATTLE:
				//seg.write(PHIZ_STATE_DEFAULT);
				seg.write(src.hasBattle() ? (byte) 1 : (byte)0);
				break;
			case PHIZ_TYPE_PHIZTITLE://更换表情称号
				//seg.write(PHIZ_STATE_CHANGEPHIZ);
				seg.writeShort(phiz.getIndex());
				break;
			}
			connectService.writeTo(seg, desc.getId());
		}
	}
	
	class Phiz{
		private byte type;
		private short index; //表情索引
		
		public Phiz(byte type, short index){
			this.type = type;
			this.index = index;
		}
		
		public byte getType(){
			return type;
		}
		
		public short getIndex(){
			return index;
		}
		
		public void setType(byte type){
			this.type = type;
		}
		
		public void setIndex(short index){
			this.index = index;
		}
		
	}

	public void run() {
		while(true){
			try{
				if(changePlayer.size() > 0){
					Iterator iter = changePlayer.entrySet().iterator();
					int index = 0;
					while(iter.hasNext()){
						Entry e = (Entry)iter.next();
						Integer playerid = (Integer)e.getKey();
						if(playerid >= 0){
							WorldPlayer player = playerService.getWorldPlayer(playerid);
							if(player != null){
								ConcurrentHashMap map = (ConcurrentHashMap)e.getValue();
								Iterator iterator = map.entrySet().iterator();
								while(iterator.hasNext()){
									Entry entry = (Entry)iterator.next();
									Phiz phiz = (Phiz) entry.getValue();
									sendPhiz(player, phiz);
		//							log.info("Phiz PlayerName[" + player.getPlayerName() + "]" + " PlayerID[" + playerid + "]");
									iterator.remove();
									index ++;
									if(index >= PHIZ_DISPOSE_COUNT){
										break;
									}
								}
							}
						}
						iter.remove();
					}
				}
				if(sendFail != null && sendFail.size() > 0){
					Iterator iter = sendFail.entrySet().iterator();
					while(iter.hasNext()){
						Entry e  = (Entry)iter.next();
						Integer playerid = (Integer)e.getKey();
						WorldPlayer player = playerService.getWorldPlayer(playerid);
						if(player != null){
							ConcurrentHashMap<Byte, Phiz> map = (ConcurrentHashMap)e.getValue();
							Iterator iterator = map.entrySet().iterator();
							while(iterator.hasNext()){
								Entry entry = (Entry)iterator.next();
								Phiz phiz = (Phiz)entry.getValue();
								if(phiz != null){
									addChangePhiz(player, phiz.getType(), phiz.getIndex());
								}
								iterator.remove();
							}
						}
						iter.remove();
					}
				}
			}catch(Exception e){
				log.error(e, e);
			}finally{
				try {
					Thread.sleep(2000L);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}
}
