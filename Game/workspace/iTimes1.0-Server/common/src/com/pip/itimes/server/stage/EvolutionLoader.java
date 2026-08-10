package com.pip.itimes.server.stage;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @file EvolutionLoader.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-11-27\
 **/
public class EvolutionLoader {
	public static HashMap<Integer, EvolutionData> evolutions = new HashMap<Integer, EvolutionData>();
	public static HashMap<Integer, Integer> evolutionVipCount = new HashMap<Integer, Integer>();
	
	public EvolutionLoader(File pkgDir) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(pkgDir);
        load(doc);
	}
	
	public static int getAllNeedPoint(int level){
		int allPoint = 0;
		for(int i=0; i<=level; i++){
			EvolutionData data = evolutions.get(i);
			allPoint += data.needpoint;
		}
		return allPoint;
	}
	public static String getEvolutionName(int level){
		String evolutionName = "";
		EvolutionData data = evolutions.get(level);
		if(data != null){
			evolutionName = data.name;
		}
		return evolutionName;
	}
	public void load(Document doc){
		synchronized (evolutions) {
			evolutions.clear();
			Element root = doc.getRootElement();
			Iterator it = root.elementIterator("pet");
			while(it.hasNext()) {
				Element elem = (Element)it.next();
				EvolutionData data = new EvolutionData();
				data.id = Integer.parseInt(elem.attributeValue("id"));
				data.name = elem.attributeValue("name");
				data.needpoint = Integer.parseInt(elem.attributeValue("needpoint"));
				data.getpoint = Integer.parseInt(elem.attributeValue("getpoint"));
				data.getpoint2 = Integer.parseInt(elem.attributeValue("getpoint2"));
				data.getpoint3 = Integer.parseInt(elem.attributeValue("getpoint3"));
				data.getpoint4 = Integer.parseInt(elem.attributeValue("getpoint4"));
				data.times = Integer.parseInt(elem.attributeValue("times"));
				data.expectpoint = Integer.parseInt(elem.attributeValue("expectpoint"));
				data.freeday = Integer.parseInt(elem.attributeValue("freeday"));
				data.rmbday = Integer.parseInt(elem.attributeValue("rmbday"));
				data.changefateid = Integer.parseInt(elem.attributeValue("changefateid"));
				data.develop = Integer.parseInt(elem.attributeValue("develop"));
				
				Element elAttr = elem.element("attr");
				data.pa = Integer.parseInt(elAttr.attributeValue("pa"));
				data.ma = Integer.parseInt(elAttr.attributeValue("ma"));
				data.pd = Integer.parseInt(elAttr.attributeValue("pd"));
				data.md = Integer.parseInt(elAttr.attributeValue("md"));
				data.hp = Integer.parseInt(elAttr.attributeValue("hp"));
				
				Iterator elTypes = elem.elementIterator("type");
				while(elTypes.hasNext()){
					Element elType = (Element)elTypes.next();
					int typeValue = Integer.parseInt(elType.attributeValue("value"));
					data.types.add(typeValue);
				}
				
				evolutions.put(data.id, data);
			}
			evolutionVipCount.clear();
			it = root.elementIterator("vip");
			while(it.hasNext()) {
				Element elem = (Element)it.next();
				int level = Integer.parseInt(elem.attributeValue("level"));
				int count = Integer.parseInt(elem.attributeValue("count"));
				evolutionVipCount.put(level, count);
			}
		}
	}
	
	
}
