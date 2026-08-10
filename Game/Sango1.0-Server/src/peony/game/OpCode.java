package peony.game;





public class OpCode {
	/**
	 * 代理->世界，通知IP地址
	 * ip		int		IP地址
	 */
	public static final short PROXY_SYNC_IP = 30000;
	/**
	 * 代理->世界，代理服务器登录
	 * ip 		int		代理服务器IP地址
	 * port		short	代理服务器端口号
	 */
	public static final short PROXY_LOGIN = 30001;
	/**
	 * 代理<->世界，断开客户端连接通知
	 */
	public static final short PROXY_SESSION_DISCONNECT = 30002;
	/**
	 * 客户端<->世界，设定加密key。
	 */
	public static final short SET_ENCRYPT_KEY = 30003;
	
	/**
	 * 错误
	 * serial					int
	 * type						short
	 * message					string
	 */
	public static final short ERROR = -1;
	
	public static final short SYSTEM_NEWSESSION = 10;
	public static final short SYSTEM_CLOSESESSION = 11;
	
	public static final short SYNC_TIME_CLIENT = 101;
	public static final short SYNC_TIME_SERVER = 102;
	
	/**
	 * 登录角色
	 * serial					int
	 * 角色Id					int
	 * IMEI						String			IMEI串
	 */
	public static final short ACTOR_LOGIN_CLIENT = 103;
	/**
	 * 角色登录成功
	 * serial					int
	 * 角色信息					ACTOR
	 * 账号信息                 String
	 */
	public static final short ACTOR_LOGIN_SERVER = 104;
	
	/**
	 * 移动
	 * 时间						int
	 * x						short
	 * y						short
	 * 方向						byte
	 * 状态               		short
	 */
	public static final short MOVE_CLIENT = 105;
	
	/**
	 * 类型		byte 	1 是玩家，3是Npc
	 * id		int
	 * name		string
	 * level	byte
	 * 时间		int
	 * x		int
	 * y		int
	 * 方向		byte
	 * 状态		short
	 * 动画Id	int  	如果是人物走动动画id为-1
	 * 阵营		byte	
	 */
	public static final short MOVE_SERVER = 106;
	public static final short RIDE_CLIENT = 107;
	public static final short UNRIDE_CLIENT = 108;
	public static final short LOGOUT_CLIENT = 113;
	public static final short LOGOUT_SERVER = 114;
//	public static final short UNIT_INVISIBLE = 115;
	
	/**
	 * serial		int
	 * 时间 			int
	 * 出口Id 		int
	 */
	public static final short TOUCHEXIT_CLIENT = 116;
	
	/**
	 * 关卡号 int
	 * 所在地图号 	byte
	 * 角色x坐标  	int
	 * 角色y坐标  	int
	 * 
	 * 数据   		byte[]
	 */
	public static final short PKG_SERVER = 117;
	
	
	/**
	 * animateName	string	图片名字
	 */
	public static final short ANIMATEGET_CLIENT = 118;
	
	/**
	 * animateName 	string
	 * byte[]   	data
	 */
	public static final short ANIMATEGET_SERVER = 119;
	
	/**
	 * npcinstanceIdid					int
	 * questId							int
	 */
	public static final short TOUCHNPC_CLIENT = 120;
	
	
	/**
	 * npcId		int
	 * message		string
	 * notifyId		int
	 */
	public static final short NPC_CHAT_SERVER = 121;
	
	/**
	 * message 		string
	 * timeout 		int
	 * notifyId		int
	 */
	public static final short MESSAGE_SERVER = 122;
	
	/**
	 * message 		string
	 * options		string
	 * notifyId		int
	 */
	public static final short QUESTION_SERVER = 123;
	
	
	/**
	 * 技能列表，在登录时下发，跟在关卡包后
	 * count			byte
	 * 循环:
	 * 	id				int
	 * 	name			string
	 * 	type			byte	0 近战 1 远程 2 魔法 3 AOE
	 * 	攻击距离			int
	 * 	攻击时间			int		（单位：毫秒）
	 * 	cd时间			int		（单位：毫秒）
	 * 	攻击范围			int
	 */
//	public static final short ABILITIES_SERVER = 124;
	
	/**
	 * 添加关卡中可以接受的任务
	 * 起始npcid			int
	 * 终止npcid			int
	 * 任务id			int
	 * 任务等级			byte
	 * 任务名字			string
	 * 前描述			String
	 * 中描述			String
	 * 后描述			String
	 * 描述				String
	 */
	public static final short QUEST_START_ADDED_SERVER = 125;
	
	
	/**
	 * 移除关卡中可以接受的任务
	 * 起始npcId			int
	 * 任务id			int
	 */
	public static final short QUEST_START_REMOVED_SERVER = 126;
	
	/**
	 * 添加关卡中可完成的任务
	 * 结束npcid			int
	 * 任务id			int
	 * 是否需要完成提醒	byte
	 */
	public static final short QUEST_FINISH_ADDED_SERVER = 127;
	

	/**
	 * 移除关卡中可完成的任务
	 * 结束npcid			int
	 * 任务id			int
	 */
	public static final short QUEST_FINISH_REMOVED_SERVER = 128;
	
	/**
	 * 获取任务描述
	 * 任务id			int

	 */
	public static final short QUEST_DESC_CLIENT = 129;
	
	
	/**
	 * 任务描述
	 * 任务id						int
	 * 描述							string
	 * 任务目标的数量				byte
	 * 奖励组数量					byte
	 * 循环n次
	 	奖励组Id						byte
	 	奖励数量						byte
	 	循环n次
	 		奖励类型						byte
	 		奖励内容						int,ITEM(属性值，如果类型是101，那么这个字段就是ITEM)
	 */
	public static final short QUEST_DESC_SERVER = 130;
	
	/**
	 * 接受任务
	 * 任务id						int
	 */
	public static final short QUEST_ACCEPT_CLIENT = 131;
	
	/**
	 * 接受任务成功
	 * 任务id						int
	 * 任务etf						byte[]
	 * 循环n次
	 *  任务目标描述					string
	 */
	public static final short QUEST_ACCEPTED_SERVER = 132;
	
	/**
	 * 关卡载入完成，通知服务器可以正常进行游戏了
	 */
	public static final short LOADING_FINISHED_CLIENT = 133;
	
	/**
	 * 允许通过地图
	 * 目标地图id			int
	 * 目标地图instanceId	int
	 * 目标地图x				int
	 * 目标地图y				int
	 * 是否允许使用跟随		byte (1 允许 0 不允许)
	 */
	public static final short GOMAP_ALLOW_SERVER = 134;
	
	/**
	 * 任务完成
	 * 任务id			int
	 */
	public static final short QUEST_FINISHED_SERVER = 135;
	
	/**
	 * 攻击失败
	 * 类型						byte  	1 距离太远 2 当前有攻击正在进行 3 目标已经死亡 4 目标不存在 
	 * 									5 没有技能 6 此技能不能在马上使用 7 这个技能必须选择一个目标
	 * 									8 这个技能不能对这个目标使用 9 目标没有死亡 10 次技能CD时间没到 11 当前状态不能使用此技能
	 * 									12 没有足够的mana 13 级别保护 14 目标不在视野范围内 15 技能被打断
	 * sourceInstanceId			int     源InstanceId
	 * targetInstanceId			int		目标InstanceId
	 * attackId					int		技能Id
	 */
	public static final short ATTACK_FAIL_SERVER = 136;
	
	/**
	 * 完成任务
	 * serial					int
	 * 任务Id					int
	 * 任务奖励Id				int
	 */
	public static final short QUEST_FINISH_CLIENT = 137;
	
	/**
	 * 完成任务失败
	 * 任务Id			int
	 * 任务失败原因		byte	1 没有任务 2 不能完成
	 */
	public static final short QUEST_FINISH_FAIL_SERVER = 138;
	
	/**
	 * 任务失败
	 * 任务Id				int
	 */
	public static final short QUEST_FAIL_SERVER = 139;
	
//	/**
//	 * 物品变化的数量	byte
//	 * 循环
//	 * 		itemId		int
//	 * 		instanceId	int
//	 * 		数量			byte
//	 * 背包变化的数量	byte
//	 * 循环
//	 * 		包格Id		int
//	 * 		itemId		int		-1 表示没有物品
//	 * 		instanceId	int	
//	 * 		当前数量		byte
//	 */
//	public static final short BAG_CHANGED_SERVER = 139;
	
	/**
	 * 获取unit的ctn文件
	 * instanceId				int
	 */
	public static final short CTNGET_CLIENT = 140; 
	
	/**
	 * 得到unit的ctn文件
	 * instanceId				int
	 * imageId     				int
	 * byte[]   				data
	 */
	public static final short CTNGET_SERVER = 141;
	
	/**
	 * 请求物品信息
	 * 物品Id							int
	 * 物品实例Id						int		-1 表示请求一般信息
	 * 类型								byte	0位为1说明需要ITEM信息，1位为1说明需要物品描述
	 * value							String	类型对应的value值
	 */
	public static final short ITEMINFO_CLIENT = 142;
	
	/**
	 * 类型								byte  0位为1说明需要ITEM信息，1位为1说明需要物品描述
	 * 物品Id							int
	 * 物品实例Id						int
	 * 物品信息							ITEM  如果没有要ITEM信息，那么这个字段不存在
	 * 物品描述信息						String 如果没有要描述信息，这个字段不存在
	 */
	public static final short ITEMINFO_SERVER = 143;
	
	/**
	 * 同步玩家数据
	 * 同步数量							byte
	 * 重复n次
	 * 	属性描述							short
	 *  属性值							int,string,complex
	 * 提示数量
	 * 重复n次
	 * 	属性描述							short
	 * 	属性值							int,string,complex
	 */		
	public static final short SYNC_PLAYER_SERVER = 144;
	
	/**
	 * 使用物品
	 * 包格Id						byte    如果没指定包格，那么包格Id为-1
	 * 物品Id						int
	 * 物品实例Id					int
	 * 目标Id						int
	 * 使用时间						int
	 */
	public static final short USEITEM_CLIENT = 145;
	
	/**
	 * 获取包格物品信息
	 */
	public static final short BAG_CLIENT = 146;
	
	/**
	 * 包格数量						byte
	 * 循环n次
	 *	包格信息						GRID
	 */
	public static final short BAG_SERVER = 147;
	
	/**
	 * 移除物品
	 * 包格Id						byte
	 * 物品Id						int
	 * InstanceId					int
	 * 物品数量						byte
	 */
	public static final short REMOVEITEM_CLIENT = 148;
	

	/**
	 * 取任务描述
	 * 任务Id						int
	 */
	public static final short QUEST_PREDESC_CLIENT = 149;
	
	/**
	 * 任务描述
	 * 任务Id						int
	 * 任务起始描述					String
	 * 奖励组数量		byte
	 * 循环n次
	 	奖励组Id						byte
	 	奖励数量						byte
	 	循环n次
	 		奖励类型						byte
	 		奖励内容						int,ITEM(属性值，如果类型是101，那么这个字段就是ITEM)
	 */
	
	public static final short QUEST_PREDESC_SERVER = 150;
	
	/**
	 * 取任务结束描述
	 * 任务Id						int
	 */
	public static final short QUEST_POSTDESC_CLIENT = 151;
	
	/**
	 * 任务结束描述
	 * 任务Id						int
	 * 任务结束描述					string
	 * 奖励组数量		byte
	 * 循环n次
	 	奖励组Id						byte
	 	奖励数量						byte
	 	循环n次
	 		奖励类型						byte
	 		奖励内容						int,ITEM(属性值，如果类型是101，那么这个字段就是ITEM)
	 */
	public static final short QUEST_POSTDESC_SERVER = 152;
	
	/**
	 * 获取玩家当前的技能列表
	 */
	public static final short SKILL_LIST_CLIENT = 153;
	/**
	 * 玩家当前的技能列表
	 * 技能数量						byte
	 * 循环n次
	 * 	技能							SKILL
	 * 
	 */
	public static final short SKILL_LIST_SERVER = 154;
	
	/**
	 * 给技能加点
	 * serial						int
	 * 技能GroupId					short
	 * 技能等级						byte
	 */
	public static final short SKILL_ADDPOINT_CLIENT = 155;
	
	/**
	 * 洗技能点
	 * serial						int
	 */
	public static final short SKILL_REFRESH_CLIENT = 156;
	
	/**
	 * 取技能名字列表
	 */
	public static final short SKILL_NAMELIST_CLIENT = 157;
	
	/**
	 * 技能名字列表
	 * 技能数量						byte
	 * 循环n次
	 * 	技能GroupId					short
	 * 	技能名						string
	 */
	public static final short SKILL_NAMELIST_SERVER = 158;
	
	/**
	 * 获取任务列表
	 */
	public static final short QUEST_LIST_CLIENT = 159;
	
	/**
	 * 任务列表
	 * 任务数量						byte
	 * 循环n次
	 * 	任务Id						int
	 *	起始npcid					int
	 * 	终止npcid					int
	 * 	任务等级						byte
	 * 	任务名字						string
	 * 	任务etf						byte[]
	 *  任务变量						int[]
	 *  目标数量						byte
	 *  循环n次
	 *   任务目标描述					string
	 *  任务失败状态					byte (1 失败 0 没失败)
	 */
	public static final short QUEST_LIST_SERVER = 160;
	
	/**
	 * 任务信息下发，暂时只有场景任务需要
	 * 任务Id						int
	 * 任务名字						string
	 * 任务etf						byte[]
	 * 任务变量						int[]
	 */
	public static final short AREAQUEST_INFO_SERVER = 161;
	
	/**
	 * 客户端跟服务器端同步变量
	 * 任务Id						int
	 * 变量Index						int
	 * 变量值						int
	 */
	public static final short VM_VARIABLE_SYNC_CLIENT = 162;
	
	/**
	 * 服务器跟客户端同步任务变量
	 * 同步数量						byte
	 * 循环n次
	 * 	任务Id						int
	 * 	变量Index					int
	 * 	变量值						int
	 */
	public static final short VM_VARIABLE_SYNC_SERVER = 163;
	
	/**
	 * 获取物品描述
	 * serial						int
	 * 物品Id						int
	 * instanceId					int
	 */
	public static final short ITEM_DESC_CLIENT = 164;
	
	/**
	 * 物品描述
	 * serial						int
	 * 物品Id						int
	 * instanceId					int
	 * 描述							string
	 */
	public static final short ITEM_DESC_SERVER = 165;
	
	/**
	 * 登录帐号
	 * serial						int
	 * 帐号名						string
	 * 密码							string
	 * 机型                         string
	 * 版本号                       string
	 * 手机号(可空)                 string
	 * 角色ID(可选)                 int
	 * cmccUserId					string(只有电信以及卓望版本有此字段)
	 * cmccUserKey					string(只有电信以及卓望版本有此字段)
	 * IMEI							String
	 * qmeId						String		只有台湾用户有此字段
	 */
	public static final short ACCOUNT_LOGIN_CLIENT = 166;
	
	/**
	 * 登录帐号成功
	 * serial						int
	 * 帐号Id						int
	 * 帐号名						string
	 * i币							int			
	 * 修改帐号名的次数				int		
	 * gameCode                     String	
	 */
	public static final short ACCOUNT_LOGIN_SERVER = 167;
	
	/**
	 * 获取角色列表
	 * serial						int
	 */
	public static final short ACTOR_LIST_CLIENT = 168; 

	/**
	 * 角色列表
	 * serial						int
	 * 角色数量						byte
	 * 循环n次						
	 * 	Id						int
	 * 	名字						int
	 * 	性别						byte
	 * 	等级						byte
	 *	职业						byte
	 *	阵营						byte
	 *  headscore				int
	 *  bodyscore				int
	 *  weaponscore				int
	 *  发光效果					byte
	 *  地图名					String
	 */
	public static final short ACTOR_LIST_SERVER = 169;
	
	/**
	 * 创建角色
	 * serial						int
	 * 名字							string
	 * 性别							byte
	 * 职业							byte
	 * 阵营							faction
	 * 方式							byte		(1为自动生成名字，0或者不写为旧的方式)	
	 */
	public static final short ACTOR_CREATE_CLIENT = 170;
	
	/**
	 * 创建角色成功
	 * serial						int
	 * 	Id						int
	 * 	名字						int
	 * 	性别						byte
	 * 	等级						byte
	 *	职业						byte
	 */
	public static final short ACTOR_CREATE_SERVER = 171;
	
	
	/**
	 * 放弃任务
	 * serial						int
	 * 任务Id						int
	 */
	public static final short QUEST_ABANDON_CLIENT = 172;
	
	/**
	 * 放弃任务成功
	 * serial						int
	 * 任务Id						int
	 */
	public static final short QUEST_ABANDON_SERVER = 173;
	
	/**
	 * 界面Notify
	 * 任务Id						int
	 * notifyId						byte
	 * notifyType					byte	1 chat 2 message 3 question
	 * questionAnswer				byte
	 */
	public static final short NOTIFY_CLIENT = 174;
	
	/**
	 * 装备
	 * serial						int
	 * ItemId						int
	 * instanceId					int
	 */
	public static final short EQUIP_CLIENT = 175;

	/**
	 * 装备成功
	 * serial						int
	 * 
	 */
	public static final short EQUIP_SERVER = 176;

	/**
	 * 卸下装备
	 * serial						int
	 * ItemId						int
	 * instanceId					int
	 */
	public static final short UNEQUIP_CLIENT = 177;

	/**
	 * 卸下装备成功
	 * serial						int
	 */
	public static final short UNEQUIP_SERVER =178;
	
	/**
	 * 增加属性点
	 * serial						int
	 * strength						short   //力量上新添加的点
	 * agility						short
	 * stamina						short
	 * intellect					short
	 */
	public static final short PROPERTYPOINT_ADD_CLIENT = 179;
	
	/**
	 * 增加属性点成功
	 * serial						int
	 */
	public static final short PROPERTYPOINT_ADD_SERVER = 180;
	
	
	/**
	 * 获取技能描述
	 * serial                      int
	 * 技能组Id						short
	 * 技能等级						byte
	 */
	public static final short SKILL_DESC_CLIENT = 181;
	
	/**
	 * 技能描述
	 * serial                      int
     * 技能组Id                     short
     * 技能等级                     byte
	 * 升级点数						byte
	 * 描述							string
	 */
	public static final short SKILL_DESC_SERVER = 182;
	
	/**
	 * 加点成功
	 * serial						int
	 * skill						SKILL
	 */
	public static final short SKILL_ADDPOINT_SERVER = 183;
	
	/**
	 * 洗点成功
	 * serial						int
	 */
	public static final short SKILL_REFRESH_SERVER = 184;
	
	
	/**
	 * 技能攻击
	 * 时间							int
	 * x							short
	 * y							short
	 * 方向							byte
	 * 目标InstanceId				int
	 * 技能Id						int		
	 */
	public static final short SKILL_ATTACK_CLIENT = 185;
	
	/**
	 * 技能攻击
	 * 源InstanceId					int
	 * 目标InstanceId				int
	 * 释放动画ID                   	int
	 * 
	 */
	public static final short SKILL_ATTACK_SERVER = 186;
	
	/**
	 * 被攻击
	 * 目标InstanceId				int
	 * 时间							int
	 * 源InstanceId					int
	 * 攻击结果类型					byte	0 命中 1 miss 2 免疫 3 命中且暴击
	 * 伤害类型     					byte     0 物理 1 法术 2 抽蓝 3 诅咒 4 加血 5 回蓝 6 增强
	 * 伤害值						int		只有在命中时有意义
	 * 受攻击动画					int
	 */
	public static final short SKILL_ATTACKED_SERVER = 187;
	
	/**
	 * 版本比较
	 * 数量							short
	 * 循环n次
	 * 	文件名						string
	 * 	版本							int
	 */
	public static final short VERSION_COMPARE_CLIENT = 188;
	
	/**
	 * 版本比较结果
     * 客户端UIMODEL                   string
     * 客户端版本                        string
     * 客户端MODEL                     string
	 * 数量							short
	 * 循环n次
	 * 	需要删除缓存的文件名			string
	 * 	文件版本						int
	 */
	public static final short VERSION_COMPARE_SERVER = 189;

	/**
	 * 强制进行版本比较
	 */
	public static final short SYNC_VERSION_SERVER = 190;
	
	/**
	 * 获取文件
	 * 文件名						string
	 */
	public static final short GETFILE_CLIENT = 191;
	/**
	 * 文件信息
	 * 客户端机型                   string
	 * 文件名						string
	 * 版本信息						int
	 * 文件内容						byte[]
	 */
	public static final short GETFILE_SERVER = 192;
	
	/**
	 * NPC，玩家，怪物进入(走出)视野
	 * type							byte(第7位如果为1，表示走出视野)
	 * id							int
	 * instanceId					int
	 * imageId						short (怪物，采集npc在走进视野的时候存在)
	 */
	public static final short UNIT_REFRESH_SERVER = 193;
	
	
	/**
	 * 刷新多人走入走出视野信息
	 * 数量							byte
	 * 循环n次
	 * 	type						byte
	 *  id							int
	 *  instanceId					int
	 *  imageId						short (怪物，采集npc在走进视野的时候存在)
	 */
	public static final short UNIT_MULTI_REFRESH_SERVER = 194;
	
//	/**
//	 * UNIT次级信息
//	 * type							byte
//	 * id							int
//	 * name							string
//	 * level						byte
//	 * faction						byte
//	 * state						short
//	 */
//	public static final short UNIT_DETAIL_SERVER = 194;
	
	/**
	 * UNIT行走信息
	 * type							byte 起始的5位分别代表一下的5段内容是否包含
	 * instanceId					int
	 * mapid						short (第一段)(最高位表示是否有mapInstanceId)
	 * mapInstanceId				int
	 * x							short (第一段)
	 * y							short (第一段) 
	 * 角度							byte（角度/2）(第二段)
	 * 时间							int(从系统启动开始计算的毫秒速) (第二段)
	 * 速度							byte(每秒的像素） (第二段)
	 * hp百分比						byte(200为单位)	(第三段)
	 * mp百分比						byte(200为单位) (第三段)
	 * state						short (第四段) (0 running 1 attack 2 ride 3 die 4 组队 5 队长 6 恐惧 7 麻痹 8 定身 9 同阵营PVP 10 不同阵营PVP)
	 * 第五段的Mask					byte  (只有第五段存在时次字段才存在 0 name 1 level 2 faction 3 装备分数 4 sex 5 owner 6 clazz 7 马)
	 * name							string (第五段 0)
	 * level						byte (第五段 1)
	 * faction						byte (第五段 2)
	 * headscore					int  (第五段 3)
	 * bodyscore					int  (第五段 3)
	 * weaponscore					int  (第五段 3)
	 * flash						byte (第五段 3)
	 * sex							byte (第五段 4)
	 * owner						int  (第五段 5)
	 * clazz						byte (第五段 6)
	 * houseImageId					int
	 * housescore					int
	 * isFindPath					byte(0是非寻路状态、1寻路状态)
	 */
	public static final short UNIT_MOVE_SERVER = 195;
	
	
	/**
	 * 请求unit信息
	 * instanceId					int
	 */
	public static final short UNIT_INFO_CLIENT = 196;
	
	/**
	 * unit信息
	 * instanceId					int
	 * 如果是NPC
	 * 	通过性						byte
	 * 	是否是功能NPC				byte
	 * 	功能名字						string
	 * 如果是其他玩家
	 * 	工会							string
	 * 	荣誉							string
	 *  称号							string
	 * 如果是资源
	 * 	任务ID						int (如果小于0，那么跟任务无关)
	 *  采集时间						int
	 */
	public static final short UNIT_INFO_SERVER = 197;
	
	/**
	 * 开始采集
	 * serial						int
	 * instanceId					int
	 */
	public static final short GATHER_START_CLIENT = 198;
	
	/**
	 * 下发小提示
	 * message                      String
	 */
	public static final short PUSH_HINT_SERVER = 199;
	
	public static final short RELOAD_CLIENT = 200;
	/**
	 * 转让队长职位(队长有效)
	 * 目标成员Id						int
	 */
	public static final short PARTY_TRANSFER_LEADER_CLIENT = 240;
	/**
	 * 请求添加好友/黑名单/仇人。
	 * serial	int
	 * id		int			玩家ID，-1表示不使用此参数
	 * name		String		玩家名称，空串表示不使用此参数
	 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
	 */
	public static final short ADD_FRIEND_CLIENT = 241;
	/**
	 * 添加好友/黑名单/仇人成功。
	 * serial	int
	 * id		int			玩家ID
	 * name		String		玩家名称
	 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
	 * degree	int			好友度/仇人度
	 * onine	boolean		是否在线，只有在线玩家的下面4个参数才有效
	 * level	short		级别
	 * sex		byte		性别
	 * clazz	byte		职业
	 * tong		String		军团
	 * faction	byte		阵营
	 * delId    int         删除玩家的id   ：delId = -1 不需要删除       delId != -1  要删除的角色id
	 */
	public static final short ADD_FRIEND_SERVER = 242;
	/**
	 * 请求删除好友/黑名单/仇人。
	 * serial	int
	 * id		int			玩家ID
	 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
	 */
	public static final short DEL_FRIEND_CLIENT = 243;
	/**
	 * 删除好友/黑名单/仇人成功。
	 * serial	int
	 * id		int			玩家ID
	 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
	 */
	public static final short DEL_FRIEND_SERVER = 244;
	/**
	 * 取关联玩家列表。
	 * serial	int
	 * type		byte        类型：0 - 好友、1 - 黑名单、2 - 仇人、3 - 临时
	 */
	public static final short GET_FRIENDLIST_CLIENT = 245;
	/**
	 * 返回关联玩家列表。
	 * serial	int
	 * count	int			列表大小
	 * 	循环N次
	 * 		id		int			玩家ID
	 * 		name	String		玩家名称
	 * 		type	byte		类型：0 - 好友、1 - 黑名单、2 - 仇人、3 - 临时
	 * 		degree	int			好友度/仇人度/临时好友交互类型
	 * 		onine	boolean		是否在线
	 * 		level	short		级别
	 * 		sex		byte		性别
	 * 		clazz	byte		职业
	 * 		tong	String		军团
	 *      faction	byte		阵营
	 *      isLock  byte        是否锁定    0：未锁定    1：锁定
	 *      inParty	byte		是否有队伍    0：没有     1：有
	 */
	public static final short GET_FRIENDLIST_SERVER = 246;
	/**
	 * 好友/仇人上下线通知。
	 * id		int			好友/仇人ID
	 * name		String		好友/仇人名称
	 * online	byte		1 - 上线，0 - 下线 
	 */
	public static final short FRIEND_ONLINE_SERVER = 247;
	
	/**
	 * 查看仇人方位
	 * serial     int
	 * id         int         仇人ID
         
	 */
	public static final short ENEMY_POSITION_CLIENT = 248;
	
	/**
	 * 查看仇人方位成功
	 * serial     int
	 * mapName    String        地图名称
	 * position   String        方位         
	 */
	public static final short ENEMY_POSITION_SERVICE = 249;
	
	/**
	 * 发送聊天信息
	 * channel						byte  0 世界 1 国家 2 地区 3 同乡 4 帮派 5 队伍 6 私聊 7 系统(系统频道不可用，私聊需要加上对方Id，其他忽略)
	 * destId						int
	 * message						string
	 * attachment					byte[] 如果是物品{01(byte),itemId(int),instanceId(int)},如果是任务{02{byte},questId(int)}
	 * 									   如果是成就{03(byte),type(byte),title(String)}
	 * 									   如果是卡片{04(byte),,title(String),level(byte),quality(byte)}
	 */
	public static final short CHAT_CLIENT = 201;
	
	/**
	 * 聊天信息
	 * channel						byte
	 * sourceId						int 
	 * 职位							byte (-1 没有职位 0 国公 1 丞相 2 御史大夫 3 大司马 4 骠骑将军)
	 * name							string 
	 * message						string
	 * playerLevel					int 玩家等级	
	 * attachment					byte[] 如果是物品{01(byte),itemId,instanceId(int),name(string),showType(byte),quality(byte)},如果是任务{02{byte},questId(int,name(string)}
	 * 									   如果是成就{03(byte),type(byte),title(String)}
	 */
	public static final short CHAT_SERVER = 202;
	
	/**
	 * 聊天信息设置
	 * serial						int
	 * 改变了的聊天设置数量			byte
	 * 重复n次
	 * 	聊天频道						byte
	 * 	聊天设置						byte  (0~3 颜色的Index,4 是否加入该频道 5 收到信息是否提示)
	 * 
	 */
	public static final short CHAT_OPTION_CLIENT = 203;
	
	/**
	 * 聊天信息设置成功
	 * serial						int
	 */
	public static final short CHAT_OPTION_SERVER = 204;
	
	/**
	 * 改变同乡信息
	 * serial						int
	 * 地区							string
	 */
	public static final short CHAT_NATIVE_CHANGE_CLIENT = 205;

	/**
	 * 改变同乡信息成功
	 * serial						int
	 */
	public static final short CHAT_NATIVE_CHANGE_SERVER = 206;
	
	/**
	 * 取玩家信息
	 * serial 						int
	 * type							byte 0普通请求，1挑战页面请求玩家信息
	 * 玩家Id						int
	 */
	public static final short PLAYER_INFO_CLIENT = 207;
	
	/**
	 * 玩家信息
	 * serial						int
	 * 角色名称						String
	 * 级别							byte
	 * 职业							byte
	 * 阵营							byte
	 * 军团							string
	 * 家乡							string
	 * 称号							string
	 * 师傅							string
	 * 夫妻							string
	 * 恩爱度						int
	 * 军衔							string
	 * 装备信息						byte[]
	 * headscore					int
	 * bodyscore					int
	 * weaponscore					int
	 * 性别							byte
	 * 行动力						int
	 * 战功							int
	 * 周战功						int
	 * 是否骑马						byte		（1为上骑，0为下骑）
	 * 当前马信息					horse(horse.toClientBytes(player))
	 * 血盟名称						String
	 * 是否召唤随从					byte		（1已召唤，0未召唤）
	 * 当前随从信息					attendant(attendant.toClientBytes(player))
	 * 是否有七彩光效                byte    (0:没有    1：有)
	 * 每日贡献度上线				int
	 * 日贡献度						int
	 * 总贡献度						int
	 * size							byte	装备位镶嵌卡片数量
	 *		null					byte	装备位是否镶有卡片（0否 1是）
	 *		cardId					int		卡片ID
	 *		cardName				String	卡片名字
	 *		level					byte	卡片级别
	 *		att						String	属性增强描述
	 * size							byte	坐骑装备位镶嵌卡片数量
	 *		null					byte	坐骑装备位是否镶有卡片（0否 1是）
	 *		cardId					int		卡片ID
	 *		cardName				String	卡片名字
	 *		level					byte	卡片级别
	 *		att						String	属性增强信息
	 */
	public static final short PLAYER_INFO_SERVER = 208;
	
	/**
	 * 创建队伍
	 * serial						int
	 */
	public static final short PARTY_CREATE_CLIENT = 209;
	
	/**
	 * 创建队伍成功
	 * serial						int
	 */
	public static final short PARTY_CREATE_SERVER = 210;

	/**
	 * 邀请加入队伍
	 * id							int
	 */
	public static final short PARTY_INVIT_CLIENT = 211;

	/**
	 * 邀请加入队伍
	 * 邀请序号						int
	 * 队长Id						int
	 * 队长名字						string
	 * 队长等级						byte
	 * 队长职业						byte
	 * 队长性别                     byte
	 */
	public static final short PARTY_INVIT_SERVER = 212;
	
	/**
	 * 答应组队请求
	 * 邀请序号						int
	 */
	public static final short PARTY_INVIT_OK_CLIENT = 213;
	
	/**
	 * 拒绝组队请求
	 * 邀请序号						int
	 */
	public static final short PARTY_INVIT_REJECT_CLIENT = 214;
	
	/**
	 * 拒绝组队请求
	 * 拒绝人名字					string
	 * 拒绝原因						string
	 */
	public static final short PARTY_INVIT_REJECT_SERVER = 215;

	/**
	 * 小队信息
	 * 小队成员数量					byte
	 * 循环n次
	 * 	成员Id						int
	 *  成员名字						string
	 *  成员等级						byte
	 *  成员职业						byte
	 * 	hp百分比						byte(200为单位) 
	 * 	mp百分比						byte(200为单位)
	 *  成员状态						byte(最高位为1表明是队长,第0位为1表明现在离线)
	 */
	public static final short PARTY_INFO_SERVER = 216;
	
	/**
	 * 踢出队员(队长有效)
	 * 成员Id						int
	 */
	public static final short PARTY_KICK_CLIENT = 217;
	
	
	/**
	 * 被踢出队伍
	 */
	public static final short PARTY_KICK_SERVER = 218;

	/**
	 * 离开队伍
	 */
	public static final short PARTY_LEAVE_CLIENT = 219;
	
	/**
	 * 离开队伍成功
	 */
	public static final short PARTY_LEAVE_SERVER = 220;
	
	/**
	 * 发送邮件
	 * serial							int
	 * destName							string
	 * title							string
	 * content							string
	 * price							int
	 * attachment						byte[]如果是物品{1(byte),itemId(int),instanceId(int),count(byte)}，如果是金钱{2(byte),count(int)}
	 */
	public static final short MAIL_POST_CLIENT = 221;
	
	/**
	 * serial							int
	 */
	public static final short MAIL_POST_SERVER = 222;
	
	/**
	 * 获取Mail列表
	 * serial							int
	 * type								short 0:系统邮件   1：玩家  2：付费邮件列表
	 * 每页条数							short
	 * 页数								short
	 */
	public static final short MAIL_LIST_CLIENT = 223;
	
	/**
	 * 收件箱Mail列表
	 * serial							int
	 * 每页条数							short
	 * 页数								short
	 * 信件的总数						int
	 * 本页实际条数						short
	 * 循环n次
	 *  mailId							int
	 * 	sourceId						int
	 *  destId							int
	 * 	sourceName						string
	 * 	title							string
	 *  date							string
	 *  expirationTime                  string
	 * 	status							byte (0位标识是否看过 1位标识是否收藏)
	 * 	是否有附件						byte (0 没有 1 有)
	 * 	付费钱数							int
	 */	
	public static final short MAIL_LIST_SERVER = 224;
	
	/**
	 * 获取Mail内容
	 * serial							int
	 * mailId							int
	 */
	public static final short MAIL_CONTENT_CLIENT = 225;
	
	/**
	 * Mail内容
	 * serial							int
	 * mailId							int
	 * sourceId							int
	 * sourceName						string
	 * title							string
	 * content							string
	 * date								string
	 * expirationTime                   string
	 * price							int
	 * attachment						byte[]如果是物品{1(byte),itemId(int),instanceId(int),count(byte),name(string),showType(short),quality(byte)}，如果是金钱{2(byte),count(int)}
	 */
	public static final short MAIL_CONTENT_SERVER = 226;

	/**
	 * 提取附件
	 * serial							int
	 * mailId							int
	 */
	public static final short MAIL_ATTACHMENT_CLIENT = 227;
	
	/**
	 * 提取附件成功
	 * serial							int
	 * mailId							int
	 * 是否删除							byte(0 没删除 1 删除)
	 */
	public static final short MAIL_ATTACHMENT_SERVER = 228;
	
	/**
	 * 删除邮件
	 * serial							int
	 * mailId							int 如果是-1那么代表删除非收藏邮件
	 */
	public static final short MAIL_DELETE_CLIENT = 229;
	/**
	 * 删除邮件成功
	 * serial							int
	 * mailId							int
	 */
	public static final short MAIL_DELETE_SERVER = 230;
	
	/**
	 * 收藏邮件
	 * serial							int
	 * mailId							int
	 */
	public static final short MAIL_FAVORITE_CLIENT = 231;
	/**
	 * 搜藏邮件成功
	 * serial							int
	 * mailId							int
	 */
	public static final short MAIL_FAVORITE_SERVER = 232;
	
	/**
	 * 有新邮件
	 */
	public static final short MAIL_NEW_SERVER = 233;
	
	
	/**
	 * 脚本名字								string
	 * 参数字符串							string
	 */
	public static final short OPENUI_SERVER = 235;
	
	/**
	 * 获取动作条Option
	 */
	public static final short ACTIONBAR_OPTION_CLIENT = 236;
	/**
	 * 动作条Option
	 * options								byte[]
	 */
	public static final short ACTIONBAR_OPTION_SERVER = 237;
	
	/**
	 * 设置动作条Option
	 * serial								int
	 * options								byte[]
	 */
	public static final short SET_ACTIONBAR_OPTION_CLIENT = 238;
	
	/**
	 * 设置动作条Option成功
	 * serial								int
	 */
	public static final short SET_ACTIONBAR_OPTION_SERVER = 239;
	
	
	
	/**
	 * 请求创建军团。
	 * serial	int
	 * name		String		军团名称
	 */
	public static final short TONG_CREATE_CLIENT = 251;
	/**
	 * 创建军团成功。
	 * serial	int
	 * id		int			军团ID
	 * name		int			军团名称
	 * duty		int			军团职务
	 */
	public static final short TONG_CREATE_SERVER = 252;
	/**
	 * 军团成员属性变更通知（可能是自己的，也可能是别人的）。
	 * id		int			角色ID
	 * tid		int			军团ID，-1表示无军团
	 * tname	String		军团名称
	 * duty		int			军团职务
	 * title	String		军团头衔
	 * forbid	byte		是否禁言，1 - 是，0 - 否
	 */
	public static final short TONG_MEMBER_CHANGE_SERVER = 253;
	/**
	 * 取军团成员列表。
	 * serial	int
	 * start	short		页号，0表示第一页
	 * page		short		页大小
	 */
	public static final short TONG_LIST_CLIENT = 254;
	/**
	 * 返回军团成员列表。
	 * serial	int
	 * tid		int			军团ID
	 * tname	String		军团名称
	 * slogan	String		军团公告
	 * duty		int			本用户的职务
	 * title	String		本用户的头衔
	 * pcount	short		总页数
	 * pno		short		当前页号（0表示第一页）
	 * count	short		返回记录数
	 * ratio	String		在线比例
	 * 	循环N次
	 *		pid		int			角色ID
	 *		pname	String		角色名称
	 *		online	byte		是否在线，0 - 不在线、1 - 在线
	 *		level	short		级别（只对在线用户有效）
	 *		sex		byte		性别（只对在线用户有效）
	 *		clazz	byte		职业（只对在线用户有效）
	 *		duty	int			职务
	 *		title	String		头衔
	 *		honor	int			荣誉
	 *		forbid	byte		是否禁言，1 - 禁言、0 - 未禁言
	 *		tag		byte		是否被标记 0，否  1，是
	 */
	public static final short TONG_LIST_SERVER = 255;
	/**
	 * 邀请加入军团。
	 * serial	int
	 * tid		int			目标角色ID，-1表示无效
	 * tname	int			目标角色名称
	 */
	public static final short TONG_INVITE_CLIENT = 256;
	/**
	 * 邀请发送成功。
	 * serial	int
	 */
	public static final short TONG_INVITE_SERVER = 257;
	/**
	 * 向被邀请加入军团的角色发送的通知。
	 * sid		int			邀请人ID
	 * sname	String		邀请人名称
	 * tid		int			邀请人军团ID
	 * tname	String		邀请人军团名称
	 * invid	int			请柬ID
	 */
	public static final short TONG_INVITATION_SERVER = 258;
	/**
	 * 请求加入军团。
	 * serial	int
	 * invid	int			请柬ID
	 */
	public static final short TONG_JOIN_CLIENT = 259;
	/**
	 * 加入军团成功。
	 * serial	int
	 * tid		int			军团ID
	 * tname	String		军团名称
	 * duty		int			军团职务
	 */
	public static final short TONG_JOIN_SERVER = 260;
	/**
	 * 拒绝军团邀请。
	 * invid	int			请柬ID
	 */
	public static final short TONG_REJECT_CLIENT = 261;
	
	/**
	 * 请求退出军团。（老协议改造）
	 * serial						int
	 * type     					int  0:不使用完美重置符      1：使用完美重置符
	 */
	public static final short TONG_QUIT_CLIENT = 262;
	
	/**
	 * 退出军团成功。
	 * serial	int
	 */
	public static final short TONG_QUIT_SERVER = 263;
	/**
	 * 请求修改军团公告。
	 * serial	int
	 * slogan	String		新军团公告
	 */
	public static final short TONG_SET_SLOGAN_CLIENT = 264;
	/**
	 * 修改军团公告成功。
	 * serial	int
	 * slogan	String		新军团公告
	 */
	public static final short TONG_SET_SLOGAN_SERVER = 265;
	/**
	 * 请求提升/降职（包括转让都督职务）。
	 * serial	int
	 * tid		int			目标角色ID
	 * op		byte		0 - 升职、1 - 降职
	 */
	public static final short TONG_PROMOTE_CLIENT = 266;
	/**
	 * 提升/降职成功。
	 * serial	int
	 * duty		int			请求者的新职务
	 * tid		int			目标角色ID
	 * tduty	int			目标角色的新职务
	 */
	public static final short TONG_PROMOTE_SERVER = 267;
	/**
	 * 请求移除军团成员。
	 * serial	int
	 * tid		int			目标角色ID
	 */
	public static final short TONG_KICK_CLIENT = 268;
	/**
	 * 移除军团成员成功。
	 * serial	int
	 * tid		int			目标角色ID
	 */
	public static final short TONG_KICK_SERVER = 269;
	/**
	 * 请求禁言/解除禁言某军团成员。
	 * serial	int
	 * tid		int			目标角色ID
	 */
	public static final short TONG_FORBID_CLIENT = 270;
	/**
	 * 禁言/解除禁言成功。
	 * serial	int
	 * tid		int			目标角色ID
	 * forbid	byte		目标的新禁言状态，1 - 禁言、0 - 未禁言
	 */
	public static final short TONG_FORBID_SERVER = 271;
	
	/**
	 * 申请军团改名。
	 * serial	int
	 * name		String		新名称
	 */
	public static final short TONG_RENAME_CLIENT = 697;
	
	/**
	 * Symbian版本更新地址
	 * url							string
	 * sisname						string
	 */
	public static final short SYMBIAN_UPDATE_URL_SERVER = 272;
	
	/**
	 * roll点
	 * rollId								int
	 * 方式									byte (0 放弃 1 roll)
	 */
	public static final short ROLL_CLIENT = 280;
	
	/**
	 * 下发可roll物品
	 * 	rollId								int
	 *  物品									ITEM
	 *  到期时间								int
	 */
	public static final short ROLL_SERVER = 281;
	
	/**
	 * 发起PK
	 * 对方ID								int
	 * 赌注									int
	 */
	public static final short PK_INVIT_CLIENT = 282;
	
	/**
	 * PKID									int
	 * 发起PK玩家Id							int
	 * 发起PK玩家名字						string
	 * 发起PK玩家等级						byte
	 * 发起PK玩家职业						byte
	 * 赌注									int
	 */
	public static final short PK_INVIT_SERVER = 283;
	
	/**
	 * 拒绝PK
	 * PKID									int
	 */
	public static final short PK_REFUSE_CLIENT = 284;
	
	/**
	 * 拒绝PK
	 * 拒绝PK原因							string
	 */
	public static final short PK_REFUSE_SERVER = 285;
	
	/**
	 * 同意PK
	 * PKID									int
	 */
	public static final short PK_OK_CLIENT = 286;
	
	/**
	 * PK建立成功
	 * 源InstanceId							int
	 * 目标InstanceId						int
	 * 赌注									int
	 * 中心点x坐标							int
	 * 中心点y坐标							int
	 * PK范围(正方形半径)					int
	 */
	public static final short PK_OK_SERVER = 287;
	
	/**
	 * PK结束
	 * 结束类型								byte(0 正常结束 1 打断)
	 * 获胜方InstanceId						int
	 */
	public static final short PK_OVER_SERVER = 288;
	
	/**
	 * Buff列表
	 * Buffs									BUFFS
	 */
	public static final short BUFFLIST_SERVER = 290;
	
	/**
	 * InstanceId							int
	 * buff数量								byte	
	 * 循环n次
	 * 	instanceId							int
	 *  IconId								int
	 *  time								int
	 */
	public static final short SYNC_BUFF_SERVER = 291;
	
	/**
	 * 冷却组CoolDown
	 * 冷却组Id								short
	 * 开始时间								int
	 * 结束时间								int
	 */
	public static final short COOLDOWN_SERVER = 292;
	
	/**
	 * 冷却组消失
	 * 数量									byte
	 * 循环n次
	 * 	冷却组ID								short
	 */
	public static final short COOLDOWN_END_SERVER = 293;
	
	/**
	 * 玩家死亡消息
	 * 释放的到期时间						int
	 * 复活选项数量							byte
	 * 循环n次
	 * 	复活Id								int
	 * 	选项显示字符串						string
	 */
	public static final short DIE_SERVER = 294;
	
	
	/**
	 * 复活
	 * 复活Id								int
	 */
	public static final short RELIVE_CLIENT = 295;
	
	/**
	 * 通知玩家复活
	 * InstanceId							int
	 * 复活时的地图mapId						int
	 * 复活时的mapInstanceId					int
	 * 复活时的x坐标							int
	 * 复活时的y坐标							int
	 * 复活所使用的动画						int				
	 */
	public static final short RELIVE_SERVER = 296;
	
	/**
	 * 技能攻击
	 * 源InstanceId					int
	 * 目标InstanceId				int
	 * 起手动画ID                   int
	 * 
	 */
	public static final short SKILL_PREPARE_ATTACK_SERVER = 297;
	
	
	/**
	 * 取商店商品列表
	 * serial		int
	 * shopIDs		int[]		商店ID列表
	 */
	public static final short SHOP_LIST_CLIENT = 300;
	/**
	 * 返回商店商品列表
	 * serial		int
	 * count		byte		商店数量
	 * 循环N次
	 *   shopID		short		商店ID
	 *   title		String		商店标题
	 *   itemCount	byte		商品数量
	 *   循环N次
	 *     id		int			物品ID
	 *     name		String		物品名称
	 *     quality	byte		品质
	 *     image	byte		图标图片ID
	 *     icon		byte		图标ID
	 *     remain	short		剩余数量，0表示无限制
	 *     limit	byte		购买上限，0表示无限制
	 *     reqcnt	byte		购买需求项数量，0表示无限制
	 *       循环N次
	 *         reqtype	byte	需求类型，见Shop类中的常量
	 *         amount	String	需求金钱/i币/荣誉，或物品数量，或军衔ID
	 *         deduct	byte	是否扣除，1表示是，0表示否
	 *         rank		String	需求荣誉名称，只有需求类型为TYPE_RANK时有效
	 *         itemID	int		物品ID，只有需求类型为TYPE_ITEM时有效
	 *         itemName	String	物品名称，只有需求类型为TYPE_ITEM时有效
	 *         quality	byte	品质，只有需求类型为TYPE_ITEM时有效
	 *         icon		byte	图标ID，只有需求类型为TYPE_ITEM时有效
	 *         varDesc  String  额外条件描述，只有需求类型为TYPE_VARIABLE时有效
	 *  税率			int (百分比*100)
	 */
	public static final short SHOP_LIST_SERVER = 301;
	/**
	 * 请求购买商品
	 * serial		int
	 * shopID		int		商店ID
	 * itemID		int			物品ID
	 * count		short		购买数量
	 */
	public static final short SHOP_BUY_CLIENT = 302;
	/**
	 * 购买商品成功
	 * serial		int
	 * itemID		int			物品ID
	 * count		short		购买数量
	 * itemName		String		物品名称
	 * quality		byte		品质
	 * iconImage	byte		图标图片ID
	 * icon			byte		图标ID
	 */
	public static final short SHOP_BUY_SERVER = 303;
	/**
	 * 请求出售物品
	 * serial		int
	 * gridId		byte		包格Id
	 * itemID		int 		物品ID
	 * instanceID	int			实例ID
	 * count		short		物品数量
	 */
	public static final short SHOP_SELL_CLIENT = 304;
	/**
	 * 出售物品成功
	 * serial		int
	 * amount		int			获得金钱数量
	 */
	public static final short SHOP_SELL_SERVER = 305;
	
	/**
	 * 删除角色
	 * 角色Id								int
	 */
	public static final short ACTOR_DELETE_CLIENT = 310;
	
	/**
	 * 删除角色成功
	 * 角色Id								int
	 */
	public static final short ACTOR_DELETE_SERVER = 311;
	
	/**
	 * 取buff描述
	 * instanceId							int
	 * 数量									byte
	 * 循环n次
	 * 	buffinstanceId							int
	 */
	public static final short BUFF_DESC_CLIENT = 312;
	
	/**
	 * buff描述
	 * instanceId							int
	 * 数量									byte
	 * 循环n次
	 * 	buffinstanceId							int
	 *  name								string
	 * 	desc								string
	 */
	public static final short BUFF_DESC_SERVER = 313;
	
	/**
	 * 取消自动攻击
	 */
	public static final short CANCEL_AUTOATTACK_CLIENT = 314;
	
	/**
	 * 自动攻击开始
	 * 自动攻击对象InstanceId				int
	 */
	public static final short AUTOATTACK_START_SERVER = 315;
	
	/**
	 * 注册帐号
	 * serial								int
	 * 帐号名								string
	 * 电话号码								string
	 * model								string
	 * version								string
     * 手机号(可空)                          string
     * 初始密码								string
     * cmccUserId							string(只有电信以及卓望版本有此字段)
     * cmccUserKey							string(只有电信以及卓望版本有此字段)
	 */
	public static final short ACCOUNT_REG_CLIENT = 316;
	
	/**
	 * 注册帐号成功
	 * serial								int
	 * 帐号名								string
	 * 帐号Id								int
	 * 密码									string
	 * 角色ID								int   -1表示尚未创建角色
	 */
	public static final short ACCOUNT_REG_SERVER = 317;
	
	/**
	 * 取消攻击
	 */
	public static final short CANCEL_ATTACK_CLIENT = 318;
	
	/**
	 * 取消攻击通知。
	 * 源InstanceId					int
	 */
	public static final short CANCEL_ATTACK_SERVER = 318;
	
	
	/**
	 * 取消使用物品
	 */
	public static final short CANCEL_USEITEM_CLIENT = 319;
	
	/**
	 * 队友touch了地图出口
	 * exitId								int
	 */
	public static final short PARTY_TOUCHED_EXIT_SERVER = 320;
	
	/**
	 * 强制过地图
	 * 目标地图id			int
	 * 目标地图InstanceId	int
	 * 目标地图x				int
	 * 目标地图y				int
	 * 是否允许使用跟随		byte 	(1 允许 0 不允许)
	 */
	public static final short FORCE_GOMAP_SERVER = 321;
	
	/**
	 * 快速注册帐号
	 * serial								int
	 * 电话号码								string
	 * model								string
	 * version								string
     * 手机号(可空)                          string
     * IMEI									String			手机串号
	 */
	public static final short ACCOUNT_QUICK_REG_CLIENT = 322;
	
	/**
	 * 修理装备
	 * serial								int
	 * type                                 int 0 - 身上装备，1 - 包内，2 - 所有
	 */
	public static final short REPAIR_CLIENT = 323;
	
	/**
	 * 修改性别
	 * serial								int
	 * sex									int(0 男 1 女)
	 */
	public static final short CHANGE_SEX_CLIENT = 324;
	
	/**
	 * 修改性别成功
	 * serial								int
	 * sex									int
	 */
	public static final short CHANGE_SEX_SERVER = 325;
	
	/**
	 * 修改名字
	 * serial								int
	 * type                                 byte
	 * name									string
	 */
	public static final short CHANGE_NAME_CLIENT = 326;
	
	/**
	 * 修改名字成功
	 * serial								int
	 * name									string
	 */
	public static final short CHANGE_NAME_SERVER = 327; 
	
	/**
	 * 修改职业
	 * serial								int
	 * class								int(0 武将 1 刺客 2 谋士 3 方士
	 */
	public static final short CHANGE_CLASS_CLIENT = 328;
	
	/**
	 * 修改职业成功
	 * serial								int
	 * class								int
	 */
	public static final short CHANGE_CLASS_SERVER = 329;
	
	/**
	 * 修改国家
	 * serial								int
	 * faction								int(1 魏 2 蜀 3 吴)
	 */
	public static final short CHANGE_FACTION_CLIENT = 330;
	
	/**
	 * 修改国家成功
	 * serial								int
	 * faction								int
	 */
	public static final short CHANGE_FACTION_SERVER = 331;
	
	/**
	 * 获取任务未完成描述
	 * 任务id			int

	 */
	public static final short QUEST_UNFINISHDESC_CLIENT = 332;
	
	
	/**
	 * 任务未完成描述
	 * 任务id						int
	 * 描述							string
	 * 任务目标的数量				byte
	 * 奖励组数量					byte
	 * 循环n次
	 	奖励组Id						byte
	 	奖励数量						byte
	 	循环n次
	 		奖励类型						byte
	 		奖励内容						int,ITEM(属性值，如果类型是101，那么这个字段就是ITEM)
	 */
	public static final short QUEST_UNFINISHDESC_SERVER = 333;
	
	/**
	 * 修改密码
	 * serial							int
	 * 旧密码							string
	 * password							string
	 */
	public static final short CHANGE_PASSWORD_CLIENT = 334;
	
	/**
	 * 修改密码成功
	 * serial							int
	 * 新密码							string
	 */
	public static final short CHANGE_PASSWORD_SERVER = 335;
	
	/**
	 * 追击
	 * sourceId							int
	 * targetId							int (如果停止追击值为-1)
	 * 停止范围							short
	 * 开始追击的x坐标					short
	 * 开始追击的y坐标					short
	 * 追击速度							byte
	 * 追击停止距离                      short
	 */
	public static final short CHASE_SERVER = 336;
	
	/**
	 * 修理装备成功
	 * serial								int
	 * money                                int 费用
	 */
	public static final short REPAIR_SERVER = 337;
	
	/**
	 * 脱离卡死
	 */
	public static final short OUT_PRISON_CLIENT = 338;
	
	/**
	 * 显示HPMP变化
	 * 目标InstanceId				int
	 * 伤害类型     					byte     0 物理 1 法术 2 抽蓝 3 诅咒 4 加血 5 回蓝 6 增强
	 * 伤害值						int		只有在命中时有意义
	 */
	public static final short SHOW_HPMP_SERVER = 339;
	
	/**
	 * 使用物品失败
	 * itemId						int
	 * cause						string
	 */
	public static final short USEITEM_FAIL_SERVER = 340;
	
	/**
	 * 解除队伍
	 */
	public static final short PARTY_DESTORY_CLIENT = 341;
	
	/**
	 * 队伍已经解除
	 */
	public static final short PARTY_DESTORY_SERVER = 342;
	
	/**
	 * 接任务失败
	 * questId							int
	 * 失败原因							string
	 */
	public static final short QUEST_ACCEPT_FAIL_SERVER = 343;
	
	/**
	 * 交换包格
	 * serial								int
	 * sourceGridId							short
	 * targetGridId							short
	 */
	public static final short GRID_EXCHANGE_CLIENT = 344;
	
	/**
	 * 交换包格成功
	 * serial								int
	 */
	public static final short GRID_EXCHANGE_SERVER = 345;
	
	/**
	 * 呼叫GM
	 * serial								int
	 * 玩家申请								string
	 */
	public static final short GM_CALL_CLIENT = 346;
	
	/**
	 * 呼叫GM成功
	 * serial								int
	 */
	public static final short GM_CALL_SERVER = 347;
	
	
	/**
	 * 强制去MOVE信息
	 * instanceId							int
	 */
	public static final short GET_MOVE_CLIENT = 348;
	
	/**
	 * 喊话
	 * message								string
	 * color                                int
	 * time                                 int(毫秒)
	 */
	public static final short SHOUT_SERVER = 349;
	
	/**
	 * 寻找道路
	 * serial								int
	 * mapid								short
	 * x									short
	 * y									short
	 */
	public static final short FINDPATH_CLIENT = 350;
	
	/**
	 * 寻找道路返回
	 * serial								int
	 * 数量									byte
	 * 循环n次
	 * 	mapId								short
	 *  x									short
	 *  y									short
	 */
	public static final short FINDPATH_SERVER = 351;
	
	/**
	 * 发起交易
	 * serial								int
	 * targetId								int
	 */
	public static final short EXCHANGE_INVIT_CLIENT = 352;
	
	/**
	 * 交易请求
	 * 请求Id								int
	 * sourceName							string
	 */
	public static final short EXCHANGE_INVIT_SERVER = 353;
	
	/**
	 * 接受交易请求
	 * 请求Id								int
	 */
	public static final short EXCHANGE_INVIT_OK_CLIENT = 354;
	
	/**
	 * 拒绝交易
	 * 请求Id								int
	 */
	public static final short EXCHANGE_INVIT_REFUSE_CLIENT = 355;
	
	/**
	 * 拒绝交易
	 * serial								int
	 * message								string
	 */
	public static final short EXCHANGE_INVIT_REFUSE_SERVER = 356;
	
	/**
	 * 交易信息
	 * 交易ID								int
	 * sourceId								int
	 * 物品栏数量							byte
	 * 循环N次
	 * 	数量									short
	 *  物品									ITEM（如果数量为0，没有此字段)
	 * 金钱数量								int
	 * 状态									byte(0 未确认 1 确认)
	 * targetId								int
	 * 物品栏数量							byte
	 * 循环N次
	 * 	数量									short
	 * 	物品									ITEM(如果数量为0，没有此字段)
	 * 金钱数量								int
	 * 状态									byte(0 未确认 1 确认)
	 * 交易流水ID							int
	 */
	public static final short EXCHANGE_INFO_SERVER = 357;
	
	
	/**
	 * 增加交易物品
	 * serial								int
	 * ItemId								int (如果-1 那么是金钱)
	 * instanceId							int (如果-1 那么是金钱)
	 * count								int (数量)
	 */
	public static final short EXCHANGE_ADDITEM_CLIENT  = 358;
	
	/**
	 * 移除交易物品
	 * serial								int
	 * gridId								int (如果-1 那么是金钱)
	 * count								int (数量)			
	 */
	public static final short EXCHANGE_REMOVEITEM_CLIENT = 359;
	
	/**
	 * 确认交易
	 * serial								int
	 * 交易Id								int
	 * 交易流水Id							int
	 */
	public static final short EXCHANGE_ACCEPT_CLIENT = 360;
	
	/**
	 * 取消交易
	 * 交易Id								int
	 */
	public static final short EXCHANGE_CANCEL_CLIENT = 361;
	
	/**
	 * 交易完成
	 * 交易Id
	 * 原因									byte(0 正常完成 1 取消)
	 */
	public static final short EXCHANGE_COMPLETE_SERVER = 362;
	
	/**
	 * 交易时包格满
	 */
//	public static final short EXCHANGE_GRIDFULL_SERVER = 363;

	
	/**
	 * 获取Titles
	 * serial								int
	 */
	public static final short TITLES_GET_CLIENT = 365;
	
	/**
	 * Titles
	 * serial								int
	 * 当前展示Title						    short
	 * 当前装备Title                        short
	 * 其他称号数量							byte
	 * 	循环n次
	 * 	title								Title
	 * 官职称号数量
	 * 	循环n次
	 * 	title								Title
	 * 国家称号数量
	 * 	循环n次
	 * 	title								Title
	 */
	public static final short TITLES_SERVER = 366;
	
	/**
	 * 设置Title
	 * serial								int
	 * id									short (如果-1，那么是设置title为空)
	 */
	public static final short TITLE_SET_CLIENT = 367;
	
	/**
	 * 设置Title成功
	 * serial								int
	 * id									short(如果-1是卸下，否则是装配)
	 */
	public static final short TITLE_SET_SERVER = 368;
	
	/**
	 * 丢弃Title
	 * serial								int
	 * id									short
	 */
	public static final short TITLE_REMOVE_CLIENT = 369;
	
	/**
	 * 丢弃Title成功
	 * serial								int
	 * id									short
	 */
	public static final short TITLE_REMOVE_SERVER = 370;
	/**
	 * 获取指定的Title列表
	 * serial								int
	 * type									byte (0 其他称号 1 官职称号 2 国家称号)
	 */
	public static final short TITLE_LIST_CLIENT = 371;
	/**
	 * Title列表
	 * serial								int
	 * 数量									short
	 * 循环N次
	 * 	title								TITLE
	 *  当前是否拥有此Title					byte (0 没有 1 有)
	 */
	public static final short TITLE_LIST_SERVER = 372;
	
	/**
	 * 购买Title
	 * serial								int
	 * id									short
	 */
	public static final short TITLE_BUY_CLIENT = 373;
	
	/**
	 * 购买Title成功
	 * serial								int
	 */
	public static final short TITLE_BUY_SERVER = 374;
	
	
	/**
	 * 忘记技能书技能
	 * serial								int
	 * 技能Id								int
	 */
	public static final short FORGET_SKILL_CLIENT = 375;
	
	/**
	 * 忘记技能书技能成功
	 * serial								int
	 * 技能Id								int
	 */
	public static final short FORGET_SKILL_SERVER = 376;
	
	/**
	 * 获取Gift列表
	 * serial								int
	 * giftgroupId							int
	 */
	public static final short GIFT_LIST_CLIENT = 377;
	
	
	/**
	 * 获取Gift列表成功
	 * serial								int
	 * giftgroupId							int
	 * 数量									short
	 * 循环N次
	 * 	giftId								int
	 * 	描述									string
	 */
	public static final short GIFT_LIST_SERVER = 378;
	
	
	/**
	 * 获取Gift
	 * serial								int
	 * giftgroupId							int
	 * giftId								int
	 */
	public static final short GIFT_GET_CLIENT = 379;
	
	
	/**
	 * 获取Gift成功
	 * serial								int
	 * message                              String
	 */
	public static final short GIFT_GET_SERVER = 380;
	
	
	/**
	 * 获取内测奖励
	 * 内测账号								string
	 * 内测密码								string
	 */
	public static final short ALPHA_GIFT_CLIENT = 381;
	
	/**
	 * 客户端取配置请求
	 * serial								int
	 */
	public static final short CONFIG_CLIENT = 382;
	
	
	/**
	 * 客户端配置
	 * serial								int
	 * data									byte[]
	 */
	public static final short CONFIG_SERVER = 383;
	
	/**
	 * 保存客户端配置
	 * serial								int
	 * data									byte[]
	 */
	public static final short CONFIG_SAVE_CLIENT = 384;
	
	/**
	 * 保存客户端配置成功
	 * serail								int
	 */
	public static final short CONFIG_SAVE_SERVER = 385;
	
	/**
	 * 马装备
	 * serial						int
	 * ItemId						int
	 * instanceId					int
	 * horseInstanceId				int
	 */
	public static final short HORSE_EQUIP_CLIENT = 386;
	
	
	/**
	 * 马装备成功
	 * serial						int
	 */
	public static final short HORSE_EQUIP_SERVER = 387; 
	
	/**
	 * 上马
	 * serial						int
	 * instanceId					int
	 * change                       int(是否换装 0 换装，-1 不换)
	 */
	public static final short HORSE_RIDE_CLIENT = 388;
	
	/**
	 * 上马成功
	 * serial						int
	 */
	public static final short HORSE_RIDE_SERVER = 389;
	
	/**
	 * 喂食
	 * serial						int
	 * gridId						int
	 * itemId						int
	 * instanceId					int
	 * horseInstanceId				int
	 */
	public static final short HORSE_FEED_CLIENT = 390;
	
	/**
	 * 喂食成功
	 * serial						int
	 */
	public static final short HORSE_FEED_SERVER = 391;
	
	
	/**
	 * 装配喂食
	 * serial						int
	 * itemid						int (-1 卸下装备)
	 * horsetInstanceId				int
	 */
	public static final short HORSE_FOOD_CLIENT = 392;
	
	/**
	 * 装配喂食成功
	 * serial						int
	 */
	public static final short HORSE_FOOD_SERVER = 393;
	
	/**
	 * 丢弃坐骑
	 * serial						int
	 * instanceId					int
	 */
	public static final short HORSE_THROW_CLIENT = 394;
	
	/**
	 * 丢弃坐骑成功
	 * serial						int
	 */
	public static final short HORSE_THROW_SERVER = 395;
	
	/**
	 * 修改坐骑名字
	 * serial						int
	 * horseInstanceId					int
	 * name							string
	 */
	public static final short HORSE_CHANGENAME_CLIENT = 396;
	
	/**
	 * 修改坐骑名字成功
	 * serial						int
	 */
	public static final short HORSE_CHANGENAME_SERVER = 397;
	
	/**
	 * 替换坐骑技能
	 * serial						int
	 * horseInstanceId				int
	 * skillId						int
	 */
	public static final short HORSE_CHANGE_SKILL_CLIENT = 398;
	
	/**
	 * 替换坐骑技能成功
	 * serial						int
	 */
	public static final short HORSE_CHANGE_SKILL_SERVER = 399;
	
	/**
	 * 获取坐骑包信息
	 * serial						int
	 */
	public static final short HORSE_BAG_CLIENT = 400;
	
	/**
	 * 坐骑包信息
	 * serial						int
	 * data							HORSEBAG
	 */
	public static final short HORSE_BAG_SERVER = 401;
	
	/**
	 * 坐骑打包
	 * serial						int
	 * instanceId					int
	 */
	public static final short HORSE_PACK_CLIENT = 402;
	
	/**
	 * 坐骑打包成功
	 * serial						int
	 * instanceId					int
	 */
	public static final short HORSE_PACK_SERVER = 403;
	
	/**
	 * 卸下坐骑装备
	 * serial						int
	 * itemId						int
	 * instanceId					int
	 * horseInstanceId				int
	 */
	public static final short HORSE_UNEQU_CLIENT = 404;
	
	/**
	 * 卸下坐骑装备成功
	 * serial						int
	 */
	public static final short HORSE_UNEQU_SERVER = 405;
	
	/**
	 * 下马
	 * serial						int
	 */
	public static final short HORSE_UNRIDE_CLIENT = 406;
	/**
	 * 下马成功
	 * serial						int
	 */
	public static final short HORSE_UNRIDE_SERVER = 407;
	
	
	/**
	 * 领取俸禄
	 * serial						int
	 */
	public static final short TITLE_SALARY_CLIENT = 408;
	
	/**
	 * 领取俸禄成功
	 * serial						int
	 */
	public static final short TITLE_SALARY_SERVER = 409;
	
	
	/**
	 * 取消采集
	 */
	public static final short GATHER_CANCEL_CLIENT = 410;

	/**
	 * 采集被打断
	 * serial						int
	 * code							byte 1 采集物品已经不存在 2 遭受攻击  3主动打断 4等级不够 5拔棋时间限制
	 */
	public static final short GATHER_CANCLED_SERVER = 411;
	
	/**
	 * 切换地图以后的服务器确认，客户端在发送完LOADING_FINISH_CLIENT包以后等待次包，然后才将场景显示出来
	 * 
	 */
	public static final short VIEW_ACCEPT_SERVER = 412;
	
	/**
	 * 战场报名
	 * serial						int
	 * 战场Id						int
	 */
	public static final short BATTLEFIELD_SIGNUP_CLIENT = 413;
	
	/**
	 * 战场报名成功
	 * serial						int
	 * money						int
	 */
	public static final short BATTLEFIELD_SIGNUP_SERVER = 414;
	
	/**
	 * 战场传送
	 * 战场Id						int
	 * 战场描述						string
	 * 传送时间						int
	 */
	public static final short BATTLEFIELD_TRAN_SERVER = 415;
	
	/**
	 * 确认战场传送
	 * 战场Id						int
	 * type                         byte(0确定 1取消)
	 */
	public static final short BATTLEFIELD_TRAN_CLIENT = 416;
	
	/**
	 * 战场开始通知
	 * 通告内容						string
	 * 开始时间						int
	 */
	public static final short BATTLEFIELD_START_NOTIFY_SERVER = 417;
	
	/**
	 * 离开战场
	 */
	public static final short BATTLEFIELD_QUIT_CLIENT = 418;
	
	/**
	 * 查看当前战场信息
	 * 获胜阵营						byte
	 * 数量							short
	 * 循环n次
	 *  阵营							byte
	 * 	名字							string
	 * 	杀人数						short
	 * 	死亡次数						short
	 * 	归还军旗数					short
	 * 	积分							int
	 * 数量							short
	 * 循环n次
	 *  阵营							byte
	 * 	名字							string
	 * 	杀人数						short
	 * 	死亡次数						short
	 * 	归还军旗数					short
	 * 	积分							int
	 */
	public static final short BATTLEFIELD_INFO_CLIENT = 419;
	
	/**
	 * 地图信息
	 * mapId						short
	 * message						string
	 */
	public static final short MAP_INFO_SERVER = 420;
	
	/**
	 * 获取洗点需要的金钱数量
	 * serial						int
	 */
	public static final short SKILL_REFRESH_MONEY_CLIENT = 421;
	
	/**
	 * 洗点需要的金钱数量
	 * serial						int
	 * money						int
	 */
	public static final short SKILL_REFRESH_MONEY_SERVER = 422;
	
	/**
	 * 技能属性变化
	 * 数量							byte
	 * 循环n次
	 * 技能Id						int
	 * 技能cd时间					int
	 * 技能距离						short
	 * 技能施法时间					short
	 * 技能消耗魔法					short
	 */
	public static final short SKILL_INFO_CHANGED_SERVER = 423;
	

	
	/**
	 * 神州行充值
	 * serial						int
	 * 充值金额						int
	 * 充值卡号						string
	 * 充值卡密码					string
	 * 充值的角色名					string (如果是自己充值，这个字段是空字符串，如果为他人充值，是他人的角色名)
	 * 卡类型						byte(0 移动卡 1 联通卡 2电信)
	 */
	public static final short CHINARUN_CLIENT = 424;
	
	/**
	 * 神州行充值返回
	 * serial						int
	 * message						string
	 */
	public static final short CHINARUN_SERVER = 425;
	
	/**
	 * 激活码换物品
	 * serial						int
	 * code							string
	 */
	public static final short ACTIVATIONCODE_CLINET = 426;
	
	
	/**
	 * 激活码换物品成功
	 * serial						int
	 */
	public static final short ACTIVATIONCODE_SERVER = 427;
	
	/**
	 * 获取i币商店列表
	 * serial							int
	 */
	public static final short ISHOP_LIST_CLIENT = 428;
	
	/**
	 * i币商店列表
	 * serial							int
	 * arg								string
	 */
	public static final short ISHOP_LIST_SERVER = 429;
	
	/**
	 * 队长传送过地图
	 * mapId							int
	 * instanceId						int
	 */
	public static final short LEADER_TRAN_SERVER = 430;
	
	/**
	 * 整理背包
	 * serial							int
	 */
	public static final short BAG_ARRANGE_CLIENT = 431;
	
	/**
	 * 整理背包完成
	 * serial							int
	 * 循环n次							byte
	 * 	包格信息							GRID
	 */
	public static final short BAG_ARRANGE_SERVER = 432;
	
	/**
	 * 对装备进行星级鉴定
	 * serial							int
	 * itemId							int
	 * instanceId						int
	 * type								byte(0 无星级鉴定符 1 低级星级鉴定符 2 高级星级鉴定符 3 顶级星级鉴定符 4 重复鉴定符)
	 */
	public static final short STAR_ENHANCE_CLIENT = 433;
	
	/**
	 * 鉴定成功
	 * serial							int
	 * GameItem							byte[](参见后面的ITEM结构信息)
	 */
	public static final short STAR_ENHANCE_SERVER = 434;
	
	/**
	 * 获取星级鉴定金钱
	 * serial							int
	 * itemId							int
	 * instanceId						int
	 * type								byte(0 无星级鉴定符 1 低级星级鉴定符 2 高级星级鉴定符 3 顶级星级鉴定符 4重复鉴定符)
	 */
	public static final short START_ENHANCE_MONEY_CLIENT = 435;
	
	/**
	 * 获取星级鉴定金钱成功
	 * serial							int
	 * money1							int(如果-1，那么不可鉴定)星级鉴定
	 * money2							int(如果-1，那么不可鉴定)
	 * money3							int(如果-1，那么不可鉴定)
	 * money
	 * money4							int(如果-1，那么不可鉴定)
	 * money5                           int(如果-1，那么不可鉴定 重复鉴定符)
	 * money                            int(如果-1，那么不可鉴定)资质鉴定
	 * starLowSheetPrice                int低级星级鉴定符价格
	 * starHighSheetPrice               int高级星级鉴定符价格
	 * starTopSheetPrice                int顶级星级鉴定符价格
	 * naturalSheetPrice                int资质鉴定符价格
	 */
	public static final short START_ENHANCE_MONEY_SERVER = 436;
	
	/**
	 * 资质鉴定
	 * serial							int
	 * itemId							int
	 * instanceId						int
	 * isNatural						byte
	 */
	public static final short NATURAL_ENHANCE_CLIENT = 437;
	
	/**
	 * 资质鉴定成功
	 * serial							int
	 * GameItem							byte[](参见后面的ITEM结构信息)
	 */
	public static final short NATURAL_ENHANCE_SERVER = 438;
	
	/**
	 * 获取资质鉴定金钱(作废)
	 * serial							int
	 * itemId							int
	 * instanceId						int
	 */
	public static final short NATURAL_ENHANCE_MONEY_CLIENT = 439;
	
	/**
	 * 获取资质鉴定金钱成功
	 * serial							int
	 * money							int(如果-1，那么不可鉴定)
	 */
	public static final short NATURAL_ENHANCE_MONEY_SERVER = 440;
	
	/**
	 * 金翎投票
	 * serial							int
	 */
	public static final short CHINAJOY_GIFT_CLIENT = 441;
	
	/**
	 * 投票成功
	 * serial							int
	 */
	public static final short CHINAJOY_GIFT_SERVER = 442;
	
	/**
	 * 今天的投票次数
	 * serial							int
	 */
	public static final short CHINAJOY_COUNT_CLIENT = 443;
	
	/**
	 * 返回投票次数
	 * serial							int
	 * count							int
	 */
	public static final short CHINAJOY_COUNT_SERVER = 444;
	
	/**
	 * 发布国家信息
	 * serial							int
	 * message							string
	 * flag								int			发布类型：1为国王，0为大臣
	 */
	public static final short NATION_SLOGAN_CLIENT = 445;
	
	/**
	 * 发布国家信息成功
	 * serial							int
	 */
	public static final short NATION_SLOGAN_SERVER = 446;
	
	/**
	 * 获取国家信息
	 * serial							int
	 * faction							int
	 */
	public static final short NATION_INFO_CLIENT = 447;
	
	
	/**
	 * 获取国家信息成功
	 * serial
	 * 名称								string
	 * 公告								string
	 * 国战胜利次数						int
	 * 国战失败次数						int
	 * 国王ID							int
	 * 国王名字							string
	 * 国库								String(不足万按原数显示，大于万按规则显示万，亿)
	 * 统率力							int
	 * 国家税率							int(税率*100)
	 */
	public static final short NATION_INFO_SERVER = 448;
	
	/**
	 * 国家官员禁言
	 * serial							int
	 * 目标的名字							string
	 * 原因								string
	 * flag								int			发布类型：1为国王，0为大臣
	 */
	public static final short NATION_FORBID_CLINET = 449;
	
	/**
	 * 禁言成功
	 * serial							int
	 */
	public static final short NATION_FORBID_SERVER = 450;
	
	/**
	 * 国家罚款
	 * serial							int
	 * 目标的名字						string
	 * 原因								string
	 * 罚款数量							int
	 * flag								int			1为国王权利，0为大臣权利
	 */
	public static final short NATION_PUNISH_CLIENT = 451;
	
	/**
	 * 国家罚款成功
	 * serial							int
	 */
	public static final short NATION_PUNISH_SERVER = 452;
	
	/**
	 * 认命官员
	 * serial							int
	 * 目标名称							string
	 * 职位								byte (1 丞相 2 御史大夫 3 大司马 4 骠骑将军)
	 * 公告								String
	 */
	public static final short NATION_OFFICER_CLIENT = 453;
	
	/**
	 * 认命官员成功
	 * serial							int
	 */
	public static final short NATION_OFFICER_SERVER = 454;
	
	/**
	 * 宣战或者结盟
	 * serial							int
	 * faction							int (敌对阵营)
	 * type								byte(1 结盟 3 宣战 13 发起反击)
	 */
	public static final short NATION_DECLARE_CLIENT = 455;
	
	/**
	 * 宣战或者结盟成功
	 */
	public static final short NATION_DECLARE_SERVER = 456;
	
	/**
	 * 传送到国战战场
	 * type								int (1 进攻 2 防守 3 反击进攻 4 反击防守)
	 * direct							byte(方向 1 东 2 南 3 西 4 北)
	 */
	public static final short NATION_BATTLE_TELE_CLIENT = 457;
	
	/**
	 * 获取宣战或者结盟信息列表
	 * serial							int
	 */
	public static final short NATION_DECLARE_LIST_CLIENT = 458;
	
	
	/**
	 * 宣战或结盟信息列表
	 * serial							int
	 * 数量								short
	 * 循环n次
	 * 	来源的阵营						int
	 *  目标的阵营						int
	 *  类型								byte(0 和平状态 1 发起结盟 2 结盟 3 发起宣战 4 战争准备 5 被发起结盟 6 被宣战 7 被结盟 8 防守准备 9 胜利 10 失败 11 进攻 12 防守 15 反击 16 被反击)
	 *  截止时间							string
	 * 魏国的保护时间					string
	 * 蜀国的保护时间					string
	 * 吴国的保护时间					string
	 * 
	 */
	public static final short NATION_DECLARE_LIST_SERVER = 459;
	
	/**
	 * 同意或者拒绝宣战结盟
	 * serial							int
	 * 阵营								int
	 * 类型								byte（1 结盟 3 宣战)
	 * 同意或者拒绝						byte (1 同意 2 拒绝)
	 */
	public static final short NATION_DECLARE_ACCEPT_CLIENT = 460;
	
	/**
	 * 同意或者拒绝宣战结盟成功
	 * serial							int
	 */
	public static final short NATION_DECLARE_ACCEPT_SERVER = 461;
	
	/**
	 * 获取国家关系
	 * serial							int
	 */
	public static final short NATION_REL_CLIENT = 462;
	
	/**
	 * 国家关系
	 * serial							int
	 * 循环3次
	 * 	国家阵营							int
	 *  国王Id							int(0 表示没有国王)
	 *  国王名字							string
	 *  盟国								int (0 没有盟国 1 魏 2 蜀 3 吴)
	 */
	public static final short NATION_REL_SERVER = 463;
	
	/**
	 * 发布国家任务请求
	 * serial							int
	 */
	public static final short NATION_QUEST_REQUEST_CLIENT = 474;
	
	/**
	 * 发布国家任务请求返回
	 * serial							int
	 * 当前国库的金钱					int
	 * 任务数量							short
	 * 循环n次
	 * 	任务ID							int
	 * 	任务名字							String
	 * 	任务状态							(0 没开启 1 开启)
	 *  押运时间                           byte
	 * 修改状态                        byte(0,未开启过或未修改过时间 1，已经修改过，2，当前时段不能开启或修改)
	 * 
	 */
	public static final short NATION_QUEST_REQUEST_SERVER = 475;
	
	/**
	 * 发布国家任务
	 * serial							int
	 * id								国家任务Id
	 */
	public static final short NATION_QUEST_CLIENT = 476;
	
	
	/**
	 * 发布国家任务成功
	 * serial							int
	 * 扣除的金钱						int
	 */
	public static final short NATION_QUEST_SERVER = 477;
	
	
	/**
	 * 发布拍卖信息
	 * serial						int
	 * itemId						int			物品ID
	 * itemInstanceId				int			物品实例ID
	 * count						short		物品数量 (如果是有实例ID的物品count需为1)
	 * startPrice   				int   		初始价格
	 * endPrice						int			一口价 (一口价可以为0,表示不设置一口价)
	 */
	public static final short AUCTION_CREATEAUCTION_CLIENT = 477;
	
	/**
	 * 发布拍卖信息成功
	 * serial		int
	 * fees			int		扣除手续费
	 */
	public static final short AUCTION_CREATEAUCTION_SERVER = 478;
	
	/**
	 * 查看详细信息
	 * serial       int
	 * type			int			物品类型 0为武器,1为防具,2为饰品,3为普通物品类型 4(同时包括0、1、2)
	 * quality		int			物品的品质
	 * downlevel	int			物品等级下限
     * uplevel 		int 		物品等级上限
     * name 		String 		物品名字
     * sortfeild	int			排序号 1为名称排序，2为当前价格排序，3为结束时间排序
     * pageNum		int			页号
     * amount		int			每页显示条数
     * asc			int			0为升序排列，1为降序排列
	 */
	public static final short AUCTION_LIST_CLIENT = 479;
	
	/**
	 * 查看详细信息结果返回
	 * serial 				int 
	 * pageamount 			int	 		数据的总页数
	 * amount				int			数据总条数
	 * pageNum				int			页数      从1开始
	 * articleamount		int			本页实际条数
	 *   循环N次
	 * 		auctionId		int 		拍卖行ID
	 * 		item			byte[]		物品信息
	 * 		itemamount		int			物品数量
	 * 		currentPrice	int			当前价格
	 * 		endPrice		int			一口价 0为一口价交易
	 * 		playername		String 		拍卖者名字
	 * 		validtime		String		结束时间
	 */
	public static final short AUCTION_LIST_SERVER = 480;
	
	/**
	 * 竞拍请求
	 * serial		int
	 * auctionID	int		   拍卖行ID
	 * price        int        出价
	 */
	public static final short AUCTION_BUY_CLIENT = 481;
	
	/**
	 * 竞拍成功
	 * serial		int
	 * auctionID	int		拍卖行ID
	 * currentPrice	int		当前价
	 * result 		String 	竞拍结果
	 */
	public static final short AUCTION_BUY_SERVER = 482;
	
	/**
	 * 请求显示最近本角色在拍卖行发布的依旧在拍卖中的物品的拍卖信息
	 * serial 		int
	 */
	public static final short AUCTION_PUBLISHIED_CLIENT = 483;
	
	/**
	 * 返回本角色最近在拍卖行发布的拍卖信息
	 * serial		int
	 * count1		int			循环次数
	 * 循环N次
	 * 		auctionId		int 		拍卖行ID
	 * 		item			byte[]		物品信息
	 * 		itemamount		int			物品数量
	 * 		currentPrice	int			当前价格
	 * 		endPrice		int			一口价 0为一口价交易
	 * 		validtime		String		结束时间
	 * count2		int 		循环次数
	 * 		auctionId		int 		拍卖行ID
	 * 		item			byte[]		物品信息
	 * 		name			String		玩家姓名
	 * 		itemamount		int			物品数量
	 * 		startPrice		int			起拍价
	 * 		currentPrice	int			当前价格
	 * 		endPrice		int			一口价 0为一口价交易
	 * 		validtime		String		结束时间
	 */
	public static final short AUCTION_PUBLISHIED_SERVER = 484;
	
	/**
	 * 查看套装信息
	 * serial				int
	 * itemid				int				物品ID
	 * instanceid			int				物品实例ID
	 * type					byte			类型（0其他玩家、1玩家自己、2随从、3坐骑  4其他玩家随从 5从擂台战界面查看玩家 6从擂台战界面查看坐骑 7从擂台战界面查看随从）
	 * instanceid			int				实例ID(分别为：其他玩家id、自己id、随从instanceid、坐骑主人ID)
	 * horseInstanceId		int				坐骑instanceid
	 */
	public static final short SUITE_CLIENT = 485;
	/**
	 * 
	 * 套装信息查询返回结果
	 * serial				int
	 * specaileffect		String			当前物品特效
	 * title				String			套装前缀
	 * num1					byte				套装中装备数量
	 * 循环 num1 次
	 * 		name			String		 	装备名称
	 * 		flag			byte				是否穿着 0为没穿，1为穿着
	 * 
	 * ratio				String			比例
	 * 
	 * num2					byte				套装描述信息
	 * 循环 num2 次
	 * 		desc			String			套装效果描述信息
	 * 		flag			byte				是否显示 0为不显示，1为显示
	 */
	public static final short SUITE_SERVER = 486;
	 
	
	/**
	 * 结婚申请
	 * serial			int
	 * womanId			int				配偶id (-1为组队成员)
	 */
	public static final short MARRIAGE_CLIENT = 487;
	
	/**
	 *结婚申请结果
	 * result			String			结果
	 */
	public static final short MARRIAGE_SERVER = 488;
	
	/**
	 * 邀请结婚
	 * personId				int					邀请者id
	 * name					String				邀请者姓名 
	 */
	public static final short MARRIAGE_REQUEST_SERVER = 489;
	
	/**
	 * 邀请结婚回复		
	 * personId				int					邀请者id
	 * answer				int					邀请回复（0为拒绝接受请求，1为同意）
	 */
	public static final short MARRIAGE_ANSWER_CLIENT = 490;
	
	/**
	 * 离婚申请
	 * type					int					离婚类型 （0为协议离婚，1为强制离婚）
	 */
	public static final short MARRIAGE_DIVORCE_CLIENT = 491;
	
	/**
	 * 离婚协商
	 * id 					int					提出离婚者的id
	 * name 				String				提出离婚者的姓名
	 */
	public static final short MARRIAGE_DIVORCEREQUEST_SERVER = 492;
	
	/**
	 * 离婚协商回复
	 * id					int					提出离婚者的id
	 * answer				int					回复 （0为拒绝离婚，1为同意离婚）
	 */
	public static final short MARRIAGE_DIVORCEANSWER_CLIENT = 493;
	/**
	 * 离婚结果返回
	 * result					String			离婚结果描述 
	 */
	public static final short MARRIAGE_DIVORCE_SERVER = 494;
	/**
	 * 镶嵌：向装备上添加宝石。
	 * serial               int                 序列号
	 * itemid               int                 要镶嵌的装备的物品id
	 * instanceid           int                 要镶嵌的装备的instanceid
	 * hole                 byte                镶孔索引(0表示第一个)
	 * jewelid              int                 宝石物品ID
	 * method               byte                镶嵌方法：0 - 无镶嵌符，1 - 用等级镶嵌符，2 - 用高级镶嵌符
	 */
	public static final short DECORATE_ADD_JEWEL_CLIENT = 495;
    /**
     * 镶嵌：向装备上添加宝石返回。
     * serial               int                 序列号
     * itemid               int                 镶嵌的装备的物品id
     * instanceid           int                 镶嵌的装备的instanceid
     * jewelinfo            byte[]              镶嵌后装备宝石信息（参见后面DECORATION数据结构说明）
     */
    public static final short DECORATE_ADD_JEWEL_SERVER = 496;
	/**
	 * 镶嵌：从装备上取下宝石。
     * serial               int                 序列号
     * itemid               int                 要镶嵌的装备的物品id
     * instanceid           int                 要镶嵌的装备的instanceid
     * hole                 byte                镶孔索引(0表示第一个)
	 */
	public static final short DECORATE_REMOVE_JEWEL_CLIENT = 497;
    /**
     * 镶嵌：从装备上取下宝石返回。
     * serial               int                 序列号
     * itemid               int                 要镶嵌的装备的物品id
     * instanceid           int                 要镶嵌的装备的instanceid
     * jewelinfo            byte[]              镶嵌后装备宝石信息（参见后面DECORATION数据结构说明）
     */
    public static final short DECORATE_REMOVE_JEWEL_SERVER = 498;
	/**
	 * 镶嵌：在装备上打新孔。（需要扣除一个打孔物品）
     * serial               int                 序列号
     * itemid               int                 装备的物品id
     * instanceid           int                 装备的instanceid
	 */
	public static final short DECORATE_ADD_HOLE_CLIENT = 499;
    /**
     * 镶嵌：在装备上打新孔返回。
     * serial               int                 序列号
     * itemid               int                 装备的物品id
     * instanceid           int                 装备的instanceid
     * holecount            byte                新附加孔数
     */
    public static final short DECORATE_ADD_HOLE_SERVER = 500;
	/**
	 * 废弃
	 * 
	 * 镶嵌：扩展装备的最大孔数。（需要扣除一个扩展孔物品，暂不开放）
     * serial               int                 序列号
     * itemid               int                 装备的物品id
     * instanceid           int                 装备的instanceid
	 */
	public static final short DECORATE_ADD_MAX_HOLE_CLIENT = 501;
    /**
     * 废弃
     * 
     * 镶嵌：扩展装备的最大孔数返回。
     * serial               int                 序列号
     * itemid               int                 装备的物品id
     * instanceid           int                 装备的instanceid
     * holeadd              byte                新附加孔数
     * holecount            byte                总的孔数
     */
    public static final short DECORATE_ADD_MAX_HOLE_SERVER = 502;
    /**
     * 镶嵌：取镶嵌系统相关配置数据。
     */
    public static final short DECORATE_GET_CONFIG_CLIENT = 503;
    /**
     * 镶嵌：返回镶嵌系统相关配置数据。
     * levelcount           byte                宝石级别总数（循环变量）
     *    mergesuccrate1    byte                3颗成功概率（百分比）
     *    mergesuccrate2    byte                4颗成功概率（百分比）
     *    mergesuccrate3    byte                5颗成功概率（百分比）
     *    mergeprice        int                 合成需求金钱
     *    mergeitem         int                 需求合成符ID
     *    mergeitemicon     byte                需求合成符图标
     *    mergeitemname     String              合成符名称
     *    mergeitemprice    int                 合成符价格
     *    addprice          int                 镶嵌需求金钱
     *    removeprice       int                 取下需求金钱
     *    removeitem        int                 取下需要摘除符ID
     *    removeitemicon    byte                摘除符图标
     *    removeitemname    String              摘除符名称
     *    removeitemprice   int                 摘除符价格
     *  addsuccrate         byte                无镶嵌符时的镶嵌成功率
     *  additem1            int                 低级镶嵌符ID
     *  additemicon1        byte                低级镶嵌符图标
     *  additemname1        String              低级镶嵌符名称
     *  additemprice1       int                 低级镶嵌符价格
     *  addsuccrate1        byte                低级镶嵌符成功率
     *  additem2            int                 高级镶嵌符ID
     *  additemicon2        byte                高级镶嵌符图标
     *  additemname2        String              高级镶嵌符名称
     *  additemprice2       int                 高级镶嵌符价格
     *  addsuccrate2        byte                高级镶嵌符成功率
     *  holeconfigs         byte                打孔符配置数（循环变量）
     *    holelvl           byte                装备级别上限（含）
     *    holeitem          int                 打孔符ID
     *    holeitemicon      byte                打孔符图标
     *    holeitemname      String              打孔符名称
     *    holeitemprice     int                 打孔符价格
     */
    public static final short DECORATE_GET_CONFIG_SERVER = 504;
    /**
     * 镶嵌：请求合成宝石。
     * serial               int                 序列号
     * jewelid              int                 宝石物品ID
     * count                byte                宝石数量
     */
    public static final short DECORATE_MERGE_JEWEL_CLIENT = 505;
    /**
     * 镶嵌：合成宝石成功。
     * serial               int                 序列号
     * jewelid              int                 如果成功，返回合成后的宝石物品ID
     * jewelicon            byte                如果成功，返回合成后的宝石物品图标
     * jewelname            String              如果成功，返回合成后的宝石物品名称
     */
    public static final short DECORATE_MERGE_JEWEL_SERVER = 506;
    
    /**
	 * 已经学习的配方列表
	 * serial			int
	 */
	public static final short FORMULA_LIST_CLIENT = 510;
	
	/**
	 * 已经学习的配方列表返回
	 * serial				int
	 * gatherPractice		short				采集熟练度(显示在滚动信息里面)
	 * gatherPracticename	String				采集熟练度名称(显示在滚动信息里面)
	 * producePractice		short				打造熟练度
	 * producePracticename	String				打造熟练度名称
	 * 按级别依次传输
	 * num					short				循环次数
	 * 		id				int					配方书ID
	 * 		name			String				配方书名字
	 * 		level			String				配方级别		
	 */
	public static final short FORMULA_LIST_SERVER = 511;
	
	/**
	 * 配方书详细信息查看
	 * id 					int					配方书ID
	 */
	public static final short FORMULA_INFO_CLIENT = 512;
	
	/**
	 * 配方书详细信息
	 * fornulaId			int					配方ID
	 * practice				short				所需熟练度
	 * num					short				循环次数
	 * 		type			byte				消耗材料类型（0为金钱，1为i币，2为荣誉，3为军衔，4为物品）
	 * 		item			byte[]				物品信息(只有类型为物品的时候读取)
	 * 		amount			int					消耗材料数量
	 * 		deduct			byte				是否扣除(0为不扣除，1为扣除)
	 * 		rank			String				军衔达到的级别
	 * movePoint			short				消耗行动力
	 * description			String				描述信息
	 * itemtype				byte				产出类型（1为掉落组，0为物品或装备）
	 * minAmount			byte				掉落组中物品最小数量
	 * maxAmount			byte				掉落组中物品最大数量
	 * item					byte[]				配方产物信息(如果是掉落组，则这个物品是显示物品)
	 */
	public static final short FORMULA_INFO_SERVER = 513;
	
	/**
	 * 打造
	 * serial				int
	 * formulaid			int					配方书ID
	 */
	public static final short PRODUCE_CLIENT = 514;
	
	/**
	 * 打造结果
	 * serial				int
	 * num					byte				循环次数
	 * 		item			byte[]				产物信息
	 * 		count			byte				物品数量
	 * practice				short				增加打造熟练度
	 * practicename			String				打造熟练度名称
	 */
	public static final short PRODUCE_SERVER = 515;
	
	/**
	 * 删除已学习的配方
	 * serial				int
	 * formulaid			int					配方ID
	 */
	public static final short FORMULA_DELETE_CLIENT = 516;
	
	/**
	 * 删除已学习的配方返回
	 * serial				int
	 */
	public static final short FORMULA_DELETE_SERVER = 517;
	
	/**
	 * 驿站传送列表
	 * serial 				int
	 * teleportID			int					驿站ID
	 */
	public static final short TELEPORT_LIST_CLIENT = 518;
	
	/**
	 * 驿站传送列表返回
	 * serial				int
	 * num					int					循环次数
	 * 		mapid				int					地图ID
	 * 		x					int					x坐标
	 * 		y					int					y坐标
	 * 		mapname				String				地图名称
	 * 		require				String				要求
	 */
	public static final short TELEPORT_LIST_SERVER = 519;
	
	/**
	 * 驿站传送
	 * serial				int
	 * npcinstanceId		int					NPC的instanceid
	 * teleportid			int					驿站ID
	 * mapIndex				int					传送地点索引
	 */
	public static final	short TELEPORT_CLIENT = 520;
	
	/**
	 * 是否已经开启过仓库
	 * serial				int
	 */
	public static final short DEPOT_YESORNO_CLIENT = 521;
	
	/**
	 * 是否已经开启过仓库请求结果
	 * serial				int
	 * result				int					0为未申请，1为已申请
	 * 包格数量				byte
	 * 循环n次
	 * 包格信息				GRID
	 */
	public static final short DEPOT_YESORNO_SERVER = 522;
	
	/**
	 * 仓库申请
	 * serial				int
	 */
	public static final short DEPOT_REQUEST_CLIENT = 523;
	
	/**
	 * 仓库申请结果返回
	 * serial						int
	 * 包格数量						byte
	 * 循环n次
	 *		包格信息					GRID
	 */
	public static final short DEPOT_REQUEST_SERVER = 524;
	
	/**
	 * 仓库整理
	 * serial				int
	 */
	public static final short DEPOT_ARRANGE_CLIENT = 525;
	
	/**
	 * 整理仓库完成
	 * serial							int
	 * 循环n次							byte
	 * 	包格信息							GRID
	 */
	public static final short DEPOT_ARRANGE_SERVER = 526;
	
	/**
	 * 从仓库中取出物品放入背包
	 * serial				int
	 * gridid				int				包格ID
	 * itemid				int				物品ID
	 * instanceid			int				物品实例ID
	 * count				int				取出物品数量
	 */
	public static final short DEPOT_GETFROMDEPOT_CIENT = 527;
	
	/**
	 * 从背包中取出物品放入仓库
	 * serial				int
	 * gridid				int				包格ID
	 * itemid				int				物品ID
	 * instanceid			int				物品实例ID
	 * count				int				取出物品数量
	 */
	public static final short DEPOT_GETFROMBAG_CLIENT = 528; 
	
	/**
	 * 改变的包格信息
	 * serial					int
	 * 包格数量					byte
	 * 		包格信息					GRID
	 */
	public static final short DEPOT_GETFROMBAG_SERVER = 529;
	
	/**
	 * 获取杀人数量列表
	 * serial						int
	 */
	public static final short TOPLIST_KILLCOUNT_CLINET = 530;
	
	/**
	 * 杀人数量列表
	 * serial
	 * 数量							short
	 * 循环n次
	 * 	名字							string
	 * 	杀人数						int
	 */
	public static final short TOPLIST_KILLCOUNT_SERVER = 531;
	
	/**
	 * 本周战功列表
	 * serial
	 */
	public static final short TOPLIST_WEEKRANK_CLIENT = 532;
	
	/**
	 * 本周战功列表
	 * serial						int
	 * 数量							short
	 * 循环n次
	 *   名字						string
	 *   战功等级					string
	 *   		
	 */
	public static final short TOPLIST_WEEKRANK_SERVER = 533;
	
	/**
	 * 清除所有副本进度
	 * serial
	 */
	public static final short INSTANCE_CLEAR_CLIENT = 534;
	
	/**
	 * 清除所有进度成功
	 * serial
	 */
	public static final short INSTANCE_CLEAR_SERVER = 535;

	 /**
	 * 竞选国王报名
	 * serial					int
	 */
	public static final short CANDIDATE_SIGNUP_CLIENT = 536;
	
	/**
	 * 竞选国王报名返回(返回代表报名成功)
	 * serial					int
	 */
	public static final short CANDIDATE_SIGNUP_SERVER = 537;
	
	/**
	 * 候选人列表
	 * serial					int
	 */
	public static final short CANDIDATE_LIST_CLIENT = 538;
	
	/**
	 * 候选人列表返回
	 * serial					int
	 * num						byte				循环次数
	 * 		id						int 			候选人ID
	 * 		name					String			候选人姓名
	 * 		tong					String			军团名称
	 * 		vote					int				当前票数
	 * 		order					int				排名
	 */
	public static final short CANDIDATE_LIST_SERVER = 539;
	
	/**
	 * 投票选举国王
	 * serial					int
	 * kingid					int				候选人ID
	 * type						byte			投票方式（0默认投票方式、1一盒酥投票）
	 * count                    int             投票个数
	 */
	public static final short CANDIDATE_VOTE_CLIENT = 540;
	
	/**
	 * 投票选举国王返回
	 * serial					int
	 */
	public static final short CANDIDATE_VOTE_SERVER = 541;
	
	/**
	 * 国王发起募捐
	 * serial					int
	 */
	public static final short COLLECT_LAUNCH_CLIENT = 542;
	
	/**
	 * 国家募捐
	 * serial 					int
	 * money					int				捐赠金钱数量
	 */
	public static final short COLLECT_CLIENT = 543;
	
	/**
	 * 国家募捐返回
	 * serial					int
	 */
	public static final short COLLECT_SERVER = 544;
	
	/**
	 * 捐献战功
	 * serial					int
	 * credit					int				所出战功	
	 */
	public static final short CONTRIBUTECREDIT_CLIENT = 545;
	
	/**
	 * 捐献战功返回
	 * serial					int
	 */
	public static final short CONTRIBUTECREDIT_SERVER = 546;
	
	/**
	 * 大臣一览表
	 * serial					int
	 */
	public static final short OFFICER_LIST_CLIENT = 547;
	
	/**
	 * 大臣一览表返回
	 * num				byte			循环次数
	 * 		id				int			官员ID
	 * 		officer			String		官职
	 * 		name			String		玩家名字		
	 */
	public static final short OFFICER_LIST_SERVER = 548;
	
	/**
	 * 密保状态
	 * serial				int
	 */
	public static final short ACCOUNTBINDING_STATUS_CLIENT = 549;

	/**
	 * 密保状态
	 * serial				int
	 * code					byte(0位 是否绑定手机号 1位  是否绑定身份证 2位 是否绑定问题 3位 是否绑定邮箱 -1 获取信息失败)
	 */
	public static final short ACCOUNTBINDING_STATUS_SERVER = 550;
	
	/**
	 * 绑定密保
	 * serial				int
	 * type					byte(1 绑定手机 2 绑定身份证 3 绑定问题 4 绑定邮箱)
	 * value1				string (绑定手机号或者身份证号或者绑定问题或者绑定邮箱)
	 * value2				string (如果绑定问题需要填入答案，如果是其他那么传空字符串)
	 * idcard				string (身份证号，如果是绑定身份证号，那么传空字符串)
	 */
	public static final short ACCOUNTBINDING_CLIENT = 551;
	
	/**
	 * 绑定成功
	 * serial				int
	 * message				string 返回内容
	 */
	public static final short ACCOUNTBINDING_SERVER = 552;
	
	/**
	 * 绑定手机号时的短信内容
	 * sreial				int
	 * message				string
	 */
	public static final short ACCOUNTBINDING_SMS_SERVER = 553;
	
	
	/**
	 * bbs列表
	 * serial
	 * 每页条数(-1 所有)					short
	 * 页数								short
	 */
	public static final short BBS_LIST_CLIENT = 554;
	
	/**
	 * Mail列表
	 * serial							int
	 * 每页条数							short
	 * 页数								short
	 * 总数								int
	 * 本页实际条数						short
	 * 循环n次
	 *  Id								int
	 * 	title							String
	 * 	时间								string
	 */
	public static final short BBS_LIST_SERVER = 555;
	
	/**
	 * 取bbs内容
	 * serial							int
	 * id								int
	 */
	public static final short BBS_CONTENT_CLIENT = 556;
	
	/**
	 * bbs内容
	 * serial							int
	 * message							string
	 */
	public static final short BBS_CONTENT_SERVER = 557;
	
	/**
	 * 获取配偶信息
	 * serial							int
	 */
	public static final short MARRIAGE_INFO_CLIENT = 558;
	
	/**
	 * 配偶信息
	 * serial							int
	 * 角色Id							int (如果没有id为-1)
	 * 角色名							string
	 * 配偶是否在线                      byte(0不在线，1在线);
	 */
	public static final short MARRIAGE_INFO_SERVER = 559;
	
	/**
	 * 查询角色的各种属性
	 * serial							int
	 * 相对等级							int 
	 */
	public static final short PLAYER_RATE_CLIENT = 560;
	
	/**
	 * 角色的各种属性
	 * serial						 	int
	 * 相对等级							int
	 * 物理暴击*100						int
	 * 法术暴击*100						int
	 * 物理命中*100						int
	 * 法术命中*100						int
	 * 物理闪避*100						int
	 * 法术闪避*100						int 
	 */
	public static final short PLAYER_RATE_SERVER = 561;
	
	/**
	 * 获取当前场景的NPC列表
	 * serial							int
	 */
	public static final short NPC_LIST_CLIENT = 562;
	
	/**
	 * 当前场景NPC列表
	 * serial							int
	 * 数量								short
	 * 循环n次
	 * 	id								int
	 * 	名字								String
	 * 	描述								String
	 * 	x坐标							short
	 *	y坐标							short
	 */
	public static final short NPC_LIST_SERVER = 563;
	
	/**
	 * npc功能描述
	 * serial							int
	 * NPC id							int
	 */
	public static final short NPC_DESC_CLIENT = 564;
	
	/**
	 * npc功能描述
	 * serial							int
	 * NPC id 							int
	 * 描述								string
	 */
	public static final short NPC_DESC_SERVER = 565;
	
	/**
	 * 兑换人物离线经验
	 * serial							int
	 * count							int //一盒酥数量
	 */
	public static final short EXCHANGE_EXP_CLIENT = 566;
	
	/**
	 * 兑换人物离线经验返回
	 * serial							int
	 * exp								int			兑换的经验值
	 * count							int 		消耗经验修炼符
	 * leaving							int			剩余经验值
	 */
	public static final short EXCHANGE_EXP_SERVER = 567;
	
	/**
	 * 查看当前积累离线经验
	 * serial							int
	 * count							int	一合酥数量
	 */
	public static final short QUERY_OFFLINE_EXP_CIENT = 568;
	
	/**
	 * 查看当前积累离线经验返回
	 * serial							int
	 * exp								int			兑换的经验值
	 * String                           String      兑换前的提示
	 */
	public static final short QUERY_OFFLINE_EXP_SERVER = 569;
	
	/**
	 * 代理饲养马
	 * serial							int
	 * horseInstanceId					int			马的实例ID
	 */
	public static final short HORSE_AGENT_CLIENT = 570;
	
	/**
	 * 代理饲养马返回
	 * serial							int
	 */
	public static final short HORSE_AGENT_SERVER = 571;
	
	/**
	 * 兑换马的经验
	 * serial							int
	 * horseInsranceId					int			马的实例ID
	 */
	public static final short EXCHANGE_HORSEEXP_CLIENT = 572;
	
	/**
	 * 兑换马的经验返回
	 * serial							int
	 * horseexp							int			兑换的马的经验值
	 * count							int  	 	消耗马的经验修炼符
	 * leaving							int			剩余经验值
	 */
	public static final short EXCHANGE_HORSEEXP_SERVER = 573;
	
	/**
	 * 取消代理饲养马
	 * serial							int
	 * horseInstanceId					int			代理饲养马的实例ID
	 */
	public static final short CANCEL_AGENTHORSE_CLIENT = 574;
	
	/**
	 * 取消代理饲养马返回
	 * serial							int
	 */
	public static final short CANCEL_AGENTHORSE_SERVER = 575; 
	
	/**
	 * 从NPC处领取物品
	 * serial							int
	 * npcId							int			npcID
	 * 领取ID							int
	 */
	public static final short GETITEM_FROM_NPC_CLIENT = 576;
	
	/**
	 * 领取成功
	 */
	public static final short GETITEM_FROM_NPC_SERVER = 577;
	
	/**
	 * 获取国家技能列表
	 * serial							int
	 */
	public static final short NATION_SKILL_LIST_CLIENT = 600;
	/**
	 * 获取国家技能列表成功
	 * serial							int
	 * 数量								short
	 * 	国家技能							byte[](详见文件最后的NATIONSKIL)	 
	 */
	public static final short NATION_SKILL_LIST_SERVER = 601;
	/**
	 * 学习国家科技
	 * serial							int
	 * 科技ID							int
	 * 科技等级							byte
	 */
	public static final short NATION_SKILL_STUDY_CLIENT = 602;
	/**
	 * 学习国家技能成功
	 * serial							int
	 */
	public static final short NATION_SKILL_STUDY_SERVER= 603;
	
	/**
	 * 国家科技描述
	 * serial							int
	 * 数量								short
	 * 循环n次
	 * 	科技ID							int
	 * 	科技等级							byte
	 */
	public static final short NATION_SKILL_DESC_CLIENT = 604;
	
	/**
	 * 国家科技描述
	 * serial							int
	 * 数量								short
	 * 循环n次
	 * 	科技ID							int
	 * 	科技等级							byte
	 * 	科技升级费用						int
	 *  科技维护费用						int
	 *  科技描述							string
	 */
	public static final short NATION_SKILL_DESC_SERVER = 605;
	
	/**
	 * 领取国家科技道具
	 * serial							int
	 * 科技ID							int
	 * 科技等级							byte
	 */
	public static final short NATION_SKILL_ITEM_CLIENT = 606;
	
	/**
	 * 领取国家科技道具成功
	 * serial							int
	 */
	public static final short NATION_SKILL_ITEM_SERVER = 607;
	/**
	 * 删除一个月前不含附件的邮件
	 * serial							int
	 * type								int			0:系统邮件   1：玩家  2：付费邮件列表
	 */
	public static final short MAIL_OBSOLETE_DELETE_CLIENT=608;
	/**
	 * 删除成功
	 * serial							int
	 */
	public static final short MAIL_OBSOLETE_DELETE_SERVER=609;
	
	/**
	 * 锁定坐骑技能
	 * serial						int
	 * horseInstanceId				int
	 * skillIndex					int
	 */
	public static final short LOCK_HORSESKILL_CLIENT = 610;
	
	/**
	 * 锁定坐骑技能返回
	 * serial						int
	 */
	public static final short LOCK_HORSESKILL_SERVER = 611;
	
	/**
	 * 解除坐骑技能锁定
	 * serial						int
	 * horseInstanceId				int
	 * skillIndex					int
	 */
	public static final short UNLOCK_HORSESKILL_CLIENT = 612;
	
	/**
	 * 解除坐骑技能锁定返回
	 * serial						int
	 */
	public static final short 	UNLOCK_HORSESKILL_SERVER = 613;
	
	/**
	 * 天气变化
	 * 地图ID						int
	 * 开关							byte (0 关 1 开)
	 * 类型							byte (0 雨 1 雪)如果是开的状态下才存在此字段
	 * 大小							byte 如果是开的状态下才存在此字段
	 * 数量							byte 如果是开的状态下才存在此字段	
	 * 速度							byte 如果是开的状态下才存在此字段
	 * 风力							byte 如果是开的状态下才存在此字段
	 * 颜色							int	 如果是开的状态下才存在此字段
	 */
	public static final short WEATHER_SERVER = 614;
	
	/**
	 * 世界地图传送
	 * serial						int
	 * mapId						int				地图ID
	 */
	public static final short WORLD_TELEPORT_CLIENT = 615;
	
	/**
	 * 下发马技能信息
	 * count						int
	 * 		skill					skill
	 */
	public static final short HORSE_SKILLS_SERVER = 616;
	
	/**
	 * 世界地图传送返回
	 * serial						int
	 */
	public static final short WORLD_TELEPORT_SERVER = 617;
	
	/**
	 * 国公设置玩家进入国战的最低级别限制
	 * serial						int
	 * minlevel						short				最低级别
	 */
	public static final short NATIONBATTLE_MINLEVEL_CLIENT = 618;
	
	/**
	 * 国公设置玩家进入国战的最低级别限制返回
	 * serial						int
	 */
	public static final short NATIONBATTLE_MINLEVEL_SERVER = 619;
	
	/**
	 *	通知当前场景背景音乐
	 *	文件名						string 
	 */
	public static final short MUSIC_SERVER = 620;
	
	/**
	 * 获取文件
	 * serial						int
	 * 文件名						string
	 * 版本							int
	 */
	public static final short GET_FILE_CLIENT = 621;
	
	/**
	 * 获取文件成功
	 * serial						int
	 * 文件名						string
	 * 版本							int
	 * 是否存在数据					byte(0 不存在，版本相同 1存在)
	 * 数据							byte[](如果是否存在数据字段是0，这个字段不存在)
	 */
	public static final short GET_FILE_SERVER = 622;
	
	/**
	 * 创建元宝卡
	 * serial						int
	 * 面额							int (0 10元)
	 */
	public static final short IMONEYCARD_CREATE_CLIENT = 623;
	
	/**
	 * 创建元宝卡成功
	 * serial						int
	 */
	public static final short IMONEYCARD_CREATE_SERVER = 624;
	
	/**
	 * 自动资质鉴定
	 * serial						int
	 * itemId						int
	 * instanceId					int
	 * level						byte					期望级别(0,1,2,3,4)
	 * specialAtt                   byte                    期望提升的特殊属性
	 */
	public static final short AUTO_NATURALENHANCE_CLIENT = 625;
	
	/**
	 * 自动资质鉴定返回
	 * serial						int
	 * count						int					消耗资质鉴定符数量
	 * money						int					消耗金钱数量
	 * GameItem						byte[](参见后面的ITEM结构信息)
	 * cause						byte				原因（0符不足，1钱不足，2成功, 3角色状态异常）
	 */
	public static final short AUTO_NATURALENHANCE_SERVER = 626;
	
	/**
	 * 国公设置国家税率
	 * serial						int
	 * tax							byte				税率
	 */
	public static final short KING_TAXRATE_CLIENT = 627;
	
	/**
	 * 国公设置国家税率返回
	 * serial						int
	 */
	public static final short KING_TAXRATE_SERVER = 628;
	
	/**
	 * 国家押镖
	 * serial						int
	 * type                         byte(0开启，1关闭)
	 * index                        byte
	 */
	public static final short NATION_CONVOY_CLIENT = 629;
	
	/**
	 * 国家押镖启动
	 * serial						int
	 * state                        byte(0,未开启过或未修改过时间 1，已经修改过，2，当前时段不能开启或修改)
	 */
	public static final short NATION_CONVOY_SERVER = 630;
	
	/**
	 * 给装备刻字
	 * serial						int
	 * itemId						int
	 * instanceId					int	 
	 * message						string
	 */
	public static final short EQUIPEMENT_MARK_CLIENT = 631;
	
	/**
	 * 给装备刻字成功
	 * serial						int
	 * 装备信息						byte[](ITEM)
	 */
	public static final short EQUIPEMENT_MARK_SERVER = 632;
	
	/**
	 * 答题
	 * serial						int
	 * questionId					int				问题ID
	 * answer						String			问题答案
	 */
	public static final short ANSWER_CLIENT = 633;
	
	/**
	 * 答题返回
	 * serial						int
	 */
	public static final short ANSWER_SERVER = 634;
	
	/**
	 * 台湾版本Q币购买元宝。
	 * serial						int
	 * 购买元宝数量					int
	 * 充值的角色名					string (如果是自己充值，这个字段是空字符串，如果为他人充值，是他人的角色名)
	 */
	public static final short QME_PAY_CLIENT = 635;
	
	/**
	 * 台湾版本Q币购买元宝返回。
	 * serial						int
	 * message						string
	 */
	public static final short QME_PAY_SERVER = 636;
	
	/**
	 * 坐骑栏扩展
	 * serial						int
	 */
	public static final short HORSEBAG_EXTEND_CLIENT = 637;
	
	/**
	 * 坐骑栏扩展返回
	 * serial						int
	 */
	public static final short HORSEBAG_EXTEND_SERVER = 638;
	
	/**
	 * 获取最早击杀Boss的排名榜
	 * serial					int
	 * bossId					int
	 */
	public static final short BOSS_SCOREBOARD_CLIENT = 639;
	
	/**
	 * 获取最早击杀Boss的排名榜返回
	 * serial					int
	 * account					byte			中榜队伍数量
	 * 		score						byte			名次		
	 * 		date						String			击杀时间
	 * 		num							int				队伍中玩家数量
	 * 				name						String			玩家名称
	 * 				faction						byte			阵营
	 * 				level						byte			玩家等级
	 * 				sex							byte			玩家性别
	 * 				tong						String			帮会名称
	 * 				clazz						byte			职业
	 */
	public static final short BOSS_SCOREBOARD_SERVER = 640;
	
	/**
	 * 获取最快击杀Boss的排名榜
	 * serial					int
	 * bossId					int
	 */
	public static final short BOSS_TIMEBOARD_CLIENT = 641;
	
	/**
	 * 获取最快击杀Boss的排名榜返回
	 * serial					int
	 * account					byte			中榜军团数量
	 * 		score						byte			名次		
	 * 		time						int				击杀速度
	 * 		num							int				队伍中玩家数量
	 * 				name						String			玩家名称
	 * 				faction						byte			阵营
	 * 				level						byte			玩家等级
	 * 				sex							byte			玩家性别
	 * 				tong						String			帮会名称
	 * 				clazz						byte			职业
	 */
	public static final short BOSS_TIMEBOARD_SERVER = 642;
	
	/**
	 * 马装备解绑
	 * serial								int
	 * gameitemid							int					装备id
	 * gameiteminstanceid					int					装备instanceid
	 */
	public static final short HORSE_EQUIPMENT_UNBIND_CLIENT = 643;
	
	/**
	 * 马装备解绑返回
	 * serial								int
	 * GameItem								byte[](ITEM)
	 */
	public static final short HORSE_EQUIPMENT_UNBIND_SERVER = 644;
	
	/**
	 * 世界喊话
	 * serial							int
	 * content							String
	 */
	public static final short WORLD_SHOUT_CLIENT = 645; 
	
	/**
	 * 世界喊话返回
	 * serial							int
	 */
	public static final short WORLD_SHOUT_SERVER = 646;

	/**
	 * 台湾版本查询Q币余额。
	 */
	public static final short QME_QUERY_BALANCE_CLIENT = 647;
	
	/**
	 * 台湾版本查询Q币余额返回。
	 * balance						int
	 */
	public static final short QME_QUERY_BALANCE_SERVER = 648;
	
	/**
	 * iPhone版本充值。（废弃）
	 * AppStore收据						byte[]
	 */
	public static final short APP_STORE_CHARGE_CLIENT = 649;

	/**
	 * iPhone版本获取AppStore产品列表。（废弃）
	 */
	public static final short APP_STORE_LIST_PRODUCT_CLIENT = 695;

	/**
	 * iPhone版本返回AppStore产品列表。（废弃）
	 * count							byte
	 * productID						String
	 * productName						String
	 * price							int			购买价格（单位是美分）
	 * imoney							int			对应i币（单位是1/100i）
	 */
	public static final short APP_STORE_LIST_PRODUCT_SERVER = 696;
	
	/**
	 * iPhone版本充值（版本2）。
	 * bid								String		客户端bundle id
	 * receipt							byte[] 		AppStore收据
	 * clientid                         String      客户端串号
	 */
	public static final short APP_STORE_CHARGE2_CLIENT = 698;
	
	/**
	 * iPhone版本获取AppStore产品列表。（新）
	 * serial							int
	 * bid								String		客户端bundle id
	 * clientid                         String      客户端串号
	 */
	public static final short APP_STORE_LIST_PRODUCT2_CLIENT = 699;

	/**
	 * iPhone版本返回AppStore产品列表。（新）
	 * serial							int
	 * count							byte
	 *   productID						String
	 *   productName					String
	 *   price							int			购买价格（单位是美分）
	 *   imoney							int			对应i币（单位是1/100i）
	 * limit                            int         今日剩余购买限额（美分）
	 */
	public static final short APP_STORE_LIST_PRODUCT2_SERVER = 700;
	
	/**
	 * iPhone版本充值（版本3）。
	 * serial							int
	 * bid								String		客户端bundle id
	 * receipt							byte[] 		AppStore收据
	 * clientid                         String      客户端串号
	 */
	public static final short APP_STORE_CHARGE3_CLIENT = 701;
	
	/**
	 * iPhone版本充值（版本3）。
	 * serial							int
	 */
	public static final short APP_STORE_CHARGE3_SERVER = 702;

	/**
	 * 帮派捐款
	 * serial								int
	 * money								int
	 */
	public static final short TONG_CONTRIBUTE_CLIENT = 650;
	
	
	/**
	 * 帮派捐款成功
	 * serial								int
	 * money								int
	 */
	public static final short TONG_CONTRIBUTE_SERVER = 651;
	
	/**
	 * 帮派技能列表
	 * serial								int
	 */
	public static final short TONG_SKILL_LIST_CLIENT = 652;
	
	
	/**
	 * 帮派技能列表
	 * serial								int
	 * 数量									short
	 * 		ID							int
	 *		名字							string
	 *  	等级							byte
	 *		最高等级						byte
	 *      是否专属                     byte(0:军团科技    1：专属科技)
	 *      维护/升级所需贡献度          int
	 *  个人军团贡献度               int
	 */
	public static final short TONG_SKILL_LIST_SERVER = 653;
	
	/**
	 * 学习帮派科技
	 * serial							int
	 * 科技ID							int
	 * 科技等级							byte
	 */
	public static final short TONG_SKILL_STUDY_CLIENT = 654;
	
	/**
	 * 学习帮派能成功
	 * serial							int
	 */
	public static final short TONG_SKILL_STUDY_SERVER= 655;
	
	/**
	 * 帮派科技描述
	 * serial							int
	 * 数量								short
	 * 循环n次
	 * 	科技ID							int
	 * 	科技等级							byte
	 */
	public static final short TONG_SKILL_DESC_CLIENT = 656;
	
	/**
	 * 帮派科技描述
	 * serial							int
	 * 数量								short
	 * 循环n次
	 * 	科技ID							int
	 * 	科技等级							byte
	 * 	科技升级/维护所需贡献度			int
	 *  科技描述							string
	 */
	public static final short TONG_SKILL_DESC_SERVER = 657;
	
	/**
	 * 军团信息
	 * serial						int
	 */
	public static final short TONG_INFO_CLIENT = 658;
	
	/**
	 * 军团信息返回
	 * serial						int
	 * tongname						String			军团名称
	 * name							String			都督名称
	 * level						int				等级
	 * battlerecord					String			团站记录
	 * money						int				军团资金
	 * maxplayer					int				人数上限
	 * cityName						String          占领城池
	 * tongContribute               int				军团当前贡献度
	 * upLevelContribute            int             军团升级所需贡献度
	 * ismaintain                   byte            科技是否维护          0:未维护   1:已维护     
	 * autoAccept                   byte            是否开启自动接收新人  0:关闭      1：开启
	 * contribute                   int             自己当前贡献度
	 * contributeDay                int             自己今天的贡献度      
	 * contributeDayMax             int             今天最大可获得的贡献度
	 */	
	public static final short TONG_INFO_SERVER = 659;
	
	/**
	 * 发布军团任务请求
	 * serial							int
	 */
	public static final short TONG_QUEST_REQUEST_CLIENT = 660;
	
	/**
	 * 发布军团任务请求返回
	 * serial							int
	 * 当前军团的金钱						int
	 * 任务数量							short
	 * 循环n次
	 * 	任务ID							int
	 * 	任务名字							String
	 * 	任务状态							(0 没开启 1 开启)
	 */
	public static final short TONG_QUEST_REQUEST_SERVER = 661;
	
	/**
	 * 发布军团任务
	 * serial							int
	 * id								军团任务Id
	 */
	public static final short TONG_QUEST_CLIENT = 662;
	
	/**
	 * 发布军团任务成功
	 * serial							int
	 * 扣除的金钱							int
	 */
	public static final short TONG_QUEST_SERVER = 663;
	
	/**
	 * 申请攻城
	 * serial							int
	 * mapid							int			城市id
	 * money							int			攻城金额
	 */
	public static final short TONG_BATTLE_APPLY_CLIENT = 664;
	
	/**
	 * 申请攻城返回
	 * serial							int
	 */
	public static final short TONG_BATTLE_APPLY_SERVER = 665;
	
	/**
	 * 攻城竞价排名
	 * serial							int
	 * mapid							int				城市id
	 */
	public static final short TONG_BATTLEAPPLY_LIST_CLENT = 666;
	
	/**
	 * 攻城竞价排名列表
	 * serial							int
	 * num								int				数量
	 * 		tongname					String			军团名称
	 */
	public static final short TONG_BATTLEAPPLY_LIST_SERVER = 667;
	
	/**
	 * 攻城竞价
	 * serial						int
	 * mapid						int					城市id
	 * money						int					加价
	 */
	public static final short TONG_BATTLEBID_CLIENT = 668;
	
	/**
	 * 攻城竞价返回
	 * serial						int
	 */
	public static final short TONG_BATTLEBID_SERVER = 669;
	
	/**
	 * 查询申请夺城时间
	 * serial						int
	 * mapid						int					城市id
	 */
	public static final short TONG_BATTLETIME_CLIENT = 670;
	
	/**
	 * 查询申请夺城时间返回
	 * serial						int
	 * date							String				时间信息
	 */
	public static final short TONG_BATTLETIME_SERVER = 671;
	
	/**
	 * 城战传送战场
	 * serial						int
	 */
	public static final short TONG_BATTLE_TRANSPORT_CLIENT = 672;
	
	/**
	 * 城战传送战场返回
	 * serial						int
	 */
	public static final short TONG_BATTLE_TRANSPORT_SERVER = 673;
	
	/**
	 * 购买攻城道具
	 * serial						int
	 * type							byte			0为攻城车，1为箭塔
	 */
	public static final short TONGBATTLE_BUY_CLIENT = 674;
	
	/**
	 * 购买攻城道具返回
	 * serial						int
	 */
	public static final short TONGBATTLE_BUY_SERVER = 675;
	
	/**
	 * 城战标记
	 * serial						int
	 * playerid						int			角色id
	 */
	public static final short TONG_BATTLE_TAG_CLIENT = 676;
	
	/**
	 * 城战标记返回
	 * serial						int
	 */
	public static final short TONG_BATTLE_TAG_SERVER = 677;
	
	/**
	 * 查询占领军团
	 * serial						int
	 * signmapId					int				报名城市地图id
	 */
	public static final short TONGBATTLE_WINNER_INFO_CLIENT = 678;
	
	/**
	 * 查询占领军团返回
	 * serial						int
	 * tongname						String 			军团名称
	 */
	public static final short TONGBATTLE_WINNER_INFO_SERVER = 679;
	
	/**
	 * 发送客户端电话号码
	 * phone							string
	 */
	public static final short PHONE_NOTIFY_CLIENT = 680;
	
	/**
	 * 获取消费历史记录
	 * serial							int
	 * type								byte			1消费历史，2充值历史
     * startDate        				String          起始日期
     * endDate          				String          结束日期
     * startSeq         				int             起始记录号，1表示第一条
     * pageSize         				int             每页数据条数
     * timeType         				int             查询时间类型，可选，0 - 当日，1 - 指定月，2 - 10天内
     * queryType        				int             查询类型，可选，充值历史：0 - 全部；消费历史：0 - 查询所有客户端网游，1 - 查询所有WAP网游，2 - 查询自己
     * cmccUserId       				String          卓望平台用户ID（如果accountId不为-1，此条可选）
	 */
	public static final short IBUY_HISTORY_CLIENT = 681;
	
	/**
	 * 消费历史记录
	 * serial							int
	 * count							int				返回记录数量
     * 循环N次
     *   point			int				点数(单位1点)
     *   info			String			充值/消费信息
	 */
	public static final short IBUY_HISTORY_SERVER = 682;
	
	/**
	 * 查询军团城战城池当前税率
	 * serial			int
	 * mapid			int				
	 */
	public static final short TONG_BATTLE_TAX_CLIENT = 683;
	
	/**
	 * 查询军团城战城池当前税率返回
	 * serial			int
	 * tax				byte
	 */
	public static final short TONG_BATTLE_TAX_SERVER = 684;
	
	/**
	 * 发布军团税率
	 * serial			int
	 * tax				byte			税率
	 */
	public static final short TONG_BATTLE_MAKETAX_CLIENT = 685;
	
	/**
	 * 发布军团税率返回
	 * serial			int
	 */
	public static final short TONG_BATTLE_MAKETAX_SERVER = 686;
	
	/**
	 * 取消城战标记
	 * serial					int
	 * playerid					int				角色id
	 */
	public static final short TONG_BATTLE_UNTAG_CLIENT = 687;
	
	/**
	 * 取消城战标记返回
	 * serial					int
	 */
	public static final short TONG_BATTLE_UNTAG_SERVER = 688;
	
	/**
	 * 放弃占领城池
	 * serial					int
	 */
	public static final short TONG_BATTLE_ABANDON_CLIENT = 689;
	
	/**
	 * 放弃占领城池返回
	 * serial					int
	 */
	public static final short TONG_BATTLE_ABANDON_SERVER = 690;
	
	/**
	 * 领取经验
	 * serial					int
	 */
	public static final short TONG_BATTLE_GETEXP_CLIENT = 691;
	
	/**
	 * 领取经验返回
	 * serial					int
	 */
	public static final short TONG_BATTLE_GETEXP_SERVER = 692;
	
	/**
	 * 是否允许充值，仅台湾版本
	 */
	public static final short QME_CANPAY_CLIENT = 693;
	
	/**
	 * 是否允许							byte(1 允许 0 不允许)
	 */
	public static final short QME_CANPAY_SERVER = 694;
	
	/**
	 * 客户端直接充值模式，请求生成一个新的订单。
	 * serial					int
	 * phone					String 手机号
	 * clientid					String 手机串号
	 */
	public static final short CLIENT_CHARGE_GET_ORDER_CLIENT = 703;
	/**
	 * 客户端直接充值模式，生成订单成功。
	 * serial					int
	 * orderid					String 订单ID
	 * enckey					String 加密密钥
	 */
	public static final short CLIENT_CHARGE_GET_ORDER_SERVER = 704;
	/**
	 * 客户端直接充值模式，验证订单信息。
	 * serial					int
	 * receipt					byte[] 加密订单信息
	 */
	public static final short CLIENT_CHARGE_CHECK_RECEIPT_CLIENT = 705;
	/**
	 * 客户端直接充值模式，验证订单信息成功。
	 * serial					int
	 */
	public static final short CLIENT_CHARGE_CHECK_RECEIPT_SERVER = 706;
	
	/**
	 * 请求地图中所有NPC资源的文件名字
	 * serial					int
	 * mapId					short		地图ID
	 */
	public static final short MAP_NPC_CLINT = 707;
	
	/**
	 * 请求地图中所有NPC资源的文件名字返回
	 * serial					int
	 * size						int
	 * 		fileName			String
	 */
	public static final short MAP_NPC_SERVER = 708;
	
	/**
	 * CMCC充值请求
	 * serial							int
	 * cmccUserId						string
	 * cmccUserKey						string
	 * amount							int
	 */
	public static final short CMCC_CHARGE_CLIENT  = 901;
	
	/**
	 * CMCC充值成功
	 * serial							int
	 * 充值信息							string
	 */
	public static final short CMCC_CHARGE_SERVER = 902;
	
	/**
	 * 获取系统当前时间以及往前数7天的时间
	 * serial							int
	 */
	public static final short CMCC_CHARGE_TIME_CLIENT = 903;
	
	/**
	 * 系统当前时间以及往前数7天的时间
	 * serial							int
	 * startTime						string(yyyyMMdd)
	 * endTime							string(yyyyMMdd)
	 */
	public static final short CMCC_CHARGE_TIME_SERVER = 904;
	
	/**
	 * 请求话费专区商品列表
	 * serial								int
	 * cmccuserid							string
	 * cmccuserkey							string
	 */
	public static final short CMCC_ISHOP_LIST_CLIENT = 905;
	
	/**
	 * 返回商店商品列表
	 * serial		int
	 * count		byte		商店数量
	 * 循环N次
	 *   shopID		short		商店ID
	 *   title		String		商店标题
	 *   itemCount	byte		商品数量
	 *   循环N次
	 *     id		int			物品ID
	 *     name		String		物品名称
	 *     quality	byte		品质
	 *     icon		byte		图标ID
	 *     remain	short		剩余数量，0表示无限制
	 *     limit	byte		购买上限，0表示无限制
	 *     reqcnt	byte		购买需求项数量，0表示无限制
	 *       循环N次
	 *         reqtype	byte	需求类型，见Shop类中的常量
	 *         amount	String	需求金钱/i币/荣誉，或物品数量，或军衔ID
	 *         deduct	byte	是否扣除，1表示是，0表示否
	 *         rank		String	需求荣誉名称，只有需求类型为TYPE_RANK时有效
	 *         itemID	int		物品ID，只有需求类型为TYPE_ITEM时有效
	 *         itemName	String	物品名称，只有需求类型为TYPE_ITEM时有效
	 *         quality	byte	品质，只有需求类型为TYPE_ITEM时有效
	 *         icon		byte	图标ID，只有需求类型为TYPE_ITEM时有效
	 *         varDesc  String  额外条件描述，只有需求类型为TYPE_VARIABLE时有效
	 *  税率			int (百分比*100)
	 */
	public static final short CMCC_ISHOP_LIST_SERVER = 906;
	
	/**
	 * 请求话费专区购买商品
	 * serial		int
	 * shopID		int		商店ID
	 * itemID		int			物品ID
	 * count		short		购买数量
	 */
	public static final short CMCC_ISHOP_BUY_CLIENT = 907;
	
	/**
	 * 购买商品成功
	 * serial		int
	 * itemID		int			物品ID
	 * count		short		购买数量
	 * itemName		String		物品名称
	 * quality		byte		品质
	 * icon			byte		图标ID
	 */
	public static final short CMCC_ISHOP_BUY_SERVER = 908;
	
	/**
	 * PUSHDOWNLOAD
	 * url							string
	 */
	public static final short CMCC_PUSHDOWNLOAD_SERVER = 909;
	
	/**
	 * PUSHDOWNLOADOK		
	 */
	public static final short CMCC_PUSHDOWNLOADOK_CLINET = 910;
	
	/**
	 * 自动装备打孔
	 * serial					int
	 * itemId					int				装备ID
	 * itemInstanceId			int				装备instanceId
	 * wantHole					byte			期望孔数
	 */
	public static final short AUTO_ADDHOLE_CLIENT = 911;
	
	/**
	 * 自动装备打孔结果返回
	 * serial					int
	 * realHoles				byte			实际打孔数
	 * useBannerAccount			int				消耗打孔符数量
	 * money					int				实际花费金钱
	 * cause					byte			原因（0为成功,1为金钱不足,2为打孔符不足）
	 */
	public static final short AUTO_ADDHOLE_SERVER = 912;
	
	/**
	 * 个人成就分类
	 * serial                   int
	 * instanceId               int  //玩家ID
	 */
	public static final short PERSONAL_ACHIEVEMENT_CLIENT=  913;
		
	/**
	 * 个人成就分类返回
	 * serial                   int
	 * size                     short           成就分类个数
	 * 循环N次
	 * 		name                     String         该成就分类名称
	 * 		count                    int            该分类完成成就项数
	 *      total                    int            该分类总的项数
	 *      point                    int            获得的成就点数
	 * 
	 * 
	 * name                     String          成就分类
	 * count                    int             该分类完成成就项数
	 * total                    int             总共完成成就项数
	 * totalnumber              int             成就点数
	 
	 */
	public static final short PERSONAL_ACHIEVEMENT_SERVER =  914;	

	/**
	 * 个人成就
	 * serial                   int
	 * instanceId               int            //玩家ID
	 * type                     int             成就分类索引（0为综合楼，1为荣誉类，2为生活类，3击杀敌国统计） //按913下发顺序
	 */
	public static final short PERSONAL_ACHIEVEMENT_DETAIL_CLIENT = 915;
	
	/**
	 * 个人成就返回
	 * serial                   int
	 * size                     short           0-2类成就数量
	 * 循环N次
	 	 *ID                        int             成就唯一ID
		 * name                     String          成就名称
		 * accomplish               byte            成就记录（0为未完成，1为完成）
		 * dec                      String          描述
		 * point                    int             完成该成就获得的点数
		 * date                     String          完成日期
		 * accomplish2              byte            自己的成就记录
		 * size                     short           该成就奖励物品个数
		 * 循环N次
		   * itemId                 int             奖励物品Id
		   * iconId                 int             奖励物品IconId
		   * count                  int             奖励物品个数
		   * itemDec                String          奖励物品描述
		   * itemName               String          物品名称
	 * size                     short           3类成就数量
	 * 循环N次
	     * faction                  int             国家代码(1 魏国 2 蜀国 3 吴国)
	     * totalnum                 int             击杀敌国总数
	    * size                     short            该国职业数量
	    * 循环N次
	      * killclazz               int             杀死该国的人的职业
	      * num                     int             杀死该职业人的数量
	      
	 */
	public static final short PERSONAL_ACHIEVEMENT_DETAIL_SERVER =  916;
	
	/**
	 * 客服公告查看
	 * serial                   int
	 */
	public static final short CLIENTBBS_LOOK_OVER_CLIENT = 917;
	
	/**
	 * 客服公告查看返回
	 * serial                   int
	 * textexplation            String            文字说明
	 * 循环N次
	 * size                     short             
	 *    	activeitem               String            活动名称
	 * 		detail                   String            更新具体内容
	 * offlineexp               String              离线经验
	 */
	public static final short CLIENTBBS_LOOK_OVER_SERVER = 918;
    
	/**
	 * 玩家退出提示
	 * serial                   int
	 */
	public static final short PLAYER_LOGOUT_CLIENT = 919;
	
	/**
	 * 玩家推出提示返回
	 * serial                   int
	 * exp                      int               本次登录获得的经验
	 * money                    int               本次登录获得的金钱
	 * credit                   int               本次登录获得的功勋
	 * questfinished            int               本次登录完成的任务
	 * questunfinished          int               尚未完成的任务
	 * 循环N次
	 * size                     short
	 *     activeName                String             当前进行的活动
	 * 循环N次
	 * size                     short
	 *     activeName                String              未完成的日常活动
	 * offLineExp               int                未兑换的离线经验
	 * 
	 */
	public static final short PLAYER_LOGOUT_SERVER = 920;
	
	/**
	 * 保存客服公告到xml文件
	 * serial                   int
	 * minLevel                 int                 等级下限
	 * maxLevel                 int                 等级上限
	 * textexplation            String            文字说明
	 * 循环N次
	 * size                     short             
	 *    	activeitem               String            活动名称
	 * 		detail                   String            更新具体内容
	 */
	public static final short CLIENTBBS_SAVE_CLIENT = 921;
	
	/**
	 * 保存客服公告到xml文件成功
	 * serial                    int
	 */
	public static final short CLIENTBBS_SAVE_SERVER = 922;
	
	/**
	 * 选美报名
	 * serial					int
	 * sloagn					String			选举口号
	 */
	public static final short BEAUTYPARADE_SIGNUP_CLIENT = 923;
	
	/**
	 * 选美报名结果
	 * serial					int
	 */
	public static final short BEAUTYPARADE_SIGNUP_SERVER = 924;
	
	/**
	 * 选美投票
	 * serial					int
	 * targetId					int					候选人ID
	 * type						byte				投票方式（1 鲜花，2 大花篮， 3黄玫瑰）
	 * count					int					鲜花数量
	 */
	public static final short BEAUTYPARADE_VOTE_CLIENT = 925;
	
	/**
	 * 选美投票结果
	 * serial					int
	 */
	public static final short BEAUTYPARADE_VOTE_SERVER = 926;
	
	/**
	 * 选美候选人列表
	 * serial					int
	 */
	public static final short BEAUTYPARADE_LIST_CLINET = 927;
	
	/**
	 * 选美候选人列表返回
	 * serial					int
	 * num						int				候选人数量
	 * 			score			int				排名
	 * 			playerId		int				候选人ID
	 * 			name			String			候选人姓名
	 * 			sex				byte			候选人性别
	 * 			slogan			String			候选人口号
	 * 			votes			int				候选人目前所得票数
	 * 			faction			byte			候选人阵营
	 * mPlayerId				int				投票最多的玩家ID
	 * mPlayerNmae				String 			投票最多的玩家姓名
	 * mVotes					int				投票最多的玩家所投的票数
	 * mFaction					byte				投票最多的玩家阵营
	 */
	public static final short 	BEAUTYPARADE_LIST_SERVER = 928;
	
	/**
	 * 查看好友的名次
	 * serial					int
	 */
	public static final short BEAUTY_FRIEND_LIST_CLIENT = 929;
	
	/**
	 * 查看好友的名次返回
	 * serial					int
	 *  num						int				候选人数量
	 *  		score			int				名次
	 * 			playerId		int				候选人ID
	 * 			name			String			候选人姓名
	 * 			sex				byte			候选人性别
	 * 			slogan			String			候选人口号
	 * 			votes			int				候选人目前所得票数
	 * 			faction			byte			候选人阵营
	 */
	public static final short BEAUTY_FRIEND_LIST_SERVER = 930;
	
	/**
	 * 开启婚礼
	 * serial					int
	 * level                    int             婚礼等级
	 * guestLevel               int             参加婚礼的人的最低等级
	 * jewelTypeNum             int             宝石总种类数
	 * 		循环jewelTypeNum次
	 * 			itemId          int              宝石id
	 * 			itemNum         int              宝石数量             
	 */
	public static final short WEDDING_OPEN_CLIENT = 931;
	
	/**
	 * 开启婚礼返回
	 * serial					int
	 */
	public static final short WEDDING_OPEN_SERVER = 932;
	
	/**
	 * 婚庆礼堂列表
	 * serial					int
	 */
	public static final short WEDDING_LIST_CLIENT = 933;
	
	/**
	 * 婚庆礼堂列表返回
	 * serial					int
	 * num						int
	 * 				manId		int			男方id
	 * 				manName		String		男方名字
	 * 				womanId		int			女方id
	 * 				womanNamw	String		女方名字
	 */
	public static final short WEDDING_LIST_SERVER = 934;
	
	/**
	 * 参加婚礼
	 * serial					int
	 * manId					int			参加对方男方的Id
	 */
	public static final short WEDDING_JOINWEDDING_CLIENT = 935;
	
	/**
	 * 参加婚礼返回
	 * serial					int
	 */
	public static final short WEDDING_JOINWEDDING_SERVER = 936;
	
	/**
	 * 婚礼礼堂签到
	 * serrial					int
	 */
	public static final short WEDDING_SIGNIN_CLIENT = 937;
	
	/**
	 * 婚礼礼堂签到返回
	 * serial					int
	 */
	public static final short WEDDING_SIGNIN_SERVER = 938;
	
	/**
	 * 礼堂签到人列表
	 * serial					int
	 */
	public static final short WEDDING_SIGNINLIST_CLIENT = 939;
	
	/**
	 * 礼堂签到人列表返回
	 * sirail					int
	 * num						int
	 * 				playerId	int
	 * 				name		String
	 *              type        byte(0-好友，1-团友，2-其他）
	 *              isfetch     byte(0-未领取，1-已领取）             
	 */
	public static final short WEDDING_SIGNINLIST_SERVER = 940;
	
	/**
	 * 发红包
	 * serial					int
	 * itemId					int				物品ID
	 * targerPlyerId			int				目标人ID
	 * count                    int             物品数量
	 */
	public static final short WEDDING_GIFT_CLINT = 941;
	
	/**
	 * 发红包返回
	 * serial					int
	 */
	public static final short WEDDING_GIFT_SERVER = 942;
	
	/**
	 * 婚礼踢宾客
	 * serial					int
	 * targetId					int				被踢的宾客的ID
	 */
	public static final short WEDDING_KICK_CLINT = 943;
	
	/**
	 * 婚礼踢宾客返回
	 * serial					int
	 */
	public static final short WEDDING_KICK_SERVER = 944;
	
    /**
     * 婚礼领取奖励
     * serial                   int
     */
	public static final short WEDDING_GETEXP_CLIENT = 945;
	
	/**
     * 婚礼领取奖励
     * serial                   int
     */
	public static final short WEDDING_GETEXP_SERVER = 946;
	
	/**
	 * 邀请加入队伍
	 * id							int
	 */
	public static final short PARTY_JOIN_CLIENT = 947;

	/**
	 * 申请加入队伍
	 * 申请序号						int
	 * 队长名字						string
	 * 队长等级						byte
	 * 队长职业						byte
	 * 队长性别                     byte
	 */
	public static final short PARTY_JOIN_SERVER = 948;
	/**
	 * 答应组队请求
	 * 邀请序号						int
	 * 答复                         byte //0拒绝1同意
	 */
	public static final short PARTY_JOIN_ANSWER_CLIENT = 949;
	
	/**
	 * 名人堂玩家保存信息
	 * serial                   int 
	 * playerid                 int            放置的玩家的id
	 */
	public static final short FAME_ADDINFO_CLIENT = 950;
	
	/**
	 * 名人堂玩家保存信息返回
	 * serial                   int 
	 */
	public static final short FAME_ADDINFO_SERVER = 951;
	/**
	 * 快速注册中选择性别职业国籍
	 * serial                   int 
	 * info                     short       低5位性别(有效值范围[1,2])，中5位职业(有效值范围[1,4])，高5位国籍(有效值范围[1,3]),0表示无改动  
	 */
	public static final short CHANGE_PLAYER_INFO_CLIENT = 952;
	
	/**
	 * 快速注册中选择性别职业国籍成功
	 * serial                   int 
	 */
	public static final short CHANGE_PLAYER_INFO_SERVER = 953;
	
	/**
	 * 比武招亲报名
	 * serial					int
	 */
	public static final short DUEL_SIGNUP_CLIENT = 954;
	
	/**
	 * 比武招亲报名返回
	 * serial					int
	 * min						int
	 */
	public static final short DUEL_SIGNUP_SERVER = 955;
	
	/**
	 * 玩家提交密信
	 * serial						int
	 * count					    int				密信的个数
	 */
	public static final short HANDIN_LETTER_CLIENT = 956;
	
	/**
	 * 玩家提交密信返回
	 * serial						int
	 */
	public static final short HANDIN_LETTER_SERVER = 957;
	
	/**
	 * 比武招亲排行榜查看
	 * serial						int
	 */
	public static final short DUEL_SCORE_CLIENT = 958;
	
	/**
	 * 比武招亲排行榜查看结果
	 * serial						int
	 * num							int
	 * 			title				String
	 */
	public static final short DUEL_SCORE_SERVER = 959;
	
	/**
	 * 比武招亲领取称号
	 * serial						int
	 */
	public static final short DUEL_GETTITLE_CLIENT = 960;
	
	/**
	 * 比武招亲领取称号返回
	 * serial						int
	 */
	public static final short DUEL_GETTITLE_SERVER = 961;
	
	/**
	 * 玩家最近一小时充值记录查询
	 * serial						int
	 */
	public static final short CHARGE_RECORD_CLIENT = 962;
	
	/**
	 * 玩家最近一小时充值机理查询返回
	 * serial						int
	 * size							int
	 * 		card					String				卡号
	 * 		password				String				密码
	 * 		orderTime				String				下单时间
	 * 		money					String				金额
	 * 		state					String				状态(成功/失败/进行中)
	 */
	public static final short CHARGE_RECORD_SERVER = 963;
	
	/** 
	 * 玩家合成卡片
	 * serial                       int
	 * size                         int
	 *    itemId                           int                添加的卡片id
	 */
	public static final short CARD_MERGE_CLINET = 964;
	
	/**
	 * 玩家合成卡片返回
	 * serial                       int
	 * cardId                       int                      合成卡片的id
	 * cardName                     String                   合成卡片的名字
	 */
	public static final short CARD_MERGE_SERVER = 965;
	
	/**
	 * 玩家收藏的卡片列表
	 * serial                      int
	 * personId                    int                    被查看的玩家Id
	 */
	public static final short CARD_LIST_CLIENT = 966;
	
	/**
	 * 玩家收藏的卡片列表返回
	 * serial                      int
	 * size                        int
	 *     groupId                     int                 卡片套装的id
	 *     groupName                   String              卡片套装的名称
	 *     num                         int                 已经收藏卡片的数量
	 *     sum                         int                 套装卡片总数
	 *     per                         int                 完成本套的百分数
	 * nums                        int                总共收藏卡片的数量
	 * sums                        int                全部套装卡片总数
	 * pers                        int                完成占总张数的百分数
	 * exp						   int				  卡片经验
	 */
	public static final short CARD_LIST_SERVER = 967;
	
	/**
	 * 玩家具体卡片列表
	 * serial                      int
	 * playerId                    int                   被查看的玩家Id
	 * groupId                     int                   卡片套装的id
	 */
	public static final short CARD_DETAILLIST_CLIENT = 968;
	
	/**
	 * 玩家具体卡片列表返回
	 * serial                      int
	 * size                        int
	 *     type                            byte              是否收藏(0-未收藏，1-已收藏)
	 *     name                            String            卡片名称
	 *     // mergeDec                     String            // 合成信息
	 *     cardId                          int               卡片Id
	 *     cardDec                         String            卡片描述
	 *     quality						   byte				 卡片品质
	 *     res							   String			 卡片资源
	 *     hasPicture			    	   byte			     1有图卡片、0无图卡片
	 *     // energy					   short			 // 卡片能量
	 *     // maxenergy					   shor			 	 // 卡片最大能量
	 *     star							   byte			     卡片星级
	 *     // buffDec                      String            // 卡片BUFF描述
	 *     equip						   byte			 	 是否已镶嵌（0否 1是）
	 *     level						   byte				 卡片级别
	 *     exp							   int				 升级所需经验
	 *     attDesc						   String			 属性增强描述
	 *     nextLevelDesc                   String           下一级属性增强描述
	 *     totalExp							int					卡片累计经验
	 */
	public static final short CARD_DETAILLIST_SERVER = 969;
	
	/**
	 * 显示卡片名字
	 * serial                      int
	 * groupId                     int            要显示名字的卡片套装id
	 */
	public static final short CARD_SHOWNAME_CLIENT = 970;
	
	/**
	 * 显示卡片名字返回
	 * serial                      int
	 */
	public static final short CARD_SHOWNAME_SERVER = 971;
	
	/**
	 * 塔防战报名
	 * serial					int
	 */
	public static final short TOWERDEFEND_SIGNUP_CLIENT = 972;
	
	/**
	 * 塔防战报名返回
	 * serial					int
	 */
	public static final short TOWERDEFEND_SIGNUP_SERVER = 973;
	
	/**
	 * 是否重复收藏
	 * serial                      int
	 * yesorno                     byte                  是否重复收藏
	 * cardId                      int                   卡片ID
	 * gridId					   int					 包格Id
	 */
	public static final short CARD_COLLECTAGAIN_CLIENT = 974;
	
	/**
	 * 是否重复收藏返回
	 * serial                      int
	 * type                        byte (-1不成功，1闪卡0普卡)
	 */
	public static final short CARD_COLLECTAGAIN_SERVER = 975;
	
	/**
	 * 卡片收藏
	 * serial                      int
	 * gridId                      int
	 * itemId                      int
	 * itemInstId                  int
	 */
	public static final short CARD_COLLECTION_CLIENT = 976;
	
	/**
	 * 卡片收藏返回
	 * serial                      int
	 * quality                     byte
	 */
	public static final short CARD_COLLECTION_SERVER = 977;
	
	/**
	 * 快速购买商品
	 * serial		int
	 * shopID		int		商店ID
	 * itemID		int			物品ID
	 * count		short		购买数量
	 */
	public static final short SHOP_QUICK_BUY_CLIENT = 978;
	
	/**
	 * 玩家未开启名字的卡片系列
	 * serial                      int
	 */
	public static final short CARD_LIST_4SHEET_CLIENT = 979;
	
	/**
	 * 玩家收藏的卡片列表返回
	 * serial                      int
	 * size                        int
	 *     groupId                     int                 卡片套装的id
	 *     groupName                   String              卡片套装的名称
	 */
	public static final short CARD_LIST_4SHEET_SERVER = 980;
	
	/**
	 * 举报挂机
	 * serial						int
	 * playerId						int						被举报playerid
	 */
	public static final short REPORT_CLIENT = 981;
	
	/**
	 * 举报挂机返回
	 * serial						int
	 */
	public static final short REPORT_SERVER = 982;
	
	/**
	 * 商店商品列表(附加一周时间内元宝商店购买前20名的商品列表)
	 * serial				int
	 * shopIDs				int[]		商店ID列表
	 */
	public static final short SHOP_TOPLIST_CLIENT = 983;
	
	/**
	 * 返回商店商品列表(附加一周时间内元宝商店购买前20名的商品列表)
	 * serial		int
	 * count		byte		商店数量
	 * 循环N次
	 *   shopID		short		商店ID
	 *   title		String		商店标题
	 *   itemCount	byte		商品数量
	 *   循环N次
	 *     id		int			物品ID
	 *     name		String		物品名称
	 *     quality	byte		品质
	 *     image	byte		图标图片ID
	 *     icon		byte		图标ID
	 *     remain	short		剩余数量，0表示无限制
	 *     limit	byte		购买上限，0表示无限制
	 *     reqcnt	byte		购买需求项数量，0表示无限制
	 *       循环N次
	 *         reqtype	byte	需求类型，见Shop类中的常量
	 *         amount	String	需求金钱/i币/荣誉，或物品数量，或军衔ID
	 *         deduct	byte	是否扣除，1表示是，0表示否
	 *         rank		String	需求荣誉名称，只有需求类型为TYPE_RANK时有效
	 *         itemID	int		物品ID，只有需求类型为TYPE_ITEM时有效
	 *         itemName	String	物品名称，只有需求类型为TYPE_ITEM时有效
	 *         quality	byte	品质，只有需求类型为TYPE_ITEM时有效
	 *         icon		byte	图标ID，只有需求类型为TYPE_ITEM时有效
	 *         varDesc  String  额外条件描述，只有需求类型为TYPE_VARIABLE时有效
	 *  一周时间内元宝商店购买前20名的商品列表:
	 *  循环N次
	 *  	d		int			物品ID
	 *     name		String		物品名称
	 *     quality	byte		品质
	 *     image	byte		图标图片ID
	 *     icon		byte		图标ID
	 *     remain	short		剩余数量，0表示无限制
	 *     limit	byte		购买上限，0表示无限制
	 *     reqcnt	byte		购买需求项数量，0表示无限制
	 *       循环N次
	 *         reqtype	byte	需求类型，见Shop类中的常量
	 *         amount	String	需求金钱/i币/荣誉，或物品数量，或军衔ID
	 *         deduct	byte	是否扣除，1表示是，0表示否
	 *         rank		String	需求荣誉名称，只有需求类型为TYPE_RANK时有效
	 *         itemID	int		物品ID，只有需求类型为TYPE_ITEM时有效
	 *         itemName	String	物品名称，只有需求类型为TYPE_ITEM时有效
	 *         quality	byte	品质，只有需求类型为TYPE_ITEM时有效
	 *         icon		byte	图标ID，只有需求类型为TYPE_ITEM时有效
	 *         varDesc  String  额外条件描述，只有需求类型为TYPE_VARIABLE时有效
	 * 税率			int (百分比*100)
	 */
	public static final short SHOP_TOPLIST_SERVER = 984;
	
	/**
	 * 活动指引信息
	 * serial				int
	 * areaType				byte				级别类型(0最适宜 1小5级 2大5级)
	 * timeType				byte				时间类型(0、所有类型 1、近一个月)
	 * type					byte				活动类型(0、所有类型 1、活动 2、任务 3、怪物 4、商品)
	 * rewardType			byte				奖励类型(0、所有类型 1、金钱 2、珍珠 3、声望 4、战功)
	 * start				short				页号，0表示第一页
	 * page					short				页大小
	 */
	public static final short INDICATOR_AREA_TASK_CLIENT = 985;
	
	/**
	 * 活动指引信息返回
	 * serial	int
	 * count	short		循环次数
	 * 		name		String	活动名称
	 * 		mapId		int		地图id
	 * 		mapName		String	地图名称
	 * 		x			int		横坐标		
	 * 		y			int		纵坐标
	 */
	public static final short INDICATOR_AREA_TASK_SERVER = 986;
	
	/**
	 * 批量出售白装
	 * serial         int
	 */
	public static final short DELETE_WHITE_EQUIPMENT_CLIENT = 987;
	
	/**
	 * 批量出售白装返回
	 * serial         int
	 */
	public static final short DELETE_WHITE_EQUIPMENT_SERVER = 988;
	/**
	 * 聊天中查看卡片请求信息
	 * serial         int
	 * id			  int
	 * level		  byte
	 */
	public static final short CARD_INFO_CLIENT = 989; 
	/**
	 * 聊天中查看卡片返回信息
	 * serial		  int
	 * desc			  String   描述
	 * formula		  String   配方
	 * desc			  String   技能卡片描述
	 * quality		  byte     卡片品质
	 */
	public static final short CARD_INFO_SERVER = 990;
	
	/**
	 * NPC兑换物品
	 * serial						int
	 * requestId					int				领取物品ID
	 * itemId						int				背包物品ID
	 * instanceId					int				物品instanceId
	 * condition					String			兑换条件（"5stars":星级达5星）
	 */
	public static final short NPC_EXCHANGE_CLIENT = 991;
	 
	/**
	 * NPC兑换物品返回
	 * serial						int
	 */
	public static final short NPC_EXCHANGE_SERVER = 992;
	
	/**
	 * 获取指定装备信息
	 * serial						int
	 * value						String			(套装前缀,为避免国际化的麻烦和可读性用简称的汉语拼音表示)
	 */
	public static final short APPOINTITEM_DESC_CLIENT = 993;
	
	/**
	 * 获取指定装备信息返回
	 * serial						int
	 * num							int
	 * 		itemId					int			物品ID
	 * 		conId					int			图标
	 * 		name					String		名称
	 * 		desc					String		描述
	 * 		qulity					byte		品质
	 */
	public static final short APPOINITEM_DESC_SERVER = 994;
	
	/**
	 * 创建血盟
	 * serial				int
	 * name					String			血盟名称
	 */
	public static final short ASSOCIATION_CREATE_CLIENT = 995;
	
	/**
	 * 创建血盟返回
	 * serial				int
	 */
	public static final short ASSOCIATION_CREATE_SERFER = 996;
	
	/**
	 * 血盟权利
	 * serial				int
	 * type					byte			(0邀请结盟、1转让结盟、2剔除成员、3删除结盟、4退出结盟)
	 * value				int				(邀请目标id、转让目标id、被剔除成员id、空、空)
	 */
	public static final short ASSOCIATION_EXECISE_CLIENT = 	997;
	
	/**
	 * 血盟权利返回
	 * serial				int
	 */
	public static final short ASSOCIATION_EXECISE_SERVER = 998;
	
	/**
	 * 下发结盟邀请
	 * sourcePlayerId		int				请求者ID
	 * sourceName			String			请求者姓名
	 * associationId		int				血盟ID
	 * name					String			血盟名称
	 */
	public static final short ASSOCIATION_INVITE_SERVER = 999;
	
	/**
	 * ADMIN错误
	 * serial								int
	 * type									byte
	 * message								string
	 */
	
	public static final short ADMIN_ERROR = 1000;
	
	/**
	 * ADMIN客户端登录
	 * serial								int
	 * name									string
	 * password								string
	 */
	public static final short ADMIN_LOGIN_CLIENT = 1001;
	
	/**
	 * ADMIN客户端登录成功
	 * serial								int
	 */
	public static final short ADMIN_LOGIN_SERVER = 1002;
	
	/**
	 * 查看当前玩家列表
	 * serial								int
	 * mapId								int(-1 代表所有玩家)
	 */
	public static final short ADMIN_WHO_CLIENT = 1003;
	
	/**
	 * 玩家列表
	 * serial								int
	 * count								short
	 * 循环N次
	 * 	玩家ID								int
	 *  name								string
	 *  性别									byte
	 *  等级									byte
	 *  职业									byte (0 武将 1 刺客 2 谋士 3 方士)
	 *  阵营									byte (1 魏 2 蜀 3 吴)
	 *  玩家mapId							int
	 *  x									short
	 *  y									short
	 *  帮派									string
	 *  
	 */
	public static final short ADMIN_WHO_SERVER = 1004;
	
	/**
	 * 取指定玩家信息
	 * serial								int
	 * id									int
	 * name									string(优先name,如果name长度为0,那么根据id载入玩家) 
	 */
	public static final short ADMIN_PLAYER_INFO_CLIENT = 1005;
	
	/**
	 * 玩家信息
	 * serial								int
	 * 基本信息								byte[]
	 * 背包信息								byte[]
	 * 技能信息								byte[]
	 * 坐骑信息                              byte[]
	 * 仓库信息                              byte[]
	 * 随从bag信息                           byte[]
	 */
	public static final short ADMIN_PLAYER_INFO_SERVER = 1006;
	
	/**
	 * 获取玩家帐号信息
	 * serial								int
	 * 角色ID								int
	 */
	public static final short ADMIN_ACCOUNT_INFO_CLIENT = 1007;
	
	/**
	 * 玩家帐号信息
	 * serial								int
	 * 帐号Id								int
	 * 帐号名								string
	 * 帐号密码								string
	 * 电话									string
	 */
	public static final short ADMIN_ACCOUNT_INFO_SERVER = 1008;
	
	/**
	 * 玩家角色列表
	 * serial								int
	 * 帐号Id								int
	 */
	public static final short ADMIN_PLAYERLIST_CLIENT = 1009;
	
	/**
	 * 玩家角色列表
	 * serial								int
	 * 帐号Id								int
	 * count								short
	 * 循环N次
	 * 	Id						int
	 * 	名字						string
	 * 	性别						byte
	 * 	等级						byte
	 *	职业						byte
	 *	阵营						byte
	 *  地图Id					short
	 */
	public static final short ADMIN_PLAYERLIST_SERVER = 1010;
	
	
	/**
	 * 改变账号状态
	 * serial									int
	 * 帐号Id									int
	 * 状态										int(0 封号 1 解封)
	 * 信息										string
	 */
	public static final short ADMIN_ACCOUNT_STATUS_CLIENT = 1011;
	
	/**
	 * 封账号成功(因为此协议认证是没有返回的，所以这个包只能代表已经向认证服务器发送了请求)
	 * serial									int
	 */
	public static final short ADMIN_ACCOUNT_STATUS_SERVER = 1012;
	
	/**
	 * 禁言/解封
	 * serial									int
	 * playerId									int
	 * flag										int( 1 世界 1<<1 国家 1<<2 地图 1<<3 家乡 1<<4 私聊 )	
	 * time										long (如果时间是0，那么就是解封)
	 */
	public static final short ADMIN_CHAT_FORBID_CLIENT = 1013;
	
	/**
	 * 禁言/解封成功
	 * serial									int
	 */
	public static final short ADMIN_CHAT_FORBID_SERVER = 1014;
	
	/**
	 * 禁飞鸽/解禁
	 * serial									int
	 * playerId									int
	 * time										long(如果时间是0，那么就是解封)
	 */
	public static final short ADMIN_MAIL_FORBID_CLIENT = 1015;
	

	
	/**
	 * 禁飞鸽/解禁成功
	 * serial									int
	 */
	public static final short ADMIN_MAIL_FORBID_SERVER = 1016;
	
	/**
	 * 进入聊天频道
	 * serial									int
	 * channel									int (0 世界 1 国家 2 地区 3 家乡 4 帮派 7 系统)
	 * targetId									int (如果是国家:1 魏 2 蜀 3 吴;如果是地区:mapId;如果是帮派:帮派id)
	 * 家乡名称									string
	 */
	public static final short ADMIN_JOIN_CHATCHANNEL_CLIENT = 1016;
	
	/**
	 * 进入聊天频道成功
	 * serial
	 */
	public static final short ADMIN_JOIN_CHATCHANNEL_SERVER = 1017;
	
	/**
	 * 发送信息						
	 * channel									int
	 * targetId									int
	 * nativeString								string
	 * message									string
	 * sirial									int	
	 */
	public static final short ADMIN_CHAT_CLIENT = 1018;
	
	/**
	 * 踢玩家
	 * playerId									int
	 * time										long (0 解除)
	 */
	public static final short ADMIN_KICK_CLIENT = 1019;
	
	/**
	 * 获取GM请求列表
	 * serial									int
	 * 每页条数									short
	 * 页数										short
	 * date										String(格式：2009-09-10,如果为空串则查询所有数据)
	 */
	public static final short ADMIN_GMREQUEST_LIST_CLIENT = 1022;
	
	/**
	 * GM请求列表
	 * serial									int
	 * 每页条数									short
	 * 页数										short
	 * 信件的总数								int
	 * 本页实际条数								short
	 * 循环N次
	 * 	请求Id						int
	 *  请求类型						byte(暂时都为0，以后分各种问题组)
	 * 	请求玩家Id					int
	 * 	请求玩家名					string
	 * 	请求内容						string
	 * 	请求状态						byte(0 未解决 1 解决)
	 * 	解决方案						string
	 * 	提交时间						long
	 *  玩家机型						string
	 *  玩家mapId					short
	 *  玩家x坐标					short
	 *  玩家y坐标					short
	 */
	public static final short ADMIN_GMREQUEST_LIST_SERVER = 1023;
	
	/**
	 * 解决GM请求
	 * 请求Id									int 
	 * 角色Id									int(如果为0，那么请求Id有效，如果不为0，那么角色Id有效)
	 * 解决邮件Title								string
	 * 解决方案									string
	 */
	public static final short ADMIN_GMREQUEST_SOLVE_CLIENT = 1024;
	
	/**
	 * 解决GM请求成功
	 * 请求Id									int
	 * 角色Id									int
	 * 解决GM名字								string
	 * 解决方案									string
	 */
	public static final short ADMIN_GMREQUEST_SOLVE_SERVER  = 1025;
	
	/**
	 * 删除GM请求
	 * 数量										short
	 * 循环N次
	 * 	请求Id									int
	 */
	public static final short ADMIN_GMREQUEST_DELETE_CLIENT = 1026;
	
	/**
	 * 删除GM请求成功
	 * 数量										short
	 * 循环N次
	 * 	请求Id									int
	 * gm名字									string
	 */
	public static final short ADMIN_GMREQUEST_DELETE_SERVER = 1027;
	
	/**
	 * 新增GM请求
	 * 	请求Id						int
	 *  请求类型						byte(0、玩家呼叫 1、举报挂机邮件)
	 * 	请求玩家Id					int
	 * 	请求玩家名					string
	 * 	请求内容						string
	 * 	请求状态						byte(0 未解决 1 解决)
	 * 	解决方案						string
	 * 	提交时间						long
	 *  玩家机型						string
	 *  玩家mapId					short
	 *  玩家x坐标					short
	 *  玩家y坐标					short
	 */
	public static final short ADMIN_GMREQUEST_ADDED_SERVER = 1028;
	
	/**
	 * 发送信件
	 * serial									int
	 * playerId									int
	 * title									string
	 * content									string
	 * ItemId									int (如果-1那么就是金钱)
	 * 数量										int
	 * 是否强制绑定								byte(0 不强制绑定 1 强制绑定)
	 * 星级										byte
	 * 孔数										byte(新增的孔数，不包括初始的孔数)
	 */
	public static final short ADMIN_SENDMAIL_CLIENT = 1029;
	
	/**
	 *  掉落组测试接口。
	 *  请求： Admin客户端  》 世界服务器
	 *  参数：
	 *  1：int 序列号
	 *  2：int 掉落组id
	 *  3：int 虚拟玩家级别
	 *  4：int 测试次数
	 *  
	 *  返回：Admin客户端  《 世界服务器
	 *  参数：
	 *  1：int 序列号
	 *  2：short 掉落格子数量
	 *  	 3 掉落物品详情，重复上述次数
	 *       3.1 int 物品id
	 *       3.2 String 物品名称
	 *       3.2 int 数量
	 */
	public static final short ADMIN_GM_DROP_TEST = 1030;
	/**
	 * 获取玩家关系。
	 * 请求：Admin客户端 》 世界服务器
	 * 参数：
	 *  1： int 序列号
	 *  2： int 玩家Id
	 *  
	 * 回复：Admin客户端 《 世界服务器
	 *  1： int 序列号
	 *  2： int 玩家Id
	 *  3：short  好友数量
	 *  4：结构：好友信息（重复好友数量次）
	 *    4.1 int 玩家id
	 *    4.2 String 玩家名称
	 *    4.3 int 关系度 
	 *  5：short  黑名单数量
	 *  6：结构：黑名单信息（重复黑名单数量次） 
	 *    6.1 int 玩家id
	 *    6.2 String 玩家名称
	 *    6.3 int 关系度 
	 *  7：short  仇人数量
	 *  8：结构：仇人信息（重复仇人数量次） 
	 *    8.1 int 玩家id
	 *    8.2 String 玩家名称
	 *    8.3 int 关系度 
	 *  9：short  临时关系数量
	 *  10：结构：临时关系信息（重复临时关系数量次） 
	 *    10.1 int 玩家id
	 *    10.2 String 玩家名称
	 *    10.3 int 关系度 
	 * 
	 */
	public static final short ADMIN_GM_PLAYER_RELATION = 1031;
	
	/**
	 * 运行指定的patch
	 * serial								int
	 * 全类名								string(eg:xx.xxx.xx)
	 */
	public static final short ADMIN_EXEC_CLIENT = 1032;
	
	/**
	 * 运行指定的patch成功
	 * serial								int
	 */
	public static final short ADMIN_EXEC_SERVER = 1033;
	
	/**
	 * 指定经验倍数
	 * serial								int
	 * 经验倍数								string(原始经验用1.0,双倍用2.0)
	 */
	public static final short ADMIN_EXPRATIO_CLIENT = 1034;
	
	/**
	 * 指定经验倍数成功
	 * serial								int
	 */
	public static final short ADMIN_EXPRATIO_SERVER = 1035;
	
	/**
	 * 跳转到指定地方(角色必须在线)
	 * serial								int
	 * playerId								int
	 * mapId								int (如果是-1那么跳转到地图的复活点)
	 * x									int
	 * y									int
	 */
	public static final short ADMIN_GOTO_CLIENT = 1036;
	
	/**
	 * 跳转成功								
	 * serial								int
	 */
	public static final short ADMIN_GOTO_SERVER = 1037;
	
	/**
	 * reload服务器信息
	 * serial								int
	 * param								string
	 */
	public static final short ADMIN_RELOAD_CLIENT = 1038;

	/**
	 * reload服务器成功
	 * serial								int
	 */
	public static final short ADMIN_RELOAD_SERVER = 1039;
	
	/**
	 * 取账号密保信息
	 * serial								int
	 * accountId							int
	 */
	public static final short ADMIN_ACCOUNTBINDING_STATUS_CLIENT = 1040;
	
	/**
	 * 账号密保信息
	 * serial								int
	 * 电话									string
	 * 身份证								string
	 * 问题									string
	 * 邮箱									string
	 */
	public static final short ADMIN_ACCOUNTBINDING_STATUS_SERVER = 1041;
	
	/**
	 * 添加bbs条目
	 * serial								int
	 * order								int (顺序 数字越大排列越靠前)
	 * title								string
	 * message								string
	 */
	public static final short ADMIN_BBS_ADD_CLIENT = 1042;
	
	/**
	 * 添加bbs条目成功
	 * serial								int
	 */
	public static final short ADMIN_BBS_ADD_SERVER = 1043;
	
	/**
	 * 删除bbs条目
	 * serial								int
	 * titile								String
	 */
	public static final short ADMIN_BBS_DELETE_CLIENT = 1044;
	
	/**
	 * 删除bbs条目成功
	 * serial								int
	 */
	public static final short ADMIN_BBS_DELETE_SERVER = 1045;
	
	/**
	 * 获取bbs列表
	 * serial
	 */
	public static final short ADMIN_BBS_LIST_CLIENT = 1046;
	
	/**
	 * bbs列表
	 * serial								int
	 * 数量									short
	 * 循环n此
	 * 	id									int
	 * 	order								int
	 * 	title								string
	 * 	content								string
	 * 	time								string
	 */
	public static final short ADMIN_BBS_LIST_SERVER = 1047;
	
	/**
	 * 发送信件
	 * serial									int
	 * playerId									int
	 * title									string
	 * content									string
	 * 装备描述									string(特有格式的装备描述，直接从log日志从获取)
	 */
	public static final short ADMIN_SENDMAIL2_CLIENT = 1048;
	
	/**
	 * 发送信件成功
	 * serial									int
	 * name										string(装备名字)
	 */
	public static final short ADMIN_SENDMAIL2_SERVER = 1049;
	
	/**
	 * 发送信件
	 * serial									int
	 * playerId									int
	 * title									string
	 * content									string
	 */
	public static final short ADMIN_SENDMESSMAIL_CLIENT = 1050;

	/**
	 * 添加元宝。
	 * serial								int
	 * id									int 帐号ID
	 * amount								int 数量，单位是分(0.1元宝)
	 */
	public static final short ADMIN_ADD_IMONEY_CLIENT = 1100;
	
	/**
	 * 添加元宝成功。
	 * serial								int
	 * amount								int 余额，单位是分(0.1元宝)
	 */
	public static final short ADMIN_ADD_IMONEY_SERVER = 1101;
	
	/**
	 * 列出现在正在进行的活动。
	 * serial 								int
	 */
	public static final short ADMIN_LIST_ACTIVITY_CLIENT = 1102;
	
	/**
	 * 返回活动列表。
	 * serial								int
	 * count								short
	 * 		id								int
	 * 		name							String
	 * 		title							String
	 * 		enabled							boolean
	 * 		active							boolean
	 */
	public static final short ADMIN_LIST_ACTIVITY_SERVER = 1103;
	
	/**
	 * 取得活动详情。
	 * serial								int
	 * id									int
	 */
	public static final short ADMIN_ACTIVITY_DETAIL_CLIENT = 1104;
	
	/**
	 * 返回活动详情。
	 * serial								int
	 * id									int
	 * name									String
	 * title								String
	 * description							String
	 * schedule								String
	 * implClass							String
	 * configData							String
	 * enabled								boolean
	 * visible								boolean
	 * active								boolean
	 */
	public static final short ADMIN_ACTIVITY_DETAIL_SERVER = 1105;
	
	/**
	 * 新建活动。
	 * serial								int
	 * name									String
	 * title								String
	 * description							String
	 * schedule								String
	 * implClass							String
	 * configData							String
	 * visible								boolean
	 */
	public static final short ADMIN_NEW_ACTIVITY_CLIENT = 1106;
	
	/**
	 * 创建活动成功。
	 * serial								int
	 * id									int
	 */
	public static final short ADMIN_NEW_ACTIVITY_SERVER = 1107;
	
	/**
	 * 激活/禁止活动。
	 * serial								int
	 * name									String
	 * enabled								boolean
	 */
	public static final short ADMIN_ENABLE_ACTIVITY_CLIENT = 1108;
	
	/**
	 * 激活/禁止活动成果。
	 * serial								int
	 * id									int
	 * enabled								boolean
	 */
	public static final short ADMIN_ENABLE_ACTIVITY_SERVER = 1109;
	
	/**
	 * 删除活动。
	 * serial								int
	 * name									String
	 */
	public static final short ADMIN_DELETE_ACTIVITY_CLIENT = 1110;
	
	/**
	 * 删除活动成果。
	 * serial								int
	 * id									int
	 */
	public static final short ADMIN_DELETE_ACTIVITY_SERVER = 1111;
	
	/**
	 * 删除物品。
	 * serial								int
	 * playerID								int
	 * itemID								int
	 * instanceID/count						int
	 */
	public static final short ADMIN_REMOVE_ITEM_CLIENT = 1112;
	
	/**
	 * 删除物品成功。
	 * serial								int
	 */
	public static final short ADMIN_REMOVE_ITEM_SERVER = 1113;
	
	/**
	 * 删除账号
	 * serial						int
	 * playerId						int
	 */
	public static final short ADMIN_DELETEPLAYER_CLIENT = 1114;
	
	/**
	 * 删除账号成功
	 * serial						int
	 */
	public static final short ADMIN_DELETEPLAYER_SERVER = 1115;
	
	/**
	 * 保存客服公告到xml文件
	 * serial                   int
	 * minLevel                 int                 等级下限
	 * maxLevel                 int                 等级上限
	 * textexplation            String            文字说明
	 * 循环N次
	 * size                     short             
	 *    	activeitem               String            活动名称
	 * 		detail                   String            更新具体内容
	 */
	public static final short ADMIN_SAVECLIENTBBS_CLIENT = 1116;
	
	/**
	 * 保存客服公告到xml文件成功
	 * serial                    int
	 */
	public static final short ADMIN_SAVECLIENTBBS_SERVER = 1117;
	
	/**
	 * 查看玩家信息
	 * serial					int
	 * name						String			角色名字
	 */
	public static final short ADMIN_PLAYERINFO_CLIENT = 1118;
	
	/**
	 * 查看玩家信息返回
	 * serial					int
	 * name						String			角色名字
	 * size 					int
	 * 		playerId			int				角色ID
	 * 		exit				byte			是否删除（0删除、1存在）
	 * 		sex					byte			性别
	 * 		class				byte			职业
	 * 		faction				byte			阵营
	 */
	public static final short ADMIN_PLAYERINFO_SERVER = 1119;
	
	/**
	 * 恢复玩家角色
	 * serial					int
	 * actorId					int				角色ID
	 */
	public static final short ADMIN_RENEWPLAYER_CLIENT = 1120;
	
	/**
	 * 恢复玩家角色返回
	 * serial					int
	 */
	public static final short ADMIN_RENEWPLAYER_SERVER = 1121;
	
	/**
	 * 添加新的活动指引项
	 * serial					int
	 * name						String				名称
	 * type						String				类型(1、活动 2、任务 3、怪物 4、商品)
	 * createTime				String				创建时间
	 * mapName					String				地图名称
	 * mapId					int					地图ID
	 * x						int					横坐标
	 * y						int					纵坐标
	 * rewardType				String				奖励类型(1、金钱 2、珍珠 3、声望 4、战功)
	 * minLevel					int					最小级别
	 * maxLevel					int					最大级别
	 * faction					String				适用阵营(1,2,3)
	 * instruction				String				说明
	 */
	public static final	short ADMIN_NEWACTLEADER_CLIENT = 1122;
	
	/**
	 * 添加新的活动指引项结果返回
	 * serial					int
	 */
	public static final short ADMIN_NEWACTLEADER_SERVER = 1123;
	
	/**
	 * 删除活动指引项
	 * serial					int
	 * name						String				名称
	 */
	public static final short ADMIN_DELETEACTLEADER_CLIENT = 1124;
	
	/**
	 * 删除活动指引项结果返回
	 * serial					int
	 */
	public static final short ADMIN_DELETEACTLEADER_SERVER = 1125;
	
	/**
	 * 活动指引项列表
	 * serial					int
	 */
	public static final short ADMIN_ACTLEADERLIST_CLIENT = 1126;
	
	/**
	 * 活动指引项列表返回
	 * serial					int
	 * num						int				数量
	 * 		name				String			活动名称
	 * 		type				String			类型(1、活动 2、任务 3、怪物 4、商品)
	 * 		createTime			String			创建时间
	 * 		mapName				String			地图名称
	 * 		mapId				int				地图ID
	 * 		x					int				横坐标
	 * 		y					int				纵坐标
	 * 		rewardType			String			奖励类型(1、金钱 2、珍珠 3、声望 4、战功)
	 * 		minLevel			int				最小级别
	 * 		maxLevel			int				最大级别
	 * 		faction				String			适用阵营(1,2,3)
	 * 		instruction			String			说明
	 */
	public static final short ADMIN_ACTLEADERLIST_SERVER = 1127;
	
	/**
	 * 按日期查询GM请求列表
	 * serial									int
	 * 每页条数									short
	 * 页数										short
	 * 日期										String(格式：2009-09-10)
	 */
	public static final short ADMIN_GMREQUEST_LIST_BYDATE_CLIENT = 1128;
	
	/**
	 * 按日期查询GM请求列表返回
	 * serial									int
	 * 每页条数									short
	 * 页数										short
	 * 信件的总数								int
	 * 本页实际条数								short
	 * 循环N次
	 * 	请求Id						int
	 *  请求类型						byte(暂时都为0，以后分各种问题组)
	 * 	请求玩家Id					int
	 * 	请求玩家名					string
	 * 	请求内容						string
	 * 	请求状态						byte(0 未解决 1 解决)
	 * 	解决方案						string
	 * 	提交时间						long
	 *  玩家机型						string
	 *  玩家mapId					short
	 *  玩家x坐标					short
	 *  玩家y坐标					short
	 */
	public static final short ADMIN_GMREQUEST_LIST_BYDATE_SERVER = 1129;
	
	/**
	 * 发送消息成功
	 * 	serial						int	
	 */
	public static final short ADMIN_CHAT_SERVER = 1130;
	
	/**
	 * 批量标注GM请求已被处理
	 * serial									int
	 * 请求Ids									int[] 
	 */
	public static final short ADMIN_MULTIGMREQUEST_SOLVE_CLIENT = 1131;
	
	/**
	 * 批量标注GM请求已被处理返回
	 * serial									int
	 * 成功标注的请求ids							int[]
	 */
	public static final short ADMIN_MULTIGMREQUEST_SOLVE_SERVER = 1132;
	
	/**
	 * GM工具控制校验加速参数
	 *		TIME_ERROR_SCORE_1 = 0;				 int	
	 *		TIME_ERROR_SCORE_2 = 1;				 int
	 *		TIME_ERROR_SCORE_3 = 2;				 int
	 *		MAX_MOVE_DISTANCE =  3;				 int
	 *		EXCEED_DISTANCE_SCORE = 4;			 int
	 *		POSITION_ERROR_SCORE1 = 5;			 int
	 *		POSITION_ERROR_SCORE2 = 6;			 int
	 *		TOTAL_CHEAT_ERROR_SCORE = 7;		 int	
	 *		TOO_MUCH_MOVE_ERROR_SCORE = 8;		 int
	 *		MOVE_CHEAT_VALVE1 = 9;		         double		
	 *		MOVE_CHEAT_VALVE2 = 10;				 double
	 *		TOTAL_CHEAT_VALVE = 11;				 int	
	 *		TOO_MUCH_MOVE_VALVE = 12;	         int
	 * 如上 一共有13个参数可配置 命令字符串格式为   参数序号,更新值 (更新值注意类型)  
	 * serial                                   int
	 * xuhao                                    byte   
	 * value                                    String
	 */
	public static final short ADMIN_CHANGE_VIOLATIONPARAM_CLIENT = 1133;
	
	/**
	 * GM工具控制校验加速参数 成功
	 * serial                                   int
	 */
	public static final short ADMIN_CHANGE_VIOLATIONPARAM_SERVER = 1134;
	
	/**
	 * 请求校验加速参数列表
	 * serial                                   int
	 */
	public static final short ADMIN_SHOW_VIOLATIONPARAM_CLIENT = 1135;
	
	/**
	 * 请求校验加速参数列表返回
	 * serial   							    int
	 * 		(循环读13次)
	 * 		paramName                           String    参数名
	 * 		value								String    参数值 
	 * 		paramSerial 						byte      序号                       
	 */
	public static final short ADMIN_SHOW_VIOLATIONPARAM_SERVER = 1136;
	
	/**
	 * 修改玩家VIP等级
	 * serial                                   int
	 * accountId                                int'
	 * playerId                                 int
	 * vipLevel                                 int(修改目标等级)
	 */
	public static final short ADMIN_CHANGE_VIPLEVEL_CLIENT = 1137;
	
	/**
	 * 修改玩家VIP等级返回
	 * serial   							    int                      
	 */
	public static final short ADMIN_CHANGE_VIPLEVEL_SERVER = 1138;
	
	/**
	 * 对java类进行hotswape替换。被替换的类文件放在hotfix目录下。
	 * serial						int
	 * 类全名(com.pip.xxxx)			string
	 */
	public static final short ADMIN_HOTSWAP_CLIENT = 1139;
	
	/**
	 * hotswap替换成功
	 * serial						int
	 */
	public static final short ADMIN_HOTSWAP_SERVER = 1140;
	
	/**
	 * 修改活动参数。
	 * serial					int
	 * name						String
	 * title					String
	 * description				String
	 * schedule					String
	 */
	public static final short ADMIN_MODIFY_ACTIVITY_CLIENT = 1141;

	/**
	 * 修改活动参数成功。
	 * serial								int
	 * id									int
	 */
	public static final short ADMIN_MODIFY_ACTIVITY_SERVER = 1142;
	
	
	/*
	 * Flash合作版本特有协议。
	 */
	
	/**
	 * 刷新客户端登录会话密码。因为会话密码15分钟失效，所以每10分钟服务器会刷新所有已成功连接的用户的会话密码，
	 * 以保证用户断线后可以重连。
	 * name									string
	 * passcode								string
	 */
	public static final short REFRESH_PASSCODE_SERVER = 2001;
	
	/**
	 * 当客户端正在排队时，刷新当前排队数。
	 * seq				int			排队序号，1表示第一个
	 * estTime			int			预计排队时间(秒),-1表示不可预估
	 */
	public static final short LOGIN_WAIT_SERVER = 2002;
	
	/**
	 * 拆分物品
	 * 包格Id						byte
	 * 物品Id						int
	 * 拆分数量						byte
	 */
	public static final short SPLITITEM_CLIENT = 2003;

	/**
	 * 获取任意场景的NPC列表
	 * serial							int
	 * 地区ID							int
	 */
	public static final short GLOBAL_NPC_LIST_CLIENT = 2004;
	
	/**
	 * 返回任意场景NPC列表
	 * serial							int
	 * 地区ID							int
	 * 数量								short
	 * 循环n次
	 * 	id								int
	 *  instanceID						int
	 * 	名字								String
	 * 	描述								String
	 *  场景ID							int
	 * 	x坐标(码)						short
	 *	y坐标(码)						short
	 */
	public static final short GLOBAL_NPC_LIST_SERVER = 2005;
	
	/**
	 * npc功能描述
	 * serial							int
	 * NPC id							int
	 * instanceId						int
	 */
	public static final short GLOBAL_NPC_DESC_CLIENT = 2006;
	
	/**
	 * npc功能描述
	 * serial							int
	 * NPC id							int
	 * instanceId						int
	 * 描述								string
	 */
	public static final short GLOBAL_NPC_DESC_SERVER = 2007;
	/**
	 * 交换仓库包格
	 * serial								int
	 * sourceGridId							short
	 * targetGridId							short
	 */
	public static final short DEPOT_EXCHANGE_CLIENT = 2008;
	
	/**
	 * 交换仓库包格成功
	 * serial								int
	 * 包格数量					byte
	 * 		包格信息					GRID
	 */
	public static final short DEPOT_EXCHANGE_SERVER = 2009;

	/**
	 * 拆分仓库物品
	 * serial						int
	 * 包格Id						byte
	 * 物品Id						int
	 * 拆分数量						byte
	 */
	public static final short DEPOT_SPLITITEM_CLIENT = 2010;
	
	/**
	 * 拆分仓库物品成功
	 * serial						int
	 * 包格数量					byte
	 * 		包格信息					GRID
	 */
	public static final short DEPOT_SPLITITEM_SERVER = 2011;
	
	/**
	 * 扩展背包
	 * serial						int
	 */
	public static final short EXTEND_BAG_CLIENT = 2012;
	
	/**
	 * 扩展背包成功
	 * serial						int
	 */
	public static final short EXTEND_BAG_SERVER = 2013;
	
	/**
	 * 取得扩展下一个背包的价格
	 * serial						int
	 */
	public static final short EXTEND_BAG_PRICE_CLIENT = 2014;
	
	/**
	 * 返回扩展下一个背包的价格
	 * serial						int
	 * imoney						int		所需元宝数量（单位是1/10元宝）
	 */
	public static final short EXTEND_BAG_PRICE_SERVER = 2015;
	
	/**
	 * 扩展仓库
	 * serial						int
	 */
	public static final short EXTEND_DEPOT_CLIENT = 2016;
	
	/**
	 * 扩展仓库成功
	 * serial						int
	 * newcount						int     新仓库容量
	 * 新增包格数量					byte
	 * 		包格信息					GRID
	 */
	public static final short EXTEND_DEPOT_SERVER = 2017;
	
	/**
	 * 取得扩展下一个仓库包格的价格
	 * serial						int
	 */
	public static final short EXTEND_DEPOT_PRICE_CLIENT = 2018;
	
	/**
	 * 返回扩展下一个仓库包格的价格
	 * serial						int
	 * imoney						int		所需元宝数量（单位是1/10元宝）
	 */
	public static final short EXTEND_DEPOT_PRICE_SERVER = 2019;
	
	/**
	 * 获取用户配置项信息。
	 * serial						int
	 * name							String	配置项名称
	 */
	public static final short GET_CONFIG_CLIENT = 2020;
	
	/**
	 * 返回用户配置项信息。
	 * serial						int
	 * name							String	配置项名称
	 * value						String	配置项值
	 */
	public static final short GET_CONFIG_SERVER = 2021;
	
	/**
	 * 保存用户配置项。
	 * serial						int
	 * name							String	配置项名称
	 * value						String	配置项值
	 */
	public static final short SET_CONFIG_CLIENT = 2022;
	
	/**
	 * 保存用户配置项返回。
	 * serial						int
	 * name							String	配置项名称
	 */
	public static final short SET_CONFIG_SERVER = 2023;
	
	/**
	 * 广播玩家离开场景通知。
	 * id							int		玩家ID
	 * exitId						int		出口ID
	 */
	public static final short PLAYER_EXIT_SERVER = 2024;
	
	/**
	 * 查询级别排行榜
	 * serial
	 * faction		byte	阵营，0表示取全部
	 */
	public static final short TOPLIST_LEVEL_CLIENT = 2025;
	
	/**
	 * 级别排行榜
	 * serial						int
	 * 数量							short
	 * 循环n次
	 *   名字						string
	 *   阵营						byte
	 *   等级						short
	 */
	public static final short TOPLIST_LEVEL_SERVER = 2026;
	
	/**
	 * 回复结盟请求
	 * serial				int
	 * sourceId				int				(邀请者playerid)
	 * associationId		int				血盟ID
	 * answer				byte			(0拒绝、1同意)
	 */
	public static final short ASSOCIATION_ANSWER_CLIENT = 2027;
	
	/**
	 * 结盟成员列表
	 * serial				int
	 */
	public static final short ASSOCIATION_LIST_CLIENT = 2028;
	
	/**
	 * 结盟成员列表返回
	 * serial					int
	 * associationName			String			血盟名称
	 * num						byte			成员数量
	 * 		playerId			int				成员ID
	 * 		name				String			成员名字
	 * 		state				byte			成员状态(0等待状态、1加入状态)
	 * 		duty				byte			成员职位(0普通成员、1盟主)
	 * 		online				byte			(0、不在线 1、在线)
	 */
	public static final short ASSOCIATION_LIST_SERVER = 2029;
	
	/**
	 * 强化装备请求
	 * serial         int
	 */
	public static final short ENHANCE_EQUIP_REQUEST_CLIENT = 2030;
	
	/**
	 * 强化装备请求返回
	 * serial         int
	 * count          int           当天第N次强化
	 * maxCount       int           最多强化次数
	 * money          int           每次强化需要的金钱
	 */
	public static final short ENHANCE_EQUIP_REQUEST_SERVER = 2031;
	
	/**
	 * 强化装备
	 * serial         int
	 * itemId         int            装备Id
	 * instanceId     int            装备instanceId
	 */
	public static final short ENHANCE_EQUIP_CLIENT = 2032;
	
	/**
	 * 强化装备返回
	 * serial         int
	 * size           int
	 *   num          int           属性值
	 *   showAtt      String        显示属性值
	 *   result       byte          强化结果(0为持平，1为下降，2为上升)
	 */
	public static final short ENHANCE_EQUIP_SERVER = 2033;
	
	/**
	 * 展示或隐藏称号
	 * serial        int
	 * titleid       int            称号id(-1为隐藏，其它为展示)
	 */
	public static final short TITLE_SHOW_CLIENT = 2034;
	
	/**
	 * 展示或隐藏称号返回
	 * serial        int
	 * titleId       int            称号Id
	 */
	public static final short TITLE_SHOW_SERVER = 2035;
	
	/**
	 * 星辉BUFF描述
	 * serial				int
	 * type					byte			0为玩家星辉、1为坐骑星辉
	 */
	public static final short BUFF_DESC_BYID_CLIENT = 2036;
	
	/**
	 * 星辉BUFF描述返回
	 * serial						int
	 * num(循环N次)					byte
	 * 		name						string			buff名称
	 * 		desc						string			buff描述
	 */
	public static final short BUFF_DESC_BYID_SERVER = 2037;
	
	/**
	 * 服务器下发反外挂加密数据
	 * flag							byte			(0为A,1为B)
	 * value						int
	 */
	public static final short ANTI_PLUG_SERVER = 2038;
	
	/**
	 * 客户端发送反外挂加密数据
	 * uiModel						String				uiModel
	 * value						int				D
	 */
	public static final short ANTI_PLUG_CLIENT = 2039;
	
	/**
	 * 充值金额兑换元宝数请求
	 * serial                       int
	 */
	public static final short CHARGE_INFO_CLIENT = 2040;
	
	/**
	 * 充值金额兑换元宝数返回
	 * serial						int				
	 * num						    String				兑换数据
	 */
	public static final short CHARGE_INFO_SERVER = 2041;
	
	/**
	 * 团购报名
	 * serial						int
	 */
	public static final short CLEARANCESALE_SIGN_CLIENT = 2042;
	
	/**
	 * 团购报名返回
	 * serial						int
	 */
	public static final short CLEARANCESALE_SIGN_SERVER = 2043;
	
	/**
	 * 团购列表
	 * serial						int
	 */
	public static final short CLEARSALE_LIST_CLIENT = 2044;
	
	/**
	 * 团购列表返回
	 * serial						int
	 * condition					String				参加条件
	 * icon							int					图标
	 * itemname						商品名称				String
	 * desc							商品描述				String
	 * costprice					short				原价
	 * price						short				现价
	 * minplayers					short				最低团购人数
	 * maxplayers					short				高高团购人数
	 * leavingTime					String				剩余时间
	 * nextItem						String				下期商品
	 * 循环N次						short				当前团购人数
	 * 		id		int			玩家角色ID
	 * 		name	String		玩家名称		
	 *		online	byte		是否在线，0 - 不在线、1 - 在线
	 *		level	short		级别（只对在线用户有效）
	 *		sex		byte		性别（只对在线用户有效）
	 *		clazz	byte		职业（只对在线用户有效）		
	 */
	public static final short CLEARSALE_LIST_SERVER = 2045;
	
	/**
	 * 领取奖励
	 * serial						int
	 * type                         int                 奖励类型
	 * subtype                      int                 每个奖励的子类型
	 */
	public static final short FETCH_GIFT_CLIENT = 2046;
	
	/**
	 * 领取奖励返回
	 * serial						int
	 */
	public static final short FETCH_GIFT_SERVER = 2047;
	
	/**
	 * 修改血盟名称
	 * serial						int
	 * newName						String			新名称
	 */
	public static final short ASSOCIATION_RENAME_CLIENT = 2048;
	
	/**
	 * 修改血盟名称返回
	 * serial						int
	 */
	public static final short ASSOCIATION_RENAME_SERVER = 2049;
	
	/**
	 * 完成成就领取奖励
	 * serial						int
	 * type                         int                 成就类型Id
	 * achieveId                    int                 成就子类型Id
	 */
	public static final short ACHIEVEMENT_GIFT_CLIENT = 2050;
	
	/**
	 * 领取奖励返回
	 * serial						int
	 */
	public static final short ACHIEVEMENT_GIFT_SERVER = 2051;
	
	/**
	 * 获取随从包信息
	 * serial						int
	 */
	public static final short ATTENDANT_BAG_CLIENT = 2052;
	
	/**
	 * 随从包信息
	 * serial						int
	 * data							ATTENDANTBAG
	 */
	public static final short ATTENDANT_BAG_SERVER = 2053;
	
	/**
	 * 跟随
	 * serial						int
	 * instanceId					int				随从instanceid
	 * change                       int(-1不换装,0换装)
	 */
	public static final short ATTENDANT_FOLLOW_CLIENT = 2054;
	
	/**
	 * 跟随返回
	 * serial						int
	 */
	public static final short ATTENDANT_FOLLOW_SERVER = 2055;
	
	/**
	 * 取消跟随
	 * serial						int
	 * instanceId					int				随从instanceid
	 */
	public static final short ATTENDANT_CANCELFOLLOW_CLIENT = 2056;
	
	/**
	 * 取消跟随返回
	 * serial						int
	 */
	public static final short ATTENDANT_CANCELFOLLOW_SERVER = 2057;
	
	/**
	 * 随从装备
	 * serial						int
	 * ItemId						int
	 * itemInstanceId				int
	 * attendantInstanceId			int
	 */
	public static final short ATTENDANT_EQUIP_CLIENT = 2058;
	
	/**
	 * 随从装备成功
	 * serial						int
	 */
	public static final short ATTENDANT_EQUIP_SERVER = 2059; 
	
	/**
	 * 随从卸下装备
	 * serial						int
	 * itemId						int
	 * itemInstanceId				int
	 * attendantInstanceId			int
	 */
	public static final short ATTENDANT_UNEQUIP_CLIENT = 2060;
	
	/**
	 * 随从卸下装备返回
	 * serial						int
	 */
	public static final short ATTENDANT_UNEQUIP_SERVER = 2061;
	
	/**
	 * 放生随从
	 * serial						int
	 * instanceId					int				随从instanceid
	 */
	public static final short ATTENDANT_DELETE_CLIENT = 2062;
	
	/**
	 * 放生随从返回
	 * serial						int
	 */
	public static final short ATTENDANT_DELETE_SERVER = 2063;
	
	/**
	 * 修改随从名称
	 * serial						int
	 * instanceIf					int				随从instanceid
	 * newName						String			随从新名称
	 */
	public static final short ATTENDANT_RENAME_CLIENT = 2064;
	
	/**
	 * 修改随从名称返回
	 * serial						int
	 */
	public static final short ATTENDANT_RENAME_SERVER = 2065;
	
	/**
	 * 点亮技能位
	 * serial						int
	 * instanceId					int				随从instanceid
	 * index						byte			技能位（0-5）
	 */
	public static final short ATTENDANT_LIGHTSKILL_CLIENT = 2066;
	
	/**
	 * 点亮技能位返回
	 * serial						int
	 */
	public static final short ATTENDANT_LIGHTSKILL_SERVER = 2067;
	
	/**
	 * 判断是否开启账号仓库
	 * serial						int
	 */
	public static final short ACCOUNTDEPOT_CHECK_CLIENT = 2068;
	
	/**
	 * 判断是否开启账号仓库返回
	 * serial						int
	 * 包格数量				byte 0未申请 1已申请
	 * 循环n次
	 * 包格信息				GRID
	 */
	public static final short ACCOUNTDEPOT_CHECK_SERVER = 2069;
	
	/**
	 * 仓库申请
	 * serial				int
	 */
	public static final short ACCOUNTDEPOT_REQUEST_CLIENT = 2070;
	
	/**
	 * 仓库申请结果返回
	 * serial						int
	 * 包格数量						byte
	 * 循环n次
	 *		包格信息					GRID
	 */
	public static final short ACCOUNTDEPOT_REQUEST_SERVER = 2071;
	
	/**
	 * 从账号仓库中取出物品放入背包
	 * serial				int
	 * gridid				int				包格ID
	 * itemid				int				物品ID
	 * instanceid			int				物品实例ID
	 * count				int				取出物品数量
	 */
	public static final short ACCOUNTDEPOT_GETFROMDEPOT_CIENT = 2072;
	
	/**
	 * 从背包中取出物品放入账号仓库
	 * serial				int
	 * gridid				int				包格ID
	 * itemid				int				物品ID
	 * instanceid			int				物品实例ID
	 * count				int				取出物品数量
	 */
	public static final short ACCOUNTDEPOT_GETFROMBAG_CLIENT = 2073; 
	
	/**
	 * 改变的账号仓库包格信息
	 * serial					int
	 * 包格数量					byte
	 * 		包格信息					GRID
	 */
	public static final short ACCOUNTDEPOT_GETFROMBAG_SERVER = 2074;
	
	/**
	 * 账号仓库整理
	 * serial				int
	 */
	public static final short ACCOUNTDEPOT_ARRANGE_CLIENT = 2075;
	
	/**
	 * 账号整理仓库完成
	 * serial							int
	 * 循环n次							byte
	 * 	包格信息							GRID
	 */
	public static final short ACCOUNTDEPOT_ARRANGE_SERVER = 2076;
	
	/**
	 * 激活随从忠诚度
	 * serial							int
	 * instanceId						int				随从instanceid
	 */
	public static final short ATTENDANT_ADDLOYAL_CLIENT = 2077;
	
	/**
	 * 激活忠诚度返回
	 * serial							int
	 */
	public static final short ATTENDANT_ADDLOYAL_SERVER = 2078;
	
	/**
	 * 替换随从技能
	 * serial							int
	 * skillGroupId						int
	 * skillLevel						int
	 * instanceId						int				随从instanceid
	 * index							byte			(0--5)
	 * itemid							int				物品ID
	 * type								byte			(0为普通学习，1为一键学习)
	 */
	public static final short ATTENDANT_ADDSKILL_CLIENT = 2079;
	
	/**
	 * 替换随从技能返回
	 * serial							int
	 */
	public static final short ATTENDANT_ADDSKILL_SERVER = 2080;
	
	/**
	 * 
	 * serial							int
	 * instanceId						int				NPC instanceid
	 * text								String 			冒泡内容
	 * time								int 			持续毫秒数
	 */
	public static final short NPC_BUBBLE_SERVER = 2081;
	
	/**
     * 请求合成宝石需求。
     * serial               int                 序列号
     * jewelId           	int                	宝石等级
     */
    public static final short MERGE_JEWEL_REQUEST_CLIENT = 2082;
    
    /**
     * 请求合成宝石需求返回。
     * serial               int                 序列号
     * currentNum			int					当前宝石数量
     * lowItem				int					低级合成符数量
     * highItem				int					高级合成符数量
     * num					byte				循环N次
     * 		type					byte				类型（0金钱1物品2i币）
     * 		value					String				金钱数量或者物品名称数量或者i币
     */
    public static final short MERGE_JEWEL_REQUEST_SERVER = 2083;
    
    /**
     * 自动宝石合成
     * serial				int
     * jewelid				int				宝石ID
     */
    public static final short AUTO_MERGE_JEWEL_CLIENT = 2084;
    
    /**
     * 自动合成宝石返回
     * serial               int                 序列号
     * jewelid              int                 如果成功，返回合成后的宝石物品ID
     * jewelicon            byte                如果成功，返回合成后的宝石物品图标
     * jewelname            String              如果成功，返回合成后的宝石物品名称
     */
    public static final short AUTO_MERGER_JEWEL_SERVER = 2085;
    
    /**
     * 越南充值
     * serial				int		
     * cardnum				String
     * password				String
     */
    public static final short VIRTNAM_CHARGE_CLIENT = 2086;
    
    /**
     * 越南充值返回
     * serial				int
     */
    public static final short VIETNAM_CHARGE_SERVER = 2087;
    
	/**
	 * 修改关系人锁定状态
	 * serial	int
	 * id		int			玩家ID
	 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
	 * state    byte		类型：0 - 解除锁定   1 - 锁定
	 */
    public static final short CHANGE_FRIEND_LOCKSTATE_CLIENT = 2088;
    
    /**
     * 更改关系人锁定状态返回
     * serial				int
     * id                   int
     * isLock               byte  类型:0 未锁定    1：锁定
     */
    public static final short CHANGE_FRIEND_LOCKSTATE_SERVER = 2089;
    
	/**
	 * 请求一键摘除宝石所需资源。 
	 * serial 				int			 序列号
	 * itemid 				int			 要镶嵌的装备的物品id
	 * instanceid 			int 		 要镶嵌装备的instanceid
	 */
	public static final short REMOVE_ALLJEWEL_RESOURCE_CLIENT = 2090;
	
	/**
	 * 请求一键摘除宝石所需资源。 
	 * serial 				int			 序列号
	 * num					byte 		 符合条件摘除符的种类(循环N次)
	 * 		signLevel		byte		 摘除符等级
	 * 		signNum			byte         摘除符数量
	 * money				int			 需要金钱
	 * Imoney				int			 需要的i币
     * num					byte		 需要的摘除符种类循环N次
     * 		signLevel		byte		 摘除符等级
     * 		signNum			byte		 摘除符数量
	 */
	public static final short REMOVE_ALLJEWEL_RESOURCE_SERVER = 2091;
	
	/**
	 * 一键摘除宝石。 
	 * serial 				int 		序列号 
	 * itemid 				int 		要摘除的装备的物品id
	 * instanceid 			int 		要摘除的装备的instanceid
	 */
	public static final short DECORATE_REMOVE_ALLJEWEL_CLIENT = 2092;
	
	/**
	 * 一键摘除宝石。 
	 * serial 				int 		序列号 
	 * itemid 				int 		要摘除的装备的物品id
	 * instanceid 			int 		要摘除的装备的instanceid
	 * jewelinfo            byte[]      镶嵌后装备宝石信息（参见后面DECORATION数据结构说明）
	 */
	public static final short DECORATE_REMOVE_ALLJEWEL_SERVER = 2093;
	
	/**
	 * 坐骑技能升级
	 * serial						int
	 * horseInstanceId				int
	 * skillId						int
	 */
	public static final short HORSE_UP_SKILL_CLIENT = 2094;
	
	/**
	 * 坐骑技能升级成功
	 * serial						int
	 */
	public static final short HORSE_UP_SKILL_SERVER = 2095;
	
	/**
	 * 福星赐福包客户端使用 
	 * serial 				int 		序列号 
	 * useIB 				byte 		0: 不用   1：使用
	 * int 					itemId		物品id
	 */
	public static final short FUXING_BAG_CLIENT = 2096;
	
	/**
	 * 福星赐福宝客户端返回
	 * serial 				int 		序列号
	 */
	public static final short FUXING_BAG_SERVER = 2097;
	
	/**
	 * 客户端触发某动作
	 * type					int				类型 (参看data\action.xml)
	 * subType				byte			子类型 (参看data\action.xml)
	 */
	public static final short PLAYER_ACTION_CLIENT = 2098;
	
	/**
	 * 设置寻路状态
	 * type                 byte			1开始寻路0终止寻路
	 */
	public static final short PLAYER_SET_FIND_PATH_CLIENT = 2099;
	
	/**
	 * 激活坐骑
	 * serial				int
	 * instanceId			int				
	 */
	public static final short HORSE_ACTIVE_CLIENT = 2100;
	
	/**
	 * 激活坐骑返回
	 * serial				int
	 */
	public static final short HORSE_ACTIVE_SERVER = 2101;
	
	
	/** 拍卖行撤单请求 
	 * serial 		int 
	 * auctionID 	int 	拍卖物品
	 */
	public static final short AUCTION_DELETE_CLIENT = 2102;
	
	/** 拍卖行撤单成功
	 * serial		int
	 * auctionID 	int 	撤单物品
	 */
    public static final short AUCTION_DELETE_SERVER = 2103;
    
    /**
     * 越南Telco_充值
     * serial				int
     * cardCode				String				卡号
     * type					byte				方式（0为MOBI、1为VINA）
     */
    public static final short VIETNAM_TELCO_MOBIPHONE_CHARGE_CLIENT = 2104;
    
    /**
     * 越南Telco_充值返回
     * serial				int
     */
    public static final short VIETNAM_TELCO_MOBIPHONE_CHARGE_SERVER = 2105;
	
    /**
     * 索回付费邮件
     * serial							int
     * mailId							int
     */
    public static final short MAIL_RECOVER_CLIENT = 2107;
    
    /**
     * 索回付费邮件返回
     * serial							int
     * mailId							int
     */
    public static final short MAIL_RECOVER_SERVER = 2108;
    
    /**
     * 一键提取邮件附件
     * serial							int
     */
    public static final short MAIL_GETALLATTACH_CLIENT = 2109;
    
    /**
     * 一键提取邮件附件返回
     * serial							int
     */
    public static final short MAIL_GETALLATTACH_SERVER = 2110;
    
	/**
	 * 随从技能栏学习随从技能
	 * serial							int
	 * instanceId						int				随从instanceid
	 * index							byte			(0--5)
	 * itemid							int				物品ID
	 */
	public static final short ATTENDANT_LEARNSKILL_CLIENT = 2111;
	
	/**
	 *  随从技能栏学习随从技能
	 * serial							int
	 */
	public static final short ATTENDANT_LEARNSKILL_SERVER = 2112;
    
    /**
     * 解散军团
     * serial							int
     * type     					    int   0:不使用完美重置符      1：使用完美重置符
     */
	public static final short TONG_REMOVE_CLIENT = 2113;
	
	/**
	 * 解散军团返回
	 * serial							int
	 */
	public static final short TONG_REMOVE_SERVER = 2114;
    
    /**
     * 查看全七BUFF描述（台湾专用）
     * serial							int
     */
	public static final short START_7_BUFF_DESC_CLIENT = 2115;
	
	/**
	 * 查看全七BUFF描述返回（台湾专用）
	 * name							string			buff名称
	 * desc							string			buff描述
	 */
	public static final short START_7_BUFF_DESC_SERVER = 2116;
	
	/**
	 * 请求每日福利列表
	 * serial				int
	 * playerId				角色id
	 */
	public static final short WELFARE_LIST_CLIENT = 2117;
	
	/**
	 * 每日福利返回
	 * seral						int
	 * size         				short   福利分类数
	 * 循环size次
	 * 		welfareTypeName     	String  福利分类名称
	 * 		welfareSize         	short   包含具体福利数 
	 * 		finishNum 				short   本分类完成的福利数
	 * 			循环welfareSize次   
	 * 				welfareId		short   福利id
	 * 				welfareName     String  福利名称
	 * 				welfareDec      String  福利描述
	 * 				welfareState    byte    福利状态（0:未完成  1：完成  2：已领取奖励）
	 * 				rewardSize      short    奖励的种类数
	 * 					循环rewardSize次
	 * 						itemId  int     奖励物品id
	 * 						count   int     物品的数量
	 *                      iconId  int     图标id
	 */
	public static final short WELFARE_LIST_SERVER = 2118;
	
	/**
	 * 请求福利奖励
	 * serial						int
	 * welfareId 					int   福利id
	 */
	public static final short WELFARE_REWARD_CLIENT = 2119;
	
	/**
	 * 福利奖励发放成功返回
	 * serial						int
	 */
	public static final short WELFARE_REWARD_SERVER = 2120;
	
	/**
	 * 镶嵌宝石升级
	 * serial               int                 序列号
	 * itemid               int                 镶嵌的装备的物品id
	 * instanceid           int                 镶嵌的装备的instanceid
	 * hole                 byte                镶孔索引(0表示第一个)
	 * type					byte				请求类型 0-普通， 1-请求升级需要扣除的元宝价格
	 */
	public static final short DECORATE_UPGRADE_JEWEL_CLIENT = 2121;
	
	/**
	 * 镶嵌宝石升级结果返回
	 * serial				int
	 * type					byte
	 * type == 0				
	 *	jewelinfo           byte[]              镶嵌后装备宝石信息（参见后面DECORATION数据结构说明）
	 * type == 1
	 *	price				String				 需要扣除的价格
	 */
	public static final short DECORATE_UPGRADE_JEWEL_SERVER = 2122;
	
	/**
	 * 比武大会报名
	 * serial					int
	 */
	public static final short DUELMETTING_SIGNUP_CLIENT = 2123;
	
	/**
	 * 比武大会报名返回
	 * serial					int
	 */
	public static final short DUELMETTING_SIGNUP_SERVER = 2124;
	
	/**
	 * 查看比武大会对手信息
	 * serial                   int
	 */
	public static final short DUELMETTING_PLAYERINFO_ClIENT = 2125;
	
	/**
	 * 查看比武大会对手信息返回
	 * serial
	 * playerId                  int     
	 */
	public static final short DUELMETTING_PLAYERINFO_SERVER = 2126;	
	
	/**
	 * 获取其他玩家当前的技能列表
	 * serial                   int
	 * targetId                 int             目标玩家id
	 */
	public static final short OTHER_PLAYER_SKILL_LIST_CLIENT = 2130;
	/**
	 * 其他玩家当前的技能列表
	 * serial                       int
	 * 技能数量						byte
	 * 循环n次
	 * 	技能							SKILL
	 * 
	 */
	public static final short OTHER_PLAYER_SKILL_LIST_SERVER = 2131;
	
	/**
	 * 玩家锁定经验
	 * serial                   int
	 * type                     byte             0:解除锁定    1:锁定
	 */
	public static final short PLAYER_LOCK_EXP_CLIENT = 2132;
	/**
	 * 玩家锁定经验返回
	 * serial                       int
	 */
	public static final short PLAYER_LOCK_EXP_SERVER = 2133;
	
    /**
	 * 卡片镶嵌（附魔）(已过期)
	 * serial						int
	 * cardId						int			    卡片ID
	 * itemid               		int             要镶嵌的装备的物品id
	 * instanceid          			int             要镶嵌的装备的instanceid
	 * hole                 		byte            镶孔索引(0表示第一个)
	 * method						byte			附魔方式0普通附魔、1闪卡附魔
	 */
	public static final short CARD_ADDTOEQU_CLIENT = 2134;
	
	/**
	 * 卡片镶嵌（附魔）返回(已过期)
	 * serial						int
	 * decenergy					short				消耗能量数
	 * DECORATION            		byte[]              镶嵌后装备宝石信息（参见后面DECORATION数据结构说明）
	 */
	public static final short CARD_ADDTOEQU_SERVER = 2135;
	
	/**
	 * 卡片槽激活(已过期)
	 * serial						int
	 * itemid               		int                 装备的物品id
     * instanceid           		int                 装备的instanceid
	 */
	public static final short CARD_ADDHOLE_CLIENT = 2136;
	
	/**
	 * 卡片槽激活返回(已过期)
	 * serial						int
	 */
	public static final short CARD_ADDHOLE_SERVER = 2137;
	
	/**
	 * 所有已收集卡片基本信息列表
	 * serial						int
	 * prorertyType					byte
	 * targetPlayerId               int   目标ID
	 */
	public static final short CARD_ALLLIST_CLIENT = 2138;
	
	/**
	 * 所有已收集卡片基本信息列表返回
	 * serial						int
	 * exp                          int             玩家卡片经验
	 * size							int
	 *		type					byte    		卡片类型0-普通卡片 1-技能卡片 
	 *		type==0
	 * 			cardId					int				卡片ID
	 * 			cardName				String			卡片名称
	 * 			cardDec                 String          卡片描述
	 *     		quality					byte			卡片品质
	 *     		res						String			卡片资源
	 *      	hasPicture			    byte			1有图卡片、0无图卡片
	 *      	// energy				short			// 卡片能量
	 *      	// maxenergy			short			// 卡片最大能量
	 *     	 	star					byte			卡片星级
	 *      	// buffDec              String          // 卡片BUFF描述
	 *      	hadEquip				byte			是否已装备
	 *      	level					byte			卡片级别
	 *      	exp						int				升级所需经验
	 *     	 	attDesc					String			属性增强描述
	 *      	nextLevelDesc           String          下一级属性增强描述
	 *      	totalExp				int				卡片累计经验
	 *		type==1
	 * 			cardId					int				卡片ID
	 * 			cardName				String			卡片名称
	 * 			cardDec                 String          卡片描述
	 *     		quality					byte			卡片品质
	 *     		res						String			卡片资源
	 *      	hasPicture			    byte			1有图卡片、0无图卡片
	 *     	 	star					byte			卡片星级
	 *      	hadEquip				byte			是否已装备
	 *      	level					byte			卡片级别
	 *      	upGradeNeedCount		int				升级所需此卡片数量
	 *     	 	attDesc					String			Buff属性增强描述
	 *      	nextLevelDesc           String          下一级Buff属性增强描述
	 *      	totalCount				int				此类卡片拥有数量
	 *      	preDesc					String			卡片3，6，9，12描述
	 *      	upGradeNeedExp			int				技能卡片升级所需经验
	 *  isHave					byte			是否已收藏0-未收藏  1=已收藏
	 */
	public static final short CARD_ALLLIST_SERVER = 2139;
	
	/**
	 * 为自己的婚礼申请伴郎/伴娘
	 * serial                   int
	 * type                     byte   0：伴郎    1:伴娘
	 * playerId                 int    被申请人id
	 */
	public static final short MARRAY_BANLANG_CLIENT = 2140; 
	
	/**
	 * 向被申请人发送请求
	 * serial                  int
	 * type					   byte     0：伴郎    1：伴娘
	 * beApplayId			   int      被申请人id
	 * palyerId				   int   	申请人id
	 * playerName			   String   申请人姓名
	 */
	public static final short MARRAY_BANLANG_SERVER = 2141; 
	
	/**
	 * 被申请人应答
	 * serial					int
	 * type						byte    0:伴郎     1:伴娘
	 * response					byte    0:拒绝     1:同意
	 * playerId					int     申请人id
	 */
	public static final short MARRAY_BANLANG_REQUEST_CLIENT = 2142; 
	
	/**
	 * 被申请人应答返回
	 * serial					int
	 * type						byte     0:伴郎     1:伴娘
	 * beApplayId               int      被申请人id
	 * beapplayName              String   被申请人name
	 * applayId                 int      申请人id
	 * response                 byte     0:同意    1：拒绝
	 */
	public static final short MARRAY_BANLANG_REQUEST_SERVER = 2143;

	/**
	 * 队友请求开始答题
	 * serial 						int
	 */
	public static final short WEDDING_QUESTION_CLIENT = 2144;
	
	/**
	 * 服务器下发邀请队友答题
	 * serial						int
	 * applyId                      int  		申请人id
	 * applyName                    String 		申请人name
	 */
	public static final short WEDDING_QUESTION_SERVER = 2145;
	
	/**
	 * 队友答复是否同意开始答题
	 * serial                       int
	 * response						byte 0:同意   1：拒绝
	 * applyId                      int    申请人id
	 */
	public static final short WEDDING_QUESTION_RESPONSE_CLIENT = 2146;
	
	/**
	 * 服务器下发邀请结果
	 * serial						int
	 * response						byte 0:同意   1：拒绝
	 */
	public static final short WEDDING_QUESTION_RESPONSE_SERVER = 2147;
	
	/**
	 * 具体问题
	 * serial						int
	 * questionId                   int     问题序号
	 * question                     String  问题
	 * 循环四次
	 * 			answerId            byte     答案id
	 * 			answer              String   具体答案                 
	 */
	public static final short WEDDING_QUESTION1_SERVER = 2148;
	
	/**
	 * 问题应答
	 * serial                       int 
	 * questionId					int    问题序号
	 * answer                       byte   对应答案id(0-3)
	 */
	public static final short WEDDING_QUESTION1_CLIENT = 2149;
	
	/**
	 * 答题过程中拒绝答题
	 * serial						int
	 */
	public static final short WEDDING_QUESTION_REFUSE_CLIENT = 2150;
	
	/**
	 * 答题过程中拒绝答题返回
	 * serial						int
	 */
	public static final short WEDDING_QUESTION_REFUSE_SERVER = 2151;
	
	/**
	 * 答题结束
	 * serial						int
	 */
	public static final short WEDDING_QUESTION_END_SERVER = 2152;
	
	/**
	 * 建立师徒关系
	 * serial						int
	 */
	public static final short PLAYER_APPRENTICE_CLIENT = 2153;
	
	/**
	 * 建立师徒关系返回
	 * serial						int
	 */
	public static final short PLAYER_APPRENTICE_SERVER = 2154;
	
	/**
	 * 解除师徒关系
	 * serial						int
	 * targetId                     int
	 */
	public static final short PLAYER_REMOVEAPPRENTICE_CLIENT = 2155;
	
	/**
	 * 解除师徒关系返回
	 * serial						int
	 */
	public static final short PLAYER_REMOVEAPPRENTICE_SERVER = 2156;
	
	/**
	 * 师徒列表
	 * serial						int
	 */
	public static final short PLAYER_APPRENTICELIST_CLIENT = 2157;
	
	/**
	 * 师徒列表返回
	 * serial						int
	 * size                         int 
	 *     playerId                      int
	 *     playerName                    String
	 *     online                        byte
	 *     level                         short
	 *     sex                           byte
	 *     clazz                         byte
	 *     faction                       byte
	 *     teacher                       byte(0 师傅，1 徒弟)
	 */
	public static final short PLAYER_APPRENTICELIST_SERVER = 2158;
	
	/**
	 * 邀请拜师
	 * serial							int
	 */
	public static final short APPRENTICE_INVIT_CLIENT = 2159;

	/**
	 * 邀请拜师
	 * serial                           int
	 * 邀请序号						    int
	 * 邀请人Id						    int
	 * 邀请人名字						string
	 * 邀请人等级						byte
	 * 邀请人职业						byte
	 * 邀请人性别                        byte
	 */
	public static final short APPRENTICE_INVIT_SERVER = 2160;
	
	/**
	 * 拒绝拜师请求
	 * 邀请序号						int
	 */
	public static final short APPRENTICE_INVIT_REJECT_CLIENT = 2161;
	
	/**
	 * 拒绝拜师请求返回
	 * 拒绝人名字					string
	 * 拒绝原因						string
	 */
	public static final short APPRENTICE_INVIT_REJECT_SERVER = 2162;
	
	/**
	 * 答应拜师请求
	 * 邀请序号						int
	 */
	public static final short APPRENTICE_INVIT_OK_CLIENT = 2163;
	
	/**
	 * 卡片充能
	 * serial						int
	 * cardId						int				卡片Id
	 * type                         byte           0:普通充能   1:万能卡充能
	 */
	public static final short CARD_ADDENERGY_CLIENT = 2164;
	
	/**
	 * 卡片充能返回
	 * serial						int
	 * energy						short
	 * maxEnergy					short
	 */
	public static final short CARD_ADDENERGY_SERVER = 2165;
	
	/**
	 * 越南VTC卡充值
	 * serial						int
	 * cardId						String				卡ID
	 * cardCode						String				卡号
	 */
	public static final short VIETNAM_VTC_CHARGE_CLIENT = 2166;
	
	/**
	 * 越南VTC卡充值返回
	 * serial						int
	 */
	public static final short VIETNAM_VTC_CHARGE_SERVER = 2167;
	
	/**
	 * 使用物品获得坐骑经验请求
	 * serial						int
	 * itemId						int				    物品Id
	 */
	public static final short ASK_TOGET_HORSEEXP_CLIENT = 2168;
	
	/**
	 * 使用物品获得坐骑经验请求返回
	 * serial							int
	 * itemId                           int
	 * msg                              String      获取前的提示
	 */
	public static final short ASK_TOGET_HORSEEXP_SERVER = 2169;
	
	/**
	 * 使用物品获得坐骑经验请求
	 * serial						int
	 * itemId						int				    物品Id
	 */
	public static final short USEWHOLE_TOGET_HORSEEXP_CLIENT = 2170;
	
	/**
	 * 使用物品获得坐骑经验请求返回
	 * serial						int
	 */
	public static final short USEWHOLE_TOGET_HORSEEXP_SERVER = 2171;
	/**
	 * 镶嵌：扩展装备的最大孔数。（需要扣除一个扩展孔物品，暂不开放）
     * serial               int                 序列号
     * itemid               int                 装备的物品id
     * instanceid           int                 装备的instanceid
	 */
	public static final short DECORATE_ADD_MAXHOLE_ALL_CLIENT = 2172;
    /**
     * 镶嵌：扩展装备的最大孔数返回。
     * serial               int                 序列号
     * itemid               int                 装备的物品id
     * instanceid           int                 装备的instanceid
     * holeadd              byte                新附加孔数
     * holecount            byte                总的孔数
     */
    public static final short DECORATE_ADD_MAXHOLE_ALL_SERVER = 2173;
   	    
    /**
     * 客户端引导动作下发
     * id					int			事件ID
	 * size	     			byte  		数量
	 * 		promptType      short      	引导提示类型
	 * 		promptParam     String   	提示参数
	 * 		actionType      short      	玩家操作类型
	 * 		eventDes        String   	事件描述
     */
    public static final short CLIENT_GUID_ACTION_SERVER = 2174;
    
	/**
	 * 请求卡片属性类型列表
	 * serial						int
	 * playerId						int
	 */
	public static final short CARD_PRORERTY_ALLLIST_CLIENT = 2175;
	
	/**
	 * 请求卡片属性类型列表返回
	 * serial						int
	 * typeNum						int
	 * 		typeId                       int               类型id
	 * 		typeName                     String            类型名字
	 *      size                         int               可用卡片数量
	 * exp                          int                    卡片经验
	 */
	public static final short CARD_PRORERTY_ALLLIST_SERVER = 2176;
	
	/**
	 * 客户端引导结果反馈到服务器	
	 * id					int			事件ID
	 * result	     		byte  		结果  0，未完成，1完成
	 */
	public static final short CLIENT_GUID_RESULT_CLIENT = 2177;
	
	/**
	 * cmcc短信购买
	 * serial					int
	 * shopId					short			商店ID
	 * itemId					int				物品ID
	 */
	public static final short CMCC_ANDROID_SMS_BUY_REQ_CLIENT = 2178;
	
	/**
	 * cmcc短信购买返回
	 * serial					int
	 * sms						String			短信内容
	 */
	public static final short CMCC_ANDROID_SMS_BUY_REQ_SERVER = 2179;
	
	/**
	 * 卡片自动充能(已过期)
	 * serial						int
	 * groupId                      int
	 */
	public static final short CARD_AUTOADDENERGY_CLIENT = 2180;
	
	/**
	 * 卡片自动充能返回
	 * serial                       int
	 *  size                        int
	 *     groupId                     int                 卡片套装的id
	 *     groupName                   String              卡片套装的名称
	 *     num                         int                 已经收藏卡片的数量
	 *     sum                         int                 套装卡片总数
	 *     per                         int                 完成本套的百分数         
	 * nums                        int                总共收藏卡片的数量
	 * sums                        int                全部套装卡片总数
	 * pers                        int                完成占总张数的百分数
	 * size                        int
	 *     type                            byte              是否收藏(0-未收藏，1-已收藏)
	 *     name                            String            卡片名称
	 *     mergeDec                        String            合成信息
	 *     cardId                          int               卡片Id
	 *     cardDec                         String            卡片描述
	 *     quality						   byte				 卡片品质
	 *     res							   String			 卡片资源
	 *     hasPicture			    	   byte			     1有图卡片、0无图卡片
	 *     energy						   short			 卡片能量
	 *     maxenergy					   short			 卡片最大能量
	 *     star							   byte			     卡片星级
	 *     buffDec                         String            卡片BUFF描述

	 */
	public static final short CARD_AUTOADDENERGY_SERVER = 2181;
	
	/**
	 * 绑定微博
	 * serial						int
	 * name                         String
	 * password                     String
	 */
	public static final short BIND_WEIBO_CLIENT = 2182;
	
	/**
	 * 绑定微博返回
	 * serial						int
	 * type                         byte(0未绑定，1已绑定)
	 */
	public static final short BIND_WEIBO_SERVER = 2183;
	
	/**
	 * 不绑定微博
	 * serial						int
	 */
	public static final short UNBIND_WEIBO_CLIENT = 2184;
	
	/**
	 * 不绑定微博返回
	 * serial						int
	 */
	public static final short UNBIND_WEIBO_SERVER = 2185;
	
	/**
	 * 发送微博
	 * serial						int
	 * type                         byte
	 * playerId                     int
	 * message                      String
	 * version                      byte
	 */
	public static final short SEND_WEIBO_CLIENT = 2186;
	
	/**
	 * 发送微博返回
	 * serial						int
	 */
	public static final short SEND_WEIBO_SERVER = 2187;
	
	/**
	 * 登陆微博
	 * serial						int
	 * name                         String
	 * password                     String
	 */
	public static final short LOGIN_WEIBO_CLIENT = 2188;
	
	/**
	 * 登陆微博返回
	 * serial						int
	 */
	public static final short LOGIN_WEIBO_SERVER = 2189;
	
	/**
	 * 定向礼包：可领取的宝石列表
	 * jewelNum                  int     种类数
	 * 	    id                   int     宝石id
	 *      name                 String  宝石名字
	 */
	public static final short EFFECT_JEWELS_LIST_SERVER = 2190;
	
	/**
	 * 领取宝石定向礼包物品
	 * serial                     int
	 * itemId                     int    礼包id
	 * jewelsId                   int    宝石id
	 * jewelsNum                  int    领取数量
	 */
	public static final short EFFECT_JEWEL_GET_CLIENT = 2191;
    
	/**
	 * 领取宝石定向礼包物品返回
	 * serial                      int
	 */
	public static final short EFFECT_JEWEL_GET_SERVER = 2192;
	
	/**
	 * 显示微博
	 * bind                         byte(0 未绑定，1 绑定)
	 * message                      String
	 */
	public static final short SHOW_WEIBO_SERVER = 2193;
	
	/**
	 * 绑定微博显示
	 * serial						int
	 */
	public static final short BINDED_WEIBO_CLIENT = 2194;
	
	/**
	 * 绑定微博显示返回
	 * serial						int
	 * bind                         byte(0 未绑定，1 绑定)
	 */
	public static final short BINDED_WEIBO_SERVER = 2195;
	
	/**
	 * 请求打卡信息
	 * serial						int
	 * type							byte	打卡类型(0为普通登陆打卡 1为周年庆打卡)
	 */
	public static final short CARD_PUNCH_INFO_CLIENT = 2196;
	
	/**
	 * 请求打卡信息返回
	 * serial						int
	 * hasPunch						byte			是否已打卡（0没有，1已打）
	 * shine						byte			按位（高-低 2-8 第1位忽略）0为灰显，1为亮显
	 * count						byte
	 * 		rewardInfo				String			奖励物品信息
	 */
	public static final short CARD_PUNCH_INFO_SERVER = 2197;
	
	/**
	 * 打卡
	 * serial						int
	 * type							byte	打卡类型(0为普通登陆打卡 1为周年庆打卡)
	 */
	public static final short CARD_PUNCH_CLIENT = 2198;
	
	/**
	 * 打卡返回
	 * serial						int
	 * shine						byte			按位（高-低 2-8 第1位忽略）0为灰显，1为亮显
	 */
	public static final short CARD_PUNCH_SERVER = 2199;
	
	/**
	 * 快速注册微博
	 * serial						int
	 */
	public static final short QUICKREGISTRATE_WEIBO_CLIENT = 2200;
	
	/**
	 * 快速注册微博返回
	 * serial						int
	 * provideName                  String
	 * SMSNumber                    String
	 * 
	 */
	public static final short QUICKREGISTRATE_WEIBO_SERVER = 2201;
	
	/**
	 * 转发微博
	 * serial						int
	 * targetId                     int(对方玩家Id)
	 */
	public static final short TRANSFORM_WEIBO_CLIENT = 2202;
	
	/**
	 * 转发微博返回
	 * serial						int
	 * canTransform                 byte(是否可以转发微博0不可以，1可以)
	 */
	public static final short TRANSFORM_WEIBO_SERVER = 2203;
	
	/**
	 * 闯关传送
	 * serial						int
	 * instanceLevel				byte			副本级别
	 */
	public static final short CYCLE_INSTANCE_GO_CLIENT = 2204;
	
	/**
	 * 开启包月服务
	 * serial						int
	 * itemId					    int			包月物品id
	 */
	public static final short MONTH_PAY_CLIENT = 2205;
	
	/**
	 * 开启包月服务返回
	 * serial						int
	 */
	public static final short MONTH_PAY_SERVER = 2206;
	
	/**
	 * 玩家到达20级还未加军团时系统下发加入提示
	 * serial                       int
	 */
	public static final short TONG_SEND_APPLY_SERVER = 2207;
	
	/**
	 * 客户端发送申请回复 请求所有开启自动接收新人的军团
	 * serial                      int
	 */
	public static final short TONG_SEND_APPLY_CLIENT = 2208;
	
	/**
	 * 开启自动接收新人的军团列表
	 * serial                       int
	 * size                         int       军团个数
	 * 		tongId                  int       军团id
	 * 		name					String    名字
	 * 		peopleNum               int       现在人数
	 */
	public static final short TONG_OPEN_APPLY_LIST_SERVER = 2209;
	
	/**
	 * 申请加入军团（开启自动加入军团的情况）
	 * serial                       int
	 * tongID						int       军团id
	 */
	public static final short TONG_APPLY_JOIN_CLIENT = 2210;
	
	/**
	 * 申请加入军团返回
	 * serial                        int
	 */
	public static final short TONG_APPLY_JOIN_SERVER = 2211;
	
	/**
	 * 军团官员更改自动接受新人的状态
	 * serial                        int
	 * status                        int  0:关闭   1：开启
	 */
	public static final short TONG_AUTO_APPLY_STATUS_CLIENT = 2212;
	
	/**
	 * 军团官员更改自动接受新人的状态返回
	 * serial                        int
	 * dayContribute                 int   当日贡献度上限
	 */
	public static final short TONG_AUTO_APPLY_STATUS_SERVER = 2213;
	
	/**
	 * 玩家自己请求退出军团
	 * serial                        int
	 * type                          byte(0:解散军团,1:退出军团)
	 */
	public static final short TONG_REQUEST_EXIT_CLIENT = 2214;
	
	/**
	 * 玩家自己请求退出军团返回
	 * serial                        int
	 * contribute                    int    玩家在本军团的贡献度
	 */
	public static final short TONG_REQUEST_EXIT_SERVER = 2215;

	/**
	 * 使用军团百宝箱
	 * serial   					int
	 */
	public static final short TONG_USER_BAIBAO_BOX_CLIENT = 2216;
	
	/**
	 * 使用军团百宝箱返回
	 * serial                       int
	 * type                         int 0:20点贡献值(直接给珍珠)     1：100点贡献值(返回物品列表)
	 * type == 0时 下面的物品列表不读
	 * 	    size                    int     可选物品的数量
	 * 			id						int     物品id
	 * 			name					String  物品name
	 * 			icon					int     图标
	 */
	public static final short TONG_USER_BAIBAO_BOX_SERVER = 2217;
	
	/**
	 * 请求领取军团百宝箱物品
	 * serial   					int
	 */
	public static final short TONG_GET_BAIBAO_CLIENT = 2218;
	
	/**
	 * 领取军团百宝箱返回
	 * serial                       int
	 */
	public static final short TONG_GET_BAIBAO_SERVER = 2219;
	
	/**
	 * 激活专属科技
	 * serial						int
	 * tongSkillId                  int             军团技能id
	 * currentLevel                 int             当前等级
	 */
	public static final short TONG_LEVELUP_SKILL_CLIENT = 2220;
	
	/**
	 * 激活专属科技返回
	 * serial                       int
	 * contribute                   int
	 */
	public static final short TONG_LEVELUP_SKILL_SERVER = 2221;
	
	/**
	 * 请求军团卖场列表
	 * serial                      int
	 */
	public static final short TONG_SHOP_LIST_CLIENT = 2222;
	
	/**
	 * 请求军团卖场返回
	 * serial                     int
	 * playerContribute           int   角色当前贡献度
	 * playerSuipian              int   角色碎片个数
	 * size                       int   物品数量
	 *     itemID                 int   	物品id
	 *     itemName               String    物品名称
	 *     icon                   int       图标id
	 *     contribute             int       购买需要的贡献度
	 *     suipian                int       购买需要的碎片
	 */
	public static final short TONG_SHOP_LIST_SERVER = 2223;
	
	/**
	 * 购买军团商店物品
	 * serial					  int
	 * itemId                     int    物品id
	 */
	public static final short TONG_SHOP_BUY_CLIENT = 2224;
	
	/**
	 * 购买军团商店物品
	 * serial					  int
	 * contribute                 int   当前贡献度
	 * suipian                    int   当前碎片数
	 */
	public static final short TONG_SHOP_BUY_SERVER = 2225;
	
	/**
	 * 包月列表
	 * serial					  int
	 */
	public static final short MONTH_PAY_LIST_CLIENT = 2226;
	
	/**
	 * 包月列表返回
	 * serial					  int
	 * size                       int
	 *     itemId                     int
	 *     itemName                   String
	 *     imoney                     int
	 *     remainTime                 int
	 *     dec                        String
	 *     totalTime                  int
	 */
	public static final short MONTH_PAY_LIST_SERVER = 2227;
	
	/**
	 * 索要
	 * serial                           int
	 * playerId                         int(要求为我付费的玩家，现在为配偶)
	 * itemId                           int
	 * count                            int
	 */
	public static final short PAYFORME_INVIT_CLIENT = 2228;
	
	/**
	 * 索要返回
	 * serial                           int
	 * 申请序号						    int
	 * message						    String
	 * itemId                           int
	 * itemName                         String
	 * price                            String
	 * count                            int
	 * totalPrice                       String
	 * */
	public static final short PAYFORME_INVIT__SERVER = 2229;
	
	/**
	 * 答应付费请求
	 * 邀请序号						int
	 * itemId                       int
	 * count                        int
	 */
	public static final short PAYFORME_INVIT_OK_CLIENT = 2230;
	
	/**
	 * 拒绝付费请求
	 * 邀请序号						int
	 */
	public static final short PAYFORME_REJECT_CLIENT = 2231;
	
//	/**
//	 * 拒绝付费返回
//	 * 拒绝人名字					string
//	 * 拒绝原因						string
//	 */
//	public static final short PAYFORME_REJECT_SERVER = 2232;
	
	/**
	 * 请求抽奖
	 * serial                     int
	 * type                       byte(0,上线一小时抽奖;1,抽奖活动)
	 */
	public static final short GET_AWARD_ITEMS_CLIENT = 2233;
	
	/**
	 * 请求抽奖返回
	 * serial  					  int
	 * 循环5次
	 *      state                  byte(是否关闭)
	 *      if(state!=0)
	 * 		   itemId                 int
	 * 		   itemName               String
	 * 		   itemIcon               int
	 */
	public static final short GET_AWARD_ITEMS_SERVER = 2234;
	
	/**
	 * 抽奖
	 * serial					int
	 * closeId1                 int  关闭的第一个物品id(-1 为未关闭)
	 * closeId2                 int  关闭的第二个物品id(-1 为未关闭)
	 */
	public static final short GET_AWARD_CLIENT = 2235;
	
	/**
	 * 抽奖返回  抽中的物品id
	 * serial                  int
	 * itemId                  int  奖品id
	 */
	public static final short GET_AWARD_SERVER = 2236;
	
	/**
	 * 领取抽中的物品
	 * serial                   int
	 * type                     byte(0,上线一小时抽奖;1,抽奖活动)
	 * itemId                   int  奖品id
	 */
	public static final short GET_AWARDITEM_CLIENT = 2237;
	
	/**
	 * 领取抽中物品返回
	 * serial                   int
	 */
	public static final short GET_AWARDITEM_SERVER = 2238;
	
	/**
	 * 许愿树
	 * serial					int
	 * type						byte			操作方式（0、许愿 1、祈福 2、灌溉 4 祈福（面板和祈福树））
	 * limit					byte			一次性祈福次数
	 */
	public static final short VOW_CLIENT = 2239;
	
	/**
	 * 许愿树返回
	 * serial					int
	 * count					byte数量(循环count次)
	 * 		GameItem				byte[]	 物品
	 * 		count2					byte   	 数量
	 */
	public static final short VOW_SERVER = 2240;
	
	/**
	 * 在线一小时提醒
	 * serial                   int
	 */
	public static final short ONLINE_ONEHOUR_SERVER = 2241;
	
	/**
	 * 请求排行榜信息
	 * serial                   int
	 */
	public static final short RANKING_RONGYUTA_CLIENT = 2242;
	
	/**
	 * 请求排行榜信息返回
	 * serial                   int
	 * isIntop10                byte     自己当前状态 0：没有排名   1：有排名但不在前十   2：在前十
	 * size                     byte      循环size次（<=11）
	 * 		name                    String   姓名
	 * 		value                   int      层数
	 * 		rank                    int      名次
	 */
	public static final short RANKING_RONGYUTA_SERVER = 2243;
	
	/**
	 * 日文版邀请好友通知
	 * String                   inviteResult
	 */
	public static final short HANGAME_INVITE_FRIENDS_CLIENT = 2301;
	
	/**
	 * 查看工资列表
	 * serial					int
	 * playerID                 int
	 * startPage                int
	 * pageCount                int
	 */
	public static final short SALARY_INFO_CLIENT = 2302;
	
	/**
	 * 查看工资列表返回
	 * serial                    int
	 * size                      int   循环size次
	 *     dec                   String
	 *     price                 int   
	 * totalItem                 int(总数) 
	 * daySalary                 int(当天已完成工资)
	 */
	public static final short SALARY_INFO_SERVER = 2303;
	
	/**
	 * 苹果专区(如果服务器配置为APP专区，则玩家第一次登陆loading后下发此协议,客户端依次来判断是否是APP专区)
	 */
	public static final short APP_SECTION_SERVER = 2304;
	
	/**
	 * 请求引导列表
	 * serial						int
	 * page							byte			页码（从0开始）
	 * count						byte			每页显示条目数
	 */
	public static final short CLIENT_DIRECTORY_NORMAL_LIST_CLIENT = 2305;
	
	/**
	 * 请求引导时间表
	 * serial						int
	 * week							byte			星期几（中国习惯）
	 * page							byte			页码（从0开始）
	 * count						byte			每页显示条目数
	 */
	public static final short CLIENT_DIRECTORY_TIME_LIST_CLIENT = 2306;
	
	/**
	 * 请求排序引导列表
	 * serial						int
	 * type							byte			排序方式(0经验、1金钱、2战功、3珍珠)
	 * page							byte			页码（从0开始）
	 * count						byte			每页显示条目数
	 */
	public static final short CLIENT_DIRECTORY_BUBBLE_LIST_CLIENT = 2307;
	
	/**
	 * 引导列表返回
	 * serial						int
	 * week							byte			周几（中国习惯）
	 * totalSize					int				总数
	 * size							int				
	 * 		name					String			引导名称
	 * 		timescheduel			String			引导时间
	 * 		desc					String			引导描述
	 * 		directoryList			String			引导明细
	 * 		starLevel				byte			引导星级
	 * 		mapid					short			目标地图ID
	 * 		x						short			目标x坐标
	 * 		y						short			目标y坐标
	 * 		mapName					String			地图名称
	 * 		rewardSize				byte			奖励数量
	 * 			count				byte			物品数量
	 * 			itemInfo			GAMEITEM		物品信息			
	 * 		rewardDec				String			奖励描述
	 */		
	public static final short CLIENT_DIRECTORY_LIST_SERVER = 2308;
	
	/**
	 * 越南Viettel卡充值
	 * serial						int
	 * cardno						String				卡号
	 * pin							String				密码
	 */
	public static final short VIETNAM_VIETTEL_CHARGE_CLIENT = 2309;
	
	/**
	 * 越南Viettel卡充值成功
	 * serial						int
	 */
	public static final short VIETNAM_VIETTEL_CHARGE_SERVER = 2310;
	
	/**
	 * 打孔：显示在装备打孔或摘除费用。
     * serial               int                 序列号
	 * temid                int                 装备的物品id
	 * instanceid           int                 装备的instanceid
	 */
	public static final short SHOW_DECORADE_PRICE_CLIENT = 2311;
	
    /**
     * 打孔：显示在装备打孔或摘除返回。
     * serial               int                 序列号
     * money                int                 费用
     */
    public static final short SHOW_DECORADE_PRICE_SERVER = 2312;
    
    /**
     * 获取商店物品元宝价格
     * serial				int
     * itemId				int					物品ID
     * shopId				short				商店ID
     * itemCount			short				物品数量
     */
    public static final short SHOPITEM_PRICE_CLIENT = 2313;
    
    /**
     * 获取商店物品元宝价格返回
     * serial				int
     * price				String				i币价格
     */
    public static final short SHOPITEM_PRICE_SERVER = 2314;
	
    /**
     * 坐骑幻化
     * serial               int
     * destInstId           int                 坐骑实例ID(需要幻化的坐骑)
     * resInstId			int					坐骑实例ID(被幻化的坐骑)
     */
    public static final short HORSE_CHANGE_CLIENT = 2315;
    
    /**
     * 坐骑幻化返回
     * serial               int
     */
    public static final short HORSE_CHANGE_SERVER = 2316;
    
    /**
     * 解除坐骑幻化
     * serial               int
     * instId           	int                 坐骑实例ID
     */
    public static final short REMOVE_HORSE_CHANGE_CLIENT = 2317;
    
    /**
     * 解除坐骑幻化返回
     * serial               int
     */
    public static final short REMOVE_HORSE_CHANGE_SERVER = 2318;
    
    /**
     * 坐骑合成
     * serial               int
     * destInstId           int                 坐骑实例ID(主坐骑)
     * resInstId			int					坐骑实例ID(副坐骑)
     */
    public static final short HORSE_FIX_CLIENT = 2319;
    
    /**
     * 坐骑合成返回
     * serial               int
     * fixCount             byte		合成等级
     * count				short		保留副坐骑需要的元宝数量(如果大于0表示合成失败)
     */
    public static final short HORSE_FIX_SERVER = 2320;
    
    /**
     * 坐骑合成失败是否保留副坐骑确认
     * serial               int
     * type           		byte            0-不保留,1-保留(是否保留副坐骑)
     * instId           	int             坐骑实例ID
     */
    public static final short HORSE_FIXFAILURE_CLIENT = 2321;
    
    /**
     * 坐骑合成失败是否保留副坐骑确认返回
     * serial               int
     */
    public static final short HORSE_FIXFAILURE_SERVER = 2322;
    
    /**
	 * 领悟的新坐骑技能信息
	 * serial						int
	 * horseInstanceId				int
	 */
	public static final short HORSE_SKILL_CONFIRE_CLIENT = 2323;
	
	/**
	 * 领悟的新坐骑技能信息返回
	 * serial						int
	 * randomIndex					byte	//随即失去的技能
	 * skill						skill	//新技能信息
	 * desc							String 	//描述信息
	 */
	public static final short HORSE_SKILL_CONFIRE_SERVER = 2324;
	
	/**
	 * 缺少物品时自动购买
	 * itemId						int 	//物品ID
	 */
	public static final short QUICK_BUY_ITEM_SERVER = 2325;
	
	/**
	 * 资质鉴定信息
	 * serial						int
	 * itemId						int
	 * instanceId					int
	 */
	public static final short EVALUATION_CONFIRE_CLIENT = 2326;
	
	/**
	 * 资质鉴定信息返回
	 * serial						int
	 * GameItem						byte[](参见后面的ITEM结构信息)
	 */
	public static final short EVALUATION_CONFIRE_SERVER = 2327;
	
	/**
	 * 情人节送花排行榜查看
	 * serial						int
	 * type							byte		0为送花榜，1为收花榜
	 */
	public static final short VALENTINE_RANKING_CLIENT = 2328;
	
	/**
	 * 情人节送花排行榜查看返回
	 * serial						int
	 * topsize						byte	
	 * 		name					String			玩家名字
	 * 		faction					byte			阵营
	 * 		count					int				票数
	 * ownercount					int				本人票数
	 */
	public static final short VALENTINE_RANKING_SERVER = 2329;
	
	/**
	 * 袁绍副本传送
	 * serial						int
	 * type                         byte(0返回入口，1进入二层）
	 */
	public static final short FIVEELEMENT_TRANSFORM_CLIENT = 2330;
	
	/**
	 * 读取书籍
	 * serial						int
	 * bookId					    int
	 */
	public static final short PLAYER_READBOOK_CLIENT = 2331;
	
	/**
	 * 读取书籍返回
	 * serial						int
	 * bookId                       int                书籍id
	 * chapter                      int                当前等级
	 * leftTime                     int                剩余时间
	 * state                        byte               状态
	 */
	public static final short PLAYER_READBOOK_SERVER = 2332;
	
	/**
	 * 获取书籍列表
	 * serial						int
	 */
	public static final short PLAYER_BOOKLIST_CLIENT = 2333;
	
	/**
	 * 获取书籍列表返回
	 * serial						int
	 * size                         short
	 *    id                             int                书籍ID
	 *    name                           String             书籍名称
	 *    level                          int                书籍等级
	 *    onRead                         byte               是否阅读(1是，0否,2暂停)
	 *    minute                         int                剩余分钟
	 *    pay                            String             下次缩短时间费用
	 *    value                          String             下个级别所提升的属性点
	 *    auto                           byte               是否新书(0否，1是)
	 */
	public static final short PLAYER_BOOKLIST_SERVER = 2334;
	
	/**
	 * 暂停阅读书籍
	 * serial						int
	 * bookId                       int
	 */
	public static final short PLAYER_PAUSEREAD_CLIENT = 2335;
	
	
	/**
	 * 暂停阅读书籍返回
	 * serial						int
	 */
	public static final short PLAYER_PAUSEREAD_SERVER = 2336;
	
	/**
	 * 缩短阅读时间
	 * serial						int
	 * bookId                       int
	 */
	public static final short PLAYER_PAYFORREDA_CLIENT = 2337;
	
	
	/**
	 * 缩短阅读时间返回
	 * serial						int
	 * bookId                       int
	 * pay                          String                下次缩短时间费用
	 * leftTime                     int                   剩余时间
	 */
	public static final short PLAYER_PAYFORREDA_SERVER = 2338;
	
	/**
	 * 人物属性重置
	 * serial						int
	 */
	public static final short PLAYER_REBUILDPROPERTY_CLIENT = 2339;
	
	
	/**
	 *人物属性重置返回
	 * serial						int
	 */
	public static final short PLAYER_REBUILDPROPERTY_SERVER = 2340;
	
	/**
	 * 请求是否扣战功增加随从忠诚度
	 * serial							int
	 * pointCredit                      int
	 */
	public static final short  ATTENDANT_IS_DELCREDIT_ClIENT = 2341;
	
	/**
	 * 请求是否扣战功增加随从忠诚度返回
	 * serial							int
	 */
	public static final short  ATTENDANT_IS_DELCREDIT_SERVER = 2342;
	
	/**
	 * 领取每日押镖任务
	 * serial						int
	 */
	public static final short ACCEPT_ESCORT_QUEST_CLIENT = 2343;
	
	/**
	 * 领取每日押镖任务返回
	 * serial						int
	 * carLevel						byte		镖车品质
	 * isRefresh					byte		是否刷新
	 * refreshIB					String		刷新镖车需要的元宝数
	 */
	public static final short ACCEPT_ESCORT_QUEST_SERVER = 2344;
	
	/**
	 * 刷新镖车品质
	 * serial						int
	 * isVip						byte	是否VIP刷新(0--不是，1--是)
	 */
	public static final short ACCEPT_REFRESH_ESCORT_CLIENT = 2345;
	
	/**
	 * 刷新镖车品质返回
	 * serial						int
	 * carLevel						byte		镖车品质
	 * refreshIB					String		刷新镖车需要的元宝数
	 */
	public static final short ACCEPT_REFRESH_ESCORT_SERVER = 2346;
	
	/**
	 * 开始押镖
	 * serial						int
	 * convoyType					byte	//镖车类型（经验型，战功型）
	 */
	public static final short START_ESCORT_CLIENT = 2347;

	/**
	 * 书籍升级返回
	 * bookId							int
	 * leftTime                         int
	 * state                            byte
	 */
	public static final short  BOOK_LEVELUP_SERVER = 2348;
	
	/**
	 * 查看书籍描述
	 * serial							int
	 * bookId                           int
	 */
	public static final short  REVIEW_BOOKDEC_ClIENT = 2349;
	
	/**
	 *查看书籍描述返回
	 * serial						int
	 * dec                          String
	 */
	public static final short REVIEW_BOOKDEC_SERVER = 2350;
	
	/**
	 * 立即完成申请价格
	 * serial							int
	 * bookId                           int
	 */
	public static final short  BOOKTIME_PAY_ClIENT = 2351;
	
	/**
	 *立即完成申请价格返回
	 * serial						int
	 * pay                          String
	 * count                        int                   剩余提升次数
	 */
	public static final short BOOKTIME_PAY_SERVER = 2352;
	
	/**
	 * 立即缩短书籍时间
	 * serial							int
	 * bookId                           int
	 */
	public static final short  QUICKDEC_BOOKTIME_ClIENT = 2353;
	
	/**
	 *立即缩短书籍时间返回
	 * serial						int
	 */
	public static final short QUICKDEC_BOOKTIME_SERVER = 2354;
	
	/**
	 * 祈福树摇卡片
	 * serial						int
	 * npcId						int				祈福树ID
	 * type							byte			祈福方式（0免费 1战功 2元宝）
	 * limit						byte			批量
	 */
	public static final short CARD_PRAY_CLIENT = 2355;
	
	/**
	 * 祈福树摇卡片返回
	 * serial						int
	 * size							byte			摇得卡片数量
	 * 			cardName			String			卡片名字
	 * 			quality				byte			卡片品质
	 * 			AttDesc				String			说明
	 * 			groupId				byte			群组ID(8为技能卡片)
	 * exp							int				本次摇得总经验
	 * freeCount					byte			遥卡剩余次数
	 * creditRockCardHaveCount		short			战功摇卡剩余次数0为达到上限
	 */
	public static final short CARD_PRAY_SERVER = 2356;
	
	/**
	 * 卡片升级
	 * serial						int
	 * cardId						int				卡片ID
	 */
	public static final short CARD_UPGRADE_CLIENT = 2357;
	
	/**
	 * 卡片升级返回
	 * serial						int
	 * totalExp						int				玩家的卡片总经验
     * type							byte    		卡片类型0-普通卡片 1-技能卡片
     *      type==0
	 * 			exp						int				下次升级所需经验
	 * 			attDesc					String			卡片属性描述
	 * 			nextLevelAttDesc		String			下一级卡片属性描述
	 *      type==1
	 * 			needCount				int				下次升级所需卡片数量
	 * 			totalCount				int				拥有此卡片的数量
	 * 			attDesc					String			卡片Buff属性描述
	 * 			nextLevelAttDesc		String			下一级卡片Buff属性描述
	 * 			upGradeNeedExp			int				升级所需经验
	 * 			preLevelDesc			String			前瞻描述3，6，9，12
	 * groupId							byte			群组ID
	 */
	public static final short CARD_UPGRADE_SERVER = 2358;
	
	/**
	 * 卡片镶嵌
	 * serial						int
	 * cardId						int			 	卡片ID
	 * index						byte			装备位
	 * owner						byte			镶嵌对象（0为玩家 1为坐骑 2为随从）
	 */
	public static final short CARD_ADDTOEQUINDEX_CLIENT = 2359;
	
	/**
	 * 卡片镶嵌返回
	 * serial						int
	 * index						byte			装备位
	 * cardId						int				卡片ID
	 * cardName						String			卡片名称
	 * level						byte			卡片级别
	 * att							String			卡片属性增强描述
	 * quality                      byte            卡片品质
	 * star							byte			卡片星级
	 * groupIdEquip					byte			镶嵌的卡片群组
	 * groupIdUnEquip				int				摘除的卡片群组 -1为没有摘除卡片
	 */
	public static final short CARD_ADDTOEQUINDEX_SERVER = 2360;
	
	/**
	 * 卡片重置经验
	 * serial						int
	 * cardId						int				卡片ID
	 */
	public static final short CARD_REMOVEEXP_CLIENT = 2361;
	
	/**
	 * 卡片重置经验返回
	 * serial						int
	 * type							byte    卡片类型0-普通卡片 1-技能卡片
     *      type==0
	 * 			exp							int				升级所需经验
	 * 			totalExp					int				当前总经验
	 * 			attDesc						String			卡片属性描述
	 * 			nextAttDesc					String			下一级卡片属性描述
	 *      type==1
	 * 			needCount				int				下次升级所需卡片数量
	 * 			totalCount				int				拥有此卡片的数量
	 * 			attDesc					String			卡片Buff属性描述
	 * 			nextLevelAttDesc		String			下一级卡片Buff属性描述		
	 */
	public static final short CARD_REMOVEEXP_SERVER = 2362;
	
	/**
	 * 请求摇卡片树信息
	 * serial						int
	 * npcId						int				祈福树ID
	 */
	public static final short CARD_PRAY_INFO_CLIENT = 2363;
	
	/**
	 * 请求摇卡片树信息返回
	 * serial						int
	 * freeCount					byte			免费摇树次数
	 * creditCount					int				战功摇树次数
	 * creditRatio					byte			扣除战功（不再是计算系数）
	 * price                        String          祈福元宝数
	 * exps                         int             玩家卡片总经验
	 * haveCreditRockCount			short			玩家战功摇卡剩余次数0为达到上限
	 */
	public static final short CARD_PRAY_INFO_SERVER = 2364;
	
	/**
	 * 摘除已镶嵌的卡片(两种摘除方式)
	 * serial						int
	 * type							byte			摘除方式（0从装备位 1从卡册）
	 * owner						byte			镶嵌对象（type==0时，发送此字段 | 0为玩家 1为坐骑 2为随从）
	 * index						byte			装备位（type==0时，发送此字段）
	 * cardId						int				卡片ID（type==1时，发送此字段）
	 */
	public static final short CARD_UNEQUIPCARD_CLIENT = 2365;
	
	/**
	 * 摘除已镶嵌的卡片返回
	 * serial						int
	 * owner						byte			镶嵌对象（0为玩家 1为坐骑 2为随从）
	 * index						byte			装备位
	 * groupId						byte			群组ID
	 */
	public static final short CARD_UNEQUIPCARD_SERVER = 2366;
	
	/**
	 * 可扫荡的副本列表
	 * serial						int
	 */
	public static final short INSTANCE_SWEEPLIST_CLIENT = 2367;
	
	/**
	 * 可扫荡的副本列表返回
	 * size                         int
	 *    serial						  int
	 *    id                              int                  副本ID
	 *    name                            String               副本名称
	 *    time                            int                  扫荡时间(分钟)
	 *    reward                          String               奖励
	 *    state                           byte                 状态(0未扫荡，1扫荡中,2已扫荡) 
	 *    money                           String               扫荡需要金钱
	 *    pay                             byte                 是否已付费(0未付费,1已付费)                                     
	 */
	public static final short INSTANCE_SWEEPLIST_SERVICE = 2368;
	
	/**
	 * 扫荡副本
	 * serial						int
	 * instanceId				    int			 	副本ID
	 */
	public static final short INSTANCE_SWEEP_CLIENT = 2369;
	
	/**
	 * 扫荡副本返回
	 * serial						int
	 * instanceId                   int             副本ID
	 * state                        byte            状态(0未扫荡，1扫荡中)
	 * count                        int             扫荡次数
	 * money                        String          下次扫荡需要金钱
	 * time                         int             剩余时间  
	 */
	public static final short INSTANCE_SWEEP_SERVER = 2370;
	
	/**
	 * 缩短副本扫荡时间
	 * serial						int
	 * instanceId				    int	            副本ID		 	
	 */
	public static final short DEC_SWEEPTIME_CLIENT = 2371;
	
	/**
	 * 缩短副本扫荡时间返回
	 * serial						int
	 */
	public static final short DEC_SWEEPTIME_SERVER = 2372;
	
	/**
	 * 星级提升
	 * serial						int
	 * type                         byte            升级类型(1单次，2一键)
	 * equId                        int             装备id
	 * instanceId				    int	            装备instanceId		 	
	 */
	public static final short STAR_PROMOTE_CLIENT = 2373;
	
	/**
	 * 星级提升返回
	 * serial						int
	 * rate                         int             成功率
	 * credit                       int             当前剩余战功
	 */
	public static final short STAR_PROMOTE_SERVER = 2374;
	
	/**
	 * 星级成功率申请
	 * serial						int
	 * equId                        int             装备id
	 * instanceId				    int	            装备instanceId		 	
	 */
	public static final short STARPROMOTE_APPLYRATE_CLIENT = 2375;
	
	/**
	 * 星级成功率申请返回
	 * serial						int
	 * rate                         int             成功率
	 * credit1                      int             单次升星需要战功
	 * credit2                      int             一键升星需要战功
	 */
	public static final short STARPROMOTE_APPLYRATE_SERVER = 2376;
	
	/**
	 * 推送绑定申请
	 * deviceToken                  String          手机token 	
	 */
	public static final short NOTIFICATION_BIND_CLIENT = 2377;
	
	/**
	 * 力美渠道激活
	 * serial						int
	 * udid							String				手机唯一号						
	 */
	public static final short LIMEI_ACTIVATION_CLIENT = 2378;
	
	/**
	 * 每日摇卡经验排行榜申请
	 * serial                       int
	 * type                    byte(1:摇卡排行榜，2：祈福排行榜)          
	 */
	public static final short CARDEXP_ADD_CLIENT = 2379;
	
	/**
	 * 每日摇卡经验排行榜申请返回
	 * serial						int
	 * size                    byte     
	 * 		name                    String   姓名
	 *      faction                 byte     国家
	 * 		exp                     int      摇卡经验
	 * 		rank                    int      名次			
	 */
	public static final short CARDEXP_ADD_SERVICE = 2380;
	
	/**
	 * 获取文件
	 * 文件名						string
	 */
	public static final short NEW_GETFILE_CLIENT = 2381;
	/**
	 * 文件信息
	 * 客户端机型                   string
	 * 文件名						string
	 * 版本信息						int
	 * 文件内容总长度				int
	 * 下发的数据在文件中的起始索引	int
	 * 文件内容(分段下发)			byte[]
	 */
	public static final short NEW_GETFILE_SERVER = 2382;
    
    /**
     * UC Android/Java/IOS客户端账号登录。
     * serial					int
     * 验证码(从SDK获得)				string
     * 机型							string
     * 机型ui版本					string
     * 版本号						string
     * 手机号(可空)					string
     * 角色ID(可选)					int
     */
    public static final short ACCOUNT_LOGIN_UC_ANDROID_CLIENT = 2383;
    
    /**
     * UC Android客户端刷新access token成功（登录成功也会下发这个包）。
     * uid						string
     * access token				string
     * refresh token			string
     */
    public static final short UC_ANDROID_REFRESH_TOKEN_SERVER = 2384;
    
    /**
	 * 祈福元宝申请
	 * serial                       int       
	 */
	public static final short PRAY_IMONEY_CLIENT = 2385;
	
	/**
	 * 每日摇卡经验排行榜申请返回
	 * serial						int
	 * price                        String
	 */
	public static final short PRAY_IMONEY_SERVICE = 2386;
    
    /**
     * 云游 Android客户端账号登录。
     * serial						int
     * sessionId					string
     * userId						String
     * 机型							string
     * 机型UI						string
     * 版本号						string
     * 手机号(可空)					string
     * 角色ID(可选)					int
     */
    public static final short ACCOUNT_LOGIN_YUNYOU_ANDROID_CLIENT = 2387;
    
    /**
     * 云游Android客户端账号登陆返回
     * serial						int
     * balance						String			余额（云游币）
     */
    public static final short ACCOUNT_LOGIN_YUNYOU_ANDROID_SERVER = 2388;
    
    /**
     * 云游Android购买元宝
     * serial						int
     * imoney						int				元宝数					
     */
    public static final short YUNYOU_BUYIMONEY_CLIENT = 2389;
    
    /**
     * 云游Android购买元宝返回
     * serial						int
     */
    public static final short YUNYOU_BUYIMONEY_SERVER = 2390;
    
	/**
	 * 当乐189hi平台充值
	 * serial						int
	 * 充值金额						int 实际的充值卡面额，单位元
	 * 充值卡号						string
	 * 充值卡密码					string
	 * 充值的角色名					string (如果是自己充值，这个字段是空字符串，如果为他人充值，是他人的角色名)
	 * 卡类型						byte(0 移动神州行 1 联通卡 2 电信卡)
	 */
	public static final short CHARGE_DOWNJOY_CLIENT = 2391;
	
	/**
	 * 充值返回
	 * serial						int
	 * message						string
	 * 充值结果						byte(0 失败 1 成功)
	 */
	public static final short CHARGE_DOWNJOY_SERVER = 2392;
	
	/**
	 * CMCC用点卡数购买元宝
	 * serial			int
     * cost				int				价格(元宝)
	 */
	public static final short CMCC_CHARGE_NEW_CLIENT = 2393;
	
	/**
	 * CMCC用点卡数购买元宝
	 * serial						int
	 */
	public static final short CMCC_CHARGE_NEW_SERVER = 2394;
	
	 /**
	 * 抽奖列表申请
	 * serial                       int       
	 */
	public static final short GAMBLE_LIST_CLIENT = 2395;
	
	/**
	 * 抽奖列表申请返回
	 * serial						int
	 * size                         byte
	 *     gambleId                       byte               抽奖类型
	 *     decMoney                       int                需要消费元宝
	 *     rewardItem                     int                奖励物品id
	 *     itemName                       String             物品名称
	 *     iconIndex                      int                图标索引
	 *     iconImage                      int                图标引用图片索引
	 * fuliValue                    int                   玩家福利值
	 */
	public static final short GAMBLE_LIST_SERVICE = 2396;
	
	/**
	 * 抽奖类型列表申请
	 * serial                       int 
	 * gambleId                     byte      
	 */
	public static final short GAMBLE_DETAILLIST_CLIENT = 2397;
	
	/**
	 * 抽奖类型列表返回
	 * serial						int
	 * size                         byte
	 *     typeId                         byte               类型
	 *     rate                           int                中奖概率
	 *     decMoney                       int                需扣除金钱
	 */
	public static final short GAMBLE_DETAILLIST_SERVICE = 2398;
	
	/**
	 * 抽奖
	 * serial                       int 
	 * gambleId                     byte  
	 * typeId                       byte    
	 */
	public static final short GAMBLE_PROCESS_CLIENT = 2399;
	
	/**
	 * 抽奖返回
	 * serial						int
	 */
	public static final short GAMBLE_PROCESS_SERVICE = 2400;
	
	/**
	 * 满汉全席NPC功能
	 * serial                       int 
	 * typeId                       byte(0,确定报名，1，确定传进场景,2不进入场景)      
	 */
	public static final short FEAST_SIGNANDENTER_CLIENT = 2401;
	
	/**
	 * 满汉全席NPC功能2
	 * serial                       int 
	 * typeId                       byte(0多了，1，少了) 
	 * materialname                 String(材料名称)     
	 */
	public static final short FEAST_NPCFUNCTION_CLIENT = 2402;
	
	/**
	 * 满汉全席NPC功能2返回						
	 * serial                       int
	 */
	public static final short FEAST_NPCFUNCTION_SERVICE = 2403;
	
	/**
	 * 满汉全席查看菜谱
	 * serial                       int      
	 */
	public static final short FEAST_VIEWMENU_CLIENT = 2404;
	
	/**
	 * 满汉全席查看菜谱返回						
	 * serial                       int
	 * menuName                     String(菜谱名称)
	 * size                         int(材料个数)
	 *     indexId                          byte（索引id）
	 *     materialId                       int(材料id)
	 *     name                             String(材料名称)
	 *     count                            int(需要个数)
	 *     killCount                        int(已获得个数)
	 */
	public static final short FEAST_VIEWMENU_SERVICE = 2405;
	
	/**
	 * 天语通SDK充值订单验证。
	 * serial 			int
	 * orderid 			string		订单ID
	 * price			int			价格（分）
	 * resultcode		string		返回代码
	 * sign				string		订单签名
	 */
	public static final short KTOUCH_CHECK_RECEIPT_CLIENT = 2406;
	/**
	 * 天语通SDK充值订单验证通过。
	 * serial			int
	 */
	public static final short KTOUCH_CHECK_RECEIPT_SERVER = 2407;
	
	/**
	 * 请求客户端防外挂数据
	 * type				int			(-1为清零客户端计算数据，非-1是加密key)
	 */
	public static final short ANTI_BOT_SERVER = 2408;
	
	/**
	 * 客户端返回防外挂数据
	 * tickCount			int			服务器两次请求时间段内跑过的tick总数
	 * exceptTickCount		int 		服务器两次请求时间段内移动距离异常的tick总数
	 * moveDistance			int			服务器两次请求时间段内移动总距离差
	 * CRC					int			加密数字
	 */
	public static final short ANTI_BOT_CLIENT = 2409;
	
	/**
	 * 广播材料数量
	 * serial                       int 
	 * typeId                       byte(0多了，1，少了) 
	 * materialname                 String(材料名称)     
	 */
	public static final short FEAST_MATERIALCOUNT_CLIENT = 2410;
	
	/**
	 * 使用国公物品
	 * serial                       int 
	 * typeId                       byte(0准备物品，1，坐骑物品) 
	 */
	public static final short USE_KINGITEM_CLIENT = 2411;
	
	/**
     * 91账号登录。
     * serial					int
     * 用户uin					string
     * 用户sessionid				string
     * 机型						string
     * 版本号					string
     * 手机号(可空)				string
     * 角色ID(可选)				int
     */
    public static final short ACCOUNT_LOGIN_91_CLIENT = 2412;
    
    /**
     * 明珠三周年上交火种
     * serial					int
     */
    public static final short THREE_YEAR_HANDIN_CLIENT = 2413;
    
    /**
     * 明珠三周年上交火种成功
     * serial					int
     */
    public static final short THREE_YEAR_HANDIN_SERVER = 2414;
    
    /**
     * 明珠三周年活动火炬排行榜
     * serial					int
     */
    public static final short THREE_YEAR_REPUTE_CLIENT = 2415;
    
    /**
     * 明珠三周年活动火炬排行榜返回
     * serial					int
     * num						byte
     * 		repute				byte		名次
     * 		faction				byte		阵营
     * 		count				int			火种数
     * 		king				String		国公名字
     */
    public static final short THREE_YEAR_REPUTE_SERVER = 2416;
    
    /**
	 * 星级鉴定
	 * serial						int
	 * itemId						int
	 * instanceId					int
	 */
	public static final short STARENHANCE_CONFIRE_CLIENT = 2417;
	
	/**
	 * 星级鉴定返回
	 * serial						int
	 * GameItem						byte[](参见后面的ITEM结构信息)
	 */
	public static final short STARENHANCE_CONFIRE_SERVER = 2418;
	
    /**
	 * 请求当前荣誉塔闯关级别
	 */
	public static final short CYCLE_INSTANCE_LEVEL_CLIENT = 2419;
	
	 /**
	 * 发送当前荣誉塔闯关级别
	 * instanceLevel				byte	当前闯关的最高级别
	 */
	public static final short CYCLE_INSTANCE_LEVEL_SERVER = 2420;
	
	/**
	 * 支付宝支付接入，创建支付订单。
	 * serial 			int		序列号
	 * amount			String 	金额（元）
	 * subject			String	充值项目标题
	 * desc				String	充值项目描述
	 */
	public static final short ALIPAY_GETORDER_CLIENT = 2421;
	
	/**
	 * 支付宝支付接入，返回新的支付订单信息。
	 * serial			int		序列号
	 * orderInfo		String	订单信息（可直接用于支付接口）
	 */
	public static final short ALIPAY_GETORDER_SERVER = 2422;
	
	/**
	 * 随从任务没有随从返回。
	 * serial			int		序列号
	 * type             int     任务类型
	 */
	public static final short ATTQUEST_NOATTENDANT_SERVER = 2423;
	
	/**
	 * 跨服战场报名
	 * serial			int
	 * level			short
	 * type				byte
	 */
	public static final short STEPSERVER_BATTLE_SIGNUP_CLIENT = 2424;
	
	/**
	 * 跨服战场报名返回	
	 * serial			int
	 * cause			byte
	 * type				byte
	 */
	public static final short STEPSERVER_BATTLE_SIGNUP_SERVER = 2425;
	
	/**
	 * 跨服战场排行榜单查询	
	 * serial			int
	 * (0.常规赛   1.1v1  2.争霸赛)
	 */
	public static final short STEPSERVER_BATTLE_SCORE_CLIENT = 2426;
	
	/**
	 * 跨服战场排行榜单查询返回
	 * serial						int
	 * 数量							short
	 * 循环n次
	 *   名次						short
	 *   服务器名字					String
	 *   名字						String
	 *   阵营						byte
	 *   第一名次数					short
	 */
	public static final short STEPSERVER_BATTLE_SCORE_SERVER = 2427;
	
	/**
	 * 进入五子棋地图	
	 * serial			int
	 * type             byte(0,五子棋任务;1,国庆节任务;2,师徒任务)
	 */
	public static final short ENTERMAP_CHESSINSTANCE_CLIENT = 2428;
	
	/**
	 * 进入五子棋地图返回
	 * serial						int
	 */
	public static final short ENTERMAP_CHESSINSTANCE_SERVER = 2429;
	
	/**
	 * 新手小副本排行榜	
	 * serial			int
	 * type             byte(0,最快击杀;1,击杀最少棋子)
	 */
	public static final short CHESSINSTANCE_BOARD_CLIENT = 2430;
	
	/**
	 * 进入屋子棋地图返回
	 * serial						int
	 * size                         int
	 *   名字                          String
	 *   名次                          int
	 *   时间或个数                      String
	 *   国家                          byte
	 *   级别                          int
	 *   性别                          byte
	 *   职业                          byte
	 */
	public static final short CHESSINSTANCE_BOARD_SERVER = 2431;
	
	/**
	 * 客户端服务器通知跨服服务器
	 * type							byte	(1为掉线协议)
	 * value						String	参数值
	 */
	public static final short CLIENTSERVER_STEPSERVER_INFO_CLIENT = 2432;
	
	/**
	 * 跨服服务器通知客户端服务器
	 * type							byte	(1为跨服战场结束2为战场开始3为背包改变)
	 * value						String	参数值
	 */
	public static final short STEPSERVER_CLIENTSERVER_INFO_SERVER = 2433;
	
	/**
	 * 活动道具效果二次确认
	 * itemId                       int
	 * type							int	
	 * value						int	
	 * addValue                     String
	 */
	public static final short ACTIVITY_ITEMEFFECT_CLIENT = 2434;

	/**
	 * 华为SDK支付接入，创建支付订单。
	 * serial 			int		序列号
	 * amount			int 	金额（分）
	 */
	public static final short HUAWEI_GETORDER_CLIENT = 2435;
	
	/**
	 * 华为SDK支付接入，返回新的支付订单信息。
	 * serial			int		序列号
	 * orderID			String	订单ID
	 * notifyurl		String 	通知地址
	 */
	public static final short HUAWEI_GETORDER_SERVER = 2436;
	
	/**
     * 小米账号登录。
     * serial					int
     * 用户uin					string
     * 用户sessionid				string
     * 机型						string
     * uiModel					string
     * 版本号					string
     * 手机号(可空)				string
     * 角色ID(可选)				int
     */
    public static final short ACCOUNT_LOGIN_XIAOMI_CLIENT = 2437;
    
	/**
	 * 小米SDK支付接入，创建支付订单。
	 * serial 			int		序列号
	 * amount			int 	金额（分）
	 */
	public static final short XIAOMI_GETORDER_CLIENT = 2438;
	
	/**
	 * 小米SDK支付接入，返回新的支付订单信息。
	 * serial			int		序列号
	 * orderID			String	订单ID
	 */
	public static final short XIAOMI_GETORDER_SERVER = 2439;
	
	/**
     * 联想乐逗账号登录。
     * serial					int
     * game_id					string
     * openid					string
     * 用户sessionid				string
     * 机型						string
     * uiModel					string
     * 版本号					string
     * 手机号(可空)				string
     * 角色ID(可选)				int
     */
    public static final short ACCOUNT_LOGIN_LENOVO_CLIENT = 2440;
    
    /**
	 * 随从转换经验	
	 * serial			            int
	 * instanceId                   int(坐骑instanceId)
	 */
	public static final short ATTENDANT_CHANGETOEXP_CLIENT = 2441;
	
	/**
	 * 随从转换经验返回
	 * serial						int
	 * attExp                       int               玩家拥有的随从经验
	 */
	public static final short ATTENDANT_CHANGETOEXP_SERVER = 2442;
	
	/**
	 * 随从升级	
	 * serial			            int
	 * instanceId                   int(随从instanceId)
	 */
	public static final short ATTENDANT_LEVELUP_CLIENT = 2443;
	
	/**
	 * 随从升级返回
	 * serial						int
	 * level                        int               随从等级
	 * nextExp                      int               随从升级
	 * attExp                       int               玩家拥有的随从经验
	 * skill                        byte[]            特殊技能信息
	 * exchangeExp                  int               随从可以转换的经验
	 */
	public static final short ATTENDANT_LEVELUP_SERVER = 2444;
	
	/**
	 * 感恩节活动传送
	 * serial						int
	 * level						byte			难度级别(0-普通，1-困难)
	 */
	public static final short THANKS_GIVING_GO_CLIENT = 2445;
	
	/**
	 * 批量使用物品
	 * serial						int
	 * itemId						int
	 * count                     int	
	 */
	public static final short BULK_USEITEM_CLIENT = 2446;
	
	/**
	 * 批量使用物品返回
	 * serial						int
	 */
	public static final short BULK_USEITEM_SERVER = 2447;
	
    /**
     * 中兴SDK支付，客户端请求创建订单。
     * serial					int
     * amount					int	金额(分)
     */
	public static final short ZHONGXING_GET_ORDER_CLIENT = 2448;
	
    /**
     * 中兴SDK支付，创建订单成功。
     * serial					int
     * orderid					String 订单号
     */
	public static final short ZHONGXING_GET_ORDER_SERVER = 2449;
	
	/**
     * 首充或者累计充值界面
     * serial					int
     * type					    byte	(0新区活动,1首充,2累计充值)
     */
	public static final short CHARGEACTIVITY_UIINFO_CLIENT = 2450;
	
    /**
     * 首充或者累计充值界面返回
     * serial					int
     * size                     int
     * hasGetMoney              int		累计充值金额
     * lastDays              	byte	新区活动剩余天数
     * 循环N次
     *    money                     int                充值额度
     *    rewardState               byte               奖励状态(0-未达到，1-可领取, 2-已领取)
     *    size                      int 
	      循环N次
			   * itemId                 int             奖励物品Id
			   * iconId                 int             奖励物品IconId
			   * imageId                int             奖励物品imageId
			   * count                  int             奖励物品个数
			   * quality                byte            奖励物品品质
			   * itemDec                String          奖励物品描述
			   * itemName               String          物品名称
	*/
	public static final short CHARGEACTIVITY_UIINFO_SERVER = 2451;

	public static final short LOADING_FINISHED1_CLIENT = 2452;
	
	
	
	
	/**
	 * 跨服争霸战场押注榜单查询	
	 * serial			int
	 */
	public static final short STEPSERVER_BATTLE_SCORE_FINALS_CLIENT = 2453;
	
	/**
	 * 跨服争霸战场押注榜单查询返回
	 * serial						int
	 * 数量							short
	 * 循环n次
	 *   服务器名字					String
	 *   名字						String
	 *   betplayerid					int
	 *   betaccountid					int
	 *   押注数（金币）				int
	 *   我的押注数					int
	 * hadWatch                     byte      是否已有观战对像(1有，0没有)
	 * watchPlayerId				int       被观战的playerId
	 * watchAccountId				int		  被观战的accountId
	 */
	public static final short STEPSERVER_BATTLE_SCORE_FINALS_SERVER = 2454;
	
	
	/**
	 * 跨服争霸赛观战，押注
	 * serial  						int
	 * type							byte     0-观战  1-押注
	 * targerPlayerId          		int
	 * targerPlayerAccountId   		int
	 * coinCount					int      金币数量(最多100万)(type==0时可不用传此参数)
	 */
	public static final short STEPBATTLE_FINALS_BETANDWATCH_CLIENT=2455;
	/**
	 * 跨服争霸赛观战，押注返回
	 * serial  						int
	 * type							byte     0-观战  1-押注
	 * type==1 返回
	 * betcoins						int    成功返回扣除金额，超过100万返回0
	 * errorMessage					String 如果成功返回""  失败返回（您的押注超过100万）
	 * type==0 返回
	 * Message					    String 您已成功预约观战???
	 */
	public static final short STEPBATTLE_FINALS_BETANDWATCH_SERVER=2456;
	
	
	/***
	 * 本地服务器通知跨服扣除金币成功
	 * serial 						int
	 * type							byte  0-观战  1-押注
	 * 		type==0返回
	 * 		
	 *      type==1返回
	 * 		扣除金币数
	 */
	public static final short STEPBATTLE_FINALS_BETANDWATCH_CLIENTTOSTEPSERVER=2457;
	
	/**
     * 末日狂欢抽奖
     * serial					int
     */
	public static final short AWARDACTIVITY_RESULT_CLIENT = 2458;
	
	/**
	 * 末日狂欢抽奖返回
	 * serial  					  int
	 * awardId                    int(抽中的物品)
	 */
	public static final short AWARDACTIVITY_RESULT_SERVER = 2459;
	
	/**
     * 请求随从信息
     * serial					int
     * 玩家Id					int
     * 随从instId				int
     */
	public static final short ATTENDANT_INFO_CLIENT = 2460;
	
	/**
	 * 请求随从信息返回
	 * serial  					int
	 * 当前随从信息				attendant(attendant.toClientBytes(player))
	 */
	public static final short ATTENDANT_INFO_SERVER = 2461;
	
	
	/**
	 * 购买并自动使用商品
	 * serial		int
	 * shopID		int		商店ID
	 * itemID		int			物品ID
	 * count		short		购买数量
	 */
	public static final short SHOP_QUICK_BUYANDUSE_CLIENT = 2462;
	
	/**
	 * 购买并自动使用商品成功
	 * serial  					int
	 */
	public static final short SHOP_QUICK_BUYANDUSE_SERVER = 2463;
	
	/**
     * CMCC商品列表
     */
	public static final short GET_CMCC_YUANBAO_LIST_CLIENT = 2464;
	
	/**
	 * CMCC商品列表
	 * int 						size
	 * 			String 			title
	 * 			String			consumeCode
	 */
	public static final short GET_CMCC_YUANBAO_LIST_SERVER = 2465;
	
	/**
	 * 自动装备强化
	 * serial						int
	 * itemId						int
	 * instanceId					int
	 * percent						int					    期望强化级别基数(60绿色,70蓝色,80紫色,90橙色)
	 * specialAtt                   byte                    期望提升的特殊属性（从0开始）
	 */
	public static final short AUTO_EQUIPENHANCE_CLIENT = 2466;
	
	/**
	 * 自动装备强化返回
	 * serial						int
	 * money						int					消耗金钱数量
	 * count                        int                 强化次数
	 * GameItem						byte[](参见后面的ITEM结构信息)
	 * cause						byte				原因（0次数用完，1钱不足，2成功, 3角色状态异常）
	 */
	public static final short AUTO_EQUIPENHANCE_SERVER = 2467;
	
	/**
     * VIP押镖需求
     * serial				int
     * convoyType			byte	//镖车类型（经验型，战功型）
     //* vipDemand			byte	//VIP要求(1绿2蓝3紫4橙品质, 5双倍奖励) -- 暂时废弃
     */
	public static final short VIP_ESCORT_QUEST_CLIENT = 2468;
	
	/**
	 * VIP押镖需求返回
	 * serial  					int
	 */
	public static final short VIP_ESCORT_QUEST_SERVER = 2469;
	
	/**
	 * 本地服务器通知跨服服务器本地玩家的在线状态
	 * 
	 * */
	public static final short STEPBATTLE_CHECKSTEPCLIENT_PLAYERSTATE_STEPCLIENT=2470;
	
	/**
     * VIP充值额度请求
     * serial  					int
     */
	public static final short VIP_CHARGE_VALUE_CLIENT = 2471;
	
	/**
	 * VIP充值额度请求返回
	 * serial  					int
	 * valuenow                 int
	 * valuenextlevel           int
	 */
	public static final short VIP_CHARGE_VALUE_SERVER = 2472;
	
	/**
     * 资质鉴定属性请求
     * serial  					int
     * itemId                   int
     */
	public static final short VIP_NATURAL_PROPERTY_CLIENT = 2473;
	
	/**
	 * 资质鉴定属性请求返回
	 * serial  				int
	 * type                 int 装备属性
	 * size                 int
	 *   property               int
	 *   propertyName           String
	 */
	public static final short VIP_NATURAL_PROPERTY_SERVER = 2474;
	
	/**
	 * 春节发送祝福
	 * serial  				int
	 * type					byte	//0-正常发送，1-确认用1元宝发送祝福
	 * destId				int		//目标ID
	 */
	public static final short SEND_NEWYEAR_PRAY_CLIENT = 2475;
	
	/**
	 * 春节发送祝福返回
	 * serial  				int
	 * type					byte	//0-成功，1-错误
	 * 	错误时返回二次确认信息
	 * 	String				message		//确认信息
	 */
	public static final short SEND_NEWYEAR_PRAY_SERVER = 2476;
	
	/**
     * 新年活动请求
     * serial  					int
     * type                     byte(0福星对话，1，寿星对话，2，禄星对话)
     */
	public static final short NEWYEAR_ACTIVITY_CLIENT = 2477;
	
	/**
	 * 新年活动请求返回
	 * serial  				int
	 */
	public static final short NEWYEAR_ACTIVITY_SERVER = 2478;
	
    /**
     * 天语SDK支付，客户端请求创建订单。
     * serial					int
     * amount					int	金额(分)
     */
	public static final short KTOUCH_GET_ORDER_CLIENT = 2479;
	
    /**
     * 天语SDK支付，创建订单成功。
     * serial					int
     * orderid					String 订单号
     * notifyUrl				String 通知地址
     */
	public static final short KTOUCH_GET_ORDER_SERVER = 2480;
	
	
	
	
	public static final short BEAUTYPARADE_REWARD_CLIENT=2481;
	/***
	 * 请求选美奖励列表
	 * serial							int
	 * 名次数							int
	 * 循环N次
	 *      奖励物品名字(多个)   		String
	 *      奖励物品说明 (多个用|分割)    String
	 */
	public static final short BEAUTYPARADE_REWARD_SERVER=2482;
	
	
	/**
     * 女人节活动
     * serial					int
     * type					    byte(0报名请求，1进入请求)
     */
	public static final short WOMEN_DAY_CLIENT = 2483;
	
    /**
     * 女人节活动返回
     * serial					int
     */
	public static final short WOMEN_DAY_SERVER = 2484;
	
	
	/**
	 * 修炼信息请求
	 * serial				    int
	 */
	public static final short ALCHEMY_INFO_CLIENT=2485;
	
	/***
	 * 修炼信息返回
	 * serial					int
	 * 修炼示图数据
	 * currentLevel				byte 当前重天
	 * currentPulse				byte 当前脉(0-4共5个脉)
	 * currentAcupointNum		byte 当前穴位(0-8共9个穴位)
	 * currentAcupointLevel		byte 当前穴位修炼的等级(0-9共10层)
	 * currentExp				int 当前已修炼的经验
	 * currentNeedExp			int 当前修炼需要的总经验
	 * restExp					int 留存经验
	 * 
	 * 左方信息显示所需数据
	 * info						String 左方显示信息
	 * attack					byte每个穴增加的攻击力
	 * spellatt					byte每个穴增加的法攻
	 * hp						short血上限
	 * def						byte防御
	 * spelldef					byte法防
	 * jewel					byte宝石*1000
	 * attBreak					int突破重天增加的物攻
	 * spellDefBreak			int突破重天增加的法攻
	 * firstAlchemy				byte当天第一次修炼(0提示  -1不提示)
	 * pay						short 元宝百修价格
	 */
	public static final short ALCHEMY_INFO_SERVER=2486;
	
	
	/**
	 * 人物经验修炼
	 * serial					int
	 */
	public static final short ALCHEMY_BYPLAYEREXP_CLIENT=2487;
	
	/**
	 * 人物经验修炼返回
	 * serial 					int
	 * 修炼示图数据
	 * currentLevel				byte 当前重天
	 * currentPulse				byte 当前脉(0-4共5个脉)
	 * currentAcupointNum		byte 当前穴位(0-8共9个穴位)
	 * currentAcupointLevel		byte 当前穴位修炼的等级(0-9共10层)
	 * currentExp				int 当前已修炼的经验
	 * currentNeedExp			int 当前修炼需要的总经验
	 * restExp					int 留存经验
	 * 
	 * 左方信息显示所需数据
	 * info						String 左方显示信息
	 * 本次修炼增加的属性和修炼经验
	 * changeInfo				String 本次修炼变化的属性和经验
	 */
	public static final short ALCHEMY_BYPLAYEREXP_SERVER=2488;
	
	/**
	 * 元宝百修
	 * serial					int
	 */
	public static final short ALCHEMY_BYIMONEY100_CLIENT=2489;
	
	/**
	 * 元宝百修返回
	 * serial 					int
	 * 修炼示图数据
	 * currentLevel				byte 当前重天
	 * currentPulse				byte 当前脉(0-4共5个脉)
	 * currentAcupointNum		byte 当前穴位(0-8共9个穴位)
	 * currentAcupointLevel		byte 当前穴位修炼的等级(0-9共10层)
	 * currentExp				int 当前已修炼的经验
	 * currentNeedExp			int 当前修炼需要的总经验
	 * restExp					int 留存经验
	 * 
	 * 左方信息显示所需数据
	 * info						String 左方显示信息
	 * 本次修炼增加的属性和修炼经验
	 * changeInfo				String 本次修炼变化的属性和经验
	 */
	public static final short ALCHEMY_BYIMONEY100_SERVER=2490;
	
	/**
	 * 元宝修炼（每次5元宝）
	 * serial					int
	 */
	public static final short ALCHEMY_BYIMONEY_CLIENT=2491;
	
	/**
	 * 元宝修炼返回
	 * serial 					int
	 * 修炼示图数据
	 * currentLevel				byte 当前重天
	 * currentPulse				byte 当前脉(0-4共5个脉)
	 * currentAcupointNum		byte 当前穴位(0-8共9个穴位)
	 * currentAcupointLevel		byte 当前穴位修炼的等级(0-9共10层)
	 * currentExp				int 当前已修炼的经验
	 * currentNeedExp			int 当前修炼需要的总经验
	 * restExp					int 留存经验
	 * 
	 * 左方信息显示所需数据
	 * info						String 左方显示信息
	 * 本次修炼增加的属性和修炼经验
	 * changeInfo				String 本次修炼变化的属性和经验
	 */
	public static final short ALCHEMY_BYIMONEY_SERVER=2492;
	
	/**
	 * 突破重天
	 * serial					int
	 */
	public static final short ALCHEMY_BREAKLEVEL_CLIENT=2493;
	
	/**
	 * 突破重天返回
	 * serial					int
	 */
	public static final short ALCHEMY_BREAKLEVEL_SERVER=2494;
	
	/**
     * 充值活动领取奖励
     * serial					int
     * type					    byte	(0首充,1累计充值,2新区活动)
     */
	public static final short CHARGEACTIVITY_GETREWARD_CLIENT = 2495;
	
    /**
     * 充值活动领取奖励返回
     * serial					int
	*/
	public static final short CHARGEACTIVITY_GETREWARD_SERVER = 2496;
	
	/***
	 * 改变玩家修炼经验
	 * serial                                   int
	 * accountId                                int
	 * playerId                                 int
	 * alchemyExp                               int 修改玩家修炼经验,可为负数（负数为减少修炼经验)
	 */
	public static final short ADMIN_DEC_ALCHEMYEXP_CLIENT=2497;
	
	/***
	 * 改变玩家修炼经验返回
	 * serial                                   int
	 */
	public static final short ADMIN_DEC_ALCHEMYEXP_SERVER=2498;
	
	/**
     * 新版通用联运平台创建订单接口。
     * serial					int
     * platform					string		目前支持：UPMP = 银联支付
     * money					int			金额（单位分）
     */
    public static final short PARTNER_GETORDER_CLIENT = 2499;
    
    /**
     * 新版通用联运平台创建订单接口，返回订单信息。
     * serial					int
     * orderInfo				string		订单信息，通常是订单ID，但也有一些平台的订单信息较为复杂
     */
    public static final short PARTNER_GETORDER_SERVER = 2500;
    
    /**
     * 五一活动之众志传烽提交材料请求
     * serial					int
     * type                     byte(0提交材料，1查询分数)
     */
    public static final short MAYDAY_HANDIN_CLIENT = 2501;
    
    /**
     * 五一活动之众志传烽提交材料返回
     * serial					int
     */
    public static final short MAYDAY_HANDIN_SERVER = 2502;
    
    /**
     * 装备升级信息
     * serial					int
     * itemid					int
     * instanceId				int
     */
    public static final short EQUIPMENT_LEVELUP_ITEMINFO_CLIENT=2503;
    
    /**
     * 装备升级信息返回
     * serial					int
     * sourceGameItemHadSuite   byte 原装备是否有套装信息 0-没有 1-有
     * if(sourceGameItemHadSuite==1)====================================
     * specaileffect		String			当前物品特效
	 * title				String			套装前缀
	 * num1					byte			套装中装备数量(此处默认为1)
	 * 循环 num1 次
	 * 		name			String		 	装备名称
	 * 		flag			byte			是否穿着 0为没穿，1为穿着
	 * 
	 * ratio				String			比例(默认1/total)
	 * 
	 * num2					byte			套装描述信息(此处默认为1)
	 * 循环 num2 次
	 * 		desc			String			套装效果描述信息
	 * 		flag			byte			是否显示 0为不显示，1为显示
	 * ==================================================================
     * GameItem					byte[]
     * desGameItemHadSuite      byte 下一级装备是滞有套装信息 0-没有 1-有
     * if(desGameItemHadSuite==1)-----------------------------------------
     *      * specaileffect		String			当前物品特效
	 * title				String			套装前缀
	 * num1					byte			套装中装备数量(此处默认为1)
	 * 循环 num1 次
	 * 		name			String		 	装备名称
	 * 		flag			byte			是否穿着 0为没穿，1为穿着
	 * 
	 * ratio				String			比例(默认1/total)
	 * 
	 * num2					byte			套装描述信息(此处默认为1)
	 * 循环 num2 次
	 * 		desc			String			套装效果描述信息
	 * 		flag			byte			是否显示 0为不显示，1为显示
	 * ----------------------------------------------------------------------
     * needType					byte 需要扣除的类型 0无 1战功 2元宝 3材料 4材料不够用元宝补齐
     * needNums					int	对应扣除类型扣除数量
     * hintUseMetrialChoose		String 提示选择升级方式:材料还是元宝升级的提示(type==4时才有此值)
     * hintUseMetrial           String 选材料升级时提示:4时显示，其他为""
     * hintUseMoney				String 材料升级时选元宝升级的提示(needType为2,3和4时都要有提示)
     * savePropPriceType		byte 保留属性要消耗的原料类型 0无  1元宝 99默认白装不显示保留属性选择框
     * savePropPriceNums		int 对应原料类型要扣除的数量
     * EquipLevelUpExplain		String 装备升级说明
     */
    public static final short EQUIPMENT_LEVELUP_ITEMINFO_SERVER=2504;
    
    /**
     * 装备升级
     * serial					int
     * itemId					int
     * instanceId				int
     * needType					byte 升级类型 0无 1战功 2元宝 3材料够用时 4材料不够用元宝补齐
     * isSaveProp				byte 0不保留属性，1保留属性
     */
    public static final short EQUIP_LEVELUP_CLIENT=2505;
    
    /***
     * 装备升级返回
     * serial						int
     * newItemId					int升级后的装备id
     * newItemInstanceId			int升级后的装备实例id
     * newItemCanLevelUp			byte 0-不可升级  1-可升级
     */
    public static final short EQUIP_LEVELUP_SERVER=2506;
    
    /**
     * 查看轩辕套装信息
     * serial					int
     * itemid					int
     * instanceId				int
     */
    public static final short EQUIPMENT_SUITEITEMINFO_CLIENT=2507;
    
    /**
     * 查看套信息返回
     * serial					int
     * count					byte套装数量
     * 循环count次======================================================
     * 	suiteName					String			套装名字
	 * 		num1					byte			套装中装备数量
	 * 		循环 num1 次
	 * 			 name				String			装备名称
	 * quality;     				int				品质
	 * desc							String			最高级套装效果描述信息
	 * ==================================================================
     */
    public static final short EQUIPMENT_SUITEITEMINFO_SERVER=2508;
    
    /***
     * 查看玩家身上可升级装备列表
     * serial 					int
     */
    public static final short VIEWCANLEVELUPEQUIP_CLIENT=2509;

    /***
     * 查看玩家身上可升级装备列表返回
     * serial 					int
     * count					int 玩家身上装备数量
     * GameItem					byte[]循环
     * count1					int 背包里装备数量
     * GameItem1				byte[]循环
     */
    public static final short VIEWCANLEVELUPEQUIP_SERVER=2510;
    
    /**
     * 360账号登陆
     * serial 					int
     * String 					authorityCode
     * String 					model;
	 * String 				 	uiModel;
	 * String  					clientVersion;
	 * String 				 	clientPhone;
	 * int 					 	playerId;
     */
    public static final short ACCOUNT_LOGIN_360_CLIENT=2511;

    /**
     * APP充值失败
     */
    public static final short APPSTORE_CHARGING_FAILED_CLIENT=2512;
    
    /**
     * APP充值失败返回(暂时没用)
     */
    public static final short APPSTORE_CHARGING_FAILED_SERVER=2513;
    
    /**
     * 新版通用联运账号登陆
     * serial					int
     * platform					string		例如UC、91
     * 用户uid					string
     * 用户sessionid				string
     * 机型						string
     * 版本号					string
     */
    public static final short PARTNER_ACCOUNT_LOGIN_CLIENT = 2514;
    
    
	/**
	 * 当前卡片信息请求
	 * serial                      int
	 * cardId                      int 卡片id
	 */
	public static final short CARD_INFO_NEW_CLIENT = 2515;
	
	
	/***
	 * 当前卡片信息请求返回
	 * serial					int
	 * nextLevelAttDesc			String 下级属性描述
	 * exp						int    升级所需经验（经验）
	 * totalExp					int    玩家当前卡片经验
	 */
	public static final short CARD_INFO_NEW_SERVER = 2516;
	
	/***
	 * 查看玩家当前卡片增加的力智体敏
	 * serial					int
	 */
    public static final short GETPLAYERPROP_CARDS_CLIENT=2517;
    
    /***
	 * 查看玩家当前卡片增加的力智体敏返回
	 * serial					int
	 * strength				    short			力
	 * agility					short			敏
	 * intellect				short			智
	 * stamina					short			体
	 */
    public static final short GETPLAYERPROP_CARDS_SERVER=2518;
    
    /***
	 * 端午押镖活动排行榜
	 * serial					int
	 */
    public static final short FESTIVAL_ESCORTBOARD_CLIENT=2519;
    
    /***
	 * 端午押镖活动排行榜返回
	 * serial					int
	 * size                     int
	 *   循环
	 *   rank				        byte			排名
	 *   faction					byte			国家
	 *   escortCount				int			    押镖数量
	 */
    public static final short FESTIVAL_ESCORTBOARD_SERVER=2520;
    
    /***
	 * 端午押镖请求
	 * serial					int
	 */
    public static final short FESTIVAL_ESCORT_CLIENT=2521;
    
    /***
	 * 端午押镖返回
	 * serial					int
	 */
    public static final short FESTIVAL_ESCORT_SERVER=2522;
    
    /**
     * 多酷游戏平台账号登录。
     * serial					int
     * 用户uin					string
     * 用户sessionid				string
     * 机型						string
     * 版本号					string
     * 手机号(可空)				string
     * 角色ID(可选)				int
     */
    public static final short ACCOUNT_LOGIN_DUOKU_CLIENT = 2523;
    /***
     * 获取已装备卡片列表（包括人物和坐骑）
     * serial					int
     */
    public static final short CARD_LIST_EQUIP_CLIENT=2525;
    
    /***
     * 获取已装备卡片列表返回（包括人物和坐骑）
     * serial						int
     * totalExp				int    玩家卡片经验
     * size							byte	装备位镶嵌卡片数量
     * 		null					byte    装备位是否镶有卡片（0否 1是)
     *      type					byte	卡片类型0-普通卡片 1-技能卡片
     *      type==0			
	 *			cardId					int		卡片ID
	 *			star					byte	星级
	 *			nextLevelDesc			String  下级卡片描述
	 *			UpGradeExp				int     升级所需经验
	 *		type==1
	 *			cardId					int		卡片ID
	 *			star					byte	星级
	 *			currentLevelDesc		String  卡片当前等级描述
	 *			nextLevelDesc			String  下级卡片描述
	 *			UpGradeCardCount		int		升级所需卡片数量
	 *			cardTotalCount			int		玩家所拥有此种卡片数量
	 *			preDesc					String	3,6,9,12卡片描述
	 *			upGradeNeedExp			int		升级所需经验
	 * size							byte	坐骑装备位镶嵌卡片数量
	 * 		null					byte    装备位是否镶有卡片（0否 1是)
	 * 		type					byte    卡片类型0-普通卡片 1-技能卡片
     *      type==0			
	 *			cardId					int		卡片ID
	 *			star					byte	星级
	 * 			nextLevelDesc			String  下级卡片描述
	 *			UpGradeExp				int     升级所需经验
	 *		type==1
	 *			cardId					int		卡片ID
	 *			star					byte	星级
	 *			currentLevelDesc		String  卡片当前等级描述
	 *			nextLevelDesc			String  下级卡片描述
	 *			UpGradeCardCount		int		升级所需卡片数量
	 *			cardTotalCount			int		玩家所拥有此种卡片数量
	 *			preDesc					String	3,6,9,12卡片描述
	 *			upGradeNeedExp			int		升级所需经验
     */
    public static final short CARD_LIST_EQUIP_SERVER=2526;
    
    
    /**
     * 获取10周年活动奖励信息
     * serial		int 
     */
    public static final short TENTHANNIVERSARY_INFO_CLIENT=2527;
    
    
    
    /**
     * 获取10周年活动奖励信息返回
     * serial					int
     * daySalaryValue			int  当日已获取工资
     * size						byte
     * dayInfo
     * 		daySalary3			short 是否已经完成当天的第一档工资数量 0未完成，1已完成，-1为已经领取过
     * 		daySalaryStep3		short 当天需要完成的第一档工资数量 
     * 		count				byte数量(循环count次)
     * 			name					String
     * 			desc					String
	 * 			templateId				int
	 * 			templateShowImage		byte
	 * 			showType				short
	 * 			quality					byte
	 * 			count2					byte   	 数量
     * 		daySalary30			short 是否已经完成当天的第二档工资数量 0未完成，1已完成，-1为已经领取过
     * 		daySalaryStep30		short 当天需要完成的第二档工资数量 
     * 		count				byte数量(循环count次)
     * 			name					String
     * 			desc					String
     * 			templateId				int
     * 			templateShowImage		byte
     * 			showType				short
     * 			quality					byte
     * 			count2					byte   	 数量
     * 		daySalary70			short 是否已经完成当天的第三档工资数量 0未完成，1已完成，-1为已经领取过
     * 		daySalaryStep70		short 当天需要完成的第三档工资数量 
     * 		count				byte数量(循环count次)
     * 			name					String
     * 			desc					String
     * 			templateId				int
     * 			templateShowImage		byte
     * 			showType				short
     * 			quality					byte
     * 			count2					byte   	 数量
     * size						byte
     * weekInfo
     * 		weekSalary3Count	short 已经完成本周的第一档的工资数量 >=0&&<weekSalaryStep3未完成，==weekSalaryStep3已完成，-1为已经领取过
     * 		weekSalaryStep3		short 本周需要完成的第一档工资数量 
     * 		count				byte数量(循环count次)
     * 			name					String
     * 			desc					String
     * 			templateId				int
     * 			templateShowImage		byte
     * 			showType				short
     * 			quality					byte
     * 			count2					byte   	 数量
     * 		weekSalary30Count	short 已经完成本周的第二档的工资数量 >=0&&<weekSalaryStep30未完成，==weekSalaryStep30已完成，-1为已经领取过
     * 		weekSalaryStep30	short 本周需要完成的第二档工资数量 
     * 		count				byte数量(循环count次)
     * 			name					String
     * 			desc					String
     * 			templateId				int
     * 			templateShowImage		byte
     * 			showType				short
     * 			quality					byte
     * 			count2					byte   	 数量
     * 		weekSalary70Count	short 已经完成本周的第三档的工资数量 >=0&&<weekSalaryStep70未完成，==weekSalaryStep70已完成，-1为已经领取过
     * 		weekSalaryStep70	short 本周需要完成的第三档工资数量 
     * 		count				byte数量(循环count次)
     * 			name					String
     * 			desc					String
     * 			templateId				int
     * 			templateShowImage		byte
     * 			showType				short
     * 			quality					byte
     * 			count2					byte   	 数量
     */
    public static final short TENTHANNIVERSARY_INFO_SERVER=2528;
    
    /**
     * 领取每天的10周年活动奖励请求
     * serial		int
     * dayOrWeek	byte 领取每天还是每周的奖励  1每天 2每周
     * type			byte 奖励类型1-3分奖励 2-30奖励  3-70分奖励
     */
    public static final short TENTHANNIVERSARY_GETEWARD_CLIENT=2529;
    
    /**
     * 领取每天的10周年活动奖励返回
     * serial 		int
     * dayOrWeek	byte 领取每天还是每周的奖励  1每天 2每周
	 * type			byte 0领取失败  1，2，3分别为对应奖励领取成功
     */
    public static final short TENTHANNIVERSARY_GETREWARD_SERVER=2530;
    
    /**
	 * 个人军团信息
	 * serial						int
	 * playerId                     int
	 */
	public static final short TONG_PLAYERINFO_CLIENT = 2531;
	
	/**
	 * 个人军团信息返回
	 * serial						int
	 * contribute                   int             自己当前贡献度
	 * lastLoginTime                int             上次登录时间
	 */	
	public static final short TONG_PLAYERINFO_SERVER = 2532;
	
	/**
	 * 异步战场：擂台战排行榜单请求
	 * serial					int
 	 * type						byte  0-普通列表  1-换一批列表
	 */
	public static final short ASYNCBATTLE_CHALLENGE_RANK_CLIENT = 2533;
	
	/**
	 * 异步战场：擂台战排行榜单返回
	 * serial					int
 	 * type						byte  0-普通列表  1-换一批列表
	 * size						byte			
	 * 		id					int				玩家ID
	 * 		name				String			名字
	 * 		faction				byte			国别
	 * 		sex					byte			性别
	 * 		clazz				byte			职业
	 * 		level				byte			级别
	 * 		headscore			int	`			头部image
	 * 		rank				int				排名
	 * size2
	 * 		id					int				玩家ID
	 * 		name				String			名字
 	 * 		faction				byte			国别
 	 * 		clazz				byte			职业
	 * 		role				byte			0为挑战者 1为被挑战者
	 * 		date				String			时间
	 * 		result`				byte			0为失败 1为胜利
	 * 		selfrank			int				本人当前名次
	 *		battleResult		byte			0排名不变,1升至，2降至，
	 * selfrank					int				本人排名
	 * leave					byte			本天剩余挑战次数
	 * leaveImoney				byte			本天剩余元宝挑战次数
	 * officerScore				int				积分
	 * needIMoney				byte			免费5次之后挑战所需元宝数
	 */
	public static final short ASYNCBATTLE_CHALLENAGE_RANK_SERVER = 2534;
	
	/**
	 * 异步战场：英雄榜榜单请求
	 * serial					int
	 */
	public static final short ASYNCBATTLE_HERO_RANK_CLIENT = 2535;
	
	/**
	 * 异步战场：英雄榜榜单请求返回
	 * serial					int
	 * size						byte			
	 * 		id					int				玩家ID
	 * 		name				String			名字
	 * 		faction				byte			国别
	 * 		clazz				byte			职业
	 * 		level				byte			级别
	 * selfrank					int				本人排名
	 */
	public static final short ASYNCBATTLE_HERO_RANK_SERVER = 2536;
	
	/**
	 * 异步战场：晋升最快榜单请求
	 * serial					int
	 */
	public static final short ASYNCBATTLE_HIGHSCORE_RANK_CLIENT = 2537;
	
	/**
	 * 异步战场：晋升最快榜单请求返回
	 * serial					int
	 * size						byte			
	 * 		id					int				玩家ID
	 * 		name				String			名字
	 * 		faction				byte			国别
	 * 		clazz				byte			职业
	 * 		level				byte			级别
	 * 		rank				int				晋升名次
	 * selfRank					int				自己的晋升名次
	 */
	public static final short ASYNCBATTLE_HIGHSCORE_RANK_SERVER = 2538;
	
	/**
	 * 异步战场:挑战
	 * serial					int
	 * type						byte			0表示擂台榜单中 1表示反击榜单中
	 * index					byte			榜单中的索引
	 */
	public static final short ASYNCBATTLE_CHALLENGE_CLIENT = 2539;
	
	/**
	 * 异步战场:挑战返回信息
	 * serial					int
	 */
	public static final short ASYNCBATTLE_CHALLENGE_SERVER = 2540;
	
	/**
	 * 异步战场：奖励信息请求
	 * serial					int
	 * type						byte 0-首进奖励 1-累计奖励
	 */
	public static final short ASYNCBATTLE_GETREWARDINFO_CLEINT = 2541;

	/**
	 * 异步战场：奖励信息返回
	 * serial							int
	 * type								byte 0-首进奖励 1-累计奖励
	 * size								byte 奖励数量
	 * ------------------------------------------------------------
	 * 		count						int 奖励物品种类数
	 * 			count2					byte   	 数量
	 * 			name					String
	 * 			desc					String
	 * 			templateId				int
	 * 			templateShowImage		byte
	 * 			showType				short
	 * 			quality					byte
	 * ------------------------------------------------------------
	 * 		title						String 奖励标题
	 * 		rewardDesc					String 奖励说明
	 * 		hadGetReward        		byte 是否已经领取 0未完成任务，1未领取，2已领取
	 *		haveDoneNumber				byte	已经完成的数量
	 *		totalNumber					byte    需要完成的数量
	 */
	public static final short ASYNCBATTLE_GETREWARDINFO_SERVER = 2542;
	
	/**
	 * 异步战场：获取奖励
	 * serial					int
	 * type						byte	0-首进  1-累计
	 * rewardType				byte	获取奖励类型  type==0  0,1,2,3;type==1  0,1,2
	 */
	public static final short ASYNCBATTLE_GETREWARD_CLIENT = 2543;

	/**
	 * 异步战场：获取奖励返回
	 * serial					int
	 * type						byte   0-首进  1-累计
	 * rewardType				byte   领取奖励的类型分别返回type==0  0,1,2,3;type==1  0,1,2
	 * alredayGetReward			byte   领取奖励是否成功    0-未完成 1-可领取 2-已领取
	 */
	public static final short ASYNCBATTLE_GETREWARD_SERVER = 2544;
	
	/**
	 * 异步战场：官职奖励信息
	 * serial 					int
	 */
	public static final short ASYNCBATTLE_JOBSREWARDINFO_CLIENT=2545;
	
	/**
	 * 异步战场：官职奖励信息返回
	 * serial					int
	 * currentJob				String 官职
	 * currentJobDesc			String 提供属性描述
	 * 		size				int 物品种类数量
	 * 			name					String
	 * 			desc					String
	 * 			templateId				int
	 * 			templateShowImage		byte
	 * 			showType				short
	 * 			quality					byte
	 * 			count2					byte   	 数量
	 * nextJobTitle				String 下一级官职
	 * nextJobDesc				String 下一级官职描述
	 * 		size				int 物品种类数量
	 * 			name					String
	 * 			desc					String
	 * 			templateId				int
	 * 			templateShowImage		byte
	 * 			showType				short
	 * 			quality					byte
	 * 			count2					byte   	 数量
	 * isGetReward				byte是否已经领取奖励0未，1已经领取
	 * currentJobScore					int 当前官职积分
	 * nextJobNeedScore					int	下级官职需要积分
	 */
	public static final short ASYNCBATTLE_JOBSREWARDINFO_SERVER=2546;
	
	/**
	 * 异步战场：获取官职每日奖励
	 * serial				int
	 */
	public static final short ASYNCBATTLE_GETJOBSREWARD_CLIENT=2547;
	
	/**
	 * 异步战场：获取官职每日奖励返回
	 * serial				int
	 * hadGetReward			byte   是否已经领取奖励 0未领取  1已经领取
	 */
	public static final short ASYNCBATTLE_GETJOBSREWARD_SERVER=2548;
	
	/**
	 * 获取物品描述
	 * serial						int
	 * 物品Id						int
	 * instanceId					int
	 */
	public static final short ITEM_REDUCEDESC_CLIENT = 2549;
	
	/**
	 * 物品描述
	 * serial						int
	 * 描述							string
	 */
	public static final short ITEM_REDUCEDESC_SERVER = 2550;
	
	/**
	 * 异步战场：官职升级
	 * serial				int
	 */
	public static final short ASYNCBATTLE_JOBSUPGRADE_CLIENT=2551;
	
	/**
	 * serial				int
	 * hadUpGrade			byte	升级成功 0失败  1成功
	 *     hadUpGrade==1
	 * 	   nextJobTitle				String 下一级官职
	 *     nextJobDesc				String 下一级官职描述
	 * 	   		size						int 物品种类数量
	 * 				name					String
	 * 				desc					String
	 * 				templateId				int
	 * 				templateShowImage		byte
	 * 				showType				short
	 * 				quality					byte
	 * 				count2					byte   	 数量
	 * currentJobScore					int 当前官职积分
	 * nextJobNeedScore					int	下级官职需要积分
	 * canGetReward						byte是否可领取奖励 0可领取  1已经领取
	 */
	public static final short ASYNCBATTLE_JOBSUPGRADE_SERVER=2552;
	
	/**
	 * 请求可领取奖励数量（包括官职和排名）
	 * serial			int
	 */
	public static final short ASYNCBATTLE_GETREWARDCOUNT_CLIENT=2553;
	
	/**
	 * 请求可领取奖励数量返回
	 * serial			int
	 * count			int数量
	 */
	public static final short ASYNCBATTLE_GETREWARDCOUNT_SERVER=2554;
	
	/**
	 * 背包切换日志请求
	 * serial			int
	 * type             String(0全部，1物品，2装备，3宝石，4任务)
	 */
	public static final short BAG_ENTRANCELOG_CLIENT=2555;
	
	/**
	 * 冷却组消失
	 * 数量									byte
	 * 循环n次
	 * 	冷却组ID								short
	 */
	public static final short ASYNC_COOLDOWN_END_SERVER = 2556;
	
	/**
	 * 玩家最近一次擂台战结果
	 * serial			int
	 */
	public static final short ASYNC_LASTASYNCBATTLERESULT_CLIENT=2557;
	
	/**
	 * 玩家最近一次擂台战结果返回
	 * serial			int
	 * result			byte 0失败 1胜利
	 * uprankNums		int 上升了多少名
	 * score			int 本次挑战获得的积分
	 * currentRank		int 当前名次
	 */
	public static final short ASYNC_LASTASYNCBATTLERESULT_SERVER=2558;
	
	/**
	 * 申请考试题目
	 * serial			int
	 */
	public static final short EXAM_QUESTION_REQUEST_CLIENT = 2559;
	
	/**
	 * 申请考试题目返回
	 * serial			int
	 * type				byte			(0为娱乐，1为乡试，2为会试，3为殿试)
	 * count			byte			题目数量
	 * 		id			int				题目ID
	 * 		title		String			题目描述
	 * 		num			byte			答案数量
	 * 			aid		byte			答案ID
	 * 			desc	String			答案描述
	 */
	public static final short EXAM_QUESTION_REQUEST_SERVER = 2560;
	
	/**
	 * 换一题
	 * serial			int
	 */
	public static final short EXAM_CHANGE_QUESTION_CLIENT = 2561;
	
	/**
	 * 换一题返回
	 * serial		int
	 * id			int				题目ID
	 * title		String			题目描述
	 * num			byte			答案数量
	 * 		aid		byte			答案ID
	 * 		desc	String			答案描述
	 */
	public static final short EXAM_CHANGE_QUESTION_SERVER = 2562;
	
	/**
	 * 去伪存真
	 * serial			int
	 * id				int			题目ID
	 */
	public static final short EXAM_REMOVE_CLIENT = 2563;
	
	/**
	 * 去伪存真返回
	 * serial			int
	 * aid1				byte
	 * aid2				byte
	 */
	public static final short EXAM_REMOVE_SERVER = 2564;
	
	/**
	 * 答题
	 * serial			int
	 * id				int			 题目ID
	 * aid				byte		 答案id
	 */
	public static final short EXAM_ANSWER_CLIENT = 2565;
	
	/**
	 * serial			int
	 * result			byte		(0为错误，1为正确)
	 * dayCount			byte		今日答题正确数量
	 * dayValue			byte		今日赢得答题分数
	 * weekValue		int			本周总分数
	 * score			String		当前排名
	 */
	public static final short EXAM_ANSWER_SERVER = 2566;
	
	/**
	 * 请求当前考试累计分数和排名
	 * serial			int
	 */
	public static final short EXAM_RESULT_CLIENT = 2567;
	
	/**
	 * 请求当前考试累计分数和排名返回
	 * serial			int
	 * value			int			分数
	 * score			String		排名
	 * canJoin			byte		0不可参加当前考试 1可参加当前考试
	 * err				String		不可参加的理由(canJoin=0时发送)
	 */
	public static final short EXAM_RESULT_SERVER = 2568;
	
	/**
	 * 考试排行榜单请求
	 * serial			int
	 */
	public static final short EXAM_BOARD_CLIENT = 2569;
	
	/**
	 * 考试排行榜单请求返回
	 * serial			int
	 * count			byte
	 * 		score			byte			排名
	 * 		faction			byte			国家
	 * 		name				String			玩家名字
	 * 		value			int				分数
	 * 		time				String				用时
	 */
	public static final short EXAM_BOARD_SERVER = 2570;
	
	/**
	 * 直接答对题目
	 * serial			int
	 * id				int				题目ID
	 */
	public static final short EXAM_REDICTPASS_CLIENT = 2571;
	
	/**
	 * 直接答对题目返回
	 * serial			int
	 * dayCount			byte		今日答题正确数量
	 * dayValue			byte		今日赢得答题分数
	 * weekValue		int			本周总分数
	 * score			String		当前排名
	 */
	public static final short EXAM_REDICTPASS_SERVER = 2572;
    
}
/**
类型定义

1	ITEM							
		物品Id						int
	 	物品名						string
	 	最大堆叠						byte
	 	物品显示类型+绑定类型			short(14+2) (1 不绑定 2 装备绑定 3 拾取绑定)
	 		红药水					1
			蓝药水					2									
			紫药水					3									
			状态类					4									
			生产类					5									
			精炼类					6									
			卷轴类					7									
			坐骑类					8									
			技能类					9									
			书籍类					10									
			杂物类					11
	 	使用等级						byte									
		品质							byte									
	 	价格							int	
	 	使用类型						byte
		是否能使用      	(0) (1位)
			战斗中使用		(1:非战斗状态使用 2:战斗状态使用) (2位)
	 		使用的目标类型   (3:对自己使用 4:对队友使用 5:对敌人使用) (3位) 
	 		使用是否消耗    	(6) (1位)
	 		是否任务物品     (7) (1位)
	 	使用生效时间					short	(如果不能使用此字段不存在)
	 	cd组							short	(如果不能使用此字段不存在)
	 	cd时间						int		(如果不能使用此字段不存在)
	 	使用距离						byte    (如果不能使用此字段不存在 单位：码)	
	 	使用次数						byte	(如果不能使用此字段不存在)
	 	使用对应职业					byte    (如果不能使用此字段不存在)
	 	使用提示						string  (如果不能使用此字段不存在)
	 	物品类型						byte	参见Item类的类型常量
	 	装备使用等级					byte    (如果不是装备字段不存在)
	 	装备职业限制					byte	 (如果不是装备字段不存在)
	 	装备部位						byte   	(如果不是装备字段不存在)
	 	装备力量限制					short	(如果不是装备字段不存在)
	 	装备敏捷限制					short	(如果不是装备字段不存在)
        初始镶孔数                   byte    (如果不是装备字段不存在)
        初始最大镶孔数               byte    (如果不是装备字段不存在)
        初始卡槽数					byte	（如果不是装备字段不存在）
        最大卡槽数					byte	（如果不是装备字段不存在）
		mask1						byte (0:最大hp 1:最大mp 2:力量 3:敏捷 4:耐力 5:智力 6:物理攻击力 7:魔法攻击力) 	(如果不是装备字段不存在)
		mask2						byte (0:物理防御 1:魔法防御 2:命中等级 3:闪避等级 4:暴击等级 5:法术闪避等级 6:生命恢复 7:魔法恢复) 	(如果不是装备字段不存在)
		mask3						byte (0:装甲 1:伤害下限 2:伤害上限 3:耐久 4:免暴 5: 随机附魔)	(如果不是装备字段不存在)
		循环N次								(如果不是装备字段不存在)	
			根据mask读取属性			short	(如果不是装备字段不存在)
	 	剩余次数						byte	(如果不能使用此字段不存在)
	 	到期时间		      			int
	 	绑定Id						int  	-1 没绑定 0 绑定玩家 其他数 绑定马的instanceId
		剩余耐久						short
		镶嵌信息长度                 short
		镶嵌信息                     DECORATION
	 	物品实例Id					int	
	 	是否有产地                   byte  0:没有(产地不用读)   1： 有
	 	产地							String 产地	
	 	限制装备类型｜是否可升级      byte  (高四位表示限制类型（0无限制，1仅人物可装备，2-仅随从可装备）低四位表示是否可升级0不能升级，1可以升级)
2	SKILL							
		技能组Id						short
		技能等级						byte
		技能名字						string
		技能攻击距离					short
		技能攻击时间					short
		技能cd组						short
		技能cd时间					short
		技能范围						byte
		技能类型						byte	//技能类型标志，按位划分(0:主动伤害1:主动辅助2:被动技能3:光环技能7:可装配技能)
		技能目标类型					byte	//技能目标类型，按位划分(0:目标阵营1:目标范围2:AOE中心)
		升级需要的点数				byte
		技能动画Id					int		//技能动画Id
		技能图标Id					int		//图标Id
		技能消耗mana					short
		技能可使用武器
		 数量						byte
		  循环n次
		  武器Id						byte
		是否有下一个级别				boolean
		下一个级别需要的等级			short
		技能最大级别					short
		技能职业						byte  (如果是4那么就是技能书技能)
3	GRID
        包格Id						byte
        物品数量						byte
        物品							ITEM	//如果数量为0则没有这个字段存在
4	SKILL_SIMPLE
         技能Id						byte
         技能level					byte
5	ACTOR
		id							int
		名字							string
		性别							byte
		等级							byte
		职业							byte
		阵营							byte
		最大Hp						short
		最大Mp						short
		当前Hp						short
		当前Mp						short
		力量							short
		敏捷							short
		耐力							short
		智力							short
		攻击上限						short
		攻击下限						short
		法术攻击力					short
		防御							short
		法术防御						short
		暴击*100						short
		法暴*100						short
		命中*100						short
		法术命中*100					short
		闪避*100						short
		法术闪避*100					short
		物理减伤						short
		每五秒回血					short
		每五秒回蓝					short
		剩余技能点					short
		剩余属性点					short
		当前经验						int
		到下一级需要的经验			int
		金钱							int
		地图Id						short
		地图InstanceId				int
		x							short
		y							short
		方向							short
		状态							short
		荣誉值						int
		荣誉							string

		帮派							string
		装备信息						byte[]
			循环8次
				装备数量					byte (0 此位置没有装备 1 有装备)
				装备信息					ITEM (如果装备数量的字段为0，没有此字段)
		头部装备分数					int
		身体装备分数					int
		武器装备分数					int
		装备发光等级					byte
		聊天信息						byte[]
			循环8次
				聊天频道定义				byte
			同乡信息						string
		冷却信息						byte[]
			冷却组数量					byte
			循环N次
				冷却组Id					byte
				冷却组开始时间			int
				冷却组到期时间			int
		buff信息						byte[]
			buff数量						byte
			循环n次
				buffId					int
				IconId					int
				到期时间					int
		声望							int
		称号							string
		官阶                        short
		锁定经验                    byte(0未锁定，1已锁定)
		包月数量                    int
		循环N次
		    itemId                          int
		    是否开启包月功能                 byte(0未开启，1已开启)
		玩家的工资数值               int
		size						byte	角色装备位镶嵌卡片数量
			null					byte	角色装备位是否镶有卡片（0否 1是）
			cardId					int		卡片ID
			cardName				String	卡片名字
			level					byte	卡片级别
			att						String	属性增强描述
		size						byte	坐骑装备位镶嵌卡片数量
			null					byte	坐骑装备位是否镶有卡片（0否 1是）
			cardId					int		卡片ID
			cardName				String	卡片名字
			level					byte	卡片级别
			att						String	属性增强信息
		enterMap                    byte    是否第一次进入指定地图(0是，1不是)
		attendantExp                int     玩家拥有的随从经验
6	EQUIP
    	装备部位						byte
    	 装备数量					byte (0 次位置没有装备 1 有装备)
    	 装备信息					ITEM (如果装备数量的字段为0，没有此字段)
7 	物品数量变化
	同步
		GRID
	显示
		物品Id						int
		物品instanceId				int
		物品数量						byte
8	BUFFS
		数量							byte
		循环n次
			BuffId					int
			buffIconId				int
			到期时间					int(如果是-1，那么代表是永久buff)
9 	TITLES
		当前TITLEID					short
		最后领取俸禄时间				long
		TITLES数量					short
		循环N次
			TITLEID					short
			名字						string
			俸禄						int
			阵营						byte(4为中立阵营)
			价钱						int
			描述						string
			等级						byte
10 	HORSEBAG
		坐骑包大小					byte
		当前坐骑数量					byte
		循环N次
			坐骑信息					HORSE

11	HORSE
		坐骑实例Id					int
		坐骑名字						string
		坐骑等级						byte
		坐骑经验						int
		升到下级需要的经验			int
		坐骑剩余点数					short
		坐骑最大饱食度				short
		坐骑当前饱食度				short
		召唤时间						int
		坐骑力量						short
		坐骑敏捷						short
		坐骑智力						short
		坐骑耐力						short
		坐骑速度						short
		坐骑装备分数					short
		坐骑ImageId					short
		坐骑IconId					short
		坐骑最大可学习技能数量		byte
		坐骑当前技能数量				byte
		循环N次
			技能信息					SKILL
		装备信息						
		是否装备口粮					boolean
		坐骑模板名称					String
		代理饲养						int
		状态							byte
		价钱                        String
		坐骑合成次数					byte
12  DECORATION
        打孔数                      byte
        扩展最大孔数                byte
        宝石数                      byte
            宝石孔位                byte
            宝石图标                byte
            宝石名称                String
            宝石属性                byte
            加属性值                short（最高4位用来表示宝石级别）
            宝石升级数				byte
        星级							byte
        资质数量						byte
        	资质等级					byte
        	资质属性					byte
        	资质属性值				short
        	资质附加百分比			byte
        装备刻字						String
        当前卡槽数					byte
        扩展最大卡槽数				byte
        镶嵌卡片数					byte
        	卡槽位					byte
        	卡片图标					byte
        	卡片名称					String
        	卡片效果描述				String
        	剩余时间(秒)				int
        	
13	NATIONSKILL(国家科技)
		ID							int
		名字							string
		等级							byte
		最高等级						byte
		科技类型						byte(第0位为1表示可以领取道具)
14 TONGSKILL(帮派科技)
		ID							int
		名字							string
		等级							byte
		最高等级						byte
		科技类型						byte(第0位为1表示可以领取道具)
15 ATTENDANTBAG
		MAXSIZE						byte			随从栏最大栏数
		size						byte			随从数量
		循环size次
			instanceId				int				随从instanceid
			name					String			随从名字
			attLevel                int             随从等级
			upExp                   int             升级所需经验值
			sex						byte			性别
			animateId				short			动画ID
			qulity					byte			随从品质(1-9品)
			qulityName				String			品质名称
			loyal					int				忠诚度
			maxLoyal				byte			最大忠诚度
    		hp						short			生命
    		maxHp					short			最大生命
    		mp						short			精力
    		maxMp					short			最大精力
    		armor					short			护甲
    		magicArmor				short			法防
    		weaponAP1				short			武器攻击下限
    		weaponAP2				short			武器攻击上限
    		critical				short			物理暴击
    		spellcritical			short			法术暴击
    		spellhit				short			法攻
    		dodge					short			物闪
    		spelldodge				short			法闪
    		decritical				short			免爆
    		hit						short			物理命中
    		spellhit				short			法术命中
			strength				short			力
			agility					short			敏
			intellect				short			智
			stamina					short			体
			skillSize				byte			技能size
			循环skillSize次
				canLight			byte			是否允许点亮	(0不允许、1允许)	
				light				byte			是否点亮（1点亮0未点亮）
				hasSkill			byte			是否有初始技能（1有0没有）
				SKILL				SKILL			技能信息
			EQUIP					EQUIP			装备信息
			hasSpecialSkill         byte            是否具有特殊技能(1有0没有)
			speicialSkill		    SKILL			特殊技能信息(hasSpecialSkill=1的时候下发)
*/
