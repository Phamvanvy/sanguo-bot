package com.pip.itimes.server.world.fee;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ChargePlan {

    private String id;
    private String serviceNo;
    private String content;

    public ChargePlan() {
    }

    public String getServiceNo() {
        return serviceNo;
    }

    public String getId() {
        return id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setServiceNo(String serviceNo) {
        this.serviceNo = serviceNo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    private static Map id2plans = new HashMap();

    public static ChargePlan getChargePlan(String id){
        return (ChargePlan)id2plans.get(id);
    }

    public static ChargePlan[] getChargePlans(String[] ids){
        ChargePlan[] ret = new ChargePlan[ids.length];
        for(int i=0;i<ret.length;i++){
            ret[i] = getChargePlan(ids[i]);
        }
        return ret;
    }

    public static void addChargePlan(ChargePlan plan){
        id2plans.put(plan.getId(),plan);
    }
}
