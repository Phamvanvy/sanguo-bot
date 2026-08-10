package com.pip.itimes.server.auth;

import org.apache.mina.common.IoSession;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.util.IDGenerator;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.net.ServerConstants;
import com.pip.security.SecurityUtils;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PipConnectSession extends ConnectSession {

    private Map<String,AtomicInteger> times = new HashMap<String,AtomicInteger>();

    public PipConnectSession(IoSession session) {
        super(session);
    }

    public void cmccGetHistory(UWAPData data) throws Exception{

    }

    public void cmccCharge(UWAPData data) throws Exception {

    }

    public void quickReg(UWAPData data) throws Exception {
        int requestId = -1;
        try {
            requestId = data.readInt();
            String phone = data.readString();
            String version = data.readString();
            String model = data.readString();
            String gameCode = data.readString();
            int count = 0;
//            if (phone.length() > 11)
//                phone.substring(phone.length() - 11);
//            Account account = accountService.getFirstValidAccountByPhone(phone);
//            if (account != null) {
//                UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
//                                                  data.getSerial(),
//                                                  data.getSessionId());
//                seg.writeInt(account.getId());
//                seg.writeString(account.getUserName());
//                seg.writeString(account.getPassword());
//                seg.writeString("");
//                seg.write((byte) 1); //表示没有建立新的帐号，用的是原来的
//                write(seg);
//                return;
//            }
//            if (phone.length() > 0)
//                count = accountService.getAccountCountByPhone(phone);
            String mid = "";
            if (phone.length() > 0) {
//                if (!Utils.isValidMID(phone)) {
//                    throw new ITimesException("非法请求", data.getSerial(),
//                                              data.getSessionId(),
//                                              data.getAppType());
//                }
                mid = Utils.decodeMid(phone);
                if (mid.length() > 0) {
                    Account account = accountService.
                                      getFirstValidAccountByPhone(
                                              phone);
                    if (account != null) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                QUICK_REG,
                                data.getSerial(),
                                data.getSessionId());
                        seg.writeInt(requestId);
                        seg.writeInt(account.getId());
                        seg.writeString(account.getUserName());
                        seg.writeString(account.getPassword());
                        seg.writeString("");
                        seg.write((byte) 1); //表示没有建立新的帐号，用的是原来的
                        write(seg);
                        return;
                    }
                } else {
                    log.info("Illegal MID[" + phone + "]");
                    throw new ITimesException("非法请求", data.getSerial(),
                                              requestId,
                                              data.getAppType());
                }
            }
            if (count == -1)
                throw new ITimesException("注册错误", data.getSerial(),
                                          requestId, data.getAppType());
            if (count >= 3)
                throw new ITimesException("同一手机号只能注册3个帐号", data.getSerial(),
                                          requestId, data.getAppType());
            String name = null;
            name = QUICKREG_PREFIX + IDGenerator.getAccountName();
            String password = getPassword(RND);
            Account account = accountService.createNewAccount(name,
                    password, "", mid, "", 20000, "", true, getChannel(version),
                    1, 1, model,gameCode);
            if (account != null) {
                log.info(account.getUserName() + "Quick Registered Version[" +
                         version + "]model[" + model+"]GameCode["+gameCode+"]");
                UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
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

    protected void incLoginErrorTime(String name){
        AtomicInteger i = times.get(name);
        if(i==null){
            i = new AtomicInteger(0);
            times.put(name,i);
        }
        i.incrementAndGet();
    }

    protected int getLoginErrorTimeAndRefresh(String name){
        AtomicInteger i = times.get(name);
        if(i==null)
            return 0;
        int ret = i.intValue();
        i.set(0);
        return ret;
    }

    protected void accountLogin(UWAPData data) throws Exception {
        int requestId = data.readInt();
        String accountName = data.readString();
        String password = data.readString();

//        String model = data.readString();
//        String version = data.readString();
//        String[] charge = data.readStrings();
//        String fee = data.readString();

//        Account account = accountService.loadAccountByNameAndPassword(
//                accountName, password);
        Account account = accountService.loadAccountByName(accountName);
        if (account == null) {
            loginError(requestId,"帐号名或者密码错误");
            return;
        }
        if (!account.getPassword().equals(password)) {
            if (account.getPassword().startsWith("#")) {
                if (!SecurityUtils.verifyMD5(password,
                                             account.getPassword().substring(1))) {
                    log.info("Login Error Name[" + accountName + "]Pass[" +
                             password + "]");
                    loginError(requestId,"帐号名或者密码错误");
                    incLoginErrorTime(accountName);
                    return;
                }
            } else {
                log.info("Login Error Name[" + accountName + "]Pass[" +
                         password + "]");
                loginError(requestId,"帐号名或者密码错误");
                incLoginErrorTime(accountName);
                return;
            }

        }
        if (account != null && !account.getValid()) {
            loginError(requestId,"您的角色数据异常,帐号已停封!");
            return;
        }

        synchronized (accountService) {
            AccountState a = accountService.getAccount(account.getId());
            long current = System.currentTimeMillis();
            boolean newCreated = false;
            if (a == null) {
                a = new AccountState(account, current,
                                     sessionId);
                newCreated = true;
            }
            if ((current - a.getTime()) < 30000L&&!newCreated){
//            if ((current - a.getTime()) < 30000L && (current != a.getTime())) {
                loginError(requestId,"帐号已经在使用中，请在30秒后重新尝试登陆");
                return;
            }
            ConnectSession oldSession = a.getSession();
            if (oldSession!=null&&oldSession!=this) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.FORCELOGOUT);
                seg.writeInt(a.getId());
                oldSession.write(seg);
            }
            a.setSession(this);
            a.setTime(current);
            a.setLastLiveTime(current);
            accountService.registry(a);
            registry(a);
            UWAPSegment seg = new UWAPSegment(ClientConstants.LOGIN_OK,
                                              data.getSerial(),
                                              data.getSessionId());
            seg.writeInt(requestId);
            seg.writeInt(account.getId());
            seg.writeString(account.getUserName());
            seg.writeString(account.getPassword());
            seg.writeString(account.getPhone());
            seg.writeInt(account.getModifyPasswordTimes());
            seg.writeInt(account.getiMoney());
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
            seg.writeInt(getLoginErrorTimeAndRefresh(account.getUserName()));
//            if (account.getSubscribeStatus() == Account.SUBSCRIBED ||
//                account.getMonthFee() >= Const.MONTH_MAX) {
//                seg.writeBoolean(false);
//            } else {
//                seg.writeBoolean(true);
//            }
//                if (account.getSubscribeStatus() == Account.SUBSCRIBED) {
//                    seg.writeInt(Const.MONTH_MAX);
//                } else {
//                    seg.writeInt(account.getMonthFee());
//                }
            write(seg);
            log.info("AccountID[" + a.getId() + "]Phone[" + a.getPhone() +
                     "]Logined SessionId[" + data.getSessionId() + "]GameCode["+a.getGameCode()+"]");

        }

    }

    public void loginError(int requestId,String cause){
        UWAPSegment seg = new UWAPSegment(ServerConstants.LOGIN_RESULT);
        seg.writeInt(requestId);
        seg.writeString(cause);
        write(seg);
    }


    protected void buy(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int cost = data.readInt();
        int id = data.readInt();
        log.info("AccountID[" + accountId + "]Cost[" + cost + "]");
        AccountState account = accountService.getAccount(accountId);
        Account a = null;
        if (account == null) {
            a = accountService.loadAccountById(accountId);
        } else {
            a = account.getAccount();
        }
        if (a != null) {
//            if (a.getSubscribeStatus() == Account.SUBSCRIBED) {
//                cost = (((int) ((long) (cost * 88) / 100)) / 100) * 100;
//            }
            synchronized (a) {
                if (a.getiMoney() >= cost) {
                    a.setiMoney(a.getiMoney() - cost);
                    accountService.saveAccount(a);
                    UWAPSegment seg = new UWAPSegment(ServerConstants.
                            BUY_RESULT);
                    seg.writeInt(id);
                    seg.writeBoolean(true);
                    seg.writeInt(a.getiMoney());
                    seg.writeInt(cost);
                    seg.writeString("");
                    write(seg);
                    log.info("AccountID[" + accountId + "] BuyID[" + id +
                             "] OK");
                } else {
                    UWAPSegment seg = new UWAPSegment(ServerConstants.
                            BUY_RESULT);
                    seg.writeInt(id);
                    seg.writeBoolean(false);
                    seg.writeInt(a.getiMoney());
                    seg.writeInt(cost);
                    seg.writeString("没有足够的i币");
                    write(seg);
                    log.info("AccountID[" + accountId + "] BuyID[" + id +
                             "] BuyError NotEnough");
                }
            }
        } else {
            UWAPSegment seg = new UWAPSegment(ServerConstants.BUY_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(false);
            seg.writeInt(0);
            seg.writeString("没有找到此帐号");
            write(seg);
            log.info("AccountID[" + accountId + "] BuyID[" + id +
                     "] BuyError NotFound");
        }
    }

    protected void playerRegister(UWAPData data) throws ITimesException {
        int requestId = -1;
        try {
            requestId = data.readInt();
            String name = data.readString();
            String phone = data.readString();
            String recommend = data.readString();
            int recommendAccountId = data.readInt();
            String model = data.readString(); //机器型号
            String version = data.readString();
            String[] charge = data.readStrings();
            String feeplan = data.readString();
            boolean needReturn = data.readBoolean();
            String gameCode = data.readString();
            name = name.trim();
            if (name.length() == 0)
                throw new ITimesException("帐号名不能为空", data.getSerial(),
                                          requestId, data.getAppType());
            if (name.getBytes("GBK").length > 16)
                throw new ITimesException("帐号名太长", data.getSerial(),
                                          requestId, data.getAppType());
            if (KeywordsUtil.isInvalidName(name.toLowerCase()))
                throw new ITimesException("帐号名出现非法字符", data.getSerial(),
                                          requestId, data.getAppType());
            if (!Utils.checkString(name, false))
                throw new ITimesException("帐号名出现非法字符", data.getSerial(),
                                          requestId, data.getAppType());
            String newName = KeywordsUtil.filterKeywords(name);
            if (!newName.equals(name))
                throw new ITimesException("帐号名出现非法字符", data.getSerial(),
                                          requestId, data.getAppType());
            if (!Utils.isValidMobilePhone(phone))
                throw new ITimesException("手机号有误", data.getSerial(),
                                          requestId, data.getAppType());
            int count = accountService.getAccountCountByPhone(phone);
            if (count == -1)
                throw new ITimesException("注册错误", data.getSerial(),
                                          data.getSessionId(), data.getAppType());
            if (count >= 3)
                throw new ITimesException("同一手机号只能注册3个帐号", data.getSerial(),
                                          requestId, data.getAppType());
            if (recommendAccountId != -1) {
                String r = accountService.getAccountName(recommendAccountId);
                if (r != null) {
                    recommend = r;
                }
            }
            Account account = accountService.loadAccountByName(name);
//            if(password.length()==0)
//                throw new ITimesException("密码不能为空",data.getSerial(),data.getSessionId(),data.getAppType());
//            if(password.getBytes("GBK").length>16)
//                throw new ITimesException("密码超过最大长度",data.getSerial(),data.getSessionId(),data.getAppType());
//            if(!Utils.checkString(password,false))
//                throw new ITimesException("密码出现非法字符",data.getSerial(),data.getSessionId(),data.getAppType());
            if (account != null) {
                if (account.getPhone().equals(phone)) {
                    throw new ITimesException(
                            "该帐号已经存在，如有问题请打客服电话：010-64465123。", data.getSerial(),
                            requestId, data.getAppType());
//                    UWAPSegment seg = new UWAPSegment(ClientConstants.ACCOUNT_REG_OK,data.getSerial(),data.getSessionId());
//                    seg.writeString(phone);
//                    write(seg);
//                    sender.send(phone,
//                                "恭喜您成功注册幻想i时代，帐户名：" + name + "，密码：" + account.getPassword() +
//                            "。祝您游戏愉快。客服电话：010-64465123。本条免费");
//                    log.info("AccountID["+account.getId()+"]Reregistered");
                } else
                    throw new ITimesException("已经存在同名帐号", data.getSerial(),
                                              requestId,
                                              data.getAppType());
            }
            String password = getPassword(RND);
            String cause = "";
            if (!needReturn) {
                if (version.indexOf("CCCCCC3G") >= 0) {
                    account = accountService.createNewAccount(name, password,
                            "", phone, recommend, 20000, "注册", false,
                            getChannel(version), 0, 0, model,gameCode);
                } else {
                    account = accountService.createNewAccount(name, password,
                            "", phone, recommend, 20000,
                            "注册" + version.substring(version.length() - 8), false,
                            getChannel(version), 0, 0, model,gameCode);
                }
            } else {
                account = accountService.createNewAccount(name, password, "",
                        phone, recommend, 20000, "", true, getChannel(version),
                        0, 0, model,gameCode);
            }
            if (account != null) {
                log.info(account.getUserName() + "Registered Version[" +
                         version + "]model[" + model+"]GameCode["+account.getGameCode()+"]");
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  ACCOUNT_REG_OK,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(requestId);
                seg.writeString(phone);
                seg.writeString(password);
                seg.writeBoolean(needReturn);
                write(seg);
                if (needReturn)
                    sender.send(phone,
                                "恭喜您注册明珠通行证，帐户：" + name + "，密码：" + password +
                                "。客服：010-64465123。本条免费", "0738A0000I");
            } else {
                throw new ITimesException("创建帐号错误", requestId,
                                          data.getSessionId(), data.getAppType());
            }
        } catch (ITimesException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ITimesException("创建帐号错误", requestId,
                                      requestId, data.getAppType());
        }
    }

    protected void relogin(UWAPData data) throws Exception {
        int requestId = data.readInt();
        String accountName = data.readString();
        String password = data.readString();
        Account account = accountService.loadAccountByNameAndPassword(
                accountName, password);
        if (account == null) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
                                              data.getSerial(),
                                              data.getSessionId());
            seg.writeInt(requestId);
            seg.write((byte) 2);
            write(seg);
            return;
        }
        synchronized (accountService) {
            AccountState a = accountService.getAccount(account.getId());
//            if(a!=null){
//                UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
//                                                  data.getSerial(),
//                                                  data.getSessionId());
//                seg.write((byte)2);
//                write(seg);
//            }else{
            long current = System.currentTimeMillis();
            boolean newCreated = false;
            if (a == null) {
                newCreated = true;
                a = new AccountState(account, current,
                                     sessionId);
            }
            if ((current - a.getTime()) < 30000L && !newCreated) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  RELOGIN_RESULT,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(requestId);
                seg.write((byte) 2);
                write(seg);
                return;
            }
            ConnectSession oldSession = a.getSession();
            if (oldSession!=null&&oldSession!=this) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.FORCELOGOUT);
                seg.writeInt(a.getId());
                oldSession.write(seg);
            }
            a.setSession(this);
            accountService.registry(a);
            registry(a);
