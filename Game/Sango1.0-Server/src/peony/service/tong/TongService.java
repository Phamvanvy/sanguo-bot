package peony.service.tong;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.mortbay.log.Log;
import peony.db.DBService;
import peony.db.TongMemberDAO;
import peony.game.Actor;
import peony.game.DayListener;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.ItemTemplate;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.game.party.PartyMember;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.Account;
import peony.service.player.ActorCacheService;
import peony.service.stat.StatService;
import peony.service.tong.apply.TongBattleApplyService;
import peony.util.IStringValidator;
import peony.util.IntHashMap;
import peony.util.StringUtil;

/**
 * 军团服务
 * @author lighthu
 */
public class TongService implements Service, ServiceEventListener ,DayListener{
	
	private static final Logger log = Logger.getLogger(TongService.class);
	
	// 所有在线玩家关联的军团
	protected Map<Integer, Tong> tongs = new ConcurrentHashMap<Integer, Tong>();
	// 所有玩家对应的TongMember对象
	protected Map<Integer, TongMember> tongMembers = new ConcurrentHashMap<Integer, TongMember>();
	// 军团邀请ID生成器
	protected AtomicInteger inviteIDGen = new AtomicInteger(0);
	// 所有军团邀请
	protected Map<Integer, TongInvitation> invitations = new ConcurrentHashMap<Integer, TongInvitation>();
	//拒绝加入军团的时间
	protected static long REFUSEADDTONGTIME = 24 * 3600 * 1000L;
	
	
	/**
	 * 帮派职务：都督。
	 */
	public static final int CHAIRMAN = 100;
	/**
	 * 帮派职务：副将。
	 */
	public static final int VICE_CHAIRMAN = 99;
	/**
	 * 帮派职务：参军。
	 */
	public static final int EXPERT = 50;
	/**
	 * 帮派职务：小兵。
	 */
	public static final int NORMAL = 1;
	
	/**
	 * 帮派职务数组，从低到高。
	 */
	public static final int[] DUTIES = { NORMAL, EXPERT, VICE_CHAIRMAN, CHAIRMAN };
	
	/**
	 * 帮派职务名称。
	 */
	public static final String[] DUTY_NAMES = { peony.Messages.STRING_00718, peony.Messages.STRING_00719, peony.Messages.STRING_00720, peony.Messages.STRING_00721 }; 
	
	/**
	 * 各军团级别的人数限制。每个级别4个数字，分别表示：总人数上限、都督上限、副将上限、参军上限。
	 */
	public static final int[][] LEVEL_CONFIG = {
		{ 100, 1, 1, 10 },
		{ 100, 1, 1, 10 },
		{ 100, 1, 1, 10 },
		{ 100, 1, 1, 10 },
		{ 100, 1, 1, 10 },
		{ 100, 1, 1, 10 },
		{ 100, 1, 1, 10 },
	};
	
	public static int[][] TONG_QUESTS_ID = {
			{0, 0},
			{1177},
			{1176},
			{1178}
	};
	
	protected static Map<Integer,TongSkill> skills = new TreeMap<Integer,TongSkill>();
	
	//坑爹的内存问题，存在两份同样的数据的copy对象在内存，引以为戒！！
	/***********************军团扩展新加***********************/
	//所有开启自动接受新人的军团缓存  为节省内存 不对members进行初始化 不用此对象进行数据库的更新操作
//	public Map<Integer, Tong> autoAcceptTongs = new ConcurrentHashMap<Integer, Tong>();
	
	public List<Integer> autoAcceptTongs = new ArrayList<Integer>();
	
	public static int EXIT_TONG_NOUSE_DAOJU = 0;

	public static int EXIT_TONG_USE_DAOJU = 1;
	
	public static int ITEMID_BAIBAO_BOX = 3882;//加军团成功后给军团百宝箱
	
	public static int ITEMID_WANMEI_CHONGZHI = 3888;//贡献度完美重置符
	
	//军团升级所需贡献度                                     				     1   2    3     4     5      6
	public static int[] UPLEVEL_CONTRIBUTE = new int[]{0,0,15000,30000,45000,60000,75000};
	
	public static int MAINTAIN_NO = 0;
	
	public static int MAINTAIN = 1;
	
	public static int ITEMID_ZHENZHU = 1311; //珍珠
	public static int ITEMID_DAZAOFU = 1578; //装备打造符
	public static int ITEMID_DIJIHECHENG = 1336;//低级装备合成符
	public static int ITEMID_CHUANSONG = 1165;//传送符
	public static int ITEMID_ZIZHIJIANDING = 1582;//资质鉴定符
	public static int ITEMID_DOUBING = 797;//豆饼
	public static int ITEMID_XIAOJINGYAN = 2245;//小经验卷轴
	public static int ITEMID_MANHUANG = 984;//蛮荒驯兽铃
	public static int ITEMID_HUANHUN = 670;//还魂香包
	public static int ITEMID_YIHESU = 1183;//一合酥
	public static int ITEMID_DLI = 3883;//军团大礼香包
	
	public static final String PROPERTY_GETCONTRIBUTE_TIMES = "getcontributetimes";//领取百宝箱次数  每日初始化
	
//	//各等级军团百宝箱物品
//	public static int[][] ITEMS_BAIBAO_BOX = new int[][]{
//		{},
//		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG},
//		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING},
//		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING,ITEMID_DOUBING,ITEMID_XIAOJINGYAN},
//		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING,ITEMID_DOUBING,ITEMID_XIAOJINGYAN,ITEMID_MANHUANG},
//		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING,ITEMID_DOUBING,ITEMID_XIAOJINGYAN,ITEMID_MANHUANG,ITEMID_HUANHUN,ITEMID_YIHESU},
//		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING,ITEMID_DOUBING,ITEMID_XIAOJINGYAN,ITEMID_MANHUANG,ITEMID_HUANHUN,ITEMID_YIHESU,ITEMID_DLI}
//	};
	
	//各等级军团百宝箱物品
	public static int[][] ITEMS_BAIBAO_BOX = new int[][]{
		{},
		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG},
		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING},
		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING,ITEMID_DOUBING,ITEMID_XIAOJINGYAN},
		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING,ITEMID_DOUBING,ITEMID_XIAOJINGYAN},
		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING,ITEMID_DOUBING,ITEMID_XIAOJINGYAN,ITEMID_HUANHUN,ITEMID_YIHESU},
		{ITEMID_DAZAOFU,ITEMID_DIJIHECHENG,ITEMID_CHUANSONG,ITEMID_ZIZHIJIANDING,ITEMID_DOUBING,ITEMID_XIAOJINGYAN,ITEMID_HUANHUN,ITEMID_YIHESU,ITEMID_DLI}
	};
	
	//一天
	public static final long ONEDAY = 24 * 3600 * 1000L;
	
	//军团商城物品id                             彩票
	public static int[] shopItemIds = new int[]{3872,3889,3890,3891,3892,665,666,3303};
	
	//对应贡献度
	public static int[] needContributes = new int[]{10,3000,3000,3000,3000,30,30,50};
	
	//对应碎片
	public static int[] needSuiPian = new int[]{0,20,20,20,20,0,0,0,0,0};
	
	//军团商城中忽略职业物品
	public static int[] ignoreClazzItem = new int[]{665,666,3303};
	
	public Map<Integer,ItemTemplate> shopItems = new ConcurrentHashMap<Integer,ItemTemplate>();
	
	//四种职业所需碎片
	public static int[] suiPianIds = new int[]{3884,3885,3886,3887};

	public static final String PROPERTY_TONGBOX="tongbox";//是否已领取过军团百宝箱
	
	public static final int TONGBOX_GIVE = 1;
	
