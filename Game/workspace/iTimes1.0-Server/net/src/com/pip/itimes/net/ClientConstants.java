package com.pip.itimes.net;

public interface ClientConstants {

    public static final byte ERROR = -1;

    public static final byte ACCOUNT_REG = 1;
    public static final byte ACCOUNT_REG_OK = 2;
    public static final byte ACTOR_CREATE = 3;
    public static final byte ACTOR_CREATE_OK = 4;
    public static final byte ACTOR_GET_LIST = 5;
    public static final byte ACTOR_LIST = 6;
    public static final byte PLAYER_LOGIN = 7;
    public static final byte PLAYER_LOGIN_OK = 8;
    public static final byte RELOGIN = 9;
    public static final byte RELOGIN_RESULT = 10;
    public static final byte LOGIN = 77;
    public static final byte LOGIN_OK = 78;
    public static final byte GET_FILE = 11;
    public static final byte GET_FILE_OK = 11;
    public static final byte SEND_POSITION = 12;
    public static final byte CMCC_CHARGE = 13;
    public static final byte CMCC_CHARGE_OK = 13;
    public static final byte BBS_POST = 14;
    public static final byte BBS_POST_OK = 15;
    public static final byte BBS_GET_LIST = 16;
    public static final byte BBS_LIST = 16;
    public static final byte BBS_GET_CONTENT = 17;
    public static final byte BBS_CONTENT = 18;
    public static final byte REQUEST_FACE = 19;
    public static final byte FACE_LIST = 19;
    /**
     * 客户端申请自动寻路。
	 * <table border="2" cellspacing="0">
	 * <tr><th>方向</th><td colspan=3>世界服务器 → 客户端</td></tr>
	 * <tr><th>说明</th><td colspan=3>请求自动寻路</td></tr>
	 * <tr><th>命令</th><td>REQUEST_AUTO_TRACE</td><th>代码</th><td>20</td></tr>
	 * <tr><th>协议</th><td colspan=3>
	 * <table border="1"  cellspacing="0">
	 * <tr><th>序号</th><th>title</th><th>简介</th><th>类型</th><th>备注</th></tr>
	 * <tr><td align="right">1</td><th>mapid</th><td>目标场景ID</td><td>short</td><td> </td></tr>
	 * <tr><td align="right">2</td><th>x</th><td>目标场景X（像素）</td><td>short</td><td> </td></tr>
	 * <tr><td align="right">3</td><th>y</th><td>目标场景Y（像素）</td><td>short</td><td> </td></tr>
	 * </table>
	 * </td></tr>
	 * </table>
     */
    public static final byte REQUEST_AUTO_TRACE = 20;
    /**
     * 发送自动行路路线.
	 * <table border="2" cellspacing="0">
	 * <tr><th>方向</th><td colspan=3>世界服务器 → 客户端</td></tr>
	 * <tr><th>说明</th><td colspan=3>发送自动行路路线</td></tr>
	 * <tr><th>命令</th><td>CONN_SEND_WAYPOSITION</td><th>代码</th><td>20</td></tr>
	 * <tr><th>协议</th><td colspan=3>
	 * <table border="1"  cellspacing="0">
	 * <tr><th>序号</th><th>title</th><th>简介</th><th>类型</th><th>备注</th></tr>
	 * <tr><td align="right">1</td><th>count</th><td>路点数量</td><td>unsigned byte</td><td> </td></tr>
	 * <tr><td align="right" rowspan="2">重复count次</td><th>x</th><td>x坐标(像素)</td><td>short</td><td> </td></tr>
	 * <tr><th>y</th><td>y坐标(像素)</td><td>short</td><td> </td></tr>
	 * <tr><td align="right">3</td><th>toaction</th><td>到达后动作(0 - 停止，1 - 传送)</td><td>unsigned byte</td><td> </td></tr>
	 * <tr><td align="right">4</td><th>tomap</th><td>传送目标场景</td><td>short</td><td> </td></tr>
	 * <tr><td align="right">4</td><th>tox</th><td>传送目标X（格点）</td><td>short</td><td> </td></tr>
	 * <tr><td align="right">4</td><th>toy</th><td>传送目标Y（格点）</td><td>short</td><td> </td></tr>
	 * </table>
	 * </td></tr>
	 * </table>
     */
    public static final byte SEND_WAYPOSITION = 20;
    
