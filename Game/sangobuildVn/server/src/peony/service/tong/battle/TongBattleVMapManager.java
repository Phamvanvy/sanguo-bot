package peony.service.tong.battle;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
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
import peony.game.Point;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.ai.FixedPointAI;
import peony.game.buff.BuffUtil;
import peony.game.buff.ImmuneAllBuff;
import peony.game.changed.ChangedItem;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.tong.Tong;
import peony.service.tong.TongService;
import peony.service.tong.apply.TongBattleApply;
import peony.service.tong.apply.TongBattleApplyService;
import peony.service.tong.apply.TongBattleException;
import peony.util.TimeUtil;

public class TongBattleVMapManager implements VMapManager,Service {
	
	protected TongBattleDieCallback dieCallback = new TongBattleDieCallback();
	
	protected List<TongBattleDef> defs = new ArrayList<TongBattleDef>();
	
	protected List<TongBattleFieldInstance> instances = new ArrayList<TongBattleFieldInstance>();
	
	public static int BATTLESTARTTIME = 19; // 城战开始时间
	public static long ONEDAY = 24 * 3600 * 1000L;

	public static int ITEMID1 = 1105; // 攻城车ID
	public static int PRICE1 = 50000; // 攻城车价格
	public static int PRICE2 = 60000; // 箭塔价格
	
