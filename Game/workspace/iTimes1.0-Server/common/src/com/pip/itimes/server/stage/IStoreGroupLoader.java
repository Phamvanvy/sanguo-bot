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

public class IStoreGroupLoader {
    public IStoreGroupLoader(File file,int iMoneyType,int IMONEY_TYPE_CMCC) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadStoreGroups(doc,iMoneyType,IMONEY_TYPE_CMCC);
    }

    private void loadStoreGroups(Document doc,int iMoneyType,int IMONEY_TYPE_CMCC){
        Element root = doc.getRootElement();
        Element elementMessage = root.element("Message");
        if(elementMessage != null){
	        String message = elementMessage.attributeValue("value");
	        if(message != null){
	        	IStoreGroups.setMessage(message);
	        }else{
	        	IStoreGroups.setMessage("");
	        }
        }
        for(Iterator i=root.elementIterator("group");i.hasNext();){
            Element node = (Element)i.next();
            String name = node.attributeValue("name");
            String strHide = node.attributeValue("hide");
            boolean hide = false;
            if(strHide != null){
            	if(strHide.equals("true")){
            		hide = true;
            	}
            }
            IStoreItem[] items;       
            items = loadItems(node,iMoneyType,IMONEY_TYPE_CMCC);
            IStoreGroup group = new IStoreGroup(name, hide, items);
            IStoreGroups.addGroup(group);
           
            
        }
    }

    private IStoreItem[] loadItems(Element node,int iMoneyType,int IMONEY_TYPE_CMCC){
    	int allDiscount = 100;
    	String strAllDiscount = node.attributeValue("discount");
    	if(strAllDiscount != null){
    		allDiscount = Integer.parseInt(strAllDiscount);
    	}
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
                int discount = 100;
                String strDiscount = n.attributeValue("discount");
                if(strDiscount != null){
                	discount = Integer.parseInt(strDiscount);
                }else{
                	discount = allDiscount;
                }
                String desc = n.attributeValue("desc");
                String consumeCode = null;
                Attribute att = n.attribute("consumecode");
                if(att!=null){
                    consumeCode = att.getValue();
                    if(consumeCode.length()==0)
                        consumeCode = null;
                }
                ArrayList<IStoreTime> times = new ArrayList<IStoreTime>();
                Element time = n.element("time");
                if(time != null){
                	for(Iterator j=n.elementIterator("time");j.hasNext();){
                		Element time1 = (Element)j.next();
                		IStoreTime iStoreTime = new IStoreTime();
                		iStoreTime.setStart(time1.attributeValue("start"));
                		iStoreTime.setEnd(time1.attributeValue("end"));
                		iStoreTime.setCount(Integer.parseInt(time1.attributeValue("count")));;
                		iStoreTime.setPrice(Integer.parseInt(Utils.getWholeDataPrice(time1.attributeValue("price"))));
                		times.add(iStoreTime);
                	}
                }
                IStoreItem storeItem = new IStoreItem();
                storeItem.item = item;
                storeItem.price = price;
                storeItem.desc = desc;
                storeItem.count = count;
                storeItem.consumeCode = consumeCode;
                storeItem.discount = discount;
                storeItem.times = times;
                l.add(storeItem);
            }
        }
        IStoreItem[] items = new IStoreItem[l.size()];
        l.toArray(items);
        return items;
    }
   
}
