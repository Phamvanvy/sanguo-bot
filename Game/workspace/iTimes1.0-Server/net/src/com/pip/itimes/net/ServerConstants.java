package com.pip.itimes.net;

public interface ServerConstants {
	/*世界服务器->认证服务器，连接服务器->世界服务器*/
	public static final byte SERVER_LOGIN = (byte)180;

	public static final byte SERVER_LOGIN_OK = (byte)181;

	public static final byte SERVER_LOGIN_FAIL = (byte)182;

	public static final byte SERVER_FORWARD = (byte)200;

        public static final byte FORCE_BROADCAST = (byte)183;
        public static final byte BROADCAST = (byte)184;
        public static final byte SYNC_CHANNEL  = (byte)185;
        public static final byte SESSION_CLOSED = (byte)186;  //由连接发往世界
        public static final byte LOGIN_RESULT = (byte)213;
        public static final byte CLEAR_CHANNELS = (byte)214;
        public static final byte GET_ACCOUNTNAME = (byte)215;
        public static final byte GET_ACCOUNTNAME_OK = (byte)215;
        /**
         * 同步玩家的登录版本号协议
         */
        public static final byte  SYNC_PLAYER_DATAVESION= (byte)66;
        

        public static final byte SYNC_CHAT = (byte)187;

        public static final byte PLAYER_LOGOUT = (byte)188;

        public static final byte GATHER_OK = (byte)189;

        public static final byte STOP = (byte)190;
        public static final byte KICK = (byte)191;
        public static final byte SHUTDOWN = (byte)192;
        public static final byte RELOAD = (byte)193;
        public static final byte FORBID = (byte)194;
        public static final byte FINITERELOAD = (byte)195;
        public static final byte RELEASEACCOUNT = (byte)196;
        public static final byte MAXPLAYER = (byte)197;
        public static final byte LIVE_NOTIFY = (byte)198;
        public static final byte FORCELOGOUT = (byte)199;
        public static final byte CMCC_LIVE_NOTIFY = (byte)219;

        public static final byte RELOGIN_RESULT = (byte)201;
        public static final byte FEE = (byte)202;
        public static final byte FEE_RESULT = (byte)202;
        public static final byte SYNC_IMONEY = (byte)203;
        public static final byte MODIFY_PASSWORD = (byte)204;
        public static final byte MODIFY_PASSWORD_RESULT = (byte)204;
        public static final byte BILLING_OK = (byte)205; //已经被客户端占用，不可用

        public static final byte MODIFY_PHONE = (byte)206;
        public static final byte MODIFY_PHONE_RESULT = (byte)206;

        public static final byte BUY = (byte)207;
        public static final byte BUY_RESULT = (byte)207;
        public static final byte ADD_IMONEY = (byte)208;
        public static final byte MAINTANCE = (byte)209;
        public static final byte CHARGEUP = (byte)210;
        public static final byte CHARGEUP_RESULT = (byte)210;
        public static final byte ADD_ONLINE = (byte)211;
        public static final byte ADD_RECOMMEND_IMONEY = (byte)212;
        
        // Light: 下面的代码和GM协议重用部分代码
        /**
         * 卓望版本申请短信购买Token（16位数字）
         * accountId        int             帐号ID
         * playerId         int             请求购买的玩家ID
         * consumeCode      String          计费代码(卓望版本才有)
         * requestId        int             请求ID
         */
        public static final byte CMCC_SMS_BUY_REQ = (byte)216;
        /**
         * 卓望版本申请短信购买Token结果。
         * requestId        int             请求ID
         * result           boolean         true成功，false失败
         * accountId        int             帐号ID
         * playerId         int             玩家ID
         * token            String          短信购买请求号(成功)/错误信息(失败)
         */
        public static final byte CMCC_SMS_BUY_REQ_RESULT = (byte)216;
        /**
         * 卓望版本短信购买商品成功。
         * requestId        int             请求ID
         * accountId        int             帐号ID
         * playerId         int             玩家ID
         * token            String          短信购买请求号
         */
        public static final byte CMCC_SMS_BUY_SUCC = (byte)218;
        /**
         * 卓望版本，玩家升级通知。
         * userId           String          用户登录ID
         * accountId        int             帐号ID
         * playerId         int             用户ID
         * level            int             用户级别
         */
        public static final byte CMCC_LEVELUP_NOTIFY = (byte)223;

