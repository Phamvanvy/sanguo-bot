package com.pip.itimes.server.gift;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class ExchangeDataLoader {
	private static final Logger log = Logger.getLogger(ExchangeDataLoader.class);
	public ExchangeDataLoader(File file) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		loadExchangeData(doc);
	}

	public void loadExchangeData(Document doc) throws Exception {
		Element root = doc.getRootElement();
		ExchangeDefine exchangeDefine = new ExchangeDefine();
		exchangeDefine.removeAllExchangeData();
		log.info("loadExchangeData start removeAllExchangeData ok");
		if (root == null) {
			return;
		}
		int count_i = 0;
		for (Iterator i = root.elementIterator("exchange"); i.hasNext();) {
			try {
				Element exchangeNode = (Element) i.next();
				Attribute attr = exchangeNode.attribute("valid");
				boolean valid = attr.getValue().equals("true");
				if (valid) {
					ExchangeData exchangeData = new ExchangeData();//兑换类型（一级）
					
					attr = exchangeNode.attribute("id");
					int exchange_id = Integer.parseInt(attr.getValue());
					for (Iterator j = exchangeNode.elementIterator("group"); j.hasNext();) {
						Element exchangeGroupNode = (Element) j.next();
						ExchangeGroup exchangeGroup = new ExchangeGroup();//兑换组（二级）
						
						attr = exchangeGroupNode.attribute("id");
						int group_id = Integer.parseInt(attr.getValue());
						exchangeGroup.setId(group_id);
						attr = exchangeGroupNode.attribute("beginlevel");
						exchangeGroup.setBeginlevel(Integer.parseInt(attr.getValue()));
						attr = exchangeGroupNode.attribute("endlevel");
						exchangeGroup.setEndlevel(Integer.parseInt(attr.getValue()));
						attr = exchangeGroupNode.attribute("needitem");
						exchangeGroup.setNeeditem(Integer.parseInt(attr.getValue()));
						attr = exchangeGroupNode.attribute("needitemcount");
						exchangeGroup.setNeeditemcount(Integer.parseInt(attr.getValue()));
						attr = exchangeGroupNode.attribute("content");
						exchangeGroup.setContent(attr.getValue());
						attr = exchangeGroupNode.attribute("type");
						exchangeGroup.setType(Integer.parseInt(attr.getValue()));
						
						Map<Integer, Integer> needEquMap = new HashMap<Integer, Integer>();
						Map<Integer, Integer> giveItemMap = new HashMap<Integer, Integer>();
						for (Iterator k = exchangeGroupNode.elementIterator("needequ"); k.hasNext();) {
							Element exchangeneedItemNode = (Element) k.next();
							
							attr = exchangeneedItemNode.attribute("itemid");
							int itemId = Integer.parseInt(attr.getValue());
							attr = exchangeneedItemNode.attribute("count");
							int count = Integer.parseInt(attr.getValue());
							
							needEquMap.put(itemId, count);
						}
						
						for (Iterator k = exchangeGroupNode.elementIterator("giveitem"); k.hasNext();) {
							Element exchangeneedItemNode = (Element) k.next();
							
							attr = exchangeneedItemNode.attribute("itemid");
							int itemId = Integer.parseInt(attr.getValue());
							attr = exchangeneedItemNode.attribute("count");
							int count = Integer.parseInt(attr.getValue());
							
							giveItemMap.put(itemId, count);
						}
						exchangeGroup.setGiveItemMap(giveItemMap);
						exchangeGroup.setNeedEquMap(needEquMap);
						
						//放入第一级map
						exchangeData.addExchangeData(group_id, exchangeGroup);
						exchangeData.addExchangeContent(group_id, exchangeGroup.getContent());
					}
					exchangeDefine.addExchangeData(exchange_id, exchangeData);
					count_i++;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.info("loadExchangeData ok count = " + count_i);
	}
}
