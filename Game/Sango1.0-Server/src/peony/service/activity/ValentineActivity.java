package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import peony.game.CycleListener;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.service.sleepycat.SleepyCatService;
import com.pip.util.Utils;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

/**
 * 情人节活动
 * @author dchen
 */
public class ValentineActivity implements IActivityImpl, CycleListener {

	public Activity activity;
	
	public int[] receiveTopPlayerIds = new int[10]; //收花top排行榜
	public int[] sendTopPlayerIds = new int[10]; //送花top排行榜

	public HashMap<Integer, Integer> playerFactions = new HashMap<Integer, Integer>(); //参与者国别
	public HashMap<Integer, String> playerNames = new HashMap<Integer, String>(); //参与者名字
	
	public HashMap<Integer, Integer> receivePlayerCounts = new HashMap<Integer, Integer>(); //各个玩家收花数量
	public HashMap<Integer, Integer> sendPlayerCounts = new HashMap<Integer, Integer>(); //各个玩家送花数量
	
	public HashMap<Integer, Integer> tempGiftStack = new HashMap<Integer, Integer>();
	
	public List<Integer> reciveRewardPlayers1 = new ArrayList<Integer>();
	public List<Integer> reciveRewardPlayers2 = new ArrayList<Integer>();
	public List<Integer> sendRewardPlayers1 = new ArrayList<Integer>();
	public List<Integer> sendRewardPlayers2 = new ArrayList<Integer>();
	
	public static String RANKTYPE_RECEIVE = "RECEICE"; //收花排行方式
	public static String RANKTYPE_SEND = "SEND"; //送花排行方式
	
	protected ActivitySchedule schedule = new ActivitySchedule(); //揭榜时间
	
	// 上次保存的时间
	private int lastSaveTime;
	public static int saveDistance = 600000;
	
	public static int GAINEXP = 150;
	public static int GAINCREDIT = 1;
	
	private static final Logger log = Logger.getLogger(ValentineActivity.class);

	public ValentineActivity(Activity activity) {
		this.activity = activity;
	}

