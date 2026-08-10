package peony.service;

/**
 * 服务之间的事件。所有事件被放到一个公共的事件队列中，并由服务器主循环分派给关心此事件的服务。
 * @author lighthu
 */
public class ServiceEvent {
	/**
	 * 添加场景事件。参数：VMap
	 */
	public static final int EVENT_MAP_ADDED = 101;
	/**
	 * 删除场景事件。参数：VMap
	 */
	public static final int EVENT_MAP_REMOVED = 102;
	/**
	 * 玩家进入场景事件。参数：VMap, Player
	 */
	public static final int EVENT_MAP_PLAYER_ADDED = 201;
	/**
	 * 新进入场景的玩家载入完成事件。参数：VMap, Player
	 */
	public static final int EVENT_MAP_PLAYER_LOADED = 202;
	/**
	 * 玩家离开场景事件。参数：VMap, Player
	 */
	public static final int EVENT_MAP_PLAYER_REMOVED = 203;
	/**
	 * 队伍创建事件。参数：Party
	 */
	public static final int EVENT_PARTY_CREATED = 301;
	/**
	 * 队伍解散事件。参数：Party
	 */
	public static final int EVENT_PARTY_DESTROIED = 302;
	/**
	 * 玩家离开队伍事件。参数：Party, PartyMember
	 */
	public static final int EVENT_MEMBER_LEAVED = 401;
	/**
	 * 玩家进入队伍事件。参数：Party, PartyMember
	 */
	public static final int EVENT_MEMBER_ADDED = 402;
	/**
	 * 玩家升级事件。参数：Player
	 */
	public static final int EVENT_PLAYER_LEVELUP = 501;
	/**
	 * 角色创建事件。参数：Player
	 */
	public static final int EVENT_PLAYER_CREATED = 601;
	/**
	 * 角色数据载入完成事件。参数：Player
	 */
	public static final int EVENT_PLAYER_LOADED = 602;
	/**
	 * 角色登录事件。参数：Player
	 */
	public static final int EVENT_PLAYER_LOGINED = 603;
	/**
	 * 角色退出事件。参数：Player
	 */
	public static final int EVENT_PLAYER_LOGOUTED = 604;
	/**
	 * 角色首次载入事件。参数：Player
	 */
	public static final int EVENT_PLAYER_FIRSTLOAD = 605;
	/**
	 * 角色保存事件。参数：Player
	 */
	public static final int EVENT_PLAYER_SAVED = 606;
	/**
	 * 角色数据从内存中移除事件。参数：Player
	 */
	public static final int EVENT_PLAYER_UNLOADED = 607;
	/**
	 * 角色改名事件。参数：Player
	 */
	public static final int EVENT_PLAYER_CHANGENAME = 608;
	
	/**
	 * 角色在Pk的时候的移动消息。参数:Player,x,y
	 */
	public static final int EVENT_PLAYER_PK_MOVE = 609;
	
	/**
	 * 角色在Pk的时候被攻击。参数:Player
	 */
	public static final int EVENT_PLAYER_PK_ATTACKED = 610;

	/**
	 * 玩家完成任务。参数：Player，任务ID，分支ID
	 */
	public static final int EVENT_FINISH_QUEST = 611; 

	/**
	 * 连接建立事件。参数：ClientSession
	 */
	public static final int EVENT_SESSION_ADDED = 701;
	/**
	 * 连接关闭事件。参数：ClientSession
	 */
	public static final int EVENT_SESSION_REMOVED = 702;
	/**
	 * 玩家交互事件。当玩家A和玩家B进行了X交互时需要触发此事件，以更新双方的临时好友表。参数：Player, Player, Integer
	 */
	public static final int EVENT_INTERACT = 801;
	/**
	 * 军团载入事件。参数：Tong
	 */
	public static final int EVENT_TONG_LOADED = 901;
	/**
	 * 玩家加入军团事件。参数：Actor, Tong
	 */
	public static final int EVENT_PLAYER_CHANGETONG = 902;
	/**
	 * 游戏对象（怪物或者玩家）死亡。参数：死亡Unit，杀手Unit(可能为null)
	 */
	public static final int EVENT_UNIT_DIE = 903;
	
