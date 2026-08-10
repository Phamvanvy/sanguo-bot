package com.pip.itimes.server.world;

import com.pip.accountskeleton.LoginRequest;

public class ReloginResult {
    public byte type;
    public int accountId;
    public String name;
    public String key;
    public String password;
    public String phone;
    public int modifyPasswordTimes;
    public long iMoney;
    public long bBalance;
    public boolean isMonth;
    public boolean isSubscribe;
    public int[] purchased;

    public AccountRequest aRequest;
    public LoginRequest bRequest;
}
