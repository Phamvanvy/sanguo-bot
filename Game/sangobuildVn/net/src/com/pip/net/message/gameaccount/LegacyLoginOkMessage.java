package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyLoginOkMessage extends AbstractMessage {

	protected int accountId;
	protected String name;
	protected String key;
	protected String phone;
	protected int modifiedNameTimes;
	protected int iMoney;
	protected boolean isMonth;
	protected boolean isSubscribe;
	protected int loginErrorTimes;
	protected int[] purchasedCodes;
	
	public LegacyLoginOkMessage(int serial, int accountId, String name,
			String key, String phone, int modifiedNameTimes, int iMoney,
			boolean isMonth, boolean isSubscribe, int loginErrorTimes,int[] purchasedCodes) {
		super(GameAccountMessageType.LEGACY_LOGIN_OK, serial, true);
		this.accountId = accountId;
		this.name = name;
		this.key = key;
		this.phone = phone;
		this.modifiedNameTimes = modifiedNameTimes;
		this.iMoney = iMoney;
		this.isMonth = isMonth;
		this.isSubscribe = isSubscribe;
		this.loginErrorTimes = loginErrorTimes;
		this.purchasedCodes = purchasedCodes;
	}

	public int getAccountId() {
		return accountId;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
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

	public boolean isMonth() {
		return isMonth;
	}

	public boolean isSubscribe() {
		return isSubscribe;
	}

	public int getLoginErrorTimes() {
		return loginErrorTimes;
	}
	
	public int[] getPurchasedCodes(){
		return purchasedCodes;
	}

}
