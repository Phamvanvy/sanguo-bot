package com.pip.gameaccount.request;

import com.pip.net.ISession;
import com.pip.net.SessionRequest;

public class LegacyFeeRequest extends SessionRequest {
	
	protected String name;
	protected String key;
	protected int value;
	protected int balance;
	protected int accountId;

	public LegacyFeeRequest(int id,String serverId,String name,String key,int accountId,int value,int balance){
		super(id,RequestType.LEGACY_FEE,serverId);
		this.name = name;
		this.key = key;
		this.value = value;
		this.accountId = accountId;
		this.balance = balance;
	}
	
	public int getAccountId(){
		return accountId;
	}
	
	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	public int getValue() {
		return value;
	}
	
	public int getBalance(){
		return balance;
	}
}
