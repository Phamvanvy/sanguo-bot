package com.pip.itimes.server.world.game;

import com.pip.itimes.server.util.IntHashSet;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.WorldPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class BattleFieldInstance extends Instance {

    private int beginLevel;
    private int endLevel;
    private long createTime;
    private int attendedCount;  //付费的人数
    private int battleCount;

    private IntHashSet registeredPlayers = new IntHashSet();

    private Map<Integer,BattleFieldRecord> records = new HashMap<Integer,BattleFieldRecord>();

    private BattleFieldRecord[] first = new BattleFieldRecord[3];

    private BattleFieldReserveComparator comparator = new BattleFieldReserveComparator();

    private int totalMoney;

    public BattleFieldInstance(int id,InstanceDefinition idf,InstanceService service,int beginLevel,int endLevel) {
        super(id,idf,service);
        this.beginLevel = beginLevel;
        this.endLevel = endLevel;
        this.createTime = System.currentTimeMillis();
   }

   public int getBeginLevel(){
       return beginLevel;
   }

   public int getEndLevel(){
       return endLevel;
   }

   public synchronized int[] getRegisteredPlayers(){
       return registeredPlayers.getValues();
   }

   public synchronized void register(WorldPlayer player){
       registeredPlayers.add(player.getId());
   }

   public void unRegister(int id){
       registeredPlayers.remove(id);
   }

   public void preAdd(WorldPlayer[] players) throws InstanceException{
       super.preAdd(players);
       attendedCount++;
   }

   public int getAttendedCount(){
       return attendedCount;
   }

   public int getBattleCount(){
       return battleCount;
   }


   public synchronized void removePlayer(int playerId){
       super.removePlayer(playerId);
       registeredPlayers.remove(playerId);
   }

   public synchronized boolean removeActive(int playerId){
       boolean b = super.removeActive(playerId);
       if(b){
           registeredPlayers.remove(playerId);
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
       compareToFirst(bfr);
       bfr = records.get(failure.getId());
       if(bfr==null){
           bfr = new BattleFieldRecord();
           bfr.id = failure.getId();
           bfr.name = failure.getPlayerName();
           records.put(bfr.id,bfr);
       }
       bfr.loseTimes++;
       compareToFirst(bfr);
       battleCount++;
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

   public synchronized BattleFieldRecord[] getFirstRecord(){
       return first;
   }

   public BattleFieldRecord getRecord(int playerId){
       return records.get(playerId);
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

   public static void main(String[] args){
       BattleFieldRecord r1 = new BattleFieldRecord();
       r1.id = 1;
       r1.name = "1";
       r1.winTimes = 2;
       r1.loseTimes = 3;
       BattleFieldRecord r2 = new BattleFieldRecord();
       r2.id = 2;
       r2.name = "2";
       r2.winTimes = 3;
       r2.loseTimes = 1;
       BattleFieldRecord[] rs = new BattleFieldRecord[3];
       rs[0]= r1;
       rs[1] = r2;
       Arrays.sort(rs,new BattleFieldReserveComparator());
       for(int i=0;i<rs.length;i++){
           System.out.println(rs[i]);
       }
   }
}

//class BattleFieldReserveComparator implements Comparator<BattleFieldRecord>{
//    public int compare(BattleFieldRecord o1, BattleFieldRecord o2){
//        if(o1==null&&o2==null)
//            return 0;
//        if(o1==null)
//            return 1;
//        if(o2==null)
//            return -1;
//        return o2.compareTo(o1);
//    }
//
//    public boolean equals(Object obj){
//        return false;
//    }
//}
