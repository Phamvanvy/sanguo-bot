package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

public class LevelTips {

    public static final Map<Integer,LevelTip> tips = new HashMap<Integer,LevelTip>();

    public static void addTip(int level,String message,String shout,int[] id,int[] count,String[] mailmsg,int worldmsg){
        LevelTip tip = new LevelTip(message,shout,id,count,mailmsg, worldmsg);
        tips.put(level,tip);
    }

    public static String getTip(int level){
        LevelTip tip = tips.get(level);
        if(tip!=null)
            return tip.getMessage();
        return null;
    }

    public static String getShout(int level){
        LevelTip tip = tips.get(level);
        if(tip!=null)
            return tip.getShout();
        return null;
    }
    
    public static int[] getitimeID(int level){
        LevelTip tip = tips.get(level);
        if(tip!=null)
            return tip.getId();
        return null;
    }
    
    public static int[] getitimecount(int level){
        LevelTip tip = tips.get(level);
        if(tip!=null)
            return tip.getCount();
        return null;
    }
    
    public static String[] getmailmsg(int level){
        LevelTip tip = tips.get(level);
        if(tip!=null)
            return tip.getMailmsg();
        return null;
    }
    public static int getWorldMsg(int level){
        LevelTip tip = tips.get(level);
        if(tip!=null)
            return tip.getWorldMsg();
        return 0;
    }
}
