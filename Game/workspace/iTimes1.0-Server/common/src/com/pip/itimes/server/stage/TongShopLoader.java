package com.pip.itimes.server.stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.util.Utils;

public class TongShopLoader {
	public TongShopLoader(File file,int iMoneyType,int IMONEY_TYPE_CMCC) throws Exception{
	SAXReader reader = new SAXReader();
    Document doc = reader.read(file);
    loadTongShop(doc,iMoneyType,IMONEY_TYPE_CMCC);
}

private void loadTongShop(Document doc,int iMoneyType,int IMONEY_TYPE_CMCC){
    Element root = doc.getRootElement();
    Element tmpLimit = root.element("magicMoneyUseLimit");
    int useMax = Integer.parseInt(tmpLimit.attributeValue("limit"));
    TongShopGroups.setUseMax(useMax);
    for(Iterator i=root.elementIterator("shop");i.hasNext();){
        Element node = (Element)i.next();
        int id = Integer.parseInt(node.attributeValue("id"));
        String shopname = node.attributeValue("name");
        TongShopGroups.addShop(id,shopname);
        for(Iterator tmp=node.elementIterator("group");tmp.hasNext();){
        	Element tmpNode = (Element)tmp.next();
        	String name = tmpNode.attributeValue("name");
        	IStoreItem[] items = loadItems(tmpNode,iMoneyType,IMONEY_TYPE_CMCC);
        	IStoreGroup group = new IStoreGroup(name, false, items);
        	TongShopGroups.addGroup(id,shopname,group);
        }
    }
}

private IStoreItem[] loadItems(Element node,int iMoneyType,int IMONEY_TYPE_CMCC){
    List l = new ArrayList();
    for(Iterator i=node.elementIterator("Item");i.hasNext();){
        Element n = (Element)i.next();
        int id = Integer.parseInt(n.attributeValue("itemid"));
        IItemTemplate item = Items.getTemplate(id);
        //mengjie add 20100916 cmcc item binded
        if (iMoneyType == IMONEY_TYPE_CMCC){
        	try{
        		if(item instanceof ExtendedItemTemplate){//一般物品
        			((ExtendedItemTemplate) item).setBindType((byte) 1);	
				}else if(item instanceof BasicItemTemplate){//基本物品
        			((BasicItemTemplate) item).setBindType((byte) 1);	
				}else if(item instanceof EquipmentTemplate){//装备
					((EquipmentTemplate) item).setBindType((byte) 2);
				}
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}
        }
        
        if(item!=null){
            int price = Integer.parseInt(Utils.getWholeDataPrice(n.attributeValue("price")));
            int contribute = Integer.parseInt(n.attributeValue("contribute"));
            int count = Integer.parseInt(n.attributeValue("count"));
            String desc = n.attributeValue("desc");
            String consumeCode = null;
            Attribute att = n.attribute("consumecode");
            if(att!=null){
                consumeCode = att.getValue();
                if(consumeCode.length()==0)
                    consumeCode = null;
            }
            IStoreItem storeItem = new IStoreItem();
            storeItem.item = item;
            storeItem.price = price;
            storeItem.desc = desc;
            storeItem.count = count;
            storeItem.consumeCode = consumeCode;
            storeItem.contribute = contribute;
            l.add(storeItem);
        }
    }
    IStoreItem[] items = new IStoreItem[l.size()];
    l.toArray(items);
    return items;
}
}
