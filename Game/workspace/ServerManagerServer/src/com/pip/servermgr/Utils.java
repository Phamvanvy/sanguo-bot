package com.pip.servermgr;

import java.util.*;
import java.text.*;

public class Utils {
	public static String basePath = "/data2/servercontrol";
	
	public static int daysBetween(Date date1, Date date2) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		if (date1.compareTo(date2) > 0) {
			return -1;
		}
		int ret = 0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date1);
		cal.add(Calendar.DATE, 1);
		while (df.format(cal.getTime()).compareTo(df.format(date2)) <= 0) {
			ret++;
		}
		return ret;
	}
	
	public static int yearsBetween(Date date1, Date date2) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		if (date1.compareTo(date2) > 0) {
			return -1;
		}
		int ret = 0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date1);
		cal.add(Calendar.YEAR, 1);
		while (df.format(cal.getTime()).compareTo(df.format(date2)) <= 0) {
			ret++;
			cal.add(Calendar.YEAR, 1);
		}
		return ret;
	}
	
	public static int str2ip(String str) {
		String[] secs = str.split("\\.");
		int b1 = Integer.parseInt(secs[0]);
		int b2 = Integer.parseInt(secs[1]);
		int b3 = Integer.parseInt(secs[2]);
		int b4 = Integer.parseInt(secs[3]);
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}
	
	public static String ip2str(int ip) {
		int b1 = (ip >> 24) & 0xFF;
		int b2 = (ip >> 16) & 0xFF;
		int b3 = (ip >> 8) & 0xFF;
		int b4 = ip & 0xFF;
		return b1 + "." + b2 + "." + b3 + "." + b4;
	}
}
