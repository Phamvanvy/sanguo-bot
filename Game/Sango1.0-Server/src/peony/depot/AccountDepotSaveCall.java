package peony.depot;

import peony.common.ClientSessionAsyncCall;
import peony.game.AccountDepotService;
import peony.game.Server;
import peony.net.ClientSession;

public class AccountDepotSaveCall extends ClientSessionAsyncCall {

	protected int accountId;
	
	public AccountDepotSaveCall(ClientSession session, int accountId) {
		super(session);
		this.accountId = accountId;
	}

	public void callFinish() throws Exception {
		
	}
	
	public void run() {
		AccountDepotService service = Server.server.getServiceRegistry().getAccountDepotService();
		service.saveAccountDepot(accountId);
	}

}
