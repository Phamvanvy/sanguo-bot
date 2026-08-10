package com.pip.engine;

import com.pip.common.Tool;


public class Random{

    public Random(){
        this(Tool.getSystemTime());
    }

    public Random(long seed){
        setSeed(seed);
    }

    public synchronized void setSeed(long seed){
        this.seed = (seed ^ 25214903917L) & 281474976710655L;
    }

    protected synchronized int next(int bits){
        long nextseed = seed * 25214903917L + 11L & 281474976710655L;
        seed = nextseed;
        return (int)(nextseed >>> 48 - bits);
    }

    public int nextInt(){
        return next(32);
    }

    public int nextInt(int n){
        if(n <= 0)
            throw new IllegalArgumentException("n must be positive");
        if((n & -n) == n)
            return (int)((long)n * (long)next(31) >> 31);
        int bits;
        int val;
        do{
            bits = next(31);
            val = bits % n;
        }while((bits - val) + (n - 1) < 0);
        return val;
    }

    public long nextLong(){
        return ((long)next(32) << 32) + next(32);
    }

    private long seed;
}
