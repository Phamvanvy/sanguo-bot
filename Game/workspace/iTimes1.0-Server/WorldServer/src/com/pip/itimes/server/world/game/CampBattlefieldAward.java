package com.pip.itimes.server.world.game;

/**
 * 阵营战场：奖励
 * @author hchen
 *
 */
public class CampBattlefieldAward {
	/**
	 * 进入战场的等级分组类型
	 */
	private int levelType;
	/**
	 * 胜者获得的经验比率
	 */
	private int winnerExpRate;
	/**
	 * 败者获得的经验比率
	 */
    private int loserExpRate;
    /**
     * 胜者获得杀戮点数
     */
    private int winnerPoint;
    /**
     * 败者获得杀戮点数
     */
    private int loserPoint;
    /**
     * 随机战场的奖励比率
     */
    private int rate;
	/**
     * 百级玩家获得礼物ID
     */
    private int giftID;
    /**
     * 战场召唤奖励比率
     */
    private int summonRate;
    /**
     * 战场召唤胜者获得的经验比率
     */
    private int summonWinnerExpRate;
    /**
     * 战场召唤败者获得的经验比率
     */
    private int summonLoserExpRate;
    /**
     * 战场召唤胜者获得杀戮点数
     */
    private int summonWinnerPoint;
    /**
     * 战场召唤败者获得的经验比率
     */
    private int summonLoserPoint;
    /**
     * 战场召唤时间
     */
    private int[] timePeriods;
    
    public CampBattlefieldAward (int levelType, int summonLoserExpRate, int summonLoserPoint,
    			int summonWinnerExpRate, int summonWinnerPoint, int winnerExpRate,
    				int winnerPoint, int loserExpRate, int loserPoint) {
    	this.levelType = levelType;
    	this.summonLoserExpRate = summonLoserExpRate;
    	this.summonLoserPoint = summonLoserPoint;
    	this.summonWinnerExpRate = summonWinnerExpRate;
    	this.summonWinnerPoint = summonWinnerPoint;
    	this.winnerExpRate = winnerExpRate;
    	this.winnerPoint = winnerPoint;
    	this.loserExpRate = loserExpRate;
    	this.loserPoint = loserPoint;
    }
	
	public int getLevelType() {
		return levelType;
	}
	public void setLevelType(int levelType) {
		this.levelType = levelType;
	}
	
	public int getWinnerExpRate () {
		return winnerExpRate;
	}
	public void setWinnerExpRate(int winnerExpRate) {
		this.winnerExpRate = winnerExpRate;
	}
	
	public int getLoserExpRate () {
		return loserExpRate;
	}
	public void setLoserExpRate (int loserExpRate) {
		this.loserExpRate = loserExpRate;
	}
	
	public int getWinnerPoint() {
		return winnerPoint;
	}
	public void setWinnerPoint (int winnerPoint) {
		this.winnerPoint = winnerPoint;
	}
	
	public int getLoserPoint() {
		return loserPoint;
	}
	public void setLoserPoint (int loserPoint) {
		this.loserPoint = loserPoint;
	}
	
	public int getRate () {
		return rate;
	}
	public void setRate (int rate) {
		this.rate = rate;
	}
	
	public int getGiftID () {
		return giftID;
	}
	public void setGiftID (int giftID) {
		this.giftID = giftID;
	}
	
	public int getSummonRate () {
		return summonRate;
	}
	public void setSummonRate (int summonRate) {
		this.summonRate = summonRate;
	}
	
	public void setTimePeriods (int[] timePeriods) {
		this.timePeriods = timePeriods;
	}
	public int[] getTimePeriods () {
		return timePeriods;
	}
	
	public int getSummonWinnerExpRate() {
		return summonWinnerExpRate;
	}
	public void setSummonWinnerExpRate(int summonWinnerExpRate) {
		this.summonWinnerExpRate = summonWinnerExpRate;
	}
	
	public int getSummonLoserExpRate() {
		return summonLoserExpRate;
	}
	public void setSummonLoserExpRate(int summonLoserExpRate) {
		this.summonLoserExpRate = summonLoserExpRate;
	}
	
	public int getSummonWinnerPoint() {
		return summonWinnerPoint;
	}
	public void setSummonWinnerPoint(int summonWinnerPoint) {
		this.summonWinnerPoint = summonWinnerPoint;
	}
	
	public int getSummonLoserPoint() {
		return summonLoserPoint;
	}
	public void setSummonLoserPoint(int summonLoserPoint) {
		this.summonLoserPoint = summonLoserPoint;
	}
}
