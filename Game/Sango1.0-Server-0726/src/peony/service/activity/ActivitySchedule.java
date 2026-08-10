package peony.service.activity;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.pip.util.Utils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

/**
 * 活动的时间安排。
 * @author lighthu
 */
public class ActivitySchedule {
	/**
	 * 开始日期/时间。
	 */
	public Date startTime = new Date();
	/**
	 * 结束日期/时间。
	 */
	public Date stopTime = new Date();
	/**
	 * 一周中生效的天。null表示不限制。
	 */
	public int[] weekdays;
	/**
	 * 一天中生效的时间段。单位是分，每2个数字表示起始时间和结束时间（包含）。
	 * null表示不限制。
	 */
	public int[] timePeriods;
	
	private Calendar calendar = Calendar.getInstance();
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 深度复制。
	 * @return
	 */
	@Override
	public ActivitySchedule clone() {
		ActivitySchedule ret = new ActivitySchedule();
		if (startTime != null) {
			ret.startTime = new Date(startTime.getTime());
		}
		if (stopTime != null) {
			ret.stopTime = new Date(stopTime.getTime());
		}
		if (weekdays != null) {
			ret.weekdays = weekdays.clone();
		}
		if (timePeriods != null) {
			ret.timePeriods = timePeriods.clone();
		}
		return ret;
	}
	
	/**
	 * 从存储的字符串中恢复。格式为：
	 * starttime=xxx
	 * stoptime=xxx
	 * weekdays=1,2,...   可选
	 * timeperiods=1,2,3,4...    可选
	 * @param data
	 */
	public void parse(String data) throws Exception {
		String[] lines = data.split(";");
		startTime = new Date();
		stopTime = new Date();
		weekdays = null;
		timePeriods = null;
		for (String line : lines) {
		    int pos = line.indexOf('=');
		    if (pos == -1) {
		        continue;
		    }
		    String varName = line.substring(0, pos);
		    String varValue = line.substring(pos + 1);
		    if ("starttime".equals(varName)) {
		    	startTime = df.parse(translateTimeString(varValue));
		    } else if ("stoptime".equals(varName)) {
		    	stopTime = df.parse(translateTimeString(varValue));
		    } else if ("weekdays".equals(varName)) {
		    	weekdays = Utils.stringToIntArray(varValue, ',');
		    } else if ("timeperiods".equals(varName)) {
		    	timePeriods = Utils.stringToIntArray(varValue, ',');
		    }
		}
	}
	
	/*
	 * 转化时间字符串格式，允许在时间字符串中使用${today+n}来表示日期。
	 */
	private String translateTimeString(String source) {
		StringBuilder out = new StringBuilder();
		int start = 0;
		while (start < source.length()) {
			int next = source.indexOf("${", start);
			if (next == -1) {
				out.append(source.substring(start));
				break;
			} else {
				int next2 = source.indexOf("}", next);
				if (next2 == -1) {
					out.append(source.substring(start));
					break;
				}
				out.append(translateTimeToken(source.substring(next + 2, next2)));
				start = next2 + 1;
			}
		}
		return out.toString();
	}
	
	private String translateTimeToken(String token) {
		Calendar cal;
		if (token.equals("today")) {
			cal = Calendar.getInstance();
			cal.setTime(new Date());
		} else if (token.startsWith("today+")) {
			cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DATE, Integer.parseInt(token.substring(6)));
		} else {
			return token;
		}
		return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
	}
	
	/**
	 * 转换为存储格式。
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("starttime=");
		buf.append(df.format(startTime));
		buf.append(";");
		buf.append("stoptime=");
		buf.append(df.format(stopTime));
		buf.append(";");
		if (weekdays != null) {
			buf.append("weekdays=");
			buf.append(Utils.intArrayToString(weekdays, ','));
			buf.append(";");
		}
		if (timePeriods != null) {
			buf.append("timeperiods=");
			buf.append(Utils.intArrayToString(timePeriods, ','));
			buf.append(";");
		}
		if (buf.charAt(buf.length() - 1) == ';') {
			buf.setLength(buf.length() - 1);
		}
		return buf.toString();
	}
	
	/**
	 * 判断两个列表是否完全相同。
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ActivitySchedule)) {
			return false;
		}
		ActivitySchedule oo = (ActivitySchedule)o;
		if (!startTime.equals(oo.startTime)) {
			return false;
		}
		if (!stopTime.equals(oo.stopTime)) {
			return false;
		}
		if (!Arrays.equals(weekdays, oo.weekdays)) {
			return false;
		}
		if (!Arrays.equals(timePeriods, oo.timePeriods)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 判断当前时间是否在活动有效时间段内。
	 */
	public boolean in() {
		long now = System.currentTimeMillis();
		if (now < startTime.getTime() || now >= stopTime.getTime()) {
			return false;
		}
		calendar.setTimeInMillis(System.currentTimeMillis());
		if (weekdays != null) {
			int day = calendar.get(Calendar.DAY_OF_WEEK);
			// Calendar的周内日编码是1-7（周日是1），我们这里采用1-7（周一是1），所以要转换一下
			day--;
			if (day == 0) {
				day = 7;
			}
			boolean match = false;
			for (int i : weekdays) {
				if (day == i) {
					match = true;
					break;
				}
			}
			if (!match) {
				return false;
			}
		}
		if (timePeriods != null) {
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			boolean match = false;
			for (int i = 0; i < timePeriods.length; i += 2) {
				if (hour >= timePeriods[i] && hour < timePeriods[i + 1]) {
					match = true;
					break;
				}
			}
			if (!match) {
				return false;
			}
		}
		return true;
	}
}
