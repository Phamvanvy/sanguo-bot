package com.pip.itimes.server.world;

import com.pip.accountskeleton.*;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.world.StoreService.Request;
import com.pip.net.message.gameaccount.*;

import org.apache.mina.common.IoSession;
import com.pip.itimes.server.world.fee.ChargePlan;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.bean.IMoneyCard;
import com.pip.itimes.server.stage.IItem;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.HouseData;
import com.pip.itimes.server.world.game.HouseException;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.fee.FeePlan;
import com.pip.itimes.server.stage.Changed;

public class QQConnectSession extends ConnectSession2 {
    public QQConnectSession(IoSession session) {
        super(session);
    }

//    public char getIMoneyChar(){
//        return 'i';
//    }

    public void quickReg(UWAPData data) throws Exception {
//        String phone = data.readString();
//        String versionString = data.readString();
//        String model = data.readString();
//        Version version = versionService.getVersion(versionString);
//
//        if (version == null) {
//            throw new ITimesException("不支持的版本", data.getSerial(),
//                                      data.getSessionId(),
//                                      data.getAppType());
//        }
//        if (version.getStatus() == Version.STATUS_CANCELED) {
//            throw new ITimesException(version.getMessage(), data.getSerial(),
//                                      data.getSessionId(),
//                                      data.getAppType());
//        }
//        if (!version.isCanReg()) {
//            throw new ITimesException("此版本不支持注册功能", data.getSerial(),
//                                      data.getSessionId(),
//                                      data.getAppType());
//        }
//        LegacyQuickRegMessage msg = new LegacyQuickRegMessage(phone, versionString, model,
//                configuration.getString("gamecode"));
//        QuickRegRequest request = new QuickRegRequest(data.getSerial(), data.getSessionId(), this, phone, versionString,
//                model, configuration.getString("gamecode"));
//        requestService.add(msg.getSerial(),request);
//        accountSkeleton.send(msg);
//        if (!model.startsWith("NK-NGage") && !model.startsWith("MotoV300")) {
//            syncChannels(data.getSessionId(), new String[] {NORMAL90CHANNEL}, new String[] {FAST90CHANNEL});
//        }
    }

    public void accountReg(UWAPData data) throws Exception {
//        String name = data.readString();
//        String phone = data.readString();
//        String recommend = data.readString();
//        String model = data.readString();
//        String versionString = data.readString();
//        Version version = versionService.getVersion(versionString);
//        if (version == null) {
//            throw new ITimesException("不支持的版本", data.getSerial(),
//                                      data.getSessionId(),
//                                      data.getAppType());
//        }
//        if (version.getStatus() == Version.STATUS_CANCELED) {
//            throw new ITimesException(version.getMessage(), data.getSerial(),
//                                      data.getSessionId(),
//                                      data.getAppType());
//        }
//        if (!version.isCanReg()) {
//            throw new ITimesException("此版本不支持注册功能", data.getSerial(),
//                                      data.getSessionId(),
//                                      data.getAppType());
//        }
//        int recommendAccountId = -1;
//        if (recommend.length() > 0) {
//            recommendAccountId = playerService.getAccountIdByPlayerName(
//                    recommend);
//        }
//        AccountRegMessage msg = new AccountRegMessage(name,phone,recommend,recommendAccountId,model,configuration.getString("gamecode"),versionString);
//        AccountRegRequest request = new AccountRegRequest(data.getSerial(),data.getSessionId(),this);
//        requestService.add(msg.getSerial(),request);
//        accountSkeleton.send(msg);
//        if (!model.startsWith("NK-NGage") && !model.startsWith("MotoV300")) {
//            syncChannels(data.getSessionId(), new String[] {NORMAL90CHANNEL}, new String[] {FAST90CHANNEL});
//        }

    }

    public void notifyAccountName(int sessionId,WorldPlayer player){
//        UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE, -1, sessionId);
//        seg.writeString("你现在使用的帐号是" + player.getAccountName());
//        write(seg);
    }

    public void login(UWAPData data) throws Exception {
        String accountName = data.readString();
        String password = data.readString();
        String model = data.readString();
        String versionString = data.readString();
        String uin = data.readString();
        String key = data.readString();
        
        // 新版本会多传一个参数手机号
        String realPhone = "";
        try {
            realPhone = cutPhone(data.readString());
        } catch (Exception e) {
        }
        log.info("Uin[" + uin + "]Key[" + key + "]AccountName[" + accountName + "]Phone[" + realPhone + "]Version[" + versionString + "]Try Login");

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
        
        // 保存客户端信息
        Client client = getAndCreateClient(data.getSessionId());
        client.realPhone = realPhone;
        client.version = version;
        client.model = model;
        client.rawVersion = versionString;
        client.channel = cutChannel(versionString);
        
        if (client.status == Client.STATUS.INIT) {
            client.password = password;
            LegacyLoginMessage lm = new LegacyLoginMessage(uin,key,realPhone);
            LoginRequest lr = new LoginRequest(data.getSerial(),data.getSessionId(),this,accountName,password,model,versionString,false,null);
            requestService.add(lm.getSerial(),lr);
            accountSkeleton.send(lm);
            if (!model.startsWith("NK-NGage") && !model.startsWith("MotoV300")) {
                syncChannels(data.getSessionId(), new String[] {NORMAL90CHANNEL}, new String[] {FAST90CHANNEL});
            }
        } else {
            //todo:cut session
        }
    }


