package com.pip.itimes.server.stage;

import java.util.List;

/**
 * @author Jeffery
 * @version 1.0
 */
public interface IEquipment extends IValuableItem{
	/**
	 * 命中率，和敏捷率, 魔暴，物理的系数  1%换算为等级10
	 */
	public final static byte CONVERTNUM = 10; 
	
	
	public static final int DIAMOND_NOPROPERTY = 1000000;
	/**
	 * 因为打孔无法镶嵌
	 */
	public static final byte CURRENT_EQU_DIAMOND_CANOTEMEDED = -1;
	
	
	/**
	 * 没有打孔
	 */
	public static final byte CURRENT_EQU_DIAMOND_NOTROLE = 0;
	/**
	 * 打孔了，没有镶嵌宝石
	 */
	public static final byte CURRENT_EQU_CANDIAMOND = 1;
	
	
	/**
	 * 已经镶嵌了同类宝石
	 */
	public static final byte CURRENT_EQU_DIAMOND_PROPERTY = 2;
	/**
	 * 已经镶嵌了
	 */
	public static final byte CURRENT_EQU_DIAMOND = 3;
	
	
	public static final byte CURRENT_EQU_VERSION = 9;
	
    /**
     * 体力
     */
    public static final byte EQUIP_ADD_VIT = 1;
    /**
     * 智力
     */
    public static final byte EQUIP_ADD_INT = 2;
    /**
     * 力量
     */
    public static final byte EQUIP_ADD_STR = 3;
    /**
     * 敏捷
     */
    public static final byte EQUIP_ADD_AGI = 4;
    public static final byte EQUIP_ADD_PATTACK = 5;
    public static final byte EQUIP_ADD_MATTACK = 6;
    public static final byte EQUIP_ADD_PDEFENCE = 7;
    public static final byte EQUIP_ADD_MDEFENCE = 8;
    public static final byte EQUIP_ADD_HIT = 9;
    public static final byte EQUIP_ADD_FLEE = 10;
    public static final byte EQUIP_ADD_PCRI = 11;
    public static final byte EQUIP_ADD_MCRI = 12;
    public static final byte EQUIP_ADD_DEFENCE = 20;
    public static final byte EQUIP_ADD_ATTACK_MAX = 21;
    public static final byte EQUIP_ADD_ATTACK_MIN = 22;
    public static final byte EQUIP_ADD_WEAPON_TYPE = 30;
    //mengjie add
    public static final byte EQUIP_FAILURE_DATE = 23;
    public static final byte EQUIP_FAILURE_TIME = 24;
    
    //jwp add
    public static final byte EQUIP_ADD_HPMAX = 15;
    public static final byte EQUIP_ADD_MPMAX = 16;
    
    public static final byte EQUIP_ADD_NOCRI= 17;
    
    public static final byte PART_HEAD = 0;
    public static final byte PART_NECK = 1;
    public static final byte PART_CHEST = 2;
    public static final byte PART_WAIST = 3;
    public static final byte PART_WRIST = 4;
    public static final byte PART_FINGER = 5;
    public static final byte PART_FEET = 6;
    public static final byte PART_WEAPON = 7;
    public static final byte PART_SHIELD = 8;

    public static final short WEAPON_SWORD = 0;
    public static final short WEAPON_AXE = 1;
    public static final short WEAPON_SPEAR = 2;
    public static final short WEAPON_STAFF = 3;

    public static final String[] EQUIP_TYPE_NAME = {
                    "头盔", "项链", "盔甲", "腰带", "护腕", "戒指", "鞋", "武器", "盾牌"/*, "宠物"*/
    };

    public static final String[] WEAPON_TYPE_NAME = {
            "剑", "斧", "枪", "法杖"
    };

    public static final String[] EQUIP_PROPERTIES_NAME = new String[] {
            "体力", "智力", "力量", "敏捷", "物攻", "魔攻", "物防", "魔防", "命中等级", "闪躲等级", "物暴等级",
            "魔暴等级","", "", "HP上限", "MP上限", "免爆"
    };

    public static final String[] STARS = new String[]{"0星","1星","2星","3星","4星","5星","6星","7星","8星","9星"};