	/**
	 * 玩家离开军团事件。参数：Actor，Tong
	 */
	public static final int EVENT_PLAYER_LEAVETONG = 904;
	
	/**
	 * 玩家更改阵营。参数：Player，原来的阵营
	 */
	public static final int EVENT_PLAYER_CHANGE_FACTION = 905;
	
	/**
	 * out prison to relive point
	 * parameter:Player
	 */
	public static final int EVENT_PLAYER_OUTPRISON_RELIVEPOINT = 906;
	
	/**
	 * 玩家金钱提升.参数：Player ,原来金钱, 增加金钱
	 */
	public static final int EVENT_PLAYER_MONEY_UP = 907;
	
	/**
	 * 玩家结婚.参数：Player, 对象
	 */
	public static final int EVENT_PLAYER_MARRIAGE = 908;
	
	/**
	 * 玩家向国库捐献金钱.参数：Player, 原来所有捐赠, 本次捐赠
	 */
	public static final int EVENT_NATIONCOLLECT = 909;
	
	/**
	 * 玩家获得新的军衔。参数：Player,军衔(都尉 中郎将 大将军)
	 */
	public static final int EVENT_RANK_UP = 910;
	
	/**
	 * 玩家镶嵌宝石.参数：Player,item,镶嵌物品
	 */
	public static final int EVENT_ADDJEWEL_SUCCESS = 911;
	
	/**
	 * 战争胜利。参数：PlayerId,type(0,国战 1,战场 2,城战,3,国家反击战),victorySide
	 */
	public static final int EVENT_BATTLE_WIN = 912;
	
	/**
	 * 玩家发送聊天。参数：Player,type
	 */
	public static final int EVENT_CHAT = 913;
	
	/**
	 * 打孔成功。参数：Player
	 */
	public static final int EVENT_DIG_SUCCESS = 914;
	
	/**
	 * 摘除宝石。参数：Player
	 */
	public static final int EVENT_EXTIRPADE = 915;
	
	/**
	 * 装备鉴定。参数：Player,type(0,资质鉴定 1,星级鉴定)
	 */
	public static final int EVENT_ENHANCE = 916;
	
	/**
	 * 洗点。参数：Player
	 */
	public static final int EVENT_SKILL_REFRESH = 917;
	
	/**
	 * 使用特殊物品成功。参数：Player，itemId,count
	 */
	public static final int EVENT_USEITEM = 918;
	
	/**
	 * 玩家打造。参数：Player,outType,formulaLevel,outPut
	 */
	public static final int EVENT_PRODUCE = 919;
	
	/**
	 * 玩家充值成功。参数：Player ，money(官服：人民币；台湾：元宝)
	 */
	public static final int EVENT_CHARGE_SUCCESS = 920;
	
	/**
	 * 宝石合成。参数：Player
	 */
	public static final int EVENT_MERGEJEWEL = 921;
	
	/**
	 * 马升级事件。参数：Player
	 */
	public static final int EVENT_HORSE_LEVELUP = 922;
	
	/**
	 * 马上骑事件。参数：Player
	 */
	public static final int EVENT_HORSE_RIDE = 923;
	
	/**
	 * 马添加装备事件。参数：Player
	 */
	public static final int EVENT_HORSE_EQUIP = 924;
	
	/**
	 * 反击战结束事件。参数：sourceFaction
	 */
	public static final int EVENT_SNEAKBATTLE_END = 925;
	
	/**
	 * 获得称号事件。参数：Player
	 */
	public static final int EVENT_ADD_TITLE = 926;
	
	/**
	 * 获得坐骑事件。参数：Player
	 */
	public static final int EVENT_ADD_HORSE = 927;
	
	/**
	 * 选美结束事件。参数：playerId
	 */
	public static final int EVENT_BEAUTY_END = 928;
	
