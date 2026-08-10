package com.pip.itimes.server.stage;

public class Enemy implements Comparable {
    public int id;
    public String name;
    public long lastTime;
    public int times;
    public Enemy(int id,String name,int times,long lastTime){
        this.id = id;
        this.name = name;
        this.times = times;
        this.lastTime = lastTime;
    }


    public int compareTo(Object o) {
        Enemy e = (Enemy)o;
        long t = lastTime-e.lastTime;
        if(t>0)
            return -1;
        else if(t<0)
            return 1;
        return 0;
    }
}
