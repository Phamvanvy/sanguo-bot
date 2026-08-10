package peony.service.activity;

import java.text.MessageFormat;
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
 * 消费活动2：活动期间每消费一定元宝数，送礼物,当玩家登录时检测距上次兑换后的消费钱数
 * @author mfou
 */
public class IBuyRewardActivity2 implements IActivityImpl, ServiceEventListener {

	private static Logger log = Logger.getLogger(IBuyRewardActivity2.class);

	private static final int BASEMONEY = 1800000; //基础消费
	
	private static final int itemID = 832; // 奖励的物品ID

	protected Activity activity;

	protected IntHashMap<Integer> ibuyCache = new IntHashMap<Integer>();

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
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
			Cursor cursor = null;
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            try {
	            cursor = db.openCursor(null, new CursorConfig());
	            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
	            	int playerID = Integer.parseInt(StringBinding.entryToString(keyEntry));
	            	int money = Integer.parseInt(StringBinding.entryToString(dataEntry));
	            	ibuyCache.put(playerID, new Integer(money));
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
				int money = ibuyCache.get(playerId);
				StringBinding.stringToEntry(String.valueOf(money), dataEntry);
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
		Server.server.getEventManager().registerListener(this);
	}

	/**
	 * 删除临时数据。
	 */
	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		dbservice.removeDatabase(getDBName());
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_IBUY,
				ServiceEvent.EVENT_PLAYER_LOGINED };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_IBUY:
			playerIBuyOk(((Integer) event.param1).intValue(),
					((Integer) event.param2).intValue());
			break;
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			checkRuleAndSendGift((Player) event.param1);
			break;
		}
	}

	/**
	 * 玩家消费成功时，缓存消费金额
	 * 
	 * @param p
	 * @param money
	 */
	protected void playerIBuyOk(int playerId, int money) {
			int newMoney = 0;
			if (ibuyCache.containsKey(playerId)) {
				int leftMoney = ibuyCache.get(playerId);
				newMoney = leftMoney + money;
			} else {
				// 活动期间首次消费
				newMoney = money;
			}
			ibuyCache.put(playerId, new Integer(newMoney));
	}

	/**
	 * 玩家登录时检测如果达到条件的发放奖励，每满50元通过飞鸽发放一次礼物
	 * 
	 * @param p
	 */
	protected void checkRuleAndSendGift(Player p) {
		if (p != null) {
			if(ibuyCache.containsKey(p.id)){
			    int leftMoney = ibuyCache.get(p.id);
			    while (leftMoney / BASEMONEY != 0) {
				   sendGift(p);
				   leftMoney = leftMoney - BASEMONEY;
			    }
			    ibuyCache.put(p.id, new Integer(leftMoney));
		    }
		}
	}

	/**
	 * 通过飞鸽发送奖励物品，每消费50元赠送一个3级宝石兑换符
	 * @param p
	 */
	public void sendGift(Player p) {
		GameItem item = ObjectAccessor.createGameItem(itemID);
		String content = MessageFormat.format("感謝您的參与,更多精彩請繼續關註暑期活動!", p.name);
		Server.server.getServiceRegistry().getMailService().sendSystemMail(
				p.id, "系統", "暑期活動獎勵", content, 0, item, 1, "ACTV");
		// 记录日志
		LogUtil.logActivityRewards(p, activity);
	}
}
