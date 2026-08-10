package com.pip.itimes.server.world.noahsark;

public class TimesCount {
	private int itemId;
	private int timesId;
	private int counts;
	private long period;
	
	public TimesCount() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TimesCount(int itemId, int timesId, int counts,long period) {
		super();
		this.itemId = itemId;
		this.timesId = timesId;
		this.counts = counts;
		this.period = period;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getTimesId() {
		return timesId;
	}

	public void setTimesId(int timesId) {
		this.timesId = timesId;
	}

	public int getCounts() {
		return counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}
	
	public long getPeriod(){
		return period;
	}
	
	public void setPeriod(long period){
		this.period = period;
	
	}
	
}
