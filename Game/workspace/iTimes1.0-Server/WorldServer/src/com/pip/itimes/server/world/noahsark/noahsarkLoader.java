package com.pip.itimes.server.world.noahsark;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.StageService;

public class noahsarkLoader {
	public noahsarkLoader(File file,StageService service) throws Exception{
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		load(doc,service);
		
	}
	public void load(Document doc,StageService service) throws Exception {
		Element root = doc.getRootElement();
		Element start = root.element("Start");
		NoahsarkConfig.year = Integer.parseInt(start.attributeValue("year"));
		NoahsarkConfig.month = Integer.parseInt(start.attributeValue("month"));
		NoahsarkConfig.day = Integer.parseInt(start.attributeValue("day"));
		Element end = root.element("End");
		NoahsarkConfig.endYear = Integer.parseInt(end.attributeValue("year"));
		NoahsarkConfig.endMonth = Integer.parseInt(end.attributeValue("month"));
		NoahsarkConfig.endDay = Integer.parseInt(end.attributeValue("day"));
		NoahsarkConfig.title = new StringBuffer().append(root.elementText("Title"));
		
		Element donate = root.element("Donate");
		Element donateStart = donate.element("Start");
		int year = Integer.parseInt(donateStart.attributeValue("year"));
		int month = Integer.parseInt(donateStart.attributeValue("month"));
		int day = Integer.parseInt(donateStart.attributeValue("day"));
		Element material = donate.element("Material");
		NoahsarkDonateMaterial[] materials = getItems(material ,"Item");
		Element awards = donate.element("Award");
		Element item = awards.element("Item");
		NoahsarkDonateMaterial award = new NoahsarkDonateMaterial();
		award.setItemId(Integer.parseInt(item.attributeValue("itemId")));
		award.setItemCount(Integer.parseInt(item.attributeValue("itemCount")));
		int score =Integer.parseInt(awards.elementTextTrim("Score"));
		StringBuffer message[] = new StringBuffer[5];
		Element messages = donate.element("Message");
		for(int i = 0;i < message.length;i ++){
			message[i] = new StringBuffer();
			message[i].append(messages.elementTextTrim("Message" + (i+1)));
		}
		NoahsarkConfig.donate=new NoahsarkDonate(year,month,day,materials,award,score,message);
		NoahsarkScoreTop scoreTop = new NoahsarkScoreTop();
		
		Element top = donate.element("ScoreTop");
		scoreTop.setFirst(getItem(top.element("First")));
		scoreTop.setSecond(getItem(top.element("Second")));
		scoreTop.setThird(getItem(top.element("Third")));
		scoreTop.setBase(getItem(top.element("ScoreCount")));
		scoreTop.setScoreCount(Integer.parseInt(top.element("ScoreCount").elementTextTrim("Count")));
		NoahsarkConfig.scoreTop = scoreTop;
		
		Element boss = root.element("Boss");
		NoahsarkDonateMaterial[] bossPrize ={
			getItem(boss.element("First")),
					getItem(boss.element("Second")),getItem(boss.element("Third"))};
		
		NoahsarkConfig.bossPrize = bossPrize;
			
			
		
		
		
	}
	public NoahsarkDonateMaterial[] getItems(Element element, String key){
		ArrayList<NoahsarkDonateMaterial> listItems = new ArrayList<NoahsarkDonateMaterial>();
        
        for(Iterator j=element.elementIterator("Item"); j.hasNext();){
        	Element node = (Element)j.next();
        	int itemId = Integer.parseInt(node.attributeValue("itemId"));
        	int itemCount = Integer.parseInt(node.attributeValue("itemCount"));
        	NoahsarkDonateMaterial item = new NoahsarkDonateMaterial();
        	item.setItemId(itemId);
        	item.setItemCount(itemCount);
        	listItems.add(item);
        }
        NoahsarkDonateMaterial[] item = new NoahsarkDonateMaterial[listItems.size()];
        listItems.toArray(item);
        return item;
	}
	public NoahsarkDonateMaterial getItem(Element element){
		Element el = element.element("Item");
    	int itemId = Integer.parseInt(el.attributeValue("itemId"));
    	int itemCount = Integer.parseInt(el.attributeValue("itemCount"));
    	NoahsarkDonateMaterial item = new NoahsarkDonateMaterial();
    	item.setItemId(itemId);
    	item.setItemCount(itemCount);
    	return item;
	}
}
