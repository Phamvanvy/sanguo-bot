package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import peony.game.changed.DurationChangedItem;
import peony.game.changed.EquipChangedItem;
import peony.game.changed.SkillChangedItem;
import peony.game.itemenhance.ItemEnhance;

public class Equipments implements PropertyEnhancer{
	
	private static final Random RND = new Random();

//	public static final int HEAD = 0;      // 头
//	public static final int NECK = 1;      // 脖子
//	public static final int CHEST = 2;     // 胸
//	public static final int WRIST = 5;     // 护腕
//	public static final int HAND = 3;      // 手
//	public static final int FINGER = 4;    // 手指
//	public static final int LEG = 6;       // 腿
//	public static final int FEET = 7;      // 脚
//	public static final int HAND2 = 8;     // 副手
//	public static final int BACK = 9;      // 背
	
	public static final int HEAD = 1;      // 头
	public static final int NECK = 2;      // 脖子
	public static final int CHEST = 0;     // 胸
	public static final int WRIST = 7;     // 护腕
	public static final int HAND = 5;      // 手
	public static final int FINGER = 6;    // 手指
	public static final int LEG = 8;       // 腿
	public static final int FEET = 9;      // 脚
	public static final int HAND2 = 4;     // 副手
	public static final int BACK = 3;      // 背
	
	public byte starState = 0; //星辉状态
	
	// 品质对应的价值
	public static final int[] POINT = {
		100,
		120,
		140,
		150,
		170
	};
	
	// 装备类型对应的部位
	public static final int[] EQU_INDEXES = {
		HAND,
		HAND,
		HAND,
		HAND,
		HAND,
		HAND,
		HAND,
		HEAD,
		CHEST,
		LEG,
		FEET,
		HAND2,
		WRIST,
		NECK,
		FINGER,
		BACK
	};
	
	//防具的index
	public static final int[] ARMOR_INDEXES = {
		HEAD,
		CHEST,
		LEG,
		FEET,
		HAND2
	};
		
	public GameItem[] equs;
	
	public Unit owner;
	
	
	
	public Equipments(Unit owner){
		equs = new GameItem[10];
		this.owner = owner;
	}
	
	
	@Override
	public Equipments clone(){
		Equipments ret = new Equipments(owner);
		for(int i=0;i<equs.length;i++){
			ret.equs[i] = equs[i];
		}
		return ret;
	}
	
	public Equipments clone1(){
		Equipments ret = new Equipments(owner);
		for(int i=0;i<equs.length;i++){
			if(equs[i]!=null)
				ret.equs[i] = equs[i].clone();
			else
				ret.equs[i] = null;
		}
		return ret;
	}
	
	public GameItem equip(GameItem equ){
		if(!equ.template.isEquipment())
			throw new IllegalArgumentException();
		int index = getIndex(equ.template.equipment.minorType);
		GameItem oldItem = equs[index];
		equs[index] = equ;
		if (oldItem == null
				&& equ != null
				&& equ.template.equipment.minorType == EquipmentTemplate.MINORTYPE_BOW) {
			if(owner.changed!=null){
				SkillChangedItem changedItem = new SkillChangedItem(ObjectAccessor.getSkill(1),false);
				owner.changed.addChangedItem(changedItem);
			}
		}
		if (oldItem != null
				&& equ == null
				&& oldItem.template.equipment.minorType == EquipmentTemplate.MINORTYPE_BOW) {
			if (owner.changed != null) {
				SkillChangedItem changedItem = new SkillChangedItem(
						ObjectAccessor.getSkill(1), false);
				owner.changed.addChangedItem(changedItem);
			}
		}
		if(owner.changed!=null){
			EquipChangedItem changedItem = new EquipChangedItem(index,equ);
			owner.changed.addChangedItem(changedItem);
		}
		return oldItem;
	}
	
