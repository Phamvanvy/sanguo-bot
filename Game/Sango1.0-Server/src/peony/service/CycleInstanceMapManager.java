package peony.service;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.CreatureDieCallback;
import peony.game.CycleInstanceDieCallBack;
import peony.game.DieCallback;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.LogUtil;
import peony.game.MoveCallback;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.attendant.Attendant;

/**
 * 无限闯关地图管理器
 * @author dchen
 */
public class CycleInstanceMapManager implements VMapManager, Service, ServiceEventListener {

	public Map<Integer, VMap> player2maps = new HashMap<Integer, VMap>(); //玩家对应的地图信息
	protected Map<Integer, Integer> player2level = new HashMap<Integer, Integer>(); //玩家想去的副本级别
	protected Map<Integer,Map<Integer, int[]>> level2bossids = new HashMap<Integer,Map<Integer, int[]>>(); //副本级别对应的BOSS
	protected List<Integer> players = new ArrayList<Integer>(); //所有副本中的玩家
	protected DieCallback dieCallBack = new CycleInstanceDieCallBack();
	
	public static Map<Integer,Integer> mapId=new HashMap<Integer, Integer>(); //副本地图ID,当前为只有一个地图
	public static int x; //副本地图ID,当前为只有一个地图
	public static int y; //副本地图ID,当前为只有一个地图
	public static Map<Integer, int[]> out = new HashMap<Integer, int[]>(); //副本复活点
	public static int currentDay; //副本当前的日期,03:00:00到次日03:00:00为一天
	
	protected static int[] beginLevel = {1,11,21,31,41,51,61,71,81,91}; //开始级别
	protected static int[] endLevel = {10,20,30,40,50,60,70,80,90,100}; //结束级别
	public static int instanceClearHour = 3; //副本清除时
	public static int instanceClearMin = 0; //副本清除分
	public static int instanceClearNoticeHour = 2; //副本结束前提示时
	public static int instanceClearNoticeMin = 50; //副本结束前提示分
	
	public static String propertyOfCycleLevel = "propertyOfCycleLevel"; //玩家当前的闯关级别
	public static String propertyOfCycleMaxLevel = "propertyOfCycleMaxLevel"; //玩家最大的闯关级别
	public static String propertyOfCycleDay = "propertyOfCycleDay"; //玩家最后一次闯关时间
	public static String propertyOfCycleDieDay = "propertyOfCycleDieDay"; //玩家最后一次闯关失败时间
	
	private boolean overTime = true;
	private long lastUpdataTime;
	public long lastNoticeTime;
	
	private static final Logger log = Logger.getLogger(CycleInstanceMapManager.class);
	
	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("Areas/cycleinstance.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		for(int i=0;i<mapId.size();i++){
			Server.server.getWorld().registerVMapManager(mapId.get(i), this);
		}
		Server.server.getWorld().addVMapManager(this);
		Calendar calendar = Calendar.getInstance();
		if(overTime(calendar, instanceClearHour, instanceClearMin))
			currentDay = Time.day;
		else
			currentDay = Time.day - 1;
		Server.server.getEventManager().registerListener(this);
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
//		mapId = Integer.parseInt(root.attributeValue("mapId"));
		String maps=root.attributeValue("mapId");
		int[] ids=parseMapIds(maps);
		mapId.put(0, ids[0]);
		mapId.put(1, ids[1]);
		mapId.put(2, ids[2]);
		mapId.put(3, ids[3]);
		