//	public static final String TONG_FACTION = "tongfaction";
	
	public static Random random = new Random();
	
	public static final int OPEN_SKILL_VERSION1 = 4;//上个版本开放的专属科技个数
	
	public static final int OPEN_SKILLS_NUM = 10;//当前开放的专属科技个数
	
	public static final String PROPERTY_LASTINT_DAY = "lastinitday";//最后一次初始化的天数
	
	protected IntHashMap<Integer> getBoxCounts = new IntHashMap<Integer>();
	
	public static int BAIBAOXIANG = 3882;
	
	public static int[] BAIBAOXIANG_GAINITEM = {1578,1336, 1165,1582,797,2245,984,670,1183,3883};
	
	static{
		skills.put(1, new TongSkill1(1));
		skills.put(2, new TongSkill2(1));
		skills.put(3, new TongSkill3(1));
		skills.put(4, new TongSkill4(0));
		skills.put(5, new TongSkill5(0));
		skills.put(6, new TongSkill6(0));
		skills.put(7, new TongSkill7(0));
		skills.put(8, new TongSkill8(0));
		skills.put(9, new TongSkill9(0));
		skills.put(10, new TongSkill10(0));
		skills.put(11, new TongSkill11(0));
		skills.put(12, new TongSkill12(0));
		skills.put(13, new TongSkill13(0));
	}
	
	public static TongSkill newTongSkill(int id,int level){
		TongSkill skill = skills.get(id).clone();
		skill.level = level;
		return skill;
	}
	
	public static Collection<TongSkill> getTongSkills(){
		return skills.values();
	}
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		loadAutoAceeptTongs();
		initShopItems();
		Time.addDayListener(this);
	}
	
	/**初始化军团商店物品**/
	public void initShopItems(){
		for(int i = 0;i < shopItemIds.length;i++){
			ItemTemplate item = ObjectAccessor.getItemTemplate(shopItemIds[i]);
			shopItems.put(shopItemIds[i],item);
		}
	}
	
	/**
	 * 午夜12点维护军团科技
	 * @throws TongException 
	 */
	public void processTongCotribute(){
		DBService db = Server.server.getServiceRegistry().getDbService();
		List<Tong> alltongs = db.tongDAO.getAllTong();
		Iterator<Tong> it = alltongs.iterator();
		while(it.hasNext()){
			Tong t = it.next();
			if(t != null){
				if(t.skills == null || t.skills.skills.size() != 3){
					initTongSkill(t);
					db.tongDAO.updateEntity(t);
				}
				if(tongs.containsKey(t.id)){
					Tong to = tongs.get(t.id);
					if(to!=null ){
						synchronized(to){
							if(maintainSkills(to)){
								db.tongDAO.updateEntity(to);
							}
						}
					}
				}else{
					if(maintainSkills(t)){
						db.tongDAO.updateEntity(t);
					}
				}
			}
		}
	}
	
	/**
	 * 午夜12点初始化在线玩家的每日贡献度
	 */
	public void initOnlinePlayerCon(){
		Iterator<Player> it = ObjectAccessor.players.values().iterator();
		while(it.hasNext()){
			Player p = it.next();
			if(p!=null){
				initPlayerCon(p);
			}
		}
	}
	
	public void initPlayerCon(Player player){
		player.pool.setInt(PROPERTY_LASTINT_DAY, Time.day);
		player.contributeDay = 0;
		addContribute(player, 5 ,true);
		player.pool.setInt(PROPERTY_GETCONTRIBUTE_TIMES, 0);
	}
	
	public static int[] getTongQuestIds(int faction){
		return TONG_QUESTS_ID[faction];
	}
	
	public static boolean isTongQuest(int questId){
		for(int i=0;i<TONG_QUESTS_ID.length;i++){
			for(int j=0;j<TONG_QUESTS_ID[i].length;j++){
				if(TONG_QUESTS_ID[i][j]==questId){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 服务关闭时保存所有军团数据。
	 */
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		for (Tong t : tongs.values()) {
			if(t.modify==true)
			dbs.tongDAO.updateEntity(t);
		}
		for (TongMember m : tongMembers.values()) {
			dbs.tongMemberDAO.updateEntity(m);
		}
	}

	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_PLAYER_LOGINED,
				ServiceEvent.EVENT_PLAYER_SAVED,
				ServiceEvent.EVENT_PLAYER_UNLOADED,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
				ServiceEvent.EVENT_PLAYER_LEVELUP,
				ServiceEvent.EVENT_FINISH_QUEST,
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_BATTLE_WIN,
				ServiceEvent.EVENT_IBUY,
				ServiceEvent.EVENT_WELFARE_FINISH,
				ServiceEvent.EVENT_ACHIEVE_FINISH
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			playerLogin((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_SAVED:
			playerSaved((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_UNLOADED:
//			playerUnloaded((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			playerLogouted((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LEVELUP:
			processPlayerUpLevel((Player)event.param1);
			break;
		case ServiceEvent.EVENT_FINISH_QUEST:
			playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
			break;
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit)event.param1,(Unit)event.param2);
			break;
		case ServiceEvent.EVENT_BATTLE_WIN:
			processBattleWin((Integer) event.param1, (Integer) event.param2);
			break;
		case ServiceEvent.EVENT_IBUY:
			playerBuyOK(((Integer)event.param1).intValue(), ((Integer)event.param2).intValue());
			break;
		case ServiceEvent.EVENT_WELFARE_FINISH:
			processWelfareFinish((Player)event.param1);
			break;
		case ServiceEvent.EVENT_ACHIEVE_FINISH:
			processAchieveFinish((Player)event.param1);
			break;
		}
	}
	
	private void notifyTongLoaded(Tong t) {
		ServiceEvent event = new ServiceEvent(ServiceEvent.EVENT_TONG_LOADED, t);
		Server.server.getEventManager().addEvent(event);
	}
	
	private void notifyPlayerChangeTong(Actor p, Tong t) {
		ServiceEvent event = new ServiceEvent(ServiceEvent.EVENT_PLAYER_CHANGETONG, p, t);
		Server.server.getEventManager().addEvent(event);
	}
	
	private void notifyPlayerExitTong(Actor p, Tong t){
		ServiceEvent event = new ServiceEvent(ServiceEvent.EVENT_PLAYER_LEAVETONG,p,t);
		Server.server.getEventManager().addEvent(event);
	}
	
	private void notifyTongLevel(Tong t){
		ServiceEvent event = new ServiceEvent(ServiceEvent.EVENT_TONG_UPLEVEL,t);
		Server.server.getEventManager().addEvent(event);
	}
	
	/*
	 * 当玩家数据被载入时，同步载入其军团信息。
	 * @param player
	 */
	private void playerLogin(Player player) {
		Server.server.getServiceRegistry().getDbService().
        schedule(new LoadTongLoginCall(player==null ? null : player.session, player));
//		if (!tongMembers.containsKey(player.id)) {
//			DBService dbs = Server.server.getServiceRegistry().getDbService();
//			TongMember m = dbs.tongMemberDAO.findByPlayerID(player.id);
//			if (m != null) {
//				Tong tong = loadTong(m.tongID);
//				if(tong!=null){
//					player.setGuildName(tong.name);
//				}
//			}else if(!player.getGuildName().equals("")){
//				player.addStringPropertyChangedItem(ChangedItem.GUILD, "", false);
//				player.moveExtended |= Player.MOVEEXT_GUILD;
//			}
//		}else{
//			Tong tong = getPlayerTong(player.id,false);
//			if(tong!=null){
//				player.setGuildName(tong.name);
//			}else{
//				tong = loadTong(tongMembers.get(player.id).tongID);
//				if(tong!=null){
//					player.setGuildName(tong.name);
//				}
//			}
//		}
//		//若加入军团但没有百宝箱  加入
//		if(getPlayerTong(player.id,false) != null && player.pool.getInt(PROPERTY_TONGBOX) == 0){
//			giveTongBox(player);
//		}
//		//初始化每日贡献度	
//		if(player.pool.getInt(PROPERTY_LASTINT_DAY, 0) == 0 || player.pool.getInt(PROPERTY_LASTINT_DAY, 0) != Time.day){
//			initPlayerCon(player);
//		}
	}
	
	/*
	 * 载入一个军团的信息，并缓存所有军团成员。
	 */
	public Tong loadTong(int tongID) {
		// 载入军团对象
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		Tong t = tongs.get(tongID);
		if(t==null){
			t = dbs.tongDAO.findByTongID(tongID);
		}
		if (t == null) {
			return null;
		}
		synchronized(this){
		    tongs.put(tongID, t);
		}
		initTong(t);
		return t;
	}	
	
	protected void initTong(Tong t){
		if(t.level == 0){
			t.level = 1;
			t.modify = true;
		}
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		//初始化帮派科技
		initTongSkill(t);
		// 载入所有军团成员
		t.members = dbs.tongMemberDAO.listTongMembers(t.id);
		int count = t.members.size();
		int[] ids = new int[t.members.size()];
		for (int i = 0; i < count; i++) {
			ids[i] = t.members.get(i).id;
		}
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService(); 
		acs.loadAll(ids);
		for (int i = 0; i < count; i++) {
			TongMember m = t.members.get(i);
			m.actor = acs.find(m.id);
			if (m.actor == null) {
				t.members.remove(i);
				i--;
				count--;
				t.modify = true;
			}
		}
		t.peoplenum = count;
		for (int i = 0; i < count; i++) {
			TongMember m = t.members.get(i);
			initTongMemberSkill(m);
			tongMembers.put(m.id, m);
		}
		// 发布军团载入事件
		notifyTongLoaded(t);
	}
	
	/**
	 * 当玩家数据被保存时，同步保存其军团信息。
	 * @param player
	 */
	private void playerSaved(Player player) {
		TongMember m = tongMembers.get(player.id);
		if (m != null) {
			DBService dbs = Server.server.getServiceRegistry().getDbService();
			dbs.tongMemberDAO.updateEntity(m);
		}
	}
	
	/**
	 * 取得指定职位的高一级职位。
	 * @param duty
	 * @return 如果是最高级了，返回-1
	 */
	public static int getNextDuty(int duty) {
		for (int i = 0; i < DUTIES.length - 1; i++) {
			if (DUTIES[i] == duty) {
				return DUTIES[i + 1];
			}
		}
		return -1;
	}
	
	/**
	 * 取得指定职位的第一级职位。
	 * @param duty
	 * @return 如果是最低级了，返回-1
	 */
	public static int getPrevDuty(int duty) {
		for (int i = DUTIES.length -1; i > 0; i--) {
			if (DUTIES[i] == duty) {
				return DUTIES[i - 1];
			}
		}
		return -1;
	}
	
	/**
	 * 取得指定职位的名称。
	 * @param duty
	 * @return
	 */
	public static String getDutyName(int duty) {
		for (int i = DUTIES.length - 1; i >= 0; i--) {
			if (DUTIES[i] == duty) {
				return DUTY_NAMES[i];
			}
		}
		return "";
	}
	
	/**
	 * 取得某个职位在某个级别军团中的人数上限。
	 * @param level 军团级别
	 * @param duty 职位
	 * @return
	 */
	public static int getLimitCount(int level, int duty) {
		int index;
		switch (duty) {
		case CHAIRMAN:
			index = 1;
			break;
		case VICE_CHAIRMAN:
			index = 2;
			break;
		case EXPERT:
			index = 3;
			break;
		default:
			return 0;
		}
		return LEVEL_CONFIG[level][index];
	}
	
	/**
	 * 根据ID查找一个军团对象
	 * @param tongID
	 * @return
	 */
	public Tong getTong(int tongID) {
		return tongs.get(tongID);
	}
	
	/**
	 * 取得一个玩家的军团对象。
	 * @param playerID 玩家ID
	 * @return 如果玩家不在一个军团中，返回null。
	 */
	public Tong getPlayerTong(int playerID,boolean db) {
		TongMember tm = tongMembers.get(playerID);
		if (tm == null) {
			if(db){
				DBService dbs = Server.server.getServiceRegistry().getDbService();
				tm = dbs.tongMemberDAO.findByPlayerID(playerID);
				if (tm != null) {
					synchronized(this){
					    tongMembers.put(playerID, tm);
					}
//					TongService tongService = Server.server.getServiceRegistry().getTongService();
					log.info("[LOADTONG]getPlayerTong");
					return loadTong(tm.tongID);
				}
			}
	        return null;
		} else {
			return tongs.get(tm.tongID);
		}
	}
	
	/**
	 * 取得一个玩家在军团中的信息对象。
	 * @param playerID 玩家ID
	 * @return 如果玩家不在一个军团中，返回null。
	 */
	public TongMember getPlayerInfo(int playerID) {
		return tongMembers.get(playerID);
	}
	
	/**
	 * 创建一个新的军团。
	 * @param owner 创建者
	 * @param name 军团名称
	 * @return 返回新创建的军团对象。
	 * @exception 如果军团名称重复，抛出异常。
	 */
	public Tong createTong(int owner, String name) throws TongException {
		// 判断玩家是否已经有军团了
		TongMember tm = getPlayerInfo(owner);
		if (tm != null) {
			throw new TongException(peony.Messages.STRING_00722);
		}
		
		// 判断名称是否合法
		int code = StringUtil.isValidTongName(name);
		if (code != IStringValidator.OK) {
			throw new TongException(MessageFormat.format(peony.Messages.STRING_00723, StringUtil.getValidatorMessage(code)));
		}
		
		// 判断军团名称是否已存在
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		if (dbs.tongDAO.findByName(name) != null) {
			throw new TongException(peony.Messages.STRING_00724);
		}
		
		// 创建军团对象
		Tong tong = new Tong();
		tong.name = name;
		tong.createTime = new Date();
		tong.owner = owner;
		tong.slogan = "";
		tong.level = 1;
		tong.honor = 0;
		tong.money = 0;
		tong.imoney = 0;
		tong.autoaccept = Tong.AUTOACCEPT_OPEN;
		tong.ismaintain = MAINTAIN;
		tong.peoplenum = 1;
		dbs.tongDAO.newEntity(tong);
		autoAcceptTongs.add(tong.id);
		tong.members = new ArrayList<TongMember>();
		tongs.put(tong.id, tong);
		initTongSkill(tong);
		// 把owner加入军团
		tm = dbs.tongMemberDAO.findByPlayerID(owner);
		if(tm != null){
			tm.tongID = tong.id;
			tm.duty = CHAIRMAN;
			tm.title = "";
			tm.honor = 0;
			tm.money = 0;
			tm.forbid = false;
			initTongMemberSkill(tm);
			tm.actor = Server.server.getServiceRegistry().getActorCacheService().find(owner);
			dbs.tongMemberDAO.updateEntity(tm);
		}else{
			tm = new TongMember();
			tm.id = owner;
			tm.tongID = tong.id;
			tm.duty = CHAIRMAN;
			tm.title = "";
			tm.honor = 0;
			tm.money = 0;
			tm.forbid = false;
			initTongMemberSkill(tm);
			tm.actor = Server.server.getServiceRegistry().getActorCacheService().find(owner);
			dbs.tongMemberDAO.newEntity(tm);
		}
		tong.members.add(tm);
		tongMembers.put(owner, tm);
		Player p = ObjectAccessor.getPlayer(tm.id);
		if(p!=null){
			p.setGuildName(tong.name);
			StatService statService = Server.server.getServiceRegistry().getStatService();
			statService.createTong(p);
			p.refreshProperties(false);
		}
		giveTongBox(p);
		// 发出通知事件
		notifyTongLoaded(tong);
		notifyPlayerChangeTong(tm.actor, tong);
		tong.modify = true;
		return tong;
	}
	
	/**
	 * 初始化一个角色的专属科技
	 */
	public void initTongMemberSkill(TongMember tm){
		//科技是上一个版本的  添加新科技
		if(tm.skills != null && tm.skills.skills.size() == OPEN_SKILL_VERSION1){
			tm.skills.skills.put(8, new TongSkill8(0));
			tm.skills.skills.put(9, new TongSkill9(0));
			tm.skills.skills.put(10, new TongSkill10(0));
			tm.skills.skills.put(11, new TongSkill11(0));
			tm.skills.skills.put(12, new TongSkill12(0));
			tm.skills.skills.put(13, new TongSkill13(0));
			return;
		}
		//从未初始化过专属科技
		if(tm.skills == null || tm.skills.skills.size() != OPEN_SKILLS_NUM ){
			tm.skills = null;
			TongSkills ts = new TongSkills();
			ts.skills.put(4, new TongSkill4(0));
			ts.skills.put(5, new TongSkill5(0));
			ts.skills.put(6, new TongSkill6(0));
			ts.skills.put(7, new TongSkill7(0));
			ts.skills.put(8, new TongSkill8(0));
			ts.skills.put(9, new TongSkill9(0));
			ts.skills.put(10, new TongSkill10(0));
			ts.skills.put(11, new TongSkill11(0));
			ts.skills.put(12, new TongSkill12(0));
			ts.skills.put(13, new TongSkill13(0));
			tm.skills = ts;
		}
	}
	
	/**
	 * 初始化军团科技
	 */
	public void initTongSkill(Tong tong){
		if(tong.skills == null || tong.skills.skills.size() != 3){
			tong.skills = null;
			TongSkills ts = new TongSkills();
			ts.skills.put(1, new TongSkill1(1));
			ts.skills.put(2, new TongSkill2(1));
			ts.skills.put(3, new TongSkill3(1));
			tong.skills = ts;
			tong.modify = true;
		}
	}
	
	/**
	 * 创建一个军团邀请。操作者至少要有参军权限。
	 * @param oper 操作者
	 * @param target 目标用户
	 * @return 返回邀请ID
	 */
	public int createInvitation(int oper, String target) throws TongException {
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException(peony.Messages.STRING_00725);
		}
		if (tm1.duty < EXPERT) {
			throw new TongException(peony.Messages.STRING_00726);
		}
		
		// 检查军团是否已到达人数上限
		Tong tong = getTong(tm1.tongID);
		if (getMemberCount(tong, NORMAL, CHAIRMAN) >= LEVEL_CONFIG[tong.level][0]) {
			throw new TongException(peony.Messages.STRING_00727);
		}

		// 检查玩家是否存在
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService();
		Actor actor1 = acs.find(oper);
		Actor actor2 = acs.find(target);
		if (actor1 == null || actor2 == null ) {
			throw new TongException(peony.Messages.STRING_00270);
		}
		if( actor1.faction != actor2.faction){
			throw new TongException(peony.Messages.STRING_00728);
		}
		if (!actor2.online) {
			throw new TongException(peony.Messages.STRING_00729);
		}
		
		Player t = ObjectAccessor.getPlayer(actor2.id);
		if(t!=null && (t.pool.getLong(Player.PROPERTY_LEAVETONG_TIME, 0)+REFUSEADDTONGTIME)>new Date().getTime()){
			throw new TongException(peony.Messages.STRING_00730);
		}
		
		// 检查目标玩家是否已有军团
		TongMember tm2 = getPlayerInfo(actor2.id);
		if (tm2 != null) {
			if (tm2.id == tm1.id) {
				throw new TongException(peony.Messages.STRING_00731);
			} else {
				throw new TongException(peony.Messages.STRING_00732);
			}
		}
		
		// 创建邀请
		TongInvitation newInvite = new TongInvitation();
		newInvite.id = inviteIDGen.getAndIncrement();
		newInvite.source = actor1;
		newInvite.target = actor2;
		newInvite.tong = tong;
		invitations.put(newInvite.id, newInvite);
		return newInvite.id;
	}
	
	/**
	 * 应邀加入一个军团。
	 * @param inviteID 邀请ID
	 * @param oper 操作者
	 * @throws TongException
	 */
	public void join(int inviteID, int oper) throws TongException {
		// 检查邀请是否存在
		TongInvitation invite = invitations.get(inviteID);
		if (invite == null || invite.target.id != oper) {
			throw new TongException(peony.Messages.STRING_00733);
		}
		invitations.remove(inviteID);
		// 检查玩家是否存在
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService();
		Actor actor = acs.find(oper);
		if (actor == null) {
			throw new TongException(peony.Messages.STRING_00270);
		}
		// 检查玩家是否已经在军团中
		TongMember tm = getPlayerInfo(oper);
		if (tm != null) {
			throw new TongException(peony.Messages.STRING_00734);
		}
		Player t = ObjectAccessor.getPlayer(oper);
		if(t!=null && (t.pool.getLong(Player.PROPERTY_LEAVETONG_TIME, 0)+REFUSEADDTONGTIME)>new Date().getTime()){
			throw new TongException(peony.Messages.STRING_00730);
		}
		synchronized (invite.tong) {
			// 检查军团是否已到达人数上限
			if (getMemberCount(invite.tong, NORMAL, CHAIRMAN) >= LEVEL_CONFIG[invite.tong.level][0]) {
				throw new TongException(peony.Messages.STRING_00727);
			}
			// 把此玩家加入军团
			tm = new TongMember();
			tm.id = oper;
			tm.tongID = invite.tong.id;
			tm.duty = NORMAL;
			tm.title = "";
			tm.honor = 0;
			tm.money = 0;
			tm.forbid = false;
			tm.actor = actor;
			tm.contributeTong = 0;
			initTongMemberSkill(tm);
			invite.tong.members.add(tm);
			invite.tong.modify = true;
			invite.tong.peoplenum = invite.tong.members.size();
			invite.tong.modify = true;
			if(autoAcceptTongs.contains(invite.tong.id)){
				Tong tt = getTong(invite.tong.id);
				tt.peoplenum = invite.tong.members.size();
				tt.modify = true;
			}
		}
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		dbs.tongMemberDAO.newEntity(tm);
		tongMembers.put(oper, tm);
		Player p = ObjectAccessor.getPlayer(actor.id);
		if(p!=null){
			Server.server.getServiceRegistry().getChatService()
			.sendGuildSystemMessage(MessageFormat.format(peony.Messages.STRING_00735, p.name),invite.tong.id);
			p.setGuildName(invite.tong.name);
			p.refreshProperties(false);
		}
		giveTongBox(p);
		log.info("[JOINTONG]PLAYERID[" + p.id + "]TONGID[" + invite.tong.id + "]");
		notifyPlayerChangeTong(actor, invite.tong);
	}
	
	/**
	 * 拒绝一个军团邀请。
	 * @param inviteID 邀请ID
	 * @param oper 操作者
 	 * @throws TongException
	 */
	public void reject(int inviteID, int oper) {
		// 检查邀请是否存在
		TongInvitation invite = invitations.get(inviteID);
		if (invite == null || invite.target.id != oper) {
			return;
		}
		invitations.remove(inviteID);
		Player source = ObjectAccessor.getPlayer(invite.source.id);
		Player target = ObjectAccessor.getPlayer(invite.target.id);
		if(source!=null){
			source.message(-1, "对方拒绝了请求", -1, -1);
			//发私聊，提示
			ChatService cs = Server.server.getServiceRegistry().getChatService();
			String message="如果您不想再接受军团的邀请，可以打开菜单-系统服务-其他设置-免打扰模式中会有一个“军团邀请”的选项，关闭它就可以解决这个烦恼啦！";
			if(target!=null){
				Account account = target.getAccount();
				if(account!=null){
					String mod = null;
					if(account.getUiModel()!=null)
						mod = account.getUiModel().trim();
					if(mod!=null){
						if(mod.equals("AndroidNew") || mod.equals("AndroidLargeNew") || mod.equals("iOSNewUI") || mod.equals("iOSNewUILarge") || mod.equals("Nokia5800New") || mod.equals("Nokia5800NewC")){
							//蓝界面
						}else if(mod.equals("NewUI_AndroidLarge") || mod.equals("NewUI_Android") || mod.equals("NewUI_iOS") || mod.equals("NewUI_iOSLarge")){
							//新ui
							message="如果您不想再接受军团的邀请，可以打开菜单-系统-系统设置-免打扰模式中会有一个“军团邀请”的选项，关闭它就可以解决这个烦恼啦！";
						}else{//java
						}
					}else{//蓝界面
					}
				}
				cs.sendPrivateMessage(invite.target.id,message);
			}
		}
	}
	
	/**
	 * 把一个玩家从军团中移除。操作者至少要有参军权限，且目标权限必须低于操作者。
	 * @param oper 操作者
	 * @param target 目标用户
	 */
	public void removeMember(int oper, int target) throws TongException {
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException(peony.Messages.STRING_00725);
		}
		if (tm1.duty < EXPERT) {
			throw new TongException(peony.Messages.STRING_00726);
		}
		
		Tong tong = getTong(tm1.tongID);

		// 检查目标玩家是否在本军团中
		TongMember tm2 = getPlayerInfo(target);
		if(tm2 == null){
			List<TongMember> tms = tong.members;
			for(TongMember tm : tms){
				if(tm.id == target){
					tm2 = tm;
				}
			}
		}
		if (tm2 == null || tm2.tongID != tm1.tongID) {
			throw new TongException(peony.Messages.STRING_00736);
		}
		
		// 检查目标玩家的职位是否比操作者低
		if (tm1.duty <= tm2.duty) {
			throw new TongException(peony.Messages.STRING_00726);
		}

		// 把此玩家从军团中移除
		synchronized (tong) {
			tong.members.remove(tm2);
			tong.modify = true;
		}
		if(tongMembers.containsKey(target)){
		    tongMembers.remove(target);
		}
		tong.peoplenum = tong.members.size();
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		dbs.tongMemberDAO.makeTransient(tm2);
//		if(autoAcceptTongs.contains(tong.id)){
//			tong.peoplenum = tong.members.size();
//		}
		Player p = ObjectAccessor.getPlayer(tm2.actor.id);
		if(p!=null){
			p.setGuildName(null);
			p.pool.setLong(Player.PROPERTY_LEAVETONG_TIME, new Date().getTime());
			//被踢出军团返还个人贡献度
			p.contribute += tm2.contributeTong;
		}else{
			p = Server.server.getServiceRegistry().getPlayerService().getFromCache(tm2.actor.id);
			if(p==null)
				p = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(tm2.actor.id);
			p.pool.setLong(Player.PROPERTY_LEAVETONG_TIME, new Date().getTime());
			//被踢出军团返还个人贡献度
			p.contribute += tm2.contributeTong;
			Server.server.getServiceRegistry().getDbService().playerDAO.updateEntity(p);
		}
		log.info("[KICKLEAVETONG]ID["+p.id+"]CONTRIBUTEDAY["+p.contributeDay+"]CONTRIBUTE["+p.contribute+"]");
		// 发布通知事件
		notifyPlayerExitTong(tm2.actor, tong);
	}

	public void removeTongMember(int memberId){
		tongMembers.remove(memberId);
	}
	
	public void removeTong(int tongId){
		tongs.remove(tongId);
	}
	
	/**
	 * 提升玩家职位（或者转让都督职位）。
	 * @param oper 操作者
	 * @param target 目标用户
	 */
	public void promote(int oper, int target) throws TongException {
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException(peony.Messages.STRING_00725);
		}
		if (tm1.duty < VICE_CHAIRMAN) {
			throw new TongException(peony.Messages.STRING_00726);
		}

		// 检查目标玩家是否在本军团中
		TongMember tm2 = getPlayerInfo(target);
		Tong tong = getTong(tm1.tongID);
		if(tm2 == null){
			List<TongMember> tms = tong.members;
			for(TongMember tm : tms){
				if(tm.id == target){
					tm2 = tm;
				}
			}
		}
		if (tm2 == null || tm2.tongID != tm1.tongID) {
			throw new TongException(peony.Messages.STRING_00736);
		}
		
		// 检查目标玩家的职位是否比操作者低
		int nextDuty = getNextDuty(tm2.duty);
		if (nextDuty == -1) {
			throw new TongException(peony.Messages.STRING_00726);
		}
		if (tm1.duty != CHAIRMAN && tm1.duty <= nextDuty) {
			throw new TongException(peony.Messages.STRING_00726);
		}

		synchronized (tong) {
			// 检查目标职位的人数是否满了
			if (nextDuty != CHAIRMAN) {
				int existCount = getMemberCount(tong, nextDuty, nextDuty);
				int maxCount = getLimitCount(tong.level, nextDuty);
				if (existCount >= maxCount) {
					throw new TongException(MessageFormat.format(peony.Messages.STRING_00737, getDutyName(nextDuty)));
				}
			}
			TongMemberDAO tmd = Server.server.getServiceRegistry().getDbService().tongMemberDAO;
			// 如果是都督转让，则把原都督的职位修改为副将
			if (nextDuty == CHAIRMAN) {
				//如果是申请城战军团或者城池占领军团的都督转让，则不允许转让
				TongBattleApplyService service = Server.server.getServiceRegistry().getTongBattleApplyService();
				if(service.isPreBattleSide(tm1.tongID)){
					throw new TongException(peony.Messages.STRING_00738);
				}
				tm1.duty = tm2.duty;
				tmd.updateEntity(tm1);
				tmd.updateEntity(tm2);
				tm2.forbid = false;
			}
			// 修改目标的职位
			tm2.duty = nextDuty;
			tmd.updateEntity(tm2);
		}
	}

	/**
	 * 降低玩家职位（不用用于踢人）。
	 * @param oper 操作者
	 * @param target 目标用户
	 */
	public void demote(int oper, int target) throws TongException {
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException(peony.Messages.STRING_00725);
		}
		if (tm1.duty < VICE_CHAIRMAN) {
			throw new TongException(peony.Messages.STRING_00726);
		}

		// 检查目标玩家是否在本军团中
		TongMember tm2 = getPlayerInfo(target);
		Tong tong = getTong(tm1.tongID);
		if(tm2 == null){
			List<TongMember> tms = tong.members;
			for(TongMember tm : tms){
				if(tm.id == target){
					tm2 = tm;
				}
			}
		}
		if (tm2 == null || tm2.tongID != tm1.tongID) {
			throw new TongException(peony.Messages.STRING_00736);
		}
		
		// 检查目标玩家的职位是否比操作者低
		if (tm1.duty <= tm2.duty) {
			throw new TongException(peony.Messages.STRING_00726);
		}

		// 修改目标的职位
		int nextDuty = getPrevDuty(tm2.duty);
		if (nextDuty == -1) {
			throw new TongException(peony.Messages.STRING_00739);
		}
		tm2.duty = nextDuty;
		TongMemberDAO tmd = Server.server.getServiceRegistry().getDbService().tongMemberDAO;
		tmd.updateEntity(tm2);
	}
	
	/**
	 * 设置军团公告。
	 * @param oper 操作者
	 * @param newStr 新公告内容
	 */
	public void setSlogon(int oper, String newStr) throws TongException {
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException(peony.Messages.STRING_00725);
		}
		if (tm1.duty < VICE_CHAIRMAN) {
			throw new TongException(peony.Messages.STRING_00726);
		}
		
		// 检查新公告内容是否合法
		int code = StringUtil.isValidText(newStr);
		if (code != IStringValidator.OK) {
			throw new TongException(MessageFormat.format(peony.Messages.STRING_00740, StringUtil.getValidatorMessage(code)));
		}
		newStr = StringUtil.filterBadWords(newStr);
		
		// 修改公告
		Tong tong = getTong(tm1.tongID);
		tong.slogan = newStr;
	}
	
	/**
	 * 禁言。如果玩家已经被禁言，此方法取消其禁言效果。
	 * @param oper 操作者
	 * @param target 目标用户
	 */
	public void forbid(int oper, int target) throws TongException {
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException(peony.Messages.STRING_00725);
		}
		if (tm1.duty < VICE_CHAIRMAN) {
			throw new TongException(peony.Messages.STRING_00726);
		}

		// 检查目标玩家是否在本军团中
		TongMember tm2 = getPlayerInfo(target);
		Tong tong = getTong(tm1.tongID);
		if(tm2 == null){
			for(TongMember t : tong.members){
				if(t.id == target){
					tm2 = t;
				}
			}
		}
		if (tm2 == null || tm2.tongID != tm1.tongID) {
			throw new TongException(peony.Messages.STRING_00736);
		}
		
		// 检查目标玩家的职位是否比操作者低
		if (tm1.duty <= tm2.duty) {
			throw new TongException(peony.Messages.STRING_00726);
		}

		// 修改目标的禁言标志
		tm2.forbid = !tm2.forbid;
		if(tm2.forbid){
			String msg = MessageFormat.format("{0}被禁言",tm2.actor.name);
			Server.server.getServiceRegistry().getChatService().sendGuildSystemMessage(msg, tm2.tongID);
		}
	}
	
	/**
	 * 计算军团成员数量。
	 * @param minDuty 最小职务(包含)
	 * @param maxDuty 最大职务(包含)
	 * @return
	 */
	public int getMemberCount(Tong tong, int minDuty, int maxDuty) {
		synchronized (tong) {
			if (minDuty == NORMAL && maxDuty == CHAIRMAN) {
				return tong.members.size(); 
			}
			int ret = 0;
			for (TongMember m : tong.members) {
				if (m.duty >= minDuty && m.duty <= maxDuty) {
					ret++;
				}
			}
			return ret;
		}
	}
	
	/**
	 * 列出军团成员（在线的排在前面）。
	 * @param tong 军团对象
	 * @param pageStart 开始下标（0开始）
	 * @param pageSize 页大小
	 * @return
	 */
	public List<TongMember> listMember(Tong tong, int pageStart, int pageSize) {
		synchronized (tong) {
			List<TongMember> ret = new ArrayList<TongMember>();
			List<TongMember> total = new ArrayList<TongMember>();
			List<TongMember> online = getOnlineMembers(tong); // 在线成员
			List<TongMember> notOnline = getNotOnlineMembers(tong); // 非在线成员
			
			// 排序（按在线不在线、职位高低）
			for(int i=0;i<online.size();i++){
				for(int j=i+1;j<online.size();j++){
					TongMember tm1 = online.get(i);
					TongMember tm2 = online.get(j);
					if(tm2.duty>tm1.duty){
						online.set(i, tm2);
						online.set(j, tm1);
					}
				}
			}
			for(int i=0;i<notOnline.size();i++){
				for(int j=i+1;j<notOnline.size();j++){
					TongMember tm1 = notOnline.get(i);
					TongMember tm2 = notOnline.get(j);
					if(tm2.duty>tm1.duty){
						notOnline.set(i, tm2);
						notOnline.set(j, tm1);
					}
				}
			}
			for(TongMember m : online){
				total.add(m);
			}
			for(TongMember m : notOnline){
				total.add(m);
			}
			for(int i=pageStart;i<pageStart+pageSize;i++){
				try {
					ret.add(total.get(i));
				} catch (Exception e) {
					
				}
			}
			return ret;
		}
	}
	
	public List<TongMember> getOnlineMembers(Tong tong){
		synchronized (tong) {
			List<TongMember> online = new ArrayList<TongMember>(); 
			for (TongMember m : tong.members) {
				if (m!=null && m.actor!=null && m.actor.online) {
					online.add(m);
				}
			}
			return online;
		}
	}
	
	public List<TongMember> getNotOnlineMembers(Tong tong){
		synchronized (tong) {
			List<TongMember> notOnline = new ArrayList<TongMember>();
			for (TongMember m : tong.members) {
				if (!m.actor.online) {
					notOnline.add(m);
				}
			}
			return notOnline;
		}
	}
	
	public void update(){
//		for(int tongId : tongs.keySet()){
//			Tong tong = tongs.get(tongId);
//			tong.update();
//		}
	}
	
	/**
	 * 军团改名字。
	 * @param oper 操作员
	 * @param name 新名字
	 */
	public void rename(int oper, String name) throws TongException {
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException(peony.Messages.STRING_00725);
		}
		if (tm1.duty != CHAIRMAN) {
			throw new TongException(peony.Messages.STRING_00741);
		}

		// 判断名称是否合法
		int code = StringUtil.isValidTongName(name);
		if (code != IStringValidator.OK) {
			throw new TongException(MessageFormat.format(peony.Messages.STRING_00723, StringUtil.getValidatorMessage(code)));
		}
		
		// 判断军团名称是否已存在
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		if (dbs.tongDAO.findByName(name) != null) {
			throw new TongException(peony.Messages.STRING_00724);
		}
		
		// 修改军团名称
		Tong tong = tongs.get(tm1.tongID);
		tong.name = name;
		tong.modify = true;
		dbs.tongDAO.updateEntity(tong);
		
		// 修改此军团中所有在线玩家的军团名称
		for (TongMember m : tong.members) {
			if (m.actor.online) {
				Player p = ObjectAccessor.getPlayer(m.id);
				if (p != null) {
					p.setGuildName(name);
				}
			}
		}
	}
	
