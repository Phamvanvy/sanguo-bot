package peony.service.fiveelement;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import peony.game.CombatContext;
import peony.game.Creature;
import peony.game.ErrorHandler;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.attendant.Attendant;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.instance.NormalInstance;
import peony.game.instance.NormalVMapManager;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.stat.StatService;

/**
 * 袁绍副本
 * @author mfou
 *
 */
public class FiveElementService implements Service, ServiceEventListener{
	
	public static int[] MAPS = {2080,2048,2096}; //分别为入口地图，一层地图，二层地图
	public static int[] FIVEELE_BUFFS = {528,529,530,531,532}; //按照金，木，水，火，土的顺序排列
	public int[] BOSS_LEVEL1 = {8388608,8388650,8388651,8388656,8388661};
	public String[] BOSSNAME_LEVEL1 = {"金属性","木属性","水属性","火属性","土属性"};
	public int[] BOSSBUFF_LEVEL2 = {517,518,519,520,521};
	public int[] BOSS_LEVEL2 = {8585225,8585228,8585230,8585232,8585234};//二层机关bossID
	public int[] FENSHEN_MUBOSS = {8388645,8388684,8388687,8388686,8388688,8388689,8388688,8388657,8388692,8388683,8388609,8388691,8388690,8388694,8388615,8388693,8388654,8388652,8388685};
	public int[] FENSHEN_YUANSHAO = {8585216,8585217,8585218,8585220,8585221,8585222,8585223,8585236,8585240};
	protected Map<Integer,List<Integer>> killBoss = new HashMap<Integer,List<Integer>>(); //已击杀死的一层boss的
	public Map<Integer,List<Integer>> hasBuff = new HashMap<Integer,List<Integer>>();
	public Map<Integer,Integer> lastBuff = new HashMap<Integer,Integer>();
	public Map<Integer,List<Integer>> playerFetchBuff = new HashMap<Integer,List<Integer>>();
	public int lastCheckTime = 0;
	
	public static Random rnd = new Random();

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_MAP_PLAYER_REMOVED,
				ServiceEvent.EVENT_MAP_PLAYER_ADDED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_UNIT_DIE:
			processUnitDie((Unit)event.param1, (Unit)event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_REMOVED:
			processPlayerLeaveMap((VMap)event.param1, (Player)event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerAddMap((VMap)event.param1, (Player)event.param2);
			break;
		}
	}
	
	public void processPlayerAddMap(VMap map,Player p){
		if(p!=null && map!=null){
			if(map.getId()!=MAPS[1]){
				if(p.buffs!=null){
				    Buff[] buffs = p.buffs .getBuffs();
					for(int i=0;i<buffs.length;i++){
						Buff buff = buffs[i];
						if(StatService.isInArray(FIVEELE_BUFFS, buff.getId())!=-1 
								|| (map.getId()!=MAPS[2] && StatService.isInArray(BOSSBUFF_LEVEL2, buff.getId())!=-1)){
							p.buffs.removeBuff(buff);
						}
					}
				}
				if(p.attendant!=null && p.attendant.buffs!=null){
					for(Buff b : p.attendant.buffs.getBuffs()){
						if(StatService.isInArray(BOSSBUFF_LEVEL2, b.getId())!=-1
								|| (map.getId()!=MAPS[2] && StatService.isInArray(BOSSBUFF_LEVEL2, b.getId())!=-1)){
					        p.attendant.buffs.removeBuff(b);
						}
					}
				}
			} else {
				NormalInstance instance = (NormalInstance)p.map.map.instance;
				if(instance!=null){
					List<Integer> playerList = playerFetchBuff.get(instance.id);
					if(playerList==null || !playerList.contains(p.id))
						return;
					if(lastBuff.get(instance.id)!=null && (killBoss.get(instance.id) == null ||(killBoss.get(instance.id)!=null && killBoss.get(instance.id).size()<BOSS_LEVEL1.length))){
						p.buffs.addBuff(BuffUtil.createBuff(lastBuff.get(instance.id), 1, p, p, 0));
						if(p.attendant!=null){
							p.attendant.buffs.addBuff(BuffUtil.createBuff(lastBuff.get(instance.id), 1, p.attendant, p.attendant, 0));
						}
					}
				}
			}
		}
	}
	
