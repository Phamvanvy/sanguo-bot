package com.pip.itimes.server.stage;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.Element;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class StoreGroupLoader {


    public StoreGroupLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadStoreGroups(doc);
    }

    private void loadStoreGroups(Document doc){
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("Group");i.hasNext();){
            Element node = (Element)i.next();
            int type = Integer.parseInt(node.attributeValue("type"));
            int id = Integer.parseInt(node.attributeValue("id"));
            String desc = node.attributeValue("desc");
            StoreGroup group = new StoreGroup(id);
            group.setDesc(desc);
            group.setType(type);
            StoreItem[] items = loadItems(node);
            group.setItems(items);
            if(1 == type){
            	StoreGroups.addStoreGroup(group);
            }else if(2 == type){
            	StoreGroups.addFightLevelStoreGroup(group);
            }
            
        }
    }

    private StoreItem[] loadItems(Element node){
        List l = new ArrayList();
        for(Iterator i=node.elementIterator("Item");i.hasNext();){
            Element n = (Element)i.next();
            int id = Integer.parseInt(n.attributeValue("itemid"));
            IItemTemplate item = Items.getTemplate(id);
            if(item!=null){
                int price = Integer.parseInt(n.attributeValue("price"));
                int count = Integer.parseInt(n.attributeValue("count"));
                String desc = n.attributeValue("desc");
                //String consumeCode = n.attributeValue("consumecode");不用消费码还留着干吗
                int level = Integer.parseInt(n.attributeValue("level"));
                StoreItem storeItem = new StoreItem();
                storeItem.item = item;
                storeItem.price = price;
                storeItem.desc = desc;
                storeItem.count = count;
                storeItem.level = level;
//                storeItem.consumeCode = consumeCode;
                l.add(storeItem);
            }
        }
        StoreItem[] items = new StoreItem[l.size()];
        l.toArray(items);
        return items;
    }
}
