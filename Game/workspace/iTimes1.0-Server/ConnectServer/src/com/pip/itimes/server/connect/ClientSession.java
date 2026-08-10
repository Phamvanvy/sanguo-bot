package com.pip.itimes.server.connect;

import com.pip.itimes.net.*;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.*;
import com.pip.itimes.server.connect.chat.ISendMessage;
import com.pip.itimes.server.stage.*;
import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;
import java.util.ArrayList;
import java.util.List;

public abstract class ClientSession extends Session {

    protected final static Logger log = Logger.getLogger(ClientSession.class);

    int accountId = -1;
    String accountName;
    String password;
    String phone;
    int iMoney;
    boolean isMonth;
    boolean isSubscribe;
    int modifyPasswordTimes;

    protected boolean created = false;

    protected BbsService bbsService = null;
    protected WorldSession worldSession = null;
    protected AuthSession authSession = null;
    protected MailService mailService = null;
    protected PlayerService playerService = null;

    protected ClientService clientService;


    protected ChatService chatService = null;

    protected BuyService buyService = null;
    protected OemService oemService = null;
    protected AuctionService auctionService = null;
    protected VersionService versionService = null;

    protected StageService stageService = null;

//    private TrustIpService trustIpService = null;

    protected boolean accountLogined = false;
    protected boolean playerLogined = false;
    public boolean playerLogouted = false;


//    PlayerData player;

    public int playerId = -1;
    protected short lastMapId = -1;
    protected String playerName = null;
    protected int modifyNameTimes = 0;

    protected Version version = null;

    protected boolean needFastSyncMode = false;
    protected int sync = 0;

    public String model = "";

    protected boolean offline_mode = false;

    protected boolean forceClose = false;

//    private static final SMSSender smsSender = new SMSSender(2);

//    private AccountDao accountDao = new AccountDao();

    public ClientSession(IoSession session) {
        super(session);
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }


//    public void setStageService(StageService stageService) {
//        this.stageService = stageService;
//    }

    public void setBbsService(BbsService bbsService) {
        this.bbsService = bbsService;
    }

    public void setWorldSession(WorldSession worldSession) {
        this.worldSession = worldSession;
    }

    public void setAuthSession(AuthSession authSession) {
        this.authSession = authSession;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

//    public void setStageService(StageService stageService) {
//        this.stageService = stageService;
//    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setBuyService(BuyService buyService) {
        this.buyService = buyService;
    }

    public void setOemService(OemService oemService) {
        this.oemService = oemService;
    }

//    public void setShopService(ShopService shopService){
//        this.shopService = shopService;
//    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    public void setStageService(StageService stageService) {
        this.stageService = stageService;
    }

//    public void setTrustIpService(TrustIpService trustIpService){
//        this.trustIpService = trustIpService;
//    }

    private static int segNum = 0;

    public void handle(Packet packet) {
        try {
            UWAPData data = packet.datas[0];
            log.debug("Client receive seg:" + (++segNum));
//            System.out.println("data type:" + data.getAppType());
            byte type = data.getAppType();
            switch (type) {
                case ClientConstants.ACCOUNT_REG:
                    register(data);
                    break;
                case ClientConstants.PLAYER_LOGIN:
                    playerLogin(data);
                    break;
                case ClientConstants.BBS_GET_LIST:
                    dispatchToWorld(data);
//                    bbsService.getBbsList(this, data);
                    break;
                case ClientConstants.BBS_POST:
                    dispatchToWorld(data);
//                    bbsService.addBbs(this, data);
                    break;
                case ClientConstants.BBS_GET_CONTENT:
                    dispatchToWorld(data);
//                    bbsService.getContent(this, data);
                    break;
                case ClientConstants.MAIL_POST:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.MAIL_GET_LIST:
                    dispatchToWorld(data);
//                    mailService.getMailList(this, data);
                    break;
                case ClientConstants.MAIL_GET_ATTACHMENT:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.MAIL_CONTENT:
                    dispatchToWorld(data);
//                    mailService.getContent(this, data);
                    break;
                case ClientConstants.MAIL_DELETE:
                    dispatchToWorld(data);

//                    mailService.deletMail(this,data);
                    break;
                case ClientConstants.GET_FILE:
                    getFile(data);

//                    getFile(data);
                    break;
                case ClientConstants.SEND_POSITION:
                    position(data);
                    break;
                case ClientConstants.LOGIN:
                    login(data);
                    break;
                case ClientConstants.ACTOR_CREATE:
                    actorCreate(data);
                    break;
                case ClientConstants.ACTOR_GET_LIST:
                    actorGetList(data);
                    break;
                case ClientConstants.CHAT:
                    chat(data);
                    break;
                case ClientConstants.PLAYER_UPLOAD:

//                    dispatchToWorld(data);
                    upload(data);
                    break;
                case ClientConstants.BATTLE_RESULT:
                    dispatchToWorld(data);

//                    battleResult(data);
                    break;
                case ClientConstants.TOUCH_NPC:
                    dispatchToWorld(data);

//                    touchNpc(data);
                    break;
                case ClientConstants.LEARN_ABILITY:
                    dispatchToWorld(data);

//                    learnAbility(data);
                    break;
                case ClientConstants.LEARN_SKILL:
                    dispatchToWorld(data);

//                    learnSkill(data);
                    break;
                case ClientConstants.GATHER:
                    dispatchToWorld(data);

//                    gather(data);
                    break;
                case ClientConstants.GET_DESC:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.PRODUCT:
                    dispatchToWorld(data);

//                    product(data);
                    break;
                case ClientConstants.GET_SKILL_LIST:
                    dispatchToWorld(data);

//                    getSkillList(data);
                    break;
                case ClientConstants.EQU_CHANGED:
                    dispatchToWorld(data);

//                    equChanged(data);
                    break;
                case ClientConstants.GATHER_RESULT:
                    dispatchToWorld(data);

//                    gatherResult(data);
                    break;
                case ClientConstants.TASK_COMPLETED:
                    dispatchToWorld(data);

//                    taskCompleted(data);
                    break;
                case ClientConstants.USE_ITEM:
                    dispatchToWorld(data);

//                    useItem(data);
                    break;
                case ClientConstants.GOT_TASKITEM:
                    dispatchToWorld(data);

//                    gotTaskItem(data);
                    break;
                case ClientConstants.ADD_PROPERTY_POINT:
                    dispatchToWorld(data);

//                    addPropertyPoint(data);
                    break;
                case ClientConstants.GET_CHATFAVORITE_LIST:
                    chatFavoriteList(data);
                    break;
                case ClientConstants.GET_CHATFAVORITE_DESC:
                    chatFavoriteDesc(data);
                    break;
                case ClientConstants.CHAT_OPTION:
                    dispatchToWorld(data);

//                    chatOption(data);
                    break;
                case ClientConstants.CHANGE_CHATFAVORITE:
                    dispatchToWorld(data);

//                    changeChatFavorite(data);
                    break;
                case ClientConstants.BATTLE_REQUEST:
                    battleRequest(data);
                    break;
                case ClientConstants.BATTLE_JOIN:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.BATTLE_FIGHT:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.PK_REQUEST:
                    pkRequest(data);
                    break;
                case ClientConstants.PK_REFUSE:
                    pkRefuse(data);
                    break;
                case ClientConstants.PK_OK:
                    pkOk(data);
                    break;
                case ClientConstants.PK_FIGHT:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.TEAM_CREATE:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.TEAM_INVIT:
                    teamInvit(data);
                    break;
                case ClientConstants.TEAM_INVIT_RESULT:
                    teamInvitResult(data);
                    break;
                case ClientConstants.TEAM_LEAVE:
                    teamLeave(data);
                    break;
                case ClientConstants.COMMAND:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.LOOK_EQU:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.REQUEST_SHOP_ITEM_LIST:
                    requestShopItemList(data);
                    break;
                case ClientConstants.REQUEST_AUCTION_LIST:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.REQUEST_AUCTION_DESC:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.AUCTION_ITEM:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.AUCTION_PRICE:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.RQUEST_BUY_MATERIAL_LIST:
                    requestBuyMaterialList(data);
                    break;
                case ClientConstants.REQUEST_OEM_LIST:
                    dispatchToWorld(data);

//                    requestOemList(data);
                    break;
                case ClientConstants.SHOP_ADD_ITEM:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.SHOP_MONEY_CHANGE:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.SHOP_REMOVE_ITEM:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.REQUEST_ITEM_LINK:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.SELL_MATERIAL:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.OEM:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.STORE_TRADE:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.SHOP_CHANGE:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.ADD_FRIEND:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.REQUEST_TASK_DESC:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.ADD_POINT:
                    dispatchToWorld(data);
                    break;
                case ServerConstants.STOP:

//                    dispatchToWorld(data);
                    stopServer(data);
                    break;
                case ClientConstants.REQUEST_TONG_MEMBERS:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.TONG_GRANT:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.TONG_MODIFY_TITLE:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.RELOGIN:
                    relogin(data);
                    break;
                case ClientConstants.TASK_ABANDON:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.USE_PET:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.ADD_PET_POINT:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.BUY_PET_POINT:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.FEED:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.DELETE_USER:
                    deleteUser(data);
                    break;
                case ClientConstants.CHANGE_OPTION:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.REPAIRE:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.HTTP_CLOSE:
                    httpClose(data);
                    break;
                case ClientConstants.BILLING_OK:
                    billingOk(data);
                    break;
                case ClientConstants.SEG_402:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.ECHO:
                    echo(data);
                    break;
                case ClientConstants.QUICK_REG:
                    quickReg(data);
                    break;
                case ClientConstants.REQUEST_FRIENDS_LIST:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.SNEAK_ATTACK:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.GENERIC_LIST_CONTENT:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.REQUEST_ISHOP_LIST:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.ISHOP_TRADE:
                    dispatchToWorld(data);
                    break;
                case ClientConstants.REQUEST_FACE:
                    requestFace(data);
                    break;
                case ClientConstants.CMCC_CHARGE:
                    cmccCharge(data);
                    break;

            }
        } catch (ITimesException ex) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,
                                              ex.getSerial(), ex.getSerial());
            seg.write(ex.getAppType());
            seg.writeString(ex.getMessage());
            write(seg);
        } catch (Exception e) {
            log.debug(e, e);
        }
    }

    public abstract void cmccCharge(UWAPData data) throws Exception;

    private void sneakAttack(UWAPData data) throws Exception {
        int playerId = data.readInt();
    }

    private void requestFace(UWAPData data) throws Exception {
        int count = data.readByte();
        short[] faces = new short[count];
        List<RoleFaceData> l = new ArrayList<RoleFaceData>();
        for (int i = 0; i < count; i++) {
            faces[i] = data.readShort();
            RoleFaceData d = RoleFaces.getRoleFace(faces[i]);
            if (d != null) {
                l.add(d);
            }
        }
        if (l.size() > 0) {
//            1	Count	数量	Byte
//            循环N次	Face	形象id	Short
//            循环N次	Type	类型	Type	0 行走 1 战斗 2 头像 3 战斗光效
//                    .p	.p文件	Byte[]
//                    .s	.s文件	Byte[]
            UWAPSegment seg = new UWAPSegment(ClientConstants.FACE_LIST,
                                              data.getSerial());
            seg.write((byte) l.size());
            for (int i = 0; i < l.size(); i++) {
                seg.writeShort((short) l.get(i).getFace());
            }
            for (int i = 0; i < l.size(); i++) {
                RoleFaceData d = l.get(i);
                seg.write((byte) 0);
                seg.write(d.getWalk().getPfile());
                seg.write(d.getWalk().getSfile());
                seg.write((byte) 1);
                seg.write(d.getBattle().getPfile());
                seg.write(d.getBattle().getSfile());
                seg.write((byte) 2);
                seg.write(d.getPortrait().getPfile());
                seg.write(d.getPortrait().getSfile());
                seg.write((byte) 3);
                seg.write(d.getEffect().getPfile());
                seg.write(d.getEffect().getSfile());
            }
            write(seg);
        }
    }


    protected abstract void quickReg(UWAPData data) throws Exception;

