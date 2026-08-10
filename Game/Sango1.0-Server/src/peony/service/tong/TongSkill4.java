package peony.service.tong;

import peony.game.Player;

public class TongSkill4 extends TongSkill {
	
	static final String NAME = peony.Messages.STRING_00484;
	
	static final int MAXLEVEL = 2;
	
	static int[] MAINTAIN_CONTRIBUTE = {0,100,200};
	
	static String[] DESC = { 
		peony.Messages.STRING_00484,
		peony.Messages.STRING_00485,
		peony.Messages.STRING_00486
	};
	
	static int[] value = {
		20,30,40
	};

	public int getValue(){
		return value[level];
	}
	
	public TongSkill4(int level) {
		super(4, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
	}

	public TongSkill4 clone() {
		return new TongSkill4(level);
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
