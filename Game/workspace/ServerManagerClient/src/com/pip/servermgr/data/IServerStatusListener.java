package com.pip.servermgr.data;

public interface IServerStatusListener {
	public void statusChanged(Server server);
	public void onError(Server server, Exception ex);
}
