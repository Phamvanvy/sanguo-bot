package com.pip.itimes.server.world.ItemGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Element;
import org.hibernate.cache.ReadWriteCache.Item;

import com.pip.itimes.server.util.Utils;

public class ItemData {
	/**
	 * 针对所有人需要刷新的物品
	 * KEY：物品ID
	 * VALUE:商品详细信息
	 */
	private Map<Integer, ItemInfo> refreshAll = new HashMap<Integer, ItemInfo>();
	/**
	 * 针对个人需要刷新的物品
	 * KEY：物品ID
	 * VALUE:商品详细信息
	 */
	private Map<Integer, ItemInfo> refreshOne = new HashMap<Integer, ItemInfo>();
	/**
	 * 角色购买信息
	 * KEY：角色ID
	 * VALUE：HashMap
	 * 	KEY：物品ID
	 *  Value：购买信息
	 */
	private Map<Integer, ConcurrentHashMap<Integer, ItemBuyInfo>> playerbuy = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ItemBuyInfo>>();
	
	public void addOneItem(ItemInfo itemInfo){
		refreshOne.put(new Integer(itemInfo.getItemID()), itemInfo);
	}
	
	public void addAllItem(ItemInfo itemInfo){
		refreshAll.put(new Integer(itemInfo.getItemID()), itemInfo);
	}
	
	/**
	 * 添加购买信息到HashMap中
	 * @param playerid
	 * @param itemBuyInfo
	 */
	public byte addBuyInfo(int playerid, ItemBuyInfo itemBuyInfo){
		synchronized (this) {
			if(refreshAll.containsKey(new Integer(itemBuyInfo.getItemID()))){
				ItemInfo itemInfo = refreshAll.get(new Integer(itemBuyInfo.getItemID()));
				if(itemInfo != null){
					int count = getAllCount(itemInfo.getItemID());
					if(count == 0){
						return ItemConstants.BUY_TYPE_ALLCOUNT_ZERO;
					}
					if(count < itemBuyInfo.getCount()){
						return ItemConstants.BUY_TYPE_ALLCOUNT_NOTENOUGH;
					}
				}
			}
			if(refreshOne.containsKey(new Integer(itemBuyInfo.getItemID()))){
				ItemInfo itemInfo = refreshOne.get(new Integer(itemBuyInfo.getItemID()));
				if(itemInfo != null){
					int count = getOneCount(playerid, itemInfo.getItemID());
					if(count == 0){
						return ItemConstants.BUY_TYPE_ONECOUNT_ZERO;
					}
					if(count < itemBuyInfo.getCount()){
						return ItemConstants.BUY_TYPE_ONECOUNT_NOTENOUGH;
					}
				}
			}
			if(playerbuy.containsKey(playerid)){
				ConcurrentHashMap<Integer, ItemBuyInfo> playerBuyInfo = playerbuy.get(new Integer(playerid));
				if(playerBuyInfo.containsKey(new Integer(itemBuyInfo.getItemID()))){
					ItemBuyInfo itemBuyInfoTemp = playerBuyInfo.get(new Integer(itemBuyInfo.getItemID()));
					itemBuyInfoTemp.setCount(itemBuyInfoTemp.getCount() + itemBuyInfo.getCount());
					itemBuyInfoTemp.setLastTimer(itemBuyInfo.getLastTimer());
				}else{
					playerBuyInfo.put(new Integer(itemBuyInfo.getItemID()), itemBuyInfo);
				}
			}else{
				ConcurrentHashMap<Integer, ItemBuyInfo> playerBuyInfo = new ConcurrentHashMap<Integer, ItemBuyInfo>();
				playerBuyInfo.put(new Integer(itemBuyInfo.getItemID()), itemBuyInfo);
				playerbuy.put(new Integer(playerid), playerBuyInfo);
			}
			//全局的个数需要设置
			if(refreshAll.containsKey(new Integer(itemBuyInfo.getItemID()))){
				ItemInfo itemInfo = refreshAll.get(new Integer(itemBuyInfo.getItemID()));
				itemInfo.setCount(itemInfo.getCount() - itemBuyInfo.getCount());
			}
			return ItemConstants.BUY_TYPE_OK;
		}
	}
	
	public void refresh(byte refreshType, long now, ItemGroup itemGroup){
		refreshData(refreshAll.entrySet().iterator(), refreshType, now, itemGroup);
		refreshData(refreshOne.entrySet().iterator(), refreshType, now, itemGroup);
	}
	