//    private void quickReg(UWAPData data) throws Exception{
////        if(Server.isMaintance)
////            throw new ITimesException("服务器正在维护状态",data.getSerial(),data.getAppType());
//        String phone = data.readString();
//        String version = data.readString();
//        String model = data.readString();
//        UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,data.getSerial(),getSessionId());
//        seg.writeString(phone);
//        seg.writeString(version);
//        seg.writeString(model);
//        authSession.write(seg);
//    }

    private void httpClose(UWAPData data) throws Exception {
        close();
    }

    private void billingOk(UWAPData data) throws Exception {
        log.info("AccountID[" + accountId + "]playerId[" + playerId +
                 "]Version[" + version.getId() + "]BillingOk");
    }

    private void echo(UWAPData data) throws Exception {
        byte[] bytes = data.readBytes();
        UWAPSegment seg = new UWAPSegment(ClientConstants.ECHO);
        seg.write(bytes);
        write(seg);
    }

    private void upload(UWAPData data) throws Exception {
        short mapId = data.readShort();
        short x = data.readShort();
        short y = data.readShort();
        byte[] taskSave = data.readBytes();
        boolean logout = data.readBoolean();
        dispatchToWorld(data);
        if (logout) {
            synchronized (this) {
                logout(false);
            }
        }
    }

    private void deleteUser(UWAPData data) throws Exception {
//        dispatchToWorld(data);
        String userName = data.readString();
        UWAPSegment seg = new UWAPSegment(ClientConstants.DELETE_USER,
                                          data.getSerial(), getSessionId());
        seg.writeString(userName);
        seg.writeInt(accountId);
        worldSession.write(seg);
//        Player player = playerService.getPlayerByNameAndAccountId(userName,
//                accountId);
//        if (player == null)
//            throw new ITimesException("没有找到角色", data.getSerial(),
//                                      data.getAppType());
//        if (player.getTongDuty() == Tong.OWNER)
//            throw new ITimesException("不能删除帮主角色", data.getSerial(),
//                                      data.getAppType());
//        player.setValid(false);
//        playerService.savePlayer(player);
//        UWAPSegment seg = new UWAPSegment(ClientConstants.DELETE_USER_OK,
//                                          data.getSerial());
//        write(seg);
    }

    protected abstract void relogin(UWAPData data) throws Exception;


    private void stopServer(UWAPData data) throws Exception {
        log.info("ID[" + playerId + "]Hack");
//        String name = data.readString();
//        String password = data.readString();
//        if("admin".equals(name)&&"admin".equals(password)){
//            dispatchToWorld(data);
//            authSession.forward(data,getSessionId());
//        }
//        clientService.stop();
//        Thread.sleep(1000*10);
//        System.exit(1);
    }

