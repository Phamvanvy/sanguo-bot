package peony.service.tong;

import peony.game.Player;

public class TongSkill11 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_00855;
	
	static final int MAXLEVEL = 3;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,400,800,1200};
	
	static String[] DESC = { 
		peony.Messages.STRING_00855,
		peony.Messages.STRING_00857,
		peony.Messages.STRING_00858,
		peony.Messages.STRING_00859
	};
	
	static int[] ratios = {
	};

	public int getRatios(){
		return 0;
	}
	
	public TongSkill11(int level) {
		super(11, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill11 clone() {
		return new TongSkill11(level);
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
