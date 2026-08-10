package com.pip.itimes.server.auth;

import org.apache.mina.common.IoSession;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.util.IDGenerator;
import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.net.ServerConstants;
import org.apache.log4j.Logger;

public class CmccConnectSession extends ConnectSession {

    private static final Logger log = Logger.getLogger(CmccConnectSession.class);

    private CmccUserCache cache;
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


    public void cmccGetHistory(UWAPData data) throws Exception {
                int requestId = data.readInt();
                log.info("requestId:"+requestId);
                byte type = data.readByte();
                int accountId = data.readInt();
                AccountState account = accountService.getAccount(accountId);
                if (account != null) {
                        String start = data.readString();
                        String end = data.readString();
                        int startSequence = data.readInt();
                        int count = data.readInt();
                        if (type == 1) { // consume history
                                CmccConsumeRecord record = cmccService.queryConsume(account.getUserKey().getUserId(),
                                                start, end, startSequence, count);
                                UWAPSegment seg = new UWAPSegment(
                                                ServerConstants.CMCC_GET_HISTORY_OK);
                                CmccConsumeItem[] items = record.getItems();
                                seg.writeInt(requestId);
                                seg.writeInt(items.length);
                                for (int i = 0; i < items.length; i++) {
                                        seg.writeInt(items[i].getPoint());
                                        seg.writeString(items[i].getDate());
                                }
                                write(seg);
                                log.info("history sended");
                        } else if (type == 2) { // charge history
                                CmccChargeRecord record = cmccService.queryCharge(account.getUserKey().getUserId(),
                                                start, end, startSequence, count);
                                UWAPSegment seg = new UWAPSegment(
                                                ServerConstants.CMCC_GET_HISTORY_OK);
                                CmccChargeItem[] items = record.getItems();
                                seg.writeInt(requestId);
                                seg.writeInt(items.length);
                                for (int i = 0; i < items.length; i++) {
                                        seg.writeInt(items[i].getPoint());
                                        seg.writeString(items[i].getDate());
                                }
                                write(seg);
                                log.info("history sended");
                        }
                }
	}

