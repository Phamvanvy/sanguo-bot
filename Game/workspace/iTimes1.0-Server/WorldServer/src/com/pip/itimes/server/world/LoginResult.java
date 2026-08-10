package com.pip.itimes.server.world;

import com.pip.accountskeleton.LoginRequest;

public class LoginResult {
    public int accountId;
    public String name;
    public String key;
    public String phone;
    public String password;
    public int[] purchased;
    public int modifyPasswordTimes;
    public long iMoney;
    public long bBalance;
    public boolean isMonth;
    public boolean isSubscribe;
    public int loginErrorTime;
    //mengjie add
    public String cityname = "";

    public AccountRequest aRequest;
    public LoginRequest bRequest;

}
