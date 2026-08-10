package peony.game.battlefield;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

import ch.javasoft.util.Arrays;

import peony.game.ChatOption;
import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.ErrorHandler;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.GatherEndCall;
import peony.game.GatherUnit;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.MoveCallback;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.OutPrisonUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.World;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.util.SimpleTimer;
import edu.emory.mathcs.backport.java.util.Collections;

public class FlagBattleFieldVMapManager implements VMapManager, Service, ServiceEventListener  {

	private static final Logger log = Logger.getLogger(FlagBattleFieldVMapManager.class);
	
	
	protected static int MAX_PLAYER = 6;
	protected static int MIN_PLAYER = 3;
	
	protected LinkedHashMap<Integer, SignUp> signups = new LinkedHashMap<Integer, SignUp>();
	
	protected static int[] latestTen = new int[10] ;
	protected static int latestTenNum = 0 ;

	protected World world;

	protected List<FlagBattleFieldDefinition> definitions = new ArrayList<FlagBattleFieldDefinition>();
	protected List<FlagBattleFieldInstance> instances = new ArrayList<FlagBattleFieldInstance>();

	protected TimeTrigger[] triggers = null;
	
	protected TimeTrigger currentTrigger = null;

//	protected IntRange[] levelRanges;
	
	protected int[][] outs = new int[3][];
	
	protected LevelDef[] levels = null;
	
	protected Map<Integer,FlagBattleFieldInstance> playerid2instance = new HashMap<Integer,FlagBattleFieldInstance>();
	
	protected Map<Integer,Forbid> playerid2forbid = new HashMap<Integer,Forbid>();
	
	protected Map<Integer,TimeOut> playerid2TimeOut = new HashMap<Integer,TimeOut>();
	
	protected GatherEndCall flagGatherEndCall = new FlagGatherEndCall();

	protected MoveCallback moveCallback = new FlagMoveCallback();
	
	protected DieCallback dieCallback = new FlagBattleFieldDieCallback();
	
	protected int lastCheckCreateTime = Time.currTime;
	
	protected SimpleTimer timer;
	
	public boolean checkFlagPosition = true;
	
	protected List<FlagBattleBoss> bosss = new ArrayList<FlagBattleBoss>();
	