//    private void requestOemList(UWAPData data) throws Exception{
//        short areaId = data.readShort();
//        byte type = data.readByte();
//        String name = data.readString();
//        short pageSize = data.readShort();
//        int pageNo = data.readInt();
//        int count = oemService.getCount(areaId,type,name);
//        if(pageSize*pageNo>count){
//            throw new ITimesException("没有可显示的求做", data.getSerial(),
//                                      data.getAppType());
//        }
//        int pageCount = count / pageSize;
//        if (count % pageSize != 0) pageCount++;
//        Oem[] oems = oemService.getOems(areaId,type,name,pageNo*pageSize,pageSize);
//        UWAPSegment seg = new UWAPSegment(ClientConstants.OEM_LIST,data.getSerial());
//        seg.write(type);
//        seg.writeShort(pageSize);
//        seg.writeInt(pageNo);
//        seg.writeInt(count);
//        seg.writeShort((short)oems.length);
//        for(int i=0;i<oems.length;i++){
//            seg.writeInt(oems[i].getId());
//            seg.writeString(oems[i].getName());
//            seg.writeShort((short)(oems[i].getTotal()-oems[i].getCurrent()));
//            seg.writeInt(oems[i].getPay());
//            seg.writeShort((short)oems[i].getWorkPoint());
//            Recipe recipe = Recipes.getRecipe(oems[i].getItemId());
//            seg.writeBoolean(recipe.getPlayeGame());
//            seg.writeBoolean(player.containsRecipe(recipe));
//        }
//        write(seg);
//    }

    private void requestBuyMaterialList(UWAPData data) throws Exception {
        short areaId = data.readShort();
        byte type = data.readByte();
        String name = data.readString();
        short pageSize = data.readShort();
        int pageNo = data.readInt();
        int count = buyService.getCount(areaId, type, name);
        if (pageSize * pageNo > count) {
            throw new ITimesException("没有可显示的求购", data.getSerial(),
                                      data.getAppType());
        }
        int pageCount = count / pageSize;
        if (count % pageSize != 0)
            pageCount++;
        Buy[] buys = buyService.getBuys(areaId, type, name, pageSize * pageNo,
                                        pageSize);
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                          BUY_MATERIAL_LIST,
                                          data.getSerial(), data.getSessionId());
        seg.writeShort(areaId);
        seg.write(type);
        seg.writeShort(pageSize);
        seg.writeInt(pageNo);
        seg.writeInt(pageCount);
        seg.writeShort((short) buys.length);
        for (int i = 0; i < buys.length; i++) {
            seg.writeInt(buys[i].getId());
            seg.write((byte) buys[i].getItemId());
            seg.writeShort((short) (buys[i].getTotal() - buys[i].getCurrent()));
            seg.writeInt(buys[i].getPrice());
            //modify
            seg.write(buys[i].getQuality());
        }
        write(seg);
    }

    private void requestShopItemList(UWAPData data) throws Exception {
        int shopId = data.readInt();
        byte type = data.readByte();
        short pageSize = data.readShort();
        int pageNo = data.readInt();
        if (type == 1) { //acution
            int count = auctionService.getCount(shopId);
            if (pageSize * pageNo > count) {
                throw new ITimesException("没有可显示的拍卖物品", data.getSerial(),
                                          data.getAppType());
            }
            int pageCount = count / pageSize;
            if (count % pageSize != 0)
                pageCount++;
            Auction[] auctions = auctionService.getAuctions(shopId, type,
                    pageSize * pageNo, pageSize);
            UWAPSegment seg = new UWAPSegment(ClientConstants.SHOP_ITEM_LIST,
                                              data.getSerial());
            seg.writeInt(shopId);
            seg.write(type);
            seg.writeShort(pageSize);
            seg.writeInt(pageNo);
            seg.writeInt(pageCount);
            seg.writeShort((short) auctions.length);
            for (int i = 0; i < auctions.length; i++) {
                seg.writeInt(auctions[i].getId());
                seg.writeString(auctions[i].getName());
                seg.write(ItemUtils.getShopData(auctions[i]));
            }
            write(seg);
        } else if (type == 2) { //buy
            int count = buyService.getCount(shopId);
            if (pageNo * pageSize > count)
                throw new ITimesException("没有可显示的求购物品", data.getSerial(),
                                          data.getAppType());
            int pageCount = count / pageSize;
            if (count % pageSize != 0)
                pageCount++;
            Buy[] buys = buyService.getBuys(shopId, pageSize * pageNo, pageSize);
            UWAPSegment seg = new UWAPSegment(ClientConstants.SHOP_ITEM_LIST,
                                              data.getSerial());
            seg.writeInt(shopId);
            seg.write(type);
            seg.writeShort(pageSize);
            seg.writeInt(pageNo);
            seg.writeInt(pageCount);
            seg.writeShort((short) buys.length);
            for (int i = 0; i < buys.length; i++) {
                seg.writeInt(buys[i].getId());
                IItemTemplate item = Items.getTemplate(buys[i].getItemId());
                seg.writeString(item.getName());
                seg.write(ItemUtils.getShopData(buys[i]));
            }
            write(seg);
        } else if (type == 3) { //oem
            int count = oemService.getCount(shopId);
            if (pageNo * pageSize > count)
                throw new ITimesException("没有可显示的求做物品", data.getSerial(),
                                          data.getAppType());
            int pageCount = count / pageSize;
            if (count % pageSize != 0)
                pageCount++;
            Oem[] oems = oemService.getOems(shopId, pageSize * pageNo, pageSize);
            UWAPSegment seg = new UWAPSegment(ClientConstants.SHOP_ITEM_LIST,
                                              data.getSerial());
            seg.writeInt(shopId);
            seg.write(type);
            seg.writeShort(pageSize);
            seg.writeInt(pageNo);
            seg.writeInt(pageCount);
            seg.writeShort((short) oems.length);
            for (int i = 0; i < oems.length; i++) {
                seg.writeInt(oems[i].getId());
                seg.writeString(oems[i].getName());
                seg.write(ItemUtils.getShopData(oems[i]));
            }
            write(seg);
        } else if (type == 4||type==5) { //depot
            dispatchToWorld(data);
//            Shop shop = shopService.getShop(shopId);
//            if (shop != null) {
//                ShopData shopData = new ShopData(shop);
//                Grid[] items = shopData.getItems();
//                int count = items.length;
//                if(pageSize*pageNo>count){
//                    throw new ITimesException("超过最大页数", data.getSerial(),
//                                          data.getAppType());
//                }
//                int pageCount = count / pageSize;
//                if (count % pageSize != 0) pageCount++;
//                int begin = pageSize*pageNo;
//                int end = begin+pageSize-1;
//                if(end>count-1)
//                    end = count-1;
//                UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                                  SHOP_ITEM_LIST,
//                                                  data.getSerial());
//                seg.writeInt(shopId);
//                seg.write(type);
//                seg.writeShort(pageSize);
//                seg.writeInt(pageNo);
//                seg.writeInt(count);
//                seg.writeShort((short)(end-begin+1));
//                for (int i = begin; i < end; i++) {
//                    seg.writeInt(-1);
//                    seg.writeString(items[i].item.getName());
//                    seg.write(ItemUtils.getShopData(items[i]));
//                }
//                write(seg);
//            } else {
//                throw new ITimesException("查询仓库错误", data.getSerial(),
//                                          data.getAppType());
//            }
        }
    }

    private void teamLeave(UWAPData data) {
        try {
            int teamId = data.readInt();
            data.readInt();
            byte state = data.readByte();
            UWAPSegment seg = new UWAPSegment(ClientConstants.TEAM_LEAVE,
                                              data.getSerial(), getSessionId());
            seg.writeInt(teamId);
            seg.writeInt(playerId);
            seg.write(state);
            worldSession.write(seg);
        } catch (IllegalAccessException ex) {
        }
    }

    private void teamInvitResult(UWAPData data) {
        dispatchToWorld(data);
    }

    private void teamInvit(UWAPData data) {
        try {
            int teamId = data.readInt();
            data.readInt(); //id
            data.readString(); //name
            int targetId = data.readInt();
            UWAPSegment seg = new UWAPSegment(ClientConstants.TEAM_INVIT,
                                              data.getSerial(), getSessionId());
            seg.writeInt(teamId);
            seg.writeInt(playerId);
            seg.writeString(playerName);
            seg.writeInt(targetId);
            worldSession.write(seg);
        } catch (IllegalAccessException ex) {
        }
    }

    private void pkFight(UWAPData data) {
        try {
            int pkId = data.readInt();
            short roundId = data.readShort();
            int action = data.readInt();
            byte target = data.readByte();
            UWAPSegment seg = new UWAPSegment(ClientConstants.PK_FIGHT,
                                              data.getSerial(), getSessionId());
            seg.writeInt(playerId);
            seg.writeInt(pkId);
            seg.writeShort(roundId);
            seg.writeInt(action);
            seg.write(target);
            worldSession.write(seg);
        } catch (IllegalAccessException ex) {
        }
    }

    private void pkOk(UWAPData data) {
        try {
            int pkId = data.readInt();
            short level = data.readShort();
            UWAPSegment seg = new UWAPSegment(ClientConstants.PK_OK,
                                              data.getSerial(), getSessionId());
            seg.writeInt(playerId);
            seg.writeInt(pkId);
            seg.writeShort(level);
            worldSession.write(seg);
        } catch (IllegalAccessException ex) {
        }
    }

    private void pkRefuse(UWAPData data) {
        try {
            byte code = data.readByte();
            String cause = data.readString();
            int pkId = data.readInt();
            UWAPSegment seg = new UWAPSegment(ClientConstants.PK_REFUSE,
                                              data.getSerial(), getSessionId());
            seg.writeInt(playerId);
            seg.writeInt(pkId);
            seg.write(code);
            seg.writeString(cause);
            worldSession.write(seg);
        } catch (IllegalAccessException ex) {
        }
    }

    private void pkRequest(UWAPData data) {
        try {
            int id = data.readInt();
            String name = data.readString();
            int targetId = data.readInt();
            short level = data.readShort();
            short wager = data.readShort();
            UWAPSegment seg = new UWAPSegment(ClientConstants.PK_REQUEST,
                                              data.getSerial(), getSessionId());
            seg.writeInt(playerId);
            seg.writeString(playerName);
            seg.writeInt(targetId);
            seg.writeShort(level);
            seg.writeShort(wager);
            seg.writeInt(0);
            worldSession.write(seg);
        } catch (IllegalAccessException ex) {
        }

    }

    private void battleFight(UWAPData data) {
        try {
            int pkId = data.readInt();
            short roundId = data.readShort();
            int action = data.readInt();
            byte target = data.readByte();
            UWAPSegment seg = new UWAPSegment(ClientConstants.BATTLE_FIGHT,
                                              data.getSerial(), getSessionId());
            seg.writeInt(playerId);
            seg.writeInt(pkId);
            seg.writeShort(roundId);
            seg.writeInt(action);
            seg.write(target);
            worldSession.write(seg);
        } catch (IllegalAccessException ex) {
        }

    }

    private void battleJoin(UWAPData data) {
        worldSession.forward(data, getSessionId());
    }

    private void battleRequest(UWAPData data) {
        try {
            int teamId = data.readInt();
            int mgId = data.readInt();
            UWAPSegment seg = new UWAPSegment(ClientConstants.BATTLE_REQUEST,
                                              data.getSerial(), getSessionId());
            seg.writeInt(playerId);
            seg.writeInt(teamId);
            seg.writeInt(mgId);
            worldSession.write(seg);
        } catch (IllegalAccessException ex) {
        }

    }

    private void changeChatFavoriteW(UWAPData data) {
        try {
            int oldId = data.readInt();
            int newId = data.readInt();
            chatService.changeChatFavorite(playerId, oldId, newId);
//            if(index==-1){
//                player.setChatFavoriteId(-1);
//            }else{
//                ChatFavorite[] favorites = ChatFavorites.getChatFavorites();
//                int id = favorites[index].id;
//                player.setChatFavoriteId(id);
//            }
//            player.resetChatOptions();
//            playerService.savePlayer(player.getPlayer());
//            UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                              CHANGE_CHATFAVORITE,
//                                              data.getSerial(), getSessionId());
//            seg.writeInt(player.getId());
//            seg.writeInt(player.getChatFavoriteId());
//            worldSession.write(seg);
        } catch (Exception ex) {
        }
    }

    private void chatOptionW(UWAPData data) throws Exception {
        byte[] bytes = data.readBytes();
        byte[] bytes1 = data.readBytes();
        ChatOption[] chatOptions = getChatOptions(bytes);
        ChatOption[] newChatOptions = getChatOptions(bytes1);
//        for (int i = 0; i < 8; i++) {
//            ChatOption option = new ChatOption();
//            option.pri = bytes[i * 2];
//            option.color = bytes[i * 2 + 1];
//            chatOptions[i] = option;
//        }
//        ChatOption[] oldOptions = player.getChatOptions();
        //                player.setChatOptions(chatOptions);
        //                player.resetChatOptions();
        //                playerService.savePlayer(player.getPlayer());
        chatService.setOptions(playerId, newChatOptions, chatOptions);

    }

    private ChatOption[] getChatOptions(byte[] bytes) {
        ChatOption[] chatOptions = new ChatOption[8];
        for (int i = 0; i < 8; i++) {
            ChatOption option = new ChatOption();
            option.pri = bytes[i * 2];
            option.color = bytes[i * 2 + 1];
            chatOptions[i] = option;
        }
        return chatOptions;
    }

    private void chatFavoriteDesc(UWAPData data) {
        try {
            byte index = data.readByte();
            ChatFavorite[] favorites = ChatFavorites.getChatFavorites();
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              CHAT_FAVORITE_DESC,
                                              data.getSerial());
            seg.writeString(favorites[index].desc);
            write(seg);
        } catch (Exception ex) {
        }
    }

    private void chatFavoriteList(UWAPData data) {
        ChatFavorite[] favorites = ChatFavorites.getChatFavorites();
        String[] names = new String[favorites.length];
        for (int i = 0; i < favorites.length; i++) {
            names[i] = favorites[i].name;
        }
        UWAPSegment seg = new UWAPSegment(ClientConstants.CHATFAVORITE_LIST,
                                          data.getSerial());
        seg.writeStrings(names);
        write(seg);
    }

