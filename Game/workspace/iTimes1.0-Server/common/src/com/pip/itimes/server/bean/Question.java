package com.pip.itimes.server.bean;

public class Question {

	private int id;
	private int questionId;
	private int succeed;
	private int fail;
	
	public Question() {
		
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	
	public int getQuestionId() {
		return questionId;
	}
	
	public void setSucceed(int succeed) {
		this.succeed = succeed;
	}
	
	public int getSucceed() {
		return succeed;
	}
	
	public void setFail(int fail) {
		this.fail = fail;
	}
	
	public int getFail() {
		return fail;
	}
}
