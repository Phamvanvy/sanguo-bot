package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.nation.NationService;
import peony.game.nation.NationSneakBattleFieldInstance;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 战功雨扩展 当国战、司隶战役、军团战结束后，获胜的一方所属国将自动开启60分钟的战功雨。
 * 
 * @author mfou
 * 
 */
public class WeatherActivity2 implements IActivityImpl, ServiceEventListener,
		Runnable {

	protected Activity activity;
	private int[] battleType = { 0, 1, 2 }; // 0为国战，1为战场，2为军团战
	protected Map<Integer, WeatherCreditConfig> configs = new HashMap<Integer, WeatherCreditConfig>();
	protected Map<Integer, List<GameObject>> players = new HashMap<Integer, List<GameObject>>();
	boolean HAVE_SNEAK_BATTLE = false; // 国战后是否有反击战
	private int NATION_BATTLE_WIN_FACTION = -1;  //国战胜利的时间
	private int count = 0;
	private Date CHECK_SNEAKBATTLE_TIME;    //开始检测是否有反击战的时间
	private long SNEAKBATTLE_END_TIME = 0;  //反击战结束的时间

	public WeatherActivity2(Activity owner) {

		this.activity = owner;
	}

	public Activity getActivity() {

		return activity;
	}

	public void clear() {

	}

	public void load() {
		String config = activity.configData;
		parseConfig(config);
	}

	public void parse() {

	}

	public void save() {

	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		new Thread(this).start();
	}

	public void processBattleWin(int playerId, int type) {
		Player p = ObjectAccessor.getPlayer(playerId);
		if (p != null) {
			if (type == 0) { // 国战胜利
				WeatherCreditConfig ws = configs.get(0);
				if (ws != null) {
					NATION_BATTLE_WIN_FACTION = p.faction;
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY, 21);
					cal.set(Calendar.MINUTE, 32);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
//					cal.add(Calendar.MINUTE, 6);
					CHECK_SNEAKBATTLE_TIME = cal.getTime();
				}
			}
		}
	}

	public void parseConfig(String config) {
		String[] strss = config.split(";");
		for (int i = 0; i < strss.length; i += 4) {
			int nationType = 0;
			int credit = 0;
			long lastTime = 0L;
			String[] strs = { strss[i], strss[i + 1], strss[i + 2],
					strss[i + 3] };
			List<Integer> list = new ArrayList<Integer>();
			for (String ss : strs) {
				String[] strrs = ss.split(":");
				if (strrs[0].equals("type")) {
					nationType = Integer.parseInt(strrs[1]);
				} else if (strrs[0].equals("credit")) {
					credit = Integer.parseInt(strrs[1]);
				} else if (strrs[0].equals("lasttime")) {
					lastTime = Long.parseLong(strrs[1]);
				} else if (strrs[0].equals("mapIds")) {
					String[] str = strrs[1].split(",");
					for (String s : str) {
						list.add(Integer.parseInt(s));
					}
				}
			}
			WeatherCreditConfig weatherConfig = new WeatherCreditConfig(
					nationType, credit, lastTime);
			weatherConfig.mapIds = list;
			configs.put(nationType, weatherConfig);
			List<GameObject> playerList = new ArrayList<GameObject>();
			players.put(nationType, playerList);
		}
	}

	public void processPlayerAddMap(VMap map, Player p) {
		if (p != null) {
			Set<Integer> keys = configs.keySet();
			if (keys != null && keys.size() > 0) {
				for (Integer k : keys) {
					WeatherCreditConfig wc = configs.get(k);
					List<Integer> mapList = wc.mapIds;
					if (mapList != null && mapList.size() > 0) {
						if (wc.isRaining && isMapIn(map.getId(), mapList)) {
							if (k == 0
									&& p.faction != NATION_BATTLE_WIN_FACTION)
								continue;
							List<GameObject> list = players.get(k);
							list.add(p);
						}
					}
				}
			}
		}
	}

	public void processPlayerRemoveMap(VMap map, Player p) {
		if (p != null) {
			Set<Integer> keys = players.keySet();
			if (keys != null && keys.size() > 0) {
				for (Integer k : keys) {
					WeatherCreditConfig wc = configs.get(k);
					List<GameObject> player = players.get(k);
					List<Integer> mapList = wc.mapIds;
					if (player != null && mapList != null && mapList.size() > 0) {
						if (wc.isRaining && isMapIn(map.getId(), mapList)) {
							Iterator<GameObject> it = player.iterator();
							while (it.hasNext()) {
								if (it.next().id == p.id) {
									it.remove();
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void processSneakBattleEnd(int sourceFaction){
		if(sourceFaction == NATION_BATTLE_WIN_FACTION){
			SNEAKBATTLE_END_TIME = System.currentTimeMillis();
		}
	}
	
	/**
	 * 战功雨开始后添加相关地图上的玩家
	 * @param battleType
	 * @param faction
	 * @param ws
	 */
	public void addPlayerInMap(int battleType,WeatherCreditConfig ws){
		if(ws.mapIds!=null && ws.mapIds.size()>0){
			for(Integer mapId : ws.mapIds){
				VMap map = ((NoInstanceVMapManager)Server.server.getWorld().getVMapManager(mapId)).getVMaps(mapId)[0];
				Iterator<GameObject> it = map.instanceid2objects.values().iterator();
				while (it.hasNext()) {
					GameObject o = it.next();
					if (o.type == GameObject.TYPE_PLAYER) {
						if(battleType == 0){ //如果是国战战功雨，只添加胜利国家的玩家
							if(o.faction != NATION_BATTLE_WIN_FACTION ){
								continue;
							}
						}
						List<GameObject> player = players.get(battleType);
						player.add(o);
					}
				}
			}
		}
	}

	public boolean isMapIn(int mapId, List<Integer> mapList) {
		for (Integer l : mapList) {
			if (l == mapId)
				return true;
		}
		return false;
	}

	public int[] getEventTypes() {

		return new int[] { ServiceEvent.EVENT_BATTLE_WIN,
				ServiceEvent.EVENT_MAP_PLAYER_ADDED,
				ServiceEvent.EVENT_MAP_PLAYER_REMOVED,
				ServiceEvent.EVENT_SNEAKBATTLE_END };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_BATTLE_WIN:
			processBattleWin((Integer) event.param1, (Integer) event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerAddMap((VMap) event.param1, (Player) event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_REMOVED:
			processPlayerRemoveMap((VMap) event.param1, (Player) event.param2);
			break;
		case ServiceEvent.EVENT_SNEAKBATTLE_END:
			processSneakBattleEnd((Integer)event.param1);
			break;
		}
	}

	public String getPropertyByType(int type) {
		return "WEATHERCREDITTIME" + type;
	}

	/**
	 * 判断战功雨是否到期
	 * 
	 * @param nationType
	 * @return
	 */
	public boolean checkDue(int nationType) {
		WeatherCreditConfig ws = configs.get(nationType);
		if (ws != null) {
			if (ws.startTime != 0
					&& System.currentTimeMillis() - ws.startTime >= ws.lastTime) {
				if (nationType == 0) {
					NATION_BATTLE_WIN_FACTION = -1;
					HAVE_SNEAK_BATTLE = false;
					count = 0;
					ws.startTime = 0L;
					SNEAKBATTLE_END_TIME = 0L;
					
				}
				return true;
			}
		}

		return false;
	}


	/**
	 * 确定战功雨开始的时间
	 * 
	 * @param nationType
	 */
	public void checkRaining(int nationType) {
		NationService nationService = Server.server.getServiceRegistry()
				.getNationService();
		if (nationType == 0) {
			WeatherCreditConfig ws = configs.get(nationType);
			if (ws != null) {
				if (NATION_BATTLE_WIN_FACTION != -1 && count == 0) {
					if (System.currentTimeMillis() >= CHECK_SNEAKBATTLE_TIME.getTime()) {
						if (nationService.sneakInstances != null
								&& nationService.sneakInstances.size() > 0) {
							for (NationSneakBattleFieldInstance instance : nationService.sneakInstances) {
								// 有反击战
								if (instance.def.sourceFaction == NATION_BATTLE_WIN_FACTION) {
									HAVE_SNEAK_BATTLE = true;
								}
							}
						}
					    
						//有反击战
					    if(HAVE_SNEAK_BATTLE){
					    	if (SNEAKBATTLE_END_TIME != 0 && System.currentTimeMillis() >= SNEAKBATTLE_END_TIME) {
								ws.startTime = System
										.currentTimeMillis() + 10 * 60 * 1000L;
								count = 1;
							}
					    }
						// 没有反击战
						if (!HAVE_SNEAK_BATTLE) {
							ws.startTime = System.currentTimeMillis() + 10 * 60 * 1000L;
							count = 1;
						}
					}
				}
			}
		}
	}

	/**
	 * 确定战功雨是否开始
	 * 
	 * @param nationType
	 * @return
	 */
	public boolean startRaining(int nationType) {
		WeatherCreditConfig ws = configs.get(nationType);
		if (ws != null) {
			if (ws.startTime != 0 && System.currentTimeMillis() >= ws.startTime)
				return true;
		}
		return false;
	}
	
	public String getMapNameString(List<Integer> mapIds){
		if(mapIds != null && mapIds.size()>0){
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<mapIds.size();i++){
				VMap map = ((NoInstanceVMapManager)Server.server.getWorld().getVMapManager(mapIds.get(i))).getVMaps(mapIds.get(i))[0];
				String tempString = map.mapDef.mapInfo.name;
				String mapName = tempString.substring(0, 2);
				sb.append(mapName);
				if(i<mapIds.size()-1){
					sb.append(",");
				}
			}
			return sb.toString();
		}
		return null;
	}

	@SuppressWarnings("static-access")
	public void run() {
		while (true) {
			try {
				Thread.currentThread().sleep(10000);
			} catch (InterruptedException e) {

			}

			Set<Integer> keys = configs.keySet();
			if (keys != null && keys.size() > 0) {
				for (Integer k : keys) {
					WeatherCreditConfig ws = configs.get(k);

					// 确定各种战役战功雨开始时间
					try{
					    checkRaining(k);
				    } catch(Exception e){
				    	
				    }
				    	
					// 查看各种战役战功雨是否到期
					try{
						if (ws.isRaining && checkDue(k)) {
							ws.isRaining = false;
							List<GameObject> list = new ArrayList<GameObject>();
							players.put(k, list);
							continue;
						}
					} catch (Exception ex){
						
					}

					// 确定战役战功雨是否开始
					if (!ws.isRaining && startRaining(k)) {
						ws.isRaining = true;
						if (k == 0) {
							try{
								if (NATION_BATTLE_WIN_FACTION > 0) {
									String mapNames = getMapNameString(ws.mapIds);
									String msg = MessageFormat.format(
											peony.Messages.STRING_01660,
											GameObject.getFactionName(NATION_BATTLE_WIN_FACTION),mapNames);
									Server.server.getServiceRegistry()
											.getChatService().sendWorldMessage(msg);
									
									String msg2 = MessageFormat.format(
											peony.Messages.STRING_01661,mapNames);
									Server.server
									.getServiceRegistry()
									.getChatService()
									.sendFactionSystemMessage(NATION_BATTLE_WIN_FACTION,msg2);
								}
						    } catch (Exception e){
							   
						    }
					    }
						
						//添加相关地图上的玩家
						try{
						   addPlayerInMap(k,ws);
						} catch (Exception ee){
							
						}
					}

					if (ws.isRaining) {
						for (GameObject ref : players.get(k)) {
							if (ref == null)
								continue;
							Player p = ObjectAccessor.getPlayer(ref.id);
							if (p != null && p.isAlive() && !p.isInStep) {
								long lastGetWEATHERCREDITTIME = p.pool.getLong(
										getPropertyByType(k), 0L);
								if (System.currentTimeMillis()
										- lastGetWEATHERCREDITTIME >= 60 * 1000l) {
									PlayerTransaction tx = p
											.newTransaction("WEATHERCREDIT");
									p.addCredit(ws.credit, tx, true);
									tx.commit();
									p.pool.setLong(getPropertyByType(k), System
											.currentTimeMillis());
								}
							}
						}
					}
				}
			}
		}
	}
}

class WeatherCreditConfig {
	public int battleType;
	public int credit;
	public long lastTime;
	public boolean isRaining;
	public long startTime;
	public List<Integer> mapIds = new ArrayList<Integer>();

	public WeatherCreditConfig(int battleType, int credit, long lastTime) {
		this.battleType = battleType;
		this.credit = credit;
		this.lastTime = lastTime;
	}
}
