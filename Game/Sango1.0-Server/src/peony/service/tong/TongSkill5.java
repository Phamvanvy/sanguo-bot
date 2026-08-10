package peony.service.tong;

import peony.game.Player;

public class TongSkill5 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_00816;
	
	static final int MAXLEVEL = 1;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,300};
	
	static String[] DESC = { 
		peony.Messages.STRING_00816,
		peony.Messages.STRING_00817
	};
	
	static int[] value = {
		0,300
	};

	public int getValue(){
		return value[level];
	}
	
	public TongSkill5(int level) {
		super(5, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill5 clone() {
		return new TongSkill5(level);
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
