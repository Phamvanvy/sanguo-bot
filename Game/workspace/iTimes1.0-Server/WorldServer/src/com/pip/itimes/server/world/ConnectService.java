package com.pip.itimes.server.world;

import com.pip.itimes.net.*;
import com.pip.itimes.server.stage.AutoUseData;
import com.pip.itimes.server.stage.Changed;
import org.apache.mina.common.IoAcceptor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ConnectService implements Runnable{

    private ConnectSession[] connects = new ConnectSession[10];
    private IoAcceptor acceptor;
    private AtomicInteger ids = new AtomicInteger(1);

    private StageService stageService;
    
    //jwp add
    private PlayerService playerService;
    public PlayerService getPlayerService() {
		return playerService;
	}

	public void setPlayerService(PlayerService playerService) {
		this.playerService = playerService;
	}
	
	//jwp add end
	public ConnectService() {
        new Thread(this).start();
    }

    public void setAcceptor(IoAcceptor acceptor){
        this.acceptor = acceptor;
    }

    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }
    
    public void stop(){
        acceptor.unbindAll();
    }

    public void addConnect(ConnectSession session){
        int id = ids.incrementAndGet();
        synchronized(this){
            for(int i=0;i<connects.length;i++){
                if(connects[i]==null){
                    connects[i] = session;
                    session.setId(id);
//                    connects[i].notifyMaxPlayer();
                    break;
                }
            }
        }
    }

    public void broadcast(UWAPSegment seg){
//        synchronized(connects){

            for (int i = 0; i < connects.length; i++) {
                if(connects[i]!=null)
                    connects[i].write(seg);
            }
//        }
    }

    //把消息发送给指定的player
    public void writeTo(UWAPSegment seg,int playerId){
//        synchronized(connects){
        for (int i = 0; i < connects.length; i++) {
            if(connects[i]!=null)
                connects[i].write(seg, playerId);
        }
//        }
    }

    public void removeConnect(ConnectSession session){
        synchronized (this) {
            for(int i=0;i<connects.length;i++){
                if(connects[i]==session)
                    connects[i] = null;
            }
        }
    }

    public void sendGetItem(Changed changed,int playerId,byte cause) {
    	
    	int playerDataVersion = 0;
    	WorldPlayer player = playerService.getWorldPlayer(playerId);
    	if (player == null){
    		return;
    	}else{
    		playerDataVersion = player.getClientDataVersion();
    	}              
        Object[] os = changed.toClientBytes(playerDataVersion);
        if (os.length > 0) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_ITEM);
            seg.write(cause);
            seg.write((byte) os.length);
            for (int i = 0; i < os.length; i++) {
                seg.write((byte[]) os[i]);
            }
            writeTo(seg,playerId);
        }
        
        //leo add
        Object[] autos = changed.getAutoUseData();
        
        for(int i = 0; i < autos.length; i++){
            AutoUseData atUse = (AutoUseData)autos[i];
            
            byte[] bytes = stageService.getTaskBytes((short)atUse.getTaskId(), atUse.getTaskStrings());
            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
            seg.writeShort((short)atUse.getTaskId());
            seg.writeShort((short)2);
            seg.write(bytes);
            
            writeTo(seg, playerId);
        }
        //leo add end
    }

    public void sendError(int playerId,String cause,int serial,byte appType){
        UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,
                                          serial);
        seg.write(appType);
        seg.writeString(cause);
        writeTo(seg,playerId);
    }

    public void sendMessage(int playerId, String message) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
        seg.writeString(message);
        writeTo(seg, playerId);
    }

//    private void checkLevelChangedAndSendTips(Changed changed, int serial, int sessionId) {
//        int level = changed.getProperty(Changed.LEVEL);
//        if (level != 0) {
//            String message = LevelTips.getTip(level);
//            if (message != null) {
//                byte[] bytes = stageService.getTaskBytes((short) 31019,
//                        new String[] {message});
//                UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                                  GET_FILE_OK, serial,
//                                                  sessionId);
//                seg.writeShort((short) 31019);
//                seg.writeShort((short) 2);
//                seg.write(bytes);
//                write(seg);
//            }
//        }
//    }


    public ConnectSession[] getConnectSession(){
        return connects;
    }

    //todo
    public void syncTime(int time){
//        UWAPSegment seg = new UWAPSegment(ClientConstants.SYNC_TIME);
//        seg.writeInt(time);
//        broadcast(seg);
    }



    public void sendNewMailMessage(int id) {
        UWAPSegment seg1 = new UWAPSegment(ClientConstants.
                                           MAIL_NEW);
        writeTo(seg1, id);
    }

    public void shutdown(){
        UWAPSegment seg = new UWAPSegment(ServerConstants.SHUTDOWN);
        for(int i=0;i<connects.length;i++){
            if(connects[i]!=null)
                connects[i].shutdown();
        }
    }

    public void logOnline(){
        for(int i=0;i<connects.length;i++){
            if(connects[i]!=null)
                connects[i].logOnline();
        }
    }

    public void write(String id,UWAPSegment seg){
        for(int i=0;i<connects.length;i++){
            if(connects[i]!=null&&connects[i].getName().equals(id)){
                connects[i].write(seg);
            }
        }
    }

    public void forceLogout(int accountId,String key){
        for(int i=0;i<connects.length;i++){
            if(connects[i]!=null){
                connects[i].forceLogout(accountId,key);
            }
        }
    }

    public void forceLogout(int accountId){
        for(int i=0;i<connects.length;i++){
            if(connects[i]!=null){
                connects[i].forceLogout(accountId);
            }
        }
    }

    public void kick(int playerId){
        for(int i=0;i<connects.length;i++){
            if(connects[i]!=null){
                connects[i].kick(playerId);
            }
        }
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(90 * 1000L);
            } catch (InterruptedException ex) {
            }
            long current = System.currentTimeMillis();
            for (int i = 0; i < connects.length; i++) {
                if (connects[i] != null) {
                    connects[i].notifyMaxPlayer(current);
                }
            }
        }
    }
    //mengjie add
    public void addRecommendbalanceresult(int accountId,int accountId2) {
    	for(int i=0;i<connects.length;i++){
            if(connects[i]!=null){
                connects[i].addRecommendbalanceresult(accountId,accountId2);
            }
        }
    }
    public void addPPIPRecommendbalanceresult(int accountId,int accountId2) {
    	for(int i=0;i<connects.length;i++){
            if(connects[i]!=null){
                connects[i].addRecommendbalanceresult(accountId,accountId2);
            }
        }
    }
}
