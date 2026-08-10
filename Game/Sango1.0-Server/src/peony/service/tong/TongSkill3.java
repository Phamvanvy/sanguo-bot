package peony.service.tong;

import peony.game.Player;

public class TongSkill3 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_01293;
	
	static final int MAXLEVEL = 6;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,15,30,45,60,75,90};
	
	static String[] DESC = { 
		peony.Messages.STRING_01293,
		peony.Messages.STRING_01294,
		peony.Messages.STRING_01295,
		peony.Messages.STRING_01296,
		peony.Messages.STRING_01297,
		peony.Messages.STRING_01298,
		peony.Messages.STRING_01299,
	};
	
	static int[] ratios = {
		0,50,100,150,200,250,300
	};

	public int getRatio(){
		return ratios[level];
	}
	
	public TongSkill3(int level) {
		super(3, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill3 clone() {
		return new TongSkill3(level);
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
		return TongSkill.SKILL_TYPE_TONG;
	}
}
