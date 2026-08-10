package com.pip.sanguo.performancetest.net;


import java.util.Hashtable;


public class KeyMaker{
    private Hashtable maxKey = new Hashtable();
    private Integer key = new Integer(0);
    
    public KeyMaker(){
        maxKey.put(key, key);
    }
    
    public int nextKey(){
        synchronized(maxKey){
            int k = ((Integer)maxKey.get(key)).intValue() + 1;
            maxKey.put(key, new Integer(k));
            return k;
        }
    }
}
