package com.pip.itimes.server.auth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.Packet;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.net.Session;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.sms.SMSSender;
import com.pip.itimes.server.util.IDGenerator;
import com.pip.itimes.server.util.Utils;

public abstract class ConnectSession extends Session {

    protected final static char[] NUM = {'0', '1', '2', '3', '4', '5', '6', '7',
                                        '8', '9'};
    protected final static Random RND = new Random();


    protected final static Logger log = Logger.getLogger(ConnectSession.class);

    protected SMSSender sender = new SMSSender(8);

    protected String id;
    protected AccountService accountService;

    //accountId,accountstate
    protected Map clients = new HashMap();

    protected Configuration configuration = null;

    protected static int segNum = 0;

    protected ConnectService connectService;

    protected FeeService feeService;

    public ConnectSession(IoSession session) {
        super(session);
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void addAccount(AccountState account, int sessionId) {
        clients.put(new Integer(account.getId()), account);
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }

    public void setFeeService(FeeService feeService) {
        this.feeService = feeService;
    }

    public void idle(IdleStatus status) {

    }

    public void closed() {
        List l = new ArrayList(clients.values());
        for (int i = 0; i < l.size(); i++) {
            AccountState account = (AccountState) l.get(i);
            account.setSession(null);
            unRegistry(account);
        }
    }

    public void handle(Packet packet) {
        try {
            log.debug("Connect receive seg:" + (++segNum));
            UWAPData data = packet.datas[0];
            byte type = data.getAppType();
            switch (type) {
            case ServerConstants.SERVER_LOGIN:
                serverLogin(data);
                break;
            case ClientConstants.PLAYER_LOGIN:
                playerLogin(data);
                break;
            case ClientConstants.ACCOUNT_REG:
                playerRegister(data);
                break;
            case ServerConstants.PLAYER_LOGOUT:
                playerLogout(data);
                break;
            case ClientConstants.LOGIN:
                accountLogin(data);
                break;
            case ServerConstants.STOP:
                stopServer(data);
                break;
            case ClientConstants.RELOGIN:
                relogin(data);
                break;
            case ServerConstants.FORBID:
                forbid(data);
                break;
            case ServerConstants.RELEASEACCOUNT:
                releaseAccount(data);
                break;
            case ServerConstants.ADMIN_ACCOUNTINFO:
                accountinfo(data);
                break;
            case ServerConstants.ADMIN_MODIFYACCOUNT:
                modifyaccount(data);
                break;
            case ServerConstants.LIVE_NOTIFY:
                liveNotify(data);
                break;
//            case ServerConstants.IMONEY_CHANGE:
//                iMoneyChange(data);
//                break;
            case ServerConstants.FEE:
                fee(data);
                break;
            case ServerConstants.MODIFY_PASSWORD:
                modifyPassword(data);
                break;
            case ClientConstants.QUICK_REG:
                quickReg(data);
                break;
            case ServerConstants.MODIFY_PHONE:
                modifyPhone(data);
                break;
            case ServerConstants.BUY:
                buy(data);
                break;
            case ServerConstants.ADD_IMONEY:
                addiMoney(data);
                break;
            case ServerConstants.CHARGEUP:
                chargeUp(data);
                break;
            case ClientConstants.CMCC_CHARGE:
                cmccCharge(data);
                break;
            case ServerConstants.ADD_RECOMMEND_IMONEY:
                addRecommendIMoney(data);
                break;
            case ServerConstants.GET_ACCOUNTNAME:
                getAccountName(data);
                break;
            case (byte) 216:
                paradiseRegister(data);
                break;
            case ServerConstants.CMCC_GET_HISTORY:
                    cmccGetHistory(data);
                    break;

            }
        } catch (ITimesException ex) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,
                                              ex.getSerial(),
                                              ex.getSessionId());
            seg.write(ex.getAppType());
            seg.writeString(ex.getMessage());
            write(seg);
        } catch (Exception ex1) {
            log.error(ex1, ex1);
        }
    }

    public static final String QUICKREG_PREFIX = "游客";

    public abstract void cmccCharge(UWAPData data) throws Exception;

    public abstract void quickReg(UWAPData data) throws Exception;

    public abstract void chargeUp(UWAPData data) throws Exception;

    public abstract void cmccGetHistory(UWAPData data) throws Exception;

    /**
     * 乐园单独的注册。
     *
     * @param data
     * @throws Exception
     */
    public void paradiseRegister(UWAPData data) throws Exception {
        int requestId = -1;
        try {
            // 玩家会话ID。
            requestId = data.readInt();
            // 手机号码。
            String phone = data.readString();
            // MIDlet版本。
            String version = data.readString();
            // 机型。
            String model = data.readString();
            // 推荐人。
            String recommend = data.readString();
            // 游戏代码。
            String gameCode = data.readString();

            String name = IDGenerator.getAccountName();
            String password = getPassword(RND);
            Account account = accountService.createNewAccount(name,
                    password, "", phone, recommend, 10000, "", true,
                    getChannel(version), 1, 1, model,gameCode);
            if (account != null) {
                log.info(account.getUserName() + "Paradise Registered Version[" +
                         version + "]model[" + model+"]GameCode["+gameCode+"]");
                UWAPSegment seg = new UWAPSegment((byte) 216,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(requestId);
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString("");
                seg.write((byte) 0); //表示建立了新帐号
                write(seg);
            } else {
                throw new ITimesException("创建帐号错误", data.getSerial(),
                                          requestId,
                                          data.getAppType());
            }
        } catch (ITimesException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(ex, ex);
            throw new ITimesException("创建帐号错误", data.getSerial(),
                                      requestId, data.getAppType());
        }
    }

    public void getAccountName(UWAPData data) throws Exception{
        int requestId = data.readInt();
        int accountId = data.readInt();
        String accountName = accountService.getAccountName(accountId);
        if(accountName!=null){
            UWAPSegment seg = new UWAPSegment(ServerConstants.GET_ACCOUNTNAME_OK,data.getSerial(),data.getSessionId());
            seg.writeInt(requestId);
            seg.writeInt(accountId);
            seg.writeString(accountName);
            write(seg);
        }
    }

    public void addRecommendIMoney(UWAPData data) throws Exception{
        int accountId = data.readInt();
        int iMoney = data.readInt();
        log.info("Add Recommend iMoney AccountId["+accountId+"] iMoney["+iMoney+"] TRY");
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        if (account == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = account.getAccount();
        }
        if (a != null) {
            if(a.getRecommend().length()>0&&!a.getRecommend().startsWith("(已加)")){
                int recommendAccountId = accountService.getAccountId(a.getRecommend());
                if(recommendAccountId!=-1){
                    AccountState recommendAccount = accountService.getAccount(recommendAccountId);
                    Account recommendA = null;
                    if(recommendAccount==null){
                        recommendA = accountService.loadAccountById(recommendAccountId);
                    }else{
                        recommendA = recommendAccount.getAccount();
                    }
                    if(recommendA!=null){
                        recommendA.setiMoney(recommendA.getiMoney()+iMoney);
                        a.setRecommend("(已加)"+a.getRecommend());
                        accountService.saveAccount(recommendA);
                        accountService.saveAccount(a);
                        log.info("Add Recommend iMoney AccountId["+accountId+"] iMoney["+iMoney+"] RecommendAccountId["+recommendAccountId+"]");
                    }
                }
            }
        }
    }
//    public void quickReg(UWAPData data) throws Exception {
//        try {
//            String phone = data.readString();
//            String version = data.readString();
//            String model = data.readString();
//            int count = 0;
////            if (phone.length() > 11)
////                phone.substring(phone.length() - 11);
////            Account account = accountService.getFirstValidAccountByPhone(phone);
////            if (account != null) {
////                UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
////                                                  data.getSerial(),
////                                                  data.getSessionId());
////                seg.writeInt(account.getId());
////                seg.writeString(account.getUserName());
////                seg.writeString(account.getPassword());
////                seg.writeString("");
////                seg.write((byte) 1); //表示没有建立新的帐号，用的是原来的
////                write(seg);
////                return;
////            }
////            if (phone.length() > 0)
////                count = accountService.getAccountCountByPhone(phone);
//            String mid = "";
//            if (phone.length() > 0) {
////                if (!Utils.isValidMID(phone)) {
////                    throw new ITimesException("非法请求", data.getSerial(),
////                                              data.getSessionId(),
////                                              data.getAppType());
////                }
//                mid = Utils.decodeMid(phone);
//                if(mid.length()>0){
//                    Account account = accountService.
//                                      getFirstValidAccountByPhone(
//                                              phone);
//                    if (account != null) {
//                        UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                QUICK_REG,
//                                data.getSerial(),
//                                data.getSessionId());
//                        seg.writeInt(account.getId());
//                        seg.writeString(account.getUserName());
//                        seg.writeString(account.getPassword());
//                        seg.writeString("");
//                        seg.write((byte) 1); //表示没有建立新的帐号，用的是原来的
//                        write(seg);
//                        return;
//                    }
//                }else{
//                    log.info("Illegal MID["+phone+"]");
//                    throw new ITimesException("非法请求",data.getSerial(),data.getSessionId(),data.getAppType());
//                }
//            }
//            if (count == -1)
//                throw new ITimesException("注册错误", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            if (count >= 3)
//                throw new ITimesException("同一手机号只能注册3个帐号", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            String name = null;
//            name = QUICKREG_PREFIX + IDGenerator.getAccountName();
//            String password = getPassword(RND);
//            Account account = accountService.createNewAccount(name,
//                    password, "", mid, "", 0, "", true, getChannel(version),
//                    1,1);
//            if (account != null) {
//                log.info(account.getUserName() + "Quick Registered Version " +
//                         version + " model " + model);
//                UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
//                                                  data.getSerial(),
//                                                  data.getSessionId());
//                seg.writeInt(account.getId());
//                seg.writeString(account.getUserName());
//                seg.writeString(account.getPassword());
//                seg.writeString("");
//                seg.write((byte) 0); //表示建立了新帐号
//                write(seg);
//            } else {
//                throw new ITimesException("创建帐号错误", data.getSerial(),
//                                          data.getSessionId(),
//                                          data.getAppType());
//            }
//        } catch (ITimesException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            log.error(ex,ex);
//            throw new ITimesException("创建帐号错误", data.getSerial(),
//                                      data.getSessionId(), data.getAppType());
//        }
//    }

    protected String getChannel(String version) {
        if (version.length() > 8) {
            return version.substring(version.length() - 8);
        } else {
            return version;
        }
    }

    public void checkBalance() {

    }

    public void created() {
        connectService.addConnect(this);
    }

    public void opened() {

    }

    private void modifyPhone(UWAPData data) throws Exception {
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

    private void sendModifyPhoneResult(int playerId, boolean success,
                                       String msg) {
        if (success) {
            UWAPSegment seg = new UWAPSegment(ServerConstants.
                                              MODIFY_PHONE_RESULT);
            seg.write((byte) 0);
            seg.writeInt(playerId);
            seg.writeString(msg);
            write(seg);
        } else {
            UWAPSegment seg = new UWAPSegment(ServerConstants.
                                              MODIFY_PHONE_RESULT);
            seg.write((byte) 1);
            seg.writeInt(playerId);
            seg.writeString(msg);
            write(seg);
        }
    }

    private void modifyPassword(UWAPData data) throws Exception {
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
                if (new1.getBytes("GBK").length > 16) {
                    sendModifyPasswordResult(playerId, false, "新密码超过最大长度(16)");
                    return;
                }
                if (!Utils.checkString(new1, false)) {
                    sendModifyPasswordResult(playerId, false, "新密码存在非法字符");
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

    private void sendModifyPasswordResult(int playerId, boolean success,
                                          String msg) {
        if (success) {
            UWAPSegment seg = new UWAPSegment(ServerConstants.
                                              MODIFY_PASSWORD_RESULT);
            seg.write((byte) 0);
            seg.writeInt(playerId);
            seg.writeString(msg); //新密码
            write(seg);
        } else {
            UWAPSegment seg = new UWAPSegment(ServerConstants.
                                              MODIFY_PASSWORD_RESULT);
            seg.write((byte) 1);
            seg.writeInt(playerId);
            seg.writeString(msg);
            write(seg);
        }
    }

    protected abstract void buy(UWAPData data) throws Exception;


    public void addiMoney(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int iMoney = data.readInt();
        int id = data.readInt();
        log.info("AccountID[" + accountId + "]AddiMoney[" + iMoney + "] BuyID[" +
                 id + "]");
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

    private void fee(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int fee = data.readInt();
        int iMoney = data.readInt();
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        log.info("AccountID[" + accountId + "]Fee[" + fee + "]iMoney[" + iMoney +
                 "]");
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
                    if (lastBillingTime != null &&
                        !Const.inLaterMonth(lastBillingTime, currentTime)) {
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
                        UWAPSegment seg = new UWAPSegment(ServerConstants.
                                FEE_RESULT);
                        seg.write((byte) 0);
                        seg.writeInt(a.getId());
                        seg.writeInt( -1);
                        write(seg);
                    } else {
                        a.setiMoney(a.getiMoney() - fee);
                        a.setLastBillingTime(currentTime);
                        a.setMonthFee(a.getMonthFee() + fee);
                        needSave = true;
                        if (a.getiMoney() != iMoney ||
                            a.getMonthFee() >= Const.MONTH_MAX) {
                            UWAPSegment seg = new UWAPSegment(ServerConstants.
                                    SYNC_IMONEY);
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

// private void iMoneyChange(UWAPData data) throws Exception{
// int accountId = data.readInt();
// int playerId = data.readInt();
// byte type = data.readByte();
// int change = data.readInt();
// AccountState account = accountService.getAccount(accountId);
//        Account a = null;
//        log.info("AccountID["+accountId+"]TYPE["+type+"]Change["+change+"]TRY");
//        if(account==null){
//            a = accountService.loadAccountById(accountId);
//        }else{
//            a = account.getAccount();
//        }
//        if(a!=null){
//            synchronized(a){
//                log.info("AccountID["+accountId+"]TYPE["+type+"]Change["+change+"]iMoney["+a.getiMoney()+"]TRY");
//                if (type == 0) {
//                    a.setiMoney(a.getiMoney()-change);
//                    accountService.saveAccount(a);
//                }else{
//                    a.setiMoney(a.getiMoney()+change);
//                    accountService.saveAccount(a);
//                }
//                log.info("AccountID["+accountId+"]TYPE["+type+"]Change["+change+"]iMoney["+a.getiMoney()+"]");
//            }
//            UWAPSegment seg = new UWAPSegment(ServerConstants.IMONEY_CHANGE,data.getSerial());
//            seg.writeInt(accountId);
//            seg.writeInt(playerId);
//            seg.writeInt(a.getiMoney());
//            write(seg);
//        }
//    }

    private void liveNotify(UWAPData data) throws Exception {
        int accountId = data.readInt();
        AccountState account = accountService.getAccount(accountId);
        if (account != null) {
            account.setLastLiveTime(System.currentTimeMillis());
        }
    }

    private void modifyaccount(UWAPData data) throws Exception {
        int accountId = data.readInt();
        String password = data.readString();
        Account a = accountService.loadAccountById(accountId);
        a.setPassword(password);
        accountService.saveAccount(a);
    }


    private void accountinfo(UWAPData data) throws Exception {
        int id = data.readInt();
        if (id != -1) {
            Account account = accountService.loadAccountById(id);
            if (account != null) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  ADMIN_ACCOUNTINFO,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString(account.getPhone());
                write(seg);
            }
        } else {
            Account account = accountService.loadAccountByName(data.readString());
            if (account != null) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  ADMIN_ACCOUNTINFO,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString(account.getPhone());
                write(seg);
            }
        }
    }

    private void forbid(UWAPData data) throws Exception {
        byte type = data.readByte();
        int accountId = data.readInt();
        if (type == 1) {
            String cause = data.readString();
            Account a = accountService.loadAccountById(accountId);
            a.setValid(false);
            a.setCause(cause);
            accountService.saveAccount(a);
        } else if (type == 2) {
            Account a = accountService.loadAccountById(accountId);
            if (!a.getValid()) {
                a.setValid(true);
                accountService.saveAccount(a);
            }
        }
    }


    private void releaseAccount(UWAPData data) throws Exception {
        int id = data.readInt();
        AccountState account = accountService.getAccount(id);
        if (account != null) {
            unRegistry(account);
            accountService.unRegistry(account);
        }
    }

    private void stopServer(UWAPData data) throws Exception {
        accountService.stop();
        System.exit(1);
    }

//    private void forbid(UWAPData data) throws Exception{
//        int accountId = data.readInt();
//        Account a = accountService.loadAccountById(accountId);
//        a.setValid(false);
//        accountService.saveAccount(a);
//    }

    protected abstract void relogin(UWAPData data) throws Exception;

//    private void relogin(UWAPData data) throws Exception {
//        String accountName = data.readString();
//        String password = data.readString();
//        String playerName = data.readString();
//        String model = data.readString();
//        String version = data.readString();
//        String[] charge = data.readStrings();
//        String feeplan = data.readString();
//        byte type = data.readByte();
//        Account account = accountService.loadAccountByNameAndPassword(
//                accountName, password);
//        if (account == null) {
//            UWAPSegment seg = new UWAPSegment(ServerConstants.RELOGIN_RESULT,
//                                              data.getSerial(),
//                                              data.getSessionId());
//            seg.write((byte) 2);
//            write(seg);
//            return;
//        }
//        synchronized (accountService) {
//            AccountState a = accountService.getAccount(account.getId());
////            if(a!=null){
////                UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
////                                                  data.getSerial(),
////                                                  data.getSessionId());
////                seg.write((byte)2);
////                write(seg);
////            }else{
//            if (a == null) {
//                a = new AccountState(account, System.currentTimeMillis(),
//                                     sessionId);
//                a.setSession(this);
//
//                accountService.registry(a);
//                registry(a);
//            }
//            if (a.getSession() != this) {
//                UWAPSegment seg = new UWAPSegment(ServerConstants.
//                                                  RELOGIN_RESULT,
//                                                  data.getSerial(),
//                                                  data.getSessionId());
//                seg.write((byte) 2);
//                write(seg);
//                return;
//            }
//            a.setLastLiveTime(System.currentTimeMillis());
//
//            UWAPSegment seg = new UWAPSegment(ServerConstants.RELOGIN_RESULT,
//                                              data.getSerial(),
//                                              data.getSessionId());
//            seg.write((byte) 0);
//            seg.writeInt(account.getId());
//            seg.writeString(account.getUserName());
//            seg.writeString(account.getPassword());
//            seg.writeString(account.getPhone());
//            seg.writeInt(account.getModifyPasswordTimes());
//            seg.writeString(playerName);
//            seg.writeInt(account.getiMoney());
//            if(account.getSubscribeStatus()==Account.SUBSCRIBED||account.getMonthFee()>=Const.MONTH_MAX){
//                seg.writeBoolean(false);
//            }else{
//                seg.writeBoolean(true);
//            }
////            if (account.getSubscribeStatus() == Account.SUBSCRIBED) {
////                seg.writeInt(Const.MONTH_MAX);
////            } else {
////                seg.writeInt(account.getMonthFee());
////            }
//            seg.write(type);
//            write(seg);
//            log.info("AccountID[" + a.getId() + "]Relogined");
////            }
//        }
//
//    }

    protected abstract void accountLogin(UWAPData data) throws Exception;

    ;

//    private void accountLogin(UWAPData data) throws Exception {
//        String accountName = data.readString();
//        String password = data.readString();
//        String model = data.readString();
//        String version = data.readString();
//        String[] charge = data.readStrings();
//        String fee = data.readString();
//
//        Account account = accountService.loadAccountByNameAndPassword(
//                accountName, password);
//        if (account == null) {
//            throw new ITimesException("帐号名或者密码错误", data.getSerial(),
//                                      data.getSessionId(), data.getAppType());
//        }
//        if (account != null && !account.getValid()) {
//            throw new ITimesException("您的角色数据异常,帐号已停封!", data.getSerial(),
//                                      data.getSessionId(),
//                                      data.getAppType());
//        }
//
//        synchronized (accountService) {
//            AccountState a = accountService.getAccount(account.getId());
//            if (a != null) {
//                throw new ITimesException("帐号已经在使用中", data.getSerial(),
//                                          data.getSessionId(),
//                                          data.getAppType());
//            } else {
//                a = new AccountState(account, System.currentTimeMillis(),
//                                     sessionId);
//                a.setSession(this);
//                a.setLastLiveTime(System.currentTimeMillis());
//                accountService.registry(a);
//                registry(a);
//                UWAPSegment seg = new UWAPSegment(ClientConstants.LOGIN_OK,
//                                                  data.getSerial(),
//                                                  data.getSessionId());
//                seg.writeInt(account.getId());
//                seg.writeString(account.getUserName());
//                seg.writeString(account.getPassword());
//                seg.writeString(account.getPhone());
//                seg.writeInt(account.getModifyPasswordTimes());
//                seg.writeInt(account.getiMoney());
//                if(account.getSubscribeStatus()==Account.SUBSCRIBED||account.getMonthFee()>=Const.MONTH_MAX){
//                    seg.writeBoolean(false);
//                }else{
//                    seg.writeBoolean(true);
//                }
////                if (account.getSubscribeStatus() == Account.SUBSCRIBED) {
////                    seg.writeInt(Const.MONTH_MAX);
////                } else {
////                    seg.writeInt(account.getMonthFee());
////                }
//                write(seg);
//                log.info("AccountID[" + a.getId() + "]Phone[" + a.getPhone() +
//                         "]Logined");
//            }
//        }
//
//    }

    private void playerLogout(UWAPData data) throws Exception {
        int accountId = data.readInt();
//        int playerId = data.readInt();
//        boolean logout = data.readBoolean();
        AccountState account = accountService.getAccount(accountId);
        if (account != null) {
            unRegistry(account);
            accountService.unRegistry(account);
            log.info("AccountID[" + account.getId() + "]Logouted");
        } else {
            log.info("AccountID[" + accountId + "] hasn't logined");
        }

    }

    protected abstract void playerRegister(UWAPData data) throws
            ITimesException;

//    private void playerRegister(UWAPData data) throws ITimesException {
//        try {
//            String name = data.readString();
//            String phone = data.readString();
//            String recommend = data.readString();
//            String model = data.readString(); //机器型号
//            String version = data.readString();
//            String[] charge = data.readStrings();
//            String feeplan = data.readString();
//            boolean needReturn = data.readBoolean();
//            name = name.trim();
//            if (name.length() == 0)
//                throw new ITimesException("帐号名不能为空", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            if (name.getBytes("GBK").length > 16)
//                throw new ITimesException("帐号名太长", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            if (KeywordsUtil.isInvalidName(name.toLowerCase()))
//                throw new ITimesException("帐号名出现非法字符", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            if (!Utils.checkString(name, false))
//                throw new ITimesException("帐号名出现非法字符", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            String newName = KeywordsUtil.filterKeywords(name);
//            if (!newName.equals(name))
//                throw new ITimesException("帐号名出现非法字符", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            if (!Utils.isValidMobilePhone(phone))
//                throw new ITimesException("手机号有误", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            int count = accountService.getAccountCountByPhone(phone);
//            if (count == -1)
//                throw new ITimesException("注册错误", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            if (count >= 3)
//                throw new ITimesException("同一手机号只能注册3个帐号", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            Account account = accountService.loadAccountByName(name);
////            if(password.length()==0)
////                throw new ITimesException("密码不能为空",data.getSerial(),data.getSessionId(),data.getAppType());
////            if(password.getBytes("GBK").length>16)
////                throw new ITimesException("密码超过最大长度",data.getSerial(),data.getSessionId(),data.getAppType());
////            if(!Utils.checkString(password,false))
////                throw new ITimesException("密码出现非法字符",data.getSerial(),data.getSessionId(),data.getAppType());
//            if (account != null) {
//                if (account.getPhone().equals(phone)) {
//                    throw new ITimesException(
//                            "该帐号已经存在，如有问题请打客服电话：010-64465123。", data.getSerial(),
//                            data.getSessionId(), data.getAppType());
////                    UWAPSegment seg = new UWAPSegment(ClientConstants.ACCOUNT_REG_OK,data.getSerial(),data.getSessionId());
////                    seg.writeString(phone);
////                    write(seg);
////                    sender.send(phone,
////                                "恭喜您成功注册幻想i时代，帐户名：" + name + "，密码：" + account.getPassword() +
////                            "。祝您游戏愉快。客服电话：010-64465123。本条免费");
////                    log.info("AccountID["+account.getId()+"]Reregistered");
//                } else
//                    throw new ITimesException("已经存在同名帐号", data.getSerial(),
//                                              data.getSessionId(),
//                                              data.getAppType());
//            }
//            String password = getPassword(RND);
//            String cause = "";
//            if (!needReturn) {
//                if (version.indexOf("CCCCCC3G") >= 0) {
//                    account = accountService.createNewAccount(name, password,
//                            "", phone, recommend, 0, "注册", false,
//                            getChannel(version), 0,0);
//                } else {
//                    account = accountService.createNewAccount(name, password,
//                            "", phone, recommend, 0,
//                              "注册" + version.substring(version.length() - 8), false,
//                                       getChannel(version), 0,0);
//                }
//            } else {
//                account = accountService.createNewAccount(name, password, "",
//                        phone, recommend, 0, "", true, getChannel(version), 0,0);
//            }
//            if (account != null) {
//                log.info(account.getUserName() + "Registered Version " +
//                         version + " model" + model);
//                UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                                  ACCOUNT_REG_OK,
//                                                  data.getSerial(),
//                                                  data.getSessionId());
//                seg.writeString(phone);
//                seg.writeString(password);
//                seg.writeBoolean(needReturn);
//                write(seg);
//                if (needReturn)
//                    sender.send(phone,
//                                "恭喜您注册幻想i时代，帐户名：" + name + "，密码：" + password +
//                                "。客服：010-64465123。本条免费", "0738A0000I");
//            } else {
//                throw new ITimesException("创建帐号错误", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            }
//        } catch (ITimesException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            throw new ITimesException("创建帐号错误", data.getSerial(),
//                                      data.getSessionId(), data.getAppType());
//        }
//    }

    private void playerLogin(UWAPData data) throws ITimesException {
        try {
            int accountId = data.readInt();
            int playerId = data.readInt();
            AccountState account = accountService.getAccount(accountId);
            if (account == null) {
                throw new ITimesException("帐号名或者密码错误", data.getSerial(),
                                          data.getSessionId(), data.getAppType());
            }
            account.setPlayerId(playerId);
//            if(accountService.contains(account.getId())){
//                throw new ITimesException("帐号正在使用",data.getSerial(),data.getSessionId(),data.getAppType());
//            }
//            UWAPSegment seg = new UWAPSegment(ClientConstants.LOGIN_OK,
//                                              data.getSerial(),
//                                              data.getSessionId());
//            seg.writeInt(account.getId());
//            write(seg);
        } catch (ITimesException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(ex, ex);
            throw new ITimesException("登录错误", data.getSerial(),
                                      data.getSessionId(), data.getAppType());
        }
    }

    protected void registry(AccountState account) {
        clients.put(new Integer(account.getId()), account);
    }

    protected void unRegistry(AccountState account) {
        clients.remove(new Integer(account.getId()));
    }

    private void serverLogin(UWAPData data) {
        log.info("Server login");
        boolean success = false;
        try {
            String id = data.readString();
            String password = data.readString();
            String serverPassword = configuration.getString("serverpassword");
            if (password != null && password.equals(serverPassword)) {
                this.id = id;
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  SERVER_LOGIN_OK);
                write(seg);
                success = true;
                log.info("Server: " + id + " logined");
            }
        } catch (Exception e) {

        }
        if (!success) {
            close();
            log.info("Server login fail");
        }
    }

    private static int writeSeg = 0;

    public void write(UWAPSegment seg) {
        log.debug("Connect write seg:" + (++writeSeg));
        super.write(seg);
    }

    public static String getPassword(Random rnd) {
        char[] ret = new char[6];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = NUM[rnd.nextInt(ret.length)];
        }
        return new String(ret);
    }

//    public void write(UWAPSegment seg, int accountId) {
//        Integer sessionId = (Integer) sessionIds.get(new Integer(accountId));
//        if (sessionId != null) {
//            seg.setSessionId(sessionId.intValue());
//            write(seg);
//        } else {
//            log.error("Account not found");
//        }
//    }

}
