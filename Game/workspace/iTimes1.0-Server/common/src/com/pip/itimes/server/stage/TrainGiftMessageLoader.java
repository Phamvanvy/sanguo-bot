package com.pip.itimes.server.stage;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class TrainGiftMessageLoader {
	
	public static int trainPointAward = 0;

	public TrainGiftMessageLoader(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadTrainGift(doc);
	}

	public void loadTrainGift(Document doc){
		Element root = doc.getRootElement();
		for(Iterator i=root.elementIterator("TrainPointAward");i.hasNext();){
            Element node = (Element)i.next();
            int AwardItemId = Integer.parseInt(node.attributeValue("AwardItemId"));
            trainPointAward = AwardItemId;
		}
	}

}
