package com.pip.itimes.server.stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Pet implements IPet{
	
	public final static int CURRENT_VERSION = 9;
	
	/**
	 * 力量型
	 */
    public static final int TYPE_0 = 1;
    /**
	 * 智力型
	 */
    public static final int TYPE_1 = 2;
    /**
	 * 体力型
	 */
    public static final int TYPE_2 = 3;
    /**
	 * 敏捷型
	 */
    public static final int TYPE_3 = 4;
    /**
	 * 智力体力型
	 */
    public static final int TYPE_4 = 5;
    /**
	 * 力量敏捷型
	 */
    public static final int TYPE_5 = 6;

    private int itemId;
    private int id;
    private String name;
    private int petType;
    private int level;
    private int currentPoint;
    private int point;
    private int favor;
    private int nextExp;
    /**
     * 敏捷
     */
    private int agility;
    private int agilityDevelop;
    /**
     * 力量
     */
    private int strength;
    private int strengthDevelop;
    /**
     * 体力
     */
    private int vitality; 
    private int vitalityDevelop; 
    /**
     * 智力
     */
    private int intelligence;
    private int intelligenceDevelop;
    /**
     * 灵性等级
     */
    private int spiritualityLevel;
    /**
     * 宠物当前悟性经验
     */
    private int perceptionPoint;
    /**
     * 宠物当前悟性经验上线
     */
    private int nextPerceptionPoint;
    /**
     * 宠物悟性等级
     */
    private int perceptionLevel;
    
    private int hp;
    private int mp;
    private boolean baby;
    private int exp;

    private String desc;
  //jwp add 宠我一生
    private int maxEnchancePoint; 
    private int currentEnchancePoint;

	private int enhancestrength;//力量
    private int enhanceintelligence;//智力
    private int enhancevitality;//敏捷
    private int enhanceagility;//体力
    private List<PetEnhance> petEnhances = new ArrayList<PetEnhance>(40);
    
    protected List PetBufs = new ArrayList();//存宠物阵营宝石buff
    
    //宠物穿的装备
    protected Grid[] usedEquipments = new Grid[9];
    //0头；1项链；2甲；3腰带；4护腕；5戒指；6鞋；7武器；8盾牌；
    protected int[] usedEquinfo = {0,0,0,0,2,2,2,0,0};
    
    /**
     * 添加宠物的绑定类型和绑定状态
     */
    private byte bindType;
    private boolean binded;
    
    private short colorIndex;//宠物当前颜色
    private short colorIndexBack;//临时保存宠物随机得到的颜色
    
    private int evolutionLevel;	//宠物进化等级
    private int evolutionPoint;	//当前的占卜之力值
    private int evolutionType;	//进化的类型
    
    private ArrayList<Integer> evolutionOpenPoints = new ArrayList<Integer>();	//翻牌的备份号码
    
    public List<PetEnhance> getPetEnhances() {
		return petEnhances;
	}

	public void setPetEnhances(List<PetEnhance> petEnhances) {
		this.petEnhances = petEnhances;
	}
	
	// petversion >= 4 炼化星等，改为悟性等级
	private String enhanceName = ""; 
   
	public String getEnhanceName() {
		return enhanceName;
	}
	
	/**
	 * petversion >= 4 炼化星等，改为悟性等级
	 * */
	public void setEnhanceName(String enhanceName) {
		this.enhanceName = enhanceName;
	}
	//jwp add end 
    //jwp add end 
    public int getmaxEnchancePoint() {
		return maxEnchancePoint;
	}

	public void setmaxEnchancePoint(int maxEnchancePoint) {
		this.maxEnchancePoint = maxEnchancePoint;
	}

	public int getCurrentEnchancePoint() {
		return currentEnchancePoint;
	}

	public void setCurrentEnchancePoint(int currentEnchancePoint) {
		this.currentEnchancePoint = currentEnchancePoint;
	}
   
    public int getEnhanceagility() {
		return enhanceagility;
	}

	public void setEnhanceagility(int enhanceagility) {
		this.enhanceagility = enhanceagility;
	}

	public int getEnhancestrength() {
		return enhancestrength;
	}

	public void setEnhancestrength(int enhancestrength) {
		this.enhancestrength = enhancestrength;
	}

	public int getEnhancevitality() {
		return enhancevitality;
	}

	public void setEnhancevitality(int enhancevitality) {
		this.enhancevitality = enhancevitality;
	}

	public int getEnhanceintelligence() {
		return enhanceintelligence;
	}

	public void setEnhanceintelligence(int enhanceintelligence) {
		this.enhanceintelligence = enhanceintelligence;
	}
    private List abilities = new ArrayList();
    private List abilitiesdata = new ArrayList();
    public static final int PET_ABILITY_EVOLUTION_MAX  = 1;			//宠物进化为圣宠是圣宠技能上限
    public static final int PET_ABILITY_MAX = 9;
	public static final int PET_ABILITY_LOCK_MAX = 3;		//宠物技能锁定上限 目前为3个
	private byte[] abilitiesLock = new byte[PET_ABILITY_MAX];
//    private int renameTimes;

    public Pet() {
    }
	public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public byte getType(){
        return IItem.TYPE_PET;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getItemId(){
        return itemId;
    }

    public void setItemId(int itemId){
        this.itemId = itemId;
    }

    public int getPetType(){
        return petType;
    }

    public void setPetType(int type){
        this.petType = type;
    }

    public int getLevel(){
        return level;
    }

    public byte getBindType(){
//        return IItem.BIND_NO;
    	return bindType;
    }
    
    public void setBindType(byte bindType){
    	this.bindType = bindType; 
    }

    public void setBinded(boolean binded){
    	this.binded = binded;
    }
    
    public boolean getBinded(){
    	return binded;
    }
    
    public void setLevel(int level){
        this.level = level;
    }
    
    public void setNextExp(int nextExp){
        this.nextExp = nextExp;
    }

    public int getCurrentPoint(){
        return currentPoint;
    }

    public void setCurrentPoint(int currentPoint){
        this.currentPoint = currentPoint;
    }

    public int getPoint(){
        return point;
    }

    public void setPoint(int point){
        this.point = point;
    }

    public int getFavor(){
        return favor;
    }

    public void setFavor(int favor){
        this.favor = favor;
    }

    public int getAgility(){
        return agility;
    }
    
    public int getAgilityDevelop(){
    	return agilityDevelop;
    }

    public void setAgility(int agility){
        this.agility = agility;
    }
    
    public void setAgilityDevelop(int agilityDevelop){
    	this.agilityDevelop = agilityDevelop;
    }

    public int getStrength(){
        return strength;
    }
    
    public int getStrengthDevelop(){
    	return strengthDevelop;
    }

    public void setStrength(int strength){
        this.strength = strength;
    }
    
    public void setStrengthDevelop(int strengthDevelop){
    	this.strengthDevelop = strengthDevelop;
    }

    public int getVitality(){
        return vitality;
    }
    
    public int getVitalityDevelop(){
    	return vitalityDevelop;
    }

    public void setVitality(int vitality){
        this.vitality = vitality;
    }
    
    public void setVitalityDevelop(int vitalityDevelop){
    	this.vitalityDevelop = vitalityDevelop;
    }

    public int getIntelligence(){
        return intelligence;
    }
    
    public int getIntelligenceDevelop(){
    	return intelligenceDevelop;
    }

    public void setIntelligence(int intelligence){
        this.intelligence = intelligence;
    }
    
    public void setIntelligenceDevelop(int intelligenceDevelop){
    	this.intelligenceDevelop = intelligenceDevelop;
    }

    public int getHp(){
        return hp;
    }

    public void setHp(int hp){
        this.hp = hp;
    }

    public int getMp(){
        return mp;
    }

    public void setMp(int mp){
        this.mp = mp;
    }

    public void setBaby(boolean baby){
        this.baby = baby;
    }

    public boolean getBaby(){
        return baby;
    }

    public void setExp(int exp){
        this.exp = exp;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getExp(){
        return exp;
    }

    public String getDesc() {
        return desc;
    }
    
    public short getColorIndex(){
    	return colorIndex;
    }
    
    public void setColorIndex(short index){
    	colorIndex = index;
    	colorIndexBack = 0;
    }
    
    public void setColorIndexBack(short index){
    	colorIndexBack = index;
    }
    
    public short getColorIndexBack(){
    	return colorIndexBack;
    }
    
    public void addAbility(Ability ability){
    	addAbility(ability, new AbilityData());
    }
    
    public void addAbility(Ability ability, AbilityData abilitydata){
		abilities.add(ability);
		abilitiesdata.add(abilitydata);
    }
    
    public Ability[] addEvoAbilities(int[] except){
    	Ability[] newAbilities = Utils.getPetAbilities(getType(), 1, except,1029);
    	for(int i=0; i<newAbilities.length; i++){
    		AbilityData data = new AbilityData();
    		data.setIsSaint(1);
    		addAbility(newAbilities[i], data);
    	}
    	return newAbilities;
    	/*int[] skillId = new int[newAbilities.length];
    	for(int i=0; i<newAbilities.length; i++){
    		skillId[i] = newAbilities[i].getId();
    	
    	}
    	PetSaintSkill petSaintSkill = null;
    	if(petSaintSkillMap.containsKey(""+player.getId()+pet.getId())){
		
    	}else{
    		petSaintSkill = new PetSaintSkill(player.getId(),pet.getName(),pet.getId(),pet.getEvolutionLevel(),skillId);
    		petSaintSkillMap.put(""+player.getId()+pet.getId(), petSaintSkill);
    		
    	}
    	setPetSaintSkillList(petSaintSkillMap);
		saveData();*/
    }
    public void setAbilities(Ability[] as){
        abilities.clear();
        abilitiesdata.clear();
        for(int i=0;i<as.length;i++){
        	addAbility(as[i]);
        }
    }
    
    public boolean hasAbility(Ability ability){
        for(int i=0;i<abilities.size();i++){
            Ability ab = (Ability)abilities.get(i);
            if(ab.getId()==ability.getId())
                return true;
        }
        return false;
    }
    //mengjie add 宠物穿装备计算属性
    /**
     * petversion >=4 增加宠物悟性奖励提成
     */
    public int getRealVitality() {
        int vitality_result = vitality + enhancevitality +
        		vitalityDevelop +
                       getUsedEquipmentProperty(IEquipment.EQUIP_ADD_VIT);
        
        //加上附魔属性
        vitality_result += getUsedEquipmentEnchantingProperty(IEquipment.EQUIP_ADD_VIT);
        //加阵营宝石
        vitality_result += getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_VIT);
        //悟性分级
        int point = getPetPerceptionSubLevel();
        vitality_result = vitality_result * (Utils.PET_PERCEPTION_AWARD[getPerceptionLevel()] + point) / 10000;
        
        return vitality_result;
    }

    public int getRealStrength() {
        int strength_result = strength + enhancestrength + 
        		strengthDevelop +
                       getUsedEquipmentProperty(IEquipment.EQUIP_ADD_STR);
        //加上附魔属性
        strength_result += getUsedEquipmentEnchantingProperty(IEquipment.EQUIP_ADD_STR);
        //加阵营宝石
        strength_result += getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_STR);
        //悟性分级
        int point = getPetPerceptionSubLevel();
        strength_result = strength_result * (Utils.PET_PERCEPTION_AWARD[getPerceptionLevel()] + point) / 10000;
        return strength_result;
    }

    public int getRealIntelligence() {
        int intelligence_result = intelligence + enhanceintelligence +
        		intelligenceDevelop + 
                           getUsedEquipmentProperty(IEquipment.EQUIP_ADD_INT);
        //加上附魔属性
        intelligence_result += getUsedEquipmentEnchantingProperty(IEquipment.EQUIP_ADD_INT);
        //加阵营宝石
        intelligence_result += getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_INT);
        //悟性分级
        int point = getPetPerceptionSubLevel();
        intelligence_result = intelligence_result * (Utils.PET_PERCEPTION_AWARD[getPerceptionLevel()] + point) / 10000;
        return intelligence_result;
    }

    public int getRealAgility() {
        int agility_result = agility + enhanceagility +
        		agilityDevelop + 
                      getUsedEquipmentProperty(IEquipment.EQUIP_ADD_AGI);
        //加上附魔属性
        agility_result += getUsedEquipmentEnchantingProperty(IEquipment.EQUIP_ADD_AGI);
        //加阵营宝石
        agility_result += getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_AGI);
        //悟性分级
        int point = getPetPerceptionSubLevel();
        agility_result = agility_result * (Utils.PET_PERCEPTION_AWARD[getPerceptionLevel()] + point) / 10000;
        return agility_result;
    }
    
    //获取细分后的悟性等级（分为5段）
    public int getPetPerceptionSubLevel(){
    	int result = 0;
    	if(getPerceptionLevel()<8 && Utils.Pet_PerceptionPoint[getPerceptionLevel()]>0){
    		result = Utils.Pet_PerceptionPoint[getPerceptionLevel()] / 5;
    		if(result!=0){
    			result = (Utils.PET_PERCEPTION_AWARD[getPerceptionLevel() + 1] - Utils.PET_PERCEPTION_AWARD[getPerceptionLevel()]) / 5 * (this.getPerceptionPoint() / result);
    		}
    	}
    	return result;
    }
    
    public int getMaxHp() {
        return Utils.calculateMaxHp(
		        getRealVitality(), getRealAgility(),
		        getRealStrength(), getRealIntelligence(),
		        level, getUsedEquipmentProperty(IEquipment.EQUIP_ADD_HPMAX)+ 
		        getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_HPMAX) + getEvolutionProperty(IEquipment.EQUIP_ADD_HPMAX));
    }

    public int getMaxMp(){
        return Utils.calculateMaxMp(
                getRealVitality(), getRealAgility(),
                getRealStrength(), getRealIntelligence(),
                level, getUsedEquipmentProperty(IEquipment.EQUIP_ADD_MPMAX)+
                getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_MPMAX));
    }

    public void addHp(int hp){
        setHp(Math.min(getMaxHp(),getHp()+hp));
    }

    public void addMp(int mp){
        setMp(Math.min(getMaxMp(),getMp()+mp));
    }

    public byte getQuality(){
        return 0;
    }

    public boolean isBinded(){
//        return false;
    	return binded;
    }

    public Ability[] getAbilities(){
        Ability[] ret = new Ability[abilities.size()];
        abilities.toArray(ret);
        return ret;
    }
    
    public AbilityData[] getAbilitiesData(){
    	AbilityData[] ret = new AbilityData[abilitiesdata.size()];
    	abilitiesdata.toArray(ret);
    	return ret;
    }
    
    /**
     * 移除最后一个技能 并且设定该位为非锁定状态
     * @return
     */
    public Ability removeAbilityEnd(){
    	int size = abilities.size();
    	Ability remove = (Ability)abilities.remove(size - 1);
    	if(remove != null){
    		if(size <= PET_ABILITY_MAX){
    			abilitiesLock[size - 1] = 0;
    		}
    	}
    	return remove;
    }
    public void removeAbilitySaint(){
    	int size = abilitiesdata.size();
    	AbilityData abilitydata = null;
    	for(int i = size-1; i>=0; i--){
    		abilitydata = (AbilityData)abilitiesdata.get(i);
    		if(abilitydata.getIsSaint() == 1){
    			abilities.remove(i);
    			abilitiesdata.remove(i);
    		}
    	}
    	
    }
    
    //zxyu add 宠物技能锁定
    public byte[] getAbilitesLock(){
    	return this.abilitiesLock;
    }
    /**
     * 锁定指定的技能
     * @param index
     * @param modify 是否进行修改
     * @return
     */
    public boolean lockAbility(int index, boolean modify){
    	if(abilitiesLock[index] == 0){
    		if(modify){
    			abilitiesLock[index] = 1;
    		}
    		return true;
    	}
    	return false;
    }
    public boolean unlockAbility(int index){
    	if(abilitiesLock[index] == 1){
    		abilitiesLock[index] = 0;
    		return true;
    	}
    	return false;
    }
    /**
     * 是否已经达到锁定最大数
     * @return
     */
    public boolean isLockMax(){
    	int count = 0;
    	for(int i=0; i<Pet.PET_ABILITY_MAX; i++){
    		if(abilitiesLock[i] != 0){
    			count ++;
    		}
    	}
    	return count >= Pet.PET_ABILITY_LOCK_MAX;
    }
    /**
     * 获得锁定的个数
     * @return
     */
    public int getLockCount(){
    	int count = 0;
    	for(int i=0; i<Pet.PET_ABILITY_MAX; i++){
    		if(abilitiesLock[i] != 0){
    			count ++;
    		}
    	}
    	return count;
    }
    /**
     * 获得指定技能在技能表里的编号
     * @param skillId
     * @return
     */
    public int getAbilityIndex(int skillId){
    	Ability ability = null;
    	for(int i=0; i<Pet.PET_ABILITY_MAX; i++){
    		ability = (Ability)this.abilities.get(i);
    		if(ability != null && ability.getId() == skillId){
    			return i;
    		}
    	}
    	return -1;
    }
    /**
     * 技能重生
     * @return
     */
    public short[] getFlashSkill(){
    	Ability ability = null;
    	int[] list = new int[Pet.PET_ABILITY_MAX];
    	int lockCount = 0;
    	//排除那些普通锁的技能
    	for(int i=0; i<Pet.PET_ABILITY_MAX; i++){
    		if(this.abilitiesLock[i] != 0 && ((AbilityData)abilitiesdata.get(i)).getIsSaint() == 0){
    			ability = (Ability)abilities.get(i);
    			list[lockCount++] = ability.getId();
    		}
    	}
    	AbilityData abilitydata = null;
    	//将没有圣宠技能排除
    	for(int i=0; i<abilitiesdata.size(); i++){
    		abilitydata = (AbilityData)abilitiesdata.get(i);
    		if(abilitydata.getIsSaint() == 1){
    			ability = (Ability)abilities.get(i);
    			list[lockCount++] = ability.getId();
    		}
    	}
    	int[] except = new int[lockCount];
    	for(int i=0; i<except.length; i++){
    		except[i] = list[i];
    	}
    	int totalCount = 1026;
    	Ability[] newAbilities = Utils.getPetAbilities(this.getType(), abilities.size() - lockCount, except, totalCount);
    	int index = 0;
    	for(int i=0; i<abilities.size(); i++){
    		if(this.abilitiesLock[i] == 0 && ((AbilityData)abilitiesdata.get(i)).getIsSaint() == 0){
    			abilities.set(i, newAbilities[index++]);
    		}
    	}
//    	//进行圣宠重生
//    	list = new int[Pet.PET_ABILITY_MAX];
//    	lockCount = 0;
//    	//将其它技能都排除 或者加锁的圣宠技能
//    	for(int i=0; i<abilitiesdata.size(); i++){
//    		abilitydata = (AbilityData)abilitiesdata.get(i);
//    		if(abilitydata.getIsSaint() == 0 || this.abilitiesLock[i] != 0){
//    			ability = (Ability)abilities.get(i);
//    			list[lockCount++] = ability.getId();
//    		}
//    	}
//    	except = new int[lockCount];
//    	for(int i=0; i<except.length; i++){
//    		except[i] = list[i];
//    	}
//    	totalCount = 1029;
//    	newAbilities = Utils.getPetAbilities(this.getType(), abilities.size() - lockCount, except, totalCount);
//    	index = 0;
//    	for(int i=0; i<abilities.size(); i++){
//    		if(this.abilitiesLock[i] == 0 && ((AbilityData)abilitiesdata.get(i)).getIsSaint() == 1){
//    			abilities.set(i, newAbilities[index++]);
//    		}
//    	}
    	
    	list = new int[Pet.PET_ABILITY_MAX];
    	lockCount = 0;
    	//排除圣宠技能
    	for(int i=0; i<abilitiesdata.size(); i++){
    		if(((AbilityData)abilitiesdata.get(i)).getIsSaint() == 0){
    			ability = (Ability)abilities.get(i);
    			list[lockCount++] = ability.getId();
    		}
    	}
    	
    	short[] skillid = new short[lockCount];
    	for(int i=0; i<lockCount; i++){
    		skillid[i] = (short)list[i];
    	}
    	return skillid;
    }
    //zxyu add end
    
    public void setSpiritualityLevel (int spiritualityLevel) {
    	this.spiritualityLevel = spiritualityLevel;
    }
    
    public int getSpiritualityLevel () {
    	return spiritualityLevel;
    }
    
    public void setPerceptionPoint (int perceptionPoint) {
    	this.perceptionPoint = perceptionPoint;
    }
    
    public int getPerceptionPoint () {
    	return perceptionPoint;
    }
    
    public void setNextPerceptionPoint (int nextPerceptionPoint) {
    	this.nextPerceptionPoint = nextPerceptionPoint;
    }
    
    public int getNextPerceptionPoint () {
    	return nextPerceptionPoint;
    }
    
    public void setPerceptionLevel (int perceptionLevel) {
    	this.perceptionLevel = perceptionLevel;
    }
    
    public int getPerceptionLevel () {
    	return perceptionLevel;
    }
    
    public IntList getAbilityId(){
    	IntList ret = new ArrayIntList(abilities.size());
    	for (int i = 0; i < abilities.size(); i ++) {
    		Ability ability = (Ability) abilities.get(i);
    		ret.add(ability.getId());
    	}
        return ret;
    }
    
    /**
     * @return获得技能id
     */
    public int[] getAbilitiesId() {
        int[] ret = new int[abilities.size()];
        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = (Ability) abilities.get(i);
            ret[i] = ability.getId();
        }
        return ret;
    }
    
    public int getPropertyPoints(){
        return currentPoint + point + agility + vitality + strength + intelligence+enhanceagility+enhanceintelligence+enhancestrength+enhancevitality;
    }

    public byte[] toDbBytes(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(id);
            dos.writeUTF(name);
            dos.writeByte(petType);
            dos.writeBoolean(baby);
            dos.writeShort(level);
            dos.writeInt(exp);
            dos.writeShort(currentPoint);
            dos.writeShort(point);
            dos.writeByte(favor);
            dos.writeShort(agility);
            dos.writeShort(strength);
            dos.writeShort(vitality);
            dos.writeShort(intelligence);
            dos.writeInt(hp);
            dos.writeInt(mp);
//            dos.writeInt(renameTimes);
            dos.writeByte(abilities.size());
            for (int i = 0; i < abilities.size(); i++) {
                Ability ability = (Ability) abilities.get(i);
                dos.writeShort(ability.getId());
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    //jwp add 
    public byte[] toDbBytes_version2(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(id);
            dos.writeUTF(name);
            dos.writeByte(petType);
            dos.writeBoolean(baby);
            dos.writeShort(level);
            dos.writeInt(exp);
            dos.writeShort(currentPoint);
            dos.writeShort(point);
            dos.writeByte(favor);
            dos.writeShort(agility);
            dos.writeShort(strength);
            dos.writeShort(vitality);
            dos.writeShort(intelligence);
            dos.writeInt(hp);
            dos.writeInt(mp);
//            dos.writeInt(renameTimes);
            dos.writeByte(abilities.size());
            for (int i = 0; i < abilities.size(); i++) {
                Ability ability = (Ability) abilities.get(i);
                dos.writeShort(ability.getId());
            }
            
            //jwp add
            dos.writeInt(maxEnchancePoint);
            //以下修改为实际属性值存储
            dos.writeInt(enhancestrength);
            dos.writeInt(enhanceintelligence);
            dos.writeInt(enhancevitality);
            dos.writeInt(enhanceagility);
            dos.writeUTF(enhanceName);
            dos.writeInt(currentEnchancePoint);
            for(int i=0;i<currentEnchancePoint;i++){
            	dos.writeInt(petEnhances.get(i).getProperty());
            	//System.out.println(petEnhances.get(i).getProperty());
            }
            //jwp add end
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    // jwp add end 
  //mengjie add 
    public byte[] toDbBytes_version3(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(id);
            dos.writeUTF(name);
            dos.writeByte(petType);
            dos.writeBoolean(baby);
            dos.writeShort(level);
            dos.writeInt(exp);
            dos.writeShort(currentPoint);
            dos.writeShort(point);
            dos.writeByte(favor);
            dos.writeShort(agility);
            dos.writeShort(strength);
            dos.writeShort(vitality);
            dos.writeShort(intelligence);
            dos.writeInt(hp);
            dos.writeInt(mp);
            dos.writeByte(abilities.size());
            for (int i = 0; i < abilities.size(); i++) {
                Ability ability = (Ability) abilities.get(i);
                dos.writeShort(ability.getId());
            }
            dos.writeInt(maxEnchancePoint);
            //以下修改为实际属性值存储
            dos.writeInt(enhancestrength);
            dos.writeInt(enhanceintelligence);
            dos.writeInt(enhancevitality);
            dos.writeInt(enhanceagility);
            dos.writeUTF(enhanceName);
            dos.writeInt(currentEnchancePoint);
            for(int i=0;i<currentEnchancePoint;i++){
            	dos.writeInt(petEnhances.get(i).getProperty());
            }
            //宠物装备信息
            //byte equversion = 3;
            //byte equversion = 4;           //items version 4  增加鉴定
            //byte equversion = 5;				//items version 5 装备刻字
            byte equversion = 6;	               //items version 6 增加宝石系统
            dos.writeByte(equversion);
            short size = 9;
            dos.writeShort(size);
            for (int ii = 0; ii < size; ii++) {
            	dos.writeByte(ii);//part记录部位
            	if (usedEquipments[ii] == null){
            		dos.writeByte(usedEquinfo[ii]);//是否可装备 2 0
            	}else{
            		dos.writeByte(1);
            		Grid grid = (Grid) usedEquipments[ii];
                    if (grid != null) {
                        IEquipment equ = (IEquipment) grid.item;
                        dos.write(equ.toDbBytes());
                    }
            	}
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    // mengjie add end 
    
    public byte[] toDbBytes_version4(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(id);
            dos.writeUTF(name);
            dos.writeByte(petType);
            dos.writeBoolean(baby);
            dos.writeShort(level);
            dos.writeInt(exp);
            dos.writeShort(currentPoint);
            dos.writeShort(point);
            dos.writeByte(favor);
            dos.writeShort(agility);
            dos.writeShort(strength);
            dos.writeShort(vitality);
            dos.writeShort(intelligence);
            dos.writeInt(hp);
            dos.writeInt(mp);
            // 如果技能超过8个做下限制（虽然不可能）
            if (abilities.size() > 8) {
            	dos.writeByte((byte)8);
            	for (int i = 0; i < 8; i++) {
                    Ability ability = (Ability) abilities.get(i);
                    dos.writeShort(ability.getId());
                    //zxyu add
                    dos.writeByte(abilitiesLock[i]);
                    //zxyu add end
                }
            } else {
            	dos.writeByte(abilities.size());
            	for (int i = 0; i < abilities.size(); i++) {
            		Ability ability = (Ability) abilities.get(i);
            		dos.writeShort(ability.getId());
            		//zxyu add
            		dos.writeByte(abilitiesLock[i]);
            		//zxyu add end
            	}
            }
            dos.writeInt(maxEnchancePoint);
            
            // 灵性
            dos.writeInt(spiritualityLevel);
            // 悟性等级
            dos.writeUTF(enhanceName);
            // 当前悟性等级
            dos.writeShort(perceptionLevel);
            // 当前悟性经验
            dos.writeInt(perceptionPoint);
            
            
            //宠物装备信息
            //byte equversion = 3;
            //byte equversion = 4;           //items version 4  增加鉴定
            //byte equversion = 5;				//items version 5 装备刻字
            byte equversion = 6;	               //items version 6 增加宝石系统
            dos.writeByte(equversion);
            short size = 9;
            dos.writeShort(size);
            for (int ii = 0; ii < size; ii++) {
            	dos.writeByte(ii);//part记录部位
            	if (usedEquipments[ii] == null){
            		dos.writeByte(usedEquinfo[ii]);//是否可装备 2 0
            	}else{
            		dos.writeByte(1);
            		Grid grid = (Grid) usedEquipments[ii];
                    if (grid != null) {
                        IEquipment equ = (IEquipment) grid.item;
                        dos.write(equ.toDbBytes());
                    }
            	}
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
    public byte[] toDbBytes_version5(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(id);
            dos.writeUTF(name);
            dos.writeByte(petType);
            dos.writeBoolean(baby);
            dos.writeShort(level);
            dos.writeInt(exp);
            dos.writeShort(currentPoint);
            dos.writeShort(point);
            dos.writeByte(favor);
            dos.writeShort(agility);
            dos.writeShort(strength);
            dos.writeShort(vitality);
            dos.writeShort(intelligence);
            dos.writeInt(hp);
            dos.writeInt(mp);
            // 如果技能超过8个做下限制（虽然不可能）
            if (abilities.size() > 8) {
            	dos.writeByte((byte)8);
            	for (int i = 0; i < 8; i++) {
                    Ability ability = (Ability) abilities.get(i);
                    dos.writeShort(ability.getId());
                    //zxyu add
                    dos.writeByte(abilitiesLock[i]);
                    //zxyu add end
                }
            } else {
            	dos.writeByte(abilities.size());
            	for (int i = 0; i < abilities.size(); i++) {
            		Ability ability = (Ability) abilities.get(i);
            		dos.writeShort(ability.getId());
            		//zxyu add
            		dos.writeByte(abilitiesLock[i]);
            		//zxyu add end
            	}
            }
            dos.writeInt(maxEnchancePoint);
            
            // 灵性
            dos.writeInt(spiritualityLevel);
            // 悟性等级
            dos.writeUTF(enhanceName);
            // 当前悟性等级
            dos.writeShort(perceptionLevel);
            // 当前悟性经验
            dos.writeInt(perceptionPoint);
            
            //当前绑定类型和绑定状态
            dos.writeByte(bindType);
            dos.writeBoolean(binded);
            
            //宠物装备信息
            //byte equversion = 3;
            //byte equversion = 4;           //items version 4  增加鉴定
            //byte equversion = 5;				//items version 5 装备刻字
//            byte equversion = 6;	               //items version 6 增加宝石系统
//            byte equversion = 7;					//items version 7增加附魔系统
//            byte equversion = 8;					//items version 8调整附魔数值
            byte equversion = 9;					//items version 9增加属性攻
            dos.writeByte(equversion);
            short size = 9;
            dos.writeShort(size);
            for (int ii = 0; ii < size; ii++) {
            	dos.writeByte(ii);//part记录部位
            	if (usedEquipments[ii] == null){
            		dos.writeByte(usedEquinfo[ii]);//是否可装备 2 0
            	}else{
            		dos.writeByte(1);
            		Grid grid = (Grid) usedEquipments[ii];
                    if (grid != null) {
                        IEquipment equ = (IEquipment) grid.item;
                        dos.write(equ.toDbBytes());
                    }
            	}
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
    public byte[] toDbBytes_version6(int version){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(id);
            dos.writeUTF(name);
            dos.writeByte(petType);
            dos.writeBoolean(baby);
            dos.writeShort(level);
            dos.writeInt(exp);
            dos.writeShort(currentPoint);
            dos.writeShort(point);
            dos.writeByte(favor);
            dos.writeShort(agility);
            dos.writeShort(strength);
            dos.writeShort(vitality);
            dos.writeShort(intelligence);
            dos.writeInt(hp);
            dos.writeInt(mp);
            // 如果技能超过8个做下限制（虽然不可能）
            if (abilities.size() > 8) {
            	dos.writeByte((byte)abilities.size());
            	for (int i = 0; i < abilities.size(); i++) {
                    Ability ability = (Ability) abilities.get(i);
                    AbilityData abilitydata = (AbilityData) abilitiesdata.get(i);
                    dos.writeShort(ability.getId());
                    //zxyu add
                    dos.writeByte(abilitiesLock[i]);
                    //zxyu add end
                    if(version >= 9){
                    	dos.writeByte(abilitydata.getIsSaint());
                    }
                }
            } else {
            	dos.writeByte(abilities.size());
            	for (int i = 0; i < abilities.size(); i++) {
            		Ability ability = (Ability) abilities.get(i);
            		AbilityData abilitydata = (AbilityData) abilitiesdata.get(i);
            		dos.writeShort(ability.getId());
            		//zxyu add
            		dos.writeByte(abilitiesLock[i]);
            		//zxyu add end
            		if(version >= 9){
                    	dos.writeByte(abilitydata.getIsSaint());
                    }
            	}
            }
            dos.writeInt(maxEnchancePoint);
            
            // 灵性
            dos.writeInt(spiritualityLevel);
            // 悟性等级
            dos.writeUTF(enhanceName);
            // 当前悟性等级
            dos.writeShort(perceptionLevel);
            // 当前悟性经验
            dos.writeInt(perceptionPoint);
            
            //当前绑定类型和绑定状态
            dos.writeByte(bindType);
            dos.writeBoolean(binded);
            
            //当前颜色索引和上一个颜色索引
            dos.writeShort(colorIndex);
            dos.writeShort(colorIndexBack);
            
            //宠物装备信息
            //byte equversion = 3;
            //byte equversion = 4;           //items version 4  增加鉴定
            //byte equversion = 5;				//items version 5 装备刻字
//            byte equversion = 6;	               //items version 6 增加宝石系统
//            byte equversion = 7;					//items version 7增加附魔系统
//            byte equversion = 8;					//items version 8调整附魔数值
//            byte equversion = 9;					//items version 9增加属性攻
            byte equversion = 10;					//items version 10宝石养成
            dos.writeByte(equversion);
            short size = 9;
            dos.writeShort(size);
            for (int ii = 0; ii < size; ii++) {
            	dos.writeByte(ii);//part记录部位
            	if (usedEquipments[ii] == null){
            		dos.writeByte(usedEquinfo[ii]);//是否可装备 2 0
            	}else{
            		dos.writeByte(1);
            		Grid grid = (Grid) usedEquipments[ii];
                    if (grid != null) {
                        IEquipment equ = (IEquipment) grid.item;
                        dos.write(equ.toDbBytes());
                    }
            	}
            }
            
            if(version >= 7){		//版本7之后增加了宠物培养系统
            	dos.writeInt(getStrengthDevelop());
            	dos.writeInt(getVitalityDevelop());
            	dos.writeInt(getAgilityDevelop());
            	dos.writeInt(getIntelligenceDevelop());
            }
            
            if(version >= 8){		//版本8增加宠物进化系统
            	dos.writeInt(evolutionLevel);
            	dos.writeInt(evolutionPoint);
            	dos.writeInt(evolutionType);
            }
            
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
//mengjie add
    public static byte[] toBytes_arena(Pet pet){
    	try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            //dos.writeByte(2);
            //dos.write(pet.toDbBytes_version2());
//            dos.writeByte(3);
//            dos.write(pet.toDbBytes_version3());
//            dos.writeByte(4);
//            dos.write(pet.toDbBytes_version4());
//            dos.writeByte(5);
//            dos.write(pet.toDbBytes_version5());
//            dos.writeByte(6);
//            dos.write(pet.toDbBytes_version6());
            dos.writeByte(Pet.CURRENT_VERSION);
            dos.write(pet.toDbBytes_version6(Pet.CURRENT_VERSION));
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
//    public int getRenameTimes(){
//        return renameTimes;
//    }
//
//    public void setRenameTimes(int times){
//        renameTimes = times;
//    }

    public byte[] toClientBytesWithLevel(int varLevel){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(id);
            
            //zxyu add
            //在名字中添加第N代字样
            String preName = new String(name);
            if(bindType > 0){
            	if(evolutionLevel > 0){
            		EvolutionData data = EvolutionLoader.evolutions.get(evolutionLevel);
            		preName = preName.concat("(" + data.name + ")");
            	}else{
            		preName = preName.concat("(" + (bindType + 1) + "代)");
            	}
            }
            //zxyu add end
            
            //jwp add 
            if(enhanceName.equals("")&& enhanceName.length()==0){
            	dos.writeUTF(preName);
            }else{
            	dos.writeUTF(preName.concat(enhanceName));
            }
            //jwp add end 
            dos.writeByte(petType);
            dos.writeBoolean(baby);
            dos.writeShort(level);
            dos.writeInt(exp);
            dos.writeInt(nextExp);
            dos.writeShort(currentPoint);
            dos.writeShort(point);
            dos.writeByte(favor);
            //jwp add 
            if(currentEnchancePoint == 0){
	            dos.writeShort(strength + strengthDevelop);
	            dos.writeShort(agility + agilityDevelop);
	            dos.writeShort(vitality + vitalityDevelop);
	            dos.writeShort(intelligence + intelligenceDevelop);
            }else{
            	dos.writeShort(strength+enhancestrength + strengthDevelop);
	            dos.writeShort(agility+enhanceagility + agilityDevelop);
	            dos.writeShort(vitality+enhancevitality + vitalityDevelop);
	            dos.writeShort(intelligence+enhanceintelligence + intelligenceDevelop);
            }
            //jwp add end
            dos.writeInt(hp);
            dos.writeInt(mp);
            
            // 灵性
            dos.writeInt(spiritualityLevel);
            // 当前悟性等级
            dos.writeShort(perceptionLevel);
            // 当前悟性经验
            dos.writeInt(perceptionPoint);
            
            //添加绑定状态
            dos.writeByte(bindType);
            dos.writeBoolean(binded);
            
            //当前颜色
            dos.writeShort(colorIndex);
            //需要把凯化部位也写进去
            short size = 9;
            dos.writeShort(size);
            for (int ii = 0; ii < size; ii++) {
            	dos.writeByte(usedEquinfo[ii]);//是否可装备 2 0
            }
            
            dos.writeByte(abilities.size());
            for (int i = 0; i < abilities.size(); i++) {
                Ability ability = (Ability) abilities.get(i);
                dos.writeShort(ability.getId());
//                dos.writeByte(ability.getIsSaint());
            }
            
            //进化相关数据
            dos.writeInt(evolutionLevel);
        	EvolutionData data = EvolutionLoader.evolutions.get(evolutionLevel);
        	dos.writeInt(data.pa);
        	dos.writeInt(data.ma);
        	dos.writeInt(data.pd);
        	dos.writeInt(data.md);
        	dos.writeInt(data.hp);
        	dos.writeInt(evolutionType);
            
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * 宠物技能锁定、解锁、重生时写log用
     * @return
     */
    public  byte[] toBytes_log(){
    	 try {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos);
             dos.writeInt(itemId);
             dos.writeInt(id);
             dos.writeUTF(name);
             dos.writeByte(petType);
             dos.writeBoolean(baby);
             dos.writeShort(level);
             // 如果技能超过8个做下限制（虽然不可能）
             if (abilities.size() > 8) {
             	dos.writeByte((byte)8);
             	for (int i = 0; i < 8; i++) {
                     Ability ability = (Ability) abilities.get(i);
                     dos.writeShort(ability.getId());
                     dos.writeByte(abilitiesLock[i]);
                 }
             } else {
             	dos.writeByte(abilities.size());
             	for (int i = 0; i < abilities.size(); i++) {
             		Ability ability = (Ability) abilities.get(i);
             		dos.writeShort(ability.getId());
             		dos.writeByte(abilitiesLock[i]);
             	}
             }
             return bos.toByteArray();
         } catch (IOException ex) {
             return null;
         }
    }
    public static byte[] toDbBytes(Pet[] pet){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            //mengjie add petversion
            dos.writeByte(1);
            dos.writeByte(pet.length);
            for(int i=0;i<pet.length;i++){
                dos.write(pet[i].toDbBytes());
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    //jwp add
    public static byte[] toDbBytes_version2(Pet[] pet){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            //jwp add petversion
            dos.writeByte(2);
            dos.writeByte(pet.length);
            for(int i=0;i<pet.length;i++){
                dos.write(pet[i].toDbBytes_version2());
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    //jwp add end
  //mengjie add
    public static byte[] toDbBytes_version3(Pet[] pet){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(3);
            dos.writeByte(pet.length);
            for(int i=0;i<pet.length;i++){
                dos.write(pet[i].toDbBytes_version3());
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    //mengjie add end
    
    public static byte[] toDbBytes_version4(Pet[] pet){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(4);
            dos.writeByte(pet.length);
            for(int i=0;i<pet.length;i++){
                dos.write(pet[i].toDbBytes_version4());
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static byte[] toDbBytes_version5(Pet[] pet){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(5);
            dos.writeByte(pet.length);
            for(int i=0;i<pet.length;i++){
                dos.write(pet[i].toDbBytes_version5());
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static byte[] toDbBytes_version6(Pet[] pet){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(Pet.CURRENT_VERSION);
            dos.writeByte(pet.length);
            for(int i=0;i<pet.length;i++){
                dos.write(pet[i].toDbBytes_version6(Pet.CURRENT_VERSION));
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static Pet[] getPetsFromDb(byte[] bytes){
        if(bytes==null||bytes.length==0)
            return new Pet[0];
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            //mengjie add petversion
            byte petversion = dis.readByte();
            Pet[] ret = null;
            if (petversion == 1){
            	int len = dis.readByte();
                ret = new Pet[len];
                for (int i = 0; i < len; i++) {
                    Pet pet = new Pet();
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
//                    pet.setRenameTimes(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                        pet.addAbility(Ability.getAbility(dis.readShort()));
                    }
                    ret[i] = pet;
                }
            }else if(petversion == 2){//jwp add 
            	int len = dis.readByte();
                ret = new Pet[len];
                for (int i = 0; i < len; i++) {
                    Pet pet = new Pet();
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
//                    pet.setRenameTimes(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                        pet.addAbility(Ability.getAbility(dis.readShort()));
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                  //原属性总数。现按照每个星单算
                    int enhance_strength = dis.readInt();
                    int enhance_intelligence = dis.readInt();
                    int enhance_vitality = dis.readInt();
                    int enhance_agility = dis.readInt();
//                    pet.setEnhancestrength(dis.readInt() * Utils.getEnhanceRation(1) );
//                    pet.setEnhanceintelligence(dis.readInt() * Utils.getEnhanceRation(2) );
//                    pet.setEnhancevitality(dis.readInt() * Utils.getEnhanceRation(3) );
//                    pet.setEnhanceagility(dis.readInt() * Utils.getEnhanceRation(4) );
                    int enhance_strength_point = 0;
                    int enhance_intelligence_point = 0;
                    int enhance_vitality_point = 0;
                    int enhance_agility_point = 0;
                    
                    /**
                     * petversion = 4改动
                     */
//                    pet.setEnhanceName(dis.readUTF()); 
//                    int k=dis.readInt();
//                    pet.setCurrentEnchancePoint(k);

                    // 取消炼化
                    String enhanceName = dis.readUTF();
                    int enchancePoint = dis.readInt();
                    int k = 0;
                    pet.setCurrentEnchancePoint(k);
                    
                    List<PetEnhance> petEnhances = new ArrayList<PetEnhance>(40);
                    for(int j=0;j< enchancePoint;j++){
                    	//property 1为enhancestrength 2为enhanceintelligence 3为enhancevitality ，4为enhanceagility 
                    	int property = dis.readInt();
                    	/*switch(property){
                    		case 1:{
                    			enhance_strength_point = enhance_strength_point + Utils.getEnhanceRation(property,j+1);
                    		}
                    		break;
                    		case 2:{
                    			enhance_intelligence_point = enhance_intelligence_point + Utils.getEnhanceRation(property,j+1);
                    		}
                    		break;
							case 3:{
								enhance_vitality_point = enhance_vitality_point + Utils.getEnhanceRation(property,j+1);
							}
							break;
							case 4:{
								enhance_agility_point = enhance_agility_point + Utils.getEnhanceRation(property,j+1);
							}
							break;
                    	}
                        pet.setEnhancestrength(enhance_strength_point);
    					pet.setEnhanceintelligence(enhance_intelligence_point);
    					pet.setEnhancevitality(enhance_vitality_point);
    					pet.setEnhanceagility(enhance_agility_point);
    					
                    	petEnhances.add(PetEnhance.getPetEnhance(property)[0]);*/
                    }
                    pet.setPetEnhances(petEnhances);
                    /**
                     * petversion = 4新加
                     */
                    // 设置灵性
                    pet.setSpiritualityLevel(0);
                    // 获得上一次悟性等级
                    int lastPetPerceptionLevel = pet.getPerceptionLevel();
                    // 将炼化转换成悟性
                    int perceptionPoint = Utils.changePracticeToPerception(enchancePoint);
                    // 设置当前悟性等级
                    pet.setPerceptionLevel(Utils.getPetPerceptionLevel(perceptionPoint));
                    // 设置悟性等级
                    String perceptionLevelName = Utils.getPerceptionLevelName(pet.getPerceptionLevel());
                    pet.setEnhanceName(perceptionLevelName);
                    // 设置当前悟性经验
                    pet.setPerceptionPoint(0);
                    //当前颜色索引和上一个颜色索引   petversion6 增加
                    pet.setColorIndex((short)0);
                    pet.setColorIndexBack((short)0);
                    int count = Utils.getAddSkillCount(pet.getPerceptionLevel(), lastPetPerceptionLevel);
                	if (count > 0) {
                		Ability[] abs = Utils.getAddPetAbilities(pet.getAbilityId(), count);
                		for (int j = 0; j < abs.length; j++) {
                			pet.addAbility(abs[j]);
                		}
                	}
                    ret[i] = pet;
                }
            }else if(petversion == 3){//mengjie add 
            	int len = dis.readByte();
                ret = new Pet[len];
                for (int i = 0; i < len; i++) {
                    Pet pet = new Pet();
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                        pet.addAbility(Ability.getAbility(dis.readShort()));
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                  //原属性总数。现按照每个星单算
                    int enhance_strength = dis.readInt();
                    int enhance_intelligence = dis.readInt();
                    int enhance_vitality = dis.readInt();
                    int enhance_agility = dis.readInt();
//                    pet.setEnhancestrength(dis.readInt() * Utils.getEnhanceRation(1) );
//                    pet.setEnhanceintelligence(dis.readInt() * Utils.getEnhanceRation(2) );
//                    pet.setEnhancevitality(dis.readInt() * Utils.getEnhanceRation(3) );
//                    pet.setEnhanceagility(dis.readInt() * Utils.getEnhanceRation(4) );
                    int enhance_strength_point = 0;
                    int enhance_intelligence_point = 0;
                    int enhance_vitality_point = 0;
                    int enhance_agility_point = 0;
                    
                    /**
                     * petversion = 4改动
                     */
//                    pet.setEnhanceName(dis.readUTF()); 
//                    int k=dis.readInt();
//                    pet.setCurrentEnchancePoint(k);

                    // 取消炼化
                    String enhanceName = dis.readUTF();
                    int enchancePoint = dis.readInt();
                    int k = 0;
                    pet.setCurrentEnchancePoint(k);
                    
                    List<PetEnhance> petEnhances = new ArrayList<PetEnhance>(40);
                    for(int j=0;j< enchancePoint;j++){
                    	//property 1为enhancestrength 2为enhanceintelligence 3为enhancevitality ，4为enhanceagility 
                    	int property = dis.readInt();
                    	/*switch(property){
                    		case 1:{
                    			enhance_strength_point = enhance_strength_point + Utils.getEnhanceRation(property,j+1);
                    		}
                    		break;
                    		case 2:{
                    			enhance_intelligence_point = enhance_intelligence_point + Utils.getEnhanceRation(property,j+1);
                    		}
                    		break;
							case 3:{
								enhance_vitality_point = enhance_vitality_point + Utils.getEnhanceRation(property,j+1);
							}
							break;
							case 4:{
								enhance_agility_point = enhance_agility_point + Utils.getEnhanceRation(property,j+1);
							}
							break;
                    	}
                        pet.setEnhancestrength(enhance_strength_point);
    					pet.setEnhanceintelligence(enhance_intelligence_point);
    					pet.setEnhancevitality(enhance_vitality_point);
    					pet.setEnhanceagility(enhance_agility_point);
                    	petEnhances.add(PetEnhance.getPetEnhance(property)[0]);*/
                    }
                    pet.setPetEnhances(petEnhances);
                    
                    /**
                     * petversion = 4新加
                     */
                    // 设置灵性
                    pet.setSpiritualityLevel(0);
                    // 获得上一次悟性等级
                    int lastPetPerceptionLevel = pet.getPerceptionLevel();
                    // 将炼化转换成悟性
                    int perceptionPoint = Utils.changePracticeToPerception(enchancePoint);
                    // 设置当前悟性等级
                    pet.setPerceptionLevel(Utils.getPetPerceptionLevel(perceptionPoint));
                    // 设置悟性等级
                    String perceptionLevelName = Utils.getPerceptionLevelName(pet.getPerceptionLevel());
                    pet.setEnhanceName(perceptionLevelName);
                    // 设置当前悟性经验
                    pet.setPerceptionPoint(0);
                    // 增加技能
                    int count = Utils.getAddSkillCount(pet.getPerceptionLevel(), lastPetPerceptionLevel);
                	if (count > 0) {
                		Ability[] abs = Utils.getAddPetAbilities(pet.getAbilityId(), count);
                		for (int j = 0; j < abs.length; j++) {
                			pet.addAbility(abs[j]);
                		}
                	}
                	//当前颜色索引和上一个颜色索引   petversion6 增加
                    pet.setColorIndex((short)0);
                    pet.setColorIndexBack((short)0);
                    try{
//						pet_equipments
						byte version = dis.readByte();
			            short size = dis.readShort();
			            for (int ii = 0; ii < size; ii++) {
			            	byte equpart = dis.readByte();
			            	byte equflag = dis.readByte();
			            	if (equflag == 1){
			            		IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
				                if (equ == null)
				                    throw new PlayerDataException("数据错误");
				                Grid grid = new Grid();
				                grid.item = equ;
				                grid.count = 1;
				                pet.usedEquipments[equ.getPart()] = grid;
				                pet.usedEquinfo[equ.getPart()] = equflag;
			            	}else{
			            		pet.usedEquinfo[equpart] = equflag;
			            	}
			            }
					}catch (Exception e) {
						
					}
                    ret[i] = pet;
                    
                }
            } else if (petversion == 4) {
            	int len = dis.readByte();
                ret = new Pet[len];
                for (int i = 0; i < len; i++) {
                    Pet pet = new Pet();
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                        pet.addAbility(Ability.getAbility(dis.readShort()));
                        //zxyu add
                        pet.abilitiesLock[j] = dis.readByte();
                        //zxyu add end
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                    
                    // 灵性
                    int spirituality = dis.readInt();
                    pet.setSpiritualityLevel(spirituality);
                    // 悟性等级
                    String perLevel = dis.readUTF();
                    pet.setEnhanceName(perLevel);
                    // 当前悟性等级
                    short perceptionLevel = dis.readShort();
                    pet.setPerceptionLevel(perceptionLevel);
                    // 当前悟性经验
                    int perceptionPoint = dis.readInt();
                    pet.setPerceptionPoint(perceptionPoint);
                    
                    //当前颜色索引和上一个颜色索引   petversion6 增加
                    pet.setColorIndex((short)0);
                    pet.setColorIndexBack((short)0);
                    
                    try{
						byte version = dis.readByte();
			            short size = dis.readShort();
			            for (int ii = 0; ii < size; ii++) {
			            	byte equpart = dis.readByte();
			            	byte equflag = dis.readByte();
			            	if (equflag == 1){
			            		IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
				                if (equ == null)
				                    throw new PlayerDataException("数据错误");
				                Grid grid = new Grid();
				                grid.item = equ;
				                grid.count = 1;
				                pet.usedEquipments[equ.getPart()] = grid;
				                pet.usedEquinfo[equ.getPart()] = equflag;
			            	}else{
			            		pet.usedEquinfo[equpart] = equflag;
			            	}
			            }
					}catch (Exception e) {
						
					}
                    ret[i] = pet;
                }
            } else if (petversion == 5) {
            	int len = dis.readByte();
                ret = new Pet[len];
                for (int i = 0; i < len; i++) {
                    Pet pet = new Pet();
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                        pet.addAbility(Ability.getAbility(dis.readShort()));
                        //zxyu add
                        pet.abilitiesLock[j] = dis.readByte();
                        //zxyu add end
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                    
                    // 灵性
                    int spirituality = dis.readInt();
                    pet.setSpiritualityLevel(spirituality);
                    // 悟性等级
                    String perLevel = dis.readUTF();
                    pet.setEnhanceName(perLevel);
                    // 当前悟性等级
                    short perceptionLevel = dis.readShort();
                    pet.setPerceptionLevel(perceptionLevel);
                    // 当前悟性经验
                    int perceptionPoint = dis.readInt();
                    pet.setPerceptionPoint(perceptionPoint);
                    
                    //绑定类型跟绑定状态
                    byte bindType = dis.readByte();
                    pet.setBindType(bindType);
                    boolean binded = dis.readBoolean();
                    pet.setBinded(binded);
                    
                    //当前颜色索引和上一个颜色索引   petversion6 增加
                    pet.setColorIndex((short)0);
                    pet.setColorIndexBack((short)0);
                    
                    try{
						byte version = dis.readByte();
			            short size = dis.readShort();
			            for (int ii = 0; ii < size; ii++) {
			            	byte equpart = dis.readByte();
			            	byte equflag = dis.readByte();
			            	if (equflag == 1){
			            		IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
				                if (equ == null)
				                    throw new PlayerDataException("数据错误");
				                Grid grid = new Grid();
				                grid.item = equ;
				                grid.count = 1;
				                pet.usedEquipments[equ.getPart()] = grid;
				                pet.usedEquinfo[equ.getPart()] = equflag;
			            	}else{
			            		pet.usedEquinfo[equpart] = equflag;
			            	}
			            }
					}catch (Exception e) {
						
					}
                    ret[i] = pet;
                }
            }else if (petversion >= 6) {
            	int len = dis.readByte();
                ret = new Pet[len];
                for (int i = 0; i < len; i++) {
                    Pet pet = new Pet();
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                    	Ability ability = Ability.getAbility(dis.readShort());
                    	AbilityData abilitydata = new AbilityData();
                        //zxyu add
                        pet.abilitiesLock[j] = dis.readByte();
                        //zxyu add end
                        if(petversion >= 9){
                        	abilitydata.setIsSaint(dis.readByte());
                        }
                        pet.addAbility(ability, abilitydata);
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                    
                    // 灵性
                    int spirituality = dis.readInt();
                    pet.setSpiritualityLevel(spirituality);
                    // 悟性等级
                    String perLevel = dis.readUTF();
                    pet.setEnhanceName(perLevel);
                    // 当前悟性等级
                    short perceptionLevel = dis.readShort();
                    pet.setPerceptionLevel(perceptionLevel);
                    // 当前悟性经验
                    int perceptionPoint = dis.readInt();
                    pet.setPerceptionPoint(perceptionPoint);
                    
                    //绑定类型跟绑定状态
                    byte bindType = dis.readByte();
                    pet.setBindType(bindType);
                    boolean binded = dis.readBoolean();
                    pet.setBinded(binded);
                    
                    //当前颜色索引和上一个颜色索引
                    short tmpColorIndex = dis.readShort();
                    pet.setColorIndex(tmpColorIndex);
                    short tmpColorIndexBack = dis.readShort();
                    pet.setColorIndexBack(tmpColorIndexBack);
                    
                    try{
						byte version = dis.readByte();
			            short size = dis.readShort();
			            for (int ii = 0; ii < size; ii++) {
			            	byte equpart = dis.readByte();
			            	byte equflag = dis.readByte();
			            	if (equflag == 1){
			            		IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
				                if (equ == null)
				                    throw new PlayerDataException("数据错误");
				                Grid grid = new Grid();
				                grid.item = equ;
				                grid.count = 1;
				                pet.usedEquipments[equ.getPart()] = grid;
				                pet.usedEquinfo[equ.getPart()] = equflag;
			            	}else{
			            		pet.usedEquinfo[equpart] = equflag;
			            	}
			            }
					}catch (Exception e) {
						
					}
                    if(petversion >= 7){	//增加宠物培养系统
                    	pet.setStrengthDevelop(dis.readInt());
                    	pet.setVitalityDevelop(dis.readInt());
                    	pet.setAgilityDevelop(dis.readInt());
                    	pet.setIntelligenceDevelop(dis.readInt());
                    }
                    
                    if(petversion >= 8){	//宠物进化系统
                    	pet.setEvolutionLevel(dis.readInt());
                    	pet.setEvolutionPoint(dis.readInt());
                    	pet.setEvolutionType(dis.readInt());
                    }
                    
                    ret[i] = pet;
                }
            }
            return ret;
        } catch (IOException ex) {
            return new Pet[0];
        }
    }
    //mengjie add
    public static Pet getPetFromDb(byte[] bytes){
        if(bytes==null||bytes.length==0)
            return new Pet();
        try {
        	ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            Pet pet = new Pet();
            byte petversion = dis.readByte();
            if (petversion == 1){
            	pet.setItemId(dis.readInt());
                pet.setId(dis.readInt());
                pet.setName(dis.readUTF());
                pet.setPetType(dis.readByte());
                pet.setBaby(dis.readBoolean());
                pet.setLevel(dis.readShort());
                pet.setExp(dis.readInt());
                pet.setCurrentPoint(dis.readShort());
                pet.setPoint(dis.readShort());
                pet.setFavor(dis.readByte());
                pet.setAgility(dis.readShort());
                pet.setStrength(dis.readShort());
                pet.setVitality(dis.readShort());
                pet.setIntelligence(dis.readShort());
                pet.setHp(dis.readInt());
                pet.setMp(dis.readInt());
//                    pet.setRenameTimes(dis.readInt());
                int n = dis.readByte();
                for(int j=0;j<n;j++){
                    pet.addAbility(Ability.getAbility(dis.readShort()));
                }
            }else if(petversion == 2){//jwp add 
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
//                    pet.setRenameTimes(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                        pet.addAbility(Ability.getAbility(dis.readShort()));
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                    //原属性总数。现按照每个星单算
                    int enhance_strength = dis.readInt();
                    int enhance_intelligence = dis.readInt();
                    int enhance_vitality = dis.readInt();
                    int enhance_agility = dis.readInt();
//                    pet.setEnhancestrength(dis.readInt() * Utils.getEnhanceRation(1) );
//                    pet.setEnhanceintelligence(dis.readInt() * Utils.getEnhanceRation(2) );
//                    pet.setEnhancevitality(dis.readInt() * Utils.getEnhanceRation(3) );
//                    pet.setEnhanceagility(dis.readInt() * Utils.getEnhanceRation(4) );
                    int enhance_strength_point = 0;
                    int enhance_intelligence_point = 0;
                    int enhance_vitality_point = 0;
                    int enhance_agility_point = 0;
                    
                    /**
                     * petversion = 4改动
                     */
//                    pet.setEnhanceName(dis.readUTF()); 
//                    int k=dis.readInt();
//                    pet.setCurrentEnchancePoint(k);

                    // 取消炼化
                    String enhanceName = dis.readUTF();
                    int enchancePoint = dis.readInt();
                    int k = 0;
                    pet.setCurrentEnchancePoint(k);
                    
                    List<PetEnhance> petEnhances = new ArrayList<PetEnhance>(40);
                    for(int j=0;j< enchancePoint;j++){
                    	//property 1为enhancestrength 2为enhanceintelligence 3为enhancevitality ，4为enhanceagility 
                    	int property = dis.readInt();
                    	/*switch(property){
                    		case 1:{
                    			enhance_strength_point = enhance_strength_point + Utils.getEnhanceRation(property,j+1);
                    		}
                    		break;
                    		case 2:{
                    			enhance_intelligence_point = enhance_intelligence_point + Utils.getEnhanceRation(property,j+1);
                    		}
                    		break;
							case 3:{
								enhance_vitality_point = enhance_vitality_point + Utils.getEnhanceRation(property,j+1);
							}
							break;
							case 4:{
								enhance_agility_point = enhance_agility_point + Utils.getEnhanceRation(property,j+1);
							}
							break;
                    	}
                        
                    	petEnhances.add(PetEnhance.getPetEnhance(property)[0]);*/
                    }
                    pet.setEnhancestrength(enhance_strength_point);
					pet.setEnhanceintelligence(enhance_intelligence_point);
					pet.setEnhancevitality(enhance_vitality_point);
					pet.setEnhanceagility(enhance_agility_point);
					
                    pet.setPetEnhances(petEnhances);
                    
                    /**
                     * petversion = 4新加
                     */
                    // 设置灵性
                    pet.setSpiritualityLevel(0);
                    // 获得上一次悟性
                    int lastPetPerceptionLevel = pet.getPerceptionLevel();
                    // 将炼化转换成悟性
                    int perceptionPoint = Utils.changePracticeToPerception(enchancePoint);
                    // 设置当前悟性等级
                    pet.setPerceptionLevel(Utils.getPetPerceptionLevel(perceptionPoint));
                    // 设置悟性等级
                    String perceptionLevelName = Utils.getPerceptionLevelName(pet.getPerceptionLevel());
                    pet.setEnhanceName(perceptionLevelName);
                    // 设置当前悟性经验
                    pet.setPerceptionPoint(perceptionPoint);
                    //当前颜色索引和上一个颜色索引   petversion6 增加
                    pet.setColorIndex((short)0);
                    pet.setColorIndexBack((short)0);
                    // 增加技能
                    int count = Utils.getAddSkillCount(pet.getPerceptionLevel(), lastPetPerceptionLevel);
                	if (count > 0) {
                		Ability[] abs = Utils.getAddPetAbilities(pet.getAbilityId(), count);
                		for (int j = 0; j < abs.length; j++) {
                			pet.addAbility(abs[j]);
                		}
                	}
                }else if(petversion == 3){//mengjie add 
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                        pet.addAbility(Ability.getAbility(dis.readShort()));
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                    //原属性总数。现按照每个星单算
                    int enhance_strength = dis.readInt();
                    int enhance_intelligence = dis.readInt();
                    int enhance_vitality = dis.readInt();
                    int enhance_agility = dis.readInt();
//                    pet.setEnhancestrength(dis.readInt() * Utils.getEnhanceRation(1) );
//                    pet.setEnhanceintelligence(dis.readInt() * Utils.getEnhanceRation(2) );
//                    pet.setEnhancevitality(dis.readInt() * Utils.getEnhanceRation(3) );
//                    pet.setEnhanceagility(dis.readInt() * Utils.getEnhanceRation(4) );
                    int enhance_strength_point = 0;
                    int enhance_intelligence_point = 0;
                    int enhance_vitality_point = 0;
                    int enhance_agility_point = 0;
                    /**
                     * petversion = 4改动
                     */
//                    pet.setEnhanceName(dis.readUTF()); 
//                    int k=dis.readInt();
//                    pet.setCurrentEnchancePoint(k);

                    // 取消炼化
                    String enhanceName = dis.readUTF();
                    int enchancePoint = dis.readInt();
                    int k = 0;
                    pet.setCurrentEnchancePoint(k);
                    
                    List<PetEnhance> petEnhances = new ArrayList<PetEnhance>(40);
                    for(int j=0;j< enchancePoint;j++){
                    	//property 1为enhancestrength 2为enhanceintelligence 3为enhancevitality ，4为enhanceagility 
                    	int property = dis.readInt();
                    	/*switch(property){
                    		case 1:{
                    			enhance_strength_point = enhance_strength_point + Utils.getEnhanceRation(property,j+1);
                    		}
                    		break;
                    		case 2:{
                    			enhance_intelligence_point = enhance_intelligence_point + Utils.getEnhanceRation(property,j+1);
                    		}
                    		break;
							case 3:{
								enhance_vitality_point = enhance_vitality_point + Utils.getEnhanceRation(property,j+1);
							}
							break;
							case 4:{
								enhance_agility_point = enhance_agility_point + Utils.getEnhanceRation(property,j+1);
							}
							break;
                    	}
                        
                    	petEnhances.add(PetEnhance.getPetEnhance(property)[0]);*/
                    }
                    pet.setEnhancestrength(enhance_strength_point);
					pet.setEnhanceintelligence(enhance_intelligence_point);
					pet.setEnhancevitality(enhance_vitality_point);
					pet.setEnhanceagility(enhance_agility_point);
                    pet.setPetEnhances(petEnhances);
                    
                    /**
                     * petversion = 4新加
                     */
                    // 设置灵性
                    pet.setSpiritualityLevel(0);
                    // 获得上一次悟性
                    int lastPetPerceptionLevel = pet.getPerceptionLevel();
                    // 将炼化转换成悟性
                    int perceptionPoint = Utils.changePracticeToPerception(enchancePoint);
                    // 设置悟性等级
                    String perceptionLevelName = Utils.getPerceptionLevelName(perceptionPoint);
                    pet.setEnhanceName(perceptionLevelName);
                    // 设置当前悟性等级
                    pet.setPerceptionLevel(Utils.getPetPerceptionLevel(perceptionPoint));
                    // 设置当前悟性经验
                    pet.setPerceptionPoint(perceptionPoint);
                    //当前颜色索引和上一个颜色索引   petversion6 增加
                    pet.setColorIndex((short)0);
                    pet.setColorIndexBack((short)0);
                    // 增加技能
                    int count = Utils.getAddSkillCount(pet.getPerceptionLevel(), lastPetPerceptionLevel);
                	if (count > 0) {
                		Ability[] abs = Utils.getAddPetAbilities(pet.getAbilityId(), count);
                		for (int j = 0; j < abs.length; j++) {
                			pet.addAbility(abs[j]);
                		}
                	}

					try{
//						pet_equipments
						byte version = dis.readByte();
			            short size = dis.readShort();
			            for (int i = 0; i < size; i++) {
			            	byte equpart = dis.readByte();
			            	byte equflag = dis.readByte();
			            	if (equflag == 1){
			            		IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
				                if (equ == null)
				                    throw new PlayerDataException("数据错误");
				                Grid grid = new Grid();
				                grid.item = equ;
				                grid.count = 1;
				                pet.usedEquipments[equ.getPart()] = grid;
				                pet.usedEquinfo[equ.getPart()] = equflag;
			            	}else{
			            		pet.usedEquinfo[equpart] = equflag;
			            	}
			            }
					}catch (Exception e) {
						
					}
                } else if (petversion == 4) {
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                        pet.addAbility(Ability.getAbility(dis.readShort()));
                        //zxyu add
                        pet.abilitiesLock[j] = dis.readByte();
                        //zxyu add end
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                    
                    // 灵性
                    int spirituality = dis.readInt();
                    pet.setSpiritualityLevel(spirituality);
                    // 悟性等级
                    String perLevel = dis.readUTF();
                    pet.setEnhanceName(perLevel);
                    // 当前悟性等级
                    short perceptionLevel = dis.readShort();
                    pet.setPerceptionLevel(perceptionLevel);
                    // 当前悟性经验
                    int perceptionPoint = dis.readInt();
                    pet.setPerceptionPoint(perceptionPoint);
                    //当前颜色索引和上一个颜色索引   petversion6 增加
                    pet.setColorIndex((short)0);
                    pet.setColorIndexBack((short)0);
					try{
						byte version = dis.readByte();
			            short size = dis.readShort();
			            for (int i = 0; i < size; i++) {
			            	byte equpart = dis.readByte();
			            	byte equflag = dis.readByte();
			            	if (equflag == 1){
			            		IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
				                if (equ == null)
				                    throw new PlayerDataException("数据错误");
				                Grid grid = new Grid();
				                grid.item = equ;
				                grid.count = 1;
				                pet.usedEquipments[equ.getPart()] = grid;
				                pet.usedEquinfo[equ.getPart()] = equflag;
			            	}else{
			            		pet.usedEquinfo[equpart] = equflag;
			            	}
			            }
					}catch (Exception e) {
						
					}
                } else if (petversion == 5) {
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                        pet.addAbility(Ability.getAbility(dis.readShort()));
                        //zxyu add
                        pet.abilitiesLock[j] = dis.readByte();
                        //zxyu add end
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                    
                    // 灵性
                    int spirituality = dis.readInt();
                    pet.setSpiritualityLevel(spirituality);
                    // 悟性等级
                    String perLevel = dis.readUTF();
                    pet.setEnhanceName(perLevel);
                    // 当前悟性等级
                    short perceptionLevel = dis.readShort();
                    pet.setPerceptionLevel(perceptionLevel);
                    // 当前悟性经验
                    int perceptionPoint = dis.readInt();
                    pet.setPerceptionPoint(perceptionPoint);
                    
                    //绑定类型和绑定状态
                    byte bindType = dis.readByte();
                    pet.setBindType(bindType);
                    boolean binded = dis.readBoolean();
                    pet.setBinded(binded);
                    
                    //当前颜色索引和上一个颜色索引   petversion6 增加
                    pet.setColorIndex((short)0);
                    pet.setColorIndexBack((short)0);
					try{
						byte version = dis.readByte();
			            short size = dis.readShort();
			            for (int i = 0; i < size; i++) {
			            	byte equpart = dis.readByte();
			            	byte equflag = dis.readByte();
			            	if (equflag == 1){
			            		IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
				                if (equ == null)
				                    throw new PlayerDataException("数据错误");
				                Grid grid = new Grid();
				                grid.item = equ;
				                grid.count = 1;
				                pet.usedEquipments[equ.getPart()] = grid;
				                pet.usedEquinfo[equ.getPart()] = equflag;
			            	}else{
			            		pet.usedEquinfo[equpart] = equflag;
			            	}
			            }
					}catch (Exception e) {
						
					}
                }else if (petversion >= 6) {
                    pet.setItemId(dis.readInt());
                    pet.setId(dis.readInt());
                    pet.setName(dis.readUTF());
                    pet.setPetType(dis.readByte());
                    pet.setBaby(dis.readBoolean());
                    pet.setLevel(dis.readShort());
                    pet.setExp(dis.readInt());
                    pet.setCurrentPoint(dis.readShort());
                    pet.setPoint(dis.readShort());
                    pet.setFavor(dis.readByte());
                    pet.setAgility(dis.readShort());
                    pet.setStrength(dis.readShort());
                    pet.setVitality(dis.readShort());
                    pet.setIntelligence(dis.readShort());
                    pet.setHp(dis.readInt());
                    pet.setMp(dis.readInt());
                    int n = dis.readByte();
                    for(int j=0;j<n;j++){
                    	Ability ability = Ability.getAbility(dis.readShort());
                    	AbilityData abilitydata = new AbilityData();
                        //zxyu add
                        pet.abilitiesLock[j] = dis.readByte();
                        //zxyu add end
                        if(petversion >= 9){
                        	abilitydata.setIsSaint(dis.readByte());
                        }
                        pet.addAbility(ability);
                    }
                    pet.setmaxEnchancePoint(dis.readInt());
                    
                    // 灵性
                    int spirituality = dis.readInt();
                    pet.setSpiritualityLevel(spirituality);
                    // 悟性等级
                    String perLevel = dis.readUTF();
                    pet.setEnhanceName(perLevel);
                    // 当前悟性等级
                    short perceptionLevel = dis.readShort();
                    pet.setPerceptionLevel(perceptionLevel);
                    // 当前悟性经验
                    int perceptionPoint = dis.readInt();
                    pet.setPerceptionPoint(perceptionPoint);
                    
                    //绑定类型和绑定状态
                    byte bindType = dis.readByte();
                    pet.setBindType(bindType);
                    boolean binded = dis.readBoolean();
                    pet.setBinded(binded);
                    
                    //当前颜色索引和上一个颜色索引
                    short tmpColorIndex = dis.readShort();
                    pet.setColorIndex(tmpColorIndex);
                    short tmpColorIndexBack = dis.readShort();
                    pet.setColorIndexBack(tmpColorIndexBack);
					try{
						byte version = dis.readByte();
			            short size = dis.readShort();
			            for (int i = 0; i < size; i++) {
			            	byte equpart = dis.readByte();
			            	byte equflag = dis.readByte();
			            	if (equflag == 1){
			            		IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
				                if (equ == null)
				                    throw new PlayerDataException("数据错误");
				                Grid grid = new Grid();
				                grid.item = equ;
				                grid.count = 1;
				                pet.usedEquipments[equ.getPart()] = grid;
				                pet.usedEquinfo[equ.getPart()] = equflag;
			            	}else{
			            		pet.usedEquinfo[equpart] = equflag;
			            	}
			            }
					}catch (Exception e) {
						
					}
					if(petversion >= 7){		//增加宠物培养系统
						pet.setStrengthDevelop(dis.readInt());
						pet.setVitalityDevelop(dis.readInt());
						pet.setAgilityDevelop(dis.readInt());
						pet.setIntelligenceDevelop(dis.readInt());
					}

					if(petversion >= 8){	//增加宠物进化系统
						pet.setEvolutionLevel(dis.readInt());
						pet.setEvolutionPoint(dis.readInt());
						pet.setEvolutionType(dis.readInt());
					}
                }
            return pet;
        } catch (IOException ex) {
            return new Pet();
        }
       
    }
    //jwp add 
    public void addEnhance(PetEnhance petEnhance){
        if(petEnhance==null)
            throw new IllegalArgumentException("enhancePet can not be null");
        if(maxEnchancePoint>0){
	        if(petEnhances.size()>=maxEnchancePoint)
	            throw new IllegalStateException("enhancePet can not > maxEnchancePoint");
        }
        petEnhances.add(petEnhance);
    }
    public void addPoint(int property){
    	currentEnchancePoint++;
    	if(property==1){
    		enhancestrength =enhancestrength + Utils.getEnhanceRation(property,currentEnchancePoint);
    	}else if(property==2){
    		enhanceintelligence = enhanceintelligence + Utils.getEnhanceRation(property,currentEnchancePoint);
    	}else if(property==3){
    		enhancevitality = enhancevitality + Utils.getEnhanceRation(property,currentEnchancePoint);
    	}else if(property == 4){
    		enhanceagility = enhanceagility + Utils.getEnhanceRation(property,currentEnchancePoint);
    	}   
    	
    }
    public void DelEnhance(){
        if(petEnhances.size() <= 0){
            throw new IllegalArgumentException("petEnhances size can not be 0");
        }else{
        	PetEnhance petEnhance = (PetEnhance) petEnhances.remove(petEnhances.size() - 1); 
        }
      
    }
    public void DelPoint(){
    	PetEnhance petEnhance = (PetEnhance) petEnhances.get(petEnhances.size() - 1);
    	int delpoint = petEnhance.getProperty();
    	if(delpoint==1){
    		enhancestrength =enhancestrength-Utils.getEnhanceRation(delpoint,currentEnchancePoint);
    	}else if(delpoint==2){
    		enhanceintelligence = enhanceintelligence -Utils.getEnhanceRation(delpoint,currentEnchancePoint);
    	}else if(delpoint==3){
    		enhancevitality = enhancevitality -Utils.getEnhanceRation(delpoint,currentEnchancePoint);
    	}else if(delpoint == 4){
    		enhanceagility = enhanceagility - Utils.getEnhanceRation(delpoint,currentEnchancePoint);
    	}   
    	currentEnchancePoint--;
    }
    //jwp add end
    public IEquipment getPetEquipment(int itemId, int id) {
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null){
            	if (usedEquipments[i].item.getItemId() == itemId && usedEquipments[i].item.getId() == id)
                    return (IEquipment) usedEquipments[i].item;
            }
        }
        return null;
    }
    
    public Grid[] getUsedEquipments() {
    	Grid[] ret = new Grid[usedEquipments.length];
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null)
                ret[i] = (Grid) usedEquipments[i];
        }
        return ret;
    }
    
    /**
     * @return 装备列表
     */
    public IEquipment[] getUsedEquipments2() {
        IEquipment[] ret = new IEquipment[usedEquipments.length];
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null)
                ret[i] = (IEquipment) usedEquipments[i].item;
        }
        return ret;
    }
    
    public void setUsedEquipments(Grid[] usedequipments){
    	usedEquipments = usedequipments;
    }
    public int[] getUsedEquipmentinfo() {
        return usedEquinfo;
    }
    public void setUsedEquipmentsinfo(int count,byte flag){
    	usedEquinfo[count] = flag;
    }
    public void setUsedEquipments(int count,IEquipment e){
    	if (e != null){
    		usedEquipments[count] = (Grid) e;
    	}else{
    		usedEquipments[count] = null;
    	}
    }
    public int getUsedEquipmentProperty(int pro) {
        int ret = 0;
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null) {
                IEquipment equ = (IEquipment) usedEquipments[i].item;
                if (equ.isValid())
                    ret += equ.getProperty(pro, getLevel());
            }
        }
        return ret;
    }
    
    /**
     * 获得当前装备的附魔属性
     * @param pro
     * @return
     */
    public int getUsedEquipmentEnchantingProperty(int pro){
    	int ret = 0;
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null) {
                IEquipment equ = (IEquipment) usedEquipments[i].item;
                if (equ.isValid())
                    ret += equ.getEnchanting().getProperty(pro);
            }
        }
        return ret;
    }
    
    /**
     * 设置宠物阵营宝石BUFF(人物扩展buff)
     * @param buff
     */
    public void setPetExtendBuff(List buf){
    	PetBufs = buf;
    }
    
    public List getpetBuf(){
    	return PetBufs;
    }
    
    /**
     * 获得宠物阵营buff值
     * @param pro
     * @return 
     */
    public Buf getPetBuf(int pro){
    	if(pro < 0){//扩展buff
    		int value = 0;
            for(int i=0;i<PetBufs.size();i++){
                Buf buf = (Buf)PetBufs.get(i);
                if(buf.getProperty()==pro) {
                	value = buf.getValue();
                	break;
                }
            }
            if (value > 0) {
            	Buf ret = new Buf(0, (byte)0, value, 0, (byte)0);
            	return ret;
            }
    	}
    	return null;
    }
    
    /**
     * 获得宠物装备阵营宝石加成
     * @param pro
     * @return
     */
    public int getUsedEquipmentPropertyStoneBuf(int pro){
    	int ret = 0;
    	Buf buf = getPetBuf(Buf.CAMP_STONE);		//阵营宝石加成
    	if(buf == null) return 0;
    	for (int i = 0; i < usedEquipments.length; i++) {//宠物装备
    		if (usedEquipments[i] != null) {
    			IEquipment equ = (IEquipment) usedEquipments[i].item;
    			if(equ.isValid()){
    				ret += equ.getDiamondMosiacProperty(pro);
    			}
    		}
    	}
    	return ret * buf.getValue() / 100;
    } 
    
    public int getEvolutionProperty(int pro){
    	EvolutionData data = EvolutionLoader.evolutions.get(evolutionLevel);
    	if(data != null){
    		switch(pro){
    		case IEquipment.EQUIP_ADD_HPMAX:
    			return data.hp;
    		case IEquipment.EQUIP_ADD_PATTACK:
    			return data.pa;
    		case IEquipment.EQUIP_ADD_MATTACK:
    			return data.pa;
    		case IEquipment.EQUIP_ADD_DEFENCE:
    			return data.pd;
    		case IEquipment.EQUIP_ADD_MDEFENCE:
    			return data.md;
    		}
    	}
    	return 0;
    }
    
    public int calculateMaxHp() {
        return Utils.calculateMaxHp(getRealVitality(), getRealAgility(),
                                    getRealStrength(), getRealIntelligence(),
                                    getLevel(), getUsedEquipmentProperty(IEquipment.EQUIP_ADD_HPMAX));
    }
    
    public int calculateMaxMp() {
        int vitality = getVitality() +
                       getUsedEquipmentProperty(IEquipment.EQUIP_ADD_VIT);

        int intelligence = getIntelligence() +
                           getUsedEquipmentProperty(IEquipment.EQUIP_ADD_INT);
        return Utils.calculateMaxMp(vitality, 0, 0, intelligence, getLevel(), getUsedEquipmentProperty(IEquipment.EQUIP_ADD_MPMAX));
    }

	   /**
     * 物品是否单独成列
     */
    private byte itemShowType;
    

	public byte getItemShowType() {
		return itemShowType;
	}

	public void setItemShowType(byte itemShowType) {
		this.itemShowType = itemShowType;
	}
	
	public int getNextExp() {
		return nextExp;
	}
	
	public byte[] toClientBytes(int dataVersion) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// 给宠物加悟性
    public boolean addPetPerceptionPoint (int exp) {
        int nPoint = getPerceptionPoint() + exp;
        int upPoint = Utils.getPetUpLevelPerceptionPoint(getPerceptionLevel());
        
        int nPoint1 = nPoint;
        int upPoint1 = upPoint;
        int upPerceptionLevel = 0;
        
        while (nPoint1 >= upPoint1) {
        	nPoint1 -= upPoint1;
        	upPerceptionLevel++;
    		upPoint1 = Utils.getPetUpLevelPerceptionPoint(getPerceptionLevel() + upPerceptionLevel);
        }
        
        if (upPerceptionLevel > 1) {
        	int tmpPerceptionLevel = getPerceptionLevel() + upPerceptionLevel - 1;
        	nPoint = nPoint1 + Utils.getPetUpLevelPerceptionPoint(tmpPerceptionLevel);
        	upPoint = Utils.getPetUpLevelPerceptionPoint(tmpPerceptionLevel);
        	setPerceptionLevel(tmpPerceptionLevel);
        }

        if (nPoint >= upPoint) {
        	if (getPerceptionLevel() + 1 >= Utils.PET_MAX_PERCEPTION_LEVEL) {
        		setPerceptionLevel(getPerceptionLevel() + 1);
        		setPerceptionPoint(0);
        		setNextPerceptionPoint(0);
        	} else {
        		setPerceptionLevel(getPerceptionLevel() + 1);
        		setPerceptionPoint(nPoint - upPoint);
        		setNextPerceptionPoint(Utils.getPetUpLevelPerceptionPoint(getPerceptionLevel()));
        	}
            return true;
        } else {
            int oldExp = getPerceptionPoint();
            nPoint = Math.min(upPoint-1, nPoint);  //不能超过最高升级点数
            int Exptmp = nPoint - getPerceptionPoint();
            setPerceptionPoint(nPoint);
            if (nPoint > oldExp) {
            	return true;
            }
            return false;
        }
    }
    
    public void setPetSkillAndEnhanceName (int petId, int lastPetPerceptionLevel, Changed changed) {
		int count = Utils.getAddSkillCount(this.getPerceptionLevel(), lastPetPerceptionLevel);
    	if (count > 0) {
    		Ability[] abs = Utils.getAddPetAbilities(this.getAbilityId(), count);
    		for (int j = 0; j < abs.length; j++) {
    			this.addAbility(abs[j]);
    		}
    		if(changed != null){
    			changed.addPetAbility(this, Changed.PET_ADD_SKILL, abs);
    		}
    	}
    	if (this.getPerceptionLevel() - lastPetPerceptionLevel > 0) {
    		this.setEnhanceName(Utils.getPerceptionLevelName(this.getPerceptionLevel()));
    		String newName = this.getName();
    		if(getBindType() > 0){
				newName = newName.concat("(" + (getBindType() + 1) + "代)");
			}
    		if(changed != null){
	    		if (this.getEnhanceName().equals("") && this.getEnhanceName().length() == 0) {
	    			changed.addPetProperty(this, Changed.PET_NAME, newName);
	    		} else {
	    			changed.addPetProperty(this, Changed.PET_NAME, newName + this.getEnhanceName());
	    		}
    		}
    	}
    }
    
    public int getEvolutionLevel(){
    	return evolutionLevel;
    }
    
    public void setEvolutionLevel(int level){
    	evolutionLevel = level;
    }
    
    public int getEvolutionPoint(){
    	return evolutionPoint;
    }
    
    public void setEvolutionPoint(int point){
    	evolutionPoint = point;
    }
    
    public int getEvolutionType(){
    	return evolutionType;
    }
    
    public void setEvolutionType(int type){
    	evolutionType = type;
    }
    
    public ArrayList<Integer> getEvolutionOpenPoints(){
    	return evolutionOpenPoints;
    }
}