	public void refreshData(Iterator iter, byte refreshType, long now, ItemGroup itemGroup){
		if(iter == null) return;
		while(iter.hasNext()){
			Entry e = (Entry)iter.next();
			int itemid = (Integer)e.getKey();
			ItemInfo itemInfo = (ItemInfo)e.getValue();
			switch(itemInfo.getRefreshType()){
			case ItemConstants.REFRESHTYPE_DAY:
				itemInfo.setTimer(now);
				refreshPlayerBuy(itemInfo.getItemID());
				itemInfo.setCount(itemGroup.getItemInfo().get(new Integer(itemInfo.getItemID())).getCount());
				break;
			case ItemConstants.REFRESHTYPE_WEEK:
				long useTime = now - itemInfo.getTimer();
				if(useTime > ItemConstants.WEEK_MILLS){
					itemInfo.setTimer(now);
					refreshPlayerBuy(itemInfo.getItemID());
					itemInfo.setCount(itemGroup.getItemInfo().get(itemInfo.getItemID()).getCount());
				}
				break;
//			case ItemInfo.REFRESHTYPE_MONTH:
//				useTime = now - itemInfo.getTimer();
//				if(useTime > ItemGroup.MONTH_MILLS){
//					itemInfo.setTimer(now);
//					refreshPlayer(itemInfo.getItemID());
//				}
//				break;
			}
		}
	}
	
	/**
	 * 刷新玩家购买的物品（删除）
	 * @param itemid
	 */
	public void refreshPlayerBuy(int itemid){
		Iterator iter = playerbuy.entrySet().iterator();
		while(iter.hasNext()){
			Entry e = (Entry)iter.next();
			int playerid = (Integer)e.getKey();
			Map<Integer, ItemBuyInfo> itemBuyInfoMap = (ConcurrentHashMap<Integer, ItemBuyInfo>)e.getValue();
			if(itemBuyInfoMap != null){
				if(itemBuyInfoMap.containsKey(new Integer(itemid))){
					itemBuyInfoMap.remove(new Integer(itemid));
				}
			}
		}
	}
	
	public void save(Element element){
		if(refreshAll.size() > 0){
			Element elementAll = element.addElement("AllRefresh");
			Iterator iter = refreshAll.values().iterator();
			while(iter.hasNext()){
				ItemInfo itemInfo = (ItemInfo)iter.next();
				if(itemInfo != null){
					Element elementItem = elementAll.addElement("Item");
					elementItem.addAttribute("itemid", "" + itemInfo.getItemID());
					elementItem.addAttribute("timer", "" + itemInfo.getTimer());
					elementItem.addAttribute("count", "" + itemInfo.getCount());
				}
			}
		}
		if(refreshOne.size() > 0){
			Element elementOne = element.addElement("OneRefresh");
			Iterator iter = refreshOne.values().iterator();
			while(iter.hasNext()){
				ItemInfo itemInfo = (ItemInfo)iter.next();
				if(itemInfo != null){
					Element elementItem = elementOne.addElement("Item");
					elementItem.addAttribute("itemid", "" + itemInfo.getItemID());
					elementItem.addAttribute("timer", "" + itemInfo.getTimer());
					elementItem.addAttribute("count", "" + itemInfo.getCount());
				}
			}
		}
		if(playerbuy.size() > 0){
			Iterator iter = playerbuy.entrySet().iterator();
			while(iter.hasNext()){
				Entry e = (Entry)iter.next();
				int playerid = (Integer)e.getKey();
				Map<Integer, ItemBuyInfo> itemBuyInfoMap = (ConcurrentHashMap<Integer, ItemBuyInfo>)e.getValue();
				if(itemBuyInfoMap != null && itemBuyInfoMap.size() > 0){
					Element elementPlayerBuy = element.addElement("PlayerBuy");
					elementPlayerBuy.addAttribute("playerid", "" + playerid);
					Element elementItemBuyInfo = elementPlayerBuy.addElement("ItemBuyInfos");
					Iterator iterItemBuyInfo = itemBuyInfoMap.values().iterator();
					while(iterItemBuyInfo.hasNext()){
						ItemBuyInfo itemBuyInfo = (ItemBuyInfo)iterItemBuyInfo.next();
						Element elementItem = elementItemBuyInfo.addElement("Item");
						elementItem.addAttribute("itemid", "" + itemBuyInfo.getItemID());
						elementItem.addAttribute("buycount", "" + itemBuyInfo.getCount());
						elementItem.addAttribute("startTime", "" + itemBuyInfo.getStartTimer());
						elementItem.addAttribute("lastTime", "" + itemBuyInfo.getLastTimer());
					}
				}
			}
		}
	}
	
