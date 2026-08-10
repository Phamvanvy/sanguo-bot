package com.pip.itimes.server.world.unline;

import java.util.Calendar;
import java.util.Date;

public class UnlineExpNew {
	private int startYear;
	private int startMonth;
	private int startDay;
	private int startHour;
	private int startMinute;
	private long startTime;
	
	private int endYear;
	private int endMonth;
	private int endDay;
	private int endHour;
	private int endMinute;
	private long endTime;
	
	private String message;
	
	public void setMessage(String message){
		this.message = message;
	}
	public String getMessage(){
		return message;
	}
	
	public void setStartYear(int startYear){
		this.startYear = startYear;
	}
	public int getStartYear(){
		return startYear;
	}
	public void setStartMonth(int startMonth){
		this.startMonth = startMonth;
	}
	public int getStartMonth(){
		return startMonth;
	}
	public void setStartDay(int startDay){
		this.startDay = startDay;
	}
	public int getStartDay(){
		return startDay;
	}
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
	public void setEndYear(int endYear){
		this.endYear = endYear;
	}
	public int getEndYear(){
		return endYear;
	}
	public void setEndMonth(int endMonth){
		this.endMonth = endMonth;
	}
	public int getEndMonth(){
		return endMonth;
	}
	public void setEndDay(int endDay){
		this.endDay = endDay;
	}
	public int getEndDay(){
		return endDay;
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
	
    /**
     * 重置时间 一般在每天0点时进行重置 或是启动的时候重置
     */
    public void resetTime(){
    	startTime = getConfigDate(true);
    	endTime = getConfigDate(false);
    }
    
    public long getStartTime(){
    	return startTime;
    }
    
    public long getEndTime(){
    	return endTime;
    }
    
	public long getConfigDate(boolean start){
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		if(start){
			cal.set(Calendar.YEAR, startYear);
			cal.set(Calendar.MONTH, startMonth);
			cal.set(Calendar.DAY_OF_MONTH, startDay);
			cal.set(Calendar.HOUR_OF_DAY, startHour);
			cal.set(Calendar.MINUTE, startMinute);
		}else{
			cal.set(Calendar.YEAR, endYear);
			cal.set(Calendar.MONTH, endMonth);
			cal.set(Calendar.DAY_OF_MONTH, endDay);
			cal.set(Calendar.HOUR_OF_DAY, endHour);
			cal.set(Calendar.MINUTE, endMinute);
		}
		return cal.getTime().getTime();
	}
}
