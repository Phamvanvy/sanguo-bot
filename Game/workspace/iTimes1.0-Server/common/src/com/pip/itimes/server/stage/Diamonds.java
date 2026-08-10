package com.pip.itimes.server.stage;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.util.Utils;

/**
 * @author wpjiang 装备鉴定管理类
 */
public class Diamonds {
	public static  Map diamondMap = new HashMap();
	/**
	 * 攻击力提升
	 */
	public static  Map diamondAddRateMap = new HashMap();
	
	/**
	 * 属性提升
	 */
	public static Map diamondPropertyMap = new HashMap();
	
	/**
	 * 成功率概率
	 */
	public static Map diamondSuccessRateMap = new HashMap();
	public static Map getDiamondMap(){
		return diamondMap;
	}
	public void loadEnhances(File pkgDir) throws Exception {
		diamondMap.clear();
		diamondAddRateMap.clear();
		diamondPropertyMap.clear();
		String stageDirName = pkgDir.getAbsolutePath();
		String dirName = FilenameUtils.concat(stageDirName,
				"Items/enhances.xml");
		SAXReader reader = new SAXReader();
		Document doc = reader.read(dirName);
		Element root = doc.getRootElement();
		for (Iterator i = root.elementIterator("diamond"); i.hasNext();) {
			Element el = (Element) i.next();
			byte type = Byte.parseByte(el.attributeValue("type"));

			int itemId = Integer.parseInt(el.attributeValue("itemid"));
			int min = Integer.parseInt(el.attributeValue("min"));
			int max = Integer.parseInt(el.attributeValue("max"));
			
			Diamond diamond = new Diamond(itemId, min, max);
			DiamondSucessRate diamondSuccessRate = new DiamondSucessRate();
			for(Iterator j = el.elementIterator("rate"); j.hasNext();){
				
				Element el2 = (Element) j.next();
				int id = Integer.parseInt(el2.attributeValue("id"));
				int rate = Integer.parseInt(el2.attributeValue("successrate"));
				
				diamondSuccessRate.addDiamondSuccessRateMap(id, rate);
				
			}
			
			diamondSuccessRateMap.put(type, diamondSuccessRate);
			diamondMap.put(type, diamond);
		}
		
		for (Iterator i = root.elementIterator("diamondrate"); i.hasNext();) {
			Element el = (Element) i.next();
			byte type = Byte.parseByte(el.attributeValue("type"));

			int addRate = Integer.parseInt(el.attributeValue("rate"));
			
			diamondAddRateMap.put(type, addRate);
		}
		
		for (Iterator i = root.elementIterator("diamondpropertyrate"); i.hasNext();) {
			Element el = (Element) i.next();
			byte type = Byte.parseByte(el.attributeValue("type"));

			int addRate = Integer.parseInt(el.attributeValue("rate"));
			diamondPropertyMap.put(type, addRate);
		}
	}
	
	
	
	public static int getDiamondNeedMoney(int equLevel){		
		return equLevel * equLevel;
	}
	
	public static boolean CanNotDiamond(byte quality, byte part){
		if(Utils.CLR_EQUIP[quality]== Utils.CLR_WHITE || Utils.CLR_EQUIP[quality]== Utils.CLR_YELLOW ){
			return true;
		}
		
		/*//项链戒指不可鉴定
		if(part == IEquipment.PART_NECK || part == IEquipment.PART_FINGER){
			return true;
		}*/
		
		return false;
	}
	
	public static Diamond getDiamond(byte type){
		Diamond diamond = (Diamond) diamondMap.get(type);
		return diamond;
	}
	
	
	/**
	 * 攻击力提升几率
	 * @param diamond
	 * @return
	 */
	public static int getDiamondRate(byte diamond){
		int rate = 0;
		if(diamondAddRateMap.containsKey(diamond)){
			rate = (Integer) diamondAddRateMap.get(diamond);
		}
		return rate;
	}
	
	/**
	 * 属性提升
	 * @param diamond
	 * @return
	 */
	public static int getDiamondPropertyRate(byte diamond){
		
		int rate = 0;
		if(diamondPropertyMap.containsKey(diamond)){
			rate = (Integer) diamondPropertyMap.get(diamond);
		}
		return rate;
	}
	
	
	public static byte getDiamondRndCount(Random rnd, byte type){
		
		byte rand = 0;
		DiamondSucessRate diamondRate = (DiamondSucessRate) diamondSuccessRateMap.get(type);
		if(diamondRate == null){
			return rand;
		}else{
			Map rateMap = diamondRate.getDiamondSuccessRateMap();
			if(rateMap == null){
				return rand;
			}else{
				int temprand = rnd.nextInt(10000) + 1;
				int count = (Integer) rateMap.get(1);
				for(int i = 1; i < 10; i++){
					if(temprand < count){
						rand = (byte) i;
						break;
					}else{
						int t = (Integer) rateMap.get(i+1);
						count = count + t;
					}
				}
			}
		}
		
		//rand = (byte) (rnd.nextInt(max - min + 1) + min);
		return rand;
	}
}
