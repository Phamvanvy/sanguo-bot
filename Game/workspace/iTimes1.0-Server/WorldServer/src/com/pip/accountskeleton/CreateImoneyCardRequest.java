package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;

public class CreateImoneyCardRequest extends SessionRequest{

    protected int playerId;
    protected int type;

    public CreateImoneyCardRequest(int id, int sessionId, ConnectSession session, int playerId, int type){
        super(RequestType.CREATE_IMONEY_CARD, id, sessionId, session);
        this.playerId = playerId;
        this.type = type;
    }

    public int getPlayerId(){
        return playerId;
    }

    public int getType(){
        return type;
    }
}
