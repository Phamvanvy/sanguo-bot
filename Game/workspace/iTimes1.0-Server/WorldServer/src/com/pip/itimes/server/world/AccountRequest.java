package com.pip.itimes.server.world;

public class AccountRequest {
    public byte appType;
    public int id;
    public int sessionId;
    public int serial;
    public String model;
    public ConnectSession session;

    public AccountRequest(int id, byte appType, int serial, int sessionId, ConnectSession session) {
        this.appType = appType;
        this.id = id;
        this.serial = serial;
        this.sessionId = sessionId;
        this.session = session;
    }
}
