package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author wpjiang
 *	宝石镶嵌类。里面包含了镶嵌的几率
 */
public class DiamondMosaic {
	
	/**
	 * 宝石合成最小数量
	 */
	public final static int minDiamondMix = 3;
	/**
	 * 宝石合成最大数量
	 */
	public final static int maxDiamondMix = 5;
	/**
	 * 宝石合成成功几率
	 */
	public final static short[] diamondMixSuccessRate = new short[]{30, 70, 100};
	
	/**
	 * 最小的宝石级别
	 */
	public final static int minDiamondLevel = 1;
	public final static byte max_diamondCount = 7;
	
	public final static byte canNotDiamond = -1;
	
	/**
	 * 可以进行宝石升级的最低等级
	 */
	public final static byte minAutoMixLevel = 3;
	/**
	 * 可以进行宝石升级的最高等级
	 */
	public final static byte maxAutoMixLevel = 5;
	/**
	 * 默认宝石摘除的使用物品的默认等级
	 */
	public final static byte throwDiamondMosaicNeedItemLevel = 0;
	
	/**
	 * 默认宝石摘除的使用物品的默认等级
	 */
	public final static byte throwDiamondMosaicNeedItemMaxLevel = 7;
	
	 /**
	 * 存放宝石的成功率 对应的物品id, 和宝石镶嵌类
	 */
	protected static Map<Integer,DiamondMosaic> diamondMosaicMap = new HashMap<Integer,DiamondMosaic>();
	
	
	/**
	 * 用于宝石合成所需物品合成符, 数组1为宝石等级，数组2为物品id
	 */
	protected static Vector<Integer[]> dimondMosaicNeedItemVector = new Vector<Integer[]>();
	
	
	public static Vector<Integer[]> getDimondMosaicNeedItemMap() {
		return dimondMosaicNeedItemVector;
	}
	
	public static void addDiamondMosaicNeedItem(Integer[] needItem){
		dimondMosaicNeedItemVector.add(needItem);
	}
	/**
	 * 清空合成符的定义
	 */
	public static void clearDiamondMosaicNeedItemMap(){
		dimondMosaicNeedItemVector.clear();
	}
	/**
	 * 清空宝石加载
	 */
	public static void clearDiamondMosaicMap(){
		diamondMosaicMap.clear();
	}
	
	/**
	 * 用于打孔符, 数组1为物品装备等级i, 数组2为d
	 */
	protected static Vector<Integer[]> dimondMosaicRoleNeedItemVector = new Vector<Integer[]>();
	
	
	public static Vector<Integer[]> getDimondMosaicRoleNeedItemMap() {
		return dimondMosaicRoleNeedItemVector;
	}
	
	public static void addDiamondMosaicRoleNeedItem(Integer[] needItem){
		dimondMosaicRoleNeedItemVector.add(needItem);
	}
	/**
	 * 清空打孔符的定义
	 */
	public static void clearDiamondMosaicRoleNeedItemMap(){
		dimondMosaicRoleNeedItemVector.clear();
	}
	
	/**
	 * 摘除符的定义
	 */
	protected static Map<Integer, Integer> diamondMosaicThrowNeedItemMap = new HashMap<Integer, Integer>();
	
	public static Map<Integer, Integer> getDiamondMosaicThrowNeedItemMap(){
		return diamondMosaicThrowNeedItemMap;
	}
	
	public static void addDiamondMosaicThrowNeedItem(int needItemId, int level){
		diamondMosaicThrowNeedItemMap.put(level, needItemId);
	}
	
	/**
	 * 清除摘除符
	 */
	public static void clearDiamondMosaicThrowItemMap(){
		diamondMosaicThrowNeedItemMap.clear();
	}
	
	public DiamondMosaic(int itemId, byte property, byte diamondLevel, short addPoint, boolean canUse){
		this.itemId = itemId;
		this.property = property;
		this.diamondLevel = diamondLevel;
		this.addPoint = addPoint;
		this.canUse = canUse;
	}
	
