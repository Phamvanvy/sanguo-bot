package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;
import peony.channel.Channel;
import peony.db.DBService;
import peony.db.LogoutCall;
import peony.depot.AccountDepotSaveCall;
import peony.game.admin.GMRequest;
import peony.game.association.Association;
import peony.game.association.AssociationException;
import peony.game.association.AssociationInvite;
import peony.game.association.AssociationMember;
import peony.game.association.AssociationService;
import peony.game.attendant.Attendant;
import peony.game.attendant.AttendantBag;
import peony.game.battlefield.FlagBattleFieldVMapManager;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.changed.AddTitleChangedItem;
import peony.game.changed.BindChangedItem;
import peony.game.changed.ChangedItem;
import peony.game.changed.PacketChangedItemVisitor;
import peony.game.changed.SkillChangedItem;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.exchange.Exchange;
import peony.game.exp.ExpService;
import peony.game.itemeffect.AddItemEffect;
import peony.game.itemenhance.AutoAddHole;
import peony.game.itemenhance.AutoNaturalEnhance;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.JewelService;
import peony.game.itemenhance.NaturalEnhance;
import peony.game.map.ForceGoMapCall;
import peony.game.map.ReliveTransferCall;
import peony.game.nation.CandidateService;
import peony.game.nation.Nation;
import peony.game.nation.NationSkill2;
import peony.game.nation.NationSkill3;
import peony.game.nation.NationSkill6;
import peony.game.party.Party;
import peony.game.pk.PkInfo;
import peony.game.skill.Skill;
import peony.game.suite.SuiteEffect;
import peony.game.suite.SuiteEffects;
import peony.game.touchaction.TouchAction;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.account.AccountStatService;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.player.PlayerService;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
import peony.util.TimeUtil;
import peony.vm.ASMGameVM;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;
import com.pip.net.message.gameaccount.OnlineTimeNotifyMessage;
import com.pip.sanguo.data.quest.QuestTarget;

@Entity
@Table(name = "player")
@AccessType("field")
public class Player extends Unit implements Client {

	public static final WarState PVESTATE = new PveState();
	public static final WarState PVPSTATE = new PvpState();
	public static final WarState PVEPVPSTATE = new PvePvpState();
	public static final WarState PVPPVESTATE = new PvpPveState();
	
	private static final int AUTO_ATTACK_INTERVAL = 3000;
	public static final int MAX_PVE_LEVEL = 35;
	public static final int MAX_LEVEL = 90;

	private static final Logger log = Logger.getLogger(Player.class);

	public static final int NOTIFY_CHAT = 1;
	public static final int NOTIFY_MESSAGE = 2;
	public static final int NOTIFY_QUESTION = 3;

	public static final int PVP_TIME = 3 * 60 * 1000; // 3分钟

	@Column(name = "accountid", nullable = false)
	public int accountId;

	@Transient
	public ClientSession session;

	@Transient
	public int[] attack_timer = new int[3];
	@Transient
	public GameObjectRef targetRef;
	public static final int ATTACK_NO = -2;
	public static final int ATTACK_PREPARE = -1;

	@Transient
	public Creature lastkillCreature;
	@Transient
	public Player lastkillPlayer;
	@Transient
	public String lastMessage;
	@Transient
	public List<TouchNpcInfo> touchedNpc = new LinkedList<TouchNpcInfo>();
	@Transient
	public int questToFinishQuestId = -1;

	@Transient
	public List<UINotify> uiNotifies = new LinkedList<UINotify>();

	@Type(type = "peony.game.ASMGameVMUserType")
	@Column(name = "vm")
	public ASMGameVM asmVm;

	@Transient
	public Random rnd = new Random();

	@Type(type = "peony.game.TransactionBagUseType")
	@Column(name = "bag")
	public TransactionBag bag;

	@Transient
	public KillCreatureRecorder kills = new KillCreatureRecorder();

	// 本cycle杀死的玩家ID集合
	@Transient
	public Set<Integer> killPlayers = new HashSet<Integer>();

	// 死亡原因，0 - 未死亡，1 - 被玩家杀死，2 - 被怪物杀死
	@Transient
	public int dieCause;

	@Column(name = "skillpoint", nullable = false)
	public int skillPoint;

	@Column(name = "exp", nullable = false)
	public int exp;

	@Column(name = "propertypoint", nullable = false)
	public int propertyPoint;

	@Column(name = "actionbar")
	public byte[] actionBarOptions;

	@Column(name = "config")
	public byte[] config;

	@Transient
	public PlayerService serivce;

	@Type(type = "peony.game.ChatOptionsUserType")
	@Column(name = "chatoptions")
	public ChatOptions chatOptions;

	@Transient
	volatile public int loadTimes; // 维护客户端发送LOADING_FINISHED_CLIENT的次数

	@Transient
	public Party party;

	@Transient
	public int systemState;
	@Transient
	public long changeStateStamp;

	@Column(name = "exist", nullable = false)
	public int exist; // 1 代表角色正常 0代表角色删除

	@Transient
	public long loginTime;// logout的时间，如果logout时间超过10分钟，那么将会从ObjectAccessor中删除

	@Transient
	public int ignoreMoveTime; // 复活以后5秒内忽略move信息

	@Column(name = "money", nullable = false)
	public int money;

	@Column(name = "honor", nullable = false)
	public int honor;

	public static final int SYSTEMSTATE_LOAD = 1;// 被加载到内存
	public static final int SYSTEMSTATE_LOGINED = 2; // 登录,在过地图的时候也转换到这种状态
	public static final int SYSTEMSTATE_READY = 3; // 到每个地图以后收到第一个move信息以后设置成这个状态
	public static final int SYSTEMSTATE_DISCONNECTED = 4; // 断连

	@Transient
	protected List<CoolDown> cds = null;

	@Transient
	public ReliveOptions reliveOptions = null;

	@Transient
	public AutoAttack autoAttack = null;

	@Transient
	protected List<PlayerTransaction> transactions = new LinkedList<PlayerTransaction>();
	@Transient
	protected Lock txLock = new ReentrantLock();

	// protected Ride ride = null;
	@Transient
	protected ItemUse itemUse = null;

	@Transient
	public int lastSkillId = -1;

	@Transient
	public int lastItemId = -1;

	@Column(name = "createtime", nullable = false)
	public Date createTime;

	@Transient
	protected String guildName;

	@Transient
	public int mapNumber;

	@Transient
	public boolean cheat;

	// 记录每个威胁自己的敌对玩家的最后威胁时间
	@Transient
	public Map<Integer, Integer> enemyPlayers = new HashMap<Integer, Integer>();

	@Transient
	public Exchange exchange;

	@Column(name = "titles")
	@Type(type = "peony.game.TitlesUserType")
	public Titles titles;

	@Transient
	public int pvpTime;

	@Transient
	public int pvpFactionTime;
	
	@Transient
	public int pvp2pveMapId;

	@Transient
	protected TransactionIntProperty moneyTx; // 管理Money的事务，Money字段不能直接访问，必须通过此字段间接访问

	@Transient
	protected TransactionIntProperty creditTx;

	@Transient
	protected TransactionIntProperty honorTx;

	@Transient
	protected TransactionIntProperty expTx;

	// @Transient
	// protected List<ClientMove> moves = new LinkedList<ClientMove>();

	@Transient
	protected Position lastPosition, lastCalcPosition = null;

	@Transient
	public int forbidScore, runDist, violationTime1, violationTime2;

	@Column(name = "horses")
	@Type(type = "peony.game.HorseBagUserType")
	public HorseBag horseBag;

	@Transient
	public HorseRide ride;

	@Column(name = "pool")
	@Type(type = "peony.game.PropertyPoolType")
	public PropertyPool pool;

	@Transient
	public Gather gather;

	@Column(name = "lastlogin", nullable = true)
	public Date lastLoginTime;

	@Transient
	public boolean acceptMoving;

	@Transient
	public boolean logouted; // 是否是正常退出

	@Transient
	public Flag flag;
	
//	@Transient 
//	public ReliveOption relive; 

	@Column(name="activepower", nullable=false)
	public int activePower; // 行动力
	
	@Column(name="formulalist")
	@Type (type="peony.game.FormulaListUserType")
	public FormulaList formulaList;
	
	@Transient
	public int lastRestoreActivePowerTime;
	
	@Transient
	public WarState warState;
	
	//在pvp状态下，在特殊地区被杀的次数，如果次数大于2次，那么将会变成pve状态15分钟
	@Transient
	public int pvpKilledTimes;
	
	@Type(type = "peony.depot.DepotTransactionBagUseType")
	@Column(name = "depot")
//	@Transient
	public TransactionBag depot;
	
	@Column(name = "lastlogout", nullable = true)
	public Date lastLogoutTime;
	
	@Transient
	public int onlineExpTime = 0; 
	
	@Transient
	public AutoNaturalEnhance autoNaturalEnhance;
	
	@Transient
	public AutoAddHole autoAddHole;
	
	@Transient
	protected ConcurrentHashMap<Integer,Action> actions = new ConcurrentHashMap<Integer,Action>(10);
	
	@Transient
	public PacketChangedItemVisitor pv = new PacketChangedItemVisitor();
	
	@Column(name = "attendants")
	@Type(type = "peony.game.attendant.AttendantUserType")
	public AttendantBag attendantBag;
	
	// 像creature一样，player也模拟行走
	@Transient
    public int nextX,nextY,startX,startY,lastX,lastY;
	@Transient
    private int nextDistance;			// 到下一点的距离
	@Transient
    private int needRunTime;			// 到下一点的预计时间
	@Transient
    public int runToNextPointTime; //从一点开始到另外一点的时间
	@Transient
    private int cycle;
	@Transient
	private List<Packet> scheduledPacket = new ArrayList<Packet>();
	@Transient
	private List<ChatMessage> scheduledChat = new ArrayList<ChatMessage>();
	@Transient
	public float tirePercent = 1.0f;	// 防沉迷系统下的收益比例
	@Transient
	public boolean tireChecked = false;
	@Transient
	private int errorCount = 0;
	@Transient
	public PlayerRelation relations;   // 为提高效率在这里保存一份冗
	@Transient
	public long enterMarrigTime;
	@Transient
	public byte isFetch = 0;
	@Transient
	public boolean isKicked = false;
	@Transient
	public Report report = new Report();
	@Transient
	public AssociationInvite associationInvite;
	@Transient
	public static int[] STAR_BUFF = {0, 390, 391, 392}; //星辉BUFF
	@Transient
	public static Buff[] STAR_BUFFS = new Buff[4];
	@Transient
	public static int STAR_7_BUFF_ID = 419; //全七BUFF
	@Transient
	public static Buff STAR_7_BUFF = BuffUtil.createSuiteBuff(STAR_7_BUFF_ID, 1);
	@Transient
	public AntiPlug antiPlug = new AntiPlug(this);
	@Transient
	public static int ANTIPLUG_MODEL_LOG = 0; //外挂处理方式--日志
	@Transient
	public static int ANTIPLUG_MODEL_NONBENEFIT = 1; //外挂处理方式--无收益
	@Transient
	public static int antiPlugModel = ANTIPLUG_MODEL_LOG; //外挂处理方式
	@Transient
	public static int maxSkillOffsetTime = 100000; //攻击补偿上限
	@Transient
	public static int maxBotScore = 10; 
	@Transient
	public Attendant attendant; //当前跟随
	@Transient
	public byte isFindPath = 0; //是否正在寻路(0是非寻路状态、1寻路状态)
	@Transient
	public long lastChatTime = 0;//最后一次发言的时间
	@Transient
	public Map<Integer, Integer> lastRemindFriendTime = new HashMap<Integer, Integer>();//最后一次提醒好友上线的时间
	
	private static final int VIOLATION_SCORE = 20;
	private static final int FATAL_ERROR_SCORE = 100;
	private static final int MAX_RUN_DIST = 500 * 500;

	public static final String PROPERTY_REFRESH_SKILL = "refreshskill";
	public static final String PROPERTY_GATHER_ABILITY = "gatherability";
	public static final String PROPERTY_PRODUCE_ABILITY = "produceability";
	public static final String PROPERTY_LAST_FATION_CHAT = "lastfactionchat";
	public static final String PROPERTY_TODAY_FACTION_CHAT_COUNT = "todayfactionchatcount";
	public static final String PROPERTY_RESTORE_ACTIVEPOWER_DAY = "restoreapday"; 
	public static final String PROPERTY_INSTANCE_DAY = "instanceday";
	public static final String PROPERTY_INSTANCE_TIMES = "instancetimes";
	public static final String PROPERTY_NATIONCOLLECT_MONEY = "nationcollect";
	public static final String PROPERTY_GETNATIONSKILL_ITEM_DAY = "getnationskillitemday";
	public static final String PROPERTY_FLAGBATTLE_SIGNUPMAP = "battlesignupmap";
	public static final String PROPERTY_CLICKEXP_START_TIME = "clickexps";
	public static final String PROPERTY_CLICKEXP_CUMULATE_TIME = "clickexpc";
	public static final String PROPERTY_CLICKEXP_DAY = "clickexpday";
	public static final String PROPERTY_CLICKEXP_TIMES = "clickexptimes";
	public static final String PROPERYY_CLICKEXPSUC_TIME = "lastclickexptime";
	public static final String PROPERTY_TONGBATTLE_EXPDAY = "tongbattleexoday";
	public static final String PROPERTY_TONGBATTLE_SIGNMAPID = "tongbattlesignmapid";
	public static final String PROPERTY_LEAVETONG_TIME = "leavetongtime"; //离开军团的时间
	public static final String PROPERTY_COPYEQUIP_COUNT = "copyequip"; // 杀人掉装备的数量
	public static final String PROPERTY_COPYEQUIP_DAY = "copyequipday"; // 杀人掉装备的时间
	public static final String PROPERTY_KILLED_COPYEQUIP_COUNT = "killedcopyequip";
	public static final String PROPERTY_KILLED_COPYEQUIP_DAY = "killedcopyequipday";
	public static final String PROPERTY_CLICKMONEY_START_TIME = "clickmoneys";
	public static final String PROPERTY_CLICKMONEY_CUMULATE_TIME = "clickmoneyc";
	public static final String PROPERTY_CLICKMONEY_DAY = "clickmoneyday";
	public static final String PROPERTY_CLICKMONEY_TIMES = "clickmoneytimes";
	public static final String PROPERTY_LAST_HORSE_INSTANCEID = "LAST_HORSE_INSTANCEID";
	public static final String PROPERTY_LAST_ATTENDANT_INSTANCEID = "LAST_ATTENDANT_INSTANCEID";
	public static final String PROPERTY_LAST_REMOVEFROMASSOCIATION_TIME = "LAST_REMFASS_TIME";
	public static final String PROPERTY_GET_LIBAO_TIMES = "PROPERTY_GET_LIBAO_TIMES";//领取过xx礼包的次数
	public static final String PROPERTY_FAME_GETITEM_DAY = "PROPERTY_FAME_GETITEM_DAY"; //演武场领取物品时间
	
	/**
	 * 取得已经分配的属性点数量。
	 * @return
	 */
	public int getAddedPropertyPoint() {
		return strengthAdded + agilityAdded + staminaAdded + intellectAdded;
	}
	
	/**
	 * 取得已分配的技能点数量。
	 * @return
	 */
	public int getAddedSkillPoint() {
		return PlayerUtil.SKILL_POINT[level] - skillPoint;
	}
	
	public String getGuildName() {
		return guildName == null ? "" : guildName;
	}
	
	public int hasGuild() {
		return guildName != null ? 1 : 0;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
		addStringPropertyChangedItem(ChangedItem.GUILD, getGuildName(), false);
		moveExtended |= MOVEEXT_GUILD;
	}

	public int getKillCreatureCount(int templateId) {
		return kills.get(templateId);
	}

	public int getKillCreatureCount(int templateId, int mateId) {
		return kills.get(templateId, mateId);
	}
	
	public void addKillCreatureCount(int templateId, List<Player> owners) {
		kills.add(templateId, owners);
	}

	public void addKillPlayer(int playerID) {
		killPlayers.add(playerID);
	}

	public void setSystemState(int systemState) {
		this.systemState = systemState;
		this.changeStateStamp = System.currentTimeMillis();
	}

	public Player() {
		super(GameObject.TYPE_PLAYER);
		this.changed = new Changed();
		this.chatOptions = ChatOptions.newDefaultChatOptions();
		this.moneyTx = new MoneyIntProperty();
		this.expTx = new ExpIntProperty();
		this.creditTx = new CreditIntProperty();
		this.honorTx = new HonorIntProperty();
		this.exist = 1;
		this.autoAttack = new AutoAttack();
		this.titles = new Titles(this);
		this.horseBag = new HorseBag(this);
		this.attendantBag = new AttendantBag(this);
		this.pool = new PropertyPool();
		this.formulaList = new FormulaList();
		this.lastRestoreActivePowerTime = Time.currTime;
	}

	public Player(int accountId, String name, int sex, int clazz) {
		super(GameObject.TYPE_PLAYER);
		this.accountId = accountId;
		this.setName(name);
		this.sex = sex;
		this.clazz = clazz;
		this.asmVm = new ASMGameVM(this);
		this.bag = new TransactionBag(this, 27 + level / 5,0);
		this.depot = new TransactionBag(this, 0, 0); // 初始化仓库
		this.changed = new Changed(); // 只有player才需要changed
		this.moneyTx = new MoneyIntProperty();
		this.expTx = new ExpIntProperty();
		this.creditTx = new CreditIntProperty();
		this.honorTx = new HonorIntProperty();
		this.exist = 1;
		Arrays.fill(attack_timer, -2);
		this.autoAttack = new AutoAttack();
		this.titles = new Titles(this);
		this.horseBag = new HorseBag(this);
		this.attendantBag = new AttendantBag(this);
		this.pool = new PropertyPool();
		this.formulaList = new FormulaList();
		this.lastRestoreActivePowerTime = Time.currTime;
	}

	public void setSession(ClientSession session) {
		this.session = session;
	}
	
	public Account getAccount() {
		if (session == null) {
			return null;
		}
		return (Account)session.getIdentity();
	}

	public void cancelAutoAttack() {
		autoAttack.clear();
	}

	public void cancelAttack() {
		if (attack != null) {
			attack = null;
		}
	}

	public void cancelUseItem() {
		if (itemUse != null) {
			itemUse = null;
		}
	}
	
	/**
	 * 记录外挂检测分数。
	 * @param value 新增分值
	 */
	public void addForbidScore(int value) {
	    // 每记满20分认为违规1次，然后停半分钟不记分；如果连续3次违规之间的间隔都少于5分钟，则
	    // 踢下线10分钟。
		this.forbidScore += value;
		if (this.forbidScore >= FATAL_ERROR_SCORE) {
		    // 如果发生了严重错误，直接踢下线15分钟
		    log.info("[FORBID][FATALERROR]" + LogUtil.getPlayerLogString(this) + "SCORE[" + forbidScore + "]");
            forbidScore = 0;
		    if (!cheat) {
                Server.server.getServiceRegistry().getPlayerService().mute(id, System.currentTimeMillis()+15*60*1000);
            }
		    return;
		}
		if (Time.currTime < violationTime2 + 30000L) {
		    // 上次普通违规半分钟内不记分
		    this.forbidScore = 0;
		}
		if (this.forbidScore >= VIOLATION_SCORE) {
            forbidScore = 0;
		    if (violationTime2 - violationTime1 < 300000L && Time.currTime - violationTime2 < 300000L) {
		        log.info("[FORBID][3ERRORS]" + LogUtil.getPlayerLogString(this));
    			if (!cheat) {
    			    Server.server.getServiceRegistry().getPlayerService().mute(id, System.currentTimeMillis()+15*60*1000);
    			}
		    }
		    violationTime1 = violationTime2;
		    violationTime2 = Time.currTime;
		}
	}

	public void setFaction(int faction, boolean notify, int serial) {
		int oldFaction = this.faction;
		if (oldFaction != faction) {
			super.setFaction(faction, notify);
			Server.server.getEventManager().fireEvent(
					new ServiceEvent(ServiceEvent.EVENT_PLAYER_CHANGE_FACTION,
							this, oldFaction));
		}
		Packet pt = new Packet(OpCode.CHANGE_FACTION_SERVER);
		pt.putInt(serial);
		pt.putInt(faction);
		send(pt);
	}

