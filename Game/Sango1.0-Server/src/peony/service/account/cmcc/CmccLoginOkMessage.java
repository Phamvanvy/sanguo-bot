package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.LegacyLoginOkMessage;

public class CmccLoginOkMessage extends LegacyLoginOkMessage {

	protected String cityName;
	protected long balance2;
	protected String attr;

	public CmccLoginOkMessage(int serial, int accountId, String name,
			String key, String phone, int modifiedNameTimes, int iMoney,
			boolean isMonth, boolean isSubscribe, int loginErrorTimes,
			int[] purchasedCodes, String cityName, long longBalance, String attr, long bBalance) {
		super(serial, accountId, name, key, phone, modifiedNameTimes,
				iMoney, isMonth, isSubscribe, loginErrorTimes, purchasedCodes, longBalance,bBalance);
		this.cityName = cityName;
		this.attr = attr;
	}

	public String getCityName() {
		return cityName;
	}

	public long getBalance2() {
		return balance2;
	}

	public void setBalance2(long balance2) {
		this.balance2 = balance2;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getAttr() {
		return attr;
	}
	
	public void setAttr(String value) {
		attr = value;
	}
}
