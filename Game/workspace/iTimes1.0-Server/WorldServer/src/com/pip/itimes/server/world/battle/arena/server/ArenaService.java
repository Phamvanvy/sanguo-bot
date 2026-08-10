package com.pip.itimes.server.world.battle.arena.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoAcceptor;

import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.world.StageService;
import com.pip.itimes.server.world.battle.arena.ArenaWorldListManager;

public class ArenaService implements Runnable{
    private static final Logger log = Logger.getLogger(ArenaService.class);

    private ConcurrentHashMap<Integer, ArenaServerSession> worldConnects = new ConcurrentHashMap<Integer, ArenaServerSession>();
    private AtomicInteger ids = new AtomicInteger(1);
    private IoAcceptor acceptor;
    public ArenaWorldListManager arenaWorldListManager;

    public ArenaService(){
        new Thread(this).start();
    }
    
    public void setArenaWorldListManager(ArenaWorldListManager arenaWorldListManager){
        this.arenaWorldListManager = arenaWorldListManager;
    }

    public void setAcceptor(IoAcceptor acceptor){
        this.acceptor = acceptor;
    }

    public void stop(){
        acceptor.unbindAll();
    }

    public synchronized void addWorld(ArenaServerSession session){
        int id = ids.incrementAndGet();
        session.setId(id);
        worldConnects.put(id, session);
    }

    public synchronized void removeWorld(ArenaServerSession session){
        worldConnects.remove(session.getId());
    }
    
    public void writeTo(int serverId, UWAPSegment seg){
        ArenaServerSession session = worldConnects.get(serverId);
        
        if(session != null){
            session.write(seg);
        }
    }
    
    public String getServerName(int serverId){
        ArenaServerSession session = worldConnects.get(serverId);
        
        if(session != null){
            return session.getName();
        }
        
        return "";
    }
    
    public synchronized int getServerId(String serverId){
    	Iterator<Integer> it = worldConnects.keySet().iterator();
        while(it.hasNext()){
            ArenaServerSession world = worldConnects.get(it.next());
            if(world.getName().equals(serverId)){
            	return world.getId();
            }
        }
        return -1;
    }
    
    /**
     * 获取所有已经连接的服务器列表 格式:服务器ID 服务器名称
     * @return
     */
    public synchronized String[] getServers(int exceptid){
    	ArrayList<String> servers = new ArrayList<String>();
    	Iterator<Integer> it = worldConnects.keySet().iterator();
        while(it.hasNext()){
            ArenaServerSession world = worldConnects.get(it.next());
            if(world.getId() != exceptid){
            	servers.add("" + world.getId());
            	servers.add(world.getName());
            }
        }
        String[] str = new String[servers.size()];
        servers.toArray(str);
        return str;
    }
    
    public ConcurrentHashMap<Integer, ArenaServerSession> getWorldConnects(){
    	return worldConnects;
    }

    public void run(){
        for(;;){
            try{
                Iterator<Integer> it = worldConnects.keySet().iterator();

                while(it.hasNext()){
                    ArenaServerSession world = worldConnects.get(it.next());
                }

                Thread.sleep(5 * 1000L);
            }catch(Throwable e){
                log.info(e, e);
            }
        }
    }
}
