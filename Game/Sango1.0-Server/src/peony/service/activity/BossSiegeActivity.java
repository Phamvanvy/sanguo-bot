package peony.service.activity;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.CycleListener;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.attendant.Attendant;
import peony.game.nation.Nation;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.stat.StatService;
import peony.util.TimeUtil;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

/**
 * boss攻城活动 每日14：00,19:00一次进攻,3主城分别刷新boss; 3.boss击杀后场景内用户获得奖励
 */
public class BossSiegeActivity implements IActivityImpl, ServiceEventListener, CycleListener {
	
	private static Logger log = Logger.getLogger(BossSiegeActivity.class);

//	private static final int[] NPC2MAPS = {1114190, 983116, 1441865};
	private static final int[][] NPC2MAPS = {{1114190,1114221,1114222},{983150,983155,983160},{1441898,1441903,1441908}};
    private Map<Integer,List<Integer>> NPCS2MAPS;
	private static final int[] MAPIDS = {272, 240, 352};
	private static int TIME = 30*60*1000; //30分钟
	private static int[] KINGIDS = {1114223,983165,1441913}; //虚弱国公id
	private int DEPOSITEGET = 1000000; //扣除国库金钱额度
	private int BEGINHOUR1 = 14;
	private int BEGINHOUR2 = 21;

	protected Activity activity;

//	private Set<Integer> bossInstanceIds;
    private Map<Integer,List<Integer>> bossIds;  //各国已击杀boss集合

	private int lastRefreshTime = 0;

	private int lastAddDay = 0;
	
	private int lastAddHour = 0;

	private Map<Integer, Integer[]> bossDieTimes = new HashMap<Integer, Integer[]>(); 
	
//	protected int rewardItem;

	public BossSiegeActivity(Activity owner) {
		this.activity = owner;
	}

	public void clear() {

	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
//		String data = activity.configData;
//		rewardItem = Integer.parseInt(data);
	}

