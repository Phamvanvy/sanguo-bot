package peony.service.activity;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
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
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

/**
 * boss攻城活动 每日14：00,19:00一次进攻,3主城分别刷新boss; 2小时未击杀boss消失;3.boss击杀后场景内用户获得奖励
 */
public class BossSiegeActivity implements IActivityImpl, ServiceEventListener, CycleListener {
	
	private static Logger log = Logger.getLogger(BossSiegeActivity.class);

	private static final int[] NPC2MAPS = {1114190, 983116, 1441865};

	private static final int[] MAPIDS = {272, 240, 352};

	protected Activity activity;

	private Set<Integer> bossInstanceIds;

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
	}

	public boolean update(int diff) {
		if (lastRefreshTime == 0 || Time.currTime - lastRefreshTime > 2 * 3600 * 1000L) {
			tryRemove();
			tryAdd();
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
		if (hour == 14 || hour == 19) {
			ProjectData pro = Server.server.getServiceRegistry().getDataService().data;
			bossInstanceIds = new HashSet<Integer>();
			for (int i = 0; i < NPC2MAPS.length; i++) {
				GameMapObject gmo = GameMapObject.findByID(pro, NPC2MAPS[i]); // boss
				if (gmo != null && gmo instanceof GameMapNPC) {
					int mapId = gmo.owner.getGlobalID();
					VMapManager mgr = Server.server.getWorld().getVMapManager(mapId);
					if (mgr instanceof NoInstanceVMapManager) {
						VMap[] maps = ((NoInstanceVMapManager) mgr).getVMaps(mapId);
						GameObject npc = VMapUtil.addCreature(maps[0],(GameMapNPC) gmo, true, 0, null);
						bossInstanceIds.add(npc.instanceId);
						String format = "Tướng địch{0} xuất hiện tại đô thành này, toàn thể quốc dân cấp tốc nghênh đón quân địch!";
						Server.server.getServiceRegistry().getChatService()
						.sendFactionSystemMessage(getFaction(npc.id),MessageFormat.format(format, npc.name));
					}
				}
			}
			lastRefreshTime = Time.currTime;
			lastAddDay = day;
			lastAddHour = hour;
		}

	}

	private void tryRemove() {
		if (bossInstanceIds == null || bossInstanceIds.size() == 0) {
			return;
		}
		bossDieTimes.clear();
		for (int instanceId : bossInstanceIds) {
			Creature c = (Creature) ObjectAccessor.getGameObject(instanceId);
			if (c != null) {
				String format = "nhân dân{0}  không thể ngăn chặn được địch {1} tấn công quốc gia, lấy làm tiêc vì đã bỏ qua mất phần thưởng ";
				String facName = getFactionName(c.id);
				Server.server.getServiceRegistry().getChatService()
						.sendFactionSystemMessage(getFaction(c.id),MessageFormat.format(format, facName, c.name));
				c.removeFromWorld();
			}
		}
		bossInstanceIds.clear();
		lastRefreshTime = 0;
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_UNIT_DIE };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit) event.param1);
			break;
		}
	}

	protected void unitDie(Unit u) {
		if (bossInstanceIds == null)
			return;
		if (bossInstanceIds.contains(u.instanceId)) {
			Packet pt = new Packet(OpCode.SHOUT_SERVER);
			String format = "{0}民众阻止了敌将{1}对国主的进攻，获得了大量奖励";
			String fac = getFactionName(u.id);
			pt.putString(MessageFormat.format(format, fac, u.name));
			pt.putInt(0xFF0000);
			pt.putInt(10000);
			pt.put(1);
			int faction = getFaction(u.id);
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
			bossDieTimes.put(u.id, times);

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

	private int getFaction(int npcId) {
		for (int i = 0; i < NPC2MAPS.length; i++) {
			if (NPC2MAPS[i] == npcId) {
				return i+1;
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
			if (Time.currTime - bossTimes[0] > 5 * 60 * 1000
					&& bossTimes[2] > 60) {
				it.remove();
			}
		}
	}

	public void addExp(int bossId) {
		int faction = getFaction(bossId);
		if (faction > 3 || faction < 1) {
			return;
		}
		for (Player p : ObjectAccessor.players.values()) {
			if (p.map.map.getId() == MAPIDS[faction - 1] && p.faction==faction) {
				PlayerTransaction tx = p.newTransaction("BOSSACT");
				p.addExp(p.level * 5, tx, true);
				tx.commit();
			}
		}
	}

}