//    private void addPropertyPoint(UWAPData data) throws ITimesException {
//        try {
//            byte[] pros = data.readBytes();
//            int total = 0;
//            for (int i = 0; i < pros.length; i++) {
//                total += pros[i];
//            }
//            if (total > player.getLeavePoints())
//                throw new ITimesException("加属性点错误", data.getSerial(),
//                                          data.getAppType());
//            player.setLeavePoints(player.getLeavePoints() - total);
//            player.setVitality(player.getVitality() + pros[0]);
//            player.setStrength(player.getStrength() + pros[1]);
//            player.setAgility(player.getAgility() + pros[2]);
//            player.setIntelligence(player.getIntelligence() + pros[3]);
//            playerService.savePlayer(player.getPlayer());
//            UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                              ADD_PROPERTY_POINT_OK,
//                                              data.getSerial());
//            write(seg);
//        } catch (ITimesException e) {
//            throw e;
//        } catch (Exception ex) {
//
//        }
//    }

//    private void gotTaskItem(UWAPData data) {
//        try {
//            String name = data.readString();
//            byte count = data.readByte();
//            IItem item = Items.getTaskItem(name);
//            if (item != null) {
//                player.addItem(item, count);
//                player.reset();
//                playerService.savePlayer(player.getPlayer());
//            }
//        } catch (IllegalAccessException ex) {
//        }
//    }

//    private void useItem(UWAPData data) throws ITimesException {
//        try {
//            byte usedType = data.readByte();
//            byte itemType = data.readByte();
//            if(usedType==2){
//                if (itemType == IItem.TYPE_TASK) {
//                    String name = data.readString();
//                    int count = data.readInt();
//                    player.removeTaskItem(name, count);
//                } else {
//                    int id = data.readInt();
//                    int count = data.readInt();
//                    player.removeItem(itemType, id, count);
//                }
//            }
//            else if(usedType==1){
//                int itemId = data.readInt();
//                IItem item = Items.getItem(itemId);
//                Changed changed = new Changed();
//                if(item instanceof IEffectItem){
//                    bufService.playerUseItem(player,(IEffectItem)item,changed);
//                }
//                sendGetItem(changed,data.getSerial(),(byte)4);
//            }
//            player.reset();
//            playerService.savePlayer(player.getPlayer());
//        } catch (IllegalAccessException ex) {
//        }
//    }

//    private void taskCompleted(UWAPData data) throws ITimesException {
//        try {
//            short taskId = data.readShort();
//            byte id = data.readByte();
//            if (player.hasTask(taskId)) {
//                player.taskCompleted(taskId);
//                TaskAward award = TaskAwards.getTaskAward(taskId);
//                if (award != null) {
//                    Changed c = new Changed();
//                    c.addAward(award, id);
//                    c = player.addFallResult(c);
//                    player.resetTasks();
//                    player.reset();
//                    playerService.savePlayer(player.getPlayer());
//                    sendGetItem(c, data.getSerial(), (byte) 4);
//                }
//            }
//        } catch (IllegalAccessException ex) {
//        }
//    }


//    private void equChanged(UWAPData data) throws ITimesException {
//        try {
//            int[] equs = data.readInts();
//            if (player.changedEquipment(equs)) {
//                player.reset();
//                playerService.savePlayer(player.getPlayer());
//                UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                                  EQU_CHANGED_OK,
//                                                  data.getSerial());
//                write(seg);
//            } else {
//                throw new ITimesException("更换装备失败", data.getSerial(),
//                                          data.getAppType());
//            }
//        } catch (ITimesException e) {
//            throw e;
//        } catch (Exception ex) {
//        }
//
//    }

//    private void getSkillList(UWAPData data) {
//        try {
//            byte type = data.readByte();
//            Recipe[] recipe = player.getRecipes(type);
//            UWAPSegment seg = new UWAPSegment(ClientConstants.SKILL_LIST,
//                                              data.getSerial());
//            seg.write((byte) recipe.length);
//            for (int i = 0; i < recipe.length; i++) {
//                seg.writeShort(recipe[i].getId());
//                seg.writeString(recipe[i].getName());
//                seg.writeShort(recipe[i].getLevel());
//            }
//            write(seg);
//        } catch (IllegalAccessException ex) {
//        }
//    }

//    private void product(UWAPData data) throws ITimesException {
//        try {
//            short id = data.readShort();
//            Recipe recipe = Recipes.getRecipe(id);
//            Changed changed = ProductService.product(player, recipe);
//            playerService.savePlayer(player.getPlayer());
//            Object[] os = getChangedBytes(changed);
//            if (os.length > 0) {
//                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_ITEM);
//                seg.write((byte) 2);
//                seg.write((byte) os.length);
//                for (int i = 0; i < os.length; i++) {
//                    seg.write((byte[]) os[i]);
//                }
//                write(seg);
//            }
//        } catch (ProductException e) {
//            throw new ITimesException(e.getMessage(), data.getSerial(),
//                                      data.getAppType());
//        } catch (Exception ex) {
//        }
//    }

    private void getDesc(UWAPData data) throws ITimesException {
        try {
            byte type = data.readByte();
            int id = data.readInt();
            byte lookType = data.readByte();
            if (type == 1) { //skill
                Recipe recipe = Recipes.getRecipe(id);
                if (recipe == null)
                    throw new ITimesException("没找到指定配方", data.getSerial(),
                                              data.getAppType());
                String desc = recipe.getDesc();
                if (lookType == 1) {
                    desc += "\n学习费用：" + recipe.getMoney() + "J";
                }
                UWAPSegment seg = new UWAPSegment(ClientConstants.DESC,
                                                  data.getSerial());

                seg.write(type);
                seg.writeString(desc);
                write(seg);
            } else if (type == 2) { //ability
                Ability ability = Ability.getAbility(id);
                if (ability == null)
                    throw new ITimesException("没找到指定技能", data.getSerial(),
                                              data.getAppType());
                UWAPSegment seg = new UWAPSegment(ClientConstants.DESC,
                                                  data.getSerial());
                String desc = ability.getDesc();
                if (lookType == 1) {
                    desc += "\n学习费用：" + ability.getPrice() + "J";
                }
                seg.write(type);
                seg.writeString(desc);
                write(seg);
            } else if (type == 3) { //item
                IItemTemplate item = Items.getTemplate(id);
                if (item == null)
                    throw new ITimesException("没找到指定物品", data.getSerial(),
                                              data.getAppType());
                UWAPSegment seg = new UWAPSegment(ClientConstants.DESC,
                                                  data.getSerial());

                seg.write(type);
                seg.writeString(item.getDesc());
                write(seg);
            }
        } catch (ITimesException e) {
            throw e;
        } catch (Exception ex) {
        }
    }

//    private void gatherResult(UWAPData data) {
//        try {
//            int id = data.readInt();
//            int point = data.readInt();
//            UWAPSegment seg = new UWAPSegment(ClientConstants.GATHER_RESULT,
//                                              data.getSerial(),
//                                              getSessionId());
//            seg.writeInt(player.getId());
//            seg.writeInt(id);
//            seg.writeInt(point);
//            worldSession.write(seg);
//        } catch (Exception ex) {
//        }
//
//    }

//    private void gather(UWAPData data) {
//        try {
//            int id = data.readInt();
//            UWAPSegment seg = new UWAPSegment(ClientConstants.GATHER,
//                                              data.getSerial(), getSessionId());
//            seg.writeInt(player.getId());
//            seg.writeInt(id);
//            worldSession.write(seg);
//        } catch (Exception ex) {
//        }
//    }

//    private void learnSkill(UWAPData data) throws ITimesException {
//        try {
//            short id = data.readShort();
//            Recipe recipe = Recipes.getRecipe(id);
//            if (player.containsRecipe(recipe)) {
//                throw new ITimesException("已经学会此配方", data.getSerial(),
//                                          data.getAppType());
//            }
//            if(player.getMoeny()<recipe.getMoney()){
//                throw new ITimesException("没有足够金钱", data.getSerial(),
//                                          data.getAppType());
//            }
//            if(player.getSkillLevel(recipe.getType())<recipe.getSkillLevel()){
//                throw new ITimesException("你还不能学此配方", data.getSerial(),
//                                          data.getAppType());
//            }
//            player.addRecipe(recipe);
//            player.resetRecipes();
//            int money = player.getMoeny();
//            player.setMoeny(money-recipe.getMoney());
//            Changed changed = new Changed();
//            changed.addProperty(Changed.MONEY,-money);
//            playerService.savePlayer(player.getPlayer());
//            sendGetItem(changed,data.getSerial(),(byte)5);
//            UWAPSegment seg = new UWAPSegment(ClientConstants.LEAR_SKILL_OK,
//                                              data.getSerial());
//            write(seg);
//        } catch (ITimesException e) {
//            throw e;
//        } catch (Exception ex) {
//        }
//    }

//    private void learnAbility(UWAPData data) throws ITimesException {
//        try {
//            short id = data.readShort();
//            Ability ability = Ability.getAbility(id);
//            if (player.containsAbility(ability))
//                throw new ITimesException("已经学会此技能", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            if (player.getLevel() >= ability.getRequiredLevel()) {
//                player.addAbility(ability);
//                player.resetAbilities();
//                playerService.savePlayer(player.getPlayer());
//                UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                                  LEAR_ABILITY_OK,
//                                                  data.getSerial());
//                seg.writeShort((short) ability.getId());
//                write(seg);
//            } else {
//                throw new ITimesException("等级不够", data.getSerial(),
//                                          data.getSessionId(), data.getAppType());
//            }
//        } catch (ITimesException e) {
//            throw e;
//        } catch (Exception ex) {
//
//        }
//    }