	/**
	 * 收集卡片事件。参数：Player,groupId
	 */
	public static final int EVENT_COLLECT_CARD = 929;
	
	/**
	 * 玩家换装事件。参数：Player
	 */
	public static final int EVENT_CHANGE_EQUIP = 930;
	
	/**
	 * 玩家切磋事件。参数：Player
	 */
	public static final int EVENT_PK_END = 931;
	
	/**
	 * 玩家离婚事件。参数：Player:man  int:womanId
	 */
	public static final int EVENT_DIVORCE = 932;
	
	/**
	 * 玩家背包中添加物品。参数Player，itemId
	 */
	public static final int EVENT_GETITEM = 933;
	
	/**
	 * 玩家i币消费成功。参数：Player ID，消费金额（1/100分）,itemID,count
	 */
	public static final int EVENT_IBUY = 1001;
	
	/**
	 * 军团升级事件。参数：Tong
	 */
	public static final int EVENT_TONG_UPLEVEL = 1002;
	
	/**
	 * 每日福利完成事件。参数 Player
	 */
	public static final int EVENT_WELFARE_FINISH = 1003;
	
	/**
	 * 成就完成事件。参数 Player
	 */
	public static final int EVENT_ACHIEVE_FINISH = 1004;
	
	/**
	 * 斗阵胜利事件。参数  partId
	 */
	public static final int EVENT_DOUZHEN_WIN = 1005;
	
	/**
	 * 参加司隶战役事件。参数  player
	 */
	public static final int EVENT_JOIN_SILI = 1006;
	
	/**
	 * 参加天下第一比武。参数 player
	 */
	public static final int EVENT_JOIN_BIWU = 1007;
	
	/**
	 * 参加比武招亲。参数 player
	 */
	public static final int EVENT_BIWU_ZHAOQIN = 1008;
	
	/**
	 * 玩家在线一小时事件（从凌晨三点开始）。参数  player
	 */
	public static final int EVENT_ONLINE_HOUR = 1009;
	
	/**
	 * 凌晨三点换天事件。
	 */
	public static final int EVENT_CHANGEDAY_THREE = 1010;
	
	/**
	 * 玩家闯关完成事件。参数：Player player, int maxCycle
	 */
	public static final int EVENT_CYCLEINSTANCE_FINISH = 1011;
	
	/**
	 * 账号数据池加载成功。参数：Player
	 */
	public static final int EVENT_ACCOUNTPROPERTY_LOADED = 1012;
	
	/**
	 * 玩家战功消耗。参数：Player ，credit
	 */
	public static final int EVENT_CREDIT_DEC = 1013;
	
	/**
	 * 玩家获取工资事件。参数：Player，salary
	 */
	public static final int	EVENT_SALARY_ADD = 1014;
	
	public static final int PLAYER_LOAD_SILENT = 0; //角色以Silent方式载入，一般用于GM工具载入一个未登录的用户
	public static final int PLAYER_LOAD_DB = 1; //角色从数据库载入
	public static final int PLAYER_LOAD_CACHE = 2; //角色从Cache载入
	public static final int PLAYER_LOAD_ACCESSOR = 3; //角色从ObjectAccessor载入
	
	
	public int type;
	public Object param1;
	public Object param2;
	public Object param3;
	public Object param4;
	public Object param5;
	
	public ServiceEvent(int type) {
		this.type = type;
	}
	
	public ServiceEvent(int type, Object param1) {
		this.type = type;
		this.param1 = param1;
	}

	public ServiceEvent(int type, Object param1, Object param2) {
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
	}

	public ServiceEvent(int type, Object param1, Object param2, Object param3) {
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}
	
	public ServiceEvent(int type, Object param1, Object param2, Object param3, Object param4){
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
		this.param4 = param4;
	}
	
	public ServiceEvent(int type, Object param1, Object param2, Object param3, Object param4, Object param5){
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
		this.param4 = param4;
		this.param5 = param5;
	}
}
