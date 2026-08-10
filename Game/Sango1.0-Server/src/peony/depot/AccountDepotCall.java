package peony.depot;

import peony.common.ClientSessionAsyncCall;
import peony.db.AccountDepotDAO;
import peony.game.AccountDepot;
import peony.game.Server;
import peony.net.ClientSession;

public class AccountDepotCall extends ClientSessionAsyncCall {

	protected AccountDepot depot;
	
	public AccountDepotCall(ClientSession session, AccountDepot depot) {
		super(session);
		this.depot = depot;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(depot!=null){
			AccountDepotDAO accountDepotDAO= Server.server.getServiceRegistry().getDbService().accountDepotDAO;
			accountDepotDAO.newEntity(depot);
		}
		addToClientSession();
	}

}
