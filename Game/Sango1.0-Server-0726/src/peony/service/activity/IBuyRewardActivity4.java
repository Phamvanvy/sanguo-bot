package peony.service.activity;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.util.Utils;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.chat.ChatService;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;
import ch.javasoft.util.intcoll.IntHashMap;

public class IBuyRewardActivity4 implements IActivityImpl, ServiceEventListener {

	private static Logger log = Logger.getLogger(IBuyRewardActivity4.class);
	private static Random rnd = new Random();
	private List<IBuyReward> ibuyreward = new ArrayList<IBuyReward>();
	//list里为按照IBuyReward.id顺序记录的购买物品个数
	protected IntHashMap<List<Integer>> ibuynum = new IntHashMap<List<Integer>>(); 

	protected Activity activity;

	public IBuyRewardActivity4(Activity owner) {
		this.activity = owner;
	}

	public Activity getActivity() {
		return activity;
	}
	
	public String getDBName() {
		return "IBUYREWARDACTIVITY" + activity.getId();
	}
	
	// 上次保存的时间
	protected int lastSaveTime;

	public void load() {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("ibuyreward.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(
					bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
		
		SleepyCatService dbservice = Server.server.getServiceRegistry()
		.getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
			Cursor cursor = null;
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			try {
				cursor = db.openCursor(null, new CursorConfig());
				while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
					int playerId = Integer.parseInt(StringBinding
							.entryToString(keyEntry));
					String value = StringBinding.entryToString(dataEntry);
					int[] arr = Utils.stringToIntArray(value, ',');
					List<Integer> list = new ArrayList<Integer>();
					for (int i = 0; i < arr.length; i++) {
						list.add(arr[i]);
					}
					ibuynum.put(playerId, list);
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
		SleepyCatService dbservice = Server.server.getServiceRegistry()
		.getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			for (int playerid :ibuynum.keySet()) {
				StringBinding.stringToEntry(String.valueOf(playerid), keyEntry);
				List<Integer> list = ibuynum.get(playerid);
				int arr[] = new int[list.size()];
				for (int i = 0; i < list.size(); i++) {
					arr[i] = list.get(i);
				}
				StringBinding.stringToEntry(Utils.intArrayToString(arr, ','),
						dataEntry);
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

	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		if (root != null) {
			List ib = root.elements("ibuy");
			for (int i = 0; i < ib.size(); i++) {
				int id = Integer.parseInt(((Element) ib.get(i))
						.attributeValue("id"));
				String itemIds = ((Element) ib.get(i))
						.attributeValue("itemids");
				int sum = Integer.parseInt(((Element) ib.get(i))
						.attributeValue("sum"));
				int rewardItemId = Integer.parseInt(((Element) ib.get(i))
						.attributeValue("rewarditemid"));
				int count = Integer.parseInt(((Element) ib.get(i))
						.attributeValue("count"));
				int rate = Integer.parseInt(((Element) ib.get(i))
						.attributeValue("rate"));
				IBuyReward ibr = new IBuyReward(id, sum, rewardItemId, count,
						rate);
				String[] items = itemIds.split(",");
				if (items != null && items.length != 0) {
					for (int j = 0; j < items.length; j++) {
						ibr.addItemIds(Integer.parseInt(items[j]));
					}
				}
				ibuyreward.add(ibr);
			}
		}
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_IBUY };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_IBUY:
			playerIBuyOK(((Integer) event.param1).intValue(),
					((Integer) event.param2).intValue(),
					((Integer) event.param3).intValue(),
					((Integer) event.param4).intValue());
			break;
		}
	}

	public void playerIBuyOK(int playerid, int money, int itemId, int count) {
		Player p = (Player) ObjectAccessor.getGameObject(playerid);
		if (p != null) {
			IBuyReward ibr = getIBuyRewardByItemid(itemId);
			if (ibr != null) {
				List<Integer> list = ibuynum.get(p.id);
				if (list != null) {
					if(list.size() < ibr.id+1){
						for(int i=list.size();i<ibr.id+1;i++){
							list.add(0);
						}
					}
					int cnt = list.get(ibr.id);
					if (cnt != 0)
						count += cnt;
				} else {
					list = new ArrayList<Integer>();
					for(int i=0;i<ibuyreward.size();i++){
						list.add(0);
					}
				}
				list.set(ibr.id, count);
				int c = 0;
				while (count / ibr.sum != 0) {
					c += sendReward(p, itemId, ibr);
					count -= ibr.sum;
				}
				list.set(ibr.id, count);
				ibuynum.put(p.id, list);
				GameItem item = ObjectAccessor.createGameItem(itemId);
		        GameItem rewardItem = ObjectAccessor.createGameItem(ibr.rewardItemId);
		        if(c > 0){
					String content = MessageFormat.format(
							"恭喜你在元宝商店购买{0}时获得{1}个{2}的奖励。",
							item.template.name, c,
							rewardItem.template.name);
					Server.server.getServiceRegistry().getChatService()
		               .sendPrivateMessage(p.id, content);
		        }
			}
		}
		// 每10分钟保存一次记录
		if (Time.currTime > lastSaveTime + 600000) {
			save();
		}
	}

	public IBuyReward getIBuyRewardByItemid(int itemId) {
		if (ibuyreward != null && ibuyreward.size() > 0) {
			for (IBuyReward ibr : ibuyreward) {
				List<Integer> itemIds = ibr.itemIds;
				for (int i = 0; i < itemIds.size(); i++) {
					if (itemIds.get(i) == itemId)
						return ibr;
				}
			}
		}
		return null;
	}

	public int sendReward(Player p, int itemId, IBuyReward ibr) {
		if (p != null) {
			int r = rnd.nextInt(100);
			if (r < ibr.rate) {
				GameItem item = ObjectAccessor
				        .createGameItem(itemId);
				GameItem rewardItem = ObjectAccessor
						.createGameItem(ibr.rewardItemId);
				int count = ibr.count;
				PlayerTransaction tx = p.newTransaction("IBUYREWARD");
				try {
					p.bag.addGameItemComplete(rewardItem, count, tx, true);
					tx.commit();
				} catch (Exception e) {
					tx.rollback();
					String content = MessageFormat.format(
							"恭喜你在元宝商店购买{0}时获得{1}个{2}的奖励。",
							item.template.name, count,
							rewardItem.template.name);
					Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(p.id, "系统", "消费奖励", content, 0,
									rewardItem, count, "IBUYREWARD");
				}
				return ibr.count;
			}
		}
		return 0;
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry()
		.getSleepyCatService();
        dbservice.removeDatabase(getDBName());
	}
}

class IBuyReward {
	int id;
	int sum;
	int rewardItemId;
	int count;
	int rate;
	List<Integer> itemIds = new ArrayList<Integer>();

	public IBuyReward(int id, int sum, int rewardItemId, int count, int rate) {
		this.id = id;
		this.sum = sum;
		this.rewardItemId = rewardItemId;
		this.count = count;
		this.rate = rate;
	}

	public void addItemIds(int itemId) {
		itemIds.add(itemId);
	}
}
