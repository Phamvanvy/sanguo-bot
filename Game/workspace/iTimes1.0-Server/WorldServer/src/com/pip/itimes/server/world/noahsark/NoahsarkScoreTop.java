package com.pip.itimes.server.world.noahsark;

public class NoahsarkScoreTop {
    private NoahsarkDonateMaterial first;
    private NoahsarkDonateMaterial second;
    private NoahsarkDonateMaterial third;
    private NoahsarkDonateMaterial base;
    private int scoreCount;
	public NoahsarkScoreTop() {
		super();
		// TODO Auto-generated constructor stub
	}
	public NoahsarkScoreTop(NoahsarkDonateMaterial first,
			NoahsarkDonateMaterial second, NoahsarkDonateMaterial third,
			NoahsarkDonateMaterial base, int scoreCount) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
		this.base = base;
		this.scoreCount = scoreCount;
	}
	public NoahsarkDonateMaterial getFirst() {
		return first;
	}
	public void setFirst(NoahsarkDonateMaterial first) {
		this.first = first;
	}
	public NoahsarkDonateMaterial getSecond() {
		return second;
	}
	public void setSecond(NoahsarkDonateMaterial second) {
		this.second = second;
	}
	public NoahsarkDonateMaterial getThird() {
		return third;
	}
	public void setThird(NoahsarkDonateMaterial third) {
		this.third = third;
	}
	public NoahsarkDonateMaterial getBase() {
		return base;
	}
	public void setBase(NoahsarkDonateMaterial base) {
		this.base = base;
	}
	public int getScoreCount() {
		return scoreCount;
	}
	public void setScoreCount(int scoreCount) {
		this.scoreCount = scoreCount;
	}
    
}
