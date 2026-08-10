package peony.service.towerdefend;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.GameItem;
import peony.game.GameMapDefinition;
import peony.game.LogUtil;
import peony.game.MoveCallback;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionException;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.party.Party;
import peony.game.party.PartyMember;
import peony.game.party.PartyService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.util.TimeUtil;

/**
 * 塔防系统管理器
 * @author dchen
 */
public class TowerDefendService implements Service, VMapManager, ServiceEventListener {

	private static final Logger log = Logger.getLogger(TowerDefendService.class);
	protected List<TowerDefend> signUps = new ArrayList<TowerDefend>(); // 塔防报名集合
	protected TowerDefendConfig config; //配置信息
	protected List<TowerDefendInstance> instances = new ArrayList<TowerDefendInstance>(); //副本集合
	DieCallback dieCallBack = new TowerDefendDieCallBack();
	protected static String[] endPiroid = {"14:00","16:30","20:00","23:30"};
	protected static long ONEDAY = 24 * 3600 * 1000L;
	protected static Random random = new Random();
	protected static int[][] notDefend = {
			{178,448},{265,526},{350,589},{455,653}
	};
	protected static int[][] notAttack = {
			{521,226},{605,278},{692,350},{796,408}
	};

	public void startup() throws Exception {
		loadConfig();
		registerManager();
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				// 定时检测防守方进攻方，发现配对则开启战场
				try {
					checkAndOpenInstance();
				} catch (Exception e) {
				}
			}
		}, 0, 1000, TimeUnit.MILLISECONDS);
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				if(signUps.size()==1){
					Server.server.getServiceRegistry().getChatService().
					sendWorldMessage("已经有英勇的队伍，在充满冒险与谋略的斗阵场报名成功，向天下英雄发起挑战！");
				}
			}
		}, 0, 10*60*1000L, TimeUnit.MILLISECONDS);
		Server.server.getEventManager().registerListener(this);
		for(int i=0;i<endPiroid.length;i++){
			String p = endPiroid[i];
			int hour = Integer.parseInt(p.split(":")[0]);
			int min = Integer.parseInt(p.split(":")[1]);
			Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
				public void run() {
					synchronized (this) {
						signUps.clear();
					}
				}
			}, TimeUtil.getScheduleTimeMills(new Date(), hour, min), ONEDAY, TimeUnit.MILLISECONDS);
		}
	}
	
	protected void loadConfig(){
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("Areas/towerdefend.xml");
		try{
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		}catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) throws Exception {
		Element root = doc.getRootElement();
		Element instanceEl = root.element("instance");
		int instanceMapId = Integer.parseInt(instanceEl.attributeValue("mapId"));
		int instanceDuration = Integer.parseInt(instanceEl.attributeValue("duration"));
		int minLevel = Integer.parseInt(instanceEl.attributeValue("minLevel"));
		Element itemTotowerEl = instanceEl.element("itemTotower");
		List<Element> itbsEl = itemTotowerEl.elements("itb");
		List<TDItemToTower> itbList = new ArrayList<TDItemToTower>();
		for(Element itbEl : itbsEl){
			int itemId = Integer.parseInt(itbEl.attributeValue("itemId"));
			int towerId = Integer.parseInt(itbEl.attributeValue("towerId"));
			String type = itbEl.attributeValue("type");
			TDItemToTower itemToTower = new TDItemToTower(itemId, towerId, type);
			itbList.add(itemToTower);
		}
		Element inEl = instanceEl.element("in");
		Element attack = inEl.element("attack");
		int ainX = Integer.parseInt(attack.attributeValue("x"));
		int ainY = Integer.parseInt(attack.attributeValue("y"));
		int[] attackPosition = new int[2];
		attackPosition[0] = ainX;
		attackPosition[1] = ainY;
		Element defend = inEl.element("defend");
		int dinX = Integer.parseInt(defend.attributeValue("x"));
		int dinY = Integer.parseInt(defend.attributeValue("y"));
		int[] defendPosition = new int[2];
		defendPosition[0] = dinX;
		defendPosition[1] = dinY;
		Element outEl = instanceEl.element("out");
		int[][] out = new int[3][3];
		Element wei = outEl.element("wei");
		int weiMapId = Integer.parseInt(wei.attributeValue("mapId"));
		out[0][0] = weiMapId;
		int weiX = Integer.parseInt(wei.attributeValue("x"));
		out[0][1] = weiX;
		int weiY = Integer.parseInt(wei.attributeValue("y"));
		out[0][2] = weiY;
		Element shu = outEl.element("shu");
		int shuMapId = Integer.parseInt(shu.attributeValue("mapId"));
		out[1][0] = shuMapId;
		int shuX = Integer.parseInt(shu.attributeValue("x"));
		out[1][1] = shuX;
		int shuY = Integer.parseInt(shu.attributeValue("y"));
		out[1][2] = shuY;
		Element wu = outEl.element("wu");
		int wuMapId = Integer.parseInt(wu.attributeValue("mapId"));
		out[2][0] = wuMapId;
		int wuX = Integer.parseInt(wu.attributeValue("x"));
		out[2][1] = wuX;
		int wuY = Integer.parseInt(wu.attributeValue("y"));
		out[2][2] = wuY;
		Element centerEl = instanceEl.element("centers");
		List<Element> centersEl = centerEl.elements("center");
		List<int[]> centers = new ArrayList<int[]>();
		for(Element center : centersEl){
			int x = Integer.parseInt(center.attributeValue("x"));
			int y = Integer.parseInt(center.attributeValue("y"));
			int bossId = Integer.parseInt(center.attributeValue("bossID"));
			int[] c = new int[3];
			c[0] = x;
			c[1] = y;
			c[2] = bossId;
			centers.add(c);
		}
		Element initEl = instanceEl.element("initItem");
		List<Element> items = initEl.elements("item");
		List<TDInit> inits = new ArrayList<TDInit>();
		for(Element item : items){
			int itemId = Integer.parseInt(item.attributeValue("id"));
			int count = Integer.parseInt(item.attributeValue("count"));
			int type = Integer.parseInt(item.attributeValue("type"));
			TDInit init = new TDInit(type, itemId, count);
			inits.add(init);
		}
		Element defendEl = instanceEl.element("defend");
		int flagId = Integer.parseInt(defendEl.attributeValue("flag"));
		int flagX = Integer.parseInt(defendEl.attributeValue("x"));
		int flagY = Integer.parseInt(defendEl.attributeValue("y"));
		int[] flag = new int[3];
		flag[0] = flagId;
		flag[1] = flagX;
		flag[2] = flagY;
		Element rewardEl = instanceEl.element("reward");
		Element winEl = rewardEl.element("win");
		List<Element> itemEl = winEl.elements("item");
		List<Integer> itemIds = new ArrayList<Integer>();
		List<Integer> count = new ArrayList<Integer>();
		for(Element item : itemEl){
			int itemId = Integer.parseInt(item.attributeValue("id"));
			int c = Integer.parseInt(item.attributeValue("count"));
			itemIds.add(itemId);
			count.add(c);
		}
		Element rankEl = winEl.element("rank");
		int rank = Integer.parseInt(rankEl.attributeValue("amount"));
		Reward reward = new Reward(Reward.TYPE_WIN, itemIds, count, rank);
		Element failEl = rewardEl.element("fail");
		Element rankEl1 = failEl.element("rank");
		int rank1 = Integer.parseInt(rankEl1.attributeValue("amount"));
		Reward reward1 = new Reward(Reward.TYPE_FAIL, null, null, rank1);
		config = new TowerDefendConfig(instanceMapId, defendPosition, attackPosition, out, 
				instanceDuration*60*1000, centers, itbList, inits, flag, minLevel, reward, reward1);
	}
	
	/** 注册地图管理器 */
	protected void registerManager(){
		Server.server.getWorld().addVMapManager(this);
		Server.server.getWorld().registerVMapManager(config.mapId, this);
	}
	
	@SuppressWarnings("unchecked")
	protected void checkAndOpenInstance(){
		synchronized (this) {
			if(instances.size()>=TowerDefendConfig.MAXINSTANCE)
				return;
			Iterator<TowerDefend> iterator = signUps.iterator();
			while(iterator.hasNext()){
				TowerDefend td = iterator.next();
				if(!canGo(td))
					iterator.remove();
			}
			List<TowerDefend> wei = new ArrayList<TowerDefend>();
			List<TowerDefend> shu = new ArrayList<TowerDefend>();
			List<TowerDefend> wu = new ArrayList<TowerDefend>();
			for(TowerDefend td : signUps){
				if(td.faction==1)
					wei.add(td);
				else if(td.faction==2)
					shu.add(td);
				else if(td.faction==3)
					wu.add(td);
			}
			if((wei.size()==0 && shu.size()==0) || (wei.size()==0 && wu.size()==0) 
					|| (shu.size()==0 && wu.size()==0) || (wei.size()==0 && shu.size()==0 && wu.size()==0))
				return;
			Iterator<TowerDefend> itWei = wei.iterator();
			Iterator<TowerDefend> itShu = shu.iterator();
			Iterator<TowerDefend> itWu = wu.iterator();
			Iterator[] its = {itWei, itShu, itWu};
			boolean flag = true;
			int startFaction = (itWei.hasNext() ? 1 : (itShu.hasNext() ? 2 : (itWu.hasNext() ? 3: 1)));
			while(flag){
				try {
					Iterator<TowerDefend> it1 = its[startFaction-1];
					TowerDefend td1 = it1.next();
					td1.type = random.nextInt(2);
					int nextFaction = (startFaction==3 ? 1 : (startFaction+1));
					if(nextFaction==2 && !its[nextFaction-1].hasNext()){
						nextFaction = 3;
					}
					Iterator<TowerDefend> it2 = its[nextFaction-1];
					TowerDefend td2 = it2.next();
					td2.type = (td1.type==0 ? 1 : 0);
					startFaction = (nextFaction==3 ? 1 : (nextFaction+1));
					it1.remove();
					it2.remove();
					VMap map = VMapUtil.create(this, Server.server.getWorld(), config.mapId, Server.server.revision);
					map.manager =  this;
					TowerDefendInstance instance = new TowerDefendInstance(map, config, (td1.type==0 ? td2 : td1), (td1.type==0 ? td1 : td2), this);
					instances.add(instance);
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
					signUps.remove(td1);
					signUps.remove(td2);
					goToInstance(td1, instance);
					goToInstance(td2, instance);
					if(instances.size()>=TowerDefendConfig.MAXINSTANCE)
						return;
					LogUtil.logTowerDefendBattles(td1, td2);
				} catch (Exception e) {
					flag = false;
				}
			}
		}
	}
	
	protected boolean canGo(TowerDefend td){
		PartyService service = Server.server.getServiceRegistry().getPartyService();
		int partyId = td.partyId;
		Party party = service.getPartyById(partyId);
		if(party==null)
			return false;
		if(party.getCount()<2)
			return false;
		return true;
	}
	
	protected void goToInstance(TowerDefend td, TowerDefendInstance instance){
		PartyService service = Server.server.getServiceRegistry().getPartyService();
		int partyId = td.partyId;
		Party party = service.getPartyById(partyId);
		if(party==null)
			return;
		List<PartyMember> members = party.members;
		for(PartyMember m : members){
			Player p = ObjectAccessor.getPlayer(m.player.id);
			if(p!=null){
				try {
					p.goMap(config.mapId, config.getInPosition(td.type)[0], config.getInPosition(td.type)[1]);
				} catch (VMapException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		PartyService partyService = Server.server.getServiceRegistry().getPartyService();
		if(player.party==null || partyService.getParty(player.id)==null || !isAheadInInstance(player)){
			return Server.server.getWorld().addPlayerToMap(player, config.out[player.faction-1][0], 
					config.out[player.faction-1][1], config.out[player.faction-1][2], true);
		}else{
			TowerDefendInstance instance = getInstanceByPartyId(player.party.id);
			
			if(player.getVMap().getId()!=config.mapId){
				initItems(player, instance);
				player.removeFromMap();
				instance.playerState.put(player.id, 1);
			}else{
				instance.playerState.put(player.id, 0);
				player.removeFromMap();
				instance.playerState.put(player.id, 1);
			}
			
			instance.addPlayer(player);
			instance.map.addPlayer(player, x, y);
			return instance.map;
		}
	}
	
	protected void initItems(Player player, TowerDefendInstance instance){
		if(player.party==null || (player.party!=null && player.party.memberInfo(player.id).isLeader)){
			List<TDInit> inits = config.inits;
			for(TDInit init : inits){
				int type = init.type;
				int itemId = init.itemId;
				int count = init.count;
				if(type==instance.getType(player)){
					GameItem item = ObjectAccessor.createGameItem(itemId);
					PlayerTransaction tx = player.newTransaction("TOWERDEFEND");
					try {
						player.bag.addGameItemComplete(item, count, tx, true);
						tx.commit();
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
						e.printStackTrace();
					}
				}
			}
		}
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
	
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void removeFromMap(Player player) {
		synchronized (this) {
			for(TowerDefendInstance instance : instances){
				instance.removePlayer(player);
			}
		}
	}

	public void update(int diff) {
		synchronized (this) {
			for(TowerDefendInstance instance : instances){
				instance.update(diff);
			}
		}
	}

	/**
	 * 塔防战报名
	 * @param p,报名玩家
	 * @param type,类型(0位守方,1为攻方)
	 */
	public void signUp(Player player) throws TowerDefendException {
		synchronized (this) {
			if (player != null) {
				if (player.party == null || player.party.leader.getId() != player.id)
					throw new TowerDefendException("只有队长才能报名");
				if(isInQueue(player))
					throw new TowerDefendException("这支队伍已经报过名");
				if(getInstanceByPartyId(player.party.id)!=null)
					throw new TowerDefendException("您上一场激烈的斗阵尚在酣战当中，请您完成后，再开始新一场斗阵比赛。");
				boolean canSignUp = true;
				for(PartyMember m : player.party.members){
					if(m.getLevel()<config.MINLEVEL){
						canSignUp = false;
						break;
					}
				}
				if(!canSignUp)
					throw new TowerDefendException(MessageFormat.format("队伍中的成员必须达到{0}级", config.MINLEVEL));
				TowerDefend td = new TowerDefend(player);
				signUps.add(td);
				if(signUps.size()==1){
					Server.server.getServiceRegistry().getChatService().
					sendWorldMessage("已经有英勇的队伍，在充满冒险与谋略的斗阵场报名成功，向天下英雄发起挑战！");
				}
				LogUtil.logTowerDefendSignUp(player);
			}
		}
	}
	
	/** 是否在报名队列中 */
	protected boolean isInQueue(Player p){
		for(TowerDefend t : signUps){
			if(p.party!=null && p.party.id==t.partyId)
				return true;
		}
		return false;
	}
	
	protected boolean isAheadInInstance(Player p){
		for(TowerDefendInstance instance : instances){
			if(instance.attack.partyId==p.party.id)
				return true;
			if(instance.defend.partyId==p.party.id)
				return true;
		}
		return false;
	}
	
	protected TowerDefendInstance getInstanceByPartyId(int partyId){
		for(TowerDefendInstance instance : instances){
			if(instance.attack.partyId==partyId || instance.defend.partyId==partyId)
				return instance;
		}
		return null;
	}
	
	public void removeInstance(TowerDefendInstance instance){
		synchronized (this) {
			instances.remove(instance);
		}
	}
	
	/** 玩家所处的塔防底座的中心位置 */
	public int[] getNearestCenter(Player p, String towerType){
		if(p!=null){
			List<int[]> centers = config.centers;
			for(int[] c : centers){
				if(p.distance(c[0], c[1])<=25*25){
					if(canTower(towerType, c[0], c[1]))
						return c;
				}
			}
		}
		return null;
	}
	
	private boolean canTower(String towerType, int x, int y){
		int[][] arr = null;
		if(towerType.equals(TDItemToTower.TYPE_ATTACK))
			arr = notAttack;
		else
			arr = notDefend;
		for(int[] a : arr){
			if(a[0]==x && a[1]==y)
				return false;
		}
		return true;
	}
	
	public int getItemToTowerId(int itemId){
		return config.getItemToTowerId(itemId);
	}
	
	public TDItemToTower getItemToTower(int itemId){
		return config.getItemToTower(itemId);
	}
	
	public boolean isTower(int id){
		return config.isTower(id);
	}
	
	public String getTowerType(int id){
		return config.getTowerType(id);
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
		try {
			if(p!=null && p.getVMap().getId()!=config.mapId){
				for(int itemId : config.referItems){
					int count = p.bag.getGameItemCount(itemId);
					if(count>0){
						PlayerTransaction tx = p.newTransaction("TOWERDEFEND");
						GameItem item = p.bag.removeGameItemIngoreInstanceId(itemId, count, tx, false);
						if(item!=null)
							tx.commit();
						else
							tx.rollback();
					}
				}
				p.buffs.removeBuff(config.BUFF);
			}
		} catch (TransactionException e) {
		}
	}

}