//            if (a.getSession() != this) {
//                UWAPSegment seg = new UWAPSegment(ServerConstants.
//                                                  RELOGIN_RESULT,
//                                                  data.getSerial(),
//                                                  data.getSessionId());
//                seg.write((byte) 2);
//                write(seg);
//                return;
//            }
            a.setTime(current);
            a.setLastLiveTime(System.currentTimeMillis());

            UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
                                              data.getSerial(),
                                              data.getSessionId());
            seg.writeInt(requestId);
            seg.write((byte)0);
            seg.writeInt(account.getId());
            seg.writeString(account.getUserName());
            seg.writeString(account.getPassword());
            seg.writeString(account.getPhone());
            seg.writeInt(account.getModifyPasswordTimes());
            seg.writeInt(account.getiMoney());
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
//            if (account.getSubscribeStatus() == Account.SUBSCRIBED ||
//                account.getMonthFee() >= Const.MONTH_MAX) {
//                seg.writeBoolean(false);
//            } else {
//                seg.writeBoolean(true);
//            }
//            if (account.getSubscribeStatus() == Account.SUBSCRIBED) {
//                seg.writeInt(Const.MONTH_MAX);
//            } else {
//                seg.writeInt(account.getMonthFee());
//            }
            write(seg);
            log.info("AccountID[" + a.getId() + "]Relogined");
//            }
        }

    }

    public void chargeUp(UWAPData data) throws Exception {

    }
}
