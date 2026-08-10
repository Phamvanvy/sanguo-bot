package com.pip.itimes.server.world;

import org.apache.mina.common.IoSession;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.net.UWAPDataReadAndWrite;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPData;
import com.pip.accountskeleton.GetAccountNameRequest;
import com.pip.accountskeleton.ModifyPhoneRequest;
import com.pip.accountskeleton.StoreRequest;
import com.pip.net.message.gameaccount.Logout1Message;
import com.pip.net.message.gameaccount.GetAccountNameMessage;
import com.pip.net.message.gameaccount.LegacyBuy1Message;
import com.pip.accountskeleton.ModifyPasswordRequest;
import com.pip.net.message.gameaccount.ModifyPhoneMessage;
import com.pip.itimes.server.world.StoreService.Request;
import com.pip.net.message.gameaccount.AddRecommendBalanceMessage;
import com.pip.net.message.gameaccount.ModifyPasswordMessage;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.server.world.game.HouseException;
import com.pip.itimes.server.stage.IStoreGroup;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.IItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.pip.itimes.server.stage.HouseData;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.world.fee.ChargePlan;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.server.util.Utils;
import com.pip.accountskeleton.LoginRequest;
import com.pip.itimes.server.world.fee.FeePlan;
import com.pip.itimes.server.bean.IMoneyCard;
import com.pip.itimes.server.bean.Ibuy;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.bean.SmsFee;
import com.pip.itimes.net.Packet;
import com.pip.itimes.server.ITimesException;

public class CmccConnectSession extends ConnectSession2 {

//    public char getIMoneyChar(){
//        return '点';
//    }

	//新快速注册标志
	private byte fastRegFlag ;
	
	//模拟登陆需要保留versionString.这里进行保留
	private String versionString;
	//模拟登陆需要保留电话号码.这里进行保留
	private String realPhone;
	//快速注册或者快速进入的角色名
	private String cmccUserId;
	private String cmccKey;
	
    public CmccConnectSession(IoSession session) {
        super(session);
    }
    
