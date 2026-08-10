package com.pip.itimes.server.world.rabbitRace;

public class RabbitRacePlayerData {
	// 兔子赛跑下注注数
	private int playerId;
	private String playerName;
	private int jettonNumFir;
	private int jettonNumSec;
	private int jettonNumThi;
	private int jettonNumFou;
	private int jettonNumFif;

	public RabbitRacePlayerData() {

	}

	// public RabbitRacePlayerData(int playerId, String name) {
	// RabbitRacePlayerData(playerId, name, 0, 0, 0, 0, 0);
	// }

	// public RabbitRacePlayerData(int playerId, String name, int jettonNumFir,
	// int jettonNumSec, int jettonNumThi, int jettonNumFou,
	// int jettonNumFif) {
	public RabbitRacePlayerData(int playerId, String name) {
		this.playerId = playerId;
		this.playerName = name;
		this.jettonNumFir = 0;
		this.jettonNumSec = 0;
		this.jettonNumThi = 0;
		this.jettonNumFou = 0;
		this.jettonNumFif = 0;
	}

	public int getJettonNum(int index) {
		int jettonNum = 0;
		switch (index) {
		case 0:
			jettonNum = jettonNumFir;
			break;
		case 1:
			jettonNum = jettonNumSec;
			break;
		case 2:
			jettonNum = jettonNumThi;
			break;
		case 3:
			jettonNum = jettonNumFou;
			break;
		case 4:
			jettonNum = jettonNumFif;
			break;
		default:
			break;
		}
		return jettonNum;
	}

	// 本保存为一数组，但是由于数组无法进行永久保存，就单独拆开了，但是还是保留了类似于索引的选择信息
	public void addJettonNum(int index, int jettonNum) {
		switch (index) {
		case 0:
			jettonNumFir += jettonNum;
			break;
		case 1:
			jettonNumSec += jettonNum;
			break;
		case 2:
			jettonNumThi += jettonNum;
			break;
		case 3:
			jettonNumFou += jettonNum;
			break;
		case 4:
			jettonNumFif += jettonNum;
			break;
		default:
			break;
		}
	}

	public void resetJettonNumsAfterRace() {
		jettonNumFir = 0;
		jettonNumSec = 0;
		jettonNumThi = 0;
		jettonNumFou = 0;
		jettonNumFif = 0;
	}

	public int[] getJettonNums() {
		int[] jettonNum = new int[5];
		jettonNum[0] = jettonNumFir;
		jettonNum[1] = jettonNumSec;
		jettonNum[2] = jettonNumThi;
		jettonNum[3] = jettonNumFou;
		jettonNum[4] = jettonNumFif;
		return jettonNum;
	}

	public int getId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getJettonNumFir() {
		return jettonNumFir;
	}

	public void setJettonNumFir(int jettonNumFir) {
		this.jettonNumFir = jettonNumFir;
	}

	public int getJettonNumSec() {
		return jettonNumSec;
	}

	public void setJettonNumSec(int jettonNumSec) {
		this.jettonNumSec = jettonNumSec;
	}

	public int getJettonNumThi() {
		return jettonNumThi;
	}

	public void setJettonNumThi(int jettonNumThi) {
		this.jettonNumThi = jettonNumThi;
	}

	public int getJettonNumFou() {
		return jettonNumFou;
	}

	public void setJettonNumFou(int jettonNumFou) {
		this.jettonNumFou = jettonNumFou;
	}

	public int getJettonNumFif() {
		return jettonNumFif;
	}

	public void setJettonNumFif(int jettonNumFif) {
		this.jettonNumFif = jettonNumFif;
	}

}
