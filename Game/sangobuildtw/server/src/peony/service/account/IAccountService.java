package peony.service.account;

import peony.net.ClientSession;


public interface IAccountService {
	//千万别学这里message的用法，为了兼容原来的认证系统，无奈之举
	public void sendAndRegister(Object message,AccountAsyncCall call);
	
	public void schedule(AccountAsyncCall call);
	
	public void registerClientSession(ClientSession session);
	
	public Account getAccount(int accountId);
	
	public ClientSession getClientSession(int accountId);
}
