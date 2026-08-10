package peony.game.itemenhance;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import peony.game.GameItem;
import peony.game.GameItemObject;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.Marshaller;
import peony.game.ObjectAccessor;
import peony.game.PropertyCalculator;
import peony.game.Serializer;

import com.pip.sanguo.data.equipment.AttributeCalculator;

/**
 * 装备强化信息，包含宝石镶嵌信息以及将来的其他物品强化数据。
 * @author lighthu
 */
public class ItemEnhance implements GameItemObject {
    // 打孔数
    public int addHole;
    // 扩展最大孔数
    public int addMaxHole;
    // 各孔上打的宝石，每2个int表示一个宝石，其中第一个表示孔位（0表示第一个），第二个表示物品ID
    protected int[] jewels = new int[0];
    
    protected int star;
    
    /** 称号系统星级增加系数 */
    public static float starCoefficient = 1f;

    
    protected NaturalEnhance[] naturals = new NaturalEnhance[0];
    
    /**
     * 4种装备强化
     */
    public int[] equipEnhanceData = {0,0,0,0};
    
    protected String markString="";
    
    public String getMarkString(){
    	return markString;
    }
    
    public void setMarkString(String s){
    	this.markString = s;
    }
    
    /**
     * 获得4种装备强化
     * @return
     */
    public int[] getEnhanceData(){
    	return equipEnhanceData;
    }
    
    /**
     * 基础强化
     * @return
     */
    public int getPrimaryEnhance(){
    	return equipEnhanceData[0];
    }
    
    /**
     * 星级强化
     * @return
     */
    public int getStarEnhance(){
    	return equipEnhanceData[1];
    }
    
    /**
     * 资质强化
     * @return
     */
    public int getNaturalsEnhance(){
    	return equipEnhanceData[2];
    }
    
    /**
     * 宝石强化
     * @return
     */
    public int getJewelsEnhance(){
    	return equipEnhanceData[3];
    }
    /**
     * 设置4种装备强化
     * @param value
     */
    public void setEnhanceData(int[] value){
    	System.arraycopy(value, 0,equipEnhanceData , 0, value.length);
    }
    
    public int getStar(){
    	return star;
    }
    
    public void setStar(int star){
    	this.star = star;
    }
    
    public NaturalEnhance[] getNaturals(){
    	return naturals;
    }
    
    public void setNaturals(NaturalEnhance[] naturals){
    	this.naturals = naturals;
    }
    
    public int getAddHole() {
        return addHole;
    }
    
    public void setAddHole(int value) {
        addHole = value;
    }
    
    public int getAddMaxHole() {
        return addMaxHole;
    }
    
    public void setAddMaxHole(int value) {
        addMaxHole = value;
    }
    
    /**
     * 取得当前镶嵌宝石数。
     * @return
     */
    public int getJewelCount() {
        return jewels.length / 2;
    }
    
    /**
     * 取得第N个宝石的孔位
     * @param index
     * @return 0表示第一个
     */
    public int getJewelHole(int index) {
        return jewels[index * 2];
    }
    
    /**
     * 取得第N个宝石的ID。
     * @param index
     * @return
     */
    public int getJewelID(int index) {
        return jewels[index * 2 + 1];
    }
    
    public int[] getJewelIDs(){
    	int[] ret = new int[jewels.length/2];
    	for(int i=0;i<ret.length;i++){
    		ret[i] = jewels[i * 2 + 1];
    	}
    	return ret;
    }
    
    /**
     * 查询某个孔位上的宝石。
     * @param hole 孔位（0表示第一个）
     * @return 宝石物品ID, -1表示没有
     */
    public int getJewel(int hole) {
        for (int i = 0; i < jewels.length; i += 2) {
            if (jewels[i] == hole) {
                return jewels[i + 1];
            }
        }
        return -1;
    }
    
    /**
     * 按孔位取出宝石。
     * @param hole 孔位（0表示第一个）
     */
    public void removeJewel(int hole) {
        for (int i = 0; i < jewels.length; i += 2) {
            if (jewels[i] == hole) {
                int[] newarr = new int[jewels.length - 2];
                System.arraycopy(jewels, 0, newarr, 0, i);
                System.arraycopy(jewels, i + 2, newarr, i, jewels.length - i - 2);
                jewels = newarr;
                break;
            }
        }
    }
    
