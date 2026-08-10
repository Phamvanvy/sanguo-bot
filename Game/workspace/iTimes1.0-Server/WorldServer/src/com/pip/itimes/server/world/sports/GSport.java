package com.pip.itimes.server.world.sports;

import com.pip.itimes.server.world.WorldPlayer;
import java.util.*;
import com.pip.itimes.server.stage.Changed;

public class GSport extends Sport{

    private Map<Integer,List<SportRecord>> gRecords = new HashMap<Integer,List<SportRecord>>();

    public GSport(long start,long end,long interval,String name,int bbsId) {
        super(start,end,interval,name,bbsId);
        chatString = getDateString(this.start,this.end)+"的公会赛开始了，请大家到幻想运动场参加比赛吧。";
    }

    public synchronized SportRecord play(WorldPlayer player,Changed changed) throws SportException{
        if(status!=STATUS_STARTED){
            throw new SportException("比赛没有开始");
        }
        if(player.completeRemoveItem(560002,1,changed)==null){
            throw new SportException("你没有参赛入场券");
        }
        SportRecord sr = new SportRecord(player.getId(),player.getPlayerName());
        sr.startTime = System.currentTimeMillis();
        records.put(sr.playerId,sr);
        return sr;
    }

    public SportRecord over(WorldPlayer player) throws SportException{
        SportRecord sr = super.over(player);
        if((sr.overTime-sr.startTime)<=20*1000L)
            return sr;
        String gName = player.getTongName();
        sr.guildName = gName;
        if(player.getTongId()!=-1){
            List<SportRecord> l = gRecords.get(player.getTongId());
            if(l==null){
                l = new ArrayList<SportRecord>(3);
                gRecords.put(player.getTongId(),l);
            }
            if(l.size()>0){
                for(int i=0;i<l.size();i++){
                    SportRecord st = l.get(i);
                    if(st.playerId==sr.playerId){
                        if(st.compareTo(sr)>0){
                            l.set(i,sr);
                        }
                        return sr;
                    }
                }
            }
            if(l.size()>=3){
                SportRecord max = l.get(0);
                int maxIndex = 0;
                for(int i=1;i<l.size();i++){
                    SportRecord st = l.get(i);
                    if((st.overTime-st.startTime)>(max.overTime-max.startTime)){
                        max = st;
                        maxIndex = i;
                    }
                }
                if((sr.overTime-sr.startTime)<(max.overTime-max.startTime)){
                    l.set(maxIndex,sr);
                }
            }else{
                l.add(sr);
            }
        }
        return sr;
    }
    public SportResult[] getFirst20(){
    	return null;
    }
    public SportResult[] getFirst10(){
        TMP[] tmps = new TMP[gRecords.size()];
        Iterator<Map.Entry<Integer,List<SportRecord>>> ite = gRecords.entrySet().iterator();
        int i = 0;
        while(ite.hasNext()){
            Map.Entry<Integer,List<SportRecord>> entry = ite.next();
            TMP tmp = new TMP();
            tmp.id = entry.getKey();
            List<SportRecord> l = entry.getValue();
            if(l.size()==1){
                SportRecord sr = l.get(0);
                tmp.name = sr.guildName;
                tmp.value += (sr.overTime-sr.startTime)+3000L;
            }
            if(l.size()==2){
                SportRecord sr0 = l.get(0);
                SportRecord sr1 = l.get(1);
                tmp.name = sr0.guildName;
                tmp.value = ((sr0.overTime-sr0.startTime)+(sr1.overTime-sr1.startTime)*2)/3+2000L;
            }
            if(l.size()==3){
                SportRecord sr0 = l.get(0);
                SportRecord sr1 = l.get(1);
                SportRecord sr2 = l.get(2);
                tmp.name = sr0.guildName;
                tmp.value = ((sr0.overTime-sr0.startTime)+(sr1.overTime-sr1.startTime)+(sr2.overTime-sr2.startTime))/3;
            }
            tmps[i] = tmp;
            i++;
        }
        Arrays.sort(tmps);
        SportResult[] ret = null;
        if(tmps.length<10){
            ret = new SportResult[tmps.length];
        }else{
            ret = new SportResult[10];
        }
        for(int j=0;j<ret.length&&j<10;j++){
            ret[j] = new SportResult();
            ret[j].id = tmps[j].id;
            ret[j].name = tmps[j].name;
            List<SportRecord> ll = gRecords.get(tmps[j].id);
            ret[j].records = new SportRecord[ll.size()];
            ll.toArray(ret[j].records);
        }
        return ret;
    }


}
class TMP implements Comparable {
    int id;
    String name;
    long value;

    public int compareTo(Object o) {
        if((value - ((TMP) o).value)>0)
            return 1;
        else if((value - ((TMP) o).value)<0)
            return -1;
        return 0;
    }
}
