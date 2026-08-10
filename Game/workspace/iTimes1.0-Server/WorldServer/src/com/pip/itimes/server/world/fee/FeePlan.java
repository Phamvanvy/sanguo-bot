package com.pip.itimes.server.world.fee;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class FeePlan {

    private int max;
    private int fee;
    private String id;
    private int beginLevel;
    private String content;


    public FeePlan() {
    }

    public int getMax() {
        return max;
    }

    public String getId() {
        return id;
    }

    public int getFee() {
        return fee;
    }

    public void setBeginLevel(int beginLevel) {
        this.beginLevel = beginLevel;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public int getBeginLevel() {
        return beginLevel;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getContent(){
        return content;
    }

    private static Map id2plans = new HashMap();

    public static FeePlan getFeePlan(String id){
        return (FeePlan)id2plans.get(id);
    }

    public static void addFeePlan(FeePlan plan){
        id2plans.put(plan.getId(),plan);
    }
}