    /**
     * 添加镶嵌宝石。
     * @param hole 孔位（0表示第一个）
     */
    public void addJewel(int hole, int itemID) {
        int[] newarr = new int[jewels.length + 2];
        System.arraycopy(jewels, 0, newarr, 0, jewels.length);
        newarr[jewels.length] = hole;
        newarr[jewels.length + 1] = itemID;
        jewels = newarr;
    }
    
    /**
     * 查找是否已经镶嵌了某一类的宝石。
     * @param type
     * @return
     */
    public boolean findJewelByType(int type) {
        for (int i = 0; i < jewels.length; i += 2) {
            ItemTemplate it = ObjectAccessor.getItemTemplate(jewels[i + 1]);
            if (it.jewelType == type) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 转换为客户端可以识别的格式：
     *     打孔数                byte
     *     扩展最大孔数          byte
     *     宝石数                byte
     *         宝石孔位          byte
     *         宝石图标          byte
     *         宝石名称          String
     *         宝石属性          byte
     *         加属性值          short（最高4位用来表示宝石级别）
     * @return
     */
    public byte[] toClientBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(addHole);
            dos.writeByte(addMaxHole);
            dos.writeByte(jewels.length / 2);
            for (int i = 0; i < jewels.length; i += 2) {
                dos.writeByte(jewels[i]);
                int itemID = jewels[i + 1];
                ItemTemplate template = ObjectAccessor.getItemTemplate(itemID);
                dos.write(template.showType);
                dos.writeUTF(template.name);
                dos.writeByte(template.jewelAttrType);
                dos.writeShort(template.jewelAttrValue | (template.useLevel << 12));
            }
            dos.write(star);
            dos.write(naturals.length);
            for(NaturalEnhance ne:naturals){
            	dos.write(ne.level);
            	dos.write(ne.attType);
            	dos.writeShort(ne.value);
            	dos.write(ne.percent);
            }
            dos.writeUTF(markString);
            dos.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
    
    protected void enhance0(PropertyCalculator pc, boolean withBasicAttrs,
			int attType, int value) {
		switch (attType) {
		case AttributeCalculator.ATTRIBUTE_STR:
			if (withBasicAttrs) {
			    pc.strength += value;
			}
			break;
		case AttributeCalculator.ATTRIBUTE_AGI:
			if (withBasicAttrs) {
				pc.agility += value;
			}
			break;
		case AttributeCalculator.ATTRIBUTE_STA:
			if (withBasicAttrs) {
				pc.stamina += value;
			}
			break;
		case AttributeCalculator.ATTRIBUTE_INT:
			if (withBasicAttrs) {
				pc.intellect += value;
			}
			break;
		case AttributeCalculator.ATTRIBUTE_HP:
			pc.hp += value;
			break;
		case AttributeCalculator.ATTRIBUTE_MP:
			pc.mp += value;
			break;
		case AttributeCalculator.ATTRIBUTE_CRIT:
			pc.criticalrating += value;
			pc.spellcriticalrating += value;
			break;
		case AttributeCalculator.ATTRIBUTE_HIT:
			pc.hitrating += value;
			pc.spellhitrating += value;
			break;
		case AttributeCalculator.ATTRIBUTE_DODGE:
			pc.dodgerating += value;
			break;
		case AttributeCalculator.ATTRIBUTE_MAGICDODGE:
			pc.spelldodgerating += value;
			break;
		case AttributeCalculator.ATTRIBUTE_ATTACKPOWER:
			pc.attackpowerup += value;
			pc.attackpowerdown += value;
			break;
		case AttributeCalculator.ATTRIBUTE_MAGICPOWER:
			pc.spellpower += value;
			pc.spellheal += value;
			break;
		case AttributeCalculator.ATTRIBUTE_ARMOR:
			pc.defense += value;
			break;
		case AttributeCalculator.ATTRIBUTE_MAGICARMOR:
			pc.spelldefense += value;
			break;
		case AttributeCalculator.ATTRIBUTE_HPRENEW:
			pc.healthrestore += value;
			break;
		case AttributeCalculator.ATTRIBUTE_MPRENEW:
			pc.manarestore += value;
			break;
		case AttributeCalculator.ATTRIBUTE_SPEED:
			if (withBasicAttrs) {
				pc.speed += value;
			}
			break;
		case AttributeCalculator.ATTRIBUTE_ANTICRIT:
			pc.anticritrating += value;
			break;
		}
	}
    
     /**
     * 把宝石效果附加到人物属性上。
     * @param pc
     */
    public void enhance(PropertyCalculator pc, boolean withBasicAttrs) {
        for (int i = 0; i < jewels.length; i += 2) {
            int itemID = jewels[i + 1];
            ItemTemplate template = ObjectAccessor.getItemTemplate(itemID);
            int jewelValue = template.jewelAttrValue;
            float fv = jewelValue *(1f+getJewelsEnhance()/1000f+pc.jewelEnhance);
            jewelValue = Math.round(fv);
            enhance0(pc,withBasicAttrs,template.jewelAttrType,jewelValue);
        }
        
        for(int i=0;i<naturals.length;i++){
        	int naturalsValue = naturals[i].value;
    		float nv = naturalsValue * (1f+getNaturalsEnhance()/1000f+pc.natualEnhance);
    		naturalsValue = Math.round(nv);
        	enhance0(pc,withBasicAttrs,naturals[i].attType,naturalsValue);
        }
    }
    
    public static ItemEnhance fromDBBytes(DataInputStream dis,GameItem owner) {
        try {
            byte version = dis.readByte();
            ItemEnhance ret = new ItemEnhance();
            if (version == 1) {
				ret.addHole = dis.readInt();
				ret.addMaxHole = dis.readInt();
				int jewelCount = dis.readInt();
				ret.jewels = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
				}
			}else if(version==2){
				ret.addHole = dis.readInt();
				ret.addMaxHole = dis.readInt();
				int jewelCount = dis.readInt();
				ret.jewels = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
				}
				ret.star = dis.read();
				int neLen = dis.read();
				ret.naturals = new NaturalEnhance[neLen];
				for(int i=0;i<neLen;i++){
					int neLevel = dis.read();
					int neAttType = dis.read();
					int nePercent = dis.read();
					int addedValue = owner.getNatureEnhanceAttribute(neAttType, nePercent, ret.star);
					NaturalEnhance ne = new NaturalEnhance(neLevel,neAttType,nePercent,addedValue);
					ret.naturals[i] = ne;
				}
			}else if(version==3){
				ret.addHole = dis.readInt();
				ret.addMaxHole = dis.readInt();
				int jewelCount = dis.readInt();
				ret.jewels = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
				}
				ret.star = dis.read();
				int neLen = dis.read();
				ret.naturals = new NaturalEnhance[neLen];
				for(int i=0;i<neLen;i++){
					int neLevel = dis.read();
					int neAttType = dis.read();
					int nePercent = dis.read();
					int addedValue = owner.getNatureEnhanceAttribute(neAttType, nePercent, ret.star);
					NaturalEnhance ne = new NaturalEnhance(neLevel,neAttType,nePercent,addedValue);
					ret.naturals[i] = ne;
				}
				ret.markString = dis.readUTF();
			} else if(version == 4){
				ret.addHole = dis.readInt();
				ret.addMaxHole = dis.readInt();
				int jewelCount = dis.readInt();
				ret.jewels = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
				}
				ret.star = dis.read();
				int neLen = dis.read();
				ret.naturals = new NaturalEnhance[neLen];
				for(int i=0;i<neLen;i++){
					int neLevel = dis.read();
					int neAttType = dis.read();
					int nePercent = dis.read();
					int addedValue = owner.getNatureEnhanceAttribute(neAttType, nePercent, ret.star);
					NaturalEnhance ne = new NaturalEnhance(neLevel,neAttType,nePercent,addedValue);
					ret.naturals[i] = ne;
				}
				ret.markString = dis.readUTF();
				int size = dis.readInt();
				ret.equipEnhanceData = new int[size];
				for(int i=0;i<ret.equipEnhanceData.length;i++){
					ret.equipEnhanceData[i] = dis.readInt();
				}
			}
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public byte[] toDBBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(4);
            dos.writeInt(addHole);
            dos.writeInt(addMaxHole);
            dos.writeInt(jewels.length / 2);
            for (int i = 0; i < jewels.length; i++) {
                dos.writeInt(jewels[i]);
            }
            dos.write(star);
            dos.write(naturals.length);
            for(NaturalEnhance ne:naturals){
            	dos.write(ne.level);
            	dos.write(ne.attType);
            	dos.write(ne.percent);
            }
            dos.writeUTF(markString);
            dos.writeInt(equipEnhanceData.length);
            for(int i=0;i<equipEnhanceData.length;i++){
            	dos.writeInt(equipEnhanceData[i]);
            }
            dos.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
    