	/**
	 * 添加一个宝石镶嵌类
	 * @param diamondMosaic
	 */
	public static void addDiamondMosaicMap(DiamondMosaic diamondMosaic){
		diamondMosaicMap.put(diamondMosaic.itemId, diamondMosaic);
	}
	
	/**
	 * @param itemId 请求获得合成符
	 * @return 如果已经是最高级别了，返回-1, 找不到返回0
	 */
	public static int  findNextDiamondMosaicProductItemId(int itemId){
		int itemIdReturn = 0;
		//获得物品属性，还有等级
		DiamondMosaic diamondMosaic = diamondMosaicMap.get(itemId);
		if(diamondMosaic != null){
			if(diamondMosaic.getDiamondLevel() == max_diamondCount){//达到了最高级别
				itemIdReturn = canNotDiamond;
			}else{//获得物品界别
				byte itemLevel = diamondMosaic.getDiamondLevel();
				
				for(Map.Entry<Integer, DiamondMosaic> dia: diamondMosaicMap.entrySet()){
					if(diamondMosaic.getProperty() == dia.getValue().getProperty()
							&& (diamondMosaic.getDiamondLevel() + 1) == dia.getValue().getDiamondLevel() && diamondMosaic.isCanUse() == dia.getValue().isCanUse()){
						itemIdReturn = dia.getValue().getItemId();
					}
				}
			}
		}
		return itemIdReturn;
	}
	
	/**
	 *@level为指定等级
	 *@return相同类型指定等级宝石id 
	 */
	public static int findSameProHigerItemId(int itemId,int level){
		int itemIdReturn = 0;
		//获得物品属性，还有等级
		DiamondMosaic diamondMosaic = diamondMosaicMap.get(itemId);
		if(diamondMosaic != null){
			if(diamondMosaic.getDiamondLevel() == max_diamondCount){//达到了最高级别
				itemIdReturn = canNotDiamond;
			}else{//获得物品界别
				byte itemLevel = diamondMosaic.getDiamondLevel();
				if(level > itemLevel){//合成比自身等级高
					for(Map.Entry<Integer, DiamondMosaic> dia: diamondMosaicMap.entrySet()){
						if(diamondMosaic.getProperty() == dia.getValue().getProperty()
								&& level == dia.getValue().getDiamondLevel() && diamondMosaic.isCanUse() == dia.getValue().isCanUse()){
							itemIdReturn = dia.getValue().getItemId();
						}
					}
					
				}
			}
		}
		return itemIdReturn;
	}
	
	/**
	 * @param itemId 
	 * @return 请求获得打孔符
	 */
	public static int  findNextDiamondMosaicRoleItemId(int itemLevel){
		int itemIdReturn = 0;
		for(int i = 0; i < dimondMosaicRoleNeedItemVector.size(); i++){
			Integer[] needItem = dimondMosaicRoleNeedItemVector.get(i);
			if(itemLevel <= needItem[0]){
				itemIdReturn = needItem[1];
				break;
			}
		}
		return itemIdReturn;
	}
	
	/**
	 * @param itemLevel
	 * @return获取该宝石的摘除符定义
	 */
	public static int findDiamondMosaicThrowItemId(int itemLevel){
		return diamondMosaicThrowNeedItemMap.get(itemLevel);
	}
	
