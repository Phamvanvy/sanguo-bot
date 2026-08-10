package peony.service.activity;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.util.Utils;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.CreatureDieCallback;
import peony.game.DayListener;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.MapPoint;
import peony.game.NoEnoughSpaceException;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.attendant.Attendant;
import peony.game.buff.ImmuneAllBuff;
import peony.game.mail.MailService;
import peony.net.Packet;
import peony.service.quest.PlayerConvoy;
import peony.service.quest.PlayerConvoyDef;
import peony.service.sleepycat.SleepyCatService;
import peony.util.TimeUtil;

public class FestivalConvoyActivity implements IActivityImpl,DayListener{
	

	public static Map<Integer,List<Integer>> faction2Player = new HashMap<Integer,List<Integer>>();
	protected static List<Integer> player2GetReward = new ArrayList<Integer>();
	public static Map<Integer, Integer> convoyRob = new HashMap<Integer, Integer>();//劫镖次数
	protected static Map<Integer,Creature> faction2ShowNpc = new HashMap<Integer,Creature>();
	protected static Map<Integer,NpcConfig> faction2Npc = new HashMap<Integer,NpcConfig>();
	protected static List<Integer> player2Convoyed = new ArrayList<Integer>();
	
	//魏国押镖时间
	private static int BEGIN_HOUR1 = 11;	//11	开始时间	小时
	private static int END_HOUR1 = 12;		//12	结束时间
	private static int BEGIN_HOUR2 = 17;	//18	开始时间 小时
	private static int END_HOUR2 = 18;		//30	结束时间
	
	//蜀国押镖时间
	private static int BEGIN_HOUR3 = 12;	//12	开始时间	小时
	private static int END_HOUR3 = 13;		//13	结束时间
	private static int BEGIN_HOUR4 = 18;	//18	开始时间 小时
	private static int END_HOUR4 = 19;		//19	结束时间
	
	//吴国押镖时间
	private static int BEGIN_HOUR5 = 13;	//13	开始时间	小时
	private static int END_HOUR5 = 14;		//14	结束时间
	private static int BEGIN_HOUR6 = 19;	//19	开始时间 小时
	private static int END_HOUR6 = 20;		//20	结束时间
	
	private static int FRESHNPC_HOUR = 20;	//20	各主城刷新NPC
	public static long ONEDAY = 24 * 3600 * 1000L;
	public static int ESCORT_QUEST_MAX = 50;//各时段镖车数量
	public static int acceptCount;		//每日押镖任务被领取的数量
	public static int LEVEL_LIMIT = 65;
	public static int ESCORT_DIE_COUNT = 0;//镖车可修复次数，0为不可修复
	public static int SUCC_ITEM = 4784; //押镖成功物品
	public static int FAIL_ITEM = 4785; //押镖失败物品
	public static int ROB_ITEM = 4786; //劫镖所得物品
	public static int ROB_LIMIT = 5;//劫镖次数限制
	
	public static int WIN_REWARD = 4788;//胜利国物品
	
	public static int[] factionRecord = new int[3];
	public static int[] convoyRank = new int[3];
	
	// 上次保存的时间
	private int lastSaveTime;
	/**
	 * 各镖车路线
	 */
	public static PlayerConvoyDef[] defs = new PlayerConvoyDef[4];
	