	public void setSex(int sex, boolean notify, int serial) {
		super.setSex(sex, notify);
		Packet pt = new Packet(OpCode.CHANGE_SEX_SERVER);
		pt.putInt(serial);
		pt.putInt(sex);
		send(pt);
	}

	public void setClazz(int clazz, boolean notify, int serial) {
		int oldClazz = this.clazz;
		super.setClazz(clazz, notify);
		if (oldClazz != clazz) {
			Gain gain = new Gain(this);
//			GameItem equ = ObjectAccessor
//					.createGameItem(PlayerUtil.INIT_EQUIPMENT[clazz]);
//			gain.addGainItem(equ, 1);
			PlayerTransaction tx = newTransaction("CCL");
			boolean ok = bag.addGain(gain, tx, false);
			tx.commit();
			if (ok) {
//				equip(equ.template.id, equ.instanceId, -1);
				GameItem item = bag
						.getGameItem(PlayerUtil.INIT_EQUIPMENT[oldClazz]);
				if (item != null) {
					tx = newTransaction("CCL");
					bag.removeGameItem(item.template.id, item.instanceId, 1,
							tx, true);
					tx.commit();
				}
			}
			skills.clear();
			List<Skill> initSkills = ObjectAccessor.getPlayerInitSkills(clazz);
			for (Skill skill : initSkills) {
				skills.addSkill(skill, null, false);
			}
			sendSkillList();
			refreshPropertiesWhenClazzChanged(false);
		}
		Packet pt = new Packet(OpCode.CHANGE_CLASS_SERVER);
		pt.putInt(serial);
		pt.putInt(this.clazz);
		send(pt);
	}
	
	public void refreshPropertiesWhenClazzChanged(boolean levelUp){
		PropertyCalculator calc = new PropertyCalculator(this);
		processTitleBuff(calc);
		equipments.enhance(calc);
		if(horse!=null)
			horse.enhance(calc);
		buffs.enhance(calc);
		setHeadScore(equipments.getHeadScore(level,clazz));
		setBodyScore(equipments.getBodyScore(level,clazz));
		setWeaponScore(equipments.getWeaponScore(level,clazz));
		setFlashLevel(equipments.getFlashLevel());
		calc.caculate();
		setStrength(calc.strength, false);
		setAgility(calc.agility,false);
		setStamina(calc.stamina,false);
		setIntellect(calc.intellect,false);
		setMaxhp(calc.hp,false);
		setMaxmp(calc.mp,false);
		if(levelUp){
			setHp(calc.hp,false);
			setMp(calc.mp,false);
		}else{
			if(hp != calc.hp){
				setHp(calc.hp,false);
			}
			if(mp != calc.mp){
				setMp(calc.mp,false);
			}
		}
		setAttackpowerup(calc.attackpowerup);
		setAttackpowerdown(calc.attackpowerdown);
		setSpellpower(calc.spellpower);
		setSpellheal(calc.spellheal);
		setDefense(calc.defense);
		setSpelldefense(calc.spelldefense);
		setCritical(calc.critical);
		setSpellCritical(calc.spellcritical);
		setHit(calc.hit);
		setSpellhit(calc.spellhit);
		setDodge(calc.dodge);
		setSpelldodge(calc.spelldodge);
		setAnticrit(calc.anticrit);
		setHealthrestore(calc.healthrestore);
		setManarestore(calc.manarestore);
		setDefensePercent(calc.defensePercent);
		setSpeedRatio(calc.getSpeed());
		expRatio = calc.expRatio;
		rewardRation = calc.rewardRation;
		horseExpRatio = calc.horseExpRatio;
		moneyRatio = calc.moneyRatio;
	}

	public void setMoney(int money, boolean notify, String cause) {
		if (money >= 0 && this.money != money) {
			int oldMoney = this.money;
			this.money = money;
			addIntPropertyChangedItem(ChangedItem.MONEY, oldMoney, this.money,
					notify);
			if (money > oldMoney) {
				LogUtil.logGetMoney(this, oldMoney, money, cause);
			} else {
				LogUtil.logRemoveMoney(this, oldMoney, money, cause);
			}
		}
	}

	public int getMoney() {
		return this.money;
	}

	public void setHonor(int honor, boolean notify, String cause) {
		if (honor >= 0 && this.honor != honor) {
			int oldHonor = this.honor;
			this.honor = honor;
			addIntPropertyChangedItem(ChangedItem.HONOR, oldHonor, this.honor,
					notify);
			if (honor > oldHonor) {
				LogUtil.logGetHonor(this, oldHonor, honor, cause);
			} else {
				LogUtil.logRemoveHonor(this, oldHonor, honor, cause);
			}
		}

	}

	@Override
	public void setLevel(int level, boolean notify) {
		super.setLevel(level, notify);
		List<Skill> autoLearns = skills.getAutoLearSkills(level); // 找到所有需要自动学习的Skill，依次学习
		for (Skill skill : autoLearns) {
			addSkill(skill.getNextLevel());
		}

	}

	// public void clientMove(int x,int y , byte direct, short state ,int time){
	// Position p = new Position(map.id,x,y,Time.currTime,time);
	// if(lastPosition!=null){
	// comparePosition(lastPosition,p);
	// }
	// lastPosition = p;
	// if(lastCalcPosition==null){
	// lastCalcPosition = p;
	// }
	// moves.add(new ClientMove(x,y,direct,state,time));
	// }

	/*
	 * 检查用户新到达的一个点是否合法的点，如果不合法，则视为作弊。
	 */
	protected void checkPosition(Position p1) {
//		if (lastPosition != null && lastPosition.mapId == p1.mapId && lastPosition.x == p1.x &&
//				lastPosition.y == p1.y) {
//			return;
//		}
//		if (map.map != null && !map.map.mapDef.mapInfo.getPathFinder().canReach(p1.x, p1.y)) {
//			log.info("[REACHERROR]" +
//                    LogUtil.getPlayerLogString(this) + 
//                    "]DEST[" + p1.mapId + "," + p1.x + "," + p1.y + "]");
//		}
	}
	
	protected void comparePosition(Position p1, Position p2) {
		if (p1.clientTime != -1 && p2.clientTime != -1) {
		    if (p2.mapId == p1.mapId) {
		        // 计算两次移动间按正常速度估算的时间，与实际两次pos包之间的时间比较
		        int distY = Math.abs(p2.y - p1.y);
                int distX = Math.abs(p2.x - p1.x);
                int d1 = distY * distY + distX * distX;
                int speed = Math.max(p1.speed, p2.speed);
                double estTime = d1 / (double)(speed * speed);
                double realTime = (p2.clientTime - p1.clientTime) / 1000.0;
                
                // 允许时间误差0.2秒，计算出客户端的加速率
                double speedRate = estTime / (realTime * realTime + realTime * 0.4 + 0.04);
                
                /*if (estTime - realTime * realTime > 100 + 20 * realTime) {
                    // 如果客户端获益超过10秒，扣100分
                    log.info("[POSITIONERROR][10]" +
                            LogUtil.getPlayerLogString(this) + 
                            "]SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
                            "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
                            "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
                            "]");
                    addForbidScore(20);
                } else if (estTime - realTime * realTime > 9 + 6 * realTime) {
                    // 如果客户端获益超过3秒，则扣10分
                    log.info("[POSITIONERROR][3]" +
                            LogUtil.getPlayerLogString(this) + 
                            "]SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
                            "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
                            "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
                            "]");
                    addForbidScore(10);
                } else if (estTime - realTime * realTime > 4 + 4 * realTime) {
                    // 如果客户端获益超过2秒，则扣5分
                    log.info("[POSITIONERROR][2]" +
                            LogUtil.getPlayerLogString(this) + 
                            "]SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
                            "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
                            "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
                            "]");
                    addForbidScore(5);
                } else if (estTime - realTime * realTime > 1 + 2 * realTime) {
                    // 如果客户端获益超过1秒，则扣1分
                    log.info("[POSITIONERROR][1]" +
                            LogUtil.getPlayerLogString(this) + 
                            "]SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
                            "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
                            "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
                            "]");
                    addForbidScore(1);
                }*/
                
//                System.out.println(speedRate);
                if (speedRate > 25) {
	                // 如果客户端加速5倍，扣20分
	                log.info("[POSITIONERROR][5]" +
	                        LogUtil.getPlayerLogString(this) + 
	                        "]SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
	                        "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
	                        "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
	                        "]");
	                addForbidScore(20);
	            } else if (speedRate > 9) {
	                // 如果客户端加速3倍，扣10分
	                log.info("[POSITIONERROR][3]" +
	                        LogUtil.getPlayerLogString(this) + 
	                        "]SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
	                        "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
	                        "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
	                        "]");
	                addForbidScore(10);
	            } else if (speedRate > 4) {
	                // 如果客户端加速2倍，扣5分
	                log.info("[POSITIONERROR][2]" +
	                        LogUtil.getPlayerLogString(this) + 
	                        "]SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
	                        "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
	                        "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
	                        "]");
	                addForbidScore(5);
	            } else if (speedRate > 1.69) {
	                // 如果客户端加速30%，扣1分
	                log.info("[POSITIONERROR][1]" +
	                        LogUtil.getPlayerLogString(this) + 
	                        "]SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
	                        "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
	                        "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
	                        "]");
	                addForbidScore(2);
	            }
		    }
//			if (p2.clientTime <= p1.clientTime) {
//				log.info("[POSITIONERROR][TIME]"
//						+ LogUtil.getPlayerLogString(this) + "NEWTIME["
//						+ p2.clientTime + "]OLDTIME[" + p1.clientTime + "]");
//				// setForbidScore(forbidScore + 5);
//			} else {
//				if (p2.mapId == p1.mapId && (p2.x != p1.x || p2.y != p1.y)) {
//					int distY = Math.abs(p2.y - p1.y);
//					int distX = Math.abs(p2.x - p1.x);
//					int d1 = distY * distY + distX * distX;
//					if (d1 < 256) {
//						return;
//					}
//					int d2 = getSpeed() * (p2.clientTime - p1.clientTime)
//							/ 1000;
//					d2 *= d2;
//					if (d1 > d2 * 300 / 100) {
//						log.info("[POSITIONERROR][SPEED]"
//								+ LogUtil.getPlayerLogString(this)
//								+ "REALDIST[" + d1 + "]DIST[" + d2 + "]SRC[" +
//								p1.mapId + "," + p1.x + "," + p1.y + "]DEST[" + 
//								p2.mapId + "," + p2.x + "," + p2.y + "]");
////						setForbidScore(forbidScore + 5);
//					}
//				}
//			}
		}
	}

	// public void move(int x, int y, byte direct, short state, int time) {
	// // if (ignoreMoveTime != 0 && ignoreMoveTime > Time.currTime) {
	// // return;
	// // }
	// super.move(x, y);
	// this.direct = direct;
	// this.state = (short) (((this.state & 0xFFFE) | (state & 0x1)) & 0xFFFF);
	// this.lastMoveTime = time;
	// if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
	// Server.server.eventManager.fireEvent(new ServiceEvent(
	// ServiceEvent.EVENT_PLAYER_PK_MOVE, this, x, y));
	// }
	// moveType |= MOVE_RUNNING_STATE;
	// }
	
	/**
	 * 当Player第一次被数据库载入的时候用来调整skills,因为skills字段中记录了所有学过的技能,其中包括0级的每职业的所有技能
	 * 一旦技能做出调整,比如说技能增加的时候,就需要往其中加入新的技能,因为Hibernate载入字段的顺序很可能是不固定的,所有不能
	 * 在skills被载入的时候做,当时的clazz字段未必能取到
	 */
	public void skillsPatch(){
		List<Skill> ss = ObjectAccessor.getPlayerInitSkills(clazz);
		for(Skill s:ss){
			if(skills.getSkillByGroupId(s.getGroupId())==null){
				skills.addSkillSlient(s);
			}
		}
		Iterator<Skill> ite = skills.skills.values().iterator();
		while(ite.hasNext()){
			Skill skill = ite.next();
			if(skill.getGroupId()==175){ //由于175号技能被策划弄错，导致有些角色也学习了这个NPC的技能，所以需要特别判断
				ite.remove();
				if(skill.getLevel() != 0){
					skillPoint += skill.getLevel();
				}
			}
		}

	}

	public int getNextPointX(){
		if (this.nextDistance == 0) {
			return -1;
		}
		return nextX;
	}
	
	public int getNextPointY(){
		if (this.nextDistance == 0) {
			return -1;
		}
		return nextY;
	}
	
	public void setNextPoint(int x,int y){
		if (x == -1 && y == -1) {
			this.nextDistance = 0;
			return;
		}
		if(nextX!=x||nextY!=y){
			this.startX = this.x;
			this.startY = this.y;
			this.nextX = x;
			this.nextY = y;
			this.nextDistance = (int)Math.sqrt((this.nextX - this.startX) * (this.nextX - this.startX) + 
					(this.nextY - this.startY) * (this.nextY - this.startY));
			this.runToNextPointTime = Time.currTime;
			if (getSpeed() == 0) {
				this.needRunTime = runToNextPointTime;
			} else {
				this.needRunTime = this.runToNextPointTime + (this.nextDistance * 1000) / getSpeed();
			}
		}
	}
	
	public void runToNextPoint() {
		if (this.nextDistance == 0) {
			return;
		}
		if (Time.currTime < runToNextPointTime || Time.currTime >= needRunTime) {
			// 时间错误或时间超出，都直接跳转到目标位置
			move(nextX, nextY);
			this.nextDistance = 0;
		} else {
			int distance = getSpeed() * (Time.currTime - runToNextPointTime) / 1000;
			int dx = distance * (this.nextX - this.startX) / this.nextDistance;
			int dy = distance * (this.nextY - this.startY) / this.nextDistance;
			move(startX + dx, startY + dy);
		}
		lastMoveTime = Time.currTime;
	}
	
