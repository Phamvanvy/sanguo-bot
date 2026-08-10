package com.pip.itimes.server.stage;

import java.util.Random;

import com.pip.itimes.server.util.Utils;

/**
 * @author wpjiang
 *	宝石鉴定参数类
 */
public class Diamond{

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	private int itemId;
	private int min;
	private int max;

	public Diamond(int itemId, int min, int max){
		this.itemId = itemId;
		this.min = min;
		this.max = max;
	}
	
	
	public int getDiamondItemId(){
		return itemId;
	}
}