package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
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

/**
 * 消费活动2：活动期间每消费一定元宝数，送礼物,其中消费额度，奖励物品ID,个数以及赠送次数上限由GM工具配置
 * 
 * @author mfou
 */
public class IBuyRewardActivity2 implements IActivityImpl, ServiceEventListener {

	private static Logger log = Logger.getLogger(IBuyRewardActivity2.class);

	protected Activity activity;

	protected IntHashMap<List<Integer>> ibuyCache = new IntHashMap<List<Integer>>();

	protected List<Integer> active = new ArrayList<Integer>();

	protected int baseMoney = 0;

	protected int itemID;

	protected int count = 0;

	protected int limit = 0; // 上限为0时为不限次数

	// 上次保存的时间
	protected int lastSaveTime;

	public IBuyRewardActivity2(Activity owner) {
		this.activity = owner;
	}

	public Activity getActivity() {
		return activity;
	}

	private String getDBName() {
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
					int playerId = Integer.parseInt(StringBinding
							.entryToString(keyEntry));
					String value = StringBinding.entryToString(dataEntry);
					int[] arr = Utils.stringToIntArray(value, ',');
					List<Integer> list = new ArrayList<Integer>();
					for (int i = 0; i < arr.length; i++) {
						list.add(arr[i]);
					}
					ibuyCache.put(playerId, list);
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
			for (int playerId : ibuyCache.keySet()) {
				StringBinding.stringToEntry(String.valueOf(playerId), keyEntry);
				List<Integer> list = ibuyCache.get(playerId);
				int str[] = new int[list.size()];
				for (int i = 0; i < str.length; i++) {
					str[i] = list.get(i);
				}
				StringBinding.stringToEntry(Utils.intArrayToString(str, ','),
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

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		String config = activity.getConfigData();
		if (config != null) {
			String[] str = config.split(",");
			baseMoney = Integer.parseInt(str[0]);
			itemID = Integer.parseInt(str[1]);
			count = Integer.parseInt(str[2]);
			limit = Integer.parseInt(str[3]);
		}
		Server.server.getEventManager().registerListener(this);
	}

	/**
	 * 删除临时数据。
	 */
	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry()
				.getSleepyCatService();
		dbservice.removeDatabase(getDBName());
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_IBUY, };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_IBUY:
			playerIBuyOk(((Integer) event.param1).intValue(),
					((Integer) event.param2).intValue());
			break;
		}
	}

	/**
	 * 玩家消费成功给予奖励
	 * @param p
	 * @param money
	 */
	protected void playerIBuyOk(int playerId, int money) {
		log.info("[IBUYACTIVITY2_PRE]ID["+playerId+"]MONEY["+money+"]");
		Player p = ObjectAccessor.getPlayer(playerId);
		if (p != null) {
			int newMoney = 0;
			int preMoney=0;
			List<Integer> list = null;
			if (ibuyCache.containsKey(playerId)) {
				list = ibuyCache.get(playerId);
				preMoney=list.get(0);
				newMoney = list.get(0) + money;
				list.set(0, newMoney);
			} else {
				// 活动期间首次消费
				list = new ArrayList<Integer>();
				newMoney = money;
				list.add(newMoney);
			}
			log.info("[IBUYACTIVITY2_PROCESSING]ID["+playerId+"]PREMONEY["+preMoney+"]NEWMONEY["+newMoney+"]BASEMONEY["+baseMoney+"]");
			checkRuleAndSendGift(p, newMoney,list);
		}
		// 每10分钟保存一次记录
		if (Time.currTime > lastSaveTime + 600000) {
			save();
		}	
	}

	/**
	 * 每满一定金额通过飞鸽发放一次礼物
	 * 
	 * @param p
	 * @param newMoney
	 */
	protected void checkRuleAndSendGift(Player p, int newMoney,List<Integer> list) {
		if (p != null) {
			if (list.size() < 2) {
				list.add(0);
			}
			int cnt = list.get(1);
			while (newMoney / baseMoney != 0) {
				if (limit != 0 && cnt >= limit)
					break;
				sendGift(p);
				newMoney = newMoney - baseMoney;
				cnt++;
			}
			list.set(0, newMoney);
			list.set(1, cnt);
			ibuyCache.put(p.id, list);
		}
	}

	/**
	 * 通过飞鸽发送奖励物品，每消费一定金额赠送礼物
	 * 
	 * @param p
	 */
	public void sendGift(Player p) {
		GameItem item = ObjectAccessor.createGameItem(itemID);
		String content = MessageFormat.format(peony.Messages.STRING_01022, p.name);
		Server.server.getServiceRegistry().getMailService().sendSystemMail(
				p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01282, content, 0, item, count, "ACTV");
		// 记录日志
		LogUtil.logActivityRewards(p, activity);
	}
}
