package peony.service.tong;

import peony.game.Player;

public class TongSkill10 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_00856;
	
	static final int MAXLEVEL = 3;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,300,600,1200};
	
	static String[] DESC = { 
		peony.Messages.STRING_00856,
		peony.Messages.STRING_01122,
		peony.Messages.STRING_01123,
		peony.Messages.STRING_01124
	};
	
	static int[] ratios = {
		0,15,30,45
	};

	public int getRatios(){
		return ratios[level];
	}
	
	public TongSkill10(int level) {
		super(10, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill10 clone() {
		return new TongSkill10(level);
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
