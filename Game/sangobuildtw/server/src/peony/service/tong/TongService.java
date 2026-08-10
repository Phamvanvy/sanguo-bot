package peony.service.tong;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import peony.db.DBService;
import peony.game.Actor;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.changed.ChangedItem;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.player.ActorCacheService;
import peony.service.tong.apply.TongBattleApplyService;
import peony.util.IStringValidator;
import peony.util.StringUtil;

/**
 * 管理玩家关系的服务。玩家的关系包括：好友关系、黑名单、仇人关系、临时关系。 
 * @author lighthu
 */
public class TongService implements Service, ServiceEventListener {
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
	public static final String[] DUTY_NAMES = { "軍士", "參軍", "副將", "都督" }; 
	
	/**
	 * 各军团级别的人数限制。每个级别4个数字，分别表示：总人数上限、都督上限、副将上限、参军上限。
	 */
	public static final int[][] LEVEL_CONFIG = {
		{ 100, 1, 1, 10 },
		{ 100, 1, 1, 10 },
		{ 150, 1, 1, 12 },
		{ 210, 1, 2, 14 },
		{ 280, 1, 2, 16 },
		{ 360, 1, 3, 20 }
	};
	
	public static int[][] TONG_QUESTS_ID = {
			{0, 0},
			{1177},
			{1176},
			{1178}
	};
	
	protected static Map<Integer,TongSkill> skills = new TreeMap<Integer,TongSkill>();
	
