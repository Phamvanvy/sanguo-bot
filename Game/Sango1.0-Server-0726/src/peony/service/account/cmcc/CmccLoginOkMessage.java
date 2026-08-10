package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.LegacyLoginOkMessage;

public class CmccLoginOkMessage extends LegacyLoginOkMessage {

	protected String cityName;

	public CmccLoginOkMessage(int serial, int accountId, String name,
			String key, String phone, int modifiedNameTimes, int iMoney,
			boolean isMonth, boolean isSubscribe, int loginErrorTimes,
			int[] purchasedCodes, String cityName, long longBalance) {
		super(serial, accountId, name, key, phone, modifiedNameTimes,
				iMoney, isMonth, isSubscribe, loginErrorTimes, purchasedCodes, longBalance);
		this.cityName = cityName;
	}

	public String getCityName() {
		return cityName;
	}

}
