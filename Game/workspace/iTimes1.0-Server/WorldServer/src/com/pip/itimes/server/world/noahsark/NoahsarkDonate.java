package com.pip.itimes.server.world.noahsark;


public class NoahsarkDonate {
	private int year;
	private int month;
	private int day;
	private NoahsarkDonateMaterial[] material;
	private NoahsarkDonateMaterial award;
	private int score;
	private StringBuffer message[];
	public NoahsarkDonate() {
		super();
		// TODO Auto-generated constructor stub
	}
	public NoahsarkDonate(int year,int month, int day,
			NoahsarkDonateMaterial[] material, NoahsarkDonateMaterial award,
			int score,StringBuffer[] message) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
		this.material = material;
		this.award = award;
		this.score = score;
		this.message = message;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public NoahsarkDonateMaterial[] getMaterial() {
		return material;
	}
	public void setMaterial(NoahsarkDonateMaterial[] material) {
		this.material = material;
	}
	public NoahsarkDonateMaterial getAward() {
		return award;
	}
	public void setAward(NoahsarkDonateMaterial award) {
		this.award = award;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public StringBuffer[] getMessage() {
		return message;
	}
	public void setMessage(StringBuffer[] message) {
		this.message = message;
	}
	
}