	public FlagBattleFieldVMapManager(World world) throws Exception {
		this.world = world;
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("Areas/flagbattlefield.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		world.addVMapManager(this);
	}
	
	public GatherEndCall getGatherEndCall(){
		return flagGatherEndCall;
	}

	@SuppressWarnings("unchecked")
	protected void parse(Document doc) throws Exception {
		Element elBf = doc.getRootElement();
		int maxplayer = Integer.parseInt(elBf.attributeValue("maxplayer"));
		definitions
				.add(createDefinition(elBf, "wei_shu", GameObject.FACTION_WEI,
						GameObject.FACTION_SHU, maxplayer));
		definitions
				.add(createDefinition(elBf, "wei_wu", GameObject.FACTION_WEI,
						GameObject.FACTION_WU, maxplayer));
		definitions
				.add(createDefinition(elBf, "shu_wu", GameObject.FACTION_SHU,
						GameObject.FACTION_WU, maxplayer));
		for (FlagBattleFieldDefinition def : definitions) {
			world.registerVMapManager(def.mapId, this);
		}
		List elOuts = elBf.elements("out");
		outs[0] = getInts(((Element) elOuts.get(0)).attributeValue("point"));
		outs[1] = getInts(((Element) elOuts.get(1)).attributeValue("point"));
		outs[2] = getInts(((Element) elOuts.get(2)).attributeValue("point"));
		List elLevels = elBf.elements("level");
		levels = new LevelDef[elLevels.size()];
		for(int i=0;i<levels.length;i++){
			int min = Integer.parseInt(((Element)elLevels.get(i)).attributeValue("begin"));
			int max = Integer.parseInt(((Element)elLevels.get(i)).attributeValue("end"));
			int credit = Integer.parseInt(((Element)elLevels.get(i)).attributeValue("credit"));
			levels[i] = new LevelDef(min,max,credit);
		}
		List times = elBf.elements("time");
		Iterator ite = times.iterator();
		List<TimeTrigger> ts = new ArrayList<TimeTrigger>(times.size());
		while (ite.hasNext()) {
			Element el = (Element) ite.next();
			TimePoint start = getTimePoint(el.attributeValue("start"));
			TimePoint end = getTimePoint(el.attributeValue("end"));
			int duration = Integer
					.parseInt(el.attributeValue("durationsecond"));
			ts.add(new TimeTrigger(start, end, duration));
		}
		triggers = new TimeTrigger[ts.size()];
		ts.toArray(triggers);
	}

	@SuppressWarnings("unchecked")
	protected FlagBattleFieldDefinition createDefinition(Element elBf,
			String name, int faction1, int faction2, int maxplayer) {
		Element el = elBf.element(name);
		Element elMap = el.element("map");
		int mapId = Integer.parseInt(elMap.attributeValue("id"));
		int[][] ins = new int[2][];
		int[][] outs = new int[2][];
		int[][] aims = new int[2][2];
		List aimEls = el.elements("aim");
		List inEls = el.elements("in");
		ins[0] = getInts(((Element) inEls.get(0)).attributeValue("point"));
		ins[1] = getInts(((Element) inEls.get(1)).attributeValue("point"));
		Element elFlag = el.element("flag");
		int flagId = Integer.parseInt(elFlag.attributeValue("id"));
		int x = Integer.parseInt(elFlag.attributeValue("x"));
		int y = Integer.parseInt(elFlag.attributeValue("y"));
		aims[0][0] = Integer.parseInt(((Element)aimEls.get(0)).attributeValue("x"));
		aims[0][1] = Integer.parseInt(((Element)aimEls.get(0)).attributeValue("y"));
		aims[1][0] = Integer.parseInt(((Element)aimEls.get(1)).attributeValue("x"));
		aims[1][1] = Integer.parseInt(((Element)aimEls.get(1)).attributeValue("y"));
		List<Element> bossEls = el.elements("boss");
		for(Element bossEl : bossEls){
			int minLevel = Integer.parseInt(bossEl.attributeValue("begin"));
			int maxLevel = Integer.parseInt(bossEl.attributeValue("end"));
			int bossId = Integer.parseInt(bossEl.attributeValue("bossId"));
			int bossX = Integer.parseInt(bossEl.attributeValue("x"));
			int bossY = Integer.parseInt(bossEl.attributeValue("y"));
			FlagBattleBoss battleBoss = new FlagBattleBoss(bossId,mapId,maxLevel,minLevel,bossX,bossY);
			bosss.add(battleBoss);
		}
		FlagBattleFieldDefinition definition = new FlagBattleFieldDefinition(
				maxplayer, flagId,x,y, mapId, ins, outs, faction1, faction2,aims);
		return definition;
	}

	protected int[] getInts(String s) {
		String[] ss = s.split(",");
		int[] ret = new int[ss.length];
		for (int i = 0; i < ss.length; i++) {
			ret[i] = Integer.parseInt(ss[i]);
		}
		return ret;
	}

	protected TimePoint getTimePoint(String s) {
		String[] ss = s.split(":");
		return new TimePoint(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		Forbid forbid = playerid2forbid.get(player.id);
		if(forbid!=null){
			int[] out = outs[player.faction-1];
			return world.addPlayerToMap(player, out[0], out[1],
					out[2], false);
		}
		FlagBattleFieldInstance instance = playerid2instance.get(player.id);
		if(instance!=null){
			boolean instanceTran = player.getVMap()!=null&&player.getVMap().instance==instance;
			if(instanceTran){
				if(instance.state==FlagBattleFieldInstance.STATE_INIT){
					int startTime = instance.startTime;
					int t = (startTime-Time.currTime);
					int min = t/(60*1000);
					
					String s = null;
					if(min!=0){
						s = MessageFormat.format("戰場將在{0}分鐘以后開啟",min);
					}else{
						int sec = t/(1000);
						s = MessageFormat.format("戰場將在{0}秒后開啟", sec);
					}
					throw new VMapException(s);
				}
			}
			VMap map = instance.getMap(mapId);
			if (map != null) {
				map.instance.addPlayer(player);
				player.removeFromMap();
				map.addPlayer(player, x, y);
				if(!instanceTran){
					if(player.party!=null){
						player.party.leave(player.id);
					}
				}
				playerid2TimeOut.remove(player.id);
				return map;
			}
			throw new VMapException("戰場已經不存在");
		}else{
			int[] out = outs[player.faction-1];
			return world.addPlayerToMap(player, out[0], out[1],
					out[2], false);
		}
	}

	public void signup(Player player,int serial){
		if(!canSignup()){
			ErrorHandler.sendErrorMessage(player.session, serial, OpCode.BATTLEFIELD_SIGNUP_CLIENT, "還沒到報名時間");
			return;
		}
		if (signups.containsKey(player.id)){
			ErrorHandler.sendErrorMessage(player.session, serial, OpCode.BATTLEFIELD_SIGNUP_CLIENT, "已經報名");
			return;
		}
		if (playerid2instance.containsKey(player.id)){
			ErrorHandler.sendErrorMessage(player.session, serial, OpCode.BATTLEFIELD_SIGNUP_CLIENT, "已經報名");
			return;
		}
		if  (playerid2forbid.containsKey(player.id)){
			ErrorHandler.sendErrorMessage(player.session, serial, OpCode.BATTLEFIELD_SIGNUP_CLIENT, "你稍后才可以再次報名");
			return;
		}
		if (player.getVMap().instance!=null){
			ErrorHandler.sendErrorMessage(player.session, serial, OpCode.BATTLEFIELD_SIGNUP_CLIENT, "狀態錯誤,不能報名");
			return;
		}
		int levelIndex = getLevelIndex(player.level);
		if(levelIndex==-1){
			ErrorHandler.sendErrorMessage(player.session, serial, OpCode.BATTLEFIELD_SIGNUP_CLIENT, "等級不夠");
			return;
		}
		int money = player.level * 10;
		PlayerTransaction tx = player.newTransaction("FBF");
		try {
			player.decMoney(money, tx, true);
			signups.put(player.id, new SignUp(player.ref(),Time.currTime));
			tx.commit();
			player.pool.setString(Player.PROPERTY_FLAGBATTLE_SIGNUPMAP, player.map.id+","+player.x+","+player.y);
			Packet pt = new Packet(OpCode.BATTLEFIELD_SIGNUP_SERVER);
			pt.putInt(serial);
			pt.putInt(money);
			player.send(pt);
		} catch (NoEnoughValueException e) {
			tx.rollback();
			ErrorHandler.sendErrorMessage(player.session, serial, OpCode.BATTLEFIELD_SIGNUP_CLIENT, MessageFormat.format("需要{0}錢才能報名",money));
			return;
		}
		
	}
	
	
	public boolean canSignup(){
		return currentTrigger != null;
	}

	public boolean removeSignup(Player player) {
		return signups.remove(player.id) != null ;
	}

	public void removeFromMap(Player player) {
		FlagBattleFieldInstance instance = (FlagBattleFieldInstance)player.getVMap().instance;
		instance.removePlayer(player);
	}

	public void update(int diff) {
		Iterator<Forbid> iteForbid = playerid2forbid.values().iterator();
		while(iteForbid.hasNext()){
			Forbid f = iteForbid.next();
			if(Time.currTime>=f.endTime){
				iteForbid.remove();
			}
		}
		Iterator<TimeOut> iteTimeout = playerid2TimeOut.values().iterator();
		while(iteTimeout.hasNext()){
			TimeOut t = iteTimeout.next();
			if(Time.currTime>=t.endTime){
				iteTimeout.remove();
				FlagBattleFieldInstance instance = playerid2instance.get(t.ref.id);
				if(instance!=null){
					log.info("[UNBINDTIMEOUT]ID["+t.ref.id+"]");
					instance.unAttach(t.ref, t.faction);
				}
				signups.remove(t.ref.id);
				forbid(t.ref.id);
			}
		}
		Iterator<FlagBattleFieldInstance> ite = instances.iterator();
		while (ite.hasNext()) {
			FlagBattleFieldInstance instance = ite.next();
			instance.update(diff);
			if (instance.state == FlagBattleFieldInstance.STATE_END)
				ite.remove();
			
		}
		checkStart();
		if (currentTrigger != null)
			checkCreate();
	}
	
	@SuppressWarnings("unchecked")
	protected void checkCreate() {
		if(Time.currTime-lastCheckCreateTime<60*1000)
			return;
		if(signups.size()>0&&currentTrigger!=null){ 
			List<Player>[][] players = new List[levels.length][];
			//先按等级分组
			for(int i=0;i<players.length;i++){
				players[i] = new List[3];
				players[i][0] = new ArrayList();
				players[i][1] = new ArrayList();
				players[i][2] = new ArrayList();
			}
			for(SignUp su:signups.values()){
				Player p = ObjectAccessor.getPlayer(su.ref.instanceId);
				if(p!=null&&p.systemState==Player.SYSTEMSTATE_READY){
					int factionIndex = getFactionIndex(p.faction);
					int levelIndex = getLevelIndex(p.level);
					if(factionIndex==-1||levelIndex==-1)
						continue;
					players[levelIndex][factionIndex].add(p);
				}
			}
			for(FlagBattleFieldInstance instance:instances){
				checkInstance(instance,players);
			}
			for(int i=0;i<players.length;i++){
				arrange(players[i],levels[i]);
			}
			lastCheckCreateTime = Time.currTime;
		}
	}
	
	protected void checkInstance(FlagBattleFieldInstance instance,List<Player>[][] players){
		if(instance.state!=FlagBattleFieldInstance.STATE_ENDING&&instance.state!=FlagBattleFieldInstance.STATE_END){
			int count = instance.getFactionCount(instance.def.faction1);
			if(count<MAX_PLAYER){
				List<Player> l = players[getLevelIndex(instance.levelDef.min)][getFactionIndex(instance.def.faction1)];
				for(int i=0;i<MAX_PLAYER-count;i++){
					if(l.size()>0){
						attachInstance(instance,l);
					}else{
						break;
					}
				}
			}
			count = instance.getFactionCount(instance.def.faction2);
			if(count<MAX_PLAYER){
				List<Player> l = players[getLevelIndex(instance.levelDef.min)][getFactionIndex(instance.def.faction2)];
				for(int i=0;i<MAX_PLAYER-count;i++){
					if(l.size()>0){
						attachInstance(instance,l);
					}else{
						break;
					}
				}
			}

		}
	}
	
	protected int getFactionIndex(int faction){
		if(faction==GameObject.FACTION_WEI)
			return 0;
		else if(faction==GameObject.FACTION_SHU)
			return 1;
		else if(faction==GameObject.FACTION_WU)
			return 2;
		return -1;
	}
	
	protected int getLevelIndex(int level){
		for(int i=0;i<levels.length;i++){
			if(levels[i].in(level))
				return i;
		}
		return -1;
	}
	
	protected void arrange(List<Player>[] players,LevelDef def){
		List<List<Player>> l = new ArrayList<List<Player>>(3);
		l.add(players[0]);
		l.add(players[1]);
		l.add(players[2]);
		Collections.sort(l, new Comparator<List<Player>>(){
			public int compare(List<Player> o1, List<Player> o2) {
				return o2.size() - o1.size();
			}
		});
		if(!arrange1(l.get(0),l.get(1),def)){
			return;
		}else{
			arrange(players,def);
		}
		
	}
	
	protected boolean arrange1(List<Player> side1,List<Player> side2,LevelDef levelDef){
		if(side1.size()<MIN_PLAYER||side2.size()<MIN_PLAYER)
			return false;
		int faction1 = side1.get(0).faction;
		int faction2 = side2.get(0).faction;
		FlagBattleFieldDefinition def = getDefinition(faction1, faction2);
		if(def==null){
			throw new IllegalArgumentException();
		}
		
		FlagBattleFieldInstance instance = createInstance(def,levelDef);
		for(int i=0;i<MIN_PLAYER;i++){
			attachInstance(instance,side1);
			attachInstance(instance,side2);
		}
		for(int i=0;i<MAX_PLAYER-MIN_PLAYER;i++){
			if(side1.size()>0&&side2.size()>0){
				attachInstance(instance,side1);
				attachInstance(instance,side2);			
			}else{
				break;
			}
		}
		
		// 记录开始日志
		LogUtil.logFlagBattleStart(instance.id, faction1, faction2, instance.attachedRefs);
		return true;
	}
	
	protected void attachInstance(FlagBattleFieldInstance instance,List<Player> l){
		Player p = l.remove(0);
		setLatestTenUserWaitTime(p.id);
		removeSignup(p);		
		instance.attach(p);
	}
	
	
	protected FlagBattleFieldInstance createInstance(FlagBattleFieldDefinition def,LevelDef levelDef){
		FlagBattleFieldInstance instance = new FlagBattleFieldInstance(this,def,currentTrigger.duration,levelDef);
		VMap map = VMapUtil.create(this, world, def.mapId,1024,1024,null);
		map.sameCellCount = 15;
		map.adjCellCount1 = 5;
		map.adjCellCount1 = 3;
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
		instance.setVMap(map);
		instances.add(instance);
		addBoss(map, levelDef);
		return instance;
	}
	
	protected void addBoss(VMap map, LevelDef levelDef){
		int minLevel = levelDef.min;
		FlagBattleBoss battleBoss = null;
		for(FlagBattleBoss boss : bosss){
			if(boss.mapId==map.getId() && boss.minLevel==minLevel){
				battleBoss = boss;
			}
		}
		if(battleBoss!=null){
			ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
			GameMapObject gmo = GameMapObject.findByID(proj, battleBoss.bossId);
			VMapUtil.addCreature(map, battleBoss.x, 
					battleBoss.y, (GameMapNPC) gmo, true, 0, Server.server.revision);
		}
	}
	
	protected FlagBattleFieldDefinition getDefinition(int faction1, int faction2) {
		if ((faction1 == GameObject.FACTION_WEI && faction2 == GameObject.FACTION_SHU)
				|| (faction1 == GameObject.FACTION_SHU && faction2 == GameObject.FACTION_WEI)) {
			return definitions.get(0);
		}
		if ((faction1 == GameObject.FACTION_WEI && faction2 == GameObject.FACTION_WU)
				|| (faction1 == GameObject.FACTION_WU && faction2 == GameObject.FACTION_WEI)) {
			return definitions.get(1);
		}
		if ((faction1 == GameObject.FACTION_SHU && faction2 == GameObject.FACTION_WU)
				|| (faction1 == GameObject.FACTION_WU && faction2 == GameObject.FACTION_SHU)) {
			return definitions.get(2);
		}
		return null;
	}
	

	protected void checkStart() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		if (currentTrigger != null) {
			if (!currentTrigger.in(hour, min)) {
				currentTrigger = null;
				signups.clear();
			}
		}
		if (currentTrigger == null) {
			for (int i = 0; i < triggers.length; i++) {
				if (triggers[i].in(hour, min)) {
					currentTrigger = triggers[i];
					signups.clear();
					playerid2TimeOut.clear();
					playerid2forbid.clear();
					lastCheckCreateTime = Time.currTime;
					Server.server.getServiceRegistry().getChatService()
							.sendSystemMessage(ChatOption.SYSTEM, "系統",
									"南郡之戰開放,可以到國都戰場管理員報名參加");
					break;
				}
			}
		}
	}

	public void shutdown() {

	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
				ServiceEvent.EVENT_PLAYER_LOGINED,
				ServiceEvent.EVENT_PLAYER_OUTPRISON_RELIVEPOINT,
		};
	}
	
	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			playerLogouted((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			playerLogined((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_OUTPRISON_RELIVEPOINT:
			outPrison((Player)event.param1,(Integer)event.param3,(Integer)event.param4);
			break;
		}
	}
	
	protected void outPrison(Player player,int x,int y){
		Instance instance = player.getVMap().instance;
		if(instance!=null){
			if(instance instanceof FlagBattleFieldInstance){
				if(player.flag!=null){
					((FlagBattleFieldInstance)instance).throwFlag(player,x,y);
				}
			}
		}
	}
	
	protected void playerLogined(Player player){
		playerid2TimeOut.remove(player.id);
	}
		
	protected void playerLogouted(Player player) {
		FlagBattleFieldInstance instance = playerid2instance.get(player.id);
		if (instance != null) {
			if (instance.state == FlagBattleFieldInstance.STATE_INIT
					|| instance.state == FlagBattleFieldInstance.STATE_STARTED) {
				if (player.logouted) {
					log.info("[UNBINDLOGOUT]ID["+player.id+"]");
					instance.unAttach(player.ref(),player.faction);
					forbid(player.id);
				} else {
					playerid2TimeOut.put(player.id, new TimeOut(player.ref(),
							Time.currTime + 3 * 60 * 1000,player.faction));
				}
			}else{
				log.info("[UNBINDLOGOUT1]ID["+player.id+"]");
				instance.unAttach(player.ref(),player.faction);
			}
		}else{
			if(signups.containsKey(player.id)){
				if(player.logouted){
					signups.remove(player.id);
					forbid(player.id);
				}else{
					playerid2TimeOut.put(player.id, new TimeOut(player.ref(),
							Time.currTime + 3 * 60 * 1000,player.faction));
				}
			}
		}
	}
	
	public void bind(Player p,FlagBattleFieldInstance instance){
		playerid2instance.put(p.id, instance);
	}

	public void unbind(GameObjectRef ref,FlagBattleFieldInstance instance){
		FlagBattleFieldInstance oldInstance = playerid2instance.get(ref.id);
		if(oldInstance==instance){
			playerid2instance.remove(ref.id);
			if(playerid2TimeOut.remove(ref.id)!=null){
				forbid(ref.id);
			}
		}else{
			log.info("[UNBINDERROR]ID["+ref.id+"]");
		}
	}
	
	public void tran(Player p){
		FlagBattleFieldInstance instance = playerid2instance.get(p.id);
		if(instance!=null){
			instance.tran(p);
		}
	}
	
	public void forbid(int playerId){
		playerid2forbid.put(playerId, new Forbid(playerId,Time.currTime+15*60*1000));
	}
	
	public DieCallback dieCallback(){
		return dieCallback;
	}
	
	public CreatureDieCallback creatureDieCallback(){
		return null;
	}
	
	public MoveCallback moveCallback(){
		return moveCallback;
	}
	
    public void outPrison(Player p){
    	OutPrisonUtil.outPrison(p);
    }
	
	
	/**
     * 处理地图数据变化，尽可能地更新已有对象的属性。
     */
    public void mapChanged(GameMapDefinition mapDef) {
        for (FlagBattleFieldInstance instance : instances) {
            VMap map = instance.getMap();
            if (map.mapDef.mapInfo.getGlobalID() == mapDef.mapInfo.getGlobalID()) {
                map.mapDef = mapDef;
                map.mapChanged();
            }
        }
    }
    
    public int isInFlagBattle(Player p){
    	if(p!=null){
    		for(FlagBattleFieldInstance instance : instances){
    			if(instance.isInFlagBattle(p)==1)
    				return 1;
    		}
    	}
    	return 0;
    }
    
    
    public void setLatestTenUserWaitTime(Integer id){
    	SignUp su = (SignUp)signups.get(id) ;
    	if(su!=null)
    	{
    		if(latestTenNum<10)
        	{
        		latestTen[latestTenNum] = Time.currTime - su.createTime ;
        		latestTenNum++ ;
        	}
        	else
        	{
        		for(int i=0;i<9;i++)
        		{
        			latestTen[i] = latestTen[i+1] ;
        		}
        		latestTen[9] = Time.currTime  - su.createTime ;
        	}
    	}
    }
    
    /**
     * 获得平均等待时间
     * @return
     */
    public static int getAverageWaitTime(){
    	int num = 0 ;
    	if(latestTenNum<=0)
    		return num ;
    	int[] waitTimes = Arrays.copyOf(latestTen, latestTenNum) ;
    	for(int i=0;i<waitTimes.length;i++)
    	{
    		num += waitTimes[i] ;
    	}
    	num = (num/1000+30)/60/waitTimes.length ;
    	return num ;
    }
    
}

class SignUp {
	public GameObjectRef ref;
	public int createTime;
	
	public SignUp(GameObjectRef ref,int createTime){
		this.ref = ref;
		this.createTime = createTime;
	}
}

class TimeTrigger {
	public TimePoint start, end;
	public int duration;

	public TimeTrigger(TimePoint start, TimePoint end, int duration) {
		this.start = start;
		this.end = end;
		this.duration = duration;
	}
	
	public boolean in(int hour,int min){
		int t = hour*60+min;
		return t>=(start.hour*60+start.min)&&t<=(end.hour*60+end.min);
	}
}

class FlagGatherEndCall implements GatherEndCall{

	public void gatherEnd(GatherUnit unit, Player p) {
		FlagBattleFieldInstance instance = (FlagBattleFieldInstance)p.getVMap().instance;
		instance.catchedFlag(p);
	}
}

class FlagMoveCallback implements MoveCallback{
	public void moved(Player p) {
		FlagBattleFieldInstance instance = (FlagBattleFieldInstance)p.getVMap().instance;
		instance.moveAt(p);
	}
	
}

class Forbid{
	int playerId;
	int endTime;
	public Forbid(int playerId,int endTime){
		this.playerId = playerId;
		this.endTime = endTime;
	}
}

class TimeOut{
	GameObjectRef ref;
	int endTime,faction;
	public TimeOut(GameObjectRef ref,int endTime,int faction){
		this.ref = ref;
		this.faction = faction;
		this.endTime = endTime;
	}
}


class StartNotify implements Runnable{
	
	public String message;
	
	public StartNotify(String message){
		this.message = message;
	}
	
	public void run(){
		Server.server.getServiceRegistry().getChatService().sendSystemMessage(ChatOption.SYSTEM, "系統", message);
	}
}
//class FlagDieCallback implements DieCallback {
//
//	public void die(Player player, Unit source) {
//		Skill reliveSkill = null;
//		for (Skill skill : player.skills.getSkills()) {
//			if ((skill.getType() & Skill.TYPE_RELIVE) != 0
//					&& (skill.getTargetType() & Skill.TARGET_FLAG_SELF) != 0
//					&& skill.getLevel() != 0
//					&& !player.coolDowns.contains(skill.getCDGroup())) { // 被动复活技能
//				reliveSkill = skill;
//				break;
//			}
//		}
//		player.reliveOptions = new ReliveOptions(Time.currTime + 60 * 1000);
//		int[] relivePoint = player.map.map.getRelivePoint(player.faction);
//		ReliveOption option = new ReliveOption(ReliveOption.NORMAL, "释放", 14,
//				relivePoint[0], relivePoint[1], relivePoint[2]);
//		player.reliveOptions.addOption(option, false);
//		if (reliveSkill != null) {
//			ReliveOption skillReliveOption = new ReliveOption(
//					ReliveOption.SKILL_PASSIVE, reliveSkill.getName(), 14,
//					player.map.id, player.x, player.y);
//			skillReliveOption.skill = reliveSkill;
//			player.reliveOptions.addOption(skillReliveOption, false);
//		}
//
//		player.send(player.reliveOptions.getRelivePacket());
//		FlagBattleFieldInstance instance = (FlagBattleFieldInstance)player.getVMap().instance;
//		instance.died(player, source);
//		
//		
//	}
//}
