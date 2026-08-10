package peony.service.duel;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

import peony.db.RefreshNpcCall;
import peony.game.Actor;
import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DayListener;
import peony.game.DieCallback;
import peony.game.GameItem;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.MoveCallback;
import peony.game.NoEnoughSpaceException;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PlayerUtil;
import peony.game.Server;
import peony.game.Time;
import peony.game.Title;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.mail.MailService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.expansionbattle.ExpansionService;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;

/**
 * 比武招亲地图管理器
 * @author dchen
 */
public class DuelService implements Service, VMapManager, DayListener, ServiceEventListener {
	
	protected List<DuelInstance> instances = new ArrayList<DuelInstance>(); // 副本集合
	
	protected DieCallback dieCallBack = new DuelDieCallBack();
	
	protected int[] mapInfo = new int[3]; // 比武招亲地图
	
	protected int[] out = new int[3]; // 失败者传出的地图信息
	
	public List<Integer> signUps = new ArrayList<Integer>(); // 报名参加比武招亲的玩家ID集合
	
	protected static int decMoney = 500; // 报名花费金钱
	
	protected Timer timer = new Timer(); // 控制开启关闭副本
	
	protected Timer queueTimer = new Timer(); // 控制将排队等候的报名者传进战场
	
	protected List<TimeController> timeControllers = new ArrayList<TimeController>(); // 副本时间段信息
	
	protected List<TimeController> signUpControllers = new ArrayList<TimeController>(); // 报名时间段信息
	
	protected boolean canSignUp = false; // 是否报名时间
	
	protected static long ONEDAY = 24 * 60 * 60 * 1000L; // 一天时间
	
	protected int[] statueMapInfo = new int[3]; // 雕像地图信息 
	
	protected static Map<Integer,Player> statuePlayer = new HashMap<Integer,Player>(); // 场景中的雕像
	
	protected int state = 0;
	
	public static int STATE_INSTANCE_WAIT = 0; // 战前准备状态
	public static int STATE_INSTANCE_READY = 1; // 进入战斗状态
	
	protected long lastCheckTime = 0;
	
	public static int MAXPLAYERS = 7; // 每个副本限制最大人数
	
	public static int MAXINS = 10; // 每次开启最大副本数
	
	public int[] currentNpcInfo = new int[4];
	
	public int[] npcs = {8192161, 8192162, 8192160, 8192163};
	public String[] npcNames = {peony.Messages.STRING_00896, peony.Messages.STRING_00897, peony.Messages.STRING_00898, peony.Messages.STRING_00899};
	public int[] ids = new int[4];
	public int[] itemIds = {912, 913, 915, 914};
	public int[] titleIds = {62, 63, 64, 65};
	
	protected Player currentStatue;
	
