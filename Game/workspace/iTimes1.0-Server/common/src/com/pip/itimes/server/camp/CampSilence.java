package com.pip.itimes.server.camp;

public class CampSilence {
	private long startTime;
	private long endTime;
	public CampSilence(long startTime, long endTime){
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public long getStartTime(){
		return startTime;
	}
	public long getEndTime(){
		return endTime;
	}
}