    public String getDesc() {
        return null;
    }
    
    public Class<? extends Marshaller> marshallerClass() {
        return ItemEnhancePersistence.class;
    }

    public Class<? extends Serializer> serializerClass() {
        return ItemEnhancePersistence.class;
    }
    
    public String logString(){
    	StringBuilder sb = new StringBuilder(200);
    	sb.append("[STAR[").append(star).append("]HOLE[").append(addHole).append("]MAXHOLE[").append(addMaxHole).append("]");
    	if(jewels.length>0){
    		sb.append("JEWELS[");
    		for(int i=0;i<jewels.length;i+=2){
    			sb.append(jewels[i+1]);
    			sb.append(",");
    		}
    		sb.append("]");
    	}
    	if(naturals!=null&&naturals.length>0){
    		sb.append("NATURAL[");
    		for(int i=0;i<naturals.length;i++){
    			sb.append(naturals[i].attType);
    			sb.append(',');
    			sb.append(naturals[i].percent);
    			sb.append(',');
    		}
    		sb.append("]");
    	}
    	sb.append("]");
    	return sb.toString();
    }
    
    @Override
	public ItemEnhance clone(){
    	ItemEnhance ret = new ItemEnhance();
    	ret.addHole = addHole;
    	ret.addMaxHole = addMaxHole;
    	ret.jewels = new int[jewels.length];
    	System.arraycopy(jewels, 0, ret.jewels, 0, jewels.length);
    	ret.star = star;
    	ret.naturals = new NaturalEnhance[naturals.length];
    	for(int i=0;i<naturals.length;i++){
    		ret.naturals[i] = naturals[i].clone();
    	}
    	ret.markString = markString;
    	for(int i=0;i<equipEnhanceData.length;i++){
    		ret.equipEnhanceData[i] = equipEnhanceData[i];
    	}
    	return ret;
    }
    
