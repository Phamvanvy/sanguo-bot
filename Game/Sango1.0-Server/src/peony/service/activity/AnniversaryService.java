package peony.service.activity;

import ch.javasoft.util.intcoll.IntHashMap;
import com.pip.util.Utils;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.net.Packet;
import peony.service.Service;

public class AnniversaryService implements Service {

	protected static int[] rewards = {4425, 4426, 4427, 4428, 4429, 4430, 4431}; //周年庆领取奖励
	
	protected static String[] rewardDes = new String[7]; //奖励描述
	
	/** 每个账号的领取奖励信息: int[0],领取的数量 int[1],最后一次领取的星期,int[2],最后一次领取的day */
	protected IntHashMap<int[]> accountGifts = new IntHashMap<int[]>();
	
	protected int lastSaveTime;
	
	protected static long GIFTTIME = 3600000;
	
	public void startup() throws Exception {
		initAnnGiftDes();
		loadAccountGiftsData();
	}
	
	protected static void initAnnGiftDes(){
		for(int i=0;i<rewards.length;i++){
			int itemId = rewards[i];
			GameItem item = null;
			try {item = ObjectAccessor.createGameItem(itemId);} catch (Exception e) {}
			rewardDes[i] = item==null ? "" : item.getDesc();
		}
	}
	
	/** 周年庆领取奖励 */
	public void getGift(int serial, Player player) throws Exception{
		if(hasGift(player))
			throw new Exception("同一账号每天只能领取一次");
//		if(!canGift(player))
//			throw new Exception("在线未达一小时不能领取奖励");
		int gifts = getGifts(player);
		gifts++;
		if(gifts>=8)
			gifts = 1;
		setGifts(player, gifts);
		setGiftWeekDay(player, getWeekDay());
		setGiftDay(player, getDay());
		int rewardItemId = getRewardItemId(player);
		PlayerTransaction tx = player.newTransaction("ANNGIFT");
		GameItem item = ObjectAccessor.createGameItem(rewardItemId);
		if(item!=null){
			try {
				player.bag.addGameItemComplete(item, 1, tx, false);
				tx.commit();
				player.message(-1, "恭喜您签到成功，今天的任务卷轴已发放到背包，请查收。", -1, -1);
			} catch (Exception e) {
				tx.rollback();
				Server.server.getServiceRegistry().getMailService().
					sendSystemMailAsync(player.id, peony.Messages.STRING_00004, "国庆签到奖励", "", 0, item, 1, "ANNGIFT");
				player.message(-1, "恭喜您签到成功，因背包已满，奖励已通过飞鸽发放，请查收。", -1, -1);
			}
		}else{
			tx.rollback();
		}
		Packet pt = new Packet(OpCode.CARD_PUNCH_SERVER);
		pt.putInt(serial);
		pt.put(getPlayerCurrentStar(player));
		player.send(pt);
		// 每10分钟保存一次记录
		if (Time.currTime > lastSaveTime + 600000) {
//			saveAccountGiftsData();
		}	
	}
	
	/** 周期为周二到下个周二,每周二清零 */
	public void updateAnniversaryData(Player player){
		int giftWeekDay = getGiftWeekDay(player);
		int giftDay = getGiftDay(player);
		int currentWeekDay = getWeekDay();
		int currentDay = getDay();
		if(currentDay<giftDay)
			giftDay = 0;
		if(currentDay-7>=giftDay){
			//连续7天未打卡清零
			setGifts(player, 0);
		}else if(currentWeekDay==2 && giftWeekDay!=2){
			setGifts(player, 0);
		}else if(giftWeekDay==1 && currentWeekDay!=1){
			setGifts(player, 0);
		}else if(currentWeekDay!=1 && giftWeekDay>currentWeekDay){
			setGifts(player, 0);
		}
	}
	
	protected int getGiftWeekDay(Player player){
		int accountId = player.accountId;
		try {
			return accountGifts.get(accountId)[1];
		} catch (Exception e) {
			return 0;
		}
	}
	
	protected int getGiftDay(Player player){
		int accountId = player.accountId;
		try {
			return accountGifts.get(accountId)[2];
		} catch (Exception e) {
			return 0;
		}
	}
	
	public boolean hasGift(Player player){
		int giftWeekDay = getGiftWeekDay(player);
		int giftDay = getGiftDay(player);
		int currentWeekDay = getWeekDay();
		int currentDay = getDay();
		if(giftWeekDay==currentWeekDay && giftDay==currentDay)
			return true;
		return false;
	}
	
	public boolean canGift(Player player){
		return player.onlineTimeToday>=GIFTTIME;
	}
	
	public int getGifts(Player player){
		int accountId = player.accountId;
		try {
			return accountGifts.get(accountId)[0];
		} catch (Exception e) {
			return 0;
		}
	}
	
	public void setGifts(Player player, int gifts){
		int[] arr = accountGifts.get(player.accountId);
		if(arr==null)
			arr = new int[3];
		arr[0] = gifts;
		accountGifts.put(player.accountId, arr);
	}
	
	public void setGiftWeekDay(Player player, int weekDay){
		int[] arr = accountGifts.get(player.accountId);
		if(arr==null)
			arr = new int[3];
		arr[1] = weekDay;
		accountGifts.put(player.accountId, arr);
	}
	
	public void setGiftDay(Player player, int day){
		int[] arr = accountGifts.get(player.accountId);
		if(arr==null)
			arr = new int[3];
		arr[2] = day;
		accountGifts.put(player.accountId, arr);
	}
	
	public int getRewardItemId(Player player){
		return rewards[getGifts(player)-1];
	}
	
	public byte getPlayerCurrentStar(Player player){
		int gifts = getGifts(player);
		switch(gifts){
		case 0:
			return 0;
		case 1:
			return 64;
		case 2:
			return 96;
		case 3:
			return 112;
		case 4:
			return 120;
		case 5:
			return 124;
		case 6:
			return 126;
		case 7:
			return 127;
		}
		return 0;
	}
	
	public static String[] getGiftsDes(){
		return rewardDes;
	}
	
	public static int getWeekDay(){
		int weekDay = 0;
		weekDay = Time.currentWeekDay;
		weekDay--;
		weekDay = weekDay==0 ? 7 : weekDay;
		return weekDay;
	}
	
	public static int getDay(){
		int day = 0;
		day = Time.currentDayOfYear;
		return day;
	}
	
	public void loadAccountGiftsData(){
		try {
			Database db = Server.server.getServiceRegistry().getSleepyCatService().anniversaryDB;
			Cursor cursor = null;
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            try {
	            cursor = db.openCursor(null, new CursorConfig());
	            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
	            	int accountId = IntegerBinding.entryToInt(keyEntry);
	            	String value = StringBinding.entryToString(dataEntry);
	            	int[] arr = Utils.stringToIntArray(value, ',');
	            	accountGifts.put(accountId, arr);
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
		} 
		lastSaveTime = Time.currTime;
	}
	
	public void saveAccountGiftsData(){
		Database db = Server.server.getServiceRegistry().getSleepyCatService().anniversaryDB;
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		for(int accountId : accountGifts.keySet()){
			IntegerBinding.intToEntry(accountId, key);
			StringBinding.stringToEntry(Utils.intArrayToString(accountGifts.get(accountId), ','), data);
			try {
				db.put(null, key, data);
			} catch (DatabaseException e) {
			}
		}
		lastSaveTime = Time.currTime;
	}
	
	public void shutdown() {
		saveAccountGiftsData();
	}

}