	public void move(int x, int y, byte direct, short state, int time, int diff, int nextx, int nexty) {
		// log.info("["+id+","+x+","+y+"]");
		Position currentPosition = new Position(map.id, x, y, Time.currTime,
				time, getSpeed());
		checkPosition(currentPosition);
		if (lastPosition != null) {
			comparePosition(lastPosition, currentPosition);
			if ((Time.currTime - lastPosition.time) > 10000 && diff <= 5000) { // 10秒
				try {
					goMap(lastPosition.mapId, lastPosition.x, lastPosition.y);
					unMoving();
				} catch (VMapException e) {
					//不应该被执行到
					log.error(e,e);
				}
				// acceptMoving = false;
			} else {
				super.move(x, y);
				this.direct = direct;
				this.state = (short) (((this.state & 0xFFFE) | (state & 0x1)) & 0xFFFF);
				this.lastMoveTime = time;
				if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
					Server.server.eventManager.fireEvent(new ServiceEvent(
							ServiceEvent.EVENT_PLAYER_PK_MOVE, this, x, y));
				}
				moveType |= MOVE_RUNNING_STATE;
				lastPosition = currentPosition;
				moved();
				setNextPoint(nextx, nexty);
			}
		} else {
			super.move(x, y);
			this.direct = direct;
			this.state = (short) (((this.state & 0xFFFE) | (state & 0x1)) & 0xFFFF);
			this.lastMoveTime = time;
			if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
				Server.server.eventManager.fireEvent(new ServiceEvent(
						ServiceEvent.EVENT_PLAYER_PK_MOVE, this, x, y));
			}
			moveType |= MOVE_RUNNING_STATE;
			lastPosition = new Position(map.id, x, y, Time.currTime, time, getSpeed());
			moved();
			setNextPoint(nextx, nexty);
		}
	}

	protected void moved() {
		MoveCallback moveCallback = moveCallback();
		if (moveCallback != null) {
			moveCallback.moved(this);
		}
	}

	public void ride() {
		state |= STATE_RIDE; // 设置骑马状态
		addIntPropertyChangedItem(ChangedItem.RIDE, getHorseInt(), false, true);
		refreshProperties(false);
		moveType |= MOVE_POINT_STATE | MOVE_DETAIL | MOVE_HORSE;
		
		// 记录玩家动作
		addAction(Action.RIDE);
	}

	public void horseFeed(int gridId, int itemId, int instanceId,
			int horseInstanceId, int serial) {
		if(itemId!=ItemUtil.ITEM_HORSEFOOD&&itemId!=ItemUtil.ITEM_HORSEFOOD_ADDBUFF){
			return;
		}
		Horse h = horseBag.getHorse(horseInstanceId);
		if (h != null) {
			if (h.degree == 100) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_FEED_CLIENT, "饱食度已满");
				return;
			}
			PlayerTransaction tx = newTransaction("FED");
			TransactionBagGrid grid = bag.removeGridGameItem(gridId, itemId,
					instanceId, 1, tx, true);
			if (grid != null) {
				h.feed(grid.item, this, serial);
				tx.commit();
				
				// 记录玩家动作
				addAction(Action.FEED_HORSE);
			} else {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_FEED_CLIENT, "没有指定物品");
			}
		}
	}

	public void horseUnride(int serial) {
		if (horse != null) {
			Horse h = horse;
			horse = null;
			h.unRide(this);
			unRide();
			Packet pt = new Packet(OpCode.HORSE_UNRIDE_SERVER);
			pt.putInt(serial);
			send(pt);
			updateLastOnHorse();
		}
	}

	public void prepareHorseRide(int horseInstanceId, int serial) {
		Horse h = horseBag.getHorse(horseInstanceId);
		if (h == horse) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.HORSE_RIDE_CLIENT, "你已经在此坐骑上");
			return;
		}
		ride = new HorseRide(h.instanceId, Time.currTime
				+ h.template.summonTime, serial);
	}

	/**
	 * 上骑
	 * @param horseInstanceId
	 * @param serial
	 * @param change 是否换装 0 换装，-1 不换
	 */
	public void horseRide(int horseInstanceId, int serial,int change) {
		Horse h = horseBag.getHorse(horseInstanceId);
		Horse oldHorse = horse;
		if (h != null) {
			if (h != horse) {
				if(!h.isActive()){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_RIDE_CLIENT, "您的坐骑需要激活");
					return;
				}
				if(CandidateService.isKingHorse(h.itemId)){
					Nation nation = Server.server.getServiceRegistry().getNationService()
					.getNationByFaction(faction);
					if(nation.getKingId()!=id){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.HORSE_RIDE_CLIENT, "您不是国公不能上骑此坐骑");
						return;
					}
				}
				if (h.degree <= 0) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_RIDE_CLIENT, "坐骑的饱食度为零");
					return;
				}
				if (horse != null) {
					horse.unRide(this);
					horse = null;
					unRide();
				}
				horse = h;
				h.ride(this);
				ride();	
				try {
					horseExchangeEquip(oldHorse,horse,change);
				} catch (Exception e) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_RIDE_CLIENT, e.getMessage());
				}
				if(oldHorse!=null && oldHorse.equs!=null && oldHorse.equs.equs!=null)
					removeSuiteEquipmentBuffs(oldHorse.equs.equs);
				addHorseSuiteEquipmentBuffs();
				Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_HORSE_RIDE, this));
				updateLastOnHorse();
				refreshHorseAndPlayerProperty();
			}
			Packet pt = new Packet(OpCode.HORSE_RIDE_SERVER);
			pt.putInt(serial);
			pt.putInt(horseInstanceId);
			send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.HORSE_RIDE_CLIENT, "没找到指定坐骑");
		}
	}
	
	/**
	 * 坐骑装备换装
	 * @param oldHorse 原来的坐骑
	 * @param horse 目标坐骑
	 * @param change 是否换装 0 换装，-1不换
	 * @throws Exception
	 */
	protected void horseExchangeEquip(Horse oldHorse,Horse horse,int change) throws Exception{
		if(oldHorse != null && horse != null){
			if(change == 0){
				List<GameItem> equipments = new ArrayList<GameItem>();
				for(GameItem item : oldHorse.equs.equs){
					if(item != null){
						equipments.add(item);
						if(item.template.useLevel > horse.level){
							change = -1;
							break;
						}
					}
				}
				if(change == 0){
					for(GameItem item : equipments){
						GameItem gameItem = oldHorse.equs.unequip(item.template.id, item.instanceId, this);
						horse.equs.equip(gameItem, this);
					}
					oldHorse.refreshProperties(false, this);
					horse.refreshProperties(false, this);
					message(-1, "装备交换成功", -1, -1);
				} else {
					throw new Exception("您要换装的坐骑等级太低，无法穿上装备，快去升级吧！");
				}
			}
		}
	}
	
	public void refreshHorseAndPlayerProperty(){
		if(horse!=null)
			horse.refreshProperties(false, this);
		refreshProperties(false);
	}
	
	private void updateLastOnHorse(){
		if(horse != null){
			pool.setInt(PROPERTY_LAST_HORSE_INSTANCEID, horse.instanceId);
		} else {
			pool.setInt(PROPERTY_LAST_HORSE_INSTANCEID, 0);
		}
	}

	protected void processRide() {
		if (ride != null) {
			if (ride.time < Time.currTime) {
				horseRide(ride.instanceId, ride.serial,-1);
				ride = null;
			}
		}
		if (horse != null) {
			horse.update(this);
		}
	}

	/**
	 * 修理装备
	 * 
	 * @param serial
	 * @param type
	 *            0 - 身上装备，1 - 背包装备，2 - 所有装备
	 */
	public void repair(int serial, int type) {
		int totalMoney = 0;
		List<GameItem> bagItems = new ArrayList<GameItem>();
		List<Horse> horses = new ArrayList<Horse>();
		if (type == 0 || type == 2) {
			for (int i = 0; i < equipments.equs.length; i++) {
				GameItem item = equipments.equs[i];
				if (item != null) {
					totalMoney += item.getRepairMoney();
				}
			}
		}
		if (type == 1 || type == 2) {
			for (TransactionBagGrid grid : bag.grids) {
				if (grid.item == null || grid.item.template.equipment == null) {
					continue;
				}
				int m = grid.item.getRepairMoney();
				if (m > 0) {
					totalMoney += m;
					bagItems.add(grid.item);
				}
			}
		}
		if(type == 0){
			if(horse!=null){
				totalMoney += horse.getRepairMoney();
				horses.add(horse);
			}
		}
		if(type == 2){
			for(Horse h:horseBag.horses){
				totalMoney += h.getRepairMoney();
				horses.add(h);
			}
		}
		if (totalMoney == 0) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.REPAIR_CLIENT, "没有需要修理的装备");
			return;
		}

		PlayerTransaction tx = newTransaction("REP");
		try {
			decMoney(totalMoney, tx, true);
			tx.commit();
			equipments.repair();
			for (GameItem bitem : bagItems) {
				bitem.repair(this);
			}
			for (Horse h: horses){
				h.repair(this);
			}
			Packet pt = new Packet(OpCode.REPAIR_SERVER);
			pt.putInt(serial);
			pt.putInt(totalMoney);
			send(pt);
		} catch (NoEnoughValueException e) {
			tx.rollback();
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.REPAIR_CLIENT, "没有足够的金钱");
		}
	}

	// public boolean prepareRide(int time){
	// if(ride!=null)
	// return false;
	// if(isRide()){
	// return false;
	// }
	// ride = new Ride(time);
	// return true;
	// }

	public void unRide() {
		state &= 0xFFFB; // 设置成下马状态
		addIntPropertyChangedItem(ChangedItem.RIDE, getHorseInt(), false, true);
//		refreshProperties(false);
		moveType |= MOVE_POINT_STATE | MOVE_DETAIL | MOVE_HORSE;
	}

	protected void rollbackNotCommiteds() {
		txLock.lock();
		try {
			if (transactions.size() > 0) {
				log.info("[ROLLBACK]" + LogUtil.getPlayerLogString(this) + "SIZE[" + transactions.size() + "]");
				List<PlayerTransaction> l = new ArrayList<PlayerTransaction>(
						transactions);
				for (PlayerTransaction tx : l) {
					release(tx, false);
					log.info(tx);
				}
				log.info("[ROLLBACK]" + LogUtil.getPlayerLogString(this) + "OK");
			}
		} finally {
			txLock.unlock();
		}
	}

	public PlayerTransaction newTransaction(String cause) {
		txLock.lock();
		try {
			PlayerTransaction tx = new PlayerTransaction(this, cause);
			transactions.add(tx);
			return tx;
		} finally {
			txLock.unlock();
		}
	}

	void release(PlayerTransaction tx, boolean commit)
			throws TransactionException {
		txLock.lock();
		try {
			if (transactions.contains(tx)) {
				transactions.remove(tx);
				if (commit)
					tx.internalCommit();
				else
					tx.internalRollback();
			} else
				throw new TransactionException();
		} finally {
			txLock.unlock();
		}
	}

	@Override
	public void setAttack(Attack attack) {
		super.setAttack(attack);
		if (attack != null) {
			if (autoAttack != null && autoAttack.fired) {
				autoAttack.time = Time.currTime + attack.skill.getActTime(this)
						+ AUTO_ATTACK_INTERVAL;
				autoAttack.attack = null;
			}
		}
		// if (attack != null && autoAttack == null
		// && (attack.skill.getType() & Skill.TYPE_ATTACK) != 0
		// && attack.targetRef != null) { // 如果是攻击技能
		// autoAttack = new Attack(skills.getSkill(1), this, ObjectAccessor
		// .getGameObject(attack.targetRef), true);
		// }
	}

	public int prepareSkillAttack(int instanceId, int skillId, int offsetTime) {
//		offsetTime = Math.min(offsetTime, maxSkillOffsetTime);
		checkPlug(offsetTime);
		int retCode = 0;
		if (attack == null) {
			Skill skill = skills.getSkill(skillId);
			if (skill != null) {
				if (skill.getLevel() == 0) {
					retCode = 5;
				} else {
					if (!coolDowns.atCoolDown(skill.getCDGroup())) { // 检查技能CD时间
						GameObject target = ObjectAccessor
								.getGameObject(instanceId);
						if(target==null)
							target = ObjectAccessor.getPlayer(instanceId);
						if (!(target instanceof Unit)) {
						    //TODO delete
						    if(target == null && ObjectAccessor.players.containsKey(instanceId)){
						        log.info("[OBJECTPLAYERERROR]"+LogUtil.getPlayerLogString(this)+"INSTANCEID["+instanceId+"]");
						    }
						    //TODO delete end
							retCode = 8;
						} else {
							Unit unit = (Unit) target;
							if (unit != this) {
								VMap map = getVMap();
								if (!map.mapDef.mapInfo.canSee(x, y, unit.x,
										unit.y)) {
									retCode = 14;
								} else {
									retCode = super.prepareSkillAttack(unit,
											skill, offsetTime);
								}
							} else {
								retCode = super.prepareSkillAttack(unit, skill,
										offsetTime);
							}

						}
					} else {
						retCode = 10;
					}
				}
			} else {
				retCode = 5;
			}
		} else {
			retCode = 2;
		}
		if (retCode != 0) {
			Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
			pt.put((byte) retCode);
			pt.putInt(this.instanceId);
			pt.putInt(instanceId);
			pt.putInt(skillId);
			send(pt);
		}else{
			coolDowns.setCommonCD(500);
		}
		return retCode;
	}
	
	private void checkPlug(int offsetTime){
		try {
			if(antiPlug!=null && !antiPlug.isBot && offsetTime>maxSkillOffsetTime){
				antiPlug.score1 += 1;
				if(antiPlug.score1>=maxBotScore){
					LogUtil.logAntiPlug(this, "SKILLOFFSET");
					antiPlug.isBot = true;
				}
			}
		} catch (Exception e) {
		}
	}

	@Override
	public int canAttack(GameObject unit) {
		if (unit.type == GameObject.TYPE_PLAYER && this.faction != unit.faction) {// 20级内的敌对玩家不允许攻击
			Player p = (Player) unit;
			if(!p.isPvpFaction())
				return 13;
		}
		return super.canAttack(unit);
	}

	public void breakAllActions() {
		itemUse = null;
		cancelExchange();
		autoAttack.clear();
		breakAutoNaturalEnhance();
		breakAutoAddHole();
	}

//	public void removePvpFlag() {
//		if (isPvp()) {
//			unPvp();
//		}
//		if (isPvpFaction() && level <= MAX_PVE_LEVEL) {
//			unPvpFaction();
//		}
//	}

	protected DieCallback dieCallback() {
		if (map.map != null) {
			if (map.map.manager.dieCallback() != null) {
				return map.map.manager.dieCallback();
			}
		}
		return Server.server.world.dieCallback;
	}

	protected MoveCallback moveCallback() {
		if (map.map != null) {
			if (map.map.manager.moveCallback() != null) {
				return map.map.manager.moveCallback();
			}
		}
		return null;
	}
	

	
	
	protected void realDie(Unit source){
		super.realDie(source);
		moveType |= MOVE_POINT_STATE;
		moveType |= MOVE_HPMP;
		breakAllActions();
		if (source != null && source.type != GameObject.TYPE_PLAYER) { // 如果是被玩家杀死的，比如PK或者切磋状态是不掉耐久的
			equipments.decAllArmorRemainDuration(6);
		}
		if (horse != null) {
			horse.equs.decAllArmorDuration(6, 100, this);
		}
		if (!isAlive())
			dieCallback().die(this, source);
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
		if(map!=null)
			log.info("[PLAYERDIE]"+LogUtil.getPlayerLogString(this)+"MAP["+map.id+"]X["+x+"]Y["+y+"]SOURCETYPE["+(source==null ? 0 : source.type)+"]");
	}

