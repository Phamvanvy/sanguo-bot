package peony.game.nation;

import peony.game.Player;

public class NationSkill1 extends NationSkill {
	
	static final String NAME = "擂鼓助威";
	static final int MAXLEVEL = 5;
	
	static int[] UPGRADE_MONEY = { 0, 100000, 250000, 500000, 750000, 1500000, };
	
	static int[] MAINTAIN_MONEY = { 0, 20000, 50000, 100000, 150000, 300000, };
	
	static String[] DESC = { "擂鼓助威0级,无任何效果。",
		"擂鼓助威1级,瞬间恢复当前国战场景内的国民50生命。",
		"擂鼓助威2级,瞬间恢复当前国战场景内的国民100生命。", 
		"擂鼓助威3级,瞬间恢复当前国战场景内的国民200生命。",
		"擂鼓助威4级,瞬间恢复当前国战场景内的国民400生命。", 
		"擂鼓助威5级,瞬间恢复当前国战场景内的国民和国都国公军队500生命。" };
	
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
