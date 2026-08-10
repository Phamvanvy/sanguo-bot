package com.pip.itimes.server.world;

import org.apache.mina.common.IoSession;

public class QQConnectSessionFactory extends ConnectSessionFactory {

    public ConnectSession createSession(IoSession session) {
        return new QQConnectSession(session);
    }
}
