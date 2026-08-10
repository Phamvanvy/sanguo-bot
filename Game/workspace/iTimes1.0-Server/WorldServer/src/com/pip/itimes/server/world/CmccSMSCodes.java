package com.pip.itimes.server.world;

import java.util.HashMap;
import java.util.Map;

public class CmccSMSCodes {
    private static final Map<String,String[]> m = new HashMap<String,String[]>();

    public static String[] getSMSCode(String value){
        return m.get(value);
    }

    public static void addSMSCode(String value,String[] codes){
        m.put(value,codes);
    }
}
