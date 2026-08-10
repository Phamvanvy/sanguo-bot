package peony.game.convoy;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.joda.time.MutableDateTime;

import peony.db.RefreshNpcCall;
import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.CreatureDieCallback;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.MapPoint;
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
import peony.game.mail.MailService;
import peony.game.nation.Nation;
import peony.service.Service;
import peony.util.TimeUtil;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

public class NationConvoyService implements Service {
	

	
	public NationConvoyDef[] defs = new NationConvoyDef[4]; 
	public NationConvoy[] convoys = new NationConvoy[4];
	
	private static final Logger log = Logger.getLogger(NationConvoyService.class);
	
	public static int DEPOSITE = 4000000;  //押镖成功后给国库金钱
	public static int FALIDEPOSITE = 1000000;  //押镖失败后返还国库金钱
	public static int DEPOSITEGET = 2000000;  //抢镖成功平分金钱
	public static int REWARDITEM = 2639;
	public static int BEGIN_HOUR[] = {22,22,23};
	public static int BEGIN_MINUTE[] = {0,30,0};
	public static int TERMINATETIME = 20;
	
	protected MutableDateTime cachedCal = new MutableDateTime();
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("NationConvoy.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		initConvoyTime();
		processNotify();
	}
	
	public void initConvoyTime(){
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_STATE, 0)!=0 
					 && nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_DATE, 0)==0){
				nation.pool.setInt(Nation.PROPERTY_NATIONCONVOY_DATE, getStartTime(0));
			}
		}
	}
	
	public void processNotify(){
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
					if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_STATE, 0)!=0 &&
							nation.pool.getInt(Nation.PROPERTY_NATION_CONVOY, 0)!=Time.day && 
							convoys[nation.faction]==null && 
							 nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_DATE, 0)==getStartTime(0)){
						Server.server.getServiceRegistry().getChatService()
						.sendFactionSystemMessage(nation.faction,
								"5分钟后国家物资车整装待发，请广大英雄前去江陵集合，准备国家押运。");
					}
				}
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR[0], BEGIN_MINUTE[0]-5), 24*60*60*1000l, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.NATIONCONVOY);
				call.nationConveyStartTimeIndex = 0;
				Server.server.getWorld().schedule(call);
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR[0], BEGIN_MINUTE[0]), 24*60*60*1000l, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
					if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_STATE, 0)!=0 &&
							nation.pool.getInt(Nation.PROPERTY_NATION_CONVOY, 0)!=Time.day &&
							 convoys[nation.faction]==null && 
							 nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_DATE, 0)==getStartTime(1)){
						Server.server.getServiceRegistry().getChatService()
						.sendFactionSystemMessage(nation.faction,
								"5分钟后国家物资车整装待发，请广大英雄前去江陵集合，准备国家押运。");
					}
				}
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR[1], BEGIN_MINUTE[1]-5), 24*60*60*1000l, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.NATIONCONVOY);
				call.nationConveyStartTimeIndex = 1;
				Server.server.getWorld().schedule(call);
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR[1], BEGIN_MINUTE[1]), 24*60*60*1000l, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
					if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_STATE, 0)!=0 &&
							nation.pool.getInt(Nation.PROPERTY_NATION_CONVOY, 0)!=Time.day &&
							 convoys[nation.faction]==null && 
							 nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_DATE, 0)==getStartTime(2)){
						Server.server.getServiceRegistry().getChatService()
						.sendFactionSystemMessage(nation.faction,
								"5分钟后国家物资车整装待发，请广大英雄前去江陵集合，准备国家押运。");
					}
				}
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR[2], BEGIN_MINUTE[2]-5), 24*60*60*1000l, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.NATIONCONVOY);
				call.nationConveyStartTimeIndex = 2;
				Server.server.getWorld().schedule(call);
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR[2], BEGIN_MINUTE[2]), 24*60*60*1000l, TimeUnit.MILLISECONDS);
		
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc){
		Element root = doc.getRootElement();
		List l = root.elements("convoy");
		if(l.size() != 3)
			throw new IllegalArgumentException();
		for(int i=0;i<l.size();i++){
			Element elConvoy = (Element)l.get(i);
			int faction = Integer.parseInt(elConvoy.attributeValue("faction"));
			int npcId = Integer.parseInt(elConvoy.attributeValue("npcid"));
			NationConvoyDef def = new NationConvoyDef(faction,npcId);
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
	}
	
	public boolean isConvoying(int faction){
		return convoys[faction] != null;
	}
	
	public int getStartTime(int index){
		if(index>=0 && index<BEGIN_HOUR.length){
			return BEGIN_HOUR[index]*60+BEGIN_MINUTE[index];
		}
		return 0;
	}
	
	public int getIndex(int time){
		for(int i=0;i<BEGIN_HOUR.length;i++){
			if(time == BEGIN_HOUR[i]*60+BEGIN_MINUTE[i]){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 国家押运刷出镖车
	 * @param nation
	 */
	public void refreshConvoy(Nation nation){
		try{
			MapPoint point = defs[nation.faction].getFirstPoint();
			NationConvoy convoy = new NationConvoy(nation,defs[nation.faction],DEPOSITE,Time.currTime);
			ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
			GameMapObject gmo = GameMapObject.findByID(proj, defs[nation.faction].npcId);
			VMapManager manager = Server.server.getWorld().getVMapManager(point.mapId);
			VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(point.mapId);
			
			Creature npc = (Creature) VMapUtil.addCreature(maps[0],point.x,point.y, (GameMapNPC) gmo,true,0,null);
			npc.isPvp = true;
			npc.dieCallback = new DieCallback(convoy);
			npc.buffs.addBuff(new ImmuneAllBuff());
			npc.setAI(new ConvoyAI(convoy,npc));
			convoy.npc = npc;
			convoys[nation.faction] = convoy;
			nation.pool.setInt(Nation.PROPERTY_NATION_CONVOY, Time.day);
			log.info("[CONVOYSTART]FACTION["+convoy.nation.faction+"]");
			Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(nation.faction, peony.Messages.STRING_00519);
			for(int i=1;i<4;i++){
				if(i != nation.faction){
					Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(i, 
							MessageFormat.format(peony.Messages.STRING_00520, GameObject.getFactionName(nation.faction)));
				}
			}
		}catch(Exception e){
			
		}
	}
	
	public void nationConvoy(int startTimeIndex){
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			try {
				if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_STATE, 0)!=0 &&
						nation.pool.getInt(Nation.PROPERTY_NATION_CONVOY, 0)!=Time.day && 
						convoys[nation.faction]==null &&
						 nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_DATE, 0)==getStartTime(startTimeIndex)){
					synchronized(nation){
						if(nation.money<DEPOSITEGET){
							if(nation.getKingId()!=-1){
								Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(nation.getKingId(), "系统", "自动开启国家押运失败", "由于国库资金不足，国家押运自动关闭.", 0, 
										null, 0, "NATIONCONVOY");
							}
							continue;
						}
						nation.decMoney(DEPOSITEGET); //发起押镖扣除200万
					}
					refreshConvoy(nation);
				}
			} catch (Exception e) {
                 
			}
		}
	}
	
	public void startConvoy(Nation nation,int index) throws ConvoyException{
		if(index<0 || index>=BEGIN_HOUR.length){
			throw new  ConvoyException("数据错误");
		}
		
		if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_MODIFYTIME,0)==Time.day){
			throw new  ConvoyException("每天只能修改一次押运时间哦");
		}
		
		if(isInTime())
			throw new  ConvoyException("每天00:00-20:00才可以开启押运任务");
		
		if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_STATE, 0)!=0){
			throw new ConvoyException("国家押运已经开启");
		}
		
		synchronized(nation){
			if(nation.money<DEPOSITEGET){
				throw new ConvoyException(peony.Messages.STRING_00517);
			}
		}
		nation.pool.setInt(Nation.PROPERTY_NATIONCONVOY_STATE,Time.day);
		int date = BEGIN_HOUR[index]*60+BEGIN_MINUTE[index];
		nation.pool.setInt(Nation.PROPERTY_NATIONCONVOY_DATE,date);
		nation.pool.setInt(Nation.PROPERTY_NATIONCONVOY_MODIFYTIME,Time.day);
		 String kingName = "";
		    try{
		    	kingName = nation.getKingName();
		    }catch(Exception e){
		    	
		    }
		    if(!kingName.equals("")){
				String message = MessageFormat.format("国公{0}将国家押运设定在{1}:{2}{3}，请广大英雄提前做好准备！", kingName,NationConvoyService.BEGIN_HOUR[index],NationConvoyService.BEGIN_MINUTE[index],NationConvoyService.BEGIN_MINUTE[index]==0?"0":"");
				Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(nation.faction, message);
		    }