    public static final byte CREATE_NORMAL = 1;
    public static final byte CREATE_DYNAMIC = 2;
    
    public short getLevel();
    public short getRequiredLevel();
    public byte getCreateType();
    public byte getPart();
    public short getDurability();
    public byte getBindType();
    public boolean canEnhance();
    public int getProperty(int index, int level);
    public int getDiamondProperty(int index, int level);
    public int getDiamondMosiacProperty(int index);
    public short getCurrentDurability();
    public void setCurrentDurability(short durability);
    public int getTimes();
    public int[][] getProperties(int level);
    public boolean isValid();
    public boolean isWeapon();
    public boolean isArmor();
    public int getCredit();
    public List<Enhance> getEnhances();
    public void enhance(Enhance enhance);
    public void unEnhance();
    public void unEnhanceAll();
    public boolean getLastEnhanceStatus();
    public void setLastEnhanceStatus(boolean enhanceStatus);
    public int getEnhanceStatusTimes();
    public void setEnhanceStatusTimes(int times);
    public void setId(int Id);
    public long getFAILURE_TIME();
    public void setFAILURE_TIME(long days);
    public void insteadEnhance(Enhance enhance, int insteadIndex);
    
    public byte getDiamond();
    public void setDiamond(byte diamond);
    
    public void setDataVersion(int dataVersion);
    public int getDataVesion();
    
    //是否刻字
    public void setExtendFlag(int LetteringFlag);
    public int getExtendFlag();
    
    public void setLetteringString(String letteringString);
    public String getLetteringString();
    
    /**
     * @return宝石鉴定数量
     */
    public byte getDiamondcount();
	public void setDiamondcount(byte diamondcount);
	
	
	
	/**
	 * @return 宝石默认的孔开放数量
	 */
	public byte getOpenDiamondCount();
	public void setOpenDiamondCount(byte opendDiamondCount);
	
	//装备孔位信息
	public byte[] getDiamondMosiacRoleInfo();  //获得孔位信息
	public byte checkDiamondMosiacRoleAcount(byte diamondLevel);	//检测装备宝石个数
	public void setDiamondMosiacRoleInfo(byte[] diamondMosiacRoleInfo); //设置孔位信息
	/**
	 * @param newDiamondcount 
	 * 重置最大的开放孔位
	 */
	public void resetDiamondMosiacRoleInfo(byte newDiamondcount); 
	/**
	 * @param diamondRoleInfo
	 * @return获得已经打空的数量
	 */
	public byte getDiamonRoleSuccessCount(byte[] diamondRoleInfo); 
	
	/**
	 * @param diamondrole
	 * @return该部位,属性是否可镶嵌
	 */
	public byte canDiamondMosiacEmbed(byte diamondrole, byte property);
	
	
	/**
	 * @param diamondrole
	 * @return该部位,是否镶嵌同属性宝石
	 */
	public byte isSameProDiamondMosaic(byte diamondrole, byte property);
	
	
	/**
	 * @param diamondMosaic
	 *  镶嵌
	 */
	public void diamondMosaic(byte diamondRole, DiamondMosaic diamondMosaic);
	
	/**
	 * @param diamondRole
	 * 摘除
	 */
	public void cancerDiamondMosaic(byte diamondRole);
	
	/**
	 * @param role
	 * @return获取该孔位上的宝石
	 */
	public DiamondMosaic getDiamondMosaicRole(byte role);
	
	/**
	 * @return 孔位宝石个数
	 */
	public int getDiamondMosaicRoleSize();
	
	/**
	 * 获得附魔类
	 * @return
	 */
	public Enchanting getEnchanting();
	
	/**
	 * 设置附魔类
	 * @param type
	 * @param enchanting
	 */
	public void setEnchanting(byte type, Enchanting enchanting);
	
	/**
	 * 是否可以附魔或是分解
	 * @param type
	 */
	public void canEnchanting(byte type);
	
	public void setCanSplit(boolean canSplit);
	
	public boolean canSplit();
	
	public Viany getViany();
	
	public boolean isGrow();
	
	public short[] getDevelopAddPoint();
	
	public void setDevelopAddPoint(short addpoint[]);
	
	public short[] getDevelopAddCount();
	
	public void setDevelopAddCount(short addcount[]);
}
