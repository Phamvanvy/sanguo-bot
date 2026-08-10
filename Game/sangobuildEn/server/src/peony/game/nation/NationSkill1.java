package peony.game.nation;

import peony.game.Player;

public class NationSkill1 extends NationSkill {
	
	static final String NAME = "擂鼓助威";
	static final int MAXLEVEL = 5;
	
	static int[] UPGRADE_MONEY = { 0, 100000, 250000, 500000, 750000, 1500000, };
	
	static int[] MAINTAIN_MONEY = { 0, 20000, 50000, 100000, 150000, 300000, };
	
	static String[] DESC = { "擂鼓助威0級,無任何效果.",
		"擂鼓助威1級,瞬間恢复當前國戰場景內的國民50生命.",
		"擂鼓助威2級,瞬間恢复當前國戰場景內的國民100生命.", 
		"擂鼓助威3級,瞬間恢复當前國戰場景內的國民200生命.",
		"擂鼓助威4級,瞬間恢复當前國戰場景內的國民400生命.", 
		"擂鼓助威5級,瞬間恢复當前國戰場景內的國民和國都國公軍隊500生命." };
	
	public NationSkill1(int level){
		super(1,NAME,level,MAXLEVEL,NationSkill.TYPE_ITEM);
	}

	@Override
	public void fire(Player p) {
		
	}

	@Override
	public String getDesc(int level) {
		return DESC[level];	
	}

	@Override
	public int getMaintainMoney(int level) {
		return MAINTAIN_MONEY[level];
	}

	@Override
	public int getUpgradeMoney(int level) {
		return UPGRADE_MONEY[level];
	}
	
	@Override
	public NationSkill1 clone(){
		return new NationSkill1(level);
	}

}
