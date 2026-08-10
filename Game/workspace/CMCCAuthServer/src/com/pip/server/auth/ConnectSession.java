package com.pip.server.auth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

import com.pip.server.auth.bean.Account;
import com.pip.server.auth.net.UWAPException;
import com.pip.server.auth.net.Packet;
import com.pip.server.auth.net.AccountConstants;
import com.pip.server.auth.net.Session;
import com.pip.server.auth.net.UWAPData;
import com.pip.server.auth.net.UWAPSegment;
import com.pip.server.util.IDGenerator;
import com.pip.server.util.KeywordsUtil;
import com.pip.server.util.Utils;

/**
 * PIP版本和CMCC版本连接会话共同的部分。
 */
public abstract class ConnectSession extends Session {
    protected final static char[] NUM = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    protected final static Random RND = new Random();
    protected final static Logger log = Logger.getLogger(ConnectSession.class);
    public static final String QUICKREG_PREFIX = "游客";

    protected AccountService accountService;
    protected Configuration configuration = null;
    protected ConnectService connectService;
    protected FeeService feeService;

    /*
     * 世界服务器ID
     */
    protected String id;
    /*
     * 通过此世界登录的所有帐号
     */
    protected Map<Integer, AccountState> clients = new ConcurrentHashMap<Integer, AccountState>();

    public ConnectSession(IoSession session) {
        super(session);
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }

    public void setFeeService(FeeService feeService) {
        this.feeService = feeService;
    }

    public void idle(IdleStatus status) {

    }

    public void created() {
        connectService.addConnect(this);
    }

    public void opened() {

    }
    
    public String getWorldID() {
        return id;
    }

    /**
     * 世界服务器掉线时，把所有通过此世界登录的用户下线。
     */
    public void closed() {
        connectService.removeConnect(this);
        for (AccountState account : clients.values()) {
            account.setSession(null);
            unRegistry(account);
        }
    }

