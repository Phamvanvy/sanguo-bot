package peony.service.activity;

import java.text.MessageFormat;
import java.util.Calendar;
import org.apache.log4j.Logger;
import com.pip.util.Utils;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;

/**
 * 周年登陆奖励。
 * 每天第一次登陆游戏都活获得奖励道具，
 * 连续累计登陆游戏到10次将可以获得超值奖励，中途登陆不可中断。
 */
public class DayOnlineActivity2 implements IActivityImpl, ServiceEventListener {
	
	private static Logger log = Logger.getLogger(DayOnlineActivity2.class);
	
	protected Activity activity;
	
	// 记录每个玩家活动期间连续上线的天数，key是玩家ID，value是玩家上过线的天
//	protected IntHashMap<int[]> playerOnlineDays = new IntHashMap<int[]>();
	
	// 记录玩家连续登陆N天的奖励
	protected int[] rewardItems = { 1910, 1910, 1910, 1910, 1910, 1910, 1910, 1910, 1910, 1587};
	
	protected int[] rewardItemCounts = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	
	// 上次保存的时间
//	private int lastSaveTime;
	
	protected Calendar cal = Calendar.getInstance();
	
	public DayOnlineActivity2(Activity owner) {
		this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}

	private String getDBName() {
		return "DayOnlineActivity2" + activity.getId();
	}
	
	public void load() {
//		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
//		Database db = null;
//		try {
//			db = dbservice.openDatabase(getDBName());
//			Cursor cursor = null;
//            DatabaseEntry keyEntry = new DatabaseEntry();
//            DatabaseEntry dataEntry = new DatabaseEntry();
//            try {
//	            cursor = db.openCursor(null, new CursorConfig());
//	            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
//	            	int playerID = Integer.parseInt(StringBinding.entryToString(keyEntry));
//	            	String value = StringBinding.entryToString(dataEntry);
//	            	int[] arr = Utils.stringToIntArray(value, ',');
//	            	playerOnlineDays.put(playerID, arr);
//	            }
//	        } finally {
//	            if (cursor != null) {
//	                try {
//	                    cursor.close();
//	                } catch (Exception e) {
//	                }
//	            }
//	        }
//		} catch (Exception e) {
//		} finally {
//			if (db != null) {
//				try {
//					db.close();
//				} catch (Exception e) {
//				}
//			}
//		}
//		lastSaveTime = Time.currTime;
	}
	
	public int[] getDayOnlineData(int playerId){
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(String.valueOf(playerId), keyEntry);
            if(db.get(null,keyEntry,dataEntry,LockMode.DEFAULT)==OperationStatus.SUCCESS){
            	String value = StringBinding.entryToString(dataEntry);
            	int[] arr = Utils.stringToIntArray(value, ',');
            	return arr;
            }else{
            	return null;
            }
		} catch (Exception e) {
			return null;
		} finally {
			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public void save() {
//		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
//		Database db = null;
//		try {
//			db = dbservice.openDatabase(getDBName());
//            DatabaseEntry keyEntry = new DatabaseEntry();
//            DatabaseEntry dataEntry = new DatabaseEntry();
//            for (int playerID : playerOnlineDays.keySet()) {
//            	StringBinding.stringToEntry(String.valueOf(playerID), keyEntry);
//            	int[] arr = playerOnlineDays.get(playerID);
//            	StringBinding.stringToEntry(Utils.intArrayToString(arr, ','), dataEntry);
//            	db.put(null, keyEntry, dataEntry);
//            }
//		} catch (Exception e) {
//		} finally {
//			if (db != null) {
//				try {
//					db.close();
//				} catch (Exception e) {
//				}
//			}
//		}
//		lastSaveTime = Time.currTime;
	}
	
	public void insertData(int playerId, int[] arr){
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
        	StringBinding.stringToEntry(String.valueOf(playerId), keyEntry);
        	StringBinding.stringToEntry(Utils.intArrayToString(arr, ','), dataEntry);
        	db.put(null, keyEntry, dataEntry);
		} catch (Exception e) {
		} finally {
			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
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
	
	protected void playerLogined(Player p) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(activity.getSchedule().stopTime);
		int activityEndDay = calendar.get(Calendar.DAY_OF_YEAR);
		int[] arr;
		cal.setTimeInMillis(System.currentTimeMillis());
		int day = cal.get(Calendar.DAY_OF_YEAR);
		if(day-activityEndDay<=rewardItems.length){
			arr = getDayOnlineData(p.id);
			if (arr!=null) {
//				arr = playerOnlineDays.get(p.id);
				if (arr.length == 0 || arr[arr.length - 1] == day-1) {
					int[] newArr = new int[arr.length + 1];
					System.arraycopy(arr, 0, newArr, 0, arr.length);
					newArr[arr.length] = day;
//					playerOnlineDays.put(p.id, newArr);
					insertData(p.id, newArr);
					sendReward(p, newArr.length);
				}
//				else if(day<=activityEndDay){
//					if(day!=arr[arr.length - 1]){
//						arr = new int[] { day };
//						playerOnlineDays.put(p.id, arr);
//						sendReward(p, 1);
//					}
//				}
			} else if(day<=activityEndDay){
				arr = new int[] { day };
//				playerOnlineDays.put(p.id, arr);
				insertData(p.id, arr);
				sendReward(p, 1);
			}
		}else{
			Server.server.getEventManager().unregisterListener(this);
		}
		
		// 每10分钟保存一次记录
//		if (Time.currTime > lastSaveTime + 600000) {
//			save();
//		}
	}
	
	protected void sendReward(Player p, int days) {
		if (days > rewardItems.length) {
			return;
		}
		int itemID = rewardItems[days - 1];
		int itemCount = rewardItemCounts[days - 1];
		GameItem item = ObjectAccessor.createGameItem(itemID);
		
		// 为玩家添加到背包
		PlayerTransaction tx = p.newTransaction("ACTV2");
		Gain gain = new Gain(p);
		gain.addGainItem(item, itemCount);
		try {
			p.addGainComplete(gain, tx, true);
			tx.commit();
			String content = MessageFormat.format("恭喜你，{0}，获得了登陆奖励，活动期间每日登陆将有惊喜(奖励已放入背包，请查收)", p.name);
			p.message(-1, content, -1, -1);
		} catch (Exception e) {
			tx.rollback();
			
			// 如果背包满则发送邮件
			String content = MessageFormat.format("恭喜你，{0}，获得了登陆奖励，活动期间每日登陆将有惊喜", p.name);
			Server.server.getServiceRegistry().getMailService().sendSystemMail(
				p.id, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", "登陆奖励！", content, 0, item, itemCount, "ACTV");
			content = MessageFormat.format("恭喜你，{0}，获得了登陆奖励，活动期间每日登陆将有惊喜（您的背包已满，奖励已经以飞鸽附件的方式发送到你的飞鸽里了，请及时查收。）", p.name);
			p.message(-1, content, -1, -1);
		}
		
		// 记录日志
		LogUtil.logActivityReward(p, activity, days);
	}

}
