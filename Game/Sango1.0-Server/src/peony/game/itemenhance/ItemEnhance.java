package peony.game.itemenhance;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.MessageFormat;
import peony.game.ChatOption;
import peony.game.GameItem;
import peony.game.GameItemObject;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.Marshaller;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PropertyCalculator;
import peony.game.Serializer;
import peony.game.Server;
import peony.game.attendant.Attendant;
import peony.game.chat.ChatMessage;
import peony.game.chat.ItemChatAttachment;
import peony.service.cards.CardService;
import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.equipment.AttributeCalculator;

/**
 * 装备强化信息，包含宝石镶嵌信息以及将来的其他物品强化数据。
 * @author lighthu
 */
public class ItemEnhance implements GameItemObject {
	
	/** 打孔数 */
    public int addHole;
    
    /** 扩展最大孔数 */
    public int addMaxHole;
    
    /** 卡槽数 */
    public int addCardHole;
    
    /** 扩展最大卡槽数 */
    public int addMaxCardHole;
    
    /** 各孔上打的宝石，每2个INT表示一个宝石，其中第一个表示孔位（0表示第一个），第二个表示物品ID */
    protected int[] jewels = new int[0];
    
    /** 宝石升级数据：每2个INT表示一个宝石，其中第一个表示孔位（0表示第一个），第二个表示宝石分数（1为1/5） */
    public int[] jewelUpgrades = new int[0];
    
    /** 各卡槽上打的卡片，每2个INT表示一个卡片，其中第一个表示卡槽位（0表示第一个），第二个表示卡片ID */
    public int[] cards = new int[0];
    
    /** 各卡槽上打的卡片品质，每2个INT表示一个卡片，其中第一个表示卡槽位（0表示第一个），第二个表示卡片品质 */
    public int[] cardQualitys = new int[0];
    
    /** 各卡槽上打的卡片buff生效时间，每2个INT表示一个卡片，其中第一个表示卡槽位（0表示第一个），第二个表示卡片buff生效时间 */
    public long[] cardBuffTimes = new long[0];
    
    /** 星级 */
    protected int star;
    
    /** 称号系统星级增加系数 */
    public static float starCoefficient = 1f;
    
    /** 资质 */
    protected NaturalEnhance[] naturals = new NaturalEnhance[0];
    
    /** 4种装备强化 */
    public int[] equipEnhanceData = {0,0,0,0};
    
    /** 刻字 */
    public String markString="";
    
    /** 镶嵌本等级的宝石后不能丢弃或买卖 */
    public static int CANT_REMOVE_LEVEL = 4;
    
    /** 宝石升级最高级别 */
    public static int JEWEL_TOP_LEVEL = 4;
    
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
     * 查询宝石索引。
     * @param hole 孔位（0表示第一个）
     * @return 宝石索引, -1表示没有
     */
    public int getJewelIndexByHole(int hole) {
        for (int i = 0; i < jewels.length; i += 2) {
            if (jewels[i] == hole) {
                return i; 
            }
        }
        return -1;
    }
    
