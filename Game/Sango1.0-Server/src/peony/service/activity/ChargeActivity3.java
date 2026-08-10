package peony.service.activity;

import ch.javasoft.util.intcoll.IntHashMap;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;

public class ChargeActivity3 implements IActivityImpl, ServiceEventListener {

protected Activity activity;
	
	protected IntHashMap<Integer> playerCharge = new IntHashMap<Integer>();
	
	protected IntHashMap<Integer> getReward = new IntHashMap<Integer>();
	
	protected int GIFT_MONEY = 0;
	
	protected int GIFT_ITEMID = 0;
	
	protected int MAXREWARDTIME = 0;
	
	// 上次保存的时间
	private int lastSaveTime;
	
	public ChargeActivity3(Activity owner){
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
		return "ChargeActivity3" + activity.getId();
	}

	public void load() {
		String config = activity.configData;
		parseConfig(config);
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
	            	playerCharge.put(accountId, new Integer(value));
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
	
	protected void parseConfig(String config){
		String[] str0 = config.split(";");
		for(String str1 : str0){
			String[] str = str1.split(":");
			if(str[0].equals("item")){
				this.GIFT_ITEMID = Integer.parseInt(str[1]);
			}else if(str[0].equals("max")){
				this.MAXREWARDTIME = Integer.parseInt(str[1]);
			}else if(str[0].equals("money")){
				this.GIFT_MONEY = Integer.parseInt(str[1]);
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
            for (int accountId : playerCharge.keySet()) {
            	StringBinding.stringToEntry(String.valueOf(accountId), keyEntry);
            	int value = playerCharge.get(accountId);
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
				ServiceEvent.EVENT_CHARGE_SUCCESS,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_CHARGE_SUCCESS:
			processPlayerCharge((Player)event.param1, (Integer)event.param2);
			break;
		}
	}
	
	protected void processPlayerCharge(Player p, int money){
		if(p!=null){
			if(p!=null){
				int accountId = p.accountId;
				if(playerCharge.get(accountId)==null){
					playerCharge.put(accountId, new Integer(money));
				}else{
					int leftMoney = playerCharge.get(accountId);
					playerCharge.put(accountId, new Integer(money+leftMoney));
				}
				int chargeMoney = playerCharge.get(accountId);
				if(chargeMoney>=GIFT_MONEY && (getReward.get(p.accountId)==null || getReward.get(p.accountId).intValue()<=MAXREWARDTIME)){
					sendGift(p, GIFT_ITEMID, chargeMoney);
					playerCharge.put(accountId, new Integer(chargeMoney%GIFT_MONEY));
				}
			}
		}
		if (Time.currTime > lastSaveTime + 600000) {
			save();
		}
	}
	
	protected void sendGift(Player p, int itemId, int money){
		if(p!=null){
			int count = money/GIFT_MONEY;
			GameItem item = ObjectAccessor.createGameItem(GIFT_ITEMID);
			String content = peony.Messages.STRING_01682;
			Server.server.getServiceRegistry().getMailService().sendSystemMail(
				p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01683, peony.Messages.STRING_01684
				, 0, item, count, "ACTV");
			p.message(-1, content, -1, -1);
			if(getReward.get(p.accountId)==null){
				getReward.put(p.accountId, new Integer(1));
			}else{
				int old = getReward.get(p.accountId);
				getReward.put(p.accountId, new Integer(old+1));
			}
		}
	}

}
