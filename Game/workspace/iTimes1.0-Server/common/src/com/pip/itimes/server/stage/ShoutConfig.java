package com.pip.itimes.server.stage;

import java.util.Calendar;
import java.util.Date;

public class ShoutConfig {
    public static ShoutActivityConfig[] shoutActivityConfig;
    public static int startYear;
    public static int startMonth;
    public static int startDay;
    public static int endYear;
    public static int endMonth;
    public static int endDay;
    public static int segment;
    /**
	 * 活动开始的日期
	 */
	public static Date startDate;
	/**
	 * 活动截止日期
	 */
	public static Date endDate;
    
    /**
	 * 设置活动开始日期
	 * @param date
	 */
	public static void setEventStartDate (Date date) {
		startDate = date;
	}
	
	/**
	 * 设置活动结束日期
	 * @param date
	 */
	public static void setEventEndDate (Date date) {
		endDate = date;
	}
	
	/**
	 * 设置活动日期
	 */
	public static void resetActTime() {
		setEventStartDate(getConfigDate(true));
		setEventEndDate(getConfigDate(false));
    }
	
	public static Date getConfigDate (boolean start) {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (start) {
			cal.set(Calendar.YEAR, startYear);
			cal.set(Calendar.MONTH, startMonth - 1);
			cal.set(Calendar.DAY_OF_MONTH, startDay);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		} else {
			cal.set(Calendar.YEAR, endYear);
			cal.set(Calendar.MONTH, endMonth - 1);
			cal.set(Calendar.DAY_OF_MONTH, endDay);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
		}
		return cal.getTime();
	}
	
	/**
	 * 重置时间 0点的时候调用
	 */
	public static void resetTime(){
		for(int i=0; i < segment; i++){
			shoutActivityConfig[i].resetActivityTime();
		}
	}
}
