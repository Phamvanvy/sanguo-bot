package com.pip.itimes.server.world.battle.arena.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.ArenaTeamTotal;
import com.pip.itimes.server.bean.ArenaTeamTotalWorldWar;
import com.pip.itimes.server.dao.ArenaRecordDao;
import com.pip.itimes.server.dao.ArenaTeamTotalDao;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.world.battle.arena.ArenaBattleStrategy;
import com.pip.itimes.server.world.battle.arena.ArenaConstants;
import com.pip.itimes.server.world.lyricsSystem.LyricData;
import com.pip.itimes.server.world.lyricsSystem.LyricDataServer;
import com.pip.itimes.server.world.lyricsSystem.LyricsSystemConfig;

public class ArenaServerService implements Runnable{
    private static final Logger log = Logger.getLogger(ArenaServerService.class);

    //(serverId << 32 | ownerId), ArenaQueueServer
    private ConcurrentHashMap<Long, ArenaQueueServer> serverOwner2queues = new ConcurrentHashMap<Long, ArenaQueueServer>();
    //ArenaOneQueue, ArenaOneQueu
    private ArrayList<ArenaQueueServer> queueList = new ArrayList<ArenaQueueServer>();
    //battleId ArenaBattleOneServer
    private ConcurrentHashMap<Integer, ArenaBattleServer> id2battles = new ConcurrentHashMap<Integer, ArenaBattleServer>();
    //NetWinLog NetWinLog
    private ConcurrentHashMap<Long, NetWinLog> netWinLog = new ConcurrentHashMap<Long, NetWinLog>();
    
    private AtomicInteger ids = new AtomicInteger(1);
    private ArenaService arenaService;
    private ArenaRecordDao dao;
    private ArenaTeamTotalDao teamdao;
    private long lastlogtime=0;
    private int averageWaitRound1;
    private int minWaitRound1;
    private int maxWaitRound1;
    private int averageWaitRound2;
    private int minWaitRound2;
    private int maxWaitRound2;
    private int averageWaitRound3;
    private int minWaitRound3;
    private int maxWaitRound3;
    
    private int arenaLevelGap; //队列中战队等级差
    
    //点歌列表
    private ConcurrentHashMap<Integer, LyricDataServer> serverLyrics = new ConcurrentHashMap<Integer, LyricDataServer>(); 
    private AtomicInteger lyricIds = new AtomicInteger(1);
    
    public ArenaServerService(ArenaRecordDao dao,ArenaTeamTotalDao teamdao){
    	this.teamdao = teamdao;
        this.dao = dao;
        new Thread(this).start();
    }

    public ArenaService getArenaService(){
        return arenaService;
    }

    public void setArenaService(ArenaService arenaService){
        this.arenaService = arenaService;
    }
    
    public void removeServer(int serverId){
        synchronized(serverOwner2queues){
            List<Integer> ownerList = new Vector<Integer>();
            Iterator<Long> it = serverOwner2queues.keySet().iterator();
            
            while(it.hasNext()){
                Long key = it.next();
                
                if(ArenaQueueServer.getServerIdFromKey(key) == serverId){
                    ownerList.add(ArenaQueueServer.getOwnerIdFromKey(key));
                }
            }
            
            for(Integer ownerId : ownerList){
                removeQueue(serverId, ownerId, false);
            }
        }
    }

