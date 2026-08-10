package com.pip.itimes.server.stage;

import java.io.File;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.Attribute;

import com.pip.itimes.server.util.Utils;

public class IStoreGroupLoader2 {
    public IStoreGroupLoader2(File file,int iMoneyType,int IMONEY_TYPE_CMCC) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadStoreGroups(doc,iMoneyType,IMONEY_TYPE_CMCC);
    }

    private void loadStoreGroups(Document doc,int iMoneyType,int IMONEY_TYPE_CMCC){
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("group");i.hasNext();){
            Element node = (Element)i.next();
            String name = node.attributeValue("name");
            IStoreItem[] items = loadItems(node,iMoneyType,IMONEY_TYPE_CMCC);
            IStoreGroup group = new IStoreGroup(name, false, items);
            IStoreGroups2.addGroup(group);
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
                l.add(storeItem);
            }
        }
        IStoreItem[] items = new IStoreItem[l.size()];
        l.toArray(items);
        return items;
    }
}
