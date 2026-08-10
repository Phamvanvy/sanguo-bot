package peony.game.nation;

import peony.game.Player;

public class NationSkill2 extends NationSkill {

	static final String NAME = peony.Messages.STRING_01168;
	static final int MAXLEVEL = 5;
	
	static int[] UPGRADE_MONEY = { 0,
		200000,
		400000,
		800000,
		1600000,
		3200000,
	};
	
	static int[] MAINTAIN_MONEY = { 0,
		20000,
		40000,
		80000,
		160000,
		320000,
	};
	
	static float[] ADDHOLD_ADDED = {
		0f,
		0.2f,
		0.4f,
		0.6f,
		0.8f,
		1.0f,
	};
	
	static String[] DESC = { 
		peony.Messages.STRING_01169,
		peony.Messages.STRING_01170,
		peony.Messages.STRING_01171,
		peony.Messages.STRING_01172,
		peony.Messages.STRING_01173,
		peony.Messages.STRING_01174,

	};
	
	public NationSkill2(int level){
		super(2,NAME,level,MAXLEVEL,NationSkill.TYPE_NONE);
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
	
	public float getAddHoleAdded(){
		return ADDHOLD_ADDED[level];
	}
	
	@Override
	public NationSkill2 clone(){
		return new NationSkill2(level);
	}

}
