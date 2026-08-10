package com.pip.itimes.server.world;

import org.apache.mina.common.IoSession;

public class PipConnectSessionFactory extends ConnectSessionFactory{

    public ConnectSession createSession(IoSession session) {
        return new PipConnectSession(session);
    }

}
