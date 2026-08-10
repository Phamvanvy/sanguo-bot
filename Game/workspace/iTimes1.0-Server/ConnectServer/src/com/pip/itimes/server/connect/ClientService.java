package com.pip.itimes.server.connect;

import java.util.*;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.Changed;
import org.apache.mina.common.IoAcceptor;
import org.apache.log4j.Logger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jeffery
 * @version 1.0
 */
public class ClientService implements Runnable{

    private static final Logger log = Logger.getLogger(ClientService.class);

    private ConcurrentHashMap id2session = new ConcurrentHashMap();
    private ConcurrentHashMap<Integer,ClientSession> account2session = new ConcurrentHashMap<Integer,ClientSession>();
//    private ConcurrentHashMap account2session = new ConcurrentHashMap();
//    private Map id2session = new HashMap();
    private IoAcceptor acceptor = null;
    private int maxPlayer;
    private Set unLogined = Collections.synchronizedSet(new HashSet());
    public int addedOnline = 0;

    public ClientService() {
        new Thread(this).start();
    }

    public  void setAcceptor(IoAcceptor acceptor){
        this.acceptor = acceptor;
    }

    public void stop(){
        acceptor.unbindAll();
    }

    public void closeSession(int id){
        ClientSession session = (ClientSession)id2session.get(new Integer(id));
        if(session!=null){
//            account2session.remove(new Integer(session.accountId));
            session.close();
        }
    }

    public int size(){
        return id2session.size();
    }

    public boolean isFull(){
        return size()>=maxPlayer;
    }

    public void setMaxPlayer(int maxPlayer){
        this.maxPlayer = maxPlayer;
    }

    public int getMaxPlayer(){
        return maxPlayer;
    }

    //添加已经建立，但是没有登录的session
    public void addSession(ClientSession session){
        unLogined.add(session);
    }

    public void addClient(int id,ClientSession session){
        unLogined.remove(session);
        id2session.put(new Integer(id), session);
    }

    public void addAccount(int accountId,ClientSession session){
        account2session.put(accountId,session);
    }

    public ClientSession getClientByAccountId(int accountId){
        return account2session.get(accountId);
    }

    public void removeClient(ClientSession session){
        unLogined.remove(session);
        if(session.playerId!=-1){
            id2session.remove(new Integer(session.playerId));
        }
        if(session.accountId!=-1){
            account2session.remove(session.accountId);
        }
    }

    public ClientSession getClient(int id){
        return (ClientSession)id2session.get(new Integer(id));
//        if(session!=null){
//            account2session.remove(new Integer(session.accountId));
//        }
    }


//    public void removeAndLogoutByAccountId(int accountId){
//        ClientSession client = (ClientSession)account2session.remove(new Integer(accountId));
//        if(client!=null){
//            id2session.remove(new Integer(client.playerId));
//            client.playerLogouted = true;
//        }
//    }

//    public void removeAndLogout(int id){
//        ClientSession client = (ClientSession)id2session.remove(new Integer(id));
//        if(client!=null)
//            client.setLogouted();
//    }

    public void broadcast(UWAPSegment seg){
        Collection c = id2session.values();
        Iterator ite = c.iterator();
        while (ite.hasNext()) {
            ClientSession session = (ClientSession) ite.next();
            session.write(seg);
        }
    }

    public void broadcastToMap(UWAPSegment seg,short mapId){
        Collection c = id2session.values();
        Iterator ite = c.iterator();
        while (ite.hasNext()) {
            ClientSession session = (ClientSession) ite.next();
            if (session.getLastMapId() == mapId) {
                session.write(seg);
            }
        }
    }

//    public void broadcastToFavorite(UWAPSegment seg, int id) {_
//        Collection c = id2session.values();
//        Iterator ite = c.iterator();
//        while (ite.hasNext()) {
//            ClientSession session = (ClientSession) ite.next();
//            PlayerData player = session.getPlayerData();
//            if (player.getChatFavoriteId() == id) {
//                session.write(seg);
//            }
//        }
//    }


    public void syncTime(int time) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.SYNC_TIME);
        seg.writeInt(time);

        Collection c = id2session.values();
        Iterator ite = c.iterator();
        while (ite.hasNext()) {
            ClientSession session = (ClientSession) ite.next();
            session.syncTime(seg);
        }
        synchronized(unLogined){
            if (unLogined.size() > 0) {
                ClientSession[] sessions = new ClientSession[unLogined.size()];
                unLogined.toArray(sessions);
                for (int i = 0; i < sessions.length; i++) {
                    sessions[i].write(seg);
                }
            }
        }
    }

    public void send(int id,UWAPSegment seg){
        ClientSession session = (ClientSession)id2session.get(new Integer(id));
        if(session!=null)
            session.write(seg);
    }

    public void sendGetItem(int id,Changed changed){
        ClientSession session = (ClientSession)id2session.get(new Integer(id));
        if(session!=null)
            session.sendGetItem(changed,0,(byte)4);
    }

    public void run(){
        while(true){
            try {
                Thread.sleep(2 * 60 * 1000L);
//                Thread.sleep(2 * 60 * 1000L);
            } catch (InterruptedException ex) {
            }
            try {
                Iterator ite = id2session.values().iterator();
                while(ite.hasNext()){
                    ClientSession session = (ClientSession)ite.next();
                    session.notifyAuth();
                }
                log.info("ONLINE["+(id2session.size()+addedOnline)+"]");
            } catch (Exception ex) {
                log.info(ex,ex);
            }
        }
    }
}
