package com.pip.itimes.server.stage;

import java.util.Date;


public class CampBuff {
	
	private int property;
	private int value;
    private byte unit;
    private int time;
    private String message;
    private Date startTime;
    private Date endTime;
    
    public CampBuff () {
    	
    }
    
    public int getProperty () {
        return property;
    }

    public void setProperty (int property) {
        this.property = property;
    }

    public int getValue () {
        return value;
    }

    public void setValue(int value){
        this.value = value;
    }
    
    public void setTime (int time) {
    	this.time = time;
    }
    
    public int getTime () {
    	return time;
    }
    
    public void setUnit (byte unit) {
    	this.unit = unit;
    }
    
    public byte getUnit () {
    	return unit;
    }
    
    public String getMessage () {
    	return message;
    }
    
    public void setMessage (String message) {
    	this.message = message;
    }
    
    public void setStartTime (Date startTime) {
    	this.startTime = startTime;
    }
    
    public Date getStartTime () {
    	return startTime;
    }
    
    public void setEndTime (Date endTime) {
    	this.endTime = endTime;
    }
    
    public Date getEndTime () {
    	return endTime;
    }
}

