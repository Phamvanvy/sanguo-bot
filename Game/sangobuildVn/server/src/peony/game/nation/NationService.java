package peony.game.nation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.db.NationDAO;
import peony.game.ChatOption;
import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.LogUtil;
import peony.game.MoveCallback;
import peony.game.NoEnoughValueException;
import peony.game.NoInstanceVMapManager;
import peony.game.OpCode;
import peony.game.OutPrisonUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.buff.NationBuff;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class NationService implements Service, ServiceEventListener,  VMapManager  {
	private static final Logger log = Logger.getLogger(NationService.class);
	
	
	//防守方阵营id<<32|进攻方阵营id
//	protected Map<Long,NationBattleFieldDef> defs = new HashMap<Long,NationBattleFieldDef>();
	
	protected List<NationBattleFieldDef> defs = new ArrayList<NationBattleFieldDef>();
	protected List<NationSneakBattleFieldDef> sneakDefs = new ArrayList<NationSneakBattleFieldDef>();
	
	protected int[][] DEST_FACTIONS = {
			{0,0},
			{GameObject.FACTION_SHU,GameObject.FACTION_WU},
			{GameObject.FACTION_WEI,GameObject.FACTION_WU},
			{GameObject.FACTION_WEI,GameObject.FACTION_SHU},
	};
	
	protected static int[][] NATION_QUESTS_ID = {
			{0,0},
			{1489,1454},
			{1490,1453},
			{1491,1452},
	};
	
	protected NationBattleFieldDieCallback dieCallback;
	
	protected Nation[] nations;
	
	protected List<Forbid> forbids = new ArrayList<Forbid>();
	
	protected List<Punish> punishs;
	
	protected SneakRequest[] requests = new SneakRequest[4];
	
	protected List<NationBattleFieldInstance> instances = new ArrayList<NationBattleFieldInstance>();
	public List<NationSneakBattleFieldInstance> sneakInstances  = new ArrayList<NationSneakBattleFieldInstance>();
	
	protected SneakBattleFieldVMapManager sneakManager = new SneakBattleFieldVMapManager();
	protected SneakCreatureDieCallback sneakCreatureDieCallback = new SneakCreatureDieCallback();
	protected SneakDieCallback sneakDieCallback = new SneakDieCallback();
	protected SneakMoveCallback sneakMoveCallback = new SneakMoveCallback();
	
	public static int[] BATTLE_FIELD_MINLEVEL = {0,50,50,50}; //进入战场的最低等级
	public static final int BATTLE_FIELD_MAXPLAYER = 50; //每方的最大人数
	//testmodify
//	public static final int SNEAK_MAXPLAYER = 3; //反击战每方最大人数
	public static final int SNEAK_MAXPLAYER = 15; //反击战每方最大人数
//	public static final int BATTLE_FIELD_MAXPLAYER = 6; //每方的最大人数
	public static final long BATTLE_FIELD_TIME = 3600  * 1000L; //国战的持续时间
//	public static final long BATTLE_FIELD_TIME = 20 * 60 * 1000L; //国战的持续时间
//	public static final long BATTLE_FIELD_TIME = 5 * 60 * 1000L; //国战的持续时间
	public static final long FAIL_GUARD_TIME = 2 * 24 * 3600 * 1000L; //国战防守失败以后的保护时间
//	public static final long FAIL_GUARD_TIME = 5 * 60 * 1000L; //国战防守失败以后的保护时间
	public static final long REFUSE_GUARD_TIME = 2 * 24 * 3600 *1000L; //拒绝国战以后的保护时间
//	public static final long REFUSE_GUARD_TIME = 10 * 60  * 1000L; //拒绝国战以后的保护时间
	public static final long FAIL_TAX_TIME = 3 * 24 * 3600 * 1000l; //如果国战防守失败，那么被收税3天
//	public static final long FAIL_TAX_TIME = 10 * 60 * 1000l; //如果国战防守失败，那么被收税3天
	public static final long SNEAK_BATTLE_FIELD_TIME = 1800 * 1000L;
	
	public NationRel[][] rels;
	
	protected static Map<Integer,NationSkill> skills = new TreeMap<Integer,NationSkill>();
	static{
		skills.put(2, new NationSkill2(0));
		skills.put(3, new NationSkill3(0));

//		skills.put(1, new NationSkill1(0));
		skills.put(4, new NationSkill4(0));
		skills.put(5, new NationSkill5(0));
	}
	
	public static NationSkill getNationSkill(int id){
		return skills.get(id);
	}
	
	public static Collection<NationSkill> getNationSkills(){
		return skills.values();
	}
	
	public static int[] getNationQuestsId(int faction){
		return NATION_QUESTS_ID[faction];
	}
	
	/**
	 * 返回一个NationSkill的对应等级的新的实例
	 * @param id
	 * @param level
	 * @return
	 */
	public static NationSkill newNationSkill(int id,int level){
		NationSkill skill = skills.get(id).clone();
		skill.level = level;
		return skill;
	}
	
	public NationService() throws Exception{
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("Areas/nationbattlefield.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		Server.server.getWorld().addVMapManager(this);
		dieCallback = new NationBattleFieldDieCallback();
	}
	
	public void shutdown() {
		saveNations();
	}
	
	protected void saveNations(){
		for(Nation nation:nations){
			if(nation!=null){
				nation.pool.setInt(Nation.PROPERTY_BATTLE_ATTACK, 0); //防止当前有国战正在进行
				nation.pool.setInt(Nation.PROPERTY_BATTLE_DEFENSE, 0); //防止当前有国战正在进行
				Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
			}
		}
		for(int i=0;i<4;i++){
			for(int j=0;j<4;j++){
				NationRel rel = rels[i][j];
				if(rel!=null){
					if(rel.type==NationRel.TYPE_ATTACK||rel.type==NationRel.TYPE_DEFENSE){
						rel.type = NationRel.TYPE_PEACE;
						rel.createTime = null;
						rel.endTime = null;
					}
					Server.server.getServiceRegistry().getDbService().nationRelDAO.updateEntity(rel);
				}
			}
		}
	}

	public Nation[] getNations() {
		return nations;
	}

	public void setNations(Nation[] nations) {
		this.nations = nations;
	}
	
	
	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_PLAYER_LOADED,
				ServiceEvent.EVENT_PLAYER_CHANGE_FACTION,
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
				ServiceEvent.EVENT_UNIT_DIE
		};
	}
	
	
	protected void playerFirstLoad(Player p){
		if(isKing(p)){
			int[] othersFaction = getOthersFaction(p.faction);
			if(rels[p.faction][othersFaction[0]].type==NationRel.TYPE_WAR_REQUESTED){
				p.message(-1, 
						MessageFormat.format("{0}已经发起了宣战,请到战争管理菜单处理!", GameObject.getFactionName(othersFaction[0])), -1, -1);
			}
			if(rels[p.faction][othersFaction[1]].type==NationRel.TYPE_WAR_REQUESTED){
				p.message(-1, 
						MessageFormat.format("{0}已经发起了宣战,请到战争管理菜单处理!", GameObject.getFactionName(othersFaction[1])), -1, -1);
			}
		}
	}

	
	/**
	 * 
	 * @param player
	 * @param type 1 进攻战场 2 防守战场 没个阵营同时只会有一个进攻和一个防守战场
	 * @return
	 * @throws NationBattleFieldSignupException
	 */
	public NationBattleFieldInstance battleFieldSignup(Player player,boolean attack) throws NationBattleFieldSignupException{
		if(!isKing(player) && player.level<BATTLE_FIELD_MINLEVEL[player.faction]){
			throw new NationBattleFieldSignupException(MessageFormat.format("达到{0}级才能参加国战", BATTLE_FIELD_MINLEVEL[player.faction]));
		}
		for(NationBattleFieldInstance instance:instances){ //找到sourceFaction相同的战场
			if((instance.def.sourceFaction==player.faction&&!attack)||(instance.def.destFaction==player.faction&&attack)){
				if(!instance.contains(player)){
					if(isKing(player)){ //如果是国王，那么无条件进入战场
						instance.join(player);
						return instance;
					}else{
						if(instance.getSignupPlayerCount(player.faction)<BATTLE_FIELD_MAXPLAYER){
							instance.join(player);
							return instance;
						}else{
							throw new NationBattleFieldSignupException("已经达到战场的上限人数");
						}
					}
				}else{
					return instance;
				}
			}
		}
		throw new NationBattleFieldSignupException("参战失败，无权进入该战场");
	}
	
	public NationSneakBattleFieldInstance sneakSignup(Player player,boolean attack) throws NationBattleFieldSignupException{
		if(!isKing(player) && player.level<BATTLE_FIELD_MINLEVEL[player.faction]){
			throw new NationBattleFieldSignupException(MessageFormat.format("达到{0}级才能参加反击战", BATTLE_FIELD_MINLEVEL[player.faction]));
		}
		for(NationSneakBattleFieldInstance instance:sneakInstances){ //找到sourceFaction相同的战场
			if((instance.def.sourceFaction==player.faction&&!attack)||(instance.def.destFaction==player.faction&&attack)){
				if(!instance.contains(player)){
					if(isKing(player)){ //如果是国王，那么无条件进入战场
						instance.join(player);
						return instance;
					}else{
						if(instance.getSignupPlayerCount(player.faction)<SNEAK_MAXPLAYER){
							instance.join(player);
							return instance;
						}else{
							throw new NationBattleFieldSignupException("已经达到战场的上限人数");
						}
					}
				}else{
					return instance;
				}
			}
		}
		throw new NationBattleFieldSignupException("参战失败，无权进入该战场");
	}
	
	public NationSneakBattleFieldInstance getSneakInstance(int sourceFaction,int destFaction){
		for(NationSneakBattleFieldInstance instance:sneakInstances){
			if(instance.def.sourceFaction == sourceFaction&&instance.def.destFaction == destFaction){
				return instance;
			}
		}
		return null;
	}
	
	public void declareSneak(int sourceFaction,int destFaction) throws NationDeclareException{
		NationRel rel = rels[sourceFaction][destFaction];
		if(requests[sourceFaction]!=null)
			throw new NationDeclareException("反击战不能重复发起");
		if(rel.type!=NationRel.TYPE_FAIL)
			throw new NationDeclareException("当前状态不能发起反击战");
		Date now = new Date();
		if((now.getTime()-rel.createTime.getTime())>10*60*1000)
			throw new NationDeclareException("只能在失败后10分钟内宣战");
		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.MINUTE, 1);
		//testmodify
		cal.set(Calendar.HOUR_OF_DAY, 21);
		cal.set(Calendar.MINUTE, 30);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		requests[sourceFaction] = new SneakRequest(sourceFaction,destFaction,now,cal.getTime(),rel.money);
	}
	
	public void addRequest(int sourceFaction,int destFaction,Date startTime){
		requests[sourceFaction] = new SneakRequest(sourceFaction,destFaction,new Date(),startTime,0);
	}
	
	public void declareWar(int sourceFaction,int destFaction,Date begin,Date end) throws NationDeclareException{
		//testmodify
		if(Time.betweenHour(Time.currDate, 18, 24))
			throw new  NationDeclareException("不能在这个时间段宣战");
		Nation destNation = getNationByFaction(destFaction);
		if(destNation.guardTime!=null&&destNation.guardTime.after(Time.currDate))
			throw new NationDeclareException("目标国家正在保护状态，不能宣战");
		NationRel rel = rels[sourceFaction][destFaction];
		if(rel.type==NationRel.TYPE_WAR_REQUEST)
			throw new NationDeclareException("宣战失败，该国家已被宣战");
		if(rel.type==NationRel.TYPE_WAR_REQUESTED)
			throw new NationDeclareException("目标国家已经对本国发起了宣战请求");
		if(rel.type==NationRel.TYPE_WAR_PREPARE||rel.type==NationRel.TYPE_WARED_PREPARE)
			throw new NationDeclareException("本国跟目标国家已经处于战前准备状态");
		if(rel.type==NationRel.TYPE_ATTACK||rel.type==NationRel.TYPE_DEFENSE)
			throw new NationDeclareException("本国跟目标国家已经处于交战状态");
		int otherFaction = getOtherFaction(sourceFaction,destFaction);
		if(rels[sourceFaction][otherFaction].type==NationRel.TYPE_WAR_PREPARE||rels[sourceFaction][otherFaction].type==NationRel.TYPE_WAR_REQUEST)
			throw new NationDeclareException("不能同时跟两个国家宣战");
//		if(rels[sourceFaction][otherFaction].type!=NationRel.TYPE_PEACE&&rels[sourceFaction][otherFaction].type!=NationRel.TYPE_WIN&&rels[sourceFaction][otherFaction].type!=NationRel.TYPE_FAIL)
//			throw new NationDeclareException("不能对目标国家宣战");
		if(rels[destFaction][otherFaction].type==NationRel.TYPE_WAR_REQUESTED)
			throw new NationDeclareException("目标国家已经存在宣战请求");
		if(rels[destFaction][otherFaction].type==NationRel.TYPE_WARED_PREPARE)
			throw new NationDeclareException("目标国家已经处于战争状态");
//		if(rels[destFaction][otherFaction].type==NationRel.TYPE_WAR_REQUESTED)
//			throw new NationDeclareException("目标国家已经处于战前准备状态");
		if(rels[destFaction][otherFaction].type==NationRel.TYPE_DEFENSE)
			throw new NationDeclareException("目标国家已经处于交战状态");
		rel.type = NationRel.TYPE_WAR_REQUEST;
		rel.createTime = begin;
		rel.endTime = end;
		rels[destFaction][sourceFaction].type = NationRel.TYPE_WAR_REQUESTED;
		rels[destFaction][sourceFaction].createTime = begin;
		rels[destFaction][sourceFaction].endTime = end;
	}
	
	public void acceptWar(int sourceFaction,int destFaction) throws NationDeclareException{
		NationRel rel1 = rels[sourceFaction][destFaction];
		if(rel1.type!=NationRel.TYPE_WAR_REQUESTED)
			throw new NationDeclareException("当前不处于被宣战状态");
		rel1.type = NationRel.TYPE_WARED_PREPARE;
		NationRel rel2 = rels[destFaction][sourceFaction]; 
		rel2.type = NationRel.TYPE_WAR_PREPARE;
		rel1.createTime = rel2.createTime = Time.currDate;
		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.MINUTE, 1);
		//testmodify
		cal.add(Calendar.DAY_OF_MONTH, 1);  //
		cal.set(Calendar.HOUR_OF_DAY, 20);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND,0);