    /**
     * 索要任务详细内容.
	任务id short
     */
    public static final byte REQUEST_TASKNEWINFO = 21;
    /**
     * 下发任务详细内容.
	任务序号 short
	任务名字 string
	任务等级 int
	目标点数量 byte
	  	循环
	目标点序号  int
	目标点名    string
	目标点地图id short
	目标点像素x   short
	目标点像素y   short
	目标点显示坐标x  short
	目标点显示坐标y  short
     */
    public static final byte SEND_TASKNEWINFO = 21;
    
    public static final byte TOUCH_NPC = 23;
    public static final byte MAIL_POST = 24;
    public static final byte MAIL_POST_OK = 24;
    public static final byte MAIL_GET_LIST = 25;
    public static final byte MAIL_LIST = 25;
    public static final byte MAIL_GET_CONTENT = 26;
    public static final byte MAIL_CONTENT = 26;
    public static final byte MAIL_GET_ATTACHMENT = 27;
    public static final byte MAIL_GET_ATTACHMENT_OK = 27;
    public static final byte MAIL_DELETE = 28;
    public static final byte MAIL_NEW = 29;
    public static final byte QUICK_REG = 30;
    public static final byte REQUEST_FRIENDS_LIST = 31;
//    public static final byte QUICK_REG_OK = 31;
    public static final byte USE_ITEM = 33;
    public static final byte BATTLE_RESULT = 34;
    public static final byte GOT_TASKITEM = 35;
    public static final byte ADD_PROPERTY_POINT = 36;
    public static final byte ADD_PROPERTY_POINT_OK = 36;
    public static final byte TEAM_CREATE = 38;
    public static final byte TEAM_CREATE_OK = 39;
    public static final byte TEAM_INVIT = 40;
    public static final byte TEAM_INVIT_RESULT = 41;
    public static final byte TEAM_JOIN_OK = 42;
    public static final byte TEAM_JOIN_FAIL = 43;
    public static final byte TEAM_LEAVE = 44;
    public static final byte BATTLE_REQUEST = 45;
    public static final byte BATTLE_INIT = 46;
    public static final byte BATTLE_JOIN = 47;
    public static final byte BATTLE_JOIN_RESULT = 48;
    public static final byte BATTLE_START = 49;
    public static final byte BATTLE_ABORT = 50;
    public static final byte BATTLE_FIGHT = 51;
    public static final byte BATTLE_ROUND_END = 52;
    public static final byte PLAYER_UPLOAD = 55;
    public static final byte PLAYRE_UPLOAD_OK = 56;
    public static final byte ISHOP_LIST = 57;
    public static final byte REQUEST_ISHOP_LIST = 57;
    public static final byte ISHOP_TRADE = 58;
    public static final byte ISHOP_TRADE_OK = 58;
    public static final byte SNEAK_ATTACK = 59;
    public static final byte PK_REQUEST = 60;
    public static final byte PK_CREATED = 61;
    public static final byte PK_REFUSE = 63;
    public static final byte PK_OK = 64;
    public static final byte PK_START = 65;
    public static final byte PK_FIGHT = 66;
    public static final byte PK_ROUND_END = 67;
    public static final byte GENERIC_LIST = 68;
    public static final byte GENERIC_LIST_CONTENT = 69;
    public static final byte EQU_CHANGED = 70;
    public static final byte EQU_CHANGED_OK = 71;
    public static final byte CHAT = 72;
    public static final byte CHAT_OPTION = 73;
    public static final byte GET_CHATFAVORITE_LIST = 74;
    public static final byte CHATFAVORITE_LIST = 74;
    public static final byte GET_CHATFAVORITE_DESC = 75;
    public static final byte CHAT_FAVORITE_DESC = 75;
    public static final byte CHANGE_CHATFAVORITE = 76;
    public static final byte SEG_402 = 79;
    public static final byte SEG_402_RESULT = 79;
    public static final byte REFRESH = 80;
    public static final byte COMMAND = 81;
    public static final byte MESSAGE = 82;
    public static final byte LOOK_EQU = 83;
    public static final byte LOOK_EQU_OK = 83;
    public static final byte REQUEST_ITEM_LINK = 84;
    public static final byte STORE_ITEM_LIST = 85;
    public static final byte STORE_TRADE = 86;
    public static final byte STORE_TRADE_OK = 86;
    public static final byte ADD_FRIEND = 87;
    public static final byte ADD_FRIEND_OK = 87;
    public static final byte REQUEST_TASK_DESC = 88;
    public static final byte TASK_DESC = 88;
    public static final byte ADD_POINT = 89;
    public static final byte ADD_POINT_OK = 89;
    public static final byte SYNC_TIME = 90;
    public static final byte HTTP_CLOSE = 91;
    public static final byte NOP = 92;
    public static final byte GET_ITEM = 94;
    public static final byte GATHER = 95;
    public static final byte GATHER_OK = 96;
    public static final byte GATHER_RESULT = 97;
    public static final byte LEARN_SKILL = 98;
    public static final byte LEAR_SKILL_OK = 99;
    public static final byte GET_SKILL_LIST = 100;
    public static final byte SKILL_LIST = 100;
    public static final byte GET_DESC = 101;
    public static final byte DESC = 102;
    public static final byte PRODUCT = 103;
    public static final byte ABILITY_LIST = 105;
    public static final byte LEARN_ABILITY = 106;
    public static final byte LEAR_ABILITY_OK = 107;
    public static final byte TASK_COMPLETED = 108;
    public static final byte SHOP_CREATE_OK = 110;
    public static final byte SHOP_LIST = 111;
    public static final byte REQUEST_SHOP_ITEM_LIST = 112;
    public static final byte SHOP_ITEM_LIST = 112;
    public static final byte SHOP_ADD_ITEM = 113;
    public static final byte SHOP_ADD_ITEM_OK = 113;
    public static final byte SHOP_REMOVE_ITEM = 114;
    public static final byte SHOP_REMOVE_ITEM_OK = 114;
    public static final byte SHOP_MONEY_CHANGE = 115;
    public static final byte SHOP_MONEY_CHANGE_OK = 115;
    public static final byte SHOP_CHANGE = 116;
    public static final byte SHOP_CHANGE_OK = 116;
    public static final byte AUCTION_TYPE_LIST = 117;
    public static final byte REQUEST_AUCTION_LIST = 118;
    public static final byte AUCTION_LIST = 118;
    public static final byte REQUEST_AUCTION_DESC = 119;
    public static final byte AUCTION_DESC = 119;
    public static final byte AUCTION_PRICE = 120;
    public static final byte AUCTION_PRICE_OK = 120;
    public static final byte AUCTION_ITEM = 121;
    public static final byte AUCTION_ITEM_OK = 121;
    public static final byte BUY_MAERIAL_TYPE_LIST = 122;
    public static final byte RQUEST_BUY_MATERIAL_LIST = 123;
    public static final byte BUY_MATERIAL_LIST = 123;
    public static final byte SELL_MATERIAL = 124;
    public static final byte SELL_MATERIAL_OK = 124;
    public static final byte OEM_TYPE_LIST = 125;
    public static final byte REQUEST_OEM_LIST = 126;
    public static final byte OEM_LIST = 126;
    public static final byte OEM = 127;
    public static final byte OEM_OK = 127;
    public static final byte TONG_CREATE_OK = (byte)130;
    public static final byte REQUEST_TONG_MEMBERS = (byte)131;
    public static final byte TONG_MEMBERS = (byte)131;
    public static final byte TONG_GRANT = (byte)132;
    public static final byte TONG_GRANT_OK = (byte)132;
    public static final byte TONG_MODIFY_TITLE = (byte)133;
    public static final byte FRIENDS_STATUS = (byte)134;
    public static final byte TASK_ABANDON = (byte)135;
    public static final byte TASK_ABANDON_RESULT = (byte)135;
    public static final byte ADD_PET_POINT = (byte)136;
    public static final byte ADD_PET_POINT_OK = (byte)136;
    public static final byte BUY_PET_POINT = (byte)137;
    public static final byte BUY_PET_POINT_OK = (byte)137;
    public static final byte USE_PET = (byte)138;
    public static final byte USE_PET_OK = (byte)138;
    public static final byte FEED = (byte)139;
    public static final byte DELETE_USER = (byte)140;
    public static final byte DELETE_USER_OK = (byte)140;
    public static final byte CHANGE_OPTION = (byte)141;
    public static final byte CHANGE_OPTION_OK = (byte)141;
    public static final byte REPAIRE_LIST = (byte)142;
    public static final byte REPAIRE = (byte)143;
    public static final byte REPAIRE_OK = (byte)143;
    
