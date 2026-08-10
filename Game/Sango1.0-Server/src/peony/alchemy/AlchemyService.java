package peony.alchemy;
import peony.game.GameObject;
import peony.game.Player;
import peony.game.PropertyCalculator;
import peony.game.Unit;
import peony.service.Service;

public class AlchemyService implements Service{
	public static final String PLAYEREXP_TODAYADD="PLAYEREXPTODAYADD";//玩家当天增加的经验值
	
	public static final String CURRENTDAY="CURRENTDAY";//用于玩家增加经验比较的日期
	
	public static final String ALCHEMYEXP="ALCHEMYEXPTODAYADD";//玩家已经得到修炼的经验值
	
	public static final String ALCHEMYEXP_USECALCULATE="ALCHEMYEXP_USECALCULATE";//用于计算的修炼经验值与ALCHEMYEXP同步
	
	public static final String ALCHEMYCOUNT_PLAYEREXP="ALCHEMYCOUNT";//每天的人物经验修炼次数4
	
	public static final String ALCHEMY_BYIMONEY_FIRST="ALCHEMYBYEMONEYFIRST";//第一次修炼暗箱
	
	public static final String ALCHEMY_HINT_TODAY="ALCHEMYHINTTODAY";//当重天可以突破时，每天第一次修炼给提示
	
	/**每次经验修炼需要的经验值,用于计算每次消耗  玩家等级*1969 */
	public static final int ALCHEMY_NEEDPLAYEREXP_ONCE=1969;
	
	/**每次元宝修炼所需元宝数*/
	public static final int ALCHEMY_NEEDIMONEY_ONCE=5;
	
	/**每天用经验修炼的最多次数*/
	public static final int ALCHEMY_ONEDAY_COUNT=4;
	
	/**修炼每次获得的修炼经验值*/
	public static final int ALCHEMY_EXP_ONCE=25;
	
	/**重天丸ID*/
	public static final int ALCHEMYBREAKLEVELITEM=4679;
	
	/**突破重天增加的物攻*/
	public static int BREAKLEVEL_ADDATTACK=150;

	/**突破重天增加的法攻*/
	public static int BREAKLEVEL_ADDSPELLPOWER=100;
	
	/**称号*/
	public static int ALCHEMY_TITLE=4685;
	
	public static String alchemyPropertyChangeInfo=
			"修炼为你增加了\n" +
			"物攻：<cff0000>{0}</c> 法攻：<cff0000>{1}</c>\n" +
			"护甲：<cff0000>{2}</c> 法防：<cff0000>{3}</c>\n" +
			"生命值：<cff0000>{4}</c>\n" +
			"宝石效果提升：<cff0000>{5}%</c>\n" +
			 "经验修炼：只能用本日所获人物经验进行修炼。\n" +
			 "元宝修炼：消耗<cff0000>{6}</c>元宝，可获得大量的修炼经验，有几率出现暴击使修炼经验翻倍。\n"+
			 "元宝百修：消耗<cff0000>{7}</c>元宝，直接修炼100次获得成吨的修炼经验，有几率出现暴击使修炼经验翻倍，有几率出现超级暴击获得10倍修炼经验。\n" +
			 "突破重天：每重天的经脉修满后，突破该重天都可直接获得150点物攻和100点法攻。\n";
//			 +"还差<cff0000>{6}</c>经验可进行下次修炼\n" +
//			 "您今日还剩<cff0000>{7}</c>次经验修炼机会\n";
	
	public static String alchemyByPlayerExpInfo=
		"还差<cff0000>{8}</c>经验可进行下次修炼\n" +
		"您今日还剩<cff0000>{9}</c>次经验修炼机会\n";;
	
	public static final String[] PROPERTY_TYPE_NAMES={
		"物攻",
		"法攻",
		"生命值",
		"护甲",
		"法防",
		"宝石效果",
	};
	
	public static final int ATTACK=0;
	public static final int SPELLATTACK=1;
	public static final int HP=2;
	public static final int DEFENSE=3;
	public static final int SPELLDEF=4;
	public static final int JEWEL=5;
	
	
	public static final int PULSE_1=0;//任脉 
	public static final int PULSE_2=1;//督脉
	public static final int PULSE_3=2;//带脉
	public static final int PULSE_4=3;//冲脉 
	public static final int PULSE_5=4;//阳跷脉 
	
	public static float PROPERTIES[]={//6种属性-("物攻","法攻","生命值","护甲","法防","宝石效果")
		1.6f,
		1f,
		17.8f,
		11.8f,
		2.5f,
		0.106f
	};
	
