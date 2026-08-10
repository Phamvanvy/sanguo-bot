package com.pip.itimes.server.world.farm;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.game.FarmInstanceModel;

public class FarmConfig {
	private static HashMap<Integer, FarmSeedData> farmSeeds = new HashMap<Integer, FarmSeedData>();
	public static long HourStart = 4 * 60 * 1000L;
	public static int FertilizeItemID = 0;
	public static int FertilizeAddResultsPercent = 50;
	public static int ResultsPercent = 70;
	public static int LevelPercent = 20;
	public static int LevelUpBaseMoney = 30;
	public static int LevelOpenBaseMoney = 20;
	public static final int FARM_LAND_MAXCOUNT = 10;
	
	public static HashMap<Integer, ArrayList<FarmDropItemData>> farmDropItems = new HashMap<Integer, ArrayList<FarmDropItemData>>();
	public static HashMap<Integer, Integer> farmResultsPercent = new HashMap<Integer, Integer>();
	public static ArrayList<String> farmChats = new ArrayList<String>();
	
	public static int vampireHP = 200;
	public static int vampireCount = 10;
	
	public FarmConfig(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        load(root);
	}
	
	public void load(Element root){
		synchronized (farmSeeds) {
			farmSeeds.clear();
			Element seeds = root.element("Seeds");
			for (Iterator<Element> ids = seeds.elementIterator("seed"); ids.hasNext();) {
				Element el = (Element)ids.next();
				FarmSeedData fsd = new FarmSeedData();
				fsd.setId(Integer.parseInt(el.attributeValue("id")));
				fsd.setName(el.attributeValue("name"));
				fsd.setHP(Integer.parseInt(el.attributeValue("hp")));
				fsd.setAP(Integer.parseInt(el.attributeValue("ap")));
				fsd.setGrowCycle(Integer.parseInt(el.attributeValue("lifeCycle")));
				fsd.setLandLevel(Integer.parseInt(el.attributeValue("landLevel")));
				fsd.setFruitCount(Integer.parseInt(el.attributeValue("fruitCount")));
				fsd.setResultsid(Integer.parseInt(el.attributeValue("resultsid")));
				farmSeeds.put(fsd.getId(), fsd);
			}
		}
		Element Fertilizes = root.element("Fertilize");
		for (Iterator<Element> ids = Fertilizes.elementIterator("item"); ids.hasNext();) {
			Element el = (Element)ids.next();
			FertilizeItemID = Integer.parseInt(el.attributeValue("itemid"));
			FertilizeAddResultsPercent = Integer.parseInt(el.attributeValue("addresultspercent"));
		}
		Element results = root.element("Results");
		ResultsPercent = Integer.parseInt(results.attributeValue("resultsPercent"));
		LevelPercent = Integer.parseInt(results.attributeValue("levelPercent"));
		LevelUpBaseMoney = Integer.parseInt(results.attributeValue("levelUpBaseMoney"));
		LevelOpenBaseMoney = Integer.parseInt(results.attributeValue("levelOpenBaseMoney"));
		
		Element time = root.element("Invade");
		int hour = Integer.parseInt(time.attributeValue("hour"));
		int minute = Integer.parseInt(time.attributeValue("minute"));
		int second = Integer.parseInt(time.attributeValue("second"));
		HourStart = ((hour * 60 + minute) * 60 + second) * 1000L;
		final long hour24 = 24 * 60 * 60 * 1000L;
		long tmpNow = new Date().getTime();
		FarmInstanceModel.startDay = Utils.getTodayStart() + FarmConfig.HourStart;
		if(FarmInstanceModel.startDay > tmpNow){
			FarmInstanceModel.startDay -= hour24;
		}
		
		Element vampires = root.element("Vampire");
		vampireHP = Integer.parseInt(vampires.attributeValue("hp"));
		vampireCount = Integer.parseInt(vampires.attributeValue("count"));
		
		synchronized (farmDropItems) {
			farmDropItems.clear();
			Element dropItems = root.element("DropItems");
			for (Iterator<Element> ids = dropItems.elementIterator("Items"); ids.hasNext();) {
				Element items = ids.next();
				ArrayList<FarmDropItemData> lstItem = new ArrayList<FarmDropItemData>();
				for (Iterator<Element> elItems = items.elementIterator("item"); elItems.hasNext();) {
					Element item = elItems.next();
					int itemid = Integer.parseInt(item.attributeValue("itemid"));
					int count = Integer.parseInt(item.attributeValue("count"));
					FarmDropItemData fdid = new FarmDropItemData(itemid, count);
					lstItem.add(fdid);
				}
				int type = Integer.parseInt(items.attributeValue("type"));
				int resultsPercent = Integer.parseInt(items.attributeValue("resultspercent"));
				farmDropItems.put(type, lstItem);
				farmResultsPercent.put(type, resultsPercent);
			}
		}
		
		synchronized (farmChats) {
			farmChats.clear();
			Element chats = root.element("Chats");
			for (Iterator<Element> ids = chats.elementIterator("Chat"); ids.hasNext();) {
				Element chat = ids.next();
				farmChats.add(chat.attributeValue("value"));
			}
		}
	}
	
	public static HashMap<Integer, FarmSeedData> getFarmSeeds(){
		return farmSeeds;
	}
	
	public static FarmSeedData getFarmSeed(int id){
		if(farmSeeds.containsKey(id)){
			return farmSeeds.get(id);
		}
		return null;
	}
	
	public static ArrayList<FarmDropItemData> getFarmDropItem(int type){
		return farmDropItems.get(type);
	}
	
	public static int getFarmResultsPercent(int type){
		return farmResultsPercent.get(type);
	}
	
	public static String getRandomChat(){
		if(farmChats.size() == 0){
			return null;
		}
		int index = Utils.getRandom(0, farmChats.size() - 1);
		return farmChats.get(index);
	}
}
