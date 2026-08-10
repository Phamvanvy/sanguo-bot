package com.pip.itimes.server.world.game;

public class BattleFieldRecord implements Comparable<BattleFieldRecord> {
    public int id;
    public String name;
    public int winTimes;
    public int loseTimes;

    public int compareTo(BattleFieldRecord o) {
        if(o==null)
            return 1;
        int ret = (winTimes*2-loseTimes)-(o.winTimes*2-o.loseTimes);
        if(ret==0){
            return id-o.id;
        }
        return ret;
//        return (winTimes*2-loseTimes)-(o.winTimes*2-o.loseTimes);
    }

    public int getPoint(){
        return winTimes*2-loseTimes;
    }
    
    //mengjie add
    public int getPoint2(){
        return winTimes*2+loseTimes;
    }
    //mengjie add end

    public String toString(){
        return name;
    }
}
