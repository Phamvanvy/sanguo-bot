package com.pip.itimes.server.world.noahsark;

import java.util.Calendar;
import java.util.Date;

public class NoahsarkPlayer {
	private int id;
	private String name;
	private int totalScore=0;
	private int totalCount;
	private long donateDate;
	public NoahsarkPlayer() {
		super();
		// TODO Auto-generated constructor stub
	}
	public NoahsarkPlayer(int id, String name, int totalScore, int totalCount) {
		super();
		this.id = id;
		this.name = name;
		this.totalScore = totalScore;
		this.totalCount = totalCount;
	}
	public NoahsarkPlayer(int id, String name, int totalScore, int totalCount,
			long donateDate) {
		super();
		this.id = id;
		this.name = name;
		this.totalScore = totalScore;
		this.totalCount = totalCount;
		this.donateDate = donateDate;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(int score) {
		this.totalScore += score;
	}
	public int getTotalCount() {
		return totalCount;
	}
	private static long noahsarkFirstDay = 0;
	public void setTotalCount(int count) {
		
			this.totalCount += count;
	}
	public long getDonateDate() {
		return donateDate;
	}
	public long getNoahsarkFirstDay() {
		return noahsarkFirstDay;
	}
	public void setNoahsarkFirstDay(long noahsarkFirstDay) {
		this.noahsarkFirstDay = noahsarkFirstDay;
	}
	public void setDonateDate(long donateDate) {
		this.donateDate = donateDate;
	}
	public void resetCount(){
		this.totalCount = 0;
	}
	public void resetScore(){
		this.totalScore = 0;
	}
}
