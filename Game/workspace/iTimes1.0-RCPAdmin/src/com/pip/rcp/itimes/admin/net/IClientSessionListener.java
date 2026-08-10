package com.pip.rcp.itimes.admin.net;


public interface IClientSessionListener{
    public void sessionOpened();

    public void sessionClosed();

    public void messageReceived(Packet packet);

    public void messageReceived(String s);
}
