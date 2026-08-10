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
	
	public static Date currDate = null;
	
	public static final List<DayListener> dayListeners = new ArrayList<DayListener>();
	
	public static void init(){
		currDate = new Date();
		startTime = currDate.getTime();
		Calendar cal = Calendar.getInstance();
		day = (cal.get(Calendar.YEAR)<<16)|cal.get(Calendar.DAY_OF_YEAR);
	}
	
	public static void resetDay(){
		Calendar cal = Calendar.getInstance();
		int newDay = (cal.get(Calendar.YEAR)<<16)|cal.get(Calendar.DAY_OF_YEAR);
		if(newDay!=day){
			day = newDay;
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
}
