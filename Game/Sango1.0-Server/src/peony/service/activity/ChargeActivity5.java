package peony.service.activity;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.log4j.Logger;

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
import peony.game.PlayerPacketHandler;
import peony.game.Server;
import peony.game.Time;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;

public class ChargeActivity5 implements IActivityImpl, ServiceEventListener {
	private static final Logger log = Logger.getLogger(ChargeActivity5.class);
	// 上次保存的时间
	private int lastSaveTime;
	public Activity activity;
	
	protected int baseMoney = 0;//多少钱领一次奖励
	protected int itemID;		//奖励物品ID
	protected int count = 0;	//奖励物品数量
	protected int limit = 0; 	// 上限为0时为不限次数
	
	public Date START_TIME;//活动开始时间
	public Date STOP_TIME;//活动结束时间
	
	public int SAVELIMIT=-1;//存储数量
	
	protected IntHashMap<Integer> getReward = new IntHashMap<Integer>();//获取奖励次数
	
	
	// 缓存累计消费金额，提高数据库效率
	protected IntHashMap<Integer> consumeCache = new IntHashMap<Integer>();
	
	public ChargeActivity5(Activity owner){
		this.activity = owner;
	}
	
	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		dbservice.removeDatabase(getDBName_Reward());
		dbservice.removeDatabase(getDBName_ConsumeCache());
	}

	private String getDBName_Reward() {//保存已发送的奖励
		return "ChargeActivity5_Reward" + activity.getId();
	}
	private String getDBName_ConsumeCache(){//保存玩家账号充值信息
		return "ChargeActivity5_ConsumeCache" + activity.getId(); 
	}
	
	public Activity getActivity() {
		return null;
	}

	
	public void load() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		Database db1 = null;
		try {
			//读取本地已发送奖励
			db = dbservice.openDatabase(getDBName_Reward());
			Cursor cursor = null;
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            try {
	            cursor = db.openCursor(null, new CursorConfig());
	            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
	            	int accountId = Integer.parseInt(StringBinding.entryToString(keyEntry));
	            	int value = Integer.parseInt(StringBinding.entryToString(dataEntry));
	            	getReward.put(accountId, new Integer(value));
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
		String config = activity.configData;
		if (config != null) {
			String[] str = config.split(",");
			baseMoney = Integer.parseInt(str[0]);
			itemID = Integer.parseInt(str[1]);
			count = Integer.parseInt(str[2]);
			limit = Integer.parseInt(str[3]);
		}
	}

	public void save() {
		//1.本地数据存储领取过多少次奖励
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		Database db1 = null;
		try {
			db = dbservice.openDatabase(getDBName_Reward());
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            for (int accountId : getReward.keySet()) {
            	StringBinding.stringToEntry(String.valueOf(accountId), keyEntry);
            	int value = getReward.get(accountId);
            	StringBinding.stringToEntry(String.valueOf(value), dataEntry);
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
		this.START_TIME=this.activity.schedule.startTime;
		this.STOP_TIME=this.activity.schedule.stopTime;
		Server.server.getEventManager().registerListener(this);
	}

	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_CHARGE_SUCCESS,
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_CHARGE_SUCCESS:
			processPlayerCharge((Player)event.param1, (Integer)event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerCharge_OffLine((Player)event.param1);
			break;
		}
	}
	
	public void processPlayerCharge_OffLine(Player player){
		int totalNew = Server.server.getServiceRegistry().getDbService().chargeDao
		.getTotalChargesAfter(player.accountId, START_TIME);
		int total_Old=consumeCache.containsKey(player.accountId)?consumeCache.get(player.accountId):0;
		log.info("[CHARGEACTIVITY5_OFFLINE1"+activity.getId()+"]PLAYERACC["+player.accountId+"]ID["+player.id+"]TOTALNEW["+totalNew+"]TOTALOLD["+total_Old+"]LIMIT["+limit+"]START_TIME["+START_TIME+"]STOP_TIME["+STOP_TIME+"]");
		if(totalNew>total_Old){
			consumeCache.put(player.accountId, new Integer(totalNew));
			int moneyTemp=baseMoney/36000;//转换成元
			if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
				moneyTemp=baseMoney/3600;
			}
			int sendRewardCount=getReward.containsKey(new Integer(player.accountId))?getReward.get(player.accountId):0;//已发送的奖励次数
			int allCount=getReward.containsKey(SAVELIMIT)?getReward.get(SAVELIMIT):0;//已发放奖励次数
			log.info("[CHARGEACTIVITY5_OFFLINE2"+activity.getId()+"]PLAYERACC["+player.accountId+"]ID["+player.id+"]TOTALNEW["+totalNew+"]TOTALOLD["+total_Old+"]ALREADYSENDREWARD["+allCount+"]LIMIT["+limit+"]START_TIME["+START_TIME+"]STOP_TIME["+STOP_TIME+"]");
			while(sendRewardCount<totalNew/moneyTemp&&(allCount<=limit||limit==0)){
				sendGift(player);
				sendRewardCount++;
				allCount++;
			}
			getReward.put(player.accountId, new Integer(sendRewardCount));
			getReward.put(SAVELIMIT,new Integer(allCount));
		}
	}


	public void processPlayerCharge(Player player, int money) {
		if(player!=null){
			int total;
			if (consumeCache.containsKey(player.accountId)) {
				total = consumeCache.get(player.accountId) + money;
			} else {
				total = Server.server.getServiceRegistry().getDbService().chargeDao
						.getTotalChargesAfter(player.accountId, START_TIME)+money;
			}
			consumeCache.put(player.accountId, new Integer(total));
			int moneyTemp=baseMoney/36000;//转换成元
			if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
				moneyTemp=baseMoney/3600;
			}
			int sendRewardCount=getReward.containsKey(new Integer(player.accountId))?getReward.get(player.accountId):0;//已发送的奖励次数
			int allCount=getReward.containsKey(SAVELIMIT)?getReward.get(SAVELIMIT):0;//已发放奖励次数
			log.info("[CHARGEACTIVITY5_ONLINE"+activity.getId()+"]PLAYERACC["+player.accountId+"]ID["+player.id+"]total["+total+"]ALREADYSENDREWARD["+allCount+"]LIMIT["+limit+"]SENDREWARDCOUNT["+sendRewardCount+"]MONEYTEMP["+moneyTemp+"]MONEY["+money+"]START_TIME["+START_TIME+"]STOP_TIME["+STOP_TIME+"]");
			while(sendRewardCount<total/moneyTemp&&(allCount<=limit||limit==0)){
				sendGift(player);
				sendRewardCount++;
				allCount++;
			}
			getReward.put(player.accountId, new Integer(sendRewardCount));
			getReward.put(SAVELIMIT,new Integer(allCount));
		}
		
		if (Time.currTime > lastSaveTime + 600000) {
			save();
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
				p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01683, content, 0, item, count, "ACTV_EVERYCHARGE");
		// 记录日志
		LogUtil.logActivityRewards(p, activity);
	}

}
