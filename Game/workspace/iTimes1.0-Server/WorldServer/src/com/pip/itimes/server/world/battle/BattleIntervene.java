package com.pip.itimes.server.world.battle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.stage.Ability;

public class BattleIntervene {
	public static boolean open = false;		//是否开启
	//可以使用的技能ID Value中存的是空值
	private static Map<Integer, Ability> mapSkillsID = new HashMap<Integer, Ability>();
	private static Map<Integer, InterveneVersion> mapVersions  = new HashMap<Integer, InterveneVersion>();
	
	public BattleIntervene(File file) throws Exception{
		load(file);
	}
	
	public void load(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        Element setup = root.element("Setup");
        open = Integer.parseInt(setup.attributeValue("open")) == 0 ? false : true;
        Element skills = root.element("Skills");
        synchronized (mapSkillsID) {
	        mapSkillsID.clear();
	        for (Iterator<Element> skill = skills.elementIterator("Skill"); skill.hasNext();) {
				Element el = (Element)skill.next();
				if(Integer.parseInt(el.attributeValue("use")) != 0){
					int id = Integer.parseInt(el.attributeValue("id"));
					if(!mapSkillsID.containsKey(id)){
						mapSkillsID.put(id, null);
					}else{
						throw new Exception("有重复的技能ID");
					}
				}
			}
        }
        Element versions = root.element("Versions");
        synchronized (mapVersions) {
        	mapVersions.clear();
	        for (Iterator<Element> version = versions.elementIterator("Version"); version.hasNext();) {
				Element el = (Element)version.next();
				InterveneVersion iv = new InterveneVersion();
				iv.setAge(Integer.parseInt(el.attributeValue("age")));
				iv.setRate(Integer.parseInt(el.attributeValue("rate")));
				mapVersions.put(iv.getAge(), iv);
			}
        }
	}
	
	public static int getRateInVersion(int age){
		if(mapVersions.containsKey(age)){
			return mapVersions.get(age).getRate();
		}else{
			return 0;
		}
	}
	
	public static boolean hasSkill(int id){
		return mapSkillsID.containsKey(id);
	}
	
}
