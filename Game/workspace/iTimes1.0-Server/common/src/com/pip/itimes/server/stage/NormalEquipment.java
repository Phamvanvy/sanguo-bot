package com.pip.itimes.server.stage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;

import com.pip.itimes.server.suit.Suits;
import com.pip.itimes.server.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import org.apache.commons.collections.iterators.EntrySetMapIterator;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class NormalEquipment implements IEquipment {
	
	
	/**
	 * 宝石数量
	 */
	private byte diamondcount;
	
	
    public byte getDiamondcount() {
		return diamondcount;
	}

	public void setDiamondcount(byte diamondcount) {
		this.diamondcount = diamondcount;
	}
	
	
	public static final int CAN_LETTERING = 0; //可以进行刻字
	public static final int LETTERINGED = 1;  //已经刻字了
	
	
    private NormalEquipmentTemplate template = null;

    private short currentDurability = 0;
    private int id = 0;
    private boolean isBinded = false;
    private Map properties0 = new HashMap();
//    private int times = 0;
    private List<Enhance> enhances = new ArrayList<Enhance>(9);
    private boolean lastEnhanceStatus;
    private int enhanceStatusTimes;
    //mengjie add 失效时间
    public long FAILURE_TIME = 0;
    
    
    /**
     * 宝石所加的属性
     */
    private Map<Byte, Short> diamondPropertyMap = new HashMap<Byte, Short>();
    
    /**
     * 每次宝石镶嵌的  key为镶嵌的孔位    值为镶嵌的对象（主要是孔位, 物品id）
     */
    private Map<Byte, DiamondMosaic> dimondMosaicMap = new HashMap<Byte, DiamondMosaic>();
    
    
    /**
     * 最高的孔数量
     */
    public final static byte MaxDiamondRoleCount = 6;
    /**
     * 当前的孔位信息 0为没有打孔     1为已经打孔了没有镶嵌   2为已经打孔了也镶嵌了
     */
    private byte[] diamondMosiacRoleInfo = new byte[MaxDiamondRoleCount];
    
	/**
	 * 装备上每个孔位的宝石养成增加点数
	 */
	private short developAddPoint[];
	
	
	/**
	 * 装备上每个孔位的宝石养成增加宝石数
	 */
	private short developAddCount[];
    
    /**
     * 当前的附魔属性
     */
    Enchanting enchanting = new Enchanting();
    
    private Viany viany = new Viany();
    
    public byte[] getDiamondMosiacRoleInfo() {
		return diamondMosiacRoleInfo;
	}
    
    public byte checkDiamondMosiacRoleAcount(byte diamondLevel){
    	byte acount = 0;
    	for(int i = 0;i < diamondMosiacRoleInfo.length;i++){
    		if(diamondMosiacRoleInfo[i] >= diamondLevel){
    			acount++;
    		}
    	}
    	return acount;
    }

	public void setDiamondMosiacRoleInfo(byte[] diamondMosiacRoleInfo) {
		this.diamondMosiacRoleInfo = diamondMosiacRoleInfo;
	}


	private static SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH点");
    public NormalEquipment(NormalEquipmentTemplate template,int id) {
        this.template = template;
        this.id = id;
    }

    public boolean getLastEnhanceStatus(){
        return lastEnhanceStatus;
    }

    public void setLastEnhanceStatus(boolean enhanceStatus) {
        this.lastEnhanceStatus = enhanceStatus;
    }

    public int getEnhanceStatusTimes() {
        return enhanceStatusTimes;
    }

    public void setEnhanceStatusTimes(int times) {
        this.enhanceStatusTimes = times;
    }

    public boolean canEnhance(){
        return template.canEnhance();
    }
    
    public boolean canSplit(){
    	return template.canSplit();
    }

    public void enhance(Enhance enhance){
        if(enhance==null)
            throw new IllegalArgumentException("enhance can not be null");
        if(enhances.size()>=9)
            throw new IllegalStateException("enhances can not >9");
        enhances.add(enhance);
        addProperty(enhance.getProperty(),(short)enhance.getPoint(enhances.size()));
    }

    public void unEnhance(){
        if(enhances.size()==0)
            throw new IllegalStateException("equipment hasn't enhanced");
        Enhance enhance = (Enhance)enhances.remove(enhances.size()-1);
        decProperty(enhance.getProperty(),(short)enhance.getPoint(enhances.size()+1));
    }

    public void unEnhanceAll() {
        if (enhances.size() != 0){
        	int enhancessize = enhances.size();
            for (int i = 0 ; i < enhancessize ;i++){
            	unEnhance();
            }
        }
    }
    
    public short getLevel() {
        return template.getLevel();
    }


    public short getRequiredLevel() {
        return template.getRequiredLevel();
    }

    public byte getCreateType() {
        return template.getCreateType();
    }

    public byte getPart() {
        return template.getPart();
    }


    public short getDurability() {
        return template.getDurability();
    }


    public byte getBindType() {
        return template.getBindType();
    }


    public int getProperty(int index, int level) {
    	if(level == -1){
    		level = template.level;
    	}
    	int originalProperty = template.getProperty(index) + level * template.getProperty(index, true) / 100;
    	int refineProperty = getProperty0(index);
    	int starProperty = getDiamondProperty(index, level);
    	int diamondMosaicPropeyty = getDiamondMosiacProperty(index);
    	return originalProperty + refineProperty + starProperty + diamondMosaicPropeyty;
    }
    
    public int getPropertyTemplate(int index) {
    	return template.getProperty(index);
    }
    

    public int getPrice() {
        return template.getPrice();
    }


    public int getItemId() {
        return template.getItemId();
    }


    public int getId() {
        return id;
    }

    public String getName() {
        if(getTimes()>0)
            return IEquipment.STARS[getTimes()]+template.getName();
        return template.getName();
    }

    public byte getType() {
        return 3;
    }

    public boolean isBinded() {
        return isBinded;
    }


    public void setBinded(boolean binded) {
        isBinded = binded;
    }


    public byte getQuality(){
        if(template.getQuality()==5)
            return template.getQuality();
        int c = 0;
        if(getTimes()>=5)
            c = 1;
        if(getTimes()>=9)
            c = 2;
        return (byte)Math.min(4,template.getQuality()+c);
    }
    
    /**
     * @param index
     * @return鉴定加成属性
     */
    public int getDiamondProperty(int index, int level){
    	int diamondProperty = 0;
    	int originalProperty = template.getProperty(index) + level * template.getProperty(index, true) / 100;
    	switch(index){
			case EQUIP_ADD_VIT:
			case EQUIP_ADD_INT:
			case EQUIP_ADD_STR:
			case EQUIP_ADD_AGI:
			case EQUIP_ADD_PDEFENCE:
			case EQUIP_ADD_MDEFENCE:
			case EQUIP_ADD_DEFENCE:
				diamondProperty = originalProperty * Diamonds.getDiamondPropertyRate(getDiamond()) / 100;
				break;
			case EQUIP_ADD_ATTACK_MIN:
			case EQUIP_ADD_ATTACK_MAX:
			case EQUIP_ADD_PATTACK:
			case EQUIP_ADD_MATTACK:
				diamondProperty = originalProperty * Diamonds.getDiamondRate(getDiamond()) / 100;
				break;
    	}
	
    	return diamondProperty;
    }
    
    
    public String getDesc() {
    	
    	StringBuffer buff = new StringBuffer(200);
        buff.append(getName());
        buff.append('\n');
        buff.append(IEquipment.EQUIP_TYPE_NAME[getPart()]);
        buff.append('\n');
        if (getPart() == IEquipment.PART_WEAPON) {
            buff.append(IEquipment.WEAPON_TYPE_NAME[getProperty(IEquipment.
                    EQUIP_ADD_WEAPON_TYPE, template.level)]);
            buff.append(' ');
            buff.append("攻击：");
            buff.append(getProperty(IEquipment.EQUIP_ADD_ATTACK_MIN, template.level) - getDiamondProperty(IEquipment.EQUIP_ADD_ATTACK_MIN, template.level));
            buff.append('-');
            buff.append(getProperty(IEquipment.EQUIP_ADD_ATTACK_MAX, template.level) - getDiamondProperty(IEquipment.EQUIP_ADD_ATTACK_MAX, template.level));
            if(getProperty(IEquipment.EQUIP_ADD_ATTACK_MIN, template.level) > 0){
            	buff.append("( + ");
                buff.append(getDiamondProperty(IEquipment.EQUIP_ADD_ATTACK_MIN, template.level));
                buff.append(")");
        	}
        } else {
            buff.append("防御：");
            buff.append(getProperty(IEquipment.EQUIP_ADD_DEFENCE, template.level) - getDiamondProperty(IEquipment.EQUIP_ADD_DEFENCE, template.level));
            if(getDiamondProperty(IEquipment.EQUIP_ADD_DEFENCE, template.level) > 0){
	            buff.append("( + ");
	            buff.append(getDiamondProperty(IEquipment.EQUIP_ADD_DEFENCE, template.level));
	            buff.append(")");
            }
        }
        int[][] pros = getProperties(template.level);

       // boolean flag = false;

        for (int i = 0; i < pros.length; i++) {
            if (pros[i][0] <= IEquipment.EQUIP_ADD_MCRI) {
            	if((pros[i][1] -  pros[i][2]) > 0 || getDiamondProperty(pros[i][0], template.level) > 0){
            		buff.append('\n');
                	buff.append(IEquipment.EQUIP_PROPERTIES_NAME[pros[i][0] - 1]);
                    buff.append('：');
                    buff.append(pros[i][1] -  pros[i][2]);
                    buff.append(' ');
                    if(pros[i][0] <  IEquipment.EQUIP_ADD_PATTACK){
	                	if(getDiamondProperty(pros[i][0], template.level) > 0){
		                	buff.append("( + ");
		                    buff.append(getDiamondProperty(pros[i][0], template.level));
		                    buff.append(")");
	                	}
                    }
                   // flag = true;
            	}
            	
              
            }
        }
        
        //精炼
        for (int i = 0; i < pros.length; i++) {
            if (pros[i][0] <= IEquipment.EQUIP_ADD_MCRI) {
            	if(pros[i][2] > 0){
	            	buff.append('\n');
	            	buff.append("精炼 ");
	            	buff.append(IEquipment.EQUIP_PROPERTIES_NAME[pros[i][0] - 1]);
	                buff.append(pros[i][2]);
            	}
            }
        }
        
//        for(int i = 0; i < pros.length; i++){
//        	 if (pros[i][0] <= IEquipment.EQUIP_ADD_NOCRI) {
//             	if(pros[i][3] > 0){
// 	            	buff.append('\n');
// 	            	buff.append("宝石 ");
// 	            	buff.append(IEquipment.EQUIP_PROPERTIES_NAME[pros[i][0] - 1]);
// 	                buff.append(pros[i][3]);
//             	}
//             }
//        }
        // 装备镶嵌宝石详情
		for (byte i = 0; i < Utils.maxHolesEqu; i ++) {
			DiamondMosaic diamondMosaic = getDiamondMosaicRole(i);
			if (diamondMosaic != null) {
				buff.append('\n');
				buff.append(Items.getTemplate(diamondMosaic.getItemId()).getName() + "  ");
				String tmpDesc = Items.getTemplate(diamondMosaic.getItemId()).getDesc();
				int idx = tmpDesc.indexOf(" ", 0);
				buff.append(tmpDesc.substring(0, idx));
			}
		}
		
        //if(flag){
       buff.append('\n');
        //}

       buff.append("需要等级：");
       buff.append(getRequiredLevel());
       buff.append('\n');
       buff.append("售价：");
       buff.append(getPrice());
       return buff.toString();
    }

    public void setCurrentDurability(short durability){
        this.currentDurability = durability;
    }

    public short getCurrentDurability(){
        return currentDurability;
    }


    public int getTimes(){
        return enhances.size();
    }

    public long getFAILURE_TIME() {
		return FAILURE_TIME;
	}

	public void setFAILURE_TIME(long failure_time) {
		FAILURE_TIME = failure_time;
	}

	public int[][] getProperties(int level){
        int[][] p = template.getProperties(level);
        int[][] e = getProperties0();
        HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
        for(int i=0;i<p.length;i++){
            map.put(p[i][0],p[i][1]);
        }
        for(int i=0;i<e.length;i++){
            int value = e[i][1];
            Integer pp = map.get(e[i][0]);
            if(pp!=null){
                value += pp.intValue();
            }
            map.put(e[i][0],value);
        }
        
    	
        int count = 0;
        for(Map.Entry<Byte, Short> diamondtemp: diamondPropertyMap.entrySet()){
    		int property = diamondtemp.getKey();
    		int value = diamondtemp.getValue();
    		Integer pp = map.get(property);
            if(pp == null){
            	count++;
            }
		}
        
        int[][] ret = new int[map.size() + count][4];
        Set entrys = map.entrySet();
        Iterator ite = entrys.iterator();
        int i = 0;
        while (ite.hasNext()) {
            Map.Entry entry = (Map.Entry) ite.next();
            ret[i][0] = ((Integer) entry.getKey()).intValue();
            ret[i][1] = ((Integer) entry.getValue()).intValue();
            if((Integer) properties0.get(((Integer) entry.getKey()).intValue()) == null){
            	ret[i][2] = 0;
            }else{
            	ret[i][2] = (Integer) properties0.get(((Integer) entry.getKey()).intValue());
            }
            
            ret[i][3] = getDiamondMosiacProperty((Integer) entry.getKey());
            
            i++;
        }
        
        //遍历属性 ，加上装备没有的属性
        for(Map.Entry<Byte, Short> diamondtemp: diamondPropertyMap.entrySet()){
    		int property = diamondtemp.getKey();
    		int value = diamondtemp.getValue();
    		Integer pp = (Integer) map.get(property);
            if(pp == null){
            	 ret[i][0] = property;
                 ret[i][1] = 0;
                 ret[i][2] = 0;
                 ret[i][3] = value;
                 i++;
            }
           
		}
        
        return ret;
    }

    public boolean isValid() {
        if (getCurrentDurability() > 0)
            return true;
        else {
            byte part = getPart();
            if (part == IEquipment.PART_FINGER || part == IEquipment.PART_NECK ||
                part == IEquipment.PART_WRIST)
                return true;
            else return false;
        }
    }

    public boolean isWeapon(){
        return getPart()==IEquipment.PART_WEAPON;
    }

    public boolean isArmor() {
        byte part = getPart();
        return (part == IEquipment.PART_HEAD || part == IEquipment.PART_CHEST ||
                part == IEquipment.PART_WAIST || part == IEquipment.PART_FEET ||
                part == IEquipment.PART_SHIELD);
    }

    public int getCredit(){
        return template.getCredit();
    }

    public List<Enhance> getEnhances(){
        return enhances;
    }


    public void addProperty(int index, short value) {
        if (value == 0)
            return;
        Integer oldValue = (Integer) properties0.get(index);
        if (oldValue != null) {
            value = (short) (value + oldValue.intValue());
        }
        properties0.put(new Integer(index), new Integer(value));
    }


    public void decProperty(int index, short value){
        if(value<0)
            throw new IllegalArgumentException("value can not be "+value);
        Integer oldValue = (Integer)properties0.get(index);
        if(oldValue!=null){
            int v = oldValue.intValue()-value;
            if(v<0)
                throw new IllegalArgumentException("value can not be "+value);
            if(v==0)
                properties0.remove(new Integer(index));
            else
                properties0.put(new Integer(index),new Integer(v));
        }else{
            throw new IllegalArgumentException("property "+index+" not found");
        }
    }

    /**
     * @param index
     * @return精炼属性
     */
    public int getProperty0(int index) {
        Integer ret = (Integer) properties0.get(new Integer(index));
        if (ret == null) {
            return 0;
        }
        return ret.intValue();
    }

    /**
     * @return
     * 精炼总属性
     */
    public int[][] getProperties0() {
        int[][] ret = new int[properties0.size()][2];
        Set entrys = properties0.entrySet();
        Iterator ite = entrys.iterator();
        int i = 0;
        while (ite.hasNext()) {
            Map.Entry entry = (Map.Entry) ite.next();
            ret[i][0] = ((Integer) entry.getKey()).intValue();
            ret[i][1] = ((Integer) entry.getValue()).intValue();
            i++;
        }
        return ret;
    }


    public byte[] toClientBytesWithLevel(int level) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(getItemId());
            dos.writeInt(id);
            //mengjie add 有过期时间的装备，名字后面再加过期时间
            String str_name = "";
            if (FAILURE_TIME > 0){
            	Date failuredate = new Date(FAILURE_TIME);
            	str_name = "(" + format.format(failuredate) + "过期)";
            }
            dos.writeUTF(getName() + str_name);
            dos.writeByte(getLevel());
            dos.writeByte(getRequiredLevel());
            dos.writeByte(getQuality());
            dos.writeByte(getPart());
            dos.writeShort(getDurability());
            dos.writeShort(currentDurability);
            dos.writeInt(getPrice());
            byte bindType = getBindType();
            if (isBinded()) {
                bindType |= 0x80;
            }
            dos.writeByte(bindType);
            dos.writeByte(getTimes());
            int growLevel = level;
            if(level == -1){
            	growLevel = template.level;
            }
            int[][] pros = getProperties(growLevel);
            dos.writeByte(pros.length);
            for (int j = 0; j < pros.length; j++) {
                byte pro = (byte) pros[j][0];
                short value = (short) pros[j][1];
                short enhanceValue = (short)pros[j][2];
                short diamondMosaicValue = (short)pros[j][3];
                dos.writeByte(pro);
                if(dataVersion > 0){
                	dos.writeShort(value - enhanceValue);
                	dos.writeShort(enhanceValue);
                	dos.writeShort(diamondMosaicValue);
                }else{
                	//dos.writeShort(value);
                	if(pro == IEquipment.EQUIP_ADD_HIT || pro == IEquipment.EQUIP_ADD_FLEE
                			|| pro == IEquipment.EQUIP_ADD_MCRI  || pro == IEquipment.EQUIP_ADD_MCRI){
                		dos.writeShort(value / IEquipment.CONVERTNUM);
                	}else{
                		dos.writeShort(value);
                	}
                }
              /*  if(dataVersion > 0){
                	dos.writeShort(enhanceValue);
                	dos.writeShort(diamondMosaicValue);
                }*/
            }

            // 装备镶嵌宝石详情
            if (dataVersion > 0) {
            	int size = 0;
            	byte[] roleInfo = getDiamondMosiacRoleInfo();
            	for (byte i = 0; i < roleInfo.length; i ++) {
            		if (roleInfo[i] > 1) {
            			size ++;
            		}
            	}
            	dos.write((byte) size);
            	for (byte i = 0; i < Utils.maxHolesEqu; i ++) {
            		DiamondMosaic diamondMosaic = getDiamondMosaicRole(i);
            		if (diamondMosaic != null) {
            			dos.writeUTF(Items.getTemplate(diamondMosaic.getItemId()).getName());
            			dos.writeUTF(Items.getTemplate(diamondMosaic.getItemId()).getDesc());
            			int count = 0;
            			if(developAddCount != null){
            				count = developAddCount[i];
            			}
            			dos.writeInt(count);
            			int point = diamondMosaic.getAddPoint();
            			if(developAddPoint != null){
            				point += developAddPoint[i];
            			}
            			dos.writeInt(point);
            		}
            	}
            }
            
            dos.writeInt(Suits.getSuitColor(this));
            dos.writeUTF(Suits.getSuitName(this));
            if(dataVersion > 0){
	            if(diamond == 0){//-1为写给客户端不可鉴定
	            	if(Diamonds.CanNotDiamond(getQuality(), getPart())){
	            		dos.writeByte(-1);
	            	}else{
	            		dos.writeByte(0);
	            	}
	            }else{
	            	dos.writeByte(diamond);
	            }
	            
	            dos.writeInt(extend_Flag);
	            if(canLettering()){
	            	dos.writeUTF(letteringString);
	            }
	            
	            dos.writeByte(itemShowType);
	            //2011-07-14 13:39:22 添加极限打孔符 修改下发信息 并修改装备信息
	            if(diamondcount < MaxDiamondRoleCount){
	            	diamondcount = MaxDiamondRoleCount;
	            	int maxLength = Math.max(diamondcount, diamondMosiacRoleInfo.length);
	            	byte[] dmri = new byte[maxLength];
	            	System.arraycopy(diamondMosiacRoleInfo, 0, dmri, 0, diamondMosiacRoleInfo.length);
	            	diamondMosiacRoleInfo = dmri;
	            }
	            dos.writeByte(diamondcount);
	            for(int i = 0; i < diamondcount; i++){
	            	dos.writeByte(diamondMosiacRoleInfo[i]); //这里是宝石的标志位
	            	/*if(getDiamondMosaicRole((byte) i) != null){
	            		dos.writeInt(getDiamondMosaicRole((byte) i).getItemId()); //发给客户端该孔位的宝石id
	            	}else{
	            		dos.writeInt(0); //发给客户端该孔位的宝石id
	            	}*/
	            	
	            }
            }
            if(dataVersion > 0){
            	dos.writeBoolean(canSplit());	//是否能分解
            	//附魔属性
            	Enchanting enchan = getEnchanting();
            	dos.writeByte(enchan.getArrtType());
            	dos.writeByte(enchan.getArrtValue());
            	dos.writeByte(enchan.getStoneType());
            	dos.writeByte(enchan.getStoneValue());
            	
            	//属性攻三种属性的值
            	Viany viany = getViany();
            	dos.writeInt(viany.getStone());
            	dos.writeInt(viany.getVianyAttack(Viany.STONE));
                dos.writeInt(viany.getVianyDefense(Viany.STONE));
            	dos.writeInt(viany.getScissors());
            	dos.writeInt(viany.getVianyAttack(Viany.SCISSORS));
                dos.writeInt(viany.getVianyDefense(Viany.SCISSORS));
            	dos.writeInt(viany.getPaper());
            	dos.writeInt(viany.getVianyAttack(Viany.PAPER));
                dos.writeInt(viany.getVianyDefense(Viany.PAPER));
                if(level != -1 && level != template.level && isGrow()){
                	dos.writeInt(level);
                }else{
                	dos.writeInt(-1);
                }
            }
            
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }

    }
    
    /**
     * 可以刻字
     * @return
     */
    public boolean canLettering(){
    	boolean flag = false;
    	boolean letteringFlag = false;
		try {
			letteringFlag = Utils.CheckIntN(extend_Flag, CAN_LETTERING);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(letteringFlag){ //可以刻字
			flag = true;
		}
    	return flag;
    }
    
    //已经刻字了
    public boolean lettered(){
    	boolean flag = false;
    	boolean letteringFlag = false;
    	boolean doLetteringFlag = false;
		try {
			letteringFlag = Utils.CheckIntN(extend_Flag, CAN_LETTERING);
			doLetteringFlag = Utils.CheckIntN(extend_Flag, LETTERINGED);
		} catch (Exception e) {
		}
		if(letteringFlag && doLetteringFlag){ //可以刻字
			flag = true;
		}
    	return flag;
    }
    
    public byte[] toDbBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(getItemId());
            dos.writeInt(id);
            dos.writeInt(0);
            dos.writeBoolean(isBinded());
            dos.writeShort(currentDurability);
//            dos.writeLong(0);
            dos.write(enhances.size());
            for (int i = 0; i < enhances.size(); i++) {
                dos.write(enhances.get(i).getProperty());
            }
            dos.write(lastEnhanceStatus ? 1 : 0);
            dos.write(enhanceStatusTimes);
            //version 3 add mengjie add
            dos.writeLong(FAILURE_TIME);
            
            //version 4 add jwp add
            dos.writeByte(diamond);
            dos.writeInt(extend_Flag);
            if(lettered()){
            	dos.writeUTF(letteringString);
            }
            
            //写下version 6 装备孔位信息，写下的是装备孔位， 装备的镶嵌
            dos.writeByte(diamondMosiacRoleInfo.length);
            dos.write(diamondMosiacRoleInfo);
            
            
            dos.writeByte(dimondMosaicMap.size());
            //这里没有用属性来做，只记录物品id ,,根据配置表，读取的时候生成宝石类来获取可以更改的属性
            for(Map.Entry<Byte, DiamondMosaic> diamondtemp: dimondMosaicMap.entrySet()){
            	dos.writeByte(diamondtemp.getKey());
            	dos.writeInt(diamondtemp.getValue().getItemId());
            }
            
            //version 7 附魔属性
            dos.writeInt(enchanting.getEnchantingItemId());
            dos.writeByte(enchanting.getArrtType());
            dos.writeByte(enchanting.getArrtValue());
            dos.writeByte(enchanting.getStoneType());
            dos.writeByte(enchanting.getStoneValue());
            
            //vesion 9 属性攻
            dos.writeInt(viany.getStone());
            dos.writeInt(viany.getScissors());
            dos.writeInt(viany.getPaper());
            
            //vesion 10 宝石养成
            if(developAddCount == null){
            	dos.writeByte(0);
            }else{
            	dos.writeByte(developAddCount.length);
            	for(int i=0; i<developAddCount.length; i++){
            		dos.writeShort(developAddCount[i]);
            	}
            }
            if(developAddPoint == null){
            	dos.writeByte(0);
            }else{
            	dos.writeByte(developAddPoint.length);
            	for(int i=0; i<developAddPoint.length; i++){
            		dos.writeShort(developAddPoint[i]);
            	}
            }
//            dos.writeUTF(name);
//            dos.writeByte(level);
//            dos.writeByte(requiredLevel);
//            dos.writeByte(quality);
//            dos.writeByte(part);
//            dos.writeShort(durability);
//            dos.writeShort(currentDurability);
//            dos.writeInt(price);
//            if(binded){
//                bindType |= 0x80;
//            }
//            dos.writeByte(bindType);
//            dos.writeByte(times);
//            int[][] pros = getProperties();
//            dos.writeByte(pros.length);
//            for (int j = 0; j < pros.length; j++) {
//                byte pro = (byte) pros[j][0];
//                short value = (short) pros[j][1];
//                dos.writeByte(pro);
//                dos.writeShort(value);
//            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }

    }
    public void setId(int id){
        this.id = id;
    }

	public void insteadEnhance(Enhance enhance, int insteadIndex) {
		// TODO Auto-generated method stub

			// TODO Auto-generated method stub
		if(enhance==null)
			throw new IllegalArgumentException("instead enhance can not be null");
		if(enhances.size()>=10)
		    throw new IllegalStateException(" instead enhances can not >10");
		if(insteadIndex < 1 || insteadIndex > 9){
	    	throw new IllegalStateException("istead enhances is not avlib can not < 1 or can not > 9");
	    }
		    //减去原来的属性
		Enhance oldEnhance = enhances.get(insteadIndex - 1);
		decProperty(oldEnhance.getProperty(),(short)oldEnhance.getPoint(insteadIndex));
	    //重新设置精炼属性，增加新属性
	    enhances.set(insteadIndex -1 , enhance);
	    addProperty(enhance.getProperty(),(short)enhance.getPoint(insteadIndex));
	}
	
	
	/**
	 * 鉴定数量
	 */
	private byte diamond = 0;
	public byte getDiamond() {
		// TODO Auto-generated method stub
		return diamond;
	}

	public void setDiamond(byte diamond) {
		// TODO Auto-generated method stub
		this.diamond =  diamond;
	}

	int dataVersion = 0;
	public int getDataVesion() {
		// TODO Auto-generated method stub
		return dataVersion;
	}

	public void setDataVersion(int dataVersion) {
		this.dataVersion = dataVersion;
	}

	

	private int extend_Flag = 0; //这个字节以后可以用来扩展时候否再加入其它属性
	private String letteringString = "";
	
	
	public void setLetteringString(String letteringString) {
		this.letteringString = letteringString;
	}
	
	
	public int getExtendFlag() {
		return extend_Flag;
	}
	
	
	public String getLetteringString() {
		return letteringString;
	}
	
	public void setExtendFlag(int LetteringFlag) {
		this.extend_Flag = LetteringFlag;
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

	public byte[] toClientBytes(int dataVersion) {
		return null;
	}
	
	public byte opendDiamondCount;
	public byte getOpenDiamondCount() {
		return this.opendDiamondCount;
	}

	public void setOpenDiamondCount(byte opendDiamondCount) {
		this.opendDiamondCount = opendDiamondCount;
	}
	
	
	public void resetDiamondMosiacRoleInfo(byte newDiamondcount) {
		byte[] diamondMosiacNewRoleInfo = new byte[newDiamondcount];
		System.arraycopy(diamondMosiacRoleInfo, 0, diamondMosiacNewRoleInfo, 0, diamondMosiacRoleInfo.length);
		diamondMosiacRoleInfo = diamondMosiacNewRoleInfo;
	}
	
	/**
	 * @param diamondRoleInfo
	 * @return该装备已经打孔的数量
	 */
	public byte getDiamonRoleSuccessCount(byte[] diamondRoleInfo){
		byte count = 0;
		for(int i = 0; i < diamondRoleInfo.length; i++){
			if(diamondRoleInfo[i] > IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
				count++;
			}
		}
		return count;
	}

	public byte canDiamondMosiacEmbed(byte diamondrole, byte property) {
		byte canEmbed = IEquipment.CURRENT_EQU_DIAMOND_NOTROLE;
		if(diamondMosiacRoleInfo[diamondrole] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
			canEmbed = IEquipment.CURRENT_EQU_DIAMOND_CANOTEMEDED;
		}else if(diamondMosiacRoleInfo[diamondrole] > IEquipment.CURRENT_EQU_CANDIAMOND){
			canEmbed = IEquipment.CURRENT_EQU_DIAMOND;
		}
		return canEmbed;
	}
	
	public byte isSameProDiamondMosaic(byte diamondrole, byte property){
		byte issamepro = IEquipment.CURRENT_EQU_DIAMOND_NOTROLE;
		//检查其他属性宝石是否已经打造过
		for(Map.Entry<Byte, DiamondMosaic> diamondtemp: dimondMosaicMap.entrySet()){
			DiamondMosaic dia = (DiamondMosaic) diamondtemp.getValue();
			if(property == dia.getProperty()){
				issamepro = IEquipment.CURRENT_EQU_DIAMOND_PROPERTY;
				break;
			}
		}
		return issamepro;
	}
	
	
	public void diamondMosaic(byte diamondRole, DiamondMosaic diamondMosaic) {
		this.dimondMosaicMap.put(diamondRole, diamondMosaic);
		int addPoint = 0;
		if(developAddPoint != null){
			addPoint = developAddPoint[diamondRole];
		}
		diamondPropertyMap.put(diamondMosaic.getProperty(),(short)(diamondMosaic.getAddPoint() + addPoint));
	}

	public void cancerDiamondMosaic(byte diamondRole) {
		DiamondMosaic diamondMosaic = dimondMosaicMap.get(diamondRole);
		diamondPropertyMap.remove(diamondMosaic.getProperty());
		this.dimondMosaicMap.remove(diamondRole);
	
	}
	
	 /**
     * @param index
     * @return宝石加成属性
     */
    public int getDiamondMosiacProperty(int index){
    	int diamondProperty = 0;
    	for(Map.Entry<Byte, Short> diamondtemp: diamondPropertyMap.entrySet()){
    		if(index == diamondtemp.getKey()){
				diamondProperty = diamondtemp.getValue();
				break;
    		}
		}
    	return diamondProperty;
    }
	
    public DiamondMosaic getDiamondMosaicRole(byte role) {
		return dimondMosaicMap.get(role);
	}
    
	public Enchanting getEnchanting() {
		return enchanting;
	}

	public void setEnchanting(byte type, Enchanting enchanting) {
		this.enchanting = enchanting;
	}

	public void canEnchanting(byte type) {
		
	}

	public void setCanSplit(boolean canSplit) {
		template.setCanSplit(canSplit);
	}
	
	public Viany getViany(){
		return viany;
	}

	public int getDiamondMosaicRoleSize() {
		return dimondMosaicMap.size();
	}

	public boolean isGrow() {
		return template.isGrow();
	}
	
	public short[] getDevelopAddPoint() {
		if(developAddPoint == null){
			developAddPoint = new short[MaxDiamondRoleCount];
		}
		return developAddPoint;
	}

	public void setDevelopAddPoint(short[] addpoint) {
		this.developAddPoint = addpoint;
	}

	public short[] getDevelopAddCount() {
		if(developAddCount == null){
			developAddCount = new short[MaxDiamondRoleCount];
		}
		return developAddCount;
	}

	public void setDevelopAddCount(short[] addcount) {
		developAddCount = addcount;
	}
}