	 private Activity activity;
	 public FestivalConvoyActivity(Activity owner){
			this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	private String getDBName_Convoysucc() {//保存已押镖玩家
		return "FestivalConvoyActivity_Convoysucc" + activity.getId();
	}

	private String getDBName_Robbed() {//保存玩家劫镖
		return "FestivalConvoyActivity_Robbed" + activity.getId();
	}
	
	
	/** 领取镖车*/
	public static void startFestivalEscort(Player p) throws Exception{
		if(!inFactionTime(p)){
			throw new  Exception("现在不是勇士所在国家押镖时间");
		}
		if(p.level < LEVEL_LIMIT){
			throw new  Exception("65级才能领取活动押镖任务");
		}
		if(player2Convoyed.contains(p.id)){
			p.message(-1, "您今天已经押过粽横四海镖车了，请给其他兄弟们一些机会吧。", -1, -1);
			return;
//			throw new  Exception("您今天已经押过粽横四海镖车了，请给其他兄弟们一些机会吧。");
		}

		if(acceptCount >= ESCORT_QUEST_MAX){
			throw new  Exception(MessageFormat.format("本时段的{0}次粽横四海镖车任务，已被领取完毕!",ESCORT_QUEST_MAX));
		}
		acceptCount++;
		MapPoint point = defs[p.faction].getFirstPoint();
		PlayerConvoy convoy = new PlayerConvoy(p, defs[p.faction], Time.currTime, 
				0, 0, ESCORT_DIE_COUNT, 0, 0);
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, defs[p.faction].npcId);
		
		VMapManager manager = Server.server.getWorld().getVMapManager(point.mapId);
		VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(point.mapId);
		
		Creature npc = (Creature) VMapUtil.addCreature(maps[0],point.x,point.y, (GameMapNPC) gmo,true,0,null);
		npc.isPvp = true;
		npc.name = MessageFormat.format("{0}的{1}", p.name, npc.name);
		npc.dieCallback = new DieCallback(convoy);
		npc.buffs.addBuff(new ImmuneAllBuff());
		npc.setAI(new FestivalConvoyAI(convoy, npc, 0));
		convoy.setName(npc.name);
		convoy.npc = npc;
		player2Convoyed.add(p.id);
		if(acceptCount >= ESCORT_QUEST_MAX){
			Server.server.getServiceRegistry().getChatService().sendWorldMessage("本时段四海镖局的镖车已经全部被领取完了。");
		}else if(acceptCount > ESCORT_QUEST_MAX - 10){
			Server.server.getServiceRegistry().getChatService().sendWorldMessage(
			MessageFormat.format("押镖任务只剩{0}个粽横四海镖车可以押送，今日成功押送镖车最多的国家，该国所有65级以上的国民可在20点之后，去主城找到屈原，领取丰厚大礼和3倍经验大力丸1个哦！", ESCORT_QUEST_MAX - acceptCount));
		}
		
		p.message(-1, "押镖开始！目的地:南越祥龙雕像处。", -1, -1);
	}
	
