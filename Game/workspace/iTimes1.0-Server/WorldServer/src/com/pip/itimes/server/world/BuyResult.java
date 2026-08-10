package com.pip.itimes.server.world;

public class BuyResult {
    public boolean success;
    public long iMoney;
    public long bBalance;
    public int cost;   // 理论消耗
    public int realCost;   // 实际消耗i币
    public String cause;
    public int sessionId;
    public int serial;

}
