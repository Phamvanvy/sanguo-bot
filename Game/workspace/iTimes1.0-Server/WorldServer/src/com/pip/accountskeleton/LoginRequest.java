package com.pip.accountskeleton;

import com.pip.itimes.server.world.ConnectSession;

public class LoginRequest extends SessionRequest {

    protected String model;
    protected String version;
    protected String name;
    protected String password;
    protected boolean isRelogin;
    protected String playerName;

    public LoginRequest(int id, int sessionId, ConnectSession session, String name, String password, String model,
                        String version, boolean isRelogin,String playerName) {
        super(RequestType.LOGIN, id, sessionId, session);
        this.model = model;
        this.version = version;
        this.name = name;
        this.password = password;
        this.isRelogin = isRelogin;
        this.playerName = playerName;
    }

    public String getPlayerName(){
        return playerName;
    }

    public boolean isReglogin(){
        return isRelogin;
    }

    public String getName(){
        return name;
    }

    public String getPassword(){
        return password;
    }

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }
}
