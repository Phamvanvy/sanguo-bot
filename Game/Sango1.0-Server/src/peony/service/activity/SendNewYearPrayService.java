package peony.service.activity;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import peony.game.Actor;
import peony.game.ChatOption;
import peony.game.DayListener;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.shop.ShopService;
import peony.util.TimeUtil;

/**
 * 春节发送祝福 -> 妇女节送祝福
 * @author bqzhang
 */
public class SendNewYearPrayService implements Service, ServiceEventListener ,DayListener{
	
	/** 已发送的祝福信息 */
	public static class PrayMessage{
		public int resId;		//发送人ID
		public int destId;		//目标ID
		public String resName;	//发送人姓名
		public String destName;	//目标姓名
	    public int mesIndex;	//祝福语随从
	}
	
	protected static Logger log = Logger.getLogger(SendNewYearPrayService.class);
	protected static Random rnd = new Random();
	
	/** 祝福公告奖励物品 */
	public static int IMONEY_ITEM_DRAW = 2245;
	
	/** 1元宝代扣物品 */
	public static int IMONEY_ITEM_SEND = 3476;
	
	public static long ONEHOUR = 3600 * 1000L;	//每隔一小时刷新一次
	public static int MAX_SEND_PRAY = 5;		//春节发送祝福每人每天最多5次
	
	public List<PrayMessage> sendMes = new ArrayList<PrayMessage>();	//所有人发送的祝福
	
	public static String[] prayDescs = new String[]{
		"开心有理，美丽无罪，节日快乐！",
		"男人加班女人休，男人下厨女人溜，男人送礼女人收，男人钱包女人扣，男人祝福女人受",
		"今天你消费过节，我钱包随你打劫！",
		"有困难要帮，没有困难创造困难也要帮；有祝福要送，没有祝福借别人的祝福也要送。"
	};
	
	private int startHour;	//开始时间
	
	public SendNewYearPrayService(){
		startHour = Time.currentHour;
		int curMimute = Time.currentMin;
		if(curMimute >= 29){
			startHour++;
		}
		if(startHour > 23){
			startHour = 0;
		}
	}
	
	public void startup() throws Exception {
		Time.addDayListener(this);
		load();
		processNotify();
		Server.server.getEventManager().registerListener(this);
	}
	
	private void processNotify(){
//		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
//			public void run() {
//				Server.server.getServiceRegistry().getChatService().sendWorldMessage("一分钟后将抽取一些祝福,并且送给这些幸运玩家小礼物。想送祝福的玩家可以去许愿树处为好友送上祝福。");
//			}
//		}, TimeUtil.getScheduleTimeMills(new Date(), startHour, 29), ONEHOUR, TimeUnit.MILLISECONDS);
//		
//		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
//			public void run() {
//				sendPrayWorld();
//			}
//		}, TimeUtil.getScheduleTimeMills(new Date(), startHour, 30), ONEHOUR, TimeUnit.MILLISECONDS);
	}
	
	public void load() {
		
	}
	
	public void save() {}
	
	public void shutdown() {
		save();
		Server.server.getEventManager().unregisterListener(this);
	}
	
	/**
	 * 每天刷新春节祝福次数
	 */
	public void initOnlineEscort(){
		Iterator<Player> it = ObjectAccessor.players.values().iterator();
		while(it.hasNext()){
			Player p = it.next();
			if(p!=null){
				initPlaySendPray(p);
			}
		}
	}
	
