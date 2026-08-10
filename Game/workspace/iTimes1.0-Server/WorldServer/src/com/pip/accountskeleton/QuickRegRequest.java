package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;

public class QuickRegRequest extends SessionRequest {

    protected String phone;
    protected String version;
    protected String model;
    protected String serviceId;

    public QuickRegRequest( int id, int sessionId, ConnectSession session,String phone,String version,String model,String serviceId) {
        super(RequestType.QUICK_REG, id, sessionId, session);
        this.phone = phone;
        this.version = version;
        this.model = model;
        this.serviceId = serviceId;
    }

    public String getVersion() {
        return version;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getPhone() {
        return phone;
    }

    public String getModel() {
        return model;
    }
}
