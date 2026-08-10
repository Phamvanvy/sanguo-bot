package peony.service.activity;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.DayListener;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.NoEnoughValueException;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.drop.DropGroupUtil;
import peony.game.drop.GroupDrop;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

/**
 * 许愿活动
 * @author dchen
 */
public class VowActivity implements IActivityImpl, DayListener, ServiceEventListener {
	
	protected Activity act;
	
	/** 场景、NPCID */
	protected int[] scene = {224, 917534};
	/** 许愿扣除J币 */
	protected int vowMoney = 10;
	/** 许愿扣除物品 */
	protected int vowItem = 1311;
	/** 许愿扣除物品数量 */
	protected int vowItemCount = 1;
	/** 许愿掉落 */
	protected int vowDrop = 85;
	/** 祈福扣除I币代物品 */
	protected int prayIMoneyItem = 1256;
	/** 祈福扣除I币代物品数量 */
	protected int prayIMoneyItemCount = 1;
	/** 祈福掉落 */
	protected int prayDrop = 368;
	/** 灌溉获取经验 */
	protected int floodingExp = 10;
	/** 经验雨需要的灌溉次数 */
	protected int maxFlooding = 100;
	/** 经验雨经验 */
	protected int rainExp = 5;
	/** 经验雨持续时间（分钟） */
	protected int rainDuration = 10;
	/** 经验雨频率（分钟） */
	protected int rainRate = 1;
	/** 许愿、祈福每天最大次数 */
	protected int perDay = 1;
	/** 灌溉最低级别 */
	protected int minLevel = 10;
	
	public int currentFloodingTime;
	public Map<Integer, Integer> vowRecords = new HashMap<Integer, Integer>();
	public Map<Integer, Integer> floodRecords = new HashMap<Integer, Integer>();
	
	public Map<Integer, Integer> noticeItems = new HashMap<Integer, Integer>();	//物品世界聊配置
	
	protected List<Integer> players = new ArrayList<Integer>();
	
	public static Random random = new Random();
	protected int lastSaveTime;
	
