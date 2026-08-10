package peony.service.account;

import java.util.Date;
import peony.common.ClientSessionAsyncCall;
import peony.game.Server;
import peony.net.ClientSession;

public class RecordChargeCall extends ClientSessionAsyncCall {

	private int accountId;
	private int ammount;
	private Date chargeTime;
	
	public RecordChargeCall(ClientSession session, int accountId, int ammount) {
		super(session);
		this.accountId = accountId;
		this.ammount = ammount;
		this.chargeTime = new Date();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		RecordChargeService recordChargeService = Server.server.getServiceRegistry().getRecordChargeService();
		Charge charge = new Charge(accountId, ammount, chargeTime);
		recordChargeService.addCharge(charge);
		addToClientSession();
	}

}
