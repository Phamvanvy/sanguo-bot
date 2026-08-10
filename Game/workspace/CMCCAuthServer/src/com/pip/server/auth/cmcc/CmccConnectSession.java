package com.pip.server.auth.cmcc;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.apache.mina.common.IoSession;

import com.pip.server.auth.AccountState;
import com.pip.server.auth.ConnectSession;
import com.pip.server.auth.Const;
import com.pip.server.auth.NoEnoughBalanceException;
import com.pip.server.auth.Server;
import com.pip.server.auth.bean.Account;
import com.pip.server.auth.bean.RecommendRecord;
import com.pip.server.auth.bean.RecommendRequest;
import com.pip.server.auth.bean.RewardHistory;
import com.pip.server.auth.bean.UserPhone;
import com.pip.server.auth.bean.UserRegion;
import com.pip.server.auth.dao.BaseDao;
import com.pip.server.auth.net.AccountConstants;
import com.pip.server.auth.net.UWAPException;
import com.pip.server.auth.net.UWAPData;
import com.pip.server.auth.net.UWAPSegment;
import com.pip.server.util.IDGenerator;
import com.pip.server.util.KeywordsUtil;
import com.pip.server.util.Utils;

import org.apache.log4j.Logger;

/**
 * 卓望版本世界服务器连接。
 */
public class CmccConnectSession extends ConnectSession {
    private static final Logger log = Logger.getLogger(CmccConnectSession.class);

    /*
     * 平台用户信息缓存
     */
    private CmccUserCache cache;
    /*
     * CMCC接口访问服务
     */
    private CmccService cmccService;

    public CmccConnectSession(IoSession session) {
        super(session);
    }

    public void setCmccUserCache(CmccUserCache cache) {
        this.cache = cache;
    }

    public void setCmccService(CmccService service) {
        this.cmccService = service;
    }

