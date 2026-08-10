package peony.service.tong;

import peony.game.Player;
import peony.service.tong.TongSkill;

public class TongSkill2 extends TongSkill {

	static final String NAME = "攻城车建造";

	static final int MAXLEVEL = 1;

	static int[] UPGRADE_MONEY = { 0, 70000 };

	static int[] MAINTAIN_MONEY = { 0, 7000 };

	static String[] DESC = { 
		"攻城车建造0级,无效果。", 
		"攻城车建造1级，城战中可使用攻城车"
	};

	public TongSkill2(int level) {
		super(2, NAME, level, MAXLEVEL, TongSkill.TYPE_NONE);
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

	public TongSkill clone() {
		return new TongSkill2(level);
	}

}
