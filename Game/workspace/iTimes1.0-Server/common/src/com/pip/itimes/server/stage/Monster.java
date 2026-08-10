package com.pip.itimes.server.stage;


import java.util.List;
import java.util.ArrayList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.ArrayIntList;

/**
 * @author Jeffery
 * @version 1.0
 */
public class Monster{

    private static FallItem WORLD_FALL_0 = new FallItem((byte) 8, 0, 0, 1000, 100000);
    private static FallItem WORLD_FALL_1 = new FallItem((byte) 8, 0, 0, 1001, 100000);
    private static FallItem WORLD_FALL_2 = new FallItem((byte) 8, 0, 0, 1002, 200000);
    private static FallItem WORLD_FALL_3 = new FallItem((byte) 8, 0, 0, 1003, 200000);
    private static FallItem WORLD_FALL_4 = new FallItem((byte) 8, 0, 0, 1004, 200000);
    private static FallItem WORLD_FALL_5 = new FallItem((byte) 8, 0, 0, 1005, 200000);
    private static FallItem WORLD_FALL_6 = new FallItem((byte) 8, 0, 0, 1006, 200000);
    private static FallItem WORLD_FALL_7 = new FallItem((byte) 8, 0, 0, 1007, 200000);
    private static FallItem WORLD_FALL_8 = new FallItem((byte) 8, 0, 0, 1008, 200000);
    private static FallItem WORLD_FALL_9 = new FallItem((byte) 8, 0, 0, 1009, 200000);

    private static  FallItem LOCAL_WORLD_FALL_0 = new FallItem((byte) 8, 0, 0, 2000, 50000);
    private static  FallItem LOCAL_WORLD_FALL_1 = new FallItem((byte) 8, 0, 0, 2001, 50000);
    private static  FallItem LOCAL_WORLD_FALL_2 = new FallItem((byte) 8, 0, 0, 2002, 50000);
    private static  FallItem LOCAL_WORLD_FALL_3 = new FallItem((byte) 8, 0, 0, 2003, 50000);
    private static  FallItem LOCAL_WORLD_FALL_4 = new FallItem((byte) 8, 0, 0, 2004, 50000);
    private static  FallItem LOCAL_WORLD_FALL_5 = new FallItem((byte) 8, 0, 0, 2005, 50000);
    private static  FallItem LOCAL_WORLD_FALL_6 = new FallItem((byte) 8, 0, 0, 2006, 50000);
    private static  FallItem LOCAL_WORLD_FALL_7 = new FallItem((byte) 8, 0, 0, 2007, 50000);
    private static  FallItem LOCAL_WORLD_FALL_8 = new FallItem((byte) 8, 0, 0, 2008, 50000); 
    private static  FallItem LOCAL_WORLD_FALL_9 = new FallItem((byte) 8, 0, 0, 2009, 50000);

    
    private FallItem[] LOCAL_WORLD_FALL = {LOCAL_WORLD_FALL_0,LOCAL_WORLD_FALL_1, LOCAL_WORLD_FALL_2, LOCAL_WORLD_FALL_3, LOCAL_WORLD_FALL_4, LOCAL_WORLD_FALL_5, LOCAL_WORLD_FALL_6,
    		LOCAL_WORLD_FALL_7, LOCAL_WORLD_FALL_8, LOCAL_WORLD_FALL_9};
    
