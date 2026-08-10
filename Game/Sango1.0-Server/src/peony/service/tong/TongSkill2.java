package peony.service.tong;

import peony.game.Player;

public class TongSkill2 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_01132;
	
	static final int MAXLEVEL = 6;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,20,50,75,100,125,150};
	
	static String[] DESC = { 
		peony.Messages.STRING_01133,
		peony.Messages.STRING_01134,
		peony.Messages.STRING_01135,
		peony.Messages.STRING_01136,
		peony.Messages.STRING_01137,
		peony.Messages.STRING_01138,
		peony.Messages.STRING_01139
	};
	
	static int[] values = {
		0,10,20,30,40,50,60
	};

	public int getValue(){
		return values[level];
	}
	
	public TongSkill2(int level) {
		super(2, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill2 clone() {
		return new TongSkill2(level);
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
