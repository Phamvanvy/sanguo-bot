package peony.game;

import peony.game.attendant.Attendant;
import peony.service.tong.Tong;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill2;


public class PropertyCalculator {
	
//	武将	3	0.5	3.5	1
//	刺客	1.5	3	2	1.5
//	炼气士	1	1.5	1.5	4
//	方士   	2	1	2	3

	public static final int[] INIT_ATTACKPOWER = { 15, 16, 8, 10 };
	public static final int[] INIT_DEFENSE = { 300, 250, 200, 150 };
	public static final int[] INIT_HP = { 50, 41, 34, 41 };
	public static final int[] INIT_MP = { 20, 25, 35, 28 };
	public static final int[] INIT_SPELLPOWER = { 0, 0, 10, 7 };
	public static final int[] INIT_SPELLDEFENSE = { 0, 0, 0, 0 };
	public static final float[] INIT_HIT = { 0.93F, 0.98F, 0.95F, 0.95F };
	public static final float[] INIT_SPELL_HIT = { 0.90F, 0.90F, 0.98F, 0.98F };
	public static final float[] INIT_DODGE = { 0.1F, 0.1F, 0.05F, 0.05F };
	public static final float[] INIT_SPELL_DODGE = { 0.0F, 0.0F, 0.0F, 0.0F };
	public static final float[] INIT_CRITICAL = { 0.015F, 0.027F, 0.01F, 0.01F };
	public static final float[] INIT_SPELLCRITICAL = { 0F, 0F, 0.03F, 0.026F };
	
	public int hp;
	public int mp;
	public int strength;
	public int agility;
	public int stamina;
	public int intellect;
    public int basichp;
    public int basicmp;
    public int basicstrength;
    public int basicagility;
    public int basicstamina;
    public int basicintellect;
	public float attackpowerup,attackpowerdown;
	public float spellpower;
	public float spellheal;
	public float defense;
	public float spelldefense;
	public float critical;
	public float spellcritical;
	public float hit;
	public float spellhit;
	public float dodge;
	public float spelldodge;
	public float anticrit;
	public int hitrating;
	public int spellhitrating;
	public int dodgerating;
	public int spelldodgerating;
	public int criticalrating;
	public int spellcriticalrating;
	public int anticritrating;
	protected float fastrating;   // 加速比例
	protected float fastrating2;   // 加速比例2
	protected float slowrating;  // 减速比例
	protected float horserating; // 马加速比例
	public int healthrestore; //生命回复
	public int manarestore; //魔法回复
	public float defensePercent;//物理减伤

	public float basicHpRate;
	public float basicMpRate;
	public float hpRate;
	public float mpRate;
	public float attackpowerRate;
	public float basicSpellPowerRate;
	public float spellpowerRate;
	public float spellhealRate;
	public float defenseRate;
	public float spellDefenseRate;
	
	public float expRatio = 1.0f;
	public float horseExpRatio = 1.0f;
	public float moneyRatio = 1.0f;
	public float rewardRation = 1.0f;
	
	public int speed;
	
	private int clazz,level;
	
	public float natualEnhance; //资质鉴定效果增强系数
	public float jewelEnhance; //宝石镶嵌效果增强系数
	public float playerJewelEnhance; //玩家宝石镶嵌效果增强系数
	public float horseJewelEnhance; //坐骑宝石镶嵌效果增强系数
	
	public Unit unit;
	
	public static float attendantValueRatio = 1f; //随从属性增强系数
	
	public static final int TYPE_PLAYER = 1; //所属为玩家
	public static final int TYPE_ATTENDANT = 2;	//所属为随从
	public static final int TYPE_HORSE = 3;	//所属为坐骑
	
	public PropertyCalculator(Horse horse){
		float[] f = horse.template.generateAttributes(horse.level, horse.fixCount);
		this.strength += f[0] + horse.strengthAdded;
		this.agility += f[1] + horse.agilityAdded;
		this.stamina += f[2] + horse.staminaAdded;
		this.intellect += f[3] + horse.intellectAdded;
		this.speed += f[4] + horse.speedAdded;
	}
	