	public static int MINLEVEL = 55;

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("Areas/tongbattlefield.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		Server.server.getWorld().addVMapManager(this);
		initInstance();
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.syncRunner.add(new Runnable(){
					public void run(){
						openInstance();
					}
				});
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BATTLESTARTTIME, 0), ONEDAY, TimeUnit.MILLISECONDS);
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) throws Exception {
		Element root = doc.getRootElement();
		List<Element> elDefs = root.elements("tongbattlefield");
		for(Element elDef:elDefs){
			String name = elDef.attributeValue("name");
			int mapId = Integer.parseInt(elDef.attributeValue("mapId"));
			int signMapId = Integer.parseInt(elDef.attributeValue("signMap"));
			int duration = Integer.parseInt(elDef.attributeValue("duration"));
			Element elOut = elDef.element("out");
			int[] outs = new int[2];
			int x = Integer.parseInt(elOut.attributeValue("x"));
			int y = Integer.parseInt(elOut.attributeValue("y"));
			outs[0] = x;
			outs[1] = y;
			TongBattleDef def = new TongBattleDef();
			def.name = name;
			def.mapId = mapId;
			def.signMapId = signMapId;
			def.duration = duration;
			def.outPoints = outs;
			Element elDefend = elDef.element("defend");
			List<Element> elAttacks = elDef.elements("attack");
			if(elAttacks.size() != 2)
				throw new IllegalArgumentException();
			def.defend = getSideDef(elDefend,TongBattleSideDef.TYPE_DEFEND,def);
			def.attack1 = getSideDef(elAttacks.get(0),TongBattleSideDef.TYPE_ATTACK1,def);
			def.attack2 = getSideDef(elAttacks.get(1),TongBattleSideDef.TYPE_ATTACK2,def);
			defs.add(def);
		}
	}
	
	protected TongBattleSideDef getSideDef(Element el,int type,TongBattleDef def){
		TongBattleSideDef sideDef = new TongBattleSideDef(type, def);
		Element elIn = el.element("in");
		Element elRelive = el.element("relive");
		Element elFlag = el.element("flag");
		Point in = new Point(Integer.parseInt(elIn.attributeValue("x")),Integer.parseInt(elIn.attributeValue("y")));
		Point relive = new Point(Integer.parseInt(elRelive.attributeValue("x")),Integer.parseInt(elRelive.attributeValue("y")));
		Point flag = new Point(Integer.parseInt(elFlag.attributeValue("x")),Integer.parseInt(elFlag.attributeValue("y")));
		sideDef.in = in;
		sideDef.relive = relive;
		sideDef.flag = flag;
		return sideDef;
	}
	
	protected void initInstance(){
		VMapUtil.create(this, Server.server.getWorld(), 849, Server.server.revision);
		Server.server.getWorld().registerVMapManager(849, this);
	}
	
	protected void openInstance(){
		if(TongBattleApplyService.currentCity!=0){
			int currentMapId = TongBattleApplyService.currentCity;
			for(TongBattleDef def : defs){
				if(def.signMapId==currentMapId){
					TongBattleFieldInstance instance = new TongBattleFieldInstance(def);
					VMap map = VMapUtil.create(this, Server.server.getWorld(), def.mapId, Server.server.revision);
					instance.map = map;
					instance.startTime = Time.currTime + 2*60*1000;
					setMap(map, instance);
					Server.server.getWorld().registerVMapManager(def.mapId, this);
					
					ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
					GameMapObject gmo1 = GameMapObject.findByID(proj, 3477505); //绿旗A
					GameMapObject gmo2 = GameMapObject.findByID(proj, 3477506); //蓝旗B
					GameMapObject gmo3 = GameMapObject.findByID(proj, 3477504); //红旗C
					GameMapObject gmo4 = GameMapObject.findByID(proj, 3477513); //守城大将军
					GameMapObject gmo5 = GameMapObject.findByID(proj, 3477511); //城门守卫
					GameMapObject gmo6 = GameMapObject.findByID(proj, 3477512); //城门守卫
					GameMapObject gmo7 = GameMapObject.findByID(proj, 3477507); //箭塔左
					GameMapObject gmo8 = GameMapObject.findByID(proj, 3477508); //箭塔右
					
					TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
					TongBattleApply[] applys = applyService.battles.get(currentMapId);
					if(applys!=null && applys.length>0){
						if(applys.length==2){
							// 两个进攻方
							TongBattleApply apply1 = applys[0];
							TongBattleApply apply2 = applys[1];
							Tong tong1 = apply1.getTong();
							Tong tong2 = apply2.getTong();
							TongBattleSideDef def1 = getDefBySignMapId(def.signMapId).attack1;
							TongBattleSideDef def2 = getDefBySignMapId(def.signMapId).attack2;
							TongBattleSide attack1 = new TongBattleSide(tong1, def1, apply1.faction,1);
							TongBattleSide attack2 = new TongBattleSide(tong2, def2,apply2.faction, 2);
							instance.attack1 = attack1;
							instance.attack2 = attack2;
							GameObject npc1 = VMapUtil.addCreature(map, def1.flag.x, 
									def1.flag.y, (GameMapNPC) gmo1, true, 0, Server.server.revision);
							npc1.faction = attack1.faction | (1<<5); //进攻方A旗帜的faction设为进攻方A的faction
							((Creature)npc1).setAI(null);
							((Creature)npc1).buffs.addBuff(new ImmuneAllBuff());
							GameObject npc2 = VMapUtil.addCreature(map, def2.flag.x, 
									def2.flag.y, (GameMapNPC) gmo2, true, 0, Server.server.revision);
							npc2.faction = attack2.faction | (2<<5); //进攻方B旗帜的faction设为进攻方B的faction
							((Creature)npc2).setAI(null);
							((Creature)npc2).buffs.addBuff(new ImmuneAllBuff());
							instance.attackFlag1 = (Creature) npc1;
							instance.attackFlag2 = (Creature) npc2;
							instance.attackFlag1.setAI(null); // 进攻方A的旗帜的AI置为null
							instance.attackFlag2.setAI(null); // 进攻方B的旗帜的AI置为null
						}else if(applys.length==1){
							// 一个进攻方
							TongBattleApply apply1 = applys[0];
							Tong tong1 = apply1.getTong();
							TongBattleSideDef def1 = getDefBySignMapId(def.signMapId).attack1;
							TongBattleSide attack1 = new TongBattleSide(tong1, def1,apply1.faction, 1);
							instance.attack1 = attack1;
							GameObject npc1 = VMapUtil.addCreature(map, def1.flag.x, 
									def1.flag.y, (GameMapNPC) gmo1, true, 0, Server.server.revision);
							npc1.faction = attack1.faction | (1<<5);
							((Creature)npc1).setAI(null);
							((Creature)npc1).buffs.addBuff(new ImmuneAllBuff());
							instance.attackFlag1 = (Creature) npc1;
							instance.attackFlag1.setAI(null);
						}
					}
					// 防守方C
					TongBattleApply apply3 = applyService.owners.get(currentMapId);
					TongBattleSideDef def3 = getDefBySignMapId(def.signMapId).defend;
					GameObject npc3 = VMapUtil.addCreature(map, def3.flag.x, 
							def3.flag.y, (GameMapNPC) gmo3, true, 0, Server.server.revision);
					((Creature)npc3).setAI(null);
					((Creature)npc3).buffs.addBuff(new ImmuneAllBuff());
					if(apply3!=null){
						// 存在防守方C
						if(applys==null || applys.length==0){
							// 如果没有申请攻城的军团，则防守方继续占领城池
							Server.server.getServiceRegistry().getTongBattleApplyService()
							.preWin(def.signMapId, apply3, 1);
							return;
						}
						Tong tong3 = apply3.getTong();
						TongBattleSide defend = new TongBattleSide(tong3, def3,apply3.faction, 3);
						instance.defend = defend;
						npc3.faction = defend.faction | (3<<5);
						if(apply3.hasBuyTower){
							// 如果此军团已经于开战前购买了箭塔
							GameObject npc7 = VMapUtil.addCreature(map, 460, 
									300, (GameMapNPC) gmo7, true, 0, Server.server.revision);
							GameObject npc8 = VMapUtil.addCreature(map, 540, 
									390, (GameMapNPC) gmo8, true, 0, Server.server.revision);
							npc7.faction = defend.faction | (3<<5);
							npc8.faction = defend.faction | (3<<5);
							((Creature)npc7).minorFaction = 3;
							((Creature)npc8).minorFaction = 3;
							((Creature)npc7).setAI(new FixedPointAI(((Creature)npc7)));
							((Creature)npc8).setAI(new FixedPointAI(((Creature)npc8)));
							
							apply3.hasBuyTower = false;
						}
					}else{
						// 空城防守方
						GameObject npc4 = VMapUtil.addCreature(map, 471, 
								375, (GameMapNPC) gmo4, true, 0, Server.server.revision);
						GameObject npc5 = VMapUtil.addCreature(map, 400, 
								220, (GameMapNPC) gmo5, true, 0, Server.server.revision);
						GameObject npc6 = VMapUtil.addCreature(map, 560, 
								470, (GameMapNPC) gmo6, true, 0, Server.server.revision);
						((Creature)npc4).buffs.addBuff(BuffUtil.createBuff(167, 1, (Creature)npc4, (Creature)npc4, 0));
						((Creature)npc5).buffs.addBuff(BuffUtil.createBuff(167, 1, (Creature)npc5, (Creature)npc5, 0));
						((Creature)npc6).buffs.addBuff(BuffUtil.createBuff(167, 1, (Creature)npc6, (Creature)npc6, 0));
						npc3.faction = 4 | (3<<5);
						npc4.faction = 4 | (3<<5);
						npc5.faction = 4 | (3<<5);
						npc6.faction = 4 | (3<<5);
						((Creature)npc4).minorFaction = 3;
						((Creature)npc5).minorFaction = 3;
						((Creature)npc6).minorFaction = 3;
					}
					((Creature)npc3).minorFaction = 3;
					instance.defenceFlag = (Creature) npc3;
					instance.defenceFlag.setAI(null);
					instance.state = TongBattleFieldInstance.STATE_PREPARE; //城战场状态设为准备状态
					instances.add(instance);
					LogUtil.logTongBattleStart(instance.attack1, instance.attack2, instance.defend, Time.currTime);
				}
			}
		}
	}
	
	protected void setMap(VMap map, Instance instance){
		map.instance = instance;
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if(check){
			TongBattleFieldInstance instance = getInstance(player.id);
			TongBattleDef def = getDefBySignMapId(player.pool.getInt(Player.PROPERTY_TONGBATTLE_SIGNMAPID,0));
			if(instance == null){
				int[] outPoint = def.getOutPoint();
				return Server.server.getWorld().addPlayerToMap(player, def.signMapId, outPoint[0], outPoint[1], true);
			}else{
				Tong tong = Server.server.getServiceRegistry().getTongService().getPlayerTong(player.id);
				if(tong==null || instance.getSide(tong)==null){
					return in(player,instance,null,x,y);
				}
				return in(player,instance,instance.getSide(tong),x,y);
			}
		}else{
			Instance tmpInstance = player.getVMap().instance;
			if(tmpInstance!=null && tmpInstance instanceof TongBattleFieldInstance){
				TongBattleFieldInstance instance = (TongBattleFieldInstance)tmpInstance;
				
				boolean instanceTran = player.getVMap()!=null && player.getVMap().instance==instance;
				if(instanceTran){
					if(instance.state==TongBattleFieldInstance.STATE_PREPARE){
						int startTime = instance.startTime;
						int t = (startTime-Time.currTime);
						int min = t/(60*1000);
						String s = null;
						if(min!=0){
							s = "战场将在"+min+"分钟以后开启";
						}else{
							int sec = t/(1000);
							s = "战场将在"+sec+"秒后开启";
						}
						throw new VMapException(s);
					}
				}
				
				if(instance.state == TongBattleFieldInstance.STATE_STARTED){
					instance.map.addPlayer(player, x, y);
					return instance.map;
				}else{
					throw new VMapException("战斗尚未开始");
				}
			}else{
				TongBattleFieldInstance instance = getInstanceByMapId(mapId);
				if(instance == null){
					throw new VMapException("军团城战还未开始");
				}else{
					Tong tong = Server.server.getServiceRegistry().getTongService().getPlayerTong(player.id);
					if(tong == null)
						throw new VMapException("你没有军团，不能参加城战");
					else{
						TongBattleSide side = instance.getSide(tong);
						return in(player,instance,side);
					}
				}
			}
		}
	}
	
	//用于地图过期
	protected VMap in(Player p, TongBattleFieldInstance instance, TongBattleSide side, int x,int y) throws VMapException{
		instance.addPlayer(p);
		p.removeFromMap();
		instance.map.addPlayer(p, x, y);
		if(side!=null){
			p.minorFaction = side.minorFaction;
			p.addIntPropertyChangedItem(ChangedItem.FACTION,(side.minorFaction<<5)|p.faction,true);
		}
		return instance.map;
	}
	
	//用于城战过地图
	protected VMap in(Player p, TongBattleFieldInstance instance,TongBattleSide side) throws VMapException{
		if(!side.containsPlayer(p.id)&&side.isFull())
			throw new VMapException("军团城战人数已满");
		in(p,instance,side,side.def.in.x,side.def.in.y);
		side.addPlayerId(p.id);
		if(p.party!=null){
			p.party.leave(p.id);
		}
		return instance.map;
	}

	/** 传送进城战战场 */
	public void tran(Player p) throws VMapException {
		Tong tong = Server.server.getServiceRegistry().getTongService()
				.getPlayerTong(p.id);
		if (tong != null) {
			TongBattleDef def = getDefBySignMapId(p.map.getId());
			if (def == null)
				throw new VMapException("没有此城战选项");
			TongBattleFieldInstance instance = getInstance(def);
			if (instance == null || (instance.state!=TongBattleFieldInstance.STATE_STARTED && 
					instance.state!=TongBattleFieldInstance.STATE_PREPARE))
				throw new VMapException("城战还未开始");
			TongBattleSide side = instance.getSide(tong);
			if (side == null) 
				throw new VMapException("你没有权利参加此军团城战");
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			if(tongService.getPlayerInfo(p.id).battleTag==0){
				throw new VMapException("未被标记不能参加城战");
			}
			if(p.level<MINLEVEL){
				throw new VMapException("参加城战的玩家级别必须达到"+MINLEVEL+"Cấp");
			}
			p.pool.setInt(Player.PROPERTY_TONGBATTLE_SIGNMAPID, p.map.getId());
			p.goMap(instance.map.getId(), side.def.in.x, side.def.in.y);
		} else {
			throw new VMapException("你没有军团，不能参加城战");
		}
	}
	
	// 购买攻城车
	public void buyWarCarriage(Player p) throws TongBattleException {
		if (p != null) {
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id);
			if (tong == null)
				throw new TongBattleException("您还没有加入军团");
			if (tongService.getPlayerInfo(p.id).duty != TongService.CHAIRMAN)
				throw new TongBattleException("您不是都督，不能购买此道具");
			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			if (!applyService.isWinner(tong.id) && applyService.hasApplyed(tong.id)) {
				if (in(Calendar.getInstance(), BATTLESTARTTIME - 1, 30,BATTLESTARTTIME, 0)) {
					// 处理购买
					PlayerTransaction tx = p.newTransaction("TONGBATTLEBUYCAR");
					try {
						tong.decMoney(PRICE1);
						p.bag.addGameItemComplete(ObjectAccessor
								.createGameItem(ITEMID1), 1, tx, true);
						tx.commit();
					} catch (NoEnoughValueException e) {
						tx.rollback();
						throw new TongBattleException("军团资金不足");
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
						throw new TongBattleException("Túi đồ đã đầy");
					}
				} else {
					throw new TongBattleException("此时间不能购买");
				}
			} else if(applyService.isWinner(tong.id)){
				throw new TongBattleException("只有进攻方才可以购买此道具");
			} else {
				throw new TongBattleException("此时间不能购买");
			}
		}
	}

	// 购买箭塔,只有防守方才能购买
	public void buyTower(Player p) throws TongBattleException {
		if (p != null) {
			Tong tong = Server.server.getServiceRegistry().getTongService()
					.getPlayerTong(p.id);
			if (tong == null)
				throw new TongBattleException("您还没有加入军团");
			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			if (applyService.isWinner(tong.id)
					&& p.map.getId()==applyService.getWinnerMapId(tong.id)) {
				if(applyService.getApplysAccount(applyService.getWinnerMapId(tong.id))==0)
					throw new TongBattleException("此时间不能购买");
				if (in(Calendar.getInstance(), BATTLESTARTTIME - 1, 30,BATTLESTARTTIME, 0)) {
					// 处理购买
					TongBattleApply defend = applyService.getApplyByTongId(tong.id);
					try {
						if(defend.hasBuyTower)
							throw new TongBattleException("您已经成功购买");
						tong.decMoney(PRICE2);
						defend.hasBuyTower = true; // 标记已经成功购买攻城车
					} catch (NoEnoughValueException e) {
						throw new TongBattleException("军团资金不足");
					}
				} else {
					throw new TongBattleException("此时间不能购买");
				}
			} else {
				throw new TongBattleException("只有占领方军团才可以购买此道具");
			}
		}
	}

	public CreatureDieCallback creatureDieCallback() {
		return null;
	}

	public DieCallback dieCallback() {
		return dieCallback;
	}

	public void mapChanged(GameMapDefinition mapDef) {
		
	}

	public MoveCallback moveCallback() {
		return null;
	}

	public void removeFromMap(Player player) {
		player.minorFaction = 0;
		player.addIntPropertyChangedItem(ChangedItem.FACTION,(player.faction<<3)>>3,true);
	}

	public void update(int diff) {
		Iterator<TongBattleFieldInstance> ite = instances.iterator();
		while (ite.hasNext()) {
			TongBattleFieldInstance instance = ite.next();
			instance.update(diff);
			if (instance.state == TongBattleFieldInstance.STATE_END)
				ite.remove();
		}
	}
	
	public TongBattleDef getDefBySignMapId(int mapId){
		for(TongBattleDef def:defs){
			if(def.signMapId == mapId)
				return def;
		}
		return null;
	}
	
	public TongBattleDef getDefByName(String name){
		for(TongBattleDef def:defs){
			if(def.name.equals(name))
				return def;
		}
		return null;
	}
	
	public TongBattleFieldInstance getInstanceByMapId(int mapId){
		for(TongBattleFieldInstance instance:instances){
			if(instance.map.getId()==mapId)
				return instance;
		}
		return null;
	}
	
	public TongBattleFieldInstance getInstance(TongBattleDef def){
		for(TongBattleFieldInstance instance:instances){
			if(instance.def == def)
				return instance;
		}
		return null;
	}
	
	public TongBattleFieldInstance getInstance(int playerId){
		for(TongBattleFieldInstance instance:instances){
			if(instance.contains(playerId))
				return instance;
		}
		return null;
	}

	public boolean in(Calendar cal, int beginHour, int beginMin,
			int endHour, int endMin) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, beginHour);
		cal1.set(Calendar.MINUTE, beginMin);
		Calendar cal2 = Calendar.getInstance();
		cal2.set(Calendar.HOUR_OF_DAY, endHour);
		cal2.set(Calendar.MINUTE, endMin);
		return cal.after(cal1) && cal.before(cal2);
	}
	
    public void outPrison(Player p){
		if (p.map.map != null) {
		    int[] pos = p.map.map.mapDef.mapInfo.getPathFinder().tryOutPrison(p.x, p.y);
		    if (pos == null) {
		        // 已彻底卡死，回复活点
				int[] relivePoint = ((TongBattleFieldInstance)p.map.map.instance).getRelivePoint(p.id);
				try{
				int oldMapId = p.map.map.getId();
				int oldX = p.x;
				int oldY = p.y;
				p.goMap(relivePoint[0], relivePoint[1], relivePoint[2]);
					Server.server
							.getEventManager()
							.fireEvent(
									new ServiceEvent(
											ServiceEvent.EVENT_PLAYER_OUTPRISON_RELIVEPOINT,
											p,oldMapId,oldX,oldY));
				} catch (VMapException e) {
				}
			} else {
			    try {
					p.goMap(p.map.map.getId(), pos[0], pos[1]);
				} catch (VMapException e) {
				}
			}
		}
    }
    
    public int isInTongBattle(Player p){
    	if(p!=null){
    		for(TongBattleFieldInstance instance : instances){
    			if(instance.isInTongBattle(p)==1)
    				return 1;
    		}
    	}
    	return 0;
    }
	
}
