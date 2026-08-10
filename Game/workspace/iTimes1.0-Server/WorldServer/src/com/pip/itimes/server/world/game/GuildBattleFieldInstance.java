package com.pip.itimes.server.world.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.util.IntHashSet;
import com.pip.itimes.server.world.*;

public class GuildBattleFieldInstance extends Instance {


    private long createTime;
    private int attendedCount;  //付费的人数
    private int battleCount;

    private IntHashSet registeredPlayers = new IntHashSet();

    private Map<Integer,BattleFieldRecord> records = new HashMap<Integer,BattleFieldRecord>();
    private Map<Integer,BattleFieldRecord> guildRecords = new HashMap<Integer,BattleFieldRecord>();

    private BattleFieldRecord[] first = new BattleFieldRecord[3];

    private BattleFieldReserveComparator comparator = new BattleFieldReserveComparator();

    private int totalMoney;

    private Map<Integer,Integer> tongid2count = new HashMap<Integer,Integer>();
    private Map<Integer,Integer> playerid2tongid = new HashMap<Integer,Integer>();

    private TongService tongService;

    private ConcurrentHashMap<Integer,Long> waiting = new ConcurrentHashMap<Integer,Long>();

    public GuildBattleFieldInstance(int id, InstanceDefinition idf, InstanceService service,TongService tongService) {
        super(id, idf, service);
        this.tongService = tongService;
        createTime = System.currentTimeMillis();
    }

    public synchronized int[] getRegisteredPlayers(){
        return registeredPlayers.getValues();
    }

    public synchronized void register(WorldPlayer player){
        registeredPlayers.add(player.getId());
        waiting.remove(player.getId());
    }

    public synchronized void register(int playerId){
        registeredPlayers.add(playerId);
        waiting.remove(playerId);
    }



    public synchronized void addWaiting(IPlayerData player,long time){
        waiting.put(player.getId(),time);
    }

    public void unRegister(int id){
        registeredPlayers.remove(id);
    }

    //获取角色的帮会在副本中的人数，帮会根据进入时注册的取，如果没有进入就按现在的帮会，如果没有帮会就返回-1
    public int getPlayerByOldTong(WorldPlayer player){
        if(player.getTongId()<0)
            return -1;
        Integer tongId = playerid2tongid.get(player.getId());
        if(tongId==null){
            return getPlayerByTong(player.getTongId());
        }else{
           return getPlayerByTong(tongId.intValue());
        }
    }

    public void preAdd(WorldPlayer[] players) throws InstanceException{
        synchronized(this){
            if(getPlayerByTong(players[0].getTongId())<10){
                super.preAdd(players);
                attendedCount++;
                incCount(players[0].getTongId());
                playerid2tongid.put(players[0].getId(),players[0].getTongId());
            }else{
            	//mengjie modify
                throw new InstanceException("公会中已经有10个人在战场中");
            }
        }
    }

    public boolean setActive(int id) {
        boolean ret = super.setActive(id);
        if(ret){
            waiting.put(id,System.currentTimeMillis());
        }
        return ret;
    }

    private int getPlayerByTong(int id){
        Integer count = tongid2count.get(id);
        if(count==null)
            return 0;
        return count.intValue();
    }

    private void incCount(int id){
        Integer count = tongid2count.get(id);
        if(count==null){
            tongid2count.put(id,1);
        }else{
            int c = count.intValue()+1;
            tongid2count.put(id,c);
        }
    }

    private void decCount(int id){
        Integer count = tongid2count.get(id);
        if(count!=null){
            int c = count.intValue()-1;
            if(c<=0){
                tongid2count.remove(id);
            }else{
                tongid2count.put(id,c);
            }
        }
    }

    public int getAttendedCount(){
        return attendedCount;
    }

    public int getBattleCount(){
        return battleCount;
    }


    public ConcurrentHashMap<Integer,Long> getWaiting(){
        return waiting;
    }

    public synchronized void removePlayer(int playerId){
        waiting.remove(playerId);
        super.removePlayer(playerId);
        registeredPlayers.remove(playerId);
        Integer tongId = playerid2tongid.get(playerId);
        if(tongId!=null){
            decCount(tongId.intValue());
        }
    }

