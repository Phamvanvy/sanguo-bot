package peony.game.association;

import peony.common.ClientSessionAsyncCall;
import peony.game.Server;
import peony.net.ClientSession;

public class AssociationDeleteCall extends ClientSessionAsyncCall {

	protected Association association;
	
	public AssociationDeleteCall(ClientSession session, Association association) {
		super(session);
		this.association = association;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		Server.server.getServiceRegistry().getDbService().associationDao.makeTransient(association);
	}

}
