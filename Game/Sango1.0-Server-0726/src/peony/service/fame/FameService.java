 	package peony.service.fame;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import peony.db.FameDAO;
import peony.db.IBuyDAO;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerUtil;
import peony.game.Server;
import peony.game.VMap;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.player.ActorCacheService;
import peony.service.shop.IBuy;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
import peony.util.IntHashMap;

public class FameService implements Service, ServiceEventListener {

	private static final Logger log = Logger.getLogger(FameService.class);
	
	protected Map<Integer, List<Integer>> fameIds = new HashMap<Integer, List<Integer>>(); // 名人堂ID集合
	
	public static Map<Integer,Player> statuePlayer = new HashMap<Integer,Player>(); // 场景中的雕像
	
	public static Map<Integer,Fame> fames = new HashMap<Integer,Fame>(); // 雕像信息
	
	public IntHashMap<Integer> horseState = new IntHashMap<Integer>();

	protected int[] mapid = { 0, 272, 240, 352 };

	protected int posX[][] = { { 147, 222, 136, 135, 66 },{ 178, 249, 144, 193, 110 }, { 134, 196, 109, 167, 87 } };

	protected int posY[][] = { { 655, 646, 707, 615, 656 },{ 532, 552, 590, 471, 486 }, { 532, 572, 594, 475, 474 } };

	protected static final Long ONEWEEK = 7 * 24 * 60 * 60 * 1000L; // 一周时间
	
