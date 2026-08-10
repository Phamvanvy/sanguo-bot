package peony.service.tong;

import peony.game.Player;

public class TongSkill12 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_01714;
	
	static final int MAXLEVEL = 2;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,500,1500};
	
	static String[] DESC = { 
		peony.Messages.STRING_01714,
		peony.Messages.STRING_01715,
		peony.Messages.STRING_01716,
	};
	
	static int[] ratios = {
		0,80,85
	};

	public int getRatios(){
		return ratios[level];
	}
	
	public TongSkill12(int level) {
		super(12, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill12 clone() {
		return new TongSkill12(level);
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
