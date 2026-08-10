package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TaskAwards {

    private static final Map awards = new TreeMap();

    public TaskAwards() {
    }

    public static void addAward(TaskAward award){
        awards.put(new Short(award.getTaskId()),award);
    }

    public static TaskAward getTaskAward(short id){
        return (TaskAward)awards.get(new Short(id));
    }
}
