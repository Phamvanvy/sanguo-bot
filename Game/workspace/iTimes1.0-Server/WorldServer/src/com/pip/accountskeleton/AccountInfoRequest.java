package com.pip.accountskeleton;

import com.pip.itimes.server.world.AdminSession;

public class AccountInfoRequest extends AdminRequest {


    public AccountInfoRequest(int id, int sessionId, AdminSession session) {
        super(RequestType.ACCOUNT_INFO, id, sessionId, session);
    }
}