/**********************************************************军团系统改造***************************************/	
	/**
	 * 开启自动加入军团后玩家自己请求加入军团
	 * @param p
	 * @param tongID
	 */
	public synchronized void atuoJionTong(Player p,int tongID) throws TongException{
		if(p.level<TongInviteCall.INVITEMINLEVEL){
			throw new TongException(MessageFormat.format("您的等级小于{0}级，不能加入军团", TongInviteCall.INVITEMINLEVEL));
		}
		// 检查玩家是否已经在军团中
		TongMember tm = getPlayerInfo(p.id);
		if (tm != null) {
			throw new TongException(peony.Messages.STRING_00734);
		}
		if(p!=null && (p.pool.getLong(Player.PROPERTY_LEAVETONG_TIME, 0)+REFUSEADDTONGTIME)>new Date().getTime()){
			throw new TongException(peony.Messages.STRING_00730);
		}
		Tong tong = tongs.get(tongID);
		if(tong == null){
			log.info("[LOADTONG]atuoJionTong");
			tong = loadTong(tongID);
		}
		if(tong == null)
			throw new TongException(peony.Messages.STRING_00742);
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService();
		Actor actor = acs.find(p.id);
		synchronized (tong) {
			// 检查军团是否已到达人数上限
			if (tong.peoplenum >= LEVEL_CONFIG[tong.level][0])
				throw new TongException(peony.Messages.STRING_00727);
			// 把此玩家加入军团
			tm = new TongMember();
			tm.id = p.id;
			tm.tongID = tong.id;
			tm.duty = NORMAL;
			tm.title = "";
			tm.honor = 0;
			tm.money = 0;
			tm.forbid = false;
			tm.actor = actor;
			tm.contributeTong = 0;
			initTongMemberSkill(tm);
			tong.members.add(tm);
			tong.modify = true;
			tong.peoplenum = tong.members.size();
		}
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		dbs.tongMemberDAO.newEntity(tm);
		tongMembers.put(p.id, tm);
		if(p!=null){
			Server.server.getServiceRegistry().getChatService()
			.sendGuildSystemMessage(MessageFormat.format(peony.Messages.STRING_00735, p.name),tong.id);
			p.setGuildName(tong.name);
			p.refreshProperties(false);
		}
		giveTongBox(p);
		notifyPlayerChangeTong(actor,tong);
	}
	/**赠送军团百宝箱**/
	protected void giveTongBox(Player p){
		if(p.bag.getGameItemCount(ITEMID_BAIBAO_BOX) >= 1){
			return;
		}
		if(p.depot.getGameItemCount(ITEMID_BAIBAO_BOX) >= 1){
			return;
		}
		//赠送军团百宝箱
		PlayerTransaction tx = p.newTransaction("JOINTONG");
		GameItem box_baibao = ObjectAccessor.createGameItem(ITEMID_BAIBAO_BOX);
		try{
			p.bag.addGameItemComplete(box_baibao, 1, tx, true);
			tx.commit();
		}catch(NoEnoughSpaceException e){
			tx.rollback();
	    	MailService mailService = Server.server.getServiceRegistry().getMailService();
	    	mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00743, "", 0, 
	    			box_baibao, 1, "JOINTONG");
	    	Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, peony.Messages.STRING_00744);
		}
		p.pool.setInt(PROPERTY_TONGBOX, TONGBOX_GIVE);
	}
	
	/**玩家完成每日福利**/
	private void processWelfareFinish(Player player){
		addContribute(player,5,true);
	}
	
	/**玩家完成成就**/
	private void processAchieveFinish(Player player){
		addContribute(player,10,true);
	}
	
	/**
	 * 当玩家退出时，同步卸载其军团信息。
	 * @param player
	 */
	private void playerLogouted(Player player) {
		playerSaved(player);
		TongMember tm = tongMembers.get(player.id);
		if(tm!=null){
			if(getPlayerTong(player.id,false)==null)
				return;
			DBService dbs = Server.server.getServiceRegistry().getDbService();
			dbs.tongMemberDAO.updateEntity(tm);
			if(getOnlineMembers(getPlayerTong(player.id,false)).size() == 0){
				Tong tong = tongs.get(tm.tongID);
				if(tong != null){
					dbs.tongDAO.updateEntity(tong);
					tongs.remove(tm.tongID);
				}
			}
			tongMembers.remove(player.id);
		}
	}
	
	/**
	 * 玩家升级事件
	 */
	protected void processPlayerUpLevel(Player player){
		if(player!=null){
			if((player.level == 21||player.level == 26)&&getPlayerTong(player.id,false) == null){
				Packet pt = new Packet(OpCode.TONG_SEND_APPLY_SERVER);
				pt.putInt(OpCode.TONG_SEND_APPLY_SERVER);
				player.send(pt);
			}
		}
	}

	/**
	 * 玩家完成任务事件
	 * @param player
	 */
	public void playerFinishQuest(Player player,int questId,int branch){
		if(isContainOneTong(player))
			addContribute(player, 5,true);
		else
		    addContribute(player, 3,true);
	}
	
	/**
	 * 杀死敌国玩家事件
	 */
	protected void unitDie(Unit u1,Unit u2){
		//杀人
		if(u1.type==GameObject.TYPE_PLAYER&&u2.type==GameObject.TYPE_PLAYER){
			if(u1.faction!=u2.faction){
				if(isContainOneTong((Player)u2)){
					addContribute((Player)u2, 4,true);
					return;
				}
				addContribute((Player)u2, 2,true);
			}
		}
	}
	
	/**
	 * 军团战胜利
	 * @param playerId
	 * @param type
	 */
	public void processBattleWin(int playerId, int type) {
		Player p = ObjectAccessor.getPlayer(playerId);
		if (p != null) {
			if (type == 2) { // 军团战胜利
				addContribute(p, 100,false);
			}
		}
	}
	
	/**
	 * 玩家消费事件。
	 */
	protected void playerBuyOK(int playerID, int amount) {
		Player p = ObjectAccessor.getPlayer(playerID);
		if(p!=null){
			int num = amount /36 /100 /10;
			if(num>0)
				addContribute(p,num,true);
		}
	}
	
	/**
	 * 增加军团贡献度
	 * @param tongId  军团id
	 * @param addNum  增加数量
	 */
	public void addTongContribute(Tong tong,int addNum){
		synchronized (tong) {
			if(tong.contribute >= UPLEVEL_CONTRIBUTE[6])
				return;
			tong.contribute += addNum;
			tong.modify = true;
			for(int i = 1;i < 6;i++){
				if(tong.level == i && tong.contribute >= UPLEVEL_CONTRIBUTE[i + 1]){
					tongUpLevel(tong);
				}
			}
		}
	}
	
	/**
	 * 帮会升级
	 */
	private void tongUpLevel(Tong tong){
		tong.level = tong.level + 1;
		tong.skills.skillLevelUp();
		tong.modify = true;
		notifyTongLevel(tong);
		tong.modify = true;
	}
	
	/**
	 * 减少军团贡献度
	 * @param tongId  军团id
	 * @param addNum  增加数量
	 */
	public void decTongContribute(Tong tong,int decNum) throws TongException{
		if(tong == null)
			throw new TongException(peony.Messages.STRING_00745);
		if(tong.contribute < decNum)
			throw new TongException(peony.Messages.STRING_00746);
		synchronized (tong) {
			tong.contribute -= decNum;
			tong.modify = true;
		}
	}
	
	/**
	 * 设置自动接收新人
	 * @param operate  操作者
	 * @param status   要设置的状态  AUTOACCEPT_OPEN  AUTOACCEPT_COLSE
	 */
	public void setTongAutoAcceptStatus(Player operate,int status) throws TongException{
		Tong tong = getPlayerTong(operate.id,false);
		TongMember tm = getPlayerInfo(operate.id);
		if(tong == null)
			throw new TongException(peony.Messages.STRING_00747);
		if(tm == null)
			throw new TongException(peony.Messages.STRING_00748);
		if(tm.duty < EXPERT)
			throw new TongException(peony.Messages.STRING_00749);
		if(status != Tong.AUTOACCEPT_CLOSE && status != Tong.AUTOACCEPT_OPEN)
			throw new TongException(peony.Messages.STRING_00750);
		synchronized(tong){
			tong.autoaccept = status;
			tong.modify = true;
		}
		if(status == Tong.AUTOACCEPT_OPEN){
			if(!autoAcceptTongs.contains(tong.id))
			     autoAcceptTongs.add(tong.id);
		}
		if(status == Tong.AUTOACCEPT_CLOSE){
			autoAcceptTongs.remove(new Integer(tong.id));
		}
		Packet pt = new Packet(OpCode.TONG_AUTO_APPLY_STATUS_SERVER);
		pt.putInt(OpCode.TONG_AUTO_APPLY_STATUS_CLIENT);
		pt.putInt(getContributeTop(tong));
		operate.send(pt);
	}
	
	/**
	 * 加载所有开启自动接收新人的军团信息简表
	 */
