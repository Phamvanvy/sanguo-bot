package com.pip.itimes.server.stage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class DynamicEquipment implements IEquipment{
	
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
	
    private int seed;
    private short currentDurability;
    private boolean binded;
    private Map properties = new HashMap();
    private int id;
    private int times;
    private boolean isTemplate;
    private List<Enhance> enhances = new ArrayList<Enhance>(9);
    private DynamicEquipmentTemplate template;
    private boolean lastEnhanceStatus;
    private int enhanceStatusTimes;

    public static final byte EQUIP_ADD_VIT = 1;
    public static final byte EQUIP_ADD_INT = 2;
    public static final byte EQUIP_ADD_STR = 3;
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

    public static final byte CREATE_NORMAL = 1;
    public static final byte CREATE_DYNAMIC = 2;

    //mengjie add 失效时间
    public long FAILURE_TIME = 0;
    
    
    /**
     * 宝石所加的属性
     */
    private Map<Byte, Short> diamondPropertyMap = new HashMap<Byte, Short>();
    
    /**
     * 每次宝石镶嵌的  key为镶嵌的孔位    值为镶嵌的对象
     */
    private Map<Byte, DiamondMosaic> dimondMosaicMap = new HashMap<Byte, DiamondMosaic>();
    
    
    /**
     * 最高的孔数量
     */
    public final static byte MaxDiamondRoleCount = 7;
    /**
     * 当前的孔位信息 0为没有打孔     1为已经打孔了没有镶嵌   2以上为已经打孔了加上了宝石的等级
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
    
    /**
     * 属性攻
     */
    Viany viany = new Viany();
    
    public byte[] getDiamondMosiacRoleInfo() {
		return diamondMosiacRoleInfo;
	}
    
    public byte checkDiamondMosiacRoleAcount(byte diamondLevel){
    	byte acount = 0;
    	for(int i = 0;i < diamondMosiacRoleInfo.length;i++){
    		if(diamondMosiacRoleInfo[i] > diamondLevel){
    			acount++;
    		}
    	}
    	return acount;
    }

	public void setDiamondMosiacRoleInfo(byte[] diamondMosiacRoleInfo) {
		this.diamondMosiacRoleInfo = diamondMosiacRoleInfo;
	}
	
    
    private static SimpleDateFormat format = new SimpleDateFormat("yy年MM月dd日HH时");

    public DynamicEquipment(DynamicEquipmentTemplate template,int id,int seed){
        this.id = id;
        this.seed = seed;
        this.template = template;
    }

    public DynamicEquipment(DynamicEquipmentTemplate template,int seed) {
        this(template,0,seed);
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

    public void unEnhance() {
        if (enhances.size() == 0)
            throw new IllegalStateException("equipment hasn't enhanced");
        Enhance enhance = (Enhance) enhances.remove(enhances.size() - 1);
        decProperty(enhance.getProperty(), (short) enhance.getPoint(enhances.size()+1));
    }

    public void unEnhanceAll() {
        if (enhances.size() != 0){
        	int enhancessize = enhances.size();
            for (int i = 0 ; i < enhancessize ;i++){
            	unEnhance();
//            	enhance = (Enhance) enhances.remove(i);
//            	decProperty(enhance.getProperty(), (short) enhance.getPoint(enhances.size()));
            }
        }
    }
    
    public short getLevel() {
        return template.getLevel();
    }



    public short getRequiredLevel() {
        return template.getRequiredLevel();
    }



    public byte getCreateType(){
        return template.getCreateType();
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

    public byte getPart() {
        return template.getPart();
    }


    public short getDurability() {
        return template.getDurability();
    }

    public void setCurrentDurability(short durability){
        this.currentDurability = durability;
    }

    public short getCurrentDurability(){
        return currentDurability;
    }


    public byte getBindType() {
        return template.getBindType();
    }

    public void setBinded(boolean binded){
        this.binded = binded;
    }

    public boolean isBinded(){
        return binded;
    }

    public long getFAILURE_TIME() {
		return FAILURE_TIME;
	}

	public void setFAILURE_TIME(long failure_time) {
		FAILURE_TIME = failure_time;
	}

	public void addProperty(int index,int value){
        if(value==0)
            return;
        Integer oldValue = (Integer)properties.get(index);
        if(oldValue!=null){
            value = (value + oldValue.intValue());
        }
        properties.put(new Integer(index),new Integer(value));
    }

    public void decProperty(int index, short value){
        if(value<0)
            throw new IllegalArgumentException("value can not be "+value);
        Integer oldValue = (Integer)properties.get(index);
        if(oldValue!=null){
            int v = oldValue.intValue()-value;
            if(v<0)
                throw new IllegalArgumentException("value can not be "+value);
            if(v==0)
                properties.remove(new Integer(index));
            else
                properties.put(new Integer(index),new Integer(v));
        }else{
            throw new IllegalArgumentException("property "+index+" not found");
        }
    }

    public int getProperty(int index, int level) {
    	int originalProperty = template.getProperty(index) + level * template.getProperty(index, true) / 100;
    	int starProperty = getDiamondProperty(index, level);
    	
    	//精炼属性
        Integer ret = (Integer)properties.get(new Integer(index));
        if(ret==null){
        	ret =  0;
        }
    	int diamondMosaicPropeyty = getDiamondMosiacProperty(index);
        return ret.intValue() + starProperty + originalProperty + diamondMosaicPropeyty;
    }
    
    public int getPropertyTemplate(int index) {
    	return template.getProperty(index);
    }

    public int[][] getProperties(int level){
    	int count = 0;
    	for(Map.Entry<Byte, Short> diamondtemp: diamondPropertyMap.entrySet()){
    		int property = diamondtemp.getKey();
    		int value = diamondtemp.getValue();
    		Integer pp = (Integer) properties.get(property);
    		if(pp == null){
    			count++;
    		}
  		}
          
        int[][] ret = new int[properties.size() + count][4];
        Set entrys = properties.entrySet();
        
      
        
        Iterator ite = entrys.iterator();
        int i=0;
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            ret[i][0] = ((Integer)entry.getKey()).intValue();
            ret[i][1] = ((Integer)entry.getValue()).intValue();
            ret[i][2] = getEnhanceProperties(((Integer)entry.getKey()).intValue());
            ret[i][3] = getDiamondMosiacProperty(((Integer)entry.getKey()).intValue());
            i++;
        }
        
      //遍历属性 ，加上装备没有的属性
        for(Map.Entry<Byte, Short> diamondtemp: diamondPropertyMap.entrySet()){
    		int property = diamondtemp.getKey();
    		int value = diamondtemp.getValue();
    		Integer pp = (Integer) properties.get(property);
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


    public int getItemId(){
        return template.getItemId();
    }

    public void setId(int id){
        this.id = id;
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



    public int getPrice() {
        return template.getPrice();
    }

    public int getTimes(){
        return enhances.size();
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
            buff.append('\n');
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

      //  boolean flag = false;

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
	             	//flag = true;
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
        
//       for(int i = 0; i < pros.length; i++){
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
       // if(flag){
       buff.append('\n');
       // }

       buff.append("需要等级：");
       buff.append(getRequiredLevel());
       buff.append('\n');
       buff.append("售价：");
       buff.append(getPrice());
       return buff.toString();
    }

    public int getCredit() {
        return template.getCredit();
    }

    public boolean isTemplate(){
        return isTemplate;
    }

    public void setTemplate(boolean b){
        this.isTemplate = b;
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

    public List<Enhance> getEnhances(){
        return enhances;
    }

//    public Equipment newInstance(){
//        int id = IDGenerator.getEquipmentId();
//        Equipment equ = new Equipment();
//        equ.level = level;
//        equ.requiredLevel = requiredLevel;
//        equ.createType = createType;
//        equ.quality = quality;
//        equ.part = part;
//        equ.durability = durability;
//        equ.currentDurability = durability;
//        equ.bindType = bindType;
//        equ.properties = new HashMap(properties);
//        equ.price = price;
//        equ.name = name;
//        equ.times = 0;
//        equ.itemId = itemId;
//        equ.id = id;
//        if(bindType==IItem.BIND_GET){
//            equ.binded = true;
//        }
//        return equ;
//    }

//    public static Equipment[] getEquipments(byte[] bytes) throws IOException{
//        List l = new ArrayList();
//        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//        DataInputStream dis = new DataInputStream(bis);
//        int count = dis.readShort();
//        for(int i=0;i<count;i++){
//            Equipment equ = new Equipment();
//            int itemId = dis.readInt();
//            equ.setItemId(itemId);
//            int id = dis.readInt();
//            equ.setId(id);
//            String title = dis.readUTF();
//            equ.setName(title);
//            byte itemLevel = dis.readByte();
//            equ.setLevel(itemLevel);
//            byte requiredLevel = dis.readByte();
//            equ.setRequiredLevel(requiredLevel);
//            byte equLevel = dis.readByte();
//            equ.setEquipmentLevel(equLevel);
//            byte part = dis.readByte();
//            equ.setPart(part);
//            byte durability = dis.readByte();
//            equ.setDurability(durability);
//            byte currentDurability = dis.readByte();
//            equ.setCurrentDurability(currentDurability);
//            int price = dis.readInt();
//            equ.setPrice(price);
//            byte bind = dis.readByte();
//            equ.setBindType((byte)((bind&127)&0xFF));
//            equ.setBinded((bind&0x80)!=0);
//            byte times = dis.readByte();
//            equ.setTimes(times);
//            byte size = dis.readByte();
//            for (int j = 0; j < size; j++) {
//                byte type = dis.readByte();
//                short value = dis.readByte();
//                equ.addProperty(type,value);
//            }
//            l.add(equ);
//        }
//        Equipment[] ret = new Equipment[l.size()];
//        l.toArray(ret);
//        return ret;
//    }

//    public static Equipment getEquipments(DataInputStream dis) throws
//            IOException {
//        Equipment equ = new Equipment();
//        int itemId = dis.readInt();
//        equ.setItemId(itemId);
//        int id = dis.readInt();
//        equ.setId(id);
//        String title = dis.readUTF();
//        equ.setName(title);
//        byte itemLevel = dis.readByte();
//        equ.setLevel(itemLevel);
//        byte requiredLevel = dis.readByte();
//        equ.setRequiredLevel(requiredLevel);
//        byte quality = dis.readByte();
//        equ.setQuality(quality);
//        byte part = dis.readByte();
//        equ.setPart(part);
//        short durability = dis.readShort();
//        equ.setDurability(durability);
//        short currentDurability = dis.readShort();
//        equ.setCurrentDurability(currentDurability);
//        int price = dis.readInt();
//        equ.setPrice(price);
//        byte bind = dis.readByte();
//        equ.setBindType((byte)((bind&127)&0xFF));
//        equ.setBinded((bind&0x80)!=0);
//        byte times = dis.readByte();
//        equ.setTimes(times);
//        byte size = dis.readByte();
//        for (int j = 0; j < size; j++) {
//            byte type = dis.readByte();
//            short value = dis.readShort();
//            equ.addProperty(type, value);
//        }
//        return equ;
//    }

    public byte[] toClientBytesWithLevel(int level) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(getItemId());
            dos.writeInt(getId());
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
            byte bindType = this.getBindType();
            if(binded){
                bindType |= 0x80;
            }
            dos.writeByte(bindType);
            dos.writeByte(times);
            int growLevel = level;
            if(level == -1){
            	growLevel = template.level;
            }
            int[][] pros = getProperties(growLevel);
            dos.writeByte(pros.length);
            for (int j = 0; j < pros.length; j++) {
                byte pro = (byte) pros[j][0];
                short value = (short) pros[j][1];
                short enhanceVaule = (short)pros[j][2];
                short diamondMosaicValue = (short)pros[j][3];
                dos.writeByte(pro);
                //dos.writeShort(value);
                if(dataVersion > 0){
                	dos.writeShort(value - enhanceVaule);
                	dos.writeShort(enhanceVaule);
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
               /* if(dataVersion > 0){
                	dos.writeShort(enhanceVaule);
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
            
            //TODO extraEffect
            dos.writeInt(0xFF0000);
            dos.writeUTF("");
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
	            //下面为测试代码
	            //diamondcount = 5;
	          //2011-07-14 13:39:22 添加极限打孔符 修改下发信息 并修改装备信息
	            if(diamondcount < Utils.maxHolesEqu){
	            	diamondcount = Utils.maxHolesEqu;
	            	int maxLength = Math.max(diamondcount, diamondMosiacRoleInfo.length);
	            	byte[] dmri = new byte[maxLength];
	            	System.arraycopy(diamondMosiacRoleInfo, 0, dmri, 0, diamondMosiacRoleInfo.length);
	            	diamondMosiacRoleInfo = dmri;
	            }
	            dos.writeByte(diamondcount);
	            for(int i = 0; i < diamondcount; i++){
	            	dos.writeByte(diamondMosiacRoleInfo[i]);
	            /*	if(getDiamondMosaicRole((byte) i) != null){
	            		dos.writeInt(getDiamondMosaicRole((byte) i).getItemId()); //发给客户端该孔位的宝石id
	            	}else{
	            		dos.writeInt(0); //发给客户端该孔位的宝石id
	            	}*/
	            }
	            //dos.write();
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
    	//boolean doLetteringFlag = false;
		try {
			letteringFlag = Utils.CheckIntN(extend_Flag, CAN_LETTERING);
			//doLetteringFlag = Utils.CheckIntN(extend_Flag, LETTERINGED);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
		}
		if(letteringFlag &&  doLetteringFlag){ //可以刻字
			flag = true;
		}
    	return flag;
    }
    
	public byte[] toDbBytes(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(getItemId());
            dos.writeInt(id);
            dos.writeInt(seed);
            dos.writeBoolean(binded);
            dos.writeShort(currentDurability);
//            dos.writeLong(0);
            dos.write(enhances.size());
            for(int i=0;i<enhances.size();i++){
                dos.write(enhances.get(i).getProperty());
            }
            dos.write(lastEnhanceStatus?1:0);
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
            		dos.writeInt(developAddCount[i]);
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

	public void insteadEnhance(Enhance enhance, int insteadIndex) {
		
		if(enhance==null)
			throw new IllegalArgumentException("instead enhance can not be null");
	    if(enhances.size()>=10)
	        throw new IllegalStateException("instead enhances can not >10");
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
	 * @param property
	 * @return 该属性的精炼点数
	 */
	public  int getEnhanceProperties(int property){
		int point = 0;
		for(int i = 0; i < enhances.size(); i++){
			Enhance enhance = enhances.get(i);
			if(enhance.property == property){
				point += enhance.point[i];
			}
		}
		return point;
	}
	
	/**
	 * 鉴定数量
	 */
	private byte diamond = 0;
	public byte getDiamond() {
		
		return diamond;
	}
	public void setDiamond(byte diamond) {
		
		this.diamond =  diamond;
	}
	
	int dataVersion = 0;
	public int getDataVesion() {
		
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
		// TODO Auto-generated method stub
		return null;
	}
	
	public byte openDiamondCount;
	public byte getOpenDiamondCount() {
		return this.openDiamondCount;
	}

	public void setOpenDiamondCount(byte opendDiamondCount) {
		this.openDiamondCount = opendDiamondCount;
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
