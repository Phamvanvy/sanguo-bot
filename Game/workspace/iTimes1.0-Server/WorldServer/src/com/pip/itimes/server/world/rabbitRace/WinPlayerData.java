package com.pip.itimes.server.world.rabbitRace;

public class WinPlayerData {
	private RabbitRacePlayerData player;
	private int money;

	public WinPlayerData(RabbitRacePlayerData player2, int money) {
		setPlayer(player2);
		setMoney(money);
	}

	// public WinPlayerData(int p) {
	//
	// }
	public RabbitRacePlayerData getPlayer() {
		return player;
	}

	public void setPlayer(RabbitRacePlayerData player2) {
		this.player = player2;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

}