    public int addQueue(int type, int serverId, int ownerId, String ownerName, int queuePlayerId, int[] playerId, int[] playerLevel, int[] playerArenaLevel, int arenaLevel,String[] playername,String arenaName, String serverName,int arenaId){
        synchronized(serverOwner2queues){
            Long key = ArenaQueueServer.getQueueKey(serverId, ownerId);
            
            if(serverOwner2queues.containsKey(key)){
                ArenaQueueServer queue = serverOwner2queues.get(key);
                int[] playerIds = queue.getPlayerId();
                boolean found = false;
                
                for(int i = 0; i < playerIds.length; i++){
                    if(queuePlayerId == playerIds[i]){
                        found = true;
                        
                        break;
                    }
                }
                
                if(found){
                    return ArenaConstants.ARENA_QUEUE_DUPLICATE;
                }else{
                    return ArenaConstants.ARENA_QUEUE_OTHER;
                }
            }else{
                ArenaQueueServer queue = null;
                
                switch(type){
                    case ArenaConstants.ARENA_TYPE_ONE:
                        queue = new ArenaQueueServer(ArenaConstants.ARENA_TYPE_ONE);
                        break;
                    case ArenaConstants.ARENA_TYPE_TWO:
                        queue = new ArenaQueueServer(ArenaConstants.ARENA_TYPE_TWO);
                        break;
                    case ArenaConstants.ARENA_TYPE_THREE:
                        queue = new ArenaQueueServer(ArenaConstants.ARENA_TYPE_THREE);
                        break;
                }
                
                queue.setServerId(serverId);
                queue.setOwnerId(ownerId);
                queue.setOwnerName(ownerName);
                queue.setPlayerId(playerId);
                queue.setPlayername(playername);
                queue.setPlayerLevel(playerLevel);
                queue.setPlayerArenaLevel(playerArenaLevel);
                queue.setArenaName(arenaName);
                queue.setArenaLevel(arenaLevel);
                queue.setServerName(serverName);
                
                NetWinLog netWin = netWinLog.get(key);
                
                if(netWin == null){
                    netWin = new NetWinLog();
                    netWin.serverId = serverId;
                    netWin.ownerId = ownerId;
                    netWin.netWin = 0;
                    netWinLog.put(key, netWin);
                }
                
                queue.setNetWin(netWin.netWin);
                
                //worldwar add mengjie 
				try {
					ArenaTeamTotalWorldWar worldArenaTeam;
					worldArenaTeam = teamdao.getWorldArenaTeam(arenaId, serverName);
	            	if (worldArenaTeam == null){
	            		queue.setArenaLevel_worldwar(1000);
	            	}else{
	            		queue.setArenaLevel_worldwar(worldArenaTeam.getArenalevel());
	            	}
				} catch (DataAccessException e) {
					queue.setArenaLevel_worldwar(1000);
					System.out.println(e);
				}
                serverOwner2queues.put(key, queue);
                queueList.add(queue);

                return ArenaConstants.ARENA_QUEUE_SUCCESSFUL;
            }
        }
    }

