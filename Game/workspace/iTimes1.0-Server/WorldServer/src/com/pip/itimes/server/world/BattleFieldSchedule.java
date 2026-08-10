package com.pip.itimes.server.world;



public class BattleFieldSchedule {
    public String cron;
    public int enter;
    public int enterfor;
    public int end;
    public String type;

    public BattleFieldSchedule(String cron,int enter,int enterfor,int end,String type){
        this.cron = cron;
        this.enter = enter;
        this.enterfor = enterfor;
        this.end = end;
        this.type = type;
    }
}