    //jwp add
    //public static final byte CONN_TASK_UI_VERSION = (byte)166;废弃的
   // public static final byte CONN_TASK_UI =(byte)167;废弃的
    //public static final byte CONN_TASK_UI_OK =(byte)167;废弃的
    //通知客户端取cmccuserId
    public static final byte CONN_CMCCUSERID = (byte)166;
    public static final byte CONN_NEWS = (byte)168;
    //jwp end
    
    // 新版本的下载文件版本控制
    
    /**
     * 下载文件命名规则。
     * mi/<id>.ps -> 怪物图标   
     * m<id>.ps -> 怪物图片
     * n<id>.ps -> NPC图片
     * *.ctn -> CTN文件
     * *.pip -> PIP文件
     * *.etf.gz -> UI脚本文件
     * 版本号编码规则：
     * 4字节整数，最高位是表示此文件是否优先保留在缓存中（内置），前3个字节其他23位表示文件大小，最后一个字
     * 节表示文件CRC（字节异或算法）。
     */
    
    /**
     * 客户端 -> 服务器
     * 检查客户端文件版本号是否过期。
     * version				String		客户端版本号
     * model				String		客户端机型
     * uimodel				String		客户端UI机型
     * filecount			int			文件个数
     * 以下循环
     * 		filename		String		文件名
     * 		fileversion		int			文件版本号
     */
    public static final byte CONN_CHECK_VERSION = (byte)169;
    /**
     * 服务器 -> 客户端
     * 客户端文件版本号过期检查结果。在所有内容更新包发送完以后发送这个包。
     * 一次典型的版本检查过程，会返回多个CONN_DOWNLOAD_FILE_OK包，最后带一个
     * CONN_CHECK_VERSION_OK包。
     */
    public static final byte CONN_CHECK_VERSION_OK = (byte)169;
    /**
     * 客户端 -> 服务器
     * 下载一个文件。
     * version				String		客户端版本号
     * model				String		客户端机型
     * uimodel				String		客户端UI机型
     * filename				String		文件名
     */
    public static final byte CONN_DOWNLOAD_FILE = (byte)170;
    /**
     * 服务器 -> 客户端
     * 下载文件成功。
     * filename				String		文件名
     * fileversion			int			新版本号
     * percent				int			本文件在整个下载进度中的百分比
     * hascontent			byte		是否带有内容
     * content				byte[]		如果hascontent为1，这里传送文件内容
     */
    public static final byte CONN_DOWNLOAD_FILE_OK = (byte)170;
    
