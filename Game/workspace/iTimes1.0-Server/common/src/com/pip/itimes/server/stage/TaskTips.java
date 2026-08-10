package com.pip.itimes.server.stage;

import java.util.*;

public class TaskTips {
    private Map<Integer,ArrayList<TaskTip>> tips = new HashMap<Integer,ArrayList<TaskTip>>();

    public void addTip(int id,int beginLevel,int endLevel,String tip,String[] backups){
        ArrayList<TaskTip> l = tips.get(id);
        if(l==null){
            l = new ArrayList<TaskTip>();
            tips.put(id,l);
        }
        TaskTip t = new TaskTip(beginLevel,endLevel,tip,backups);
        l.add(t);
    }

    public String getTip(int id,int level){
        ArrayList<TaskTip> l = tips.get(id);
        if(l!=null){
            for(int i=0,size=l.size();i<size;i++){
                TaskTip t = l.get(i);
                if(level>=t.beginLevel&&level<=t.endLevel)
                    return t.getTip();
            }
        }
        return "";
    }

    public void clear(){
        tips.clear();
    }
}

class TaskTip{
    int beginLevel;
    int endLevel;
    String tip;
    String[] backups;
    private Random rnd = new Random();

    public TaskTip(int beginLevel,int endLevel,String tip,String[] backups){
        this.beginLevel = beginLevel;
        this.endLevel = endLevel;
        this.tip = tip;
        this.backups = backups;
    }

    public String getTip(){
        if(tip!=null)
            return tip;
        return backups[rnd.nextInt(backups.length)];
    }
}