    /**
     * 查询某个卡槽上的卡片。
     * @param hole 卡槽（0表示第一个）
     * @return 卡槽ID, -1表示没有
     */
    public int getCard(int hole) {
    	for (int i = 0; i < cards.length; i += 2) {
    		if (cards[i] == hole) {
    			return cards[i + 1];
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
        for (int i = 0; i < jewelUpgrades.length; i += 2) {
        	if (jewelUpgrades[i] == hole) {
        		int[] newarr = new int[jewelUpgrades.length - 2];
        		System.arraycopy(jewelUpgrades, 0, newarr, 0, i);
        		System.arraycopy(jewelUpgrades, i + 2, newarr, i, jewelUpgrades.length - i - 2);
        		jewelUpgrades = newarr;
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
        
        int[] newarr1 = new int[jewelUpgrades.length + 2];
        System.arraycopy(jewelUpgrades, 0, newarr1, 0, jewelUpgrades.length);
        newarr1[jewelUpgrades.length] = hole;
        newarr1[jewelUpgrades.length + 1] = 0;
        jewelUpgrades = newarr1;
    }
    
    /**
     * 镶嵌卡片。
     * @param hole 卡槽位（0表示第一个）
     */
    public void addCard(int hole, int cardId, int quality) {
    	if(getCard(hole)>-1){
    		int index = 0;
    		for(int i=0;i<cards.length;i+=2){
    			if(cards[i]==hole){
    				index = i;
    				break;
    			}
    		}
    		cards[index+1] = cardId;
    		cardQualitys[index+1] = quality;
    		cardBuffTimes[index+1] = System.currentTimeMillis();
    	}else{
	    	int[] newarr = new int[cards.length + 2];
	    	System.arraycopy(cards, 0, newarr, 0, cards.length);
	    	newarr[cards.length] = hole;
	    	newarr[cards.length + 1] = cardId;
	    	cards = newarr;
	    	
	    	int[] newarr1 = new int[cardQualitys.length + 2];
	    	System.arraycopy(cardQualitys, 0, newarr1, 0, cardQualitys.length);
	    	newarr1[cardQualitys.length] = hole;
	    	newarr1[cardQualitys.length + 1] = quality;
	    	cardQualitys = newarr1;
	    	
	    	long[] newarr2 = new long[cardBuffTimes.length + 2];
	    	System.arraycopy(cardBuffTimes, 0, newarr2, 0, cardBuffTimes.length);
	    	newarr2[cardBuffTimes.length] = hole;
	    	newarr2[cardBuffTimes.length + 1] = System.currentTimeMillis();
	    	cardBuffTimes = newarr2;
    	}
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
     * 查找是否已经镶嵌了>=某一等级的宝石
     * @param level
     * @return
     */
    public boolean findJewelByLevel(int level){
    	 for (int i = 0; i < jewels.length; i += 2) {
             ItemTemplate it = ObjectAccessor.getItemTemplate(jewels[i + 1]);
             if (it.useLevel >= 4) {
                 return true;
             }
         }
    	return false;
    }
    
    public int getJewelGradeByHole(int hole){
    	int grade = 0;
    	try {
			grade = jewelUpgrades[getUpgradesIndexByHole(hole)+1];
		} catch (Exception e) {
		}
		return grade;
    }
    
    public void upgradeJewel(Player player,GameItem equipItem,int hole){
    	int grade = getJewelGradeByHole(hole);
    	String oldToken = "";
    	if(grade>0){
    		oldToken = "+"+String.valueOf(grade);
    	}
    	grade++;
    	int itemId = getJewel(hole);
		GameItem gi = ObjectAccessor.createGameItem(itemId);
		int jewelValue = gi.template.jewelAttrValue;
		String name = gi.template.name;
    	if(grade==JEWEL_TOP_LEVEL){
    		JewelService service = Server.server.getServiceRegistry().getJewelService();
    		ItemTemplate nextLevelItem = service.jewels[gi.template.jewelAttrType][gi.template.useLevel];
    		jewels[getUpgradesIndexByHole(hole)+1] = nextLevelItem.id;
    		jewelUpgrades[getUpgradesIndexByHole(hole)+1] = 0;
    		jewelValue = nextLevelItem.jewelAttrValue;
    		name = nextLevelItem.name;
    		
    		ItemChatAttachment attItem = new ItemChatAttachment(equipItem);
            if(nextLevelItem.useLevel==6){
            	String s = MessageFormat.format(peony.Messages.STRING_01983, player.getFactionName(),player.name,equipItem.template.name,gi.template.name,nextLevelItem.name);
//            	Server.server.getServiceRegistry().getChatService().sendSystemMessage(ChatOption.WORLD, "系统", s+"~"+String.valueOf(player.id));
            	ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1,peony.Messages.STRING_00004, s, attItem);
	    		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
            }
            else if(nextLevelItem.useLevel==7){
            	String s = MessageFormat.format(peony.Messages.STRING_01984, player.getFactionName(),player.name,equipItem.template.name,gi.template.name,nextLevelItem.name);
//            	Server.server.getServiceRegistry().getChatService().sendSystemMessage(ChatOption.WORLD, "系统", s+"~"+String.valueOf(player.id));
            	ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1,peony.Messages.STRING_00004, s, attItem);
	    		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
            }
    	}else{
    		jewelUpgrades[getUpgradesIndexByHole(hole)+1] = grade;
    	}
    	int index = getJewelIndexByHole(hole);
    	if(index!=-1){
	    	int newValue = getPropertyValue(jewelValue,player,index);
	    	String addLevel = "";
	    	if(jewelUpgrades[getUpgradesIndexByHole(hole)+1]!=0){
	    		addLevel = "+"+String.valueOf(jewelUpgrades[getUpgradesIndexByHole(hole)+1]);
	    	}
	    	String jewelShortName = AttributeCalculator.ATTRIBUTES[gi.template.jewelAttrType].shortName;
	    	String msg = MessageFormat.format("恭喜您，{0}{1}已经提升至{2}{3}，{4}变为了{5}！", gi.template.name,oldToken,name,addLevel,jewelShortName,newValue);
	    	player.message(-1, msg, -1, -1);
    	}
    }
    
    /**
     * 宝石的属性值
     * @param jewelValue
     * @param player
     * @param hole
     * @return
     */
    public int getPropertyValue(int jewelValue,Player player,int hole){
    	PropertyCalculator pc = new PropertyCalculator(player);
        float fv = jewelValue;
        fv += getUpgradeJewelValue(hole, pc);
    	return Math.round(fv);
    }
    
    public int getUpgradesIndexByHole(int hole){
    	 for (int i = 0; i < jewelUpgrades.length; i += 2) {
             if (jewelUpgrades[i] == hole) {
                 return i;
             }
         }
         return -1;
    }
    
    /**
     * 转换为客户端可以识别的格式
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
                dos.writeInt(itemID);
                ItemTemplate template = ObjectAccessor.getItemTemplate(itemID);
                dos.write(template.showType);
                dos.writeUTF(template.name);
                dos.writeByte(template.jewelAttrType);
                int upgradeValue = Math.round(getUpgradeJewelValue(i, null));
                dos.writeShort((short)(template.jewelAttrValue + upgradeValue) | (template.useLevel << 12));
                int jewelUpgrade = 0;
                try {
					jewelUpgrade = jewelUpgrades[i+1];
				} catch (Exception e) {
				}
				dos.writeByte(jewelUpgrade);
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
            dos.writeByte(addCardHole); //卡槽数
            dos.writeByte(addMaxCardHole); //最大卡槽数
            dos.writeByte(cards.length / 2); //附魔的卡片数量
            for(int i=0;i<cards.length;i+=2){
            	dos.writeByte(cards[i+1]); //卡片ID
            	CardService service = Server.server.getServiceRegistry().getCardService();
            	Card card = service.getCardByCardId(cards[i+1]);
            	int cardItemId = card.itemId;
            	ItemTemplate cardTemplate = ObjectAccessor.getItemTemplate(cardItemId);
            	dos.writeByte(cardTemplate.showType); //卡片图标
            	dos.writeUTF(card.title); //卡片名称
            	int buffLevel = cardQualitys[i+1]==0 ? card.buffLevel1 : card.buffLevel2;
            	dos.writeUTF(service.getBuffDesc(card.id, buffLevel)); //卡片效果描述
            	long buffDuration = card.buffDuration * 3600 * 1000L;
            	long leaving = System.currentTimeMillis() - cardBuffTimes[i+1];
            	if(buffDuration>leaving)
            		dos.writeInt(Math.round((buffDuration-leaving)/1000)); //卡片效果剩余时间（秒）
            	else
            		dos.writeInt(0);
            }
            dos.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
    
    protected float getUpgradeJewelValue(int hole,PropertyCalculator pc){
    	JewelService service = Server.server.getServiceRegistry().getJewelService();
    	int itemID = jewels[hole + 1];
        ItemTemplate template = ObjectAccessor.getItemTemplate(itemID);
        ItemTemplate nextJewelTemplate = null;
        try {
			nextJewelTemplate = service.jewels[template.jewelAttrType][template.useLevel];
		} catch (Exception e1) {}
        int upgradeJewelCount = 0;
        try {
			upgradeJewelCount = jewelUpgrades[hole+1];
		} catch (Exception e) {}
        float fv = template.jewelAttrValue *(1f+getJewelsEnhance()/1000f);
        if(pc!=null)
        	fv = template.jewelAttrValue *(1f+getJewelsEnhance()/1000f + pc.jewelEnhance + pc.playerJewelEnhance + pc.horseJewelEnhance);
        float nextFv = 0;
        try {
			nextFv = nextJewelTemplate.jewelAttrValue *(1f+getJewelsEnhance()/1000f);
			if(pc!=null)
				nextFv = nextJewelTemplate.jewelAttrValue *(1f+getJewelsEnhance()/1000f + pc.jewelEnhance + pc.playerJewelEnhance + pc.horseJewelEnhance);
		} catch (Exception e) {}
		float upgradeValue = ((nextFv-fv)/4f)*upgradeJewelCount;
		return upgradeValue>0 ? upgradeValue : 0;
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
      * @param withBasicAttrs
      * @param ownerType, 装备所属主人类型
      * @param ownerInstanceId,装备主人instanceId
      */
    public void enhance(PropertyCalculator pc, boolean withBasicAttrs) {
        for (int i = 0; i < jewels.length; i += 2) {
            int itemID = jewels[i + 1];
            ItemTemplate template = ObjectAccessor.getItemTemplate(itemID);
            int jewelValue = template.jewelAttrValue;
//            float fv = jewelValue *(1f+getJewelsEnhance()/1000f+pc.jewelEnhance);
            float fv = jewelValue *(1f+getJewelsEnhance()/1000f+pc.jewelEnhance+pc.playerJewelEnhance+pc.horseJewelEnhance);
            fv += getUpgradeJewelValue(i, pc);
            if(pc.unit instanceof Attendant)
            	fv *= PropertyCalculator.attendantValueRatio;
            jewelValue = Math.round(fv);
            enhance0(pc,withBasicAttrs,template.jewelAttrType,jewelValue);
        }
        for(int i=0;i<naturals.length;i++){
        	int naturalsValue = naturals[i].value;
    		float nv = naturalsValue * (1f+getNaturalsEnhance()/1000f+pc.natualEnhance);
    		naturalsValue = Math.round(nv);
    		if(pc.unit instanceof Attendant)
    			naturalsValue *= PropertyCalculator.attendantValueRatio;
        	enhance0(pc,withBasicAttrs,naturals[i].attType,naturalsValue);
        }
        
//        for(int i=0;i<cards.length;i+=2){
//        	int cardId = cards[i + 1];
//            CardService service = Server.server.getServiceRegistry().getCardService();
//            if(pc.unit instanceof Attendant)
//            	service.addCardBuff(((Attendant)pc.unit).owner, cardId, pc.ownerType, pc.ownerInstanceId, pc.gameItemId, pc.gameItemInstanceId);
//            else if(pc.unit instanceof Player){
//            	if(pc.subOwnerType==PropertyCalculator.TYPE_HORSE)
//            		service.addCardBuff((Player)pc.unit, cardId, pc.subOwnerType, pc.subOwnerInstanceId, pc.gameItemId, pc.gameItemInstanceId);
//            	else
//            		service.addCardBuff((Player)pc.unit, cardId, pc.ownerType, pc.ownerInstanceId, pc.gameItemId, pc.gameItemInstanceId);
//            }
//        }
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
				ret.jewelUpgrades = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
					if(i%2==0)
						ret.jewelUpgrades[i] = ret.jewels[i];
					else
						ret.jewelUpgrades[i] = 0;
				}
			}else if(version==2){
				ret.addHole = dis.readInt();
				ret.addMaxHole = dis.readInt();
				int jewelCount = dis.readInt();
				ret.jewels = new int[jewelCount * 2];
				ret.jewelUpgrades = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
					if(i%2==0)
						ret.jewelUpgrades[i] = ret.jewels[i];
					else
						ret.jewelUpgrades[i] = 0;
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
				ret.jewelUpgrades = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
					if(i%2==0)
						ret.jewelUpgrades[i] = ret.jewels[i];
					else
						ret.jewelUpgrades[i] = 0;
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
				ret.jewelUpgrades = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
					if(i%2==0)
						ret.jewelUpgrades[i] = ret.jewels[i];
					else
						ret.jewelUpgrades[i] = 0;
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
			} else if(version == 5){
				ret.addHole = dis.readInt();
				ret.addMaxHole = dis.readInt();
				int jewelCount = dis.readInt();
				ret.jewels = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
				}
				ret.jewelUpgrades = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewelUpgrades[i] = dis.readInt();
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
			} else if(version == 6){
				ret.addHole = dis.readInt();
				ret.addMaxHole = dis.readInt();
				int jewelCount = dis.readInt();
				ret.jewels = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewels[i] = dis.readInt();
				}
				ret.jewelUpgrades = new int[jewelCount * 2];
				for (int i = 0; i < ret.jewels.length; i++) {
					ret.jewelUpgrades[i] = dis.readInt();
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
				ret.addCardHole = dis.readInt();
				ret.addMaxCardHole = dis.readInt();
				int cardsCount = dis.readInt();
				ret.cards = new int[cardsCount * 2];
				for (int i = 0; i < ret.cards.length; i++) {
					ret.cards[i] = dis.readInt();
				}
				ret.cardQualitys = new int[cardsCount * 2];
				for(int i = 0; i < ret.cardQualitys.length; i++){
					ret.cardQualitys[i] = dis.readInt();
				}
				ret.cardBuffTimes = new long[cardsCount * 2];
				for(int i = 0; i < ret.cardBuffTimes.length; i++){
					ret.cardBuffTimes[i] = dis.readLong();
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
            dos.writeByte(6);
            dos.writeInt(addHole);
            dos.writeInt(addMaxHole);
            dos.writeInt(jewels.length / 2);
            for (int i = 0; i < jewels.length; i++) {
                dos.writeInt(jewels[i]);
            }
            for (int i = 0; i < jewelUpgrades.length; i++) {
                dos.writeInt(jewelUpgrades[i]);
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
            dos.writeInt(addCardHole);
            dos.writeInt(addMaxCardHole);
            dos.writeInt(cards.length / 2);
            for (int i = 0; i < cards.length; i++) {
                dos.writeInt(cards[i]);
            }
            for (int i = 0; i < cardQualitys.length; i++) {
            	dos.writeInt(cardQualitys[i]);
            }
            for (int i = 0; i < cardBuffTimes.length; i++) {
            	dos.writeLong(cardBuffTimes[i]);
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
    	if(jewelUpgrades.length>0){
    		sb.append("JEWELUPGRADE[");
    		for(int i=0;i<jewelUpgrades.length;i+=2){
    			sb.append(jewelUpgrades[i+1]);
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
    	ret.addCardHole = addCardHole;
    	ret.addMaxCardHole = addMaxCardHole;
    	ret.jewels = new int[jewels.length];
    	System.arraycopy(jewels, 0, ret.jewels, 0, jewels.length);
    	ret.jewelUpgrades = new int[jewelUpgrades.length];
    	System.arraycopy(jewelUpgrades, 0, ret.jewelUpgrades, 0, jewelUpgrades.length);
    	ret.cards = new int[cards.length];
    	System.arraycopy(cards, 0, ret.cards, 0, cards.length);
    	ret.cardQualitys = new int[cardQualitys.length];
    	System.arraycopy(cardQualitys, 0, ret.cardQualitys, 0, cardQualitys.length);
    	ret.cardBuffTimes = new long[cardBuffTimes.length];
    	System.arraycopy(cardBuffTimes, 0, ret.cardBuffTimes, 0, cardBuffTimes.length);
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
		if (jewelUpgrades.length > 0) {
			if (!isStart) {
				out.append(",");
			}
			out.append("JEWUPGRADE=");
			for (int i = 0; i < jewelUpgrades.length; i += 2) {
				if (i > 0) {
					out.append("+");
				}
				out.append(jewelUpgrades[i + 1]);
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
