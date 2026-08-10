package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;

public class ModifyPhoneRequest extends SessionRequest {

    protected String phone;

    public ModifyPhoneRequest( int id, int sessionId, ConnectSession session,String phone) {
        super(RequestType.MODIFY_PHONE, id, sessionId, session);
        this.phone = phone;
    }

    public String getPhone(){
        return phone;
    }
}