//		cal.add(Calendar.MINUTE,5);
		rel1.endTime = rel2.endTime = cal.getTime();
	}
	
	public void refuseWar(int sourceFaction,int destFaction) throws NationDeclareException{
		NationRel rel1 = rels[sourceFaction][destFaction];
		if(rel1.type!=NationRel.TYPE_WAR_REQUESTED)
			throw new NationDeclareException("当前不处于被宣战状态");
		NationRel rel2 = rels[destFaction][sourceFaction]; 
		rel1.type = rel2.type = NationRel.TYPE_PEACE;
		rel1.createTime = rel2.createTime = Time.currDate;
		rel1.endTime = rel2.endTime = null;
		Date guardTime = new Date(Time.currDate.getTime() + REFUSE_GUARD_TIME);
		Nation nation = getNationByFaction(sourceFaction);
		if(nation.guardTime==null||nation.guardTime.before(guardTime)){
			nation.guardTime = guardTime;
		}
		Nation destNation = getNationByFaction(destFaction);
		int v = (int)(nation.money * 0.03f);
		if(v>0){
			nation.decMoney(v);
			destNation.addMoney(v);
		}
	}
	
	protected int getOtherFaction(int faction1,int faction2){
		if ((faction1 == GameObject.FACTION_WEI && faction2 == GameObject.FACTION_SHU)
				|| (faction1 == GameObject.FACTION_SHU && faction2 == GameObject.FACTION_WEI))
			return GameObject.FACTION_WU;
		if ((faction1 == GameObject.FACTION_WEI && faction2 == GameObject.FACTION_WU)
				|| (faction1 == GameObject.FACTION_WU && faction2 == GameObject.FACTION_WEI))
			return GameObject.FACTION_SHU;
		if ((faction1 == GameObject.FACTION_SHU && faction2 == GameObject.FACTION_WU)
				|| (faction1 == GameObject.FACTION_WU && faction2 == GameObject.FACTION_SHU))
			return GameObject.FACTION_WEI;
		return -1;
	}
	
	protected int[] getOthersFaction(int faction){
		int[] ret = new int[2];
		if(faction==GameObject.FACTION_WEI){
			ret[0] = GameObject.FACTION_SHU;
			ret[1] = GameObject.FACTION_WU;
		}
		else if(faction==GameObject.FACTION_SHU){
			ret[0] = GameObject.FACTION_WEI;
			ret[1] = GameObject.FACTION_WU;
		}
		else if(faction==GameObject.FACTION_WU){
			ret[0] = GameObject.FACTION_WEI;
			ret[1] = GameObject.FACTION_SHU;
		}else
			throw new IllegalArgumentException();
		return ret;
	}
	
	public NationRel getRel(int sourceFaction,int destFaction){
		return rels[sourceFaction][destFaction];
	}
	
	public void setRel(int sourceFaction,int destFaction,int type,Date createTime,Date endTime){
		rels[sourceFaction][destFaction].type = type;
		rels[sourceFaction][destFaction].createTime = createTime;
		rels[sourceFaction][destFaction].endTime = endTime;
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_LOADED:
			playerLoaded((Player)event.param1,(Integer)event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_CHANGE_FACTION:
			playerChangeFaction((Player)event.param1,(Integer)event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			playerFirstLoad((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			playerLogOut((Player)event.param1);
			break;
		case ServiceEvent.EVENT_UNIT_DIE:
			playerDie((Unit)event.param1,(Unit)event.param2);
			break;
		}
	}
	
	protected void playerDie(Unit p, Unit p1){
		if(p instanceof Player && p1 instanceof Player && isKing((Player)p) && p.faction!=p1.faction){
			Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(p.faction, 
						MessageFormat.format("国公{0}被{1}的{2}斩杀了，大家勿忘报仇雪恨。", p.name,GameObject.getFactionName(p1.faction),p1.name));
			Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(p1.faction, MessageFormat.format("{0}勇猛过人，成功斩杀了{1}国公{2}", p1.name,GameObject.getFactionName(p.faction),p.name));
		}
	}
	
	protected void playerLogOut(Player p){
		if(isKing(p)){
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(p.faction);
			long lastLogoutTime = nation.pool.getLong(Nation.PROPERTY_KINGLOGOUT_TIME, 0);
			if(lastLogoutTime==0 || (System.currentTimeMillis()-lastLogoutTime)>=5*60*1000L){
				Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(p.faction, MessageFormat.format("国公{0}下线", p.name));
				nation.pool.setLong(Nation.PROPERTY_KINGLOGOUT_TIME, System.currentTimeMillis());
			}
		}
	}
	
	protected void playerChangeFaction(Player player,int oldFaction){
		Nation oldNation = getNationByFaction(oldFaction);
		if(oldNation.buff!=null){
			player.buffs.removeBuff(oldNation.buff.getId());
		}
		Nation nation = getNationByFaction(player.faction);
		if(nation.buff!=null){
			player.buffs.addBuff(nation.buff);
		}
	}
	
	protected void playerLoaded(Player player, int type) {
		if (type != ServiceEvent.PLAYER_LOAD_ACCESSOR) {
			Nation nation = getNationByFaction(player.faction);
			if (nation.buff != null) {
				player.buffs.addBuff(nation.buff);
			}
			if(nation.getOfficer(Officer.KING)!=null 
					&& nation.getOfficer(Officer.KING).id==player.id){
				player.setKing();
			}else{
				player.unKing();
			}
		}
		if(isKing(player)){
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
			long lastLoginTime = nation.pool.getLong(Nation.PROPERTY_KINGLOGIN_TIME, 0);
			if(lastLoginTime==0 || (System.currentTimeMillis()-lastLoginTime)>=5*60*1000L){
				Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(player.faction, MessageFormat.format("国公{0}上线", player.name));
				nation.pool.setLong(Nation.PROPERTY_KINGLOGIN_TIME, System.currentTimeMillis());
			}
		}
	}
	
	public void forbid(Officer officer, int id)throws NationVoteException {
		synchronized (forbids) {
			for(Forbid forbid : forbids){
				if(forbid.targetId==id){
					throw new NationVoteException("禁言时间内不能对已经禁言的玩家再禁言");
				}
			}
			officer.setForbidTimes(officer.getForbidTimes() + 1);
			Date endTime = new Date(System.currentTimeMillis()+officer.getForbidTime());
			final Forbid forbid = new Forbid(officer.id, id,endTime, new Date());
			forbids.add(forbid);
			Server.server.scheduExec.schedule(new Runnable(){
				public void run() {
					forbids.remove(forbid);
				}
			}, officer.getForbidTime(), TimeUnit.MILLISECONDS);
			Server.server.getServiceRegistry().getChatService().forbid(id, 2, endTime.getTime()); //2=二进制的10,第1位国家聊
		}
	}
	
	public void punish(Officer officer, int id, int money){
		synchronized(punishs){
			officer.setPunishTimes(Time.day, officer.getPunishTimes(Time.day)+1);
			Punish punish = new Punish(officer.id,id,money,new Date());
			punishs.add(punish);
		}
	}

	protected void loadForbids(){
		forbids = Server.server.getServiceRegistry().getDbService().forbidDAO.getAllForbids();
	}
	
	protected void loadPunishs(){
		punishs = Server.server.getServiceRegistry().getDbService().punishDAO.getAllPunishs();
	}
	
	protected void loadOfficers(){
		List<Officer> ofs = Server.server.getServiceRegistry().getDbService().officerDAO.getAllOfficers();
		for(Officer of:ofs){
			of.actor = Server.server.getServiceRegistry().getActorCacheService().find(of.id);
			nations[of.faction-1].addOfficer(of);
		}
	}
	
	//todo: load from db
	protected void loadNations(){
		NationDAO nationDAO = Server.server.getServiceRegistry().getDbService().nationDAO;
		nations = new Nation[3];
		for(int i=0; i<3; i++){
			Nation nation = null;
			if(nationDAO.uniqueResult("from Nation o where o.faction=?",i+1)==null){
				nation = new Nation();
				nation.faction = i+1;
				nation.taxRate = 0.05f;
				nation.skills = new NationSkills();
				nationDAO.newEntity(nation);
			}else{
				nation = (Nation) nationDAO.uniqueResult("from Nation o where o.faction=?", i+1);
				nation.pool.setInt(Nation.PROPERTY_BATTLE_ATTACK, 0);
				nation.pool.setInt(Nation.PROPERTY_BATTLE_DEFENSE, 0);
				nation.pool.setInt(Nation.PROPERTY_SNEAK_ATTACK, 0);
				nation.pool.setInt(Nation.PROPERTY_SNEAK_DEFENSE, 0);
				CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
				int flag = nation.pool.getInt("SIGNUPFLAG", 0);
				if(flag==1){
					candidateService.signupFlag = 1;
				}
			}
			nations[i] = nation;
		}
		for(Nation nation:nations){
			if(nation!=null){
//				nation.skills = new NationSkills();
				nation.skills.init(getNationSkills());
			}
		}
//		nations = new Nation[3];
//		for(int i=0; i<3; i++){
//			Nation nation = new Nation();
//			nation.faction = i+1;
//			nations[i] = nation;
//		}
//		nations[0].buff = new NationBuff(0.01f,0.02f,0.03f);
//		nations[1].buff = new NationBuff(0.02f,0.03f,0.04f);
//		nations[2].buff = new NationBuff(0.05f,0.06f,0.07f);
	}
	
	

	//todo:load from db
	public void startup() throws Exception {
		loadNations();
		Server.server.getEventManager().registerListener(this);
		loadNationsBuff();
		loadOfficers();
		loadForbids();
		loadPunishs();
		loadNationRels();
	}
	
	protected void loadNationRels() throws Exception{
		List<NationRel> l = Server.server.getServiceRegistry().getDbService().nationRelDAO.getRels();
		if(l.size()>0&&l.size()!=6)
			throw new IllegalStateException("NationRels size must be 9.");
		rels = new NationRel[4][4];
		if(l.size()==0){ //如果数据库中没有存数据进行初始化
			int id = 0;
			rels[GameObject.FACTION_WEI][GameObject.FACTION_SHU] = new NationRel(++id,GameObject.FACTION_WEI,GameObject.FACTION_SHU,NationRel.TYPE_PEACE,null,null);
			rels[GameObject.FACTION_WEI][GameObject.FACTION_WU] = new NationRel(++id,GameObject.FACTION_WEI,GameObject.FACTION_WU,NationRel.TYPE_PEACE,null,null);
			rels[GameObject.FACTION_SHU][GameObject.FACTION_WEI] = new NationRel(++id,GameObject.FACTION_SHU,GameObject.FACTION_WEI,NationRel.TYPE_PEACE,null,null);
			rels[GameObject.FACTION_SHU][GameObject.FACTION_WU] = new NationRel(++id,GameObject.FACTION_SHU,GameObject.FACTION_WU,NationRel.TYPE_PEACE,null,null);
			rels[GameObject.FACTION_WU][GameObject.FACTION_WEI] = new NationRel(++id,GameObject.FACTION_WU,GameObject.FACTION_WEI,NationRel.TYPE_PEACE,null,null);
			rels[GameObject.FACTION_WU][GameObject.FACTION_SHU] = new NationRel(++id,GameObject.FACTION_WU,GameObject.FACTION_SHU,NationRel.TYPE_PEACE,null,null);
			saveNationRels();
		}else{
			for(NationRel rel:l){
				rels[rel.sourceFaction][rel.destFaction] = rel;
			}
		}
	}
	
	protected void saveNationRels(){
		for(int i=0;i<4;i++){
			for(int j=0;j<4;j++){
				if(rels[i][j]!=null){
					Server.server.getServiceRegistry().getDbService().nationRelDAO.makePersistent(rels[i][j]);
				}
			}
		}
	}
	
	protected void loadNationsBuff() throws IOException{
		Properties pro = new Properties();
		pro.load(new FileInputStream(new File("nationbuff.properties")));
		String wei = pro.getProperty("wei");
		String shu = pro.getProperty("shu");
		String wu = pro.getProperty("wu");
		nations[0].buff = createNationBuff(getFloats(wei));
		nations[1].buff = createNationBuff(getFloats(shu));
		nations[2].buff = createNationBuff(getFloats(wu));
	}
	
	protected NationBuff createNationBuff(float[] fs){
		if(fs[0]==.0f&&fs[1]==.0f&&fs[2]==.0f){
			return null;
		}
		NationBuff buff = new NationBuff(fs[0],fs[1],fs[2]);
		return buff;
	}
	
	protected float[] getFloats(String s){
		String[] ss = s.split(",");
		float[] fs = new float[ss.length];
		for(int i=0;i<ss.length;i++){
			fs[i] = Float.parseFloat(ss[i]);
		}
		return fs;
	}
	

	public Nation getNationByFaction(int faction){
		return nations[faction-1];
	}
	
	/**
	 * 获取在国战中战胜指定国家的国家，如果另外两个都胜了指定国家，那么选择获胜时间在后的那个，如果没有则返回null
	 * @param faction
	 * @return
	 */
	public Nation getWinNation(int faction){
		int[] others = getOthersFaction(faction);
		NationRel rel1 = rels[faction][others[0]];
		NationRel rel2 = rels[faction][others[1]];
		if(rel1.type==NationRel.TYPE_FAIL&&rel2.type==NationRel.TYPE_FAIL){
			if(rel1.endTime.after(rel2.endTime)){
				return getNationByFaction(others[0]);
			}else{
				return getNationByFaction(others[1]);
			}
		}
		else if(rel1.type==NationRel.TYPE_FAIL){
			return getNationByFaction(others[0]);
		}
		else if(rel2.type==NationRel.TYPE_FAIL){
			return getNationByFaction(others[1]);
		}
		return null;
	}
	
	/**
	 * 判断玩家是否是国王
	 */
	public boolean isKing(Player p){
		Nation nation = getNationByFaction(p.faction);
		if(p.id == nation.getKingId())
			return true;
		return false;
	}
	
	/**
	 * 判断是否有国王
	 */
	public boolean hasKing(int faction){
		Nation nation = getNationByFaction(faction);
		return nation.getKingId()>0;
	}
	
	/**
	 * 国家募捐
	 */
	public void collect(Player p, int money) throws NationVoteException{
		NationDAO nationDAO = Server.server.getServiceRegistry().getDbService().nationDAO;
		if(p==null)
			return;
		if(money<=0)
			throw new NationVoteException("捐献失败，请输入正确的金额");
		log.info("[COLLECT]"+LogUtil.getPlayerLogString(p)+"MONEY["+money+"]BALANCE["+p.money+"]");
		Nation nation = getNationByFaction(p.faction);
		PlayerTransaction tx = p.newTransaction("NCL");
		try {
			p.decMoney(money, tx, false);
		} catch (NoEnoughValueException e) {
			log.info("[COLLECTFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
			tx.rollback();
			throw new NationVoteException("<cff0000>您的金钱不足</c>\n<cff0000> vàng của bạn không đủ </c>");
		}
		nation.addMoney(money);
		nationDAO.updateEntity(nation);
		tx.commit();
		if(money>=20000){
			Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(p.faction,
					MessageFormat.format("召告四方，兹收到{0}捐奉{1}金钱。共抗外敌，特此嘉勉。", p.name,money));
		}
		int oldCollectMoney = p.pool.getInt(Player.PROPERTY_NATIONCOLLECT_MONEY, 0);
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent
				.EVENT_NATIONCOLLECT, p, oldCollectMoney, money));
		p.pool.setInt(Player.PROPERTY_NATIONCOLLECT_MONEY, (oldCollectMoney+money));
		log.info("[COLLECTSUCCESS]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
	}
	
	/**
	 * 募捐活动结束，统计募捐资金
	 */
	public void calculateCollection(Nation nation){
		long collection = nation.money;
		Server.server.getServiceRegistry().getChatService().addChatMessage(
				new ChatMessage(ChatOption.FACTION,-1,nation.faction ,"<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>",nation.faction
						, MessageFormat.format("此次国公募捐资金 {0}，感谢大家的支持，{1}必将强盛起来!", collection,nation.getName()),null));
	}
	
	/**
	 * 大臣一览表
	 */
	public void officerList(Player p, int serial){
		if(p==null)
			return;
		Nation nation = getNationByFaction(p.faction);
		Officer[] officers = nation.getOfficers();
		Packet packet = new Packet(OpCode.OFFICER_LIST_SERVER);
		packet.putInt(serial);
		List<Officer> list = new ArrayList<Officer>();
		for(Officer officer : officers){
			if(officer!=null){
				list.add(officer);
			}
		}
		packet.put((list.size()-1)==-1 ? 0 : list.size()-1);
		if(list.size()>0){
			for(Officer officer : list){
				if(officer.level!=Officer.KING){
					packet.putInt(officer.id);
					packet.putString(officer.getName());
					packet.putString(officer.actor.name);
				}
			}
		}
		p.send(packet);
	}
	
	/**
	 * 根据被禁言者ID获取Forbid
	 */
	public Forbid getForbidByTargetId(int targetId){
		for(Forbid forbid : forbids){
			if(forbid.targetId == targetId){
				return forbid;
			}
		}
		return null;
	}
	
	
	
	protected void parse(Document doc) throws Exception{
		Element root = doc.getRootElement();
		Element normal = root.element("normal");
		Element f = normal.element("wei");
		createDef(GameObject.FACTION_WEI,f);
		f = normal.element("shu");
		createDef(GameObject.FACTION_SHU,f);
		f = normal.element("wu");
		createDef(GameObject.FACTION_WU,f);
		for (NationBattleFieldDef def : defs) {
			Server.server.getWorld().registerVMapManager(def.mapId, this);
		}
		Element sneak = root.element("sneak");
		f = sneak.element("wei");
		createSneakDef(GameObject.FACTION_WEI,f);
		f = sneak.element("shu");
		createSneakDef(GameObject.FACTION_SHU,f);
		f = sneak.element("wu");
		createSneakDef(GameObject.FACTION_WU,f);
		for (NationSneakBattleFieldDef def : sneakDefs) {
			Server.server.getWorld().registerVMapManager(def.mapId, sneakManager);
		}
	}
	
//	<in x="170" y="12"/>
//	<in x="8" y="137"/>
//	<out mapid="272" x="179" y="495"/>
//	<out mapid="240" x="324" y="637"/>
//	<out mapid="352" x="149" y="520"/>
//	<point x="3",y="139"/>
	@SuppressWarnings("unchecked")
	protected void createSneakDef(int faction,Element f) throws Exception{
		int mapId = Integer.parseInt(f.attributeValue("mapid"));
		int[] destFactions = getDestFactions(faction);
		List l = f.elements("out");
		if(l.size()!=3) //出口的位置必须是3个
			throw new IllegalArgumentException("out size must be 3.");
		int[][] outs = new int[3][3];
		for(int i=0;i<l.size();i++){
			outs[i][0] = Integer.parseInt(((Element)l.get(i)).attributeValue("mapid"));
			outs[i][1] = Integer.parseInt(((Element)l.get(i)).attributeValue("x"));
			outs[i][2] = Integer.parseInt(((Element)l.get(i)).attributeValue("y"));
		}
		l = f.elements("in");
		if(l.size()!=2) //npc必须是两个
			throw new IllegalArgumentException("in size must be 2.");
		int[][] ins = new int[2][2];
		for(int i=0;i<l.size();i++){
			ins[i][0] = Integer.parseInt(((Element)l.get(i)).attributeValue("x"));
			ins[i][1] = Integer.parseInt(((Element)l.get(i)).attributeValue("y"));
		}
		l = f.elements("npc");
		if(l.size()!=3) //npc必须是两个
			throw new IllegalArgumentException("npc size must be 3.");
		int[][] npcs = new int[3][3];
		for(int i=0;i<l.size();i++){
			npcs[i][0] = Integer.parseInt(((Element)l.get(i)).attributeValue("mapid"));
			int[] tmp = getInts(((Element)l.get(i)).attributeValue("id"));
			npcs[i][1] = tmp[0];
			npcs[i][2] = tmp[1];
		}
		int[] point = new int[2];
		Element el = f.element("point");
		point[0] = Integer.parseInt(el.attributeValue("x"));
		point[1] = Integer.parseInt(el.attributeValue("y"));
		for(int destFaction:destFactions){
			NationSneakBattleFieldDef def = new NationSneakBattleFieldDef(faction,destFaction,mapId,outs,ins[0],ins[1],point,npcs);
			addSneakDef(def);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void createDef(int faction,Element f) throws Exception{
		int mapId = Integer.parseInt(f.attributeValue("mapid"));
		int kingId = Integer.parseInt(f.attributeValue("kingid"));
		int[] guards = getInts(f.attributeValue("guard"));
		List l = f.elements("door");
		if(l.size()!=4) //必须只有4个门
			throw new IllegalArgumentException("door size must be 4.");
		int[][] doors = new int[4][2];
		for(int i=0;i<l.size();i++){
			doors[i][0] = Integer.parseInt(((Element)l.get(i)).attributeValue("x"));
			doors[i][1] = Integer.parseInt(((Element)l.get(i)).attributeValue("y"));
		}
		int[] destFactions = getDestFactions(faction);
		l = f.elements("out");
		if(l.size()!=3) //出口的位置必须是3个
			throw new IllegalArgumentException("out size must be 3.");
		int[][] outs = new int[3][3];
		for(int i=0;i<l.size();i++){
			outs[i][0] = Integer.parseInt(((Element)l.get(i)).attributeValue("mapid"));
			outs[i][1] = Integer.parseInt(((Element)l.get(i)).attributeValue("x"));
			outs[i][2] = Integer.parseInt(((Element)l.get(i)).attributeValue("y"));
		}
		l = f.elements("npc");
		if(l.size()!=3) //npc必须是两个
			throw new IllegalArgumentException("npc size must be 3.");
		int[][] npcs = new int[3][3];
		for(int i=0;i<l.size();i++){
			npcs[i][0] = Integer.parseInt(((Element)l.get(i)).attributeValue("mapid"));
			int[] tmp = getInts(((Element)l.get(i)).attributeValue("id"));
			npcs[i][1] = tmp[0];
			npcs[i][2] = tmp[1];
		}
		for(int destFaction:destFactions){
			NationBattleFieldDef def = new NationBattleFieldDef(faction,destFaction,mapId,kingId,guards,doors,outs[faction-1],outs[destFaction-1],npcs);
			addDef(def);
		}
	}
	
	protected int[] getDestFactions(int faction){ 
		return DEST_FACTIONS[faction];
	}
	
	protected int[] getInts(String s) {
		String[] ss = s.split(",");
		int[] ret = new int[ss.length];
		for (int i = 0; i < ss.length; i++) {
			ret[i] = Integer.parseInt(ss[i]);
		}
		return ret;
	}
	
	
	/**
	 * 如果有报名记录，那么假如地图，如果没有传到出口
	 */
	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		NationBattleFieldInstance instance = getInstanceByFactionAndMapId(
				player.faction, mapId);
		NationBattleFieldDef def = null;
		if (instance != null) {
			if (instance.contains(player)) {
				instance.addPlayer(player);
				player.removeFromMap();
				instance.map.addPlayer(player, x, y);
				return instance.map;
			} else {
				def = instance.def;
			}
		} else {
			def = getDefByFactionAndMapId(player.faction, mapId);
		}
		int[] out = def.getOutPoint(player.faction);
		return Server.server.getWorld().addPlayerToMap(player, out[0], out[1],
				out[2], check);
	}
	
	public NationBattleFieldDef getDefByFactionAndMapId(int faction,int mapId){
		for(NationBattleFieldDef def:defs){
			if(def.mapId==mapId){
				if(def.sourceFaction==faction||def.destFaction==faction){
					return def;
				}
			}
		}
		return null;
	}
	
	public NationSneakBattleFieldDef getSneakDefByFactionAndMapId(int faction,int mapId){
		for(NationSneakBattleFieldDef def:sneakDefs){
			if(def.mapId==mapId){
				if(def.sourceFaction==faction||def.destFaction==faction){
					return def;
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据阵营以及mapId寻找对应的战场，这样找出的战场必然是唯一的
	 * @param faction
	 * @param mapId
	 * @return
	 */
	public NationBattleFieldInstance getInstanceByFactionAndMapId(int faction,int mapId){
		for(NationBattleFieldInstance instance:instances){
			if(instance.def.mapId==mapId){
				if(instance.def.sourceFaction==faction||instance.def.destFaction==faction)
					return instance;
			}
		}
		return null;
	}
	
	public NationSneakBattleFieldInstance getSneakInstanceByFactionAndMapId(int faction,int mapId){
		for(NationSneakBattleFieldInstance instance:sneakInstances){
			if(instance.def.mapId==mapId){
				if(instance.def.sourceFaction==faction||instance.def.destFaction==faction)
					return instance;
			}
		}
		return null;
	}

	public DieCallback dieCallback() {
		return dieCallback;
	}
	
	public CreatureDieCallback creatureDieCallback(){
		return null;
	}

	public void mapChanged(GameMapDefinition mapDef) {

	}

	public MoveCallback moveCallback() {
		return null;
	}

	public void removeFromMap(Player player) {
		player.getVMap().instance.removePlayer(player);
	}

	protected void updateBattleFields(int diff){
		Iterator<NationBattleFieldInstance> ite = instances.iterator();
		while(ite.hasNext()){
			try {
				NationBattleFieldInstance instance = ite.next();
				if(instance.state==NationBattleFieldInstance.STATE_END){
					ite.remove();
					dark(instance.def.destFaction,instance.def.getNpc(instance.def.destFaction));
					dark(instance.def.sourceFaction,instance.def.getNpc(instance.def.sourceFaction));
				}else{
					instance.update(diff);
				}
			} catch (Exception e) {
				log.error(e,e);
			}
		}
	}
	
	protected void updateSneakBattleFields(int diff){
		Iterator<NationSneakBattleFieldInstance> ite = sneakInstances.iterator();
		while(ite.hasNext()){
			try {
				NationSneakBattleFieldInstance instance = ite.next();
				if(instance.state==NationSneakBattleFieldInstance.STATE_END){
					ite.remove();
					dark(instance.def.destFaction,instance.def.getNpc(instance.def.destFaction));
					dark(instance.def.sourceFaction,instance.def.getNpc(instance.def.sourceFaction));
					//反击战结束事件
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_SNEAKBATTLE_END,instance.def.sourceFaction));
				}else{
					instance.update(diff);
				}
			} catch (Exception e){
				log.error(e,e);
			}
		}
	}
	
	public void update(int diff) {
		updateBattleFields(diff);
		updateSneakBattleFields(diff);
		for(Nation nation:nations){
			nation.update();
		}
		updateRels();
		updateRequests();
	}
	
    public void outPrison(Player p){
    	OutPrisonUtil.outPrison(p);
    }
	
	protected void updateRequests(){
		for(int i = 0;i<requests.length;i++){
			if(requests[i] != null && Time.currDate.after(requests[i].endTime)){
				SneakRequest req = requests[i];
				requests[i] = null;
				createSneakBattle(req);
				Nation sourceNation = getNationByFaction(req.sourceFaction);
				Nation destNation = getNationByFaction(req.destFaction);
				NationSneakBattleFieldDef def = getSneakDef(req.destFaction, req.sourceFaction);
				light(def.sourceFaction,def.getNpc(def.sourceFaction));
				light(def.destFaction,def.getNpc(def.destFaction));
				sourceNation.pool.changeValue(Nation.PROPERTY_SNEAK_ATTACK, 1);
				destNation.pool.changeValue(Nation.PROPERTY_SNEAK_DEFENSE, 1);
				ChatService chatService = Server.server.getServiceRegistry().getChatService();
				chatService.sendFactionSystemMessage(req.destFaction, MessageFormat.format("无耻{0}趁我军不备，袭我军寨，众将烽火台集结拒敌。", GameObject.getFactionName(req.sourceFaction)));
				chatService.sendFactionSystemMessage(req.sourceFaction,
						MessageFormat.format("兵家之道在于出其不意，我国将派军队偷袭{0}，请全体国民去烽火台处应战",
						GameObject.getFactionName(req.destFaction)));
				chatService.sendFactionShout(req.sourceFaction, "反击战开始,请全体将士速去国都烽火台处参战", 0x0000ff, 6000);
				chatService.sendFactionShout(req.destFaction, "反击战开始,请全体将士速去国都烽火台处参战", 0x0000ff, 6000);
			}
		}
	}
	
	protected void createSneakBattle(SneakRequest req){
		NationSneakBattleFieldDef def = getSneakDef(req.destFaction, req.sourceFaction);
		createSneakInstance(def,req.endTime,new Date(req.endTime.getTime()+SNEAK_BATTLE_FIELD_TIME),req.money);
	}
	
	protected void createSneakInstance(NationSneakBattleFieldDef def,Date startTime,Date endTime,int money){
		VMap map = VMapUtil.create(sneakManager, Server.server.getWorld(), def.mapId,Server.server.revision);
		map.sameCellCount = 10;
		map.adjCellCount1 = 4;
		map.adjCellCount1 = 2;
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
		NationSneakBattleFieldInstance instance = new NationSneakBattleFieldInstance(this,map,def,startTime,endTime,money);
		map.instance = instance;
		sneakInstances.add(instance);
		
		// 记录日志
		LogUtil.logNationSneakBattleStart(def.sourceFaction, def.destFaction, startTime);
	}
	
	protected void updateRels(){
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++){
				if(rels[i][j]!=null){
					updateRel(rels[i][j]);
				}
			}
	}
	
	protected void updateRel(NationRel rel){
		if(rel.endTime!=null&&rel.endTime.before(Time.currDate)){
			if(rel.type==NationRel.TYPE_WAR_REQUEST){//如果宣战时间结束还没有确认，那么就进入战前准备
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 20);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND,0);
				rel.type = NationRel.TYPE_WAR_PREPARE;
				rel.createTime = Time.currDate;
				rel.endTime = cal.getTime();
				NationRel destRel = rels[rel.destFaction][rel.sourceFaction];
				destRel.type = NationRel.TYPE_WARED_PREPARE;
				destRel.createTime = Time.currDate;
				destRel.endTime = cal.getTime();
			}
			else if(rel.type==NationRel.TYPE_WAR_PREPARE){
				Date endTime = new Date(Time.currDate.getTime() + BATTLE_FIELD_TIME);
				rel.type = NationRel.TYPE_ATTACK;
				rel.createTime = Time.currDate;
				rel.endTime = endTime;
				NationRel destRel = rels[rel.destFaction][rel.sourceFaction];
				destRel.type = NationRel.TYPE_DEFENSE;
				destRel.createTime = Time.currDate;
				destRel.endTime = endTime;
				createInstance(getDef(rel.destFaction,rel.sourceFaction),Time.currDate,endTime);
				
				// 记录日志
				LogUtil.logNationBattleStart(rel.destFaction, rel.sourceFaction, Time.currDate);
				
				Nation sourceNation = getNationByFaction(rel.sourceFaction);
				Nation destNation = getNationByFaction(rel.destFaction);
				NationBattleFieldDef def = getDef(rel.sourceFaction,rel.destFaction);
				light(def.sourceFaction,def.getNpc(def.sourceFaction));
				light(def.destFaction,def.getNpc(def.destFaction));
				sourceNation.pool.changeValue(Nation.PROPERTY_BATTLE_ATTACK, 1);
				destNation.pool.changeValue(Nation.PROPERTY_BATTLE_DEFENSE, 1);
				ChatService chatService = Server.server.getServiceRegistry().getChatService();
				chatService.sendFactionSystemMessage(rel.sourceFaction, "逐鹿之事，伐无道正天理，众将士拼死杀敌。");
				chatService.sendFactionSystemMessage(rel.destFaction, 
						MessageFormat.format("{0}贼虏进犯边境！烽烟将起众将士请誓死拒敌", GameObject.getFactionName(rel.sourceFaction)));
				chatService.sendFactionShout(rel.sourceFaction, "国战开始,请全体将士速去国都烽火台处参战", 0x0000ff, 6000);
				chatService.sendFactionShout(rel.destFaction, "国战开始,请全体将士速去国都烽火台处参战", 0x0000ff, 6000);
			}
			else if(rel.type==NationRel.TYPE_WIN){
				rel.type = NationRel.TYPE_PEACE;
				rel.createTime = Time.currDate;
				rel.endTime = null;
				NationRel destRel = rels[rel.destFaction][rel.sourceFaction];
				destRel.type = NationRel.TYPE_PEACE;
				rel.createTime = Time.currDate;
				rel.endTime = null;
			}
		}
	}
	
	public void dark(int faction,int[] npc ){
		Nation nation = getNationByFaction(faction);
		if(nation.pool.getInt(Nation.PROPERTY_BATTLE_ATTACK)<=0&&nation.pool.getInt(Nation.PROPERTY_BATTLE_DEFENSE)<=0&&nation.pool.getInt(Nation.PROPERTY_SNEAK_ATTACK)<=0&&nation.pool.getInt(Nation.PROPERTY_SNEAK_DEFENSE)<=0){
			dark(npc);
		}
	}
	
	/**
	 * 熄灭烽火台
	 * @param def
	 */
//	public void dark(NationBattleFieldDef def){
//		int[] npc1 = def.getNpc(def.sourceFaction);
//		int[] npc2 = def.getNpc(def.destFaction);
//		Nation sourceNation = getNationByFaction(def.sourceFaction);
//		Nation destNation = getNationByFaction(def.destFaction);
//		if(sourceNation.pool.getInt(Nation.PROPERTY_BATTLE_ATTACK)<=0&&sourceNation.pool.getInt(Nation.PROPERTY_BATTLE_DEFENSE)<=0) //先查看此属性，保证没有跟本国想过的战场正在进行
//			dark(npc1);
//		if(destNation.pool.getInt(Nation.PROPERTY_BATTLE_ATTACK)<=0&&destNation.pool.getInt(Nation.PROPERTY_BATTLE_DEFENSE)<=0)
//			dark(npc2);
//	}
	
	protected void dark(int[] npc){
		VMapManager manager = Server.server.getWorld().getVMapManager(npc[0]);
		VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(npc[0]);
		for (VMap map : maps) {
			map.refreshNPC(npc[1], true);
		}
		log.info("[LIGHT]"+npc[0]+","+npc[1]);
		for (VMap map : maps) {
			map.refreshNPC(npc[2], false);
		}
		log.info("[DARK]"+npc[0]+","+npc[2]);
	}
	
	public void light(int faction,int[] npc){
		Nation nation = getNationByFaction(faction);
		if(nation.pool.getInt(Nation.PROPERTY_BATTLE_ATTACK)<=0&&nation.pool.getInt(Nation.PROPERTY_BATTLE_DEFENSE)<=0&&nation.pool.getInt(Nation.PROPERTY_SNEAK_ATTACK)<=0&&nation.pool.getInt(Nation.PROPERTY_SNEAK_DEFENSE)<=0){
			light(npc);
		}
	}
	
	/**
	 * 点燃烽火台
	 * @param def
	 */
//	public void light(NationBattleFieldDef def){
//		int[] npc1 = def.getNpc(def.sourceFaction);
//		int[] npc2 = def.getNpc(def.destFaction);
//		Nation sourceNation = getNationByFaction(def.sourceFaction);
//		Nation destNation = getNationByFaction(def.destFaction);
//		if(sourceNation.pool.getInt(Nation.PROPERTY_BATTLE_ATTACK)<=0&&sourceNation.pool.getInt(Nation.PROPERTY_BATTLE_DEFENSE)<=0) //先查看此属性，保证没有跟本国想过的战场正在进行
//			light(npc1);
//		if(destNation.pool.getInt(Nation.PROPERTY_BATTLE_ATTACK)<=0&&destNation.pool.getInt(Nation.PROPERTY_BATTLE_DEFENSE)<=0)
//			light(npc2);
//	}
	
	protected void light(int[] npc){
		VMapManager manager = Server.server.getWorld().getVMapManager(npc[0]);
		VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(npc[0]);
		for (VMap map : maps) {
			map.refreshNPC(npc[1], false);
		}
		log.info("[DARK]"+npc[0]+","+npc[1]);
		for (VMap map : maps) {
			map.refreshNPC(npc[2], true);
		}
		log.info("[LIGHT]"+npc[0]+","+npc[2]);
	}
	
	protected void createInstance(NationBattleFieldDef def,Date startTime,Date endTime){
		VMap map = VMapUtil.create(this, Server.server.getWorld(), def.mapId,Server.server.revision);
		map.sameCellCount = 10;
		map.adjCellCount1 = 4;
		map.adjCellCount1 = 2;
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
		NationBattleFieldInstance instance = new NationBattleFieldInstance(this,map,def,startTime,endTime);
		map.instance = instance;
		instances.add(instance);
	}
	
	public void addDef(NationBattleFieldDef def){
		defs.add(def);
//		long l = 0l;
//		l = l|(def.sourceFaction<<32);
//		l = l| def.destFaction;
//		defs.put(l, def);
	}
	
	public void addSneakDef(NationSneakBattleFieldDef def){
		sneakDefs.add(def);
	}
	
	public NationBattleFieldDef getDef(int sourceFaction,int destFaction){
		for(NationBattleFieldDef def:defs){
			if(def.sourceFaction == sourceFaction&&def.destFaction == destFaction)
				return def;
		}
		return null;
//		long l = 0l;
//		l = l|((sourceFaction)<<32);
//		l = l|destFaction;
//		return defs.get(l);
	}
	
	/**
	 * 
	 * @param sourceFaction 防守方阵营
	 * @param destFaction 进攻方阵营
	 * @return
	 */
	public NationSneakBattleFieldDef getSneakDef(int sourceFaction,int destFaction){
		for(NationSneakBattleFieldDef def:sneakDefs){
			if(def.sourceFaction == sourceFaction&&def.destFaction == destFaction)
				return def;
		}
		return null;
	}
	
	/**
	 * 国公设置进入战场的最低级别
	 * @param p
	 * @param setLevel
	 */
	public void setBattleFieldMinLevel(Player p, int setLevel) throws NationBattleFieldSignupException{
		if(p!=null){
			if(!isKing(p))
				throw new NationBattleFieldSignupException("您不是国公不能使用此功能");
			if(setLevel<50 || setLevel>65)
				throw new NationBattleFieldSignupException("只能设置50到65之间的等级");
			BATTLE_FIELD_MINLEVEL[p.faction] = setLevel;
		}
	}
	
	public int isInNationBattle(Player p){
		if(p!=null){
			for(NationBattleFieldInstance instance : instances){
				if(instance.isInNatinBattle(p)==1)
					return 1;
			}
		}
		return 0;
	}
	
	class SneakBattleFieldVMapManager implements VMapManager{

		public VMap addToMap(Player player, int mapId, int x, int y,
				boolean check) throws VMapException {
			NationSneakBattleFieldInstance instance = getSneakInstanceByFactionAndMapId(
					player.faction, mapId);
			NationSneakBattleFieldDef def = null;
			if (instance != null) {
				if (instance.contains(player)) {
					instance.addPlayer(player);
					player.removeFromMap();
					instance.map.addPlayer(player, x, y);
					return instance.map;
				} else {
					def = instance.def;
				}
			} else {
				def = getSneakDefByFactionAndMapId(player.faction, mapId);
			}
			int[] out = def.getOutPoint(player.faction);
			return Server.server.getWorld().addPlayerToMap(player, out[0], out[1],
					out[2], check);
		}

		public DieCallback dieCallback() {
			return sneakDieCallback;
		}

		public void mapChanged(GameMapDefinition mapDef) {
			
		}
		
		public CreatureDieCallback creatureDieCallback(){
			return sneakCreatureDieCallback;
		}
		
		public MoveCallback moveCallback() {
			return sneakMoveCallback;
		}

		public void removeFromMap(Player player) {
		}

		public void update(int diff) {
			
		}
		
	    public void outPrison(Player p){
	    	OutPrisonUtil.outPrison(p);
	    }
	}
}

class SneakRequest{
	int sourceFaction; //发起国家
	int destFaction; //目标国家
	Date createTime,endTime; //创建时间,终止时间
	int money;
	public SneakRequest(int sourceFaction,int destFaction,Date createTime,Date endTime,int money){
		this.sourceFaction = sourceFaction;
		this.destFaction = destFaction;
		this.createTime = createTime;
		this.endTime = endTime;
		this.money = money;
	}
}

