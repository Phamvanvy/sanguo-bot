package com.pip.dispatch;

import org.apache.mina.common.*;

public class ClientSessionHandler
    extends IoHandlerAdapter {

    private SocketDispatcher dispatcher;

    public ClientSessionHandler() {
    }


    public void exceptionCaught(IoSession sesion, Throwable throwable) throws
        Exception {
    }

    public void messageReceived(IoSession session, Object object) throws
        Exception {
        dispatcher.dispatchToServer(session,object);
    }

    public void sessionClosed(IoSession session) throws Exception {
        dispatcher.unRegisterClient(session);
    }

    public void sessionCreated(IoSession session) throws Exception {
        dispatcher.registerClient(session);
    }

    public void sessionIdle(IoSession session, IdleStatus idleStatus) throws
        Exception {
        session.close();
    }
}
