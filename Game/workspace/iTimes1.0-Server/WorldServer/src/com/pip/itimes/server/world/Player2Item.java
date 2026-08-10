package com.pip.itimes.server.world;


import java.util.HashMap;

import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.Pet;
public class Player2Item {

	private HashMap items = new HashMap();
	private HashMap pets = new HashMap();
	
	/**
	 * 保存玩家的物品的信息
	 * @param itemID
	 * @param grid
	 */
	public void setItem(int itemID,IItem item, int level){
		synchronized(this){
			PlayerItemInfo info = new PlayerItemInfo();
			info.item = item;
			info.level = level;
			items.put(itemID, info);
		}
	}
	/**
	 * 保存玩家的宠物的信息
	 * @param petId
	 * @param pet
	 */
	public void setPet(int petId,Pet pet){
		synchronized(this){
			pets.put(petId, pet);
		}
	}
	/**
	 * 获得玩家的物品的信息
	 * @param itemID
	 * @return
	 */
	public PlayerItemInfo getItem(int itemID){
		return (PlayerItemInfo)items.get(itemID);
	}
	/**
	 * 获得玩家的宠物的信息
	 * @param petId
	 * @return
	 */
	public Pet getPet(int petId){
		return (Pet)pets.get(petId);
	}
}