//		if(!Time.in(Calendar.getInstance(),22,30,24,0))
//			throw new  ConvoyException(peony.Messages.STRING_00515);
//		if(convoys[nation.faction] != null){
//			throw new ConvoyException(peony.Messages.STRING_00516);
//		}
//		synchronized(nation){
//			if(nation.money<DEPOSITEGET){
//				throw new ConvoyException(peony.Messages.STRING_00517);
//			}
//			nation.decMoney(DEPOSITEGET); //发起押镖扣除200万
//		}
//		if(nation.pool.getInt(Nation.PROPERTY_NATION_CONVOY, 0)==Time.day){
//			throw new ConvoyException(peony.Messages.STRING_00518);
//		}
//		MapPoint point = defs[nation.faction].getFirstPoint();
//		NationConvoy convoy = new NationConvoy(nation,defs[nation.faction],DEPOSITE,Time.currTime);
//		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
//		GameMapObject gmo = GameMapObject.findByID(proj, defs[nation.faction].npcId);
//		VMapManager manager = Server.server.getWorld().getVMapManager(point.mapId);
//		VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(point.mapId);
//		
//		Creature npc = (Creature) VMapUtil.addCreature(maps[0],point.x,point.y, (GameMapNPC) gmo,true,0,null);
//		npc.isPvp = true;
//		npc.dieCallback = new DieCallback(convoy);
//		npc.buffs.addBuff(new ImmuneAllBuff());
//		npc.setAI(new ConvoyAI(convoy,npc));
//		convoy.npc = npc;
//		convoys[nation.faction] = convoy;
//		nation.pool.setInt(Nation.PROPERTY_NATION_CONVOY, Time.day);
//		log.info("[CONVOYSTART]FACTION["+convoy.nation.faction+"]");
//		Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(nation.faction, peony.Messages.STRING_00519);
//		for(int i=1;i<4;i++){
//			if(i != nation.faction){
//				Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(i, 
//						MessageFormat.format(peony.Messages.STRING_00520, GameObject.getFactionName(nation.faction)));
//			}
//		}
	}
	
