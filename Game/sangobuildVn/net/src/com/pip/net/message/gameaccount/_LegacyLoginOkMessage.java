package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _LegacyLoginOkMessage extends AbstractMessage{
	
	protected int accountId;
	protected String name;
	protected String key;
	protected String phone;
	protected int modifiedNameTimes;
	protected int iMoney;
	protected int loginErrorTimes;
	protected int[] puchasedCodes;
	
	
	public _LegacyLoginOkMessage(int serial, int accountId,String name, String key,
			String phone, int modifiedNameTimes, int iMoney, int loginErrorTimes,int[] purchasedCodes) {
		super(GameAccountMessageType._LEGACY_LOGIN_OK,serial,true);
		this.accountId = accountId;
		this.name = name;
		this.key = key;
		this.phone = phone;
		this.modifiedNameTimes = modifiedNameTimes;
		this.iMoney = iMoney;
		this.loginErrorTimes = loginErrorTimes;
		this.puchasedCodes = purchasedCodes;
	}


	public String getName() {
		return name;
	}


	public String getKey() {
		return key;
	}


	public int[] getPuchasedCodes() {
		return puchasedCodes;
	}


	public String getPhone() {
		return phone;
	}


	public int getModifiedNameTimes() {
		return modifiedNameTimes;
	}


	public int getIMoney() {
		return iMoney;
	}


	public int getLoginErrorTimes() {
		return loginErrorTimes;
	}


	public int getAccountId() {
		return accountId;
	}
}
