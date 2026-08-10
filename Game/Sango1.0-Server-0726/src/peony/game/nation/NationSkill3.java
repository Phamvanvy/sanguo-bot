package peony.game.nation;

import peony.game.Player;

public class NationSkill3 extends NationSkill {

	static final String NAME = "驯兽大师";
	static final int MAXLEVEL = 5;
	
	static int[] UPGRADE_MONEY = { 
		0,
		150000,
		300000,
		600000,
		1200000,
		2400000,
	};
	
	static int[] MAINTAIN_MONEY = { 
		0,
		15000,
		30000,
		60000,
		120000,
		240000,
	};
	
	static float[] EXPRATIO = {
		0f,
		0.04f,
		0.08f,
		0.12f,
		0.16f,
		0.20f,
	};
	
	static String[] DESC = { 
		"坐骑经验大师0级,无任何效果。",
		"坐骑经验大师1级,坐骑获得经验额外增加4%",
		"坐骑经验大师2级,坐骑获得经验额外增加8%",
		"坐骑经验大师3级,坐骑获得经验额外增加12%",
		"坐骑经验大师4级,坐骑获得经验额外增加16%",
		"坐骑经验大师5级,坐骑获得经验额外增加20%",

	};
	
	public NationSkill3(int level){
		super(3,NAME,level,MAXLEVEL,NationSkill.TYPE_NONE);
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
	
	public float getExpRatio(){
		return EXPRATIO[level];
	}
	
	@Override
	public NationSkill3 clone(){
		return new NationSkill3(level);
	}

}

