package com.pip.itimes.server.connect;

import org.apache.mina.common.IoSession;

public class PipClientSessionFactory extends ClientSessionFactory {
    public PipClientSessionFactory() {
        super();
    }


    public ClientSession createSession(IoSession session) {
        return new PipClientSession(session);
    }
}
