package com.pip.itimes.server.stage;

import java.io.File;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.Attribute;

import com.pip.itimes.server.util.Utils;

public class CStoreGroupLoader {
    public CStoreGroupLoader(File file) throws Exception {
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadStoreGroups(doc);
    }

    private void loadStoreGroups(Document doc) {
    	Element root = doc.getRootElement();
        for (Iterator i = root.elementIterator("Group"); i.hasNext(); ) {
            Element node = (Element)i.next();
            int id = Integer.parseInt(node.attributeValue("id"));
            LinkedHashMap<String, CStoreGroup> typeGroups = new LinkedHashMap<String, CStoreGroup>();
            for (Iterator j = node.elementIterator("group"); j.hasNext(); ) {
            	Element n = (Element)j.next();
            	String name = n.attributeValue("name");
            	CStoreItem[] items = loadItems(n);
            	CStoreGroup group = new CStoreGroup(name, items);
            	group.setId(id);
            	CStoreGroups.addTypeGroup(group, typeGroups);
            }
            CStoreGroups.addGroup((Integer)id, typeGroups);
        }
    }

    private CStoreItem[] loadItems(Element node){
        List l = new ArrayList();
        for (Iterator i = node.elementIterator("Item"); i.hasNext(); ) {
            Element n = (Element)i.next();
            int id = Integer.parseInt(n.attributeValue("itemid"));
            IItemTemplate item = Items.getTemplate(id);
            if (item!=null) {
                int credit = Integer.parseInt(Utils.getWholeDataPrice(n.attributeValue("credit")));
                int count = Integer.parseInt(n.attributeValue("count"));
                String desc = n.attributeValue("desc");
                CStoreItem storeItem = new CStoreItem();
                storeItem.item = item;
                storeItem.credit = credit;
                storeItem.desc = desc;
                storeItem.count = count;
                l.add(storeItem);
            }
        }
        CStoreItem[] items = new CStoreItem[l.size()];
        l.toArray(items);
        return items;
    }
}