	/**
	 * @param itemLevel
	 * @return 
	 */
	public static int getDiamondMosaicThrowItemLevel(int itemId){
		int level = throwDiamondMosaicNeedItemLevel;
		for(Map.Entry<Integer, Integer> throwNeedItem: diamondMosaicThrowNeedItemMap.entrySet()){
			if(throwNeedItem.getValue() == itemId){
				level = throwNeedItem.getKey();
				break;
			}
		}
		return level;
	}
	/**
	 * @param orderItemId 目标宝石id
	 * @param itemId      使用的宝石id
	 * @param needItemId  需要的合成符Id
	 * @return 获取宝石合成下发的字符串
	 */
	public static String getDiamondMosaicProductString(int orderItemId, int itemId, int needItemId, int count){
		StringBuffer returnString = new StringBuffer();
		returnString.append("你将合成");
		returnString.append(Items.getTemplate(orderItemId).getName());
		returnString.append(":投入");
		returnString.append(count);
		returnString.append("枚");
		returnString.append(Items.getTemplate(itemId).getName());
//		returnString.append("、");
//		returnString.append(Items.getTemplate(needItemId).getName());   2013年3月7日 删除合成符
		returnString.append("、");
		returnString.append(DiamondMosaic.getDiamondMosaicProductMoney(orderItemId));
		returnString.append("j金钱，成功率");
		returnString.append(diamondMixSuccessRate[count - minDiamondMix]);
		returnString.append("%; 合成失败后材料和金钱不返还，确定要合成吗？");
		return returnString.toString();
	}
	
	/**
	 * @return 获取一键合成宝石信息
	 */
	public static String getVipMixtureInfo(int orderitemid,int itemid,int itemcount,int Imoney){
		StringBuffer sb = new StringBuffer();
		sb.append("你将合成");
		sb.append(Items.getTemplate(orderitemid).getName());
		sb.append(":需要购买");
		if(itemcount > 0){
			sb.append(itemcount);
			sb.append("枚");
			sb.append(Items.getTemplate(itemid).getName());
			sb.append("。");
		}
		//2013年4月1日  去掉合成符
//		if(needitemcount > 0){
//			sb.append(needitemcount);
//			sb.append("个");
//			sb.append(Items.getTemplate(needitemid).getName());
//		}
		sb.append("总价");
		sb.append(Imoney);
		sb.append("i币，确定要合成吗?");
		return sb.toString();
	}
	
	/**
	 * @return获得打孔的确认信息
	 */
	public static String getDiamondMoasicRoleString(int equItemId, int itemId, byte roleInfo,int count,byte type){
		StringBuffer bufferString = new StringBuffer();
		bufferString.append("将在");
		bufferString.append(Items.getTemplate(equItemId).getName());
		bufferString.append("开启第");
		bufferString.append(roleInfo + 1);
		bufferString.append("个孔位，需要消耗" + count + "个");
		bufferString.append(Items.getTemplate(itemId).getName());
		bufferString.append("、");
		bufferString.append(getDiamondMosaicRoleMoney(equItemId, roleInfo));
		if(type == 1 && itemId != 201373){//几率打孔(极限打孔除外)
			bufferString.append("金钱;打孔有几率失败,确定吗？");
		}else{
			bufferString.append("金钱;确定吗？");
		}
		return bufferString.toString();
	}
	
	/**
	 * @return获得自动打孔的确认信息
	 */
	public static String getAutomaticPunchingString (int equItemId, int itemId, byte roleInfo){
		StringBuffer bufferString = new StringBuffer();
		bufferString.append("将在");
		bufferString.append(Items.getTemplate(equItemId).getName());
		bufferString.append("自动开启第");
		bufferString.append(roleInfo + 1);
		bufferString.append("个孔位，每次需要");
		bufferString.append(Items.getTemplate(itemId).getName());
		bufferString.append("、");
		bufferString.append(getDiamondMosaicRoleMoney(equItemId, roleInfo));
		bufferString.append("金钱，直到成功开启该孔位为止；打孔失败后装备无损、消耗的所有打孔符、金钱不返还；确定吗？");
		return bufferString.toString();
	}
	
