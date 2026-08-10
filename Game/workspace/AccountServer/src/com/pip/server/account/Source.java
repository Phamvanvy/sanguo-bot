package com.pip.server.account;

public class Source implements ISource {

	private String id;
	private String passWord;
	private Status status;
	private int address;
	private int balance;
	
	public Source(String id,String passWord,int address,int balance){
		this.id = id;
		this.passWord = passWord;
		this.address = address;
		this.balance = balance;
		this.status = Status.disconnected;
	}
	
	public String getId() {
		return id;
	}

	public Status getStatus(){
		return status;
    }
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public String getPassWord(){
		return passWord;
	}
	
	public void setPassWord(String passWord){
		this.passWord = passWord;
	}
	
	
	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getAddress(){
		return address;
	}
	
	public void setAddress(int address){
		this.address = address;
	}

}
