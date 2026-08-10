package com.pip.itimes.server.stage;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class PetColorLoader {
	public PetColorLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        load(doc);
    }
	
	private void load(Document doc){
		Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("pet");i.hasNext();){
            Element element = (Element)i.next();
            byte type = Byte.parseByte(element.attributeValue("type"));
            String name = element.attributeValue("type_name");
            short[] petSynthetizeColor = parseShortArray(element.attributeValue("petSynthetizeColor")); 
            loadItems(type,element,petSynthetizeColor);
        }
	}
	
	private void loadItems(byte type,Element node,short[] petSynColor){
        for(Iterator i=node.elementIterator("generation");i.hasNext();){
            Element element = (Element)i.next();
            byte bindType = Byte.parseByte(element.attributeValue("bindtype"));
            String[] colors = (element.attributeValue("color")).split(",");
            short[] random = parseShortArray(element.attributeValue("random"));
            short[] fixed = parseShortArray(element.attributeValue("fixed"));
            PetColor petC = new PetColor(type,(byte)(bindType -1),random,fixed,colors);
            if(bindType == 1){
            	petC.setPetSynthetizeColor(petSynColor);
            }
            for(Iterator j =element.elementIterator("Item");j.hasNext();){
            	Element tmpElement = (Element)j.next();
            	int itemID = Integer.parseInt(tmpElement.attributeValue("id"));
            	short colorIndex = Short.parseShort(tmpElement.attributeValue("colorindex"));
            	petC.addItem(itemID, colorIndex);
            }
            PetColor.addPetColor(petC);
        }
    }
	
	private short[] parseShortArray(String str){
		if(str!=null && str.length()>0){
			String[] tmp = str.split(",");
			short[] result=new short[tmp.length];
			for(int i=0;i<tmp.length;i++){
				result[i]=Short.parseShort(tmp[i]);
			}
			return result;
		}else{
			return new short[0];
		}
	}
}
