package com.pip.server.auth.net;

/**
 * 认证服务器用到的协议常量。
 */
public interface AccountConstants {
	/**
	 * 通用错误
	 * appType			byte			错误包类型
	 * msg				String			错误信息
	 * 注：包序列号(sessionId)用于返回请求ID
	 */
    public static final byte ERROR = -1;

    /**
     * 注册帐号
     * requestId		int				请求ID
     * name				String			帐号名称(用户输入)
     * phone			String			手机号(用户输入)
     * recommend		String			推荐人(用户输入)
     * recommendId		int				推荐人帐号ID(-1表示未知)
     * model			String			机型(格式为:软件机型/JVM版本)
     * version			String			版本号(格式为:x.x.x-渠道代码)
     * charge			String[]		资费计划(废弃)
     * feeplan 			String			充值计划(废弃)
     * needReturn		boolean			是否直接激活
     * cmccUserId		String			平台用户ID(卓望版本才有)
     * cmccKey			String			平台用户Key(卓望版本才有)
     * gameCode			String			游戏区代码
     * realPhone        String          实际手机号（可空）
     */
    public static final byte ACCOUNT_REG = 1;
    /**
     * 注册帐号成功
     * requestId		int				请求ID
     * phone			String			手机号
     * password			String			密码(自动生成)
     * needReturn		boolean			是否直接激活
     * accountID        int             帐号ID
     */
    public static final byte ACCOUNT_REG_OK = 2;
    /**
     * 用户断线重连登录
     * requestId		int				请求ID
     * name				String			帐号名称
     * password			String			密码
     * cmccUserId		String			平台用户ID(卓望版本才有)
     * cmccKey			String			平台用户Key(卓望版本才有)
     */
    public static final byte RELOGIN = 9;
    /**
     * 重连登录成功
     * requestId		int				请求ID
     * result			byte			请求结果(固定为0成功)
     * id				int				帐号ID
     * name				String			帐号名称
     * password			String			密码
     * phone			String			手机号
     * mptimes			int				已修改密码次数
     * imoney			int				剩余i币(单位1/100i)/点数(单位1/100点)
     * reachFeeLimit	boolean			是否计时费用已达到月上限
     * subscribed		boolean			是否包月用户
     */
    public static final byte RELOGIN_RESULT = (byte)201;
    /**
     * 用户登录
     * requestId		int				请求ID
     * name				String			帐号名称
     * password			String			密码
     * cmccUserId		String			平台用户ID(卓望版本才有)
     * cmccKey			String			平台用户Key(卓望版本才有)
     * realPhone        String          实际手机号（可空）
     */
    public static final byte LOGIN = 77;
    /**
     * 用户登录成功
     * requestId		int				请求ID
     * id				int				帐号ID
     * name				String			帐号名称
     * password			String			密码
     * phone			String			手机号
     * mptimes			int				已修改密码次数
     * imoney			int				剩余i币(单位1/100i)/点数(单位1/100点)
     * reachFeeLimit	boolean			是否计时费用已达到月上限
     * subscribed		boolean			是否包月用户
     * errorTime		int				登录失败次数
     * region           String          用户所属地区（卓望版本才有）
     */
    public static final byte LOGIN_OK = 78;
    /**
     * 用户登录失败
     * requestId		int				请求ID
     * cause			String			错误信息
     */
    public static final byte LOGIN_RESULT = (byte)213;
    /**
     * 卓望版本游戏外充值
     * cmccUserId		String			平台用户ID
     * cmccKey			String			平台用户Key
     * amount			int				充值金额(元)
     * id				int				充值请求ID
     */
    public static final byte CMCC_CHARGE = 13;
    /**
     * 卓望版本充值
     * accountId		int				帐号ID
     * amount			int				充值金额(元)
     * id				int				充值请求ID
     */
    public static final byte CHARGEUP = (byte)210;
    /**
     * 卓望版本充值结果
     * id				int				充值请求ID
     * result			boolean			充值结果
     * balance			int				余额(单位1/100点)
     * msg				String			充值成功/失败消息
     */
    public static final byte CHARGEUP_RESULT = (byte)210;
    /**
     * 卓望版本查询充值/消费历史
     * requestId		int				请求ID
     * type				byte			1消费历史，2充值历史
     * accountId		int				帐号ID，如为-1表示可忽略此参数
     * startDate        String          起始日期
     * endDate          String          结束日期
     * startSeq         int             起始记录号，1表示第一条
     * pageSize         int             每页数据条数
     * timeType         int             查询时间类型，可选，0 - 当日，1 - 指定月，2 - 10天内
     * queryType        int             查询类型，可选，充值历史：0 - 全部；消费历史：0 - 查询所有客户端网游，1 - 查询所有WAP网游，2 - 查询自己
     * cmccUserId       String          卓望平台用户ID（如果accountId不为-1，此条可选）
     */
    public static final byte CMCC_GET_HISTORY = (byte)217;
    /**
     * 卓望版本返回充值/消费历史
     * requestId		int				请求ID
     * count			int				返回记录数量
     * 循环N次
     *   point			int				点数(单位1点)
     *   info			String			充值/消费信息
     */
    public static final byte CMCC_GET_HISTORY_OK = (byte)217;
    /**
     * 快速注册
     * requestId		int				请求ID
     * phone			String			手机号
     * version			String			版本号(格式为:x.x.x-渠道代码)
     * model			String			机型(格式为:软件机型/JVM版本)
     * cmccUserId		String			平台用户ID(卓望版本才有)
     * cmccKey			String			平台用户Key(卓望版本才有)
     * gameCode			String			游戏区代码
     * realPhone        String          实际手机号（可空）
     */
    public static final byte QUICK_REG = 30;
    /**
     * 快速注册成功
     * requestId		int				请求ID
     * id				int				帐号ID
     * name				String			帐号名称
     * password			String			密码
     * playerName		String			角色名称(废弃)
     * isNew			byte			0表示新创建，1表示找到旧帐号
     */
    public static final byte QUICK_REG_OK = 30;
    
