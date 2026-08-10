package com.pip.itimes.server.stage;

import java.io.DataOutputStream;

public class Enchanting {
	
	public static final byte ARRT_MIN[][] = {
//		{2, 2, 2, 2, 9},
//		{6, 6, 6, 6, 14},
//		{6, 6, 6, 6, 14},
		// 2011年3月10日附魔v1.3
		{2, 2, 2, 2, 9},
		{13, 13, 13, 13, 19},
		{23, 23, 23, 23, 39},
	};
	public static final byte ARRT_MAX[][] = {
//		{5, 5, 5, 5, 11},
//		{8, 8, 8, 8, 16},
//		{8, 8, 8, 8, 16},
		// 2011年3月10日附魔v1.3
		{5, 5, 5, 5, 11},
		{15, 15, 15, 15, 21},
		{25, 25, 25, 25, 41},
	};
	
	public static final byte ARRT_TYPE[] = {
		IEquipment.EQUIP_ADD_VIT, 
		IEquipment.EQUIP_ADD_INT, 
		IEquipment.EQUIP_ADD_STR, 
		IEquipment.EQUIP_ADD_AGI,
		IEquipment.EQUIP_ADD_HIT
		};
	
	public static final byte Diamond_Property[] = {
//		10, 11, 8, 9, 20
		1, 2, 3, 4, 9
	};
	
	private int enchantingItemId = 0;	//附魔用的卷轴ID
	private int enchantingItemIdTemp = 0;
	//属性
	private byte arrtType = 0;			//增加的属性类型
	private byte arrtValue = 0;			//属性类型的值
	private byte arrtTypeTemp = 0;		//临时存储的属性类型
	private byte arrtValueTemp = 0;		//临时存储的属性值
	//宝石
	private byte stoneType = 0;			//石头属性类型
	private byte stoneValue = 0;		//石头属性值
	private byte stoneTypeTemp = 0;
	private byte stoneValueTemp = 0;
	
	/**
	 * 查询是否拥有指定的镶钳宝石的类型
	 * @param equ
	 * @param stoneType
	 * @return -1为没有相同类型的 其它为相同类型的宝石等级
	 */
	public static int hasStoneType(IEquipment equ, byte stoneType){
		byte[] dmri = equ.getDiamondMosiacRoleInfo();
		for(byte i=0; i<dmri.length; i++){
			if(dmri[i] >= 2){
				DiamondMosaic dm = equ.getDiamondMosaicRole(i);
				//获得镶嵌宝石增加的类型的序号 对应于附魔的类型
				int dIndex = Enchanting.getPropertyIndex(dm.getProperty());
				//跟获得的宝石额外属性一样
				if(dIndex >= 0 && stoneType == Enchanting.Diamond_Property[dIndex]){
					return dm.getDiamondLevel();
				}
			}
		}
		return -1;
	}
	
	/**
	 * 计算宝石获得的额外增加的值
	 * @param stoneType
	 * @param stoneLevel
	 * @return
	 */
	public static int calcStoneValue(byte stoneType, byte stoneLevel){
		if(stoneLevel <= 0) return 0;
		//命中等级宝石等级平方
		if(stoneType == IEquipment.EQUIP_ADD_HIT){
			return stoneLevel * stoneLevel << 1;
		}
//		//增加类型宝石等级平方/2取整
//		return stoneLevel * stoneLevel / 2;
		// 2011年3月10日附魔v1.3
		return stoneLevel * stoneLevel;
	}
	
	/**
	 * 重置宝石带来的效果
	 * @param equ
	 */
	public void resetStone(IEquipment equ){
		int stoneLevel = Enchanting.hasStoneType(equ, stoneType);
		setStoneValue((byte)Enchanting.calcStoneValue(stoneType, (byte)stoneLevel));
	}
	
	public static int getPropertyIndex(int property){
		for(int i=0; i<Diamond_Property.length; i++){
			if(Diamond_Property[i] == property){
				return i;
			}
		}
		return -1;
	}
	
	public int getProperty(int pro){
		int value = 0;
		if(arrtType == pro){
			value += arrtValue;
		}
		if(stoneType == pro){
			value += stoneValue;
		}
		return value;
	}
	
	/**
	 * 是否拥有增加的属性
	 * @return
	 */
	public boolean hasArrt(){
		return arrtType != 0;
	}
	/**
	 * 是否拥有宝石属性
	 * @return
	 */
	public boolean hasStone(){
		return stoneType != 0;
	}
	
	public boolean hasArrtTemp(){
		return arrtTypeTemp != 0;
	}
	public boolean hasStoneTemp(){
		return stoneTypeTemp != 0;
	}
	/**
	 * 清除掉临时的属性
	 */
	public void clearTemp(){
		setArrtTypeTemp((byte)0);
		setArrtValueTemp((byte)0);
		setStoneTypeTemp((byte)0);
		setStoneValueTemp((byte)0);
		setEnchantingItemIdTemp(0);
	}
	
	/**
	 * 将临时的属性替换掉原先的属性
	 */
	public void replace(){
		setArrtType(getArrtTypeTemp());
		setArrtValue(getArrtValueTemp());
		setStoneType(getStoneTypeTemp());
		setStoneValue(getStoneValueTemp());
		setEnchantingItemId(getEnchantingItemIdTemp());
		//替换之后直接进行清除
		clearTemp();
	}
	
	public void setArrtType(byte arrtType){
		this.arrtType = arrtType;
	}
	public byte getArrtType(){
		return arrtType;
	}
	public void setArrtValue(byte arrtValue){
		this.arrtValue = arrtValue;
	}
	public byte getArrtValue(){
		return arrtValue;
	}
	
	public void setArrtTypeTemp(byte arrtTypeTemp){
		this.arrtTypeTemp = arrtTypeTemp;
	}
	public byte getArrtTypeTemp(){
		return arrtTypeTemp;
	}
	public void setArrtValueTemp(byte arrtValueTemp){
		this.arrtValueTemp = arrtValueTemp;
	}
	public byte getArrtValueTemp(){
		return arrtValueTemp;
	}
	public void setStoneType(byte stoneType){
		this.stoneType = stoneType;
	}
	public byte getStoneType(){
		return stoneType;
	}
	public void setStoneValue(byte stoneValue){
		this.stoneValue = stoneValue;
	}
	public byte getStoneValue(){
		return stoneValue;
	}
	public void setStoneTypeTemp(byte stoneTypeTemp){
		this.stoneTypeTemp = stoneTypeTemp;
	}
	public byte getStoneTypeTemp(){
		return stoneTypeTemp;
	}
	public void setStoneValueTemp(byte stoneValueTemp){
		this.stoneValueTemp = stoneValueTemp;
	}
	public byte getStoneValueTemp(){
		return stoneValueTemp;
	}
	
	public void setEnchantingItemId(int enchantingItemId){
		this.enchantingItemId = enchantingItemId;
	}
	public int getEnchantingItemId(){
		return enchantingItemId;
	}
	public void setEnchantingItemIdTemp(int enchantingItemIdTemp){
		this.enchantingItemIdTemp = enchantingItemIdTemp;
	}
	public int getEnchantingItemIdTemp(){
		return enchantingItemIdTemp;
	}
}
