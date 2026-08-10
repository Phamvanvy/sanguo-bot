package peony.net;

import peony.service.Service;

public interface ClientSessionService extends Service{
	public void addClientSession(ClientSession session);
	public void removeClientSession(ClientSession session);
}
