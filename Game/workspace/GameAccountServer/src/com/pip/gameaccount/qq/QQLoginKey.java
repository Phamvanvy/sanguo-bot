package com.pip.gameaccount.qq;

public class QQLoginKey {
	
	protected String uin;
	protected String key;
	protected int time;
	
	public QQLoginKey(String uin,String key,int time){
		this.uin = uin;
		this.key = key;
		this.time = time;
	}

	public String getUin() {
		return uin;
	}

	public String getKey() {
		return key;
	}

	public int getTime() {
		return time;
	}

	
}