    /**
     * 卓望版本查询充值/消费历史
     * requestId        int             请求ID
     * type             byte            1消费历史，2充值历史
     * accountId        int             帐号ID，如为-1表示可忽略此参数
     * startDate        String          起始日期
     * endDate          String          结束日期
     * startSeq         int             起始记录号，1表示第一条
     * pageSize         int             每页数据条数
     * timeType         int             0 - 当日，1 - 指定月，2 - 10天内
     * queryType        int             充值历史：0 - 全部；消费历史：0 - 查询所有客户端网游，1 - 查询所有WAP网游，2 - 查询自己
     * cmccUserId       String          卓望平台用户ID
     * public static final byte CMCC_GET_HISTORY = (byte)217;
     * 卓望版本返回充值/消费历史
     * requestId        int             请求ID
     * count            int             返回记录数量
     * 循环N次
     *   point          int             点数(单位1点)
     *   date           String          时间
     * public static final byte CMCC_GET_HISTORY_OK = (byte)217;
     */
    protected void cmccGetHistory(UWAPData data) throws UWAPException {
        int requestId = -1;
        try {
            requestId = data.readInt();
            byte type = data.readByte();
            int accountId = data.readInt();
            String start = data.readString();
            String end = data.readString();
            int startSequence = data.readInt();
            int count = data.readInt();
            
            // 新版本的协议，接下来是2个int
            boolean newProtocol = false;
            int timeType = 0;
            int queryType = 0;
            try {
                timeType = data.readInt();
                queryType = data.readInt();
                newProtocol = true;
            } catch (Exception e) {
            }
            if (newProtocol && timeType == 1) {
                // 检查输入的月份是否合法
                try {
                    if (start.length() != 6) {
                        throw new Exception();
                    }
                    int year = Integer.parseInt(start.substring(0, 4));
                    int month = Integer.parseInt(start.substring(4));
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month - 1, 1);
                    
                    // 从2008年1月开始，最大当前月份
                    Calendar cal1 = Calendar.getInstance();
                    cal1.set(2008, Calendar.JANUARY, 1);
                    if (cal.getTimeInMillis() < cal1.getTimeInMillis() || 
                            cal.getTimeInMillis() > System.currentTimeMillis()) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    throw new CmccException("输入的月份错误");
                }
            }
            
            CmccUserKey userKey = null;
            if (accountId != -1) {
                // 查找帐号信息
                AccountState account = accountService.getAccount(accountId);
                if (account == null) {
                    throw new CmccException("帐号不存在或者帐号不处于活动状态");
                }
                userKey = cache.getUserKey(account.getCmccUserID());
            } else {
                String uid = data.readString();
                userKey = cache.getUserKey(uid);
            }
            
            // 检查是否已登录平台
            if (userKey == null) {
                throw new CmccException("未登录平台，请退出游戏重新进入");
            }
            cache.activeUserKey(userKey);
            
            if (type == 1) {
                // 查询消费记录
                CmccConsumeRecord record;
                if (newProtocol) {
                    record = cmccService.queryConsumeNew(userKey.getUserId(), start, end,
                        startSequence, count, timeType, queryType);
                } else {
                    record = cmccService.queryConsume(userKey.getUserId(), start, end,
                        startSequence, count);
                }
                UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_GET_HISTORY_OK);
                CmccConsumeItem[] items = record.getItems();
                seg.writeInt(requestId);
                seg.writeInt(items.length);
                for (int i = 0; i < items.length; i++) {
                    seg.writeInt(items[i].getPoint());
                    seg.writeString(items[i].getDisplayText());
                }
                write(seg);
            } else if (type == 2) {
                // 查询充值记录
                CmccChargeRecord record;
                if (newProtocol) {
                    record = cmccService.queryChargeNew(userKey.getUserId(), start, end,
                        startSequence, count, timeType, queryType);
                } else {
                    record = cmccService.queryCharge(userKey.getUserId(), start, end,
                        startSequence, count);
                }
                UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_GET_HISTORY_OK);
                CmccChargeItem[] items = record.getItems();
                seg.writeInt(requestId);
                seg.writeInt(items.length);
                for (int i = 0; i < items.length; i++) {
                    seg.writeInt(items[i].getPoint());
                    seg.writeString(items[i].getDate());
                }
                write(seg);
            }
        } catch (CmccException cme) {
            throw new UWAPException(cme.getMessage(), data.getSerial(), requestId, data.getAppType());
        } catch (Exception e) {
            log.error(e, e);
            throw new UWAPException("系统错误", data.getSerial(), requestId, data.getAppType());
        }
    }

    /**
     * 卓望版本游戏外充值
     * cmccUserId       String          平台用户ID
     * cmccKey          String          平台用户Key
     * amount           int             充值金额(元)
     * id               int             充值请求ID
     * public static final byte CMCC_CHARGE = 13;
     * 卓望版本充值结果
     * id               int             充值请求ID
     * result           boolean         充值结果
     * balance          int             余额(单位1/100点)
     * msg              String          充值成功/失败消息
     * public static final byte CHARGEUP_RESULT = (byte)210;
     */
    protected void cmccCharge(UWAPData data) throws UWAPException {
        int requestId = 0;
        try {
            String userId = data.readString();
            String key = data.readString();
            int charge = data.readInt();
            requestId = data.readInt();
            
            // 验证用户是否登录平台，必须登录后才能充值
            if (!cache.isValid(userId, key)) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.CHARGEUP_RESULT, data.getSerial(), data.getSessionId());
                seg.writeInt(requestId);
                seg.writeBoolean(true);
                seg.writeInt(0);
                seg.writeString("充值失败,不是合法的用户！");
                write(seg);
                return;
            }
            
            // 如果充值金额为0，则此请求用于查询余额
            int balance;
            if (charge != 0) {
                balance = cmccService.chargeUp(userId, charge);
            } else {
                balance = cmccService.queryBalance(userId);
            }
            UWAPSegment seg = new UWAPSegment(AccountConstants.CHARGEUP_RESULT, data.getSerial(), data.getSessionId());
            StringBuilder sb = new StringBuilder(70);
            if (charge != 0) {
                sb.append("恭喜您，充值" + charge + "元成功，您已获得" + (charge * 100) + "点点数！您目前的点数余额为" + balance + "点！");
            } else {
                sb.append("您帐户中的点数余额为");
                sb.append(balance);
                sb.append("点");
            }
            seg.writeInt(requestId);
            seg.writeBoolean(true);
            seg.writeInt(balance);
            seg.writeString(sb.toString());
            write(seg);
        } catch (CmccException ex) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.CHARGEUP_RESULT, data.getSerial(), 
                    data.getSessionId());
            seg.writeInt(requestId);
            seg.writeBoolean(true);
            seg.writeInt(0);
            seg.writeString(ex.getMessage());
            write(seg);
        } catch (Exception e) {
            log.error(e, e);
            UWAPSegment seg = new UWAPSegment(AccountConstants.CHARGEUP_RESULT, data.getSerial(), 
                    data.getSessionId());
            seg.writeInt(requestId);
            seg.writeBoolean(true);
            seg.writeInt(0);
            seg.writeString("系统错误");
            write(seg);
        }
    }

    /**
     * 卓望版本充值
     * accountId        int             帐号ID
     * amount           int             充值金额(元)
     * id               int             充值请求ID
     * public static final byte CHARGEUP = (byte)210;
     * 卓望版本充值结果
     * id               int             充值请求ID
     * result           boolean         充值结果
     * balance          int             余额(单位1/100点)
     * msg              String          充值成功/失败消息
     * public static final byte CHARGEUP_RESULT = (byte)210;
     */
    protected void chargeUp(UWAPData data) throws UWAPException {
        int id = 0;
        int accountId = 0;
        try {
            accountId = data.readInt();
            int value = data.readInt();
            id = data.readInt();
            
            // 查找帐号信息
            AccountState account = accountService.getAccount(accountId);
            if (account == null) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.CHARGEUP_RESULT);
                seg.writeInt(id);
                seg.writeBoolean(false);
                seg.writeInt(0);
                seg.writeString("帐号不存在或者帐号不处于活动状态");
                write(seg);
                log.info("AccountID[" + accountId + "] ChargeID[" + id + "] ChargeError NotFound");
                return;
            }
            
            // 检查是否已登录平台
            CmccUserKey userKey = cache.getUserKey(account.getCmccUserID());
            if (userKey == null) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.CHARGEUP_RESULT);
                seg.writeInt(id);
                seg.writeBoolean(false);
                seg.writeInt(0);
                seg.writeString("未登录平台，请退出游戏重新进入");
                write(seg);
                log.info("AccountID[" + accountId + "] ChargeID[" + id + "] ChargeError NotFound");
                return;
            }
            cache.activeUserKey(userKey);
            
            // 发起充值请求
            int imoney = cmccService.chargeUp(userKey.getUserId(), value);
            imoney *= 100;
            UWAPSegment seg = new UWAPSegment(AccountConstants.CHARGEUP_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(true);
            seg.writeInt(imoney);
            seg.writeString("恭喜您，您已成功充值" + value + "元，您的帐户将增加" + (value * 100) + "点。");
            write(seg);
            log.info("AccountID[" + accountId + "] ChargeID[" + id + "] OK");
        } catch (CmccException ex) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.CHARGEUP_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(false);
            seg.writeInt(0);
            seg.writeString(ex.getMessage());
            write(seg);
            log.info("AccountID[" + accountId + "] ChargeID[" + id + "] ChargeError");
        } catch (Exception e) {
            log.error(e, e);
            UWAPSegment seg = new UWAPSegment(AccountConstants.CHARGEUP_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(false);
            seg.writeInt(0);
            seg.writeString("系统错误");
            write(seg);
            log.info("AccountID[" + accountId + "] ChargeID[" + id + "] ChargeError");
        }
    }

    /**
     * 快速注册
     * requestId        int             请求ID
     * phone            String          手机号
     * version          String          版本号(格式为:x.x.x-渠道代码)
     * model            String          机型(格式为:软件机型/JVM版本)
     * cmccUserId       String          平台用户ID(卓望版本才有)
     * cmccKey          String          平台用户Key(卓望版本才有)
     * gameCode         String          游戏区代码
     * realPhone        String          实际手机号（可空）
     * public static final byte QUICK_REG = 30;
     * 快速注册成功
     * requestId        int             请求ID
     * id               int             帐号ID
     * name             String          帐号名称
     * password         String          密码
     * playerName       String          角色名称(废弃)
     * isNew            byte            0表示新创建，1表示找到旧帐号
     * public static final byte QUICK_REG_OK = 30;
     */
    protected void quickReg(UWAPData data) throws UWAPException {
        int requestId = -1;
        try {
            requestId = data.readInt();
            String phone = data.readString();       // 卓望版本这条记录无用
            String version = data.readString();
            String model = data.readString();
            String cmccUserId = data.readString().trim();
            String cmccKey = data.readString();
            String gameCode = data.readString();

            // 新协议可以带手机号
            String realPhone = "";
            try {
                realPhone = data.readString();
            } catch (Exception e) {
            }
            
            // 必须先登录平台才能继续
            log.info("CmccUserId[" + cmccUserId + "]CmccKey[" + cmccKey + "]Phone[" + realPhone + "]Version[" + version + "]Model[" + model + "]Try QuickReg");
            if (!cache.isValid(cmccUserId, cmccKey)) {
                throw new UWAPException("未登录平台，请退出游戏重新进入", data.getSerial(), requestId, data.getAppType());
            }

            // 记录手机号和伪码之间的关系
            recordUserPhone(cmccUserId, realPhone);
            
            // 查找此手机是否过去注册过用户，如果有，直接找回
            Account account = accountService.getFirstValidAccountByPhone(cmccUserId);
            if (account != null) {
                // 记录活跃手机号
                if (!cmccUserId.equals(account.getActivePhone())) {
                    account.setActivePhone(cmccUserId);
                    accountService.saveAccount(account);
                }
                
                UWAPSegment seg = new UWAPSegment(AccountConstants.QUICK_REG_OK, data.getSerial(), data.getSessionId());
                seg.writeInt(requestId);
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString("");
                seg.write((byte) 1); // 表示没有建立新的帐号，用的是原来的
                write(seg);
                return;
            }

            // 创建新游客帐号
            String name = null;
            name = QUICKREG_PREFIX + IDGenerator.getAccountName();
            String password = getPassword(RND);
            account = accountService.createNewAccount(name, password, "", cmccUserId, "", 0, "", true, version, 1, 1,
                    model, gameCode);
            if (account != null) {
                log.info("CmccUserId[" + cmccUserId + "]CmccKey[" + cmccKey + "]AccountName[" + account.getUserName() + "]Phone[" + realPhone + "]Version[" + version + "]Model[" + model + "]QuickRegOK");
                UWAPSegment seg = new UWAPSegment(AccountConstants.QUICK_REG_OK, data.getSerial(), requestId);
                seg.writeInt(requestId);
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString("");
                seg.write((byte) 0); // 表示建立了新帐号
                write(seg);
            } else {
                throw new UWAPException("创建帐号错误", data.getSerial(), requestId, data.getAppType());
            }
        } catch (UWAPException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(ex, ex);
            throw new UWAPException("创建帐号错误", data.getSerial(), requestId, data.getAppType());
        }
    }

    /**
     * 用户登录
     * requestId        int             请求ID
     * name             String          帐号名称
     * password         String          密码
     * cmccUserId       String          平台用户ID(卓望版本才有)
     * cmccKey          String          平台用户Key(卓望版本才有)
     * realPhone        String          实际手机号（可空）
     * public static final byte LOGIN = 77;
     * 用户登录成功
     * requestId        int             请求ID
     * id               int             帐号ID
     * name             String          帐号名称
     * password         String          密码
     * phone            String          手机号
     * mptimes          int             已修改密码次数
     * imoney           int             剩余i币(单位1/100i)/点数(单位1/100点)
     * reachFeeLimit    boolean         是否计时费用已达到月上限
     * subscribed       boolean         是否包月用户
     * errorTime        int             登录失败次数
     * public static final byte LOGIN_OK = 78;
     */
    protected void accountLogin(UWAPData data) throws UWAPException {
        int requestId = -1;
        try {
            requestId = data.readInt();
            String accountName = data.readString();
            String password = data.readString();
            String cmccUserId = data.readString().trim();
            String cmccKey = data.readString();
            
            // 新协议可带实际手机号
            String realPhone = "";
            try {
                realPhone = data.readString();
            } catch (Exception e) {
            }
            
            // 验证是否已经登录平台
            // 后门，允许test和gm用户不连接平台就登录
            CmccUserKey userKey = null;
            log.info("CmccUserId[" + cmccUserId + "]CmccKey[" + cmccKey + "]AccountName[" + accountName + "]Phone[" + realPhone + "]Try Login");
            if (!isTestAccount(accountName) && !isTestAccount(cmccUserId)) {
                if (!cache.isValid(cmccUserId, cmccKey)) {
                    loginError(requestId, "未登录平台，请退出游戏重新进入");
                    return;
                }
                userKey = cache.getUserKey(cmccUserId);
                
                // 记录手机号和伪码之间的关系
                recordUserPhone(cmccUserId, realPhone);
            }
            
            Account account = accountService.loadAccountByNameAndPassword(accountName, password);
            if (account == null) {
                log.info("Login Error Name[" + accountName + "]Pass[" + password + "]");
                loginError(requestId, "帐号名或者密码错误");
                return;
            }
            if (account != null && !account.getValid()) {
                loginError(requestId, "帐号已被封停");
                return;
            }
            int iMoney = 0;
            try {
                // 后门，允许test和gm用户不连接平台就登录
                if (isTestAccount(accountName) || isTestAccount(cmccUserId)) {
                    iMoney = 0;
                } else {
                    iMoney = cmccService.queryBalance(cmccUserId);
                }
                iMoney *= 100;
            } catch (CmccException ex) {
                loginError(requestId, ex.getMessage());
                return;
            }
    
            synchronized (accountService) {
                // 检查30秒内不允许重复登录
                AccountState a = accountService.getAccount(account.getId());
                long current = System.currentTimeMillis();
                if (a == null) {
                    a = new AccountState(account, current);
                }
                if ((current - a.getTime()) < 30000L && (current != a.getTime())) {
                    loginError(requestId, "帐号已经在使用中，请在30秒后重新尝试登陆");
                    return;
                }
                
                // 如果已经有一个登录会话，强制其退出
                ConnectSession oldSession = a.getSession();
                if (oldSession != null) {
                    UWAPSegment seg = new UWAPSegment(AccountConstants.FORCELOGOUT);
                    seg.writeInt(a.getId());
                    oldSession.write(seg);
                }
    
                // 注册此新登录会话
                a.setSession(this);
                a.setTime(current);
                a.setLastLiveTime(current);
                a.setCmccUserID(cmccUserId);
                accountService.registry(a);
                registry(a);
                cache.registerLoginInfo(account.getId(), cmccUserId);
                
                // 记录用户最近活跃手机
                if (!cmccUserId.equals(a.getAccount().getActivePhone())) {
                    a.getAccount().setActivePhone(cmccUserId);
                    accountService.saveAccount(a.getAccount());
                }
                
                // 回发登录成功包
                UWAPSegment seg = new UWAPSegment(AccountConstants.LOGIN_OK, data.getSerial(), data.getSessionId());
                seg.writeInt(requestId);
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString(account.getPhone());
                seg.writeInt(account.getModifyPasswordTimes());
                seg.writeInt(iMoney);
                if (account.getMonthFee() >= Const.MONTH_MAX) {
                    seg.writeBoolean(true);
                } else {
                    seg.writeBoolean(false);
                }
                if (account.getSubscribeStatus() == Account.SUBSCRIBED) {
                    seg.writeBoolean(true);
                } else {
                    seg.writeBoolean(false);
                }
                seg.writeInt(0);
                if (userKey == null) {
                    seg.writeString("");
                } else {
                    seg.writeString(userKey.getRegion());
                }
                write(seg);
                log.info("CmccUserId[" + cmccUserId + "]CmccKey[" + cmccKey + "]AccountID[" + account.getId() + "]AccountName[" + account.getUserName() + "]Phone[" + realPhone + "]Logined");
            }
        } catch (Exception e) {
            log.error(e, e);
            loginError(requestId, "系统错误");
        }
    }

    /*
     * 发送登录错误包。
     */
    protected void loginError(int requestId, String cause) {
        UWAPSegment seg = new UWAPSegment(AccountConstants.LOGIN_RESULT);
        seg.writeInt(requestId);
        seg.writeString(cause);
        write(seg);
    }

    /**
     * 注册帐号
     * requestId        int             请求ID
     * name             String          帐号名称(用户输入)
     * phone            String          手机号(用户输入)
     * recommend        String          推荐人(用户输入)
     * recommendId      int             推荐人帐号ID(-1表示未知)
     * model            String          机型(格式为:软件机型/JVM版本)
     * version          String          版本号(格式为:x.x.x-渠道代码)
     * charge           String[]        资费计划(废弃)
     * feeplan          String          充值计划(废弃)
     * needReturn       boolean         是否直接激活
     * cmccUserId       String          平台用户ID(卓望版本才有)
     * cmccKey          String          平台用户Key(卓望版本才有)
     * gameCode         String          游戏区代码
     * realPhone        String          实际手机号（可空）
     * public static final byte ACCOUNT_REG = 1;
     * 注册帐号成功
     * requestId        int             请求ID
     * phone            String          手机号
     * password         String          密码(自动生成)
     * needReturn       boolean         是否直接激活
     * public static final byte ACCOUNT_REG_OK = 2;
     */
    protected void playerRegister(UWAPData data) throws UWAPException {
        int requestId = -1;
        try {
            requestId = data.readInt();
            String name = data.readString();
            String phone = data.readString();  // 卓望版本此字段无用
            String recommend = data.readString();
            int recommendAccountId = data.readInt();
            String model = data.readString(); // 机器型号
            String version = data.readString();
            String[] charge = data.readStrings();
            String feeplan = data.readString();
            boolean needReturn = data.readBoolean();
            String cmccUserId = data.readString().trim();
            String cmccKey = data.readString();
            String gameCode = data.readString();
            
            // 新协议可以带手机号
            String realPhone = "";
            try {
                realPhone = data.readString();
            } catch (Exception e) {
            }
            
            // 必须先登录平台才能继续
            log.info("CmccUserId[" + cmccUserId + "]CmccKey[" + cmccKey + "]AccountName[" + name + "]Phone[" + realPhone + "]Version[" + version + "]Model[" + model + "]Try Register");
            if (!cache.isValid(cmccUserId, cmccKey)) {
                throw new UWAPException("注册失败", data.getSerial(), requestId, data.getAppType());
            }

            // 记录手机号和伪码之间的关系
            recordUserPhone(cmccUserId, realPhone);
            
            // 检查名字是否合法
            name = name.trim();
            if (name.length() == 0)
                throw new UWAPException("帐号名不能为空", data.getSerial(), requestId, data.getAppType());
            if (name.getBytes("GBK").length > 16)
                throw new UWAPException("帐号名太长", data.getSerial(), requestId, data.getAppType());
            if (KeywordsUtil.isInvalidName(name.toLowerCase()))
                throw new UWAPException("帐号名出现非法字符", data.getSerial(), requestId, data.getAppType());
            if (!Utils.checkString(name, false))
                throw new UWAPException("帐号名出现非法字符", data.getSerial(), requestId, data.getAppType());
            String newName = KeywordsUtil.filterKeywords(name);
            if (!newName.equals(name))
                throw new UWAPException("帐号名出现非法字符", data.getSerial(), requestId, data.getAppType());
            
            // 检查这个名字是否已经注册过了
            Account account = accountService.loadAccountByName(name);
            if (account != null) {
                if (account.getPhone().equals(cmccUserId)) {
                    throw new UWAPException("您已经注册过此帐号，请联系客服找回：010-64465123。", data.getSerial(), requestId, 
                            data.getAppType());
                } else
                    throw new UWAPException("已经存在同名帐号", data.getSerial(), requestId, data.getAppType());
            }
            
            // 随机生成密码
            String password = getPassword(RND);
            account = accountService.createNewAccount(name, password, "", cmccUserId, recommend, 0, "", true,
                    version, 0, 0, model, gameCode);
            if (account != null) {
                log.info("CmccUserId[" + cmccUserId + "]CmccKey[" + cmccKey + "]AccountName[" + account.getUserName() + "]Phone[" + realPhone + "]Version[" + version + "]Model[" + model + "]RegOK");
                UWAPSegment seg = new UWAPSegment(AccountConstants.ACCOUNT_REG_OK, data.getSerial(), data
                        .getSessionId());
                seg.writeInt(requestId);
                seg.writeString(phone);
                seg.writeString(password);
                seg.writeBoolean(needReturn);
                seg.writeInt(account.getId());
                write(seg);
            } else {
                throw new UWAPException("创建帐号错误", data.getSerial(), requestId, data.getAppType());
            }
        } catch (UWAPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UWAPException("创建帐号错误", data.getSerial(), requestId, data.getAppType());
        }
    }

    /**
     * 用户断线重连登录
     * requestId        int             请求ID
     * name             String          帐号名称
     * password         String          密码
     * cmccUserId       String          平台用户ID(卓望版本才有)
     * cmccKey          String          平台用户Key(卓望版本才有)
     * public static final byte RELOGIN = 9;
     * 重连登录成功
     * requestId        int             请求ID
     * result           byte            请求结果(固定为0成功)
     * id               int             帐号ID
     * name             String          帐号名称
     * password         String          密码
     * phone            String          手机号
     * mptimes          int             已修改密码次数
     * imoney           int             剩余i币(单位1/100i)/点数(单位1/100点)
     * reachFeeLimit    boolean         是否计时费用已达到月上限
     * subscribed       boolean         是否包月用户
     * public static final byte RELOGIN_RESULT = (byte)201;
     */
    protected void relogin(UWAPData data) throws UWAPException {
        try {
            int requestId = data.readInt();
            String accountName = data.readString();
            String password = data.readString();
            String cmccUserId = data.readString().trim();
            String cmccKey = data.readString();
            
            // 验证是否已经登录平台
            // 后门，允许test和gm用户不连接平台就登录
            if (!isTestAccount(accountName) && !isTestAccount(cmccUserId)) {
                if (!cache.isValid(cmccUserId, cmccKey)) {
                    // 重登录错误，不发返回包了
                    return;
                }
            }
            
            // 查询余额
            int iMoney = 0;
            try {
                if (isTestAccount(accountName) || isTestAccount(cmccUserId)) {
                    iMoney = 0;
                } else {
                    iMoney = cmccService.queryBalance(cmccUserId);
                }
                iMoney *= 100;
            } catch (CmccException ex) {
                // 重登录错误，不发返回包了
                return;
            }
            
            // 查找帐号
            Account account = accountService.loadAccountByNameAndPassword(accountName, password);
            if (account == null) {
                // 重登录错误，不发返回包了
                return;
            }
            synchronized (accountService) {
                // 30秒内不允许重复登录
                AccountState a = accountService.getAccount(account.getId());
                long current = System.currentTimeMillis();
                if (a == null) {
                    a = new AccountState(account, current);
                }
                if ((current - a.getTime()) < 30000L && (current != a.getTime())) {
                    // 重登录错误，不发返回包了
                    return;
                }
                
                // 如果已有登录会话，强行下线
                ConnectSession oldSession = a.getSession();
                if (oldSession != null) {
                    UWAPSegment seg = new UWAPSegment(AccountConstants.FORCELOGOUT);
                    seg.writeInt(a.getId());
                    oldSession.write(seg);
                }
                
                // 保存新会话信息
                a.setSession(this);
                a.setTime(current);
                a.setLastLiveTime(System.currentTimeMillis());
                a.setCmccUserID(cmccUserId);
                accountService.registry(a);
                registry(a);
                cache.registerLoginInfo(account.getId(), cmccUserId);
    
                // 回发登录成功包
                UWAPSegment seg = new UWAPSegment(AccountConstants.RELOGIN_RESULT, data.getSerial(), data.getSessionId());
                seg.writeInt(requestId);
                seg.write((byte) 0);
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString(account.getPhone());
                seg.writeInt(account.getModifyPasswordTimes());
                seg.writeInt(iMoney);
                if (account.getMonthFee() >= Const.MONTH_MAX) {
                    seg.writeBoolean(true);
                } else {
                    seg.writeBoolean(false);
                }
                if (account.getSubscribeStatus() == Account.SUBSCRIBED) {
                    seg.writeBoolean(true);
                } else {
                    seg.writeBoolean(false);
                }
                write(seg);
                log.info("AccountID[" + a.getId() + "]Relogined");
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    /**
     * 请求购买商品(扣费)
     * accountId        int             帐号ID，<0表示PIP版本访问卓望认证
     * cost             int             价格(单位1/100i)(pip版本才有)
     * consumeCode      String          计费代码(卓望版本才有)
     * requestId        int             请求ID
     * version          String          客户端版本号，格式为：2.2-CPIP1000-xxxxxxx
     * cmccUserId       String          卓望平台用户ID，当accountId<0时传入
     * public static final byte BUY = (byte)207;
     * 购买商品结果
     * requestId        int             请求ID
     * result           boolean         购买结果，true成功，false失败
     * balance          int             账户余额(单位1/100i)
     * cost             int             消耗i币(单位1/100i)(卓望版本总是-1)
     * msg              String          如果失败，返回错误信息
     * public static final byte BUY_RESULT = (byte)207;
     */
    protected void buy(UWAPData data) throws UWAPException {
        int accountId = -1;
        int id = -1;
        String consumeCode = null;
        try {
            accountId = data.readInt();
            consumeCode = data.readString();
            id = data.readInt();
            String version = null;
            try {
                version = data.readString();
            } catch (Exception e) {
            }
            log.info("AccountID[" + accountId + "]consumeCode[" + consumeCode + "]Server[" + this.id + "]BuyID[" + id + "]TRY");
            
            // 检查帐号是否已登录
            AccountState account = null;
            CmccUserKey userKey = null;
            if (accountId >= 0) {
                account = accountService.getAccount(accountId);
                if (account == null) {
                    UWAPSegment seg = new UWAPSegment(AccountConstants.BUY_RESULT);
                    seg.writeInt(id);
                    seg.writeBoolean(false);
                    seg.writeInt(0);
                    seg.writeInt(0);
                    seg.writeString("帐号不存在或者帐号不处于活动状态");
                    write(seg);
                    log.info("AccountID[" + accountId + "]consumeCode[" + consumeCode + "]Server[" + this.id + "]BuyID[" + id + "] BuyError NotFound");
                    return;
                }
                userKey = cache.getUserKey(account.getCmccUserID());
            } else {
                userKey = cache.getUserKey(data.readString());
            }
            
            // 检查是否已登录平台
            if (userKey == null) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.BUY_RESULT);
                seg.writeInt(id);
                seg.writeBoolean(false);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeString("未登录平台，请退出游戏重新进入");
                write(seg);
                log.info("AccountID[" + accountId + "]consumeCode[" + consumeCode + "]Server[" + this.id + "]BuyID[" + id + "] BuyError NotFound");
                return;
            }
            cache.activeUserKey(userKey);
            
            // 如果是从PIP服务器发起的购买，则每月有购买上限
            int price = cmccService.getPrice(consumeCode);
            if (accountId < 0) {
                int monthConsume = cache.getConsumeAmount(userKey.getUserId());
                if (monthConsume + price > 10000) {
                    UWAPSegment seg = new UWAPSegment(AccountConstants.BUY_RESULT);
                    seg.writeInt(id);
                    seg.writeBoolean(false);
                    seg.writeInt(0);
                    seg.writeInt(0);
                    seg.writeString("已达到本月话费购买上限");
                    write(seg);
                    log.info("UserID[" + userKey.getUserId() + "]consumeCode[" + consumeCode + "]Server[" + this.id + "]BuyID[" + id + "] BuyError Exceed Month Limit");
                    return;
                }
                int dayConsume = cache.getBuyAmount(userKey.getUserId(), 86400000L);
                if (dayConsume + price > 2000) {
                    UWAPSegment seg = new UWAPSegment(AccountConstants.BUY_RESULT);
                    seg.writeInt(id);
                    seg.writeBoolean(false);
                    seg.writeInt(0);
                    seg.writeInt(0);
                    seg.writeString("已达到当日话费购买上限");
                    write(seg);
                    log.info("UserID[" + userKey.getUserId() + "]consumeCode[" + consumeCode + "]Server[" + this.id + "]BuyID[" + id + "] BuyError Exceed Day Limit");
                    return;
                }
            }
            
            // 发起购买请求
            int imoney = 0;
            if ((account == null || !isTestAccount(account.getAccount().getUserName())) && !isTestAccount(userKey.getUserId())) {
                if (consumeCode.length() > 0) {
                    String cid = getChannelIDFromVersion(version);
                    imoney = cmccService.buyGameTool(userKey.getUserId(), consumeCode, cid);
                } else {
                    imoney = cmccService.queryBalance(userKey.getUserId());
                }
                imoney *= 100;
            }
            UWAPSegment seg = new UWAPSegment(AccountConstants.BUY_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(true);
            seg.writeInt(imoney);
            seg.writeInt(price * 100);
            seg.writeString("");
            write(seg);
            log.info("AccountID[" + accountId + "]consumeCode[" + consumeCode + "]Server[" + this.id + "]BuyID[" + id + "] OK");
            
            // 购买成功，向Fee表插入一条记录，添加当月消费数据
            feeService.newChargedFee(accountId, cmccService.getPrice(consumeCode), 
                    "CMCC_" + userKey.getUserId());
            cache.addConsumeAmount(userKey.getUserId(), price);
        } catch (NoEnoughBalanceException ex) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.BUY_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(false);
            seg.writeInt(-1);
            seg.writeInt(0);
            seg.writeString(ex.getMessage());
            write(seg);
            log.info("AccountID[" + accountId + "]consumeCode[" + consumeCode + "]Server[" + this.id + "]BuyID[" + id + "] BuyError NotFound");
        } catch (CmccException ex) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.BUY_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(false);
            seg.writeInt(0);
            seg.writeInt(0);
            seg.writeString(ex.getMessage());
            write(seg);
            log.info("AccountID[" + accountId + "]consumeCode[" + consumeCode + "]Server[" + this.id + "]BuyID[" + id + "] BuyError NotFound");
        } catch (Exception e) {
            log.error(e, e);
            UWAPSegment seg = new UWAPSegment(AccountConstants.BUY_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(false);
            seg.writeInt(0);
            seg.writeInt(0);
            seg.writeString("系统错误");
            write(seg);
            log.info("AccountID[" + accountId + "]consumeCode[" + consumeCode + "]Server[" + this.id + "]BuyID[" + id + "] BuyError NotFound");
        }
    }
    
    /**
     * 卓望版本申请短信购买Token（16位数字）
     * accountId        int             帐号ID
     * playerId         int             请求购买的玩家ID
     * consumeCode      String          计费代码(卓望版本才有)
     * requestId        int             请求ID
     * public static final byte CMCC_SMS_BUY_REQ = (byte)216;
     * 卓望版本申请短信购买Token结果。
     * requestId        int             请求ID
     * accountId        int             帐号ID
     * playerId         int             玩家ID
     * token            String          短信购买请求号
     * public static final byte CMCC_SMS_BUY_REQ_RESULT = (byte)216;
     */
    protected void cmccSmsBuyReq(UWAPData data) throws UWAPException {
        int accountId = -1;
        int playerId = -1;
        String consumeCode = null;
        int requestId = -1;
        try {
            accountId = data.readInt();
            playerId = data.readInt();
            consumeCode = data.readString();
            requestId = data.readInt();
            log.info("AccountID[" + accountId + "]SMSConsumeCode[" + consumeCode + "]TRY");
            
            // 检查帐号是否已登录
            AccountState account = accountService.getAccount(accountId);
            if (account == null) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_SMS_BUY_REQ_RESULT);
                seg.writeInt(requestId);
                seg.writeBoolean(false);
                seg.writeInt(accountId);
                seg.writeInt(playerId);
                seg.writeString("帐号不存在或者帐号不处于活动状态");
                write(seg);
                log.info("AccountID[" + accountId + "] SMSBuyID[" + id + "] BuyError NotFound");
                return;
            }
            
            // 检查是否已登录平台
            CmccUserKey userKey = cache.getUserKey(account.getCmccUserID());
            if (userKey == null) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_SMS_BUY_REQ_RESULT);
                seg.writeInt(requestId);
                seg.writeBoolean(false);
                seg.writeInt(accountId);
                seg.writeInt(playerId);
                seg.writeString("未登录平台，请退出游戏重新进入");
                write(seg);
                log.info("AccountID[" + accountId + "] SMSBuyID[" + id + "] BuyError NotFound");
                return;
            }
            cache.activeUserKey(userKey);
            
            // 生成购买序列号
            try {
                String seq = cache.createSmsBuyReq(requestId, userKey, this.id, accountId, playerId, 
                        consumeCode);
                if (seq == null) {
                    throw new CmccException("请求购买失败");
                }
                
                UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_SMS_BUY_REQ_RESULT);
                seg.writeInt(requestId);
                seg.writeBoolean(true);
                seg.writeInt(accountId);
                seg.writeInt(playerId);
                seg.writeString(seq);
                write(seg);
                log.info("AccountID[" + accountId + "] SMSBuyID[" + id + "]SEQ[" + seq + 
                        "]SMSConsumeCode[" + consumeCode + "]");
            } catch (CmccException ex) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_SMS_BUY_REQ_RESULT);
                seg.writeInt(requestId);
                seg.writeBoolean(false);
                seg.writeInt(accountId);
                seg.writeInt(playerId);
                seg.writeString(ex.getMessage());
                write(seg);
                log.info("AccountID[" + accountId + "] SMSBuyID[" + id + "] BuyError NotFound");
            }
        } catch (Exception e) {
            log.error(e, e);
            UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_SMS_BUY_REQ_RESULT);
            seg.writeInt(requestId);
            seg.writeBoolean(false);
            seg.writeInt(accountId);
            seg.writeInt(playerId);
            seg.writeString("系统错误");
            write(seg);
            log.info("AccountID[" + accountId + "] SMSBuyID[" + id + "] BuyError NotFound");
        }
    }
    
    /*
     * 判断帐号是否测试帐号。测试帐号允许不访问平台直接登录。
     */
    protected boolean isTestAccount(String name) {
        return name.startsWith("test") || name.startsWith("gm");
    }
    
    /**
     * 用户在线通知(定时刷新以保持帐号在线)，此包也用作世界断线后同步在线用户
     * id               int             帐号ID
     * public static final byte LIVE_NOTIFY = (byte)198;
     */
    protected void liveNotify(UWAPData data) throws Exception {
        int accountId = data.readInt();
        AccountState account = accountService.getAccount(accountId);
        if (account != null) {
            account.setLastLiveTime(System.currentTimeMillis());
        } else {
            Account acc = accountService.loadAccountById(accountId);
            if (acc == null) {
                return;
            }
            long current = System.currentTimeMillis();
            account = new AccountState(acc, current);
            account.setSession(this);
            account.setTime(current);
            account.setLastLiveTime(current);
            CmccUserKey key = cache.getLoginInfo(accountId);
            if (key != null) {
                account.setCmccUserID(key.getUserId());
                cache.activeUserKey(key);
            }
            accountService.registry(account);
            registry(account);
        }
    }
    
    /**
     * 卓望平台用户在线通知（定时刷新以保持帐号在线）
     * cmccUserId       String          卓望平台用户ID
     */
    protected void cmccLiveNotify(UWAPData data) throws Exception {
        String userId = data.readString();
        CmccUserKey userKey = cache.getUserKey(userId);
        if (userKey != null) {
            cache.activeUserKey(userKey);
        }
    }
    
    /*
     * 从客户端版本号中解析出4位渠道代码来。有以下几种可能：
     * 老幻想，版本号为2.1.1-CCCCCPiP-xxxx
     * 老武林，版本号为2.2-15801001-xxxx
     * 新版本，版本号为3.0-CPIP1000-xxxx
     */
    protected String getChannelIDFromVersion(String ver) {
        if (ver == null) {
            return "1000";
        }
        String[] secs = ver.split("-");
        if (secs.length < 2) {
            return "1000";
        }
        String cid = secs[1];
        if (cid.length() != 8) {
            return "1000";
        }
        try {
            // 旧版本的客户端渠道号是整数，用缺省渠道号
            Integer.parseInt(cid);
            return "1000";
        } catch (Exception e) {
        }
        try {
            int ret = Integer.parseInt(cid.substring(4));
            if (ret >= 1000 && ret <= 1999) {
                return String.valueOf(ret);
            } else {
                return "1000";
            }
        } catch (Exception e) {
            return "1000";
        }
    }
    
    /*
     * 卓望版本，记录用户推荐好友信息。
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * targetPhone      String          目标用户手机号
     */
    /*
     * 卓望版本，用户推荐好友。
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * targetPhone      String          目标用户手机号
     * message          String          邀请标题
     * requestId        int             请求ID
     * public static final byte CMCC_RECOMMEND_REQUEST = (byte)220;
     * 卓望版本，推荐好友结果。
     * requestId        int             请求ID
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * targetPhone      String          目标用户手机号
     * result           boolean         true成功，false失败
     * message          String          成功/失败消息
     * public static final byte CMCC_RECOMMEND_RESULT = (byte)220;
     */
    protected void cmccRecommendRequest(UWAPData data) throws Exception {
        String userId = data.readString();
        int accountId = data.readInt();
        int playerId = data.readInt();
        String targetPhone = data.readString();
        log.info("[CMCC_RECOMMEND] userId[" + userId + "]accountId[" + accountId + "]playerId[" +
                playerId + "]serverid[" + this.id + "]targetPhone[" + targetPhone + "]");
        
        RecommendRequest rr = new RecommendRequest();
        rr.setUserId(userId);
        rr.setAccountId(accountId);
        rr.setPlayerId(playerId);
        rr.setServerId(this.id == null ? "" : this.id);
        rr.setTargetPhone(targetPhone);
        rr.setCreateTime(new java.util.Date());
        new BaseDao().makePersistent(rr);
        
        // 检查是否新版协议
        String msg;
        int requestId;
        try {
            msg = data.readString();
            requestId = data.readInt();
        } catch (Exception e) {
            return;
        }
        try {
            cmccService.sendRecommend(userId, targetPhone, msg);
            UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_RECOMMEND_RESULT);
            seg.writeInt(requestId);
            seg.writeString(userId);
            seg.writeInt(accountId);
            seg.writeInt(playerId);
            seg.writeString(targetPhone);
            seg.writeBoolean(true);
            seg.writeString("邀请发送成功。");
            write(seg);
        } catch (CmccException e) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_RECOMMEND_RESULT);
            seg.writeInt(requestId);
            seg.writeString(userId);
            seg.writeInt(accountId);
            seg.writeInt(playerId);
            seg.writeString(targetPhone);
            seg.writeBoolean(false);
            seg.writeString(e.getMessage());
            write(seg);
        }
    }
    
    /**
     * 卓望版本，订购移动服务。
     * requestId        int             请求ID
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * subType          int             订购类型：1 开通彩铃，2 开通飞信，3 开通邮箱，4 开通手机报，5 开通G+游戏包
     * public static final byte CMCC_SUBSCRIBE = (byte)221;
     * 卓望版本，订购移动服务请求结果（请求成功 != 订购成功）。
     * requestId        int             请求ID
     * userId           String          登录平台ID
     * accountId        int             帐号ID
     * playerId         int             角色ID
     * subType          int             订购类型：1 开通彩铃，2 开通飞信，3 开通邮箱，4 开通手机报，5 开通G+游戏包
     * result           boolean         true成功，false失败
     * public static final byte CMCC_SUBSCRIBE_RESULT = (byte)221;
     */
    protected void cmccSubscribe(UWAPData data) throws Exception {
        int requestId = data.readInt();
        String userId = data.readString();
        int accountId = data.readInt();
        int playerId = data.readInt();
        int subType = data.readInt();
        log.info("[CMCC_SUBSCRIBE]userId[" + userId + "]accountId[" + accountId + "]playerId[" +
                playerId + "]serverid[" + this.id + "]subtype[" + subType + "]");
        
        boolean result = false;
        try {
            result = cmccService.subscribe(userId, subType);
        } catch (CmccException e) {
            log.error(e, e);
        }
        UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_SUBSCRIBE_RESULT);
        seg.writeInt(requestId);
        seg.writeString(userId);
        seg.writeInt(accountId);
        seg.writeInt(playerId);
        seg.writeInt(subType);
        seg.writeBoolean(result);
        write(seg);
    }
    
    /**
     * 卓望版本，玩家升级通知。
     * userId           String          用户登录ID
     * accountId        int             帐号ID
     * playerId         int             用户ID
     * level            int             用户级别
     * public static final byte CMCC_LEVELUP_NOTIFY = (byte)223;
     */
    protected void cmccLevelUpNotify(UWAPData data) throws Exception {
        String userID = data.readString();
        int accountID = data.readInt();
        int playerId = data.readInt();
        int level = data.readInt();
        log.info("[LEVELUP]UserID[" + userID + "]AccountID[" + accountID + "]PlayerID[" +
                playerId + "]LEVEL[" + level + "]ServerID[" + id + "]");

        try {
            jilin_levelUpCheck(userID, accountID, playerId, level);
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    // 吉林活动，当被推荐玩家升到20级，推荐人获得10元话费。新注册玩家升到10级，获得
    // 10元话费，升到30级再获得20元话费。每项只能奖励一次。
    public void jilin_levelUpCheck(String userID, int accountID, int playerID, int level) throws Exception {
        BaseDao dao = new BaseDao();
        
        // 检查是否被推荐的用户到达10级奖励
        if (level == 10) {
        	String hql = "from RecommendRecord rr where rr.target = '" + userID + "'";
            List list = dao.getList(hql);
            RecommendRecord rec = null;
            if (list.size() > 0) {
                rec = (RecommendRecord)list.get(0);
            }
            if (rec != null) {
                // 检查这个用户是否已经发放过推荐奖励了
                hql = "from RewardHistory rh where rh.sourceUser = '" + userID + "' and cause = 2 and level = " + level;
                RewardHistory rh = (RewardHistory)dao.uniqueResult(hql);
                if (rh == null) {
                    // 保存赠送记录
                    rh = new RewardHistory();
                    rh.setUserID(rec.getSource());
                    rh.setCause(2);
                    rh.setSourceUser(userID);
                    rh.setAccountID(accountID);
                    rh.setPlayerID(playerID);
                    rh.setLevel(level);
                    rh.setRewardTime(new java.util.Date());
                    rh.setRewardMoney(0);
                    dao.makePersistent(rh);
                }
            }
        }

        // 检查是否被推荐的用户到达20级奖励
        if (level == 20) {
            String hql = "from RecommendRecord rr where rr.target = '" + userID + "'";
            List list = dao.getList(hql);
            RecommendRecord rec = null;
            if (list.size() > 0) {
                rec = (RecommendRecord)list.get(0);
            }
            if (rec != null) {
                // 检查这个用户是否已经发放过推荐奖励了
                hql = "from RewardHistory rh where rh.sourceUser = '" + userID + "' and cause = 2 and level = " + level;
                RewardHistory rh = (RewardHistory)dao.uniqueResult(hql);
                if (rh == null) {
                    // 通过平台发送短信
                    try {
                        cmccService.sendRewardMessage(rec.getSource(), 
                                "恭喜您推荐的好友玩幻想到达20级,下月您将得到10元话费奖励,话费将直接充到您的手机号码中.");
                    } catch (Exception e) {
                        log.error(e, e);
                    }
                    
                    // 保存赠送记录
                    rh = new RewardHistory();
                    rh.setUserID(rec.getSource());
                    rh.setCause(2);
                    rh.setSourceUser(userID);
                    rh.setAccountID(accountID);
                    rh.setPlayerID(playerID);
                    rh.setLevel(level);
                    rh.setRewardTime(new java.util.Date());
                    rh.setRewardMoney(10);
                    dao.makePersistent(rh);
                }
            }
        }
        
        // 检查是否5/1日以后注册的吉林用户到达20级或者到达30级，升级时使用的userID必须和注册时一致
        if (level == 10 || level == 20 || level == 30) {
            Account acc = accountService.loadAccountById(accountID);
            Calendar cal = Calendar.getInstance();
            cal.set(2009, Calendar.MAY, 1, 0, 0, 0);
            long valveTime = cal.getTimeInMillis();
            // 检查是否新注册用户
            if (acc != null && acc.getCreateTime().getTime() > valveTime &&
                    acc.getPhone().equals(userID)) {
                // 检查是否吉林用户
                String hql = "from UserRegion ur where ur.userID = '" + userID + "'";
                UserRegion ur = (UserRegion)dao.uniqueResult(hql);
                if (ur != null) {
                    // 检查是否发放过奖励了
                    hql = "from RewardHistory rh where rh.userID = '" + userID + "' and cause = 1 and level = " + level;
                    RewardHistory rh = (RewardHistory)dao.uniqueResult(hql);
                    if (rh == null) {
                        // 通过平台发送短信
                        try {
                            if (level == 20) {
                                cmccService.sendRewardMessage(userID, 
                                        "恭喜您幻想到达20级,下月将得到10元话费奖励,话费将充到您的手机号码中.到30级还能再得20元话费呢!赶紧升级吧!");
                            } else if (level == 30) {
                                cmccService.sendRewardMessage(userID, 
                                        "恭喜您到达30级,下月将得到20元话费奖励,话费将充到您的手机号码中.要推荐好友玩幻想达到20级,您还可以获赠10元话费.");
                            }
                        } catch (Exception e) {
                            log.error(e, e);
                        }
                        
                        // 保存赠送记录
                        rh = new RewardHistory();
                        rh.setUserID(userID);
                        rh.setCause(1);
                        rh.setSourceUser(userID);
                        rh.setAccountID(accountID);
                        rh.setPlayerID(playerID);
                        rh.setLevel(level);
                        rh.setRewardTime(new java.util.Date());
                        if (level == 20) {
                            rh.setRewardMoney(10);
                        } else if (level == 30) {
                            rh.setRewardMoney(20);
                        } else {
                        	rh.setRewardMoney(0);
                        }
                        dao.makePersistent(rh);
                    }
                }
            }
        }
    }
    
    /*
     * 记录用户ID和实际手机号之间的关系。
     * @param userID
     * @param phone
     */
    private void recordUserPhone(String userID, String phone) {
        if (phone == null || phone.length() == 0) {
            return;
        }
        try {
            String hql = "from UserPhone up where up.userID = '" + userID + "'";
            BaseDao dao = new BaseDao();
            UserPhone oldRecord = (UserPhone)dao.uniqueResult(hql);
            if (oldRecord == null) {
                UserPhone newRecord = new UserPhone();
                newRecord.setUserID(userID);
                newRecord.setPhone(phone);
                dao.makePersistent(newRecord);
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    /**
     * 查询成功推荐的玩家信息。
     * requestId        int             请求ID
     * userId           String          用户平台ID
     * public static final byte CMCC_QUERY_RECOMMEND = (byte)224;
     * 查询成功推荐的玩家信息结果。
     * requestId        int             请求ID
     * userId           String          用户平台ID
     * accounts         int[]           被推荐用户的注册帐号
     * public static final byte CMCC_QUERY_RECOMMEND_RESULT = (byte)224;
     */
    protected void cmccQueryRecommend(UWAPData data) throws Exception {
        int requestId = data.readInt();
        String userId = data.readString();
        int[] accounts = new int[0];
        try {
            BaseDao dao = new BaseDao();
            List list = dao.getList("from RecommendRecord r where r.source = '" + userId + "'");
            HashSet<String> userids = new HashSet<String>();
            for (int i = 0; list != null && i < list.size(); i++) {
                RecommendRecord r = (RecommendRecord)list.get(i);
                userids.add(r.getTarget());
            }
            String sql = "select a.id from Account a where a.phone in (";
            boolean first = true;
            for (String s : userids) {
                if (!first) {
                    sql += ',';
                }
                sql += "'" + s + "'"; 
                first = false;
            }
            sql += ")";
            list = dao.getList(sql);
            accounts = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                accounts[i] = ((Integer)list.get(i)).intValue();
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_QUERY_RECOMMEND_RESULT);
        seg.writeInt(requestId);
        seg.writeString(userId);
        seg.writeInts(accounts);
        write(seg);
    }
    
    /**
     * 卓望版本，向用户发送短信通知。
     * userId           String          用户登录ID
     * message          String          通知消息
     * public static final byte CMCC_SEND_MESSAGE = (byte)225;
     */
    protected void cmccSendMessage(UWAPData data) throws Exception {
        String userId = data.readString();
        String message = data.readString();
        try {
            cmccService.sendRewardMessage(userId, message);
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    /**
     * 查询用户是否通过卓望平台下载过客户端。
     * userId			String			用户ID
     * accountId		int				请求帐号ID
     * playerId			int 			请求角色ID
     * public static final byte CMCC_CHECK_DOWNLOAD = (byte)226;
     * 通知世界服务器用户需要通过卓望平台下载客户端。
     * userId			String			用户ID
     * accountId		int				帐号ID
     * playerId			int				角色ID
     * url				String			下载地址
     * public static final byte CMCC_PUSH_DOWNLOAD = (byte)226;
     */
    protected void cmccCheckDownload(UWAPData data) throws Exception {
        String userId = data.readString();
        int accountId = data.readInt();
        int playerId = data.readInt();
        String url = Server.instance.getConfiguration().getString("download_url");
        if (url == null) {
        	return;
        }
        
        // 检查用户是否下载过
        try {
            String hql = "from UserPhone up where up.userID = '" + userId + "'";
            BaseDao dao = new BaseDao();
            UserPhone oldRecord = (UserPhone)dao.uniqueResult(hql);
            if (oldRecord == null) {
                UserPhone newRecord = new UserPhone();
                newRecord.setUserID(userId);
                newRecord.setPhone("");
                newRecord.setDownloadDate(new java.util.Date());
                dao.makePersistent(newRecord);
            } else if (oldRecord.getDownloadDate() == null) {
            	oldRecord.setDownloadDate(new java.util.Date());
            	dao.makePersistent(oldRecord);
            } else {
            	// 下载过了，直接返回
            	return;
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        
        // 没有下载过，要求世界服务器推送下载脚本
        UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_PUSH_DOWNLOAD);
        seg.writeString(userId);
        seg.writeInt(accountId);
        seg.writeInt(playerId);
        seg.writeString(url);
        write(seg);
    }
}
