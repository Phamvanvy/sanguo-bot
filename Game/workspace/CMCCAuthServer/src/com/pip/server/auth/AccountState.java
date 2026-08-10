package com.pip.server.auth;

import com.pip.server.auth.bean.Account;
import com.pip.server.auth.cmcc.CmccUserKey;

/**
 * 登录用户状态信息。
 */
public class AccountState {
    /*
     * 帐号对象
     */
    private Account account;
    /*
     * 登录时间
     */
    private long time;
    /*
     * 最后活跃时间
     */
    private long lastLiveTime;
    /*
     * 对应卓望平台用户ID
     */
    private String cmccUserID;
    /*
     * 用户登录的世界服务器连接
     */
    private ConnectSession session = null;

    public AccountState(Account account, long time) {
        this.account = account;
        this.time = time;
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

    public long getTime(){
        return time;
    }

    public void setTime(long time){
        this.time = time;
    }

    public long lastLiveTime(){
        return lastLiveTime;
    }

    public void setLastLiveTime(long lastLiveTime){
        this.lastLiveTime = lastLiveTime;
    }

    public void setCmccUserID(String userId) {
        this.cmccUserID = userId;
    }

    public String getPhone(){
        return account.getPhone();
    }

    public Account getAccount(){
        return account;
    }

    public String getCmccUserID() {
        return cmccUserID;
    }

    public String getGameCode(){
        return account.getGameCode();
    }
}
