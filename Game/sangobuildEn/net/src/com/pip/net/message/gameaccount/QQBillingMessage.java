package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class QQBillingMessage extends AbstractMessage {
	
	protected String uin;
	protected String linkId;
	protected int goodId;
	protected int count;
	
	public QQBillingMessage(int serial,String uin,String linkId,int goodId,int count){
		super(GameAccountMessageType.QQ_BILLING,serial);
		this.uin = uin;
		this.linkId = linkId;
		this.goodId = goodId;
		this.count = count;
	}
	
	public QQBillingMessage(String uin,String linkId,int goodId,int count){
		super(GameAccountMessageType.QQ_BILLING);
		this.uin = uin;
		this.linkId = linkId;
		this.goodId = goodId;
		this.count = count;
	}

	public String getUin() {
		return uin;
	}

	public String getLinkId() {
		return linkId;
	}

	public int getGoodId() {
		return goodId;
	}

	public int getCount() {
		return count;
	}
}
