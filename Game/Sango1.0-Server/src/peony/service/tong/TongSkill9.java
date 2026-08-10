package peony.service.tong;

import peony.game.Player;

public class TongSkill9 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_00987;
	
	static final int MAXLEVEL = 3;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,400,800,1500};
	
	static String[] DESC = { 
		peony.Messages.STRING_00987,
		peony.Messages.STRING_00988,
		peony.Messages.STRING_00989,
		peony.Messages.STRING_00990
	};
	
	static int[] ratios = {
		0,15,30,45
	};

	public int getRatios(){
		return ratios[level];
	}
	
	public TongSkill9(int level) {
		super(9, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill9 clone() {
		return new TongSkill9(level);
	}

	public void fire(Player p) {
		
	}

	public String getDesc(int level) {
		return DESC[level];
	}

	public int getMaintainContribute(int level) {
		return MAINTAIN_CONTRIBUTE[level];
	}

	public int getMaintainMoney(int level) {
		return 0;
	}

	public int getUpgradeMoney(int level) {
		return 0;
	}

	public int getSkillType() {
		return TongSkill.SKILL_TYPE_PERSONAL;
	}
}