//    private void touchNpc(UWAPData data) {
//        try {
//            int id = data.readInt();
//            TaskNpc npc = TaskNpcs.getTaskNpc(id);
//            TaskNpcType npcType = TaskNpcTypes.getTaskNpcType(npc.getType());
//            if (npcType != null) {
//                int type = npcType.getType();
//                if (type == 0) { //ability
//                    AbilityNpcType aNpc = (AbilityNpcType) npcType;
//                    Ability[] abs = Ability.getAbilities(aNpc.getAbilities(),
//                            player.getLevel(), player.getAbilitiesId());
//                    short[] ids = new short[abs.length];
//                    for (int i = 0; i < ids.length; i++) {
//                        ids[i] = (short) abs[i].getId();
//                    }
//                    UWAPSegment seg = new UWAPSegment(ClientConstants.
//                            ABILITY_LIST, data.getSerial());
//                    seg.writeShorts(ids);
//                    write(seg);
//                } else if (type == 1) { //skill
//                    SkillNpcType sNpc = (SkillNpcType) npcType;
//                    Recipe[] recipes = sNpc.getRecipes();
//                    List sl = new ArrayList();
//                    for (int i = 0; i < recipes.length; i++) {
//                        if (!player.containsRecipe(recipes[i])) {
//                            sl.add(recipes[i]);
//                        }
//                    }
//                    UWAPSegment seg = new UWAPSegment(ClientConstants.
//                            SKILL_LIST, data.getSerial());
//                    seg.write((byte) sl.size());
//                    for (int i = 0; i < sl.size(); i++) {
//                        Recipe recipe = (Recipe) sl.get(i);
//                        seg.writeShort(recipe.getId());
//                        seg.writeString(recipe.getName());
//                        seg.writeShort(recipe.getLevel());
//                    }
//                    write(seg);
//                }
//            }
//        } catch (Exception ex) {
//        }
//    }

//    private void battleResult(UWAPData data) {
//        try {
//            byte result = data.readByte();
//            int hp = data.readInt();
//            int mp = data.readInt();
//            player.setHp(hp);
//            player.setMp(mp);
//            if (result == 1) { //胜利
//                int mgId = data.readInt();
//                byte count = data.readByte();
//                short stageId = (short) ((mgId >> 20) & 0xFFFF);
//                Stage stage = stageService.getStage(stageId);
//                if (stage != null) {
//                    ByteList l = new ArrayByteList();
//                    for (int i = 0; i < count; i++) {
//                        byte index = data.readByte();
//                        byte status = data.readByte();
//                        if (status == 1) { //死亡
//                            l.add(index);
//                        }
//                    }
//                    if (l.size() > 0) {
//                        Monster[] monsters = new Monster[l.size()];
//                        for (int i = 0; i < l.size(); i++) {
//                            monsters[i] = stage.getMonster(l.get(i));
//                        }
//                        Changed changed = FallCalculator.getFallItems(player,
//                                monsters);
//                        changed = player.addFallResult(changed);
//                        bufService.checkBattleBuff(player,changed);
//                        player.reset();
//                        playerService.savePlayer(player.getPlayer());
//                        sendGetItem(changed,data.getSerial(),(byte)1);
//                    }
//                }
//            }
//        } catch (IllegalAccessException ex) {
//        }
//    }

    private Object[] getChangedBytes(Changed changed) {
        return changed.toClientBytes();
    }


    private void logout(boolean sendToWorld) {
        if (forceClose) {
            log.info("ACCOUNTID[" + accountId + "]ID[" + playerId +
                     "]ForceClose");
            return;
        }
        log.info("ACCOUNTID[" + accountId + "]ID[" + playerId + "]ENTERLOGOUT");
        UWAPSegment seg = new UWAPSegment(ServerConstants.
                                          PLAYER_LOGOUT, -1,
                                          sessionId);
        seg.writeInt(accountId);
        seg.writeInt(playerId);
        seg.writeBoolean(true);
        if (!playerLogouted) {
            authSession.write(seg);
            log.info("ACCOUNTID[" + accountId + "]ID[" + playerId + "]SENDAUTH");
        }
        clientService.removeClient(this);
        if (playerId != -1) {
            chatService.unRegistry(playerId);
        }
        playerLogouted = true;
        if (sendToWorld) {
            worldSession.write(seg);
            log.info("ACCOUNTID[" + accountId + "]ID[" + playerId +
                     "]SENDWORLD");
        }
        if (!session.isClosing()) {
            session.close();
        }
        playerId = -1;
        playerName = null;
        lastMapId = -1;
        accountId = -1;
        accountName = "";
        password = "";
        phone = "";
        modifyPasswordTimes = 0;
        model = "";
    }

