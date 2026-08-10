package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyBuyMessage extends AbstractMessage {
	
	protected String name;
	protected String key;
	protected int value;
	protected boolean trustOnly;
	
	public LegacyBuyMessage(int serial,String name,String key,int value,boolean trustOnly){
		super(GameAccountMessageType.LEGACY_BUY,serial);
		this.name = name;
		this.key = key;
		this.value = value;
		this.trustOnly = trustOnly;
	}

	public LegacyBuyMessage(String name,String key,int value,boolean trustOnly){
		super(GameAccountMessageType.LEGACY_BUY);
		this.name = name;
		this.key = key;
		this.value = value;
		this.trustOnly = trustOnly;
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
	
	public boolean isTrustOnly() {
		return trustOnly;
	}
}
