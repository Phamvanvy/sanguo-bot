package com.pip.itimes.server.world;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.Packet;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.net.SessionRegistry;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import com.pip.itimes.net.Session;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.world.fee.FeeService;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.world.game.*;
import com.pip.itimes.server.stage.HouseData;
import java.util.Date;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.StoreService.CmccHistoryRequest;

public class AuthSession extends Session implements Runnable {

    private static final Logger log = Logger.getLogger(AuthSession.class);

    private SessionRegistry clientRegistry;
    private Configuration configuration;
    private FeeService feeService;
    private ConnectService connectService;
    private StoreService storeService;
    private PlayerService playerService;
    private HouseInstanceModel houseModel;
    private MailService mailService;
    private ChatService chatService;
    private AccountService loginService;
    private StageService stageService;
    private boolean loginOK = false;

    public AuthSession(IoSession session) {
        super(session);
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public void setHouseModel(HouseInstanceModel houseModel) {
        this.houseModel = houseModel;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setClientRegistry(SessionRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
    }

    public void setFeeService(FeeService feeService) {
        this.feeService = feeService;
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setLoginService(AccountService loginService){
        this.loginService = loginService;
    }

    public void setStageService(StageService stageService) {
		this.stageService = stageService;
	}

	private static int segNum = 0;

    public void handle(Packet packet) {
        log.debug("receive auth seg:" + (++segNum));
        UWAPData data = packet.datas[0];
        byte type = data.getAppType();
        try {
            switch (type) {
            case ClientConstants.ERROR:
                error(data);
                break;
            case ServerConstants.SERVER_LOGIN_OK:
                serverLoginOk(data);
                break;
//            case ClientConstants.ACCOUNT_REG_OK:
//                dispatchTo(packet);
//                break;
//            case ClientConstants.LOGIN_OK:
//                dispatchTo(packet);
//                break;
//            case ClientConstants.RELOGIN_RESULT:
//                dispatchTo(packet);
//                break;
                case ServerConstants.ADMIN_ACCOUNTINFO:
                    log.info("accountinfo receive");
                    dispatchTo(packet);
                    break;
                case ServerConstants.FEE_RESULT:
                    feeResult(data);
                    break;
                case ServerConstants.SYNC_IMONEY:
                    synciMoney(data);
                    break;
                case ServerConstants.MODIFY_PASSWORD_RESULT:
                    modifyPassowrdResult(data);
                    break;
                case ServerConstants.MODIFY_PHONE_RESULT:
                    modifyPhoneResult(data);
                    break;
                case ServerConstants.BUY_RESULT:
                    buyResult(data);
                    break;
                case ServerConstants.CHARGEUP_RESULT:
                    chargeResult(data);
                    break;
                case ClientConstants.LOGIN_OK:
                    loginOk(data);
                    break;
                case ClientConstants.ACCOUNT_REG_OK:
                    regOk(data);
                    break;
                case ClientConstants.QUICK_REG:
                    quickRegResult(data);
                    break;
                case ServerConstants.RELOGIN_RESULT:
                    reloginResult(data);
                    break;
                case ServerConstants.FORCELOGOUT:
                    forceLogout(data);
                    break;
                case ServerConstants.LOGIN_RESULT:
                    loginResult(data);
                    break;
                case ServerConstants.GET_ACCOUNTNAME_OK:
                    getAccountNameOk(data);
                    break;
                case ServerConstants.CMCC_GET_HISTORY_OK:
                    cmccGetHistoryOk(data);
                    break;
                case ServerConstants.CMCC_SMS_BUY_REQ_RESULT:
                    cmccSmsBuyReqResult(data);
                    break;
                case ServerConstants.CMCC_SMS_BUY_SUCC:
                    cmccSmsBuySucc(data);
                    break;
                //mengjie add
                case ServerConstants.CMCC_RECOMMEND_REQUEST_OK:
                    cmccRecommendSucc(data);
                    break;
                case ServerConstants.CMCC_SUBSCRIBE_RESULT:
                	CmccBusinessSucc(data);
                    break;
                case ServerConstants.CMCC_SUBSCRIBE_NOTIFY:
                	CmccBusinessreturn(data);
                    break;
                case ServerConstants.CMCC_QUERY_RECOMMEND_RESULT:
                	CmccRecommendinforeturn(data);
                    break;
                case ServerConstants.CMCC_PUSH_DOWNLOAD:
                	Cmccpushdownload(data);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void created() {

    }
    
    /*
     * 认证服务器通知短信购买道具成功。
     */
    private void cmccSmsBuySucc(UWAPData data) throws Exception {
        int requestId = data.readInt();
        int accountId = data.readInt();
        int playerId = data.readInt();
        String token = data.readString();
        log.info("ID[" + playerId + "]AccountID[" + accountId + "]Token[" + token + "] SmsBuySucc");
        StoreService.Request request = storeService.remove(requestId);
        if (request != null && request.session instanceof CmccConnectSession) {
            // 购买成功，发货
            ((CmccConnectSession)request.session).cmccSmsBuySucc(request);
        }
    }
    
    /*
     * 向认证服务器请求短信购买序列号的结果。
     */
    private void cmccSmsBuyReqResult(UWAPData data) throws Exception {
        int requestId = data.readInt();
        boolean result = data.readBoolean();
        int accountId = data.readInt();
        int playerId = data.readInt();
        String token = data.readString();
        if (result) {
            log.info("ID[" + playerId + "]AccountID[" + accountId + "]Token[" + token + "] SmsBuyReqOK");
            StoreService.Request request = storeService.get(requestId);
            if (request != null) {
                // 请求成功，向客户端PUSH购买脚本
                request.session.smsBuyReqOk(request, token);
            }
        } else {
            log.info("ID[" + playerId + "]AccountID[" + accountId + "] SmsBuyReqFail");
            StoreService.Request request = storeService.remove(requestId);
            if (request != null) {
                // 向客户端发送购买错误的消息
                UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR, request.serial);
                seg.write(ClientConstants.ISHOP_TRADE);
                seg.writeString("购买请求失败");
                connectService.writeTo(seg, playerId);
            }
        }
    }

    private void cmccGetHistoryOk(UWAPData data) throws Exception{
        log.info("history ok");
        int requestId = data.readInt();
        log.info("requestId:"+requestId);
        CmccHistoryRequest request = storeService.removeHistory(requestId);
        if(request!=null){
                log.info("rquest process");
                int count = data.readInt();
            UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST, request.serial, request.sessionId);
            seg.writeShort((short) 10);
            if(request.type==1){
                seg.writeString("点数消费历史记录");
            }else{
                seg.writeString("点数充值历史记录");
            }
            seg.write((byte) 0);
            seg.writeShort((short) count);
            for (int i = 0; i < count; i++) {
                int point = data.readInt();
                String date = data.readString();
                seg.writeInt(i);
//                seg.writeString((i + 1) + ". " + date + "充值"+ point + "点");
                if(request.type==2)
                        seg.writeString(date + " "+ point + "点");
                else if(request.type==1)
                        seg.writeString(date + " "+ point + "点");
                seg.writeInt(0x0); //listItem Color
            }
            request.session.write(seg);
        }
    }
    
    private String getDateString(String date){
       return date.substring(0,4)+"-"+date.substring(4,6)+"-"+date.substring(6,8);
    }

    private void forceLogout(UWAPData data) throws Exception{
        int accountId = data.readInt();
        connectService.forceLogout(accountId);
    }

    private void getAccountNameOk(UWAPData data) throws Exception{
        int requestId = data.readInt();
        AccountRequest request = loginService.removeRequest(requestId);
        if(request!=null){
            request.session.getAccountNameOk(data,request);
        }
    }

    private void loginResult(UWAPData data) throws Exception{
        int requestId = data.readInt();
        AccountRequest request = loginService.removeRequest(requestId);
        if(request!=null){
            request.session.loginResult(data,request);
        }
    }


    private void error(UWAPData data) throws Exception{
        byte appType = data.readByte();
        String msg = data.readString();
        int requestId = data.getSessionId();
        switch(appType){
            case ClientConstants.LOGIN:
            case ClientConstants.ACCOUNT_REG:
            case ClientConstants.QUICK_REG:
                accountError(requestId,msg);
                break;
            case ClientConstants.CMCC_CHARGE:
                cmccChargeError(requestId,msg);
                break;
            case ServerConstants.CMCC_GET_HISTORY:
                cmccGetHistoryError(requestId, msg);
                break;
        }
    }

    private void sendError(ConnectSession session,int serial,int sessionId,byte appType,String msg){
        UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,
                                          serial, sessionId);
        seg.write(appType);
        seg.writeString(msg);
        session.write(seg);
    }


    private void accountError(int requestId,String msg){
        AccountRequest request = loginService.removeRequest(requestId);
        if(request!=null){
            sendError(request.session,request.serial,request.sessionId,request.appType,msg);
        }
    }

    private void loginOk(UWAPData data) throws Exception{
        log.info("loginOk");
        int requestId = data.readInt();
        AccountRequest request = loginService.removeRequest(requestId);
        if(request!=null){
            log.info("loginOk1");
            LoginResult result = new LoginResult();
            result.accountId = data.readInt();
            result.name = data.readString();
            result.password = data.readString();
            result.phone = data.readString();
            result.modifyPasswordTimes = data.readInt();
            result.iMoney = data.readInt();
            result.isMonth = false;
            result.isSubscribe = false;
            result.loginErrorTime = 0;
            result.aRequest = request;
            //mengjie add
            data.readBoolean();
            data.readBoolean();
            data.readInt();
        	String tmp = data.readString();
        	log.info("AccountID[" + result.accountId + "]cityname CMCC [" + tmp +"]");
        	result.cityname = tmp;

            request.session.loginOk(result);
        }
    }

    private void reloginResult(UWAPData data) throws Exception{
        int requestId = data.readInt();
        AccountRequest request = loginService.removeRequest(requestId);
        if(request!=null){
            ReloginResult result = new ReloginResult();
            result.type = data.readByte();
            result.accountId = data.readInt();
            result.name = data.readString();
            result.password = data.readString();
            result.phone = data.readString();
            result.modifyPasswordTimes = data.readInt();
            result.iMoney = data.readInt();
            result.isMonth = data.readBoolean();
            result.isSubscribe = data.readBoolean();
            result.aRequest = request;
            request.session.reloginResult(result);
        }
    }

    private void regOk(UWAPData data) throws Exception{
        int requestId = data.readInt();
        AccountRequest request = loginService.removeRequest(requestId);
        if(request!=null){
            String phone = data.readString();
            String password = data.readString();
            request.session.regOk(password,request.sessionId,request.serial);
        }
    }

    private void quickRegResult(UWAPData data) throws Exception{
        int requestId = data.readInt();
        AccountRequest request = loginService.removeRequest(requestId);
        if(request!=null){
            int accountId = data.readInt();
            String accountName = data.readString();
            String password = data.readString();
            String playerName = data.readString();
            byte type = data.readByte();
            request.session.quickRegResult(accountId, accountName, password, type, request.model, request.sessionId,
                                           request.serial);
        }
    }

    private void cmccChargeError(int requestId,String msg){
        StoreService.ChargeRequest request = storeService.removeCharge(requestId);
        if(request!=null){
            sendError(request.connectSession, request.serial, request.sessionId, ClientConstants.CMCC_CHARGE, msg);
        }
    }

    private void cmccGetHistoryError(int requestId, String msg){
        CmccHistoryRequest request = storeService.removeHistory(requestId);
        if(request!=null){
            sendError(request.session, request.serial, request.sessionId, ClientConstants.CMCC_HISTORY, msg);
        }
    }
    
    private void chargeResult(UWAPData data) throws Exception {
        int id = data.readInt();
        boolean success = data.readBoolean();
        int iMoney = data.readInt();
        String cause = data.readString();
        StoreService.ChargeRequest request = storeService.removeCharge(id);
        if (request != null) {
            if (success) {
                if(request.playerId!=-1){
//                    WorldPlayer player = playerService.loadWorldPlayer(request.
//                            playerId);
                    WorldPlayer player = playerService.getWorldPlayerAndCatch(request.playerId);
                    if (player != null) {
                        player.setLongIMoney(iMoney);
                    }
                    request.connectSession.sendMessage(cause,request.serial,request.sessionId);
                    playerService.releasePlayer(player);
                }else{
                    UWAPSegment seg = new UWAPSegment(ClientConstants.CMCC_CHARGE_OK,request.serial,request.sessionId);
                    seg.writeString(cause);
                    request.connectSession.write(seg);
                }

            } else {
                request.connectSession.sendMessage(cause,request.serial,request.sessionId);
//                connectService.sendError(request.playerId, cause,
//                                         request.serial, (byte) - 1);
            }
        }
    }

    private void buyResult(UWAPData data) throws Exception {
        int id = data.readInt();
        StoreService.Request request = storeService.remove(id);
        BuyResult result = new BuyResult();
        result.success = data.readBoolean();
        result.iMoney = data.readInt();
        result.cost = data.readInt();
        result.realCost = result.cost;

        // 如果是PIP版本使用卓望认证服务器通过话费购买道具，消耗i币为价格的3.6倍
        if (request.session instanceof PipConnectSession) {
            WorldPlayer wp = playerService.getWorldPlayer(request.playerId);
            log.info("[CMCC_BUY_OK]ID[" + request.playerId + "]ACCOUNTID[" + wp.getPlayer().getAccountId() + "]RESULT[" + result.success + "]COST[" + result.cost + "]CONSUMECODE[" + request.consumeCode + "]");
            result.iMoney = wp == null ? 0 : wp.getLongIMoney();
            result.cost = result.cost * 36 / 10;
            result.realCost = 0;
        }
        
        result.cause = data.readString();
        request.session.buyResult(result,request);
    }


    private void modifyPassowrdResult(UWAPData data) throws Exception {
        byte type = data.readByte();
        int playerId = data.readInt();
        if (type == 0) {
            String password = data.readString();
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString("修改密码成功。您的新密码是：" + password + "，请牢记。");
            connectService.writeTo(seg, playerId);
            write(seg);
            Changed changed = new Changed();
            changed.setProperty(Changed.PASSWORD, password);
            connectService.sendGetItem(changed, playerId, (byte) 10);
        } else {
            String msg = data.readString();
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString(msg);
            connectService.writeTo(seg, playerId);
        }
    }

    private void modifyPhoneResult(UWAPData data) throws Exception {
        byte type = data.readByte();
        int playerId = data.readInt();
        if (type == 0) {
            String phone = data.readString();
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString("修改绑定手机成功。" + "你的新绑定手机号是：" + phone + "。");
            connectService.writeTo(seg, playerId);
        } else {
            String msg = data.readString();
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString(msg);
            connectService.writeTo(seg, playerId);
        }
    }

    private void feeResult(UWAPData data) throws Exception {
        byte type = data.readByte();
        int accountId = data.readInt();
        int feeId = data.readInt();
        feeService.feeResult(accountId, feeId);
    }

    private void synciMoney(UWAPData data) throws Exception {
        int accountId = data.readInt();
        int iMoney = data.readInt();
        boolean isMonth = data.readBoolean();
        boolean isSubscribe = data.readBoolean();
        feeService.synciMoney(accountId, iMoney, isMonth, isSubscribe);
    }
    
    /*
     * 登录认证成功，同步所有在线用户状态。
     */
    private void serverLoginOk(UWAPData data) {
        loginOK = true;
        
        // 只有卓望版本才同步在线用户状态
        String factory = configuration.getString("connectsessionfactory");
        if ("cmcc".equals(factory)) {
            WorldPlayer[] ps = playerService.getPlayers();
            for (WorldPlayer p : ps) {
                if (p.getState() == WorldPlayer.ONLINE && p.getPlayer() != null) {
                    UWAPSegment seg = new UWAPSegment(ServerConstants.LIVE_NOTIFY);
                    seg.writeInt(p.getAccountId());
                    write(seg);
                }
            }
        }
        
        // 启动定时发送用户在线状态通知的线程
        new Thread(this, "CMCCAuthSessionThread").start();
    }

    private void dispatchTo(Packet packet) {
        UWAPData data = packet.datas[0];
        AdminSession session = (AdminSession) clientRegistry.getSession(data.
                getSessionId());
        if (session != null) {
            session.handleServer(packet);
        }
    }

    public void idle(IdleStatus status) {

    }

    public void opened() {
        UWAPSegment seg = new UWAPSegment(ServerConstants.SERVER_LOGIN);
        String serverId = (String) configuration.getProperty(ServerConstants.SERVERID);
        String serverName = (String) configuration.getProperty("cmccserverpassword");
        seg.writeString(serverId);
        seg.writeString(serverName);
        write(seg);
    }

    public void closed() {
        // 认证连接断开，启动重复连接过程
        loginOK = false;
        Server.instance.authSessionClosed(this);
    }
    
    public void run() {
        while (loginOK) {
            // 每分钟检查所有连接的状态，找出所有超过5分钟没有同步状态带cmccUserId的连接，
            // 向卓望认证服务器发送CMCC_LIVE_NOTIFY包。
            try {
                Thread.sleep(60000);
            } catch (Exception e) {
            }
            try {
                ConnectSession[] cc = connectService.getConnectSession();
                for (ConnectSession s : cc) {
                    if (s == null) {
                        continue;
                    }
                    Object[] clients = s.sessionId2Clients.values().toArray();
                    for (Object o : clients) {
                        Client c = (Client)o;
                        if (c.cmccUserId != null && c.cmccUserId.length() > 0 &&
                                System.currentTimeMillis() - c.lastReportTime > 300000L) {
                            UWAPSegment seg = new UWAPSegment(ServerConstants.CMCC_LIVE_NOTIFY);
                            seg.writeString(c.cmccUserId);
                            write(seg);
                            c.lastReportTime = System.currentTimeMillis();
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }
    
    //mengjie add
    
    /*
     * 认证服务器返回推荐成功后的结果。
     */
    private void cmccRecommendSucc(UWAPData data) throws Exception {
        int requestId = data.readInt();
        String userId = data.readString();
        int accountId = data.readInt();
        int playerId = data.readInt();
        String tel = data.readString();
        boolean result = data.readBoolean();
        String errormsg = data.readString();
        if (result) {
            log.info("ID[" + playerId + "]AccountID[" + accountId + "]cmccRecommendSucc");
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString("推荐成功。" + "推荐的手机号是：" + tel + "。");
            connectService.writeTo(seg, playerId);
        } else {
            log.info("ID[" + playerId + "]AccountID[" + accountId + "] cmccRecommend Fail [" +errormsg+ "]");
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString("推荐失败。" + errormsg);
            connectService.writeTo(seg, playerId);
        }
    }

    /*
     * 认证服务器返回定制业务后的结果。
     */
    private void CmccBusinessSucc(UWAPData data) throws Exception {
        int requestId = data.readInt();
        String userId = data.readString();
        int accountId = data.readInt();
        int playerId = data.readInt();
        int type = data.readInt();
        boolean result = data.readBoolean();
        if (result) {
            log.info("ID[" + playerId + "]AccountID[" + accountId + "]CmccBusinessSucc");
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString("定制申请发送成功。");
            connectService.writeTo(seg, playerId);
        } else {
            log.info("ID[" + playerId + "]AccountID[" + accountId + "] CmccBusiness Fail");
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString("定制申请发送失败。请稍后再试。" );
            connectService.writeTo(seg, playerId);
        }
    }
    /*
     * 认证服务器返回定制业务的最终定制结果。
     */
    private void CmccBusinessreturn(UWAPData data) throws Exception {
        String userId = data.readString();
        int type = data.readInt();
        boolean result = data.readBoolean();
        int playerId = -1;
        ConnectSession[] cc = connectService.getConnectSession();
        for (ConnectSession s : cc) {
            if (s == null) {
                continue;
            }
            Object[] clients = s.sessionId2Clients.values().toArray();
            for (Object o : clients) {
                Client c = (Client)o;
                if (c.cmccUserId != null && c.cmccUserId.length() > 0 &&
                		userId.equals(c.cmccUserId)) {
                	playerId = c.playerId;
                	if (result) {
                        log.info("ID[" + playerId + "]AccountID[" + c.accountId + "]CmccBusinessSucc");
                        UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
                        seg.writeString("定制成功。");
                        connectService.writeTo(seg, playerId);
                    } else {
                        log.info("ID[" + playerId + "]AccountID[" + c.accountId + "] CmccBusiness Fail");
                        UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
                        seg.writeString("定制失败。请稍后再试。" );
                        connectService.writeTo(seg, playerId);
                    }
                	break;
                }
            }
        }
        
    }
    /*
     * 认证服务器返回推荐人的结果。
     */
    private void CmccRecommendinforeturn(UWAPData data) throws Exception {
    	int requestId = data.readInt();
    	String userId = data.readString();
        int [] accounts = data.readInts();

        int playerId = -1;
        ConnectSession[] cc = connectService.getConnectSession();
        for (ConnectSession s : cc) {
            if (s == null) {
                continue;
            }
            Object[] clients = s.sessionId2Clients.values().toArray();
            for (Object o : clients) {
                Client c = (Client)o;
                if (c.cmccUserId != null && c.cmccUserId.length() > 0 &&
                		userId.equals(c.cmccUserId)) {
                	playerId = c.playerId;
                	String tmp = "您推荐的好友有：";
                	for(int i=0;i<accounts.length;i++){
                		int accountId = accounts[i];
                		Player[] players = playerService.getPlayerByAccountId(accountId);
                        if (players.length != 0) {
                        	int playerleveltmp = 0;
                        	int playerint = 0;
                        	for (int j = 0; i < players.length; i++) {
                        		if (playerleveltmp<players[j].getLevel()){
                        			playerleveltmp = players[j].getLevel();
                        			playerint = j;
                        		}
                            }
                        	tmp = tmp + "\n"+players[playerint].getPlayerName()+";";
                        }
                	}
                	log.info("ID[" + playerId + "]AccountID[" + c.accountId + 
                			"]CmccRecommendinforeturn-count["+accounts.length+"]");
                    UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
                    seg.writeString(tmp);
                    connectService.writeTo(seg, playerId);
                	break;
                }
            }
        }
        
    }
    //mengjie add
    /**
     * 通知世界服务器用户需要通过卓望平台下载客户端。
     * userId			String			用户ID
     * accountId		int				帐号ID
     * playerId			int				角色ID
     * url				String			下载地址
     */
    private void Cmccpushdownload(UWAPData data) throws Exception {
    	String userId = data.readString();
        int  accountId = data.readInt();
        int  playerId = data.readInt();
        String url = data.readString();
        
        // 把下载地址记录到用户的Client对象里
        ConnectSession[] cc = connectService.getConnectSession();
        for (ConnectSession s : cc) {
            if (s == null) {
                continue;
            }
            Client c = (Client)s.accountId2Clients.get(accountId);
            if (c != null) {
            	c.cmccDownloadUrl = url;
            }
        }
		log.info("CMCC_PUSH_DOWNLOAD ID[" + playerId + "]AccountID[" + accountId + "]userId[" + userId + "] URL[" + url + "]");
    }
    /**
     * 向认证服务器通知用户升级。
     * @param userID
     * @param accountID
     * @param playerID
     * @param level
     */
    public void sendLevelUpNotify(String userID, int accountID, int playerID, int level) {
        UWAPSegment seg = new UWAPSegment(ServerConstants.CMCC_LEVELUP_NOTIFY);
        seg.writeString(userID);
        seg.writeInt(accountID);
        seg.writeInt(playerID);
        seg.writeInt(level);
        write(seg);
    }
}