    /**
	 * 把对象添加到一个日志字符串中。
	 */
	public void dump(StringBuilder out) {
		// 星级
		boolean isStart = true;
		if (star > 0) {
			out.append("S=").append(star);
			isStart = false;
		}
		
		// 打孔
		if (addHole > 0) {
			if (!isStart) {
				out.append(",");
			}
			out.append("H=").append(addHole);
			isStart = false;
		}
		
		// 扩展最大孔
		if (addMaxHole > 0) {
			if (!isStart) {
				out.append(",");
			}
			out.append("MH=").append(addMaxHole);
			isStart = false;
		}
		
		// 已镶嵌宝石
		if (jewels.length > 0) {
			if (!isStart) {
				out.append(",");
			}
			out.append("JEW=");
			for (int i = 0; i < jewels.length; i += 2) {
				if (i > 0) {
					out.append("+");
				}
				out.append(jewels[i + 1]);
			}
			isStart = false;
		}
		
		// 资质鉴定
		if (naturals != null && naturals.length > 0) {
			if (!isStart) {
				out.append(",");
			}
			out.append("NR=");
    		for (int i = 0; i < naturals.length; i++) {
    			if (i > 0) {
					out.append("+");
				}
    			out.append(naturals[i].attType).append("/").append(naturals[i].percent);
    		}
    		isStart = false;
    	}
		
		// 刻字
		if (markString != null && markString.length() > 0) {
			if (!isStart) {
				out.append(",");
			}
			out.append("MK=");
			out.append(LogUtil.filter(markString));
			isStart = false;
		}
	}
}
