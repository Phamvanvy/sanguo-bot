package peony.util;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

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
		StringBuffer sb = new StringBuffer();
		if(leave[0]>0)
			sb.append(leave[0]+"Thời");
		if(leave[1]>0)
			sb.append(leave[1]+"Phút");
		if(leave[2]>0)
			sb.append(leave[2]+"Giây");
		if(sb.length()==0)
			return "秒\nGiây";
		return sb.toString();
	}
	
}
