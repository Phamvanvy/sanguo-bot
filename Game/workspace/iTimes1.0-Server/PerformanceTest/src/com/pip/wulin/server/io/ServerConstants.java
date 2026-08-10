package com.pip.wulin.server.io;

/**
 * 服务器之间的协议常量。
 */
public interface ServerConstants {
    /** 登录管理服务器。连接服务器和世界服务器启动时需要向管理服务器发送此请求。 */
    public static final byte SERVER_LOGIN = 100;
    /** 用于SERVER_LOGIN命令的常量：世界服务器。 */
    public static final byte SERVERTYPE_WORLD = 10;
    /** 用于SERVER_LOGIN命令的常量：连接服务器。 */
    public static final byte SERVERTYPE_CONNECT = 11;

    /** 注册世界服务器地址。世界服务器启动时向管理服务器发送此命令注册。 */
    public static final byte REGISTER_WORLD = 101;
    /** 注册连接服务器地址。连接服务器启动时向管理服务器发送此命令注册。并且向世界服务器
     * 发送此请求注册 */
    public static final byte REGISTER_CONNECT = 102;
    /** 注册世界服务器成功。 */
    public static final byte REGISTER_WORLD_OK = 103;
    /** 注册连接服务器成功。此包应该包含世界服务器地址。 */
    public static final byte REGISTER_CONNECT_OK = 104;

    /** 取得连接服务器地址。此请求可以用于连接服务器负载均衡。 */
    public static final byte QUERY_ADDRESS = 105;
    /** 通知连接服务器地址。此包是QUERY_ADDRESS请求的返回。 */
    public static final byte NOTIFY_CONNECT_ADDR = 106;

    /** 向世界服务器通知用户登录。 */
    public static final byte LOGIN_USER = 107;
    /** 向世界服务器通知用户登出。 */
    public static final byte LOGOUT_USER = 108;
    /** 向连接服务器通知用户更换服务器了。 */
    public static final byte CLEAR_USER = 109;
    /** 发送聊天或通知消息。 */
    public static final byte SENDMESSAGE = 5;
    /** 无条件转发到客户端的消息，包括用户移动和用户场景切换等。 */
    public static final byte SERVER_FORWARD = 110;
    /** 从客户端发过来，转发给世界服务器处理的消息，包括PK消息等。 */
    public static final byte USER_FORWARD = 111;
    /** 试图向世界服务器登录用户。 */
    public static final byte USER_LOGIN_TRY = 112;
    /** 世界服务器返回试图登录结果。 */
    public static final byte USER_LOGIN_TRY_RET = 113;
    /** 通知世界服务器用户连接状况。 */
    public static final byte USER_CONNECTION_STATUS = 114;
    /** 通知连接服务器重载数据。 */
    public static final byte FORCE_RELOAD = 115;
    /** 通知世界服务器用户存盘数据改变。 */
    public static final byte PLAYER_DATA_CHANGED = 116;
    /** 通知连接服务器重载地区配置。*/
    public static final byte AREA_DATA_CHANGED = 117;
    /** 通知世界服务器某个收税任务完成。*/
    public static final byte TAX_TASK_FINISHED = 118;
    /**通知连接服务器同步用户信息*/
    public static final byte SYNC_USERDATA = 119;
    /** 服务器交换用户续费信息 */
    public static final byte BILLING_NOTIFY = (byte)190;
    /*发送PK擂台的报名请求*/
    public static final byte PKARENA_REG = (byte)191;
    /*擂台结束*/
    public static final byte PKARENA_END = (byte)192;
    public static final byte COMMAND = (byte)193;
    /*添加免费用户*/
    public static final byte ADD_FREEUSER = (byte)194;
    /*删除免费用户*/
    public static final byte DEL_FREEUSER = (byte)195;
    /*增加禁飞鸽用户*/
    public static final byte ADD_FROZE = (byte) 196;
    /**添加/删除被禁手机号*/
    public static final byte ADD_DEL_FORBIDDEN = (byte)197;
    /**帮派官员说话*/
    public static final byte TONG_OFFICER_MESSAGE = (byte)198;
    /** 重载机器人 */
    public static final byte RELOAD_ROBOT = (byte)199;
    /** 删除用户金钱通知，用于修改PK刷钱的BUG。*/
    public static final byte REDUCE_MONEY = (byte)200;

    // 上面两种转发消息都是在data[1]中放真实消息，data[0]中放本命令。USER_FORWARD命令
    // 还需要四个附加参数：用户名、用户ID、用户级别、用户阵营。

    /** 时间同步消息。 */
    public static final byte SYNCTIME = 90;

    /** 以下是GM命令。 */

