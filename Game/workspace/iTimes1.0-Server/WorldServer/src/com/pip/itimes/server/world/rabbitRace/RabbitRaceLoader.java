package com.pip.itimes.server.world.rabbitRace;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class RabbitRaceLoader {
	public RabbitRaceLoader(File rabbitRaceFile) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(rabbitRaceFile);
		Element root = doc.getRootElement();
		loadRabbitRace(root);
		RabbitRaceConfig.resetTime();
		RabbitRaceConfig.reload();
	}

	private void loadRabbitRace(Element root) {
		Element day = root.element("Day");
		RabbitRaceConfig.startHour = Integer.parseInt(day
				.attributeValue("StartHour"));
		RabbitRaceConfig.startMinute = Integer.parseInt(day
				.attributeValue("StartMinute"));
		RabbitRaceConfig.startSecond = Integer.parseInt(day
				.attributeValue("StartSecond"));
		RabbitRaceConfig.endHour = Integer.parseInt(day
				.attributeValue("EndHour"));
		RabbitRaceConfig.endMinute = Integer.parseInt(day
				.attributeValue("EndMinute"));
		RabbitRaceConfig.endSecond = Integer.parseInt(day
				.attributeValue("EndSecond"));
		RabbitRaceConfig.actionClose = Integer.parseInt(day
				.attributeValue("ActionClose"));
		// RabbitRaceConfig.raceMaxNum =
		// Integer.parseInt(day.attributeValue("HourTime"));

		// long minutemillisecond = 1000 * 60;
		// RabbitRaceConfig.TIME =
		// Integer.parseInt(day.attributeValue("ActionMinute")) *
		// minutemillisecond;
		// RabbitRaceConfig.HOURTIME =
		// Integer.parseInt(day.attributeValue("ActionNext")) *
		// minutemillisecond;
		// RabbitRaceConfig.TIME5 =
		// Integer.parseInt(day.attributeValue("ActionAd")) * minutemillisecond;
	}
}
