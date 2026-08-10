package com.pip.server.account;

public interface ISource {
	
	public static enum Status{connected,disconnected,valid};
	
	public String getId();
	public String getPassWord();
	public void setPassWord(String password);
	public Status getStatus();
	public void setStatus(Status status);
	public int getAddress();
	public void setAddress(int address);
	public int getBalance();
	public void setBalance(int balance);
}
