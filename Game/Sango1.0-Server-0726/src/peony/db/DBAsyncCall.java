package peony.db;

import peony.common.ClientSessionAsyncCall;
import peony.net.ClientSession;

public abstract class DBAsyncCall extends ClientSessionAsyncCall{
	
	protected DBService dbService;

	
	public DBAsyncCall(DBService service,ClientSession session){
		super(session);
		this.dbService = service;
	}
}
