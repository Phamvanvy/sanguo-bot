package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Ability {

//    public static final int ENMITY_TYPE_SINGLE = 1;
//    public static final int ENMITY_TYPE_ALL = 2;


    public static final int TARGET_ENMY_SINGLE = 1;
    public static final int TARGET_ENMY_ALL = 2;
    public static final int TARGET_TEAM_SINGLE = 2;
    public static final int TARGET_TEAM_ALL = 3;

    public static final int ACTION_DEBUFF = 1;
    public static final int ACTION_NODEBUFF = 2;
    public static final int ACTION_BUFF = 3;
    public static final int ACTION_HEALTH = 4;
    public static final int ACTION_RELIVE = 5;
    public static final int ACTION_ATTACK = 6;
    public static final int ACTION_MAGIC = 7;

    private static final AbilityComparator comparator = new AbilityComparator();

    private int id;
    private String name;
    private int effect;
    private int status;
    private int position;
    private int cd;
    private int cdTime;
    private int maxLevel;
	private int level;
    private int value1;
    private int value2;
    private int hit;
    private int effectTime;
    private int mana;
    private byte type;
    private int requiredLevel;
    private int price;
    private String desc;
    private int enmity;
    private int adjust;
    private int enmityType;
    private int targetType;
    private int targetCount;
    private int arithmetic;
    private int actionType;

	public Ability() {
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setArithmetic(int arithmetic){
        this.arithmetic = arithmetic;
    }

    public int getArithmetic(){
        return arithmetic;
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

    public byte getType(){
        return type;
    }

    public void setRequiredLevel(int requiredLevel){
        this.requiredLevel = requiredLevel;
    }

    public int getRequiredLevel(){
        return requiredLevel;
    }



    public void setEffect(int effect){
        this.effect = effect;
    }

    public int getEffect() {
        return effect;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public int getPrice(){
        return price;
    }


    public void setStatus(int status){
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setPosition(int position){
        this.position = position;
    }


    public int getPosition() {
        return position;
    }

    public void setCD(int CD){
        this.cd = CD;
    }

    public int getCD() {
        return cd;
    }

    public void setCDTime(int cdTime){
        this.cdTime = cdTime;
    }

    public int getCDTime() {
        return cdTime;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
    public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

    public void setValue1(int value1){
        this.value1 = value1;
    }

    public int getValue1() {
        return value1;
    }


    public void setValue2(int value2){
        this.value2 = value2;
    }

    public int getValue2() {
        return value2;
    }


    public void setHit(int hit){
        this.hit = hit;
    }

    public int getHit() {
        return hit;
    }

    public void setEffectTime(int effectTime){
        this.effectTime = effectTime;
    }

    public int getEffectTime() {
        return effectTime;
    }

    public void setMana(int mana){
        this.mana = mana;
    }

    public int getMana() {
        return mana;
    }

    public int getMpUse(int oppLevel, int currentMp){
        int result;
        int mpUse = getMana();
        
        if(mpUse >= 0){
            result = mpUse;
        }else if(mpUse > -9999){
            result = (short)(-mpUse * oppLevel / 100);
        }else{ //小于-9999的设置视为消耗全部mp
            result = currentMp;

            if(result <= 0){
                result = 1;
            }
        }
        
        return result;
    }
    
    public String getDesc(){
        return desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public void setEnmity(int enmity){
        this.enmity = enmity;
    }

    public int getEnmity(){
        return enmity;
    }

    public int getAdjust(){
        return adjust;
    }

    public void setAdjust(int adjust){
        this.adjust = adjust;
    }

    public int getEnmityType(){
        return enmityType;
    }

    public void setEnmityType(int enmityType){
        this.enmityType = enmityType;
    }

    public int getTargetType(){
        return targetType;
    }

    public void setTargetType(int targetType){
        this.targetType = targetType;
    }

    public int getTargetCount(){
        return targetCount;
    }

    public int getActionType() {
        return actionType;
    }

    public void setTargetCount(int targetCount){
        this.targetCount = targetCount;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public boolean equals(Object obj) {
        if(this==obj) return true;
        Ability obj1 = (Ability)obj;
        if(obj1.id==this.id) return true;
        return false;
    }

    private static final Map abilities = new HashMap();

    public static void addAbility(Ability ability){
        abilities.put(new Integer(ability.getId()),ability);
    }

    public static Ability getAbility(int id){
        return (Ability)abilities.get(new Integer(id));
    }

    public static Ability[] getAbilitites(){
        Ability[] ret = new Ability[abilities.size()];
        Collection c = abilities.values();
        c.toArray(ret);
        return ret;
    }

    public static Ability[] getAbilities(byte type,int level,int[] filter){
        Collection c = abilities.values();
        Iterator ite = c.iterator();
        List l = new ArrayList();
        while(ite.hasNext()){
            Ability ability = (Ability)ite.next();
            if(ability.getType()==type&&ability.getRequiredLevel()<=level){
                if(!contains(ability.getId(),filter)){
                    l.add(ability);
                }
            }
        }
        Ability[] ret = new Ability[l.size()];
        l.toArray(ret);
        return ret;
    }

    public static Ability[] getAbilities(Ability[] abilities,int level,int[] filter){
        List l = new ArrayList();
        for(int i=0;i<abilities.length;i++){
            if (!contains(abilities[i].getId(), filter)) {
                l.add(abilities[i]);
            }
        }
        Collections.sort(l,comparator);
        Ability[] ret = new Ability[l.size()];
        l.toArray(ret);
        return ret;
    }

    private static boolean contains(int id,int[] filter){
        if(filter==null)
            return false;
        for(int i=0;i<filter.length;i++){
            if(filter[i]==id)
                return true;
        }
        return false;
    }
    public static byte[] getAllAbilitiesBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        Ability[] abilities = Ability.getAbilitites();
        Map map = new HashMap();
        for (int i = 0; i < abilities.length; i++) {
            if(abilities[i].getId()<10000){ //10000以上为boss技能
                int effect = abilities[i].getEffect();
                List l = (List) map.get(new Integer(effect));
                if (l == null) {
                    l = new ArrayList();
                    map.put(new Integer(effect), l);
                }
                l.add(abilities[i]);
            }
        }
        dos.writeShort(map.size());
        Collection c = map.values();
        Iterator ite = c.iterator();
        while (ite.hasNext()) {
            List sub = (List) ite.next();
            for (int j = 0; j < sub.size(); j++) {
                Ability ability = (Ability) sub.get(j);
                if (j == 0) {
                    dos.writeByte(ability.getType());
                    dos.writeUTF(ability.getName());
                    dos.writeByte(ability.getEffect());
                    dos.writeByte(ability.getStatus());
                    dos.writeByte(ability.getPosition());
                    dos.writeByte(ability.getCD());
                    dos.writeByte(ability.getCDTime());
                    dos.writeByte(sub.size());
                }
                dos.writeShort(ability.getId());
                dos.writeByte(ability.getLevel());
                dos.writeInt(ability.getValue1());
                dos.writeInt(ability.getValue2());
                dos.writeByte(ability.getEffectTime());
                dos.writeShort(ability.getMana());
                dos.writeByte(ability.getArithmetic());
                dos.writeByte(ability.getHit());
            }
        }
        return bos.toByteArray();
    }
    
    public Ability getAbilityCloning() {
    	Ability AbilityCloning = new Ability();
    	AbilityCloning.setId(getId());
    	AbilityCloning.setName(getName());
    	AbilityCloning.setEffect(getEffect());
    	AbilityCloning.setStatus(getStatus());
    	AbilityCloning.setPosition(getPosition());
    	AbilityCloning.setCD(getCD());
    	AbilityCloning.setCDTime(getCDTime());
    	AbilityCloning.setLevel(getLevel());
    	AbilityCloning.setValue1(getValue1());
    	AbilityCloning.setValue2(getValue2());
    	AbilityCloning.setHit(getHit());
    	AbilityCloning.setEffectTime(getEffectTime());
    	AbilityCloning.setMana(getMana());
    	AbilityCloning.setType(getType());
    	AbilityCloning.setRequiredLevel(getRequiredLevel());
    	AbilityCloning.setPrice(getPrice());
    	AbilityCloning.setDesc(getDesc());
    	AbilityCloning.setEnmity(getEnmity());
    	AbilityCloning.setAdjust(getAdjust());
    	AbilityCloning.setEnmityType(getEnmityType());
    	AbilityCloning.setTargetType(getTargetType());
    	AbilityCloning.setTargetCount(getTargetCount());
    	AbilityCloning.setArithmetic(getArithmetic());
    	AbilityCloning.setActionType(getActionType());
		return AbilityCloning;
    }
}

class AbilityComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        Ability a1 = (Ability)o1;
        Ability a2 = (Ability)o2;
        return a1.getRequiredLevel()-a2.getRequiredLevel();
    }

}
