package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.LegacyLoginMessage;

public class CmccAccountLoginMessage extends LegacyLoginMessage {

	protected String cmccUserId, cmccUserKey;

	public CmccAccountLoginMessage(String name, String password, String phone, String cmccUserId, String cmccUserKey) {
		super(name, password, phone);
		this.cmccUserId = cmccUserId;
		this.cmccUserKey = cmccUserKey;
	}


	public String getCmccUserId(){
		return this.cmccUserId;
	}
	
	public String getCmccUserKey(){
		return this.cmccUserKey;
	}
}
