package peony.service.tong;

import peony.game.Player;

public class TongSkill1 extends TongSkill {

	static final String NAME = "药品专精";
	
	static final int MAXLEVEL = 5;
	
	static int[] UPGRADE_MONEY = {0,7500,15000,30000,60000,120000};
	
	static int[] MAINTAIN_MONEY = {0,750,1500,3000,6000,12000};
	
	static String[] DESC = { 
		"药品专精0级,无效果。",
		"药品专精1级,每次使用即时回复药品时补充气力\\精力上限提高2%",
		"药品专精2级,每次使用即时回复药品时补充气力\\精力上限提高4%",
		"药品专精3级,每次使用即时回复药品时补充气力\\精力上限提高8%",
		"药品专精4级,每次使用即时回复药品时补充气力\\精力上限提高16%",
		"药品专精5级,每次使用即时回复药品时补充气力\\精力上限提高32%"
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
