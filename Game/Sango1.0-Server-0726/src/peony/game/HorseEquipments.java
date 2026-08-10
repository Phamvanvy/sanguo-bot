package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import peony.game.changed.DurationChangedItem;
import peony.game.changed.HorseEquipChangedItem;

public class HorseEquipments {
	
	private static final Random RND = new Random();
	
//	public static final int MINORTYPE_HORSE_HEAD = 21; //马,面具
//	public static final int MINORTYPE_HORSE_NECK = 22;//马,颈甲
//	public static final int MINORTYPE_HORSE_CHEST = 23;//马,胸甲
//	public static final int MINORTYPE_HORSE_ASS = 24;//马,臀甲
//	public static final int MINORTYPE_HORSE_BACK = 25;//马,鞍
//	public static final int MINORTYPE_HORSE_LEG = 26;//马,蹄

	public static final int HEAD = 0;      // 头
	public static final int NECK = 1;      // 脖子
	public static final int CHEST = 2;     // 胸
	public static final int ASS = 3;     // 臀
	public static final int BACK = 4;      // 鞍
	public static final int LEG = 5;    // 蹄
	public static final int PEDAL = 6; //脚蹬
	
	
	// 装备类型对应的部位
	public static final int[] EQU_INDEXES = {
		HEAD,
		NECK,
		CHEST,
		ASS,
		BACK,
		LEG,
		PEDAL,
	};
	
//	//防具的index
//	public static final int[] ARMOR_INDEXES = {
//		HEAD,
//		CHEST,
//		LEG,
//		FEET,
//		HAND2
//	};
		
	public GameItem[] equs;
	
	public Horse owner;
	
	
	
	public HorseEquipments(Horse owner){
		equs = new GameItem[7];
		this.owner = owner;
	}
	
	public boolean isEmpty(){
		for(GameItem item:equs){
			if(item!=null)
				return false;
		}
		return true;
	}
	
	@Override
	public HorseEquipments clone(){
		HorseEquipments ret = new HorseEquipments(owner);
		for(int i=0;i<equs.length;i++){
			ret.equs[i] = equs[i];
		}
		return ret;
	}
	
	public HorseEquipments clone1(){
		HorseEquipments ret = new HorseEquipments(owner);
		for(int i=0;i<equs.length;i++){
			if(equs[i]!=null)
				ret.equs[i] = equs[i].clone();
			else
				ret.equs[i] = null;
		}
		return ret;
	}
	
	public GameItem equip(GameItem equ,Unit horseOwner){
		if(!equ.template.isEquipment())
			throw new IllegalArgumentException();
		int index = getIndex(equ.template.equipment.minorType);
		GameItem oldItem = equs[index];
		equs[index] = equ;
		if(horseOwner.changed!=null){
			HorseEquipChangedItem changedItem = new HorseEquipChangedItem(owner,index,equ);
			horseOwner.changed.addChangedItem(changedItem);
		}
		return oldItem;
	}
	
	public GameItem unequip(int itemId,int instanceId,Unit horseOwner){
		for(int i=0;i<equs.length;i++){
			if(equs[i]!=null&&equs[i].template.id==itemId&&equs[i].instanceId==instanceId){
				GameItem old = equs[i];
				equs[i] = null;
				if(horseOwner.changed!=null){
					HorseEquipChangedItem changedItem = new HorseEquipChangedItem(owner,i,null);
					horseOwner.changed.addChangedItem(changedItem);
				}
				return old;
			}
		}
		return null;
	}
	
	/**
	 * 查找装备。
	 * @param itemId
	 * @param instanceId
	 * @param horseOwner
	 * @return
	 */
	public GameItem find(int itemId, int instanceId) {
        for (int i = 0; i < equs.length; i++) {
            if (equs[i] != null && equs[i].template.id == itemId && equs[i].instanceId == instanceId) {
                return equs[i];
            }
        }
        return null;
    }
	
	public void enhance(PropertyCalculator pc, boolean includeBasicAttrs) {
		for(int i=0;i<equs.length;i++){
			GameItem item = equs[i];
			if(item!=null){
			    if (includeBasicAttrs) {
			        item.enhance(pc);
			    } else {
			        item.enhanceWithOutBasicAttrs(pc);
			    }
			}
		}
	}
	
//	public int getArmor(){
//		int armor = 0;
//		for(int i=0;i<equs.length;i++){
//			if(equs[i]!=null){
//				armor += equs[i].template.equipment.armor;
//			}
//		}
//		return armor;
//	}

	
	public static int getIndex(int minorType){
		return EQU_INDEXES[minorType-21];
	}
	
