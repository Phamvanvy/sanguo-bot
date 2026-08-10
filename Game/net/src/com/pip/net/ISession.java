package com.pip.net;

import java.net.SocketAddress;

public interface ISession{
	String getId();
	boolean isConnected();
	boolean isValid();
	void send(IMessage message);
	void close();
	SocketAddress getRemoteAddress();
	public boolean equals(ISession session);
}