    /**
     * 世界服务器登录
     * id				String			服务器ID
     * password			String			密码
     */
	public static final byte SERVER_LOGIN = (byte)180;
	/**
	 * 世界服务器登录成功
	 * 无参数
	 */
	public static final byte SERVER_LOGIN_OK = (byte)181;

	/**
	 * 根据帐号ID查询帐号名称
	 * requestId		int				请求ID
	 * accountId		int				帐号ID
	 */
    public static final byte GET_ACCOUNTNAME = (byte)215;
    /**
     * 查询帐号名称成功
     * requestId		int				请求ID
     * accountId		int				帐号ID
     * accountName		String			帐号名称
     */
    public static final byte GET_ACCOUNTNAME_OK = (byte)215;

    /**
     * 用户登出通知
     * accountId		int				帐号ID
     */
    public static final byte PLAYER_LOGOUT = (byte)188;

    /**
     * 关闭服务器
     * 无参数
     */
    public static final byte STOP = (byte)190;
    /**
     * 封停/解封帐号
     * type				byte			1为封停，2为解封
     * accountId		int				帐号ID
     * cause			String			封停原因(type为1是存在)
     */
    public static final byte FORBID = (byte)194;
    /**
     * 释放帐号(从缓存中移除)
     * id				int				帐号ID
     */
    public static final byte RELEASEACCOUNT = (byte)196;
    /**
     * 用户在线通知(定时刷新以保持帐号在线)，此包也用作世界断线后同步在线用户
     * id				int				帐号ID
     */
    public static final byte LIVE_NOTIFY = (byte)198;
    /**
     * 通知世界服务器强制用户下线
     * id				int				帐号ID
     */
    public static final byte FORCELOGOUT = (byte)199;
    /**
     * 卓望平台用户在线通知（定时刷新以保持帐号在线）
     * cmccUserId       String          卓望平台用户ID
     */
    public static final byte CMCC_LIVE_NOTIFY = (byte)219;

    /**
     * 请求扣时长费用(废弃)
     * accountId		int				帐号ID
     * fee				int				扣除i币(单位1/100i)
     * iMoney			int				剩余i币，如果和认证服务器不一致则需要同步(单位1/100i)
     */
    public static final byte FEE = (byte)202;
    /**
     * 扣时长费用失败
     * result			byte			固定为0
     * id				int				帐号ID
     * balance			int				余额(单位1/100i)
     */
    public static final byte FEE_RESULT = (byte)202;
    /**
     * 向世界服务器同步帐号余额
     * id				int				帐号ID
     * balance			int				余额(单位1/100i)
     * reachFeeLimit	boolean			是否计时费用已达到月上限
     * subscribed		boolean			是否包月用户
     */
    public static final byte SYNC_IMONEY = (byte)203;
    /**
     * 请求修改密码
     * accountId		int				帐号ID
     * playerId			int				请求修改密码的角色ID
     * old				String			旧密码
     * new1				String			新密码
     * new2				String			重复新密码
     */
    public static final byte MODIFY_PASSWORD = (byte)204;
    /**
     * 修改密码结果
     * result			byte			0成功，1失败
     * playerId			int				请求修改密码的角色ID
     * msg				String			新密码(成功)/错误信息(失败)
     */
    public static final byte MODIFY_PASSWORD_RESULT = (byte)204;