	/** 领取奖励*/
	public static void getReward(Player p) throws Exception{
		if(p.level<LEVEL_LIMIT){
			throw new  Exception("65级以下没有礼物哦，赶快升级去吧");
		}
		int winFaction = getWinFaction();
		int max = 0;
		if(winFaction!=0){
			max = faction2Player.get(winFaction).size();
		}
		int count = 0;
		if(faction2Player.containsKey(p.faction)){
			count = faction2Player.get(winFaction).size();
		}
		if(p.faction!=winFaction&&count<max){
			throw new  Exception("获胜国玩家才可领取");
		}
		if(player2GetReward.contains(p.id)){
			throw new Exception("您今日已经领取过奖励了，屈老我还没有老糊涂啊！");
		}
		//胜利国领取奖励
		GameItem item = ObjectAccessor.createGameItem(WIN_REWARD);
		PlayerTransaction tx = p.newTransaction("FESTIVALCONVOYWIN");
		try{
			p.bag.addGameItemComplete(item, 1, tx, true);
			tx.commit();
		}catch(NoEnoughSpaceException e){
			tx.rollback();
	    	MailService mailService = Server.server.getServiceRegistry().getMailService();
	    	mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, "领取端午押镖奖励成功", "", 0, 
	    			item, 1, "FESTIVALCONVOYWIN");
	    	Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, "您的背包已满，物品已通过飞鸽发送");
		}
		player2GetReward.add(p.id);
	}
	
	/** 押镖排行榜 */
	public static void bubbleBoard(){
		factionRecord = new int[3];
		convoyRank = new int[3];
		int winFaction = getWinFaction();
		if(winFaction!=0){
			factionRecord[0] = winFaction;
			convoyRank[0] = 1;
			int max = faction2Player.get(winFaction).size();
			int secondFaction = 0;
			int secondNum = -1;
			int thirdFaction = 0;
			for(int faction=1;faction<4;faction++){
				if(faction!=winFaction){
					int num = 0;
					if(faction2Player.containsKey(faction)){
						num = faction2Player.get(faction).size();
					}
					if(secondNum<num){
						thirdFaction = secondFaction;
						secondFaction = faction;
						secondNum = num;
					}else{
						thirdFaction = faction;
					}
				}
			}
			factionRecord[1]=secondFaction;
			factionRecord[2]=thirdFaction;
			int count = 1;
			int num2 = 0;
			if(faction2Player.containsKey(secondFaction)){
				num2 = faction2Player.get(secondFaction).size();
			}
			if(num2<max){
				count ++;
			}
			convoyRank[1] = count;
			int num3=0;
			if(faction2Player.containsKey(thirdFaction)){
				num3 = faction2Player.get(thirdFaction).size();
			}
			if(num3<max && num3<num2){
				count ++;
			}
			convoyRank[2] = count;
		}else{
			factionRecord[0] = 1;
			factionRecord[1] = 2;
			factionRecord[2] = 3;
			convoyRank[0] = 1;
			convoyRank[1] = 1;
			convoyRank[2] = 1;
		}
	}
	
	
	public static void convoyBoard(int serial,Player p){
		Packet pt = new Packet(OpCode.FESTIVAL_ESCORTBOARD_SERVER);
		pt.putInt(serial);
		pt.put(factionRecord.length);
		for(int i=0;i<factionRecord.length;i++){
			pt.put(convoyRank[i]);
			pt.put(factionRecord[i]);
			int num = 0;
			if(faction2Player.containsKey(factionRecord[i])){
				num = faction2Player.get(factionRecord[i]).size();
			}
			pt.putInt(num);
		}
		p.send(pt);
	}

	/** 当天是否已经领取押镖任务*/
	public static boolean hasGetEscort(Player p){
		if(faction2Player.containsKey(p.faction)){
			List<Integer> playerList = faction2Player.get(p.faction);
			if(playerList.contains(p.id))
				return true;
		}else{
			List<Integer> list = new ArrayList<Integer>();
			faction2Player.put(p.faction, list);
		}
		return false;
	}
	
	public static void addEscortPlayer(Player p){
		if(faction2Player.containsKey(p.faction)){
			List<Integer> list = faction2Player.get(p.faction);
			if(!list.contains(p.id)){
				list.add(p.id);
				faction2Player.put(p.faction, list);
			}
		}else{
			List<Integer> list = new ArrayList<Integer>();
			list.add(p.id);
			faction2Player.put(p.faction, list);
		}
	}
	
	/**是否在指定时间段内**/
	private static boolean inTime(int sHour, int sMin, int eHour, int eMin){
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(new Date());
		cal1.set(Calendar.HOUR_OF_DAY, sHour);
		cal1.set(Calendar.MINUTE, sMin);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(new Date());
		cal2.set(Calendar.HOUR_OF_DAY, eHour);
		cal2.set(Calendar.MINUTE, eMin);
		return cal.after(cal1) && cal.before(cal2);
	}
	
   private static boolean inFactionTime(Player p){
	   if(p.faction==1){
		   return inTime(BEGIN_HOUR1,0,END_HOUR1,0) || inTime(BEGIN_HOUR2,0,END_HOUR2,0);
	   }else if(p.faction == 2){
		   return inTime(BEGIN_HOUR3,0,END_HOUR3,0) || inTime(BEGIN_HOUR4,0,END_HOUR4,0);
	   }else if(p.faction == 3){
		   return inTime(BEGIN_HOUR5,0,END_HOUR5,0) || inTime(BEGIN_HOUR6,0,END_HOUR6,0);
	   }
	   return false;
   }
	
	public void clear() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		dbservice.removeDatabase(getDBName_Convoysucc());
		dbservice.removeDatabase(getDBName_Robbed());
	}


	public void load() {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("activityconvoy.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			//读取本地已押镖玩家，押镖成功玩家
			db = dbservice.openDatabase(getDBName_Convoysucc());
			Cursor cursor = null;
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            try {
	            cursor = db.openCursor(null, new CursorConfig());
	            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
	            	String key = StringBinding.entryToString(keyEntry);
	            	if(key.contains("acceptcount")){
	            		int value = Integer.parseInt(StringBinding.entryToString(dataEntry));
	            		acceptCount = value;
	            	}else if(key.contains("getrewardplayer")){
	            		String value = StringBinding.entryToString(dataEntry);
						int[] arr = Utils.stringToIntArray(value, ',');
						for (int i = 0; i < arr.length; i++) {
							player2GetReward.add(arr[i]); 
						}
	            	}else if(key.contains("playerconvoyed")){
	            		String value = StringBinding.entryToString(dataEntry);
						int[] arr = Utils.stringToIntArray(value, ',');
						for (int i = 0; i < arr.length; i++) {
							player2Convoyed.add(arr[i]);
						}
	            	}else{
		            	int faction = Integer.parseInt(key);
						String value = StringBinding.entryToString(dataEntry);
						int[] arr = Utils.stringToIntArray(value, ',');
						List<Integer> list = new ArrayList<Integer>();
						for (int i = 0; i < arr.length; i++) {
							list.add(arr[i]);
						}
						faction2Player.put(faction, list);
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
		
		Database db1 = null;
	    try{
	      //读取本地已劫镖玩家
			db1 = dbservice.openDatabase(getDBName_Robbed());
			Cursor cursor1 = null;
            DatabaseEntry keyEntry1 = new DatabaseEntry();
            DatabaseEntry dataEntry1 = new DatabaseEntry();
            try {
	            cursor1 = db1.openCursor(null, new CursorConfig());
	            while (cursor1.getNext(keyEntry1, dataEntry1, null) != OperationStatus.NOTFOUND) {
	            	int playerId = Integer.parseInt(StringBinding.entryToString(keyEntry1));
	            	int value = Integer.parseInt(StringBinding.entryToString(dataEntry1));
	            	convoyRob.put(playerId, value);
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
			if (db1 != null) {
				try {
					db1.close();
				} catch (Exception e) {
				}
			}
		}
		
		lastSaveTime = Time.currTime;
	}

	public void save() {
		//1.各国已成功押镖的玩家
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName_Convoysucc());
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            for (int faction : faction2Player.keySet()) {
            	StringBinding.stringToEntry(String.valueOf(faction), keyEntry);
            	List<Integer> list = faction2Player.get(faction);
            	int arr[] = new int[list.size()];
				for (int i = 0; i < list.size(); i++) {
					arr[i] = list.get(i);
				}
				StringBinding.stringToEntry(Utils.intArrayToString(arr, ','),
						dataEntry);
				db.put(null, keyEntry, dataEntry);
            }
            StringBinding.stringToEntry("acceptcount", keyEntry);
        	StringBinding.stringToEntry(String.valueOf(acceptCount), dataEntry);
        	db.put(null, keyEntry, dataEntry);
        	if(player2GetReward!=null && player2GetReward.size()>0){
        		StringBinding.stringToEntry("getrewardplayer", keyEntry);
	        	int arr[] = new int[player2GetReward.size()];
				for (int i = 0; i < player2GetReward.size(); i++) {
					arr[i] = player2GetReward.get(i);
				}
				StringBinding.stringToEntry(Utils.intArrayToString(arr, ','),
						dataEntry);
				db.put(null, keyEntry, dataEntry);
        	}
        	
        	if(player2Convoyed!=null && player2Convoyed.size()>0){
        		StringBinding.stringToEntry("playerconvoyed", keyEntry);
	        	int arr[] = new int[player2Convoyed.size()];
				for (int i = 0; i < player2Convoyed.size(); i++) {
					arr[i] = player2Convoyed.get(i);
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
		
		 //2.当日已经劫镖的玩家
		Database db1 = null;
		try{
            db1 = dbservice.openDatabase(getDBName_Robbed());
            DatabaseEntry keyEntry1 = new DatabaseEntry();
            DatabaseEntry dataEntry1 = new DatabaseEntry();
            for (int playerId : convoyRob.keySet()) {
            	StringBinding.stringToEntry(String.valueOf(playerId), keyEntry1);
            	int value = convoyRob.get(playerId);
            	StringBinding.stringToEntry(String.valueOf(value), dataEntry1);
            	db1.put(null, keyEntry1, dataEntry1);
            }
		} catch (Exception e) {
		} finally {
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

	}

	public void startup() throws Exception {
		Time.addDayListener(this);
//		processNotify();
	}
	
	public void parse(Document doc){
		Element root = doc.getRootElement();
		List l = root.elements("convoy");
		for(int i=0;i<l.size();i++){
			Element elConvoy = (Element)l.get(i);
			int faction = Integer.parseInt(elConvoy.attributeValue("faction"));
			int pointType = Integer.parseInt(elConvoy.attributeValue("type"));
			int npcId = Integer.parseInt(elConvoy.attributeValue("npcid"));
			PlayerConvoyDef def = new PlayerConvoyDef(faction, pointType, npcId);
			defs[def.faction] = def;
			List l1 = elConvoy.elements("point");
			for(int j=0;j<l1.size();j++){
				Element elPoint = (Element)l1.get(j);
				int mapId = Integer.parseInt(elPoint.attributeValue("mapid"));
				int x = Integer.parseInt(elPoint.attributeValue("x"));
				int y = Integer.parseInt(elPoint.attributeValue("y"));
				def.addMapPoint(mapId, x, y);
			}
		}
		
		List npclist = root.elements("npc");
		for(int i=0;i<npclist.size();i++){
			Element elNpc = (Element)npclist.get(i);
			int faction = Integer.parseInt(elNpc.attributeValue("faction"));
			int winNpcId = Integer.parseInt(elNpc.attributeValue("winnpcid"));
			int normalNpcId = Integer.parseInt(elNpc.attributeValue("normalnpcid"));
			int mapId = Integer.parseInt(elNpc.attributeValue("mapid"));
			int x = Integer.parseInt(elNpc.attributeValue("x"));
			int y = Integer.parseInt(elNpc.attributeValue("y"));
			NpcConfig config = new NpcConfig(faction,winNpcId,normalNpcId,mapId,x,y);
			faction2Npc.put(faction, config);
		}
	}
	
	public void processNotify(){
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				acceptCount = 0;
				initData();
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("魏国的将士们！粽横四海镖车可以开始押送啦，三个国家哪一个成功押送到南越祥龙处的镖车越多，哪个国家的全体国民就有福啦！");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR1, 0), ONEDAY, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				acceptCount = 0;
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("魏国的将士们！粽横四海镖车可以开始押送啦，三个国家哪一个成功押送到南越祥龙处的镖车越多，哪个国家的全体国民就有福啦！");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR2, 0), ONEDAY, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				acceptCount = 0;
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("蜀国的将士们！粽横四海镖车可以开始押送啦，三个国家哪一个成功押送到南越祥龙处的镖车越多，哪个国家的全体国民就有福啦！");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR3, 0), ONEDAY, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				acceptCount = 0;
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("蜀国的将士们！粽横四海镖车可以开始押送啦，三个国家哪一个成功押送到南越祥龙处的镖车越多，哪个国家的全体国民就有福啦！");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR4, 0), ONEDAY, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				acceptCount = 0;
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("吴国的将士们！粽横四海镖车可以开始押送啦，三个国家哪一个成功押送到南越祥龙处的镖车越多，哪个国家的全体国民就有福啦！");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR5, 0), ONEDAY, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				acceptCount = 0;
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("吴国的将士们！粽横四海镖车可以开始押送啦，三个国家哪一个成功押送到南越祥龙处的镖车越多，哪个国家的全体国民就有福啦！");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR6, 0), ONEDAY, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("屈原现身三大主城，到底哪个国在今天成功押送粽横四海镖车的数量最多呢？答案即将揭晓!");
				refreshNpc();
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), FRESHNPC_HOUR, 0), ONEDAY, TimeUnit.MILLISECONDS);
		
		
		if(inTime(20,0,23,59)){
			refreshNpc();
		}
	}
	
	public static int getWinFaction(){
		int winFaction = 0;
		int getNum = 0;
		if(faction2Player!=null && faction2Player.size()>0){
			for(int faction : faction2Player.keySet()){
				List<Integer> playerList = faction2Player.get(faction);
				if(getNum<playerList.size()){
					getNum = playerList.size();
					winFaction = faction;
				}
			}
		}
		return winFaction;
	}
	
	public void refreshNpc(){
		bubbleBoard();
		removeNpc();
		faction2ShowNpc.clear();
		int winFaction = getWinFaction();
		int max = 0;
		if(winFaction!=0){
			max = faction2Player.get(winFaction).size();
		}
		for(int faction : faction2Npc.keySet()){
			NpcConfig config = faction2Npc.get(faction);
		    VMap map = ((NoInstanceVMapManager) Server.server.getWorld().
					getVMapManager(config.mapId)).getVMaps(config.mapId)[0];
			ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
			int npcId = config.normalNpcId;
			int count = 0;
			if(faction2Player.containsKey(faction)){
				count = faction2Player.get(faction).size();
			}
			if(max!=0 && (faction == winFaction || count>=max)){
				npcId = config.winNpcId;
			}
			GameMapObject gmo = GameMapObject.findByID(proj,npcId);
			GameObject npc0 = VMapUtil.addCreature(map,config.x, config.y,(GameMapNPC) gmo, true, 0, null);
			faction2ShowNpc.put(faction, (Creature)npc0);
        }	  
	}
	
	public static void removeNpc(){
		if(faction2ShowNpc!=null && faction2ShowNpc.size()>0){
			for(int faction : faction2ShowNpc.keySet()){
				Creature c = faction2ShowNpc.get(faction);
				if(c!=null && c.isVisibleAndAlive()){
					c.removeFromWorld();
				}
			}
		}
	}

	public void dayChanged() {
		initData();
		removeNpc();
		clear();
	}
	
	public void initData(){
		faction2Player.clear();
		player2GetReward.clear();
		convoyRob.clear();
		player2Convoyed.clear();
	}
	
	public static void success(PlayerConvoy convoy) {
		convoy.npc.removeFromWorld();
		Player player = ObjectAccessor.getPlayer(convoy.playerId);
		if(player != null){
			player.message(-1, "押镖成功", -1, -1);
			//押镖成功奖励
			GameItem item = ObjectAccessor.createGameItem(SUCC_ITEM);
			PlayerTransaction tx = player.newTransaction("FESTIVALCONVOYSUCC");
			try{
				player.bag.addGameItemComplete(item, 1, tx, true);
				tx.commit();
			}catch(NoEnoughSpaceException e){
				tx.rollback();
		    	MailService mailService = Server.server.getServiceRegistry().getMailService();
		    	mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, "押镖成功", "端午节押镖成功奖励", 0, 
		    			item, 1, "FESTIVALCONVOYSUCC");
		    	Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您的背包已满，满满的物品已通过飞鸽发送");
			}
			if(Time.currentHour<FRESHNPC_HOUR)
			    addEscortPlayer(player);
		}
	}
	
	@SuppressWarnings("static-access")
	public static void fail(PlayerConvoy convoy, Unit source){
		Player player = ObjectAccessor.getPlayer(convoy.playerId);
		convoy.npc.getVMap().notifyDisappear(convoy.npc);
		convoy.npc.removeFromWorld();
		
		GameItem item = ObjectAccessor.createGameItem(FAIL_ITEM);
		if(player != null){
			player.message(-1, MessageFormat.format("押镖失败了,仅抢回{0}", item.template.name), -1, -1);
			//押镖失败物品
			PlayerTransaction tx = player.newTransaction("FESTIVALCONVOYFAIL");
			try{
				player.bag.addGameItemComplete(item, 1, tx, true);
				tx.commit();
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, MessageFormat.format("镖车被劫，{0}已放入背包，请查收", item.template.name));
			}catch(NoEnoughSpaceException e){
				tx.rollback();
		    	MailService mailService = Server.server.getServiceRegistry().getMailService();
		    	mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, item.template.name, "", 0, 
		    			item, 1, "FESTIVALCONVOYFAIL");
		    	Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, MessageFormat.format("您的背包已满，{0}已通过飞鸽发送", item.template.name));
			}
		}else{
			Server.server.getServiceRegistry().getMailService()
			.sendSystemMail(convoy.playerId, peony.Messages.STRING_00004, item.template.name, "押镖失败奖励", 0,
					item, 1, "FESTIVALCONVOYFAIL");
		}
		
		//劫镖得到物品
		if(source != null){
			if(source.type == GameObject.TYPE_PLAYER || source.type == GameObject.TYPE_ATTENDANT){
				Player netPlayer = null;
				if(source.type == GameObject.TYPE_ATTENDANT){
					Attendant att = (Attendant)source;
					netPlayer = att.owner;
				}else{
				    netPlayer = (Player)source;
				}
				if(netPlayer == null)
					return;
				int count = 0;
				try {
					if(convoyRob.containsKey(netPlayer.id))
					     count = convoyRob.get(netPlayer.id);
				} catch (Exception e1) {
					
				}
				count++;
				convoyRob.put(netPlayer.id, count);
				if(count <= ROB_LIMIT){
					PlayerTransaction tx = netPlayer.newTransaction("FESTIVALCONVOYROB");
					GameItem item2 = ObjectAccessor.createGameItem(ROB_ITEM);
					int itemCnt = 1;
					try{
						netPlayer.bag.addGameItemComplete(item2, itemCnt, tx, true);
						tx.commit();
					}catch(NoEnoughSpaceException e){
						tx.rollback();
				    	MailService mailService = Server.server.getServiceRegistry().getMailService();
				    	mailService.sendSystemMailAsync(netPlayer.id, peony.Messages.STRING_00004, item2.template.name, "", 0, 
				    			item2, itemCnt, "CONVOYROB");
				    	Server.server.getServiceRegistry().getChatService().sendPrivateMessage(netPlayer.id, MessageFormat.format("您的背包已满，{}已通过飞鸽发送", item2.template.name));
					}
					netPlayer.message(-1, MessageFormat.format("劫镖成功，获得<cff0000>{0}</c>", item2.template.name), -1, -1);
				}else{
					netPlayer.message(-1, "您本日已截取了5辆镖车，再次击杀他国镖车，将不再获取抢来的粽子，正所谓得饶人处且饶人啊！", -1, -1);
				}
			}
		}
	}
	static class DieCallback implements CreatureDieCallback{
		protected PlayerConvoy convoy;
		public DieCallback(PlayerConvoy convoy){
			this.convoy = convoy;
		}
		
		public void die(Creature c,Unit source){
			fail(convoy, source);
		}
	}

}

class NpcConfig{
	int faction;
	int winNpcId;
	int normalNpcId;
	int mapId;
	int x;
	int y;
	public NpcConfig(int faction,int winNpcId,int normalNpcId,int mapId,int x,int y){
		this.faction = faction;
		this.winNpcId = winNpcId;
		this.normalNpcId = normalNpcId;
		this.mapId = mapId;
		this.x = x;
		this.y = y;
	}
}
