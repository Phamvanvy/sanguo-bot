package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;
import com.pip.itimes.server.world.StoreService;

public class StoreRequest extends SessionRequest {

    protected StoreService.Request request;

    public StoreRequest( int id, int sessionId, ConnectSession session,StoreService.Request request) {
        super(RequestType.BUY, id, sessionId, session);
        this.request = request;
    }

    public StoreService.Request getRequest(){
        return request;
    }
}