    /**
     * 修改注册手机号
     * accountId		int				帐号ID
     * playerId			int				请求修改的角色ID
     * phone			String			新手机号
     */
    public static final byte MODIFY_PHONE = (byte)206;
    /**
     * 修改注册手机号结果
     * result			byte			0成功，1失败
     * playerId			int				请求修改密码的角色ID
     * msg				String			新手机号(成功)/错误信息(失败)
     */
    public static final byte MODIFY_PHONE_RESULT = (byte)206;

    /**
     * 请求购买商品(扣费)
     * accountId		int				帐号ID，<0表示PIP版本访问卓望认证
     * cost				int				价格(单位1/100i)(pip版本才有)
     * consumeCode		String			计费代码(卓望版本才有)
     * requestId		int				请求ID
     * version          String          客户端版本号，格式为：2.2-CPIP1000-xxxxxxx
     * cmccUserId       String          卓望平台用户ID，当accountId<0时传入
     */
    public static final byte BUY = (byte)207;
    /**
     * 购买商品结果
     * requestId		int				请求ID
     * result			boolean			购买结果，true成功，false失败
     * balance			int				账户余额(单位1/100i)
     * cost				int				消耗i币(单位1/100i)(卓望版本总是-1)
     * msg				String			如果失败，返回错误信息
     */
    public static final byte BUY_RESULT = (byte)207;
    /**
     * 添加i币
     * accountId		int				账号ID
     * imoney			int				添加额(单位1/100i)
     * requestId		int				请求ID
     */
    public static final byte ADD_IMONEY = (byte)208;
    /**
     * 添加推荐奖励i币
     * accountId		int				帐号ID
     * imoney			int				添加额(单位1/100i)
     */
    public static final byte ADD_RECOMMEND_IMONEY = (byte)212;

    /**
     * GM工具查询帐号信息（返回信息也用这个）
     * accountId		int				帐号ID
     * accountName		String			帐号ID传-1时用于指定帐号名称
     * 返回：
     * accountId		int				帐号ID
     * accountName		String			帐号名称
     * password			String			密码
     * phone			String			注册手机号				
     */
    public static final byte ADMIN_ACCOUNTINFO = (byte)247;
    /**
     * GM工具修改密码
     * accountId		int				帐号ID
     * password			String			新密码
     */
    public static final byte ADMIN_MODIFYACCOUNT = (byte)249;

    /**
     * 修改帐号名称
     * accountId        int             帐号ID
     * playerId         int             修改帐号名称的角色ID
     * name             String          新名字
     */
    public static final byte MODIFY_ACCOUNT_NAME = (byte)214;
    /**
     * 修改帐号名称结果
     * result           byte            0成功，1失败
     * playerId         int             请求修改密码的角色ID
     * msg              String          新名称(成功)/错误信息(失败)
     */
    public static final byte MODIFY_ACCOUNT_NAME_RESULT = (byte)214;
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
     * requestId        int             请求ID（CMCC_SMS_BUY_REQ传入）
     * accountId        int             帐号ID
     * playerId         int             玩家ID
     * token            String          短信购买请求号
     */
    public static final byte CMCC_SMS_BUY_SUCC = (byte)218;
    /**
     * 卓望版本，用户推荐好友。
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * targetPhone      String          目标用户手机号
     * message          String          邀请标题
     * requestId        int             请求ID
     */
    public static final byte CMCC_RECOMMEND_REQUEST = (byte)220;
    /**
     * 卓望版本，推荐好友结果。
     * requestId        int             请求ID
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * targetPhone      String          目标用户手机号
     * result           boolean         true成功，false失败
     * message          String          成功/失败消息
     */
    public static final byte CMCC_RECOMMEND_RESULT = (byte)220;
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
     * 卓望版本，玩家升级通知。
     * userId           String          用户登录ID
     * accountId        int             帐号ID
     * playerId         int             用户ID
     * level            int             用户级别
     */
    public static final byte CMCC_LEVELUP_NOTIFY = (byte)223;
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
}

