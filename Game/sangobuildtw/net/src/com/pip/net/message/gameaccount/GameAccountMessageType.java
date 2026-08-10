package com.pip.net.message.gameaccount;

public class GameAccountMessageType {
	
	
	public static final short LEGACY_LOGIN = 501;
	public static final short LEGACY_LOGIN_OK = 502;
	public static final short ACCOUNT_REG = 503;
	public static final short ACCOUNT_REG_OK = 504;
	
	public static final short _LEGACY_LOGIN_OK = 505;
	
	public static final short FORCE_LOGOUT = 506;
	
	public static final short LEGACY_FEE = 507;
	public static final short LEGACY_CHARGEUP = 509;
	public static final short LEGACY_BUY = 511;
	public static final short LEGACY_BUY_RESULT = 512;
	public static final short LEGACY_BUY1 = 513;
	public static final short LEGACY_BUY1_RESULT = 514;
	public static final short LEGACY_FEE1 = 515;
	
	public static final short LEGACY_QUICKREG = 516;
	public static final short LEGACY_QUICKREG_RESULT = 517;
	public static final short _LEGACY_QUICKREG_RESULT = 518;
	public static final short LOGOUT1 = 519;
	public static final short SYNC_BALANCE = 520;
	public static final short MODIFY_PASSWORD = 521;
	public static final short MODIFY_PASSWORD_OK = 522;
	public static final short MODIFY_PHONE = 523;
	public static final short MODIFY_PHONE_OK = 524;
	public static final short ADD_RECOMMEND_BALANCE = 525;
	public static final short CHANGE_STATUS = 526;
	public static final short _CHANGE_STATUS = 527;
	public static final short ACCOUNT_INFO = 528;
	public static final short ACCOUNT_INFO_OK = 529;
	public static final short _ACCOUNT_INFO = 530;
	public static final short GET_ACCOUNTNAME = 531;
	public static final short GET_ACCOUNTNAME_OK = 532;
	public static final short LOGIN = 533;
	public static final short MODIFY_PASSWORD2 = 535;
	public static final short MODIFY_PASSWORD2_OK = 536;
	public static final short MODIFY_PHONE2 = 537;
	public static final short MODIFY_PHONE2_OK = 538;
	public static final short QUERY_BALANCE = 539;
	public static final short QUERY_BALANCE_OK = 540;
    public static final short ADD_RECOMMEND_BALANCE_OK = 541;
    public static final short ONLINE_TIME_NOTIFY = 542;
    public static final short CREDIT_CHANGE_NOTIFY = 543;
    public static final short RECOMMEND_REQUEST = 544;
    public static final short LEVEL_UP_NOTIFY = 545;
    public static final short RECOMMEND_REWARD_NOTIFY = 546;
    public static final short ADD_BALANCE = 547;
    public static final short ADD_BALANCE_OK = 548;
	public static final short PHONE_NOTIFY = 549;
	
	public static final short BUY = 550;
	public static final short BUY_OK = 551;
	public static final short DEC_BALANCE = 552;  //不登陆扣除i币
	public static final short DEC_BALANCE_OK = 553;
	
	
	public static final short _LOGOUT = 554;
	
	public static final short RESET_PASSWORD = 555;
	public static final short RESET_PASSWORD_OK = 556;
	
	public static final short DEC_BALANCE2 = 557;
	public static final short DEC_BALANCE2_OK = 558;
	
	public static final short RENAME = 559;
	public static final short RENAME_OK = 560;
	
	// 创建i币卡
	public static final short CREATE_IMONEY_CARD = 561;
	// 创建i币卡成功
	public static final short CREATE_IMONEY_CARD_OK = 562;
	// 兑换i币卡
	public static final short USE_IMONEY_CARD = 563;
	// 兑换i币卡成功
	public static final short USE_IMONEY_CARD_OK = 564;
	
	public static final short NUMERICAL_REG = 1001;
	public static final short NUMERICAL_REG_OK = 1002;
	
	public static final short PHONE_REG = 1003;  //专门为财付通做的接口，支持用手机当作用户名以及定制密码以及赠送的C币
	public static final short PHONE_REG_OK = 1004;
	
	public static final short QUERY_ABC_BALANCE = 1005;
	public static final short QUERY_ABC_BALANCE_OK = 1006;
	
	public static final short DEC_CBALANCE = 1007;
	public static final short DEC_CBALANCE_OK = 1008;
	
	public static final short PHONEACCOUNT_LOGIN = 1009;
	public static final short PHONEACCOUNT_LOGIN_OK = 1010;
	
	public static final short QQ_BILLING = 9000;
	
}
