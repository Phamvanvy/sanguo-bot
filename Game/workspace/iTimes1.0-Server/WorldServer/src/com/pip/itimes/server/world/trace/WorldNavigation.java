package com.pip.itimes.server.world.trace;

import java.io.File;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class WorldNavigation {
	public static HashMap<Integer,HashMap<Integer,Integer>> highWay = new HashMap<Integer,HashMap<Integer,Integer>>();
	public static int getNearCityIdTo(int fromMapId, int toMapId) {
		HashMap<Integer,Integer> m = highWay.get(Integer.valueOf(fromMapId));
		if (m != null) {
			Integer ret = m.get(Integer.valueOf(toMapId));
			if (ret != null) {
				return ret.intValue();
			}
		}
		return 0;
	}
	public static void load(File file) {
		highWay.clear();
		SAXReader reader = new SAXReader();
        try {
			Document document = reader.read(file);
			load(document);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	private static void load(Document doc) {
		Element root = doc.getRootElement();
		Iterator<Element> rows = root.elementIterator("city");
		while (rows.hasNext()) {
			Element city = rows.next();
			int id = Integer.parseInt(city.attributeValue("id"));
			HashMap<Integer,Integer> m = new HashMap<Integer,Integer>();
			Iterator<Element> roads = city.elementIterator("road");
			while (roads.hasNext()) {
				Element road = roads.next();
				int toId = Integer.parseInt(road.attributeValue("to"));
				int via = Integer.parseInt(road.attributeValue("via"));
				m.put(Integer.valueOf(toId), Integer.valueOf(via));
			}
			if (m.size() > 0) {
				highWay.put(Integer.valueOf(id), m);
			}
		}
	}
}
