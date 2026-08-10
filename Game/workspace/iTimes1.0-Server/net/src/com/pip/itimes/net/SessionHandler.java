package com.pip.itimes.net;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;

public abstract class SessionHandler implements IoHandler {

    protected SessionRegistry registry = null;

    public SessionHandler(SessionRegistry registry) {
        this.registry = registry;
    }

    public void exceptionCaught(IoSession arg0, Throwable arg1) throws
            Exception {

    }

    public void messageReceived(IoSession session, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        Session s = registry.getSession(session);
        if (s != null) {
            s.handle(packet);
        }
    }

    public void messageSent(IoSession arg0, Object arg1) throws Exception {

    }

    public void sessionClosed(IoSession session) throws Exception {
        Session s = registry.removeSession(session);
        if (s != null)
            s.closed();
    }

    public void sessionCreated(IoSession session) throws Exception {
        Session s = createSession(session);
        if(s!=null){
            registry.registry(s);
            s.created();
        }
    }

    public Session sessionCreated2(IoSession session) throws Exception{
        Session s = createSession(session);
        if(s!=null){
            registry.registry(s);
            s.created();
            return s;
        }
        return null;
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        Session s = registry.getSession(session);
        if(s != null){
            s.idle(status);
        }
    }

    public void sessionOpened(IoSession session) throws Exception {
        Session s = registry.getSession(session);
        if (s != null) {
            s.opened();
        }
    }

    public SessionRegistry getSessionRegistry(){
        return registry;
    }

    public abstract Session createSession(IoSession session);
}