	/**
	 * @param equItemId
	 * @param itemId
	 * @param roleInfo
	 * @return镶嵌的字符串
	 */
	public static String getDiamondMosaicEmbedString(int equItemId, int itemId, byte roleInfo){
		StringBuffer bufferString = new StringBuffer();
		bufferString.append("将");
		bufferString.append(Items.getTemplate(itemId).getName());
		bufferString.append("镶嵌在");
		bufferString.append(Items.getTemplate(equItemId).getName());
		bufferString.append("上，需要金钱");
		bufferString.append(getDiamondMosaicEmbedMoney(DiamondMosaic.getDiamondMosaicLevel(itemId)));
		bufferString.append("确定吗？");
		return bufferString.toString();
	}
	
	
	/**
	 * @param equItemId
	 * @param itemId        摘除符id
	 * @param roleInfo
	 * @param embedItemId 宝石id
	 * @return宝石摘除的字符串
	 */
	public static String getDiamondMosaicThrowString(int equItemId, int itemId, byte roleInfo, int embedItemId){
		StringBuffer bufferString = new StringBuffer();
		bufferString.append("将");
		bufferString.append(Items.getTemplate(equItemId).getName());
		bufferString.append("第");
		bufferString.append(roleInfo + 1);
		bufferString.append("个孔位摘除");
		bufferString.append(Items.getTemplate(embedItemId).getName());
		bufferString.append(", 需要");
//		bufferString.append(Items.getTemplate(itemId).getName());
//		bufferString.append(", ");   2013年3月7日 删除摘除符
		bufferString.append(getDiamondMosaicThrowMoney(getDiamondMosaicLevel(embedItemId)));
		bufferString.append("j金钱。确定吗？");
		
		return bufferString.toString();
	}
	
	/**
	 * @param itemId
	 * @return返回物品等级
	 */
	public static int getDiamondMosaicLevel(int itemId){
		int level = minDiamondLevel;
		DiamondMosaic diamondMosaic = diamondMosaicMap.get(itemId);
		if(diamondMosaic != null){
			level = diamondMosaic.getDiamondLevel();
		}
		return level;
	}
	/**
	 * @param itemId
	 * @return返回物品的金钱数
	 */
	public static int getDiamondMosaicProductMoney(int itemId){
		int money = 0;
		DiamondMosaic diamondMosaic = diamondMosaicMap.get(itemId);
		if(diamondMosaic != null){
			int itemLevel = diamondMosaic.getDiamondLevel();
			money = (int) (Math.pow(itemLevel, 3) * 150);
		}
		return money;
	}
	
	/**
	 * 打孔的成功概率
	 */
	//public final static short[] diamondRoleSuccessRate = new short[]{100, 100, 90, 20, 5, 4, 3, 2, 1};
	public final static short[] diamondRoleSuccessRate = new short[]{200, 200, 180, 40, 10, 8, 6, 4, 2};	
	
	/**
	 * @param itemId
	 * @return返回打孔的权属
	 */
	public static int getDiamondMosaicRoleMoney(int equItemId, byte roleInfo){
		int money = 0;
		
		int itemLevel = Items.getTemplate(equItemId).getLevel();
		money = itemLevel * itemLevel * (roleInfo + 1) / 2;
		return money;
	}
	