//	public void loadAutoAceeptTongs(){
//		DBService dbs = Server.server.getServiceRegistry().getDbService();
//		List<Integer> tongIds = dbs.tongDAO.getAutoAcceptTongIds();
////		List<Tong> t = dbs.tongDAO.getAutoAcceptTong();
//		Iterator<Integer> it = tongIds.iterator();
//		while(it.hasNext()){
//			int tongId = it.next().intValue();
//			Tong tong = getTong(tongId);
//			if(tong==null){
//				log.info("[LOADTONG]loadAutoAceeptTongs");
//				tong = loadTong(tongId);
//			}
////			if(tong.pool.getInt(TONG_FACTION,0) == 0)
////				continue;
//			tong.members = dbs.tongMemberDAO.listTongMembers(tong.id);
//	        int count = tong.members.size();
//	        int[] ids = new int[tong.members.size()];
//			for (int i = 0; i < count; i++) {
//				ids[i] = tong.members.get(i).id;
//			}
//			ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService(); 
//			acs.loadAll(ids);
//			for (int i = 0; i < count; i++) {
//				TongMember m = tong.members.get(i);
//				m.actor = acs.find(m.id);
//				if (m.actor == null) {
//					tong.members.remove(i);
//					i--;
//					count--;
//				}
//			}
//	        if(tong.members.size()>0){
//	        	tong.peoplenum = tong.members.size();
//		        autoAcceptTongs.add(tong.id);
//	        }
////			if(tong.peoplenum == 0){
////			loadTongPeopleNum(tong);
////			}
//		}
//	}
	
	/**
	 * 加载所有开启自动接收新人的军团信息简表
	 */
	public void loadAutoAceeptTongs(){
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		List<Tong> ts = dbs.tongDAO.getAutoAcceptTong();
		Iterator<Tong> it = ts.iterator();
		while(it.hasNext()){
			Tong tong = it.next();
			initTong(tong);
	        if(tong.members.size()>0){
	        	tong.peoplenum = tong.members.size();
		        autoAcceptTongs.add(tong.id);
	        }
		}
	}
	
	/**
	 * 载入指定军团人数 用于第一次加载某个军团初始化军团人数
	 */
	public void loadTongPeopleNum(Tong t){
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		t.members = dbs.tongMemberDAO.listTongMembers(t.id);
		t.peoplenum =  t.members.size();
	}
	
	/**
	 * 玩家是否是一天的首次登录
	 */
