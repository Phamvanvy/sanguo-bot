package com.pip.itimes.server.world;

import com.pip.itimes.net.UWAPData;
import com.pip.itimes.server.ITimesException;
import com.pip.accountskeleton.*;
import com.pip.net.message.gameaccount.*;

import org.apache.mina.common.IoSession;
import com.pip.itimes.server.world.StoreService.Request;
import com.pip.itimes.server.world.game.HouseException;
import com.pip.itimes.server.bean.IMoneyCard;
import com.pip.itimes.server.bean.Ibuy;
import com.pip.itimes.server.bean.Mate;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.IItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.net.UWAPDataReadAndWrite;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IStoreGroup;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.HouseData;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.PlayerDataException;
import com.pip.itimes.server.stage.Tips;
import com.pip.itimes.server.world.fee.ChargePlan;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.fee.FeePlan;

public class PipConnectSession extends ConnectSession2 {
	
//	//新快速注册标志
//	private byte fastRegFlag ;
	
//	//模拟登陆需要保留versionString.这里进行保留
//	private String versionString;
//	//模拟登陆需要保留电话号码.这里进行保留
//	private String realPhone;
	//快速注册或者快速进入的角色名
//	private String roleName;

	public PipConnectSession(IoSession session) {
        super(session);
    }

