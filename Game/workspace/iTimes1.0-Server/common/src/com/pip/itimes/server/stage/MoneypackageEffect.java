package com.pip.itimes.server.stage;

public class MoneypackageEffect extends Effect {

    private int percent;
    private int money;

    public MoneypackageEffect(int percent,int money) {
        this.percent = percent;
        this.money = money;
    }

    public int getPercent(){
        return percent;
    }

    public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public byte getType() {
        return 43;
    }
}