    private FallItem[] WORLD_FALL = {WORLD_FALL_0,WORLD_FALL_1, WORLD_FALL_2, WORLD_FALL_3, WORLD_FALL_4, WORLD_FALL_5, WORLD_FALL_6,
                                    WORLD_FALL_7, WORLD_FALL_8, WORLD_FALL_9};
    
//    public static  FallItem HOLIDAY_WORLD_FALL_1 = new FallItem((byte) 8, 0, 0, 302, 50000);//5%
//    public static  FallItem HOLIDAY_WORLD_FALL_2 = new FallItem((byte) 8, 0, 0, 302, 50000);//5%
//    public static  FallItem HOLIDAY_WORLD_FALL_3 = new FallItem((byte) 8, 0, 0, 302, 50000);//5%
//    private FallItem[] HOLIDAY_FALL = {null,null, HOLIDAY_WORLD_FALL_1, HOLIDAY_WORLD_FALL_1, HOLIDAY_WORLD_FALL_1, 
//    									HOLIDAY_WORLD_FALL_2, HOLIDAY_WORLD_FALL_2,HOLIDAY_WORLD_FALL_2,
//    									HOLIDAY_WORLD_FALL_3,HOLIDAY_WORLD_FALL_3};
//    
//    public static  FallItem HOLIDAY_WORLD_FALL2_1 = new FallItem((byte) 8, 0, 0, 302, 50000);//5%
//    public static  FallItem HOLIDAY_WORLD_FALL2_2 = new FallItem((byte) 8, 0, 0, 302, 50000);//5%
//    public static  FallItem HOLIDAY_WORLD_FALL2_3 = new FallItem((byte) 8, 0, 0, 302, 50000);//5%
//    private FallItem[] HOLIDAY_FALL2 = {null,null, HOLIDAY_WORLD_FALL2_1, HOLIDAY_WORLD_FALL2_1, HOLIDAY_WORLD_FALL2_1, 
//    		HOLIDAY_WORLD_FALL2_2, HOLIDAY_WORLD_FALL2_2,HOLIDAY_WORLD_FALL2_2,
//    		HOLIDAY_WORLD_FALL2_3,HOLIDAY_WORLD_FALL2_3};

    private short stageId;
    private int index;
    private int pngId;
    private String name;
    private byte type;
    private short level;
    private short vit;
    private short str;
    private short Int;
    private short agi;
    private int pMinAttack;
    private int pMaxAttack;
    private int mMinAttack;
    private int mMaxAttack;
    private int mDef;
    private int pDef;
    private short parry;
    private short hit;
    private short pCritial;
    private short mCritial;
    private int hp;
    private int mp;
    private int maxHp;
    private int maxMp;
    private short exp;
    private byte petType;
    private int babyRate;
    private boolean canCatch;
    private IntList abilities = new ArrayIntList();
    private IntList items = new ArrayIntList();
    private IntList counts = new ArrayIntList();
    private List fallItems = new ArrayList();
    private int specialHP;
    private int specialMP;
    private String aiClass;