    public void handle(Packet packet) {
        UWAPData data = packet.datas[0];
        byte type = data.getAppType();

        try {
            switch (type) {
                case ClientConstants.BILLING_OK:
                    cmccSmsBuyResult(data);
                    
                    break;
                default:
                    super.handle(packet);
                    break;
            }
        } catch (ITimesException ex) {
            sendError(ex);
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    public void quickReg(UWAPData data) throws Exception {
        String phone = data.readString();
        String versionString = data.readString();
        String model = data.readString();
        String cmccUserId = data.readString().trim();
        String cmccKey = data.readString();
        
        this.versionString = versionString;
        this.cmccUserId = cmccUserId;
        this.cmccKey = cmccKey;
        // 新版本会多传一个参数手机号
        String realPhone = "";
        try {
            realPhone = cutPhone(data.readString());
            this.realPhone = realPhone;
        } catch (Exception e) {
        }
        log.info("CmccUserId[" + cmccUserId + "]Phone[" + realPhone + "]Version[" + versionString + "]Model[" + model + "]Try QuickReg");

        fastRegFlag = 0;
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
        
        // 保存客户端信息
        Client client = getAndCreateClient(data.getSessionId());
        client.cmccUserId = cmccUserId.trim();
        client.cmccKey = cmccKey;
        client.realPhone = realPhone;
        client.version = version;
        client.model = model;
        client.rawVersion = versionString;
        client.channel = cutChannel(versionString);

        // 向认证服务器转发请求
        AccountRequest request = accountService.registerRequest(data.getAppType(), data.getSerial(), data.getSessionId(), this);
        request.model = model;
        UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG, data.getSerial());
        seg.writeInt(request.id);
        seg.writeString(phone);
        seg.writeString(versionString);
        seg.writeString(model);
        seg.writeString(cmccUserId);
        seg.writeString(cmccKey);
        seg.writeString(configuration.getString("gamecode"));
        seg.writeString(realPhone);
        Server.instance.authSession.write(seg);
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
       boolean needReturn = data.readBoolean();
       String cmccUserId = data.readString().trim();
       String cmccKey = data.readString();
       
       // 新版本多一个参数手机号
       String realPhone = "";
       try {
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
       
       // 向认证服务器转发请求
       AccountRequest request = accountService.registerRequest(data.getAppType(), data.getSerial(), data.getSessionId(), this);
       UWAPSegment seg = new UWAPSegment(ClientConstants.ACCOUNT_REG,
                                         data.getSerial());
       seg.writeInt(request.id);
       seg.writeString(name);
       seg.writeString(phone);
       seg.writeString(recommend);
       seg.writeInt(recommendAccountId);
       seg.writeString(model);
       seg.writeString(versionString);
       seg.writeStrings(version.getCharge());
       seg.writeString(version.getFeeplan());
       seg.writeBoolean(needReturn);
       seg.writeString(cmccUserId);
       seg.writeString(cmccKey);
       seg.writeString(configuration.getString("gamecode"));
       seg.writeString(realPhone);
       seg.writeString(password);
       Server.instance.authSession.write(seg);
       if (!model.startsWith("NK-NGage") && !model.startsWith("MotoV300")) {
           syncChannels(data.getSessionId(), new String[] {NORMAL90CHANNEL}, new String[] {FAST90CHANNEL});
       }
   }

   public void login(UWAPData data) throws Exception {
       String accountName = data.readString();
       String password = data.readString();
       String model = data.readString();
       String versionString = data.readString();
       String cmccUserId = data.readString().trim();
       String cmccKey = data.readString();
       
       // 新版本多一个参数手机号
       String realPhone = "";
       try {
           realPhone = cutPhone(data.readString());
       } catch (Exception e) {
       }
       fastRegFlag = 0;
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
       
       if (client.status == Client.STATUS.INIT) {
    	   client.password = password;
    	   
           // 向认证服务器转发请求
           AccountRequest request = accountService.registerRequest(data.getAppType(), data.getSerial(),
                   data.getSessionId(), this);
           UWAPSegment seg = new UWAPSegment(ClientConstants.LOGIN,
                                             data.getSerial(), getSessionId());
           seg.writeInt(request.id);
           seg.writeString(accountName);
           seg.writeString(password);
           seg.writeString(cmccUserId);
           seg.writeString(cmccKey);
           seg.writeString(realPhone);
           Server.instance.authSession.write(seg);
           log.info("requestId["+request.id+"]");
           log.info("send to auth");
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
        String cmccUserId = data.readString().trim();
        String cmccKey = data.readString();
        
        // 新版本多一个参数手机号
        String realPhone = "";
        try {
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
            client.version = version;
            client.channel = cutChannel(versionString);
            client.model = model;
            
            // 向认证服务器转发请求
            AccountRequest request = accountService.registerReloginRequest(data.getAppType(), data.getSerial(),
                    data.getSessionId(), this, playerName);
            UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN,
                                              data.getSerial(), getSessionId());
            seg.writeInt(request.id);
            seg.writeString(accountName);
            seg.writeString(password);
            seg.writeString(cmccUserId);
            seg.writeString(cmccKey);
            seg.writeString(realPhone);
            Server.instance.authSession.write(seg);
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
                UWAPSegment seg1 = new UWAPSegment(ServerConstants.PLAYER_LOGOUT);
                seg1.writeInt(client.accountId);
                Server.instance.authSession.write(seg1);
            }
        }
        if (client.status == Client.STATUS.PLAYERLOGIN) {
            WorldPlayer player = playerService.getWorldPlayer(client.playerId);
            if (player != null)
                logout(player);
        }
    }
    protected void sendRequestToAuth(Request request, int accountId, String key, int serial, int sessionId, boolean useBalance) throws
            ITimesException {
        if (request.type == StoreService.Request.ITEM && request.count > 1) {
            // 卓望认证一次只能购买一件
            throw new ITimesException("本类商品一次只能购买一个。", serial, sessionId, ClientConstants.ISHOP_TRADE);
        }
        // lighthu添加：移动版本检查短信支付条件
        Client client = sessionId2Clients.get(sessionId);
        if (checkSmsBuy(request, playerService.getWorldPlayer(request.playerId), client)) {
            return;
        }
        
        UWAPSegment seg = new UWAPSegment(ServerConstants.BUY);
        seg.writeInt(accountId);
        seg.writeString(request.consumeCode);
        seg.writeInt(request.id);
        seg.writeString(client.rawVersion);
        Server.instance.authSession.write(seg);
    }

    void addRecommendBalance(WorldPlayer player) {
//        UWAPSegment seg = new UWAPSegment(ServerConstants.ADD_RECOMMEND_IMONEY);
//        seg.writeInt(player.getAccountId());
//        seg.writeInt(value);
//        Server.instance.authSession.write(seg);
    }

    void sendLogoutToAuth(WorldPlayer player) {
        UWAPSegment seg1 = new UWAPSegment(ServerConstants.PLAYER_LOGOUT);
        seg1.writeInt(player.getAccountId());
        Server.instance.authSession.write(seg1);
    }

    void modifyPassword(WorldPlayer player, int sessionId, int serial, String oldPassword, String newPassword) {
        UWAPSegment seg = new UWAPSegment(ServerConstants.
                                          MODIFY_PASSWORD);
        seg.writeInt(player.getAccountId());
        seg.writeInt(player.getId());
        seg.writeString(oldPassword);
        seg.writeString(newPassword);
        seg.writeString(newPassword);
        Server.instance.authSession.write(seg);
    }

    void modifyPhone(WorldPlayer player, int sessionId, int serial, String phone) {
        UWAPSegment seg = new UWAPSegment(ServerConstants.
                                          MODIFY_PHONE);
        seg.writeInt(player.getAccountId());
        seg.writeInt(player.getId());
        seg.writeString(phone);
        Server.instance.authSession.write(seg);
    }

    void cmccCharge(WorldPlayer player, int sessionId, int serial, int value) {

        StoreService.ChargeRequest request = storeService.charge(value, player, serial,
                sessionId, this);
        UWAPSegment seg = new UWAPSegment(ServerConstants.CHARGEUP);
        seg.writeInt(player.getAccountId());
        seg.writeInt(value/100);
        seg.writeInt(request.id);
        seg.writeInt(value);
        Server.instance.authSession.write(seg);
    }

    void getAccountName(int sessionId, int serial, int accountId, String playerName) {
        AccountRequest request = accountService.registerRequest((byte) 0, serial,
                sessionId, this);
        request.model = playerName;

        UWAPSegment seg = new UWAPSegment(ServerConstants.GET_ACCOUNTNAME, serial,
                                          sessionId);
        seg.writeInt(request.id);
        seg.writeInt(accountId);
        Server.instance.authSession.write(seg);
    }


//    void buyResult(BuyResult result,StoreService.Request request) throws Exception{
//        boolean success = result.success;
//        int iMoney = result.iMoney;
//        int cost = result.cost;
//        String cause = result.cause;
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
//                                UWAPSegment seg = new UWAPSegment(
//                                        ServerConstants.ADD_IMONEY);
//                                seg.writeInt(player.getAccountId());
//                                seg.writeInt(request.price);
//                                seg.writeInt(request.id);
//                                write(seg);
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
//                                            connectService.sendError(player.getId(), "你购买的" + request.item.item.getName() + "由于背包满，已经邮寄到邮箱中，请注意查收。",
//                                                                     request.serial, (byte) 86);
//                                        } else {
//                                            log.info("ID[" + player.getId() +
//                                                    "] iShop Buy Fail Item[" +
//                                                    request.item.item.getItemId() +
//                                                    "] Count[" +
//                                                    request.count + "]");
//                                            connectService.sendError(player.getId(),
//                                                    "包格满，购买失败", request.serial,
//                                                    (byte) 86);
//                                            UWAPSegment seg = new UWAPSegment(
//                                                    ServerConstants.ADD_IMONEY);
//                                            seg.writeInt(player.getAccountId());
//                                            seg.writeInt(request.price);
//                                            seg.writeInt(request.id);
//                                            write(seg);
//                                        }
//                                    } else {
//                                        if (request.item.count != -1) {
//                                            request.item.count -= request.count;
//                                            if (request.item.count < 0)
//                                                request.item.count = 0;
//                                        }
//                                        player.setiMoney(iMoney);
//                                        player.addCredit(cost*15/10000,changed);
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
//                                        connectService.sendGetItem(changed,player.getId(),(byte)33);
//                                    }
//                                }
//                            }
//                            Ibuy ibuy = new Ibuy();
//                            ibuy.setAccountid(player.getAccountId());
//                            ibuy.setPlayerid(player.getId());
//                            ibuy.setItemid(request.item.item.getItemId());
//                            ibuy.setItemname(request.item.item.getName());
//                            ibuy.setType((byte)1);
//                            if (cost == -1)
//                                ibuy.setImoney(request.item.price);
//                            else
//                                ibuy.setImoney(request.price/100);
//                            Date now = new Date();
//                            ibuy.setBuytime(now);
//                            ibuyService.addIbuy(ibuy);
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
//                                    (short) request.count,cost);
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
//                            houseModel.changeStyle(player, request.ht.getStyle(),cost);
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
//                            houseModel.addPart(player, request.hp,cost);
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
//                    } else if(request.type==StoreService.Request.WAITER){
//                        log.info("ID[" + player.getId() +
//                                 "] Buy Waiter UsediMoney[" +
//                                 cost + "] CurrentiMoney[" +
//                                 iMoney + "]");
//                        player.setiMoney(iMoney);
//                        HouseData hd = houseModel.getHouseByPlayerId(player.getId());
//                        hd.setCanUseWaiterTime(new Date(System.currentTimeMillis()+30*24*3600*1000L));
//                        houseModel.saveHouse(hd);
//                        connectService.sendMessage(player.getId(),"你已经成功雇佣了管家.");
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
//
//    }

    public void loginOk(LoginResult lr){
        int accountId = lr.accountId;
        String name = lr.name;
        String password = lr.password;
        String phone = lr.phone;
        int modifyPasswordTimes = lr.modifyPasswordTimes;
        long iMoney = lr.iMoney;
        boolean isMonth = lr.isMonth;
        boolean isSubscribe = lr.isSubscribe;
        int loginErrorTime = lr.loginErrorTime;
        Client client = getClient(lr.aRequest.sessionId);
        if (client != null && client.status == Client.STATUS.INIT) {
//            removeClient(client);
            Client client1 = accountId2Clients.get(accountId);
            if(client1!=null){
                connectService.forceLogout(accountId, client1.key);
            }

            client.status = Client.STATUS.LOGIN;
            client.accountId = accountId;
            client.name = name;
            client.password = password;
            client.phone = phone;
            client.modifyPasswordTimes = modifyPasswordTimes;
            client.iMoney = iMoney;
            client.isMonth = isMonth;
            client.isSubscribe = isSubscribe;
            client.loginErrorTime = loginErrorTime;
            //mengjie add
            client.cityname = lr.cityname;
            accountId2Clients.put(client.accountId, client);
            UWAPSegment seg = new UWAPSegment(ClientConstants.LOGIN_OK, lr.aRequest.serial, lr.aRequest.sessionId);
            seg.writeInt(accountId);
            seg.writeString(name);
            seg.writeString(phone);
            seg.writeInt(modifyPasswordTimes);
            seg.writeInt((int)(iMoney));
            seg.writeBoolean(isMonth);
            seg.writeBoolean(isSubscribe);

            write(seg);
            if(fastRegFlag == 1){
                	if(client.getDataVersion() >= 4 ){			//快速注册的时候
                		WorldPlayer  player = null;
    		            try {
    		            	player = playerService.loadWorldPlayer(client.roleName, client.accountId);
    		            } catch (ITimesException ex) {
    			            fastRegFlag = 0;
    			            sendError(ex);
    			        }catch (Exception ex) {
    		                log.error(ex, ex);
    		                removeClient(client);
    		                fastRegFlag = 0;
    		            }
    		            client.status = Client.STATUS.PLAYERLOGIN;
    		            client.playerId = player.getId();
    		            
    				}
            }
            log.info("AccountID[" + accountId + "]Logined");
        }
    }

    public void reloginResult(ReloginResult result) throws Exception{
        try {
            ReloginRequest r = (ReloginRequest) result.aRequest;
            byte type = result.type;
            if (type == 2) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT, r.serial, r.sessionId);
                seg.write((byte) 2);
                write(seg);
                Client client = getClient(result.aRequest.sessionId);
                if (client != null) {
                    removeClient(client);
                }
            } else {
                if (getPlayerCount() >= maxPlayer) {
                    UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT, r.serial, r.sessionId);
                    seg.write((byte) 2);
                    write(seg);
                    Client client = getClient(result.aRequest.sessionId);
                    if (client != null) {
                        removeClient(client);
                    }
                    return;
                }
                int accountId = result.accountId;
                String name = result.name;
                String password = result.password;
                String phone = result.phone;
                int modifyPasswordTimes = result.modifyPasswordTimes;
                long iMoney = result.iMoney;
                boolean isMonth = result.isMonth;
                boolean isSubscribe = result.isSubscribe;
                Client client = getClient(result.aRequest.sessionId);
                if (client != null) {
                    if (client.status == Client.STATUS.INIT || client.status == Client.STATUS.LOGIN) {
                        Client client1 = accountId2Clients.get(accountId);
                        if (client1 != null) {
                            connectService.forceLogout(accountId, client1.key);
                        }
                        client.status = Client.STATUS.LOGIN;
                        client.accountId = accountId;
                        client.name = name;
                        client.key = password;
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
                        throw new ITimesException("登录角色错误", result.aRequest.serial,
                                                  result.aRequest.sessionId,
                                                  result.aRequest.appType);
                    }

                    WorldPlayer player = playerService.loadWorldPlayer(r.playerName, accountId);

                    log.info("AccountId[" + accountId + "]Name[" + name + "]TRY RELOGIN");
                    if (player != null) {

                        if (player.getLevel() >= feePlan.getBeginLevel() && iMoney <= 0 &&
                            !isMonth && !isSubscribe) {
                        	playerService.unRegistry(player);
                            throw new ITimesException("您的余额不足，请登录\nwap.pipfit.com\n续费。",
                                    result.aRequest.serial, result.aRequest.sessionId,
                                    result.aRequest.appType);
                        }

                        if (playerService.isFrobiden(player.getId())) {
                        	playerService.unRegistry(player);
                            throw new ITimesException("此角色不可用", result.aRequest.serial,
                                    result.aRequest.sessionId, result.aRequest.appType);
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
                        player.setKey(password);
                        player.setPhone(phone);
                        player.setModifyPasswordTimes(modifyPasswordTimes);
                        player.setLastLifeTime(System.currentTimeMillis());
                        player.clearPosition();
                        player.setIsFirstEnter(true);
                        player.setIsOnce(true);
                        if (!contains(player.getId())) {
                            registry(player, result.aRequest.sessionId);
                            playerService.acquire(player);
                        } else {
                            sessionIds.put(new Integer(player.getId()), new Integer(result.aRequest.sessionId));
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
                                    UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
                                            result.aRequest.serial, result.aRequest.appType);
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
                                UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
                                        result.aRequest.serial, result.aRequest.appType);
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
                        addToChannels(result.aRequest.sessionId, getPlayerChannels(player));
                        addPlayerDispatchChat(result.aRequest.sessionId, player.getClientDataVersion(), player.getCamp());
                        
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
                                result.aRequest.serial,
                                result.aRequest.sessionId);
                        seg.write((byte) 0);
                        seg.writeInt(player.getId());
                        seg.writeString(player.getPlayerName());
                        seg.writeInt(player.getModifyNameTimes());
                        seg.writeShort(player.getMapId());
                        seg.writeShort(player.getX());
                        seg.writeShort(player.getY());
                        seg.write(player.toClientBytes(client.getDataVersion()));
                        seg.writeInt(result.aRequest.sessionId);
                        seg.writeInt(100);
                        seg.write(Ability.getAllAbilitiesBytes());
                        write(seg);

                        if (player.getTeam() != null) {
                            leaveTeam(player);
                        }
                        friendService.sendOnlineFriends(player);
                        player.setClient(client);
                        Utils.log(log, player.getId(), result.aRequest.appType,
                                  "RELOGIN OK ");
                    }
                }
            }
        } catch (ITimesException ex) {
            sendError(ex);
        }
    }
    
    public void buyResult(BuyResult result,StoreService.Request request) throws Exception {
        boolean success = result.success;
        long iMoney = result.iMoney;
        long bBalance = result.bBalance;
        
        int sendType = 0;
        
        /*if(request != null){
            if(!success && iMoney <= 0){
                switch(request.type){
                    case StoreService.Request.ITEM:
                        String[] cmccSMSCode = CmccSMSCodes.getSMSCode(request.consumeCode);
                        
                        if(cmccSMSCode != null && cmccSMSCode.length > 0){
                            WorldPlayer player = playerService.getWorldPlayer(request.playerId);
                            Integer sessionId = (Integer)sessionIds.get(player.getId());
                            Client client = null;
                            
                            if(sessionId != null){
                                client = sessionId2Clients.get(sessionId);
                            }
                            
                            if(client != null && client.version.getId().compareTo("2.1.0") > 0){
                                int smsFeeAmount = 0;
                                
                                try{
                                    smsFeeAmount = smsFeeService.getMonthSmsFee(player.getPhone());
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                
                                if(smsFeeAmount + request.price <= SMS_BUY_MAX){
                                    sendType = 1;
                                    request.cmccSmsMode = true;
                                    request.cmccSmsCode = cmccSMSCode[0];
                                    storeService.addCmccSmsRequest(request);
                                    
                                    byte[] bytes = stageService.getTaskBytes((short) 31035, new String[] {request.item.item.getName(), request.cmccSmsCode, String.valueOf(request.id), String.valueOf(request.price / 100)});
                                    UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
                                    seg.writeShort((short) 31035);
                                    seg.writeShort((short) 2);
                                    seg.write(bytes);
                                    
                                    request.session.write(seg, request.playerId);
                                    
                                    if("".equals(request.forothername)){
                                        log.info("ID[" + request.playerId + "] Cmcc SMS TRY iShop Buy Item[" +
                                                                    request.item.item.getItemId() +
                                                                    "] Count[" +
                                                                    request.count + "] iMoneyPrice[" +
                                                                    request.price + "] CurrentiMoney[" +
                                                                    player.getiMoney() + "] consumeCode[" + request.consumeCode + "] smsCode[" + request.cmccSmsCode + "]");
                                    }else{
                                        log.info("ID[" + request.playerId + "] Cmcc SMS TRY iShop Buy Item[" +
                                                                    request.item.item.getItemId() +
                                                                    "] Count[" +
                                                                    request.count + "] iMoneyPrice[" +
                                                                    request.price + "] CurrentiMoney[" +
                                                                    player.getiMoney() + "] consumeCode[" + request.consumeCode + "] smsCode[" + request.cmccSmsCode + "] " + 
                                                                    "] forother[" + request.forothername + "]");
                                    }
                                }else{
                                    sendType = 2;
                                }
                            }
                        }
                
                        break;
                }
            }
        }*/
        
        if(sendType == 0){
        	super.buyResult(result, request);
//            buyResultImpl(result, request);
        }else{
            connectService.sendError(request.playerId, "本月消费限额已满", request.serial, (byte) 86);
        }
    }
    
    /*
     * 收到客户端发送短信后上传的包。目前版本暂时在这里给玩家发道具。将来会改成收到认证服务器
     * 发过来的购买成功通知再发道具。
     */
    public void cmccSmsBuyResult(UWAPData data) throws Exception {
        // Lighthu 090312: 修改成收到认证服务器下发确认才发道具，这里就不发道具了
        if (true) {
            return;
        }
        
        Client client = getClient(data.getSessionId());
        WorldPlayer player = getPlayer(data.getSessionId());
        
        int requestId = data.readInt();
        StoreService.Request request = storeService.remove(requestId);
        
        BuyResult resulttemp = new BuyResult();
        resulttemp.success = true;
        resulttemp.iMoney = player.getLongIMoney();
        resulttemp.bBalance = player.getBBalance();
        resulttemp.cost = request.price;
        resulttemp.realCost = request.price;
        resulttemp.cause = "";
        resulttemp.sessionId = data.getSessionId();
        resulttemp.serial = data.getSerial();
        
        if("".equals(request.forothername)){
            log.info("ID[" + request.playerId + "] Cmcc SMS iShop Buy Item[" +
                                        request.item.item.getItemId() +
                                        "] Count[" +
                                        request.count + "] iMoneyPrice[" +
                                        request.price + "] CurrentiMoney[" +
                                        player.getLongIMoney() + "] consumeCode[" + request.consumeCode + "] smsCode[" + request.cmccSmsCode + "]"); 
        }else{
            log.info("ID[" + request.playerId + "] Cmcc SMS iShop Buy Item[" +
                                        request.item.item.getItemId() +
                                        "] Count[" +
                                        request.count + "] iMoneyPrice[" +
                                        request.price + "] CurrentiMoney[" +
                                        player.getLongIMoney() + "] consumeCode[" + request.consumeCode + "] smsCode[" + request.cmccSmsCode + "] " + 
                                        "] forother[" + request.forothername + "]");
        }
        
        try{
            SmsFee smsFee = new SmsFee();
            smsFee.setCharged(true);
            smsFee.setCreateTime(new Date());
            smsFee.setAccountId(player.getAccountId());
            smsFee.setPhone(request.cmccUserId);
            smsFee.setAmount(request.price);
            smsFee.setConsumeCode(request.consumeCode);
            smsFee.setSmsCode(request.cmccSmsCode);
            smsFeeService.addSmsFee(smsFee);
        }catch(Exception e){
            log.error(e, e);
        }
        
        buyResultImpl(resulttemp,request);
    }
    
    /*
     * 收到认证服务器的购买成功确认后的发货处理。
     */
    public void cmccSmsBuySucc(StoreService.Request request) throws Exception {
        WorldPlayer player = playerService.getWorldPlayer(request.playerId);

        BuyResult resulttemp = new BuyResult();
        resulttemp.success = true;
        resulttemp.iMoney = player.getLongIMoney();
        resulttemp.bBalance = player.getBBalance();
        resulttemp.cost = request.price;
        resulttemp.realCost = request.price;
        resulttemp.cause = "";
        
        if("".equals(request.forothername)){
            log.info("ID[" + request.playerId + "] Cmcc SMS iShop Buy Item[" +
                                        request.item.item.getItemId() +
                                        "] Count[" +
                                        request.count + "] iMoneyPrice[" +
                                        request.price + "] CurrentiMoney[" +
                                        player.getLongIMoney() + "] consumeCode[" + request.consumeCode + "] smsCode[" + request.cmccSmsCode + "]"); 
        }else{
            log.info("ID[" + request.playerId + "] Cmcc SMS iShop Buy Item[" +
                                        request.item.item.getItemId() +
                                        "] Count[" +
                                        request.count + "] iMoneyPrice[" +
                                        request.price + "] CurrentiMoney[" +
                                        player.getLongIMoney() + "] consumeCode[" + request.consumeCode + "] smsCode[" + request.cmccSmsCode + "] " + 
                                        "] forother[" + request.forothername + "]");
        }
        
        try{
            SmsFee smsFee = new SmsFee();
            smsFee.setCharged(true);
            smsFee.setCreateTime(new Date());
            smsFee.setAccountId(player.getAccountId());
            smsFee.setPhone(request.cmccUserId);
            smsFee.setAmount(request.price);
            smsFee.setConsumeCode(request.consumeCode);
            smsFee.setSmsCode(request.cmccSmsCode);
            smsFeeService.addSmsFee(smsFee);
        }catch(Exception e){
            log.error(e, e);
        }
        
        buyResultImpl(resulttemp,request);
    }

    public static int SMS_BUY_RATE = 20;
    public static int SMS_BUY_MAX = 500000;

    /*
     * 检查一个购买请求是否短信购买请求。短信消费代码是120121916或120121973开头的。
     * 如果是短信购买，则向认证服务器请求短信购买Token，并返回true，否则返回false。
     */
    protected boolean checkSmsBuy(StoreService.Request request, WorldPlayer player, Client client) {
        if (request.consumeCode != null && 
                (request.consumeCode.startsWith("120121916") || request.consumeCode.startsWith("120121973"))) {
            // 检查本月短信购买上限
            int smsFeeAmount = 0;
            try {
                smsFeeAmount = smsFeeService.getMonthSmsFee(client.cmccUserId);
            } catch (Exception e) {
                log.error(e, e);
            }
            if (smsFeeAmount + request.price <= SMS_BUY_MAX) {
                // 允许购买，向认证服务器请求短信购买序列号
                request.cmccSmsMode = true;
                request.cmccSmsCode = request.consumeCode;
                request.cmccUserId = client.cmccUserId.trim();
                storeService.addCmccSmsRequest(request);
                UWAPSegment seg = new UWAPSegment(ServerConstants.CMCC_SMS_BUY_REQ);
                seg.writeInt(player.getAccountId());
                seg.writeInt(player.getId());
                seg.writeString(request.cmccSmsCode);
                seg.writeInt(request.id);
                Server.instance.authSession.write(seg);
                return true;
            }
        }
        return false;
    }
    
    /*
     * 短信购买请求成功。
     */
    protected void smsBuyReqOk(StoreService.Request request, String token) {
        // 下发短信购买脚本
        byte[] bytes = stageService.getTaskBytes((short) 31035, new String[] {
                request.item.item.getName(), request.cmccSmsCode, 
                String.valueOf(request.id), String.valueOf(request.price / 100),
                token
        });
        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
        seg.writeShort((short) 31035);
        seg.writeShort((short) 2);
        seg.write(bytes);
        
        write(seg, request.playerId);
        
        if ("".equals(request.forothername)) {
            log.info("ID[" + request.playerId + "] Cmcc SMS2 TRY iShop Buy Item[" +
                                        request.item.item.getItemId() +
                                        "] Count[" + request.count + "] iMoneyPrice[" +
                                        request.price + "] consumeCode[" + request.consumeCode + 
                                        "] smsCode[" + request.cmccSmsCode + "]");
        } else {
            log.info("ID[" + request.playerId + "] Cmcc SMS2 TRY iShop Buy Item[" +
                                        request.item.item.getItemId() +
                                        "] Count[" + request.count + "] iMoneyPrice[" +
                                        request.price + "] consumeCode[" + request.consumeCode + 
                                        "] smsCode[" + request.cmccSmsCode + "] " + 
                                        "] forother[" + request.forothername + "]");
        }
    }

    /**
     * 过滤i币商店分类。只有短信版本才能看到短信购买分类。
     */
    protected Collection<IStoreGroup> filterIshopGroups(Client client, Collection<IStoreGroup> allGroups) {
        // 2.1.1版本是短信版本
        if (client != null && client.version != null && 
        		(client.version.getId().startsWith("2.1.1") || 
        		        client.version.getId().startsWith("3.2") ||
        		        client.version.getId().startsWith("3.4") ||
        		        client.version.getId().startsWith("3.6") ||
        		        client.version.getId().startsWith("4.2") ||
        		        client.version.getId().startsWith("4.4") ||
        		        client.version.getId().startsWith("4.6") ||
        		        client.version.getId().startsWith("4.8") ||
        		        client.version.getId().startsWith("5.2") ||
        		        client.version.getId().startsWith("4.11") ||
        		        client.version.getId().startsWith("4.12"))) {
            return allGroups;
        }
        List<IStoreGroup> ret = new ArrayList<IStoreGroup>();
        for (IStoreGroup g : allGroups) {
            if (g.getName().contains("短信")) {
                continue;
            }
            ret.add(g);
        }
        return ret;
    }
    public void quickRegResult(int accountId, String accountName, String password, byte type, String model,
            int sessionId, int serial) throws Exception {
			Client client = sessionId2Clients.get(sessionId); 
			client.accountId = accountId;
			if (type == 0) { //新建了帐号，需要新建角色
				//快速注册的玩家性别默认为女
				Player player = playerService.quickCreatePlayer(client, accountName, (byte) 1, serial);
				UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
				                           serial, sessionId);
				seg.writeString(accountName);
				seg.writeString(password);
				seg.writeString(player.getPlayerName());
				seg.write((byte) 0);  
				if(fastRegFlag == 0){
					write(seg);
				}
				if(fastRegFlag == 1){
					if(client.getDataVersion() >= 4){
						client.roleName = player.getPlayerName();
						seg.write((byte) 1);            //注册采用了新标志
						seg.write((byte) 1);            //
							// 此版本以后有快速注册【角色名，性别，阵营】
						if(Utils.hit(50, 100)){
							seg.write((byte) 2);			//默认为光明阵营
						}else{
							seg.write((byte) 1);			//默认为光明阵营
						}		
						write(seg);	
					     // 此版本以后有快速注册【角色名，性别，阵营】
						//模拟登陆
						UWAPDataReadAndWrite.makeUWApDataReadAndWrite(ClientConstants.LOGIN);
						UWAPDataReadAndWrite.writeString(accountName);
						UWAPDataReadAndWrite.writeString(password);
						UWAPDataReadAndWrite.writeString(model);
						UWAPDataReadAndWrite.writeString(versionString);
						
						UWAPDataReadAndWrite.writeString(cmccUserId); //cmccUserId
						UWAPDataReadAndWrite.writeString(cmccKey);	 //cmccKey
						UWAPDataReadAndWrite.writeString(realPhone);
						UWAPDataReadAndWrite.writeByte(fastRegFlag);
						UWAPDataReadAndWrite.writeString(player.getPlayerName());
						byte[] data = UWAPDataReadAndWrite.getByteArray();
						UWAPDataReadAndWrite uWapdata = new UWAPDataReadAndWrite(data, serial, sessionId, false, 1);
						try{
							login(uWapdata);
						}
						catch (ITimesException ex) {
							fastRegFlag = 0;
				            sendError(ex);
				        } catch (Exception ex) {
	
				            log.error(ex, ex);
				           
				        }
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
		
		if (client != null) {
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
						fastRegFlag = 0;
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
        
    }
	
	public void extend_create_imoney_card_result(int serial, int playerId, int accountId, String cardno, String password, int cost, long balance) throws Exception{
        
    }

    public void extend_use_imoney_card(WorldPlayer player, UWAPData data) throws Exception{

    }

    public void extend_use_imoney_card_result(int serial, int playerId, int accountId, IMoneyCard card, long balance) throws Exception{

    }
}
