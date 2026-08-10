package com.pip.itimes.server.world.message;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class WelcomeMessageLoader {
	private static SimpleDateFormat format = new SimpleDateFormat ("yyyy-MM-dd");
	
	public WelcomeMessageLoader(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        loadRiddles(root);
	}
	
	public void loadRiddles(Element root){
		synchronized (WelcomeMessage.channelMessage) {
			WelcomeMessage.clearMessage();
			for (Iterator<Element> channels = root.elementIterator("Channel"); channels.hasNext();) {
				Element el = (Element)channels.next();
				String channel = el.attributeValue("channel");
				String message = el.attributeValue("message");
				WelcomeMessage.addMessage(channel, message);
			}
		}
		synchronized (WelcomeMessage.channelGift) {
			WelcomeMessage.clearGift();
			for (Iterator<Element> channels = root.elementIterator("ChannelRegGift"); channels.hasNext();) {
				Element el = (Element)channels.next();
				String channel = el.attributeValue("channel");
				String start = el.attributeValue("start");
				String end = el.attributeValue("end");
				ChannelGiftData giftData = new ChannelGiftData();
				try{
					giftData.setStartTime(format.parse(start).getTime());
					giftData.setEndTime(format.parse(end).getTime() + 24*60*60*1000L - 1);
				}catch(Exception e){
				}
				ArrayList<String> lstItemids = new ArrayList<String>();
				for (Iterator<Element> gifts = el.elementIterator("Gift"); gifts.hasNext();) {
					Element el2 = (Element)gifts.next();
					lstItemids.add(el2.attributeValue("itemid"));
				}
				int[] itemids = new int[lstItemids.size()];
				for(int i=0; i<itemids.length; i++){
					itemids[i] = Integer.parseInt(lstItemids.get(i));
				}
				giftData.setItemId(itemids);
				WelcomeMessage.addChannelGift(channel, giftData);
			}
		}
	}
}
