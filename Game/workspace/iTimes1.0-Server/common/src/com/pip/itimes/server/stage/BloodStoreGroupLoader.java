package com.pip.itimes.server.stage;

import java.io.File;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.Attribute;

import com.pip.itimes.server.util.Utils;

public class BloodStoreGroupLoader {

	public BloodStoreGroupLoader(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadStoreGroups(doc);
	}

	private void loadStoreGroups(Document doc) {
		Element root = doc.getRootElement();
		for(Iterator i=root.elementIterator("group");i.hasNext();){
            Element node = (Element)i.next();
            String name = node.attributeValue("name");
            BloodStoreItem[] items = loadItems(node);
            BloodStoreGroup group = new BloodStoreGroup(name,items);
            BloodShopGroups.addGroup(group);
        }
    }
	
	private BloodStoreItem[] loadItems(Element node){
        List l = new ArrayList();
        for (Iterator i = node.elementIterator("Item"); i.hasNext();){
            Element n = (Element)i.next();
            int id = Integer.parseInt(n.attributeValue("itemid"));
            IItemTemplate item = Items.getTemplate(id);
            if (item!=null) {
                int price = Integer.parseInt(Utils.getWholeDataPrice(n.attributeValue("price")));
                int count = Integer.parseInt(n.attributeValue("count"));
                ArrayList<ExChangeItemData> list = new ArrayList();
                for (Iterator iter = n.elementIterator("exchangeitem"); iter.hasNext();){
                	Element element = (Element)iter.next();
                	int changeid = Integer.parseInt(element.attributeValue("id"));
                	IItemTemplate changeitem = Items.getTemplate(changeid);
                	if(changeitem!=null){
                		int changecount = Integer.parseInt(element.attributeValue("count"));
                		ExChangeItemData ex = new ExChangeItemData();
                		ex.changitem = changeitem;
                		ex.changecount = changecount;
                		list.add(ex);
                	}
                }
                BloodStoreItem bloodItem = new BloodStoreItem();
                bloodItem.item = item;
                bloodItem.price = price;
                bloodItem.count = count;
                bloodItem.exchangeitems = list;
                l.add(bloodItem);
                 	
            }
        }
        BloodStoreItem[] items = new BloodStoreItem[l.size()];
        l.toArray(items);
        return items;
    }

}
