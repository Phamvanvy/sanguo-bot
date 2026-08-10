package peony.service.tong;

import peony.game.Player;

public class TongSkill7 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_01717;
	
	static final int MAXLEVEL = 3;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,200,400,600};
	
	static String[] DESC = { 
		peony.Messages.STRING_01717,
		peony.Messages.STRING_01718,
		peony.Messages.STRING_01719,
		peony.Messages.STRING_01720
	};
	
	static int[] ratios = {
		0,7,14,20
	};

	public int getRatios(){
		return ratios[level];
	}
	
	public TongSkill7(int level) {
		super(7, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill7 clone() {
		return new TongSkill7(level);
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
