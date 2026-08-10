package com.pip.itimes.server.stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class MonsterConstants {
	private static ArrayList<FallItem[]> holiday_world_fall = new ArrayList<FallItem[]>();
//	public static int HOLIDAY_WORLD_FALL_1_ID[] = {302};
//	public static int HOLIDAY_WORLD_FALL_1_CHANCE[] = {50000};
//	public static int HOLIDAY_WORLD_FALL_2_ID[] = {302};
//	public static int HOLIDAY_WORLD_FALL_2_CHANCE[] = {50000};
//	public static int HOLIDAY_WORLD_FALL_3_ID[] = {302};
//	public static int HOLIDAY_WORLD_FALL_3_CHANCE[] = {50000};
	
//	public static int HOLIDAY_WORLD_FALL2_1_ID = 302;
//	public static int HOLIDAY_WORLD_FALL2_1_CHANCE = 50000;
//	public static int HOLIDAY_WORLD_FALL2_2_ID = 302;
//	public static int HOLIDAY_WORLD_FALL2_2_CHANCE = 50000;
//	public static int HOLIDAY_WORLD_FALL2_3_ID = 302;
//	public static int HOLIDAY_WORLD_FALL2_3_CHANCE = 50000;
	
	public static void loadMonsterConstants(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        load(root);
	}
	
	private static void load(Element root){
		synchronized (holiday_world_fall) {
			holiday_world_fall.clear();
			for (Iterator<Element> chats = root.elementIterator("HolidayConstants"); chats.hasNext();) {
				Element em = chats.next();
				int id1 = Integer.parseInt(em.attributeValue("FALL_1_ID"));
				int chance1 = Integer.parseInt(em.attributeValue("FALL_1_CHANCE"));
				int id2 = Integer.parseInt(em.attributeValue("FALL_2_ID"));
				int chance2 = Integer.parseInt(em.attributeValue("FALL_2_CHANCE"));
				int id3 = Integer.parseInt(em.attributeValue("FALL_3_ID"));
				int chance3 = Integer.parseInt(em.attributeValue("FALL_3_CHANCE"));
				FallItem HOLIDAY_WORLD_FALL_1 = new FallItem((byte) 8, 0, 0, id1, chance1);
				FallItem HOLIDAY_WORLD_FALL_2 = new FallItem((byte) 8, 0, 0, id2, chance2);
				FallItem HOLIDAY_WORLD_FALL_3 = new FallItem((byte) 8, 0, 0, id3, chance3);
				FallItem[] falls = {null,null, HOLIDAY_WORLD_FALL_1, HOLIDAY_WORLD_FALL_1, HOLIDAY_WORLD_FALL_1, 
					HOLIDAY_WORLD_FALL_2, HOLIDAY_WORLD_FALL_2,HOLIDAY_WORLD_FALL_2,
					HOLIDAY_WORLD_FALL_3,HOLIDAY_WORLD_FALL_3};
				holiday_world_fall.add(falls);
			}
		}
	}
	
	public static ArrayList<FallItem[]> getHolidayWorldFall(){
		return holiday_world_fall;
	}
	
}
