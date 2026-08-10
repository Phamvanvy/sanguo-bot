package peony.game.nation;

import peony.game.Player;

public class NationSkill2 extends NationSkill {

	static final String NAME = "打孔大师";
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
		"打孔大师0级,无任何效果。",
		"打孔大师1级,提高当前打孔成功率的20%",
		"打孔大师2级,提高当前打孔成功率的40%",
		"打孔大师3级,提高当前打孔成功率的60%",
		"打孔大师4级,提高当前打孔成功率的80%",
		"打孔大师5级,提高当前打孔成功率的100%",

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
