package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

import peony.game.itemeffect.QuestItemEffect;
import peony.game.itemenhance.ItemEnhance;

import com.pip.sanguo.data.item.Item;


public class ItemTemplate {
	
	
	public static final int BIND_NO = 1;
	public static final int BIND_USED = 2;
	public static final int BIND_REWARD = 3;
	
	public int id;
	public String name;
	public int itemType;
	public int extraItemType;
	public int maxCount;
	public int showImage;
	public int showType;
	public boolean isTaskItem;
	public boolean isTaskCopy;
	public UseType useType;
	public ItemValid itemValid;
	public int level;
	public int useLevel;
	public int quality;
	public int price;
	public int bindType;
	public String desc;
	public boolean newInstance;
	public boolean canSale;
	public int jewelAttrType;         // 宝石附加属性类型
	public int jewelAttrValue;        // 宝石附加属性值
	public int jewelType;             // 宝石分类
	public boolean isHorseJewel;      // 是否坐骑宝石
	public boolean isFlaw;            // 宝石是否有瑕疵
	public QuestItemEffect triggerQuest;     // 触发任务的物品对应的效果，特殊处理，null表示没有
	public String produceArea = "";//产地
	
	public boolean canLevelUp;//是否可升级
	
	public int nextLevelEquipID;//下级装备ID
	
	public int equipLevelUpPriceType;//装备升级消耗类型
	
	public int equipLevelUpPriceNum;//装备升级消耗数量
	
	public EquipmentTemplate equipment;

	public void copyFrom(ItemTemplate oo) {
	    id = oo.id;
	    name = oo.name;
	    extraItemType = oo.extraItemType;
	    maxCount = oo.maxCount;
	    showImage = oo.showImage;
	    showType = oo.showType;
	    isTaskItem = oo.isTaskItem;
        isTaskCopy = oo.isTaskCopy;
        useType = oo.useType;
        itemValid = oo.itemValid;
        level = oo.level;
        useLevel = oo.useLevel;
        quality = oo.quality;
        price = oo.price;
        bindType = oo.bindType;
        desc = oo.desc;
        newInstance = oo.newInstance;
        canSale = oo.canSale;
        isFlaw = oo.isFlaw;
        equipment = oo.equipment;
        
        canLevelUp=oo.canLevelUp;
        nextLevelEquipID=oo.nextLevelEquipID;
        equipLevelUpPriceType=oo.equipLevelUpPriceType;
        equipLevelUpPriceNum=oo.equipLevelUpPriceNum;
	}
	
	public boolean isEquipment(){
		return equipment!=null;
	}
	
	public boolean isHorseEquipment(){
		return equipment!=null&&equipment.isHorseEquipment();
	}
	
	public boolean hasDuration(){
		if(equipment==null)
			return false;
		return equipment.duration!=0;
	}
	
	public byte[] toClientBytes(ItemEnhance ie){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(id);
			dos.writeUTF(name);
			dos.write(maxCount);
			dos.write(showImage);
			dos.writeShort(((showType<<2|bindType)&0xFFFF));
			dos.write(useLevel);
			boolean hasEffect = equipment!=null&&equipment.hasEffect();
			dos.write(quality|((hasEffect?1:0)<<7));
			dos.writeInt(price);
			byte[] useBytes = useType.toClientBytes();
			useBytes[0]|=(isTaskItem?1:0)<<7;
			dos.write(useBytes);
			if(equipment!=null){
				dos.write(Item.TYPE_EQUIP);
				dos.write(equipment.toClientBytes(ie));
			}else{
				dos.write(itemType);
			}
			dos.write(extraItemType);
		} catch (IOException e) {
		}
		return baos.toByteArray();
	}
	
	//物品由坐骑打包而成,增加合成信息
	public byte[] toClientBytesHorse(Horse horse){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(id);
			
			String itemName = name;
			if(horse.fixCount > 0){
				itemName = MessageFormat.format("{0} +{1}", itemName, horse.fixCount);
			}
			dos.writeUTF(itemName);
			dos.write(maxCount);
			dos.write(showImage);
			dos.writeShort(((showType<<2|bindType)&0xFFFF));
			dos.write(useLevel);
			boolean hasEffect = equipment!=null&&equipment.hasEffect();
			dos.write(quality|((hasEffect?1:0)<<7));
			dos.writeInt(price);
			byte[] useBytes = useType.toClientBytes();
			useBytes[0]|=(isTaskItem?1:0)<<7;
			dos.write(useBytes);
			if(equipment!=null){
				dos.write(Item.TYPE_EQUIP);
				dos.write(equipment.toClientBytes(null));
			}else{
				dos.write(itemType);
			}
			dos.write(extraItemType);
		} catch (IOException e) {
		}
		return baos.toByteArray();
	}
}
