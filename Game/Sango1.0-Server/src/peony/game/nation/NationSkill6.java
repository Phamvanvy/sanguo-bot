package peony.game.nation;

import peony.game.Player;

public class NationSkill6 extends NationSkill {

	static final String NAME = peony.Messages.STRING_01544;
	
	static final int MAXLEVEL = 3;
	
	static int[] UPGRADE_MONEY = {0,2000000,4000000,8000000};
	
	static int[] MAINTAIN_MONEY = {0,200000,400000,800000};
	
	static float[] EXPRATIO = {
		0f,
		0.05f,
		0.10f,
		0.15f
	};
	
	static String[] DESC = { 
		peony.Messages.STRING_01545,
		peony.Messages.STRING_01546,
		peony.Messages.STRING_01547,
		peony.Messages.STRING_01548,
	};
	
	public NationSkill6(int level) {
		super(6, NAME, level, MAXLEVEL, TYPE_NONE);
	}

	public NationSkill clone() {
		return new NationSkill6(level);
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
	
	public float getExpRatio(){
		return EXPRATIO[level];
	}

}