//	public boolean isFirstLogin(Player p){
//		if(p.lastLogoutTime.equals(p.lastLoginTime))
//			return true;
//		Long nextDay = Time.getDateNextDay(p.lastLogoutTime).getTime();
//		Long now = new Date().getTime();
//		if(now > nextDay){
//			return true;
//		}
//		return false;
//	}
	
	/**
	 * 玩家主动退出军团时扣除在本团所西消耗的贡献度
	 */
	public void returnContribute(Player p,int type) throws TongException{
		TongMember tm = getPlayerInfo(p.id);
		if(tm!=null){
			if(type == EXIT_TONG_USE_DAOJU){
				PlayerTransaction tx = p.newTransaction("TONGEXIT");
				GameItem it = p.bag.removeGameItemIngoreInstanceId(ITEMID_WANMEI_CHONGZHI, 1, tx, false);
				if (it == null) {
					tx.rollback();
					throw new TongException(peony.Messages.STRING_00751);
				} else {
					tx.commit();
				}
				p.contribute += tm.contributeTong;
			}else{
				p.contribute += (tm.contributeTong * 70 / 100);
			}
			if(p!=null){
			    log.info("[LEAVETONG]ID["+p.id+"]CONTRIBUTEDAY["+p.contributeDay+"]CONTRIBUTE["+p.contribute+"]");
			}
		}
	}
	
	/**
	 * 玩家在当前军团中的贡献度
	 * @param p
	 */
	public int getPlayerContributeInTong(Player p){
		if(p != null){
			if(tongMembers.get(p.id) != null){
				return tongMembers.get(p.id).contributeTong;
			}
		}
		return 0;
	}
	
	/**
	 * 退出军团。
	 * @param oper 操作者
	 */
	public void exitTong(int oper ,int type) throws TongException {
		Player player = ObjectAccessor.getPlayer(oper);
		if(player == null){
			throw new TongException(peony.Messages.STRING_00752);
		}
		returnContribute(player,type);
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException(peony.Messages.STRING_00725);
		}
		if (tm1.duty == CHAIRMAN) {
			throw new TongException(peony.Messages.STRING_00753);
		}
		
		// 把此玩家从军团中移除
		Tong tong = getTong(tm1.tongID);
		synchronized (tong) {
			tong.members.remove(tm1);
			tong.modify = true;
		}
		tongMembers.remove(oper);
		tong.peoplenum = tong.members.size();
		tong.modify = true;
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		dbs.tongMemberDAO.makeTransient(tm1);
		Player p = ObjectAccessor.getPlayer(tm1.actor.id);
		if(p!=null){
			p.setGuildName(null);
			p.pool.setLong(Player.PROPERTY_LEAVETONG_TIME, new Date().getTime());
			p.refreshProperties(false);
		}else{
			p = Server.server.getServiceRegistry().getPlayerService().getFromCache(tm1.actor.id);
			if(p==null)
				p = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(tm1.actor.id);
			p.pool.setLong(Player.PROPERTY_LEAVETONG_TIME, new Date().getTime());
			Server.server.getServiceRegistry().getDbService().playerDAO.updateEntity(p);
		}
		// 发布通知事件
		notifyPlayerExitTong(tm1.actor, tong);
	}
	
	/**
	 * 五种方式增加贡献度
	 * 1.每日上线          5
	 * 2.完成任务          3
	 * 3.击杀敌国玩家      2
	 * 4.完成成就         10
	 * 5.每日福利         5
	 * 6.军团战胜利       100
	 * type   是否需要判断上限
	 */
	public void addContribute(Player p,int addNum,boolean type){ 
		TongMember tm = getPlayerInfo(p.id);
		Tong tong = getPlayerTong(p.id,false);
		if(tm != null && tong != null){
			synchronized(tong){
				if(addNum <= 0)
					return;
				if(type){
					if(p.contributeDay >= getContributeTop(tong))
						return;
					if((p.contributeDay + addNum) > getContributeTop(tong)){
						if(getContributeTop(tong) - p.contributeDay <= 0){
							return;
						}
						p.contributeDay = getContributeTop(tong);
						addTongContribute(tong, (getContributeTop(tong) - p.contributeDay));
						p.contribute += (getContributeTop(tong) - p.contributeDay);
						return;
					}
					p.contributeDay += addNum;
					p.contribute += addNum;
					addTongContribute(tong, addNum);
					return;
				}
				p.contribute += addNum;
				addTongContribute(tong, addNum);
			}
		}
	}
	
	/**
	 * 秘籍增加军团贡献度
	 */
	public void addTongContributeMiji(Player p,int addNum){
		Tong t = getPlayerTong(p.id,false);
		addTongContribute(t, addNum);
	}
	
	/**
	 * 秘籍 重置军团贡献度
	 */
	public void setTongContribute(Player p,int num){
		Tong t = getPlayerTong(p.id,false);
		t.contribute = num;
		t.modify = true;
	}
	
	/**
	 * 得到军团每日贡献度的上线
	 */
	public int getContributeTop(Tong tong){
		if(tong.autoaccept == Tong.AUTOACCEPT_OPEN)
			return 110;
		if(tong.autoaccept == Tong.AUTOACCEPT_CLOSE)
			return 100;
		return 0;
	}
	
	/**
	 * 判断一个玩家的队伍中是否有同军团的人
	 */
	public boolean isContainOneTong(Player p){
		if(p.party != null){
			Iterator<PartyMember> it = p.party.members.iterator();
			while(it.hasNext()){
				PartyMember pm = it.next();
				if(pm.getId() == p.id)
					continue;
				if(pm.player != null && getPlayerTong(pm.player.id,false)!= null && getPlayerTong(p.id,false)!=null && getPlayerTong(pm.player.id,false).id == getPlayerTong(p.id,false).id){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 使用军团百宝箱
	 */
	public void useBaibaoBox(Player p) throws TongException{
		if(getPlayerTong(p.id,false) == null)
			throw new TongException(peony.Messages.STRING_00756);
		if(p.contributeDay < 20)
			throw new TongException(peony.Messages.STRING_00754);
		if(p.pool.getInt(PROPERTY_GETCONTRIBUTE_TIMES) == 2)
			throw new TongException(peony.Messages.STRING_00755);
		Packet pt = new Packet(OpCode.TONG_USER_BAIBAO_BOX_SERVER);
		pt.putInt(OpCode.TONG_USER_BAIBAO_BOX_CLIENT);
		if(p.pool.getInt(PROPERTY_GETCONTRIBUTE_TIMES) == 0 && p.contributeDay >= 20){
			GameItem zhenzhu = ObjectAccessor.createGameItem(ITEMID_ZHENZHU);
			if(zhenzhu != null){
				PlayerTransaction tx = p.newTransaction("TONGBOX");
				try{
					p.bag.addGameItemComplete(zhenzhu, 2, tx, true);
					tx.commit();
				}catch(NoEnoughSpaceException e){
					tx.rollback();
					MailService mailService = Server.server.getServiceRegistry().getMailService();
					mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00757, "", 0, 
							zhenzhu, 2, "TONGBOX");
				}
			}
			p.pool.setInt(PROPERTY_GETCONTRIBUTE_TIMES,1);
			String msg = peony.Messages.STRING_00758;
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,msg);
			pt.putInt(0);
			p.send(pt);
			return;
		}
		if(p.pool.getInt(PROPERTY_GETCONTRIBUTE_TIMES) == 1 && p.contributeDay < 100){
			throw new TongException(peony.Messages.STRING_00759);
		}
		if(p.pool.getInt(PROPERTY_GETCONTRIBUTE_TIMES) == 1 && p.contributeDay >= 100){
			Tong tong = getPlayerTong(p.id,false);
			if(tong == null)
				throw new TongException(peony.Messages.STRING_00760);
			pt.putInt(1);
			pt.putInt(ITEMS_BAIBAO_BOX[tong.level].length);
			for(int i = 0;i < ITEMS_BAIBAO_BOX[tong.level].length;i++){
				ItemTemplate item = ObjectAccessor.getItemTemplate(ITEMS_BAIBAO_BOX[tong.level][i]);
				pt.putInt(item.id);
				pt.putString(item.name);
				pt.putInt(item.showType);
			}
			p.send(pt);
		}
	}
	
	/**
	 * 获得百宝箱物品
	 */
	public void getBaiBaoItem(Player p,int itemId,int serial) throws TongException{
		if(getPlayerTong(p.id,false) == null)
			throw new TongException(peony.Messages.STRING_00756);
		if(getBoxCounts.get(p.id)!=null && getBoxCounts.get(p.id)>=1)
			throw new TongException("每天只能领取一次奖励");
		if(p.bag.getGameItemCount(itemId)>0)
			throw new TongException("已经有此物品了");
		if(p.depot.getGameItemCount(itemId)>0)
			throw new TongException("已经有此物品了");
		GameItem item = ObjectAccessor.createGameItem(itemId);
		if(item != null){
			PlayerTransaction tx = p.newTransaction("GETTONGBOX");
			try{
				p.bag.addGameItemComplete(item, 1, tx, true);
				tx.commit();
				getBoxCounts.put(p.id, 1);
			}catch(NoEnoughSpaceException e){
				tx.rollback();
				throw new TongException("背包已满");
			}
		}
		Packet pt = new Packet(OpCode.TONG_GET_BAIBAO_SERVER);
		pt.putInt(serial);
		p.send(pt);
	}
	
	/**
	 * 获得百宝箱物品
	 */
	public void getBaiBaoItem(Player p,int itemId) throws TongException{
//		StatService service = Server.server.getServiceRegistry().getStatService();
		if(StatService.isInArray(BAIBAOXIANG_GAINITEM, itemId)==-1)
			throw new TongException("非法的物品");
		if(p.pool.getInt(PROPERTY_GETCONTRIBUTE_TIMES) == 2)
			throw new TongException(peony.Messages.STRING_00755);
		if(getPlayerTong(p.id,false) == null)
			throw new TongException(peony.Messages.STRING_00756);
		if(p.pool.getInt(PROPERTY_GETCONTRIBUTE_TIMES) != 1)
			throw new TongException(peony.Messages.STRING_00761);
		GameItem item = ObjectAccessor.createGameItem(itemId);
		if(item != null){
			PlayerTransaction tx = p.newTransaction("TONGBOX");
			try{
				p.bag.addGameItemComplete(item, 1, tx, true);
				tx.commit();
			}catch(NoEnoughSpaceException e){
				tx.rollback();
				MailService mailService = Server.server.getServiceRegistry().getMailService();
				mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00757, "", 0, 
						item, 1, "TONGBOX");
			}
		}
		p.pool.setInt(PROPERTY_GETCONTRIBUTE_TIMES, 2);
		Packet pt = new Packet(OpCode.TONG_GET_BAIBAO_SERVER);
		pt.putInt(OpCode.TONG_GET_BAIBAO_CLIENT);
		p.send(pt);
	}
	
	/**
	 * 维护技能所需的贡献度
	 */
	public int getMaintainContribute(Tong tong){
		int num = 0;
		Iterator<TongSkill> skills = tong.skills.skills.values().iterator();
		while(skills.hasNext()){
			TongSkill ts = skills.next();
			num += ts.getMaintainContribute(ts.level);
		}
		return num;
	}
	
	/**
	 * 维护军团技能
	 */
	public boolean maintainSkills(Tong tong){
		int decNum = getMaintainContribute(tong);
		if(tong.contribute >= decNum){
			try {
				decTongContribute(tong, decNum);
			} catch (TongException e) {
				return false;
			}
			tong.ismaintain = MAINTAIN;
			tong.modify = true;
			return true;
		}
		if(tong.contribute  < decNum){
			tong.ismaintain = MAINTAIN_NO;
			return true;
		}
		return false;
	}
	
	/**
	 * 升级专属科技
	 * level    当前等级
	 */
	public synchronized void levelUpSkill(Player p,int skillId,int level) throws TongException{
		TongMember tm = getPlayerInfo(p.id);
		int skillLevel = 0;
		if(tm.skills.skills.containsKey(skillId)){
			TongSkill tongSkill = tm.skills.skills.get(skillId);
			if(tongSkill!=null){
				skillLevel = tongSkill.level;
			}
		}
		int nextLevel = skillLevel + 1;
		if(newTongSkill(skillId,0).maxLevel < nextLevel)
			throw new TongException(peony.Messages.STRING_00762);
		TongSkill skill = newTongSkill(skillId, nextLevel);
		if(tm != null && skill!=null){
			if(skill == null)
				throw new TongException(peony.Messages.STRING_00763);
			Log.info(skill.getMaintainContribute(nextLevel) + "");
			if(p.contribute < skill.getMaintainContribute(nextLevel))
				throw new TongException(peony.Messages.STRING_00764);
			p.contribute -= skill.getMaintainContribute(nextLevel);
			if(tm.skills.skills.containsKey(skillId)){
				TongSkill tongskill = tm.skills.skills.get(skillId);
				tongskill.level = nextLevel;
			}else{
				tm.skills.skills.put(skillId,skill);
			}
			tm.contributeTong += skill.getMaintainContribute(nextLevel);
			Server.server.getServiceRegistry().getDbService().tongMemberDAO.updateEntity(tm);
			if(skill.id == 9){
				//刷新buff列表
				p.refreshProperties(false);
			}
		}
	}
	
	/**
	 * 根据商品id得到期所需的贡献度
	 * @param itemId
	 * @return
	 */
	public int getItemContribute(int itemId){
		int con = 0;
		for(int i = 0;i < shopItemIds.length;i++){
			if(shopItemIds[i] == itemId)
				return needContributes[i];
		}
		return con;
	}
	
	/**
	 * 根据商品id得到期所需碎片的数量
	 * @param itemId
	 * @return
	 */
	public int getItemSuipian(int itemId){
		int suipian = 0;
		for(int i = 0;i < shopItemIds.length;i++){
			if(shopItemIds[i] == itemId)
				return needSuiPian[i];
		}
		return suipian;
	}
	
	/**
	 *商品是否受职业限制
	 * @param itemId
	 * @return
	 */
	public boolean ignoreClazz(int itemId){
		for(int i=0;i<ignoreClazzItem.length;i++){
			if(itemId == ignoreClazzItem[i]){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 购买军团商店中的物品
	 */
	public void tongShopBuy(Player p,int itemId) throws TongException{
		if(!shopItems.containsKey(itemId))
			throw new TongException(peony.Messages.STRING_00765);
		Tong tong = getPlayerTong(p.id,false);
		if(tong == null)
			throw new TongException(peony.Messages.STRING_00766);
		int needcon = getItemContribute(itemId);
		int needsuipian = getItemSuipian(itemId);
		if(needcon < 0)
			throw new TongException(peony.Messages.STRING_00765);
		if(p.contribute < needcon)
			throw new TongException(peony.Messages.STRING_00767);
		if(needsuipian > 0){
			int clazz = 0;
			for(int i = 0; i < shopItemIds.length;i++){
				if(shopItemIds[i] == itemId){
					clazz = i - 1;
					break;
				}
			}
			if(clazz != p.clazz && !ignoreClazz(itemId))
				throw new TongException(peony.Messages.STRING_00768);
			if(needsuipian > 0 && p.bag.getGameItemCount(suiPianIds[clazz])< needsuipian)
				throw new TongException(peony.Messages.STRING_00769);
		}
		PlayerTransaction tx = p.newTransaction("TONGSHOPBUY");
		p.decContribute(needcon);
		if(needsuipian > 0)
			p.bag.removeGameItemIngoreInstanceId(suiPianIds[p.clazz],needsuipian,tx,true);
		GameItem box_baibao = ObjectAccessor.createGameItem(itemId);
		try{
			p.bag.addGameItemComplete(box_baibao, 1, tx, true);
			tx.commit();
		}catch(NoEnoughSpaceException e){
			tx.rollback();
			MailService mailService = Server.server.getServiceRegistry().getMailService();
	    	mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00743, "", 0, 
	    			box_baibao, 1, "TONGSHOPBUY");
		}
	}

	public void dayChanged() {
		new Thread(new Runnable(){
			public void run(){
				try{
					processTongCotribute();
					initOnlinePlayerCon();
				}catch(Exception e){
				}
			}
		}).start();
		getBoxCounts.clear();
	}
	
	@SuppressWarnings("unchecked")
	public void convertSort(List<Tong> factionTong){
		Collections.sort(factionTong, new ConverSort());
	}
}

@SuppressWarnings("unchecked")
class ConverSort implements Comparator {
	public int compare(Object o1, Object o2) {
		Tong t1 = (Tong) o1;
		Tong t2 = (Tong) o2;
		if (t1.peoplenum<t2.peoplenum)
			return 1;
		else 
			return 0;
	}
}
