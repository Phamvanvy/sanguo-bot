package com.pip.accountskeleton;

import com.pip.itimes.server.bean.IMoneyCard;
import com.pip.itimes.server.world.ConnectSession;

public class UseImoneyCardRequest extends SessionRequest{

    protected int playerId;
    protected IMoneyCard card;

    public UseImoneyCardRequest(int id, int sessionId, ConnectSession session, int playerId, IMoneyCard card){
        super(RequestType.CREATE_IMONEY_CARD, id, sessionId, session);
        this.playerId = playerId;
        this.card = card;
    }

    public int getPlayerId(){
        return playerId;
    }

    public IMoneyCard getCard(){
        return card;
    }
}
