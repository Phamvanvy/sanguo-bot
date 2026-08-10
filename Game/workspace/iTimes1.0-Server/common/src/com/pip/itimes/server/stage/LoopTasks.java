package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * @author mengjie
 * @version 1.0
 */
public class LoopTasks {

    private short taskid;//循环的任务id
    private int loops;//每个周期循环次数
    private int time;//周期间隔（小时）。如24小时，按整天算
    private int group;//分组。同一分组内任务不能同时接
    
    private int finishcount = 0;
    private Date lastfinishtime = null;
    
    public int getPreTask() {
		return preTask;
	}

	public void setPreTask(int preTask) {
		this.preTask = preTask;
	}

	/**
	 * 没有前置任务
	 */
	public final static int noPreTask  = -1;
	
	/**
	 * 前置任务起始编号
	 */
	public final static int preTaskStart = 0;
	/**
     * 跑环任务的前置任务号
     */
    private int preTask = -1;
    
    public static Map<Short,LoopTasks> LoopTaskbyTaskid = new HashMap<Short,LoopTasks>();
    public static Map<Integer,ArrayList<LoopTasks>> LoopTaskbygroupid = new HashMap<Integer,ArrayList<LoopTasks>>();
    
    
  /*  *//**
     * 阵营的每日任务 主要用于战斗胜利的检索
     *//*
    public static Map<Integer,ArrayList<LoopTasks>> campLoopTask = new HashMap<Integer,ArrayList<LoopTasks>>();*/
    
    public static  void addLoopTasks(LoopTasks loopTasks){

    	LoopTaskbyTaskid.put(loopTasks.getTaskid(), loopTasks);
    	if (LoopTaskbygroupid.get(loopTasks.getGroup()) == null){
    		ArrayList LoopTasklist = new ArrayList<LoopTasks>();
    		LoopTasklist.add(loopTasks);
    		LoopTaskbygroupid.put(loopTasks.getGroup(), LoopTasklist);
    	}else{
    		ArrayList LoopTasklist = LoopTaskbygroupid.get(loopTasks.getGroup());
    		LoopTasklist.add(loopTasks);
    		LoopTaskbygroupid.remove(loopTasks.getGroup());
    		LoopTaskbygroupid.put(loopTasks.getGroup(), LoopTasklist);
    	}
    }
    
    /**
     * @return
     * 获取阵营里面的每一个每日任务
     */
    public static ArrayList<LoopTasks> getCampLoopTaskIds(){
    	return LoopTaskbygroupid.get(2);
    }
    public LoopTasks(Short taskid,int loops,int time,int group, int campId) {
    	this.taskid = taskid;
    	this.loops = loops;
    	this.time = time;
    	this.group = group;
    	this.campId = campId;
    }

	public Short getTaskid() {
		return taskid;
	}

	public void setTaskid(Short taskid) {
		this.taskid = taskid;
	}

	public int getLoops() {
		return loops;
	}

	public void setLoops(int loops) {
		this.loops = loops;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getFinishcount() {
		return finishcount;
	}

	public void setFinishcount(int finishcount) {
		this.finishcount = finishcount;
	}

	public Date getLastfinishtime() {
		return lastfinishtime;
	}

	public void setLastfinishtime(Date lastfinishtime) {
		this.lastfinishtime = lastfinishtime;
	}


	public int getCampId() {
		return campId;
	}

	public void setCampId(int campId) {
		this.campId = campId;
	}

    
    /**
     * 阵营任务物品id
     */
    private int campId;
    
    
}
