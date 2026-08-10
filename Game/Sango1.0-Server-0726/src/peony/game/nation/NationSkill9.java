package peony.game.nation;

import peony.game.Player;

public class NationSkill9 extends NationSkill {

	protected static String NAME = "任务大师";
	protected static int MAXLEVEL = 5;
	public static final byte TYPE_NONE = 0; 
	
	static int[] UPGRADE_MONEY = {
		0,
		200000,
		400000,
		800000,
		1600000,
		3200000
	};
	
	static int[] MAINTAIN_MONEY = {
		0,
		20000,
		40000,
		80000,
		160000,
		320000
	};
	
	static float[] RATIOS = {
		0,
		0.02f,
		0.04f,
		0.06f,
		0.08f,
		0.10f
	};
	
	static String[] DESC = { 
		"任务大师0级,无任何效果。",
		"任务大师1级,玩家完成任意任务时，可额外获得任务奖励中经验，荣誉和声望的2%。",
		"任务大师成2级,可额外获得任务奖励中经验，荣誉和声望的4%。",
		"任务大师成3级,可额外获得任务奖励中经验，荣誉和声望的6%。",
		"任务大师大师4级,可额外获得任务奖励中经验，荣誉和声望的8%。",
		"任务大师大师5级,可额外获得任务奖励中经验，荣誉和声望的10%。"
	};
	
	public NationSkill9(int level) {
		super(9, NAME, level, MAXLEVEL, TYPE_NONE);
	}

	public NationSkill clone() {
		return new NationSkill9(level);
	}

	public void fire(Player p) {
		
	}

	public String getDesc(int level) {
		return DESC[level];
	}

	public int getMaintainMoney(int level) {
		return MAINTAIN_MONEY[level];
	}

	public int getUpgradeMoney(int level) {
		return UPGRADE_MONEY[level];
	}
	
	public float getRatio(){
		return RATIOS[level];
	}

}