	public void load(Element element){
		if(refreshAll.size() > 0){
			Element elementAll = element.element("AllRefresh");
			if(elementAll != null){
				for(Iterator iter = elementAll.elementIterator("Item"); iter.hasNext();){
					Element elementItem = (Element)iter.next();
					int itemid = Integer.parseInt(elementItem.attributeValue("itemid"));
					long timer = Long.parseLong(elementItem.attributeValue("timer"));
					int count = Integer.parseInt(elementItem.attributeValue("count"));
					if(refreshAll.containsKey(new Integer(itemid))){
						ItemInfo itemInfo = refreshAll.get(new Integer(itemid));
						if(itemInfo != null){
							itemInfo.setTimer(timer);
							itemInfo.setCount(count);
						}
					}
				}
			}
		}
		if(refreshOne.size() > 0){
			Element elementOne = element.element("OneRefresh");
			if(elementOne != null){
				for(Iterator iter = elementOne.elementIterator("Item"); iter.hasNext();){
					Element elementItem = (Element)iter.next();
					int itemid = Integer.parseInt(elementItem.attributeValue("itemid"));
					long timer = Long.parseLong(elementItem.attributeValue("timer"));
					int count = Integer.parseInt(elementItem.attributeValue("count"));
					if(refreshOne.containsKey(new Integer(itemid))){
						ItemInfo itemInfo = refreshOne.get(new Integer(itemid));
						if(itemInfo != null){
							itemInfo.setTimer(timer);
							itemInfo.setCount(count);
						}
					}
				}
			}
		}
		for(Iterator<Element> iterPlayerBuy = element.elementIterator("PlayerBuy"); iterPlayerBuy.hasNext();){
			Element elementPlayerBuy = iterPlayerBuy.next();
			int playerid = Integer.parseInt(elementPlayerBuy.attributeValue("playerid"));
			for(Iterator<Element> iter = elementPlayerBuy.elementIterator("ItemBuyInfos"); iter.hasNext(); ){
				Element el = (Element)iter.next();
				ConcurrentHashMap<Integer, ItemBuyInfo> itemBuyInfoMap;
				boolean putFlag = false;
				if(playerbuy.containsKey(new Integer(playerid))){
					itemBuyInfoMap = playerbuy.get(new Integer(playerid));
				}else{
					itemBuyInfoMap = new ConcurrentHashMap<Integer, ItemBuyInfo>();
					putFlag = true;
				}
				for(Iterator iterItemBuyInfo = el.elementIterator("Item"); iterItemBuyInfo.hasNext();){
					Element itemBuyInfo = (Element)iterItemBuyInfo.next();
					int itemid = Integer.parseInt(itemBuyInfo.attributeValue("itemid"));
					int buycount = Integer.parseInt(itemBuyInfo.attributeValue("buycount"));
					long startTimer = Long.parseLong(itemBuyInfo.attributeValue("startTime"));
					long lastTimer = Long.parseLong(itemBuyInfo.attributeValue("lastTime"));
					ItemBuyInfo buyInfo;
					if(itemBuyInfoMap.containsKey(new Integer(itemid))){
						buyInfo = itemBuyInfoMap.get(new Integer(itemid));
						buyInfo.setCount(buycount);
						buyInfo.setStartTimer(startTimer);
						buyInfo.setLastTimer(lastTimer);
					}else{
						buyInfo = new ItemBuyInfo();
						buyInfo.setCount(buycount);
						buyInfo.setStartTimer(startTimer);
						buyInfo.setLastTimer(lastTimer);
						itemBuyInfoMap.put(new Integer(itemid), buyInfo);
					}
				}
				if(putFlag){
					playerbuy.put(new Integer(playerid), itemBuyInfoMap);
				}
			}
		}
	}
	
	/**
	 * 获得全局共享的物品个数
	 * @param playerid
	 * @param itemid
	 * @return
	 */
	public int getAllCount(int itemid){
		if(refreshAll.containsKey(new Integer(itemid))){
			ItemInfo itemInfo = refreshAll.get(new Integer(itemid));
			if(itemInfo != null){
				return itemInfo.getCount();
			}
		}
		return 0;
	}
	/**
	 * 获得独享个数的角色指定物品可购个数
	 * @param playerid
	 * @param itemid
	 * @return
	 */
	public int getOneCount(int playerid, int itemid){
		if(refreshOne.containsKey(itemid)){
			Map<Integer, ItemBuyInfo> map = playerbuy.get(new Integer(playerid));
			if(map != null && map.containsKey(new Integer(itemid))){
				ItemInfo itemInfo = refreshOne.get(new Integer(itemid));
				ItemBuyInfo itemBuyInfo = map.get(new Integer(itemid));
				if(itemBuyInfo == null){
					return itemInfo.getCount();
				}else{
					return itemInfo.getCount() - itemBuyInfo.getCount();
				}
			}else if(map == null || !map.containsKey(new Integer(itemid))){
				ItemInfo itemInfo = refreshOne.get(new Integer(itemid));
				return itemInfo.getCount();
			}
		}
		return 0;
	}
	
	public HashMap<Integer, ItemInfo> getPlayerItem(int playerid, ItemGroup itemGroup){
		HashMap<Integer, ItemInfo> map = new HashMap<Integer, ItemInfo>();
		for(Iterator<ItemInfo> iter = itemGroup.getItemInfo().values().iterator(); iter.hasNext();){  
			ItemInfo itemInfo = iter.next();
			ItemInfo itemInfoCopy = itemInfo.copy();
			switch(itemInfoCopy.getCountType()){
			case ItemConstants.COUNTTYPE_AllCOUNT:
				itemInfoCopy.setCount(getAllCount(itemInfoCopy.getItemID()));
				break;
			case ItemConstants.COUNTTYPE_ONECOUNT:
				itemInfoCopy.setCount(getOneCount(playerid, itemInfoCopy.getItemID()));
				break;
			default:
				//其它个数型 设置个数为-1 不限个数
				itemInfoCopy.setCount(-1);
			}
			map.put(new Integer(itemInfoCopy.getItemID()), itemInfoCopy);
		}
		return map;
	}
	
}
