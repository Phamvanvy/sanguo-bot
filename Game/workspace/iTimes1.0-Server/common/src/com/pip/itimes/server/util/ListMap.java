package com.pip.itimes.server.util;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffery
 * @version 1.0
 */
public class ListMap {

    private Map map = new HashMap();


    public ListMap() {
    }

    public List getList(Object key){
        List l = (List)map.get(key);
        if(l==null){
            l = new ArrayList();
            map.put(key,l);
        }
        return l;
    }

    public void put(Object key,Object value){
        List l = getList(key);
        synchronized(l){
            l.add(value);
        }
    }

}
