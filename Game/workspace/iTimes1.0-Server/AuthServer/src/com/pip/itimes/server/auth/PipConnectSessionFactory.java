package com.pip.itimes.server.auth;

import org.apache.mina.common.IoSession;

public class PipConnectSessionFactory extends ConnectSessionFactory {
    public PipConnectSessionFactory() {
        super();
    }

    public ConnectSession createSession(IoSession session) {
        return new PipConnectSession(session);
    }
}