	public GameItem unequip(int itemId,int instanceId){
		for(int i=0;i<equs.length;i++){
			if(equs[i]!=null&&equs[i].template.id==itemId&&equs[i].instanceId==instanceId){
				GameItem old = equs[i];
				equs[i] = null;
				if(old!=null&&old.template.equipment.minorType==EquipmentTemplate.MINORTYPE_BOW){
					if (owner.changed != null) {
						SkillChangedItem changedItem = new SkillChangedItem(
								ObjectAccessor.getSkill(1), false);
						owner.changed.addChangedItem(changedItem);
					}
				}
				if(owner.changed!=null){
					EquipChangedItem changedItem = new EquipChangedItem(i,null);
					owner.changed.addChangedItem(changedItem);
				}
				return old;
			}
		}
		return null;
	}
	
	/**
	 * 查找某件装备。
	 * @param itemId
	 * @param instanceId
	 * @return
	 */
	public GameItem find(int itemId,int instanceId) {
        for (int i = 0; i < equs.length; i++) {
            if (equs[i] != null && equs[i].template.id == itemId && 
                    equs[i].instanceId == instanceId) {
                return equs[i];
            }
        }
        return null;
    }
	
	public void enhance(PropertyCalculator pc) {
		for(int i=0;i<equs.length;i++){
			GameItem item = equs[i];
			if(item!=null){
				item.enhance(pc);
			}
		}
	}
	
	public GameItem getWeapon(){
		return equs[HAND];
	}
	