    public Monster() {
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public void setStageId(short stageId){
        this.stageId = stageId;
    }

    public short getStageId(){
        return stageId;
    }

    public void setPngId(int pngId){
        this.pngId = pngId;
    }

    public int getPngId() {
        return pngId;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(byte type){
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public void setLevel(short level){
        this.level = level;
    }

    public short getLevel() {
        return level;
    }

    public void setVit(short vit){
        this.vit = vit;
    }

    public short getVit() {
        return vit;
    }

    public void setStr(short str){
        this.str = str;
    }

    public short getStr() {
        return str;
    }

    public void setInt(short Int){
        this.Int = Int;
    }

    public short getInt() {
        return Int;
    }

    public void setAgi(short agi){
        this.agi = agi;
    }

    public short getAgi() {
        return agi;
    }

    public void setPMinAttack(int pMinAttack){
        this.pMinAttack = pMinAttack;
    }

    public int getPMinAttack() {
        return pMinAttack;
    }

    public void setPMaxAttack(int pMaxAttack){
        this.pMaxAttack = pMaxAttack;
    }

    public int getPMaxAttack() {
        return pMaxAttack;
    }

    public void setPDef(int pDef){
        this.pDef = pDef;
    }

    public int getPDef() {
        return pDef;
    }

    public void setMMinAttack(int mMinAttack){
        this.mMinAttack = mMinAttack;
    }

    public int getMMinAttack() {
        return mMinAttack;
    }

    public void setMMaxAttack(int mMaxAttack){
        this.mMaxAttack = mMaxAttack;
    }

    public int getMMaxAttack() {
        return mMaxAttack;
    }

    public void setParry(short parry){
        this.parry = parry;
    }

    public short getParry() {
        return parry;
    }

    public void setHit(short hit){
        this.hit = hit;
    }

    public short getHit() {
        return hit;
    }

    public void setPCritial(short pCritial){
        this.pCritial = pCritial;
    }

    public short getPCritial() {
        return pCritial;
    }

    public void setMCritial(short mCritial){
        this.mCritial = mCritial;
    }

    public short getMCritial() {
        return mCritial;
    }
    
    public void setMaxHp(int maxHp){
    	this.maxHp = maxHp;
    }
    
    public int getMaxHp(){
    	return maxHp;
    }

    public void setHp(int hp){
        this.hp = hp;
    }

    public int getHp() {
        return hp;
    }
    
    public int getMaxMp() {
    	return maxMp;
    }
    
    public void setMaxMp(int maxMp){
    	this.maxMp = maxMp;
    }

    public void setMp(int mp){
        this.mp = mp;
    }

    public int getMp() {
        return mp;
    }

    public void setExp(short exp){
        this.exp = exp;
    }

    public short getExp() {
        return exp;
    }

    public void addAbility(int ability){
        abilities.add(ability);
    }

    public int[] getAbilities() {
        return abilities.toArray();
    }

    public void addItem(int item){
        for(int i=0;i<items.size();i++){
            if(items.get(i)==item){
                int count = counts.get(i);
                count++;
                counts.set(i,count);
                return;
            }
        }
        items.add(item);
        counts.add(0);
    }

    public void addItem(int item,int nCount){
        for(int i=0;i<items.size();i++){
            if(items.get(i)==item){
                int count = counts.get(i);
                count += nCount;
                counts.set(i,count);
                return;
            }
        }
        items.add(item);
        counts.add(nCount);
    }

    public int[][] getItems() {
        int[][] ret = new int[items.size()][2];
        for(int i=0;i<items.size();i++){
            ret[i][0] = items.get(i);
            ret[i][1] = counts.get(i);
        }
        return ret;
    }

    public void addFallItem(FallItem fallItem){
        fallItems.add(fallItem);
    }

    public FallItem[] getFallItems() {
        int size = fallItems.size();
//        if(getLevel()>=11){
            size++;
//        }
        FallItem[] ret = new FallItem[size];
        fallItems.toArray(ret);
//        if(getLevel()>=11){
            ret[ret.length-1] = WORLD_FALL[(getLevel()-1)/10];
//        }
        return ret;
    }
    
    public FallItem[] getholidayFallItems(FallItem[] holiday_fall) {
        FallItem addedFallItem = holiday_fall[(getLevel()-1)/10];
//        int size = fallItems.size();
        int size = 0;
        
        if(addedFallItem != null){
            size++;
        }
        FallItem[] ret = new FallItem[size];
        fallItems.toArray(ret);
        
        if(addedFallItem != null){
            ret[ret.length-1] = addedFallItem;
        }
        
        return ret;
    }
    
//    public FallItem[] getholidayFall2Items() {
//    	FallItem addedFallItem = HOLIDAY_FALL2[(getLevel()-1)/10];
////        int size = fallItems.size();
//    	int size = 0;
//    	
//    	if(addedFallItem != null){
//    		size++;
//    	}
//    	FallItem[] ret = new FallItem[size];
//    	fallItems.toArray(ret);
//    	
//    	if(addedFallItem != null){
//    		ret[ret.length-1] = addedFallItem;
//    	}
//    	
//    	return ret;
//    }
    public FallItem[] getLocalFallItems() {
        int size = fallItems.size();
//        if(getLevel()>=11){
            size++;
//        }
        FallItem[] ret = new FallItem[size];
        fallItems.toArray(ret);
//        if(getLevel()>=11){
            ret[ret.length-1] = LOCAL_WORLD_FALL[(getLevel()-1)/10];
//        }
        return ret;
    }
    public void setMDef(int mDef){
        this.mDef = mDef;
    }

    public int getMDef(){
        return mDef;
    }

    public byte getPetType(){
        return petType;
    }

    public void setPetType(byte petType){
        this.petType = petType;
    }

    public boolean getCanCatch(){
        return canCatch;
    }

    public void setCanCatch(boolean canCatch){
        this.canCatch = canCatch;
    }

    public void setBabyRate(int rate){
        this.babyRate = rate;
    }

    public int getBabyRate(){
        return babyRate;
    }

    public void setAiClass(String aiClass){
        this.aiClass = aiClass;
    }

    public String getAiClass(){
        return aiClass;
    }
    
    public int getSpecialHP(){
    	return specialHP;
    }
    
    public void setSpecialHP(int specialHP){
    	this.specialHP = specialHP;
    }
    
    public int getSpecialMP(){
    	return specialMP;
    }
    
    public void setSpecialMP(int specialMP){
    	this.specialMP = specialMP;
    }
}
