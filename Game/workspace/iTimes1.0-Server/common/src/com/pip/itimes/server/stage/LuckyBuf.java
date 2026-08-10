package com.pip.itimes.server.stage;

/**
 * 次类不属于一般的buf体系
 * @author Jeffrey
 * @version 1.0
 */
public class LuckyBuf {

    private long validTime;
    private int expRatio;
    private int moneyRatio;

    public LuckyBuf() {
    }

    public long getValidTime() {
        return validTime;
    }

    public int getMoneyRatio() {
        return moneyRatio;
    }

    public void setExpRatio(int expRatio) {
        this.expRatio = expRatio;
    }

    public void setValidTime(long validTime) {
        this.validTime = validTime;
    }

    public void setMoneyRatio(int moneyRatio) {
        this.moneyRatio = moneyRatio;
    }

    public int getExpRatio() {
        return expRatio;
    }
}
