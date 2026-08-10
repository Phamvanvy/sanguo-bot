package com.pip.wulin.server.io;

/**
 * 客户端与服务器间的消息常量定义。
 */
public interface ClientConstants {
    /** 用户登录 */
    public static final byte LOGIN = 1;
    /** 登录成功 */
    public static final byte LOGINOK = 2;
    /** 用户重登录 */
    public static final byte RELOGIN = 69;
    /** 重登录结果 */
    public static final byte RELOGIN_RESULT = 70;

    /** 下载文件 */
    public static final byte GETFILE = 3;
    /** 下载文件类型：关卡 */
    public static final byte FILE_PKG = 1;
    /** 下载文件类型：任务 */
    public static final byte FILE_TASK = 2;
    /** 下载文件类型：精简关卡文件 */
    public static final byte FILE_PKGS = 3;
    /** 下载文件类型：任务描述 */
    public static final byte FILE_TASKDESC = 4;
    /** 下载成功 */
    public static final byte DOWNLAODOK = 4;
    /** 下载失败 */
    public static final byte DOWNLAODFAILD = 6;

    /** 发送聊天消息 */
    public static final byte CHATTING = 5;
    /** 通知用户移动 */
    public static final byte NOTIFYMOVE = 7;

    /** BBS发帖 */
    public static final byte BBSPOST = 8;
    /** BBS发帖成功 */
    public static final byte BBSPOSTOK = 12;
    /** BBS请求列表 */
    public static final byte BBSGETLIST = 9;
    /** BBS返回列表 */
    public static final byte BBSLIST = 10;
    /** BBS请求帖子 */
    public static final byte BBSGETITEM = 11;
    /** BBS帖子内容 */
    public static final byte BBSITEM = 13;

    /** 上传用户信息 */
    public static final byte UPDATEUSERDATA = 14;
    /** 下载用户信息 */
    public static final byte USERDATA_GET = 15;
    /** 用户信息下载成功 */
    public static final byte USERDATA_DOWN = 16;

    /** 用户注册 */
    public static final byte USERREG = 17;
    /** 用户注册成功，自动登录 */
    public static final byte USERREGOK = 18;
    /** 用户注册失败 */
    public static final byte USERREGFAILED = 19;

    /** 请求开始PK */
    public static final byte PK_REQUEST = 20;
    /** PK请求发送成功 */
    public static final byte PK_REQUEST_ESTABLISHED = 29;
    /** 撤销PK请求 */
    public static final byte PK_DISREQUEST = 21;
    /** 拒绝PK */
    public static final byte PK_NO = 22;
    /** 同意PK */
    public static final byte PK_OK = 23;
    /** PK回合开始 */
    public static final byte PK_ROUNDBEGIN = 24;
    /** 发送本回合动作 */
    public static final byte PK_ROUNDPLAYFIGHT = 25;
    /** 通知本回合结果 */
    public static final byte PK_ROUNDSCORE = 26;
    /** 通知PK胜利 */
    public static final byte PK_WINNER = 27;
    /** 通知PK失败 */
    public static final byte PK_FAILED = 28;

    /** 时间同步消息。 */
    public static final byte SYNCTIME = 90;
    /** 强制下线消息。 */
    public static final byte FORCEEXIT = 91;
    /** 更新客户端用户数据。 */
    public static final byte FORCEUPDATE = 92;
    /** 更新客户端用户数据的响应。 */
    public static final byte FORCEUPDATEACK = 93;
    /** 计费成功通知。 */
    public static final byte BILLINGOK = 94;
    /** 计费成功通知的响应。*/
    public static final byte BILLINGOK_ACK = 95;
    /** 好友上线/下线通知。 */
    public static final byte FRIEND_INFO = 96;
    /** 获取缩略图 */
    public static final byte GET_MAP = 97;
    /** 返回缩略图 */
    public static final byte MAP_INFO = 98;

    /** 拍卖出价 */
    public static final byte AUCTION_PRICE = 30;

    /** 创建队伍 */
    public static final byte TEAM_CREATE = 31;
    /** 创建队伍成功 */
    public static final byte TEAM_CREATEOK = 32;
    /** 请求加入队伍 */
    public static final byte TEAM_JOIN = 33;
    /** 拒绝/同意加入队伍 */
    public static final byte TEAM_JOINRESULT = 34;
    /** 离开队伍 */
    public static final byte TEAM_LEAVE = 35;
    /** 得到任务物品 */
    public static final byte TEAM_ADDTASKITEM = 36;

    /** 初始化组队战斗 */
    public static final byte TEAM_BATTLE = 41;
    /** 加入战斗 */
    public static final byte TEAM_BATTLE_JOIN = 42;
    /** 加入战斗结果 */
    public static final byte TEAM_BATTLE_JOIN_RESULT = 43;
    /** 战斗开始 */
    public static final byte TEAM_BATTLE_BEGIN = 44;
    /** 战斗中止 */
    public static final byte TEAM_BATTLE_STOP = 45;
    /** 发送战斗动作 */
    public static final byte TEAM_BATTLE_FIGHT = 46;
    /** 回合结束 */
    public static final byte TEAM_BATTLE_FIGHT_OVER = 47;

    /** 发送信件 */
    public static final byte SENDMAIL = 51;
    /** 发送信件成功 */
    public static final byte SENDMAILOK = 52;
    /** 取信件列表 */
    public static final byte GETMAILLIST = 53;
    /** 返回信件列表 */
    public static final byte MAILLIST = 54;
    /** 取信件内容 */
    public static final byte GETMAIL = 55;
    /** 返回信件内容 */
    public static final byte MAILCONTENT = 56;
    /** 提取附件 */
    public static final byte GETMAILATTACH = 57;
    /** 提取附件成功 */
    public static final byte GETMAILATTACHOK = 58;
    /** 删除信件 */
    public static final byte DELMAIL = 59;
    /** 删除信件成功 */
    public static final byte DELMAILOK = 60;
    /** 新信件通知 */
    public static final byte MAILNOTICE = 61;

    /** 取形象列表 */
    public static final byte GETFACELIST = 71;
    /** 返回形象列表 */
    public static final byte FACELIST = 72;
    /** 购买形象 */
    public static final byte CHANGEFACE = 73;
    /** 购买形象成功 */
    public static final byte CHANGEFACEOK = 74;

    /** 查看擂台排名 */
    public static final byte GETARENAINFO = 81;
    /** 擂台排名 */
    public static final byte ARENAINFO = 82;
    /** 提交套路 */
    public static final byte ARENASUBMIT = 83;
    /** 提交套路成功 */
    public static final byte ARENASUBMITOK = 84;

    public static final byte REQUESTITEM = 85;

    public static final byte REQUESTITEMOK = 86;
}
