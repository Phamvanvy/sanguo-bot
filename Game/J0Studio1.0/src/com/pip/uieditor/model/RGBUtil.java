package com.pip.uieditor.model;

import org.eclipse.swt.graphics.RGB;

public class RGBUtil {
	
	public static RGB intToRGB(int value) {
		return new RGB((value >> 16)&0xFF, (value >> 8) & 0xFF, value & 0xFF);
	}
	
	
	public static int RGBToInt(RGB rgb) {
		int color = 0;
		color |= (rgb.red << 16);
		color |= (rgb.green<< 8);
		color |= rgb.blue;
		return color;
	}
	
	public static int ARGBToInt(ARGB argb) {
		return argb.alpha << 24 | argb.red << 16 | argb.green << 8 | argb.blue;
	}
	
}
