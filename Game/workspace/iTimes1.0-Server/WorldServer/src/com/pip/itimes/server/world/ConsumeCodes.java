package com.pip.itimes.server.world;

import java.util.HashMap;
import java.util.Map;

public class ConsumeCodes {
    private static final Map<Integer,String[]> m = new HashMap<Integer,String[]>();

    public static String[] getConsumeCode(int value){
        return m.get(value);
    }

    public static void addConsumeCode(int value,String[] codes){
        m.put(value,codes);
    }
}
