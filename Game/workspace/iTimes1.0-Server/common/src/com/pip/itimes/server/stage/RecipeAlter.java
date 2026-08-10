package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * @author wpjiang
 * 此类用于采集，锻造的额外附加
 * 
 *采集的配置地图上id， 采集的资源id号，配置掉落组。
 *
 * 锻造配倍数，用于锻造是熟练度提高
 *
 */
public class RecipeAlter {
	
	/**
	 * 锻造的配方id, 获取熟练度的倍数
	 */
	private static Map<Integer, Integer>  productMap= new HashMap<Integer, Integer>();
	
	
	/**
	 * 配置的采集修正组
	 */
	private static Set<GatherAlter> gatherSet = new HashSet<GatherAlter>();
	
	/**
	 * @param type  类型
	 * @param id    id号
	 * @param count 翻倍数量
	 * 增加修正方案
	 */
	public static void addGatherAlter(int type, int id , int count){
		GatherAlter gatherAlter = new GatherAlter(type, id, count);
		gatherSet.add(gatherAlter);
	}
	
	/**
	 * 清理修正组
	 */
	public static void clearGatherAlter(){
		gatherSet.clear();
	}
	
	
	/**
	 * @param recipeId 配方id
	 * @param skillpointCount 技能熟练度提升倍数
	 */
	public static void addProduct(int id, int count){
		productMap.put(id, count);
	}
	
	/**
	 * @param recipeId
	 * @return获得配方的熟练程度
	 */
	public static int getProductSkillPointCount(int recipeId){
		int recipeSkillId = 1;
		if(productMap.containsKey(recipeId)){
			recipeSkillId = productMap.get(recipeId); 
		}
		return recipeSkillId;
	}
	
	/**
	 * 锻造配方清除
	 */
	public static void clearProductMap(){
		productMap.clear();
	}
	
	/**
	 * @param mapId
	 * @param gatherId
	 * @return按照mapid和资源点id来获取对应的倍数
	 */
	public static int getGatherSkillPointCount(int mapId, int gatherId){
		int gatherCount = 1;
		Iterator<GatherAlter> gatherIterator = gatherSet.iterator();
		while(gatherIterator.hasNext()){
			GatherAlter  gatherAlter = gatherIterator.next();
			//如果是地图号的话，类型必须是1， 资源号的话，类型必须是2
			if((gatherAlter.getId() == mapId &&  gatherAlter.getType() == GatherAlter.GATHER_MAP)
					|| (gatherAlter.getId() == gatherId && gatherAlter.getType() == GatherAlter.GATHER_RESOURCE)){
				gatherCount = gatherAlter.getCount();
				break;
			}
			
		}
		
		return gatherCount;
	}
}




