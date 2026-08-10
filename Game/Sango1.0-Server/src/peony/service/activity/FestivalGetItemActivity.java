package peony.service.activity;

import java.util.ArrayList;
import java.util.List;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.service.sleepycat.SleepyCatService;

public class FestivalGetItemActivity implements IActivityImpl{
	
	private static List<Integer> playerHasGifted = new ArrayList<Integer>();
	private static int itemId;
	private static int itemCount;
	private static int LEVEL_LIMIT = 50;

    private Activity activity;
    
 // 上次保存的时间
	private int lastSaveTime;
	
	public FestivalGetItemActivity(Activity owner){
		this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	private String getDBName() {
		return "FestivalGetItemActivity" + activity.getId();
	}

    public static void getReward(Player player) throws Exception{
    	if(playerHasGifted.contains(player.id)){
    		throw new Exception("节日礼物每人只有一份，可不要太贪心哦");
    	}
    	if(player.level<LEVEL_LIMIT){
    		throw new Exception("50级以下没有礼物哦，赶快升级去吧");
    	}
    	GameItem rewardItem = ObjectAccessor.createGameItem(itemId);
    	if(rewardItem != null && itemCount > 0){
    		PlayerTransaction tx = player.newTransaction("SALARY");
			try {
				player.bag.addGameItemComplete(rewardItem, itemCount, tx, true);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				Server.server.getServiceRegistry().getMailService()
						.sendSystemMail(player.id, peony.Messages.STRING_00004, "节日活动奖励", "节日活动奖励", 0,rewardItem, itemCount, "SALARY");
			}
    	}
    	playerHasGifted.add(player.id);
    }

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
	            	int playerId = Integer.parseInt(StringBinding.entryToString(keyEntry));
//	            	int value = Integer.parseInt(StringBinding.entryToString(dataEntry));
	            	playerHasGifted.add(playerId);
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
            for (int playerId : playerHasGifted) {
            	StringBinding.stringToEntry(String.valueOf(playerId), keyEntry);
            	int value = 1;
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
		
		
	}

	public void startup() throws Exception {
		String config = activity.getConfigData();
		if (config != null) {
			String[] str = config.split(",");
			itemId = Integer.parseInt(str[0]);
			itemCount = Integer.parseInt(str[1]);
		}
	}

    public void clear() {
    	SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		dbservice.removeDatabase(getDBName());
	}

	

}