	public static int rewardItem = 2284; //演武场奖励称号物品
	public static int rewardTitle = 77; //演武场奖励称号
	public static int getItem = 2285; //演武场领取物品
	
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		try {
			loadFromDb();
		} catch (Exception e) {
			log.error(e, e);
		}
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.syncRunner.add(new Runnable(){
					public void run() {
						synchronized (statuePlayer) {
							try {
								fameIds.clear();
								Set<Integer> keys = statuePlayer.keySet();
								if(keys!=null && keys.size()>0){
									for(Player p : statuePlayer.values()){
										p.removeFromMap();
									}
								}
								statuePlayer.clear();
								fames.clear();
								FameDAO dao = Server.server.getServiceRegistry().getDbService().fameDAO;
								List<Fame> fas = dao.findFame();
								if(fas!=null && fas.size()>0){
									for(int i=0;i<fas.size();i++){
										Fame fa = fas.get(i);
										dao.makeTransient(fa);
									}
								}
								processData(Calendar.getInstance());
							} catch (Exception e) {
								log.info(e, e);
							}
						}
					}
				});
			}
		}, getScheduleTime(Calendar.getInstance()),ONEWEEK, TimeUnit.MILLISECONDS);
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.syncRunner.add(new Runnable(){
					public void run() {
						synchronized (statuePlayer) {
							for(Player p : statuePlayer.values()){
								if (p != null) {
									p.moveType = Player.STATE_STOP;
									p.setWarState(Player.PVPSTATE);
									p.moveExtended |= Player.MOVEEXT_GUILD;
									if(horseState.get(p.id)!=null && horseState.get(p.id)==1){
										p.state |= Player.STATE_RIDE; // 设置骑马状态
										p.moveType |= Player.MOVE_POINT_STATE | Player.MOVE_DETAIL | Player.MOVE_HORSE;
									}
								}
							}
						}
					}
				});
			}
		}, 0, 5000, TimeUnit.MILLISECONDS);
		Server.server.getEventManager().registerListener(this);
	}

	/**
	 * 刷新最新雕像
	 * @param playerId, 玩家ID
	 * @param mapId
	 * @param x
	 * @param y
	 */
	protected void freshStatue(int playerId, int mapId, int x, int y) {
		synchronized (statuePlayer) {
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(playerId);
			if(actor==null)
				return;
			if(actor.faction==0)
				actor = Server.server.getServiceRegistry().getDbService().playerDAO.getActor(playerId);
			VMap map = ((NoInstanceVMapManager) Server.server.getWorld().getVMapManager(mapId)).getVMaps(mapId)[0];
			Player statue = PlayerUtil.createPlayer(actor.name, actor.sex, actor.clazz, actor.faction, -1);
			statue.level = actor.level;
			statue.setVisible();
			Player p = ObjectAccessor.getPlayer(playerId);
			if (p == null)
				p = Server.server.getServiceRegistry().getPlayerService().getFromCache(playerId);
			if(p==null)
				p = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(playerId);
			if(p==null)
				return;
			statue.equipments = p.equipments.clone();
			statue.equipments.owner = statue;
			statue.activePower = p.activePower;
			statue.horseBag = p.horseBag.clone();
			statue.horseBag.owner = statue;
			if(p.relations!=null)
				statue.relations = p.relations.clone();
			statue.bag = p.bag.clone();
			for(GameItem item : statue.equipments.equs){
				if(item!=null && item.template!=null){
					statue.equip(item.template.id, item.instanceId, 0);
				}
			}
			statue.id = -p.id; // 雕像ID为负数
			statue.instanceId = -p.id; // 雕像instanceID为负数
			if (p.horseBag.horses.size() > 0) {
				if(p.horse!=null){
					int horseInstanceId = p.horse.instanceId;
					statue.horseRide(horseInstanceId, 0,-1);
					statue.horse = p.horse.clone();
					statue.horse.equs = p.horse.equs.clone1();
					statue.horse.equs.owner = statue.horse;
					horseState.put(statue.id, 1);
				}else{
					horseState.put(statue.id, 0);
				}
			}else{
				horseState.put(statue.id, 0);
			}
			statue.x = x;
			statue.y = y;
			statue.name = "[演武场]"+p.name;
			statue.setWeekCredit(p.getWeekCredit());
			statue.setGuildName(p.getGuildName());
		    statue.setRank(p.getRank());
		    statue.hp = p.hp;
		    statue.mp = p.mp;
		    statue.titles = p.titles.clone();
		    statue.chatOptions.nativeName = p.chatOptions.nativeName;
		    statue.setCredit(p.getCredit(), "FAME");
			statue.systemState = Player.SYSTEMSTATE_READY;
			map.addPlayer(statue, x, y);
			statuePlayer.put(statue.id, statue);
			saveFame(statue);
			statue.mapCell.broadcastRefreshPlayer(statue, true);
		}
	}
	
	public void processData(Calendar cal) {
		cal.setTime(new Date());
		getFameFromIbuy(cal);
		Set<Integer> keys = fameIds.keySet();
		if (keys!=null && keys.size()>0) {
			for (int faction : keys) {
				List<Integer> ids = fameIds.get(faction);
				for (int i=0; i<ids.size(); i++) {
					freshStatue(ids.get(i), mapid[faction], posX[faction-1][i],posY[faction-1][i]);
					rewardTitleItems(ids.get(i));
				}
			}
		}
	}

	/** 数据库中载入雕像信息 */
	public void loadFromDb() {
		List<Fame> fame = Server.server.getServiceRegistry().getDbService().fameDAO.findFame();
		try {
			processData0(fame);
		} catch (Exception e) {
		}
		if (fame != null && fame.size() != 0) {
			int weiIndex = 0;
			int shuIndex = 0;
			int wuIndex = 0;
			for (Fame fa : fame) {
				fames.put(fa.playerId,fa);
				VMap map = ((NoInstanceVMapManager) Server.server.getWorld()
						.getVMapManager(mapid[fa.faction]))
						.getVMaps(mapid[fa.faction])[0];
				Player statue = PlayerUtil.createPlayer(fa.name, fa.sex,fa.clazz, fa.faction, -1);
				statue.level = fa.level;
				statue.setVisible();
				statue.equipments = fa.equipments;
				statue.activePower = fa.activePower;
				statue.horseBag = fa.horseBag;
				statue.mp = fa.mp;
				statue.hp = fa.hp;
				statue.id = fa.playerId;
				statue.instanceId = fa.playerId;
				if(statue.horseBag.horses.size()>0){
					int horseInstanceId = fa.horseinstanceid;
					if(horseInstanceId!=0){
						statue.horseRide(horseInstanceId, 0,-1);
						statue.horse = statue.horseBag.getHorse(horseInstanceId);
						horseState.put(statue.id, 1);
					}else{
						horseState.put(statue.id, 0);
					}
				}else{
					horseState.put(statue.id, 0);
				}
				statue.x = fa.x;
				statue.y = fa.y;
				statue.name = fa.name;
				statue.setWeekCredit(fa.weekCredit);
				statue.setGuildName(fa.guildName);
			    statue.setRank(fa.rank);
			    statue.titles = fa.titles;
			    statue.chatOptions.nativeName = fa.chatOptions.nativeName;
			    statue.setCredit(fa.credit,"FAME");
			    statue.bag = fa.bag;
			    if(fa.faction==1){
			    	map.addPlayer(statue, posX[fa.faction-1][weiIndex], posY[fa.faction-1][weiIndex]);
			    	weiIndex++;
			    }else if(fa.faction==2){
			    	map.addPlayer(statue, posX[fa.faction-1][shuIndex], posY[fa.faction-1][shuIndex]);
			    	shuIndex++;
			    }else if(fa.faction==3){
			    	map.addPlayer(statue, posX[fa.faction-1][wuIndex], posY[fa.faction-1][wuIndex]);
			    	wuIndex++;
			    }
				statue.systemState = Player.SYSTEMSTATE_READY;
				statuePlayer.put(statue.id, statue);
			}
		} else {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_WEEK, 2);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			processData(cal);
		}
	}
	
	/** 合服使用 */
	public void processData0(List<Fame> fame){
		int[] temp = {0,0,0};
		Iterator<Fame> it = fame.iterator();
		while(it.hasNext()){
			Fame f = it.next();
			int faction = f.faction;
			if(temp[faction-1]>=5){
				it.remove();
			}else{
				temp[faction-1]++;
			}
		}
	}

	public void getFameFromIbuy(Calendar cal) {
		Date endTime = cal.getTime();
		Date startTime = new Date(endTime.getTime() - ONEWEEK);
		IBuyDAO dao = Server.server.getServiceRegistry().getDbService().ibuyDAO;
		for(int faction=GameObject.FACTION_WEI; faction<=GameObject.FACTION_WU; faction++){
			List<IBuy> ibuys = dao.getTopConsume(startTime, endTime, 5, faction);
			if(ibuys==null || ibuys.size()==0)
				continue;
			List<Integer> playerIds = new ArrayList<Integer>();
			for(IBuy ibuy : ibuys){
				playerIds.add(ibuy.playerid);
			}
			fameIds.put(faction, playerIds);
		}
	}
	
	/**
	 * 玩家修改雕像信息
	 * @param packet
	 * @param session
	 */
	public void saveFameInfo(Packet packet,ClientSession session) {
		synchronized (statuePlayer) {
			int serial = packet.getInt();
			int playerId = packet.getInt();
			Player p = (Player) session.getClient();
			if (p!=null) {
				if (p.id!=playerId) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.FAME_ADDINFO_CLIENT, "对不起，这不是属于您的雕像，无法保存您的信息");
					return;
				}
				Player statue = statuePlayer.get(-playerId);
				statue.name = "[演武场]"+p.name;
				statue.level = p.level;
				statue.clazz = p.clazz;
				statue.sex = p.sex;
				statue.faction = p.faction;
				statue.chatOptions.nativeName = p.chatOptions.nativeName;
				statue.activePower = p.activePower;
				statue.equipments = p.equipments.clone1();
				statue.equipments.owner = statue;
				statue.bag = p.bag.clone();
				statue.horseBag = p.horseBag.clone();
				statue.horseBag.owner = statue;
				statue.hp = p.hp;
			    statue.mp = p.mp;
				for(GameItem item : statue.equipments.equs){
					if(item!=null && item.template!=null){
						statue.equip(item.template.id, item.instanceId, 0);
					}
				}
				statue.setGuildName(p.getGuildName());
				statue.titles = p.titles.clone();
				if (p.horse != null) {
					statue.horse = p.horse.clone();
					statue.horse.equs = p.horse.equs.clone1();
					statue.ride();
					horseState.put(-playerId, 1);
				} else {
					statue.unRide();
					statue.horse = null;
					horseState.put(-playerId, 0);
				}
				statue.setCredit(p.getCredit(),"FAME"); 
				statue.setWeekCredit(p.getWeekCredit());
				statue.setRank(p.getRank());
				statue.relations = p.relations;
				saveFame(statue);
				Packet pt = new Packet(OpCode.FAME_ADDINFO_SERVER);
				pt.putInt(serial);
				p.send(pt);
			}
		}
	}
	
	/** 将雕像存储在数据库中 */
	public void saveFame(Player statue){
		FameDAO dao = Server.server.getServiceRegistry().getDbService().fameDAO;
		Fame fame = dao.findFameByPlayerId(statue.id);
		ActorCacheService actorCacheService = Server.server.getServiceRegistry().getActorCacheService();
		StatService service = Server.server.getServiceRegistry().getStatService();
		if(fame!=null)
			dao.makeTransient(fame);
		fame = new Fame();
		fame.playerId = statue.id;
		fame.faction = statue.faction;
		fame.x = statue.x;
		fame.y = statue.y;
		fame.mp = statue.mp;
		fame.hp = statue.hp;
		fame.activePower = statue.activePower;
		fame.clazz = statue.clazz;
		fame.level = statue.level;
		fame.rank = statue.getRank();
		fame.name = statue.name;
		fame.credit = statue.getCredit();
		fame.weekCredit = statue.getWeekCredit();
		fame.sex = statue.sex;
		fame.guildName = statue.getGuildName();
		fame.titles = statue.titles;
		fame.chatOptions = statue.chatOptions;
		fame.equipments = statue.equipments;
		fame.horseBag = statue.horseBag;
		fame.bag = statue.bag;
		int horState = horseState.get(statue.id);
		if(horState==1 && statue.horse.instanceId>0){
			fame.horseinstanceid = statue.horse.instanceId;
		} else {
			fame.horseinstanceid = 0;
		}
		if(statue.relations!=null && statue.relations.mateId > 0){
			if(actorCacheService.find(statue.relations.mateId)!=null){
				fame.mateName = actorCacheService.find(statue.relations.mateId).name;
			} else {
				fame.mateName = "";
			}
		} else {
			fame.mateName = "";
		}
		PvpInfo pvpInfo = service.getPvpInfo(-statue.id, statue.faction);
		fame.pool=pvpInfo.pool.clone();
		fames.put(statue.id, fame);
		Server.server.getServiceRegistry().getDbService().fameDAO.newEntity(fame);
	}
	
	public Player getStatue(int playerId){
		for(Player p : statuePlayer.values()){
			if(p.id==playerId)
				return p;
		}
		return null;
	}
	
