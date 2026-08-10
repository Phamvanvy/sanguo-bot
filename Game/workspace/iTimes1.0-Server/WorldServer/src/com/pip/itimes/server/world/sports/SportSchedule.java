package com.pip.itimes.server.world.sports;

public class SportSchedule {
    public String cron;
    public int start;
    public int interval;
    public int end;
    public String type;
    public int bbsId;

    public SportSchedule(String cron, int start,  int end,  int interval,String type,int bbsId) {
        this.cron = cron;
        this.start = start;
        this.end = end;
        this.interval = interval;
        this.type = type;
        this.bbsId = bbsId;
    }
}
