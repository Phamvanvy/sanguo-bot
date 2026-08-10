package com.pip.itimes.server.world.worldboss;
/**
 * @file WorldBossLoader.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-9-19
 **/
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.StageService;


public class WorldBossLoader {
	public WorldBossLoader(File file, StageService service) throws Exception{
		SAXReader reader = new SAXReader();
	    Document doc = reader.read(file);
	    load(doc, service);
	}
	
	public void load(Document doc, StageService service) throws Exception{
		Element root = doc.getRootElement();
		Element elOpen = root.element("Open");
		boolean isOpen = elOpen.attributeValue("open").equals("true") ? true : false;
		//加载进来的是关闭状态 则关闭当前的系统
		if(WorldBossConfig.open && isOpen == false){
			WorldBossConfig.close();
		}
		WorldBossConfig.open = isOpen;
		WorldBossConfig.blessingPercent = Integer.parseInt(elOpen.attributeValue("blessingPercent"));
		WorldBossConfig.blessingPercentMax = Integer.parseInt(elOpen.attributeValue("blessingPercentMax"));
		
		
		synchronized (WorldBossConfig.worldBossTimes) {
			WorldBossConfig.worldBossTimes.clear();
			ArrayList<String> strStartTimeMessage = new ArrayList<String>();
			int index = 0;
			for(Iterator i=elOpen.elementIterator("time"); i.hasNext();){
	            Element node = (Element)i.next();
	            int hour = Integer.parseInt(node.attributeValue("hour"));
	            int minute = Integer.parseInt(node.attributeValue("minute"));
	            int endhour = Integer.parseInt(node.attributeValue("endhour"));
	            int endminute = Integer.parseInt(node.attributeValue("endminute"));
	            WorldBossTime worldBossTime = new WorldBossTime(hour, minute, endhour, endminute);
	            worldBossTime.setIndex(index);
	            WorldBossConfig.worldBossTimes.add(worldBossTime);
	            strStartTimeMessage.add("活动时间在每天" + hour + ":" + minute + "分开始");
	            index++;
			}
			WorldBossConfig.strActionTime = new String[strStartTimeMessage.size()];
			strStartTimeMessage.toArray(WorldBossConfig.strActionTime);
		}
		
		Element elBoss = root.element("Boss");
		
		int level = Integer.parseInt(elBoss.attributeValue("level"));
		int maxlevel = Integer.parseInt(elBoss.attributeValue("maxlevel"));
//		int hp = Integer.parseInt(elBoss.attributeValue("hp"));
//		int mp = Integer.parseInt(elBoss.attributeValue("mp"));
//		int strength = Integer.parseInt(elBoss.attributeValue("strength"));
//		int vitality = Integer.parseInt(elBoss.attributeValue("vitality"));
//		int agility = Integer.parseInt(elBoss.attributeValue("agility"));
//		int intelligence = Integer.parseInt(elBoss.attributeValue("intelligence"));
		int mgId = Integer.decode(elBoss.attributeValue("mgId"));
		int roundsecond = Integer.decode(elBoss.attributeValue("roundsecond"));
		int roundhardsecond = Integer.decode(elBoss.attributeValue("roundhardsecond"));
		int leveluptime = Integer.decode(elBoss.attributeValue("leveluptime"));
		
		WorldBossConfig.worldBossData = new WorldBossData(level, maxlevel, mgId);
		WorldBossConfig.worldBossData.setRoundSecond(roundsecond);
		WorldBossConfig.worldBossData.setRoundHardSecond(roundhardsecond);
		WorldBossConfig.worldBossData.setLeveluptime(leveluptime);
		
		Element elAward = root.element("Award");
		
		synchronized (WorldBossConfig.worldBossAwards) {
			WorldBossConfig.worldBossAwards.clear();
			for(Iterator i=elAward.elementIterator("Stage"); i.hasNext();){
	            Element node = (Element)i.next();
	            WorldBossAward award = new WorldBossAward();
	            int stage = Integer.parseInt(node.attributeValue("stage"));
	            int startLevel = Integer.parseInt(node.attributeValue("startLevel"));
	            int endLevel = Integer.parseInt(node.attributeValue("endLevel"));
	            award.stage = stage;
	            award.startLevel = startLevel;
	            award.endLevel = endLevel;
	            award.firstItems = getItems(node, "first");
	            award.secondItems = getItems(node, "second");
	            award.thirdItems = getItems(node, "third");
	            award.top10Items = getItems(node, "top10");
	            award.top20Items = getItems(node, "top20");
	            award.top50Items = getItems(node, "top50");
	            award.otherItems = getItems(node, "other");
	            WorldBossConfig.worldBossAwards.put(stage, award);
			}
		}
		WorldBossConfig.roundAward = getItems(root, "Round");
		
		if(WorldBossConfig.worldBossMonster == null){
			WorldBossConfig.worldBossMonster = WorldBossConfig.createWorldBoss(service);
		}else{
			synchronized (WorldBossConfig.worldBossMonster){
				WorldBossConfig.worldBossMonster = WorldBossConfig.createWorldBoss(service);
			}
		}
		
	}
	
	public WorldBossAwardItem[] getItems(Element element, String key){
		ArrayList<WorldBossAwardItem> listItems = new ArrayList<WorldBossAwardItem>();
        Element el = element.element(key);
        for(Iterator j=el.elementIterator("item"); j.hasNext();){
        	Element node2 = (Element)j.next();
        	int money = Integer.parseInt(node2.attributeValue("money"));
        	int itemid = Integer.parseInt(node2.attributeValue("itemid"));
        	int itemcount = Integer.parseInt(node2.attributeValue("itemcount"));
        	int exp = Integer.parseInt(node2.attributeValue("exp"));
        	String strDem = node2.attributeValue("dem");
        	int dem = 0;
        	if(strDem != null){
        		dem = Integer.parseInt(strDem);
        	}
        	WorldBossAwardItem item = new WorldBossAwardItem();
        	item.money = money;
        	item.itemid = itemid;
        	item.itemcount = itemcount;
        	item.exp = exp;
        	item.dem = dem;
        	listItems.add(item);
        }
        WorldBossAwardItem[] item = new WorldBossAwardItem[listItems.size()];
        listItems.toArray(item);
        return item;
	}
}