    //mengjie add 1.5client add getEQU
    public static final byte EQU_GET_DETAILS = (byte)201;
    public static final byte EQU_GET_DETAILS_RESULT = (byte)201;
    public static final byte EQU_ENHANCE_DETAILS = (byte)202;
    public static final byte EQU_ENHANCE_DETAILS_RESULT = (byte)202;
    //END
    public static final byte BILLING_OK = (byte)205;
    public static final byte ECHO = (byte)177;

    //leo add
    public static final byte CMCC_HISTORY = (byte)217;
    public static final byte CMCC_HISTORY_OK = (byte)217;
    public static final byte REPORT_CLIENT_IP = (byte)220;
    public static final byte SEND_BILLING = (byte)152;
    
    public static final byte EXTEND_PROTOCOL = (byte)22;
    
    public static final short EXTEND_PROTOCOL_BOSS_DATA = (byte)101;
    public static final short EXTEND_PROTOCOL_BOSS_REFRESH = (byte)102;
    public static final short CONN_EXTEND_PET_TRADE =(byte)103;				// 打开宠物属性点兑换界面
    public static final short EXTEND_PROTOCOL_BUFF = (byte)104;             //查看buff
    public static final short EXTEND_PROTOCOL_LEARN_SKILL = (byte)105;             //新版本学习战斗技能升级技能点
    public static final short EXTEND_PROTOCOL_USESKILL = (byte)106;             //战斗技能使用频率
    public static final short EXTEND_PROTOCOL_PLAYERANIMATE = (byte)107;        //玩家动画播放
    public static final short EXTEND_PROTOCOL_LISTENUI= (byte)108;        		//用于脚本的相关ui监听
    public static final short EXTEND_PROTOCOL_UIHELP= (byte)109;        		//用于ui的辅助演示
    public static final short EXTEND_PROTOCOL_PETEQU_LOGIN= (byte)110;        		//用于登录时同步所有宠物的装备
    public static final short EXTEND_PROTOCOL_PETCHANGEDNAME= (byte)111;        		//用于宠物改名的新接口
    public static final short EXTEND_PROTOCOL_DIAMOND = 112;  //用于增加钻数
    public static final short EXTEND_PROTOCOL_WORLDMAP = 113;  //用于进入世界地图功能
    