	public PropertyCalculator(Unit unit){
		this.unit = unit;
		if(unit.type==GameObject.TYPE_PLAYER){
			initPlayer();
		}else if(unit.type==GameObject.TYPE_CREATURE){
			initCreature();
		}else if(unit.type==GameObject.TYPE_ATTENDANT){
			initAttendant();
		}
		if(unit instanceof Player){
			((Player)unit).timeRatio = new float[]{1,1,1,1};
		}
	}
	
//	public PropertyCalculator(Creature creature){
//		this.unit = creature;
//		initCreature();
//	}
//	
//	public PropertyCalculator(Player player){
//		this.unit = player;
//		init(unit.clazz,unit.level,unit.strengthAdded,unit.agilityAdded,unit.staminaAdded,unit.intellectAdded);
//	}
	
	protected void initCreature(){
		Creature creature = (Creature)unit;
		this.clazz = unit.clazz;
		this.level = unit.level;
		this.basichp = this.hp = creature.template.hp;
		this.basicmp = this.mp = creature.template.mp;;
		this.basicstrength = this.strength = creature.template.str;;
		this.basicagility = this.agility = creature.template.agi;
		this.basicstamina = this.stamina = creature.template.sta;
		this.basicintellect = this.intellect = creature.template.inte;
//		this.attackpowerup = this.attackpowerdown = INIT_ATTACKPOWER[clazz];
//		this.spellpower = INIT_SPELLPOWER[clazz];
		this.defense = 0;
		this.spelldefense = 0;
		this.hit = INIT_HIT[clazz];
		this.spellhit = INIT_SPELL_HIT[clazz];
//		this.dodge = INIT_DODGE[clazz];
//		this.spelldodge = INIT_SPELL_DODGE[clazz];
		this.dodge = 0.02F;
		this.spelldodge = 0.02F;
		this.critical = INIT_CRITICAL[clazz];
		this.spellcritical = INIT_SPELLCRITICAL[clazz];
		this.attackpowerdown += creature.template.weaponAP1;
		this.attackpowerup += creature.template.weaponAP2;
		this.spellpower += creature.template.weaponMagicAP;
		this.spellheal += creature.template.weaponMagicAP;
		this.defense += creature.template.armor;;
		this.spelldefense  += creature.template.magicArmor;
		this.anticrit = 0.0F;
	}
	
	
	protected void initPlayer(){
		this.clazz = unit.clazz;
		this.level = unit.level;
		int[] pros = getPlayerProperties(clazz,level);
		//军团做战图腾
		TongService ts = Server.server.getServiceRegistry().getTongService();
		Tong tong = ts.getPlayerTong(unit.id,false);
		int addnum = 0;
		if(tong!=null&&tong.ismaintain == TongService.MAINTAIN&&tong.skills!=null){
			TongSkill2 tskill = (TongSkill2)tong.skills.get(2);
			if(tskill != null){
				addnum = tskill.getValue();
			}
		}
		this.basicstrength = this.strength = pros[0] + unit.strengthAdded + addnum;
		this.basicagility = this.agility = pros[1] + unit.agilityAdded + addnum;
		this.basicstamina = this.stamina = pros[2] + unit.staminaAdded + addnum;
		this.basicintellect = this.intellect = pros[3] + unit.intellectAdded + addnum;
		basichp = hp = INIT_HP[clazz];
		basicmp = mp = INIT_MP[clazz];
		attackpowerup = attackpowerdown = INIT_ATTACKPOWER[clazz];
		spellpower = INIT_SPELLPOWER[clazz];
		spellheal = 0.0f;
		defense = INIT_DEFENSE[clazz];
		spelldefense = INIT_SPELLDEFENSE[clazz];
        hit = INIT_HIT[clazz];
        spellhit = INIT_SPELL_HIT[clazz];
        dodge = INIT_DODGE[clazz];
        spelldodge = INIT_SPELL_DODGE[clazz];
		critical = INIT_CRITICAL[clazz];
		spellcritical = INIT_SPELLCRITICAL[clazz];
		this.anticrit = 0.0F;
//		if(unit.isRide()){
//			this.setHorseSpeed(0.2f);
//		}
	}
	
