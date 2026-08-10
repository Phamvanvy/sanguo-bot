package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;
import com.pip.net.message.gameaccount.GameAccountMessageType;

public class CmccChargeUpMessage extends AbstractMessage {
	
	protected String cmccUserId, cmccUserKey;
	protected int amount;
	
	public CmccChargeUpMessage(String cmccUserId, String cmccUserKey, int amount) {
		super(GameAccountMessageType.LEGACY_CHARGEUP);
		this.cmccUserId = cmccUserId;
		this.cmccUserKey = cmccUserKey;
		this.amount = amount;
	}
	
	public String getCmccUserId(){
		return this.cmccUserId;
	}
	
	public String getCmccUserKey(){
		return this.cmccUserKey;
	}
	
	public int getAmount(){
		return this.amount;
	}

}
