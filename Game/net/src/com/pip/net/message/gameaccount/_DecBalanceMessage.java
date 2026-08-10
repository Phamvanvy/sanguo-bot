package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _DecBalanceMessage extends AbstractMessage {
	protected String name;
	protected String key;
	protected int value;
	
	public _DecBalanceMessage(String name,String key,int value){
		super(GameAccountMessageType.DEC_BALANCE);
		this.name = name;
		this.key = key;
		this.value = value;
	}
	
	public _DecBalanceMessage(int serial,String name,String key,int value){
		super(GameAccountMessageType.DEC_BALANCE,serial);
		this.name = name;
		this.key = key;
		this.value = value;		
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	public int getValue() {
		return value;
	}
}