    public void quickReg(UWAPData data) throws Exception {
        String phone = data.readString();
        String versionString = data.readString();		//added by Jeremy:版本号
        String model = data.readString();
        
        // PiP版本现在也可能传卓望平台用户信息上来了
        // 新版本再多一个手机号参数
        String cmccUserId = "";
        String cmccKey = "";
        String realPhone = "";
        try {
            cmccUserId = data.readString().trim();
            cmccKey = data.readString();
            realPhone = cutPhone(data.readString());
        } catch (Exception e) {
        }
        log.info("CmccUserId[" + cmccUserId + "]Phone[" + realPhone + "]Version[" + versionString + "]Model[" + model + "]Try QuickReg");
        
        byte fastRegFlag = 0;
        try{
        	fastRegFlag = data.readByte();
        }catch(Exception e){
        	
        }
        // 检查版本是否支持
        String v = cutVersion(versionString);
        Version version = versionService.getVersion(v);
        if (version == null) {
            throw new ITimesException("不支持的版本", data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }
        if (version.getStatus() == Version.STATUS_CANCELED) {
            throw new ITimesException(version.getMessage(), data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }
        if (!version.isCanReg()) {
            throw new ITimesException("此版本不支持注册功能", data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }

        // 检查是否明珠幻想的内测服务器
        if (Server.demoForVersion2) {
        	throw new ITimesException("此版本不支持注册功能", data.getSerial(),
                    data.getSessionId(),
                    data.getAppType());
        }
        
        // 保存客户端信息
        Client client = getAndCreateClient(data.getSessionId());
        client.cmccUserId = cmccUserId.trim();
        client.cmccKey = cmccKey;
        client.realPhone = realPhone;
        client.version = version;
        client.model = model;
        client.rawVersion = versionString;
        client.channel = cutChannel(versionString);
        client.fastRegFlag = fastRegFlag;
        client.realPhone = realPhone;

        // 把客户端IP地址放到发给认证服务器的版本号字符串里，格式为：
        // 3.1-CCCCCPiP-113123121/202.38.64.1
        String reportVersion = versionString;
        if (client.getFromIp().trim().length() > 0) {
            String[] tmp = versionString.split("-");
            if (tmp.length > 2) {
                reportVersion += "/";
            } else {
                reportVersion += "-";
            }
            reportVersion += client.getFromIp();
        }
        
        // 向认证服务器转发请求
        LegacyQuickRegMessage msg = new LegacyQuickRegMessage(cmccUserId, reportVersion, model,
                configuration.getString("gamecode"), realPhone);
        QuickRegRequest request = new QuickRegRequest(data.getSerial(), data.getSessionId(), this, cmccUserId, reportVersion,
                model, configuration.getString("gamecode"));
        requestService.add(msg.getSerial(),request);
        accountSkeleton.send(msg);
        if (!model.startsWith("NK-NGage") && !model.startsWith("MotoV300")) {
            syncChannels(data.getSessionId(), new String[] {NORMAL90CHANNEL}, new String[] {FAST90CHANNEL});
        }
    }

    public void accountReg(UWAPData data) throws Exception {
        String name = data.readString();
        String phone = data.readString();
        String recommend = data.readString();
        String model = data.readString();
        String versionString = data.readString();
        boolean needReturn = true;
        try {
            needReturn = data.readBoolean();
        } catch (IllegalAccessException ex) {
        }
        
        // PiP版本现在也可能传卓望平台用户信息上来了
        // 新版本再多一个手机号参数
        String cmccUserId = "";
        String cmccKey = "";
        String realPhone = "";
        try {
            cmccUserId = data.readString().trim();
            cmccKey = data.readString();
            realPhone = cutPhone(data.readString());
        } catch (Exception e) {
        }
        log.info("AccountName[" + name + "]CmccUserId[" + cmccUserId + "]Phone[" + realPhone + "]Version[" + versionString + "]Model[" + model + "]Try Register");
        
        // 检查版本是否支持
        String v = cutVersion(versionString);
        Version version = versionService.getVersion(v);
        if (version == null) {
            throw new ITimesException("不支持的版本", data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }
        if (version.getStatus() == Version.STATUS_CANCELED) {
            throw new ITimesException(version.getMessage(), data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }
        if (!version.isCanReg()) {
            throw new ITimesException("此版本不支持注册功能", data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }
        String password = "";
    	// 取自定义密码
    	try {
    		password = data.readString();
        } catch (Exception ex) {
        }
        
        // 把推荐人信息由角色名转换为帐号ID
        int recommendAccountId = -1;
        if (recommend.length() > 0) {
            recommendAccountId = playerService.getAccountIdByPlayerName(recommend);
            if (recommendAccountId == -1) {
            	throw new ITimesException("角色不存在，请核实后再填写，如无推荐人此栏可不填写。", data.getSerial(),
                        data.getSessionId(),
                        data.getAppType());
            }
        }
        
        // 检查是否明珠幻想的内测服务器
        if (Server.demoForVersion2) {
        	throw new ITimesException("此版本不支持注册功能", data.getSerial(),
                    data.getSessionId(),
                    data.getAppType());
        }

        // 保存客户端信息
        Client client = getAndCreateClient(data.getSessionId());
        client.cmccUserId = cmccUserId.trim();
        client.cmccKey = cmccKey;
        client.realPhone = realPhone;
        client.version = version;
        client.model = model;
        client.rawVersion = versionString;
        client.channel = cutChannel(versionString);
        
        // 把客户端IP地址放到发给认证服务器的版本号字符串里，格式为：
        // 3.1-CCCCCPiP-113123121/202.38.64.1
        String reportVersion = versionString;
        if (client.getFromIp().trim().length() > 0) {
            String[] tmp = versionString.split("-");
            if (tmp.length > 2) {
                reportVersion += "/";
            } else {
                reportVersion += "-";
            }
            reportVersion += client.getFromIp();
        }
        
        // 向认证服务器转发请求
//        AccountRegMessage msg = new AccountRegMessage(name,phone,recommend,recommendAccountId,model,configuration.getString("gamecode"),reportVersion,realPhone,password);
        AccountRegMessage msg = new AccountRegMessage(name,phone,recommend,-1,model,configuration.getString("gamecode"),reportVersion,realPhone,password);
        AccountRegRequest request = new AccountRegRequest(data.getSerial(),data.getSessionId(),this);
        requestService.add(msg.getSerial(),request);
        accountSkeleton.send(msg);
        if (!model.startsWith("NK-NGage") && !model.startsWith("MotoV300")) {
            syncChannels(data.getSessionId(), new String[] {NORMAL90CHANNEL}, new String[] {FAST90CHANNEL});
        }

    }

    public void login(UWAPData data) throws Exception {
        String accountName = data.readString().trim();
        String password = data.readString();
        String model = data.readString();
        String versionString = data.readString();
        
        // PiP版本现在也可能传卓望平台用户信息上来了
        // 新版本再多一个手机号参数
        String cmccUserId = "";
        String cmccKey = "";
        String realPhone = "";
        try {
            cmccUserId = data.readString().trim();
            cmccKey = data.readString();
            realPhone = cutPhone(data.readString());
           
        } catch (Exception e) {
        	
        }
        byte fastRegFlag = 0;
        String roleName = "";
        try{
        	
        	fastRegFlag = data.readByte();
	        if(fastRegFlag == 1){//采用了快速标志，此时上传了，上传了角色名，还有
	        	roleName = data.readString();
	        	
	        }
        } catch (Exception e) {
        	fastRegFlag = 0;
        }
        log.info("AccountName[" + accountName + "]CmccUserId[" + cmccUserId + "]Phone[" + realPhone + "]Version[" + versionString + "]Model[" + model + "]Try Login");
        
        // 检查版本是否支持
        String v = cutVersion(versionString);
        Version version = versionService.getVersion(v);
       
        if (version == null)
            throw new ITimesException("版本号错误", data.getSerial(), data.getSessionId(),
                                      data.getAppType());
        if (version.getStatus() == Version.STATUS_CANCELED) {
            throw new ITimesException(version.getMessage(), data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }

        // 检查是否明珠幻想的内测服务器
        if (Server.demoForVersion2) {
        	String name = accountName.toLowerCase();
        	boolean pass = false;
        	if (name.startsWith("mzhx")) {
        		try {
        			int nid = Integer.parseInt(name.substring(4));
        			if (nid >= 1000 && nid < 12000) {
        				pass = true;
        			}
        		} catch (Exception e) {
        		}
        	}
        	if (!pass) {
	        	throw new ITimesException("请使用内测帐号登录", data.getSerial(),
	                    data.getSessionId(),
	                    data.getAppType());
        	}
        }
        
        // 保存客户端信息
        Client client = getAndCreateClient(data.getSessionId());
        client.cmccUserId = cmccUserId.trim();
        client.cmccKey = cmccKey;
        client.realPhone = realPhone;
        client.version = version;
        client.model = model;
        client.rawVersion = versionString;
        client.channel = cutChannel(versionString);
        client.roleName = roleName;
        client.fastRegFlag = fastRegFlag;

        if (client.status == Client.STATUS.INIT) {
            client.password = password;
            
            // 向认证服务器转发请求
            LegacyLoginMessage lm = new LegacyLoginMessage(accountName,password,realPhone);
            LoginRequest lr = new LoginRequest(data.getSerial(),data.getSessionId(),this,name,password,model,versionString,false,null);
            requestService.add(lm.getSerial(),lr);
            accountSkeleton.send(lm);
            if (!model.startsWith("NK-NGage") && !model.startsWith("MotoV300")) {
                syncChannels(data.getSessionId(), new String[] {NORMAL90CHANNEL}, new String[] {FAST90CHANNEL});
            }
        }
    }


    public void relogin(UWAPData data) throws Exception {
        String accountName = data.readString();
        String password = data.readString();
        String playerName = data.readString();
        String model = data.readString();
        String versionString = data.readString();
        byte type = data.readByte();

        // PiP版本现在也可能传卓望平台用户信息上来了
        // 新版本再多一个手机号参数
        String cmccUserId = "";
        String cmccKey = "";
        String realPhone = "";
        try {
            cmccUserId = data.readString().trim();
            cmccKey = data.readString();
            realPhone = cutPhone(data.readString());
        } catch (Exception e) {
        }
        log.info("AccountName[" + accountName + "]CmccUserId[" + cmccUserId + "]Phone[" + realPhone + "]Version[" + versionString + "]Model[" + model + "]Try Relogin");

        // 检查版本是否支持
        String v = cutVersion(versionString);
        Version version = versionService.getVersion(v);
        if (version == null) {
            throw new ITimesException("不支持的版本", data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }
        if (version.getStatus() == Version.STATUS_CANCELED) {
            throw new ITimesException(version.getMessage(), data.getSerial(),
                                      data.getSessionId(),
                                      data.getAppType());
        }
        
        // 保存客户端信息
        Client client = getAndCreateClient(data.getSessionId());
        client.cmccUserId = cmccUserId.trim();
        client.cmccKey = cmccKey;
        client.realPhone = realPhone;
        client.version = version;
        client.model = model;
        client.rawVersion = versionString;
        client.channel = cutChannel(versionString);
        client.roleName = playerName;

        if (client.status == Client.STATUS.INIT || client.status == Client.STATUS.LOGIN) {
            // 向认证服务器转发请求
            LegacyLoginMessage lm = new LegacyLoginMessage(accountName,password,realPhone);
            LoginRequest lr = new LoginRequest(data.getSerial(),data.getSessionId(),this,name,password,model,versionString,true,playerName);
            requestService.add(lm.getSerial(),lr);
            accountSkeleton.send(lm);
            if (!model.startsWith("NK-NGage") && !model.startsWith("MotoV300")) {
                syncChannels(data.getSessionId(), new String[] {NORMAL90CHANNEL}, new String[] {FAST90CHANNEL});
            }
        }
    }


    void removeClient(Client client) {
        if (client.sessionId != -1) {
            sessionId2Clients.remove(client.sessionId);
        }
        if (client.playerId != -1) {
            playerId2Clients.remove(client.playerId);
        }
        if (client.accountId != -1) {
            accountId2Clients.remove(client.accountId);
            if (client.isLogin()) {
                Logout1Message message = new Logout1Message(client.accountId, client.key);
                accountSkeleton.send(message);
            }
        }
        if (client.status == Client.STATUS.PLAYERLOGIN) {
            WorldPlayer player = playerService.getWorldPlayer(client.playerId);
            if (player != null)
                logout(player);
        }
    }



    protected void sendRequestToAuth(Request request, int accountId, String key,int serial, int sessionId,  boolean useBalance) throws
            ITimesException {
        LegacyBuy1Message message = new LegacyBuy1Message(accountId,key, request.price, useBalance);
        StoreRequest rq = new StoreRequest(serial, sessionId, this, request);
        requestService.add(message.getSerial(), rq);
        accountSkeleton.send(message);
    }
    
    /*
     * PiP版本新客户端可以通过卓望认证服务器用话费购买道具。
     */
    protected void sendRequestToCmccAuth(Request request, int accountId, String key,int serial, int sessionId) throws
            ITimesException {
        if (request.type == StoreService.Request.ITEM && request.count > 1) {
            // 卓望认证一次只能购买一件
            throw new ITimesException("本类商品一次只能购买一个。", serial, sessionId, ClientConstants.ISHOP_TRADE);
        }
        Client client = sessionId2Clients.get(sessionId);
        UWAPSegment seg = new UWAPSegment(ServerConstants.BUY);
        seg.writeInt(-1);
        seg.writeString(request.consumeCode);
        seg.writeInt(request.id);
        seg.writeString(client.rawVersion);
        seg.writeString(client.cmccUserId);
        Server.instance.authSession.write(seg);
        log.info("[CMCC_BUY]ID[" + request.playerId + "]ACCOUNTID[" + accountId + "]USERID[" + 
                client.cmccUserId + "]CONSUMECODE[" + request.consumeCode + "]BUYID[" + request.id + "]TRY");
    }

    void addRecommendBalance(WorldPlayer player){
    	LevelUpNotifyMessage msg = new LevelUpNotifyMessage(player.getAccountId(), player.getId(), player.getLevel(), Server.getGameCode());
	    accountSkeleton.send(msg);
    }

    void sendLogoutToAuth(WorldPlayer player){
        //老版本需要，新版本不需要
    }

    void modifyPassword(WorldPlayer player, int sessionId, int serial, String oldPassword, String newPassword) {
        ModifyPasswordMessage message = new ModifyPasswordMessage(player.getAccountName(), player.getkey(), oldPassword,
                newPassword);
        ModifyPasswordRequest request = new ModifyPasswordRequest(serial, sessionId,
                this, player.getAccountName(), player.getkey(), newPassword);
        requestService.add(message.getSerial(), request);
        accountSkeleton.send(message);
    }

    void modifyPhone(WorldPlayer player, int sessionId, int serial, String phone) {
        ModifyPhoneMessage message = new ModifyPhoneMessage(player.getAccountName(), player.getkey(), phone);
        ModifyPhoneRequest rq = new ModifyPhoneRequest(serial, sessionId, this,
                phone);
        requestService.add(message.getSerial(), rq);
        accountSkeleton.send(message);
    }

    void cmccCharge(WorldPlayer player,int sessionId,int serial,int value){

    }

    void getAccountName(int sessionId,int serial,int accountId,String playerName){
        GetAccountNameRequest rq = new GetAccountNameRequest(serial, sessionId, this,
                accountId, playerName);
        GetAccountNameMessage message = new GetAccountNameMessage(accountId);
        requestService.add(message.getSerial(), rq);
        accountSkeleton.send(message);
    }

//    void buyResult(BuyResult result,StoreService.Request request) throws Exception{
//        boolean success = result.success;
//        int iMoney = result.iMoney;
//        int cost = result.cost;
//        String cause = result.cause;
////        StoreService.Request request = rq.getRequest();
//        if (request != null) {
//            if (success) {
//                WorldPlayer player = playerService.loadWorldPlayer(request.
//                        playerId);
//                if (player != null) {
//                    playerService.acquire(player);
//                    if (request.type == StoreService.Request.ITEM) {
//                        synchronized (request.item) {
//                            if (request.item.count != -1 && (request.item.count - request.count) < 0) {
//                                log.info("ID[" + player.getId() +
//                                         "] iShop Buy Fail Item[" +
//                                         request.item.item.getItemId() + "] Count[" +
//                                         request.count + "]");
//                                connectService.sendError(player.getId(),
//                                        "此商品已经售完，或者剩余的数量不够", request.serial, (byte) 86);
////                                UWAPSegment seg = new UWAPSegment(
////                                        ServerConstants.ADD_IMONEY);
////                                seg.writeInt(player.getAccountId());
////                                seg.writeInt(request.price);
////                                seg.writeInt(request.id);
////                                write(seg);
//                            } else {
//                                synchronized (player) {
//                                    Changed changed = new Changed();
//                                    IItem item = player.completeAddItem(request.
//                                            item.item.
//                                            newInstance(), request.count, null);
//
//                                    if (item == null) {
//
//                                        if (cost == -1) { //cmcc版本进行特殊处理，邮寄到用户邮箱
//                                            log.info("ID[" + player.getId() +
//                                                    "] iShop Buy Item[" +
//                                                    request.item.item.getItemId() +
//                                                    "] Count[" +
//                                                    request.count + "] UsediMoney[" +
//                                                    cost + "] CurrentiMoney[" +
//                                                    iMoney + "] To MailBox");
//                                            byte[] att = ItemUtils.item2dbAttachment(request.item.item.newInstance(),
//                                                    request.count);
//                                            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
//                                                    request.item.item.getName() + "*" + request.item.count, "", att, 0, true);
////                                            chatService.sendPrivateMessage( -1, "系统", player.getId(),
////                                                    "你购买的" + request.item.item.getName() + "由于背包满，已经邮寄到邮箱中，请注意查收。");
//                                            connectService.sendError(player.getId(),
//                                                    "你购买的" + request.item.item.getName() + "由于背包满，已经邮寄到邮箱中，请注意查收。",
//                                                    request.serial, (byte) 86);
//
//                                        } else {
//                                            log.info("ID[" + player.getId() +
//                                                    "] iShop Buy Fail Item[" +
//                                                    request.item.item.getItemId() +
//                                                    "] Count[" +
//                                                    request.count + "] To MailBox");
//                                            byte[] att = ItemUtils.item2dbAttachment(request.item.item.newInstance(),
//                                                    request.count);
//                                            player.setiMoney(iMoney);
//                                            player.addCredit(cost / 1000, changed);
//                                            player.setConsumePoint(player.getConsumePoint()+cost/100);
////                                            if(player.getTongId()!=-1){ 目前不上线
////                                                TongData td = tongService.getTongData(player.getTongId());
////                                                td.addCredit(cost/1000);
////                                                tongService.saveTongData(td);
////                                                player.setContribution(player.getContribution()+cost/1000);
////                                                tongService.modifyPlayer(player);
////                                            }
//
//                                            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
//                                                    request.item.item.getName() + "*" + request.item.count, "", att, 0, true);
//                                            connectService.sendError(player.getId(),
//                                                    "你购买的" + request.item.item.getName() + "由于背包满，已经邮寄到邮箱中，请注意查收。",
//                                                    request.serial, (byte) 86);
//                                            connectService.sendGetItem(changed, player.getId(), (byte) 33);
////                                            connectService.sendError(player.getId(),
////                                                    "包格满，购买失败", request.serial,
////                                                    (byte) 86);
////                                            UWAPSegment seg = new UWAPSegment(
////                                                    ServerConstants.ADD_IMONEY);
////                                            seg.writeInt(player.getAccountId());
////                                            seg.writeInt(request.price);
////                                            seg.writeInt(request.id);
////                                            write(seg);
//                                        }
//                                    } else {
//                                        if (request.item.count != -1) {
//                                            request.item.count -= request.count;
//                                            if (request.item.count < 0)
//                                                request.item.count = 0;
//                                        }
//                                        player.setiMoney(iMoney);
//                                        player.addCredit(cost / 1000, changed);
//                                        player.setConsumePoint(player.getConsumePoint()+cost/100);
////                                        if (player.getTongId() != -1) { 目前不上线
////                                            TongData td = tongService.getTongData(player.getTongId());
////                                            td.addCredit(cost / 1000);
////                                            tongService.saveTongData(td);
////                                            player.setContribution(player.getContribution()+cost/1000);
////                                            tongService.modifyPlayer(player);
////                                        }
//
//                                        log.info("ID[" + player.getId() +
//                                                 "] iShop Buy Item[" +
//                                                 request.item.item.getItemId() +
//                                                 "] Count[" +
//                                                 request.count + "] UsediMoney[" +
//                                                 cost + "] CurrentiMoney[" +
//                                                 iMoney + "]");
////                                    connectService.sendGetItem(changed,
////                                            player.getId(), (byte) 22);
//                                        UWAPSegment seg = new UWAPSegment(
//                                                ClientConstants.ISHOP_TRADE_OK);
//                                        if (cost == -1) { //cmcc版本返回-1，用item本身的price
//                                            seg.writeInt(request.item.price);
//                                        } else {
//                                            seg.writeInt(cost / 100);
//                                        }
//                                        seg.writeInt(request.item.count);
//                                        seg.write(ItemUtils.getAuctionBytes(item,
//                                                request.count));
//                                        connectService.writeTo(seg, player.getId());
//                                        connectService.sendGetItem(changed, player.getId(), (byte) 33);
//                                    }
//                                    //mengjie add
//                                    int item_id = 0;
//                                    item_id = request.item.item.getItemId();
//                                    String item_msg = Items.getMessage(item_id,4,player.getPlayerName(),request.item.item.getName());
//                                    if (item_msg != null){
//                                        chatService.sendWorldMessage(-1, "系统", item_msg);
//                                    }
//
//                                    //mengjie add end
//                                }
//                            }
//
//                        }
//                    } else if (request.type == StoreService.Request.FACE) {
//                        log.info("ID[" + player.getId() +
//                                 "] iShop Buy Face[" +
//                                 request.face.getFace() +
//                                 "] UsediMoney[" +
//                                 request.price + "] CurrentiMoney[" +
//                                 iMoney + "]");
//                        player.setiMoney(iMoney);
//                        player.setFace((short) request.face.getFace());
//                        Changed changed = new Changed();
//                        changed.setProperty(Changed.FACE, player.getFace());
//                        connectService.sendGetItem(changed, player.getId(), (byte) 20);
//                        UWAPSegment seg = new UWAPSegment(ClientConstants.FACE_LIST);
//                        seg.write((byte) 1);
//                        seg.writeShort(player.getFace());
//                        seg.write((byte) 0);
//                        seg.write(request.face.getWalk().getPfile());
//                        seg.write(request.face.getWalk().getSfile());
//                        seg.write((byte) 1);
//                        seg.write(request.face.getBattle().getPfile());
//                        seg.write(request.face.getBattle().getSfile());
//                        seg.write((byte) 2);
//                        seg.write(request.face.getPortrait().getPfile());
//                        seg.write(request.face.getPortrait().getSfile());
//                        seg.write((byte) 3);
//                        seg.write(request.face.getEffect().getPfile());
//                        seg.write(request.face.getEffect().getSfile());
//                        connectService.writeTo(seg, player.getId());
//                        if (cost == -1) {
//                            connectService.sendMessage(player.getId(),
//                                    "你已经成功购买了形象" + request.face.getName() + "，价格为" + request.face.getPrice() + "点");
//                        } else {
//                            connectService.sendMessage(player.getId(),
//                                    "你已经成功购买了形象" + request.face.getName() + "，价格为" + request.price / 100 + "i。");
//                        }
//                    } else if (request.type == StoreService.Request.HOUSE) {
//                        log.info("ID[" + player.getId() +
//                                 "] Buy House[" +
//                                 request.ht.getLevel() +
//                                 "] UsediMoney[" +
//                                 cost + "] CurrentiMoney[" +
//                                 iMoney + "]");
//                        player.setiMoney(iMoney);
//                        try {
//                            houseModel.createHouse(player, request.ht.getLevel(), request.ht.getStyle(),
//                                    (short) request.count, cost);
//                            connectService.sendMessage(player.getId(), "你已经成功购买了房产，等级为" + request.ht.getLevel() + "级。");
//                        } catch (HouseException ex) {
//                            log.error(ex, ex);
//                            log.info("ID[" + player.getId() +
//                                     "] Buy House Fail[" +
//                                     request.ht.getLevel() +
//                                     "] UsediMoney[" +
//                                     cost + "] CurrentiMoney[" +
//                                     iMoney + "]");
//                        }
//                    } else if (request.type == StoreService.Request.STYLE) {
//                        log.info("ID[" + player.getId() +
//                                 "] Buy House Style[" +
//                                 request.ht.getStyle() +
//                                 "] UsediMoney[" +
//                                 cost + "] CurrentiMoney[" +
//                                 iMoney + "]");
//                        player.setiMoney(iMoney);
//                        try {
//                            houseModel.changeStyle(player, request.ht.getStyle(), cost);
//                            connectService.sendMessage(player.getId(), "你已经修改房屋样式成功。");
//                        } catch (HouseException ex1) {
//                            log.error(ex1, ex1);
//                            log.info("ID[" + player.getId() +
//                                     "] Buy House Style Fail[" +
//                                     request.ht.getStyle() +
//                                     "] UsediMoney[" +
//                                     cost + "] CurrentiMoney[" +
//                                     iMoney + "]");
//                        }
//                    } else if (request.type == StoreService.Request.PART) {
//                        log.info("ID[" + player.getId() +
//                                 "] Buy House Part[" +
//                                 request.hp.getId() +
//                                 "] UsediMoney[" +
//                                 cost + "] CurrentiMoney[" +
//                                 iMoney + "]");
//                        player.setiMoney(iMoney);
//                        try {
//                            houseModel.addPart(player, request.hp, cost);
//                            connectService.sendMessage(player.getId(), "你已经购买家具成功。");
//                        } catch (HouseException ex1) {
//                            log.error(ex1, ex1);
//                            log.info("ID[" + player.getId() +
//                                     "] Buy House Style Fail[" +
//                                     request.ht.getStyle() +
//                                     "] UsediMoney[" +
//                                     cost + "] CurrentiMoney[" +
//                                     iMoney + "]");
//                        }
//                    } else if (request.type == StoreService.Request.WAITER) {
//                        log.info("ID[" + player.getId() +
//                                 "] Buy Waiter UsediMoney[" +
//                                 cost + "] CurrentiMoney[" +
//                                 iMoney + "]");
//                        player.setiMoney(iMoney);
//                        HouseData hd = houseModel.getHouseByPlayerId(player.getId());
//                        hd.setCanUseWaiterTime(new Date(System.currentTimeMillis() + 30 * 24 * 3600 * 1000L));
//                        houseModel.saveHouse(hd);
//                        connectService.sendMessage(player.getId(), "你已经成功雇佣了管家.");
//                    }
//                    playerService.release(player);
//                }
//            } else {
//                if (request.type == StoreService.Request.ITEM) {
//                    connectService.sendError(request.playerId, cause,
//                                             request.serial, (byte) 86);
//                } else {
//                    connectService.sendMessage(request.playerId, cause);
//                }
//            }
//        }
//    }

    public void loginOk(LoginResult lr){
        int accountId = lr.accountId;
        String name = lr.name;
        String key = lr.key;
        String phone = lr.phone;
        String password = lr.password;
        int[] purchased = lr.purchased;
        int modifyPasswordTimes = lr.modifyPasswordTimes;
        long iMoney = lr.iMoney;
        long bBalance = lr.bBalance;
        boolean isMonth = lr.isMonth;
        boolean isSubscribe = false;
        int loginErrorTime = lr.loginErrorTime;
        for(int i=0;i<purchased.length;i++){
            if(purchased[i]==1){
                isSubscribe = true;
                break;
            }
        }
        if(iMoney<-300000){
            UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR, lr.bRequest.getId(),
                                              lr.bRequest.getSessionId());
            seg.write(ClientConstants.LOGIN);
            seg.writeString("您的余额不足（"+iMoney/100+"i），请先到官方网站wap.pipfit.com充值后再登陆。");
            write(seg);
            return;
        }
//        boolean isSubscribe = message.isSubscribe();
        Client client = getClient(lr.bRequest.getSessionId());
        if (client != null && client.status == Client.STATUS.INIT) {
//            removeClient(client);
            Client client1 = accountId2Clients.get(accountId);
            if (client1 != null) {
                connectService.forceLogout(accountId, client1.key);
            }

            client.status = Client.STATUS.LOGIN;
            client.accountId = accountId;
            client.name = name;
            client.key = key;
//            client.password = password;
            client.phone = phone;
            client.modifyPasswordTimes = modifyPasswordTimes;
            client.iMoney = iMoney;
            client.bBalance = bBalance;
            client.isMonth = isMonth;
            client.isSubscribe = isSubscribe;
            client.loginErrorTime = loginErrorTime;
            accountId2Clients.put(client.accountId, client);
            UWAPSegment seg = new UWAPSegment(ClientConstants.LOGIN_OK, lr.bRequest.getId(), lr.bRequest.getSessionId());
            seg.writeInt(accountId);
            seg.writeString(name);
            seg.writeString(phone);
            seg.writeInt(modifyPasswordTimes);
            seg.writeInt((int)iMoney);
            seg.writeBoolean(isMonth);
            seg.writeBoolean(isSubscribe);
            write(seg);
            //模拟登陆角色
            if(client.fastRegFlag == 1){
            /*	LoginRequest bRequest  = lr.bRequest;
				//从登陆请求中获取serial就是bRequest.getId()和sessinID
				try {
					throw new ITimesException("已达到最大登陆数量", bRequest.getId(),
	            			 bRequest.getSessionId(), ClientConstants.PLAYER_LOGIN);
				} catch (ITimesException ex) {
					fastRegFlag = 0;
		            sendError(ex);
		        } catch (Exception ex) {
		            log.error(ex, ex);
		        }*/
            	if(client.getDataVersion() >= 4 ){			//快速注册的时候
            		WorldPlayer  player = null;
		            try {
		            	player = playerService.loadWorldPlayer(client.roleName, client.accountId);
		            } catch (ITimesException ex) {
			            client.fastRegFlag = 0;
			            sendError(ex);
			        }catch (Exception ex) {
		                log.error(ex, ex);
		                removeClient(client);
		                client.fastRegFlag = 0;
		            }
		            client.status = Client.STATUS.PLAYERLOGIN;
		            if (player != null){
		            	client.playerId = player.getId();
		            }
		            
				}else{		// 快速进入和老版本的快速注册
					UWAPDataReadAndWrite.makeUWApDataReadAndWrite(ClientConstants.PLAYER_LOGIN);
					UWAPDataReadAndWrite.writeString(client.roleName);	
					byte[] data = UWAPDataReadAndWrite.getByteArray();
					LoginRequest bRequest  = lr.bRequest;
					//从登陆请求中获取serial就是bRequest.getId()和sessinID
					UWAPDataReadAndWrite uWapdata = new UWAPDataReadAndWrite(data, bRequest.getId(), bRequest.getSessionId(), false, 1);
					try {
						super.playerLogin(uWapdata);
						
					} catch (ITimesException ex) {
						client.fastRegFlag = 0;
			            sendError(ex);
			        } catch (Exception ex) {
			            log.error(ex, ex);
			        }  
				} 
	        }
            log.info("AccountID[" + accountId + "]Logined");
        }
    }

    public void reloginResult(ReloginResult result) throws Exception{
        try {
            LoginRequest r = result.bRequest;

            if (getPlayerCount() >= maxPlayer) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT, r.getId(), r.getSessionId());
                seg.write((byte) 2);
                write(seg);
                Client client = getClient(r.getSessionId());
                if (client != null) {
                    removeClient(client);
                }
                return;
            }
            int accountId = result.accountId;
            String name = result.name;
            String key = result.key;
            String password = result.password;
            String phone = result.phone;
            int modifyPasswordTimes = result.modifyPasswordTimes;
            int[] purchased = result.purchased;
            long iMoney = result.iMoney;
            long bBalance = result.bBalance;
            boolean isMonth = result.isMonth;
            boolean isSubscribe = false;
            for(int i=0;i<purchased.length;i++){
                if(purchased[i]==1){
                    isSubscribe = true;
                    break;
                }
            }
            Client client = getClient(r.getSessionId());
            if (client != null) {
                if (client.status == Client.STATUS.INIT || client.status == Client.STATUS.LOGIN) {
                    Client client1 = accountId2Clients.get(accountId);
                    if (client1 != null) {
                        connectService.forceLogout(accountId, client1.key);
                    }

                    client.status = Client.STATUS.LOGIN;
                    client.accountId = accountId;
                    client.name = name;
                    client.key = key;
                    client.password = password;
                    client.phone = phone;
                    client.modifyPasswordTimes = modifyPasswordTimes;
                    client.iMoney = iMoney;
                    client.bBalance = bBalance;
                    client.isMonth = isMonth;
                    client.isSubscribe = isSubscribe;
                    accountId2Clients.put(client.accountId, client);
                } else if (client.status == Client.STATUS.PLAYERLOGIN) {
                    log.info("AccountID[" + accountId + "]Relogin State Error playerlogin");
                    return;
                } else {
                    log.info("AccountID[" + accountId + "]Relogin State Error");
                    return;
                }
                ChargePlan[] chargePlan = ChargePlan.getChargePlans(client.version.getCharge());
                FeePlan feePlan = FeePlan.getFeePlan(client.version.getFeeplan());
                if (chargePlan == null || feePlan == null) {
                    UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT, r.getId(), r.getSessionId());
                    seg.write((byte) 2);
                    write(seg);
                    return;
                }

                WorldPlayer player = playerService.loadWorldPlayer(r.getPlayerName(), accountId);

                log.info("AccountId[" + accountId + "]Name[" + name + "]TRY RELOGIN");
                if (player != null) {

                    if (player.getLevel() >= feePlan.getBeginLevel() && iMoney <= 0 &&
                        !isMonth && !isSubscribe) {
                    	playerService.unRegistry(player);
                        UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT, r.getId(), r.getSessionId());
                        seg.write((byte) 2);
                        write(seg);
                        return;
                    }

                    if (playerService.isFrobiden(player.getId())) {
                    	playerService.unRegistry(player);
                        UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT, r.getId(), r.getSessionId());
                        seg.write((byte) 2);
                        write(seg);
                        return;
                    }
                    player.setState(WorldPlayer.ONLINE);
                    player.setMaxLevel(client.version.getMaxLevel());
                    player.setLastLoginTime(new Date());
                    player.setLongIMoney(iMoney);
                    player.setBBalance(bBalance);
                    player.setMonth(isMonth);
                    player.setSubscribe(isSubscribe);
                    player.setFeePlan(feePlan);
                    player.setChargePlan(chargePlan);
                    player.setModel(client.model);
                    player.setAccountName(client.name);
                    player.setKey(client.key);
                    player.setPhone(phone);
                    player.setModifyPasswordTimes(modifyPasswordTimes);
                    player.setLastLifeTime(System.currentTimeMillis());
                    player.clearPosition();
                    player.setIsFirstEnter(true);
                    player.setIsOnce(true);
                    if (!contains(player.getId())) {
                        registry(player, r.getSessionId());
                        playerService.acquire(player);
                    } else {
                        sessionIds.put(new Integer(player.getId()), new Integer(r.getSessionId()));
                    }
                    client.status = Client.STATUS.PLAYERLOGIN;
                    client.playerId = player.getId();
                    playerId2Clients.put(client.playerId, client);
                    GameMap map = worldService.getMap(player, player.getMapId(), true);
                    //登陆角色时检查角色是否在副本中，如果角色不在副本中，那么直接载入
                    //如果角色在副本中，分几种情况：1，角色副本存在且可以进入，直接进入 2，角色副本
                    //存在，不可进入，那么进入入口缺省位置 3，如果角色副本不存在了 那么也进入入口缺省位置
                    if (map != null) {
                        if (map.getMapId() != player.getMapId()) {
                            InstanceDefinition instance = worldService.
                                    getInstanceDefinition(player.
                                    getMapId());
                            if(instance!=null){
                                if("house".equals(instance.getType())){
                                    player.setMapId(map.getMapId());
                                    player.setX(player.getJumpX());
                                    player.setY(player.getJumpY());
                                }else{
                                    player.setMapId(instance.getEntrance());
                                    player.setX(instance.getEntrancePixelX());
                                    player.setY(instance.getEntrancePixelY());
                                }
                            }else{

                                UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT, r.getId(), r.getSessionId());
                                seg.write((byte) 2);
                                write(seg);
                                removeClient(client);
                                return;
                            }
//                        if (player.getJumpMapId() != 0) {
//                            player.setMapId(map.getMapId());
//                            player.setX(player.getJumpX());
//                            player.setY(player.getJumpY());
//                        } else {
//                            player.setMapId(map.getMapId());
//                            player.setX(map.getDefaultX());
//                            player.setY(map.getDefaultY());
//                        }
                        }
                    } else {
                        InstanceDefinition instance = worldService.
                                getInstanceDefinition(player.
                                getMapId());
                        if (instance == null) {
                            UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT, r.getId(), r.getSessionId());
                            seg.write((byte) 2);
                            write(seg);
                            removeClient(client);
                            return;
                        }
                        player.setMapId(instance.getEntrance());
                        player.setX(instance.getEntrancePixelX());
                        player.setY(instance.getEntrancePixelY());
                    }
//            BathHouse bathHouse = BathHouse.getBathHouseByMapId(player.getMapId());
//            if (bathHouse != null) {
//                synchronized (player) {
//                    tryAddBathHouseExp(player, System.currentTimeMillis(), bathHouse);
//                }
//            }
                    addToChannels(r.getSessionId(), getPlayerChannels(player));
                    addPlayerDispatchChat(r.getSessionId(), player.getClientDataVersion(), player.getCamp());
                    if(Server.player_Delay.containsKey(player.getId())){
                    	WorldBossEquipInfo bossInfo= Server.player_Delay.get(player.getId());
                    	Map<IItem, Integer> equMap = bossInfo.getEquDiamondTimeMap();
                    	for(Map.Entry<IItem, Integer> equ: equMap.entrySet()){
                    		//如果背包内不能加入物品的话，则扔掉该物品
                    		IItem item = equ.getKey();
                    		if(player.completeAddItem(item, 1, null, player.getClientDataVersion()) != null){
                    			WorldBossEquipInfo info = Server.player_Delay.get(player.getId());
                    			info.setOnline(true);
                    			chatService.sendWorldMessage(-1, "系统", "玩家"+ player.getPlayerName()+ "抢夺到了"+ item.getName());
                    			log.info("playerID["+ player.getId() + "] relogin get item [" + Utils.getHexdump(item.toDbBytes()) + "]");
                    		}else{//背包满无法加满
                    			log.info("playerID["+ player.getId() + "] relogin drop item [" + Utils.getHexdump(item.toDbBytes()) + "]");
                    		}
                    	}
                    }
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            RELOGIN_RESULT,
                            r.getId(),
                            r.getSessionId());
                    seg.write((byte) 0);
                    seg.writeInt(player.getId());
                    seg.writeString(player.getPlayerName());
                    seg.writeInt(player.getModifyNameTimes());
                    seg.writeShort(player.getMapId());
                    seg.writeShort(player.getX());
                    seg.writeShort(player.getY());
                    seg.write(player.toClientBytes(client.getDataVersion()));
                    seg.writeInt(r.getSessionId());
                    seg.writeInt(100);
                    seg.write(Ability.getAllAbilitiesBytes());
                    write(seg);

                    if (player.getTeam() != null) {
                        leaveTeam(player);
                    }
                    friendService.sendOnlineFriends(player);
                    
                    player.setClient(client);
                    Utils.log(log, player.getId(), ClientConstants.RELOGIN,
                              "RELOGIN OK ");
                }
            }

        } catch (ITimesException ex) {
            sendError(ex);
        }
    }
    
    protected boolean checkSmsBuy(StoreService.Request request, WorldPlayer player, Client client) {
        return false;
    }
    
    protected void smsBuyReqOk(StoreService.Request request, String token) {}

    /*
     * 过滤i币商店列表。只有支持话费购买的客户端才能看见话费购买分类。只有卓望短信购买版本才能看见
     * 短信购买分类。
     */
    protected Collection<IStoreGroup> filterIshopGroups(Client client, Collection<IStoreGroup> allGroups) {
        // 新版客户端，登录时传入了卓望平台用户ID的，可以看到所有分类
//        if (client.cmccUserId != null && client.cmccUserId.length() > 0 
//                && !"CCCCYYXW".equals(client.channel)) {
//            return allGroups;
//        }
    	// 4.0版本以后的特殊处理，可以看到话费购买专区
        /*if (client != null && client.version != null && 
		(client.getDataVersion() > 0) && client.cmccUserId != null && client.cmccUserId.length() > 0 ){
        	return allGroups;
        }
        // 其他用户看不到话费购买分类
        List<IStoreGroup> ret = new ArrayList<IStoreGroup>();
        for (IStoreGroup g : allGroups) {
            if (g.getName().contains("话费")) {
                continue;
            }
            ret.add(g);
        }*/
        return allGroups;
    }
    
    public void quickRegResult(int accountId, String accountName, String password, byte type, String model,
            int sessionId, int serial) throws Exception {
		Client client = sessionId2Clients.get(sessionId); 
		client.accountId = accountId;
		if (type == 0) { //新建了帐号，需要新建角色
			//Player player = playerService.quickCreatePlayer(accountId,
			//       accountName, (byte) 0, serial, model);
			//快速注册的玩家性别默认为女
			Player player = playerService.quickCreatePlayer(client, accountName, (byte) 1, serial);
			UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
			                           serial, sessionId);
			seg.writeString(accountName);
			seg.writeString(password);
			seg.writeString(player.getPlayerName());
			seg.write((byte) 0);
			if(client.fastRegFlag == 0){
				write(seg);
			}	
			if(client.fastRegFlag == 1){
				client.roleName = player.getPlayerName();
				seg.write((byte) 1);            //注册采用了新标志
				seg.write((byte) 1);            //需要保存当前角色性别，因为角色列表没有了，在这个里下发，默认为女
				if(client.getDataVersion() >= 4){	// 此版本以后有快速注册【角色名，性别，阵营】
					if(Utils.hit(50, 100)){
						seg.write((byte) 2);			//默认为光明阵营
						player.setCamp((byte)2);
						if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
						}else{
							player.setFace((byte)29);
						}
					}else{
						seg.write((byte) 1);			//默认为光明阵营
						player.setCamp((byte)1);
						if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
						}else{
							player.setFace((byte)31);
						}
					}
					playerService.savePlayer(player);
				}				
				write(seg);	
			     // 此版本以后有快速注册【角色名，性别，阵营】
				//模拟登陆
				UWAPDataReadAndWrite.makeUWApDataReadAndWrite(ClientConstants.LOGIN);
				UWAPDataReadAndWrite.writeString(accountName);
				UWAPDataReadAndWrite.writeString(password);
				UWAPDataReadAndWrite.writeString(model);
				UWAPDataReadAndWrite.writeString(client.rawVersion);
				
				UWAPDataReadAndWrite.writeString(""); //cmccUserId
				UWAPDataReadAndWrite.writeString("");	 //cmccKey
				UWAPDataReadAndWrite.writeString(client.realPhone);
				UWAPDataReadAndWrite.writeByte(client.fastRegFlag);
				UWAPDataReadAndWrite.writeString(player.getPlayerName());
				byte[] data = UWAPDataReadAndWrite.getByteArray();
				UWAPDataReadAndWrite uWapdata = new UWAPDataReadAndWrite(data, serial, sessionId, false, 1);
				try{
					login(uWapdata);
				}
				catch (ITimesException ex) {
					client.fastRegFlag = 0;
		            sendError(ex);
		        } catch (Exception ex) {

		            log.error(ex, ex);
		           
		        }
			
				
			}
		} else if (type == 1) { //没有建帐号
			UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
			                           serial, sessionId);
			seg.writeString(accountName);
			seg.writeString(password);
			seg.writeString("");
			seg.write((byte) 1);
			write(seg);
		}
    }

	public void extend_quickLogonOK(UWAPData data) throws Exception {
		int sessindID = data.getSessionId();
		Client client = sessionId2Clients.get(sessindID); 
		WorldPlayer player = getPlayer(sessindID);
		int serial = data.getSerial();
		byte flag = data.readByte();			//0：快速注册1：表示注册角色
		String name = data.readString();		// 玩家的名称
		byte sexyint = data.readByte();			//性别0：为男性，1:为女性
		byte campByte = data.readByte();	 //性别1：黑暗阵营；2：光明阵营
		data.readInt();
		if (client != null && flag == 1 && (client.status == Client.STATUS.LOGIN || client.status == Client.STATUS.PLAYERLOGIN)){
			if(player == null){		// 普通注册
				try{
					player = new WorldPlayer(playerService.createPlayer(client, name, sexyint, 0));
					String model = client.model;
					if (sexyint == 0){		//改为男性
			    		// 阵营
				    	if(campByte == 1){	//黑暗
				    		player.setCamp(campByte);
				    		if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
				    			player.setFace((short)0);
				    		}else{
				    			player.setFace((short)30);
				    		}
				    	}else{
				    		player.setCamp(campByte);
				    		if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
				    			 player.setFace((short)0);
				    		}else{
				    			 player.setFace((short)28);
				    		}
				    	}
			    	}else{//改为女性
			    		// 阵营
				    	if(campByte == 1){
				    		player.setCamp(campByte);
				    		if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
				    			 player.setFace((short)1);
				    		}else{
				    			 player.setFace((short)31);
				    		}
				    	}else{
				    		player.setCamp(campByte);
				    		if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
				    			 player.setFace((short)1);
				    		}else{
				    			player.setFace((short)29);
				    		}
				    	}
			        }
					playerService.savePlayer(player);
					UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL,data.getSerial(), data.getSessionId());
			    	seg.writeShort(ClientConstants.EXTEND_QUICKLOGOCHANGE);
			    	seg.writeString(name);
			    	seg.write(sexyint);
			        write(seg);
			        return;
				}catch (CreatePlayerException ex) {
	                throw new ITimesException(ex.getMessage(), data.getSerial(), data.getSessionId(), data.getAppType());
	            }
				
			}
		}
		
		if (client != null && player != null) {
			synchronized (player) {
				String oldName = player.getPlayerName();
				if(!oldName.equals(name)){
					if (name.length() == 0) {
						throw new ITimesException("角色名不能为空", data.getSerial(),
								data.getSessionId(), data.getAppType());
					}
					if (name.getBytes("GBK").length > 16) {
						throw new ITimesException("角色名太长", data.getSerial(),
								data.getSessionId(), data.getAppType());
					}
					
					if (KeywordsUtil.isInvalidName(name.toLowerCase())) {
						throw new ITimesException("角色名出现非法字符", data.getSerial(),
								data.getSessionId(), data.getAppType());
					}
					if (!Utils.checkString(name, false)) {
						throw new ITimesException("角色名出现非法字符", data.getSerial(),
								data.getSessionId(), data.getAppType());
					}
					String newName = KeywordsUtil.filterKeywords(name);
					if (!newName.equals(name)) {
						throw new ITimesException("角色名出现非法字符", data.getSerial(),
								data.getSessionId(), data.getAppType());
					}
					Player p = playerService.getPlayerByName(name);
					if (p != null ) {
						throw new ITimesException("存在同名角色", data.getSerial(),
								data.getSessionId(), data.getAppType());
					}
				}
				//改性别
				client.roleName = name;
				String model = client.model;
				if (sexyint == 0){		//改为男性
					player.setSex((byte) 0);
					// 阵营
					if(campByte == 1){	//黑暗
						player.setCamp(campByte);
						if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
							player.setFace((short)0);
						}else{
							player.setFace((short)30);
						}
					}else{
						player.setCamp(campByte);
						if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
							player.setFace((short)0);
						}else{
							player.setFace((short)28);
						}
					}
				}else{//改为女性
					player.setSex((byte) 1);
					// 阵营
					if(campByte == 1){
						player.setCamp(campByte);
						if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
							player.setFace((short)1);
						}else{
							player.setFace((short)31);
						}
					}else{
						player.setCamp(campByte);
						if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
							player.setFace((short)1);
						}else{
							player.setFace((short)29);
						}
					}
				}
				
				if((oldName != null) && (!oldName.equals(name))){
					player.setPlayerName(name);
					playerService.savePlayer(player);

					playerService.renametocatch(player,oldName);
					
					playerService.loadWorldPlayer(name, client.accountId);
					
					log.info("QuickLogon ID[" + player.getId() + "]RENAME OLD[" + oldName +
							"] NEW[" + name + "]");
					Changed changed = new Changed();
					changed.setProperty(Changed.PLAYERNAME, name);
					sendGetItem(changed, serial, sessindID,
							(byte) 10);
				}
				if(flag == 0){
					UWAPDataReadAndWrite.makeUWApDataReadAndWrite(ClientConstants.PLAYER_LOGIN);
					UWAPDataReadAndWrite.writeString(client.roleName);
					byte[] data1 = UWAPDataReadAndWrite.getByteArray();
					//从登陆请求中获取serial就是bRequest.getId()和sessinID
					UWAPDataReadAndWrite uWapdata = new UWAPDataReadAndWrite(data1, serial, sessindID, false, 1);
					try {
						super.playerLogin(uWapdata);
						
					} catch (ITimesException ex) {
						client.fastRegFlag = 0;
						sendError(ex);
					} catch (Exception ex) {
						log.error(ex, ex);
					}  
				}
			}
		}
	}
	public void extend_quickLogonOut(UWAPData data) throws Exception {
		int sessindID = data.getSessionId();
		WorldPlayer player = getPlayer(sessindID);
		Client client = sessionId2Clients.get(sessindID); 
		synchronized (player){
			playerService.acquire(player);
	        player.setValid(false);
	        playerService.savePlayer(player);
			removeClient(client);	
			log.info("ID[" + player.getId() + "] deleted");
		}
		UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL,data.getSerial(), data.getSessionId());
    	seg.writeShort(ClientConstants.EXTEND_QUICKLOGOOUT);
        write(seg);
		
	}
    
	public void extend_create_imoney_card(UWAPData data) throws Exception {
	    WorldPlayer player = getPlayer(data.getSessionId());
	    
	    if(player != null){
	        int type = data.readInt();

	        log.info("ID[" + player.getId() + "] iShop Buy IMoneyCard cost[" + IMoneyCardService.IMONEY_CARD_AMOUNT[type] + "] imoney[" + player.getLongIMoney() + "] TRY");
	           
	        CreateIMoneyCardMessage message = new CreateIMoneyCardMessage(configuration.getString("gamecode"),player.getAccountId(),player.getkey(),IMoneyCardService.IMONEY_CARD_AMOUNT[type]);
	        CreateImoneyCardRequest request = new CreateImoneyCardRequest(data.getSerial(), data.getSessionId(), this, player.getId(), type);
	        requestService.add(message.getSerial(), request);
	        
	        accountSkeleton.send(message);
	    }
	}
	
	public void extend_create_imoney_card_result(int serial, int playerId, int accountId, String cardno, String password, int cost, long balance) throws Exception{
//	    WorldPlayer player = playerService.loadWorldPlayer(playerId);
		WorldPlayer player = playerService.getWorldPlayerAndCatch(playerId);

	    if(player != null){
	        iMoneyCardService.addIMoneyCard(player, cardno, password, cost);
	        
	        IItem cardItem = Items.getTemplate(IMoneyCardService.IMONEY_CARD_ITEM_ID_PIP).newInstance();
	        Changed changed = new Changed();
	        
	        player.setLongIMoney(balance);
            player.setConsumePoint(player.getConsumePoint() + cost / 100);
            
	        IItem item = player.completeAddItem(cardItem, 1, changed, player.getClientDataVersion());
	        
	        if(item == null){
	            byte[] att = ItemUtils.item2dbAttachment(cardItem, 1);
	            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", cardItem.getName() + "*" + 1, "", att, 0, true);
	            connectService.sendError(player.getId(), "你购买的" + cardItem.getName() + "由于背包满，已经邮寄到邮箱中，请注意查收。", serial, (byte) 86);
            }else{
                UWAPSegment seg = new UWAPSegment(ClientConstants.ISHOP_TRADE_OK);
                seg.writeInt(cost / 100);
                seg.writeInt(friendsService.getImoney(player.getId()));
                seg.writeInt(1);
                seg.write(ItemUtils.getAuctionBytes(cardItem, 1, player.getClientDataVersion(), -1));

                connectService.writeTo(seg, player.getId());
            }
	        
	        connectService.sendGetItem(changed, player.getId(), (byte) 33);

	        log.info("ID[" + player.getId() + "] iShop Buy IMoneyCard cardno[" + cardno + "] password[" + password + "] imoney[" + balance + "] OK");
	    }
	    playerService.releasePlayer(player);
	}
	
	public void extend_use_imoney_card(WorldPlayer player, UWAPData data) throws Exception{
	    if(player != null){
	        IMoneyCard iMoneyCard = iMoneyCardService.preUseIMoneyCard(player);
	        log.info("ID[" + player.getId() + "] Use IMoneyCard cardno[" + iMoneyCard.getCardno() + "] password[" + iMoneyCard.getPassword() + "] amount[" + iMoneyCard.getAmount() + "] imoney[" + player.getLongIMoney() + "] TRY");
	        
	        UseIMoneyCardMessage message = new UseIMoneyCardMessage(configuration.getString("gamecode"), player.getAccountId(), player.getkey(), iMoneyCard.getCardno(), iMoneyCard.getPassword());
	        UseImoneyCardRequest request = new UseImoneyCardRequest(data.getSerial(), data.getSessionId(), this, player.getId(), iMoneyCard);
	        requestService.add(message.getSerial(), request);
            
            accountSkeleton.send(message);
        }
	}
	
    public void extend_use_imoney_card_result(int serial, int playerId, int accountId, IMoneyCard card, long balance) throws Exception{
//        WorldPlayer player = playerService.loadWorldPlayer(playerId);
    	WorldPlayer player = playerService.getWorldPlayerAndCatch(playerId);
        if(player != null){
            player.setLongIMoney(balance);
            sendMessage(player.getId(), "你获得了" + (card.getAmount() / 100) + "i币");
            
            iMoneyCardService.doUseIMoneyCard(card);
            
            log.info("ID[" + player.getId() + "] Use IMoneyCard cardno[" + card.getCardno() + "] password[" + card.getPassword() + "] amount[" + card.getAmount() + "] imoney[" + balance + "] OK");
        }
        playerService.releasePlayer(player);
    }
}
