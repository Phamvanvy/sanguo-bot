package com.pip.uieditor.util;

public class NumberUtil {
	
	public static int[] parseInts(String s) {
		String[] ss = s.split(",");
		if(ss.length == 0)
			return new int[0];
		int[] ret = new int[ss.length];
		for(int i = 0; i < ss.length; i++) {
			try {
				ret[i] = Integer.parseInt(ss[i]);
			} catch (NumberFormatException e) {
				return new int[0];
			}
		}
		return ret;
	}
}