	public void startup() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.setTime(schedule.stopTime);
		cal.add(Calendar.MINUTE, 1);
		new Timer().schedule(new TimerTask(){
			public void run() {
				for(int playerId : receiveTopPlayerIds){
					log.info("[VALENTINE_RECEIVE]ID[" + playerId + "]");
				}
				for(int playerId : sendTopPlayerIds){
					log.info("[VALENTINE_SEND]ID[" + playerId + "]");
				}
			}
		}, cal.getTime());
	}
	
	private String getDBName() {
		return "ValentineActivity" + activity.getId();
	}

	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		dbservice.removeDatabase(getDBName());
	}

	public Activity getActivity() {
		return this.activity;
	}

	public void load() {
		try {
			schedule.parse(activity.configData);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
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
	            	String key = StringBinding.entryToString(keyEntry);
	            	if(key.startsWith("name:")){
	            		int playerId = Integer.parseInt(key.substring(5));
	            		String value = StringBinding.entryToString(dataEntry);
	            		playerNames.put(playerId, value);
	            	}else if(key.startsWith("fac:")){
	            		int playerId = Integer.parseInt(key.substring(4));
	            		int value = Integer.parseInt(StringBinding.entryToString(dataEntry));
	            		playerFactions.put(playerId, value);
	            	}else if(key.startsWith("recount:")){
	            		int playerId = Integer.parseInt(key.substring(8));
	            		int value = Integer.parseInt(StringBinding.entryToString(dataEntry));
	            		receivePlayerCounts.put(playerId, value);
	            	}else if(key.startsWith("sencount:")){
	            		int playerId = Integer.parseInt(key.substring(9));
	            		int value = Integer.parseInt(StringBinding.entryToString(dataEntry));
	            		sendPlayerCounts.put(playerId, value);
	            	}else if(key.startsWith("retop:")){
	            		String value = StringBinding.entryToString(dataEntry);
	            		receiveTopPlayerIds = Utils.stringToIntArray(value, ',');
	            	}else if(key.startsWith("sentop:")){
	            		String value = StringBinding.entryToString(dataEntry);
	            		sendTopPlayerIds = Utils.stringToIntArray(value, ',');
	            	}else if(key.startsWith("recreward1:")){
	            		String value = StringBinding.entryToString(dataEntry);
	            		int[] arr= Utils.stringToIntArray(value, ',');
	            		for(int playerId : arr)
	            			reciveRewardPlayers1.add(playerId);
	            	}else if(key.startsWith("recreward2:")){
	            		String value = StringBinding.entryToString(dataEntry);
	            		int[] arr= Utils.stringToIntArray(value, ',');
	            		for(int playerId : arr)
	            			reciveRewardPlayers2.add(playerId);
	            	}else if(key.startsWith("senreward1:")){
	            		String value = StringBinding.entryToString(dataEntry);
	            		int[] arr= Utils.stringToIntArray(value, ',');
	            		for(int playerId : arr)
	            			sendRewardPlayers1.add(playerId);
	            	}else if(key.startsWith("senreward2:")){
	            		String value = StringBinding.entryToString(dataEntry);
	            		int[] arr= Utils.stringToIntArray(value, ',');
	            		for(int playerId : arr)
	            			sendRewardPlayers2.add(playerId);
	            	}
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

	public void save() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            for (int playerId : playerFactions.keySet()) {
            	StringBinding.stringToEntry("fac:" + String.valueOf(playerId), keyEntry);
            	int value = playerFactions.get(playerId);
            	StringBinding.stringToEntry(String.valueOf(value), dataEntry);
            	db.put(null, keyEntry, dataEntry);
            }
            for (int playerId : playerNames.keySet()) {
            	StringBinding.stringToEntry("name:" + String.valueOf(playerId), keyEntry);
            	String value = playerNames.get(playerId);
            	StringBinding.stringToEntry(String.valueOf(value), dataEntry);
            	db.put(null, keyEntry, dataEntry);
            }
            for (int playerId : receivePlayerCounts.keySet()) {
            	StringBinding.stringToEntry("recount:" + String.valueOf(playerId), keyEntry);
            	int value = receivePlayerCounts.get(playerId);
            	StringBinding.stringToEntry(String.valueOf(value), dataEntry);
            	db.put(null, keyEntry, dataEntry);
            }
            for (int playerId : sendPlayerCounts.keySet()) {
            	StringBinding.stringToEntry("sencount:" + String.valueOf(playerId), keyEntry);
            	int value = sendPlayerCounts.get(playerId);
            	StringBinding.stringToEntry(String.valueOf(value), dataEntry);
            	db.put(null, keyEntry, dataEntry);
            }
            StringBinding.stringToEntry("retop:", keyEntry);
        	StringBinding.stringToEntry(Utils.intArrayToString(receiveTopPlayerIds, ','), dataEntry);
        	db.put(null, keyEntry, dataEntry);
        	StringBinding.stringToEntry("sentop:", keyEntry);
        	StringBinding.stringToEntry(Utils.intArrayToString(sendTopPlayerIds, ','), dataEntry);
        	db.put(null, keyEntry, dataEntry);
        	StringBinding.stringToEntry("recreward1:", keyEntry);
        	StringBinding.stringToEntry(Utils.intListToString(reciveRewardPlayers1, ','), dataEntry);
        	db.put(null, keyEntry, dataEntry);
        	StringBinding.stringToEntry("recreward2:", keyEntry);
        	StringBinding.stringToEntry(Utils.intListToString(reciveRewardPlayers2, ','), dataEntry);
        	db.put(null, keyEntry, dataEntry);
        	StringBinding.stringToEntry("senreward1:", keyEntry);
        	StringBinding.stringToEntry(Utils.intListToString(sendRewardPlayers1, ','), dataEntry);
        	db.put(null, keyEntry, dataEntry);
        	StringBinding.stringToEntry("senreward2:", keyEntry);
        	StringBinding.stringToEntry(Utils.intListToString(sendRewardPlayers2, ','), dataEntry);
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
		lastSaveTime = Time.currTime;
	}
	
	/** 送花 */
	public void sendFlowers(Player sender, int targetId, int targetLevel, String targetName, int tarFaction, int count) throws Exception {
		if(sender!=null){
			if(!schedule.in())
				throw new Exception("现在不是送花时间");
			MailService mailService = Server.server.getServiceRegistry().getMailService();
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			int senderPlayerId = sender.id;
			String senderPlayerName = sender.name;
			int senderFaction = sender.faction;
			int oldCount1 = 0;
			try {
				oldCount1 = sendPlayerCounts.get(senderPlayerId);
			} catch (Exception e) {
			}
			sendPlayerCounts.put(senderPlayerId, oldCount1+count);
			refreshTops(sendTopPlayerIds, senderPlayerId, oldCount1+count, RANKTYPE_SEND);
			if(oldCount1+count>=99 && oldCount1+count<999 && !sendRewardPlayers1.contains(senderPlayerId)){
//				GameItem item = ObjectAccessor.createGameItem(4094);
//				mailService.sendSystemMailAsync(senderPlayerId, "系统", item.template.name, 
//						"恭喜您送出玫瑰花数目累计到 99个证明您人缘极佳，热情洋溢。", 0, item, 1, "VALENTINE");
				sendRewardPlayers1.add(senderPlayerId);
			}
			if(oldCount1+count>=999 && !sendRewardPlayers2.contains(senderPlayerId)){
//				GameItem item = ObjectAccessor.createGameItem(4095);
//				mailService.sendSystemMailAsync(senderPlayerId, "系统", item.template.name, 
//						"恭喜您送出玫瑰花数目累计到 999个证明您交游广阔，风流倜傥。", 0, item, 1, "VALENTINE");
				sendRewardPlayers2.add(senderPlayerId);
			}
			PlayerTransaction tx = sender.newTransaction("VALENTINE");
			try {
				sender.addExp(sender.level * count * GAINEXP, tx, true);
				if(count>1)
					sender.addCredit(count * GAINCREDIT, tx, true);
				tx.commit();
			} catch (Exception e1) {
				tx.rollback();
			}
			int oldCount2 = 0;
			try {
				oldCount2 = receivePlayerCounts.get(targetId);
			} catch (Exception e) {
			}
			receivePlayerCounts.put(targetId, oldCount2+count);
			refreshTops(receiveTopPlayerIds, targetId, oldCount2+count, RANKTYPE_RECEIVE);
			if(oldCount2+count>=99 && oldCount2+count<999 && !reciveRewardPlayers1.contains(targetId)){
//				GameItem item = ObjectAccessor.createGameItem(4089);
//				mailService.sendSystemMailAsync(targetId, "系统", item.template.name, 
//						"恭喜您得到玫瑰花数目累计到 99个证明您的魅力惊人。", 0, item, 1, "VALENTINE");
				reciveRewardPlayers1.add(targetId);
			}
			if(oldCount2+count>=999 && !reciveRewardPlayers2.contains(targetId)){
//				GameItem item = ObjectAccessor.createGameItem(4090);
//				mailService.sendSystemMailAsync(targetId, "系统", item.template.name, 
//						"恭喜您得到玫瑰花数目累计到 999个证明您的魅力无穷。", 0, item, 1, "VALENTINE");
				reciveRewardPlayers2.add(targetId);
			}
			if(playerNames.get(targetId)==null)
				playerNames.put(targetId, targetName);
			if(playerNames.get(senderPlayerId)==null)
				playerNames.put(senderPlayerId, senderPlayerName);
			playerFactions.put(senderPlayerId, senderFaction);
			playerFactions.put(targetId, tarFaction);
			if(count>=99){
				chatService.sendWorldMessage(MessageFormat
						.format("{0}收到来自{1}赠送的{2}朵玫瑰,ta现在是天下最幸福的人", targetName, senderPlayerName, count));
			}
			log.info("[VALENTINEFLOWER]"+LogUtil.getPlayerLogString(sender)+"DESTPLAYER["+targetId+"]TARFACTION["+tarFaction+"]COUNT["+count+"]");
			if(Time.currTime-lastSaveTime>saveDistance)
				save();
			int oldC = 0;
			try{oldC = tempGiftStack.get(senderPlayerId);}catch(Exception e){}
			int currC = oldC + count;
			int giftC = currC / 5;
			int leave = currC % 5;
			if(giftC>0){
				PlayerTransaction transaction = sender.newTransaction("QIXI");
				GameItem gameItem = ObjectAccessor.createGameItem(1183);
				try {
					sender.bag.addGameItemComplete(gameItem, giftC, transaction, true);
					transaction.commit();
				} catch (Exception e) {
					transaction.rollback();
					if(giftC>99){
						int c = giftC / 99;
						int l = giftC % 99;
						for(int i=0;i<c;i++){
							mailService.sendSystemMailAsync(senderPlayerId, "系统", "予人玫瑰，手有余香", 
									"亲爱的玩家，您于情人节互赠玫瑰花活动中获得回馈一合酥奖励，请尽快提取。予人玫瑰，手有余香，赠与他人5朵玫瑰后，可获得一个一合酥哦~", 
									0, gameItem, 99, "QIXI");
						}
						if(l>0){
							mailService.sendSystemMailAsync(senderPlayerId, "系统", "予人玫瑰，手有余香", 
									"亲爱的玩家，您于情人节互赠玫瑰花活动中获得回馈一合酥奖励，请尽快提取。予人玫瑰，手有余香，赠与他人5朵玫瑰后，可获得一个一合酥哦~", 
									0, gameItem, l, "QIXI");
						}
					}else{
						mailService.sendSystemMailAsync(senderPlayerId, "系统", "予人玫瑰，手有余香", 
								"亲爱的玩家，您于情人节互赠玫瑰花活动中获得回馈一合酥奖励，请尽快提取。予人玫瑰，手有余香，赠与他人5朵玫瑰后，可获得一个一合酥哦~", 
								0, gameItem, giftC, "QIXI");
					}
				}
				
			}
			tempGiftStack.put(senderPlayerId, leave);
		}
	}
	
	private void refreshTops(int[] arr, int playerId, int count, String flag){
		boolean topChanged = false;
		boolean hasOn = false;
		for(int i=0;i<arr.length;i++){
			int count1 = receivePlayerCounts.get(arr[i])==null ? 0 : receivePlayerCounts.get(arr[i]);
			int count2 = sendPlayerCounts.get(arr[i])==null ? 0 : sendPlayerCounts.get(arr[i]);
			if(flag.equalsIgnoreCase(RANKTYPE_RECEIVE) && count1<count 
					|| flag.equalsIgnoreCase(RANKTYPE_SEND) && count2<count)
				topChanged = true;
			if(arr[i]==playerId)
				hasOn = true;
		}
		if(!hasOn && topChanged){
			bubbleArr(arr, flag);
			arr[9] = playerId;
		}
	}
	
	public void bubbleArr(int[] arr, String flag){
		for(int i=0;i<arr.length;i++){
			for(int j=i+1;j<arr.length;j++){
				int playerId1 = arr[i];
				int playerId2 = arr[j];
				int count1 = 0, count2 = 0;
				if(flag.equalsIgnoreCase(RANKTYPE_RECEIVE)){
					count1 = receivePlayerCounts.get(playerId1)==null ? 0 : receivePlayerCounts.get(playerId1);
					count2 = receivePlayerCounts.get(playerId2)==null ? 0 : receivePlayerCounts.get(playerId2);
				}else if(flag.equalsIgnoreCase(RANKTYPE_SEND)){
					count1 = sendPlayerCounts.get(playerId1)==null ? 0 : sendPlayerCounts.get(playerId1);
					count2 = sendPlayerCounts.get(playerId2)==null ? 0 : sendPlayerCounts.get(playerId2);
				}
				if(count1<count2){
					int temp = arr[i];
					arr[i] = arr[j];
					arr[j] = temp;
				}
			}
		}
	}
	
	public List<IncompletePlayer> getTopList(Player player, String type){
		if(player!=null){
			List<IncompletePlayer> list = new ArrayList<IncompletePlayer>();
			if(type.equalsIgnoreCase(RANKTYPE_SEND)){
				bubbleArr(sendTopPlayerIds, RANKTYPE_SEND);
				for(int playerId : sendTopPlayerIds){
					if(playerId>0){
						IncompletePlayer inComPlayer = getIncompletePlayerInfo(playerId, RANKTYPE_SEND);
						if(inComPlayer!=null)
							list.add(inComPlayer);
					}
				}
			}
			if(type.equalsIgnoreCase(RANKTYPE_RECEIVE)){
				bubbleArr(receiveTopPlayerIds, RANKTYPE_RECEIVE);
				for(int playerId : receiveTopPlayerIds){
					if(playerId>0){
						IncompletePlayer inComPlayer = getIncompletePlayerInfo(playerId, RANKTYPE_RECEIVE);
						if(inComPlayer!=null)
							list.add(inComPlayer);
					}
				}
			}
			return list;
		}
		return null;
	}
	
	public IncompletePlayer getIncompletePlayerInfo(int playerId, String type) {
		IncompletePlayer player = null;
		try {
			if(type.equalsIgnoreCase(RANKTYPE_SEND)){
				player = new IncompletePlayer(playerId, playerNames.get(playerId), 
						playerFactions.get(playerId), sendPlayerCounts.get(playerId));
			}else if(type.equalsIgnoreCase(RANKTYPE_RECEIVE)){
				player = new IncompletePlayer(playerId, playerNames.get(playerId), 
						playerFactions.get(playerId), receivePlayerCounts.get(playerId));
			}
		} catch (Exception e) {
			log.info("NO PLAYER VALENTIME INFO" + playerId);
		}
		return player;
	}

	public void shutdown() {
		save();
	}

	public boolean update(int diff) {
		return false;
	}

}

class IncompletePlayer{
	
	public int playerId;
	public String name;
	public int faction;
	public int count;
	
	public IncompletePlayer(int playerId, String name, int faction, int count) {
		super();
		this.playerId = playerId;
		this.name = name;
		this.faction = faction;
		this.count = count;
	}
}
