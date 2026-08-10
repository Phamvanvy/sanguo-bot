package peony.game.nation;

import peony.game.Player;

public class NationSkill9 extends NationSkill {

	protected static String NAME = peony.Messages.STRING_01504;
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
		peony.Messages.STRING_01505,
		peony.Messages.STRING_01506,
		peony.Messages.STRING_01507,
		peony.Messages.STRING_01508,
		peony.Messages.STRING_01509,
		peony.Messages.STRING_01510
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
