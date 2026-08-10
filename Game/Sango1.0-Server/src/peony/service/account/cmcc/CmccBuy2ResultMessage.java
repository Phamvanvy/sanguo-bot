package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;

/**
 * @author dchen
 */
public class CmccBuy2ResultMessage extends AbstractMessage {
	
	protected int requestId;
	protected boolean result;
	protected long balance;
	protected int cost;
	protected String cause;
	
	public CmccBuy2ResultMessage(int serial, int requestId, boolean result, long balance, int cost, String cause){
		super(CmccMessageType.CMCC_BUY2_RESULT, serial);
		this.requestId = requestId;
		this.result = result;
		this.balance = balance;
		this.cost = cost;
		this.cause = cause;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public boolean isSuccess() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String msg) {
		this.cause = msg;
	}
	
}
