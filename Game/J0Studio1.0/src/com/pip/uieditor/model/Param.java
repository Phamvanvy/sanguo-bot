package com.pip.uieditor.model;

/**
 * 对齐参数，包括一个数值以及一个数值单位，单位可以是像素或者百分比
 * @author Jeffrey
 *
 */
public class Param {

	public enum Unit{PIXEL, PERCENT};
	
	public int value;
	public Unit unit;
	
	public Param(int value, Unit unit) {
		this.value = value;
		this.unit = unit;
	}

}