	protected static int itemId = 916; // 参加比武获得奖励（小蚌锄）
	
	
	protected int token = 0;

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.
		findFile("duelbattle.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		registerManager();
		for(TimeController c : signUpControllers){
			timer.schedule(new TimerTask(){
				public void run() {
					canSignUp = true;
					Server.server.getServiceRegistry().getChatService().sendWorldMessage(
							peony.Messages.STRING_00900);
				}
			}, getScheduleTime(c.startHour, c.startMin), ONEDAY);
			timer.schedule(new TimerTask(){
				public void run() {
					canSignUp = false;
				}
			}, getScheduleTime(c.endHour, c.endMin), ONEDAY);
			if(inTime(c.startHour, c.startMin, c.endHour, c.endMin))
				canSignUp = true;
		}
		for(final TimeController c : timeControllers){
			timer.schedule(new TimerTask(){
				public void run() {
					createInstances();
					currentNpcInfo[0] = c.npcId;
					currentNpcInfo[1] = c.mapId;
					currentNpcInfo[2] = c.x;
					currentNpcInfo[3] = c.y;
				}
			}, getScheduleTime(c.startHour, c.startMin), ONEDAY);
			timer.schedule(new TimerTask(){
				public void run() {
					Server.server.syncRunner.add(new Runnable(){
						public void run() {
//							endInstances();
							RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.DUELSERVICE);
							Server.server.getWorld().schedule(call);
						}
					});
				}
			}, getScheduleTime(c.endHour, c.endMin), ONEDAY);
			if(inTime(c.startHour, c.startMin, c.endHour, c.endMin)){
				createInstances();
				currentNpcInfo[0] = c.npcId;
				currentNpcInfo[1] = c.mapId;
				currentNpcInfo[2] = c.x;
				currentNpcInfo[3] = c.y;
			}
		}
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
								}
							}
						}
					}
				});
			}
		}, 0, 5000, TimeUnit.MILLISECONDS);
	}
	
	/** 注册地图管理器 */
	protected void registerManager(){
		Server.server.getWorld().addVMapManager(this);
		Server.server.getWorld().registerVMapManager(mapInfo[0], this);
		Server.server.getEventManager().registerListener(this);
		Time.addDayListener(this);
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) throws Exception {
		Element root = doc.getRootElement();
		List<Element> battles = root.elements("battle");
		for(Element battle : battles){
			mapInfo[0] = Integer.parseInt(battle.attributeValue("mapId"));
			mapInfo[1] = Integer.parseInt(battle.attributeValue("x"));
			mapInfo[2] = Integer.parseInt(battle.attributeValue("y"));
			Element outE = battle.element("out");
			int outMapId = Integer.parseInt(outE.attributeValue("mapId"));
			int outX = Integer.parseInt(outE.attributeValue("x"));
			int outY = Integer.parseInt(outE.attributeValue("y"));
			out[0] = outMapId;
			out[1] = outX;
			out[2] = outY;
			Element pdEl = battle.element("period");
			int duration = Integer.parseInt(pdEl.attributeValue("duration"));
			List<Element> pds = pdEl.elements("pd");
			for(Element pd : pds){
				int startHour = Integer.parseInt(pd.attributeValue("startHour"));
				int startMin = Integer.parseInt(pd.attributeValue("startMin"));
				int endHour = Integer.parseInt(pd.attributeValue("endHour"));
				int endMin = Integer.parseInt(pd.attributeValue("endMin"));
				int npcId = Integer.parseInt(pd.attributeValue("npcId"));
				int mapid = Integer.parseInt(pd.attributeValue("mapId"));
				int x = Integer.parseInt(pd.attributeValue("x"));
				int y = Integer.parseInt(pd.attributeValue("y"));
				TimeController p = new TimeController(startHour,startMin,endHour,endMin,duration,npcId,mapid,x,y);
				timeControllers.add(p);
			}
			List<Element> signs = pdEl.elements("sign");
			for(Element si : signs){
				int startHour = Integer.parseInt(si.attributeValue("startHour"));
				int startMin = Integer.parseInt(si.attributeValue("startMin"));
				int endHour = Integer.parseInt(si.attributeValue("endHour"));
				int endMin = Integer.parseInt(si.attributeValue("endMin"));
				TimeController p = new TimeController(startHour,startMin,endHour,endMin,duration,0,0,0,0);
				signUpControllers.add(p);
			}
			Element statueE = battle.element("statue");
			int statueMapId = Integer.parseInt(statueE.attributeValue("mapId"));
			int statueX = Integer.parseInt(statueE.attributeValue("x"));
			int statueY = Integer.parseInt(statueE.attributeValue("y"));
			statueMapInfo[0] = statueMapId;
			statueMapInfo[1] = statueX;
			statueMapInfo[2] = statueY;
		}
	}
	
	/** 创建副本 */
	protected void createInstances(){
		int wantInstanceAccount = getWantInstanceAccount();
		if(wantInstanceAccount==0)
			return;
		wantInstanceAccount = Math.min(MAXINS, wantInstanceAccount);
		for(int i=0;i<wantInstanceAccount;i++){
			VMap map = VMapUtil.create(this, Server.server.getWorld(), mapInfo[0], Server.server.revision);
			DuelInstance instance = new DuelInstance(i+1, map, this);
			instances.add(instance);
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
		}
		queueTimer = new Timer();
		queueTimer.schedule(new TimerTask(){
			public void run() {
				enterInstance();
			}
		}, 0, 2000);
//		removeStatue();
	}
	
	/** 副本结束 */
	public void endInstances(){
		int winner = getWinner();
		processWinner(winner);
		for(DuelInstance instance : instances){
			instance.transPlayers();
			Server.server.getEventManager().unregisterListener(instance);
		}
		instances.clear();
		try {
			queueTimer.cancel();
		} catch (Exception e) {
			
		}
		state = STATE_INSTANCE_WAIT;
		signUps.clear();
		token = 0;
	}
	
	/** 将玩家传送进副本 */
	protected void enterInstance(){
		Iterator<Integer> it = signUps.iterator();
		while(it.hasNext()){
			int id = it.next();
			Player player = ObjectAccessor.getPlayer(id);
			if(player!=null){
				try {
					player.goMap(mapInfo[0], mapInfo[1], mapInfo[2]);
					//进战场时角色恢复满血满蓝
					if(player.hp<player.maxhp){
					   player.setHp(player.maxhp, true);
					}
					if(player.mp<player.maxmp){
					   player.setMp(player.maxmp,true);
					}
				} catch (VMapException e) {
					e.printStackTrace();
				}
			}else{
				// 如果此时玩家离线则取消参赛资格
				it.remove();
			}
		}
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if(check){
			return Server.server.getWorld().addPlayerToMap(player, out[0], out[1], out[2], true);
		}else{
			DuelInstance instance = null;
			if(player.getVMap().instance instanceof DuelInstance){
				instance = getUsableDuelInstanceExcept(player.getVMap().instance.getId());
			}else{
				if(!signUps.contains(player.id))
					return null;
				instance = getUsableDuelInstance();
				if(instance!=null){
					PlayerTransaction tx = player.newTransaction("DUEL");
					GameItem item = ObjectAccessor.createGameItem(itemId);
					try {
						player.bag.addGameItemComplete(item, 1, tx, true);
						tx.commit();
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
						Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_00901, "", 0, item, 1, "DUEL");
					}
				}
			}
			if(instance!=null){
				if(player.party!=null){
					player.party.leave(player.id);
				}
				player.removeFromMap();
				Iterator<Integer> it = signUps.iterator();
				while(it.hasNext()){
					int id = it.next();
					if(player.id==id)
						it.remove();
				}
				instance.addPlayer(player);
				instance.map.addPlayer(player, x, y);
				return instance.map;
			}
			return null;
		}
	}
	
	/** 比武招亲报名 */
	public void signUp(Player p) throws DuelException{
		if(p!=null){
			if(!canSignUp)
				throw new DuelException(peony.Messages.STRING_00902);
			if(Server.server.getServiceRegistry().getFlagBattleFieldVMapManager().signups.containsKey(p.id))
				throw new DuelException(peony.Messages.STRING_00903);
			if(Server.server.getServiceRegistry().getTongBattleVMapManager().isInTongBattle(p)==1)
				throw new DuelException(peony.Messages.STRING_00904);
			if(signUps.contains(p.id))
				throw new DuelException(peony.Messages.STRING_00905);
			PlayerTransaction tx = p.newTransaction("DUEL");
			try {
				p.decMoney(decMoney, tx, true);
				tx.commit();
				signUps.add(p.id);
				LogUtil.logDuleSignUp(p, decMoney);
			} catch (NoEnoughValueException e) {
				tx.rollback();
				throw new DuelException(peony.Messages.STRING_00906);
			}
		}
	}
	
	/** 根据报名人数确定开启副本的数量 */
	protected int getWantInstanceAccount(){
		if(signUps.size()==0)
			return 0;
		int account = signUps.size()/MAXPLAYERS;
		int residue = signUps.size()%MAXPLAYERS;
		if(account==0 && residue>0)
			return 1;
		if(account>0 && residue==0)
			return account;
		return account+1;
	}
	
	/** 根据id获取Instance */
	protected DuelInstance getInstanceById(int id){
		for(DuelInstance instance : instances){
			if(instance.id==id){
				return instance;
			}
		}
		return null;
	}
	
	/** 获取可进入的DuelInstance,按ID优先顺序 */
	public DuelInstance getUsableDuelInstance(){
		for(int i=0;i<instances.size();i++){
			DuelInstance ins = getInstanceById(i+1);
			if(ins.players.size()<MAXPLAYERS){
				return ins;
			}
		}
		return null;
	}
	
	public DuelInstance getUsableDuelInstanceExcept(int instanceId){
		for(int i=0;i<instances.size();i++){
			DuelInstance ins = getInstanceById(i+1);
			if(ins.players.size()<MAXPLAYERS && ins.players.size()>0 && ins.id!=instanceId){
				return ins;
			}
		}
		return null;
	}

	public CreatureDieCallback creatureDieCallback() {
		return null;
	}

	public DieCallback dieCallback() {
		return dieCallBack;
	}

	public void mapChanged(GameMapDefinition mapDef) {
		
	}

	public MoveCallback moveCallback() {
		return null;
	}

	/** 脱离卡死 */
	public void outPrison(Player p) {
		if(p.map.map!=null){
		    int[] pos = p.map.map.mapDef.mapInfo.getPathFinder().tryOutPrison(p.x, p.y);
		    if(pos==null){
				int[] relivePoint = p.map.map.getRelivePoint(p.faction);
				try{
					int oldMapId = p.map.map.getId();
					int oldX = p.x;
					int oldY = p.y;
					p.goMap(relivePoint[0], relivePoint[1], relivePoint[2]);
					Server.server.getEventManager().fireEvent(
						new ServiceEvent(ServiceEvent.EVENT_PLAYER_OUTPRISON_RELIVEPOINT,
						p,oldMapId,oldX,oldY));
				}catch(VMapException e) {
				}
			}else{
			    try{
					p.goMap(p.map.map.getId(), pos[0], pos[1]);
				}catch (VMapException e) {
				}
			}
		}
	}

	/** 玩家移除地图 */
	public void removeFromMap(Player player) {
		if(player!=null && player.getVMap().instance!=null && (player.getVMap().instance instanceof DuelInstance)){
			player.getVMap().instance.removePlayer(player);
		}
	}
	
	public void update(int diff) {
		for(Instance instance : instances){
			instance.update(diff);
		}
		if(System.currentTimeMillis()-lastCheckTime>=1*1000){
			if(instances.size()>0 && signUps.size()==0 && state==STATE_INSTANCE_READY){
				// 开始监听是否有胜出者(所有战场中只剩余一人)，如果有胜出者则比武结束
				if(getTotalPlayersInInstance()<=1){
					endInstances();
				}
			}
			if(token == 0 && signUps.size() == 0 && getTotalPlayersInInstance()==2 && state==STATE_INSTANCE_READY){
				for(Instance instance : instances){
					DuelInstance in = (DuelInstance)instance;
					List<Player> players = in.players;
					if(players!=null && players.size()>0){
						Iterator<Player> it = players.iterator();
						while(it.hasNext()){
							Player p = it.next();
							p.message(-1, peony.Messages.STRING_00907, -1, -1);
							token = 1;
						}
					}
				}
			}
			lastCheckTime = System.currentTimeMillis();
		}
	}
	
	/** 获取胜利者ID */
	protected int getWinner(){
		if(getTotalPlayersInInstance()==1){
			int winnerPlayerId = 0;
			for(DuelInstance instance : instances){
				for(Player p : instance.players){
					winnerPlayerId = p.id;
				}
			}
			return winnerPlayerId;
		}else if(getTotalPlayersInInstance()>1){
			List<Integer> leavingPlayers = new ArrayList<Integer>();
			List<Integer> scores = new ArrayList<Integer>();
			for(DuelInstance instance : instances){
				for(Player p : instance.players){
					leavingPlayers.add(p.id);
					if(instance.scores.get(p.id)==null)
						scores.add(0);
					else
						scores.add(instance.scores.get(p.id));
				}
			}
			int maxScore = 0;
			int maxScoreIndex = 0;
			for(int i=0;i<scores.size();i++){
				int score = scores.get(i);
				if(score>maxScore){
					maxScore = score;
					maxScoreIndex = i;
				}else if(score==maxScore){
					int lastPlayerId = leavingPlayers.get(maxScoreIndex);
					int currentPlayerId = leavingPlayers.get(i);
					Player lastPlayer = ObjectAccessor.getPlayer(lastPlayerId);
					Player currPlayer = ObjectAccessor.getPlayer(currentPlayerId);
					if(lastPlayer!=null && currPlayer!=null && currPlayer.getCredit()>lastPlayer.getCredit()){
						maxScore = score;
						maxScoreIndex = i;
					}
				}
			}
			int winnerPlayerId = leavingPlayers.get(maxScoreIndex);
			return winnerPlayerId;
		}else if(getTotalPlayersInInstance()==0){
			return 0;
		}
		return 0;
	}
	
	/** 产生胜利者 */
	protected void processWinner(int playerId){
		try {
			LogUtil.logDuelWinner(playerId);
		} catch (Exception e) {
		}
		if(playerId==0){
			// 流战
			return;
		}
		// 树立雕像
		freshStatue(playerId, statueMapInfo[0], statueMapInfo[1], statueMapInfo[2]);
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p!=null){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(playerId, peony.Messages.STRING_00908);
			p.pool.setInt("DUELTILE", 0);
			Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.format(peony.Messages.STRING_00909, p.name));
		}
		// 刷新公主
		freshNpc(currentNpcInfo[0], currentNpcInfo[1], currentNpcInfo[2], currentNpcInfo[3]);
		// 排行榜数据
		ids[getIndex(currentNpcInfo[0])] = playerId;
		
		//发驸马称号奖励的邮件
		int index = getLastWinnerIndex(playerId);		
		String name = npcNames[index];//驸马称号的名字		         
		if(index != -1){
			int titleId = itemIds[index];
			MailService mailService = Server.server.getServiceRegistry().getMailService();
			mailService.sendSystemMailAsync(playerId,"系统","称号奖励","恭喜勇士您已获得" + name +"称号，公主已经被您那忧郁的眼神唏嘘的胡渣英勇的身姿所迷倒，希望您下次再接再厉，继续抱得美人归。",0,ObjectAccessor.createGameItem(titleId),1,null);
		}
		
	}
	
	/** 刷新雕像 */
	protected void freshStatue(int playerId, int mapId, int x, int y) {
		synchronized (statuePlayer) {
			if(currentStatue!=null)
				currentStatue.removeFromMap();
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(playerId);
			VMap map = ((ExpansionService) Server.server.getWorld().getVMapManager(mapId)).relationMap;
			Player statue = PlayerUtil.createPlayer(actor.name, actor.sex, actor.clazz, actor.faction, -1);
			statue.level = actor.level;
			statue.setVisible();
			Player p = ObjectAccessor.getPlayer(playerId);
			if (p == null)
				p = Server.server.getServiceRegistry().getPlayerService().getFromCache(playerId);
			if(p==null)
				p = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(playerId);
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
			statue.id = -p.id;
			statue.instanceId = -p.id;
			if (p.horseBag.horses.size() > 0) {
				if(p.horse!=null){
					int horseInstanceId = p.horse.instanceId;
					statue.horseRide(horseInstanceId, 0,-1);
					statue.horse = p.horse.clone();
					statue.horse.equs = p.horse.equs.clone1();
					statue.horse.equs.owner = statue.horse;
				}
			}
			statue.x = x;
			statue.y = y;
			statue.name = MessageFormat.format(peony.Messages.STRING_00910,p.name);
			statue.setWeekCredit(p.getWeekCredit());
			statue.setGuildName(p.getGuildName());
		    statue.setRank(p.getRank());
		    statue.hp = p.hp;
		    statue.mp = p.mp;
		    statue.titles = p.titles.clone();
		    statue.skills = p.skills.clone();
		    statue.pool = p.pool.clone();
		    statue.chatOptions.nativeName = p.chatOptions.nativeName;
		    statue.setCredit(p.getCredit(), "DUEL");
		    statue.systemState = Player.SYSTEMSTATE_READY;
		    StatService service = Server.server.getServiceRegistry().getStatService();
		    PvpInfo pvpInfoStatue = service.getPvpInfo(statue.id, statue.faction);
		    PvpInfo pvpInfo = service.getPvpInfo(p.id, p.faction);
		    pvpInfoStatue.pool=pvpInfo.pool.clone();
			map.addPlayer(statue, x, y);
			statue.mapCell.broadcastRefreshPlayer(statue, true);
			statuePlayer.put(statue.id, statue);
			currentStatue = statue;
		}
	}
	
	protected void freshNpc(int npcId, int mapId, int x, int y){
		VMap map = ((ExpansionService) Server.server.getWorld().getVMapManager(mapId)).relationMap;
		for(TimeController t : timeControllers){
			int npc = t.npcId;
			GameObject n = map.getCreatureById(npc);
			if(n!=null){
				n.removeFromWorld();
			}
		}
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, npcId);
		VMapUtil.addCreature(map, x, y, (GameMapNPC) gmo, true, 0, null);
	}
	
	/** 清楚雕像 */
	protected void removeStatue(){
		synchronized (statuePlayer) {
			Iterator<Integer> itor = statuePlayer.keySet().iterator();
			StatService service = Server.server.getServiceRegistry().getStatService();
			while (itor.hasNext()) {
				 int key = itor.next();
				 service.removePvpInfo(key);
			}
			statuePlayer.clear();
		}
	}
	
	/** 获取当前所有副本中玩家的数量 */
	public int getTotalPlayersInInstance(){
		int total = 0;
		for(DuelInstance instance : instances){
			if(instance.players.size()>0)
				total += instance.players.size();
		}
		return total;
	}
	
	/** 移除副本 */
	protected void removeInstance(DuelInstance ins){
		Iterator<DuelInstance> it = instances.iterator();
		while(it.hasNext()){
			Instance instance = it.next();
			if(ins.id==instance.getId())
				it.remove();
		}
	}
	
	public Date getScheduleTime(int hour, int min){
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		if (cal1.before(cal)) {
			cal1.add(Calendar.DAY_OF_YEAR, 1);
			return cal1.getTime();
		}
		return cal1.getTime();
	}
	
	public boolean inTime(int sHour, int sMin, int eHour, int eMin){
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
	
	public Player getStatue(int playerId){
		for(Player p : statuePlayer.values()){
			if(p.id==playerId)
				return p;
		}
		return null;
	}
	
	private int getIndex(int npcId){
		for(int i=0;i<this.npcs.length;i++){
			if(npcs[i]==npcId)
				return i;
		}
		return 0;
	}
	
	public int isWinner(int playerId){
		for(int i=0;i<ids.length;i++){
			if(ids[i]==playerId)
				return i;
		}
		return -1;
	}
	
	public int getLastWinnerIndex(int playerId){
		int index = -1;
		for(int i=0;i<ids.length;i++){
			if(ids[i]==playerId)
				index = i;
		}
		return index;
	}

	public void dayChanged() {
		for(int i=0;i<currentNpcInfo.length;i++){
			currentNpcInfo[i] = 0;
			ids[i] = 0;
		}
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_LOADED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_PLAYER_LOADED:
				processPlayerLoad((Player)event.param1);
				break;
		}
	}
	
	private void processPlayerLoad(Player p){
		if(p!=null){
			for(int i=0;i<titleIds.length;i++){
				if(p.titles.hasTitle(titleIds[i]) && isWinner(p.id)==-1){
					p.titles.removeTitle(titleIds[i]);
					
					MailService mailService = Server.server.getServiceRegistry().getMailService();
					mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004,  "驸马称号", "您的驸马称号已到期，已获取的称号会在您重新登录游戏的时候删除掉，下次比武招亲不久就会开始，公主尚待字闺中，等你来挑战哦！", 0, 
							null, 1, null);
				}
			}
			//修复展示称号的BUG
			if(p.buffs.getBuffByID(270) != null){
				Title title = p.titles.getCurrentEquipTitle();
				if(title == null || title.buffId != 270)
					p.buffs.removeBuff(270);
			}
		}
	}
	
	public int getLeavingMinute(){
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		long dis = 60 * 60 * 1000L;
		for(TimeController con : timeControllers){
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.HOUR_OF_DAY, con.startHour);
			c.set(Calendar.MINUTE, con.startMin);
			long d = c.getTimeInMillis()-cal.getTimeInMillis();
			if(d<dis && d>0){
				dis = d;
			}
		}
		int min = (int) (dis/60000);
		return min;
	}
	
	public boolean canSignUp(){
		return canSignUp;
	}
}
