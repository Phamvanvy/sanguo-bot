package peony.service.tong;

import peony.game.Player;

public class TongSkill8 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_01322;
	
	static final int MAXLEVEL = 2;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,300,1000};
	
	static String[] DESC = { 
		peony.Messages.STRING_01322,
		peony.Messages.STRING_01323,
		peony.Messages.STRING_01324,
	};
	
	static int[] ratios = {
		0,30,100
	};

	public int getRatios(){
		return ratios[level];
	}
	
	public TongSkill8(int level) {
		super(8, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill8 clone() {
		return new TongSkill8(level);
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