		x = Integer.parseInt(root.attributeValue("x"));
		y = Integer.parseInt(root.attributeValue("y"));
		String weiOut = root.attributeValue("weiOut");
		String shuOut = root.attributeValue("shuOut");
		String wuOut = root.attributeValue("wuOut");
		out.put(1, parseout(weiOut));
		out.put(2, parseout(shuOut));
		out.put(3, parseout(wuOut));
		List<Element> list = root.elements("instance");
		if(list!=null && list.size()>0){
			setLevel2BossIds(list, 0);
		}
		List<Element> list1 = root.elements("instance1");
		if(list1!=null && list1.size()>0){
			setLevel2BossIds(list1, 1);
		}
		List<Element> list2 = root.elements("instance2");
		if(list2!=null && list2.size()>0){
			setLevel2BossIds(list2, 2);
		}
		List<Element> list3 = root.elements("instance3");
		if(list3!=null && list3.size()>0){
			setLevel2BossIds(list3, 3);
		}
	}
	
	private void setLevel2BossIds(List<Element> elements,int clazz){
		Map<Integer, int[]> instance0=new HashMap<Integer, int[]>();
		for(Element element : elements){
			int level = Integer.parseInt(element.attributeValue("level"));
			String boss = element.attributeValue("bossId");
			String[] b = boss.split(",");
			if(b.length>0){
				int[] bossIds = new int[b.length];
				for(int i=0;i<b.length;i++)
					bossIds[i] = Integer.parseInt(b[i]);
				instance0.put(level, bossIds);
			}
		}
		level2bossids.put(clazz, instance0);
	}
	
	
	private int[] parseMapIds(String mapIds){
		int[] arr=new int[4];
		String[] str=mapIds.split(",");
		arr[0]=Integer.parseInt(str[0]);
		arr[1]=Integer.parseInt(str[1]);
		arr[2]=Integer.parseInt(str[2]);
		arr[3]=Integer.parseInt(str[3]);
		return arr;
	}
	
	private int[] parseout(String outStr){
		int[] o = new int[3];
		String[] str = outStr.split(",");
		o[0] = Integer.parseInt(str[0]);
		o[1] = Integer.parseInt(str[1]);
		o[2] = Integer.parseInt(str[2]);
		return o;
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check) throws VMapException {
		if(player.party!=null)
			throw new VMapException(peony.Messages.STRING_01274);
		VMap map = player2maps.get(player.id);
		if(map==null){
			map = VMapUtil.create(this, Server.server.getWorld(), mapId, Server.server.revision);
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
			player2maps.put(player.id, map);
		}
		int currentLevel = player.pool.getInt(propertyOfCycleLevel, 0);
		int lastCycleDay = player.pool.getInt(propertyOfCycleDay, 0);
		if(check && lastCycleDay!=0 && lastCycleDay!=currentDay){
			int[] outs = out.get(player.faction);
			return Server.server.getWorld().addPlayerToMap(player, outs[0], outs[1], outs[2], false);
		}
		if(getWannaLevel(player.id)>0){
			currentLevel = getWannaLevel(player.id) - 1;
		}else if(check){
			currentLevel = currentLevel-1<0 ? 0 : currentLevel-1;
		}
		if(!check && !canEnterNextLevel(player, currentLevel))
			throw new VMapException(peony.Messages.STRING_01275);
		if(isEndLevel(currentLevel) && !check && lastCycleDay==currentDay){
			int[] outs = out.get(player.faction);
			return Server.server.getWorld().addPlayerToMap(player, outs[0], outs[1], outs[2], false);
		}
		if(!check)
			refreshBosses(map, currentLevel+1,player.clazz);
		player.removeFromMap();
		player.addToMap(map, x, y);
		player.pool.setInt(propertyOfCycleLevel, currentLevel+1);
		int maxLevel = player.pool.getInt(propertyOfCycleMaxLevel, 0);
		if(currentLevel+1>maxLevel)
			player.pool.setInt(propertyOfCycleMaxLevel, currentLevel+1);
		players.add(player.id);
		setWannaLevel(player.id, 0);
		player.pool.setInt(propertyOfCycleDay, currentDay);
		Server.server.getServiceRegistry().getChatService()
			.sendPrivateShout(player.id, 0xff0000, 6000, player.faction, MessageFormat.format(peony.Messages.STRING_01276, currentLevel+1));
		log.info("[CYCLEINSTANCE]"+LogUtil.getPlayerLogString(player)+"LEVEL["+(currentLevel+1)+"]CLAZZ["+player.clazz+"]");
		return map;
	}
	
	protected void refreshBosses(VMap map ,int level,int clazz){
		int[] bosses = getBoss(level,clazz);
		if(bosses!=null){
			for(int boss : bosses){
				GameMapObject gmo = GameMapObject.findByID(Server.server.getServiceRegistry().getDataService().data, boss);
				VMapUtil.addCreature(map, gmo.x, gmo.y, (GameMapNPC) gmo, true, 0, null);
			}
		}
	}
	
	protected boolean canEnterNextLevel(Player player, int currentLevel){
		if(player!=null && player.map!=null && player.map.id==mapId.get(new Integer(player.clazz))){
			int[] bossId = getBoss(currentLevel,player.clazz);
			if(bossId!=null){
				for(int boss : bossId){
					if(player.map.map!=null){
						Creature c = player.map.map.getCreatureById(boss);
						if(c!=null && c.isAlive())
							return false;
					}
				}
			}
		}
		return true;
	}
	
	protected int[] getBoss(int level,int clazz){
		return level2bossids.get(clazz).get(level);
	}
	
	public void setWannaLevel(int playerId, int level){
		player2level.put(playerId, level);
	}
	
	protected int getWannaLevel(int playerId){
		if(player2level.get(playerId)==null)
			return 0;
		return player2level.get(playerId);
	}
	
	public boolean isBeginLevel(int level){
		for(int lvl : beginLevel){
			if(lvl==level)
				return true;
		}
		return false;
	}
	
	public boolean isEndLevel(int level){
		for(int lvl : endLevel){
			if(lvl==level)
				return true;
		}
		return false;
	}

	public CreatureDieCallback creatureDieCallback() {
		return null;
	}

	public DieCallback dieCallback() {
		return dieCallBack;
	}

	public void mapChanged(GameMapDefinition mapDef) {
        for (VMap map : player2maps.values()) {
            if (map.mapDef.mapInfo.getGlobalID() == mapDef.mapInfo.getGlobalID()) {
                map.mapDef = mapDef;
                map.mapChanged();
            }
        }
	}

	public MoveCallback moveCallback() {
		return null;
	}

	public void outPrison(Player p) {
		try {
			int[] outs = out.get(p.faction);
			p.goMap(outs[0], outs[1], outs[2]);
		} catch (VMapException e) {
			e.printStackTrace();
		}
	}

	public void removeFromMap(Player player) {
		players.remove((Integer)player.id);
		if(player!=null){
			int maxLevel = player.pool.getInt(propertyOfCycleMaxLevel);
			if(maxLevel>0){
//				if(canEnterNextLevel(player, maxLevel-1) && canEnterNextLevel(player, maxLevel)){
//					
//				}else if(canEnterNextLevel(player, maxLevel-1)){
//					player.pool.setInt(propertyOfCycleMaxLevel, maxLevel-1);
//				}
				if(!canEnterNextLevel(player, maxLevel))
					if(maxLevel%10!=0){
						player.pool.setInt(propertyOfCycleMaxLevel, maxLevel-1);
					}
			}
		}
	}

	public void update(int diff) {
		try {
			synchronized (player2maps) {
				for (VMap map : player2maps.values()) {
					if (map != null)
						map.update(diff);
				}
				cycle();
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	protected void cycle(){
		if(System.currentTimeMillis()-lastUpdataTime>60000){
			Calendar cal = Calendar.getInstance();
			if(!overTime(cal, instanceClearHour, instanceClearMin))
				overTime = false;
			if(!overTime && overTime(cal, instanceClearHour, instanceClearMin)){
				overTime = true;
				currentDay++;
				clearAllPlayers();
				try {
					for(VMap map : player2maps.values()){
						if(map!=null){
							for(GameObject o : map.instanceid2objects.values()){
								if(o!=null && o.type!=GameObject.TYPE_PLAYER){
									ObjectAccessor.removeGameObject(o);
								}
							}
						}
					}
				} catch (Exception e) {
					
				}
				player2maps.clear();
			}
			if(overTime(cal, instanceClearNoticeHour, instanceClearNoticeMin) 
					&& !overTime(cal, instanceClearHour, instanceClearMin)){
				if(lastNoticeTime==0 || System.currentTimeMillis()-lastNoticeTime>5*60*1000){
					for(int playerId : players){
						Player player = ObjectAccessor.getPlayer(playerId);
						noticePlayer(player);
					}
					lastNoticeTime = System.currentTimeMillis();
				}
			}
			lastUpdataTime = System.currentTimeMillis();
		}
	}
	
	protected void noticePlayer(Player player){
		if(player!=null){
			Server.server.getServiceRegistry().getChatService()
			.sendPrivateShout(player.id, 0x0000ff, 6000, player.faction, peony.Messages.STRING_01277);
		}
	}
	
	private boolean overTime(Calendar cal, int hour, int min){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, min);
		if(cal.after(c))
			return true;
		return false;
	}
	
	public void clearAllPlayers(){
		for(int playerId : players){
			Player player = ObjectAccessor.getPlayer(playerId);
			if(player!=null && player.map!=null && player.map.getId()==mapId.get(new Integer(player.clazz))){
				try {
					int[] outs = out.get(player.faction);
					player.goMap(outs[0], outs[1], outs[2]);
				} catch (VMapException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void shutdown() {
		
	}

	public int[] getEventTypes() {
		return new int[]{ServiceEvent.EVENT_UNIT_DIE, ServiceEvent.EVENT_MAP_PLAYER_LOADED};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_UNIT_DIE:
			processUnitDie((Unit)event.param1, (Unit)event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_LOADED:
			processPlayerEnterMap((VMap)event.param1, (Player)event.param2);
			break;
		}
	}
	
	protected void processPlayerEnterMap(VMap map, Player player){
		if(map!=null && map.getId()!=mapId.get(new Integer(player.clazz)) && player!=null){
			if(player2maps.get(player.id)!=null){
				synchronized (player2maps) {
					player2maps.remove(player.id);
				}
				Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_CYCLEINSTANCE_FINISH,
						player,player.pool.getInt(propertyOfCycleLevel, 0)));
			}
		}
	}
	
	protected void processUnitDie(Unit die, Unit kill){
		if(die!=null && kill!=null && die instanceof Creature && (kill instanceof Player || kill instanceof Attendant)){
			Player killPlayer = null;
			if(kill instanceof Attendant)
				killPlayer = ((Attendant)kill).owner;
			else
				killPlayer = (Player)kill;
			if(killPlayer==null)
				return;
			final int playerId = killPlayer.id;
			if(killPlayer.map.id==mapId.get(new Integer(killPlayer.clazz))){
				int cycleLevel = killPlayer.pool.getInt(propertyOfCycleLevel, 0);
				if(cycleLevel>0 && isEndLevel(cycleLevel)){
					boolean allDie = true;
					int[] bosses = level2bossids.get(killPlayer.clazz).get(cycleLevel);
					if(bosses!=null){
						for(int bossId : bosses){
							Creature c = killPlayer.map.map.getCreatureById(bossId);
							if(c!=null && c.isAlive()){
								allDie = false;
								break;
							}
						}
					}
					if(allDie){
						Server.server.getServiceRegistry().getChatService()
							.sendPrivateShout(killPlayer.id, 0x0000ff, 6000, killPlayer.faction, peony.Messages.STRING_01278);
						Server.server.scheduExec.schedule(new Runnable(){
							public void run() {
								Player p = ObjectAccessor.getPlayer(playerId);
								if(p!=null && p.map!=null && p.map.map!=null && p.map.map.getId()==mapId.get(new Integer(p.clazz)))
									outPrison(p);
							}
						}, 10*1000, TimeUnit.MILLISECONDS);
					}
				}
			}
		}
	}

}
