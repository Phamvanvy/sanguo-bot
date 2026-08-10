package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _BuyMessage extends AbstractMessage {

	protected String name;
	protected String key;
	protected int value;
	
	public _BuyMessage(String name,String key,int value){
		super(GameAccountMessageType.BUY);
		this.name = name;
		this.key = key;
		this.value = value;
	}
	
	public _BuyMessage(int serial,String name,String key,int value){
		super(GameAccountMessageType.BUY,serial);
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
