package com.pip.gameaccount;

import com.pip.net.ISession;

public interface ISessionService {
	public void addSession(ISession session);
	public void removeSession(ISession session);
	public ISession getSession(String name);
	public ISession[] getSessions();
}
