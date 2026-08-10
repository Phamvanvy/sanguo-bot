package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwelfthLunarConfig {
	public static TwelfthLunarActivityConfig[] twelfthLunarActivityConfig;
	
	// 活动开始结束的时间
    public static int startYear;
    public static int startMonth;
    public static int startDay;
    public static int endYear;
    public static int endMonth;
    public static int endDay;
    public static int endHour;
    public static int endMinute;
    public static int segment;
    // 活动排行榜开始结束的时间
    public static int topStartYear;
    public static int topStartMonth;
    public static int topStartDay;
    public static int topEndYear;
    public static int topEndMonth;
    public static int topEndDay;
    
    /**
	 * 剩余腊八粥的个数
	 */
    public static int gruelCount;
    
    /**
     * 排行榜显示玩家的个数
     */
    public static int topCount;
    /**
     * 活动中表扬榜上玩家的个数，和结束后发奖的个数
     */
    public static int topPraise;
    /**
     * 捐献物品的ID
     */
    public static int donateItemId;
    /**
     * 捐献物品获得的奖励ID
     */
    public static int donateGiftId;
    /**
     * 消费尺度
     */
    public static int donateConsumer;
    /**
     * 达到消费尺度后赠送捐献物品的个数
     */
    public static int donateBeanCount;
    /**
     * 活动结束后前topPraise发送奖品的MAP
     */
    public static Map<Integer, Integer> twelfthLunarConfigMap = new HashMap<Integer, Integer>();
    /**
     * 排行榜List（经过排序的）
     */
    public static List<TwelfthLunarShowInfo> topList = new ArrayList<TwelfthLunarShowInfo>();
    /**
     * 所有捐献玩家的MAP
     */
    public static Map<Integer, TwelfthLunarShowInfo> playerDonateMap = new HashMap<Integer, TwelfthLunarShowInfo>();
    
    /**
	 * 活动开始的日期
	 */
	public static Date startDate;
	/**
	 * 活动截止日期
	 */
	public static Date endDate;
	/**
	 * 排行榜开始的日期
	 */
	public static Date topStartDate;
	/**
	 * 排行榜截止的日期
	 */
	public static Date topEndDate;
    
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
	 * 设置排行榜开始日期
	 */
	public static void setTopStartDate (Date date) {
		topStartDate = date;
	}
	
	/**
	 * 设置排行榜结束日期
	 */
	public static void setTopEndDate (Date date) {
		topEndDate = date;
	}
	
	/**
	 * 设置活动,排行榜日期
	 */
	public static void resetActTime() {
		setEventStartDate(getConfigDate(true, startYear, startMonth, startDay, 0, 0, 0, 0, 0));
		setEventEndDate(getConfigDate(false, 0, 0, 0, endYear, endMonth, endDay, endHour, endMinute));
		setTopStartDate(getConfigDate(true, topStartYear, topStartMonth, topStartDay, 0, 0, 0, 0, 0));
		setTopEndDate(getConfigDate(false, 0, 0, 0, topEndYear, topEndMonth, topEndDay, 0, 0));
    }
	
	public static Date getConfigDate (boolean start, int startYear,
			int startMonth, int startDay, int endYear, int endMonth, int endDay, int endHour, int endMinute) {
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
			cal.set(Calendar.MILLISECOND, 0);
		} else {
			cal.set(Calendar.YEAR, endYear);
			cal.set(Calendar.MONTH, endMonth - 1);
			cal.set(Calendar.DAY_OF_MONTH, endDay);
			cal.set(Calendar.HOUR_OF_DAY, endHour);
			cal.set(Calendar.MINUTE, endMinute);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}
		return cal.getTime();
	}
	
	/**
	 * 重置时间 0点的时候调用
	 */
	public static void resetTime() {
		for (int i = 0; i < segment; i++) {
			twelfthLunarActivityConfig[i].resetActivityTime();
		}
	}
	
	public static void setTwelfthLunarConfig (int key, int value) {
		TwelfthLunarConfig.twelfthLunarConfigMap.put(key, value);
	}
	
	public static void getTwelfthLunarConfigValue (int key) {
		twelfthLunarConfigMap.get(key);
	}
}