//	public boolean isInTime(int index){
//		long now = System.currentTimeMillis();
//		cachedCal.setMillis(now);
//		int hour = cachedCal.getHourOfDay();
//		int min = cachedCal.getMinuteOfHour();
//		boolean afterStartTime = false;
//		if(hour>BEGIN_HOUR[index] || hour==BEGIN_HOUR[index] && min>=BEGIN_MINUTE[index]-10)
//			afterStartTime = true;
//		boolean beforeEndTime = false;
//		if(hour<BEGIN_HOUR[index] || hour==BEGIN_HOUR[index] && min<BEGIN_MINUTE[index])
//			beforeEndTime = true;
//		if(afterStartTime && beforeEndTime)
//			return true;
//		return false;
//	}
	
	public boolean isInTime(){
		long now = System.currentTimeMillis();
		cachedCal.setMillis(now);
		int hour = cachedCal.getHourOfDay();
		if(hour>=TERMINATETIME)
			return true;
		return false;
	}

	
	public void success(NationConvoy convoy) {
		log.info("[CONVOYSUCCESS]FACTION["+convoy.nation.faction+"]");
		convoys[convoy.nation.faction] = null;
		convoy.npc.removeFromWorld();
		convoy.nation.addMoney(DEPOSITE);
		MailService mailService = Server.server.getServiceRegistry().getMailService();
		List<Player> l = convoy.getSourcePlayers();
		for(Player p:l){
			GameItem item1 = ObjectAccessor.createGameItem(1311);
			GameItem item2 = ObjectAccessor.createGameItem(REWARDITEM);
//			mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00521, "", 0, item1, 2, "COV");
			mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00521, "", 0, item2, 1, "COV");
		}
//		if (l.size() > 0) {
//			int v = DEPOSITE / l.size();
//			for (Player p : l) {
//				try {
//					PlayerTransaction tx = p.newTransaction("COV");
//					p.addMoney(v, tx, true);
//					tx.commit();
//				} catch (Exception ex) {
//					log.error(ex, ex);
//				}
//			}
//		}
		Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(convoy.nation.faction,
						peony.Messages.STRING_00522);
		Server.server.getServiceRegistry().getChatService().sendWorldMessage(
				MessageFormat.format(peony.Messages.STRING_00523, GameObject.getFactionName(convoy.nation.faction)));
		
	}
	
	public void fail(NationConvoy convoy){
		log.info("[CONVOYFAIL]FACTION["+convoy.nation.faction+"]");
		convoys[convoy.nation.faction] = null;
		convoy.nation.addMoney(FALIDEPOSITE);
		MailService mailService = Server.server.getServiceRegistry().getMailService();
		boolean[] bs = new boolean[4];
		Arrays.fill(bs, false);
		List<Player> l = convoy.getDestPlayers();
		for(Player p:l){
			bs[p.faction] = true;
			GameItem item1 = ObjectAccessor.createGameItem(1311);
			GameItem item2 = ObjectAccessor.createGameItem(REWARDITEM);
//			mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00524, "", 0, item1, 1, "COV");
//			mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00524, "", 0, item2, 1, "COV");
		}
		if (l.size() > 0) {
			int v = DEPOSITEGET / l.size();
			for (Player p : l) {
				try {
					PlayerTransaction tx = p.newTransaction("COV");
					p.addMoney(v, tx, true);
					tx.commit();
				} catch (Exception ex) {
					log.error(ex, ex);
				}
			}
		}
		Server.server.getServiceRegistry().getChatService().sendWorldMessage(
				MessageFormat.format(peony.Messages.STRING_00525, GameObject.getFactionName(convoy.nation.faction)));
		//失败国加国库资金
		for(int i=1;i<bs.length;i++){
			if(bs[i]){
				Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(i, 
						MessageFormat.format(peony.Messages.STRING_00526, 
								GameObject.getFactionName(convoy.nation.faction)));
			}
		}
	}
	
	static class DieCallback implements CreatureDieCallback{
		
		protected NationConvoy convoy;
		
		public DieCallback(NationConvoy convoy){
			this.convoy = convoy;
		}
		
		public void die(Creature c,Unit source){
			Server.server.getServiceRegistry().getNationConvoyService().fail(convoy);
		}
	}
}
