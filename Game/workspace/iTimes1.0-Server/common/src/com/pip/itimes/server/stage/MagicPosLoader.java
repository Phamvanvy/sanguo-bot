package com.pip.itimes.server.stage;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class MagicPosLoader {
	
	public MagicPosLoader(File file)throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadMagicPositon(doc);
	}

	public void loadMagicPositon(Document doc){
		Element root = doc.getRootElement();
		MagicPosMessage.magicPositionMessageMap.clear();
		for(Iterator i=root.elementIterator("watermagic");i.hasNext();){
            Element node = (Element)i.next();
            int type = Integer.parseInt(node.attributeValue("type"));//阵眼类型
            for(Iterator j=node.elementIterator("magiclevel");j.hasNext();){
        		Element node2 = (Element)j.next();
        		int level = Integer.parseInt(node2.attributeValue("level"));//阵眼等级
        		for(Iterator k = node2.elementIterator("magicfloor");k.hasNext();){
        			Element node3 = (Element)k.next();
        			int floor = Integer.parseInt(node3.attributeValue("floor"));//阶层
        			int exp = Integer.parseInt(node3.attributeValue("exp"));
        			int attackpoint = Integer.parseInt(node3.attributeValue("attack"));
        			int mattackpoint = Integer.parseInt(node3.attributeValue("mattack"));
        			MagicPosMessage mpm = new MagicPosMessage(level,floor, exp, attackpoint, mattackpoint, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        			MagicPosMessage.addMagicPosMessage(type,mpm);
        		}
        	}
		}
		for(Iterator i=root.elementIterator("soilmagic");i.hasNext();){
            Element node = (Element)i.next();
            int type = Integer.parseInt(node.attributeValue("type"));//阵眼类型
            for(Iterator j=node.elementIterator("magiclevel");j.hasNext();){
        		Element node2 = (Element)j.next();
        		int level = Integer.parseInt(node2.attributeValue("level"));//阵眼等级
        		for(Iterator k = node2.elementIterator("magicfloor");k.hasNext();){
        			Element node3 = (Element)k.next();
        			int floor = Integer.parseInt(node3.attributeValue("floor"));//阶层
        			int exp = Integer.parseInt(node3.attributeValue("exp"));
        			int pdef = Integer.parseInt(node3.attributeValue("pdef"));
        			int mdef = Integer.parseInt(node3.attributeValue("mdef"));
        			MagicPosMessage mpm = new MagicPosMessage(level, floor, exp, 0, 0, pdef, mdef, 0, 0, 0, 0, 0, 0, 0);
        			MagicPosMessage.addMagicPosMessage(type,mpm);
        		}
        	}
		}
		for(Iterator i=root.elementIterator("firemagic");i.hasNext();){
            Element node = (Element)i.next();
            int type = Integer.parseInt(node.attributeValue("type"));//阵眼类型
            for(Iterator j=node.elementIterator("magiclevel");j.hasNext();){
        		Element node2 = (Element)j.next();
        		int level = Integer.parseInt(node2.attributeValue("level"));//阵眼等级
        		for(Iterator k = node2.elementIterator("magicfloor");k.hasNext();){
        			Element node3 = (Element)k.next();
        			int floor = Integer.parseInt(node3.attributeValue("floor"));//阶层
        			int exp = Integer.parseInt(node3.attributeValue("exp"));
        			int hit = Integer.parseInt(node3.attributeValue("hit"));
        			int pcri = Integer.parseInt(node3.attributeValue("pcri"));
        			int mcri = Integer.parseInt(node3.attributeValue("mcri"));
        			MagicPosMessage mpm = new MagicPosMessage(level, floor, exp, 0, 0, 0, 0, hit, pcri, mcri, 0, 0, 0, 0);
        			MagicPosMessage.addMagicPosMessage(type,mpm);
        		}
        	}
		}
		for(Iterator i=root.elementIterator("windmagic");i.hasNext();){
            Element node = (Element)i.next();
            int type = Integer.parseInt(node.attributeValue("type"));//阵眼类型
            for(Iterator j=node.elementIterator("magiclevel");j.hasNext();){
        		Element node2 = (Element)j.next();
        		int level = Integer.parseInt(node2.attributeValue("level"));//阵眼等级
        		for(Iterator k = node2.elementIterator("magicfloor");k.hasNext();){
        			Element node3 = (Element)k.next();
        			int floor = Integer.parseInt(node3.attributeValue("floor"));//阶层
        			int exp = Integer.parseInt(node3.attributeValue("exp"));
        			int flee = Integer.parseInt(node3.attributeValue("flee"));
        			int nocri = Integer.parseInt(node3.attributeValue("nocri"));
        			MagicPosMessage mpm = new MagicPosMessage(level, floor, exp, 0, 0, 0, 0, 0, 0, 0, flee, nocri, 0, 0);
        			MagicPosMessage.addMagicPosMessage(type,mpm);
        		}
        	}
		}
		for(Iterator i=root.elementIterator("mindmagic");i.hasNext();){
            Element node = (Element)i.next();
            int type = Integer.parseInt(node.attributeValue("type"));//阵眼类型
            for(Iterator j=node.elementIterator("magiclevel");j.hasNext();){
        		Element node2 = (Element)j.next();
        		int level = Integer.parseInt(node2.attributeValue("level"));//阵眼等级
        		for(Iterator k = node2.elementIterator("magicfloor");k.hasNext();){
        			Element node3 = (Element)k.next();
        			int floor = Integer.parseInt(node3.attributeValue("floor"));//阶层
        			int exp = Integer.parseInt(node3.attributeValue("exp"));
        			int hp = Integer.parseInt(node3.attributeValue("hp"));
        			int mp = Integer.parseInt(node3.attributeValue("mp"));
        			MagicPosMessage mpm = new MagicPosMessage(level, floor, exp, 0, 0, 0, 0, 0, 0, 0, 0, 0, hp, mp);
        			MagicPosMessage.addMagicPosMessage(type,mpm);
        		}
        	}
		}
	}
}