//    private void upload(UWAPData data) {
//        try {
//            short mapId = data.readShort();
//            short x = data.readShort();
//            short y = data.readShort();
//            byte[] taskSave = data.readBytes();
//            boolean logout = data.readBoolean();
//            if (player != null) {
//                player.setMapId(mapId);
//                player.setX(x);
//                player.setY(y);
//                player.setTaskData(taskSave);
//                playerService.savePlayer(player.getPlayer());
//                UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                                  PLAYRE_UPLOAD_OK,
//                                                  data.getSerial());
//                write(seg);
//                if (logout) {
//                    UWAPSegment seg1 = new UWAPSegment(ServerConstants.
//                            PLAYER_LOGOUT, data.getSerial(), sessionId);
//                    seg1.writeInt(accountId);
//                    seg1.writeInt(player.getId());
//                    seg1.writeBoolean(true);
//                    worldSession.write(seg1);
//                    authSession.write(seg1);
//                    clientService.removeClient(player.getId());
//                    chatService.unRegistry(player);
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    public void handleServer(Packet packet) {
        UWAPData data = packet.datas[0];
//        System.out.println("server data type:" + data.getAppType());
        byte type = data.getAppType();
        try {
            switch (type) {
                case ClientConstants.ERROR:
                    reply(data);
                    break;
                case ClientConstants.ACCOUNT_REG_OK:
                    regOk(data);
                    break;
                case ClientConstants.LOGIN_OK:
                    loginOk(data);
                    break;
//                case ClientConstants.GATHER_RESULT:
//                    gatherResultW(data);
//                    break;
                case ClientConstants.GATHER_OK:
                    gatherOkW(data);
                    break;
                case ClientConstants.BATTLE_INIT:
                    reply(data);
                    break;
                case ClientConstants.BATTLE_JOIN_RESULT:
                    reply(data);
                    break;
                case ClientConstants.BATTLE_ROUND_END:
                    reply(data);
                    break;
                case ClientConstants.BATTLE_START:
                    reply(data);
                    break;
                case ClientConstants.PK_CREATED:
                    reply(data);
                    break;
                case ClientConstants.PK_REQUEST:
                    reply(data);
                    break;
                case ClientConstants.PK_REFUSE:
                    reply(data);
                    break;
                case ClientConstants.PK_START:
                    reply(data);
                    break;
                case ClientConstants.PK_ROUND_END:
                    reply(data);
                    break;
                case ClientConstants.GET_ITEM:
                    reply(data);

//                    getItemW(data);
                    break;
                case ClientConstants.TEAM_CREATE_OK:
                    reply(data);
                    break;
                case ClientConstants.TEAM_INVIT:
                    reply(data);
                    break;
                case ClientConstants.TEAM_INVIT_RESULT:
                    reply(data);
                    break;
                case ClientConstants.TEAM_JOIN_OK:
                    reply(data);
                    break;
                case ClientConstants.TEAM_JOIN_FAIL:
                    reply(data);
                    break;
                case ClientConstants.TEAM_LEAVE:
                    reply(data);
                    break;
                case ClientConstants.GET_FILE:
                    reply(data);
                    break;
                case ClientConstants.MAIL_GET_ATTACHMENT:
                    reply(data);
                    break;
                case ClientConstants.PLAYRE_UPLOAD_OK:
                    playerUploadOkW(data);
                    break;
                case ClientConstants.CHAT_OPTION:
                    chatOptionW(data);
                    break;
                case ClientConstants.CHANGE_CHATFAVORITE:
                    changeChatFavoriteW(data);
                    break;
                case ClientConstants.BATTLE_RESULT:
                    reply(data);
                    break;
                case ClientConstants.ABILITY_LIST:
                    reply(data);
                    break;
                case ClientConstants.SKILL_LIST:
                    reply(data);
                    break;
                case ClientConstants.LEAR_ABILITY_OK:
                    reply(data);
                    break;
                case ClientConstants.LEAR_SKILL_OK:
                    reply(data);
                    break;
                case ClientConstants.EQU_CHANGED_OK:
                    reply(data);
                    break;
                case ClientConstants.ADD_PROPERTY_POINT_OK:
                    reply(data);
                    break;
                case ClientConstants.LOOK_EQU_OK:
                    reply(data);
                    break;
                case ClientConstants.SHOP_LIST:
                    reply(data);
                    break;
                case ClientConstants.AUCTION_TYPE_LIST:
                    reply(data);
                    break;
                case ClientConstants.AUCTION_LIST:
                    reply(data);
                    break;
                case ClientConstants.AUCTION_DESC:
                    reply(data);
                    break;
                case ClientConstants.AUCTION_PRICE_OK:
                    reply(data);
                    break;
                case ClientConstants.AUCTION_ITEM_OK:
                    reply(data);
                    break;
                case ClientConstants.SELL_MATERIAL_OK:
                    reply(data);
                    break;
                case ClientConstants.SHOP_MONEY_CHANGE_OK:
                    reply(data);
                    break;
                case ClientConstants.SHOP_REMOVE_ITEM_OK:
                    reply(data);
                    break;
                case ClientConstants.SHOP_ADD_ITEM_OK:
                    reply(data);
                    break;
                case ClientConstants.OEM_OK:
                    reply(data);
                    break;
                case ClientConstants.SHOP_CREATE_OK:
                    reply(data);
                    break;
                case ClientConstants.SHOP_ITEM_LIST:
                    reply(data);
                    break;
                case ClientConstants.STORE_ITEM_LIST:
                    reply(data);
                    break;
                case ClientConstants.STORE_TRADE_OK:
                    reply(data);
                    break;
                case ClientConstants.OEM_TYPE_LIST:
                    reply(data);
                    break;
                case ClientConstants.BUY_MAERIAL_TYPE_LIST:
                    reply(data);
                    break;
                case ClientConstants.TASK_COMPLETED:
                    reply(data);
                    break;
                case ClientConstants.SEND_POSITION:
                    reply(data);
                    break;
                case ClientConstants.SHOP_CHANGE_OK:
                    reply(data);
                    break;
                case ClientConstants.ADD_FRIEND_OK:
                    reply(data);
                    break;
                case ClientConstants.MAIL_POST_OK:
                    reply(data);
                    break;
                case ClientConstants.MAIL_NEW:
                    reply(data);
                    break;
                case ClientConstants.TASK_DESC:
                    reply(data);
                    break;
                case ClientConstants.ADD_POINT_OK:
                    reply(data);
                    break;
                case ClientConstants.TONG_CREATE_OK:
                    reply(data);
                    break;
                case ClientConstants.TONG_MEMBERS:
                    reply(data);
                    break;
                case ClientConstants.TONG_GRANT_OK:
                    reply(data);
                    break;
                case ClientConstants.MESSAGE:
                    reply(data);
                    break;
                case ClientConstants.CHAT:
                    reply(data);
                    break;
                case ClientConstants.FRIENDS_STATUS:
                    reply(data);
                    break;
                case ServerConstants.RELOGIN_RESULT:
                    reloginA(data);
                    break;
                case ClientConstants.TASK_ABANDON_RESULT:
                    reply(data);
                    break;
                case ClientConstants.ADD_PET_POINT_OK:
                    reply(data);
                    break;
                case ClientConstants.BUY_PET_POINT_OK:
                    reply(data);
                    break;
                case ClientConstants.USE_PET_OK:
                    reply(data);
                    break;
                case ClientConstants.BATTLE_ABORT:
                    reply(data);
                    break;
                case ClientConstants.PLAYER_LOGIN_OK:
                    playerLoginOkW(data);
                    break;
                case ClientConstants.OEM_LIST:
                    reply(data);
                    break;
                case ClientConstants.RELOGIN_RESULT:
                    reloginResultW(data);
                    break;
                case ClientConstants.REFRESH:
                    reply(data);
                    break;
                case ClientConstants.DESC:
                    reply(data);
                    break;
                case ClientConstants.REPAIRE_LIST:
                    reply(data);
                    break;
                case ClientConstants.REPAIRE_OK:
                    reply(data);
                    break;
                case ClientConstants.CHANGE_OPTION_OK:
                    changeOptionOkW(data);
                    break;
                case ClientConstants.DELETE_USER_OK:
                    reply(data);
                    break;
                case ClientConstants.SEG_402_RESULT:
                    reply(data);
                    break;
                case ClientConstants.QUICK_REG:
                    quickRegA(data);
                    break;
                case ClientConstants.GENERIC_LIST:
                    reply(data);
                    break;
                case ClientConstants.GENERIC_LIST_CONTENT:
                    reply(data);
                    break;
                case ClientConstants.ISHOP_LIST:
                    reply(data);
                    break;
                case ClientConstants.ISHOP_TRADE_OK:
                    reply(data);
                    break;
                case ClientConstants.FACE_LIST:
                    reply(data);
                    break;
                case ClientConstants.SNEAK_ATTACK:
                    reply(data);
                    break;
                case ClientConstants.MAIL_LIST:
                    reply(data);
                    break;
                case ClientConstants.MAIL_CONTENT:
                    reply(data);
                    break;
                case ClientConstants.BBS_CONTENT:
                    reply(data);
                    break;
                case ClientConstants.BBS_POST_OK:
                    reply(data);
                    break;
                case ClientConstants.BBS_LIST:
                    reply(data);
                    break;
                case ClientConstants.CMCC_CHARGE_OK:
                    reply(data);
                    break;
            }
        } catch (ITimesException ex) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,
                                              ex.getSerial(), ex.getSerial());
            seg.write(data.getAppType());
            seg.writeString(ex.getMessage());
            write(seg);
        } catch (Exception e) {
            log.debug(e, e);
        }
    }

    private void quickRegA(UWAPData data) throws Exception {
        int accountId = data.readInt();
        String accountName = data.readString();
        String password = data.readString();
        String playerName = data.readString();
        byte type = data.readByte();
        if (type == 0) { //新建了帐号，需要新建角色
            Player player = playerService.quickCreatePlayer(accountId,
                    accountName, (byte) 0, data.getSerial(), model);
            UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
                                              data.getSerial());
            seg.writeString(accountName);
            seg.writeString(password);
            seg.writeString(player.getPlayerName());
            seg.write((byte) 0);
            write(seg);
        } else if (type == 1) { //没有建帐号
            UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
                                              data.getSerial());
            seg.writeString(accountName);
            seg.writeString(password);
            seg.writeString("");
            seg.write((byte) 1);
            write(seg);
        } else if (type == 2) { //修改了角色名
            UWAPSegment seg = new UWAPSegment(ClientConstants.QUICK_REG,
                                              data.getSerial());
            seg.writeString(this.accountName);
            seg.writeString("");
            seg.writeString(playerName);
            seg.write((byte) 2);
            write(seg);
        }
    }

    private void changeOptionOkW(UWAPData data) throws Exception {
        short[] option = data.readShorts();
        if (option[0] == 1)
            offline_mode = true;
        else
            offline_mode = false;
    }

    private void reloginResultW(UWAPData data) throws Exception {
        data.readByte();
        int playerId = data.readInt();
        String pName = data.readString();
        int modifyNameTimes = data.readInt();
        if(!pName.equals(playerName)){
            log.info("Login Conflicting reloginResultW");
            throw new ITimesException("登陆错误，稍后再试",data.getSerial(),data.getSessionId(),data.getAppType());
        }
        this.playerId = playerId;
        this.playerName = pName;
        this.modifyNameTimes = modifyNameTimes;
        clientService.addClient(playerId, this);
        playerLogined = true;
        reply(data);
        log.info("Relogined[" + playerId + "] Version[" + version.getId() +
                 "]Model[" + model + "]");
    }

    private void playerLoginOkW(UWAPData data) throws Exception {
        int playerId = data.readInt();
        String name = data.readString();
        int modifyNameTimes = data.readInt();
        short mapId = data.readShort();
        if(!name.equals(playerName)){
            log.info("Login Conflicting LoginOkW");
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              RELOGIN_RESULT,
                                              data.getSerial(),
                                              data.getSessionId());
            seg.write((byte) 2);
            write(seg);
            return;
        }
        this.playerId = playerId;
        this.modifyNameTimes = modifyNameTimes;
        this.playerName = name;
        clientService.addClient(playerId, this);
        playerLogined = true;
        reply(data);
//        int unReaded = mailService.getUnReadedMailCount(playerId);
//        if (unReaded > 0) {
//            UWAPSegment seg = new UWAPSegment(ClientConstants.MAIL_NEW);
//            seg.write((byte) 1);
//            write(seg);
//        }
        log.info("Logined[" + playerId + "] Version[" + version.getId() +
                 "]Model[" + model + "]");
    }

    private void reloginA(UWAPData data) throws Exception {
        byte type = data.readByte();
        if (type == 0) {
            int accountId = data.readInt();
            String name = data.readString();
            String password = data.readString();
            String phone = data.readString();
            int modifyPasswordTimes = data.readInt();
            String pName = data.readString();
            if(!name.equalsIgnoreCase(accountName)||!pName.equals(playerName)){
                log.info("Login Conflicting reloginA");
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  RELOGIN_RESULT,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.write((byte) 2);
                write(seg);
                return;
            }
            this.accountId = accountId;
            this.playerName = name;
            this.password = password;
            this.phone = phone;
            this.modifyPasswordTimes = modifyPasswordTimes;
            this.playerName = pName;
            playerId = playerService.getPlayerId(playerName, accountId);
            if (playerId == -1) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  RELOGIN_RESULT,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.write((byte) 2);
                write(seg);
                return;
            }
            ClientSession client = clientService.getClient(playerId);
            if (client != null) {
                client.forceClose = true;
                client.close();
            }
            iMoney = data.readInt();
            isMonth = data.readBoolean();
            isSubscribe = data.readBoolean();
            byte reloginType = data.readByte();
            UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
                                              data.getSerial(),
                                              data.getSessionId());
            seg.writeInt(accountId);
            seg.writeString(accountName);
            seg.writeString(password);
            seg.writeString(phone);
            seg.writeInt(modifyPasswordTimes);
            seg.writeString(playerName);
            seg.writeInt(version.getMaxLevel());
            seg.writeString(version.getId());
            seg.writeString(model);
            seg.writeStrings(version.getCharge());
            seg.writeString(version.getFeeplan());
            seg.writeInt(iMoney);
            seg.writeBoolean(isMonth);
            seg.writeBoolean(isSubscribe);
            if (client != null)
                seg.writeInt(client.getSessionId());
            else
                seg.writeInt( -1);
            seg.write(reloginType);
            worldSession.write(seg);
        } else {
            UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT);
            seg.write(type);
            write(seg);
//            reply(data);
        }
