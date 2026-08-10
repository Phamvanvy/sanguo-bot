package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;

public class CmccChargeOkMessage extends AbstractMessage {
	
	protected int balance;
	protected String msg;

	public CmccChargeOkMessage(int serial,int balance,String msg) {
		super((short)510, serial);
		this.balance = balance;
		this.msg = msg;
	}

	public CmccChargeOkMessage() {
		super((short)510);
	}

}