	protected void initAttendant(){
		Attendant attendant = (Attendant)unit;
		this.basichp = attendant.attendantType.hp; //基础生命
		this.basicmp = attendant.attendantType.mp; //基础精力
		this.basicstrength = attendant.attendantType.str; //基础力
		this.basicagility = attendant.attendantType.agi; //基础敏
		this.basicstamina = attendant.attendantType.sta; //基础耐
		this.basicintellect = attendant.attendantType.inte; //基础智
		this.spelldefense = attendant.attendantType.magicArmor; //基础法防
		this.criticalrating = attendant.attendantType.critical; //物理暴击率
		this.spellcriticalrating = attendant.attendantType.spellcritical; //法术暴击率
		this.attackpowerdown += attendant.attendantType.weaponAP1; //攻击下限
		this.attackpowerup += attendant.attendantType.weaponAP2; //攻击上限
		this.defense += attendant.attendantType.armor; //基础护甲
		this.anticritrating = attendant.attendantType.decritical; //免爆
		this.spellpower += attendant.attendantType.weaponMagicAP; //法攻
	}
	
	public void caculate(){
//		生命值 = 初始生命值+耐力值 15+等级 30+装备加成+修正
//		物理系内力值 = 初始魔法值+力量值 *6+等级 *15+装备加成+修正
//      法系内力值 = 初始魔法值+智力值 *6+等级*15+装备加成+修正
//		武将物理攻击力 =（初始攻击力+力量值+武器加成+技能攻击力）+修正
//		刺客物理攻击力 =（初始攻击力+敏捷值+力量值/4+武器加成+技能攻击力）+修正
//		魔法攻击力 =（初始魔法攻击力+智力值+武器法伤加成+技能攻击力）+修正
//		谋士，方士物理攻击力 = （初始攻击力+力量值/2+武器物理攻击力加成）+修正
//		基础物理防御力 = 初始物理防御力+装备加成+修正 
		if(unit.type==GameObject.TYPE_PLAYER){
		    if (clazz == Unit.CLASS_1) {
		        hp += stamina*16+level*15;
		        basichp += basicstamina * 16 + level * 15;
		    } else if (clazz == Unit.CLASS_2){ 
                hp += stamina*16+level*15;
                basichp += basicstamina * 16 + level * 15;
		    } else if (clazz == Unit.CLASS_3){
                hp += stamina*16+level*15;
                basichp += basicstamina * 16 + level * 15;
		    } else {
		        hp += stamina*16+level*15;
		        basichp += basicstamina * 16 + level * 15;
		    }
			mp += level * 15;
			basicmp += level * 15;
			if (clazz == Unit.CLASS_1 || clazz == Unit.CLASS_2) {
			    mp += strength * 6;
			    basicmp += basicstrength * 6;
			} else {
			    mp += intellect*6;
			    basicmp += basicintellect * 6;
			}
		}
		hp += basichp * basicHpRate;
		mp += basicmp * basicMpRate;
		hp *= (1f + hpRate);
		mp *= (1f + mpRate);
		spellpower *= (1f + basicSpellPowerRate);
		if(clazz==Unit.CLASS_1){ //武将
			attackpowerup += strength;
			attackpowerdown += strength;
		}
		else if(clazz==Unit.CLASS_2){  //刺客
			attackpowerup += agility*3/4+strength/4;
			attackpowerdown += agility*3/4+strength/4;
		}
		else if(clazz==Unit.CLASS_3||clazz==Unit.CLASS_4){ //谋士，方士
			attackpowerup += strength/2;
			attackpowerdown += strength/2;
			spellpower += intellect;
		}
		spellheal += intellect * 0.1f;  //1智力加0.1的治疗
		attackpowerup *= (1f + attackpowerRate);
		attackpowerdown *= (1f + attackpowerRate);
		spellpower *= (1f + spellpowerRate);
        spellheal *= (1f + spellhealRate);
		defense *= (1f + defenseRate);
		criticalrating += agility / 4;            // 4点敏捷加1点物理暴击等级
		spellcriticalrating += intellect / 4;     // 4点智力加1点法术暴击等级
		critical += ratingToRate(criticalrating, unit.level);
        spellcritical += ratingToRate(spellcriticalrating, unit.level);
		hit += hitrating * 3.0f / (unit.level * 50);
        spellhit += spellhitrating * 3.0f / (unit.level * 50);
        float dodgeTemp = ratingToRate(dodgerating, unit.level);
        if(dodgeTemp<=0.53)
        	dodge += ratingToRate(dodgerating, unit.level);
        else
        	dodge += ratingToRateOfDodge(dodgerating, unit.level);
        float spellDodgeTemp = ratingToRate(spelldodgerating, unit.level);
        if(spellDodgeTemp<=0.63)
        	spelldodge += ratingToRate(spelldodgerating, unit.level);
        else
        	spelldodge += ratingToRateOfSpellDodge(spelldodgerating, unit.level);
        anticritrating += stamina / 8;           // 8点体力加1免暴等级
        float antiCritTemp = ratingToRate(anticritrating, unit.level);;
        if(antiCritTemp<=0.78)
        	anticrit += ratingToRate(anticritrating, unit.level);
        else
        	anticrit += ratingToRateOfAntiCrit(anticritrating, unit.level);
		if (clazz == Unit.CLASS_1 || clazz == Unit.CLASS_2) {
		    spelldefense += this.intellect / 4.0f;
		} else {
		    spelldefense += this.intellect / 6.0f;
		}
		spelldefense *= (1f + spellDefenseRate);
		defensePercent = calcDefensePercent(clazz, defense, level);
	}
	
