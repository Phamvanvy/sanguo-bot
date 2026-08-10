package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;

public class ChargeRequest extends SessionRequest {

    protected int playerId;
    protected int value;

    public ChargeRequest( int id, int sessionId, ConnectSession session,int playerId,int value) {
        super(RequestType.CHARGE, id, sessionId, session);
        this.playerId = playerId;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getPlayerId() {
        return playerId;
    }


}
