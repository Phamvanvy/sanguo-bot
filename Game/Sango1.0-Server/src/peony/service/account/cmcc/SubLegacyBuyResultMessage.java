package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.LegacyBuyResultMessage;

public class SubLegacyBuyResultMessage extends LegacyBuyResultMessage {

	protected long balance2;
	
	public SubLegacyBuyResultMessage(int serial, boolean success, int balance,
			int cost, String cause, long longBalance, long balance2) {
		super(serial, success, balance, cost, cause, longBalance, balance2);
		this.balance2 = balance2;
	}

	public long getBalance2() {
		return balance2;
	}

	public void setBalance2(long balance2) {
		this.balance2 = balance2;
	}

}
