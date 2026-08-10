package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;

public class CmccAndroidSmsBuyReqOkMessage extends AbstractMessage {

	protected int requestId;
	protected int accountId;
	protected int playerId;
	protected String consumeCode;
	protected int itemId;
	
	public CmccAndroidSmsBuyReqOkMessage(int serialId, int requestId, int accountId,
			int playerId, String consumeCode, int itemId) {
		super(CmccMessageType.ANDROIDBUYOK, serialId);
		this.requestId = requestId;
		this.accountId = accountId;
		this.playerId = playerId;
		this.consumeCode = consumeCode;
		this.itemId = itemId;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getConsumeCode() {
		return consumeCode;
	}

	public void setConsumeCode(String consumeCode) {
		this.consumeCode = consumeCode;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

}
