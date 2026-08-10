package com.pip.uieditor.layout;


/**
 * 记录了控件的对齐信息
 * @author Jeffrey
 *
 */
public class Align {
	
	public static final int TOP_MIDDLE = 0;
	public static final int MIDDLE_BOTTOM = 1;
	public static final int TOP_BOTTOM = 2;
	
	public static final int LEFT_CENTER = 0;
	public static final int CENTER_RIGHT = 1;
	public static final int LEFT_RIGHT = 2;
	
	public static final int UNIT_PIXEL = 0;
	public static final int UNIT_PERCENT = 1;
	
	public int align;
	public int param1, param2;
	public int unit1, unit2;
	
	public Align(int align, int param1, int unit1, int param2, int unit2) {
		super();
		this.align = align;
		this.param1 = param1;
		this.param2 = param2;
		this.unit1 = unit1;
		this.unit2 = unit2;
	}

	public static int getPixel(int value, int unit, int total) {
		if(unit == UNIT_PIXEL)
			return value;
		else if(unit == UNIT_PERCENT) {
			return total * value / 1000; //精度保留小数点后3位
		} else
			throw new IllegalArgumentException();
	}
	
	public int getPixel1(int total) {
		return getPixel(param1, unit1, total);
	}
	
	public int getPixel2(int total) {
		return getPixel(param2, unit2, total);
	}
	
}
