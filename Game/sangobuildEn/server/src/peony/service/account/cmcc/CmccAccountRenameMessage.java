package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.RenameMessage;

public class CmccAccountRenameMessage extends RenameMessage {

	protected int accountId;
	protected int playerId;

	public CmccAccountRenameMessage(String oldName, String newName, int accountId,int playerId) {
		super(oldName, newName);
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