        //mengjie add
        public static final byte ADMIN_BBS_GETID = (byte)213;
        public static final byte ADMIN_BBS_GETBYUSER = (byte)214;
        public static final byte ADMIN_BBS_DELETEBYID = (byte)215;
        public static final byte ADMIN_BBS_DELETEBYUSERID = (byte)216;
        public static final byte ADMIN_MAIL_SENDENHANCE = (byte)217;
        //mengjie add end
        //jwp add
        public static final byte ADMIN_DELETE_ROLE = (byte)220;
        public static final byte ADMIN_RECOVER_ROLE_SHOW = (byte)221;
        public static final byte ADMIN_RECOVER_ROLE = (byte)222;
        public static final byte ADMIN_CAMP = (byte)223;
        
        //jwp end
        public static final byte ADMIN_CHAP_CONFIG = (byte)218;
        public static final byte ADMIN_MAIL_GET_LIST = (byte) 144;
        public static final byte ADMIN_MAIL_LIST = (byte) 144;
        public static final byte ADMIN_MAIL_MARK_REPLIED = (byte) 152;
        public static final byte ADMIN_MAIL_STATUS_CHANGE = (byte)156;
        public static final byte ADMIN_MAIL_SEND = (byte) 145;
        public static final byte ADMIN_MAIL_DELETE = (byte) 146;
        public static final byte ADMIN_REQUEST_ISHOP_LIST = (byte) 147;
        public static final byte ADMIN_ISHOP_LIST = (byte) 147;
        public static final byte ADMIN_ISHOP_MODIFY = (byte)148;
        public static final byte ADMIN_ISHOP_MODIFY_RESULT = (byte)148;
        public static final byte ADMIN_BBS_GET_LIST = (byte)149;
        public static final byte ADMIN_BBS_LIST = (byte)149;
        public static final byte ADMIN_BBS_SEND = (byte)150;
        public static final byte ADMIN_BBS_DELETE = (byte)151;

        public static final byte ADMIN_BATCH_COMMAND = (byte)229;
        public static final byte ADMIN_COMMAND = (byte)230;
        public static final byte ADMIN_LOGIN = (byte)231;
        public static final byte ADMIN_SHOW_PLAYER = (byte)232;
        public static final byte ADMIN_WHO = (byte)233;
        public static final byte ADMIN_KICK = (byte)234;
        public static final byte ADMIN_MUTE = (byte)235;
        public static final byte ADMIN_SAY = (byte)236;
        public static final byte ADMIN_MODIFY = (byte)237;
        public static final byte ADMIN_FORBID_ACCOUNT = (byte)238;
        public static final byte ADMIN_ADMIN = (byte)239;
        public static final byte ADMIN_ADD = (byte)240;
        public static final byte ADMIN_DELETE = (byte)241;
        public static final byte ADMIN_RELEASE_ACCOUNT = (byte)242;
        public static final byte ADMIN_ADDIP = (byte)243;
        public static final byte ADMIN_AUTH = (byte)244;
        public static final byte ADMIN_KEEPWATCH = (byte)245;
        public static final byte ADMIN_ACCOUNTINFO = (byte)247;
        public static final byte ADMIN_PLAYERLIST = (byte)248;
        public static final byte ADMIN_MODIFYACCOUNT = (byte)249;
        public static final byte ADMIN_FORBID = (byte)251;
        public static final byte ADMIN_SCRIPT = (byte)253;
        public static final byte ADMIN_BATTLEFIELD = (byte)254;
        public static final byte ADMIN_EXPADD = (byte) 252;
        public static final byte ADMIN_ACTIVITY = (byte) 179;
        public static final byte ADMIN_ACTIVITY_OK = (byte) 179;
        public static final byte ADMIN_IHOP_CREDITADD = (byte) 178;
        public static final byte ADMIN_BATH_EXPCREDITADD = (byte) 177;
        public static final byte ADMIN_UNLINE_EXP = (byte) 176;
        public static final byte ADMIN_ACKNOWLEDGEMENT = (byte) 175;
        public static final byte ADMIN_ISHOP_DISCOUNT = (byte) 174;
        public static final byte ADMIN_ADD_FARMMONEY = (byte) 173;
        public static final byte ADMIN_BATTLE_CLEAR = (byte) 172;
        
