package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;

public class GetAccountNameRequest extends SessionRequest {

    protected String playerName;
    protected int accountId;

    public GetAccountNameRequest(int id, int sessionId, ConnectSession session,int accountId,String playerName) {
        super(RequestType.GET_ACCOUNTNAME, id, sessionId, session);
        this.accountId = accountId;
        this.playerName = playerName;
    }

    public String getPlayerName(){
        return playerName;
    }

    public int getAccountId(){
        return accountId;
    }
}
