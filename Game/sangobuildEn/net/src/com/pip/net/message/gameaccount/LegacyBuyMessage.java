package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyBuyMessage extends AbstractMessage {
	
	protected String name;
	protected String key;
	protected int value;
	
	public LegacyBuyMessage(int serial,String name,String key,int value){
		super(GameAccountMessageType.LEGACY_BUY,serial);
		this.name = name;
		this.key = key;
		this.value = value;
	}

	public LegacyBuyMessage(String name,String key,int value){
		super(GameAccountMessageType.LEGACY_BUY);
		this.name = name;
		this.key = key;
		this.value = value;
	}
	
	public String getName(){
		return name;
	}
	
	public int getValue(){
		return value;
	}

	public String getKey() {
		return key;
	}
	
}