    public static final short EXTEND_PROTOCL_GENERLIST = 114;              //用于取代通用列表中的 物品通用列表界面 类型保持不变（只改变向服务器返回命令的那种类型）
    public static final short EXTEND_PROTOCL_GETSKILL = 115;              //用于获取人物或者宠物的技能
    public static final short EXTEND_PROTOCL_GETATTR = 116;               //用于获取人物或者宠物的属性
    
    public static final short EXTEND_PROTOCOL_KEY9OPTION = 117;  //用于9键设置
    public static final short EXTEND_PROTOCOL_LETTERING = 118;  //装备刻字
    public static final short EXTEND_GETITEM_DETAILS = 119;  	//聊天里的物品明细查询
  
    public static final short EXTEND_PHONE = 120;  	//手机号获取
    
    public static final short EXTEND_BATTLE_REPLAY = 121;   //战斗回放
    public static final short EXTEND_BATTLE_SEED = 122;   //玩家补充协议下发   
	public static final short EXTEND_DIAMONDMOSAIC = 123;   //宝石镶嵌   
	
	public static final short EXTEND_QUICKLOGOCHANGE = 124;   //快速注册返回修改后的内容  
	public static final short EXTEND_QUICKLOGOOUT = 125;   //快速注册返回修改后,取消
	public static final short EXTEND_VOTECAMP = 126;   //阵营的竞选
	public static final short EXTEND_VOTE = 127;   //阵营领袖投票
	public static final short EXTEND_CAMPTECH = 128;   //阵营科技
	public static final short EXTEND_ROLEFACE = 129;   //人物的橱窗
	public static final short EXTEND_ROLETITLE = 130;   //人物的称号
	public static final short EXTEND_REQUIRERECIPES = 131;   //人物配方
	public static final short EXTEND_CREDIT_STORE = 132;	// 新版荣誉大厅
	public static final short EXTEND_OLD_LIFESKILL = 133;	// 旧生活技能通过NPC下发
	public static final short EXTEND_GEM_EFFECT = 134;		// 宝石特效动画
	public static final short EXTEND_STORE_EQUIP_COMPARE = 135; //商店装备对比时下发装备属性
	public static final short EXTEND_PET_SKILL_LOCK = 136;
	public static final short EXTEND_PET_RELATED_TRANSFORMATION = 137;	// 宠物改造相关
	public static final short EXTEND_ISHOP_EASY = 138;					//自动购买相关
	public static final short EXTEND_PET_SYNTHETIZE = 139;				// 宠物合成系统
	public static final short EXTEND_UNLINEEXP = 140;				// 离线经验
	public static final short EXTEND_MASTERANDAPP = 141;			// 师徒系统 
	public static final short EXTEND_ENCHANTING = 142;				// 分解和附魔
	public static final short EXTEND_SELECTFRIEND = 143;			// 情人节使用物品时选择异性好友
	public static final short EXTEND_SENDFRIEND = 144;				// 选择好友后发送奖品
	public static final short EXTEND_ITEMANIMATE = 145;				// 放爆竹的动画
	public static final short EXTEND_BOOK = 146;					// 指路宝典
	public static final short EXTEND_OPEN_UI = 147;					// 通知客户端打开一个UI
	public static final short EXTEND_SHOWPAO = 148;					// 显示新泡泡
	public static final short EXTEND_FACE = 149;					// 表情
	public static final short EXTEND_CAMPOFFICIAL = 150;			// 官员改变
	public static final short EXTEND_IMONEY_CARD_CLIENT = 151; 		//创建元宝卡
	public static final short EXTEND_CAMP_BATTLEFIELD_RELATED = 152;// 阵营战场相关
	public static final short EXTEND_INCAMP_BATTLEFIELD_BATTLEFIELD = 153;	// 战场副本中相关
	public static final short EXTEND_CONSUME_TOP = 154;	// 杀戮点数消费排行榜
	public static final short EXTEND_VIANY_ABOUT = 155;		// 属性攻相关
	public static final short EXTEND_MERCENARY = 156;		// 佣兵相关
	public static final short EXTEND_MAGIC_SHOP = 157;      // 魔法i币商城
	public static final short EXTEND_MAGIC_BUY = 158;       // i币商城购买
	public static final short EXTEND_LYRIC = 159;       // 点歌
	public static final short EXTEND_GETDISCOUNTMSG = 160;       // 获取打折信息
    public static final short EXTEND_TONGSHOP = 161;        //公会商店
    public static final short EXTEND_LOVESEND = 162;		//七夕情人节
    public static final short EXTEND_DISCOUNTSHOP = 163;   //折扣商店
    public static final short EXTEND_SENDFARM = 164;		//传送庄园
    public static final short EXTEND_BLOODSHOP = 165;		//吸血鬼商店
    public static final short EXTEND_PET_CHANGECOLOR = 166; //宠物变色
    public static final short EXTEND_BOSSRUSH = 167; //多层BOSS挑战 
    public static final short EXTEND_DIAMONDREPLACE = 168;//宝石置换
    public static final short EXTEND_RAWSTONEREPLACE = 169;//原石置换
    public static final short EXTEND_PET_DEBLOCK =	170;//宠物解锁
    public static final short EXTEND_DOWNLOAD_POINTS = 171;//下载积分相关 安卓版专用 
    public static final short EXTEND_EXP_REPLACE = 172;//修心系统经验兑换修心点
    public static final short EXTEND_VIP_DIMAOND_MIXTURE = 173;//vip一键合成任意等级宝石
    public static final short EXTEND_VIP_LEVEL_UP_BY_ONE_BUTTON = 174;//VIP一键升级功能  add by zx
    public static final short EXTEND_VIP_IDENTIFY_SEVENDIAMOND = 175;//vip鉴定7钻
    public static final short EXTEND_VIP_PLAYER_LEAVEPOINT = 176;
    public static final short EXTEND_DIAMOND_DEVELOP = 177;//宝石养成
    public static final short EXTEND_SEAL_MAGIC_POSITION = 178;//封印法阵    
    public static final short EXTEND_RABBIT_RACE = 179;//兔子赛跑  
    public static final short EXTEND_AWARD_BOX = 180;//花钱开宝箱
    public static final short EXTEND_HUNDRED_FLOOR_TOWER = 181;//花钱开宝箱
    public static final short EXTEND_PET_DEVELOP = 182;//宠物培养
    public static final short EXTEND_WORLDBOSS = 183;//世界BOSS
    public static final short EXTEND_PET_EVOLUTION = 184;//宠物进化
    public static final short EXTEND_NOAHSARK = 185; //诺亚方舟
    public static final short EXTEND_PET_EVOLUTION_TOP = 186;//宠物进化排行榜
    public static final short EXTEND_NOAHSARK_TICKET = 187; //诺亚方舟坐票
    public static final short EXTEND_PET_SKILL = 188; //圣宠技能
    
	//cmcc add mengjie
    public static final byte CONN_CHARGE_TIME = (byte) 218;//当前缺省查询日期。cmcc用
    //leo add end
    
    /**
     * Light添加：客户端需要模拟下载一次卓望客户端，才能进行话费购买。
     */
    public static final byte CMCC_EMU_DOWNLOAD = (byte)206;
    public static final byte CMCC_EMU_DOWNLOAD_OK = (byte)206;
}
