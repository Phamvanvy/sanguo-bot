package com.pip.gameaccount.qq;

public class QQBilling {
	
	protected String uin;
	protected String linkId;
	protected int goodId;
	protected int count;
	protected String sourceId;
	protected long time;
	
	public QQBilling(String uin,String linkId,int goodId,int count,String sourceId,long time){
		this.uin = uin;
		this.linkId = linkId;
		this.goodId = goodId;
		this.count = count;
		this.sourceId = sourceId;
		this.time = time;
	}
	
	public QQBilling(String linkId, String data) {
	    this.linkId = linkId;
	    String[] secs = data.split("\n");
	    uin = secs[0];
	    goodId = Integer.parseInt(secs[1]);
	    count = Integer.parseInt(secs[2]);
	    sourceId = secs[3];
	    time = Long.parseLong(secs[4]);
	}
	
	public String getSaveData() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(uin);
	    sb.append("\n");
	    sb.append(goodId);
	    sb.append("\n");
        sb.append(count);
        sb.append("\n");
        sb.append(sourceId);
        sb.append("\n");
        sb.append(time);
        return sb.toString();
	}
	
	public String getSourceId() {
		return sourceId;
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
	
	public long getTime(){
		return time;
	}
}
