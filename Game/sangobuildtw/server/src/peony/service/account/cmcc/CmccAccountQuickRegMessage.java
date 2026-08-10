package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.LegacyQuickRegMessage;

public class CmccAccountQuickRegMessage extends LegacyQuickRegMessage {
	
	protected String cmccUserId;
	protected String cmccUserKey;
	

	public CmccAccountQuickRegMessage(String phone, String version,
			String model, String serviceId, String realPhone,String cmccUserId,String cmccUserKey) {
		super(phone, version, model, serviceId, realPhone);
		this.cmccUserId = cmccUserId;
		this.cmccUserKey = cmccUserKey;
	}

	public CmccAccountQuickRegMessage(String phone, String version,
			String model, String serviceId) {
		super(phone, version, model, serviceId);
	}
	
	public String getCmccUserId(){
		return cmccUserId;
	}
	
	public String getCmccUserKey(){
		return cmccUserKey;
	}
}
