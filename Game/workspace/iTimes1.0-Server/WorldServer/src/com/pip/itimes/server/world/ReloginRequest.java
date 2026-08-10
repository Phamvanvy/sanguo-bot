package com.pip.itimes.server.world;

public class ReloginRequest extends AccountRequest {

    public String playerName;

    public ReloginRequest(int id, byte appType, int serial, int sessionId, ConnectSession session,String playerName) {
        super(id, appType, serial, sessionId, session);
        this.playerName = playerName;
    }
}
