package com.pip.itimes.server.world.sports;

public class SportRecord implements Comparable {
    public int playerId;
    public String playerName;
    public String guildName;
    public long startTime;
    public long overTime;

    public SportRecord(int playerId,String name){
        this.playerId = playerId;
        this.playerName = name;
    }


    public int compareTo(Object o) {
        SportRecord s = (SportRecord)o;
        return (int)((overTime-startTime)-(s.overTime-s.startTime));
    }
}
