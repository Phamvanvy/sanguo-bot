package com.pip.itimes.server.world.chr;

import java.util.Calendar;
import java.util.Date;

public class ChristmasAuctionConfig {
	private int startHour;
	private int startMinute;
	private int endHour;
	private int endMinute;
	
	private long startTime;
	private long endTime;
	
	private String adMessage;
	private String endMessage;
	private int giftId;
	
    public void setStartHour(int startHour){
    	this.startHour = startHour;
    }
    public int getStartHour(){
    	return startHour;
    }
    public void setStartMinute(int startMinute){
    	this.startMinute = startMinute;
    }
    public int getStartMinute(){
    	return startMinute;
    }
    public void setEndHour(int endHour){
    	this.endHour = endHour;
    }
    public int getEndHour(){
    	return endHour;
    }
    public void setEndMinute(int endMinute){
    	this.endMinute = endMinute;
    }
    public int getEndMinute(){
    	return endMinute;
    }
    public void setAdMessage(String adMessage){
    	this.adMessage = adMessage;
    }
    public String getAdMessage(){
    	return adMessage;
    }
    public void setEndMessage(String endMessage){
    	this.endMessage = endMessage;
    }
    public String getEndMessage(){
    	return endMessage;
    }
    
    public void setGiftId (int giftId) {
    	this.giftId = giftId;
    }
    
    public int getGiftId () {
    	return giftId;
    }
    
    public void setStartTime(long startTime){
    	this.startTime = startTime;
    }
    
    public long getStartTime(){
    	return startTime;
    }
    
    public void setEndTime(long endTime){
    	this.endTime = endTime;
    }
    
    public long getEndTime(){
    	return endTime;
    }
    
    /**
     * 重置时间 一般在每天0点时进行重置 或是启动的时候重置
     */
    public void resetTime(){
    	startTime = getConfigDate(true);
    	endTime = getConfigDate(false);
    }
    
	public long getConfigDate(boolean start){
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		if(start){
			cal.set(Calendar.HOUR_OF_DAY, getStartHour());
			cal.set(Calendar.MINUTE, getStartMinute());
		}else{
			cal.set(Calendar.HOUR_OF_DAY, getEndHour());
			cal.set(Calendar.MINUTE, getEndMinute());
		}
		return cal.getTime().getTime();
	}
}
