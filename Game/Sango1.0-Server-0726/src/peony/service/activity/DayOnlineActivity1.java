package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;

/**
 * 畅游运营活动1：鼓励用户每天上线。
 * 1天：1183*1
 * 2天：651*5
 * 3天：665*1
 * 4天：1818*2
 * 5天：1184*1
 * 6天：653*5
 * 7天：670*1
 * 8天：1579*1
 * 9天：1582*3
 * 10天：1580*1
 * @author lighthu
 */
public class DayOnlineActivity1 implements IActivityImpl, ServiceEventListener {
	private static Logger log = Logger.getLogger(DayOnlineActivity1.class);
	
	protected Activity activity;
	
	// 记录每个玩家活动期间上线的天数，key是玩家ID，value是玩家上过线的天（从2010年1月1日起计算）
	protected IntHashMap<int[]> playerOnlineDays = new IntHashMap<int[]>();
	// 记录玩家登陆N天的奖励
	protected int[] rewardItems = { 1183, 651, 665, 1818, 1184, 653, 670, 1579, 1582, 1580 };
	protected int[] rewardItemCounts = { 1, 5, 1, 2, 1, 5, 1, 1, 3, 1 };
	
	// 上次保存的时间
	private int lastSaveTime;
	
	protected Calendar cal = Calendar.getInstance();
	
	public DayOnlineActivity1(Activity owner) {
		this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}

	private String getDBName() {
		return "DayOnlineActivity" + activity.getId();
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
	            	String value = StringBinding.entryToString(dataEntry);
	            	int[] arr = Utils.stringToIntArray(value, ',');
	            	playerOnlineDays.put(playerID, arr);
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
            for (int playerID : playerOnlineDays.keySet()) {
            	StringBinding.stringToEntry(String.valueOf(playerID), keyEntry);
            	int[] arr = playerOnlineDays.get(playerID);
            	StringBinding.stringToEntry(Utils.intArrayToString(arr, ','), dataEntry);
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
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		dbservice.removeDatabase(getDBName());
	}
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public int[] getEventTypes() {
		return new int[] {
			ServiceEvent.EVENT_PLAYER_FIRSTLOAD
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			playerLogined((Player)event.param1);
			break;
		}
	}
	
	/*
	 * 玩家登陆通知。
	 */
	protected void playerLogined(Player p) {
		int[] arr;
		cal.setTimeInMillis(System.currentTimeMillis());
		int day = cal.get(Calendar.DAY_OF_YEAR);
		if (playerOnlineDays.containsKey(p.id)) {
			arr = playerOnlineDays.get(p.id);
			if (arr.length == 0 || arr[arr.length - 1] != day) {
				// 跨天啦
				int[] newArr = new int[arr.length + 1];
				System.arraycopy(arr, 0, newArr, 0, arr.length);
				newArr[arr.length] = day;
				playerOnlineDays.put(p.id, newArr);
				
				// 发送奖励
				sendReward(p, newArr.length);
			}
		} else {
			// 记录第一天
			arr = new int[] { day };
			playerOnlineDays.put(p.id, arr);
			
			// 发送奖励
			sendReward(p, 1);
		}
		
		// 每10分钟保存一次记录
		if (Time.currTime > lastSaveTime + 600000) {
			save();
		}
	}
	
	/*
	 * 按照一个玩家累计登陆的天数发放奖励。
	 */
	protected void sendReward(Player p, int days) {
		if (days > rewardItems.length) {
			return;
		}
		int itemID = rewardItems[days - 1];
		int itemCount = rewardItemCounts[days - 1];
		GameItem item = ObjectAccessor.createGameItem(itemID);
		
		// 为玩家添加到背包
		PlayerTransaction tx = p.newTransaction("ACTV");
		Gain gain = new Gain(p);
		gain.addGainItem(item, itemCount);
		try {
			p.addGainComplete(gain, tx, true);
			tx.commit();
			String content = MessageFormat.format("恭喜你，{0}，长期为国家征战，国君十分欣慰特此嘉奖。（奖励已放入背包，请查收。）", p.name);
			p.message(-1, content, -1, -1);
		} catch (Exception e) {
			tx.rollback();
			
			// 如果背包满则发送邮件
			String content = MessageFormat.format("恭喜你，{0}，长期为国家征战，国君十分欣慰特此嘉奖。", p.name);
			Server.server.getServiceRegistry().getMailService().sendSystemMail(
				p.id, "系统", "三国君主齐点名，每天登陆有惊喜！", content, 0, item, itemCount, "ACTV");
			content = MessageFormat.format("恭喜你，{0}，长期为国家征战，国君十分欣慰特此嘉奖。（您的背包已满，奖励已经以飞鸽附件的方式发送到你的飞鸽里了，请及时查收。）", p.name);
			p.message(-1, content, -1, -1);
		}
		
		// 记录日志
		LogUtil.logActivityReward(p, activity, days);
	}
}
