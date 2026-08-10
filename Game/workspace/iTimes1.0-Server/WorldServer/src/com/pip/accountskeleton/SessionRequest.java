package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;
import com.pip.net.IRequest;

public class SessionRequest implements IRequest{

    private int type;
    private int id;
    private int sessionId;
    private ConnectSession session;

    public SessionRequest(int type,int id,int sessionId,ConnectSession session) {
        this.type = type;
        this.id = id;
        this.sessionId = sessionId;
        this.session = session;
    }

    public int getSessionId() {
        return sessionId;
    }

    public ConnectSession getSession() {
        return session;
    }

    public int getId() {
        return id;
    }

    public int getType(){
        return type;
    }
}
