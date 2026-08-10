package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Jeffery
 * @version 1.0
 */
public class TaskNpcs {
    public static Map npcs = new HashMap();

    public static void addTaskNpc(TaskNpc npc){
        npcs.put(new Integer(npc.getId()),npc);
    }

    public static TaskNpc getTaskNpc(int id){
        return (TaskNpc)npcs.get(new Integer(id));
    }
}