    /**
     * 处理世界服务器发过来的请求。
     */
    public void handle(Packet packet) {
        // log.info("receive packet " + packet.datas[0].getAppType() + " from " + this.id);
        try {
            UWAPData data = packet.datas[0];
            byte type = data.getAppType();
            switch (type) {
            case AccountConstants.SERVER_LOGIN:             // 世界服务器登录
                serverLogin(data);
                break;
            case AccountConstants.ACCOUNT_REG:              // 注册帐号
                playerRegister(data);
                break;
            case AccountConstants.PLAYER_LOGOUT:            // 用户登出
                playerLogout(data);
                break;
            case AccountConstants.LOGIN:                    // 用户登录
                accountLogin(data);
                break;
            case AccountConstants.STOP:                     // 停止服务
                stopServer(data);
                break;
            case AccountConstants.RELOGIN:                  // 断线重登录
                relogin(data);
                break;
            case AccountConstants.FORBID:                   // 封停/解封帐号
                forbid(data);
                break;
            case AccountConstants.RELEASEACCOUNT:           // 释放帐号
                releaseAccount(data);
                break;
            case AccountConstants.ADMIN_ACCOUNTINFO:        // 查询帐号信息
                accountinfo(data);
                break;
            case AccountConstants.ADMIN_MODIFYACCOUNT:      // 修改帐号密码
                modifyaccount(data);
                break;
            case AccountConstants.LIVE_NOTIFY:              // 在线状态刷新
                liveNotify(data);
                break;
            case AccountConstants.CMCC_LIVE_NOTIFY:         // 卓望用户在线状态刷新
                cmccLiveNotify(data);
                break;
            case AccountConstants.FEE:                      // 扣时长费用
                fee(data);
                break;
            case AccountConstants.MODIFY_PASSWORD:          // 修改密码
                modifyPassword(data);
                break;
            case AccountConstants.MODIFY_ACCOUNT_NAME:      // 修改帐号名称
                modifyAccountName(data);
                break;
            case AccountConstants.QUICK_REG:                // 快速注册
                quickReg(data);
                break;
            case AccountConstants.MODIFY_PHONE:             // 修改注册手机号
                modifyPhone(data);
                break;
            case AccountConstants.BUY:                      // 购买商品
                buy(data);
                break;
            case AccountConstants.ADD_IMONEY:               // 添加i币
                addiMoney(data);
                break;
            case AccountConstants.CHARGEUP:                 // CMCC游戏内充值
                chargeUp(data);
                break;
            case AccountConstants.CMCC_CHARGE:              // CMCC游戏外充值
                cmccCharge(data);
                break;
            case AccountConstants.ADD_RECOMMEND_IMONEY:     // 添加推荐i币
                addRecommendIMoney(data);
                break;
            case AccountConstants.GET_ACCOUNTNAME:          // 查询帐号名称
                getAccountName(data);
                break;
            case AccountConstants.CMCC_GET_HISTORY:         // CMCC查询消费/充值记录
                cmccGetHistory(data);
                break;
            case AccountConstants.CMCC_SMS_BUY_REQ:         // CMCC分配短信购买Token
                cmccSmsBuyReq(data);
                break;
            case AccountConstants.CMCC_RECOMMEND_REQUEST:   // CMCC记录推荐信息
                cmccRecommendRequest(data);
                break;
            case AccountConstants.CMCC_SUBSCRIBE:           // CMCC订购移动业务
                cmccSubscribe(data);
                break;
            case AccountConstants.CMCC_LEVELUP_NOTIFY:      // CMCC用户升级通知
                cmccLevelUpNotify(data);
                break;
            case AccountConstants.CMCC_QUERY_RECOMMEND:     // CMCC推荐成功用户查询
                cmccQueryRecommend(data);
                break;
            case AccountConstants.CMCC_SEND_MESSAGE:        // CMCC发送短信
                cmccSendMessage(data);
                break;
            case AccountConstants.CMCC_CHECK_DOWNLOAD:      // CMCC检查是否下载过客户端
            	cmccCheckDownload(data);
            	break;
            }
        } catch (UWAPException ex) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.ERROR, ex.getSerial(), ex.getSessionId());
            seg.write(ex.getAppType());
            seg.writeString(ex.getMessage());
            write(seg);
        } catch (Exception ex1) {
            log.error(ex1, ex1);
        }
    }

    protected abstract void relogin(UWAPData data) throws Exception;
    protected abstract void playerRegister(UWAPData data) throws UWAPException;
    protected abstract void accountLogin(UWAPData data) throws Exception;
    protected abstract void buy(UWAPData data) throws Exception;
    protected abstract void quickReg(UWAPData data) throws Exception;
    
    protected abstract void cmccCharge(UWAPData data) throws Exception;
    protected abstract void chargeUp(UWAPData data) throws Exception;
    protected abstract void cmccGetHistory(UWAPData data) throws Exception;
    protected abstract void cmccSmsBuyReq(UWAPData data) throws Exception;
    protected abstract void cmccRecommendRequest(UWAPData data) throws Exception;
    protected abstract void cmccSubscribe(UWAPData data) throws Exception;
    protected abstract void cmccLevelUpNotify(UWAPData data) throws Exception;
    protected abstract void cmccQueryRecommend(UWAPData data) throws Exception;
    protected abstract void cmccSendMessage(UWAPData data) throws Exception;
    protected abstract void cmccCheckDownload(UWAPData data) throws Exception;

    /**
     * 根据帐号ID查询帐号名称
     * requestId        int             请求ID
     * accountId        int             帐号ID
     * public static final byte GET_ACCOUNTNAME = (byte)215;
     * 查询帐号名称成功
     * requestId        int             请求ID
     * accountId        int             帐号ID
     * accountName      String          帐号名称
     * public static final byte GET_ACCOUNTNAME_OK = (byte)215;
     */
    protected void getAccountName(UWAPData data) throws Exception {
        int requestId = data.readInt();
        int accountId = data.readInt();
        String accountName = accountService.getAccountName(accountId);
        if (accountName != null) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.GET_ACCOUNTNAME_OK, data.getSerial(), data
                    .getSessionId());
            seg.writeInt(requestId);
            seg.writeInt(accountId);
            seg.writeString(accountName);
            write(seg);
        }
    }

    /**
     * 添加推荐奖励i币
     * accountId        int             帐号ID
     * imoney           int             添加额(单位1/100i)
     */
    protected void addRecommendIMoney(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int iMoney = data.readInt();
        log.info("Add Recommend iMoney AccountId[" + accountId + "] iMoney[" + iMoney + "] TRY");
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        if (account == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = account.getAccount();
        }
        if (a != null) {
            if (a.getRecommend().length() > 0 && !a.getRecommend().startsWith("(已加)")) {
                int recommendAccountId = accountService.getAccountId(a.getRecommend());
                if (recommendAccountId != -1) {
                    AccountState recommendAccount = accountService.getAccount(recommendAccountId);
                    Account recommendA = null;
                    if (recommendAccount == null) {
                        recommendA = accountService.loadAccountById(recommendAccountId);
                    } else {
                        recommendA = recommendAccount.getAccount();
                    }
                    if (recommendA != null) {
                        recommendA.setiMoney(recommendA.getiMoney() + iMoney);
                        a.setRecommend("(已加)" + a.getRecommend());
                        accountService.saveAccount(recommendA);
                        accountService.saveAccount(a);
                        log.info("Add Recommend iMoney AccountId[" + accountId + "] iMoney[" + iMoney
                                + "] RecommendAccountId[" + recommendAccountId + "]");
                    }
                }
            }
        }
    }

    /**
     * 修改注册手机号
     * accountId        int             帐号ID
     * playerId         int             请求修改的角色ID
     * phone            String          新手机号
     * public static final byte MODIFY_PHONE = (byte)206;
     * 修改注册手机号结果
     * result           byte            0成功，1失败
     * playerId         int             请求修改密码的角色ID
     * msg              String          新手机号(成功)/错误信息(失败)
     * public static final byte MODIFY_PHONE_RESULT = (byte)206;
     */
    protected void modifyPhone(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int playerId = data.readInt();
        String phone = data.readString();
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        if (account == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = account.getAccount();
        }
        if (a != null) {
            String oldPhone = account.getPhone();
            if (!Utils.isValidMobilePhone(phone)) {
                sendModifyPhoneResult(playerId, false, "手机号错误");
                return;
            }
            if (oldPhone != null && oldPhone.length() > 0) {
                sendModifyPhoneResult(playerId, false, "已绑定过手机号");
                return;
            }
            a.setPhone(phone);
            accountService.saveAccount(a);
            sendModifyPhoneResult(playerId, true, phone);
        }
    }

    /*
     * 发送绑定手机号请求结果。
     */
    protected void sendModifyPhoneResult(int playerId, boolean success, String msg) {
        if (success) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.MODIFY_PHONE_RESULT);
            seg.write((byte) 0);
            seg.writeInt(playerId);
            seg.writeString(msg);
            write(seg);
        } else {
            UWAPSegment seg = new UWAPSegment(AccountConstants.MODIFY_PHONE_RESULT);
            seg.write((byte) 1);
            seg.writeInt(playerId);
            seg.writeString(msg);
            write(seg);
        }
    }
    
    /**
     * 请求修改密码
     * accountId        int             帐号ID
     * playerId         int             请求修改密码的角色ID
     * old              String          旧密码
     * new1             String          新密码
     * new2             String          重复新密码
     * public static final byte MODIFY_PASSWORD = (byte)204;
     * 修改密码结果
     * result           byte            0成功，1失败
     * playerId         int             请求修改密码的角色ID
     * msg              String          新密码(成功)/错误信息(失败)
     * public static final byte MODIFY_PASSWORD_RESULT = (byte)204;
     */
    protected void modifyPassword(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int playerId = data.readInt();
        String old = data.readString();
        String new1 = data.readString();
        String new2 = data.readString();
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        if (account == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = account.getAccount();
        }
        if (a != null) {
            if (a.getPassword().equals(old)) {
                if (!new1.equals(new2)) {
                    sendModifyPasswordResult(playerId, false, "新密码错误");
                    return;
                }
                if (!Utils.checkString(new1, false)) {
                    sendModifyPasswordResult(playerId, false, "新密码存在非法字符");
                    return;
                }
                if (new1.getBytes("GBK").length > 16) {
                    sendModifyPasswordResult(playerId, false, "新密码超过最大长度(16)");
                    return;
                }
                a.setPassword(new1);
                if (a.getModifyPasswordTimes() > 0) {
                    a.setModifyPasswordTimes(a.getModifyPasswordTimes() - 1);
                }
                accountService.saveAccount(a);
                sendModifyPasswordResult(playerId, true, new1);
            } else {
                sendModifyPasswordResult(playerId, false, "原始密码错误");
            }
        }
    }

    /*
     * 发送修改密码结果包。
     */
    protected void sendModifyPasswordResult(int playerId, boolean success, String msg) {
        if (success) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.MODIFY_PASSWORD_RESULT);
            seg.write((byte) 0);
            seg.writeInt(playerId);
            seg.writeString(msg); // 新密码
            write(seg);
        } else {
            UWAPSegment seg = new UWAPSegment(AccountConstants.MODIFY_PASSWORD_RESULT);
            seg.write((byte) 1);
            seg.writeInt(playerId);
            seg.writeString(msg);
            write(seg);
        }
    }

    /**
     * 修改帐号名称
     * accountId        int             帐号ID
     * playerId         int             修改帐号名称的角色ID
     * name             String          新名字
     * public static final byte MODIFY_ACCOUNT_NAME = (byte)214;
     * 修改帐号名称结果
     * result           byte            0成功，1失败
     * playerId         int             请求修改密码的角色ID
     * msg              String          新名称(成功)/错误信息(失败)
     * public static final byte MODIFY_ACCOUNT_NAME_RESULT = (byte)214;
     */
    protected void modifyAccountName(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int playerId = data.readInt();
        String name = data.readString().trim();

        // 查找现有帐号信息
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        if (account == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = account.getAccount();
        }
        if (a != null) {
            // 首先检查新名称是否合法
            if (name.length() == 0) {
                sendModifyAccountNameResult(playerId, false, "帐号名不能为空");
                return;
            }
            if (name.getBytes("GBK").length > 16) {
                sendModifyAccountNameResult(playerId, false, "帐号名太长");
                return;
            }
            if (KeywordsUtil.isInvalidName(name.toLowerCase())) {
                sendModifyAccountNameResult(playerId, false, "帐号名出现非法字符");
                return;
            }
            if (!Utils.checkString(name, false)) {
                sendModifyAccountNameResult(playerId, false, "帐号名出现非法字符");
                return;
            }
            String newName = KeywordsUtil.filterKeywords(name);
            if (!newName.equals(name)) {
                sendModifyAccountNameResult(playerId, false, "帐号名出现非法字符");
                return;
            }
            
            // 检查新名称是否存在
            if (accountService.loadAccountByName(name) != null) {
                sendModifyAccountNameResult(playerId, false, "这个名字已经被使用了");
                return;
            }
            
            // 修改名称并保存
            a.setUserName(name);
            accountService.saveAccount(a);
            sendModifyAccountNameResult(playerId, true, name);
        }
    }

    /*
     * 发送修改帐号名称结果包。
     */
    protected void sendModifyAccountNameResult(int playerId, boolean success, String msg) {
        if (success) {
            UWAPSegment seg = new UWAPSegment(AccountConstants.MODIFY_ACCOUNT_NAME_RESULT);
            seg.write((byte) 0);
            seg.writeInt(playerId);
            seg.writeString(msg); // 新名字
            write(seg);
        } else {
            UWAPSegment seg = new UWAPSegment(AccountConstants.MODIFY_ACCOUNT_NAME_RESULT);
            seg.write((byte) 1);
            seg.writeInt(playerId);
            seg.writeString(msg);
            write(seg);
        }
    }
    
    /**
     * 添加i币
     * accountId        int             账号ID
     * imoney           int             添加额(单位1/100i)
     * requestId        int             请求ID
     * public static final byte ADD_IMONEY = (byte)208;
     */
    protected void addiMoney(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int iMoney = data.readInt();
        int id = data.readInt();
        log.info("AccountID[" + accountId + "]AddiMoney[" + iMoney + "] BuyID[" + id + "]");
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        if (account == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = account.getAccount();
        }
        if (a != null) {
            synchronized (a) {
                a.setiMoney(a.getiMoney() + iMoney);
                accountService.saveAccount(a);
            }
        }
    }

    /**
     * 请求扣时长费用(废弃)
     * accountId        int             帐号ID
     * fee              int             扣除i币(单位1/100i)
     * iMoney           int             剩余i币，如果和认证服务器不一致则需要同步(单位1/100i)
     * public static final byte FEE = (byte)202;
     * 扣时长费用失败
     * result           byte            固定为0
     * id               int             帐号ID
     * balance          int             余额(单位1/100i)
     * public static final byte FEE_RESULT = (byte)202;
     */
    protected void fee(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int fee = data.readInt();
        int iMoney = data.readInt();
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        log.info("AccountID[" + accountId + "]Fee[" + fee + "]iMoney[" + iMoney + "]");
        if (account == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = account.getAccount();
        }
        if (a != null) {
            synchronized (a) {
                boolean needSave = false;
                try {
                    // 首先检查是否跨月份了，如果跨月份则先清除上月扣费记录
                    Date lastBillingTime = a.getLastBillingTime();
                    Date currentTime = new java.util.Date();
                    if (lastBillingTime != null && !Const.inLaterMonth(lastBillingTime, currentTime)) {
                        a.setMonthFee(0);
                        needSave = true;
                    }

                    // 检查是否本月已扣够上限
                    if (a.getMonthFee() >= Const.MONTH_MAX)
                        return;

                    // 检查是否已经订购了包月服务
                    if (a.getSubscribeStatus() == Account.SUBSCRIBED) {
                        return;
                    }

                    // 检查余额是否充足，如果够，则扣费修改最后扣费时间和本月已扣数量，如果不够，则扣除所有余额并返回错误（扣够本月
                    // 最大金额的不返回错误）
                    if (a.getiMoney() < fee) {
                        if (a.getiMoney() > 0) {
                            a.setMonthFee(a.getMonthFee() + a.getiMoney());
                            a.setiMoney(0);
                            a.setLastBillingTime(currentTime);
                            needSave = true;
                        }
                        if (a.getMonthFee() >= Const.MONTH_MAX)
                            return;

                        // Fee newFee = feeService.newFee(a.getId());
                        UWAPSegment seg = new UWAPSegment(AccountConstants.FEE_RESULT);
                        seg.write((byte) 0);
                        seg.writeInt(a.getId());
                        seg.writeInt(-1);
                        write(seg);
                    } else {
                        a.setiMoney(a.getiMoney() - fee);
                        a.setLastBillingTime(currentTime);
                        a.setMonthFee(a.getMonthFee() + fee);
                        needSave = true;
                        if (a.getiMoney() != iMoney || a.getMonthFee() >= Const.MONTH_MAX) {
                            UWAPSegment seg = new UWAPSegment(AccountConstants.SYNC_IMONEY);
                            seg.writeInt(a.getId());
                            seg.writeInt(a.getiMoney());
                            if (a.getMonthFee() >= Const.MONTH_MAX) {
                                seg.writeBoolean(true);
                            } else {
                                seg.writeBoolean(false);

                            }
                            if (a.getSubscribeStatus() == Account.SUBSCRIBED) {
                                seg.writeBoolean(true);
                            } else {
                                seg.writeBoolean(false);
                            }
                            write(seg);
                        }
                    }
                } finally {
                    if (needSave) {
                        accountService.saveAccount(a);
                    }
                }
            }
        }
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
            accountService.registry(account);
            registry(account);
        }
    }
    
    /**
     * 卓望平台用户在线通知（定时刷新以保持帐号在线）
     * cmccUserId       String          卓望平台用户ID
     */
    protected void cmccLiveNotify(UWAPData data) throws Exception {
    }

    /**
     * GM工具修改密码
     * accountId        int             帐号ID
     * password         String          新密码
     * public static final byte ADMIN_MODIFYACCOUNT = (byte)249;
     */
    protected void modifyaccount(UWAPData data) throws Exception {
        int accountId = data.readInt();
        String password = data.readString();
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        if (account == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = account.getAccount();
        }
        a.setPassword(password);
        accountService.saveAccount(a);
    }

    /**
     * GM工具查询帐号信息（返回信息也用这个）
     * accountId        int             帐号ID
     * accountName      String          帐号ID传-1时用于指定帐号名称
     * 返回：
     * accountId        int             帐号ID
     * accountName      String          帐号名称
     * password         String          密码
     * phone            String          注册手机号
     * public static final byte ADMIN_ACCOUNTINFO = (byte)247;               
     */
    protected void accountinfo(UWAPData data) throws Exception {
        int id = data.readInt();
        if (id != -1) {
            Account account = accountService.loadAccountById(id);
            if (account != null) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.ADMIN_ACCOUNTINFO, data.getSerial(), data
                        .getSessionId());
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString(account.getPhone());
                write(seg);
            }
        } else {
            Account account = accountService.loadAccountByName(data.readString());
            if (account != null) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.ADMIN_ACCOUNTINFO, data.getSerial(), data
                        .getSessionId());
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString(account.getPhone());
                write(seg);
            }
        }
    }

    /**
     * 封停/解封帐号
     * type             byte            1为封停，2为解封
     * accountId        int             帐号ID
     * cause            String          封停原因(type为1是存在)
     * public static final byte FORBID = (byte)194;
     */
    protected void forbid(UWAPData data) throws Exception {
        byte type = data.readByte();
        int accountId = data.readInt();
        AccountState state = accountService.getAccount(accountId);
        Account a;
        if (state == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = state.getAccount();
        }
        if (type == 1) {
            String cause = data.readString();
            a.setValid(false);
            a.setCause(cause);
            accountService.saveAccount(a);
        } else if (type == 2) {
            if (!a.getValid()) {
                a.setValid(true);
                accountService.saveAccount(a);
            }
        }
    }

    /**
     * 释放帐号(从缓存中移除)
     * id               int             帐号ID
     */
    protected void releaseAccount(UWAPData data) throws Exception {
        int id = data.readInt();
        AccountState account = accountService.getAccount(id);
        if (account != null) {
            unRegistry(account);
            accountService.unRegistry(account);
        }
    }

    /**
     * 关闭服务器
     */
    protected void stopServer(UWAPData data) throws Exception {
        accountService.stop();
        System.exit(1);
    }

    /**
     * 用户登出通知
     * accountId        int             帐号ID
     * public static final byte PLAYER_LOGOUT = (byte)188;
     */
    protected void playerLogout(UWAPData data) throws Exception {
        int accountId = data.readInt();
        AccountState account = accountService.getAccount(accountId);
        if (account != null) {
            unRegistry(account);
            accountService.unRegistry(account);
            log.info("AccountID[" + account.getId() + "]Logouted");
        } else {
            log.info("AccountID[" + accountId + "] hasn't logined");
        }
    }

    /*
     * 在本连接上注册一个用户。
     */
    protected void registry(AccountState account) {
        clients.put(new Integer(account.getId()), account);
    }

    /*
     * 在本连接上注销一个用户。
     */
    protected void unRegistry(AccountState account) {
        clients.remove(new Integer(account.getId()));
    }

    /**
     * 世界服务器登录
     * id               String          服务器ID
     * password         String          密码
     * public static final byte SERVER_LOGIN = (byte)180;
     * 世界服务器登录成功
     * 无参数
     * public static final byte SERVER_LOGIN_OK = (byte)181;
     */
    protected void serverLogin(UWAPData data) {
        log.info("Server login");
        boolean success = false;
        try {
            String id = data.readString();
            String password = data.readString();
            String serverPassword = configuration.getString("serverpassword");
            if (password != null && password.equals(serverPassword)) {
                this.id = id;
                UWAPSegment seg = new UWAPSegment(AccountConstants.SERVER_LOGIN_OK);
                write(seg);
                success = true;
                log.info("Server: " + id + " logined");
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        if (!success) {
            close();
            log.info("Server login fail");
        }
    }

    /**
     * 随机生成6位数字密码
     * @param rnd
     * @return
     */
    public static String getPassword(Random rnd) {
        char[] ret = new char[6];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = NUM[rnd.nextInt(ret.length)];
        }
        return new String(ret);
    }
}
