package peony.service.tong;

import peony.game.Player;

public class TongSkill13 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_00419;
	
	static final int MAXLEVEL = 3;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,800,1600,2400};
	
	static String[] DESC = { 
		peony.Messages.STRING_00419,
		peony.Messages.STRING_00420,
		peony.Messages.STRING_00421,
		peony.Messages.STRING_00422
	};
	
	static int[] ratios = {
		0,6,12,18
	};

	public int getRatios(){
		return ratios[level];
	}
	
	public TongSkill13(int level) {
		super(13, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill13 clone() {
		return new TongSkill13(level);
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