	public void attendantPtyCaculate(){
		if(unit.type==GameObject.TYPE_ATTENDANT){
			strength += basicstrength;
			agility += basicagility;
			intellect += basicintellect;
			stamina += basicstamina;
			defense += strength * 2f + agility; 
			attackpowerup += strength + agility * 1f / 6f; //物攻
			attackpowerdown += strength + agility * 1f / 6f; //物攻
			spellpower += intellect * 7f / 10f; //法攻
			criticalrating += agility * 3f / 5f; //物理暴击等级
			critical += 1f - (1f / (1f + (criticalrating / 3000f))) + 0.02f; //物理暴击率
			spellcriticalrating += intellect / 10f; // 法术暴击等级
			spellcritical += 1f - (1f / (1f + (spellcriticalrating / 3000f))) + 0.02f; //法术暴击率
			anticritrating += stamina / 16f; //免爆等级
			anticrit  += 1f - (1f / (1f + (anticritrating / 3000f))); //免爆率
			defensePercent += 1f - (1f / (1f + (defense / 11770f))); //物理减伤
			//todo法术减伤
			dodge += 1f - (1f / (1f + (dodgerating / 1500f))) + 0.1f; //物闪
			spelldodge += 1f - (1f / (1f + (spelldodgerating / 1500f))) + 0.1f; //法闪
			hit += hitrating * (3f / 5000f) + 0.95f; //物理命中率
			spellhit += 0.97f; //法术命中
			if(spellhit<0)
				spellhit = 0;
			else if(spellhit>1)
				spellhit = 1;
			spelldefense += intellect * 1f / 10.0f; //法防
		}
	}
	
	/*
	 * 暴击等级/闪避等级/免暴等级转换为暴击率/闪避率/免暴率，当计算结果小于30%时，是线性公式；
	 * 当>30%时，触发递减效果。
	 */
	protected float ratingToRate(int rating, int level) {
	    float valve = 3 * (1 + level * 15) / 7.0f;
	    if (rating < valve) {
	        return rating * 0.3f / valve;
	    } else {
	        return 1.0f - 1.0f / (1.0f + rating / (1.0f + level * 15));
	    }
	}
	
	/**
	 * 新计算物理闪避公式
	 * @param rating
	 * @param level
	 * @return
	 */
	protected float ratingToRateOfDodge(int rating, int level) {
		return (68-(level*15+1)*(1/(1-0.53f)-1)*(68-53)/rating)/100;
	}
	