	public void processPlayerLeaveMap(VMap map,Player p){
		if(p!=null && map!=null && map.getId() == MAPS[2]){
			if(p.buffs!=null){
			    Buff[] buffs = p.buffs .getBuffs();
				for(int i=0;i<buffs.length;i++){
					Buff buff = buffs[i];
					if(StatService.isInArray(BOSSBUFF_LEVEL2, buff.getId())!=-1){
						p.buffs.removeBuff(buff);
					}
				}
			}
			if(p.attendant!=null && p.attendant.buffs!=null){
				for(Buff b : p.attendant.buffs.getBuffs()){
					if(StatService.isInArray(BOSSBUFF_LEVEL2, b.getId())!=-1){
				        p.attendant.buffs.removeBuff(b);
					}
				}
			}
		}
	}
	
	/**
	 * boss死亡处理
	 * @param dieUnit
	 * @param killUnit
	 */
	public void processUnitDie(Unit dieUnit,Unit killUnit){
		if(dieUnit.type == GameObject.TYPE_CREATURE && (killUnit.type == GameObject.TYPE_PLAYER || killUnit.type == GameObject.TYPE_ATTENDANT)){
			Player p = null;
			if(killUnit.type == GameObject.TYPE_ATTENDANT){
				Attendant att = (Attendant)killUnit;
				if(att!=null){
					p = att.owner;
				}
			}else{
			   p = (Player)killUnit;
			}
			if(p==null)
				return;
			if(StatService.isInArray(BOSS_LEVEL1, dieUnit.id)!=-1 || StatService.isInArray(BOSS_LEVEL2, dieUnit.id)!=-1){
				NormalInstance instance = (NormalInstance) p.map.map.instance;
				if(instance!=null){
					if(p.map.getId() == MAPS[1]){//一层boss死亡掉落buff
						List<Integer> list = killBoss.get(instance.id);
						if(list==null){
						    list = new ArrayList<Integer>();
						}
						list.add(dieUnit.id);
						killBoss.put(instance.id, list);
						int dropBuffId = getBuffDrop(FIVEELE_BUFFS,instance);
						if(dropBuffId != -1){
							for (GameObjectRef ref : instance.refs) {
								Player pl = (Player)ObjectAccessor.getPlayer(ref.id);
								if(pl!=null && pl.map!=null && pl.map.getId() == MAPS[1]){
									if(lastBuff.get(instance.id)!=null){
										Buff buff = pl.buffs.getBuffByID(lastBuff.get(instance.id));
										if(buff!=null)
										    pl.buffs.removeBuff(buff);
									}
									pl.buffs.addBuff(BuffUtil.createBuff(dropBuffId, 1, pl, pl, 0));
									Buff b = pl.buffs.getBuffByID(dropBuffId);
									int index = StatService.isInArray(FIVEELE_BUFFS, dropBuffId);
									if(index!= -1){
										 int antiIndex = antData(index);
										 String name = BOSSNAME_LEVEL1[antiIndex];
										 if(!killBoss.get(instance.id).contains(BOSS_LEVEL1[antiIndex])){
											 pl.message(-1, MessageFormat.format("获得了{0},可以击杀{1}的BOSS", b.getName(),name), -1, -1);
										     Server.server.getServiceRegistry().getChatService().sendPrivateMessage(pl.id, MessageFormat.format("获得了{0},可以击杀{1}的BOSS", b.getName(),name));
										 }									
									}
								}
							}
							lastBuff.put(instance.id, dropBuffId);
						} else {
							if(killBoss!=null && killBoss.get(instance.getId())!=null && killBoss.get(instance.getId()).size()>=BOSS_LEVEL1.length){//击杀所有一层boss
								for (GameObjectRef ref : instance.refs) {
									Player pl = (Player)ObjectAccessor.getPlayer(ref.id);
									if(pl!=null){
										pl.message(-1, "恭喜你们通关一层，请到传送使者处进入最后一层。", -1, -1);
										Server.server.getServiceRegistry().getChatService().sendPrivateMessage(ref.id, "恭喜你们通关一层，请到传送使者处进入最后一层。");
									}
								}
							}
						}
					}else if(p.map.getId() == MAPS[2]){ //处理二层击杀机关消除DEBUFF功能
						int dieIndex = StatService.isInArray(BOSS_LEVEL2, dieUnit.id);
						int helpIndex = partyData(dieIndex);
						if(dieIndex!=-1){
							int removeBuff = BOSSBUFF_LEVEL2[helpIndex];
					    	for (GameObjectRef ref : instance.refs) {
								Player pl = (Player)ObjectAccessor.getPlayer(ref.id);
								if(pl!=null){
									if(pl.buffs!=null){
										for(Buff b : pl.buffs.getBuffs()){
											if(b.getId() == removeBuff){
										        pl.buffs.removeBuff(b);
											}
								    	}
									}
									if(pl.attendant!=null && pl.attendant.buffs!=null){
										Buff b = pl.attendant.buffs.getBuffByID(removeBuff);
										if(b!=null)
											pl.attendant.buffs.removeBuff(b);
									}
								}
							}
						}
					}
				}
			} 
		}
	}

	
  /**
   * NPC功能
   * @param session
   * @param packet
   */
	public void checkAccess(ClientSession session,Packet packet){
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player)session.getClient();
		if(player != null){
			if(type == 0){//返回副本入口地图
				try {
					player.goMap(MAPS[0], 305, 207);
				} catch (VMapException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.FIVEELEMENT_TRANSFORM_CLIENT, "地图错误");
				}
			}else if(type == 1){//传送至二层地图
				if(player.map.getId() == MAPS[1]){
					NormalInstance instance = (NormalInstance) player.map.map.instance;
					if(instance!=null){
						if(killBoss.containsKey(instance.id) && killBoss.get(instance.id).size()==BOSS_LEVEL1.length){
							try {
								player.goMap(MAPS[2], 749, 296);
							} catch (VMapException e) {
								ErrorHandler.sendErrorMessage(session, serial, OpCode.FIVEELEMENT_TRANSFORM_CLIENT, "地图错误");
							}
						} else {
							player.message(-1, "请把一层BOSS全部击杀死后再进入", -1, -1);
						}
					}
			    }    
			}else if(type == 2){//NPC领取buff
				try{
					NormalInstance instance = (NormalInstance) player.map.map.instance;
					if(instance!=null){
						List<Integer> playerList = playerFetchBuff.get(instance.id);
						if(playerList == null){
						   playerList = new ArrayList<Integer>();
						}
						if(playerList.contains(player.id)){
							ErrorHandler.sendErrorMessage(session, serial, OpCode.FIVEELEMENT_TRANSFORM_CLIENT, "已经领取");
							return;
						}
						int buffIndex = 0;
						int addBuffId = 0;//要领取的buffId
					    List<Integer> buffList = hasBuff.get(instance.id);
					    if(buffList == null){
					    	buffList = new ArrayList<Integer>();
					    }
						if(lastBuff.get(instance.id)==null){
							buffIndex = rnd.nextInt(FIVEELE_BUFFS.length);
							addBuffId = FIVEELE_BUFFS[buffIndex];
							lastBuff.put(instance.id, addBuffId);
						}else{
							addBuffId = lastBuff.get(instance.id);
						    buffIndex = StatService.isInArray(FIVEELE_BUFFS, addBuffId);
						}
						buffList.add(addBuffId);
						hasBuff.put(instance.id, buffList);
						player.buffs.addBuff(BuffUtil.createBuff(addBuffId, 1, player, player, 0));
					    Buff b = player.buffs.getBuffByID(addBuffId);
						int antiIndex = antData(buffIndex);
						String name = BOSSNAME_LEVEL1[antiIndex];
						player.message(-1, MessageFormat.format("获得了{0},可以击杀{1}的BOSS", b.getName(),name), -1, -1);
				        playerList.add(player.id);
				        playerFetchBuff.put(instance.id, playerList);
					} 
				}catch(Exception e){
					
				}
			}
		}
	}
	
	/**
	 * 击杀某属性的boss后随即掉落一个buff,不重复掉落
	 * @param arr
	 * @param instance
	 * @return
	 */
	public int getBuffDrop(int[] arr,NormalInstance instance){
		List<Integer> ret = new ArrayList<Integer>();
		List<Integer> list = hasBuff.get(instance.id);
		if(list == null){
			list = new ArrayList<Integer>();
		}
		for(int i=0;i<arr.length;i++){
			if(!list.contains(arr[i])){
				ret.add(arr[i]);
			}	
		}
		if(ret!=null && ret.size()>0){
			int index = rnd.nextInt(ret.size());
			int dropId = ret.get(index);
			list.add(dropId);
			hasBuff.put(instance.id, list);
			return dropId;
		}
		return -1;
	}
	
	/**
	 * buff作用于与之相克属性的boss身上有效
	 * @param context
	 * @param buff
	 * @return
	 */
	public boolean checkDamage(CombatContext context,Buff buff){
		if(context.target.type == GameObject.TYPE_CREATURE){
			if(buff!=null){
			    int index = StatService.isInArray(FIVEELE_BUFFS, buff.getId());
				if(index!= -1){
					 int antiIndex = antData(index);
					 if(BOSS_LEVEL1[antiIndex]==context.target.id){
					     return true;
					 }
			     }
			}
		}
		return false;
	}
	
	public void update(){
		if(Time.currTime - lastCheckTime > 1000){
			lastCheckTime = Time.currTime;
			for(int i=1;i<MAPS.length;i++){
				NormalVMapManager manager = (NormalVMapManager) Server.server.getWorld().getVMapManager(MAPS[i]);
				List<VMap> maps = manager.getMaps(MAPS[i]);
				for(VMap map : maps){
					if(map!=null && map.instance!=null){
						NormalInstance instance = (NormalInstance) map.instance;
						if(instance!=null){
							processFenShen(map);	
						}
					}
				}
			}
		}
	}
	
	public void removeFiveElement(NormalInstance instance){
		try{
			for(GameObjectRef r : instance.refs){
				Player p = ObjectAccessor.getPlayer(r.id);
				if(p!=null && lastBuff.get(instance.id)!= null && p.buffs.getBuffByID(lastBuff.get(instance.id))!=null){
					p.buffs.removeBuff(lastBuff.get(instance.id));
				}
			}
			if(killBoss.containsKey(instance.id)){
			   killBoss.remove(instance.id);
			}
			if(hasBuff.containsKey(instance.id)){
			   hasBuff.remove(instance.id);
			}
			if(lastBuff.containsKey(instance.id)){
			   lastBuff.remove(instance.id);
			}
			if(playerFetchBuff.containsKey(instance.id)){
				playerFetchBuff.remove(instance.id);
			}
		}catch(Exception e){
			
		}
	}
	
	/**
	 * 副本中boss分身处理
	 * @param map
	 */
	public void processFenShen(VMap map){
		try{
			Creature c = null;
			List<Creature> list = new ArrayList<Creature>();
			List<Player> playerList = new ArrayList<Player>();
			for (GameObject go : map.instanceid2objects.values()){
				if(go.type == GameObject.TYPE_CREATURE){
					if(go.id == BOSS_LEVEL1[2] || go.id == 8585227){
						c = (Creature)go;
					}else if(StatService.isInArray(FENSHEN_MUBOSS, go.id)!=-1 || StatService.isInArray(FENSHEN_YUANSHAO, go.id)!=-1){
						list.add((Creature)go);
					}
				} else if(go.type == GameObject.TYPE_PLAYER){
					playerList.add((Player)go);
				}
			}
			
			if(c!=null && c.isVisibleAndAlive() && list!=null && list.size()>0 && playerList!=null && playerList.size()>0){
				int count = 0 ;
				for(Player p : playerList){
					if(p.inRange(c, c.chaseDistance))
						count++;
				}
				if(count == 0){
					for(Creature fen : list){
						if(fen.isVisibleAndAlive()){
							fen.clearThreats();
							fen.removeFromWorld();//玩家不在本身追击范围内时将分身刷没
						}
					}
				}
			}
		}catch(Exception e){
			
		}
	}
	
	public void removeAttendantBuff(Attendant att){
		try{
			if(att!=null){
				if(att.buffs!=null){
					Buff[] buffs = att.buffs .getBuffs();
					for(int i=0;i<buffs.length;i++){
						Buff buff = buffs[i];
						if((att.map.id != MAPS[2] && StatService.isInArray(BOSSBUFF_LEVEL2, buff.getId())!=-1)||
								(att.map.id != MAPS[1] && StatService.isInArray(FIVEELE_BUFFS, buff.getId())!=-1)){
							att.buffs.removeBuff(buff);
						}
					}
				}
			}
		}catch(Exception e){
			
		}
	}
		
	
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}
	
	/**
	 * 五行相克对应表
	 *
	 */
	public int antData(int index){
		switch(index){
		case 0:
			return 1;
		case 1:
			return 4;
		case 2:
			return 3;
		case 3:
			return 0;
		case 4:
			return 2;
		}
		return -1;
	}
	
	/**
	 * 五行相生对应表
	 */
   public int partyData(int index){
	   switch(index){
	   case 0:
		   return 2;
	   case 1:
		   return 3;
	   case 2:
		   return 1;
	   case 3:
		   return 4;
	   case 4:
		   return 0; 
	   }
	   return -1;
   }
}
