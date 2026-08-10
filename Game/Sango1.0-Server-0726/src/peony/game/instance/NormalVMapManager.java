package peony.game.instance;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.MoveCallback;
import peony.game.NormalDieCallback;
import peony.game.ObjectAccessor;
import peony.game.OutPrisonUtil;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.World;
import peony.game.party.Party;
import peony.game.party.PartyMember;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.stat.NMMapManagerStatistics;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.intcoll.IntHashMap;

public class NormalVMapManager implements VMapManager,Service,ServiceEventListener{
	
	private static final Logger log = Logger.getLogger(NormalVMapManager.class);
	
	protected World world;
	protected List<NormalInstanceDefinition> definitions = new ArrayList<NormalInstanceDefinition>();
	protected IntHashMap<NormalInstanceDefinition> mapid2definitions = new IntHashMap<NormalInstanceDefinition>();
	protected List<NormalInstance> instances = new LinkedList<NormalInstance>();
	protected IntHashMap<List<NormalInstance>> playerid2instances = new IntHashMap<List<NormalInstance>>();
	protected DieCallback dieCallback;
	protected AtomicInteger idgen = new AtomicInteger(0);
	
	public boolean leavePartyTransferOn = true;
	
//	public static int MAX_TIMES = 5;
	
	public NormalVMapManager(World world) throws Exception{
		this.world = world;
		this.dieCallback = new NormalDieCallback();
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("Areas/normalinstances.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		world.addVMapManager(this);
		
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) throws Exception{
		Element root = doc.getRootElement();
		for(Iterator ite = root.elementIterator("instance");ite.hasNext();){
			Element elInstance = (Element)ite.next();
			String name = elInstance.attributeValue("name");
			int maxPlayer = Integer.parseInt((elInstance.attributeValue("maxplayer")));
			int minLevel = Integer.parseInt(elInstance.attributeValue("minlevel"));
			int refreshSecond = Integer.parseInt(elInstance.attributeValue("refreshsecond"));
			IntArray mapIds = new IntArray();
			for(Iterator ite1 = elInstance.elementIterator("map");ite1.hasNext();){
				Element elMap = (Element)ite1.next();
				int mapId = Integer.parseInt(elMap.attributeValue("id"));
				mapIds.add(mapId);
				world.registerVMapManager(mapId, this);
			}
			int maxTimes = Integer.parseInt(elInstance.attributeValue("maxTimes"));
			int usePower = Integer.parseInt(elInstance.attributeValue("usepower"));
			NormalInstanceDefinition definition = new NormalInstanceDefinition(name,idgen.incrementAndGet(),maxPlayer,minLevel,refreshSecond,maxTimes,usePower);
			definition.mapIds = mapIds;
			addDefinition(definition);
		}
	}
	
	protected void addDefinition(NormalInstanceDefinition definition){
		definitions.add(definition);
		for(int i=0;i<definition.mapIds.length();i++){
			int mapId = definition.mapIds.get(i);
			mapid2definitions.put(mapId, definition);
		}
	}
	
	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_MEMBER_LEAVED, // 角色在进入地图以后发送load信息
				ServiceEvent.EVENT_MEMBER_ADDED,
				ServiceEvent.EVENT_PARTY_DESTROIED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_MEMBER_LEAVED:
			memberLeaved((Party)event.param1,(PartyMember)event.param2);
			break;
		case ServiceEvent.EVENT_MEMBER_ADDED:
			memberAdded((Party)event.param1,(PartyMember)event.param2);
			break;
		case ServiceEvent.EVENT_PARTY_DESTROIED:
			partyDestroied((Party)event.param1);
			break;
		}
	}
	
	protected void partyDestroied(Party party) {
		if (leavePartyTransferOn) {
			synchronized (party) {
				for (PartyMember member : party.members) {
					if (member != party.leader) {
						addLeaveAction(member);
					}
				}
			}
		}
	}
	
	protected void memberLeaved(Party party, PartyMember member) {
		if (leavePartyTransferOn) {
			addLeaveAction(member);
		}
	}
	
	protected void addLeaveAction(PartyMember member){
		if (member.player != null && member.player.getVMap() != null
				&& member.player.getVMap().instance != null&&member.player.getVMap().instance instanceof NormalInstance) {
			NormalInstance instance = (NormalInstance)member.player.getVMap().instance;
			instance.addPartyLeaveAction(member.player);
		}
	}
	
	protected void memberAdded(Party party, PartyMember member) {
		if (leavePartyTransferOn) {
			if (member.player != null
					&& member.player.getVMap() != null
					&& member.player.getVMap().instance != null
					&& member.player.getVMap().instance instanceof NormalInstance) {
				if (party.leader.player != null
						&& party.leader.player.getVMap() != null
						&& party.leader.player.getVMap().instance == member.player
								.getVMap().instance) {
					NormalInstance instance = (NormalInstance) member.player
							.getVMap().instance;
					instance.removePartyLeaveAction(member.player);
				}
			}
		}
	}
	
	
	protected void out(Player player,int mapId){
		NormalInstanceDefinition def = mapid2definitions.get(mapId);
		if (def != null) {
			GameMapDefinition mapDef = VMapUtil.getDefinition(mapId);
			int[] relivePoint = null;
			if (player.faction == GameObject.FACTION_WEI)
				relivePoint = mapDef.mapInfo.renascenceWei;
			else if (player.faction == GameObject.FACTION_SHU)
				relivePoint = mapDef.mapInfo.renascenceShu;
			else if (player.faction == GameObject.FACTION_WU)
				relivePoint = mapDef.mapInfo.renascenceWu;
			try {
				world.addPlayerToMap(player, relivePoint[0], relivePoint[1],
						relivePoint[2], false);
			} catch (VMapException e) {
				log.error(e,e);
			}
		}
	}
	
	
	public void clear(int playerId){
		playerid2instances.remove(playerId);
	}
	
	
	
