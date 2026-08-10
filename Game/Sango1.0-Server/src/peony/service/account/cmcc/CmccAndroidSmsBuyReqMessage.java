package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;

public class CmccAndroidSmsBuyReqMessage extends AbstractMessage {

	protected int requestId;
	protected int accountId;
	protected int playerId;
	protected String consumeCode;
	protected String version;
	
	public CmccAndroidSmsBuyReqMessage(int requestId, int accountId, int playerId, String consumeCode, String version) {
		super(CmccMessageType.ANDROIDBUYREQ);
		this.requestId = requestId;
		this.accountId = accountId;
		this.playerId = playerId;
		this.consumeCode = consumeCode;
		this.version = version;
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
		return -1;
	}

	public void setItemId(int itemId) {
	}
	
	public String getVersion() {
		return version;
	}
}
