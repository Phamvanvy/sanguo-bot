package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;


/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class EquipmentTemplate implements IItemTemplate{

    protected short level;
    protected short requiredLevel;
    protected byte createType;
    protected byte quality;
    protected byte part;
    protected short durability;
    protected byte bindType;
    protected int price;
    protected String name;
    protected int itemId;
    protected String desc;
    protected Map properties = new HashMap();
    protected int credit;
    protected boolean canEnhance;
    /**
     * 是否能分解
     */
    protected boolean canSplit;
    /**
     * 属性攻三个属性
     */
    protected byte vianyStoneValue;
    protected byte vianyScissorsValue;
    protected byte vianyPaperValue;
    
    //mengjie add 失效时间
    protected long FAILURE_TIME = 0;
    
    private byte itemType;
    
    public byte getItemSplitType() {
		return itemType;
	}

	public void setItemType(byte itemType) {
		this.itemType = itemType;
	}
	
	/**
	 * 鉴定数量
	 */
	private byte diamond;
	
	public byte getDiamond() {
		return diamond;
	}

	public void setDiamond(byte diamond) {
		this.diamond = diamond;
	}
	
	/**
	 * 是否开启第6孔
	 */
	private byte open6hole = 0;
	public byte getOpen6hole(){
		return open6hole;
	}
	public void setOpen6hole(byte open6hole){
		this.open6hole = open6hole;
	}

	/**
	 * 宝石数量
	 */
	private byte diamondCount;
	
	
    public byte getDiamondcount() {
		return diamondCount;
	}

	public void setDiamondcount(byte diamondcount) {
		this.diamondCount = diamondcount;
	}
	
	/**
	 * 当前模板开放的默认打孔数量
	 */
	private byte openDiamondCount;
	
	public byte getOpenDiamondCount() {
		return openDiamondCount;
	}

	public void setOpenDiamondCount(byte openDiamondCount) {
		this.openDiamondCount = openDiamondCount;
	}

	//2013年3月29日增加 
	/**
	 * 孔位信息 
	 */
	private byte[] DiamondMosiacRoleInfo;
	public byte[] getDiamondMosiacRoleInfo() {
		return DiamondMosiacRoleInfo;
	}  //获得孔位信息
	public void setDiamondMosiacRoleInfo(byte[] diamondMosiacRoleInfo) {
		this.DiamondMosiacRoleInfo = diamondMosiacRoleInfo;
	}
	
	/**
	 * @param diamondMosaic
	 *  镶嵌
	 */
	private DiamondMosaic[] DiamondMosaic;
	public void setDiamondMosaic(DiamondMosaic[] diamondMosaic) {
		this.DiamondMosaic= diamondMosaic;
	}
	public DiamondMosaic[] getdiamondMosaic() {
		return DiamondMosaic;
	}
	/**
	 * @param enhance
	 *  精炼信息
	 */
	private List<Enhance> enhances = new ArrayList<Enhance>(9);
	public void enhance(Enhance enhance){
        if(enhance==null)
            throw new IllegalArgumentException("enhance can not be null");
        if(enhances.size()>=9)
            throw new IllegalStateException("enhances can not >9");
        enhances.add(enhance);
    }
	public List<Enhance> getEnhance() {
		return enhances;
	}
	
	public EquipmentTemplate() {
    }

    public boolean canEnhance(){
        return canEnhance;
    }

    public void setCanEnhance(boolean canEnhance){
        this.canEnhance = canEnhance;
    }
    
    public boolean canSplit(){
    	return canSplit;
    }
    
    public void setCanSplit(boolean canSplit){
    	this.canSplit = canSplit;
    }
    
    public byte getVianyStoneValue(){
    	return vianyStoneValue;
    }
    
    public void setVianyStoneValue(byte vianyStoneValue){
    	this.vianyStoneValue = vianyStoneValue;
    }
    
    public byte getVianyScissorsValue(){
    	return vianyScissorsValue;
    }
    
    public void setVianyScissorsValue(byte vianyScissorsValue){
    	this.vianyScissorsValue = vianyScissorsValue;
    }
    
    public byte getVianyPaperValue(){
    	return vianyPaperValue;
    }
    
    public void setVianyPaperValue(byte vianyPaperValue){
    	this.vianyPaperValue = vianyPaperValue;
    }

    public short getRequiredLevel() {
        return requiredLevel;
    }

    public byte getQuality() {
        return quality;
    }

    public byte getType(){
        return IItem.TYPE_EQU;
    }

    public int getPrice() {
        return price;
    }

    public byte getPart() {
        return part;
    }

    public String getName() {
        return name;
    }

    public short getLevel() {
        return level;
    }

    public int getItemId() {
        return itemId;
    }

    public short getDurability() {
        return durability;
    }

    public String getDesc() {
        StringBuffer buff = new StringBuffer(200);
        buff.append(getName());
        buff.append(' ');
        buff.append(IEquipment.EQUIP_TYPE_NAME[getPart()]);
        buff.append('\n');
        if (getPart() == IEquipment.PART_WEAPON) {
            buff.append(IEquipment.WEAPON_TYPE_NAME[getProperty(IEquipment.
                    EQUIP_ADD_WEAPON_TYPE)]);
            buff.append(' ');
            buff.append("攻击：");
            buff.append(getProperty(IEquipment.EQUIP_ADD_ATTACK_MIN));
            buff.append('-');
            buff.append(getProperty(IEquipment.EQUIP_ADD_ATTACK_MAX));
            buff.append('\n');
        } else {
            buff.append("防御：");
            buff.append(getProperty(IEquipment.EQUIP_ADD_DEFENCE));
            buff.append('\n');
        }
        int[][] pros = getProperties(level);

        boolean flag = false;

        for (int i = 0; i < pros.length; i++) {
            if (pros[i][0] <= IEquipment.EQUIP_ADD_MCRI) {
                buff.append(IEquipment.EQUIP_PROPERTIES_NAME[pros[i][0] - 1]);
                buff.append('：');
                buff.append(pros[i][1]);
                buff.append(' ');

                flag = true;
            }
        }

        if(flag){
            buff.append('\n');
        }

        buff.append("需要等级：");
        buff.append(getRequiredLevel());
        buff.append('\n');
        buff.append("售价：");
        buff.append(getPrice());
        return buff.toString();

    }

    public byte getCreateType() {
        return createType;
    }

    public void setBindType(byte bindType) {
        this.bindType = bindType;
    }

    public void setRequiredLevel(short requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public void setQuality(byte quality) {
        this.quality = quality;
    }


    public void setPrice(int price) {
        this.price = price;
    }

    public void setPart(byte part) {
        this.part = part;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setDurability(short durability) {
        this.durability = durability;
    }

    public void setDesc(String desc) {
//        this.desc = desc;
    }

    public void setCreateType(byte createType) {
        this.createType = createType;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public byte getBindType() {
        return bindType;
    }

    public int getCredit() {
        return credit;
    }

    public String getCreditName(){
        if(credit < 0 || credit >= PlayerData.CREDIT_NAME.length){
            return "";
        }else{
            return PlayerData.CREDIT_NAME[credit];
        }
    }

    public void addProperty(int index, int value, int growvalue) {
    	EquProperty equProperty = new EquProperty(index, value, growvalue);
        properties.put(new Integer(index), equProperty);
    }

    public int getProperty(int index) {
    	EquProperty ret = (EquProperty) properties.get(new Integer(index));
        if (ret == null) {
            return 0;
        }
        return ret.value;
    }
    
    public int getProperty(int index, boolean isGrowValue) {
    	if(!isGrowValue){
    		return getProperty(index);
    	}
    	EquProperty ret = (EquProperty) properties.get(new Integer(index));
        if (ret == null) {
            return 0;
        }
        return ret.growvalue;
    }

    public int[][] getProperties(int level){
        int[][] ret = new int[properties.size()][2];
        Set entrys = properties.entrySet();
        Iterator ite = entrys.iterator();
        int i=0;
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            EquProperty pro = (EquProperty)entry.getValue();
            ret[i][0] = pro.type;
            ret[i][1] = pro.value + level * pro.growvalue / 100;
            i++;
        }
        return ret;
    }

    public int hashCode(){
        return itemId;
    }


    public abstract IEquipment newInstance(int id,int seed);
    
    public boolean isGrow(){
    	Set entrys = properties.entrySet();
        Iterator ite = entrys.iterator();
        int i=0;
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            EquProperty pro = (EquProperty)entry.getValue();
            if(pro.growvalue != 0){
            	return true;
            }
        }
        return false;
    }
    
}
