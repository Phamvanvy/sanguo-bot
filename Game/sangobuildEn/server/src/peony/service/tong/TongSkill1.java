package peony.service.tong;

import peony.game.Player;

public class TongSkill1 extends TongSkill {

	static final String NAME = "藥品專精";
	
	static final int MAXLEVEL = 5;
	
	static int[] UPGRADE_MONEY = {0,7500,15000,30000,60000,120000};
	
	static int[] MAINTAIN_MONEY = {0,750,1500,3000,6000,12000};
	
	static String[] DESC = { 
		"藥品專精0級,無效果.",
		"藥品專精1級,每次使用即時回复藥品時補充气力\\精力上限提高2%",
		"藥品專精2級,每次使用即時回复藥品時補充气力\\精力上限提高4%",
		"藥品專精3級,每次使用即時回复藥品時補充气力\\精力上限提高8%",
		"藥品專精4級,每次使用即時回复藥品時補充气力\\精力上限提高16%",
		"藥品專精5級,每次使用即時回复藥品時補充气力\\精力上限提高32%"
	};
	
	static int[] ratios = {
		0,2,4,8,16,32
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

	public int getMaintainMoney(int level) {
		return MAINTAIN_MONEY[level];
	}

	public int getUpgradeMoney(int level) {
		return UPGRADE_MONEY[level];
	}
	
	public int getRatio(){
		return ratios[level];
	}

}