//	@Override
//	public void die(Unit source) {
//		super.die(source);
//	}

	public void relive(ReliveOption option) {
		ReliveTransferCall call = new ReliveTransferCall(this, option);
		Server.server.getWorld().schedule(call);
		unMoving();
		// acceptMoving = false;
		reliveOptions = null;
	}

	public void addReliveOption(ReliveOption option) {
		if (reliveOptions != null) {
			reliveOptions.addOption(option, true);
			send(reliveOptions.getRelivePacket());
		}
	}

	/**
	 * 根据reliveOptionId来进行复活
	 * 
	 * @param reliveId
	 */
	public void relive(int reliveId) {
		if (reliveOptions != null) {
			ReliveOption option = reliveOptions.get(reliveId);
			if (option != null) {
				relive(option);
			}
		}
	}

	@Override
	public void relive(int hp, int mp) {
		reliveOptions = null;
		setHp(hp, false);
		setMp(mp, false);
		initBuffs();
		refreshProperties(false);
		moveType = MOVE_ALL;
		state &= MASK_CLEAR;
		// log.debug("relive");
		lastMoveTime = CommonUtil.currentMillis();
		ignoreMoveTime = Time.currTime + 5000;
		if(isKing()==1){
			setKing();
			buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
		}
	}

	protected void addTitleBuffs() {
		if (titles.currentEquipTitle != null) {
			Buff buff = titles.currentEquipTitle.newBuff(this);
			if (buff != null) {
				buffs.addBuff(buff);
			}
		}
	}
	
	public void addPlayerSuiteEquipmentBuffs(){
		addSuiteEquipmentBuffs(equipments.equs);
	}

	protected void addSuiteEquipmentBuffs(GameItem[] gameItems) {
		List<Buff> buffList = getSuiteBuffs(gameItems);
		for(Buff suiteBuff : buffList){
			buffs.addBuff(suiteBuff);
		}
		buffList = getSuiteSpecialBuffs(gameItems);
		for(Buff specialBuff : buffList){
			if(buffs.getBuffByID(specialBuff.getId())==null)
				buffs.addBuff(specialBuff);
		}
	}
	
	protected void removeSuiteEquipmentBuffs(GameItem[] gameItems){
		List<Buff> buffList = getSuiteBuffs(gameItems);
		for(Buff suiteBuff : buffList){
			buffs.removeBuff(suiteBuff);
		}
		buffList = getSuiteSpecialBuffs(gameItems);
		for(Buff specialBuff : buffList){
			buffs.removeBuff(specialBuff);
		}
	}
	
	/** 获取套装效果 */
	protected List<Buff> getSuiteBuffs(GameItem[] gameItems){
		List<Buff> buffList = new ArrayList<Buff>();
		Map<SuiteEffects, Integer> map = new HashMap<SuiteEffects, Integer>();
		if (gameItems != null) {
			int flag = 0;
			for (GameItem gameItem : gameItems) {
				if (gameItem != null && gameItem.template != null
						&& gameItem.template.equipment != null
						&& gameItem.template.equipment.suiteEffects != null) {
					Set<SuiteEffects> keys = map.keySet();
					if (keys != null) {
						for (SuiteEffects key : keys) {
							if (key != null && gameItem.template.equipment.suiteEffects == key) {
								int c = map.get(key);
								c++;
								map.put(gameItem.template.equipment.suiteEffects, c);
								flag++;
							}
						}
						if (flag == 0) {
							map.put(gameItem.template.equipment.suiteEffects, 1);
						} else {
							flag = 0;
						}
					} else {
						map.put(gameItem.template.equipment.suiteEffects, 1);
					}
				}
			}
		}
		for (SuiteEffects key : map.keySet()) {
			for (SuiteEffect effect : key.getEffects()) {
				if (map.get(key) >= effect.count) {
					buffList.add(effect.buff);
				}
			}
		}
		return buffList;
	}
	
	/** 获取套装特效 */
	protected List<Buff> getSuiteSpecialBuffs(GameItem[] gameItems){
		List<Buff> buffList = new ArrayList<Buff>();
		if (gameItems != null) {
			for (GameItem gameItem : gameItems) {
				if (gameItem != null && gameItem.template.isEquipment()
						&& gameItem.template.equipment.specialEffect != null) {
					if (buffs.getBuffByID(gameItem.template.equipment.specialEffect.getId()) == null) {
						buffList.add(gameItem.template.equipment.specialEffect);
					}
				}
			}
		}
		return buffList;
	}
	
	public void addHorseSuiteEquipmentBuffs() {
		if(horse!=null){
			GameItem[] gameItems = horse.equs.equs;
			if (gameItems != null) {
				addSuiteEquipmentBuffs(horse.equs.equs);
			}
		}
	}

	protected void addHorseBuffs() {
		if (horse != null) {
			for (Skill skill : horse.skills) {
				Buff buff = skill.newBuff();
				if (buff != null)
					buffs.addBuff(buff);
				else
					log.info("[SKILLERROR]SKILL[" + skill.getId()
							+ "]BUFFNOTFOUND");
			}
		}
	}

	@Override
	public void update(int diff) {
		try {
			cycle++;
			
			// lighthu: 多线程改造后，登录后的用户的请求包处理放到这里进行
			if (this.session != null) {
				this.session.update(diff);
			}
			
			if (systemState == SYSTEMSTATE_DISCONNECTED) {
				if (System.currentTimeMillis() - changeStateStamp >= 30000L) { // 如果大于等于30秒，那么终止所有行动
					removeFromWorld();
					return;
				}
			}
			if (isAlive() && Time.currTime - lastRestoreTime > 5000) {
				lastRestoreTime = Time.currTime;
				setHp(this.hp + healthrestore, false);
				setMp(this.mp + manarestore, false);
			}
			if (itemUse != null && itemUse.time <= Time.currTime) {
				useItem();
				itemUse = null;
				recordLastAction();
			}
			processActivePower();
			processDie();
			processPvp();
			cds = coolDowns.update();
			buffs.update(diff);
			// processClientMove();
			// syncBuffAndClear();
			processAttack(diff);
			processAutoAttack(diff);
			processRide();
			processGather();
			if (systemState == SYSTEMSTATE_READY)
				asmVm.update(diff);
			actions.clear();
			syncWithClient();
			clearCycle();
			processThreats();
			processMove(this);
			processMoveExt();
			processRelive();
			processOnlineExp();
			processHorseOnlineExp();
			prosessAutoNaturalEnhance();
			processAutoAddHole();
			processTireState();
			if (cycle % 5 == 0) {
				runToNextPoint();
			}
			processScheduledPacket();
			try {
				processReport();
			} catch (Exception e) {
			}
			processAntiPlug();
			errorCount = 0;
		} catch (Exception e) {
			log.error(e, e);
			log.info("[PLAYERERROR]" + LogUtil.getPlayerLogString(this));
			errorCount++;
			if (errorCount > 10) {
				// 帐号异常，踢出
				try {
					this.logout();
				} catch (Exception e1) {
					log.error(e1, e1);
				}
			}
		}
	}
	
	protected void processAntiPlug(){
		try {
			if(id>0 && antiPlug!=null && !antiPlug.isBot){
				if(antiPlug.lastSendATime==0 || (Time.currTime-antiPlug.lastSendATime>=240000)){
					//下发A
					antiPlug.calc();
					send(antiPlug.getPacketA());
					antiPlug.lastSendATime = Time.currTime;
					antiPlug.sendA = true;
				}
				if(antiPlug.lastSendATime>0 
						&& (Time.currTime-antiPlug.lastSendATime>=120000) 
						&& antiPlug.lastSendBTime==0 
						&& antiPlug.sendA){
					//30秒后下发B
					send(antiPlug.getPacketB());
					antiPlug.lastSendBTime = Time.currTime;
				}
				if(antiPlug.lastSendBTime>0 && Time.currTime-antiPlug.lastSendBTime>240000){
					//时间同步发生,重置
					antiPlug.clear();
				}
				if(antiPlug.lastSendBTime>0 
						&& Time.currTime-antiPlug.lastSendBTime>120000){
					//30秒没有收到客户端校验包则判断为外挂
					antiPlug.clear();
					antiPlug.score += 1;
					if(antiPlug.score>=maxBotScore){
						antiPlug.isBot = true;
						LogUtil.logAntiPlug(this, "TIMEOUT");
					}
				}
				if(antiPlug.D1!=null && antiPlug.D!=null){
					//校验
					if(antiPlug.D1.intValue()!=antiPlug.D.intValue()){
						antiPlug.score += 1;
						if(antiPlug.score>=maxBotScore){
							antiPlug.isBot = true;
							LogUtil.logAntiPlug(this, "ERROR");
						}
					}
					antiPlug.clear();
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void checkPlug(Packet pt){
		String uiModel = pt.getString();
		int value = pt.getInt();
		antiPlug.uiModel = uiModel;
		antiPlug.calc1();
		antiPlug.D1 = value;
		antiPlug.enCode();
	}
	
	protected void processReport(){
		if(map==null || map.map==null || map.map.manager==null)
			return;
		if(map.map.manager instanceof FlagBattleFieldVMapManager){
			// 战场举报挂机
			FlagBattleFieldVMapManager manager = Server.server.getServiceRegistry().getFlagBattleFieldVMapManager();
			if((report.lastActionTime>0 && (Time.currTime-report.lastActionTime>120000) && !manager.hasStarted(this)) || 
					(report.lastActionTime>0 && (Time.currTime-report.lastActionTime>120000) && manager.hasStarted(this)) || 
					(report.effectReport>=3)){
				try {
					goMap(manager.outs[faction-1][0], manager.outs[faction-1][1], manager.outs[faction-1][2]);
					LogUtil.logProcessReport(this, Time.currTime-report.lastActionTime, report.effectReport);
				} catch (VMapException e) {
					log.error(e, e);
				} finally{
					report.clear();
				}
			}
		}else{
			// 野外举报挂机,向GM发信
			if((report.reportPlayerId!=null && report.effectReport>=20)){
				try {
					Account a = (Account)session.getIdentity();
					GMRequest request = new GMRequest(1,id,"系统",MessageFormat.format("玩家({0})挂机,被举报{1}次", name, report.effectReport),map.id,x,y,a.getModel());
					Channel channel = Server.server.getServiceRegistry().getChannelService().getChannel("gm");
					if(channel!=null){
						Packet pt = new Packet(OpCode.ADMIN_GMREQUEST_ADDED_SERVER);
						pt.putInt(request.id);
						pt.put(request.getType());
						pt.putInt(request.getPlayerId());
						pt.putString(request.getPlayerName());
						pt.putString(request.getCause());
						pt.put(request.state);
						pt.putString(request.getSolvent());
						pt.putString(request.getModel());
						pt.putShort(request.getMapId());
						pt.putShort(request.getX());
						pt.putShort(request.getY());
						channel.broadcast(pt, null);
					}
				} catch (Exception e) {
					log.error(e, e);
				} finally{
					report.clear();
				}
			}
			if(report.reportStartTime!=0 && (Time.currTime-report.reportStartTime>=60*60*1000)){
				report.effectReport /= 2;
				report.reportStartTime = Time.currTime;
			}
		}
	}
	
	
	/*
	 * 发送延迟发送的所有包。
	 */
	protected void processScheduledPacket() {
		int size = scheduledPacket.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				send(scheduledPacket.get(i));
			}
			scheduledPacket.clear();
		}
		size = scheduledChat.size();
		if (size > 0) {
			ChatService cs = Server.server.getServiceRegistry().getChatService();
			for (int i = 0; i < scheduledChat.size(); i++) {
				cs.addChatMessage(scheduledChat.get(i));
			}
			scheduledChat.clear();
		}
	}
	
	/*
	 * 处理防沉迷系统，根据用户在线时间增加防沉迷BUFF。
	 */
	protected void processTireState() {
		// 每1分钟检查一次疲劳状态，如果是，向用户发出警告
		if ((cycle % 600) == 0 || (!tireChecked && this.systemState == SYSTEMSTATE_READY)) {
			AccountStatService ass = Server.server.getServiceRegistry().getAccountStatService();
			int tireState = ass.getTireState(accountId);
			switch (tireState) {
			case 0:
				tirePercent = 1.0f;
				break;
			case 1:
				if (tirePercent != 0.5f) {
					shout("您累计在线时间已满3小时，请您下线休息，做适当身体活动。", 0xFF0000, 10000);
				} else {
					shout("您已经进入不健康游戏时间，您的游戏收益将降为正常值的50%。", 0xFF0000, 10000);
				}
				tirePercent = 0.5f;
				break;
			case 2:
				tirePercent = 0.0f;
				shout("您已进入不健康游戏时间，为了您的健康，请您立即下线休息。如不下线，您的身体将受到损害，您的收益已降为零，直到您的累计下线时间满5小时后，才能恢复正常。", 0xFF0000, 10000);
				break;
			}
			tireChecked = true;
		}
	}
	
	/**
	 * 向此用户发送喊话消息。
	 */
	public void shout(String msg, int color, int time) {
		Packet pt = new Packet(OpCode.SHOUT_SERVER);
		pt.putString(msg);
		pt.putInt(color);
		pt.putInt(time);
		send(pt);
	}
	
	/**
	 * 处理自动资质鉴定
	 */
	protected void prosessAutoNaturalEnhance(){
		if(autoNaturalEnhance!=null){
			boolean ok = false;
			for(int i=0;i<5;i++){
				PlayerTransaction tx = newTransaction("MNE");
				GameItem gameItem = bag.removeGameItem(ItemUtil.ITEM_NATURAL_ENHANCE, -1, 1, tx, true);
				if (gameItem == null) {
					tx.rollback();
					autoNaturalEnhance.cause = 0;
					ok = true;
					LogUtil.logAutoNaturalEnhance(this, autoNaturalEnhance.item, "NOKEY");
					break;
				}
				try {
					decMoney(autoNaturalEnhance.decMoney, tx, true);
					tx.commit();
					ItemUtil.naturalEnhance(autoNaturalEnhance.item);
					autoNaturalEnhance.money += autoNaturalEnhance.decMoney;
					autoNaturalEnhance.count++;
					NaturalEnhance[] enhances = ((ItemEnhance)autoNaturalEnhance.item.object).getNaturals();
					for(NaturalEnhance enhance : enhances){
						if(enhance.getLevel()>=autoNaturalEnhance.level){
							ok = true;
						}
					}
					if(ok){
						autoNaturalEnhance.cause = 2;
						LogUtil.logAutoNaturalEnhance(this, autoNaturalEnhance.item, "OK");
						break;
					}
				} catch (NoEnoughValueException e) {
					tx.rollback();
					autoNaturalEnhance.cause = 1;
					ok = true;
					LogUtil.logAutoNaturalEnhance(this, autoNaturalEnhance.item, "NOMONEY");
					break;
				}
			}
			if(ok){
				try {
					if (autoNaturalEnhance.owner instanceof Player) {
						refreshProperties(false);
					} else if (autoNaturalEnhance.owner instanceof Horse) {
						Horse h = (Horse) autoNaturalEnhance.owner;
						h.refreshProperties(false, this);
						if (h == horse) {
							refreshProperties(false);
						}
					}
					Packet pt = new Packet(OpCode.AUTO_NATURALENHANCE_SERVER);
					pt.putInt(autoNaturalEnhance.serial);
					pt.putInt(autoNaturalEnhance.count);
					pt.putInt(autoNaturalEnhance.money);
					pt.put(autoNaturalEnhance.item.toClientBytes());
					pt.put(autoNaturalEnhance.cause);
					send(pt);
					addAction(Action.NATURAL_ENHANCE);
				} catch (Exception e) {
					log.error(e, e);
				} finally{
					autoNaturalEnhance = null;
				}
			}
		}
	}
	
	protected void breakAutoNaturalEnhance(){
		if(autoNaturalEnhance!=null){
			try {
				if (autoNaturalEnhance.owner instanceof Player) {
					refreshProperties(false);
				} else if (autoNaturalEnhance.owner instanceof Horse) {
					Horse h = (Horse) autoNaturalEnhance.owner;
					h.refreshProperties(false, this);
					if (h == horse) {
						refreshProperties(false);
					}
				}
				Packet pt = new Packet(OpCode.AUTO_NATURALENHANCE_SERVER);
				pt.putInt(autoNaturalEnhance.serial);
				pt.putInt(autoNaturalEnhance.count);
				pt.putInt(autoNaturalEnhance.money);
				pt.put(autoNaturalEnhance.item.toClientBytes());
				pt.put(3);
				send(pt);
			} catch (Exception e) {
				log.error(e, e);
			} finally{
				autoNaturalEnhance = null;
			}
		}
	}
	
	protected void processAutoAddHole(){
		if(autoAddHole==null)
			return;
		Random rand = autoAddHole.rand;
		GameItem item = autoAddHole.gameItem;
		int destHole = autoAddHole.destHole;
		ItemEnhance itemEnhance = (ItemEnhance) item.object;
		JewelService js = Server.server.getServiceRegistry().getJewelService();
		List<ItemTemplate> l = js.getAddHoleItem(item.template.useLevel);
		for(int i=0;i<5;i++){
			PlayerTransaction tx = newTransaction("AUTOADDHOLE");
			int needMoney = js.getAddHolePrice(item.template.useLevel,
					itemEnhance.addHole + item.template.equipment.initHole);
			boolean ok = false;
			try {
				decMoney(needMoney, tx, true);
				autoAddHole.decMoney += needMoney;
				for (ItemTemplate it : l) {
					if (bag.removeGameItemIngoreInstanceId(it.id, 1, tx, true) != null) {
						ok = true;
						autoAddHole.useBannerAccount++;
						break;
					}
				}
			} catch (NoEnoughValueException e) {
				tx.rollback();
				Packet pt = new Packet(OpCode.AUTO_ADDHOLE_SERVER);
				pt.putInt(autoAddHole.serial);
				pt.put(autoAddHole.realHoles);
				pt.putInt(autoAddHole.useBannerAccount);
				pt.putInt(autoAddHole.decMoney);
				pt.put(1);
				session.send(pt);
				LogUtil.logAutoAddHole(this, autoAddHole.wantHole, autoAddHole.realHoles, 
						autoAddHole.useBannerAccount,autoAddHole.decMoney);
				autoAddHole = null;
				return;
			}
			if (ok) {
				tx.commit();
				int rate = js.getAddHoleSuccRate(itemEnhance.addHole
						+ item.template.equipment.initHole);
				Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(faction);
				NationSkill2 skill = (NationSkill2)nation.skills.get(2);
				if(skill != null){
					float v = skill.getAddHoleAdded();
					if(v != 0f){
						rate *= (1 + v);
						if(rate > 10000){
							rate = 10000;
						}
					}
				}
				if (rand.nextInt(10000) <= rate) {
					itemEnhance.addHole++;
					autoAddHole.realHoles++;
					if(itemEnhance.addHole==destHole){
						Packet pt = new Packet(OpCode.AUTO_ADDHOLE_SERVER);
						pt.putInt(autoAddHole.serial);
						pt.put(autoAddHole.realHoles);
						pt.putInt(autoAddHole.useBannerAccount);
						pt.putInt(autoAddHole.decMoney);
						pt.put(0);
						session.send(pt);
						//打孔成功事件
				        Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_DIG_SUCCESS, this));
						LogUtil.logAutoAddHole(this, autoAddHole.wantHole, autoAddHole.realHoles, 
								autoAddHole.useBannerAccount,autoAddHole.decMoney);
						autoAddHole = null;
						return;
					}
				}
			}else{
				tx.rollback();
				Packet pt = new Packet(OpCode.AUTO_ADDHOLE_SERVER);
				pt.putInt(autoAddHole.serial);
				pt.put(autoAddHole.realHoles);
				pt.putInt(autoAddHole.useBannerAccount);
				pt.putInt(autoAddHole.decMoney-needMoney);
				pt.put(2);
				session.send(pt);
				LogUtil.logAutoAddHole(this, autoAddHole.wantHole, autoAddHole.realHoles, 
						autoAddHole.useBannerAccount,autoAddHole.decMoney-needMoney);
				autoAddHole = null;
				return;
			}
		}
		
	}
	
	protected void breakAutoAddHole(){
		if(autoAddHole!=null){
			try {
				Packet pt = new Packet(OpCode.AUTO_ADDHOLE_SERVER);
				pt.putInt(autoAddHole.serial);
				pt.put(autoAddHole.realHoles);
				pt.putInt(autoAddHole.useBannerAccount);
				session.send(pt);
			} catch (Exception e) {
				log.error(e, e);
			} finally{
				autoAddHole = null;
			}
		}
	}
	
	protected void processOnlineExp(){
		if(onlineExpTime != 0){
			if(Time.currTime > onlineExpTime){
				Server.server.getServiceRegistry().getExpService().handleOnlineExp(this);
				onlineExpTime = (int) (Time.currTime + ExpService.onlineDis);
			}
		}
	}
	
	protected void processHorseOnlineExp(){
		List<Horse> horses = Server.server.getServiceRegistry().getExpService().getAgentHoeses(this);
		Horse horse1 = null;
		for(Horse h : horses){
			horse1 = h;
		}
		if(horse1!=null && horse1.notOnlineExpTime != 0){
			if(Time.currTime > horse1.notOnlineExpTime){
				Server.server.getServiceRegistry().getExpService().handleHorseExp(this);
				horse1.notOnlineExpTime = (int) (Time.currTime + ExpService.notonlineDis);
			}
		}
	}
	
	@Transient
	protected int lastSystemSendPositionTime;
	
	@Override
	public void processMove(Player p){
		if(lastPosition!=null){
			if((Time.currTime-lastPosition.time)>15*1000&&(Time.currTime-lastSystemSendPositionTime)>15*1000){ //如果超过15秒没发position，系统帮发
				moveType |= GameObject.MOVE_POINT;
				lastSystemSendPositionTime = Time.currTime;
//				lastPosition = new Position(map.id,x,y,Time.currTime,Time.currTime);
			}
		}
		super.processMove(p);
	}
	
	protected void processActivePower(){
		if((Time.currTime-lastRestoreActivePowerTime)>30 * 60 *1000){ //半小时增加2点行动力
			setActivePower(Math.min(activePower + 2, 100));
			lastRestoreActivePowerTime = Time.currTime;
		}
	}
	
	public void setActivePower(int value) {
		if (value != activePower) {
			activePower = value;
			addIntPropertyChangedItem(ChangedItem.ACTIVEPOWER, activePower, false);
		}
	}
	
	public void checkActivePower(){
		int day = pool.getInt(PROPERTY_RESTORE_ACTIVEPOWER_DAY);
		if(day!=Time.day){
			setActivePower(100);
			pool.setInt(PROPERTY_RESTORE_ACTIVEPOWER_DAY, Time.day);
		}
	}

	// protected void processClientMove() {
	// if (isAlive() && systemState == SYSTEMSTATE_READY) {
	// if (moves.size() > 0) {
	// ClientMove m = moves.remove(0);
	// super.move(m.x, m.y);
	// this.direct = m.direct;
	// this.state = (short) (((this.state & 0xFFFE) | (m.state & 0x1)) &
	// 0xFFFF);
	// this.lastMoveTime = m.time;
	// if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
	// Server.server.eventManager.fireEvent(new ServiceEvent(
	// ServiceEvent.EVENT_PLAYER_PK_MOVE, this, x, y));
	// }
	// moveType |= MOVE_RUNNING_STATE;
	// }
	// }
	// }

	protected void processPvp() {
		if(warState!=null){
			warState.update(this);
		}
//		if (pvpTime != 0) {
//			if (pvpTime <= Time.currTime) {
//				unPvp();
//			}
//		}
//		if (pvpFactionTime != 0) {
//			if (pvpFactionTime <= Time.currTime) {
//				unPvpFaction();
//			}
//		}
	}

	@Override
	public void addThreatUnit(Unit u, float initThreat, boolean direct) {
		super.addThreatUnit(u, initThreat, direct);
		if (u.type == GameObject.TYPE_PLAYER) {
			if (u.faction != faction && initThreat != 0.0f) {
				Player p = (Player) u;
				p.warState.war(p);
//				if (!p.isPvpFaction()) {
//					p.pvpFaction(PVP_TIME);
//				} else {
//					if (pvpFactionTime != 0) {
//						pvpFactionTime = Time.currTime + PVP_TIME;
//					}
//				}
			}
			enemyPlayers.put(u.id, Time.currTime);
		}
	}

	protected void processMoveExt() {
		if ((moveExtended & MOVEEXT_CREDIT) != 0
				|| (moveExtended & MOVEEXT_GUILD) != 0
				|| (moveExtended & MOVEEXT_TITLE) != 0) {
			Packet pt = getInfoPacket();
			broadcast(pt, this, null, true, false, false);
		}
		if ((moveExtended & MOVEEXT_BUFFS) != 0) {
			Packet pt = getBuffsPacket();
			if(party!=null){
				party.broadcast(pt);
			}
			broadcast(pt, this, null, true, false, false);
		}
		moveExtended = 0;
	}

	@Override
	public boolean breakAttack() {
		Attack oldAttack = attack;
		super.breakAttack();
		if(attendant!=null){
			Attack oldAttAttack = attendant.attack;
			attendant.breakAttack();
			if(oldAttAttack!=null){
				Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
				pt.put((byte) 15);
				pt.putInt(oldAttAttack.getSourceInstanceId());
				pt.putInt(oldAttAttack.getTargetInstanceId());
				pt.putInt(oldAttAttack.skill.getId());
				send(pt);
			}
		}
		if (oldAttack != null) {
			Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
			pt.put((byte) 15);
			pt.putInt(oldAttack.getSourceInstanceId());
			pt.putInt(oldAttack.getTargetInstanceId());
			pt.putInt(oldAttack.skill.getId());
			send(pt);
			return true;
		}
		return false;
	}

	protected void gatherStart(GatherUnit gu, int serial) {
		gather = new Gather(serial, gu.ref(), Time.currTime + gu.gatherTime);
		gu.isGathering = 1;
		if(gu.isPvp && warState==Player.PVPPVESTATE && gu.isPvp){
			setWarState(Player.PVPSTATE);
		}
	}

	protected void processGather() {
		if (gather != null) {
			if (gather.time <= Time.currTime) {
				GatherUnit gu = (GatherUnit) ObjectAccessor
						.getGameObject(gather.ref.instanceId);
				if (gu != null && gu.isAlive()) {
					gu.gatherEnd(this);
				} else {
					cancelGather(1);
				}
				if(gu!=null)
					gu.isGathering = 0;
				gather = null;
			}
		}
	}

	public void cancelGather(int code) {
		if (gather != null) {
			Packet pt = new Packet(OpCode.GATHER_CANCLED_SERVER);
			pt.putInt(gather.serial);
			pt.put(code);
			send(pt);
			gather = null;
		}
	}

	protected void processAttack(int diff) {
		if (attack != null) {
			recordLastAction();
			int attackRet = 0;
			if ((attackRet = attack.update(diff)) != -1) { // 如果返回-1，说明技能还在施放，返回其他的说明施放成功或者不能施放，全部都得设置成null
				if (attackRet == 0) { // 成功施放
					if (attack != null) {
						if ((attack.skill.getType() & Skill.TYPE_ATTACK) != 0
								&& attack.targetRef != null) {
							autoAttack.time = Time.currTime
									+ AUTO_ATTACK_INTERVAL
									- ObjectAccessor.getSkill(1).getActTime(
											this);
							autoAttack.fired = true;
							autoAttack.ref = attack.targetRef;
							autoAttack.attack = null;
							Packet pt = new Packet(
									OpCode.AUTOATTACK_START_SERVER);
							pt.putInt(autoAttack.ref.instanceId);
							send(pt);
						} else {
							if (autoAttack.fired) {
								autoAttack.time = Time.currTime
										+ AUTO_ATTACK_INTERVAL;
								autoAttack.attack = null;
							}
						}
					}
					if(attack.skill != null)
						lastSkillId = attack.skill.getId();  //todo:NullPointerException
					broadcast(getMovePacket(moveType),null,this,false,false,false);
				} else {
        			Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
                    pt.put((byte)attackRet);
                    pt.putInt(this.instanceId);
                    pt.putInt(attack.getTargetInstanceId());
                    pt.putInt(attack.skill.getId());
                    send(pt);
				}
				// lastSkillId = attack.skill.getId();
				setAttack(null);
			}
		}
	}

	public void useItem(int gridId, int itemId, int instanceId, int targetId,
			int offsetTime) {
		if (itemUse != null) {
			sendUseItemFail(itemId, "当前有正在使用的物品");
			return;
		}
		GameItem item = bag.getGameItem(gridId, itemId, instanceId);
		if (item == null) {
			sendUseItemFail(itemId, "没找到物品");
			return;
		}
		if (item.template.useType == null
				|| item.template.useType.effect == null) {
			sendUseItemFail(itemId, "物品不能使用");
			return;
		}
		if (item.template.useLevel > level) {
			sendUseItemFail(itemId, item.template.useLevel + "级才能使用此物品");
			return;
		}
		if (coolDowns.atCoolDown(item.template.useType.coolDownId)) {
			int leaveTime = coolDowns.getLeaveTimeByCoolDownId(item.template.useType.coolDownId);
			sendUseItemFail(itemId, MessageFormat.format("还剩{0}才能继续使用", TimeUtil.getStringH_M_S(leaveTime/1000)));
			return;
		}
		if (item.template.useType.occasion == UseType.OCCASION_BATTLE) {
			if (this.getThreatCount() == 0) {
				sendUseItemFail(itemId, "只能在战斗中使用");
				return;
			}
		} else if (item.template.useType.occasion == UseType.OCCASION_NOBATTLE) {
			if (this.getThreatCount() > 0) {
				sendUseItemFail(itemId, "不能在战斗中使用");
				return;
			}
		} else if (item.template.useType.useClazz != 4
				&& item.template.useType.useClazz != clazz) {
			sendUseItemFail(itemId, "职业不对");
			return;
		}
		GameObjectRef target = null;
		if (targetId != -1) {
			GameObject o = ObjectAccessor.getGameObject(targetId);
			if (o != null) {
				if (!o.isAlive()) {
					sendUseItemFail(itemId, "无效的目标");
				} else {
					target = o.ref();
				}
			} else {
				sendUseItemFail(itemId, "无效的目标");
				return;
			}
		}
		int time = Time.currTime
				+ Math.max(item.template.useType.spellTime - offsetTime, 0);
		itemUse = new ItemUse(target, gridId, itemId, instanceId, time);
		coolDowns.setCommonCD(500);
		StatService statService = Server.server.getServiceRegistry().getStatService();
		int index = statService.isInArray(statService.foodIds, itemId);
		if(index != -1){
		   Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_USEITEM,this,itemId,1));
		}
	}

	public void sendUseItemFail(int itemId, String msg) {
		Packet pt = new Packet(OpCode.USEITEM_FAIL_SERVER);
		pt.putInt(itemId);
		pt.putString(msg);
		send(pt);
	}

	protected void useItem() {
		int gridId = itemUse.gridId;
		int itemId = itemUse.itemId;
		int instanceId = itemUse.instanceId;
		PlayerTransaction tx = newTransaction("USE");
		TransactionBagGrid grid = bag.removeGridGameItem(gridId, itemId,
				instanceId, 1, tx, true);
		if (grid == null || grid.item == null
				|| grid.item.template.id != itemId) {
			sendUseItemFail(itemId, "没找到物品");
			tx.rollback();
			return;
		}
		if (grid.item.template.useType == null
				|| grid.item.template.useType.effect == null) {
			sendUseItemFail(itemId, "物品不能使用");
			tx.rollback();
			return;
		}
		if (grid.item.template.useType.occasion == UseType.OCCASION_BATTLE) {
			if (this.getThreatCount() == 0) {
				sendUseItemFail(itemId, "只能在战斗中使用");
				tx.rollback();
				return;
			}
		} else if (grid.item.template.useType.occasion == UseType.OCCASION_NOBATTLE) {
			if (this.getThreatCount() > 0) {
				sendUseItemFail(itemId, "不能在战斗中使用");
				tx.rollback();
				return;
			}
		}
		GameObject target = null;
		if (itemUse.target != null) {
			target = ObjectAccessor.getGameObject(itemUse.target);
			if (target == null) {
				sendUseItemFail(itemId, "无效目标");
				tx.rollback();
				return;
			} else {
				if (!target.isAlive()) {
					sendUseItemFail(itemId, "无效目标");
					tx.rollback();
					return;
				}
				GameItem item = grid.item;
				ItemEffect effect = item.template.useType.effect;
				if (!effect.isAsync()) {  //不需要异步
					PlayerTransaction tx2 = newTransaction("ITE");
					try {
						effect.use(this, item, (Unit) target, tx2);
						tx2.commit();
						if (grid.item.template.useType.consume) {
							tx.commit();
						} else {
							tx.rollback();
						}
						setCoolDown(item.template.useType.coolDownId,
								Time.currTime, Time.currTime
										+ item.template.useType.coolDownTime);

						// 使用成功
						lastItemId = itemId;
						
						// 发送通知
						List<GameItem> nitems = tx2.getNoticeItems();
						if (nitems != null) {
							AddItemEffect.sendItemNotice(nitems, this, item.template.name);
						}
						
						// 需要邮件发送的物品这里发送
						List<GainItem> mitems = tx2.getMailItems();
						if (mitems != null) {
							DBService dbs = Server.server.getServiceRegistry().getDbService();
					        this.message(-1, "您背包已满，获得的物品已经通过邮件发送了，请查收。", -1, -1);
					        for (GainItem gitem : mitems) {
					            GameItem addItem = gitem.getItem();
					            String itemTitle = addItem.template.name;
					            if (gitem.getCount() > 1) {
					                itemTitle += "x" + gitem.getCount();
					            }
					            Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(this.id, "系统", itemTitle, "", 0,
					            		gitem.getItem(), gitem.getCount(), "ITE");
					        }
						}
						
						// 记录日志
						LogUtil.logUseItem(this, item, item.template.useType.consume);
					} catch (UseItemException e) {
						tx2.rollback();
						tx.rollback();
						sendUseItemFail(itemId, e.getMessage());
						return;
					}
				}else{
					tx.rollback(); //如果是异步的，那么就放入UseItemCall里去扣除
					UseItemCall useItemCall = new UseItemCall(session,ref(),itemUse);
					Server.server.getServiceRegistry().getDbService().schedule(useItemCall);
				}
			}
		} else {
			tx.rollback();
			sendUseItemFail(itemId, "没有指定目标");
		}
	}

	@Override
	public CoolDown setCoolDown(int id, int startTime, int time) {
		CoolDown cd = super.setCoolDown(id, startTime, time);
		Packet pt = new Packet(OpCode.COOLDOWN_SERVER);
		pt.putShort(id);
		pt.putInt(cd.startTime);
		pt.putInt(cd.endTime);
		send(pt);
		return cd;
	}

	protected void processAutoAttack(int diff) {
		if (autoAttack.fired) {
			if (autoAttack.attack != null) {
				recordLastAction();
				int attackResult = autoAttack.attack.update(diff);
				if (attackResult == 3 || attackResult == 4 || attackResult == 8) { // 目标死亡，目标不存在，不能对目标使用时停止自动攻击
					autoAttack.clear();
				}
				if (attackResult == 0 || attackResult == 12
						|| attackResult == 1) { // 如果成功施放 ,或者没有足够mana
					autoAttack.time = Time.currTime + AUTO_ATTACK_INTERVAL; // 两秒钟以后再出招
					autoAttack.attack = null;
				}
			} else {
				if (autoAttack.time <= Time.currTime && autoAttack.fired) {
					GameObject target = ObjectAccessor
							.getGameObject(autoAttack.ref);
					Skill skill = ObjectAccessor.getSkill(1);
					if (target == null
							|| !target.inRange(this, skill.getDistance(this))) {
						autoAttack.time = Time.currTime + AUTO_ATTACK_INTERVAL;
						autoAttack.attack = null;
					} else {
						autoAttack.attack = new Attack(ObjectAccessor
								.getSkill(1), this, target, 0, true, true);
					}
				}
			}
		}
	}

	protected void processRelive() {
//		if(relive!=null){
//			reliveOptions = null;
//			relive.relive(this);
//			relive = null;
//		}
		if (reliveOptions != null) {
			ReliveOption option = reliveOptions.update();
			if (option != null) {
				relive(option);
			}
		}
	}

	// protected void syncBuffAndClear() {
	// // List<Buff> removedBuff = buffs.getRemovedBuffs();
	// // List<Buff> addedBuff = buffs.getAddedBuffs();
	// // List<Buff> mergedBuff = buffs.getMergedBuffs();
	// // if (removedBuff.size() > 0 || addedBuff.size() > 0
	// // || mergedBuff.size() > 0) {
	// //// Packet pt = new Packet(OpCode.SYNC_BUFF_SERVER);
	// //// pt.put(removedBuff.size());
	// //// for(Buff b:removedBuff){
	// //// pt.putInt(b.getInstanceID());
	// //// }
	// //// pt.put(addedBuff.size());
	// //// for(Buff b:addedBuff){
	// //// pt.putInt(b.getInstanceID());
	// //// pt.putInt(b.getIconID());
	// //// pt.putInt(b.getEndTime());
	// //// }
	// //// pt.put(mergedBuff.size());
	// //// for(Buff b:mergedBuff){
	// //// pt.putInt(b.getInstanceID());
	// //// pt.putInt(b.getIconID());
	// //// pt.putInt(b.getEndTime());
	// //// }
	// //// send(pt);
	// // buffs.clearHistory();
	// //
	// // }
	// }

	public void syncWithClient() {
		if (changed.isChanged()) {
			changed.sendAndClean(session, this);
		}
		if (cds != null && cds.size() > 0) {
			Packet pt = new Packet(OpCode.COOLDOWN_END_SERVER);
			pt.put(cds.size());
			for (CoolDown cd : cds) {
				pt.putShort(cd.id);
			}
			send(pt);
		}
	}

	/**
	 * 取得指定对象周围指定范围内允许被治疗的对象。玩家只能治疗本队伍的玩家。
	 */
	@Override
	public Unit[] getAidUnits(GameObject ref, int dist) {
		List<Unit> list = new ArrayList<Unit>();
//		if(attendant!=null)
//			list.add(attendant);
		if (this.party == null) {
			// 如果没有在队伍中，则只判断自己
			if (this.map.map == ref.map.map && this.inRange(ref, dist)) {
				list.add(this);
			}
		} else {
			// 检查所有队友
			List<Player> ps = this.party.getPlayerInRange(this.map.map, dist,
					ref.x, ref.y);
			for (Player p : ps) {
			    if (p != ref && p.isAlive() && canAid(p)) {
			        list.add(p);
			    }
			}
		}
		Unit[] ret = new Unit[list.size()];
		list.toArray(ret);
		return ret;
	}

	public void chat(int questId, int npcId, String message, int notifyId) {
		Packet pt = new Packet(OpCode.NPC_CHAT_SERVER);
		pt.putInt(questId);
		pt.putInt(npcId);
		pt.putString(message);
		pt.putInt(notifyId);
		send(pt);
	}

	public void message(int questId, String message, int timeout, int notifyId) {
		Packet pt = new Packet(OpCode.MESSAGE_SERVER);
		pt.putInt(questId);
		pt.putString(message);
		pt.putInt(timeout);
		pt.putInt(notifyId);
		send(pt);
	}
	
	public void addBubble(int npcId, String message, int time){
		Packet pt = new Packet(OpCode.NPC_BUBBLE_SERVER);
		pt.putInt(npcId);
		pt.putString(message);
		pt.putInt(time);
		send(pt);
	}

	public void question(int questId, String question, String options,
			int notifyId) {
		Packet pt = new Packet(OpCode.QUESTION_SERVER);
		pt.putInt(questId);
		pt.putString(question);
		pt.putString(options);
		pt.putInt(notifyId);
		send(pt);
	}

	public GameObject getVictim() {
		if (targetRef != null)
			return ObjectAccessor.getGameObject(targetRef);
		return null;
	}

	public void send(Packet packet) {
		if (session != null)
			session.send(packet);
	}
	
	public void schedule(Packet packet) {
		scheduledPacket.add(packet);
	}

	public void schedule(ChatMessage cm) {
		scheduledChat.add(cm);
	}
	
	@Override
	public void addToMap(VMap map, int x, int y) {
		addToMap(map, x, y, true);
	}

	public void addToMap(VMap map, int x, int y, boolean loading) {
		this.map.setMap(map);
		if (loading) {
			setSystemState(SYSTEMSTATE_LOGINED);
		}
		map.addPlayer(this, x, y);
	}

	public void removeFromMap() {
		if (this.map != null && map.map != null) {
			clearThreats();
			breakAttack();
			cancelGather(2);
			map.removeGameObject(this, true);
			// moves.clear();
			unMoving();
			// map.map =null;
		}
	}
	
	public int getTodayFactionChatCount(){
		int lastday = pool.getInt(PROPERTY_LAST_FATION_CHAT);
		if(lastday!=Time.day){
			pool.setInt(PROPERTY_LAST_FATION_CHAT, Time.day);
			pool.setInt(PROPERTY_TODAY_FACTION_CHAT_COUNT, 0);
			return 0;
		}else{
			return pool.getInt(PROPERTY_TODAY_FACTION_CHAT_COUNT);
		}
	}
	
	public void setTodayFactionChatCount(int value){
		pool.setInt(PROPERTY_TODAY_FACTION_CHAT_COUNT, value);
	}

	public void touchNpc(GameObject unit, int questId) {
		if (unit.type == TYPE_CREATURE) {
			// 特殊处理战役副本于吉BUFF
			if(unit.id==5963804 || unit.id==5963805 || unit.id==5963806 || unit.id==5963807 || 
					unit.id==5963808 || unit.id==5963809){
				if(buffs.getBuffByID(259)==null){
					buffs.addBuff(BuffUtil.createBuff(259, 1, this, this, 0));
					message(-1, "您已得到于吉道长的法术祝福，持续时间3分钟", -1, -1);
				}else{
					message(-1, "您已获得该效果", -1, -1);
				}
			}
			if (questId <= -2) {
				Creature npc = (Creature) unit;
				TouchAction[] touchAction = npc.touchAction;
				if (touchAction != null)
					touchAction[-2 - questId].touch(this, npc);
			} else {
				TouchNpcInfo info = new TouchNpcInfo(unit.ref(), questId);
				touchedNpc.add(info);
			}
		}
	}

	public boolean hasTouchNpc(int npcId, int questId) {
		for (TouchNpcInfo info : touchedNpc) {
			if (info.questId == questId && info.npcRef.id == npcId)
				return true;
		}
		return false;
	}

	protected void unSuiteEffect(EquipmentTemplate template, GameItem[] gameItems) {
//		GameItem[] gameItems = equipments.equs; // 得到player身上的装备
		SuiteEffects effects = template.suiteEffects;
		// 清除Buff
		if (template.suiteEffects != null
				&& template.suiteEffects.getEffects() != null) {
			if (effects != null && gameItems != null) {
				for (SuiteEffect effect : template.suiteEffects.getEffects()) {
					Buff buff = effect.getBuff();
					int effectCount = effect.getCount();
					int count = 0;
					for (GameItem gameItem : gameItems) {
						if (gameItem != null
								&& template.suiteEffects != null
								&& gameItem.template.equipment.suiteEffects != null
								&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
							count++;
						}
					}
					if (count == effectCount - 1) { // 如果卸下装备后影响原来的套装效果，则清除此效果
						buffs.removeBuff(buff);
					}
				}
			}
		}
	}

	public void horseUnequip(int itemId, int instanceId, int serial,
			int horseInstanceId) {
		Horse h = horseBag.getHorse(horseInstanceId);
		if (h == null) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.HORSE_EQUIP_CLIENT, "没有找到指定的坐骑");
			return;
		}
		PlayerTransaction tx = newTransaction("HUE");
		GameItem item = h.equs.unequip(itemId, instanceId, this);
		if (item != null) {
			try {
				bag.addGameItemComplete(item, 1, tx, false);
				tx.commit();
				Packet pt = new Packet(OpCode.HORSE_UNEQU_SERVER);
				pt.putInt(serial);
				send(pt);
				unSuiteEffect(item.template.equipment, h.equs.equs);
				if (h == horse)
					refreshHorseAndPlayerProperty();
				else
					h.refreshPropertiesExcepPlayer(false, this);
			} catch (NoEnoughSpaceException e) {
				h.equs.equip(item, this);
				tx.rollback();
				ErrorHandler.sendErrorMessage(this.session, serial,
						OpCode.UNEQUIP_CLIENT, "没有足够的包位");
			}
		} else {
			ErrorHandler.sendErrorMessage(this.session, serial,
					OpCode.UNEQUIP_CLIENT, "没有找到装备");
		}

	}

	public void unequip(int itemId, int instanceId, int serial) {
		PlayerTransaction tx = newTransaction("UEQ");
		GameItem item = equipments.unequip(itemId, instanceId);
		if (item != null) {
			try {
				bag.addGameItemComplete(item, 1, tx, false);
				if (item.template.isEquipment()
						&& item.template.equipment != null) {
					EquipmentTemplate template = item.template.equipment;
					if (template.specialEffect != null) {
						buffs.removeBuff(template.specialEffect);
					}
					unSuiteEffect(template, equipments.equs);
				}
				tx.commit();
				refreshStar7Buff();
				refreshProperties(false);
			} catch (NoEnoughSpaceException e) {
				equipments.equip(item);
				tx.rollback();
				ErrorHandler.sendErrorMessage(this.session, serial,
						OpCode.UNEQUIP_CLIENT, "没有足够的包位");
			}
		} else {
			ErrorHandler.sendErrorMessage(this.session, serial,
					OpCode.UNEQUIP_CLIENT, "没有找到装备");
		}
		refreshStarState();
	}

	public void horseEquip(int itemId, int instanceId, int serial,
			int horseInstanceId) {
		Horse h = horseBag.getHorse(horseInstanceId);
		if (h == null) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.HORSE_EQUIP_CLIENT, "没有找到指定的坐骑");
			return;
		}
		PlayerTransaction tx = newTransaction("HEQ");
		TransactionBagGrid grid = bag.removeGameItemInstance(itemId,
				instanceId, tx, false);
		if (grid != null && grid.item.template.isHorseEquipment()) {
			GameItem equ = grid.item;
			EquipmentTemplate template = equ.template.equipment;
			if (equ.template.hasDuration() && equ.duration <= 0) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, "装备已损坏，不能装配");
				tx.rollback();
				return;
			}
			if (h.level < template.useLevel) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, "坐骑等级不够");
				tx.rollback();
				return;
			}
			if ((template.agilityLimit > 0 && h.agility < template.agilityLimit)
					|| (template.strengthLimit > 0 && h.strength < template.strengthLimit)
					|| (template.intelligentLimit > 0 && h.intellect < template.intelligentLimit)
					|| (template.staminaLimit > 0 && h.stamina < template.staminaLimit)) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, "属性不对");
				tx.rollback();
				return;
			}

			if (equ.template.bindType == ItemTemplate.BIND_USED
					|| equ.template.bindType == ItemTemplate.BIND_REWARD) {
//				if (!equ.isBound() || equ.bindInstance == 0
//						|| equ.bindInstance == h.instanceId) {
//					if (equ.bindInstance != h.instanceId) { // 把绑定id设置成马的id
//						equ.bindInstance = h.instanceId;
//						BindChangedItem item = new BindChangedItem(equ);
//						changed.addChangedItem(item);
//					}
//				} else {
//					ErrorHandler.sendErrorMessage(session, serial,
//							OpCode.EQUIP_CLIENT, "装备已经被绑定");
//					tx.rollback();
//					return;
//				}
				if (!equ.isBound()){
					equ.bindInstance = 0;
					BindChangedItem item = new BindChangedItem(equ);
					changed.addChangedItem(item);
				}
			}
			tx.commit();
			GameItem old = h.equs.equip(equ, this);
			if (old != null) {
				tx = newTransaction("HUE");
				grid.addGameItem(old, 1, tx, false);
				tx.commit();
			}
			if (old != null && old.template.equipment.specialEffect != null) {
				buffs.removeBuff(old.template.equipment.specialEffect);
			}
			if (old != null
					&& (old.template.equipment.suiteEffects != null)
					&& (old.template.equipment.suiteEffects != equ.template.equipment.suiteEffects)) { // 如果套装效果一样就没必要去除了
				unSuiteEffect(old.template.equipment, h.equs.equs);
			}
			// 添加套装特效
			if (template.specialEffect != null) {
				Buff specialBuff = buffs
						.getBuffByID(template.specialEffect.getId());
				if (specialBuff == null) {
					buffs.addBuff(template.specialEffect);
				}
			}
			// 添加新装备后添加Buff
			if (template.suiteEffects != null
					&& ((old == null) || (old != null && old.template.equipment.suiteEffects != template.suiteEffects))) { // 如果套装效果一样就没必要加了
				SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
				GameItem[] gameItems = h.equs.equs; // 得到player身上的装备
				for (SuiteEffect effect : suiteEffects) {
					Buff buff = effect.buff;
					int effectCount = effect.getCount();
					int count = 0;
					if (gameItems != null) {
						for (GameItem gameItem : gameItems) {
							if (gameItem != null
									&& gameItem.template.isEquipment()) {
								if (gameItem != null
										&& gameItem.template.equipment.suiteEffects != null
										&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
									count++;
								}
							}
						}
					}
					if (count == effectCount) { // 添加新装备后如果正好构成一个套装效果，则添加此套装效果
						buffs.addBuff(buff);
					}
				}
			}
			if (h == horse)
				refreshHorseAndPlayerProperty();
			else
				h.refreshPropertiesExcepPlayer(false, this);
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_HORSE_EQUIP, this));
			Packet pt = new Packet(OpCode.HORSE_EQUIP_SERVER);
			pt.putInt(serial);
			send(pt);
		}
	}

	public void equip(int itemId, int instanceId, int serial) {
		PlayerTransaction tx = newTransaction("EQU");
		TransactionBagGrid grid = bag.removeGameItemInstance(itemId,
				instanceId, tx, false);
		if (grid != null && grid.item.template.isEquipment()
				&& !grid.item.template.isHorseEquipment()) {
			GameItem equ = grid.item;
			EquipmentTemplate template = equ.template.equipment;
			if (equ.template.equipment.minorType == EquipmentTemplate.MINORTYPE_BOW
					&& clazz != CLASS_2) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, "不能装配此装备");
				tx.rollback();
				return;
			} 
			if (equ.template.hasDuration() && equ.duration <= 0) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, "装备已损坏，不能装配");
				tx.rollback();
				return;
			}
			if (level < template.useLevel) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, "等级不够");
				tx.rollback();
				return;
			}
			if (template.clazz != -1 && clazz != template.clazz) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, "不能装配此装备");
				tx.rollback();
				return;
			}
			if ((template.agilityLimit > 0 && agility < template.agilityLimit)
					|| (template.strengthLimit > 0 && strength < template.strengthLimit)
					|| (template.intelligentLimit > 0 && intellect < template.intelligentLimit)
					|| (template.staminaLimit > 0 && stamina < template.staminaLimit)) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, "属性不对");
				tx.rollback();
				return;
			}
			tx.commit();
			if (equ.template.bindType == ItemTemplate.BIND_USED
					&& !equ.isBound()) {
				equ.bindInstance = 0;
				BindChangedItem item = new BindChangedItem(equ);
				changed.addChangedItem(item);
			}
			GameItem old = equipments.equip(equ);
			if (old != null) {
				tx = newTransaction("UEQ");
				grid.addGameItem(old, 1, tx, false);
				tx.commit();
			}
			if (old != null && old.template.equipment.specialEffect != null) {
				buffs.removeBuff(old.template.equipment.specialEffect);
			}
			if (old != null
					&& (old.template.equipment.suiteEffects != null)
					&& (old.template.equipment.suiteEffects != equ.template.equipment.suiteEffects)) { // 如果套装效果一样就没必要去除了
				unSuiteEffect(old.template.equipment, equipments.equs);
			}
			// 添加套装特效
			if (template.specialEffect != null) {
				Buff specialBuff = buffs
						.getBuffByID(template.specialEffect.getId());
				if (specialBuff == null) {
					buffs.addBuff(template.specialEffect);
				}
			}
			// 添加新装备后添加Buff
			if (template.suiteEffects != null
					&& ((old == null) || (old != null && old.template.equipment.suiteEffects != template.suiteEffects))) { // 如果套装效果一样就没必要加了
				SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
				GameItem[] gameItems = equipments.equs; // 得到player身上的装备
				for (SuiteEffect effect : suiteEffects) {
					Buff buff = effect.buff;
					int effectCount = effect.getCount();
					int count = 0;
					if (gameItems != null) {
						for (GameItem gameItem : gameItems) {
							if (gameItem != null
									&& gameItem.template.isEquipment()) {
								if (gameItem != null
										&& gameItem.template.equipment.suiteEffects != null
										&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
									count++;
								}
							}
						}
					}
					if (count == effectCount) { // 添加新装备后如果正好构成一个套装效果，则添加此套装效果
						buffs.addBuff(buff);
					}
				}
			}
			refreshStar7Buff();
			refreshProperties(false);
			// moveType |= MOVE_EQUIPMENT;
			Packet pt = new Packet(OpCode.EQUIP_SERVER);
			pt.putInt(serial);
			send(pt);
			if (clazz == Unit.CLASS_2
					&& Equipments.EQU_INDEXES[equ.template.equipment.minorType - 1] == Equipments.HAND) { // 如果是刺客并且更换的是武器，那么重发一遍自动攻击技能
				SkillChangedItem changedItem = new SkillChangedItem(skills
						.getSkill(1), false);
				changed.addChangedItem(changedItem);
			}
			
			// 刷新星辉状态
			refreshStarState();
			
			//玩家换装事件
			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_CHANGE_EQUIP,this));
			
			// 记录玩家动作
			addAction(Action.EQUIP);
		}
	}
	
	/** 刷新星辉状态 */
	public void refreshStarState(){
//		int oldValue = equipments.starState;
		int totalStar = getAveStar();
		int level = 0;
		if(totalStar>=8){
			equipments.starState = (1<<2 | 3);
			level = 3;
		}else if(totalStar>=6){
			equipments.starState = (1<<1 | 1);
			level = 2;
		}else if(totalStar>=4){
			equipments.starState = 1;
			level = 1;
		}else{
			equipments.starState = 0;
			level = 0;
		}
//		if(oldValue!=equipments.starState){
			refreshStarBuff(level);
//			addIntPropertyChangedItem(ChangedItem.STAR_BUFF,oldValue,equipments.starState,false);
//		}
	}
	
	public void refreshStar7Buff(){
		if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
			if(equipments.getFlashLevel()==6){
				addIntPropertyChangedItem(ChangedItem.STAR_7_BUFF,0,1,false);
			}else{
				addIntPropertyChangedItem(ChangedItem.STAR_7_BUFF,1,0,false);
			}
			if(equipments.getFlashLevel()==6 && buffs.getBuffByID(STAR_7_BUFF_ID)==null){
				buffs.addBuff(BuffUtil.createSuiteBuff(STAR_7_BUFF_ID, 1));
			}else if(equipments.getFlashLevel()!=6 && buffs.getBuffByID(STAR_7_BUFF_ID)!=null){
				buffs.removeBuff(STAR_7_BUFF_ID);
			}
		}
	}
	
	private void refreshStarBuff(int level){
		for(Buff buff : STAR_BUFFS){
			if(buff!=null)
				buffs.removeBuff(buff);
		}
		for(int effectLevel=1;effectLevel<=level;effectLevel++){
			Buff buff = STAR_BUFFS[effectLevel];
			if(buff==null){
				buff = BuffUtil.createSuiteBuff(STAR_BUFF[effectLevel], 1);
				STAR_BUFFS[effectLevel] = buff;
			}
			buffs.addBuff(buff);
		}
	}
	
	public int getAveStar(){
		int total = 0;
		int four = 4;
		int six = 6;
		int eight = 8;
		if(equipments==null || equipments.equs==null || equipments.equs.length==0)
			return total;
		int count = 0;
		for(GameItem item : equipments.equs){
			if(item!=null && item.template!=null && item.template.isEquipment())
				count++;
		}
		if(count<10)
			return 0;
		for(GameItem item : equipments.equs){
			if(item!=null && item.template!=null && item.template.isEquipment()){
				if(item.object!=null && item.object instanceof ItemEnhance){
					ItemEnhance ie = (ItemEnhance)item.object;
					if(ie.getStar()>=eight && eight==8){
						total = eight;
					}else if(ie.getStar()>=six && six==6){
						total = six;
						eight = 0;
					}else if(ie.getStar()>=four && four==4){
						total = four;
						six = 0;
						eight = 0;
					}else{
						return 0;
					}
				}else{
					return 0;
				}
			}
		}
		return total;
	}

	/**
	 * 套装信息查询
	 */
	public void suiteIndex(int serial, int itemId, int itemInstanceId, int type,
			int instanceId, int horseInstanceId) {

		int white = 0;
		int total = 0;
		ItemTemplate item = ObjectAccessor.getItemTemplate(itemId);
		if (item == null) {
			ErrorHandler.sendErrorMessage(session, serial, instanceId,
					"没有对应的套装信息");
			return;
		}
		EquipmentTemplate equipmentTemplate = item.equipment;
		if (equipmentTemplate == null
				|| (equipmentTemplate.suiteEffects == null && equipmentTemplate.specialEffect == null)) {
			ErrorHandler.sendErrorMessage(session, serial, instanceId,
					"没有对应的套装信息");
			return;
		}

		GameItem[] gameItems = null;
		if(type==0){
			//其他玩家
			if(instanceId>0){
				if(ObjectAccessor.getPlayer(instanceId)==null){
					ErrorHandler.sendErrorMessage(session, serial, instanceId,"玩家已经下线");
					return;
				}
				gameItems = ObjectAccessor.getPlayer(instanceId).equipments.equs; // 得到player身上的装备
			}else if(instanceId<0){
				//雕像
				if(Server.server.getServiceRegistry().getFameService().getStatue(instanceId)!=null){
					gameItems = Server.server.getServiceRegistry().getFameService()
					.getStatue(instanceId).equipments.equs;
				}else{
					gameItems = Server.server.getServiceRegistry().getDuelService()
					.getStatue(instanceId).equipments.equs;
				}
			}
		}else if(type==1){
			//玩家自己
			gameItems = equipments.equs;
		}else if(type==2){
			//随从
			if(attendantBag!=null && attendantBag.getAttendant(instanceId)!=null){
				gameItems = attendantBag.getAttendant(instanceId).equs;
			}
		}else if(type==3){
			//坐骑
			if(instanceId==id || instanceId==-1 || instanceId==0){
				Horse h = horseBag.getHorse(horseInstanceId);
				if(h!=null){
					gameItems = h.equs.equs;
				}
			}else{
				Player p = ObjectAccessor.getPlayer(instanceId);
				if(p==null)
					p = Server.server.getServiceRegistry().getFameService().getStatue(instanceId);
				if(p==null)
					p = Server.server.getServiceRegistry().getDuelService().getStatue(instanceId);
				if(p!=null){
					Horse h = null;
					if(p.id<0){
						if(p.horseBag!=null)
							h = p.horseBag.getHorse(horseInstanceId);
						if(h==null)
							h = p.horse;
					}else
						h = p.horseBag.getHorse(horseInstanceId);
					if(h!=null)
						gameItems = h.equs.equs;
				}
			}
		}
		if (gameItems == null) {
			ErrorHandler.sendErrorMessage(session, serial, instanceId, "没有对应的套装信息");
			return;
		}
		Packet pt = new Packet(OpCode.SUITE_SERVER);
		pt.putInt(serial);
		pt.putString(equipmentTemplate.specialEffect == null ? ""
				: equipmentTemplate.specialEffect.getDesc());
		if (equipmentTemplate.suiteEffects == null) {
			pt.putString("");
			pt.put(0);
			pt.putString("");
			pt.put(0);
		} else {
			pt.putString(equipmentTemplate.suiteEffects.getName());
			List<Integer> equips = equipmentTemplate.suiteEffects.getEquips();
			pt.put(equips.size());
			if (equips.size() != 0) {
				for (int equipId : equips) {
					total++;
					int a = 0;
					ItemTemplate item1 = ObjectAccessor
							.getItemTemplate(equipId);
					pt.putString(item1.name);
					for (GameItem gameItem : gameItems) {
						if (gameItem != null
								&& gameItem.template.id == item1.id) {
							white++;
							pt.put(1);
							a++;
						}
					}
					if (a == 0) {
						pt.put(0);
					}
				}
			}
			pt.putString(white + "/" + total);
			SuiteEffect[] effects2 = equipmentTemplate.suiteEffects
					.getEffects();
			pt.put(effects2.length);
			for (SuiteEffect effect : effects2) {
				int effectCount = effect.getCount();
				pt.putString(MessageFormat.format("套装({0}){1}", effectCount,effect.buff.getDesc()));
				int count = 0;
				for (GameItem gameItem : gameItems) {
					if (gameItem != null
							&& gameItem.template.isEquipment()
							&& gameItem.template.equipment.suiteEffects != null
							&& gameItem.template.equipment.suiteEffects == item.equipment.suiteEffects) {
						count++;
					}
				}
				if (count >= effectCount) {
					pt.put(1);
				} else {
					pt.put(0);
				}
			}

		}
		session.send(pt);
	}

	public void uiNotify(int questId, int id, int type, int answer) {
		UINotify notify = new UINotify(questId, id, type, answer);
		uiNotifies.add(notify);
	}

	public boolean hasUINotify(int questId, int id, int type) {
		for (UINotify notify : uiNotifies) {
			if (notify.questId == questId && notify.type == type
					&& notify.id == id)
				return true;
		}
		return false;
	}

	public boolean hasUINotify(int questId, int id, int type, int answer) {
		for (UINotify notify : uiNotifies) {
			if (notify.questId == questId && notify.type == type
					&& notify.id == id && notify.answer == answer)
				return true;
		}
		return false;
	}

	public void loadFinished() {
		if (systemState != SYSTEMSTATE_READY) {
			setSystemState(SYSTEMSTATE_READY);
			loadTimes++;
			Packet pt = new Packet(OpCode.VIEW_ACCEPT_SERVER);
			send(pt);
			if (loadTimes == 1) {
				serivce.firstLoad(this);
			}
			map.playerLoadingFinished(this);
		}
		acceptMoving = true;
		if (lastPosition == null) {
			lastPosition = new Position(map.id, x, y, Time.currTime, -1, getSpeed());
		}
		
		// 进入新场景，强制刷新速度
		float speed = this.speedRating;
		this.speedRating = 0;
		this.setSpeedRatio(speed);
		// lastPosition = new Position(map.id,x,y,Time.currTime,Time.currTime);
	}

	public void clearCycle() {
		touchedNpc.clear();
		uiNotifies.clear();
		kills.clear();
		killPlayers.clear();
		cds = null;
		questToFinishQuestId = -1;
		lastSkillId = -1;
		lastItemId = -1;
		dieCause = 0;
	}

	public void sendBagInfo() {
		Packet pt = new Packet(OpCode.BAG_SERVER);
		pt.put(bag.getSize());
		for (TransactionBagGrid grid : bag.grids) {
			pt.put(grid.toClientByte());
		}
		send(pt);
	}

	public void sendSkillList() {
		// Packet pt = new Packet(OpCode.SKILL_LIST_SERVER);
		// Collection<Skill> ss = skills.getSkills();
		// pt.put(ss.size());
		// for (Skill skill : ss) {
		// pt.put(skill.toClientBytes(this));
		// }
		// send(pt);

		// bookskill
		Packet pt = new Packet(OpCode.SKILL_LIST_SERVER);
		pt.put(skills.toClientBytes(this));
		send(pt);
	}

	public void addPropertyPoint(int strength, int agility, int stamina,
			int intellect, int serial) {
		if (strength + agility + stamina + intellect > propertyPoint) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.PROPERTYPOINT_ADD_CLIENT, "没有足够点数");
		} else {
			setPropertyPoint(this.propertyPoint - strength - agility - stamina
					- intellect, false);
			strengthAdded += strength;
			agilityAdded += agility;
			staminaAdded += stamina;
			intellectAdded += intellect;

			refreshProperties(false);
			Packet pt = new Packet(OpCode.PROPERTYPOINT_ADD_SERVER);
			pt.put(serial);
			send(pt);
			
			// 记录玩家动作
			addAction(Action.ADD_PROPERTY_POINT);
		}
	}
	
	public void refreshPropertiesPoint(){
		setPropertyPoint(level*2,false);
		strengthAdded = 0;
		agilityAdded = 0;
		staminaAdded = 0;
		intellectAdded = 0;
		refreshProperties(false);
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_SKILL_REFRESH,this));
	}

	public void addSkill(Skill skill) {
		Skill oldSkill = skills.removeSkill(skill.getGroupId(), skill
				.getLevel() - 1);
		skills.addSkill(skill, changed, true);
		if (oldSkill != null) {
			buffs.removeBuff(oldSkill.getId());
		}
		if (skill.getLevel() > 0) {
			Buff b = skill.newBuff();
			if (b != null) {
				buffs.addBuff(b);
			}
			b = skill.getAreaBuff();
			if (b != null)
				buffs.addBuff(b);
		}
	}
	

	public void addBookSkill(Skill skill) {
		Skill oldSkill = skills.removeBookSkill(skill.getGroupId(), skill
				.getLevel() - 1);
		skills.addBookSkill(skill, changed, true);
		if (oldSkill != null) {
			buffs.removeBuff(oldSkill.getId());
		}
		if (skill.getLevel() > 0) {
			Buff b = skill.newBuff();
			if (b != null) {
				buffs.addBuff(b);
			}
			b = skill.getAreaBuff();
			if (b != null)
				buffs.addBuff(b);
		}
	}

	public void addSkillPoint(int skillGroupId, int level, boolean notify,
			int serial) {
		if (level <= 0)
			return;
		Skill oldSkill;
		if ((oldSkill = skills.getSkill(Skills.getSkillId(skillGroupId,
				level - 1))) != null) { // 必须要有前一级的技能
			Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(
					skillGroupId, level));
			if (skill != null) {
				if (skill.getRequireLevel() > this.level) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.SKILL_ADDPOINT_CLIENT, 
							MessageFormat.format("需要达到{0}级才能学习此技能", skill.getRequireLevel()));
				} else {
					if (skillPoint < skill.getPoint()) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.SKILL_ADDPOINT_CLIENT, "没有足够技能点");
					} else {
						setSkillPoint(skillPoint - skill.getPoint(), true);
						addSkill(skill);
						Packet pt = new Packet(OpCode.SKILL_ADDPOINT_SERVER);
						pt.putInt(serial);
						pt.put(skill.toClientBytes(this));
						send(pt);
						
						// 记录玩家动作
						addAction(Action.UPGRADE_SKILL);
						if ((skill.getType() & Skill.TYPE_VISIBLE) != 0) {
							addAction(Action.UPGRADE_ACTIVE_SKILL);
						} else {
							addAction(Action.UPGRADE_PASSIVE_SKILL);
						}
					}
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.SKILL_ADDPOINT_CLIENT, "没有足够的点数");
			}
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SKILL_ADDPOINT_CLIENT, "不能升级此项技能");
		}
	}
	
	public void refreshSkillPoint(int serial) {
		Collection<Skill> skills = super.skills.getSkills();
		super.skills.clear();
		setSkillPoint(PlayerUtil.getGrowSkillPoint(1, level), false);
		buffs.removeAreaBuffs();
		if (party != null) {
			List<Buff> l = party.getAreaBuffs(map.map);
			if (l.size() > 0) {
				for (Buff buff : l) {
					buffs.addBuff(buff);
				}
			}
		}
		for (Skill skill : skills) {
			Buff newBuff = skill.newBuff();
			if (newBuff != null) {
				buffs.removeBuff(newBuff.getId());
			}
		}
		List<Skill> newSkills = ObjectAccessor.getPlayerInitSkills(clazz);
		for (Skill skill : newSkills) {
			if (skill.getId() == 1) { // 如果是自动攻击技能加上
				addSkill(skill);
			} else {
				if (skill.isAutoLearn()
						&& level >= skill.getNextLevel().getRequireLevel()) { // 如果需要学习，并且达到学习条件，加上
					addSkill(skill.getNextLevel());
				} else {
					addSkill(skill);
				}
			}
		}
		refreshProperties(false);
		Packet pt = new Packet(OpCode.SKILL_REFRESH_SERVER);
		pt.putInt(serial);
		send(pt);
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_SKILL_REFRESH,this));
	}

	public void refreshSkillPointWithRule(int serial) {
		int times = pool.getInt(PROPERTY_REFRESH_SKILL);
		int money = PlayerUtil.getRefreshSkillMoney(times);
		PlayerTransaction tx = newTransaction("RSP");
		try {
			decMoney(money, tx, true);
			tx.commit();
			refreshSkillPoint(serial);
			pool.setInt(PROPERTY_REFRESH_SKILL, times + 1);
		} catch (NoEnoughValueException e) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SKILL_REFRESH_CLIENT, "没有足够的金钱");
		}
	}

	public void sendQuestList() {
		Packet pt = new Packet(OpCode.QUEST_LIST_SERVER);
		Collection<ASMQuest> quests = asmVm.getQuests();
		pt.put(quests.size());
		for (ASMQuest quest : quests) {
			pt.putInt(quest.getId());
			pt.putInt(quest.getGameQuest().getStartNpc());
			pt.putInt(quest.getGameQuest().getFinishNpc());
			pt.put(quest.getGameQuest().getLevel());
			pt.putString(quest.getGameQuest().getName());
			pt.put(quest.getGameQuest().getClientETF());
			pt.putInts(asmVm.getQuestStore(quest.getId()));
			List<QuestTarget> l = quest.getGameQuest().getTargets();
			pt.put((byte) l.size());
			for (QuestTarget target : l) {
				pt.putString(target.description);
			}
			pt.put(asmVm.isFail(quest.getId()) ? 1 : 0);
		}
		send(pt);
	}
	
	public Attack getAutoAttack(){
		if(autoAttack!=null){
			return autoAttack.attack;
		}
		return null;
	}
	
	protected void removeUnitEffectBuffState(){
		unFear();
		unDumb();
		unParalyze();
		unStay();
	}
	
	public byte[] toClientBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(id);
			dos.writeUTF(name);
			dos.write(sex);
			dos.write(level);
			dos.write(clazz);
			dos.write(faction);
			dos.writeShort(maxhp);
			dos.writeShort(maxmp);
			dos.writeShort(hp);
			dos.writeShort(mp);
			dos.writeShort(strength);
			dos.writeShort(agility);
			dos.writeShort(stamina);
			dos.writeShort(intellect);
			dos.writeShort(Math.round(attackpowerup));
			dos.writeShort(Math.round(attackpowerdown));
			dos.writeShort(Math.round(spellpower));
			dos.writeShort(Math.round(spellheal));
			dos.writeShort(Math.round(defense));
			dos.writeShort(Math.round(spelldefense));
			dos.writeShort(Math.round(critical * 100));
			dos.writeShort(Math.round(spellcritical * 100));
			dos.writeShort(Math.round(hit * 100));
			dos.writeShort(Math.round(spellhit * 100));
			dos.writeShort(Math.round(dodge * 100));
			dos.writeShort(Math.round(spelldodge * 100));
			dos.writeShort(Math.round(anticrit * 100));
			dos.writeShort(Math.round(defensePercent * 100));
			dos.writeShort(healthrestore);
			dos.writeShort(manarestore);
			dos.writeShort(skillPoint);
			dos.writeShort(propertyPoint);
			dos.writeInt(exp);
			dos.writeInt(PlayerUtil.getUpLevelExp(level, level + 1));
			dos.writeInt(money);
			dos.writeShort(map.id);
			if (getVMap() == null) {
				dos.writeInt(-1);
			} else {
				dos.writeInt(getVMap().getInstanceId());
			}
			dos.writeShort(x);
			dos.writeShort(y);
			dos.writeShort(direct);
			removeUnitEffectBuffState();
			dos.writeShort(state);
			dos.writeInt(credit);
			dos.writeUTF(getCreditString());
			dos.writeUTF(getGuildName());
			dos.write(equipments.toClientBytes());
			dos.writeInt(equipments.getHeadScore(level, clazz));
			dos.writeInt(equipments.getBodyScore(level, clazz));
			dos.writeInt(equipments.getWeaponScore(level, clazz));
			dos.write(equipments.getFlashLevel());
			dos.write(chatOptions.toClientBytes());
			dos.write(coolDowns.toClientBytes());
			dos.write(buffs.toClientBytes());
			dos.writeInt(honor);
			dos.writeUTF(titles.getCurrentTitle() == null ? "" : titles
					.getCurrentTitle().name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	public void setSkillPoint(int skillPoint, boolean notify) {
		if (this.skillPoint != skillPoint) {
			addIntPropertyChangedItem(ChangedItem.SKILLPOINT, this.skillPoint,
					skillPoint, notify);
			this.skillPoint = skillPoint;
		}
	}

	public void setPropertyPoint(int propertyPoint, boolean notify) {
		if (this.propertyPoint != propertyPoint) {
			addIntPropertyChangedItem(ChangedItem.PROPERTYPOINT,
					this.propertyPoint, propertyPoint, notify);
			this.propertyPoint = propertyPoint;
		}
	}

	public void addGainComplete(Gain gain, PlayerTransaction tx, boolean notify)
			throws NoEnoughSpaceException {
		bag.addGainComplete(gain, tx, notify);
		if (gain.getMoney() > 0)
			addMoney(gain.getMoney(), tx, notify);
		// levellimit
		// levellimit
		if (gain.getExp() > 0 && level < MAX_LEVEL) {
			addExp(gain.getExp(), tx, notify);
			// setExp(exp + gain.getExp(), notify);
		}
		if (gain.getCredit() > 0) {
			addCredit(gain.getCredit(), tx, notify);
			// setCredit(credit + gain.getCredit(), notify);
		}
		if (gain.getHonor() > 0) {
			addHonor(gain.getHonor(), tx, notify);
		}
	}

	public void addGain(Gain gain, PlayerTransaction tx, boolean notify) {
		bag.addGain(gain, tx, notify);
		if (gain.getMoney() > 0) {
			addMoney(gain.getMoney(), tx, notify);
		}
		// levellimit
		if (gain.getExp() > 0 && level < MAX_LEVEL) {
			int gainExp = (int) (gain.getExp() * getExpRatio());
			addExp(gainExp, tx, notify);
			// setExp(exp + gain.getExp(), notify);
		}
		if (horse != null&&horse.level<level) {
			int gainExp = (int) (gain.getExp() * 0.12f * getHorseExpRatio());
			if (gainExp > 0&&horse.level<MAX_LEVEL)
				horse.setExp(horse.exp + gainExp, this, tx.getCause());
		}
		if (gain.getCredit() > 0) {
			int addCredit = (int) (gain.getCredit() * getRewardRation());
			addCredit(addCredit, tx, notify);
			// setCredit(credit + gain.getCredit(), notify);
		}
		if (gain.getHonor() > 0) {
			addHonor(gain.getHonor(), tx, notify);
		}
	}
	
	public float getHorseExpRatio(){
		Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(faction);
		NationSkill3 skill = (NationSkill3)nation.skills.get(3);
		if(skill!=null)
			return horseExpRatio + skill.getExpRatio();
		return horseExpRatio;
	}
	
	public float getExpRatio(){
		Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(faction);
		NationSkill6 skill = (NationSkill6)nation.skills.get(6);
		if(skill!=null)
			return 1 + skill.getExpRatio();
		return 1;
	}
	
	public float getRewardRation(){
		return rewardRation;
	}

	public void addExp(int value, PlayerTransaction tx, boolean notify) {
		if (value > 0) {
//			log.info("[ADDEXP]" + LogUtil.getPlayerLogString(this) + "COUNT["
//					+ value + "]CURRENTEXP[" + exp + "]TRY");
			expTx.add(value, tx, notify);
		}
	}

	public void addCredit(int value, PlayerTransaction tx, boolean notify) {
		if (value > 0) {
//			log.info("[ADDCREDIT]" + LogUtil.getPlayerLogString(this)
//					+ "COUNT[" + value + "]CURRENTCREDIT[" + credit + "]TRY");
			creditTx.add(value, tx, notify);
		}
	}

	public void decCredit(int value, PlayerTransaction tx, boolean notify)
			throws NoEnoughValueException {
		if (value > 0) {
//			log.info("[DECCREDIT]" + LogUtil.getPlayerLogString(this)
//					+ "COUNT[" + value + "]CURRENTCREDIT[" + credit + "]TRY");
			creditTx.dec(value, tx, notify);
		}
	}

	public void addHonor(int value, PlayerTransaction tx, boolean notify) {
		if (value > 0) {
//			log.info("[ADDHONOR]" + LogUtil.getPlayerLogString(this) + "COUNT["
//					+ value + "]CURRENTHONOR[" + honor + "]TRY");
			honorTx.add(value, tx, notify);
		}
	}

	public void decHonor(int value, PlayerTransaction tx, boolean notify)
			throws NoEnoughValueException {
		if (value > 0) {
//			log.info("[REMOVEHONOR]" + LogUtil.getPlayerLogString(this)
//					+ "COUNT[" + value + "]CURRENTHONOR[" + honor + "]TRY");
			honorTx.dec(value, tx, notify);
		}
	}

	public void addMoney(int value, PlayerTransaction tx, boolean notify) {
		int oldMoney = money;
		if (value > 0) {
//			log.info("[GETMONEY]" + LogUtil.getPlayerLogString(this) + "COUNT["
//					+ value + "]BALANCE[" + money + "]TRY");
			moneyTx.add(value, tx, notify);
			StatService service = Server.server.getServiceRegistry()
			.getStatService();
	        PvpInfo pvpInfo = service.getPvpInfo(this.id, this.faction);
	        if ((oldMoney + value >= 10000 && oldMoney < 10000 && pvpInfo.pool
			        .getLong(StatService.PROPERTY_MONEY_WAN, 0) != 1)
			        || (oldMoney < 1000000 && (oldMoney + value) >= 1000000 && pvpInfo.pool
					.getLong(StatService.PROPERTY_MONEY_MILLIONARE, 0) != 1)) {
		         fireEvent(money, value);
	        }
		}
	}
	
	protected void fireEvent(int oldMoney, int newMoney){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent
				.EVENT_PLAYER_MONEY_UP,this,oldMoney,newMoney));
	}

	public void addActivePower(int value) {
		if (value > 0) {
			setActivePower(Math.min(this.activePower + value, 100));
		}
	}
	
	public void decMoney(int value, PlayerTransaction tx, boolean notify)
			throws NoEnoughValueException {
		if (value > 0) {
//			log.info("[REMOVEMONEY]" + LogUtil.getPlayerLogString(this)
//					+ "COUNT[" + value + "]BALANCE[" + money + "]TRY");
			moneyTx.dec(value, tx, notify);
		}
	}

	public void decActivePower(int value) throws NoEnoughValueException {
		if (value > 0) {
			if(value <= this.activePower){
				setActivePower(this.activePower - value);
			}else{
				throw new NoEnoughValueException();
			}
		}
	}
	
	public void setFlag(Flag flag) {
		if (this.flag != null) {
			this.flag.unbind(this);
		}
		this.flag = flag;
		if (this.flag == null) {
			state &= (~GameObject.STATE_FLAG);
		} else {
			flag.bind(this);
			state |= GameObject.STATE_FLAG;
		}
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void pvp(int time) {
		super.pvp();
		if (time == 0)
			this.pvpTime = 0;
		else
			this.pvpTime = Time.currTime + time;
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void unPvp() {
		super.unPvp();
		this.pvpTime = 0;
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void pvpFaction() {
		super.pvpFaction();
//		if (time == 0)
//			this.pvpFactionTime = 0;
//		else
//			this.pvpFactionTime = Time.currTime + time;
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void unPvpFaction() {
		super.unPvpFaction();
//		this.pvpFactionTime = 0;
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void dumb() {
		super.dumb();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
		breakAttack();
	}

	public void unDumb() {
		super.unDumb();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void fear() {
		super.fear();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
		breakAttack();
	}

	public void unFear() {
		super.unFear();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}
	
	public void setKing(){
		super.setKing();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE,state,false,true);
	}
	
	public void unKing(){
		super.unKing();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE,state,false,true);
	}

	public void paralyze() {
		super.paralyze();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
		breakAttack();
	}

	public void unParalyze() {
		super.unParalyze();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void stay() {
		super.stay();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void unStay() {
		super.unStay();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void setExp(int exp, boolean notify, String cause) {
		if (this.exp != exp) {
			LogUtil.logGetExp(this, this.exp, exp, cause);
			int upLevel = PlayerUtil.getUpLevel(level, exp);
			addIntPropertyChangedItem(ChangedItem.GAINEXP, exp - this.exp,
					notify, false);
			if (upLevel > 0) {
				int oldLevel = level;
				int oldExp = exp;
				int newLevel = level + upLevel;
				exp -= PlayerUtil.getUpLevelExp(level, newLevel);
				this.exp = exp;
				addIntPropertyChangedItem(ChangedItem.EXP, this.exp, false,
						true);
				int skillPoint = PlayerUtil.getGrowSkillPoint(this.level,
						newLevel);
				setSkillPoint(this.skillPoint + skillPoint, notify);
				setLevel(newLevel, notify);
//				if (oldLevel <= MAX_PVE_LEVEL && newLevel > MAX_PVE_LEVEL) { // 设置pvp状态
//					if (pvpFactionTime != 0 || (!isPvpFaction())) {
//						pvpFaction(0);
//					}
//				}
				setPropertyPoint(propertyPoint + upLevel * 2, notify);
				int upExp = PlayerUtil
						.getUpLevelExp(this.level, this.level + 1);
				addIntPropertyChangedItem(ChangedItem.UPLEVELEXP, upExp, false,
						true);
				int newBagSize = 27 + level / 5;
				if (newBagSize > bag.size) {
					bag.extend(newBagSize,false);
				}
				refreshProperties(true);
				serivce.notifyPlayerUpLevel(oldLevel, this);
				LogUtil.logLevelUp(this, oldLevel, oldExp, this.level, this.exp);
			} else {
				this.exp = exp;
				addIntPropertyChangedItem(ChangedItem.EXP, this.exp, false,
						true);
			}
		}
	}

	public void abandonQuest(int questId, int serial) {
		GameQuest quest = ASMQuestUtil.getGameQuest(questId);
//		if(quest != null && quest.getCycleInfo() != null){
//			ErrorHandler.sendErrorMessage(session, serial,
//					OpCode.QUEST_ABANDON_CLIENT, "不能放弃环任务");
//			return;
//		}
		if (asmVm.abandonQuest(questId) == 0) {
			Packet pt = new Packet(OpCode.QUEST_ABANDON_SERVER);
			pt.putInt(serial);
			pt.putInt(questId);
			send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.QUEST_ABANDON_CLIENT, "没有此任务");
		}
	}

	public void initBuffs() {
		addSkillBuffs();
		addTitleBuffs();
		addHorseBuffs();
		addPlayerSuiteEquipmentBuffs();
		addHorseSuiteEquipmentBuffs();
		refreshStarState();
		refreshStar7Buff();
	}

	public void loadInit(boolean refreshBuffs) {
		pvpKilledTimes = 0;
		pvp2pveMapId =  0;
		lastPosition = null;
		loadTimes = 0;
		asmVm.clear();
		moveType = 0;
		if (level > MAX_PVE_LEVEL) {
			setWarState(Player.PVPSTATE);
		}else{
			setWarState(Player.PVESTATE);
		}
		logouted = false;
		if(refreshBuffs){
			long l = getLastLogoutElapseTime();
			int value = 0;
			if(l>Integer.MAX_VALUE){
				value = Integer.MAX_VALUE;
			}else{
				value = (int)l;
			}
			buffs.update2(value);
		}
		refreshProperties(false);
		changed.clean();
	}
	
	public void refreshTitles(){
		Title currentTitle = titles.getCurrentTitle();
		Title currentEquipTitle = titles.getCurrentEquipTitle();
		titles.removeCurrentTitle(false);
		titles.removeCurrentTitle(true);
		if(currentTitle!=null)
			titles.changeShowTitle(currentTitle.id);
		if(currentEquipTitle!=null)
			titles.changeEquipTitle(currentEquipTitle.id);
		if(currentTitle==null && currentEquipTitle==null)
			return;
		refreshProperties(false);
	}
	
	public void refreshAllAttendants(){
		attendantBag.addHpAndMp();
	}

	@Override
	public GameObjectRef ref() {
		if (instanceId == 0||type == 0) {
			instanceId = id;
			type = GameObject.TYPE_PLAYER;
//			log.info("[INSTANCEIDERROR]" + LogUtil.getPlayerLogString(this));
//			Thread.dumpStack();
		}
		if (lastRef == null) {
			lastRef = new GameObjectRef(type, id, instanceId);
		}
		return lastRef;
	}

	@Override
	public Packet getInfoPacket() {
		Packet pt = new Packet(OpCode.UNIT_INFO_SERVER);
		pt.putInt(instanceId);
		pt.putString(getGuildName());
		pt.putString(getCreditString());
		pt.putString(titles.getCurrentTitleString());
		return pt;
	}

	@Override
	public Packet getMovePacket(short moveType) {
		moveType &= ~MOVE_OWNER;
		return super.getMovePacket(moveType);
	}

	public void logined() {
		setSystemState(SYSTEMSTATE_LOGINED);
		serivce.logined(this);
		
		lastLoginTime = Time.currDate;
//		lastLogoutTime = lastLoginTime;
		antiPlug = new AntiPlug(this);
		
		// 记录日志
		LogUtil.logLoginOK(this);
		Server.server.getServiceRegistry().getRealtimeStatService().loginCounter++;
		notifyMate();
	}
	
	protected void notifyMate(){
		RelationService service = Server.server.getServiceRegistry().getRelationService();
		PlayerRelation relation = service.get(id);
		if(relation!=null && relation.mateId>0){
			Player mate = ObjectAccessor.getPlayer(relation.mateId);
			if(mate!=null){
				if(mate.sex==0){
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(
							relation.mateId, MessageFormat.format("您的爱妻{0}已上线，壮士还不策马而去，与爱妻携手游戏生涯。", name));
				}else{
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(
							relation.mateId, MessageFormat.format("您的夫君{0}已上线，美女还不策马而去，与夫君携手游戏生涯。", name));
				}
			}
		}
	}
	
	public void cancelExchange() {
		if (exchange != null) {
			if(exchange.state==Exchange.STATE_INIT){
				exchange.complete(this, false);
				exchange = null;
				return;
			}
			Gain gain = new Gain(this);
			exchange.restoreToGain(id, gain);
			PlayerTransaction tx = newTransaction("EXCC");
			addGain(gain, tx, false);
			tx.commit();
			int otherId = exchange.getOtherId(id);
			Player p = ObjectAccessor.getPlayer(otherId);
			if (p != null) {
				gain = new Gain(p);
				exchange.restoreToGain(p.id, gain);
				tx = p.newTransaction("EXCC");
				p.addGain(gain, tx, false);
				tx.commit();
			}
			exchange.complete(this, false);
			if(p != null)
				p.exchange = null;
			exchange = null;
		}
	}

	public void removeFromWorld() {
		lastLogoutTime = new Date();
		autoAttack.clear();
		if (party != null) {
			party.leave(id);
		}
		cancelExchange();
		clearThreats();
		if (!isAlive()) {
			if(reliveOptions!=null)
				relive(reliveOptions.getFirstOption());
			else{
				relive(maxhp/2,maxmp/2);
				log.info("[REMOVEFROMWORLDERROR]"+LogUtil.getPlayerLogString(this));
			}
		}
		//下马，但不改变PROPERTY_LAST_HORSE_INSTANCEID
		if (horse != null) {
			Horse h = horse;
			horse = null;
			h.unRide(this);
			unRide();
			Packet pt = new Packet(OpCode.HORSE_UNRIDE_SERVER);
			pt.putInt(-1);
			send(pt);
		}
		
		gather = null;
		removeFromMap();
		ObjectAccessor.removeGameObject(this);
		setSystemState(Player.SYSTEMSTATE_LOAD);
		unMoving();
		// acceptMoving = false;
		rollbackNotCommiteds();
		Server.server.eventManager.fireEvent(new ServiceEvent(
				ServiceEvent.EVENT_PLAYER_LOGOUTED, this));
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		dbService.schedule(new LogoutCall(dbService, session, this));
		OnlineTimeNotifyMessage msg = new OnlineTimeNotifyMessage(accountId,
				(int) (System.currentTimeMillis() - loginTime));
		Server.server.getServiceRegistry().getAccountService().postMessage(msg);
		if(antiPlug!=null){
			antiPlug.clear();
			antiPlug = null;
		}
		//随从下线
		if(attendant!=null){
			pool.setInt(PROPERTY_LAST_ATTENDANT_INSTANCEID, attendant.instanceId);
			attendant.cancelFollow();
		}
		LogUtil.logLogouted(this, System.currentTimeMillis() - loginTime);
		long time = pool.getLong(StatService.PROPERTY_ONLINE_TIME,0l);
		time += System.currentTimeMillis() - loginTime;
		pool.setLong(StatService.PROPERTY_ONLINE_TIME, time);
		isFindPath = 0;
		Server.server.getServiceRegistry().getRealtimeStatService().logoutCounter++;
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_UNLOADED, this));
	}
	
	/**
	 * 返回从上次退出游戏到现在的时间（以毫秒计算）
	 * @return
	 */
	public long getLastLogoutElapseTime(){
		 long ret = lastLogoutTime==null?0L:(System.currentTimeMillis() - lastLogoutTime.getTime());
		 if(ret<0) ret = 0;
		 return ret;
	}

	public void logout() {
		LogUtil.logDisconnected(this);
		session = null;
		this.loadTimes = 0;
		if (threats.count == 0) {
			removeFromWorld();
		} else {
			setSystemState(Player.SYSTEMSTATE_DISCONNECTED);
		}
		buffs.removeUnitEffectBuffState();
		Server.server.getServiceRegistry().getDbService().schedule(new AccountDepotSaveCall(session, accountId));
	}

	public void attack(CombatContext cc) {
		if (cc.isDamage() && cc.hited()) {
			equipments.decWeaponDuration(1, 20);
		}
	}

	public void attacked(CombatContext cc) {
		if (cc.hited()) {
			if(cc.damageType == CombatContext.DAMAGE_PHYSICAL)
				equipments.decAllArmorDuration(1, 20);
			if(cc.damageType == CombatContext.DAMAGE_PHYSICAL||cc.damageType == CombatContext.DAMAGE_MAGIC)
				cancelGather(2);
		}
		if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
			if (this != cc.source && !pkInfo.in(cc.source.ref())) {
				Server.server.eventManager.fireEvent(new ServiceEvent(
						ServiceEvent.EVENT_PLAYER_PK_ATTACKED, this));
			}

		}
	}

	/**
	 * 把一个和本对象有关的信息包广播给周围玩家。
	 * @param pt 信息包
	 * @param p 生成此信息包的源玩家，如果此信息不是来自玩家，此参数传null
	 * @param target 此信息包作用的目标玩家，如果此目标不是玩家，此参数传null
	 * @param self 是否发送给玩家自己
	 * @param ingoreParty 是否不发送给队友（队友广播通过特殊接口发送）
	 * @param isAttack 是否是攻击包
	 */
	@Override
	public void broadcast(Packet pt, Player p, Player target, boolean self,
			boolean ingoreParty, boolean isAttack) {
		if (mapCell != null) {
			if (p == null)
				p = this;
			
			// 如果广播时忽略队友，则在这里强行向所有队友广播此消息
			if (ingoreParty && party != null) {
				party.broadcast(pt, p);
			}
			
			// 向CELL及相邻CELL其他玩家广播
			mapCell.broadcast(p, target, pt, self, ingoreParty, isAttack);
		}
	}

	public void unMoving() {
		breakAllActions();
		acceptMoving = false;
		lastPosition = null;
		nextDistance = 0;
	}

	@Override
	public void goMap(int mapId, int x, int y) throws VMapException {
		if (map.map.getId() != mapId) {
			ForceGoMapCall call = new ForceGoMapCall(this, mapId, x, y);
			Server.server.getWorld().schedule(call);
		} else {
			unMoving();
			// acceptMoving = false;
			move(x, y);
			if(getVMap().instance != null){
				getVMap().instance.loadingFinished(this);
			}
			Packet pt = new Packet(OpCode.FORCE_GOMAP_SERVER);
			pt.putInt(map.map.getId());
			pt.putInt(map.map.getInstanceId());
			pt.putInt(x);
			pt.putInt(y);
			pt.put(map.map.allowFollow() ? 1 : 0);
			send(pt);
		}
	}

	/**
	 * 脱离卡死
	 */
	public void outPrison() {
		if (map.map != null) {
			map.map.manager.outPrison(this);
//		    int[] pos = map.map.mapDef.mapInfo.getPathFinder().tryOutPrison(x, y);
//		    if (pos == null) {
//		        // 已彻底卡死，回复活点
//				int[] relivePoint = map.map.getRelivePoint(faction);
//				try{
//				int oldMapId = map.map.getId();
//				int oldX = x;
//				int oldY = y;
//				goMap(relivePoint[0], relivePoint[1], relivePoint[2]);
//					Server.server
//							.getEventManager()
//							.fireEvent(
//									new ServiceEvent(
//											ServiceEvent.EVENT_PLAYER_OUTPRISON_RELIVEPOINT,
//											this,oldMapId,oldX,oldY));
//				} catch (VMapException e) {
//					//不应该被执行到
//					log.error(e,e);
//				}
//			} else {
//			    try {
//					goMap(map.map.getId(), pos[0], pos[1]);
//				} catch (VMapException e) {
//					//不应该被执行到
//					log.error(e,e);
//				}
//			}
		}
	}

	public void exchangeGrid(int sourceId, int targetId, int serial) {
		boolean ret = bag.exchange(sourceId, targetId, true);
		if (ret) {
			Packet pt = new Packet(OpCode.GRID_EXCHANGE_SERVER);
			pt.putInt(serial);
			send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial, serial, "移动物品失败");
		}
	}

	public void exchangeRefuse(int serial, String msg) {
		Packet pt = new Packet(OpCode.EXCHANGE_INVIT_REFUSE_SERVER);
		pt.putInt(serial);
		pt.putString(msg);
		send(pt);
	}

	public void removeTitle(int serial, int titleId) {
		if (titleId > 0) {
			Title t = titles.removeTitle(titleId);
			if(t!=null){
				log.info("[TITLEREMOVE]"+LogUtil.getPlayerLogString(this)+"TITLE["+t.id+"]");
			}
			// if(t!=null){
			// super.addIntPropertyChangedItem(ChangedItem.REMOVE_TITLE, t.id,
			// false);
			// }
			Packet pt = new Packet(OpCode.TITLE_REMOVE_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
		}
	}

	/**
	 * 装备或卸下称号
	 * @param serial
	 * @param titleId
	 */
	public void changeTitle(int serial, int titleId) {
		if (titleId > 0) {
			titles.changeEquipTitle(titleId);
			Packet pt = new Packet(OpCode.TITLE_SET_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
			
			// 记录玩家动作
			addAction(Action.EQUIP_TITLE);
		} else {
			titles.removeCurrentTitle(false);
			Packet pt = new Packet(OpCode.TITLE_SET_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
		}
	}
	
	/**
	 * 展示或隐藏称号
	 * @param serial
	 * @param titleId
	 */
	public void changeShowTitle(int serial,int titleId){
		if(titleId>0){
			titles.changeShowTitle(titleId);
			Packet pt = new Packet(OpCode.TITLE_SHOW_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
			// 记录玩家动作
			addAction(Action.EQUIP_TITLE);
		} else {
			titles.removeCurrentTitle(true);
			Packet pt = new Packet(OpCode.TITLE_SHOW_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
		}
		
	}

	public boolean addTitle(Title t) {
		if (titles.hasTitle(t.id)) {
			return false;
		} else {
			titles.addTitle(t);
			log.info("[TITLEADD]"+LogUtil.getPlayerLogString(this)+"TITLE["+t.id+"]");
			AddTitleChangedItem c = new AddTitleChangedItem(t, true);
			changed.addChangedItem(c);
			return true;
		}
	}

	public void forgetSkill(int serial, int skillId) {
		Skill skill = skills.getBookSkill(skillId);
		if (skill == null) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.FORGET_SKILL_CLIENT, "没有找到指定技能");
			return;
		}
		if (skills.removeBookSkill(skillId) != null) {
			if (skill != null) {
				buffs.removeBuff(skill.getId());
			}
			Packet pt = new Packet(OpCode.FORGET_SKILL_SERVER);
			pt.putInt(serial);
			pt.putInt(skillId);
			send(pt);
		}
	}

	public int getShowOption(){
		if(config == null)
			return 0;
		if(config.length < 21)
			return 0;
		return config[20];
	}
	/**是否允许交易邀请**/
	public boolean isAllowTrade(){
		if(config == null || config.length < 89 || config[88] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否允许PK邀请**/
	public boolean isAllowPK(){
		if(config == null || config.length < 93 || config[92] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否允许军团邀请**/
	public boolean isAllowGuild(){
		if(config == null || config.length < 97 || config[96] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否接收军团聊天**/
	public boolean isAllowGuildChat(){
		if(config == null || config.length < 101 || config[100] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否接收国家聊天**/
	public boolean isAllowFactionChat(){
		if(config == null || config.length < 105 || config[104] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否接收组队邀请**/
	public boolean isAllowParty(){
		if(config == null || config.length < 109 || config[108] == 0){
			return true;
		}else{
			return false;
		}
	}
	
	
//	public boolean onlyShowPartyAttack() {
//		if (config == null)
//			return false;
//		if (config.length < 5)
//			return false;
//		return config[20] == 1;
//	}
	
	public void setWarState(WarState state){
		if(warState!=null){
			warState.exit(this);
		}
		warState = state;
		warState.enter(this);
	}
	
	public int getTodayInstanceTimes(int instanceId){
		int day = pool.getInt(PROPERTY_INSTANCE_DAY+instanceId, 0);
		if(day==0||day!=Time.day){
			return 0;
		}else{
			return pool.getInt(PROPERTY_INSTANCE_TIMES+instanceId,0);
		}
	}
	
	public void setTodayInstanceTimes(int instanceId,int times){
		pool.setInt(PROPERTY_INSTANCE_DAY+instanceId, Time.day);
		pool.setInt(PROPERTY_INSTANCE_TIMES+instanceId, times);
	}
	
	public static int KEYBOARD_TYPE_NO_KEYS = 0;
	public static int KEYBOARD_TYPE_TRADITIONAL = 1;
	public static int KEYBOARD_TYPE_FULL_KEYS = 2;
	/**
     * 取得此帐号所用机型的键盘类型。
     * @return 0 - 无键盘，1 - 数字键盘，2 - 全键盘
     */
	public int getKeyboardType() {
	    if (session == null) {
	        return 1;
	    }
	    Account acc = (Account)session.getIdentity();
	    if (acc == null) {
	        return 1;
	    } else {
	        return acc.getKeyboardType();
	    }
	}
	
	/**
     * 取得此帐号所用机型的鼠标类型。
     * @return 0 - 无指点设备，1 - 触摸屏，2 - 鼠标
     */
	public int getMouseType() {
        if (session == null) {
            return 1;
        }
        Account acc = (Account)session.getIdentity();
        if (acc == null) {
            return 0;
        } else {
            return acc.getMouseType();
        }
	}
	
	/**
	 * 保存玩家当前位置到指定变量
	 * @param varName
	 */
	public void savePosition(String varName) {
		pool.setString(varName, map.id + "," + x + "," + y);
	}

	/**
	 * 取得玩家的伴侣ID，如果没有返回-1。
	 */
	public int getMateID() {
		RelationService rs = Server.server.getServiceRegistry().getRelationService();
		PlayerRelation pr = rs.get(id);
		return pr.mateId;
	}
	
	/**
	 * 取得玩家的盟主，如果没有返回-1。
	 */
	public int getAssociationLeaderID() {
		AssociationService service = Server.server.getServiceRegistry().getAssociationService();
		Association association = service.getAssociationByPlayerId(id);
		if(association==null)
			return -1;
		return association.getLeader().playerId;
	}
	
	/**
	 * 为虚拟机特别制作，所以返回值设置成整形，偷懒偷懒^_^
	 * @return
	 */
	public int isKing(){
		return Server.server.getServiceRegistry().getNationService().isKing(this)?1:0;
	}
	
	public int attendantIsFollowing(){
		return (attendant==null ? 0 : 1);
	}
	
	public int isOfficer(){
		return Server.server.getServiceRegistry().getNationService().getNationByFaction(faction).getOfficerByPlayerId(id)!=null?1:0;
	}
	
	public void addAction(int type){
		actions.put(type,new Action(type));
	}
	
	public boolean hasAction(int type){
		return actions.containsKey(type);

	}
	
	public void clearReport(){
		report.clear();
	}
	
	public void recordLastAction(){
		report.lastActionTime = Time.currTime;
	}
	
	class MoneyIntProperty extends TransactionIntProperty {

		@Override
		public int getValue() {
			return money;
		}

		@Override
		protected void modifyValue(int value, boolean notify, String cause) {
			setMoney(money + value, notify, cause);
		}
	}

	class HonorIntProperty extends TransactionIntProperty {

		@Override
		public int getValue() {
			return honor;
		}

		@Override
		protected void modifyValue(int value, boolean notify, String cause) {
			setHonor(honor + value, notify, cause);
		}
	}

	class CreditIntProperty extends TransactionIntProperty {

		@Override
		public int getValue() {
			return credit;
		}

		@Override
		protected void modifyValue(int value, boolean notify, String cause) {
			setCredit(credit + value, notify, cause);
		}
	}

	class ExpIntProperty extends TransactionIntProperty {

		@Override
		public int getValue() {
			return exp;
		}

		@Override
		protected void modifyValue(int value, boolean notify, String cause) {
			setExp(exp + value, notify, cause);
		}
	}

	public int getItemUseGridId() {
		if(this.itemUse != null){
		    return this.itemUse.gridId;
		} else {
			return -1;
		}
	}
	
	public int getItemInstanceId() {
		if(this.itemUse != null){
		    return this.itemUse.instanceId;
		} else {
			return -1;
		}
	}
	
	public int hasReceiveAssoInv(){
		return associationInvite==null ? 0 : 1;
	}
	
	public void injoinAssociation(){
		if(associationInvite!=null && associationInvite.endTime>=System.currentTimeMillis()){
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			Association association = service.getAssociationByPlayerId(id);
			if(association!=null && association.getMember(id).state==AssociationMember.STAT_WAIT){
				try {
					service.injoyAssociation(this, association.id, AssociationMember.STAT_WORK);
				} catch (AssociationException e) {
					
				}
			}
		}
	}
	
	public int hasAssociation(){
		AssociationService service = Server.server.getServiceRegistry().getAssociationService();
		Association asso = service.getAssociationByPlayerId(id);
		if(asso != null){
			AssociationMember mem = asso.getMember(id);
			if(mem != null && mem.state == AssociationMember.STAT_WORK){
				return 1;
			}
		}
		return 0;
	}
	
	/** 是否为外挂 */
	public boolean isBot(){
		if(antiPlug!=null && antiPlug.isBot && antiPlugModel==ANTIPLUG_MODEL_NONBENEFIT)
			return true;
		return false;
	}
	
//	class PvpState implements WarState{
//		
//		public void war(){
//			
//		}
//		
//		public void enter() {
//			pvpFaction(0);
//		}
//
//		public void exit() {
//			
//		}
//
//		public void update() {
//		}
//		
//	}
//	
//	class PvpPveState implements WarState{
//		
//		public int mapId,time;
//		
//		public PvpPveState(int mapId,int time){
//			this.mapId = mapId;
//			this.time = Time.currTime + time;
//		}
//
//		public void war(){
//			setWarState(new PvpState());
//		}
//		
//		public void enter() {
//			unPvpFaction();
//		}
//
//		public void exit() {
//			
//		}
//
//		public void update() {
//			if(Time.currTime>time){
//				setWarState(new PvpState());
//				return;
//			}
//			VMap map = getVMap();
//			if(map.getStageId()!=(mapId>>4)){
//				setWarState(new PvpState());
//				return;
//			}
//		}
//		
//	}
//	
//	class PveState implements WarState{
//		
//		public void war(){
//			setWarState(new PvePvpState(PVP_TIME));
//		}
//		
//		public void enter() {
//			unPvpFaction();
//		}
//
//		public void exit() {
//			
//		}
//
//		public void update() {
//			if(level>MAX_LEVEL){
//				setWarState(new PvpState());
//			}
//		}
//	}
//	
//	class PvePvpState implements WarState{
//		
//		public int time;
//		
//		public void war(){
//			this.time = Time.currTime + PVP_TIME;
//		}
//		
//		public PvePvpState(int time){
//			this.time = Time.currTime + time;
//		}
//		
//		public void enter() {
//			pvpFaction(0);
//		}
//
//		public void exit() {
//			
//		}
//
//		public void update() {
//			if(level>MAX_LEVEL){
//				setWarState(new PvpState());
//				return;
//			}
//			if(Time.currTime>time){
//				setWarState(new PveState());
//			}
//		}
//	}
}

class KillCreatureRecorder {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger
			.getLogger(KillCreatureRecorder.class);

	protected List<Integer> killTemplateID = new ArrayList<Integer>(3);
	protected List<List> killOwners = new ArrayList<List>(3);

	public void add(int templateId, List<Player> owners) {
		killTemplateID.add(templateId);
		killOwners.add(owners);
	}
	
	public int get(int templateId) {
		int ret = 0;
		for (int i = killTemplateID.size() - 1; i >= 0; i--) {
			if (killTemplateID.get(i) == templateId) {
				ret++;
			}
		}
		return ret;
	}
	
	/**
	 * 取得和某个朋友共同杀死的怪物数量。
	 * @param templateId 怪物模板ID
	 * @param mate 朋友ID
	 * @return
	 */
	public int get(int templateId, int mate) {
		if (mate == -1) {
			return 0;
		}
		int ret = 0;
		for (int i = killTemplateID.size() - 1; i >= 0; i--) {
			if (killTemplateID.get(i) == templateId) {
				boolean found = false;
				List<Player> owners = (List<Player>) killOwners.get(i);
				for (Player p : owners) {
					if (p.id == mate) {
						found = true;
						break;
					}
				}
				if (found) {
					ret++;
				}
			}
		}
		return ret;
	}

	public void clear() {
		killTemplateID.clear();
		killOwners.clear();
	}
}

class TouchNpcInfo {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(TouchNpcInfo.class);

	public GameObjectRef npcRef;
	public int questId;

	public TouchNpcInfo(GameObjectRef npcRef, int questId) {
		this.npcRef = npcRef;
		this.questId = questId;
	}
}

class UINotify {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(UINotify.class);

	public int questId;
	public int id;
	public int type;
	public int answer;

	public UINotify(int questId, int id, int type, int answer) {
		this.questId = questId;
		this.id = id;
		this.type = type;
		this.answer = answer;
	}
}

class AutoAttack {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(AutoAttack.class);

	public boolean fired;
	public int time;
	public GameObjectRef ref;
	public Attack attack;

	public void clear() {
		fired = false;
		time = 0;
		ref = null;
		attack = null;
	}

}

class ItemUse {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(ItemUse.class);

	public GameObjectRef target;
	public int time;
	public int gridId;
	public int itemId;
	public int instanceId;

	public ItemUse(GameObjectRef target, int gridId, int itemId,
			int instanceId, int time) {
		this.target = target;
		this.gridId = gridId;
		this.itemId = itemId;
		this.instanceId = instanceId;
		this.time = time;
	}
}

class HorseRide {

	public HorseRide(int instanceId, int time, int serial) {
		this.instanceId = instanceId;
		this.time = time;
		this.serial = serial;
	}

	public int instanceId;
	public int time;
	public int serial;
}

class ClientMove {
	public int x, y;
	public byte direct;
	public short state;
	public int time;

	public ClientMove(int x, int y, byte direct, short state, int time) {
		this.x = x;
		this.y = y;
		this.state = state;
		this.direct = direct;
		this.time = time;
	}
}

class Position {
	public int mapId, x, y, time, clientTime, speed;

	public Position(int mapId, int x, int y, int time, int clientTime, int sp) {
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.time = time;
		this.clientTime = clientTime;
		this.speed = sp;
	}
}

class Gather {

	public int serial;

	public GameObjectRef ref;
	public int time;

	public Gather(int serial, GameObjectRef ref, int time) {
		this.serial = serial;
		this.ref = ref;
		this.time = time;
	}
}

class Report{
	
	public int lastActionTime; //上次动作时间
	public int[] reportPlayerId; //举报人
	public int reportStartTime; //第一次举报时间
	public int effectReport; //有效举报数
	
	public void report(int playerId) throws Exception{
		if(reportPlayerId==null){
			reportPlayerId = new int[1];
			reportPlayerId[0] = playerId;
		}else{
			if(hasReported(playerId))
				throw new Exception("您已经举报过此人");
			reportPlayerId = new int[reportPlayerId.length+1];
			reportPlayerId[reportPlayerId.length-1] = playerId;
		}
		if(reportStartTime==0)
			reportStartTime = Time.currTime;
		effectReport++;
	}
	
	private boolean hasReported(int playerId){
		for(int id : reportPlayerId){
			if(playerId==id)
				return true;
		}
		return false;
	}
	
	public void clear(){
		reportPlayerId = null;
		reportStartTime = 0;
		effectReport = 0;
	}
	
}

class AntiPlug{
	
	public static Random ran = new Random();
	public int lastSendATime;
	public int lastSendBTime;
	public int A = -1;
	public int B = -1;
	public int C = -1;
	public Integer D = null;
	public Integer D1 = null;
	public Player owner;
	public String uiModel;
	public boolean isBot; //是否已经判定为外挂
	public int score; //发包型外挂
	public int score1; //技能型外挂
	public boolean sendA;
	
	public AntiPlug(Player player){
		this.owner = player;
	}
	
	public void calc(){
		A = (ran.nextInt()<<29 | ran.nextInt()>>3);
		B = ran.nextInt();
	}
	
	public void calc1(){
		String name = "game_world.etf";
		try {
			DataService ds = Server.server.getServiceRegistry().getDataService();
			byte[] data = ds.data.downloadFile(name, uiModel);
			if(data != null){
				C = data.length;
			}
		} catch (Exception e) {
			clear();
		}
	}
	
	public void enCode(){
		int key = (A>>29 & 7);
		switch(key){
		case 0:
			D = A & B & C;
			break;
		case 1:
			D = (~A) & B | C;
			break;
		case 2:
			D = (A | (~B)) & C;
			break;
		case 3:
			D = (~A) & (~B) | C;
			break;
		case 4:
			D = (~A) | (~B) | (~C);
			break;
		case 5:
			D = A & (B) | (~C);
			break;
		case 6:
			D = ((~A) | (B)) & (~C);
			break;
		case 7:
			D = A & (~B) & (~C);
			break;
		}
	}
	
	public void clear(){
		A = -1;
		B = -1;
		C = -1;
		D = null;
		D1 = null;
		lastSendBTime = 0;
		score = 0;
		sendA = false;
	}
	
	public Packet getPacketA(){
		Packet pt = new Packet(OpCode.ANTI_PLUG_SERVER);
		pt.put(0);
		pt.putInt(A);
		return pt;
	}
	
	public Packet getPacketB(){
		Packet pt = new Packet(OpCode.ANTI_PLUG_SERVER);
		pt.put(1);
		pt.putInt(B);
		return pt;
	}
	
}
