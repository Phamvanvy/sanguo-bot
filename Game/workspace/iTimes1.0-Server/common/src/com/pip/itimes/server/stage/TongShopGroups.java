package com.pip.itimes.server.stage;

import java.util.Collection;
import java.util.LinkedHashMap;

public class TongShopGroups {
	private static int TS_MAGIC_USE_MAX; //每天在公会商店中消费魔法i币的上限
	private static LinkedHashMap<Integer,LinkedHashMap<String,IStoreGroup>> groups = new LinkedHashMap<Integer,LinkedHashMap<String,IStoreGroup>>();
	private static LinkedHashMap<Integer, String> tongShopNames = new LinkedHashMap<Integer, String>();
	public TongShopGroups(){
		
	}
	
	public static void addShop(int id,String name){
		Integer tmp = new Integer(id);
		groups.put(tmp, new LinkedHashMap<String,IStoreGroup>());
		tongShopNames.put(tmp, name);
	}
	
	public static void addGroup(int id,String name,IStoreGroup group){
		LinkedHashMap<String,IStoreGroup> map = getShop(id);
		if(map != null){
			map.put(group.getName(),group);
		}else{
			map = new LinkedHashMap<String,IStoreGroup>();
			map.put(group.getName(), group);
			Integer tmp = new Integer(id);
			groups.put(tmp,map);
			tongShopNames.put(tmp, name);
		}
    }
	
	public static Collection<IStoreGroup> getGroups(int id){
		LinkedHashMap<String,IStoreGroup> map = getShop(id);
		if(map!=null){
			return map.values();
		}else{
			return null;
		}
    }
	
	public static LinkedHashMap<String,IStoreGroup> getShop(int id){
		return groups.get(new Integer(id));
	}
	
	public static IStoreGroup getGroup(int id, String name){
		LinkedHashMap<String,IStoreGroup> map = getShop(id);
		if(map!=null){
			return (IStoreGroup)map.get(name);
		}else{
			return null;
		}
    }
	
	public static IStoreItem getShopItem(int id,String name,int itemId){
		IStoreGroup tmpGroup = getGroup(id,name);
		 for(IStoreItem item:tmpGroup.getItems()){
             if(item.item.getItemId()==itemId)
                 return item;
         }
		return null;
	}
	
	public static String getShopName(int id){
		return tongShopNames.get(new Integer(id));
	}
	
	public static Collection<String> getShopNames(){
		return tongShopNames.values();
	}
	
	public static void setUseMax(int value){
		TS_MAGIC_USE_MAX = value;
	}
	
	public static int getMagicUseMax(){
		return TS_MAGIC_USE_MAX;
	}
}