//        byte type = data.readByte();
//        if (type == 0) {
//            accountLogined = true;
//            accountId = data.readInt();
//            String playerName = data.readString();
//            Player p = playerService.getPlayerByNameAndAccountId(
//                    playerName, accountId);
//            if (p == null) {
//                UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,data.getSerial());
//                seg.write((byte)2);
//                write(seg);
//            } else {
//                player = new PlayerData(p);
//                if(playerService.isForbid(p.getId())){
//                    UWAPSegment seg = new UWAPSegment(ClientConstants.RELOGIN_RESULT,data.getSerial());
//                    seg.write((byte)2);
//                    write(seg);
//                    return;
//                }
//                clientService.removeAndLogout(player.getId());
//                authSession.playerLoginOk(this, data);
//                UWAPSegment seg1 = new UWAPSegment(ClientConstants.RELOGIN_RESULT,
//                        data.getSerial());
//                seg1.write((byte)0);
//                seg1.writeInt(player.getId());
//                seg1.writeString(player.getPlayerName());
//                seg1.writeShort(player.getMapId());
//                seg1.writeShort(player.getX());
//                seg1.writeShort(player.getY());
//                seg1.write(player.toClientBytes());
//                seg1.writeInt(getSessionId());
//                seg1.writeInt(100);
//                seg1.write(stageService.getAllAbilitiesBytes());
//                write(seg1);
//                clientService.addClient(this);
//                chatService.registry(player);
//                playerLogined = true;
//                int unReaded = mailService.getUnReadedMailCount(player.getId());
//                if (unReaded > 0) {
//                    UWAPSegment seg2 = new UWAPSegment(ClientConstants.MAIL_NEW);
//                    seg2.write((byte) 1);
//                    write(seg2);
//                }
//                UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                                  PLAYER_LOGIN_OK,
//                                                  data.getSerial(),
//                                                  getSessionId());
//                seg.writeInt(player.getId());
//                seg.writeInt(version.getMaxLevel());
//                worldSession.write(seg);
//            }
//        }else{
//            reply(data);
//        }
    }

    private void playerUploadOkW(UWAPData data) throws Exception {
//        boolean logout = data.readBoolean();
//        if(logout){
//            UWAPSegment seg = new UWAPSegment(ClientConstants.PLAYRE_UPLOAD_OK,data.getSerial());
//            write(seg);
//            logout(false);
//        }
    }

//    private void getItemW(UWAPData data){
//        try {
//            byte cause = data.readByte();
//            byte count = data.readByte();
//            Object[] os = new Object[count];
//            for (int i = 0; i < count; i++) {
//                os[i] = data.readBytes();
//            }
//            Changed changed = new Changed(os);
//            Changed c = player.addFallResult(changed);
//            player.reset();
//            playerService.savePlayer(player.getPlayer());
//            sendGetItem(c, data.getSerial(), cause);
//        } catch (IllegalAccessException ex) {
//        }
//    }

    private void gatherOkW(UWAPData data) {
        reply(data);
    }


//    private void gatherResultW(UWAPData data) throws ITimesException {
//        try {
//            int itemId = data.readInt();
//            int resourceId = data.readInt();
//            byte count = data.readByte();
//            IItem item = Items.getItem(itemId);
//            Changed changed = new Changed();
//            if (item != null) {
//                int t = player.addItem(item, count);
//                player.reset();
//                playerService.savePlayer(player.getPlayer());
//                if (t > 0) {
//                    changed.addItem(itemId, t);
//                    sendGetItem(changed, data.getSerial(), (byte) 3);
//                } else {
//                    throw new ITimesException("没有空余的包位", data.getSerial(),
//                                              ClientConstants.GATHER_RESULT);
//                }
//            }
//
//        } catch (ITimesException e) {
//            throw e;
//        } catch (IllegalAccessException ex) {
//        }
//    }

    public void sendGetItem(Changed changed, int serial, byte cause) {
        Object[] os = getChangedBytes(changed);
        if (os.length > 0) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_ITEM, serial);
            seg.write(cause);
            seg.write((byte) os.length);
            for (int i = 0; i < os.length; i++) {
                seg.write((byte[]) os[i]);
            }
            write(seg);
        }
    }

    public void created() {
        session.setIdleTime(IdleStatus.READER_IDLE, 300);
        clientService.addSession(this);
    }

    public void opened() {
        created = true;
    }

    public void closed() {
        log.info("ACCOUNTID[" + accountId + "]ID[" + playerId + "]ENTERCLOSED");
        synchronized (this) {
            if (!playerLogouted) {
                logout(true);
            }
        }
    }

//    public void setLogouted(){
//        playerLogouted = true;
//    }

    public void idle(IdleStatus status) {
        log.info("ID[" + playerId + "]IDLE[" + session.getIdleTime(status) +
                 "]");
        if (!session.isClosing())
            session.close();
        else {
            synchronized (this) {
                if (!playerLogouted) {
                    logout(true);
                }
            }
        }
    }


    private void chat(UWAPData data) {
        try {
            int srcId = data.readInt();
            String name = data.readString();
            int destId = data.readInt();
            String content = data.readString();
            UWAPSegment seg = new UWAPSegment(ClientConstants.CHAT,
                                              data.getSerial(), getSessionId());
            seg.writeInt(playerId);
            seg.writeString(playerName);
            seg.writeInt(destId);
            seg.writeString(content);
            worldSession.write(seg);
        } catch (Exception ex) {
            log.info(ex, ex);
        }
    }

    private void getFile(UWAPData data) throws Exception {
        String model = data.readString();
        short level = data.readShort();
        short type = data.readShort();
        short sequence = data.readShort();
        if (type == 3 || type == 4 || type == 5) {
            PngResourceData resource = stageService.getPng(type, sequence);
            if (resource == null)
                throw new Exception("下载错误");
            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                                              data.getSerial(),
                                              data.getSessionId());
            seg.writeShort(sequence);
            seg.writeShort(type);

            seg.write(resource.png);
            seg.write(resource.desc);
            write(seg);
        } else if (type == 2) {

            if (sequence >= 30000) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                                                  data.getSerial(),
                                                  data.getSessionId());
                if (!(model.equals("NK-40-2") || model.equals("MotoV300")))
                    seg.forceCompress();

                seg.writeShort(sequence);
                seg.writeShort(type);

                byte[] bytes = stageService.getTaskBytes(sequence);
                seg.write(bytes);
                write(seg);
            } else {
                dispatchToWorld(data);
            }
        } else {
            dispatchToWorld(data);
        }
