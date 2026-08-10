package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pip.util.Utils;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;

public class IBuyRewardActivity3 implements IActivityImpl, ServiceEventListener {

	protected Activity activity;

	// 上次保存的时间
	protected int lastSaveTime;

	// 缓存累计消费金额，提高数据库效率
	protected IntHashMap<Integer> consumeCache = new IntHashMap<Integer>();
	
	// 每个额度已经赠送过的玩家的集合
	protected IntHashMap<List<Integer>> sendCache = new IntHashMap<List<Integer>>(); 
	
	// 每个额度相应的奖励
	protected Map<Integer, List<Integer>> actives = new HashMap<Integer, List<Integer>>(); 

	public IBuyRewardActivity3(Activity owner) {
		this.activity = owner;
	}

	public Activity getActivity() {
		return activity;
	}

	public String getDBName() {
		return "IBUYREWARDACTIVITY" + activity.getId();
	}

	/**
	 * 如果有历史数据，载入历史数据。
	 */
	public void load() {
		SleepyCatService dbservice = Server.server.getServiceRegistry()
				.getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
			Cursor cursor = null;
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			try {
				cursor = db.openCursor(null, new CursorConfig());
				while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
					int baseMoney = Integer.parseInt(StringBinding
							.entryToString(keyEntry));
					String value = StringBinding.entryToString(dataEntry);
					int[] arr = Utils.stringToIntArray(value, ',');
					List<Integer> list = new ArrayList<Integer>();
					for (int i = 0; i < arr.length; i++) {
						list.add(arr[i]);
					}
					sendCache.put(baseMoney, list);
				}
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
		} finally {
			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {
				}
			}
		}
		lastSaveTime = Time.currTime;
	}

	/**
	 * 服务器关闭时，把临时数据保存到bdb中。
	 */
	public void save() {
		SleepyCatService dbservice = Server.server.getServiceRegistry()
				.getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			for (int baseMoney : sendCache.keySet()) {
				StringBinding.stringToEntry(String.valueOf(baseMoney), keyEntry);
				List<Integer> list = sendCache.get(baseMoney);
				int arr[] = new int[list.size()];
				for (int i = 0; i < list.size(); i++) {
					arr[i] = list.get(i);
				}
				StringBinding.stringToEntry(Utils.intArrayToString(arr, ','),
						dataEntry);
				db.put(null, keyEntry, dataEntry);
			}
		} catch (Exception e) {
		} finally {
			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {
				}
			}
		}
		lastSaveTime = Time.currTime;
	}

	/**
	 * 删除临时数据。
	 */
	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry()
				.getSleepyCatService();
		dbservice.removeDatabase(getDBName());
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		// 记录由GM工具输入的额度与奖励
		String config = activity.getConfigData();
		if (config != null) {
			String[] ss = config.split(",");
			for (int i = 0; i < ss.length; i += 3) {
				List<Integer> list = new ArrayList<Integer>();
				list.add(Integer.parseInt(ss[i + 1]));
				list.add(Integer.parseInt(ss[i + 2]));
				actives.put(Integer.parseInt(ss[i]), list);
			}
		}
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_IBUY };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_IBUY:
			playerBuyOK(((Integer) event.param1).intValue(),
					((Integer) event.param2).intValue());
			break;
		}
	}

	/*
	 * 玩家消费通知。
	 */
	protected void playerBuyOK(int playerId, int money) {
		int total;
		if (consumeCache.containsKey(playerId)) {
			total = consumeCache.get(playerId) + money;
		} else {
			total = Server.server.getServiceRegistry().getDbService().ibuyDAO
				.getTotalConsume(playerId, activity.getSchedule().startTime);
		}
		consumeCache.put(playerId, new Integer(total));
		// 判断是否送礼物
		if (actives != null && actives.size() > 0) {
			for (Integer baseMoney : actives.keySet()) {
				List<Integer> list = sendCache.get(baseMoney);
				if(list == null){
					list = new ArrayList<Integer>();
				}
				if (list.contains(playerId))
					continue;
				sendGift(playerId, total, baseMoney,list);
			}
		}
		
		// 每10分钟保存一次记录
		if (Time.currTime > lastSaveTime + 600000) {
			save();
		}
	}

	/*
	 * 活动期间消费满一定额度赠送礼物。
	 */
	public void sendGift(int playerID, int total, int baseMoney,List<Integer> list) {
		Player p = ObjectAccessor.getPlayer(playerID);
		if (p != null) {
			if (total >= baseMoney) {
				int itemID = (actives.get(baseMoney)).get(0);
				int count = (actives.get(baseMoney)).get(1);
				GameItem item = ObjectAccessor.createGameItem(itemID);
				if (item != null) {
					String content = MessageFormat.format(
							"感谢您的参与，更多精彩请继续关注相关活动!", p.name);
					Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(playerID, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", "活动奖励", content, 0,
									item, count, "ACTV");
					list.add(p.id);
					sendCache.put(baseMoney, list);
					p.message(-1, "恭喜您获得累计消费活动奖励，快到飞鸽传书去查看您的奖励吧，消费享好礼，天天送不停。", -1, -1);
					// 记录日志
					LogUtil.logActivityRewards(p, activity);
				}
			}
		}
	}
}
