package com.pip.itimes.server.world.sports;

import com.pip.itimes.server.world.WorldPlayer;
import java.util.*;
import com.pip.itimes.server.stage.Changed;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;

public abstract class Sport{

    public static final int STATUS_INIT = 0;
    public static final int STATUS_STARTED = 1;
    public static final int STATUS_END = 2;

    protected int status;
    protected long start;
    protected long end;
    protected long interval;
    protected long init;
    protected String name;
    protected int bbsId;
    protected String chatString;
    protected volatile long chatTime;

    protected Map<Integer,SportRecord> records = new HashMap<Integer,SportRecord>();
    protected Map<Integer,SportRecord> cRecords = new HashMap<Integer,SportRecord>();

    protected SportsService service;

    private static SimpleDateFormat format = new SimpleDateFormat("HH点mm分");

    private static final Logger log = Logger.getLogger(Sport.class);

    public Sport(long start,long end,long interval,String name,int bbsId){
        init = System.currentTimeMillis();
        this.start = start;
        this.end = end;
        this.interval = interval;
        this.name = name;
        this.bbsId = bbsId;
        this.chatTime = init;
        status = STATUS_INIT;
    }

    public String getName(){
        return name;
    }

    public String getChatString(){
        return chatString;
    }

    public void setNextChatTime(){
        chatTime += interval;
    }

    public long getNextChatTime(){
        return chatTime;
    }

    public int getStatus(){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public void start() {
        status = STATUS_STARTED;
    }

    public void end() {
        status = STATUS_END;
    }

    public abstract SportRecord play(WorldPlayer player,Changed changed)throws SportException;

    public synchronized SportRecord over(WorldPlayer player) throws SportException{
        if(status!=STATUS_STARTED){
            throw new SportException("比赛没有开始");
        }
        SportRecord sr = records.remove(player.getId());
        if(sr==null)
            throw new SportException("您还没有报名吧？请先报名哦。");
        sr.overTime = System.currentTimeMillis();
        if((sr.overTime-sr.startTime)<=80*1000L)
            return sr;
        SportRecord pr = cRecords.get(player.getId());
        log.info("ID["+sr.playerId+"]SportTime["+(sr.overTime - sr.startTime)+"]");
        if(pr!=null){
            if ((sr.overTime - sr.startTime) < (pr.overTime - pr.startTime)) {
                cRecords.put(sr.playerId, sr);
            }
        }else{
            cRecords.put(sr.playerId,sr);
        }
        return sr;
    }

    public String getDateString(long start,long end){
        Date s = new Date(start);
        Date e = new Date(end);
        return "["+format.format(s)+"]至["+format.format(e)+"]";
    }

    public abstract SportResult[] getFirst10();
    
    public abstract SportResult[] getFirst20();
}
