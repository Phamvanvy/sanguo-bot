package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import peony.game.changed.DurationChangedItem;
import peony.game.itemenhance.ItemEnhance;


public class GameItem implements PropertyEnhancer{
	
	public static final int GENERAL_INSTANCEID = -1;
	
	public ItemTemplate template;
//	public int bind;
//	public boolean bind;
	public int bindInstance = -1;
	public int leaveUseCount;
	public int validTime;
	public int instanceId;
	public int duration;
	public GameItemObject object;
	
	public GameItem(ItemTemplate template){
		this(template,GENERAL_INSTANCEID);
	}
	
	
	public boolean isBound(){
		return bindInstance!=-1;
	}
	
	public GameItem(ItemTemplate template,int instanceId){
		this.template = template;
		this.instanceId = instanceId;
		if(template.bindType==ItemTemplate.BIND_REWARD) bindInstance = 0;
		if(template.isEquipment()){
			this.duration = template.equipment.duration;
		}
	}
	
	@Override
	public GameItem clone(){
		GameItem ret = new GameItem(template,instanceId);
		ret.bindInstance = bindInstance;
		ret.leaveUseCount = leaveUseCount;
		ret.validTime = validTime;
		ret.instanceId = instanceId;
		if(object!=null){
			ret.object = object.clone();
		}
		return ret;
	}
	
	public static final byte[] toClientBytes(ItemTemplate template){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(template.toClientBytes(null));
			if(template.useType.canUse){
				dos.write(0);
			}
			dos.writeInt(0);
			dos.writeInt(-1);
			if(template.isEquipment()){
				dos.writeShort(template.equipment.duration);
				dos.writeShort(0);
			}
			dos.writeInt(-1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();		
	}
	
	public byte[] toClientBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			if(object!=null&&object instanceof ItemEnhance){
				dos.write(template.toClientBytes((ItemEnhance)object));
			}else{
				dos.write(template.toClientBytes(null));
			}
			if (template.useType.canUse) {
				dos.write(leaveUseCount);
			}
			dos.writeInt(validTime);
			if(template.isHorseEquipment() && bindInstance>0){
				bindInstance = 0;
			}
			if(template.isHorseEquipment() && template.bindType==ItemTemplate.BIND_REWARD){
				bindInstance = 0;
			}
			dos.writeInt(bindInstance);
//			dos.write(bind ? 1 : 0);
			if (template.isEquipment()) {
				dos.writeShort(duration);
				byte[] enh = new byte[0];
				if (object != null && object instanceof ItemEnhance) {
				    enh = ((ItemEnhance)object).toClientBytes();
				}
				dos.writeShort(enh.length);
				dos.write(enh);
			}
			dos.writeInt(instanceId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof GameItem){
			GameItem other = (GameItem)o;
			if(other.template==template&&instanceId==other.instanceId)
				return true;
			return false;
		}else{
			return false;
		}
	}
	
	public void enhance(PropertyCalculator pc){
		if(template.isEquipment()){
			if(template.hasDuration()&&duration==0)
				return;
			EquipmentTemplate et = template.equipment;
			ItemEnhance ie = (ItemEnhance)object;
			pc.hp += et.getMaxHP(ie);
			pc.mp += et.getMaxMP(ie);
			pc.strength += et.getSTR(ie);
			pc.agility += et.getAGI(ie);
			pc.stamina += et.getSTA(ie);
			pc.intellect += et.getINT(ie);
			pc.attackpowerup += et.getAttackPower(ie);
			pc.attackpowerdown += et.getAttackPower(ie);
			pc.spellpower += et.getMagicPower(ie);
			pc.spellheal += et.getMagicPower(ie);
			pc.defense += et.getArmor(ie);
			pc.spelldefense += et.getMagicArmor(ie);
			pc.hitrating += et.getHit(ie);
			pc.spellhitrating += et.getHit(ie);
			pc.dodgerating += et.getDodge(ie);
			pc.spelldodgerating += et.getMagicDodge(ie);
			pc.criticalrating += et.getCrit(ie);
			pc.spellcriticalrating += et.getCrit(ie);
			pc.attackpowerup += et.getMaxAttack(ie);
			pc.attackpowerdown += et.getMinAttack(ie);
			pc.healthrestore += et.getHpRenew(ie);
			pc.manarestore += et.getMpRenew(ie);
			pc.speed += et.getSpeed(ie);
			pc.anticritrating += et.getAnticrit(ie);
			
			// 处理宝石附魔
			if (object != null && object instanceof ItemEnhance) {
			    ((ItemEnhance)object).enhance(pc, true);
			}
		}
	}
	
