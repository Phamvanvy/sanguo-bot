package com.pip.itimes.server.world.riddles;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class RiddlesLoader {
	public RiddlesLoader(File riddlesFile) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(riddlesFile);
        Element root = doc.getRootElement();
        loadRiddles(root);
        RiddlesConfig.resetTime();
        RiddlesConfig.reload();
	}
	
	public void loadRiddles(Element root){
		Element riddles = root.element("Start");
		RiddlesConfig.startYear = Integer.parseInt(riddles.attributeValue("Year"));
		RiddlesConfig.startMonth = Integer.parseInt(riddles.attributeValue("Month"));
		RiddlesConfig.startDay = Integer.parseInt(riddles.attributeValue("Day"));
		riddles = root.element("End");
		RiddlesConfig.endYear = Integer.parseInt(riddles.attributeValue("Year"));
		RiddlesConfig.endMonth = Integer.parseInt(riddles.attributeValue("Month"));
		RiddlesConfig.endDay = Integer.parseInt(riddles.attributeValue("Day"));
		riddles = root.element("Day");
		RiddlesConfig.startHour = Integer.parseInt(riddles.attributeValue("StartHour"));
		RiddlesConfig.startMinute = Integer.parseInt(riddles.attributeValue("StartMinute"));
		RiddlesConfig.startSecond = Integer.parseInt(riddles.attributeValue("StartSecond"));
		RiddlesConfig.endHour = Integer.parseInt(riddles.attributeValue("EndHour"));
		RiddlesConfig.endMinute = Integer.parseInt(riddles.attributeValue("EndMinute"));
		RiddlesConfig.endSecond = Integer.parseInt(riddles.attributeValue("EndSecond"));
		RiddlesConfig.hourTime = Integer.parseInt(riddles.attributeValue("HourTime"));
		long minutemillisecond = 1000 * 60;
		RiddlesConfig.TIME = Integer.parseInt(riddles.attributeValue("ActionMinute")) * minutemillisecond;
		RiddlesConfig.HOURTIME = Integer.parseInt(riddles.attributeValue("ActionNext")) * minutemillisecond;
		RiddlesConfig.TIME5 = Integer.parseInt(riddles.attributeValue("ActionAd")) * minutemillisecond;
		
		ArrayList<Riddles> riddlesList = new ArrayList<Riddles>();
		riddles = root.element("AllRiddles");
		for (Iterator<Element> messages = riddles.elementIterator("Riddle"); messages.hasNext();) {
			Element el = (Element)messages.next();
			Riddles riddle = new Riddles();
			riddle.setQuestion(el.attributeValue("question"));
			riddle.setAnswer(el.attributeValue("answer"));
			riddlesList.add(riddle);
		}
		if(RiddlesConfig.riddles != null){
			synchronized (RiddlesConfig.riddles) {
				RiddlesConfig.riddles.clear();
				RiddlesConfig.riddles = riddlesList;
			}
		}else{
			RiddlesConfig.riddles = riddlesList;
		}
	}
}
