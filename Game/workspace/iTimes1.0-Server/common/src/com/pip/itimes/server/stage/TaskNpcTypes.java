package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Jeffery
 * @version 1.0
 */
public class TaskNpcTypes {

    private final static Map types = new HashMap();

    public static void adddTaskNpcType(TaskNpcType taskNpcType){
        types.put(new Integer(taskNpcType.getId()),taskNpcType);
    }

    public static TaskNpcType getTaskNpcType(int id){
        return (TaskNpcType)types.get(new Integer(id));
    }
}