	public void enhanceWithOutBasicAttrs(PropertyCalculator pc){
        if(template.isEquipment()){
            if(template.hasDuration()&&duration==0)
                return;
            EquipmentTemplate et = template.equipment;
            ItemEnhance ie = (ItemEnhance)object;
            pc.hp += et.getMaxHP(ie);
            pc.mp += et.getMaxMP(ie);
            pc.attackpowerup += et.getAttackPower(ie);
            pc.attackpowerdown += et.getAttackPower(ie);
            pc.spellpower += et.getMagicPower(ie);
            pc.spellheal += et.getMagicPower(ie);
            pc.defense += et.getArmor(ie);
            pc.spelldefense += et.getMagicArmor(ie);
            pc.hitrating += et.getHit(ie);
            pc.spellhitrating += et.getHit(ie);
            pc.dodgerating += et.getDodge(ie);
            pc.spelldodgerating += et.getMagicDodge(ie);
            pc.criticalrating += et.getCrit(ie);
            pc.spellcriticalrating += et.getCrit(ie);
            pc.attackpowerup += et.getMaxAttack(ie);
            pc.attackpowerdown += et.getMinAttack(ie);
            pc.healthrestore += et.getHpRenew(ie);
            pc.manarestore += et.getMpRenew(ie);
            pc.speed += et.getSpeed(ie);
            pc.anticritrating += et.getAnticrit(ie);

            // 处理宝石附魔
            if (object != null && object instanceof ItemEnhance) {
                ((ItemEnhance)object).enhance(pc, false);
            }
        }
    }
	
	public int getNatureEnhanceAttribute(int pro, int ratio){
		int star = 0;
		if(object != null){
			star = ((ItemEnhance)object).getStar();
		}
		return getNatureEnhanceAttribute(pro,ratio,star);
	}
	
	public int getNatureEnhanceAttribute(int pro, int ratio, int star){
		if(!template.isEquipment())
			return 0;
		int userLevel = template.equipment.useLevel;
		if(userLevel==1)
			userLevel = template.equipment.level;
		return ItemUtil.naturalEnhanceValue(pro, ratio, userLevel, star, template.equipment.equ.quality ,template.equipment.equ.extraQuality);
	}
	
//	/**
//	 * 计算资质鉴定后提升的装备属性值
//	 * @param ratio 资质鉴定提高比例
//	 * @param attributeValue 装备属性值
//	 */
//	public int getNatureEnhanceAttribute(int ratio, float attributeValue){
//		if(!template.isEquipment())
//			return 0;
//		int value;
//		int userLevel = template.equipment.useLevel;
//		if(userLevel==1)
//			userLevel = template.equipment.level;
//		if(object==null){
//			value = (int) (userLevel*720*0.1f
//					*template.equipment.equ.getQualityValue());
//		} else {
//			value = (int) ((userLevel + ((ItemEnhance) object).getStar() * 3) * 720 * 0.1f *  template.equipment.equ
//					.getQualityValue());
//		}
//		value = (int) (value*(ratio/100.0f));
//		value = (int) (value*0.2f/attributeValue);
//		return value;
//	}
	
	public int getRepairMoney(){
//		装备价格/该装备耐久度上限*0.9
		if(!template.isEquipment()){
			return 0;
		}else{
			if(template.equipment.duration<=0)
				return 0;
			int needRep = template.equipment.duration - duration;
			if (needRep <= 0) {
			    return 0;
			}
			int ret = (int)(template.price * needRep /(template.equipment.duration*0.9));
			if (ret == 0) {
			    ret = 1;
			}
			return ret;
		}
	}
	
	public void repair(Unit owner) {
	    int maxDuration = template.equipment.duration;
        if (maxDuration > 0 && duration < maxDuration) {
            duration = maxDuration;
            DurationChangedItem changedItem = new DurationChangedItem(this);
            owner.changed.addChangedItem(changedItem);
        }
	}
	
	public String getDesc(){
		if (object!=null) {
			String desc = object.getDesc();
			if (desc != null) {
			    return desc;
			}
		}
		return template.desc;
	}

	// TODO: 下面三个要考虑星级鉴定和资质鉴定的结果
	public int getMinAttack() {
	    return template.equipment.getMinAttack((ItemEnhance)object);
	}
	
	public int getMaxAttack() {
	    return template.equipment.getMaxAttack((ItemEnhance)object);
	}
	
	public int getMagicPower() {
	    return template.equipment.getMagicPower((ItemEnhance)object);
	}
}
