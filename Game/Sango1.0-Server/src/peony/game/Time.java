package peony.game;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class Time {
	
	public static final Logger log = Logger.getLogger(Time.class);
	
	public static int currTime = 0;
	public static long startTime = 0;
	public static int tick = 0;
	public static int day = 0;
	public static int currentDayOfYear;
	public static int currentWeekDay;
	public static int currentHour;
	public static int currentDayOfYear_test;
	public static int currentWeekDay_test = 1;
	public static int currentMin;
	public static Date currDate = null;
	public static long currDateTimes;
	public static final int dayStart = 0; // 每天的开始时间(分钟为单位)
	public static final int dayEnd = 24 * 60; // 每天的结束时间(分钟为单位)
	
	public static final List<DayListener> dayListeners = new ArrayList<DayListener>();
	
	public static void init(){
		currDate = new Date();
		startTime = currDate.getTime();
		currDateTimes = currDate.getTime();
		Calendar cal = Calendar.getInstance();
		day = (cal.get(Calendar.YEAR)<<16)|cal.get(Calendar.DAY_OF_YEAR);
		currentWeekDay = cal.get(Calendar.DAY_OF_WEEK);
		currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		currentHour = cal.get(Calendar.HOUR_OF_DAY);
		currentMin = cal.get(Calendar.MINUTE);
	}
	
	public static void resetDay(){
		Calendar cal = Calendar.getInstance();
		int newDay = (cal.get(Calendar.YEAR)<<16)|cal.get(Calendar.DAY_OF_YEAR);
		currentHour = cal.get(Calendar.HOUR_OF_DAY);
		currentMin = cal.get(Calendar.MINUTE);
		if(newDay!=day){
			day = newDay;
			currentWeekDay = cal.get(Calendar.DAY_OF_WEEK);
			currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
			log.info("[DAYCHANGED]OLD["+day+"]NEW["+newDay+"]");
			for(DayListener l:dayListeners){
				try {
					l.dayChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static long update(){
		currDate = new Date();
		currDateTimes = currDate.getTime();
		long t = currDate.getTime()-startTime;
		long ret = t - currTime;
		if (ret < 0) {
		    startTime += ret;
		    ret = 0;
		    t = currTime;
		}
		currTime = (int)t;
		resetDay();
		return ret;
	}
	
	/**
	 * 把TimeMillis换算成当前系统起始的Int时间
	 * @param time
	 * @return
	 */
	public static int elapseTime(long time){
		return (int)(time - startTime);
	}
	
	public static long elapseTime2(long time){
		return time - startTime;
	}
	
	/**
	 * 把当前系统起始的Int时间换算成TimeMillis
	 * @param t
	 * @return
	 */
	public static long currentTimeMillis(int t){
		return startTime + t;
	}
	
	public static void addDayListener(DayListener l){
		dayListeners.add(l);
	}
	
	/**
	 * 取下一天的最早时间
	 * @param date
	 * @return
	 */
	public static Date getDateNextDay(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static boolean betweenHour(Date date,int begin,int end){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		return hour>=begin&&hour<=end;
//		return begin>=hour&&end<=hour;
	}
	
	/**是否在指定时间段内**/
	public static boolean inTime(int sHour, int sMin, int eHour, int eMin){
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(new Date());
		cal1.set(Calendar.HOUR_OF_DAY, sHour);
		cal1.set(Calendar.MINUTE, sMin);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(new Date());
		cal2.set(Calendar.HOUR_OF_DAY, eHour);
		cal2.set(Calendar.MINUTE, eMin);
		return cal.after(cal1) && cal.before(cal2);
	}
	
	/**
	 * 取上一天的最早时间
	 * @param date
	 * @return
	 */
	public static Date getDateLastDay(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * 取当天的最早时间
	 * @param date
	 * @return
	 */
	public static Date getDateToday(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
