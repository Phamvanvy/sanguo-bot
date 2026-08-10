package com.pip.gameaccount.util;

import java.util.Calendar;

public class Util {
	public static final int MAX_MONTHFEE = 576000;
	
	public static boolean isMonth(int monthFee){
		return monthFee>=MAX_MONTHFEE;
	}
	
    public static boolean inLaterMonth(java.util.Date checkDate, java.util.Date now) {
        if (checkDate == null) {
            return false;
        }
        Calendar current = Calendar.getInstance();
        Calendar last = Calendar.getInstance();

        current.setTime(now);
        last.setTime(checkDate);
        int m1 = last.get(Calendar.YEAR) * 100 + last.get(Calendar.MONTH);
        int m2 = current.get(Calendar.YEAR) * 100 + current.get(Calendar.MONTH);
        return m1 >= m2;
    }
    
    
    public static void main(String[] args){
    	
    }
 
    
}
