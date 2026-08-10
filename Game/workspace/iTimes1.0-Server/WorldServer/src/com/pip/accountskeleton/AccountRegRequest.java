package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;

public class AccountRegRequest extends SessionRequest {
    public AccountRegRequest(int id, int sessionId, ConnectSession session) {
        super(RequestType.ACCOUNT_REG,id, sessionId, session);
    }
}
