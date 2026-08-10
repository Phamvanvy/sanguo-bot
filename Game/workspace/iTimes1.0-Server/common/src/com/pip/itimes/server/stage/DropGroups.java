package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class DropGroups {
    private static final Map id2group = new HashMap();
    private static final Map<Integer,List<DropGroup>> id2groups = new HashMap<Integer,List<DropGroup>>();

//    public static void addDropGroup(DropGroup dropGroup){
//        id2group.put(new Integer(dropGroup.getId()),dropGroup);
//    }

//    public static DropGroup getDropGroup(int id){
//        return (DropGroup)id2group.get(new Integer(id));
//    }

    public static void addDropGroup(DropGroup dropGroup){
        List<DropGroup> l = id2groups.get(dropGroup.getId());
        if(l==null){
            l = new ArrayList<DropGroup>();
            id2groups.put(dropGroup.getId(),l);
        }
        l.add(dropGroup);
    }

    public static DropGroup getDropGroup(int id,int level){
        List<DropGroup> l = id2groups.get(id);
        if(l!=null){
            for(DropGroup dr:l){
                if(level>=dr.getMinLevel()&&level<=dr.getMaxLevel()&&dr.getRate() > 0){
                	if(dr.getStartTime() == 0 || dr.getEndTime() == 0){
                		return dr;
                	}
                	long now = System.currentTimeMillis();
                	if(now >= dr.getStartTime() && now < dr.getEndTime()){
                		return dr;
                	}
                }
            }
        }
        return null;
    }
}
