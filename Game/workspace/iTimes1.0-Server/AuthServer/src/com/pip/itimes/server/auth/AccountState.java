package com.pip.itimes.server.auth;

import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.bean.FeePlan;

public class AccountState {

    private Account account;
    private int playerId;
    private long time;
    private int sessionId;
    private long lastLiveTime;

    private CmccUserKey userKey;

    private ConnectSession session = null;

    public AccountState(Account account, long time, int sessionId) {
        this.account = account;
        this.time = time;
        this.sessionId = sessionId;
    }

    public void setSession(ConnectSession session){
        this.session = session;
    }

    public ConnectSession getSession(){
        return session;
    }

    public int getId() {
        return account.getId();
    }

    public void setPlayerId(int playerId){
        this.playerId = playerId;
    }

    public int getPlayerId(){
        return playerId;
    }

    public long getTime(){
        return time;
    }

    public void setTime(long time){
        this.time = time;
    }

    public int getSessionId(){
        return sessionId;
    }


    public long lastLiveTime(){
        return lastLiveTime;
    }

    public void setLastLiveTime(long lastLiveTime){
        this.lastLiveTime = lastLiveTime;
    }

    public void setUserKey(CmccUserKey userKey) {
        this.userKey = userKey;
    }

    public String getPhone(){
        return account.getPhone();
    }

    public Account getAccount(){
        return account;
    }

    public CmccUserKey getUserKey() {
        return userKey;
    }

    public String getGameCode(){
        return account.getGameCode();
    }
}
