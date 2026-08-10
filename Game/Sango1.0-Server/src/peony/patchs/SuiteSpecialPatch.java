package peony.patchs;

import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.buff.BuffUtil;

public class SuiteSpecialPatch implements Runnable {

	public void run() {
		ItemTemplate item = ObjectAccessor.getItemTemplate(1008192);
		if(item!=null && item.isEquipment()){
			item.equipment.specialEffect = BuffUtil.createSuiteBuff(item.equipment.equ.buffID, item.equipment.equ.buffLevel);
			System.out.println("___________________OK"+item.equipment.equ.buffID);
		}
		item = ObjectAccessor.getItemTemplate(1008193);
		if(item!=null && item.isEquipment()){
			item.equipment.specialEffect = BuffUtil.createSuiteBuff(item.equipment.equ.buffID, item.equipment.equ.buffLevel);
			System.out.println("___________________OK"+item.equipment.equ.buffID);
		}
		item = ObjectAccessor.getItemTemplate(1008302);
		if(item!=null && item.isEquipment()){
			item.equipment.specialEffect = BuffUtil.createSuiteBuff(item.equipment.equ.buffID, item.equipment.equ.buffLevel);
			System.out.println("___________________OK"+item.equipment.equ.buffID);
		}
		item = ObjectAccessor.getItemTemplate(1008311);
		if(item!=null && item.isEquipment()){
			item.equipment.specialEffect = BuffUtil.createSuiteBuff(item.equipment.equ.buffID, item.equipment.equ.buffLevel);
			System.out.println("___________________OK"+item.equipment.equ.buffID);
		}
		item = ObjectAccessor.getItemTemplate(1006960);
		if(item!=null && item.isEquipment()){
			item.equipment.specialEffect = BuffUtil.createSuiteBuff(item.equipment.equ.buffID, item.equipment.equ.buffLevel);
			System.out.println("___________________OK"+item.equipment.equ.buffID);
		}
	}

}
