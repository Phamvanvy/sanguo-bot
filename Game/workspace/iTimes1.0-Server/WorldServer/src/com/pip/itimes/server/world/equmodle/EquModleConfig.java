package com.pip.itimes.server.world.equmodle;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.stage.DiamondMosaic;
import com.pip.itimes.server.stage.IEquipment;

public class EquModleConfig {
	
	private static HashMap<Integer, EquModleData> equModle = new HashMap<Integer, EquModleData>();
	
	public static EquModleData getEquModle(int id){
		if(equModle.containsKey(id)){
			return equModle.get(id);
		}
		return null;
	}
	
	public static void load(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        load(root);
	}
	
	private static void load(Element root){
		synchronized (equModle) {
			equModle.clear();
			for (Iterator<Element> element = root.elementIterator("effect"); element.hasNext();) {
				Element effect = element.next();
				int id = Integer.parseInt(effect.attributeValue("id"));
				int diamondcount = Integer.parseInt(effect.attributeValue("diamondcount"));
				int viany_stone = Integer.parseInt(effect.attributeValue("viany_stone"));
				int viany_scissors = Integer.parseInt(effect.attributeValue("viany_scissors"));
				int viany_paper = Integer.parseInt(effect.attributeValue("viany_paper"));
				EquModleData ed = new EquModleData();
				ed.setId(id);
				ed.setDiamondcount(diamondcount);
				ed.setVianystone(viany_stone);
				ed.setVianyscissors(viany_scissors);
				ed.setVianypaper(viany_paper);
				Element element1 = effect.element("hole");
				int index = 0;
				for (Iterator<Element> element2 = element1.elementIterator("stone"); element2.hasNext();) {
					Element stone = element2.next();
					int stoneid = Integer.parseInt(stone.attributeValue("id"));
					byte diamondMosaicEmbedLevel = DiamondMosaic.findDiamondMosaicLevel(stoneid);
					ed.setDiamondMosiacRoleInfo(index, (byte)(IEquipment.CURRENT_EQU_CANDIAMOND + diamondMosaicEmbedLevel));
					ed.setDiamondStoneId(index, stoneid);
					index++;
				}
				if(index == 0){
					ed.setDiamondMosiacRoleInfo(index, (byte)(IEquipment.CURRENT_EQU_CANDIAMOND));
					index = 1;
				}
				ed.setOpenDiamondCount((byte)index);
				Element element3 = effect.element("enchance");
				int[] enchances = new int[9 * 2];
				index = 0;
				for (Iterator<Element> element4 = element3.elementIterator("type"); element4.hasNext();) {
					Element type = element4.next();
					int etype = Integer.parseInt(type.attributeValue("type"));
					int value = Integer.parseInt(type.attributeValue("value"));
					enchances[index * 2] = etype;
					enchances[index * 2 + 1] = value;
					index ++;
				}
				int[] copyEnchances = new int[index * 2];
				System.arraycopy(enchances, 0, copyEnchances, 0, copyEnchances.length);
				ed.setEnchances(copyEnchances);
				equModle.put(id, ed);
			}
		}
	}
}