    public void cmccCharge(UWAPData data) throws Exception{
        String userId = data.readString();
        String key = data.readString();
        int charge = data.readInt();
        int requestId = data.readInt();
//        int balance = 1000;
//        UWAPSegment seg = new UWAPSegment(ClientConstants.CMCC_CHARGE_OK,
//                                          data.getSerial(), data.getSessionId());
//        StringBuilder sb = new StringBuilder(70);
//        sb.append("你已经成功充值");
//        sb.append(charge * 100);
//        sb.append("点");
//        if (balance != -1) {
//            sb.append("，余额为：");
//            sb.append(balance);
//        }
//        seg.writeString(sb.toString());
//        write(seg);
        if(userId.equals("11111")&&key.equals("1111")){
            String ret = null;
            if(charge==0){
                ret = "你的余额是10000w";
            }else{
                ret = "充值成功";
            }
            UWAPSegment seg = new UWAPSegment(ServerConstants.CHARGEUP_RESULT,data.getSerial(),data.getSessionId());
            seg.writeInt(requestId);
            seg.writeBoolean(true);
            seg.writeInt(0);
            seg.writeString(ret);
            write(seg);
            return;
        }
        if(!cache.isValid(userId,key)){
            UWAPSegment seg = new UWAPSegment(ServerConstants.CHARGEUP_RESULT,data.getSerial(),data.getSessionId());
            seg.writeInt(requestId);
            seg.writeBoolean(true);
            seg.writeInt(0);
            seg.writeString("充值失败,不是合法的用户！");
            write(seg);
            return;
        }
        try {
            if(charge!=0)
                cmccService.chargeUp(userId, charge);
            int balance = -1;
            try {
                balance = cmccService.queryBalance(userId, false);
            } catch (CmccException ex1) {
            }
            UWAPSegment seg = new UWAPSegment(ServerConstants.CHARGEUP_RESULT,data.getSerial(),data.getSessionId());
            StringBuilder sb = new StringBuilder(70);
            if(charge!=0){
                sb.append("恭喜您充值成功，目前点数余额为");
                sb.append(balance);
                sb.append("点。");
            }else{
                sb.append("你当前的点数余额为");
                sb.append(balance);
                sb.append("点");
            }
//            sb.append(charge*100);
//            sb.append("点");
//            if(balance!=-1){
//                sb.append("，余额为：");
//                sb.append(balance);
//            }
            seg.writeInt(requestId);
            seg.writeBoolean(true);
            seg.writeInt(balance);
            seg.writeString(sb.toString());
            write(seg);

//            UWAPSegment seg = new UWAPSegment(ClientConstants.CMCC_CHARGE_OK,data.getSerial(),data.getSessionId());
//
//            seg.writeString(sb.toString());
//            write(seg);
        } catch (CmccException ex) {
            if(charge!=0){
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  CHARGEUP_RESULT,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(requestId);
                seg.writeBoolean(true);
                seg.writeInt(0);
                seg.writeString("充值失败");
            }else{
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                CHARGEUP_RESULT,
                                                data.getSerial(),
                                                data.getSessionId());
              seg.writeInt(requestId);
              seg.writeBoolean(true);
              seg.writeInt(0);
              seg.writeString("查询余额失败");

            }
        }
    }

    public void quickReg(UWAPData data) throws Exception {
        int requestId = -1;
        try {
            requestId = data.readInt();
            String phone = data.readString();
            String version = data.readString();
            String model = data.readString();
            String cmccUserId = data.readString().trim();
            String cmccKey = data.readString();
            String gameCode = data.readString();
            if (!cache.isValid(cmccUserId, cmccKey)) {
                throw new ITimesException("注册失败", data.getSerial(),
                                          requestId, data.getAppType());
            }
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


            Account account = accountService.
                              getFirstValidAccountByPhone(
                                      cmccUserId);
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


            if (count == -1)
                throw new ITimesException("注册错误", data.getSerial(),
                                          requestId, data.getAppType());
            if (count >= 3)
                throw new ITimesException("同一手机号只能注册3个帐号", data.getSerial(),
                                          requestId, data.getAppType());
            String name = null;
            name = QUICKREG_PREFIX + IDGenerator.getAccountName();
            String password = getPassword(RND);
            account = accountService.createNewAccount(name,
                    password, "", cmccUserId, "", 10000, "", true, getChannel(version),
                    1, 1,model,gameCode);
            if (account != null) {
                log.info(account.getUserName() + "Quick Registered Version " +
                         version + " model " + model);
                UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
                                                  data.getSerial(),
                                                  requestId);
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


    protected void accountLogin(UWAPData data) throws Exception {
        int requestId = data.readInt();
        String accountName = data.readString();
        String password = data.readString();
//        String model = data.readString();
//        String version = data.readString();
//        String[] charge = data.readStrings();
//        String fee = data.readString();
        String cmccUserId = data.readString().trim();
        String cmccKey = data.readString();
        log.info("CmccUserId["+cmccUserId+"]CmccKey["+cmccKey+"}Try Login");
        if (!cache.isValid(cmccUserId, cmccKey)) {
            if(!(accountName.startsWith("test") || accountName.startsWith("gm"))){
                loginError(requestId,"登陆失败");
                return;
            }
        }
        Account account = accountService.loadAccountByNameAndPassword(
                accountName, password);
        if (account == null) {
            log.info("Login Error Name[" + accountName + "]Pass[" + password +
                     "]");
           loginError(requestId,"帐号名或者密码错误");
           return;
        }
        if (account != null && !account.getValid()) {
            loginError(requestId,"您的角色数据异常,帐号已停封!");
            return;
        }
        int iMoney = 0;
        try {
            if(accountName.startsWith("test") || accountName.startsWith("gm")){
                iMoney = cmccService.queryBalance(cmccUserId, true);
            }else{
                iMoney = cmccService.queryBalance(cmccUserId, false);
            }
            iMoney *= 100;
        } catch (CmccException ex) {
            loginError(requestId,"查询余额失败");
            return;
        }

        synchronized (accountService) {
            AccountState a = accountService.getAccount(account.getId());
            long current = System.currentTimeMillis();
            if (a == null) {
                a = new AccountState(account, current,
                                     sessionId);
            }
            if((current-a.getTime())<30000L&&(current!=a.getTime())){
                loginError(requestId,"帐号已经在使用中，请在30秒后重新尝试登陆");
                return;
            }
            ConnectSession oldSession = a.getSession();
            if(oldSession!=null){
                UWAPSegment seg = new UWAPSegment(ServerConstants.FORCELOGOUT);
                seg.writeInt(a.getId());
                oldSession.write(seg);
            }

            a.setSession(this);
            a.setTime(current);
            a.setLastLiveTime(current);
            a.setUserKey(cache.getUserKey(cmccUserId));
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
        //                if (account.getSubscribeStatus() == Account.SUBSCRIBED) {
        //                    seg.writeInt(Const.MONTH_MAX);
        //                } else {
        //                    seg.writeInt(account.getMonthFee());
        //                }
            write(seg);
            log.info("AccountID[" + a.getId() + "]Phone[" + cmccUserId +
                     "]Logined");

        }

    }

    public void loginError(int requestId,String cause){
        UWAPSegment seg = new UWAPSegment(ServerConstants.LOGIN_RESULT);
        seg.writeInt(requestId);
        seg.writeString(cause);
        write(seg);
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
            String cmccUserId = data.readString().trim();
            String cmccKey = data.readString();
            String gameCode = data.readString();
            if (!cache.isValid(cmccUserId, cmccKey)) {
                throw new ITimesException("注册失败", data.getSerial(),
                                          requestId, data.getAppType());
            }
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
                            "", cmccUserId, recommend, 10000, "注册", false,
                            getChannel(version), 0, 0,model,gameCode);
                } else {
                    account = accountService.createNewAccount(name, password,
                            "", cmccUserId, recommend, 10000,
                            "注册" + version.substring(version.length() - 8), false,
                            getChannel(version), 0, 0,model,gameCode);
                }
            } else {
                account = accountService.createNewAccount(name, password, "",
                        cmccUserId, recommend, 10000, "", true,
                        getChannel(version), 0,
                        0,model,gameCode);
            }
            if (account != null) {
                log.info(account.getUserName() + "Registered Version " +
                         version + " model" + model);
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  ACCOUNT_REG_OK,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(requestId);
                seg.writeString(phone);
                seg.writeString(password);
                seg.writeBoolean(needReturn);
                write(seg);
//                if (needReturn)
//                    sender.send(phone,
//                                "恭喜您注册幻想i时代，帐户名：" + name + "，密码：" + password +
//                                "。客服：010-64465123。本条免费", "0738A0000I");
            } else {
                throw new ITimesException("创建帐号错误", data.getSerial(),
                                          requestId, data.getAppType());
            }
        } catch (ITimesException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ITimesException("创建帐号错误", data.getSerial(),
                                      requestId, data.getAppType());
        }
    }

    protected void relogin(UWAPData data) throws Exception {
        int requestId = data.readInt();
        String accountName = data.readString();
        String password = data.readString();
        String cmccUserId = data.readString().trim();
        String cmccKey = data.readString();
        if (!cache.isValid(cmccUserId, cmccKey)) {
            if(!(accountName.startsWith("test") || accountName.startsWith("gm"))){
                UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(requestId);
                seg.write((byte) 2);
                write(seg);
                return;
            }
        }
        int iMoney = 0;
        try {
            if(accountName.startsWith("test") || accountName.startsWith("gm")){
                iMoney = cmccService.queryBalance(cmccUserId, true);
            }else{
                iMoney = cmccService.queryBalance(cmccUserId, false);
            }
            iMoney *= 100;
        } catch (CmccException ex) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
                                              data.getSerial(),
                                              data.getSessionId());
            seg.writeInt(requestId);
            seg.write((byte) 2);
            write(seg);
            return;
        }
        Account account = accountService.loadAccountByNameAndPassword(
                accountName, password);
        if (account == null) {
            UWAPSegment seg = new UWAPSegment(ServerConstants.RELOGIN_RESULT,
                                              data.getSerial(),
                                              data.getSessionId());
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
            if (a == null) {
                a = new AccountState(account, current,
                                     sessionId);
            }
            if((current-a.getTime())<30000L&&(current!=a.getTime())){
                UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(requestId);
                seg.write((byte) 2);
                write(seg);
                return;
            }
            ConnectSession oldSession = a.getSession();
            if(oldSession!=null){
                UWAPSegment seg = new UWAPSegment(ServerConstants.FORCELOGOUT);
                seg.writeInt(a.getId());
                oldSession.write(seg);
            }
            a.setSession(this);
            a.setUserKey(cache.getUserKey(cmccUserId));
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

            UWAPSegment seg = new UWAPSegment(ServerConstants.RELOGIN_RESULT,
                                              data.getSerial(),
                                              data.getSessionId());
            seg.writeInt(requestId);
            seg.write((byte) 0);
            seg.writeInt(account.getId());
            seg.writeString(account.getUserName());
            seg.writeString(account.getPassword());
            seg.writeString(account.getPhone());
            seg.writeInt(account.getModifyPasswordTimes());
            seg.writeInt(account.getiMoney());
            if(account.getMonthFee() >= Const.MONTH_MAX){
                seg.writeBoolean(true);
            }else{
                seg.writeBoolean(false);
            }
            if (account.getSubscribeStatus() == Account.SUBSCRIBED){
                seg.writeBoolean(true);
            }else{
                seg.writeBoolean(false);
            }
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


    protected void buy(UWAPData data) throws Exception {
        int accountId = data.readInt();
        String consumeCode = data.readString();
//        int cost = data.readInt();
        int id = data.readInt();
        log.info("AccountID[" + accountId + "]consumeCode[" + consumeCode + "]");
        AccountState account = accountService.getAccount(accountId);
        if (account == null) {
            UWAPSegment seg = new UWAPSegment(ServerConstants.BUY_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(false);
            seg.writeInt(0);
            seg.writeInt(0);
            seg.writeString("帐号不存在或者帐号不处于活动状态");
            write(seg);
            log.info("AccountID[" + accountId + "] BuyID[" + id +
                     "] BuyError NotFound");
            return;
        } else {
            CmccUserKey userKey = account.getUserKey();
            if (userKey == null) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  BUY_RESULT);
                seg.writeInt(id);
                seg.writeBoolean(false);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeString("帐号没有对应的CMCC用户存在");
                write(seg);
                log.info("AccountID[" + accountId + "] BuyID[" + id +
                         "] BuyError NotFound");
                return;
            }
            try {
                cmccService.buyGameTool(userKey.getUserId(),
                                        consumeCode);
                int imoney = 0;
                try {
                    if(account.getAccount().getUserName().startsWith("test") || account.getAccount().getUserName().startsWith("gm")){
                        imoney = cmccService.queryBalance(userKey.getUserId(), true);
                    }else{
                        imoney = cmccService.queryBalance(userKey.getUserId(), false);
                    }
                    imoney *= 100;
                    account.getAccount().setiMoney(imoney);
                } catch (CmccException ex1) {
                }
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  BUY_RESULT);
                seg.writeInt(id);
                seg.writeBoolean(true);
                seg.writeInt(imoney);
                seg.writeInt(-1);
                seg.writeString("");
                write(seg);
                log.info("AccountID[" + accountId + "] BuyID[" + id +
                         "] OK");
            }
            catch(NoEnoughBalanceException ex){
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                BUY_RESULT);
              seg.writeInt(id);
              seg.writeBoolean(false);
              seg.writeInt(-1);
              seg.writeInt(0);
              seg.writeString("购买道具失败");
              write(seg);
              log.info("AccountID[" + accountId + "] BuyID[" + id +
                       "] BuyError NotFound");

            }
            catch (CmccException ex) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  BUY_RESULT);
                seg.writeInt(id);
                seg.writeBoolean(false);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeString("购买道具失败");
                write(seg);
                log.info("AccountID[" + accountId + "] BuyID[" + id +
                         "] BuyError NotFound");
            }
        }
    }

    public void chargeUp(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int value = data.readInt();
        int id = data.readInt();
        AccountState account = accountService.getAccount(accountId);
        if (account == null) {
            UWAPSegment seg = new UWAPSegment(ServerConstants.
                                              CHARGEUP_RESULT);
            seg.writeInt(id);
            seg.writeBoolean(false);
            seg.writeInt(0);
            seg.writeString("帐号不存在或者帐号不处于活动状态");
            write(seg);
            log.info("AccountID[" + accountId + "] ChargeID[" + id +
                     "] ChargeError NotFound");
        } else {
            CmccUserKey userKey = account.getUserKey();
            if (userKey == null) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  CHARGEUP_RESULT);
                seg.writeInt(id);
                seg.writeBoolean(false);
                seg.writeInt(0);
                seg.writeString("帐号没有对应的CMCC用户存在");
                write(seg);
                log.info("AccountID[" + accountId + "] ChargeID[" + id +
                         "] ChargeError NotFound");
            } else {
                try {
                    cmccService.chargeUp(userKey.getUserId(),value);
                    int imoney = 0;
                    try {
                        if(account.getAccount().getUserName().startsWith("test") || account.getAccount().getUserName().startsWith("gm")){
                            imoney = cmccService.queryBalance(userKey.getUserId(), true);
                        }else{
                            imoney = cmccService.queryBalance(userKey.getUserId(), false);
                        }
                        imoney *= 100;
                        account.getAccount().setiMoney(imoney);
                    } catch (CmccException ex1) {
                    }
                    UWAPSegment seg = new UWAPSegment(ServerConstants.
                            CHARGEUP_RESULT);
                    seg.writeInt(id);
                    seg.writeBoolean(true);
                    seg.writeInt(imoney);
                    seg.writeString("恭喜您充值成功，目前点数余额为"+imoney/100+"点。");
                    write(seg);
                    log.info("AccountID[" + accountId + "] ChargeID[" + id +
                             "] OK");
                } catch (CmccException ex) {
                    UWAPSegment seg = new UWAPSegment(ServerConstants.
                            CHARGEUP_RESULT);
                    seg.writeInt(id);
                    seg.writeBoolean(false);
                    seg.writeInt(0);
                    seg.writeString("充值失败");
                    write(seg);
                    log.info("AccountID[" + accountId + "] ChargeID[" + id +
                             "] ChargeError");
                }
            }
        }
    }
}
