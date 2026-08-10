package peony.service.tong;

import peony.game.Player;

public class TongSkill1 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_00026;
	
	static final int MAXLEVEL = 6;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,10,20,30,40,50,60};
	
	static String[] DESC = { 
		peony.Messages.STRING_00027,
		peony.Messages.STRING_00028,
		peony.Messages.STRING_00029,
		peony.Messages.STRING_00030,
		peony.Messages.STRING_00031,
		peony.Messages.STRING_00032,
		peony.Messages.STRING_00033
	};
	
	static int[] ratios = {
		0,2,4,6,8,10,12
	};
	
	public TongSkill1(int level) {
		super(1, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill clone() {
		return new TongSkill1(level);
	}

	public void fire(Player p) {
		
	}

	public String getDesc(int level) {
		return DESC[level];
	}

	public int getMaintainContribute(int level) {
		return MAINTAIN_CONTRIBUTE[level];
	}

	public int getRatio(){
		return ratios[level];
	}

	public int getMaintainMoney(int level) {
		return 0;
	}

	public int getUpgradeMoney(int level) {
		return 0;
	}

	public int getSkillType() {
		return TongSkill.SKILL_TYPE_TONG;
	}
	
}