//	public int getOrder(int statueId, int faction){
//		List<Integer> ids = fameIds.get(faction);
//		if(ids!=null && ids.size()>0){
//			for(int i=0;i<ids.size();i++){
//				if(ids.get(i)==statueId)
//					return i+1;
//			}
//		}
//		return -1;
//	}
	
	protected void rewardTitleItems(int playerId){
		String message = "演武场奖励。";
		int itemId = rewardItem;
		if(itemId==0)
			return;
		GameItem item = ObjectAccessor.createGameItem(itemId);
		MailService service = Server.server.getServiceRegistry().getMailService();
		service.sendSystemMail(playerId, "系统", message, "每日可去主城雕像处领取奖励", 0, item, 1, "FAMEREWARD");
	}
	
	public Fame getFame(int statueId){
		for(Fame fame : fames.values()){
			if(fame.playerId==statueId)
				return fame;
		}
		return null;
	}

	public long getScheduleTime(Calendar cal) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		cal1.set(Calendar.DAY_OF_WEEK, 2);
		if (cal1.before(cal)) {
			cal1.add(Calendar.WEEK_OF_YEAR, 1);
			return cal1.getTime().getTime()-System.currentTimeMillis();
		} else {
			return cal1.getTime().getTime()-System.currentTimeMillis();
		}
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerLoad((Player)event.param1);
			break;
		}
	}
	
	protected void processPlayerLoad(Player p){
		if(p!=null && getFame(-p.id)==null && p.titles.hasTitle(rewardTitle)){
			p.titles.removeTitle(rewardTitle);
		}
	}
	
}