    public void relogin(UWAPData data) throws Exception {
        String accountName = data.readString();
        String password = data.readString();
        String playerName = data.readString();
        String model = data.readString();
        String versionString = data.readString();
        byte type = data.readByte();
        String uin = data.readString();
        String key = data.readString();
        
        // 新版本会多传一个参数手机号
        String realPhone = "";
        try {
            realPhone = cutPhone(data.readString());
        } catch (Exception e) {
        }
        log.info("Uin[" + uin + "]Key[" + key + "]AccountName[" + accountName + "]Phone[" + realPhone + "]Version[" + versionString + "]Try Login");

        // 检查版本是否支持
        String v = cutVersion(versionString);
        Version version = versionService.getVersion(v);
        log.info("Version[" + versionString + "]");
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
        client.realPhone = realPhone;
        client.version = version;
        client.model = model;
        client.rawVersion = versionString;
        client.channel = cutChannel(versionString);
        
        if (client.status == Client.STATUS.INIT || client.status == Client.STATUS.LOGIN) {
            LegacyLoginMessage lm = new LegacyLoginMessage(uin,key,realPhone);
            LoginRequest lr = new LoginRequest(data.getSerial(),data.getSessionId(),this,name,password,model,versionString,true,playerName);
            requestService.add(lm.getSerial(),lr);
            accountSkeleton.send(lm);
//            AccountRequest request = accountService.registerReloginRequest(data.getAppType(), data.getSerial(),
//                    data.getSessionId(), this, playerName);
//            UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN,
//                                              data.getSerial(), getSessionId());
//            seg.writeInt(request.id);
//            seg.writeString(accountName);
//            seg.writeString(password);
//            authSession.write(seg);
            if (!model.startsWith("NK-NGage") && !model.startsWith("MotoV300")) {
                syncChannels(data.getSessionId(), new String[] {NORMAL90CHANNEL}, new String[] {FAST90CHANNEL});
            }
        } else {
            //todo:cut session
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
//                UWAPSegment seg1 = new UWAPSegment(ServerConstants.PLAYER_LOGOUT);
//                seg1.writeInt(client.accountId);
//                authSession.write(seg1);
            }
        }
        if (client.status == Client.STATUS.PLAYERLOGIN) {
            WorldPlayer player = playerService.getWorldPlayer(client.playerId);
            if (player != null)
                logout(player);
        }
    }



    protected void sendRequestToAuth(Request request, int accountId, String key,int serial, int sessionId, boolean useBalance) throws
            ITimesException {
        LegacyBuy1Message message = new LegacyBuy1Message(accountId,key, request.price, useBalance);
        StoreRequest rq = new StoreRequest(serial, sessionId, this, request);
        requestService.add(message.getSerial(), rq);
        accountSkeleton.send(message);
//        UWAPSegment seg = new UWAPSegment(ServerConstants.BUY);
//        seg.writeInt(accountId);
//        if(request.consumeCode==null){
//            seg.writeInt(request.price);
//        }else{
//            seg.writeString(request.consumeCode);
//        }
//        seg.writeInt(request.id);
//        authSession.write(seg);
    }

    void addRecommendBalance(WorldPlayer player){
//        AddRecommendBalanceMessage msg = new AddRecommendBalanceMessage(player.getAccountId(),player.getRecommendvalue(),player.getRecommend2value());
//        accountSkeleton.send(msg);
    }

    void sendLogoutToAuth(WorldPlayer player){
        //老版本需要，新版本不需要
    }

    void modifyPassword(WorldPlayer player,int sessionId,int serial,String oldPassword,String newPassword){
        ModifyPasswordMessage message = new ModifyPasswordMessage(player.getAccountName(), player.getkey(),oldPassword,
                newPassword);
        ModifyPasswordRequest request = new ModifyPasswordRequest(serial, sessionId,
                this, player.getAccountName(), player.getkey(), newPassword);
        requestService.add(message.getSerial(),request);
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
		
	}

	public void extend_quickLogonOut(UWAPData data) throws Exception {
		
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
//        WorldPlayer player = playerService.loadWorldPlayer(playerId);
    	WorldPlayer player = playerService.getWorldPlayerAndCatch(playerId);
        if(player != null){
            iMoneyCardService.addIMoneyCard(player, cardno, password, cost);
            
            IItem cardItem = Items.getTemplate(IMoneyCardService.IMONEY_CARD_ITEM_ID_QQ).newInstance();
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
            sendMessage(player.getId(), "你获得了" + (card.getAmount() / 100) + "元宝");
            
            iMoneyCardService.doUseIMoneyCard(card);
            
            log.info("ID[" + player.getId() + "] Use IMoneyCard cardno[" + card.getCardno() + "] password[" + card.getPassword() + "] amount[" + card.getAmount() + "] imoney[" + balance + "] OK");
        }
        playerService.releasePlayer(player);
    }
}
