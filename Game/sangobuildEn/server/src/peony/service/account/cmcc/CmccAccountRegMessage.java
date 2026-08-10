package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.AccountRegMessage;

public class CmccAccountRegMessage extends AccountRegMessage {
	
	protected String cmccUserId,cmccUserKey;

	public CmccAccountRegMessage(String name, String phone, String recommend,
			int recommendId, String model, String service, String version,
			String realPhone,String initPass, String cmccUserId, String cmccUserKey) {
		super(name, phone, recommend, recommendId, model, service, version, realPhone, initPass);
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
