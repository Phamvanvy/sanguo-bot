package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Tips {

    private static final List tips = new ArrayList();
    private static final Random rnd = new Random();

    public static void addTip(String tip){
        tips.add(tip);
    }

    public static String getTip(){
        if(tips.size()==0)
            return "";
        return (String)tips.get(rnd.nextInt(tips.size()));
    }
}
