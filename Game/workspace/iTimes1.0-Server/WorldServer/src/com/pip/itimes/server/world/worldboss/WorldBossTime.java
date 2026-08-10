package com.pip.itimes.server.world.worldboss;

/**
 * @file WorldBossTime.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-9-19
 **/
public class WorldBossTime {
	private int hour;
	private int minute;
	private int endhour;
	private int endminute;
	
	private int startTime;
	private int endTime;
	private int index;
	
	public WorldBossTime(int hour, int minute, int endhour, int endminute){
		this.hour = hour;
		this.minute = minute;
		this.endhour = endhour;
		this.endminute = endminute;
		resetTime();
	}
	
	public void resetTime(){
		startTime = hour * 60 + minute;
		endTime = endhour * 60 + endminute;
	}
	
	public int getStartTime(){
		return startTime;
	}
	
	public int getEndTime(){
		return endTime;
	}
	
	public int getHour(){
		return hour;
	}
	
	public int getMinute(){
		return minute;
	}
	
	public int getEndHour(){
		return endhour;
	}
	
	public int getEndMinute(){
		return endminute;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public int getIndex(){
		return index;
	}
}
