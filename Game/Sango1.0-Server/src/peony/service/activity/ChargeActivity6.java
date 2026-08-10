package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ChargeActivity6 implements IActivityImpl, ServiceEventListener {
	private static final Logger log = Logger.getLogger(ChargeActivity6.class);
	// 上次保存的时间
	private int lastSaveTime;
	public Activity activity;

	protected int baseMoney = 0;// 多少钱领一次奖励
	protected int itemID; // 奖励物品ID
	protected int count = 0; // 奖励物品数量
	protected int limit = 0; // 上限为0时为不限次数

	public Date START_TIME;// 活动开始时间
	public Date STOP_TIME;// 活动结束时间

	protected IntHashMap<Integer> getReward = new IntHashMap<Integer>();// 获取奖励次数

	// 每个额度相应的奖励
	protected Map<Integer, List<Integer>> actives = new HashMap<Integer, List<Integer>>();

	// 每个额度已经赠送过的账号的集合
	protected IntHashMap<List<Integer>> sendCache = new IntHashMap<List<Integer>>();

	// 缓存累计消费金额，提高数据库效率
	protected IntHashMap<Integer> consumeCache = new IntHashMap<Integer>();

	public ChargeActivity6(Activity owner) {
		this.activity = owner;
	}

	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry()
				.getSleepyCatService();
		dbservice.removeDatabase(getDBName());
	}

	private String getDBName() {
		return "ChargeActivity6" + activity.getId();
	}
	
	private String getDBName_ConsumeCache(){//保存玩家账号充值信息
		return "ChargeActivity6_ConsumeCache" + activity.getId(); 
	}
	

	public Activity getActivity() {
		return null;
	}

	public void load() {
		SleepyCatService dbservice = Server.server.getServiceRegistry()
				.getSleepyCatService();
		Database db = null;
		Database db1 = null;
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
			 //读取本地缓存充值数据
	        db1 = dbservice.openDatabase(getDBName_ConsumeCache());
			Cursor cursor1 = null;
            DatabaseEntry keyEntry1 = new DatabaseEntry();
            DatabaseEntry dataEntry1 = new DatabaseEntry();
            try {
	            cursor1 = db1.openCursor(null, new CursorConfig());
	            while (cursor1.getNext(keyEntry1, dataEntry1, null) != OperationStatus.NOTFOUND) {
	            	int accountId = Integer.parseInt(StringBinding.entryToString(keyEntry1));
	            	int value = Integer.parseInt(StringBinding.entryToString(dataEntry1));
	            	consumeCache.put(accountId, new Integer(value));
	            }
	        } finally {
	            if (cursor1 != null) {
	                try {
	                    cursor1.close();
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
			if (db1 != null) {
				try {
					db1.close();
				} catch (Exception e) {
				}
			}
		}
		lastSaveTime = Time.currTime;
	}

	public void parseConfig() {
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
	}

	public void save() {
		SleepyCatService dbservice = Server.server.getServiceRegistry()
				.getSleepyCatService();
		Database db = null;
		Database db1 = null;
		try {
			db = dbservice.openDatabase(getDBName());
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			for (int baseMoney : sendCache.keySet()) {
				StringBinding
						.stringToEntry(String.valueOf(baseMoney), keyEntry);
				List<Integer> list = sendCache.get(baseMoney);
				int arr[] = new int[list.size()];
				for (int i = 0; i < list.size(); i++) {
					arr[i] = list.get(i);
				}
				StringBinding.stringToEntry(Utils.intArrayToString(arr, ','),
						dataEntry);
				db.put(null, keyEntry, dataEntry);
			}
			//2.存储活动开始以来的充值记录
            db1 = dbservice.openDatabase(getDBName_ConsumeCache());
            DatabaseEntry keyEntry1 = new DatabaseEntry();
            DatabaseEntry dataEntry1 = new DatabaseEntry();
            for (int accountId : consumeCache.keySet()) {
            	StringBinding.stringToEntry(String.valueOf(accountId), keyEntry1);
            	int value = getReward.get(accountId);
            	StringBinding.stringToEntry(String.valueOf(value), dataEntry1);
            	db1.put(null, keyEntry1, dataEntry1);
            }
		} catch (Exception e) {
		} finally {
			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {
				}
			}
			if (db1 != null) {
				try {
					db1.close();
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
		parseConfig();
		this.START_TIME = this.activity.schedule.startTime;
		this.STOP_TIME = this.activity.schedule.stopTime;
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_CHARGE_SUCCESS,
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_CHARGE_SUCCESS:
			processPlayerCharge((Player) event.param1, (Integer) event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerCharge_OffLine((Player)event.param1);
			break;
		}
	}
	
	public void processPlayerCharge_OffLine(Player player){
		int total_New= Server.server.getServiceRegistry().getDbService().chargeDao
					.getTotalChargesAfter(player.accountId, START_TIME);
		int total_Old=consumeCache.containsKey(player.accountId)?consumeCache.get(player.accountId):0;
		log.info("[CHARGEACTIVITY6OFFLINE"+activity.getId()+"]PLAYERACC["+player.accountId+"]id["+player.id+"]TOTAL_NEW["+total_New+"]TOTAL_OLD["+total_Old+"]");
		if(total_New>total_Old){
			consumeCache.put(player.accountId, new Integer(total_New));
			// 判断是否送礼物
			if (actives != null && actives.size() > 0) {
				for (Integer baseMoney : actives.keySet()) {
					List<Integer> list = sendCache.get(baseMoney);
					if (list == null) {
						list = new ArrayList<Integer>();
					}
					if (list.contains(player.accountId))
						continue;
					sendGift(player.id, total_New, baseMoney, list);
				}
			}
		}
	}

	public void processPlayerCharge(Player player, int money) {
		if (player != null) {
			int total;
			if (consumeCache.containsKey(player.accountId)) {
				total = consumeCache.get(player.accountId) + money;
			} else {
				total = Server.server.getServiceRegistry().getDbService().chargeDao
						.getTotalChargesAfter(player.accountId, START_TIME)+money;
			}
			consumeCache.put(player.accountId, new Integer(total));
			log.info("[CHARGEACTIVITY6ONLINE"+activity.getId()+"]PLAYERACC["+player.accountId+"]ID["+player.id+"]TOTAL["+total+"]");
			// 判断是否送礼物
			if (actives != null && actives.size() > 0) {
				for (Integer baseMoney : actives.keySet()) {
					List<Integer> list = sendCache.get(baseMoney);
					if (list == null) {
						list = new ArrayList<Integer>();
					}
					if (list.contains(player.accountId))
						continue;
					sendGift(player.id, total, baseMoney, list);
				}
			}
		}

		if (Time.currTime > lastSaveTime + 600000) {
			save();
		}
	}

	public void sendGift(int playerID, int total, int baseMoney,
			List<Integer> list) {
		Player p = ObjectAccessor.getPlayer(playerID);
		if (p != null) {
			if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
				total=total*3600;
			}else{
				total=total*36000;
			}
			if (total >= baseMoney) {
				int itemID = (actives.get(baseMoney)).get(0);
				int count = (actives.get(baseMoney)).get(1);
				GameItem item = ObjectAccessor.createGameItem(itemID);
				if (item != null) {
					String content = MessageFormat.format(
							peony.Messages.STRING_01022, p.name);
					Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(playerID,
									peony.Messages.STRING_00004,
									peony.Messages.STRING_01018, content, 0,
									item, count, "ACTV_GRANDTOTAL");
					list.add(p.accountId);
					sendCache.put(baseMoney, list);
					// 记录日志
					LogUtil.logActivityRewards(p, activity);
				}
			}
		}
	}
}
