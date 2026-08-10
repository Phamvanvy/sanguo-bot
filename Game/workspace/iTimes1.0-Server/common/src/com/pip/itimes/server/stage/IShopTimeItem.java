package com.pip.itimes.server.stage;

import java.util.Calendar;
import java.util.Date;

public class IShopTimeItem {
	public static void goodsList(long now){
		IStoreItem[] iStoreItem= IStoreGroups.getGroup("限时抢购专区").getItems();
		if(iStoreItem.length != 0){
			for(int i = 0; i < iStoreItem.length; i ++){
				for(int j = 0; j < iStoreItem[i].times.size(); j++){
					String start = iStoreItem[i].times.get(j).getStart();
					String end = iStoreItem[i].times.get(j).getEnd();
					if(now < getDate(start).getTimeInMillis()||now > getDate(end).getTimeInMillis()){
						iStoreItem[i].times.get(j).setCount(0);
					}
				}
			}
		}
	}
	public static Calendar getDate(String hourStr){
		String[] date = hourStr.split(":");
		int hour = Integer.parseInt(date[0]);
		int minute = Integer.parseInt(date[1]);
		int second = Integer.parseInt(date[2]);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(year, month, day, hour, minute, second);
		return calendar;
	}
}
