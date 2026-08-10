package com.pip.gameaccount.qq;

import com.pip.net.message.AbstractMessage;

public class QQBuyMessage extends AbstractMessage {
	
	protected String linkId;
	protected String bId;
	protected String uin;
	protected String objectId;
	protected int count;
	protected int time;
	
	public QQBuyMessage(short cmd,String linkId,String bId,String uin,String objectId,int count,int time){
		super(cmd);
		this.linkId = linkId;
		this.bId = bId;
		this.uin = uin;
		this.objectId = objectId;
		this.count = count;
		this.time = time;
	}

	public String getLinkId() {
		return linkId;
	}

	public String getBId() {
		return bId;
	}

	public String getUin() {
		return uin;
	}

	public String getObjectId() {
		return objectId;
	}

	public int getCount() {
		return count;
	}

	public int getTime() {
		return time;
	}
	
	@Override
	public String toString(){
		return "QQBuyMessage";
	}
}
