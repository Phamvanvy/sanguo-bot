package com.pip.itimes.server.world.sports;

import java.util.*;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.stage.Changed;

public class PSport  extends Sport{
    public PSport(long start,long end,long interval,String name,int bbsId) {
       super(start,end,interval,name,bbsId);
       chatString = getDateString(this.start,this.end)+"的个人赛开始了，请大家到幻想运动场参加比赛吧。";
    }

    public synchronized SportRecord play(WorldPlayer player,Changed changed) throws SportException{
        if(status!=STATUS_STARTED){
            throw new SportException("比赛没有开始");
        }
        if(player.completeRemoveItem(560001,1,changed)==null){
            throw new SportException("你没有参赛入场券");
        }
        SportRecord sr = new SportRecord(player.getId(),player.getPlayerName());
        sr.startTime = System.currentTimeMillis();
        records.put(sr.playerId,sr);
        return sr;
    }
    public SportResult[] getFirst20(){
    	return null;
    }
    public SportResult[] getFirst10(){
        Collection<SportRecord> l = cRecords.values();
        SportRecord[] srs = new SportRecord[l.size()];
        l.toArray(srs);
        Arrays.sort(srs);
        SportResult[] ret = null;
        if(srs.length<10){
            ret = new SportResult[srs.length];
        }else{
            ret = new SportResult[10];
        }
        for(int i=0;i<10&&i<srs.length;i++){
            ret[i] = new SportResult();
            ret[i].name = srs[i].playerName;
            ret[i].id = srs[i].playerId;
            ret[i].records = new SportRecord[1];
            ret[i].records[0] = srs[i];
        }
        return ret;
    }
}
