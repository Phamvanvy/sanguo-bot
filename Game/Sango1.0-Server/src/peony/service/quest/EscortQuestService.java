package peony.service.quest;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.CreatureDieCallback;
import peony.game.DayListener;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.LogUtil;
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
import peony.game.buff.ImmuneAllBuff;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.game.nation.Nation;
import peony.game.salary.SalaryService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.shop.ShopService;
import peony.util.TimeUtil;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

/**
 * 押镖任务服务
 * @author bqzhang
 */
public class EscortQuestService implements Service, ServiceEventListener ,DayListener{
	
	protected static Logger log = Logger.getLogger(EscortQuestService.class);
	protected static Random rnd = new Random();
	
	/** 刷新镖车代扣物品 */
	public static int IMONEY_ITEM_REFRESH = 3477;
	
	/** 物品--残存的货物 */
	public static int[][] ITEM_CCDHW = {
		{4158, 4169, 4170, 4171},	//经验
		{4172, 4173, 4174, 4175}	//战功
	};
	
	/** 物品--装满货物的箱子 */
	public static int[][] ITEM_ZMHWDXZ = {
		{4159, 4162, 4163, 4164},	//经验
		{4165, 4166, 4167, 4168}	//战功
	};
	
	/** 珍珠 */
	public static int ITEMID_ZHENZHU = 1311;
	
	private static int BEGIN_HOUR1 = 10;	//10	开始时间	小时
	private static int END_HOUR1 = 11;		//11	结束时间
	private static int BEGIN_MIN1 = 30;		//30	开始时间 分钟
	private static int END_MIN1 = 30;		//30	结束时间
	
	private static int BEGIN_HOUR2 = 20;		//20:00	开始时间
	private static int END_HOUR2 = 21;		//23:00	结束时间
	public static long ONEDAY = 24 * 3600 * 1000L;
	
	public static String PROPERTY_ESCORT_DAY = "ESCORTDAY"; //初始化每日押镖任务数量,每天只能调整一次
	public static int ESCORT_DIE_COUNT = 1;					//镖车修复次数
	public static int NEED_ZHENZHU_COUNT = 1; 				//需要的珍珠数量
	public static float[] convoyLvMul = {2, 2.5f, 3.5f, 5};	//镖车品质系数(绿，蓝，紫，橙)
	
	public static int ESCORT_QUEST_MAX = 100;				//每日NPC发布的押镖任务数量
	public static int acceptCount;							//每日押镖任务被领取的数量
	public static int MAX_IB_COUNT = 8;						//押镖刷新最大元宝数 -8元宝
	
	private static String[] CONVOY_DEST_NAME = {"西域山城", "朔方", "江陵", "南海"};	//镖车目的地
	private static String[] REWARD_NPC_NAME = {"西域军备监察使", "朔方军备监察使", "江陵军备监察使", "南海军备监察使"};	//领取奖励NPC名称
	
	/**
	 * 各镖车路线
	 */
	public PlayerConvoyDef[][] defs = new PlayerConvoyDef[4][4];
	
	public Map<Integer, Integer> convoyRob = new HashMap<Integer, Integer>();//劫镖次数
	
	public static int[][] ESCORT_QUESTS_ID = {
		{0, 0},
		{1177},	//魏
		{1176},	//蜀
		{1178}	//吴
	};
	
	public EscortQuestService(){
		acceptCount = 0;
	}
	
