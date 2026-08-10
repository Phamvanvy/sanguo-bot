package com.pip.rcp.itimes.admin.net;


public interface ServerConstants{
    /*世界服务器->认证服务器，连接服务器->世界服务器*/
    public static final byte SERVER_LOGIN = (byte)180;

    public static final byte SERVER_LOGIN_OK = (byte)181;

    public static final byte SERVER_LOGIN_FAIL = (byte)182;

    public static final byte SERVER_FORWARD = (byte)200;

    public static final byte FORCE_BROADCAST = (byte)183;
    public static final byte BROADCAST = (byte)184;
    public static final byte SYNC_CHANNEL = (byte)185;
    public static final byte SESSION_CLOSED = (byte)186; //由连接发往世界
    public static final byte LOGIN_RESULT = (byte)213;
    public static final byte CLEAR_CHANNELS = (byte)214;
    public static final byte GET_ACCOUNTNAME = (byte)215;
    public static final byte GET_ACCOUNTNAME_OK = (byte)215;

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

    public static final byte ADMIN_MAIL_GET_LIST = (byte)144;
    public static final byte ADMIN_MAIL_LIST = (byte)144;
    //        public static final byte ADMIN_MAIL_GET_CONTENT = (byte) 145;
    public static final byte ADMIN_MAIL_SEND = (byte)145;
    public static final byte ADMIN_MAIL_DELETE = (byte)146;
    public static final byte ADMIN_REQUEST_ISHOP_LIST = (byte)147;
    public static final byte ADMIN_ISHOP_LIST = (byte)147;
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

    public static final String SERVERID = "serverid";
    public static final String SERVERNAME = "servername";
    public static final String SERVERPASSWORD = "serverpassword";

}