	/**
	 * @param itemId
	 * @return获取宝石等级
	 */
	public static byte findDiamondMosaicLevel(int itemId){
		byte level = 0;
		DiamondMosaic diamondMosaic = diamondMosaicMap.get(itemId);
		if(diamondMosaic != null){
			level = diamondMosaic.getDiamondLevel();
		}
		return level;
	}
	/**
	 * @param itemId
	 * @return查找宝石合成所需要的物品id
	 */
	public static int findDiamondNeedPoductItemId(int itemId){
		int itemIdReturn = 0;
		//获得物品属性，还有等级
		int itemLevel = findDiamondMosaicLevel(itemId);
		if(itemLevel != 0){
			for(int i = 0; i < dimondMosaicNeedItemVector.size(); i++){
				Integer[] needItem = dimondMosaicNeedItemVector.get(i);
				if(itemLevel <= needItem[0]){
					itemIdReturn = needItem[1];
					break;
				}
			}
		}
		return itemIdReturn;
		
	}
	public static Map<Integer, DiamondMosaic> getDiamondMosaicMap() {
		return diamondMosaicMap;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public byte getProperty() {
		return property;
	}

	public void setProperty(byte property) {
		this.property = property;
	}

	public byte getDiamondLevel() {
		return diamondLevel;
	}

	public void setDiamondLevel(byte diamondLevel) {
		this.diamondLevel = diamondLevel;
	}

	public short getAddPoint() {
		return addPoint;
	}

	public void setAddPoint(short addPoint) {
		this.addPoint = addPoint;
	}

	public boolean isCanUse() {
		return canUse;
	}

	public void setCanUse(boolean canUse) {
		this.canUse = canUse;
	}

	public short getDevelopAddPoint(){
		return this.developAddPoint;
	}
	
	public void setDevelopAddPoint(short addpoint){
		this.developAddPoint = addpoint;
	}
	
	public short getDevelopAddCount(){
		return this.developAddCount;
	}
	
	public void setDevelopAddCount(short addcount){
		this.developAddCount = addcount;
	}
	
	
	//这些是加载的定义
	/**
	 * 宝石物品id
	 */
	private int itemId;
	
	/**
	 * 宝石所要加的属性
	 */
	private byte property;
	
	/**
	 * 物品等级
	 */
	private byte diamondLevel;
	
	/**
	 * 增加的点数
	 */
	private short addPoint;
	
	/**
	 * 是否开放
	 */
	private boolean canUse;
	
	/**
	 * 宝石养成增加点数
	 */
	private short developAddPoint;
	
	
	/**
	 * 宝石养成增加宝石数
	 */
	private short developAddCount;
	
	
	//这些是每次镶嵌的定义
	/**
	 * 镶嵌的孔位
	 */
	private byte dimondOrder;  
	
	
	/**
	 * 镶嵌的宝石物品id
	 */
	private int equDiamondItemId;
	
	
	//镶嵌的定义开始
	public static int getDiamondMosaicEmbedMoney(int level){
		int money = 0;
		money = level * level * 100;
		return money;
	}
	
	/**
	 * @param level
	 * @return 宝石摘除需要的金钱
	 */
	public static int getDiamondMosaicThrowMoney(int level){
		int money = 0;
		money = level * level * 50;
		return money;
	}
	
	/**
	 * 获取该宝石的指定等级的itemID
	 * @param itemId 
	 * @param level
	 * @return
	 */
	public static int getDiamondMosaic_findLevel(int itemId, int level){
		if(level > max_diamondCount || level < minDiamondLevel){
			return canNotDiamond;
		}
		int itemIdReturn = 0;
		//获得物品属性，还有等级
		DiamondMosaic diamondMosaic = diamondMosaicMap.get(itemId);
		if(diamondMosaic != null){
			byte itemLevel = diamondMosaic.getDiamondLevel();
			for(Map.Entry<Integer, DiamondMosaic> dia: diamondMosaicMap.entrySet()){
				if(diamondMosaic.getProperty() == dia.getValue().getProperty()
						&& level == dia.getValue().getDiamondLevel() && diamondMosaic.isCanUse() == dia.getValue().isCanUse()){
					itemIdReturn = dia.getValue().getItemId();
				}
			}
		}
		return itemIdReturn;
	}
	
	/**
	 * 获取宝石升级需要花费的J币
	 * @param itemId
	 * @param autoMixData
	 * @return
	 */
	public static int getTotalMoney_J(int itemId, int[][]autoMixData){
		int totalMoney = 0;
		DiamondMosaic diamondMosaic = diamondMosaicMap.get(itemId);
		int tmpID = DiamondMosaic.findNextDiamondMosaicProductItemId(autoMixData[0][0]);
		totalMoney = getDiamondMosaicProductMoney(tmpID) * autoMixData[1][1];//计算3级升4级的花费
		int tmpTime = autoMixData[1][1] / maxDiamondMix;//4级升5级要合成的宝石数量
		int level = diamondMosaic.diamondLevel - minAutoMixLevel;
		while(level >= 0){
			tmpID = DiamondMosaic.findNextDiamondMosaicProductItemId(tmpID);
			totalMoney += getDiamondMosaicProductMoney(tmpID) * tmpTime;
			level--;
			tmpTime /= maxDiamondMix;
		}
		totalMoney += getDiamondMosaicProductMoney(itemId);//加上最后合成目标宝石的花费
		return totalMoney;
	}
}
