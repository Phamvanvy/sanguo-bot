package com.pip.itimes.server.stage;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class RandomMessageLoader {
	public RandomMessageLoader(File file) throws Exception{
	    SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadRandomMessageLoader(doc);
	}
	
	public void loadRandomMessageLoader(Document doc){
		Element root = doc.getRootElement();
		RandomMessage.randomMessageMap.clear();
		RandomMessage.sexIndexMap.clear();
		for(Iterator i=root.elementIterator("randomMessage");i.hasNext();){
            Element node = (Element)i.next();
            String key = node.attributeValue("key");
            for(Iterator ite=node.elementIterator("message");ite.hasNext(); ){
            	Element subNode = (Element)ite.next();
            	byte sex = Byte.parseByte(subNode.attributeValue("sex"));
            	String messageValue = subNode.attributeValue("value");
            	RandomMessage rm = new RandomMessage(sex,messageValue);
            	RandomMessage.addRandomMessage(key, rm);
            }
		}
	}
}
