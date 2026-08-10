package com.pip.itimes.server.stage;

public class MoneyEffect extends Effect{

	private int minMoney;
	private int maxMoney;
	
	public MoneyEffect(int min,int max){
		this.minMoney = min;
		this.maxMoney = max;
	}
	
	public void setminMoney(int min){
		this.minMoney = min;
	}
	
	public int getminMoney(){
		return this.minMoney;
	}
	
	public void setmaxMoney(int max){
		this.minMoney = max;
	}
	
	public int getmaxMoney(){
		return this.maxMoney;
	}
	
	public byte getType() {
		return 90;
	}

}
