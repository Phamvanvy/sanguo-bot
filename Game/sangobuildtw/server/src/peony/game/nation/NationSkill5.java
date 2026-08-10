package peony.game.nation;

import peony.game.Player;

public class NationSkill5 extends NationSkill {

	static final String NAME = "皇恩浩蕩";
	
	static final int MAXLEVEL = 4;
	
	static int[] UPGRADE_MONEY = {0,400000,800000,1600000,3200000};
	
	static int[] MAINTAIN_MONEY = {0,40000,80000,160000,320000};
	
	static String[] DESC = { 
		"皇恩浩蕩0級,無任何效果.",
		"皇恩浩蕩1級,國戰胜利獲得的珍珠增加1顆",
		"皇恩浩蕩2級,國戰胜利獲得的珍珠增加2顆",
		"皇恩浩蕩3級,國戰胜利獲得的珍珠增加3顆",
		"皇恩浩蕩4級,國戰胜利獲得的珍珠增加4顆",
	};
	
	public NationSkill5(int level) {
		super(5, NAME, level, MAXLEVEL, NationSkill.TYPE_NONE);
	}

	public NationSkill clone() {
		return new NationSkill5(level);
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
	
	public int getItemAccount(int level){
		switch(level){
			case 0:
				return 3;
			case 1:
				return 4;
			case 2:
				return 5;
			case 3:
				return 6;
			case 4:
				return 7;
		}
		return 0;
	}

}
