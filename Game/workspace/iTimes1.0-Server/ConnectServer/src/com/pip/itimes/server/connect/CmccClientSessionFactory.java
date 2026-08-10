package com.pip.itimes.server.connect;

import org.apache.mina.common.IoSession;

public class CmccClientSessionFactory extends ClientSessionFactory {

    public CmccClientSessionFactory() {
        super();
    }


    public ClientSession createSession(IoSession session) {
        return new CmccClientSession(session);
    }
}
