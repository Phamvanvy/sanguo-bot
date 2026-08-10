package peony.service.tong;

import peony.game.Player;

public class TongSkill6 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_01829;
	
	static final int MAXLEVEL = 3;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,200,400,800};
	
	static String[] DESC = { 
		peony.Messages.STRING_01829,
		peony.Messages.STRING_01830,
		peony.Messages.STRING_01831,
		peony.Messages.STRING_01832
	};
	
	static int[] ratios = {
		0,30,60,100
	};

	public int getRatios(){
		return ratios[level];
	}
	
	public TongSkill6(int level) {
		super(6, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill6 clone() {
		return new TongSkill6(level);
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
