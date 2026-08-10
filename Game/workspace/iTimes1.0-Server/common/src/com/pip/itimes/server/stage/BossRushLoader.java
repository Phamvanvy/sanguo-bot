package com.pip.itimes.server.stage;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class BossRushLoader {
	public BossRushLoader(File file) throws Exception{
		SAXReader reader = new SAXReader();
	    Document doc = reader.read(file);
	    loadBossRush(doc);
	}
	
	public void loadBossRush(Document doc) throws Exception{
		Element root = doc.getRootElement();
		Element tmpNode = root.element("MaxStage");
		int maxStage = Integer.parseInt(tmpNode.attributeValue("value"));
		BossRush.setMaxStage(maxStage);
		for(Iterator i=root.elementIterator("Stages");i.hasNext();){
            Element node = (Element)i.next();
            short mainID = Short.parseShort(node.attributeValue("mainID"));
            loadBoss(node, mainID);
		}
	}
	
	private void loadBoss(Element node, short mainID){
		for(Iterator i=node.elementIterator("stage");i.hasNext();){
            Element n = (Element)i.next();
            short id = Short.parseShort(n.attributeValue("id"));
            int mgID = Integer.decode(n.attributeValue("MonsterGroupID"));
            short stage = (short)(mainID * 10 + id);
            BossRush br = new BossRush(stage, mgID);
            for(Iterator j =n.elementIterator("Monster");j.hasNext();){
            	Element tmpNode = (Element)j.next();
            	byte monsterID = (byte)(Integer.parseInt(tmpNode.attributeValue("monsterID")) - 1);
            	int hp = Integer.parseInt(tmpNode.attributeValue("hp"));
            	int mp = Integer.parseInt(tmpNode.attributeValue("mp"));
            	int pAttack = 0;
            	int mAttack = 0;
            	String strpa = tmpNode.attributeValue("pa");
            	String strma = tmpNode.attributeValue("ma");
            	if(strpa != null){
            		pAttack = Integer.parseInt(strpa);
            		mAttack = Integer.parseInt(strma);
            	}
            	br.addBoss(monsterID,hp,mp, pAttack, mAttack);
            }
            BossRush.addBossRushList(stage, br);
		}
	}
	
	private byte[] parseByteArray(String str){
		if(str!=null && str.length()>0){
			String[] tmp = str.split(",");
			byte[] result=new byte[tmp.length];
			for(int i=0;i<tmp.length;i++){
				result[i]=Byte.parseByte(tmp[i]);
			}
			return result;
		}else{
			return new byte[0];
		}
	}
}