	public void save() {

	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
		tryRemove();
	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("bossSeigeActivity.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
		Server.server.getEventManager().registerListener(this);
		new Thread(new Runnable() {
			public void run() {
				while (activity.active && activity.schedule.in()) {
					try {
						Thread.sleep(500L);
					} catch (InterruptedException e) {
					}
					if (bossDieTimes.size() > 0) {
						checkTime();
					}

				}
			}
		}).start();
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
					Server.server.getServiceRegistry().getChatService()
					.sendFactionSystemMessage(nation.faction,
							"发现有不明军队在都城附近出现，预计5分钟后到达都城！");
				}
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGINHOUR1-1, 55), 24*60*60*1000l, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
					Server.server.getServiceRegistry().getChatService()
					.sendFactionSystemMessage(nation.faction,
							"发现有不明军队在都城附近出现，预计5分钟后到达都城！");
					
				}
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGINHOUR2-1, 55), 24*60*60*1000l, TimeUnit.MILLISECONDS);
	}
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc){
		Element root = doc.getRootElement();
		if (root != null) {
			List<Element> list = root.elements();
			NPCS2MAPS = new HashMap<Integer,List<Integer>>();
			for (Element l : list) {
				int faction = Integer.parseInt(l.attributeValue("id"));
				List<Element> npcs = l.elements("npc");
				List<Integer> npcList = new ArrayList<Integer>();
				for (Element n : npcs) {
					int npcId = Integer.parseInt(n.attributeValue("id"));
					npcList.add(npcId);
				}
				NPCS2MAPS.put(faction, npcList);
			}
		}
	}

	public boolean update(int diff) {
 		if (lastRefreshTime == 0) {
			tryAdd();
		}
			//30分钟内未杀死所有的CREATURE,消耗一定量国库资金
			if(Time.currTime - lastRefreshTime > TIME){
				if(bossIds != null){
					for(int key : bossIds.keySet()){
						Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(key);
						if(nation!=null){
							synchronized(nation){
								if(nation.money<DEPOSITEGET){
									if(nation.money>0){
									    nation.decMoney((int)nation.money);
									}
								}else{
								   nation.decMoney(DEPOSITEGET); 
								}
							}
							 Server.server.getServiceRegistry().getChatService()
								.sendFactionSystemMessage(nation.faction,
										"敌军见势不妙立刻撤军，国库损失大量资金。");
						}
					}
				}
				tryRemove();
			}
		return false;
	}

	private void tryAdd() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int day = cal.get(Calendar.DAY_OF_YEAR);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (day == lastAddDay && hour==lastAddHour)
			return;
		if (hour == BEGINHOUR1 || hour == BEGINHOUR2) {
			tryRemove();
			ProjectData pro = Server.server.getServiceRegistry().getDataService().data;
//			bossInstanceIds = new HashSet<Integer>();
			bossIds = new HashMap<Integer,List<Integer>>();
			for (int i = 0; i < NPC2MAPS.length; i++) {
				int[] NPCS = NPC2MAPS[i];
				int npcId = 0;
				for(int j=0;j<NPCS.length;j++){
				    GameMapObject gmo = GameMapObject.findByID(pro, NPCS[j]); // boss
					if (gmo != null && gmo instanceof GameMapNPC) {
						int mapId = gmo.owner.getGlobalID();
						VMapManager mgr = Server.server.getWorld().getVMapManager(mapId);
						if (mgr instanceof NoInstanceVMapManager) {
							VMap[] maps = ((NoInstanceVMapManager) mgr).getVMaps(mapId);
							GameObject npc = VMapUtil.addCreature(maps[0],(GameMapNPC) gmo, true, 0, null);
							npcId = npc.id;
						}
					}
				}
				List<Integer> list = new ArrayList<Integer>();
				bossIds.put(getFaction(npcId), list);
				Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(getFaction(npcId),"国都受到不明敌人偷袭，请速速回防！");
				
			}
			lastRefreshTime = Time.currTime;
			lastAddDay = day;
			lastAddHour = hour;
		}

	}

	private void tryRemove() {
		if (bossIds == null) {
			return;
		}
//		bossDieTimes.clear();
		if(bossIds.size()>0){
			for(int i=0;i<MAPIDS.length;i++){
				if(bossIds.containsKey(i+1)){
					VMapManager mgr = Server.server.getWorld().getVMapManager(MAPIDS[i]);
					if(mgr!=null && mgr instanceof NoInstanceVMapManager){
						VMap[] maps = ((NoInstanceVMapManager)mgr).getVMaps(MAPIDS[i]);
						for (GameObject o : maps[0].instanceid2objects.values()) {
							if(o.type==GameObject.TYPE_CREATURE){
								Creature c = (Creature)o;
								int faction = getFaction(c.id);
								if(faction!=0 && NPCS2MAPS.get(faction).contains(c.id)){
									c.clearThreats();
									c.removeFromWorld();
								}
							}
						}
					}
				}
			}
	//		for (int instanceId : bossInstanceIds) {
	//			Creature c = (Creature) ObjectAccessor.getGameObject(instanceId);
	//			if (c != null) {
	//				String format = peony.Messages.STRING_01733;
	//				String facName = getFactionName(c.id);
	//				Server.server.getServiceRegistry().getChatService()
	//						.sendFactionSystemMessage(getFaction(c.id),MessageFormat.format(format, facName, c.name));
	//				c.removeFromWorld();
	//			}
	//		}
			bossIds.clear();
			lastRefreshTime = 0;
		}
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_UNIT_DIE };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit) event.param1,(Unit) event.param2);
			break;
		}
	}

	protected void unitDie(Unit u,Unit killU) {
		if(u.type == GameObject.TYPE_CREATURE){
			if(StatService.isInArray(KINGIDS, u.id)!=-1){
				if(killU.type == GameObject.TYPE_ATTENDANT || killU.type == GameObject.TYPE_PLAYER){
					Player player = null;
					if(killU.type == GameObject.TYPE_ATTENDANT){
						Attendant att = (Attendant)killU;
						if(att!=null && att.owner!=null){
							player = att.owner;
						}
					}else{
						player = (Player)killU;
					}
					Creature c = (Creature)u;
					if(player!=null&&c!=null){
						Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.format("{0}趁{1}国都大战之际重创{2}后扬长而去。", player.name,c.getFactionName(),u.name));
					}
				}
			}
			if (bossIds == null)
				return;
			if(NPCS2MAPS==null){
				return;
			}
			for(int i : NPCS2MAPS.keySet()){
				List<Integer> bosses = NPCS2MAPS.get(i);
				List<Integer> killBossIds = bossIds.get(i);
				if (bosses.contains(u.id) && killBossIds!=null) {
					killBossIds.add(u.id);
					bossIds.put(i, killBossIds);
					if(killBossIds.size() >= bosses.size() && Time.currTime - lastRefreshTime <= TIME){
						int faction = getFaction(u.id);
						Packet pt = new Packet(OpCode.SHOUT_SERVER);
						String format = peony.Messages.STRING_01734;
						String fac = getFactionName(u.id);
						pt.putString(MessageFormat.format(format, fac));
						pt.putInt(0xFF0000);
						pt.putInt(10000);
						pt.put(1);
						for (Player p : ObjectAccessor.players.values()) {
			//				if (p.map.map.getId() == u.map.map.getId() && p.faction == faction) {
			//					if (p.level >= 45) {
			//						sendReward(p);
			//					}
			//				}
							p.send(pt);
						}
						Integer[] times = new Integer[3];
						times[0] = Time.currTime; // 死亡时间
						times[1] = Time.currTime; // 刷新经验时间
						times[2] = 0; // 添加经验次数,保证60次
						bossDieTimes.put(faction, times);
						bossIds.remove(i);
						if(bossIds.size() == 0){
							bossIds.clear();
							lastRefreshTime = 0;
						}
					}
		            break;
				}
			}
		}
	}

	/** 发放奖励 */