    public synchronized boolean removeActive(int playerId) {
        boolean b = super.removeActive(playerId);
        if (b) {
            waiting.remove(playerId);
            registeredPlayers.remove(playerId);
            Integer tongId = playerid2tongid.get(playerId);
            if (tongId != null) {
                decCount(tongId.intValue());
            }
        }
        return b;
    }

    public synchronized void addRecord(IPlayerData winner,IPlayerData failure){
        BattleFieldRecord bfr = records.get(winner.getId());
        if(bfr==null){
            bfr = new BattleFieldRecord();
            bfr.id = winner.getId();
            bfr.name = winner.getPlayerName();
            records.put(bfr.id,bfr);
        }
        bfr.winTimes++;
        bfr = records.get(failure.getId());
        if(bfr==null){
            bfr = new BattleFieldRecord();
            bfr.id = failure.getId();
            bfr.name = failure.getPlayerName();
            records.put(bfr.id,bfr);
        }
        bfr.loseTimes++;
        int guildId = getGuildId(winner);
        if(guildId>=0){
            bfr = guildRecords.get(guildId);
            if(bfr==null){
                bfr = new BattleFieldRecord();
                bfr.id =guildId;

                bfr.name = tongService.getTongName(guildId);
                guildRecords.put(bfr.id,bfr);
            }
            bfr.winTimes++;
        }
        compareToFirst(bfr);
        guildId = getGuildId(failure);
        if(guildId>=0){
            bfr = guildRecords.get(guildId);
            if(bfr==null){
                bfr = new BattleFieldRecord();
                bfr.id =guildId;
                bfr.name = tongService.getTongName(guildId);
                guildRecords.put(bfr.id,bfr);
            }
            bfr.loseTimes++;
        }
        compareToFirst(bfr);
        battleCount++;
    }

    protected int getGuildId(IPlayerData p){
        Integer id = playerid2tongid.get(p.getId());
        if(id!=null){
            return id.intValue();
        }
        return -1;
    }


    protected void compareToFirst(BattleFieldRecord bfr) {
        int index = -1;
        for(int i=0;i<first.length;i++){
            if(first[i]==null){
                break;
            }else{
                if(first[i].id == bfr.id){
                    index = i;
                }
            }
        }
        if(index!=-1){  //如果在前3名内就排序
            Arrays.sort(first,comparator);
        } else {  //如果不在前3名那么就查看是否有一个位子可以替换
            for (int i = 0; i < first.length; i++) {
                if (first[i] == null) {
                    first[i] = bfr;
                    break;
                } else {
                    if (first[i].id != bfr.id) {
                        if (bfr.compareTo(first[i]) > 0) {
                            if (i == 0) {
                                first[2] = first[1];
                                first[1] = first[0];
                            } else if (i == 1) {
                                first[2] = first[1];
                            }
                            first[i] = bfr;
                            break;
                        }
                    } else
                        break;
                }
            }
        }
    }

    public synchronized BattleFieldRecord[] getFirstGuildRecord(){
        return first;
    }

    public BattleFieldRecord getRecord(int playerId){
        return records.get(playerId);
    }

    public BattleFieldRecord getGuildRecord(int guildId){
        return guildRecords.get(guildId);
    }


    public List<BattleFieldRecord> getOrderedGuildRecords(){
        List<BattleFieldRecord> ret = new ArrayList<BattleFieldRecord>(guildRecords.values());
        Collections.sort(ret,comparator);
        return ret;
    }

    public List<BattleFieldRecord> getOrderedRecords(){
        List<BattleFieldRecord> ret = new ArrayList<BattleFieldRecord>(records.values());
        Collections.sort(ret,comparator);
        return ret;
    }

    public synchronized void addMoney(int money){
        totalMoney += money;
    }

    public int getMoney(){
        return totalMoney;
    }


 }

// class BattleFieldReserveComparator implements Comparator<BattleFieldRecord>{
//     public int compare(BattleFieldRecord o1, BattleFieldRecord o2){
//         if(o1==null&&o2==null)
//             return 0;
//         if(o1==null)
//             return 1;
//         if(o2==null)
//             return -1;
//         return o2.compareTo(o1);
//     }
//
//     public boolean equals(Object obj){
//         return false;
//     }
//
//}