	public VowActivity(Activity act){
		this.act = act;
	}
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		Time.addDayListener(this);
	}
	
	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		dbservice.removeDatabase(getDBName());
	}

	public Activity getActivity() {
		return act;
	}
	
	private String getDBName() {
		return "VowActivity" + act.getId();
	}

	public void load() {
		String configData = act.configData;
		String[] datas = configData.split(",");
		for(String subDatas : datas){
			if(subDatas==null || subDatas.equals(""))
				continue;
			String[] data = subDatas.split(":");
			String name = data[0].trim();
			String value = data[1].trim();
			if(name.equalsIgnoreCase("mapId")){
				scene[0] = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("cId")){
				scene[1] = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("vowMoney")){
				vowMoney = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("vowItem")){
				vowItem = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("vowItemCount")){
				vowItemCount = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("vowDrop")){
				vowDrop = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("prayIMoneyItem")){
				prayIMoneyItem = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("prayIMoneyItemCount")){
				prayIMoneyItemCount = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("prayDrop")){
				prayDrop = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("rainExp")){
				rainExp = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("maxFlooding")){
				maxFlooding = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("floodExp")){
				floodingExp = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("rainRate")){
				rainRate = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("rainDuration")){
				rainDuration = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("perDay")){
				perDay = Integer.parseInt(value);
			}else if(name.equalsIgnoreCase("minLevel")){
				minLevel = Integer.parseInt(value);
			}
		}
		loadData();
		
		try{
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("Items/vownoticeitems.xml");
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			Element root = doc.getRootElement();
			Iterator ite = root.elementIterator("item");
			while (ite.hasNext()) {
				Element el = (Element)ite.next();
				int id = Integer.parseInt(el.attributeValue("id"));
				int shout = Integer.parseInt(el.attributeValue("shout"));
				int count = Integer.parseInt(el.attributeValue("count"));
				noticeItems.put(id, count);
			}
		}catch(Exception e){
			
		}
	}
	
	public void loadData(){
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
	            	int value = Integer.parseInt(StringBinding.entryToString(dataEntry));
	            	if(key.contains("vow")){
	            		vowRecords.put(Integer.parseInt(key.substring(0, key.length()-3)), value);
	            	}else if(key.contains("flood")){
	            		floodRecords.put(Integer.parseInt(key.substring(0, key.length()-5)), value);
	            	}else if(key.contains("total")){
	            		currentFloodingTime = value;
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
	
	protected Creature getVowCreature(int mapId){
		if(mapId==scene[0]){
			VMap[] maps = ((NoInstanceVMapManager)Server.server.getWorld().getVMapManager(mapId)).getVMaps(mapId);
			if(maps!=null){
				for(VMap map : maps){
					if(map!=null){
						Creature c = map.getCreatureById(scene[1]);
						if(c!=null)
							return c;
					}
				}
			}
		}
		return null;
	}
	
	public int getMapId(){
		return scene[0];
	}
	
	public int getImoneyItem(){
		return prayDrop;
	}
	
	/** 许愿 */
	public void vow(Player player) throws Exception {
		if(player!=null && player.map!=null){
			int playerMapId = player.map.id;
			Creature c = getVowCreature(playerMapId);
			if(c==null || !c.inRange(player, 80))
				throw new Exception(peony.Messages.STRING_00055);
			if(vowRecords.get(player.id)!=null && vowRecords.get(player.id).intValue()>=perDay)
				throw new Exception(MessageFormat.format(peony.Messages.STRING_00623, perDay));
			PlayerTransaction tx = player.newTransaction("VOW");
			try {
				player.decMoney(vowMoney, tx, true);
				GameItem item = null;
				try {item = player.bag.removeGameItemIngoreInstanceId(vowItem, vowItemCount, tx, true);} catch (Exception e1) {}
				if(item==null){
					tx.rollback();
					throw new Exception(MessageFormat.format(peony.Messages.STRING_00093, ObjectAccessor.createGameItem(vowItem).template.name));
				}
				tx.commit();
				GroupDrop drop = DropGroupUtil.getGroupDrop(vowDrop);
				Gain gain = new Gain(player);
				drop.calc(random, gain);
				PlayerTransaction tx1 = player.newTransaction("VOW");
				try {
					player.addGainComplete(gain, tx1, true);
					tx1.commit();
				} catch (Exception e) {
					tx1.rollback();
					GainItem[] items = gain.getGainItems();
					for(GainItem item0 : items){
						if(item0==null)
							continue;
						GameItem gameItem = item0.getItem();
						int count = item0.getCount();
						Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(player.id, 
								peony.Messages.STRING_00004, peony.Messages.STRING_00624, "", 0, gameItem, count, "VOW");
					}
				}
				player.message(-1, peony.Messages.STRING_00625, -1, -1);
				if(vowRecords.get(player.id)==null)
					vowRecords.put(player.id, 1);
				else
					vowRecords.put(player.id, vowRecords.get(player.id)+1);
			} catch (Exception e) {
				if(e instanceof NoEnoughValueException)
					throw new Exception(peony.Messages.STRING_00158);
				else 
					throw new Exception(e.getMessage());
			}
		}
	}
	
	/** 祈福 */
	public void pray(Player player, int serial, int limit) throws Exception {
		if(player!=null && player.map!=null){
//			int playerMapId = player.map.id;
//			Creature c = getVowCreature(playerMapId);
//			if(c==null || !c.inRange(player, 80))
//				throw new Exception(peony.Messages.STRING_00055);
			Server.server.getServiceRegistry().getSyncExecutorService().schedule(
					new VowIbuyCall(player.session, this, player, serial, limit));
		}
	}
	
	/** 灌溉 */
	public void flooding(Player player) throws Exception {
		if(player!=null && player.map!=null){
			int playerMapId = player.map.id;
			Creature c = getVowCreature(playerMapId);
			if(c==null || !c.inRange(player, 80))
				throw new Exception(peony.Messages.STRING_00055);
			if(player.level<minLevel)
				throw new Exception(MessageFormat.format(peony.Messages.STRING_00626, minLevel));
			if(floodRecords.get(player.id)!=null && floodRecords.get(player.id).intValue()>=perDay)
				throw new Exception(MessageFormat.format(peony.Messages.STRING_00627, perDay));
			PlayerTransaction tx = player.newTransaction("VOW");
			try {
				player.addExp(floodingExp*player.level, tx, true);
				tx.commit();
				player.message(-1, peony.Messages.STRING_00628, -1, -1);
				if(floodRecords.get(player.id)==null)
					floodRecords.put(player.id, 1);
				else
					floodRecords.put(player.id, floodRecords.get(player.id)+1);
				currentFloodingTime++;
				if(currentFloodingTime>=maxFlooding){
					final Timer timer = new Timer();
					Timer timer2 = new Timer();
					timer.schedule(new TimerTask(){

						public void run() {
							synchronized(players){
								for(int playerId : players){
									Player p = ObjectAccessor.getPlayer(playerId);
									if(p!=null){
										PlayerTransaction t = p.newTransaction("VOWRAIN");
										p.addExp(rainExp, t, true);
										t.commit();
									}
								}
							}
						}
						
					}, 0, rainRate*60000);
					timer2.schedule(new TimerTask(){

						public void run() {
							timer.purge();
							timer.cancel();
						}
						
					}, rainDuration * 60000);
					currentFloodingTime = 0;
				}
			} catch (Exception e) {
				tx.rollback();
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
            for (int playerId : vowRecords.keySet()) {
            	StringBinding.stringToEntry(String.valueOf(playerId+"vow"), keyEntry);
            	int value = vowRecords.get(playerId);
            	StringBinding.stringToEntry(String.valueOf(value), dataEntry);
            	db.put(null, keyEntry, dataEntry);
            }
            for(int playerId : floodRecords.keySet()){
            	StringBinding.stringToEntry(String.valueOf(playerId+"flood"), keyEntry);
            	int value = floodRecords.get(playerId);
            	StringBinding.stringToEntry(String.valueOf(value), dataEntry);
            	db.put(null, keyEntry, dataEntry);
            }
            StringBinding.stringToEntry("total", keyEntry);
        	StringBinding.stringToEntry(String.valueOf(currentFloodingTime), dataEntry);
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

	public void shutdown() {
		save();
	}

	public void dayChanged() {
		vowRecords.clear();
		floodRecords.clear();
		currentFloodingTime = 0;
		clear();
	}

	public int[] getEventTypes() {
		return new int[]{ServiceEvent.EVENT_MAP_PLAYER_ADDED, ServiceEvent.EVENT_MAP_PLAYER_REMOVED};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerAddMap((VMap)event.param1, (Player)event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_REMOVED:
			processPlayerRemoveMap((VMap)event.param1, (Player)event.param2);
			break;
		}
	}
	
	protected void processPlayerAddMap(VMap map, Player player){
		synchronized (players) {
			if (map.getId() == getMapId())
				players.add(player.id);
		}
		if(Time.currTime-lastSaveTime>600000){
			save();
		}
	}
	
	protected void processPlayerRemoveMap(VMap map, Player player){
		synchronized (players) {
			if (map.getId() == getMapId())
				players.remove((Integer) player.id);
		}
	}

}