	public byte[] toClientBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			for(GameItem item:equs){
				if(item!=null){
					dos.write(1);
					dos.write(item.toClientBytes());
				}else{
					dos.write(0);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
//	public int getHeadScore(int playerLevel,int clazz) {
//		int point = (int)(getPoint(HEAD, playerLevel) + getPoint(NECK, playerLevel) + getPoint(
//				FINGER, playerLevel)+getPoint(BACK,playerLevel));
//		int imageId = getImageId(point,0);
//		if(point==0)
//			return imageId;
//		if(clazz==Unit.CLASS_1||clazz==Unit.CLASS_2){ // 分近战跟法师
//			imageId |= 1<<16;
//		}else{
//			imageId |= 2<<16;
//		}
//		return imageId;
//	}

//	public int getBodyScore(int playerLevel,int clazz) {
//		int point =  (int)(getPoint(CHEST, playerLevel) + getPoint(LEG, playerLevel)
//				+ getPoint(FEET, playerLevel) + getPoint(WRIST, playerLevel) + getPoint(HAND2,playerLevel));
//		int imageId = getImageId(point,1);
//		if(point==0)
//			return imageId;
//		if(clazz==Unit.CLASS_1||clazz==Unit.CLASS_2){ 
//			imageId |= 1<<16;
//		}else{
//			imageId |= 2<<16;
//		}
//		return imageId;
//	}
	
//	public int getWeaponScore(int playerLevel,int clazz) {
//		int point = (int)getPoint(HAND, playerLevel);
//		int imageId = getImageId(point,2);
//		GameItem equ = equs[HAND];
//		if (equ != null) {
//			int minorType = equ.template.equipment.minorType;
//			imageId |= (minorType << 16);
//			if (minorType == EquipmentTemplate.MINORTYPE_AXE
//					|| minorType == EquipmentTemplate.MINORTYPE_SPEAR
//					|| minorType == EquipmentTemplate.MINORTYPE_POLEARM) { //有柄
//				imageId |= 1<<31;
//			}
//		}
//		return imageId;
//	}
	
	
	protected float getPoint(int index,int playerLevel){
		GameItem equ = equs[index];
		if(equ==null)
			return 0.0f;
		return equ.template.equipment.value;
	}
	


	
//	protected int getImageId(int point,int type){
//		if(type==0){
//			if(point==0)
//				return 0;
//			if(point>=1&&point<=466){
//				return 1;
//			}
//			else if(point>=467&&point<=933){
//				return 2;
//			}
//			else if(point>=934&&point<=2799){
//				return 3;
//			}
//			else if(point>=2800&&point<=4665){
//				return 4;
//			}
//			else if(point>=4666&&point<=5832){
//				return 5;
//			}
//		}
//		else if(type==1){
//			if(point==0)
//				return 0;
//			if(point>=1&&point<=282){
//				return 1;
//			}
//			else if(point>=283&&point<=564){
//				return 2;
//			}
//			else if(point>=565&&point<=1693){
//				return 3;
//			}
//			else if(point>=1694&&point<=2822){
//				return 4;
//			}
//			else if(point>=2823&&point<=3528){
//				return 5;
//			}
//		}
//		else if(type==2){
//			if(point==0)
//				return 0;
//			if(point>=1&&point<=288){
//				return 1;
//			}
//			else if(point>=289&&point<=576){
//				return 2;
//			}
//			else if(point>=577&&point<=1728){
//				return 3;
//			}
//			else if(point>=1729&&point<=2880){
//				return 4;
//			}
//			else if(point>=2881&&point<=3600){
//				return 5;
//			}
//		}
//			return 5;
//	}
	
	//根据百分比扣除所有防具的耐久
	public void decAllArmorDuration(int value, int percent,Player p) {
		for (int i = 0; i < equs.length; i++) {
			GameItem item = equs[i];
			if (item != null && item.duration > 0) {
				if (CommonUtil.hit(RND, percent, 100)) {
					decDuration(item,value,p);
				}
			}
		}
	}
	
	//按照剩余耐久的百分比扣除所有防具的耐久
//	public void decAllArmorRemainDuration(int percent) {
//		for (int i = 0; i < ARMOR_INDEXES.length; i++) {
//			GameItem item = equs[ARMOR_INDEXES[i]];
//			if (item != null && item.duration > 0) {
//				int value = item.template.equipment.duration * percent /100;
//				decDuration(item,value);
//			}
//		}
//	}
	
	//按照percent的百分几率扣除武器的耐久值
//	public void decWeaponDuration(int value,int percent){
//		GameItem item = equs[HAND];
//		if(item!=null&&item.duration>0){
//			if (CommonUtil.hit(RND, percent, 100)) {
//				decDuration(item,value);
//			}
//		}
//	}
	
	protected void decDuration(GameItem item,int value,Player p){
		item.duration = Math.max(0, item.duration - value);
		if(item.duration==0){
			owner.refreshProperties(false,p);
			if(p!=null){
				p.refreshProperties(false);
			}
		}
		DurationChangedItem changedItem = new DurationChangedItem(item);
		if(p!=null)
			p.changed.addChangedItem(changedItem);
	}
	
	public void repair(Player p) {
		boolean needRefresh = false;
		for (int i = 0; i < equs.length; i++) {
			GameItem item = equs[i];
			if (item != null) {
			    if (item.duration == 0) {
			        needRefresh = true;
			    }
			    item.repair(p);
			}
		}
		if (needRefresh) {
			owner.refreshProperties(false, p);
			if(p!=null&&p.horse==owner)
				p.refreshProperties(false);
		}
	}
}
