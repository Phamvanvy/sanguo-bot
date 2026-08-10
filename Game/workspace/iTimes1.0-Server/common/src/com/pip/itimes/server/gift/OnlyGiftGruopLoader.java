package com.pip.itimes.server.gift;


import java.io.File;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class OnlyGiftGruopLoader{
   
	 public OnlyGiftGruopLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadOnlyGiftGroups(doc);
    }
    private void loadOnlyGiftGroups(Document doc){
        Element root = doc.getRootElement();

        OnlyGiftGroups.clearOnlyGiftGroups();

        if(root == null){
            return;
        }

        for(Iterator i = root.elementIterator("onlygiftgroup"); i.hasNext();){
            Element giftGoupNode = (Element)i.next();
            Attribute attrGiftGroup = giftGoupNode.attribute("valid");
            boolean valid = attrGiftGroup.getValue().equals("true");
            if(valid == true){
            	OnlyGiftGroup onlyGiftGroup = new OnlyGiftGroup(valid);
            	
            	attrGiftGroup = giftGoupNode.attribute("id");
            	onlyGiftGroup.setId(Integer.parseInt(attrGiftGroup.getValue()));
            	
            	attrGiftGroup = giftGoupNode.attribute("beginlevel");
            	onlyGiftGroup.setBeginLevel(Integer.parseInt(attrGiftGroup.getValue()));
            	
            	attrGiftGroup = giftGoupNode.attribute("endlevel");
            	onlyGiftGroup.setEndLevel(Integer.parseInt(attrGiftGroup.getValue()));
            	
            	attrGiftGroup = giftGoupNode.attribute("maxcount");
            	onlyGiftGroup.setMaxCount(Integer.parseInt(attrGiftGroup.getValue()));
            	
            	onlyGiftGroup.setMessage_error(getMessage("message_error", giftGoupNode));
            	onlyGiftGroup.setMessage_maxcount(getMessage("message_maxcount", giftGoupNode));
            	onlyGiftGroup.setMessage_give(getMessage("message_give", giftGoupNode));
            	onlyGiftGroup.setMessage_bag(getMessage("message_bag", giftGoupNode));
            	onlyGiftGroup.setMessage_title(getMessage("message_title", giftGoupNode));
            	onlyGiftGroup.setMessage_content(getMessage("message_content", giftGoupNode));
            	for(Iterator j = giftGoupNode.elementIterator("gift"); j.hasNext();){
            		 Element giftNode = (Element)j.next();
                     OnlyGiftDefine onlyGiftDefine = new OnlyGiftDefine();
                     
                     Attribute attrGift = giftNode.attribute("id");
                     onlyGiftDefine.setId(Integer.parseInt(attrGift.getValue()));
                    
                     for(Iterator k = giftNode.elementIterator("needitem"); k.hasNext();){
                         Element giftItemNode = (Element)k.next();
                         
                         Attribute attrGiftItem = giftItemNode.attribute("id");
                         int id = Integer.parseInt(attrGiftItem.getValue());
                         
                         attrGiftItem = giftItemNode.attribute("type");
                         int type = Integer.parseInt(attrGiftItem.getValue());
                         
                         attrGiftItem = giftItemNode.attribute("yeartype");
                         int yeartype = Integer.parseInt(attrGiftItem.getValue());
                         
                         attrGiftItem = giftItemNode.attribute("enhancecout");
                         int enhancecout = Integer.parseInt(attrGiftItem.getValue());
                         
                         OnlyGiftNeedItem onlyGiftNeedItem = new OnlyGiftNeedItem(id, type ,yeartype, enhancecout);
                         onlyGiftDefine.addNeedItem(onlyGiftNeedItem);
                     }
                     for(Iterator k = giftNode.elementIterator("giveitem"); k.hasNext();){
                         Element giftItemNode = (Element)k.next();
                         
                         Attribute attrGiftItem = giftItemNode.attribute("itemid");
                         int itemId = Integer.parseInt(attrGiftItem.getValue());
                         
                         attrGiftItem = giftItemNode.attribute("count");
                         int count = Integer.parseInt(attrGiftItem.getValue());
                         
                         onlyGiftDefine.addGiveItems(itemId, count);
                         
                     }
                     onlyGiftGroup.addOnlyGift(onlyGiftDefine);
                     
            	}
            	OnlyGiftGroups.addOnlyGiftGroup(onlyGiftGroup);
            }
            
            
           
        }
    }
    public String getMessage(String nodeName, Element groupNode){
        Element messageNode = groupNode.element(nodeName);
        return messageNode.attributeValue("value");
    }
}
