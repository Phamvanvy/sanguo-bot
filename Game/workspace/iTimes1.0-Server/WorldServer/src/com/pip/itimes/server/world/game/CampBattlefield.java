package com.pip.itimes.server.world.game;

import java.util.HashMap;
import java.util.Map;

/**
 * 阵营战场类
 * @author hchen
 *
 */

public class CampBattlefield {
	public static final byte MODEL_NORMOL = 0;		//正常模式
	public static final byte MODEL_CHAOS = 1;		//混乱模式
	
	/**
	 * 战场名字
	 */
	private String name;
	/**
	 * 战场描述
	 */
	private String desc;
	/**
	 * 战场目标
	 */
	private String target;
	
	/**
	 * 战场模式
	 */
	private int model;
	
	/**
	 * 战场战士名额
	 * KEY：战场等级类型
	 * VALUES：对应的名额限制
	 */
	private Map<Integer, CampbattlefieldWarriorPlaces> campBattlefieldWarrior = new HashMap<Integer, CampbattlefieldWarriorPlaces>();
	/**
	 * 战场奖励
	 * KEY：战场等级类型
	 * VALUES：对应的战场奖励
	 */
	private Map<Integer, CampBattlefieldAward> campBattlefieldAward = new HashMap<Integer, CampBattlefieldAward>();

	public String getName () {
		return name;
	}
	public void setName (String name) {
		this.name = name;
	}
	public String getDesc () {
		return desc;
	}
	public void setDesc (String desc) {
		this.desc = desc;
	}
	
	public String getTarget () {
		return target;
	}
	public void setTarget (String target) {
		this.target = target;
	}
	public int getModel(){
		return model;
	}
	public void setModel(int model){
		this.model = model;
	}
	
	public Map<Integer, CampBattlefieldAward> getCampBattlefieldAward () {
		return campBattlefieldAward;
	}
	public void setCampBattlefieldAward (
			Map<Integer, CampBattlefieldAward> campBattlefieldAward) {
		this.campBattlefieldAward = campBattlefieldAward;
	}
	
	public void putCampBattlefieldAward (CampBattlefieldAward campBattlefieldAward) {
		this.campBattlefieldAward.put(campBattlefieldAward.getLevelType(), campBattlefieldAward);
	}
	
	public CampBattlefieldAward getCampBattlefieldTypeAward (int levelType) {
		return this.campBattlefieldAward.get(levelType);
	}
	
	public Map<Integer, CampbattlefieldWarriorPlaces> getCampBattlefieldWarrior () {
		return campBattlefieldWarrior;
	}
	public void setCampBattlefieldWarrior (Map<Integer, CampbattlefieldWarriorPlaces> campBattlefieldWarrior) {
		this.campBattlefieldWarrior = campBattlefieldWarrior;
	}
	public void putCampBattlefieldWarrior (CampbattlefieldWarriorPlaces campbattlefieldWarrior) {
		this.campBattlefieldWarrior.put(campbattlefieldWarrior.getLevelType(), campbattlefieldWarrior);
	}
	public CampbattlefieldWarriorPlaces getCampbattlefieldWarrior (int levelType) {
		return this.campBattlefieldWarrior.get(levelType);
	}
}
