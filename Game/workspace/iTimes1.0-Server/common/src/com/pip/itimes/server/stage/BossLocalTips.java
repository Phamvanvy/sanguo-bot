package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

public class BossLocalTips {
	public static final Map<Integer,String[]> tips = new HashMap<Integer,String[]>();

    public static void addTip(int id,String message[]){
        tips.put(id,message);
    }

    public static String[] getTip(int id){
        return tips.get(id);
    }

    public static void clear(){
        tips.clear();
    }
}
