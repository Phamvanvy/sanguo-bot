package peony.game.instance;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.Creature;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.attendant.Attendant;
import peony.game.party.PartyMember;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.CycleInstanceMapManager;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.stat.StatService;

public class WomenDayInstanceService implements Service, ServiceEventListener {
	private static Logger log = Logger.getLogger(WomenDayInstanceService.class);

	public static int MAPID[] = { 2194, 255, 161 }; //活动场景ID，传进坐标
	public static int OUTMAP[] = { 2016, 913, 467 }; //活动结束后传出地图及坐标
	public static int MAX_NUM = 3; //副本人数限制
	public static int[] CREATUREID = { 8986624,8986625,8986626,8986627,8986628,8986629,8986630,
	                          8986631,8986632,8986633,8986634,8986635};//刷出NPCID
	public static int[][] GRID = { { 324, 161 }, { 522, 279 }, { 451, 258 }, //刷出NPC坐标
			{ 308, 223 }, { 216, 209 }, { 425, 320 }, { 315, 396 },
			{ 237, 147 }, { 200, 359 }, { 266, 358 } , { 246, 422 }, { 499, 356 }};
	public static int[] CREATURE_COUNT = { 12, 10, 8, 6 }; //刷出NPC个数(根据女性角色个数来定)
	public static int REWARD_ITEM = 4672; //奖励物品
	public static int HALF_REWARDITEM = 1311;
	public static List<Integer> freshedInstance = new ArrayList<Integer>();
	public static List<Integer> finishedInstance = new ArrayList<Integer>();
	protected long lastUpdateTime;
	protected Map<Integer, Boolean> canGifts = new HashMap<Integer, Boolean>();

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);

	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	/**
	 * 玩家报名以及确定进入副本
	 */
	public void signUp(ClientSession session, Packet packet) {
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player) session.getClient();
		if (player != null) {
			NormalVMapManager manager = (NormalVMapManager) Server.server
			.getWorld().getVMapManager(MAPID[0]);
	        NormalInstanceDefinition definition = manager.mapid2definitions.get(MAPID[0]);
			if (type == 0) {
				if (player.party == null) {
					if (player.level < definition.minLevel) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, "等级60以上才可以参加");
						return;
					}
					int times = player.getTodayInstanceTimes(definition.id);
					if (times >= definition.maxTimes) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, "每人每天最多5次可以进入该场景");
						return;
					}
					Server.server.getServiceRegistry().getNormalVMapManager()
					.clear(player.id);
				} else {
					if (player.party.members.size() > MAX_NUM) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, "最多只能3个人组队");
						return;
					}
					PartyMember source = player.party.memberInfo(player.id);
					if (!source.isLeader) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, "只有队长才可以报名");
						return;
					}
					List<String> tempLevel = new ArrayList<String>();
					List<String> tempTime = new ArrayList<String>();
					for (PartyMember pm : player.party.members) {
						Player p = pm.player;
						if (p != null) {
							if(p.getVMap().instance!=null||p.getVMap().getId() == CycleInstanceMapManager.mapId.get(new Integer(p.clazz)) || p.isInStep){
								ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, "还有队员正在忙碌其他事情，暂时不能报名");
								return;
							}
							if (p.level < definition.minLevel) {
								tempLevel.add(p.name);
							}
							int times = p.getTodayInstanceTimes(definition.id);
							if (times >= definition.maxTimes) {
								tempTime.add(p.name);
							}
						}
					}
					if (tempLevel.size() > 0) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, MessageFormat.format(
										"{0}等级不足60不能报名", tempLevel.get(0)));
						return;
					}
					if (tempTime.size() > 0) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, MessageFormat.format(
										"{0}今天已经5次进入该场景", tempTime.get(0)));
						return;
					}
					for (PartyMember pm : player.party.members) {
						Player p = pm.player;
						Server.server.getServiceRegistry().getNormalVMapManager().clear(p.id);
					}
				}
				Packet pt = new Packet(OpCode.OPENUI_SERVER);
				pt.putString("ui_npc_dialog");
				pt.putString("WOMEN_DAY_ENTER| ");
				player.send(pt);
				return;
			} else if (type == 1) {
				if (player.party == null) {
					try {
						player.goMap(MAPID[0], MAPID[1], MAPID[2]);
					} catch (VMapException e) {
						log.error(e.getMessage());
					}
				} else {
					if (player.party.members.size() > MAX_NUM) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, "最多只能3个人组队");
						return;
					}
					PartyMember source = player.party.memberInfo(player.id);
					if (!source.isLeader) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, "只有队长才可以报名");
						return;
					}
					List<String> tempLevel = new ArrayList<String>();
					List<String> tempTime = new ArrayList<String>();
					for (PartyMember pm : player.party.members) {
						Player p = pm.player;
						if (p != null) {
							if(p.getVMap().instance!=null||p.getVMap().getId() == CycleInstanceMapManager.mapId.get(new Integer(p.clazz)) || p.isInStep){
								ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, "还有队员正在忙碌其他事情，暂时不能报名");
								return;
							}
							if (p.level < definition.minLevel) {
								tempLevel.add(p.name);
							}
							int times = p.getTodayInstanceTimes(definition.id);
							if (times >= definition.maxTimes) {
								tempTime.add(p.name);
							}
						}
					}
					if (tempLevel.size() > 0) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, MessageFormat.format(
										"{0}等级不足60不能报名", tempLevel.get(0)));
						return;
					}
					if (tempTime.size() > 0) {
						ErrorHandler.sendErrorMessage(session, serial,OpCode.WOMEN_DAY_CLIENT, MessageFormat.format(
										"{0}今天已经5次进入该场景", tempTime.get(0)));
						return;
					}
					for (PartyMember pm : player.party.members) {
						Player p = pm.player;
						if (p != null) {
							try {
								p.goMap(MAPID[0], MAPID[1], MAPID[2]);
							} catch (VMapException e) {
								log.error(e.getMessage());
							}
						}
					}
				}
			}
			Packet pt = new Packet(OpCode.WOMEN_DAY_SERVER);
			pt.putInt(serial);
			player.send(pt);
		}
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_MAP_PLAYER_ADDED,
				ServiceEvent.EVENT_UNIT_DIE };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerAddMap((VMap) event.param1, (Player) event.param2);
			break;
		case ServiceEvent.EVENT_UNIT_DIE:
			processUnitDie((Unit) event.param1, (Unit) event.param2);
			break;
		}
	}
	
	public void processUnitDie(Unit u1, Unit u2) {
		if (u1.type == GameObject.TYPE_CREATURE && StatService.isInArray(CREATUREID, u1.id) != -1
				&& u2.map.map.getId() == MAPID[0] && (u2.type == GameObject.TYPE_ATTENDANT || u2.type == GameObject.TYPE_PLAYER)) {
			Player player = null;
			if (u2.type == GameObject.TYPE_ATTENDANT) {
				Attendant att = (Attendant) u2;
				if (att != null)
					player = att.owner;
			} else {
				player = (Player) u2;
			}
			if (player != null) {
				int count = 0;
				for (GameObject go : u2.map.map.instanceid2objects.values()) {
					if (go.type != GameObject.TYPE_PLAYER
							&& (StatService.isInArray(CREATUREID, go.id) != -1) ) {
						Creature c = (Creature)go;
						if(c!=null && c.isVisibleAndAlive())
						     count++;
					}
				}
				if (count == 0) {
					if(player.map.map.instance!=null)
					     finishedInstance.add(player.map.map.instance.getId());
					for (GameObject go : u2.map.map.instanceid2objects.values()) {
						if (go.type == GameObject.TYPE_PLAYER) {
							Player pl = (Player) go;
							GameItem rewardItem = ObjectAccessor.createGameItem(REWARD_ITEM);
							if(this.canGifts.get(pl.id)!=null && this.canGifts.get(pl.id).booleanValue()){
								Server.server.getServiceRegistry().getMailService().sendSystemMail(pl.id,peony.Messages.STRING_00004
										,"女人节活动奖励","恭喜您在女人节活动中表现良好，特此奖励", 0, rewardItem, 1,"WOMENDAY");
								Server.server.getServiceRegistry().getChatService().sendPrivateMessage(pl.id, "恭喜您在女人节活动中表现良好，奖励已由飞鸽发送，请查收");
								this.canGifts.remove(pl.id);
							}
							try {
								pl.goMap(OUTMAP[0], OUTMAP[1], OUTMAP[2]);
							} catch (VMapException e) {
								log.error(e.getMessage());
							}
						}
					}
				}
			}
		}else if(u1.getVMap().getId() == MAPID[0] && u1.type == GameObject.TYPE_PLAYER){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(u1.id, "面对恶人，勇气、实力、团结都不可缺！快去提升实力或者寻找队友吧！");
		}
	}

	/** 进入场景时刷新NPC */
	public void processPlayerAddMap(VMap map, Player player) {
		if (player != null && map != null && map.getId() == MAPID[0]) {
			Instance instance = player.map.map.instance;
			if (instance != null && (instance instanceof NormalInstance)) {
				NormalInstance normalInstance = (NormalInstance) instance;
				if (!freshedInstance.contains(normalInstance.id)) {
					int count = 0;
					if (player.party == null) {
						if (player.sex == 1) {
							count++;
						}
					} else {
						for (PartyMember pm : player.party.members) {
							Player p = pm.player;
							if (p != null) {
								if (p.sex == 1) {
									count++;
								}
							}
						}
					}
					int creatureCount = CREATURE_COUNT[count];
					for (int i = 0; i < creatureCount; i++) {
						int[] grid = GRID[i];
						ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
						GameMapObject gmo = GameMapObject.findByID(proj,CREATUREID[i]);
						GameObject npc0 = VMapUtil.addCreature(map, grid[0],grid[1], (GameMapNPC) gmo, true, 0, null);
					}
					freshedInstance.add(normalInstance.id);
				}
				canGifts.put(player.id, true);
			}
		}
	}
	
	public String getNames(List<String> names){
		String ret = names.get(0);
		if(names.size()>1){
			for(int i=1;i<names.size();i++){
				ret += ","+names.get(i);
			}
		}
		return ret;
	}

	public boolean checkEnter(Player player) {
		NormalVMapManager manager = (NormalVMapManager) Server.server.getWorld().getVMapManager(MAPID[0]);
		if (player.party == null || player.party.members.size() == 1) { // 如果是自己一个人，那么先看自己是否有进度，如果有用自己的，如果没有就重建一个进度
			VMap map = manager.getHistoryInstanceMap(player.ref(), MAPID[0]);
			if (map != null) {
				return true;
			}
		} else {// 如果不是队长，那么先看队长是否有进度
			VMap map = manager.getHistoryInstanceMap(player.party.leader.player.ref(), MAPID[0]);
			if (map != null) {
				return true;
			} else {// 如果队长没进度，那么寻找当前是否有在副本里的队员
				NormalInstance instance = null;
				instance = manager.getPartyInstance(player, MAPID[0]);
				if (instance != null) {
					return true;
				} else {
					map = manager.getHistoryInstanceMap(player.ref(), MAPID[0]);
					if (map != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

}