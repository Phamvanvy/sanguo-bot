package com.pip.itimes.server.gift;


import java.io.File;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;



public class GiftGroupLoader{
    public GiftGroupLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadGiftGroups(doc);
    }

    private void loadGiftGroups(Document doc){
        Element root = doc.getRootElement();

        GiftGroups.clearGiftGroups();

        if(root == null){
            return;
        }

        for(Iterator i = root.elementIterator("giftgroup"); i.hasNext();){
            Element giftGoupNode = (Element)i.next();
            Attribute attrGiftGroup = giftGoupNode.attribute("valid");
            boolean valid = attrGiftGroup.getValue().equals("true");
            
            GiftGroup giftGroup = new GiftGroup(valid);
            
            attrGiftGroup = giftGoupNode.attribute("id");
            giftGroup.setId(Integer.parseInt(attrGiftGroup.getValue()));

            attrGiftGroup = giftGoupNode.attribute("begindate");
            giftGroup.setBeginTime(Long.parseLong(attrGiftGroup.getValue()));

            attrGiftGroup = giftGoupNode.attribute("enddate");
            giftGroup.setEndTime(Long.parseLong(attrGiftGroup.getValue()));

            attrGiftGroup = giftGoupNode.attribute("canseetype");
            giftGroup.setCanSeeType(Integer.parseInt(attrGiftGroup.getValue()));

            attrGiftGroup = giftGoupNode.attribute("givetype");
            giftGroup.setGiveType(Integer.parseInt(attrGiftGroup.getValue()));
            
            attrGiftGroup = giftGoupNode.attribute("directwayCanSee");
            boolean directwayCanSee = attrGiftGroup.getValue().equals("true");
            giftGroup.setDirectwayCanSee(directwayCanSee);
            
            attrGiftGroup = giftGoupNode.attribute("versionvalid");
            boolean versionvalid = attrGiftGroup.getValue().equals("true");
            giftGroup.setVersionValid(versionvalid);
            
            giftGroup.setMessage_error(getMessage("message_error", giftGoupNode));
            giftGroup.setMessage_group(getMessage("message_group", giftGoupNode));
            giftGroup.setMessage_gift(getMessage("message_gift", giftGoupNode));
            giftGroup.setMessage_count(getMessage("message_count", giftGoupNode));
            if(getMessage("message_allcount", giftGoupNode) != null){
            	giftGroup.setMessage_allcount(getMessage("message_allcount", giftGoupNode));
            }
            giftGroup.setMessage_repeat(getMessage("message_repeat", giftGoupNode));
            giftGroup.setMessage_time(getMessage("message_time", giftGoupNode));
            giftGroup.setMessage_item(getMessage("message_item", giftGoupNode));
            giftGroup.setMessage_give(getMessage("message_give", giftGoupNode));
            giftGroup.setMessage_bag(getMessage("message_bag", giftGoupNode));
            giftGroup.setMessage_mail(getMessage("message_mail", giftGoupNode));
            giftGroup.setMessage_about(getMessage("message_about", giftGoupNode));
            giftGroup.setMessage_aboutmsg(getMessage("message_aboutmsg", giftGoupNode));

            for(Iterator j = giftGoupNode.elementIterator("gift"); j.hasNext();){
                Element giftNode = (Element)j.next();
                GiftDefine gift = new GiftDefine();

                Attribute attrGift = giftNode.attribute("id");
                gift.setId(Integer.parseInt(attrGift.getValue()));
                
                attrGift = giftNode.attribute("beginlevel");
                gift.setBeginLevel(Integer.parseInt(attrGift.getValue()));

                attrGift = giftNode.attribute("endlevel");
                gift.setEndLevel(Integer.parseInt(attrGift.getValue()));

                attrGift = giftNode.attribute("max");
                gift.setMaxCount(Integer.parseInt(attrGift.getValue()));
                
                attrGift = giftNode.attribute("allcount");
                if(attrGift != null){
                	gift.setAllCount(Integer.parseInt(attrGift.getValue()));
                }else{
                	gift.setAllCount(-1);
                }
                
                attrGift = giftNode.attribute("repeat");
                gift.setMaxRepeat(Integer.parseInt(attrGift.getValue()));

                attrGift = giftNode.attribute("timespace");
                if ("W".equalsIgnoreCase(attrGift.getValue())){
                	gift.setTimeSpace(-2);
                }else if ("M".equalsIgnoreCase(attrGift.getValue())){
                	gift.setTimeSpace(-3);
                }else if ("Y".equalsIgnoreCase(attrGift.getValue())){
                	gift.setTimeSpace(-4);
                }else{
                	gift.setTimeSpace(Integer.parseInt(attrGift.getValue()));
                }
                attrGift = giftNode.attribute("begintime");
                gift.setBeginTime(Integer.parseInt(attrGift.getValue()));

                attrGift = giftNode.attribute("endtime");
                gift.setEndTime(Integer.parseInt(attrGift.getValue()));

                for(Iterator k = giftNode.elementIterator("needitem"); k.hasNext();){
                    Element giftItemNode = (Element)k.next();
                    
//                    Attribute attrGiftItem = giftItemNode.attribute("itemid");
//                    int itemId = Integer.parseInt(attrGiftItem.getValue());
                    
                    int[] itemId = parseIntArrayString(giftItemNode.attributeValue("itemid"));
                    
                    Attribute attrGiftItem = giftItemNode.attribute("count");
                    int count = Integer.parseInt(attrGiftItem.getValue());
                    
                    String strPercent = giftItemNode.attributeValue("percent");
                    int[] itempercent = null;
                    if(strPercent != null){
                    	itempercent = parseIntArrayString(strPercent);
                    }
                    
                    gift.addNeedItem(itemId, count, itempercent);
                }

                for(Iterator k = giftNode.elementIterator("giveitem"); k.hasNext();){
                    Element giftItemNode = (Element)k.next();
                    
                    Attribute attrGiftItem = giftItemNode.attribute("itemid");
                    int itemId = Integer.parseInt(attrGiftItem.getValue());
                    
                    attrGiftItem = giftItemNode.attribute("count");
                    int count = Integer.parseInt(attrGiftItem.getValue());
                    
                    gift.addGiveItems(itemId, count);
                }
                
                giftGroup.addGift(gift);
            }

            GiftGroups.addGiftGroup(giftGroup);
        }
    }
    private int[] parseIntArrayString(String s){
        String[] ss = s.split(",");
        int[] ret = new int[ss.length];
        for(int i=0;i<ss.length;i++){
            ret[i] = Integer.parseInt(ss[i]);
        }
        return ret;
    }
    
    public String getMessage(String nodeName, Element groupNode){
        Element messageNode = groupNode.element(nodeName);
        if(messageNode == null) return null;
        return messageNode.attributeValue("value");
    }
}
