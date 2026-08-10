package peony.game.nation;

import peony.game.Player;

public class NationSkill7 extends NationSkill {

static final String NAME = peony.Messages.STRING_00600;
	
	static final int MAXLEVEL = 5;
	
	static int[] UPGRADE_MONEY = {0,200000,400000,800000,1600000,3200000};
	
	static int[] MAINTAIN_MONEY = {0,10000,20000,40000,80000,160000};
	
	static float[] RATIOS = {
		0,
		0.04f,
		0.08f,
		0.12f,
		0.16f,
		0.20f
	};
	
	static String[] DESC = { 
		peony.Messages.STRING_00601,
		peony.Messages.STRING_00602,
		peony.Messages.STRING_00603,
		peony.Messages.STRING_00604,
		peony.Messages.STRING_00605,
		peony.Messages.STRING_00606
	};
	
	public NationSkill7(int level) {
		super(7, NAME, level, MAXLEVEL, TYPE_NONE);
	}

	public NationSkill clone() {
		return new NationSkill7(level);
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