//	private void sendReward(Player p) {
//		PlayerTransaction tx = p.newTransaction("BOSSACT");
//		try {
//			p.bag.addGameItemComplete(ObjectAccessor.createGameItem(rewardItem), 1, tx, true);
//			tx.commit();
//		} catch (NoEnoughSpaceException e) {
//			tx.rollback();
//		}
//		LogUtil.logActivityReward(p, activity, 0);
//	}

	private String getFactionName(int npcId) {
		int faction = getFaction(npcId);
		String factionName = GameObject.getFactionName(faction);
		return factionName;
	}

//	private int getFaction(int npcId) {
//		for (int i = 0; i < NPC2MAPS.length; i++) {
//			if (NPC2MAPS[i] == npcId) {
//				return i+1;
//			}
//		}
//		return 0;
//	}
	
	private int getFaction(int npcId) {
		if(NPCS2MAPS!=null){
			for (int faction : NPCS2MAPS.keySet()) {
				List<Integer> npcs = NPCS2MAPS.get(faction);
				if(npcs.contains(npcId)){
					return faction;
				}
			}
		}
		return 0;
	}

	public void checkTime() {
		Iterator<Integer> it = bossDieTimes.keySet().iterator();
		while (it.hasNext()) {
			int bossId = ((Integer) it.next()).intValue();
			Integer[] bossTimes = bossDieTimes.get(bossId);
			if (bossTimes[0].intValue() == bossTimes[1].intValue()) {
				bossTimes[1] += 1;
			}
			if (Time.currTime - bossTimes[1] >= 5 * 1000) {
				addExp(bossId);
				bossTimes[1] = Time.currTime;
				bossTimes[2] += 1;
			}
			if (Time.currTime - bossTimes[0] > 30 * 60 * 1000) {
				it.remove();
			}
		}
	}

	public void addExp(int faction) {
//		int faction = getFaction(bossId);
		if (faction > 3 || faction < 1) {
			return;
		}
		for (Player p : ObjectAccessor.players.values()) {
			if (p.map.map.getId() == MAPIDS[faction - 1] && p.faction==faction) {
				PlayerTransaction tx = p.newTransaction("BOSSACT");
//				p.addExp(p.level * 5, tx, true);
				p.addExp(1000, tx, true);
				tx.commit();
			}
		}
	}
	
	

}
