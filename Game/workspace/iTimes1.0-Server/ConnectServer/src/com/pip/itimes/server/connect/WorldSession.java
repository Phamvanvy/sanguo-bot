package com.pip.itimes.server.connect;

import com.pip.itimes.net.*;
import com.pip.itimes.server.connect.chat.ISendMessage;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

public class WorldSession extends Session {
    private static final Logger log = Logger.getLogger(WorldSession.class);

    private boolean isLogined;
    private StageService stageService;
    private ClientService clientService;
    private SessionRegistry clientRegistry;
    private PlayerService playerService;
    private ChatService chatService;
    private TrustIpService trustIpService;
    private VersionService versionService;

    private Configuration configuration;

    public WorldSession(IoSession session){
        super(session);
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
    }

    public void setClientService(ClientService clientService){
        this.clientService = clientService;
    }

    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }

    public void setClientRegistry(SessionRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void setChatService(ChatService chatService){
        this.chatService = chatService;
    }

    public void setTrustIpService(TrustIpService trustIpService){
        this.trustIpService = trustIpService;
    }

    public void setVersionService(VersionService versionService){
        this.versionService = versionService;
    }

    private void dispatchTo(Packet packet) {
        UWAPData data = packet.datas[0];
        ClientSession session = (ClientSession) clientRegistry.getSession(data.
                getSessionId());
        if (session != null) {
            session.handleServer(packet);
        }
    }

    private void dispatchTo(UWAPData data){
        ClientSession session = (ClientSession) clientRegistry.getSession(data.
                getSessionId());
        if (session != null) {
            Packet packet = new Packet();
            packet.datas = new UWAPData[1];
            packet.datas[0] = data;
            session.handleServer(packet);
        }
    }


    private static int segNum = 0;

    public void handle(Packet packet){
        try{
            UWAPData data = packet.datas[0];
            byte type = data.getAppType();
            switch (type) {
                case ServerConstants.SERVER_LOGIN_OK:
                    loginToWorldServerOk();
                    break;
                case ServerConstants.SERVER_LOGIN_FAIL:
                    loginToWorldServerFail();
                    break;
                case ServerConstants.RESOURCE_ADD:
                    resourceAdd(data);
                    break;
                case ServerConstants.RESOURCE_DELETE:
                    resourceDelete(data);
                    break;
                case ServerConstants.RESOURCE_SYNC_OK:
                    resourceSync(data);
                    break;
                case ClientConstants.CHAT:
                    broadcastChat(data);
                    break;
                case ClientConstants.GATHER_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.GATHER_RESULT:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SEND_POSITION:
                    dispatchTo(packet);
                    break;
                case ClientConstants.BATTLE_INIT:
                    dispatchTo(packet);
                    break;
                case ClientConstants.BATTLE_JOIN_RESULT:
                    dispatchTo(packet);
                    break;
                case ClientConstants.BATTLE_ROUND_END:
                    dispatchTo(packet);
                    break;
                case ClientConstants.BATTLE_START:
                    dispatchTo(packet);
                    break;
                case ClientConstants.PK_REQUEST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.PK_CREATED:
                    dispatchTo(packet);
                    break;
                case ClientConstants.PK_REFUSE:
                    dispatchTo(packet);
                    break;
                case ClientConstants.PK_START:
                    dispatchTo(packet);
                    break;
                case ClientConstants.PK_ROUND_END:
                    dispatchTo(packet);
                    break;
                case ClientConstants.GET_ITEM:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TEAM_CREATE_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TEAM_INVIT:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TEAM_JOIN_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TEAM_JOIN_FAIL:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TEAM_INVIT_RESULT:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TEAM_LEAVE:
                    dispatchTo(packet);
                    break;
                case ClientConstants.GET_FILE:
                    dispatchTo(packet);
                    break;
                case ClientConstants.MAIL_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.MAIL_GET_ATTACHMENT:
                    dispatchTo(packet);
                    break;
                case ClientConstants.PLAYRE_UPLOAD_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.BATTLE_RESULT:
                    dispatchTo(packet);
                    break;
                case ClientConstants.ABILITY_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SKILL_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.LEAR_ABILITY_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.LEAR_SKILL_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.EQU_CHANGED_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.ADD_PROPERTY_POINT_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.CHAT_OPTION:
                    dispatchTo(packet);
                    break;
                case ClientConstants.CHANGE_CHATFAVORITE:
                    dispatchTo(packet);
                    break;
                case ClientConstants.LOOK_EQU_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SHOP_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.AUCTION_TYPE_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.AUCTION_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.AUCTION_DESC:
                    dispatchTo(packet);
                    break;
                case ClientConstants.AUCTION_PRICE_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.AUCTION_ITEM_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SELL_MATERIAL_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SHOP_MONEY_CHANGE_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SHOP_REMOVE_ITEM_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SHOP_ADD_ITEM_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.OEM_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SHOP_CREATE_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SHOP_ITEM_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.ERROR:
                    dispatchTo(packet);
                    break;
                case ClientConstants.STORE_ITEM_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.STORE_TRADE_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.OEM_TYPE_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.BUY_MAERIAL_TYPE_LIST:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TASK_COMPLETED:
                    dispatchTo(packet);
                    break;
                case ClientConstants.SHOP_CHANGE_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.ADD_FRIEND_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.MAIL_POST_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.MAIL_NEW:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TASK_DESC:
                    dispatchTo(packet);
                    break;
                case ClientConstants.ADD_POINT_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TONG_CREATE_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TONG_MEMBERS:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TONG_GRANT_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.MESSAGE:
                    dispatchTo(packet);
                    break;
                case ClientConstants.FRIENDS_STATUS:
                    dispatchTo(packet);
                    break;
                case ClientConstants.TASK_ABANDON_RESULT:
                    dispatchTo(packet);
                    break;
                case ClientConstants.ADD_PET_POINT_OK:
                    dispatchTo(data);
                    break;
                case ClientConstants.BUY_PET_POINT_OK:
                    dispatchTo(data);
                    break;
                case ClientConstants.USE_PET_OK:
                    dispatchTo(data);
                    break;
//                case ServerConstants.CHAT:
//                    adminChat(data);
//                    break;
                case ServerConstants.RELOAD:
                    reload(data);
                    break;
                case ServerConstants.KICK:
                    kick(data);
                    break;
                case ServerConstants.SHUTDOWN:
                    shutdown(data);
                    break;
                case ClientConstants.SYNC_TIME:
                    syncTime(data);
                    break;
                case ServerConstants.FINITERELOAD:
                    finiteReload(data);
                    break;
                case ClientConstants.BATTLE_ABORT:
                    dispatchTo(data);
                    break;
                case ClientConstants.PLAYER_LOGIN_OK:
                    dispatchTo(data);
                    break;
                case ServerConstants.SYNC_CHAT:
                    syncChat(data);
                    break;
                case ClientConstants.OEM_LIST:
                    dispatchTo(data);
                    break;
                case ClientConstants.RELOGIN_RESULT:
                    dispatchTo(data);
                    break;
                case ClientConstants.REFRESH:
                    dispatchTo(data);
                    break;
                case ClientConstants.DESC:
                    dispatchTo(data);
                    break;
                case ClientConstants.REPAIRE_LIST:
                    dispatchTo(data);
                    break;
                case ClientConstants.REPAIRE_OK:
                    dispatchTo(data);
                    break;
                case ClientConstants.CHANGE_OPTION_OK:
                    dispatchTo(data);
                    break;
                case ServerConstants.ADMIN_ADDIP:
                    addIp(data);
                    break;
                case ServerConstants.MAXPLAYER:
                    maxplayer(data);
                    break;
                case ClientConstants.DELETE_USER_OK:
                    dispatchTo(data);
                    break;
                case ClientConstants.SEG_402_RESULT:
                    dispatchTo(data);
                    break;
                case ClientConstants.QUICK_REG:
                    dispatchTo(data);
                    break;
                case ServerConstants.MAINTANCE:
                    maintance(data);
                    break;
                case ClientConstants.GENERIC_LIST:
                    dispatchTo(data);
                    break;
                case ClientConstants.GENERIC_LIST_CONTENT:
                    dispatchTo(data);
                    break;
                case ClientConstants.SNEAK_ATTACK:
                    dispatchTo(data);
                    break;
                case ClientConstants.ISHOP_LIST:
                    dispatchTo(data);
                    break;
                case ClientConstants.ISHOP_TRADE_OK:
                    dispatchTo(data);
                    break;
                case ClientConstants.FACE_LIST:
                    dispatchTo(data);
                    break;
                case ClientConstants.MAIL_CONTENT:
                    dispatchTo(data);
                    break;
                case ClientConstants.BBS_CONTENT:
                    dispatchTo(data);
                    break;
                case ClientConstants.BBS_POST_OK:
                    dispatchTo(data);
                    break;
                case ClientConstants.BBS_LIST:
                    dispatchTo(data);
                    break;
                case ServerConstants.ADD_ONLINE:
                    addOnline(data);
                    break;
            }
        }catch(Exception ex){
            log.error(ex,ex);
        }
    }

    private void addOnline(UWAPData data) throws Exception{
        clientService.addedOnline = data.readInt();
    }

    private void maintance(UWAPData data) throws Exception{
        boolean value = data.readBoolean();
        Server.isMaintance = value;
    }

    private void maxplayer(UWAPData data) throws Exception{
        int count = data.readInt();
        clientService.setMaxPlayer(count);
    }

    private void syncTime(UWAPData data) throws Exception{
        int time = data.readInt();
        clientService.syncTime(time);
    }

    private void shutdown(UWAPData data) throws Exception{
        close();
        clientService.stop();
    }

    private void addIp(UWAPData data) throws Exception{
        int begin = data.readInt();
        int end = data.readInt();
        trustIpService.addTrustIp(begin,end);
    }

    private void kick(UWAPData data) throws Exception{
        int id = data.readInt();
        int time = data.readInt();
        clientService.closeSession(id);
        if(time>0)
            playerService.forbid(id,time);
    }

    private void reload(UWAPData data) throws Exception{
        byte type = data.readByte();
        if(type==0){
            stageService.reload();
        }else{
            stageService.loadTasks();
        }
    }

    private void finiteReload(UWAPData data) throws Exception{
        byte type = data.readByte();
        if(type==1){
            trustIpService.reload();
        }
        else if(type==2){
            versionService.reload();
        }
    }

    private void syncChat(UWAPData data) throws Exception{
        int playerId = data.readInt();
        short mapId = data.readShort();
        int favorite = data.readInt();
        boolean inWorldChannel = data.readBoolean();
        boolean inMapChannel = data.readBoolean();
        boolean inFavoriteChannel = data.readBoolean();
        chatService.registry(playerId,mapId,favorite,inWorldChannel,inMapChannel,inFavoriteChannel);
    }


//    private void position(UWAPData data){
//        try {
//            int playerId = data.readInt();
//            String name = data.readString();
//            byte sex = data.readByte();
//            short level = data.readShort();
//            short mapId = data.readShort();
//            short x = data.readShort();
//            short y = data.readShort();
//            byte teamState = data.readByte();
//            String tongName = data.readString();
//            byte returnTimes = data.readByte();
//            int[] ids = data.readInts();
//            UWAPSegment seg = new UWAPSegment(ClientConstants.SEND_POSITION);
//            seg.writeInt(playerId);
//            seg.writeString(name);
//            seg.write(sex);
//            seg.writeShort(level);
//            seg.writeShort(mapId);
//            seg.writeShort(x);
//            seg.writeShort(y);
//            seg.write(teamState);
//            seg.writeString(tongName);
//            seg.write(returnTimes);
//            for(int i=0;i<ids.length;i++){
//                clientService.send(ids[i],seg);
//            }
//        } catch (IllegalAccessException ex) {
//        }
//    }

    private void broadcastChat(UWAPData data){
        try {
            int srcId = data.readInt();
            String srcName = data.readString();
            int cn = data.readInt();
            int value = data.readInt();
            String message = data.readString();
            if(cn==ISendMessage.SYSTEM){
                UWAPSegment seg = new UWAPSegment(ClientConstants.CHAT,-1);
                seg.writeInt(srcId);
                seg.writeString(srcName);
                seg.writeInt(ISendMessage.SYSTEM);
                seg.writeString(message);
                clientService.broadcast(seg);
            }
            ISendMessage sendMessage = chatService.getSendMessage(srcId,srcName,cn,value,message);
            if(sendMessage!=null){
                int ids[] = sendMessage.getDestIds();
                UWAPSegment seg = new UWAPSegment(ClientConstants.CHAT,-1);
                seg.writeInt(srcId);
                seg.writeString(srcName);
                seg.writeInt(cn);
                seg.writeString(message);
                for (int i = 0; i < ids.length; i++) {
                    clientService.send(ids[i], seg);
                }
            }
        } catch (Exception ex) {
            log.error(ex,ex);
        }
    }

//    private void adminChat(UWAPData data) throws Exception{
//        int srcId = data.readInt();
//        String srcName = data.readString();
//        int destId = data.readInt();
//        int value = data.readInt();
//        String message = data.readString();
//        UWAPSegment seg = new UWAPSegment(ClientConstants.CHAT);
//        seg.writeInt(srcId);
//        seg.writeString(srcName);
//        seg.writeInt(destId);
//        seg.writeString(message);
//        if(srcId>0){
//            clientService.send(destId,seg);
//        }
//        else if(destId==-1){ //世界
//            clientService.broadcast(seg);
//        }
//        else if(destId==-2){ //场景
//            clientService.broadcastToMap(seg,(short)value);
//        }
//        else if(destId==-6){ //圈
//            clientService.broadcastToFavorite(seg,value);
//        }
//        else if(destId==-7){ //系统
//            clientService.broadcast(seg);
//        }
//        else if(destId>0){
//            clientService.send(destId,seg);
//        }
//    }

    private void resourceAdd(UWAPData data){
        try {
            int id = data.readInt();
            stageService.refreshAdd(id);
        } catch (Exception ex) {
            log.error(ex,ex);
        }
    }

    private void resourceDelete(UWAPData data){
        try {
            int id = data.readInt();
            stageService.refreshDelete(id);
        } catch (Exception ex) {
            log.error(ex,ex);
        }
    }

    private void resourceSync(UWAPData data){
        try {
            int[] ids = data.readInts();
            stageService.syncRefresh(ids);
        } catch (Exception ex) {
            log.error(ex,ex);
        }
    }

    public void idle(IdleStatus status){

    }

    public void created() {

    }

    public void opened() {
        loginToWorldServer();
    }

    public void closed() {

    }

    private void loginToWorldServerFail() {

    }

    private void loginToWorldServerOk() {
        isLogined = true;
        UWAPSegment seg = new UWAPSegment(ServerConstants.RESOURCE_SYNC);
        write(seg);
    }

    private void loginToWorldServer() {
        UWAPSegment seg = new UWAPSegment(ServerConstants.SERVER_LOGIN);
        seg.writeString((String) configuration.getProperty(ServerConstants.SERVERID));
        seg.writeString((String) configuration.getProperty(ServerConstants.
                SERVERPASSWORD));
        write(seg);
    }
}
