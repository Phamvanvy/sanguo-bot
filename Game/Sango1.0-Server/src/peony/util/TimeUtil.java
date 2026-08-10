package peony.util;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
	/**
	 * 是否监控系统执行性能。可通过hotfix关闭。
	 */
	public static boolean monitorPerformace = true;
	/**
	 * 是否监控Player.update的执行性能。
	 */
	public static boolean monitorPlayerUpdatePerformance = false;
	
	/**
	 * 取得从当前时刻开始第一个匹配时间
	 * @param date,当前时间
	 * @param hour,时
	 * @param min,分
	 * @return
	 */
	public static Date getScheduleTime(Date date, int hour, int min){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		cal1.set(Calendar.SECOND, 0);
		if (cal1.before(cal)) {
			cal1.add(Calendar.DAY_OF_YEAR, 1);
		}
		return cal1.getTime();
	}
	
	/**
	 * 取得从当前时刻距离第一个匹配时间的毫秒数
	 * @param date,当前时间
	 * @param hour,时
	 * @param min,分
	 * @return
	 */
	public static long getScheduleTimeMills(Date date, int hour, int min){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		cal1.set(Calendar.SECOND, 0);
		if (cal1.before(cal)) {
			cal1.add(Calendar.DAY_OF_YEAR, 1);
		}
		return (cal1.getTime().getTime()-System.currentTimeMillis());
	}
	
	/**
	 * 取得从当前时刻距离第一个匹配时间的毫秒数
	 * @param date,当前时间
	 * @param hour,时
	 * @param min,分
	 * @return
	 */
	public static long getScheduleTimeMills2(Date date, int hour, int min,int second){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		cal1.set(Calendar.SECOND, second);
		if (cal1.before(cal)) {
			cal1.add(Calendar.DAY_OF_YEAR, 1);
		}
		return (cal1.getTime().getTime()-System.currentTimeMillis());
	}
	
	/**
	 * 取得从当前时刻开始第一个匹配时间
	 * @param date,当前时间
	 * @param weekDay,星期(中国习惯)
	 * @param hour,时
	 * @param min,分
	 * @return
	 */
	public static Date getScheduleTime(Date date, int weekDay, int hour, int min) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date);
		cal1.set(Calendar.DAY_OF_WEEK, ((weekDay+1)==8 ? 1 : (weekDay+1)));
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		cal1.set(Calendar.SECOND, 0);
		if (cal1.before(cal)) {
			cal1.add(Calendar.WEEK_OF_MONTH, 1);
		}
		return cal1.getTime();
	}
	
	/**
	 * 取得从当前时刻距离第一个匹配时间的毫秒数
	 * @param date,当前时间
	 * @param weekDay,星期(中国习惯)
	 * @param hour,时
	 * @param min,分
	 * @return
	 */
	public static long getScheduleTimeMills(Date date, int weekDay, int hour, int min) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date);
		cal1.set(Calendar.DAY_OF_WEEK, ((weekDay+1)==8 ? 1 : (weekDay+1)));
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		cal1.set(Calendar.SECOND, 0);
		if (cal1.before(cal)) {
			cal1.add(Calendar.WEEK_OF_MONTH, 1);
		}
		return cal1.getTime().getTime()-System.currentTimeMillis();
	}
	
	/**
	 * 获取当前Date增减second秒后的Date
	 * @param date, 当前日期
	 * @param second, 秒数
	 * @param add, true为增加、false为减少
	 * @return, Date
	 */
	public static Date getDate(Date date, int second, boolean add){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(add){
			cal.add(Calendar.SECOND, second);
		}else{
			cal.add(Calendar.SECOND, -second);
		}
		return cal.getTime();
	}
	
	/**
	 * 将秒数换算成 时分秒
	 * @param times
	 * @return, arr[时、分、秒]
	 */
	public static int[] getH_M_S(int times){
		int[] arr = new int[3];
		arr[0] = times / 3600;
		arr[1] = times % 3600 / 60;
		arr[2] = times % 3600 % 60;
		return arr;
 	}
	
	/**
	 * 将秒数换算成 时分秒
	 * @param times
	 * @return, arr[时、分、秒]
	 */
	public static String getStringH_M_S(int times){
		int[] leave = getH_M_S(times);
		if (leave[0] > 0) {
			return MessageFormat.format(peony.Messages.STRING_00330, leave[0]);
		} else if (leave[1] > 0) {
			return MessageFormat.format(peony.Messages.STRING_00331, leave[1]);
		} else if (leave[2] > 0) {
			return MessageFormat.format(peony.Messages.STRING_00332, leave[2]);
		} else {
			return peony.Messages.STRING_00333;
		}
	}
	
}