	public void initPlaySendPray(Player p){
		p.pool.remove(Player.PROPERTY_SEND_NEWYEAR_PRAY);
		p.pool.setInt(Player.PROPERTY_SEND_NEWYEAR_DAY, Time.day);
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
	private void playerLogin(Player p) {
		int day = p.pool.getInt(p.PROPERTY_SEND_NEWYEAR_DAY, 0);
		if(day == 0 || day != Time.day){
			initPlaySendPray(p);
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
					Iterator<Player> it = ObjectAccessor.players.values().iterator();
					while(it.hasNext()){
						Player p = it.next();
						if(p!=null){
							initPlaySendPray(p);
						}
					}
				}catch(Exception e){
				}
			}
		}).start();
	}
	
	public static String getImoney(int itemId){
		ShopService service = Server.server.getServiceRegistry().getShopService();
		float price = 0;
		try {
			price = service.getItemPriceInAppointShop(itemId, -1);
		} catch (Exception e) {
			price = service.getFilterItemPrice(itemId);
		}
		float yaunbao = price/36;
		DecimalFormat df = new DecimalFormat("0.00");
		String showPrice = df.format(yaunbao);
		return showPrice;
	}
	
	/**
	 * @param 春节发送祝福->妇女节送祝福
	 */
	public void sendPray(Player p, ClientSession session, int serial, int type, int destId){
		if(p.level < 60){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.SEND_NEWYEAR_PRAY_CLIENT, "六十级以上玩家才能送出祝福，少年还是赶快升级去吧");
			return;
		}
		
		if(type == 0){
			int sendCnt = p.pool.getInt(Player.PROPERTY_SEND_NEWYEAR_PRAY, 0);
			if(sendCnt < 5){
				try{
					sendPrayPrivate(p, destId);
				}catch(Exception e){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.SEND_NEWYEAR_PRAY_CLIENT, e.getMessage());
				}
			}
			
			Packet pt = new Packet(OpCode.SEND_NEWYEAR_PRAY_SERVER);
			pt.putInt(serial);
			if(sendCnt < 5){
				pt.put(0);
			}else{
				pt.put(1);
				String _desc = MessageFormat.format("今日免费次数已用尽，再次发送祝福需要消耗{0}元宝", getImoney(IMONEY_ITEM_SEND));
				pt.putString(_desc);
			}
			p.send(pt);
		}else{
			Server.server.getServiceRegistry().getSyncExecutorService().schedule(
					new SendPrayIbuyCall(p.session, p, serial, destId));
		}
	}
	
	//发送祝福私聊
	public void sendPrayPrivate(Player p, int destId) throws Exception{
		RelationService rs = Server.server.getServiceRegistry().getRelationService();
		PlayerRelation relation = rs.get(p.id);
		if (relation == null) {
			throw new Exception(peony.Messages.STRING_00436);
		}
		relation.friends.refreshPlayers();
		int count = relation.friends.getCount();
		Actor destActor = null;
		for(int i=0; i<count; i++){
			Actor temp = relation.friends.getPlayerAt(i);
			if(temp.id == destId){
				destActor = temp;
				break;
			}
		}
		if(destActor == null){
			throw new Exception("该角色还不是您的好友");
		}
		
		int sendCnt = p.pool.getInt(Player.PROPERTY_SEND_NEWYEAR_PRAY, 0);
		sendCnt++;
		p.pool.setInt(Player.PROPERTY_SEND_NEWYEAR_PRAY, sendCnt);
		p.pool.setInt(Player.PROPERTY_SEND_NEWYEAR_DAY, Time.day);
		
		int prayDescCnt = prayDescs.length;
		int index = rnd.nextInt(prayDescCnt);
		
		PrayMessage pm = new PrayMessage();
		pm.resId = p.id;
		pm.destId = destId;
		pm.resName = p.name;
		pm.destName = destActor.name;
		pm.mesIndex = index;
		sendMes.add(pm);
		
		Player destPlayer = ObjectAccessor.getPlayer(destId);
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		if (destPlayer != null && destPlayer.session != null) {
			ChatMessage cm = ChatMessage.parse(prayDescs[index], new byte[0], p, ChatOption.PRIVATE, destId);
			cm.destName = destActor.name;
			chatService.addChatMessage(cm);
			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_CHAT, p, new Byte((byte)ChatOption.PRIVATE).intValue()));
		}else{
			String prayDesc = MessageFormat.format("{0}目前没有在线，但是已经送上您对好友的祝福：{1}", destActor.name, prayDescs[index]);
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, prayDesc);
		}
	}
	
	//发送祝福公告
	public void sendPrayWorld(){
//		GameItem item = ObjectAccessor.createGameItem(IMONEY_ITEM_DRAW);
//		
//		//抽取10名玩家的祝福发送公告
//		for(int i=0; i<10; i++){
//			int len = sendMes.size();
//			if(len <= 0){
//				break;
//			}
//			int index = rnd.nextInt(len);
//			PrayMessage pm = sendMes.get(index);
//			
//			String prayDesc = MessageFormat.format("{0}为{1}送上祝福：{2}恭喜他们获得经验奖励。", pm.resName, pm.destName, prayDescs[pm.mesIndex]);
//			Server.server.getServiceRegistry().getChatService().sendWorldMessage(prayDesc);
//			
//			MailService mailService = Server.server.getServiceRegistry().getMailService();
//			mailService.sendSystemMailAsync(pm.resId, peony.Messages.STRING_00004, "女人节小礼物。", "恭喜您在女人节送祝福活动中幸运获得奖励", 0, 
//	    			item, 1, "NEW_YEAR_PRAY");
//			mailService.sendSystemMailAsync(pm.destId, peony.Messages.STRING_00004, "女人节小礼物。", "恭喜您在女人节送祝福活动中幸运获得奖励", 0, 
//	    			item, 1, "NEW_YEAR_PRAY");
//			sendMes.remove(index);
//		}
		sendMes.clear();
			
	}
	
}