        public static final byte CMCC_GET_HISTORY = (byte)217;
	public static final byte CMCC_GET_HISTORY_OK = (byte)217;
	
	/**
     * 卓望版本，记录用户推荐好友信息。
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * targetPhone      String          目标用户手机号
     */
    public static final byte CMCC_RECOMMEND_REQUEST = (byte)220;
    public static final byte CMCC_RECOMMEND_REQUEST_OK = (byte)220;
    /**
     * 卓望版本，订购移动服务。
     * requestId        int             请求ID
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * subType          int             订购类型：1 开通彩铃，2 开通飞信，3 开通邮箱，4 开通手机报，5 开通G+游戏包
     */
    public static final byte CMCC_SUBSCRIBE = (byte)221;
    /**
     * 卓望版本，订购移动服务请求结果（请求成功 != 订购成功）。
     * requestId        int             请求ID
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * subType          int             订购类型：1 开通彩铃，2 开通飞信，3 开通邮箱，4 开通手机报，5 开通G+游戏包
     * result           boolean         true成功，false失败
     */
    public static final byte CMCC_SUBSCRIBE_RESULT = (byte)221;
    /**
     * 卓望版本，订购成功/失败通知。认证服务器向所有服务器广播。
     * userId           String          用户平台ID
     * subType          int             订购类型：1 开通彩铃，2 开通飞信，3 开通邮箱，4 开通手机报，5 开通G+游戏包
     * result           boolean         true成功，false失败
     */
    public static final byte CMCC_SUBSCRIBE_NOTIFY = (byte)222;
    
    /**
     * 查询成功推荐的玩家信息。
     * requestId        int             请求ID
     * userId           String          用户平台ID
     */
    public static final byte CMCC_QUERY_RECOMMEND = (byte)224;
    /**
     * 查询成功推荐的玩家信息结果。
     * requestId        int             请求ID
     * userId           String          用户平台ID
     * accounts         int[]           被推荐用户的注册帐号
     */    
	public static final byte CMCC_QUERY_RECOMMEND_RESULT = (byte)224;
    /**
     * 卓望版本，向用户发送短信通知。
     * userId           String          用户登录ID
     * message          String          通知消息
     */
    public static final byte CMCC_SEND_MESSAGE = (byte)225;
	public static final String SERVERID = "serverid";
	public static final String SERVERNAME = "servername";
	public static final String SERVERPASSWORD = "serverpassword";
	 /**
     * 查询用户是否通过卓望平台下载过客户端。
     * userId			String			用户ID
     * accountId		int				请求帐号ID
     * playerId			int 			请求角色ID
     */
    public static final byte CMCC_CHECK_DOWNLOAD = (byte)226;
    /**
     * 通知世界服务器用户需要通过卓望平台下载客户端。
     * userId			String			用户ID
     * accountId		int				帐号ID
     * playerId			int				角色ID
     * url				String			下载地址
     */
    public static final byte CMCC_PUSH_DOWNLOAD = (byte)226;
    /**
     * 通知用户已经下载一次客户端成功，以后不需要再下载了。
     * userId			String			用户ID
     */
    public static final byte CMCC_DOWNLOAD_OK = (byte)227;
}
