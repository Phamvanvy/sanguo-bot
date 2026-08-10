package com.pip.server.auth.net;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.common.IoSession;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

public class SessionRegistry {

    private static final Logger log = Logger.getLogger(SessionRegistry.class);

    private Map sessions = null;

    //sessionId session
    private Map sessions2 = null;

    private AtomicInteger ids = new AtomicInteger(1);

    public SessionRegistry() {
        sessions = new HashMap();
        sessions2 = new HashMap();
    }

    public void registry(Session session) {
        sessions.put(session.getIoSession(), session);
        synchronized (this) {
            while(true){
                int sessionId = ids.incrementAndGet();
                if(!sessions2.containsKey(sessionId)){
                    session.sessionId = sessionId;
                    sessions2.put(new Integer(sessionId), session);
                    break;
                }else{
                    log.info("conflict id["+sessionId+"]");
                }
            }
        }
    }

    public Session removeSession(IoSession session) {
        Session s = (Session) sessions.get(session);
        if (s != null) {
            sessions2.remove(new Integer(s.sessionId));
            return (Session) sessions.remove(session);
        }
        return null;
    }

    public Session getSession(IoSession session) {
        return (Session) sessions.get(session);
    }

    public Session getSession(int sessionId) {
        return (Session) sessions2.get(new Integer(sessionId));
    }

//    public void registry(HttpSession session){
//        sessions2.put(new Integer(session.getSessionId()),session);
//    }

//    public void removeSession(int sessionId){
//        sessions2.remove(new Integer(sessionId));
//    }

}
