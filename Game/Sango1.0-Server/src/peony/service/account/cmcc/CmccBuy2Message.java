package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;

/**
 * CMCCÐÂ¹ºÂòMessage
 * @author dchen
 */
public class CmccBuy2Message extends AbstractMessage {

	protected int accountId;
	protected int cost;
	protected int requestId;
	
	public CmccBuy2Message(int accountId, int cost, int requestId){
		super(CmccMessageType.CMCC_BUY2,requestId);
		this.accountId = accountId;
		this.cost = cost;
		this.requestId = requestId;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	
}
