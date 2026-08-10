package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _BuyMessage extends AbstractMessage {

	protected String name;
	protected String key;
	protected int value;
	protected boolean trustOnly;
	
	public _BuyMessage(String name,String key,int value,boolean trustOnly){
		super(GameAccountMessageType.BUY);
		this.name = name;
		this.key = key;
		this.value = value;
		this.trustOnly = trustOnly;
	}
	
	public _BuyMessage(int serial,String name,String key,int value,boolean trustOnly){
		super(GameAccountMessageType.BUY,serial);
		this.name = name;
		this.key = key;
		this.value = value;
		this.trustOnly = trustOnly;
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
	
	public boolean isTrustOnly() {
		return trustOnly;
	}
}
