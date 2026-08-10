package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;

public class ModifyPasswordRequest extends SessionRequest {

    protected String accountName;
    protected String key;
    protected String password;

    public ModifyPasswordRequest(int id, int sessionId, ConnectSession session,String accountName,String key,String password) {
        super(RequestType.MODIFY_PASSWORD, id, sessionId, session);
        this.accountName = accountName;
        this.key = key;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getKey() {
        return key;
    }

    public String getAccountName() {
        return accountName;
    }


}
