package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.LegacyBuy1Message;

public class CmccIBuyMessage extends LegacyBuy1Message{
	
	protected String consumeCode;
	protected String version;
	protected String cmccUserId;
	protected int count;
	
	public CmccIBuyMessage(int accountId, String key, int value, String consumeCode, String version, String cmccUserId, int count) {
		super(accountId, key, value);
		this.consumeCode = consumeCode;
		this.version = version;
		this.cmccUserId = cmccUserId;
		this.count = count;
	}

	public String getConsumeCode(){
		return consumeCode;
	}
	
	public String getVersion(){
		return version;
	}
	
	public String getCmccUserId(){
		return cmccUserId;
	}
	
	public int getCount(){
		return count;
	}
}
