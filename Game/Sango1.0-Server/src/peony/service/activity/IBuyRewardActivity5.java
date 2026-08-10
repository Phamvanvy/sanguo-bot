package peony.service.activity;

import java.text.MessageFormat;
import org.apache.log4j.Logger;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;
import ch.javasoft.util.intcoll.IntHashMap;

/**
 * 消费活动5：活动期间消费满X元，循环送礼物。
 */

public class IBuyRewardActivity5 implements IActivityImpl, ServiceEventListener {
	
	private static Logger log = Logger.getLogger(IBuyRewardActivity5.class);
	
	protected int BASEMONEY = 16200000; //基础消费
	
	protected int ITEM = 0; //奖励物品
	
	protected int COUNT = 0; //奖励物品数量
	
	protected int MAXCOUNT = 0; //奖励物品数量上限
	
	protected Activity activity;
	
	// 缓存累计消费金额
	protected IntHashMap<Integer> ibuyCache = new IntHashMap<Integer>();
	
	protected IntHashMap<Integer> ibuyCountCache = new IntHashMap<Integer>();
	
	protected int lastSaveTime;
	
	public IBuyRewardActivity5(Activity owner) {
		this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	private String getDBName() {
		return "IBUYREWARDACTIVITY5" + activity.getId();
	}

	/**
	 * 如果有历史数据，载入历史数据。
	 */
	public void load() {
		String configData = activity.configData;
		String[] data0 = configData.split(",");
		for(String data1 : data0){
			String[] data2 = data1.split(":");
			String propName = data2[0];
			String propValue = data2[1];
			if(propName.equalsIgnoreCase("money"))
				BASEMONEY = Integer.parseInt(propValue);
			else if(propName.equalsIgnoreCase("item"))
				ITEM = Integer.parseInt(propValue);
			else if(propName.equalsIgnoreCase("count"))
				COUNT = Integer.parseInt(propValue);
			else if(propName.equalsIgnoreCase("max"))
				MAXCOUNT = Integer.parseInt(propValue);
		}
		
		loadBdb();
	}
	
	protected void loadBdb(){
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
					int playerId = Integer.parseInt(StringBinding.entryToString(keyEntry));
					int value = Integer.parseInt(StringBinding.entryToString(dataEntry));
					ibuyCache.put(playerId, new Integer(value));
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
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			for (int playerId : ibuyCache.keySet()) {
				StringBinding.stringToEntry(String.valueOf(playerId), keyEntry);
				int value = ibuyCache.get(playerId);
				StringBinding.stringToEntry(String.valueOf(value), dataEntry);
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
	}
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public int[] getEventTypes() {
		return new int[] {
			ServiceEvent.EVENT_IBUY
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_IBUY:
			playerBuyOK(((Integer)event.param1).intValue(), ((Integer)event.param2).intValue());
			break;
		}
	}
	
	/*
	 * 玩家消费通知。
	 */
	protected void playerBuyOK(int playerId, int money) {
		int total = 0;
		if (ibuyCache.containsKey(playerId)) {
			total = ibuyCache.get(playerId) + money;
		} else {
			total = Server.server.getServiceRegistry().getDbService().ibuyDAO
				.getTotalConsume(playerId, activity.getSchedule().startTime);
		}
		if(total>=BASEMONEY){
			int count = total/BASEMONEY;
			if(count>0){
				sendGift(playerId, COUNT*count);
				total %= BASEMONEY;
			}
		}
		ibuyCache.put(playerId, new Integer(total));
		
		// 每10分钟保存一次记录
		if (Time.currTime > lastSaveTime + 600000) {
			save();
		}	
	}
	
	/**
	 * 通过飞鸽发送奖励物品，活动期间消费满500元赠送一个3级宝石兑换符
	 * @param p
	 * @param money
	 */
	public void sendGift(int playerId, int count) {
		int sendCount = 0;
		try{sendCount = ibuyCountCache.get(playerId);}catch(Exception e){}
		if(sendCount>=MAXCOUNT)
			return;
		else if(sendCount+count>MAXCOUNT && sendCount<MAXCOUNT)
			count = MAXCOUNT-sendCount;
		GameItem item = ObjectAccessor.createGameItem(ITEM);
		Player p = ObjectAccessor.getPlayer(playerId);
		String content = MessageFormat.format("感谢您的参与，更多精彩请继续关注游戏内公告！", p.name);
		Server.server.getServiceRegistry().getMailService().sendSystemMail(
				playerId, peony.Messages.STRING_00004, "消费活动奖励", content, 0, item, count, "ACTV");
        ibuyCountCache.put(playerId, new Integer(sendCount+count));
		// 记录日志
		LogUtil.logActivityRewards(p, activity);
	}
	
}
