package com.pip.accountskeleton;

import com.pip.net.IRequest;
import com.pip.itimes.server.world.AdminSession;

public class AdminRequest implements IRequest {

    private int type;
    private int id;
    private int sessionId;
    private AdminSession session;

    public AdminRequest(int type,int id,int sessionId,AdminSession session) {
        this.type = type;
        this.id = id;
        this.sessionId = sessionId;
        this.session = session;
    }

    public int getSessionId() {
        return sessionId;
    }

    public AdminSession getSession() {
        return session;
    }

    public int getId() {
        return id;
    }

    public int getType(){
        return type;
    }
}