	static{
		skills.put(1, new TongSkill1(0));
		skills.put(2, new TongSkill2(0));
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
			dbs.tongDAO.updateEntity(t);
		}
		for (TongMember m : tongMembers.values()) {
			dbs.tongMemberDAO.updateEntity(m);
		}
	}

	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_PLAYER_LOADED,
				ServiceEvent.EVENT_PLAYER_SAVED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_LOADED:
			playerLoaded((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_SAVED:
			playerSaved((Player)event.param1);
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
	
	/*
	 * 当玩家数据被载入时，同步载入其军团信息。
	 * @param player
	 */
	private void playerLoaded(Player player) {
		if (!tongMembers.containsKey(player.id)) {
			DBService dbs = Server.server.getServiceRegistry().getDbService();
			TongMember m = dbs.tongMemberDAO.findByPlayerID(player.id);
			if (m != null) {
				Tong tong = loadTong(m.tongID);
				if(tong!=null){
					player.setGuildName(tong.name);
				}
			}else if(!player.getGuildName().equals("")){
				player.addStringPropertyChangedItem(ChangedItem.GUILD, "", false);
				player.moveExtended |= Player.MOVEEXT_GUILD;
			}
		}else{
			Tong tong = getPlayerTong(player.id);
			if(tong!=null){
				player.setGuildName(tong.name);
			}
		}
	}
	
	/*
	 * 载入一个军团的信息，并缓存所有军团成员。
	 */
	public Tong loadTong(int tongID) {
		// 载入军团对象
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		Tong t = dbs.tongDAO.findByTongID(tongID);
		if (t == null) {
			return null;
		}
		tongs.put(tongID, t);
		
		//初始化帮派科技
		t.skills.init(getTongSkills());
		
		// 载入所有军团成员
		t.members = dbs.tongMemberDAO.listTongMembers(tongID);
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
			}
		}
		for (int i = 0; i < count; i++) {
			TongMember m = t.members.get(i);
			tongMembers.put(m.id, m);
		}
		
		// 发布军团载入事件
		notifyTongLoaded(t);
		return t;
	}
	
	/*
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
	public Tong getPlayerTong(int playerID) {
		TongMember tm = tongMembers.get(playerID);
		if (tm == null) {
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
			throw new TongException("您已經在一個軍團里了");
		}
		
		// 判断名称是否合法
		int code = StringUtil.isValidTongName(name);
		if (code != IStringValidator.OK) {
			throw new TongException(MessageFormat.format("軍團名稱{0}", StringUtil.getValidatorMessage(code)));
		}
		
		// 判断军团名称是否已存在
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		if (dbs.tongDAO.findByName(name) != null) {
			throw new TongException("這個名稱已經被使用了");
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
		tong.members = new ArrayList<TongMember>();
		dbs.tongDAO.newEntity(tong);
		tongs.put(tong.id, tong);
		
		// 把owner加入军团
		tm = new TongMember();
		tm.id = owner;
		tm.tongID = tong.id;
		tm.duty = CHAIRMAN;
		tm.title = "";
		tm.honor = 0;
		tm.money = 0;
		tm.forbid = false;
		tm.actor = Server.server.getServiceRegistry().getActorCacheService().find(owner);
		dbs.tongMemberDAO.newEntity(tm);
		tong.members.add(tm);
		tongMembers.put(owner, tm);
		
		Player p = ObjectAccessor.getPlayer(tm.id);
		if(p!=null)
			p.setGuildName(tong.name);
		// 发出通知事件
		notifyTongLoaded(tong);
		notifyPlayerChangeTong(tm.actor, tong);
		tong.skills.init(getTongSkills());
		return tong;
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
			throw new TongException("您不在一個軍團中");
		}
		if (tm1.duty < EXPERT) {
			throw new TongException("權限不足");
		}
		
		// 检查军团是否已到达人数上限
		Tong tong = getTong(tm1.tongID);
		if (getMemberCount(tong, NORMAL, CHAIRMAN) >= LEVEL_CONFIG[tong.level][0]) {
			throw new TongException("已到達人數上限");
		}

		// 检查玩家是否存在
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService();
		Actor actor1 = acs.find(oper);
		Actor actor2 = acs.find(target);
		if (actor1 == null || actor2 == null ) {
			throw new TongException("目標不存在");
		}
		if( actor1.faction != actor2.faction){
			throw new TongException("對方是敵對國家玩家");
		}
		if (!actor2.online) {
			throw new TongException("目標不在線");
		}
		
		Player t = ObjectAccessor.getPlayer(actor2.id);
		if(t!=null && (t.pool.getLong(Player.PROPERTY_LEAVETONG_TIME, 0)+REFUSEADDTONGTIME)>new Date().getTime()){
			throw new TongException("退出軍團后24小時內不能加入軍團");
		}
		
		// 检查目标玩家是否已有军团
		TongMember tm2 = getPlayerInfo(actor2.id);
		if (tm2 != null) {
			if (tm2.id == tm1.id) {
				throw new TongException("已經在軍團中了");
			} else {
				throw new TongException("已經加入別的軍團了");
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
			throw new TongException("邀請已失效");
		}
		invitations.remove(inviteID);
		
		// 检查玩家是否存在
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService();
		Actor actor = acs.find(oper);
		if (actor == null) {
			throw new TongException("目標不存在");
		}
		
		// 检查玩家是否已经在军团中
		TongMember tm = getPlayerInfo(oper);
		if (tm != null) {
			throw new TongException("你已經在一個軍團中了");
		}
		
		Player t = ObjectAccessor.getPlayer(oper);
		if(t!=null && (t.pool.getLong(Player.PROPERTY_LEAVETONG_TIME, 0)+REFUSEADDTONGTIME)>new Date().getTime()){
			throw new TongException("退出軍團后24小時內不能加入軍團");
		}
		
		synchronized (invite.tong) {
			// 检查军团是否已到达人数上限
			if (getMemberCount(invite.tong, NORMAL, CHAIRMAN) >= LEVEL_CONFIG[invite.tong.level][0]) {
				throw new TongException("已到達人數上限");
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
			invite.tong.members.add(tm);
		}
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		dbs.tongMemberDAO.newEntity(tm);
		tongMembers.put(oper, tm);
		Player p = ObjectAccessor.getPlayer(actor.id);
		if(p!=null){
			Server.server.getServiceRegistry().getChatService()
			.sendGuildSystemMessage(MessageFormat.format("{0}加入軍團", p.name),invite.tong.id);
			p.setGuildName(invite.tong.name);
			}
		// 发出通知事件
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
			throw new TongException("您不在一個軍團中");
		}
		if (tm1.duty < EXPERT) {
			throw new TongException("權限不足");
		}

		// 检查目标玩家是否在本军团中
		TongMember tm2 = getPlayerInfo(target);
		if (tm2 == null || tm2.tongID != tm1.tongID) {
			throw new TongException("目標不在軍團中");
		}
		
		// 检查目标玩家的职位是否比操作者低
		if (tm1.duty <= tm2.duty) {
			throw new TongException("權限不足");
		}

		// 把此玩家从军团中移除
		Tong tong = getTong(tm1.tongID);
		synchronized (tong) {
			tong.members.remove(tm2);
		}
		tongMembers.remove(target);
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		dbs.tongMemberDAO.makeTransient(tm2);
		
		Player p = ObjectAccessor.getPlayer(tm2.actor.id);
		if(p!=null){
			p.setGuildName(null);
			p.pool.setLong(Player.PROPERTY_LEAVETONG_TIME, new Date().getTime());
		}else{
			p = Server.server.getServiceRegistry().getPlayerService().getFromCache(tm2.actor.id);
			if(p==null)
				p = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(tm2.actor.id);
			p.pool.setLong(Player.PROPERTY_LEAVETONG_TIME, new Date().getTime());
			Server.server.getServiceRegistry().getDbService().playerDAO.updateEntity(p);
		}
		// 发布通知事件
		notifyPlayerExitTong(tm2.actor, tong);
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
			throw new TongException("您不在一個軍團中");
		}
		if (tm1.duty < VICE_CHAIRMAN) {
			throw new TongException("權限不足");
		}

		// 检查目标玩家是否在本军团中
		TongMember tm2 = getPlayerInfo(target);
		if (tm2 == null || tm2.tongID != tm1.tongID) {
			throw new TongException("目標不在軍團中");
		}
		
		// 检查目标玩家的职位是否比操作者低
		int nextDuty = getNextDuty(tm2.duty);
		if (nextDuty == -1) {
			throw new TongException("權限不足");
		}
		if (tm1.duty != CHAIRMAN && tm1.duty <= nextDuty) {
			throw new TongException("權限不足");
		}

		Tong tong = getTong(tm1.tongID);
		synchronized (tong) {
			// 检查目标职位的人数是否满了
			if (nextDuty != CHAIRMAN) {
				int existCount = getMemberCount(tong, nextDuty, nextDuty);
				int maxCount = getLimitCount(tong.level, nextDuty);
				if (existCount >= maxCount) {
					throw new TongException(MessageFormat.format("{0}的名額已經滿了", getDutyName(nextDuty)));
				}
			}
			
			// 如果是都督转让，则把原都督的职位修改为副将
			if (nextDuty == CHAIRMAN) {
				//如果是申请城战军团或者城池占领军团的都督转让，则不允许转让
				TongBattleApplyService service = Server.server.getServiceRegistry().getTongBattleApplyService();
				if(service.isPreBattleSide(tm1.tongID)){
					throw new TongException("奪城戰時,軍團都督不得轉讓");
				}
				tm1.duty = tm2.duty;
			}

			// 修改目标的职位
			tm2.duty = nextDuty;
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
			throw new TongException("您不在一個軍團中");
		}
		if (tm1.duty < VICE_CHAIRMAN) {
			throw new TongException("權限不足");
		}

		// 检查目标玩家是否在本军团中
		TongMember tm2 = getPlayerInfo(target);
		if (tm2 == null || tm2.tongID != tm1.tongID) {
			throw new TongException("目標不在軍團中");
		}
		
		// 检查目标玩家的职位是否比操作者低
		if (tm1.duty <= tm2.duty) {
			throw new TongException("權限不足");
		}

		// 修改目标的职位
		int nextDuty = getPrevDuty(tm2.duty);
		if (nextDuty == -1) {
			throw new TongException("已經不能再降職了");
		}
		tm2.duty = nextDuty;
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
			throw new TongException("您不在一個軍團中");
		}
		if (tm1.duty < VICE_CHAIRMAN) {
			throw new TongException("權限不足");
		}
		
		// 检查新公告内容是否合法
		int code = StringUtil.isValidText(newStr);
		if (code != IStringValidator.OK) {
			throw new TongException(MessageFormat.format("公告內容{0}", StringUtil.getValidatorMessage(code)));
		}
		newStr = StringUtil.filterBadWords(newStr);
		
		// 修改公告
		Tong tong = getTong(tm1.tongID);
		tong.slogan = newStr;
	}
	
	/**
	 * 退出军团。
	 * @param oper 操作者
	 */
	public void exitTong(int oper) throws TongException {
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException("您不在一個軍團中");
		}
		if (tm1.duty == CHAIRMAN) {
			throw new TongException("必須轉讓都督職位后才能退出軍團");
		}
		
		// 把此玩家从军团中移除
		Tong tong = getTong(tm1.tongID);
		synchronized (tong) {
			tong.members.remove(tm1);
		}
		tongMembers.remove(oper);
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		dbs.tongMemberDAO.makeTransient(tm1);
		Player p = ObjectAccessor.getPlayer(tm1.actor.id);
		if(p!=null){
			p.setGuildName(null);
			p.pool.setLong(Player.PROPERTY_LEAVETONG_TIME, new Date().getTime());
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
	 * 禁言。如果玩家已经被禁言，此方法取消其禁言效果。
	 * @param oper 操作者
	 * @param target 目标用户
	 */
	public void forbid(int oper, int target) throws TongException {
		// 检查操作者是否有权限
		TongMember tm1 = getPlayerInfo(oper);
		if (tm1 == null) {
			throw new TongException("您不在一個軍團中");
		}
		if (tm1.duty < VICE_CHAIRMAN) {
			throw new TongException("權限不足");
		}

		// 检查目标玩家是否在本军团中
		TongMember tm2 = getPlayerInfo(target);
		if (tm2 == null || tm2.tongID != tm1.tongID) {
			throw new TongException("目標不在軍團中");
		}
		
		// 检查目标玩家的职位是否比操作者低
		if (tm1.duty <= tm2.duty) {
			throw new TongException("權限不足");
		}

		// 修改目标的禁言标志
		tm2.forbid = !tm2.forbid;
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
			int index = 0;
			List<TongMember> ret = new ArrayList<TongMember>();
			
			// 先扫描在线的成员
			for (TongMember m : tong.members) {
				if (index >= pageStart + pageSize) {
					break;
				}
				if (m.actor.online) {
					if (index >= pageStart) {
						ret.add(m);
					}
					index++;
				}
			}
			
			// 然后扫描不在线的成员
			for (TongMember m : tong.members) {
				if (index >= pageStart + pageSize) {
					break;
				}
				if (!m.actor.online) {
					if (index >= pageStart) {
						ret.add(m);
					}
					index++;
				}
			}
			return ret;
		}
	}
	
	public void update(){
		for(int tongId : tongs.keySet()){
			Tong tong = tongs.get(tongId);
			tong.update();
		}
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
			throw new TongException("您不在一個軍團中");
		}
		if (tm1.duty != CHAIRMAN) {
			throw new TongException("您不是軍團長");
		}

		// 判断名称是否合法
		int code = StringUtil.isValidTongName(name);
		if (code != IStringValidator.OK) {
			throw new TongException(MessageFormat.format("軍團名稱{0}", StringUtil.getValidatorMessage(code)));
		}
		
		// 判断军团名称是否已存在
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		if (dbs.tongDAO.findByName(name) != null) {
			throw new TongException("這個名稱已經被使用了");
		}
		
		// 修改军团名称
		Tong tong = tongs.get(tm1.tongID);
		tong.name = name;
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
}
