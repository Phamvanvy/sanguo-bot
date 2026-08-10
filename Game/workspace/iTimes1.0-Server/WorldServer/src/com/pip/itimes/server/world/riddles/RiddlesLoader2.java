package com.pip.itimes.server.world.riddles;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class RiddlesLoader2 {
	public RiddlesLoader2(File riddlesFile) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(riddlesFile);
        Element root = doc.getRootElement();
        loadRiddles(root);
        RiddlesConfig2.resetTime();
        RiddlesConfig2.reload();
	}
	
	public void loadRiddles(Element root){
		Element riddles = root.element("Start");
		RiddlesConfig2.startYear = Integer.parseInt(riddles.attributeValue("Year"));
		RiddlesConfig2.startMonth = Integer.parseInt(riddles.attributeValue("Month"));
		RiddlesConfig2.startDay = Integer.parseInt(riddles.attributeValue("Day"));
		riddles = root.element("End");
		RiddlesConfig2.endYear = Integer.parseInt(riddles.attributeValue("Year"));
		RiddlesConfig2.endMonth = Integer.parseInt(riddles.attributeValue("Month"));
		RiddlesConfig2.endDay = Integer.parseInt(riddles.attributeValue("Day"));
		riddles = root.element("Day");
		RiddlesConfig2.startHour = Integer.parseInt(riddles.attributeValue("StartHour"));
		RiddlesConfig2.startMinute = Integer.parseInt(riddles.attributeValue("StartMinute"));
		RiddlesConfig2.startSecond = Integer.parseInt(riddles.attributeValue("StartSecond"));
		RiddlesConfig2.endHour = Integer.parseInt(riddles.attributeValue("EndHour"));
		RiddlesConfig2.endMinute = Integer.parseInt(riddles.attributeValue("EndMinute"));
		RiddlesConfig2.endSecond = Integer.parseInt(riddles.attributeValue("EndSecond"));
		RiddlesConfig2.hourTime = Integer.parseInt(riddles.attributeValue("HourTime"));
		long minutemillisecond = 1000 * 60;
		RiddlesConfig2.TIME = Integer.parseInt(riddles.attributeValue("ActionMinute")) * minutemillisecond;
		RiddlesConfig2.HOURTIME = Integer.parseInt(riddles.attributeValue("ActionNext")) * minutemillisecond;
		RiddlesConfig2.TIME5 = Integer.parseInt(riddles.attributeValue("ActionAd")) * minutemillisecond;
		
		ArrayList<Riddles> riddlesList = new ArrayList<Riddles>();
		riddles = root.element("AllRiddles");
		for (Iterator<Element> messages = riddles.elementIterator("Riddle"); messages.hasNext();) {
			Element el = (Element)messages.next();
			Riddles riddle = new Riddles();
			riddle.setQuestion(el.attributeValue("question"));
			riddle.setAnswer(el.attributeValue("answer"));
			riddlesList.add(riddle);
		}
		if(RiddlesConfig2.riddles != null){
			synchronized (RiddlesConfig2.riddles) {
				RiddlesConfig2.riddles.clear();
				RiddlesConfig2.riddles = riddlesList;
			}
		}else{
			RiddlesConfig2.riddles = riddlesList;
		}
	}
}
