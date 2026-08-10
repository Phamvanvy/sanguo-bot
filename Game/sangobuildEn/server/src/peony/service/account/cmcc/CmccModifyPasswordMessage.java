package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.ModifyPasswordMessage;

public class CmccModifyPasswordMessage extends ModifyPasswordMessage {

	protected int accountId;
	protected int playerId;

	public CmccModifyPasswordMessage(String name, String key,
			String oldPassword, String password, int accountId,int playerId) {
		super(name, key, oldPassword, password);
		this.accountId = accountId;
		this.playerId = playerId;
	}
	
	public int getAccountId(){
		return accountId;
	}
	
	
	public int getPlayerId(){
		return playerId;
	}
}