//        try {
//            String mode = data.readString();
//            short level = data.readShort();
//            short type = data.readShort();
//            short id = data.readShort();
//            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
//                                              data.getSerial());
//            seg.writeShort(id);
//            seg.writeShort(type);
//            Map parameters = new HashMap();
//            short[] currentTasksId = player.getCurrentTasksId();
//            parameters.put("CURRENTTASKS", currentTasksId);
//            if (type == 1) {
//                byte[] bytes = stageService.getStageBytes(id, parameters);
//                if (bytes == null) {
//                    throw new Exception("下载错误");
//                }
//                seg.write(bytes);
//                seg.write(player.getTaskData());
//                String[] names = stageService.getTasksName(currentTasksId);
//                short[] tasks = (short[]) parameters.get("TASKS");
//                seg.writeShorts(player.getCompletedTasksId(tasks));
//                seg.writeShorts(currentTasksId);
//                seg.writeStrings(names);
//                write(seg);
//            } else if (type == 2) {
//                byte[] bytes = stageService.getTaskBytes(id);
//                if (bytes == null)
//                    throw new Exception("下载错误");
//                if (id < 30000) {
//                    player.addTask(id);
//                    player.resetCurrentTasks();
//                    playerService.savePlayer(player.getPlayer());
//                    seg.write(bytes);
//                    seg.writeShorts(player.getCompletedTasksId(id));
//                    seg.writeShorts(player.getCurrentTasksId(id));
//                    write(seg);
//                } else {
//                    seg.write(bytes);
//                    write(seg);
//                }
//            } else if (type == 3 || type == 4 || type == 5) {
//                PngResourceData resource = stageService.getPng(type, id);
//                if (resource == null)
//                    throw new Exception("下载错误");
//                seg.write(resource.png);
//                seg.write(resource.desc);
//                write(seg);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new ITimesException("下载错误", data.getSerial(), data.getAppType());
//        }
    }

    private void actorGetList(UWAPData data) throws ITimesException {
        if (accountLogined) {
            playerService.getPlayerList(this, data);
        }
    }

    public int getLastMapId() {
        return lastMapId;
    }

    private void position(UWAPData data) throws Exception {
        short mapId = data.readShort();
        short x = data.readShort();
        short y = data.readShort();
        if (playerId != -1) {
            short oldMapId = lastMapId;
            if (oldMapId != -1 && oldMapId != mapId) {
                chatService.positionChanged(playerId, oldMapId, mapId);

            }
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              SEND_POSITION, data.getSerial(),
                                              getSessionId());
            seg.writeInt(playerId);
            seg.writeShort(mapId);
            seg.writeShort(x);
            seg.writeShort(y);
            worldSession.write(seg);
        }
        if (lastMapId != mapId) {
            if (lastMapId == -1 &&
                version.getStatus() == Version.STATUS_OBSOLETE) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.CHAT);
                seg.writeInt( -1);
                seg.writeString("系统");
                seg.writeInt(ISendMessage.SYSTEM);
                seg.writeString(version.getMessage());
                write(seg);
            }
            if (lastMapId == -1) {
                byte[] bytes = stageService.getTaskBytes((short) 31005,
                        new String[] {
                        "http://3g.sina.com.cn/wulin/Record.jsp?user=" +
                        playerId + "&account=" + accountId});
                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeShort((short) 31005);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
                if (mapId / 16 != 98 && mapId / 16 != 97) {
                    if ((modifyNameTimes > 0 || modifyPasswordTimes > 0) &&
                        mapId / 16 != 98 && mapId / 16 != 97) {
                        sendQuickRegMessage(mapId,x,y,data.getSerial(),data.getSessionId());
                    }
                } else {

                }
                
                Player p = playerService.getPlayerById(playerId);
                byte[] options = p.getOptions();
                
                if(options != null && options.length > 0 && (options[0] != 0 || options[1] != 0)){
                    seg = new UWAPSegment(ClientConstants.MESSAGE, -1, sessionId);
                    seg.writeString("您目前处于免打扰模式，这会加快运行速度，但会使您无法看到其他玩家。您可以随时通过“系统设置”功能将此模式关闭");
                    write(seg);
                }

//                if(modifyNameTimes>0||modifyPasswordTimes>0){
//                    UWAPSegment seg = new UWAPSegment(ClientConstants.CHAT);
//                    seg.writeInt(-1);
//                    seg.writeString("系统");
//                    seg.writeInt(ISendMessage.SYSTEM);
//                    seg.writeString(getQuickRegMessage());
//                    write(seg);
//                }
            } else if (lastMapId == 1553) {
                if ((modifyNameTimes > 0 || modifyPasswordTimes > 0) &&
                    mapId / 16 != 98 && mapId / 16 != 97) {
                    sendQuickRegMessage(mapId,x,y,data.getSerial(),data.getSessionId());
                }
            }
            lastMapId = mapId;
        }

    }

    private void sendQuickRegMessage(int mapId, int x, int y, int serial,
                                     int sessionId) {
        String passWordMessage = "您所登录的帐号为：" + accountName + "密码为：" +
                                 password + "，请牢记。\n您可以与";
        String actorNameMessage = "您所登录的角色为：" + playerName +
                                  "，您有一次修改角色名称的机会。\n您可以与";

        if (mapId == 1617 && Math.abs(x - 272) < 10 &&
            Math.abs(y - 120) < 10) {
            passWordMessage += "旁边的";
            actorNameMessage += "旁边的";
        }

        passWordMessage += "个人信息管理员对话修改登录密码。\n1.知道啦（请按数字1键）";
        actorNameMessage += "个人信息管理员对话修改角色名。\n1.知道啦（请按数字1键）";

        if (modifyNameTimes > 0 && modifyPasswordTimes > 0) {
            byte[] bytes = stageService.getTaskBytes((short) 31017,
                    new String[] {passWordMessage, actorNameMessage});
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              GET_FILE_OK,
                                              serial,
                                              sessionId);
            seg.writeShort((short) 31017);
            seg.writeShort((short) 2);
            seg.write(bytes);
            write(seg);
        } else if (modifyNameTimes > 0) {
            byte[] bytes = stageService.getTaskBytes((short) 31016,
                    new String[] {actorNameMessage});
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              GET_FILE_OK,
                                              serial,
                                              sessionId);
            seg.writeShort((short) 31016);
            seg.writeShort((short) 2);
            seg.write(bytes);
            write(seg);
        } else {
            byte[] bytes = stageService.getTaskBytes((short) 31016,
                    new String[] {passWordMessage});
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              GET_FILE_OK,
                                              serial,
                                              sessionId);
            seg.writeShort((short) 31016);
            seg.writeShort((short) 2);
            seg.write(bytes);
            write(seg);
        }
    }

    private String getQuickRegMessage() {
        return "你的帐号名是:" + accountName + ",密码是:" + password + ",角色名是:" +
                playerName + ".请尽快进行修改";
    }

    private void actorCreate(UWAPData data) throws ITimesException {
        if (accountLogined) {
            playerService.createPlayer(this, data);
        }
    }

    private void loginOk(UWAPData data) throws Exception {
        try {
            int accountId = data.readInt();
            String name = data.readString();
            String password = data.readString();
            String phone = data.readString();
            int modifyPasswordTimes = data.readInt();
            int iMoney = data.readInt();
            boolean isMonth = data.readBoolean();
            boolean isSubscribe = data.readBoolean();
            if(!name.equalsIgnoreCase(accountName)){
                log.info("Login conflict name["+name+"] AccountName["+accountName+"] sessionId["+data.getSessionId()+","+getSessionId()+"]");
                throw new ITimesException("登陆错误,请稍后再试",data.getSerial(),data.getSessionId(),data.getAppType());
            }
            this.accountId = accountId;
            this.playerName = name;
            this.password = password;
            this.phone = phone;
            this.modifyPasswordTimes = modifyPasswordTimes;
            this.iMoney = iMoney;
            this.isMonth = isMonth;
            this.isSubscribe = isSubscribe;
            accountLogined = true;
            clientService.addAccount(accountId,this);
            reply(data);
        } catch (Exception ex) {
            log.error(ex,ex);
            throw new ITimesException("登录错误", data.getSerial(),
                                      data.getSessionId(), data.getAppType());
        }
    }

    private void regOk(UWAPData data) throws Exception {
        log.debug("send register ok");
        String phone = data.readString();
        String password = data.readString();
        boolean needReturn = data.readBoolean();

        /*leo modified 2007.5.29
                 if(needReturn)
            throw new ITimesException("您已经注册成功，密码是"+password+"。",data.getSerial(),data.getAppType());
                 else{
           UWAPSegment seg = new UWAPSegment(ClientConstants.ACCOUNT_REG_OK,data.getSerial());
           write(seg);
                 }*/

        UWAPSegment seg = new UWAPSegment(ClientConstants.ACCOUNT_REG_OK,
                                          data.getSerial());
        seg.writeString(password);
        seg.writeString("您已经注册成功，密码是" + password + "。");
        write(seg);
//            throw new ITimesException("您已经注册成功。",data.getSerial(),data.getAppType());
    }

    public void syncTime(UWAPSegment seg) {
        if (needFastSyncMode) {
            write(seg);
        } else {
            if (sync == 0) {
                write(seg);
            }
            sync = (++sync) % 10;
        }
    }

    public abstract void register(UWAPData data) throws Exception;


    public abstract void login(UWAPData data) throws Exception;


    public void playerLogin(UWAPData data) throws Exception {
//        if(!accountLogined)
//            throw new ITimesException("没有登录",data.getSerial(),data.getAppType());
//        String playerName = data.readString();
//        UWAPSegment seg = new UWAPSegment(ClientConstants.PLAYER_LOGIN,data.getSerial(),sessionId);
//        seg.writeInt(accountId);
//        seg.writeString(playerName);
//        worldSession.write(seg);

        try {
//            if(Server.isMaintance)
//                throw new ITimesException("服务器正在维护状态",data.getSerial(),data.getAppType());
            if (!accountLogined)
                throw new ITimesException("没有登录", data.getSerial(),
                                          data.getAppType());
            if (clientService.isFull())
                throw new ITimesException("已经到达最大登录数量", data.getSerial(),
                                          data.getAppType());
            playerName = data.readString();
            int sid = getSessionId();
            if (sid == 0) {
                log.info("SessionId Error address["+session.getRemoteAddress().toString()+"]");
            }
            UWAPSegment seg = new UWAPSegment(ClientConstants.PLAYER_LOGIN,
                                              data.getSerial(), getSessionId());
            seg.writeInt(accountId);
            seg.writeString(accountName);
            seg.writeString(password);
            seg.writeString(phone);
            seg.writeInt(modifyPasswordTimes);
            seg.writeString(playerName);
            seg.writeInt(version.getMaxLevel());
            seg.writeString(version.getId());
            seg.writeString(model);
            seg.writeStrings(version.getCharge());
            seg.writeString(version.getFeeplan());
            seg.writeInt(iMoney);
            seg.writeBoolean(isMonth);
            seg.writeBoolean(isSubscribe);
            worldSession.write(seg);
//            Player p = playerService.getPlayerByNameAndAccountId(
//                    playerName, accountId);
//            if(playerService.isForbid(p.getId())){
//                throw new ITimesException("角色不可用",data.getSerial(),data.getAppType());
//            }
//            log.info("playerName:"+playerName+"accountId:"+accountId);
//            if (p == null) {
//                throw new ITimesException("没有找到角色", data.getSerial(),
//                                          data.getAppType());
//            } else {
//                player = new PlayerData(p);
//                authSession.playerLoginOk(this, data);
//                UWAPSegment seg1 = new UWAPSegment(ClientConstants.
//                                      PLAYER_LOGIN_OK,
//                                      data.getSerial());
//                seg1.writeInt(player.getId());
//                seg1.writeString(player.getPlayerName());
//                seg1.writeShort(player.getMapId());
//                seg1.writeShort(player.getX());
//                seg1.writeShort(player.getY());
//                seg1.write(player.toClientBytes());
//                seg1.writeInt(getSessionId());
//                seg1.writeInt(100);
//                seg1.write(stageService.getAllAbilitiesBytes());
//                write(seg1);
//                clientService.addClient(this);
//                chatService.registry(player);
//                playerLogined = true;
//                int unReaded = mailService.getUnReadedMailCount(player.getId());
//                if(unReaded>0){
//                    UWAPSegment seg2 = new UWAPSegment(ClientConstants.MAIL_NEW);
//                    seg2.write((byte)1);
//                    write(seg2);
//                }
//                worldSession.write(seg);
//            }
        } catch (ITimesException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ITimesException("登录错误", data.getSerial(), data.getAppType());
        }
    }


//    public PlayerData getPlayerData() {
//        return player;
//    }

//    public void playerLoginOk(UWAPData data) throws ITimesException {
//        playerLogined = true;
//        UWAPSegment seg = new UWAPSegment(ClientConstants.PLAYER_LOGIN_OK);
//        seg.writeInt(playerId);
//        seg.writeInt(version.getMaxLevel());
//        worldSession.forward(data, sessionId);
//    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getAccountId() {
        return accountId;
    }


    public void dispatchToWorld(UWAPData data) {
        if (playerLogined && playerId != -1) {
            UWAPSegment seg = new UWAPSegment(data, getSessionId(),
                                              playerId);
            worldSession.write(seg);
        } else {
            UWAPSegment seg = new UWAPSegment(data, getSessionId());
            worldSession.write(seg);
        }
    }

    public void write(UWAPSegment seg) {
        if (!offline_mode ||
            (seg.getType() != ClientConstants.CHAT &&
             seg.getType() != ClientConstants.SEND_POSITION)) {
            seg.setSessionId(getSessionId());
            super.write(seg);
        }
    }


    public void notifyAuth() {
        long lastReadTime = session.getLastReadTime();
        if ((lastReadTime + 60 * 1000 * 8L) < System.currentTimeMillis()) {
            log.info("ACCOUNTID[" + accountId + "]ID[" + playerId + "]IDLE");
        }
        if (accountId != -1) {
            UWAPSegment seg = new UWAPSegment(ServerConstants.LIVE_NOTIFY);
            seg.writeInt(accountId);
            authSession.write(seg);
        } else {
            if (playerId != -1)
                log.info("ID[" + playerId + "]ERROR");
        }
    }
}
