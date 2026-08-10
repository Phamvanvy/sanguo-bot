package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.Server;
import peony.service.sleepycat.SleepyCatService;
import com.pip.util.Utils;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;

public class CardPunchActivity implements IActivityImpl{
	private static Logger log = Logger.getLogger(CardPunchActivity.class);
	protected Activity activity;
	public CardPunchActivity(Activity owner) {
		this.activity = owner;
	}
	public String getDBName(){
		return "CARDPUNCHACTIVITY"+activity.getId();
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	public void loadAccountGiftsData(){
		try {
			SleepyCatService dbservice = Server.server.getServiceRegistry()
			.getSleepyCatService();
	        Database db = null;
		    db = dbservice.openDatabase(getDBName());
			Cursor cursor = null;
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            try {
	            cursor = db.openCursor(null, new CursorConfig());
	            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
	            	int accountId = IntegerBinding.entryToInt(keyEntry);
	            	String value = StringBinding.entryToString(dataEntry);
	            	int[] arr = Utils.stringToIntArray(value, ',');
	            	CardPunchActService service = Server.server.getServiceRegistry().getCardPunchActService();
	            	service.addAccountGifts(accountId, arr);
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
//		lastSaveTime = Time.currTime;
	}
	
	public void saveAccountGiftsData(){
		SleepyCatService dbservice = Server.server.getServiceRegistry()
		.getSleepyCatService();
        Database db = null;
        try {
		    db = dbservice.openDatabase(getDBName());
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			CardPunchActService service = Server.server.getServiceRegistry().getCardPunchActService();
			for(int accountId : service.accountGifts.keySet()){
				IntegerBinding.intToEntry(accountId, key);
				StringBinding.stringToEntry(Utils.intArrayToString(service.accountGifts.get(accountId), ','), data);
				try {
					db.put(null, key, data);
				} catch (DatabaseException e) {
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
//		lastSaveTime = Time.currTime;
	}
	
	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry()
		.getSleepyCatService();
        dbservice.removeDatabase(getDBName());
	}

	

	public void load() {
		loadAccountGiftsData();
		
	}

	public void save() {
		saveAccountGiftsData();	
	}

	public void shutdown() {
		
		
	}

	public void startup() throws Exception {
		
	}

}