    /** 取公告列表 */
    public static final byte GM_BBS_LIST = (byte)120;
    /** 取公告内容 */
    public static final byte GM_BBS_GETCONTENT = (byte)121;
    /** 删除公告 */
    public static final byte GM_BBS_DELETE = (byte)122;
    /** 发布公告 */
    public static final byte GM_BBS_POST = (byte)123;
    /** 修改公告 */
    public static final byte GM_BBS_UPDATE = (byte)124;
    /** 返回公告列表 */
    public static final byte GM_BBS_LIST_RET = (byte)126;
    /** 返回公告内容 */
    public static final byte GM_BBS_CONTENT = (byte)127;

    /** 取在线用户列表 */
    public static final byte GM_LIST_ONLINE = (byte)130;
    /** 强制下线 */
    public static final byte GM_KICK_USER = (byte)131;
    /** 瞬间移动用户 */
    public static final byte GM_MOVE_USER = (byte)132;
    /** 返回用户列表 */
    public static final byte GM_LIST_ONLINE_RET = (byte)133;
    /** 向用户发送客户端脚本 */
    public static final byte GM_SEND_SCRIPT = (byte)134;

    /** 取用户列表 */
    public static final byte GM_LIST_USER = (byte)140;
    /** 取用户资料（包括属性、物品） */
    public static final byte GM_GET_USER_DATA = (byte)141;
    /** 添加用户 */
    public static final byte GM_ADD_USER = (byte)142;
    /** 修改用户基本信息 */
    public static final byte GM_UDPATE_USER = (byte)143;
    /** 删除用户 */
    public static final byte GM_DELETE_USER = (byte)144;
    /** 修改角色属性 */
    public static final byte GM_UPDATE_USER_ATTR = (byte)145;
    /** 直接修改用户资料 */
    public static final byte GM_UPLOADDATA = (byte)189;
    /** 查看玩家游戏历史 */
    public static final byte GM_GET_USER_HISTORY = (byte)146;
    /** 添加物品 */
    public static final byte GM_USER_ADD_ITEM = (byte)148;
    /** 删除物品 */
    public static final byte GM_USER_DELETE_ITEM = (byte)149;
    /** 返回用户列表 */
    public static final byte GM_LIST_USER_RET = (byte)150;
    /** 返回用户资料 */
    public static final byte GM_USER_DATA = (byte)151;
    /** 返回用户游戏历史 */
    public static final byte GM_USER_HISTORY = (byte)152;
    /** 查询用户位置。*/
    public static final byte GM_LOCATE_USER = (byte)153;

    /** 查看擂台列表 */
    public static final byte GM_ARENA_LIST = (byte)160;
    /** 查看擂台信息 */
    public static final byte GM_ARENA_INFO = (byte)161;
    /** 修改擂台信息 */
    public static final byte GM_ARENA_UPDATE = (byte)162;
    /** 删除擂台 */
    public static final byte GM_ARENA_DELETE = (byte)163;
    /** 创建擂台 */
    public static final byte GM_ARENA_CREATE = (byte)164;
    /** 返回擂台列表 */
    public static final byte GM_ARENA_LIST_RET = (byte)165;
    /** 返回擂台信息 */
    public static final byte GM_ARENA_INFO_RET = (byte)166;

    /** 查看队伍列表 */
    public static final byte GM_TEAM_LIST = (byte)170;
    /** 强制离开队伍 */
    public static final byte GM_TEAM_LEAVE = (byte)172;
    /** 返回队伍列表 */
    public static final byte GM_TEAM_LIST_RET = (byte)173;

    /** 列出连接服务器名称 */
    public static final byte GM_LISTSERVER = (byte)177;
    /** 返回服务器列表 */
    public static final byte GM_SERVERLIST = (byte)178;
    /** 关闭服务器 */
    public static final byte GM_SHUTDOWN = (byte)179;
    /** 强制所有连接服务器重新载入游戏数据 */
    public static final byte GM_FORCE_RELOAD = (byte)180;
    /** 管理员登录 */
    public static final byte GM_LOGIN = (byte)181;
    /** 操作成功 */
    public static final byte GM_OK = (byte)182;
    /** 发送广播消息 */
    public static final byte GM_BROADCAST = (byte)183;
    /** 切换维护状态 */
    public static final byte GM_CHANGE_STATUS = (byte)184;
    /** GM消息通知 */
    public static final byte GM_NEWMESSAGE = (byte)185;
    /** 取GM消息列表 */
    public static final byte GM_GETMESSAGE = (byte)186;
    /** 返回GM消息 */
    public static final byte GM_MESSAGELIST = (byte)187;
    /** 删除GM消息 */
    public static final byte GM_DELETEMESSAGE = (byte)188;

    /** 添加真人擂台。 */
    public static final byte GM_PKARENA_ADD = (byte)190;
    /** 删除真人擂台。*/
    public static final byte GM_PKARENA_DELETE = (byte)191;
    /** 查询真人擂台列表。*/
    public static final byte GM_PKARENA_LIST = (byte)192;

    /** 扩展命令。*/
    public static final byte GM_EXTENSION = (byte)193;
    /** 扩展命令返回。 */
    public static final byte GM_EXTENSION_RET = (byte)194;
}
