package peony.game.nation;

import peony.game.Player;

public class NationSkill1 extends NationSkill {
	
	static final String NAME = peony.Messages.STRING_00475;
	static final int MAXLEVEL = 5;
	
	static int[] UPGRADE_MONEY = { 0, 100000, 250000, 500000, 750000, 1500000, };
	
	static int[] MAINTAIN_MONEY = { 0, 20000, 50000, 100000, 150000, 300000, };
	
	static String[] DESC = { peony.Messages.STRING_00476,
		peony.Messages.STRING_00477,
		peony.Messages.STRING_00478, 
		peony.Messages.STRING_00479,
		peony.Messages.STRING_00480, 
		peony.Messages.STRING_00481 };
	
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
