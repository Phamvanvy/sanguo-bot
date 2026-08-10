package com.pip.itimes.server.stage;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ShoutActivityConfig {
	private int startHour;
	private int startMinute;
	private int endHour;
	private int endMinute;
	private String startMessage;
	private String endMessage;
	private int mapId;
	private Date startTime;
	private Date endTime;
	
	private Map <String, ShoutChat> shoutChatMap = new HashMap <String, ShoutChat> ();
	
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
    
    public void setShoutChat (String key, ShoutChat value) {
    	shoutChatMap.put(key, value);
    }
    public ShoutChat getShoutChat (String key) {
    	return shoutChatMap.get(key);
    }
    
    public Map<String, ShoutChat> getShoutChatMap () {
    	return shoutChatMap;
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
    
    public void setMapId (int mapId) {
    	this.mapId = mapId;
    }
    public int getMapId () {
    	return mapId;
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