	/**
	 * 新计算法术闪避公式
	 * @param rating
	 * @param level
	 * @return
	 */
	protected float ratingToRateOfSpellDodge(int rating, int level) {
		return (78-(level*15+1)*(1/(1-0.63f)-1)*(78-63)/rating)/100;
	}
	
	/**
	 * 新计算免爆公式
	 * @param rating
	 * @param level
	 * @return
	 */
	protected float ratingToRateOfAntiCrit(int rating, int level) {
		return (89-(level*15+1)*(1/(1-0.78f)-1)*(89-78)/rating)/100;
	}
	
	/**
	 * 计算一个玩家的物理免伤比例。
	 * @param clazz 职业
	 * @param armor 护甲
	 * @param level 级别
	 * @return 0-1的免伤比例
	 */
	public static float calcDefensePercent(int clazz, float armor, int level) {
	    float actArmor;
	    if (clazz == Unit.CLASS_1) {
	        actArmor = armor * 1.2f;
	    } else if (clazz == Unit.CLASS_2) {
	        actArmor = armor * 0.7f;
	    } else {
	        actArmor = armor * 0.4f;
	    }
	    float def = 1.0f - 1.0f / (1 + actArmor / ((level + 9) * 51.3f));
	    if (def >= 0.99f) {
	        def = 0.99f;
	    }
	    if (def < 0) {
	        def = 0;
	    }
	    return def;
	}
	
	/**
	 * 计算一个随从的物理免伤比例。
	 * @param clazz 职业
	 * @param armor 护甲
	 * @param level 级别
	 * @return 0-1的免伤比例
	 */
	public static float calcAttendantDefensePercent(float armor, int level) {
		float actArmor;
		actArmor = armor;
		float def = 1.0f - 1.0f / (1 + actArmor / ((level + 9) * 51.3f));
		if (def >= 0.99f) {
			def = 0.99f;
		}
		if (def < 0) {
			def = 0;
		}
		return def;
	}
	
	/**
     * 计算一个玩家的法术免伤比例。
     * @param armor 法防
     * @param level 级别
     * @return 0-1的免伤比例
     */
    public static float calcSpellDefensePercent(float armor, int level) {
        float def = 1.0f - 1.0f / (1 + armor / ((level + 9) * 5.25f));
        if (def >= 0.99f) {
            def = 0.99f;
        }
        if (def < 0) {
            def = 0;
        }
        return def;
    }
    
    /**
     * 计算一个随从的法术免伤比例。
     * @param armor 法防
     * @param level 级别
     * @return 0-1的免伤比例
     */
    public static float calcAttendantSpellDefensePercent(float armor, int level) {
    	float def = 1.0f - 1.0f / (1 + armor / ((level + 9) * 5.25f));
    	if (def >= 0.99f) {
    		def = 0.99f;
    	}
    	if (def < 0) {
    		def = 0;
    	}
    	return def;
    }
	
	public static final float[] STRENGTH_GROWING = {3.0F,2.0F,1.0F,1.0F};
	public static final float[] AGILITY_GROWING = {2.0F,4.0F,1.0F,2.0F};
	public static final float[] STAMINA_GROWING = {4.0F,2.0F,2.0F,3.0F};
	public static final float[] INTELLECT_GROWING = {1.0F,2.0F,6.0F,4.0F};
	
	public static final int[] getPlayerProperties(int clazz,int level){
		int[] ret = new int[4];
		ret[0] = (int)(STRENGTH_GROWING[clazz]*level);
		ret[1] = (int)(AGILITY_GROWING[clazz]*level);
		ret[2] = (int)(STAMINA_GROWING[clazz]*level);
		ret[3] = (int)(INTELLECT_GROWING[clazz]*level);
		return ret;
	}
	
	public static final int[] getPlayerPropertiesDiff(int clazz,int oldLevel,int level){
		if(oldLevel>=level)
			throw new IllegalArgumentException();
		int[] ret = new int[4];
		int[] p1 = getPlayerProperties(clazz,oldLevel);
		int[] p2 = getPlayerProperties(clazz,level);
		for(int i=0,size=p1.length;i<size;i++){
			ret[i] = p2[i] - p1[i];
		}
		return ret;
	}

