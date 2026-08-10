package com.pip.itimes.server.world;

import java.util.HashMap;
import java.util.Map;

import com.pip.itimes.server.stage.IItem;

/**
 * @author wpjiang
 *	保留玩家因为掉线而保留的装备信息，还有时间
 */
public class WorldBossEquipInfo {
	
	public final static int delayBattleFinalTime = 7 * 60 * 1000 / 2;
	
	public final static int delayFinalTime = 60 * 1000;

	public int getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}

	/**
	 * 世界boss的装备
	 */
	Map<IItem , Integer> equDiamondTimeMap = new HashMap<IItem, Integer>();
	
	
	public Map<IItem, Integer> getEquDiamondTimeMap() {
		return equDiamondTimeMap;
	}

	public void setEquDiamondTimeMap(Map<IItem, Integer> equDiamondTimeMap) {
		this.equDiamondTimeMap = equDiamondTimeMap;
	}

	/**
	 * 掉线的默认时间
	 */
	private int delayTime;
	
	public void reduceDelayTime(int time){
		this.delayTime = this.delayTime - time;
	}
	
	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	/**
	 * 是否在线
	 */
	private boolean online;
	
	
	
}