	public int getArmor(ItemEnhance ie){
		int armor = 0;
		for(int i=0;i<equs.length;i++){
			if(equs[i]!=null){
				armor += equs[i].template.equipment.getArmor(ie);
			}
		}
		return armor;
	}

	
	public static int getIndex(int minorType){
		return EQU_INDEXES[minorType-1];
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
	
	public int getHeadScore(int playerLevel,int clazz) {
		int point = (int)(getPoint(HEAD, playerLevel) + getPoint(NECK, playerLevel) + getPoint(
				FINGER, playerLevel)+getPoint(BACK,playerLevel));
		int imageId = getImageId(point,0);
		if(point==0)
			return imageId;
		imageId |= (clazz + 1) << 16;
		return imageId;
	}

	public int getBodyScore(int playerLevel,int clazz) {
		int point =  (int)(getPoint(CHEST, playerLevel) + getPoint(LEG, playerLevel)
				+ getPoint(FEET, playerLevel) + getPoint(WRIST, playerLevel) + getPoint(HAND2,playerLevel));
		int imageId = getImageId(point,1);
		if(point==0)
			return imageId;
		imageId |= (clazz + 1) << 16;
		return imageId;
	}
	
	public int getWeaponScore(int playerLevel,int clazz) {
		int point = (int)getPoint(HAND, playerLevel);
		int imageId = getImageId(point,2);
//		if(point==0)
//			return imageId;
		GameItem equ = equs[HAND];
		if (equ != null) {
			int minorType = equ.template.equipment.minorType;
			imageId |= (minorType << 16);
			if (minorType == EquipmentTemplate.MINORTYPE_AXE
					|| minorType == EquipmentTemplate.MINORTYPE_SPEAR
					|| minorType == EquipmentTemplate.MINORTYPE_POLEARM) { //有柄
				imageId |= 1<<31;
			}
		}
		return imageId;
	}
	
	public byte getFlashLevel(){
		return getFlashLevel(equs);
	}
	
	public static byte getFlashLevel(GameItem[] equs){
		int[] levels = new int[5]; //一共5种发光效果 
		for(GameItem equ:equs){
			if(equ != null){
				GameItemObject itemObject = equ.object;
				if(itemObject instanceof ItemEnhance){
					ItemEnhance enhance = (ItemEnhance)itemObject;
					int[] jewels = enhance.getJewelIDs();
					for(int id:jewels){
						ItemTemplate template = ObjectAccessor.getItemTemplate(id);
						if(template != null && template.useLevel-3>=0){
							levels[template.useLevel-3]++;
						}
					}
				}
			}
		}
		if(levels[4]>=51){
			return 6;
		}else if(levels[4]>=35){
			return 5;
		}else if(levels[4] + levels[3] >= 35){
			return 4;
		}else if(levels[4] + levels[3] + levels[2] >= 35){
			return 3;
		}else if(levels[4] + levels[3] + levels[2] + levels[1] >= 35){
			return 2;
		}else if(levels[4] + levels[3] + levels[2] + levels[1] + levels[0] >= 35){
			return 1;
		}else if(levels[4]>=18){
			return 4;
		}else if(levels[3]>=18){
			return 3;
		}else if(levels[2]>=18){
			return 2;
		}else if(levels[1]>=18){
			return 1;
		}
		return 0;
	}
	
	
	protected float getPoint(int index,int playerLevel){
		GameItem equ = equs[index];
		if(equ==null)
			return 0.0f;
		return equ.template.equipment.value;
	}
	


	
	protected int getImageId(int point,int type){
		if(type==0){
			if(point==0)
				return 0;
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
//			0-3186	3187-7646	7647-11470	11471-17812	17813-23895
			if(point>=1&&point<=3186){
				return 2;
			}
			else if(point>=3187&&point<=7646){
				return 3;
			}
			else if(point>=7647&&point<=11470){
				return 4;
			}
			else if(point>=11471&&point<=17812){
				return 5;
			}
			else if(point>=17813&&point<=23895){
				return 5;
			}
		}
		
		else if(type==1){
			if(point==0)
				return 0;
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
//			0-6318	6319-15163	15164-22745	22746-35986	35987-48195
			if(point>=1&&point<=6318){
				return 2;
			}
			else if(point>=6319&&point<=15163){
				return 3;
			}
			else if(point>=15164&&point<=22745){
				return 4;
			}
			else if(point>=22746&&point<=35986){
				return 5;
			}
			else if(point>=35987&&point<=48195){
				return 5;
			}
		}
//		0-2700	2701-6480	6481-9720	9721-15120	15121-20250
		else if(type==2){
			if(point==0)
				return 0;
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
			if(point>=1&&point<=2700){
				return 2;
			}
			else if(point>=2701&&point<=6480){
				return 3;
			}
			else if(point>=6481&&point<=9720){
				return 4;
			}
			else if(point>=9721&&point<=15120){
				return 5;
			}
			else if(point>=15121&&point<=20250){
				return 5;
			}
		}
//		if(point==0)
//			return 0;
//		if(point<=120)
//			return 1;
//		else if(point>120&&point<=140)
//			return 2;
//		else if(point>141&&point<=150)
//			return 3;
//		else if(point>151&&point<=170)
//			return 4;
//		else 
			return 5;
	}
	
	//根据百分比扣除所有防具的耐久
	public void decAllArmorDuration(int value, int percent) {
		for (int i = 0; i < ARMOR_INDEXES.length; i++) {
			GameItem item = equs[ARMOR_INDEXES[i]];
			if (item != null && item.duration > 0) {
				if (CommonUtil.hit(RND, percent, 100)) {
					decDuration(item,value);
				}
			}
		}
	}
	
	//按照剩余耐久的百分比扣除所有防具的耐久
	public void decAllArmorRemainDuration(int percent) {
		for (int i = 0; i < ARMOR_INDEXES.length; i++) {
			GameItem item = equs[ARMOR_INDEXES[i]];
			if (item != null && item.duration > 0) {
				int value = item.template.equipment.duration * percent /100;
				decDuration(item,value);
			}
		}
	}
	
	//按照percent的百分几率扣除武器的耐久值
	public void decWeaponDuration(int value,int percent){
		GameItem item = equs[HAND];
		if(item!=null&&item.duration>0){
			if (CommonUtil.hit(RND, percent, 100)) {
				decDuration(item,value);
			}
		}
	}
	
	protected void decDuration(GameItem item,int value){
		item.duration = Math.max(0, item.duration - value);
		if(item.duration==0){
			owner.refreshProperties(false);
		}
		DurationChangedItem changedItem = new DurationChangedItem(item);
		owner.changed.addChangedItem(changedItem);
	}
	
	public void repair() {
		boolean needRefresh = false;
		for (int i = 0; i < equs.length; i++) {
			GameItem item = equs[i];
			if (item != null) {
			    if (item.duration == 0) {
			        needRefresh = true;
			    }
			    item.repair(owner);
			}
		}
		if (needRefresh) {
			owner.refreshProperties(false);
		}
	}
}
