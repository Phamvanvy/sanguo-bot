package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;

public class CmccAndroidSmsBuyReqResultMessage extends AbstractMessage {

	protected int requestId;
	protected int accountId;
	protected int playerId;
	protected boolean result;
	protected String sms;
	
	public CmccAndroidSmsBuyReqResultMessage(int serialId, int requestId, int accountId, int playerId, boolean result, String sms) {
		super(CmccMessageType.ANDROIDBUYREQRESULT, serialId);
		this.requestId = requestId;
		this.accountId = accountId;
		this.playerId = playerId;
		this.result = result;
		this.sms = sms;
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

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getSms() {
		return sms;
	}

	public void setSms(String sms) {
		this.sms = sms;
	}

}