	/***
	 * 
	 * @param propertyType		要获取的属性类型 6种属性-("物攻","法攻","生命值","护甲","法防","宝石效果")1-6
	 * @param level				当前重天0-4
	 * @param pulseIndex		当前经脉0-4
	 * @param acupointNum		当前穴位0-8
	 * @param acupointLevel		当前穴位等级0-10
	 * @return
	 */
	public static float getProperties_Value(int propertyType,int currentLevel,int currentPulseIndex,int currentAcupoint,int currentAcupointLevel){
		float attackPower=0;
		attackPower=PROPERTIES[propertyType]*10*9*(propertyType<=1?currentLevel:currentLevel-propertyType+1);
		if(propertyType<=1){
			attackPower+=currentPulseIndex>(propertyType<=1?0:propertyType)?PROPERTIES[propertyType]*10*9:PROPERTIES[propertyType]*10*currentAcupoint+PROPERTIES[propertyType]*currentAcupointLevel;
		}else{
			if(currentPulseIndex>propertyType-1){
				attackPower+=PROPERTIES[propertyType]*10*9;
			}else if(currentPulseIndex==propertyType-1){
				attackPower+=PROPERTIES[propertyType]*10*currentAcupoint+PROPERTIES[propertyType]*currentAcupointLevel;
			}
		}
		return attackPower;
	}

	/***
	 * [脉(重天数)][脉数]
	 */
	public static int PROPERTY_ALCHEMY_EXP[][]={
		{350,475,850,1150,1575},//任脉
		{0,600,1075,1450,1950},	//督脉
		{0,0,1425,1925,2600},	//带脉
		{0,0,0,2875,3900},		//冲脉
		{0,0,0,0,7800}			//阳跷脉
	};
	
	public void startup() throws Exception {
	}
	
	/**获取玩家每次修炼所需经验值*/
	public synchronized int getDecPlayerExp(int playerLevel){
		return ALCHEMY_NEEDPLAYEREXP_ONCE*playerLevel;
	}
	
	/**获取当前修炼等级需要经验*/
	public synchronized int getCurrentAlchemyNeedExp(int currentLevel,int currentPulse){
		return PROPERTY_ALCHEMY_EXP[currentPulse][currentLevel];
	}
	
	/**
	 * 重新计算各种属性增加值
	 * @param pc
	 * @param practiceLevel
	 * @param pulseIndex
	 * @param acupointNum
	 * @param acupointLevel
	 */
	public synchronized void enhanceAlchemyValue(PropertyCalculator pc,int practiceLevel,int pulseIndex,int acupointNum,int acupointLevel){
		float attackPowerup=BREAKLEVEL_ADDATTACK*practiceLevel;
		float spellPower=BREAKLEVEL_ADDSPELLPOWER*practiceLevel;
		float hp=0;
		float defense=0;
		float spellDefense=0;
		float jewelEnhance=0;
		
		switch(practiceLevel){
		case 4:
			jewelEnhance=getProperties_Value(JEWEL, practiceLevel, pulseIndex, acupointNum, acupointLevel);
		case 3:
			spellDefense=getProperties_Value(SPELLDEF, practiceLevel, pulseIndex, acupointNum, acupointLevel);
		case 2:
			defense=getProperties_Value(DEFENSE, practiceLevel, pulseIndex, acupointNum, acupointLevel);
		case 1:
			hp=getProperties_Value(HP, practiceLevel, pulseIndex, acupointNum, acupointLevel);
		case 0:
			attackPowerup+=getProperties_Value(ATTACK, practiceLevel, pulseIndex, acupointNum, acupointLevel);
			spellPower+=getProperties_Value(SPELLATTACK, practiceLevel, pulseIndex, acupointNum, acupointLevel);
			break;
		}
		pc.attackpowerup+=attackPowerup;
		pc.attackpowerdown+=attackPowerup;
		pc.spellpower+=spellPower;
		pc.hp+=hp;
		pc.defense+=defense;
		pc.spelldefense+=spellDefense;
		pc.jewelEnhance+=jewelEnhance/100f;
		//治疗
		if(pc.unit!=null&&pc.unit.type==GameObject.TYPE_PLAYER){
			Player player=(Player)pc.unit;
			if(player.clazz==Unit.CLASS_4){
				pc.spellheal+=spellPower;
			}
		}
	}
	
	/**
	 * 直接计算各种属性增加值
	 * @param pc
	 * @param attackPowerup
	 * @param spellPower
	 * @param hp
	 * @param defense
	 * @param spellDefense
	 * @param jewelEnhance
	 */
	public void enhanceAlachemyValue(PropertyCalculator pc,float attackPowerup,float spellPower,float hp,float defense,float spellDefense,float jewelEnhance){
		pc.attackpowerup+=attackPowerup;
		pc.spellpower+=spellPower;
		pc.hp+=hp;
		pc.defense+=defense;
		pc.spelldefense+=spellDefense;
		pc.jewelEnhance+=jewelEnhance/100f;
		//治疗
		if(pc.unit!=null&&pc.unit.type==GameObject.TYPE_PLAYER){
			Player player=(Player)pc.unit;
			if(player.clazz==Unit.CLASS_4){
				pc.spellheal+=spellPower;
			}
		}
	}

	public void shutdown() {
		
	}
}
