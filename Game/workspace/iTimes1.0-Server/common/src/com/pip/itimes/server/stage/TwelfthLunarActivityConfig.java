package com.pip.itimes.server.stage;

import java.util.Calendar;
import java.util.Date;

public class TwelfthLunarActivityConfig {

	private int startHour;
	private int startMinute;
	private int endHour;
	private int endMinute;
	private String startMessage;
	private String endMessage;
	// 每日活动开始的时间(每天需要重置)
	private Date startTime;
	// 每日活动结束的时间(每天需要重置)
	private Date endTime;
	
	private TwelfthLunar twelfthLunar = new TwelfthLunar();
	
	public void setTwelfthLunar (TwelfthLunar twelfthLunar) {
		this.twelfthLunar = twelfthLunar;
	}
	
	public TwelfthLunar getTwelfthLunar () {
		return twelfthLunar;
	}
	
    public void setStartHour (int startHour) {
    	this.startHour = startHour;
    }
    public int getStartHour(){
    	return startHour;
    }
    
    public void setStartMinute (int startMinute) {
    	this.startMinute = startMinute;
    }
    public int getStartMinute() {
    	return startMinute;
    }
    
    public void setEndHour (int endHour) {
    	this.endHour = endHour;
    }
    public int getEndHour(){
    	return endHour;
    }
    
    public void setEndMinute (int endMinute) {
    	this.endMinute = endMinute;
    }
    public int getEndMinute () {
    	return endMinute;
    }
    
    public void setStratMessage (String startMessage) {
    	this.startMessage = startMessage;
    }
    public String getStartMessage () {
    	return startMessage;
    }
    
    public void setEndMessage (String endMessage) {
    	this.endMessage = endMessage;
    }
    public String getEndMessage () {
    	return endMessage;
    }
    
	public void setActivityStartTime (Date time) {
		startTime = time;
	}
	public Date getActivityStartTime () {
		return startTime;
	}
	
	public void setActivityEndTime (Date time) {
		endTime = time;
	}
	public Date getActivityEndTime () {
		return endTime;
	}
	
	public void resetActivityTime (){
		setActivityStartTime(getConfigDate(true));
		setActivityEndTime(getConfigDate(false));
    }
    
	public Date getConfigDate (boolean start) {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		if (start) {
			cal.set(Calendar.HOUR_OF_DAY, getStartHour());
			cal.set(Calendar.MINUTE, getStartMinute());
		} else {
			cal.set(Calendar.HOUR_OF_DAY, getEndHour());
			cal.set(Calendar.MINUTE, getEndMinute());
		}
		return cal.getTime();
	}

}