	public void startup() throws Exception {
		Time.addDayListener(this);
		load();
		
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("PlayerConvoy.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		processNotify();
		Server.server.getEventManager().registerListener(this);
	}
	
	private void processNotify(){
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				processEscortQuest();
				initOnlineEscort();
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("本时段的明珠镖局激情开张啦！欢迎有勇有谋的少侠们前来接镖。");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR1, BEGIN_MIN1), ONEDAY, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				processEscortQuest();
				initOnlineEscort();
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("本时段的明珠镖局激情开张啦！欢迎有勇有谋的少侠们前来接镖。");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR2, 0), ONEDAY, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("本时段押镖活动已经结束，明珠镖局恭候众少侠们下一时段再来光顾。");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), END_HOUR1, END_MIN1), ONEDAY, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("本时段押镖活动已经结束，明珠镖局恭候众少侠们下一时段再来光顾。");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), END_HOUR2, 0), ONEDAY, TimeUnit.MILLISECONDS);
	}
	
	public void load() {
		
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc){
		Element root = doc.getRootElement();
		List l = root.elements("convoy");
		if(l.size() != 12)
			throw new IllegalArgumentException();
		for(int i=0;i<l.size();i++){
			Element elConvoy = (Element)l.get(i);
			int faction = Integer.parseInt(elConvoy.attributeValue("faction"));
			int pointType = Integer.parseInt(elConvoy.attributeValue("type"));
			int npcId = Integer.parseInt(elConvoy.attributeValue("npcid"));
			PlayerConvoyDef def = new PlayerConvoyDef(faction, pointType, npcId);
			defs[def.faction][i%4] = def;
			List l1 = elConvoy.elements("point");
			for(int j=0;j<l1.size();j++){
				Element elPoint = (Element)l1.get(j);
				int mapId = Integer.parseInt(elPoint.attributeValue("mapid"));
				int x = Integer.parseInt(elPoint.attributeValue("x"));
				int y = Integer.parseInt(elPoint.attributeValue("y"));
				def.addMapPoint(mapId, x, y);
			}
		}
	}
	
	public static boolean isEscortQuest(int questId){
		for(int i=0; i<ESCORT_QUESTS_ID.length; i++){
			for(int j=0; j<ESCORT_QUESTS_ID[i].length; j++){
				if(ESCORT_QUESTS_ID[i][j] == questId){
					return true;
				}
			}
		}
		return false;
	} 
	
	public void save() {}
	
	public void shutdown() {
		save();
		Server.server.getEventManager().unregisterListener(this);
	}
	
	/**
	 * 午夜12点维护押镖任务
	 */
	public void processEscortQuest(){
		acceptCount = 0;
	}
	
	/**
	 * 午夜12点初始化在线玩家的每日押镖任务
	 */
	public void initOnlineEscort(){
		Iterator<Player> it = ObjectAccessor.players.values().iterator();
		while(it.hasNext()){
			Player p = it.next();
			if(p!=null){
				initEscort(p);
			}
		}
	}
	
	public void initEscort(Player p){
		p.pool.remove(Player.PROPERTY_FIRST_ESCORT_DAY);
		p.pool.remove(Player.PROPERTY_ESCORT_ACCEPT);
		p.pool.remove(Player.PROPERTY_ESCORTCAR_LEVEL);
		p.pool.remove(Player.PROPERTY_ESCORTCAR_REFRESHCOUNT);
		p.pool.remove(Player.PROPERTY_ESCORTCAR_ISPANMONEY);
		p.pool.remove(Player.PROPERTY_ESCORTCAR_ISVIPDOUBLE);
	}
	
	public int[] getEventTypes() {
		return new int[] {
			ServiceEvent.EVENT_PLAYER_LOGINED,
			ServiceEvent.EVENT_UNIT_DIE,
			ServiceEvent.EVENT_PLAYER_LOGOUTED,
		};
	}
	
	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
			case ServiceEvent.EVENT_PLAYER_LOGINED:
				playerLogin((Player)event.param1);
				break;
			case ServiceEvent.EVENT_UNIT_DIE:
				//unitDie((Unit)event.param1,(Unit)event.param2);
				break;
			case ServiceEvent.EVENT_PLAYER_LOGOUTED:
				playerLogOut((Player)event.param1);
				break;
		}
	}
	
	/*
	 * 当玩家数据被载入时，同步更新玩家每日押镖。
	 * @param player
	 */
	private void playerLogin(Player player) {
		int day = player.pool.getInt(Player.PROPERTY_FIRST_ESCORT_DAY, 0);
		if(day==Time.day){//清除不在线玩家的押镖状态
			if(inTime(BEGIN_HOUR2, 0, END_HOUR2, 0)){
				if(player.pool.getInt(Player.PROPERTY_ESCORT_ACCEPT)==1){
					day=0;
				}
			}
		}
		if(day == 0 || day != Time.day){
			initEscort(player);
		}
	}
	
	/**
	 * 玩家下线
	 */
	protected void playerLogOut(Player p){
	}
	
	public void dayChanged() {
		new Thread(new Runnable(){
			public void run(){
				try{
					processEscortQuest();
					initOnlineEscort();
					convoyRob.clear();
				}catch(Exception e){
				}
			}
		}).start();
	}
	
	public static String getImoney(Player p, int itemId, int countResh){
		ShopService service = Server.server.getServiceRegistry().getShopService();
		float price = 0;
		try {
			price = service.getItemPriceInAppointShop(itemId, -1);
		} catch (Exception e) {
			price = service.getFilterItemPrice(itemId);
		}
		
		int itemCount = 1;
		float yaunbao = price/36;
		for(int i=0; i<countResh; i++){
			itemCount = itemCount * 2;
			yaunbao = (price*itemCount) / 36;
			if(yaunbao > MAX_IB_COUNT){
				yaunbao = (price*(itemCount/2)) / 36;
				break;
			}
		}
		DecimalFormat df = new DecimalFormat("0.00");
		String showPrice = df.format(yaunbao);
		return showPrice;
	}
	
	/**是否在指定时间段内**/
	private boolean inTime(int sHour, int sMin, int eHour, int eMin){
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
	
	/**
	 * @param 领取每日押镖任务
	 */
	public synchronized void acceptEscortQuest(Player p, ClientSession session, int serial) throws EscortException{
		if(!inTime(BEGIN_HOUR1, BEGIN_MIN1, END_HOUR1, END_MIN1)
				&& !inTime(BEGIN_HOUR2, 0, END_HOUR2, 0)){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACCEPT_ESCORT_QUEST_CLIENT, "押镖时间每日10:30-11:30点和20:00-21:00点");
			return;
		}
		
		if(p.pool.getInt(Player.PROPERTY_ESCORT_ACCEPT, 0) != 0){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACCEPT_ESCORT_QUEST_CLIENT, "本时段已领取押镖任务");
			return;
		}
		
		if(p.level < 60){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACCEPT_ESCORT_QUEST_CLIENT, "60级才能领取押镖任务");
			return;
		}
		
		p.pool.setInt(Player.PROPERTY_FIRST_ESCORT_DAY, Time.day);	//保存记录镖车时间
		
		Packet pt = new Packet(OpCode.ACCEPT_ESCORT_QUEST_SERVER);
		pt.putInt(serial);
		int escortCarLv = p.pool.getInt(Player.PROPERTY_ESCORTCAR_LEVEL, 0);	//镖车品质
		if(escortCarLv == 0){
			int randNum = rnd.nextInt(100);
		    if (randNum < 75) {
		    	escortCarLv = 1;	//绿
		    }else if(randNum >= 75 && randNum < 94){
		    	escortCarLv = 2;	//蓝
		    }else if(randNum >= 94 && randNum < 98){
		    	escortCarLv = 3;	//紫
		    }else{
		    	escortCarLv = 4;	//橙
		    }
			p.pool.setInt(Player.PROPERTY_ESCORTCAR_LEVEL, escortCarLv);
		}
		pt.put(escortCarLv);
		int count = p.pool.getInt(Player.PROPERTY_ESCORTCAR_ISPANMONEY, 0);
		if(count > 0){
			pt.put(1);
		}else{
			pt.put(0);
		}
		pt.putString(getImoney(p, IMONEY_ITEM_REFRESH, count));
		p.send(pt);
		log.info("[GETESCORTQUEST]"+LogUtil.getPlayerLogString(p)+"CARLEV["+escortCarLv+"]");
	}
	
	public synchronized void startEscort(Player p, int convoyType) throws EscortException{
		if(!inTime(BEGIN_HOUR1, BEGIN_MIN1, END_HOUR1, END_MIN1)
				&& !inTime(BEGIN_HOUR2, 0, END_HOUR2, 0)){
			throw new  EscortException("押镖时间每日10:30-11:30点和20:00-21:00点");
		}
		
		if(p.pool.getInt(Player.PROPERTY_ESCORT_ACCEPT, 0) != 0){
			throw new  EscortException("本时段已领取押镖任务");
		}
		
		int escortCarLv = p.pool.getInt(Player.PROPERTY_ESCORTCAR_LEVEL, 0) - 1;	//镖车品质
		if(escortCarLv < 0)
			throw new  EscortException("您还没有领取押镖任务");
		
		int isPayMoney = p.pool.getInt(Player.PROPERTY_ESCORTCAR_ISPANMONEY, 0);
		if(isPayMoney == 0){	//没有花费元宝，则扣珍珠
			if(acceptCount >= ESCORT_QUEST_MAX){
				throw new  EscortException(MessageFormat.format("本时段的{0}次押镖任务，已被领取完毕!",ESCORT_QUEST_MAX));
			}
			
			PlayerTransaction tx = p.newTransaction("ESCORT");
			if (p.bag.removeGameItem(ITEMID_ZHENZHU, -1,
					NEED_ZHENZHU_COUNT, tx, true) != null) {
				tx.commit();
			}else{
				tx.rollback();
				throw new EscortException("缺少珍珠");
			}
			acceptCount++;
		}
		
		if(inTime(BEGIN_HOUR1, BEGIN_MIN1, END_HOUR1, END_MIN1)){
			p.pool.setInt(Player.PROPERTY_ESCORT_ACCEPT, 1);
		}else if(inTime(BEGIN_HOUR2, 0, END_HOUR2, 0)){
			p.pool.setInt(Player.PROPERTY_ESCORT_ACCEPT, 2);
		}
		
		int isVipDouble = p.pool.getInt(Player.PROPERTY_ESCORTCAR_ISVIPDOUBLE, 0);
		MapPoint point = defs[p.faction][escortCarLv].getFirstPoint();
		PlayerConvoy convoy = new PlayerConvoy(p, defs[p.faction][escortCarLv], Time.currTime, 
				escortCarLv, convoyType, ESCORT_DIE_COUNT, 0, isVipDouble);
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, defs[p.faction][escortCarLv].npcId);
		
		VMapManager manager = Server.server.getWorld().getVMapManager(point.mapId);
		VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(point.mapId);
		
		Creature npc = (Creature) VMapUtil.addCreature(maps[0],point.x,point.y, (GameMapNPC) gmo,true,0,null);
		npc.isPvp = true;
		npc.name = MessageFormat.format("{0}的{1}", p.name, npc.name);
		npc.dieCallback = new DieCallback(convoy);
		npc.buffs.addBuff(new ImmuneAllBuff());
		npc.setAI(new PlayerConvoyAI(convoy, npc, 0));
		convoy.setName(npc.name);
		convoy.npc = npc;
		
		log.info("[CONVOYSTART]PLAYER["+p.id+"]");
		p.message(-1, MessageFormat.format("押镖开始！目的地:{0}。时限30分钟", CONVOY_DEST_NAME[convoy.convoyLevel]), -1, -1);
		
		if(isPayMoney == 0){	//没有花费元宝，之前没有增加镖车
			if(acceptCount >= ESCORT_QUEST_MAX){
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("本时段的押镖任务已全部被领取");
			}else if(acceptCount > ESCORT_QUEST_MAX - 10){
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(
				MessageFormat.format("押镖任务只剩{0}次", ESCORT_QUEST_MAX - acceptCount));
			}
		}
		
		if(escortCarLv == 2){	//紫
			Server.server.getServiceRegistry().getChatService().sendWorldMessage(
					MessageFormat.format("{0}的{1}的昂贵镖车已经开始押送了，大家快去江陵劫镖吧。", GameObject.getFactionName(p.faction), p.name));
		}else if(escortCarLv == 3){	//橙
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			chatService.sendWorldShout(p.name, p.id, p.faction, 
					MessageFormat.format("{0}的{1}的珍稀镖车已经开始押送了，大家快去南海劫镖吧。", GameObject.getFactionName(p.faction), p.name)
					, 0xff4700, 11000);
			Packet pt = new Packet(OpCode.WORLD_SHOUT_SERVER);
			pt.putInt(0);
			p.send(pt);
		}
		//获得押镖工资
		SalaryService salaryService = Server.server.getServiceRegistry().getSalaryService();
		salaryService.processConvoySalary(p);
	}
	
	public void success(PlayerConvoy convoy) {
		log.info("[CONVOYSUCCESS]PLAYER["+convoy.playerId+"]");
		convoy.npc.removeFromWorld();
		Player player = ObjectAccessor.getPlayer(convoy.playerId);
		if(player != null){
			player.pool.setInt(Player.PROPERTY_ESCORTCAR_ISVIPDOUBLE, 0);	//取消双倍奖励
			player.message(-1, "押镖成功", -1, -1);
			//押镖成功奖励
			PlayerTransaction tx = player.newTransaction("convoy");
			try {
				switch(convoy.convoyType){
					case 0:	//经验型
						int exp = (int)(player.level*3000*convoyLvMul[convoy.convoyLevel]);
						if(convoy.isVipDouble == 1){
							exp = exp * 2;
						}
						player.addExp(exp, tx, true);
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, MessageFormat.format("押镖成功！您获得{0}经验",exp));
						break;
					case 1:	//战功型
						int credit = (int)(player.level*convoyLvMul[convoy.convoyLevel]*2);
						if(convoy.isVipDouble == 1){
							credit = credit * 2;
						}
						player.addCredit(credit, tx, true);
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, MessageFormat.format("押镖成功！您获得{0}战功",credit));
						break;
				}
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
			}
		}
	}
	
	public void fail(PlayerConvoy convoy, Unit source){
		Player player = ObjectAccessor.getPlayer(convoy.playerId);
		if(convoy.escortDieCount == 0){
			log.info("[CONVOYFAIL]PLAYER["+convoy.playerId+"]");
			convoy.npc.getVMap().notifyDisappear(convoy.npc);
			convoy.npc.removeFromWorld();
			
			int itemCnt = 1;
			if(convoy.isVipDouble == 1){
				itemCnt = itemCnt * 2;
			}
			
			GameItem item = ObjectAccessor.createGameItem(ITEM_CCDHW[convoy.convoyType][convoy.convoyLevel]);
			if(player != null){
				player.pool.setInt(Player.PROPERTY_ESCORTCAR_ISVIPDOUBLE, 0);	//取消双倍奖励
				player.message(-1, "押镖失败了,仅抢回残存的货物", -1, -1);
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "镖车被劫，残存的货物已放入背包，请查收");
				
				//押镖失败物品
				PlayerTransaction tx = player.newTransaction("CONVOYFAIL");
				try{
					player.bag.addGameItemComplete(item, itemCnt, tx, true);
					tx.commit();
				}catch(NoEnoughSpaceException e){
					tx.rollback();
			    	MailService mailService = Server.server.getServiceRegistry().getMailService();
			    	mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, "残破的货物", "", 0, 
			    			item, itemCnt, "CONVOYFAIL");
			    	Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您的背包已满，残破的货物已通过飞鸽发送");
				}
				//Server.server.getServiceRegistry().getChatService().sendWorldMessage(
						//MessageFormat.format("{0}的镖车被掠夺，仅抢回残存的货物", player.name));
			}else{
				Server.server.getServiceRegistry().getMailService()
				.sendSystemMail(convoy.playerId, peony.Messages.STRING_00004, "残破的货物", "押镖失败奖励", 0,
						item, itemCnt, "CONVOYFAIL");
			}
		}else{
			convoy.escortDieCount--;
			convoy.npc.getVMap().notifyDisappear(convoy.npc);
			convoy.npc.removeFromWorld();
			MapPoint point = defs[convoy.faction][convoy.convoyLevel].getMapPoint(convoy.escortPointIndex);
			ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
			GameMapObject gmo = GameMapObject.findByID(proj, defs[convoy.faction][convoy.convoyLevel].npcId);
			VMapManager manager = Server.server.getWorld().getVMapManager(point.mapId);
			VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(point.mapId);
			Creature npc = (Creature) VMapUtil.addCreature(maps[0],point.x,point.y, (GameMapNPC) gmo,true,0,null);
			npc.isPvp = true;
			npc.name = convoy.getName();
			npc.dieCallback = new DieCallback(convoy);
			npc.buffs.addBuff(new ImmuneAllBuff());
			npc.setAI(new PlayerConvoyAI(convoy, npc, convoy.escortPointIndex));
			convoy.npc = npc;
			if(player != null){
				player.message(-1, MessageFormat.format("<cff0000>工匠修好了镖车，还可修复{0}次</c>", convoy.escortDieCount), -1, -1);
			}
		}
		
		//劫镖得到物品
		if(source != null){
			if(source.type == GameObject.TYPE_PLAYER){
				Player netPlayer = (Player)source;
				int count = 0;
				try {
					count = convoyRob.get(netPlayer.id);
				} catch (Exception e1) {
				}
				count++;
				convoyRob.put(netPlayer.id, count);
				if(count < 8){
					netPlayer.message(-1, "劫镖成功，获得<cff0000>装满货物的箱子</c>", -1, -1);
					PlayerTransaction tx = netPlayer.newTransaction("CONVOYROB");
					GameItem item2 = ObjectAccessor.createGameItem(ITEM_ZMHWDXZ[convoy.convoyType][convoy.convoyLevel]);
					int itemCnt = 1;
					try{
						netPlayer.bag.addGameItemComplete(item2, itemCnt, tx, true);
						tx.commit();
					}catch(NoEnoughSpaceException e){
						tx.rollback();
				    	MailService mailService = Server.server.getServiceRegistry().getMailService();
				    	mailService.sendSystemMailAsync(netPlayer.id, peony.Messages.STRING_00004, "装满货物的箱子", "", 0, 
				    			item2, itemCnt, "CONVOYROB");
				    	Server.server.getServiceRegistry().getChatService().sendPrivateMessage(netPlayer.id, "您的背包已满，装满货物的箱子已通过飞鸽发送");
					}
				}else{
					netPlayer.message(-1, "您已经劫了太多的镖了，给别人一次机会吧", -1, -1);
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
			Server.server.getServiceRegistry().getEscortQuestService().fail(convoy, source);
		}
	}
	
	

}
