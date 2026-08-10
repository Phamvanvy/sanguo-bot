package peony.service.activity;

import java.text.MessageFormat;
import ch.javasoft.util.intcoll.IntHashMap;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.RecordChargeService;
import peony.service.sleepycat.SleepyCatService;

public class ChargeActivity4 implements IActivityImpl, ServiceEventListener {

	protected Activity activity;
	
	protected IntHashMap<Integer> playerHasGifted = new IntHashMap<Integer>();
	
	protected static int GIFT_MONEY = 500;
	
	protected static int GIFT_ITEMID = 490;
	
	// 上次保存的时间
	private int lastSaveTime;
	
	public ChargeActivity4(Activity owner){
		this.activity = owner;
	}

	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		dbservice.removeDatabase(getDBName());
	}

	public Activity getActivity() {
		return null;
	}
	
	private String getDBName() {
		return "ChargeActivity4" + activity.getId();
	}

	public void load() {
		parseConfig();
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
	            	int accountId = Integer.parseInt(StringBinding.entryToString(keyEntry));
	            	int value = Integer.parseInt(StringBinding.entryToString(dataEntry));
	            	playerHasGifted.put(accountId, new Integer(value));
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
	
	protected void parseConfig(){
		String config = activity.configData;
		String[] str0 = config.split(";");
		for(String str1 : str0){
			String[] str = str1.split(":");
			if(str[0].equals("money")){
				GIFT_MONEY = Integer.parseInt(str[1]);
			}else if(str[0].equals("item")){
				GIFT_ITEMID = Integer.parseInt(str[1]);
			}
		}
	}

	public void save() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            for (int accountId : playerHasGifted.keySet()) {
            	StringBinding.stringToEntry(String.valueOf(accountId), keyEntry);
            	int value = playerHasGifted.get(accountId);
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

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
				ServiceEvent.EVENT_CHARGE_SUCCESS,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerLoad((Player)event.param1);
			break;
		case ServiceEvent.EVENT_CHARGE_SUCCESS:
			processPlayerCharge((Player)event.param1, (Integer)event.param2);
			break;
		}
	}
	
	protected void processPlayerCharge(Player p, int money){
		if(p!=null){
			RecordChargeService service = Server.server.getServiceRegistry().getRecordChargeService();
			int accountId = p.accountId;
			int chargeMoney = service.getChargeAfter(accountId, activity.schedule.startTime);
			if(chargeMoney>=GIFT_MONEY && (playerHasGifted.get(accountId)==null || playerHasGifted.get(accountId).intValue()==0)){
				playerHasGifted.put(accountId, new Integer(GIFT_ITEMID));
				sendGift(p, GIFT_ITEMID, 1);
			}
		}
	}
	
	protected void processPlayerLoad(Player p){
		if(p!=null){
			RecordChargeService service = Server.server.getServiceRegistry().getRecordChargeService();
			int accountId = p.accountId;
			int chargeMoney = service.getChargeAfter(accountId, activity.schedule.startTime);
			if(chargeMoney>=GIFT_MONEY && (playerHasGifted.get(accountId)==null || playerHasGifted.get(accountId).intValue()==0)){
				playerHasGifted.put(accountId, new Integer(GIFT_ITEMID));
				sendGift(p, GIFT_ITEMID, 1);
			}
		}
		// 每10分钟保存一次记录
		if (Time.currTime > lastSaveTime + 600000) {
			save();
		}
	}
	
	protected void sendGift(Player p, int itemId, int count){
		if(p!=null){
			GameItem item = ObjectAccessor.createGameItem(GIFT_ITEMID);
			PlayerTransaction tx = p.newTransaction("ACTV_CHARGE");
			Gain gain = new Gain(p);
			gain.addGainItem(item, 1);
			try {
				p.addGainComplete(gain, tx, true);
				tx.commit();
				String content = MessageFormat.format(peony.Messages.STRING_01344, p.name, GIFT_MONEY);
				p.message(-1, content, -1, -1);
			} catch (Exception e) {
				tx.rollback();
				String content = MessageFormat.format(peony.Messages.STRING_01345, p.name, GIFT_MONEY);
				Server.server.getServiceRegistry().getMailService().sendSystemMail(
					p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01346, content, 0, item, 1, "ACTV");
				content = MessageFormat.format(peony.Messages.STRING_01347, p.name, GIFT_MONEY);
				p.message(-1, content, -1, -1);
			}
		}
	}

}