    public void removeQueue(int serverId, int ownerId, boolean sendWorld){
        synchronized(serverOwner2queues){
            Long key = ArenaQueueServer.getQueueKey(serverId, ownerId);
            ArenaQueueServer queue = serverOwner2queues.get(key);

            if(queue != null){
                if(!id2battles.containsKey(queue.getBattleId())){
                    queueList.remove(queue);
                    serverOwner2queues.remove(key);
                    
                    NetWinLog netWin = netWinLog.get(key);
                
                    if(netWin == null){
                        netWin = new NetWinLog();
                        netWin.serverId = serverId;
                        netWin.ownerId = ownerId;
                        netWin.netWin = 0;
                        netWinLog.put(key, netWin);
                    }else{
                        netWin.netWin = queue.getNetWin();
                    }
                    
                    if(sendWorld){
                        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_REMOVE_QUEUE_TIMEOUNT);
                        seg.writeInt(ownerId);
                        arenaService.writeTo(serverId, seg);
                    }
                }
            }
        }
    }

    public void resetQueue(int serverId, int ownerId){
        ArenaQueueServer queue = getArenaQueue(serverId, ownerId);

        if(queue != null){
            queue.setState(ArenaQueueServer.STATE_WAIT);
            queue.setOpponent(null);
            queue.clearSyncData();
        }
    }

    public List<ArenaQueueServer> makePair(){
        synchronized(serverOwner2queues){
            averageWaitRound1 = 0;
            maxWaitRound1 = Integer.MIN_VALUE;
            minWaitRound1 = Integer.MAX_VALUE;
            averageWaitRound2 = 0;
            maxWaitRound2 = Integer.MIN_VALUE;
            minWaitRound2 = Integer.MAX_VALUE;
            averageWaitRound3 = 0;
            maxWaitRound3 = Integer.MIN_VALUE;
            minWaitRound3 = Integer.MAX_VALUE;
            int waitCount1 = 0;
            int waitCount2 = 0;
            int waitCount3 = 0;
            
            List<ArenaQueueServer> pairList = new Vector<ArenaQueueServer>();
            ArenaQueueServer p11 = null;
            ArenaQueueServer p12 = null;
            ArenaQueueServer p21 = null;
            ArenaQueueServer p22 = null;
            ArenaQueueServer p31 = null;
            ArenaQueueServer p32 = null;
            
            ArenaQueueServer[] sortedList = new ArenaQueueServer[queueList.size()];
            queueList.toArray(sortedList);
            Arrays.sort(sortedList);
            
            int lowArenaLevel = Integer.MAX_VALUE;
            int highArenaLevel = Integer.MIN_VALUE;
            
            for(ArenaQueueServer queue : sortedList){
                if(queue.getArenaLevel() < lowArenaLevel){
                    lowArenaLevel = queue.getArenaLevel();
                }
                
                if(queue.getArenaLevel() > highArenaLevel){
                    highArenaLevel = queue.getArenaLevel();
                }
                
                if(queue.getWaitRound() > 8000){ //4000秒后还在队列中则强制移除
                    removeQueue(queue.getServerId(), queue.getOwnerId(), true);
                    
                    continue;
                }
                
                if(queue.getState() == ArenaQueueServer.STATE_WAIT){
                    queue.addWaitRound();
                    
                    int waitRound = queue.getWaitRound();
                    
                    if(queue.getWaitRound() > 720){ //一小时没匹配成功则超时移除
                        removeQueue(queue.getServerId(), queue.getOwnerId(), true);
                        
                        continue;
                    }
                    
                    switch(queue.getType()){
                        case ArenaConstants.ARENA_TYPE_ONE:{
                            waitCount1++;
                            averageWaitRound1 += waitRound;
                            
                            if(maxWaitRound1 < waitRound){
                                maxWaitRound1 = waitRound;
                            }
                            
                            if(minWaitRound1 > waitRound){
                                minWaitRound1 = waitRound;
                            }
                            
                            if(p11 == null){
                                p11 = queue;
                            }else if(p12 == null){
                                p12 = queue;
                            }
        
                            if(p11 != null && p12 != null){
                                
                                if(p11.canPair(p12)){
                                    p11.setState(ArenaQueueServer.STATE_SYNC);
                                    p11.setOpponent(p12);
                                    p12.setState(ArenaQueueServer.STATE_SYNC);
                                    p12.setOpponent(p11);
                                    pairList.add(p11);
                                    p11 = null;
                                    p12 = null;
                                }else{
                                    p11.addWaitRound();
                                    p11 = null;
                                }
                            }
                        }
                            break;
                        case ArenaConstants.ARENA_TYPE_TWO:{
                            waitCount2++;
                            averageWaitRound2 += waitRound;
                            
                            if(maxWaitRound2 < waitRound){
                                maxWaitRound2 = waitRound;
                            }
                            
                            if(minWaitRound2 > waitRound){
                                minWaitRound2 = waitRound;
                            }
                            
                            if(p21 == null){
                                p21 = queue;
                            }else if(p22 == null){
                                p22 = queue;
                            }
        
                            if(p21 != null && p22 != null){
                                
                                if(p21.canPair(p22)){
                                    p21.setState(ArenaQueueServer.STATE_SYNC);
                                    p21.setOpponent(p22);
                                    p22.setState(ArenaQueueServer.STATE_SYNC);
                                    p22.setOpponent(p21);
                                    pairList.add(p21);
                                    p21 = null;
                                    p22 = null;
                                }else{
                                    p21.addWaitRound();
                                    p21 = null;
                                }
                            }
                        }
                            break;
                        case ArenaConstants.ARENA_TYPE_THREE:{
                            waitCount3++;
                            averageWaitRound3 += waitRound;
                            
                            if(maxWaitRound3 < waitRound){
                                maxWaitRound3 = waitRound;
                            }
                            
                            if(minWaitRound3 > waitRound){
                                minWaitRound3 = waitRound;
                            }
                            
                            if(p31 == null){
                                p31 = queue;
                            }else if(p32 == null){
                                p32 = queue;
                            }
        
                            if(p31 != null && p32 != null){
                                
                                if(p31.canPair(p32)){
                                    p31.setState(ArenaQueueServer.STATE_SYNC);
                                    p31.setOpponent(p32);
                                    p32.setState(ArenaQueueServer.STATE_SYNC);
                                    p32.setOpponent(p31);
                                    pairList.add(p31);
                                    p31 = null;
                                    p32 = null;
                                }else{
                                    p31.addWaitRound();
                                    p31 = null;
                                }
                            }
                        }
                            break;
                    }
                }
            }
            
            averageWaitRound1 /= (waitCount1 == 0? 1: waitCount1);
            
            if(maxWaitRound1 < 0){
                maxWaitRound1 = 0;
            }
            
            if(minWaitRound1 > maxWaitRound1){
                minWaitRound1 = 0;
            }
            
            averageWaitRound2 /= (waitCount2 == 0? 1: waitCount2);
            
            if(maxWaitRound2 < 0){
                maxWaitRound2 = 0;
            }
            
            if(minWaitRound2 > maxWaitRound2){
                minWaitRound2 = 0;
            }
            
            averageWaitRound3 /= (waitCount3 == 0? 1: waitCount3);
            
            if(maxWaitRound3 < 0){
                maxWaitRound3 = 0;
            }
            
            if(minWaitRound3 > maxWaitRound3){
                minWaitRound3 = 0;
            }
            
            arenaLevelGap = highArenaLevel - lowArenaLevel;
            
            if(arenaLevelGap > 2000){
                arenaLevelGap = 2000;
            }

            return pairList;
        }
    }

    public ArenaQueueServer getArenaQueue(int serverId, int ownerId){
        synchronized(serverOwner2queues){
            return serverOwner2queues.get(ArenaQueueServer.getQueueKey(serverId, ownerId));
        }
    }

    public ArenaBattleServer getBattle(int battleId){
        return id2battles.get(battleId);
    }

    public void removeBattle(ArenaBattleServer battle){
        id2battles.remove(battle.getId());
        
        removeQueue(battle.serverId1, battle.ownerId1, false);
        removeQueue(battle.serverId2, battle.ownerId2, false);

        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_REMOVE_BATTLE);
        seg.writeInt(battle.type);
        seg.writeInt(battle.ownerId1);
        seg.writeString(battle.ownerName1);
        seg.writeString(battle.side1arenaname);
        seg.writeInts(battle.side1playerId);
        //mengjie add
        seg.writeInt(battle.side2arenalevel);
        seg.writeInts(battle.side2playerarenalevel);
        seg.writeString(battle.side2servernamepush);
        seg.writeStrings(battle.side2playername);
        seg.writeString(battle.side2arenaname);
        seg.writeBoolean(battle.isWinner(battle.side1));
        arenaService.writeTo(battle.serverId1, seg);

        seg = new UWAPSegment(ArenaConstants.CONN_ARENA_REMOVE_BATTLE);
        seg.writeInt(battle.type);
        seg.writeInt(battle.ownerId2);
        seg.writeString(battle.ownerName2);
        seg.writeString(battle.side2arenaname);
        seg.writeInts(battle.side2playerId);
        //mengjie add
        seg.writeInt(battle.side1arenalevel);
        seg.writeInts(battle.side1playerarenalevel);
        seg.writeString(battle.side1servernamepush);
        seg.writeStrings(battle.side1playername);
        seg.writeString(battle.side1arenaname);
        seg.writeBoolean(battle.isWinner(battle.side2));
        arenaService.writeTo(battle.serverId2, seg);
    }

    public void addBattle(ArenaQueueServer queue){
        ArenaBattleServer battle = new ArenaBattleServer(queue.getType(), ids.getAndIncrement(), this, new ArenaBattleStrategy(), dao, teamdao);
        battle.setSide1(queue.getSprite(), queue.getPet(), queue.getServerId(), queue.getOwnerId(), queue.getOwnerName(), queue.getArenaLevel(), queue.getPlayerArenaLevel(), queue.getServerName(),
                        queue.getArenaName(), queue.getPlayerId(), queue.getPlayername(),queue.getArenaLevel_worldwar());
        battle.setSide2(queue.getOpponent().getSprite(), queue.getOpponent().getPet(), queue.getOpponent().getServerId(), queue.getOpponent().getOwnerId(), queue.getOpponent().getOwnerName(), queue
                        .getOpponent().getArenaLevel(), queue.getOpponent().getPlayerArenaLevel(), queue.getOpponent().getServerName(), queue.getOpponent().getArenaName(), queue.getOpponent()
                        .getPlayerId(), queue.getOpponent().getPlayername(),queue.getOpponent().getArenaLevel_worldwar());
        battle.writeDB = 0;
        queue.setBattleId(battle.getId());
        queue.getOpponent().setBattleId(battle.getId());
        id2battles.put(battle.getId(), battle);
        battle.start();
    }
    
    public void logNetWin(int serverId, int ownerId, boolean win){
        ArenaQueueServer queue = getArenaQueue(serverId, ownerId);
        
        if(queue != null){
            queue.setNetWin(queue.getNetWin() + (win? 1: -1));
        }
    }
    
    public int getNetWinWeighting(int netWin){
        if(netWin < 0){
            netWin = 0;
        }
        
        if(netWin > 10){
            netWin = 10;
        }
        
        switch(netWin){
            case 0:
            case 1:
            case 2:
            case 3:
                return arenaLevelGap / 10;
            case 4:
            case 5:
            case 6:
                return arenaLevelGap / 8;
            case 7:
            case 8:
            case 9:
                return arenaLevelGap / 5;
            case 10:
                return arenaLevelGap;
        }
        
        return 0;
    }

    public void run(){
        for(;;){
            try{
                //每5秒进行一次整体配对
                Thread.sleep(5 * 1000L);
                
                Iterator<LyricDataServer> iter = serverLyrics.values().iterator();
                while(iter.hasNext()){
                	LyricDataServer lds = iter.next();
                	if(lds != null){
                		if(sendLyric(lds)){
                			iter.remove();
                		}
                	}
                }

                List<ArenaQueueServer> pairList = makePair();

                for(ArenaQueueServer q : pairList){
                    switch(q.getType()){
                        case ArenaConstants.ARENA_TYPE_ONE:{
                            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SYNC_PLAYER);
                            seg.writeInt(ArenaConstants.ARENA_TYPE_ONE);
                            seg.writeInt(q.getOwnerId());
                            seg.writeInts(q.getPlayerId());
                            arenaService.writeTo(q.getServerId(), seg);
        
                            seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SYNC_PLAYER);
                            seg.writeInt(ArenaConstants.ARENA_TYPE_ONE);
                            seg.writeInt(q.getOpponent().getOwnerId());
                            seg.writeInts(q.getOpponent().getPlayerId());
                            arenaService.writeTo(q.getOpponent().getServerId(), seg);           
                        }
                            break;
                        case ArenaConstants.ARENA_TYPE_TWO:{
                            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SYNC_PLAYER);
                            seg.writeInt(ArenaConstants.ARENA_TYPE_TWO);
                            seg.writeInt(q.getOwnerId());
                            seg.writeInts(q.getPlayerId());
                            arenaService.writeTo(q.getServerId(), seg);
        
                            seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SYNC_PLAYER);
                            seg.writeInt(ArenaConstants.ARENA_TYPE_TWO);
                            seg.writeInt(q.getOpponent().getOwnerId());
                            seg.writeInts(q.getOpponent().getPlayerId());
                            arenaService.writeTo(q.getOpponent().getServerId(), seg);           
                        }
                            break;
                        case ArenaConstants.ARENA_TYPE_THREE:{
                            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SYNC_PLAYER);
                            seg.writeInt(ArenaConstants.ARENA_TYPE_THREE);
                            seg.writeInt(q.getOwnerId());
                            seg.writeInts(q.getPlayerId());
                            arenaService.writeTo(q.getServerId(), seg);
        
                            seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SYNC_PLAYER);
                            seg.writeInt(ArenaConstants.ARENA_TYPE_THREE);
                            seg.writeInt(q.getOpponent().getOwnerId());
                            seg.writeInts(q.getOpponent().getPlayerId());
                            arenaService.writeTo(q.getOpponent().getServerId(), seg);           
                        }
                            break;
                    }
                }

                long now = System.currentTimeMillis();

                for(ArenaBattleServer battle : id2battles.values()){
                    battle.doTime(now);
                }
                
                if (System.currentTimeMillis() - lastlogtime > 60000L){
                	lastlogtime = System.currentTimeMillis();
                	log.info("serverPlayer2queues count [" + serverOwner2queues.size() + "]id2battles count [" + 
                			id2battles.size() + "]queueList count["+ queueList.size() +"]");
                	log.info("1v1 Average Wait Round [" + averageWaitRound1 + "] Max Wait Round [" + 
                            maxWaitRound1 + "] Min Wait Round ["+ minWaitRound1 +"]");
                	log.info("2v2 Average Wait Round [" + averageWaitRound2 + "] Max Wait Round [" + 
                            maxWaitRound2 + "] Min Wait Round ["+ minWaitRound2 +"]");
                	log.info("3v3 Average Wait Round [" + averageWaitRound3 + "] Max Wait Round [" + 
                            maxWaitRound3 + "] Min Wait Round ["+ minWaitRound3 +"]");
                }
            }catch(Exception e){
                log.error(e, e);
            }
        }
    }
    
    private class NetWinLog{
        public int serverId;
        public int ownerId;
        public int netWin;
    }
    
    public ArenaTeamTotal[] getArenaLevelTop10(int type,int limit) throws Exception{
    	ArenaTeamTotal[] arenaTeamTotal = teamdao.getArenaTeamLevelOrder(limit, type);
    	
    	if (arenaTeamTotal == null){
    		return null;
    	}else{
    		return arenaTeamTotal;
    	}
    }
    
    public ArenaTeamTotalWorldWar[] getArenaLevelTop10WorldWar(int type,int limit) throws Exception{
    	ArenaTeamTotalWorldWar[] arenaTeamTotalWorldWar = teamdao.getArenaTeamLevelOrderWorldWar(limit, type);
    	
    	if (arenaTeamTotalWorldWar == null){
    		return null;
    	}else{
    		return arenaTeamTotalWorldWar;
    	}
    }
    
    public void addLyric(LyricDataServer lds){
    	serverLyrics.put(lyricIds.incrementAndGet(), lds);
    }
    
    