	/**
	 * 设置减速效果，只有最高一个起效。
	 * @param rate
	 */
	public void slow(float rate) {
		if (slowrating < rate) {
			slowrating = rate;
		}
	}
	
	/**
	 * 设置加速效果，只有最高一个起效。
	 * @param rate
	 */
	public void fast(float rate) {
		if (fastrating < rate) {
			fastrating = rate;
		}
	}
	
	/**
	 * 设置加速效果，只有最高一个起效。
	 * @param rate
	 */
	public void fast2(float rate) {
		if (fastrating2 < rate) {
			fastrating2 = rate;
		}
	}

	/**
	 * 设置坐骑加速。
	 */
	public void setHorseSpeed(float rate) {
		this.horserating = rate;
	}

	/**
	 * 取得速度因子（和基本速度对比）。
	 * @return
	 */
	public float getSpeed() {
		return 1.0f + horserating + fastrating + fastrating2 - slowrating; 
	}
	
	public PropertyCalculator clone(){
		PropertyCalculator pcRef = new PropertyCalculator(unit);
		pcRef.hp = hp;
		pcRef.mp = mp;
		pcRef.strength = strength;
		pcRef.agility = agility;
		pcRef.stamina = stamina;
		pcRef.intellect = intellect;
	    pcRef.basichp = basichp;
	    pcRef.basicmp = basicmp;
	    pcRef.basicstrength = basicstrength;
	    pcRef.basicagility = basicagility;
	    pcRef.basicstamina = basicstamina;
	    pcRef.basicintellect = basicintellect;
		pcRef.attackpowerup = attackpowerup;
		pcRef.attackpowerdown = attackpowerdown;
		pcRef.spellpower = spellpower;
		pcRef.spellheal = spellheal;
		pcRef.defense = defense;
		pcRef.spelldefense = spelldefense;
		pcRef.critical = critical;
		pcRef.spellcritical = spellcritical;
		pcRef.hit = hit;
		pcRef.spellhit = spellhit;
		pcRef.dodge = dodge;
		pcRef.spelldodge = spelldodge;
		pcRef.spelldodge = anticrit;
		pcRef.hitrating = hitrating;
		pcRef.spellhitrating = spellhitrating;
		pcRef.dodgerating = dodgerating;
		pcRef.spelldodgerating = spelldodgerating;
		pcRef.criticalrating = criticalrating;
		pcRef.spellcriticalrating = spellcriticalrating;
		pcRef.anticritrating = anticritrating;
		pcRef.fastrating = fastrating;
		pcRef.fastrating2 = fastrating2;
		pcRef.slowrating = slowrating;
		pcRef.horserating = horserating;
		pcRef.healthrestore = healthrestore;
		pcRef.manarestore = manarestore;
		pcRef.defensePercent = defensePercent;
		pcRef.basicHpRate = basicHpRate;
		pcRef.basicMpRate = basicMpRate;
		pcRef.hpRate = hpRate;
		pcRef.mpRate = mpRate;
		pcRef.attackpowerRate = attackpowerRate;
		pcRef.basicSpellPowerRate = basicSpellPowerRate;
		pcRef.spellpowerRate  = spellpowerRate;
		pcRef.spellhealRate = spellhealRate;
		pcRef.defenseRate = defenseRate;
		pcRef.spellDefenseRate = spellDefenseRate;
		pcRef.expRatio = expRatio;
		pcRef.horseExpRatio = horseExpRatio;
		pcRef.moneyRatio = moneyRatio;
		pcRef.rewardRation = rewardRation;
		pcRef.speed = speed;
		pcRef.clazz = clazz;
		pcRef.level = level;
		pcRef.natualEnhance = natualEnhance;
		pcRef.jewelEnhance = jewelEnhance;
		pcRef.playerJewelEnhance = playerJewelEnhance;
		pcRef.horseJewelEnhance = horseJewelEnhance;
		return pcRef;
	}
	
}
