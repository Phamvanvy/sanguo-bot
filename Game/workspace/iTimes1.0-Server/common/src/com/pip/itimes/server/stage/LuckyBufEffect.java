package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class LuckyBufEffect extends Effect{

    private int validTime;
    private int expRadio;
    private int moneyRadio;

    public LuckyBufEffect() {
    }

    public byte getType(){
        return 15;
    }

    public int getValidTime() {
        return validTime;
    }

    public int getMoneyRadio() {
        return moneyRadio;
    }

    public void setExpRadio(int expRadio) {
        this.expRadio = expRadio;
    }

    public void setValidTime(int validTime) {
        this.validTime = validTime;
    }

    public void setMoneyRadio(int moneyRadio) {
        this.moneyRadio = moneyRadio;
    }

    public int getExpRadio() {
        return expRadio;
    }
}
