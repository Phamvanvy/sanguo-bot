package com.pip.itimes.server.stage;

import org.dom4j.io.SAXReader;
import java.io.File;
import org.dom4j.Document;
import org.dom4j.Element;
import java.util.Iterator;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class DropGroupLoader {
    public DropGroupLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        load(doc);;
    }

    private void load(Document doc){
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("DropGroup");i.hasNext();){
            Element node = (Element)i.next();
//            DropGroup group = new DropGroup();
            int id = Integer.parseInt(node.attributeValue("groupID"));
            String name = node.attributeValue("title");
            long startTime = 0;
            long endTime = 0;
            String strTime = node.attributeValue("startTime");
            if(strTime != null){
            	startTime = Long.parseLong(strTime);
            }
            strTime = node.attributeValue("endTime");
            if(strTime != null){
            	endTime = Long.parseLong(strTime);
            }
//            group.setId(id);
//            group.setName(name);
            for(Iterator k=node.elementIterator("SubGroup");k.hasNext();){
                int rate = 0;
                Element ell = (Element)k.next();
                DropGroup group = new DropGroup();
                group.setId(id);
                group.setStartTime(startTime);
                group.setEndTime(endTime);
                group.setName(name);
                int minLevel = Integer.parseInt(ell.attributeValue("minLevel"));
                int maxLevel = Integer.parseInt(ell.attributeValue("maxLevel"));
                group.setMinLevel(minLevel);
                group.setMaxLevel(maxLevel);
                for (Iterator j = ell.elementIterator("DropGroupItem"); j.hasNext(); ) {
                    Element el = (Element) j.next();
                    int iid = Integer.parseInt(el.attributeValue("id"));
                    int itemId = Integer.parseInt(el.attributeValue("itemID"));
                    int irate = Integer.parseInt(el.attributeValue("dropRate"));
                    int min = Integer.parseInt(el.attributeValue("amountMin"));
                    int max = Integer.parseInt(el.attributeValue("amountMax"));
                    IItemTemplate item = Items.getTemplate(itemId);
                    if (item != null) {
                        DropItem dropItem = new DropItem();
                        dropItem.setId(iid);
                        dropItem.setItem(item);
                        dropItem.setMax(max);
                        dropItem.setMin(min);
                        rate += irate;
                        dropItem.setRate(rate);
                        group.addDropItem(dropItem);
                    }
                }
                group.setRate(rate);
                DropGroups.addDropGroup(group);
            }
        }
    }
}