//	protected VMap getHistoryInstance(Player p,int ownerId){
//		List<NormalInstance> l = playerid2instances.get(ownerId);
//		boolean ret = false;
//		if(l==null){
//			
//		}
//	}
	
//	protected boolean goHistoryInstance(Player p,int ownerId,int mapId,int x,int y,boolean create) throws VMapException{
//		List<NormalInstance> l = playerid2instances.get(ownerId);
//		if(l==null&&create){
//			l = new LinkedList<NormalInstance>();
//			playerid2instances.put(ownerId, l);
//		}
//		if(l==null)
//			return false;
//		Iterator<NormalInstance> ite = l.iterator();
//		while(ite.hasNext()){
//			NormalInstance instance = ite.next();
//			VMap map = instance.getMap(mapId);
//			if(map!=null){
//				if(instance.isTimeOut()){
//					ite.remove();
//				}
//			}
//		}
//	}
	
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	protected VMap getHistoryInstanceMap(GameObjectRef ref,int mapId){
		List<NormalInstance> l = playerid2instances.get(ref.id);
		if(l==null)
			return null;
		Iterator<NormalInstance> ite = l.iterator();
		while(ite.hasNext()){
			NormalInstance instance = ite.next();
			VMap map = instance.getMap(mapId);
			if(map!=null)
				return map;
		}
		return null;
	}
	
	protected VMap in(Player player,VMap map,int x,int y,boolean out) throws VMapException{
		try {
			map.instance.addPlayer(player);
			player.removeFromMap();
			map.addPlayer(player, x, y);
			
			// 记录日志
			LogUtil.logEnterInstance(player, (NormalInstance)map.instance, false);
			return map;
		} catch (VMapException e) {
			if(out){
				out(player,map.getId());
			}
			throw e;
		}
	}
	
	/**
	 * 进入逻辑：
	 * 如果是刚登录上来或者从副本的一张地图到另外一张地图，那么永远寻找的是自己的副本进度，如果自己的进度
	 * 过期，那么就被传送回墓地
	 * 如果是从副本外传送进来，那么先看是否有队伍，如果有，那么就用队长的进度。如果没有用自己的进度
	 * 如果队长没有进度，那么就建立一个新的进度，并把自己的进度设置成跟队长相同
	 */
	public VMap addToMap(Player player, int mapId, int x, int y,boolean check) throws VMapException {
		if(check){
			VMap map = getHistoryInstanceMap(player.ref(),mapId);
			if(map==null){
				out(player,mapId);
				return null;
			}else{
				return in(player,map,x,y,true);
			}
		}else{
			VMap oldMap = player.getVMap();
			if(oldMap!=null){
				if(oldMap.instance!=null){ //如果原来的地图有副本
					VMap newMap = oldMap.instance.getMap(mapId);
					if(newMap!=null){//如果就在当前副本，并且从一张地图到另外一张地图
						player.removeFromMap();
						newMap.instance.addPlayer(player);
						newMap.addPlayer(player, x, y);
						
						// 记录日志
						LogUtil.logEnterInstance(player, (NormalInstance)newMap.instance, false);
						return newMap;
					}else{
						throw new VMapException("不能从一个副本转移到另外一个副本");
					}
				}else{
					NormalInstance instance = null;
					NormalInstanceDefinition definition = mapid2definitions.get(mapId);
					int times = player.getTodayInstanceTimes(definition.id);
					if(player.level<definition.minLevel)
						throw new VMapException(MessageFormat.format("副本需要{0}才能进入", definition.minLevel));
					if (player.party == null
							|| player.party.members.size() == 1) {  //如果是自己一个人，那么先看自己是否有进度，如果有用自己的，如果没有就重建一个进度
						VMap map = getHistoryInstanceMap(player.ref(), mapId);
						if (map == null) {
							definition.checkEnter(player, times);
							times++;
							instance = createInstance(definition);							
						}else{
							instance = (NormalInstance) map.instance;
						}
					}else{ 
//						if(player.party.leader.player==player){//如果是队长，先看自己是否有进度，如果有用自己的，如果没有
//							
//						}else{ //如果不是队长，那么先看队长是否有进度
							VMap map = getHistoryInstanceMap(player.party.leader.player.ref(), mapId);
							if(map!=null){//如果队长有进度，那么用队长的进度
								instance = (NormalInstance)map.instance;
								if(!instance.refs.contains(player.ref())){
									definition.checkEnter(player, times);
									times++;
								}
							}else{//如果队长没进度，那么寻找当前是否有在副本里的队员
								instance = getPartyInstance(player,mapId); 
								if(instance!=null){  //如果队员有进度，那么用队员的进度
									if(!instance.refs.contains(player.ref())){
										definition.checkEnter(player, times);
										times++;
									}
								}else{//如果队员没有进度，那么看是否自己有进度，如果有用自己的进度，没有创建进度
									map = getHistoryInstanceMap(player.ref(), mapId);
									if (map == null) {
										definition.checkEnter(player, times);
										times++;
										instance = createInstance(definition);							
									}else{
										instance = (NormalInstance) map.instance;
									}
								}
							}
//						}
					}
					clearInstanceAndAdd(mapId,player.ref(),instance);
					VMap map = instance.getMap(mapId);
					instance.addPlayer(player);
					player.removeFromMap();
					map.addPlayer(player, x, y);
					player.setTodayInstanceTimes(definition.id, times);
					
					// 记录日志
					LogUtil.logEnterInstance(player, instance, false);
					return map;
//					NormalInstance instance = null;
//					if(player.party!=null){
//						instance = getPartyInstance(player,mapId); //获取在副本里的队友的最后的进度，有可能这个进度就是自己当前进度
//						if(instance!=null){
//							if(!instance.refs.contains(player.ref())){
//								int times = player.getTodayInstanceTimes(instance.definition.id);
//								if(times>=MAX_TIMES){
//									throw new VMapException("同一个副本一天只能进入"+MAX_TIMES+"次");
//								}
//								player.setTodayInstanceTimes(instance.definition.id, times++);
//							}
//						}
//					}
//					if(instance==null){
//						NormalInstanceDefinition definition = mapid2definitions.get(mapId);
//						if(player.level<definition.minLevel)
//							throw new VMapException("副本需要"+definition.minLevel+"才能进入");
//						int times = player.getTodayInstanceTimes(definition.id);
//						if(times>=MAX_TIMES){
//							throw new VMapException("同一个副本一天只能进入"+MAX_TIMES+"次");
//						}
//						player.setTodayInstanceTimes(definition.id, ++times);
//						instance = createInstance(definition);
//					}
//					clearInstanceAndAdd(mapId,player.ref(),instance);
//					VMap map = instance.getMap(mapId);
//					instance.addPlayer(player);
//					player.removeFromMap();
//					map.addPlayer(player, x, y);
//					return map;
				}
			}else{
				throw new VMapException("状态错误");
			}
		}
	}
	
	
	
	
	protected NormalInstance getPartyInstance(Player player,int mapId){
		NormalInstance ret = null;
		synchronized (player.party) {
			for(PartyMember pm:player.party.members){
				if(pm.player!=player){
					Instance instance = pm.player.getVMap().instance;
					if(instance!=null&&instance.getMap(mapId)!=null){
						if(ret==null){
							ret = (NormalInstance)instance;
						}else{
							if(ret.createTime<((NormalInstance)instance).createTime){
								ret = (NormalInstance)instance;
							}
						}
					}
				}
			}
		}
		return ret;
	}
	
	protected void addInstanceHistory(NormalInstance newInstance,GameObjectRef ref){
		List<NormalInstance> l = playerid2instances.get(ref.id);  //跟随队长的副本
		if (l == null) {
			l = new LinkedList<NormalInstance>();
			playerid2instances.put(ref.id, l);
			l.add(newInstance);
			newInstance.attach(ref);
			return;
		}else{
			newInstance.attach(ref);
			l.add(newInstance);
		}
	}
	
	//把副本设置成新的，用在进副本时副本进度不是本人的情况
	protected void clearInstanceAndAdd(int mapId,GameObjectRef ref,NormalInstance newInstance){
		List<NormalInstance> l = playerid2instances.get(ref.id);  //跟随队长的副本
		if (l == null) {
			l = new LinkedList<NormalInstance>();
			playerid2instances.put(ref.id, l);
		}
		Iterator<NormalInstance> ite = l.iterator();
		while(ite.hasNext()){
			NormalInstance instance = ite.next();
			if(instance==newInstance)
				return;
			if(instance.getMap(mapId)!=null){
				instance.unAttach(ref);
				ite.remove();
				break;
			}
		}
		newInstance.attach(ref);
		l.add(newInstance);
	}
	
	protected NormalInstance createInstance(NormalInstanceDefinition definition) {
		NormalInstance instance = new NormalInstance(definition,Time.currTime);
		for(int i=0;i<definition.mapIds.length();i++){
			int mapId = definition.mapIds.get(i);
			VMap map = VMapUtil.create(this, world, mapId,Server.server.revision);
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
			instance.addVMap(map);
		}
		instances.add(instance);
		return instance;
	}
	
	public void removeFromMap(Player player){
		NormalInstance instance = (NormalInstance)player.getVMap().instance;
		instance.removePlayer(player);
		
		// 记录日志
		LogUtil.logLeaveInstance(player, instance);
	}

	public void update(int diff) {
		NMMapManagerStatistics stat = new NMMapManagerStatistics();
		stat.mapCount = instances.size();
		long t = System.nanoTime();
		Iterator<NormalInstance> ite = instances.iterator();
		while(ite.hasNext()){
			NormalInstance instance = ite.next();
			instance.update(diff);
			if(instance.isTimeOut()||instance.refs.size()==0){
				ite.remove();
				instanceRemoved(instance);
			}
		}
		stat.mapCycleTime = System.nanoTime() - t;
		Server.server.getServiceRegistry().getStatisticsService().addNMMapManagerStatistics(stat);
	}
	
	protected void instanceRemoved(NormalInstance instance) {
		if (instance.refs.size() > 0) {
			for (GameObjectRef ref : instance.refs) {
				List<NormalInstance> l = playerid2instances.get(ref.id);
				if (l != null) {
					l.remove(instance);
				}
			}
		}
		for (VMap m : instance.maps.values()) {
			for (GameObject o : m.instanceid2objects.values()) {
				if (o.type == GameObject.TYPE_PLAYER) {
					log.info("[INSTANCEDESTORYERROR]");
				} else {
					ObjectAccessor.removeGameObject(o);
				}
			}
		}
	}
	
	public CreatureDieCallback creatureDieCallback(){
		return null;
	}
	
	public DieCallback dieCallback(){
		return dieCallback;
	}
	
	public MoveCallback moveCallback(){
		return null;
	}

    /**
     * 处理地图数据变化，尽可能地更新已有对象的属性。
     */
    public void mapChanged(GameMapDefinition mapDef) {
        for (NormalInstance instance : instances) {
            for (VMap map : instance.maps.values()) {
                if (map.mapDef.mapInfo.getGlobalID() == mapDef.mapInfo.getGlobalID()) {
                    map.mapDef = mapDef;
                    map.mapChanged();
                }
            }
        }
    }
    
    public void outPrison(Player p){
    	OutPrisonUtil.outPrison(p);
    }
}