//    本服点歌：“点歌玩家名”为“被点玩家名”点了一首“歌手名”的“歌名”，并对他说：“玩家输入的话”。
//    跨服点歌：X区的“点歌玩家名”为X区的“被点玩家名”点了一首“歌手名”的“歌名”，并对他说：“玩家输入的话”
    public boolean sendLyric(LyricDataServer lds){
    	try{
	    	if(lds.getCurrentIndex() < 0){
	    		LyricData ld = lds.getLyricData();
	    		StringBuilder sb = new StringBuilder();
	    		String srcPlayerName = lds.getSrcPlayerName();
	    		String destPalyerName = lds.getDestPlayerName();
	    		if(lds.getDestServerId() >= 0){
	    			String srcServerName = arenaService.getServerName(lds.getSrcServerId());
	    			if(srcServerName != null){
	    				srcPlayerName = srcServerName + "的" + srcPlayerName;
	    			}
	    			srcServerName = arenaService.getServerName(lds.getDestServerId());
	    			if(srcServerName != null){
	    				destPalyerName = srcServerName + "的" + destPalyerName;
	    			}
	    		}
	    		sb.append(srcPlayerName);
	    		sb.append("为");
	    		sb.append(destPalyerName);
	    		sb.append("点了一首");
	    		sb.append(ld.getSinger());
	    		sb.append("的");
	    		sb.append(ld.getName());
	    		sb.append("，并对他说:");
	    		sb.append(lds.getBlessings());
	    		UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_LYRIC_SEND);
	    		seg.write(LyricsSystemConfig.LYRIC_CONTEXT);
	    		seg.writeString(sb.toString());
	    		arenaService.writeTo(lds.getSrcServerId(), seg);
	    		if(lds.getDestServerId() >= 0){
	    			
	    			seg = new UWAPSegment(ArenaConstants.CONN_ARENA_LYRIC_SEND);
	    			seg.write((byte)LyricsSystemConfig.LYRIC_CONTEXT);
	                seg.writeString(sb.toString());
	                arenaService.writeTo(lds.getDestServerId(), seg);
	    		}
	    		lds.setCurrentIndex(0);
	    	}else{
	    		String lyric = lds.getNextLyric();
	    		if(lyric == null){
	    			return true;
	    		}
	    		UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_LYRIC_SEND);
	    		seg.write(LyricsSystemConfig.LYRIC_LYRIC);
	    		seg.writeString(lds.getSrcPlayerName());
	    		seg.writeString(lyric);
	    		arenaService.writeTo(lds.getSrcServerId(), seg);
	    		if(lds.getDestServerId() >= 0){
	    			//点给本服的跨服不给予发送两次
	    			if(lds.getSrcServerId() == lds.getDestServerId()){
	    			}else{
		    			seg = new UWAPSegment(ArenaConstants.CONN_ARENA_LYRIC_SEND);
		    			seg.write(LyricsSystemConfig.LYRIC_LYRIC);
		    			seg.writeString(lds.getSrcPlayerName());
			    		seg.writeString(lyric);
		                arenaService.writeTo(lds.getDestServerId(), seg);
	    			}
	    		}
	    	}
    	}catch(Exception e){
    		log.info(e, e);
    	}
    	return false;
    }
}
