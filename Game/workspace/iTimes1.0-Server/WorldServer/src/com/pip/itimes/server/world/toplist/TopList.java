package com.pip.itimes.server.world.toplist;


import java.util.Calendar;


public abstract class TopList{
    protected abstract long getMakeTime();

    protected abstract long getLastMakeTime();

    protected abstract long getPeriod();

    protected abstract long getSpace();

    protected final long getTodayStart(){
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime().getTime();
    }

    protected final boolean testTopListTime(){
        long currentTime = System.currentTimeMillis();
        long makeTime = getTodayStart() + getMakeTime();
        long lastMakeTime = getLastMakeTime();

        if(currentTime - lastMakeTime > getPeriod()){
            return true;
        }

        if(currentTime > makeTime && currentTime - makeTime < getSpace() && currentTime - makeTime < currentTime - lastMakeTime){
            return true;
        }

        return false;
    }
    
    public abstract void processTopList();
}
