package com.pip.itimes.server.stage;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class UIVersioLoader {
	 public UIVersioLoader(File file,int flag) throws Exception{
	       
	       
	        if(flag == 1){
	        	SAXReader reader = new SAXReader();
	        	Document doc = reader.read(file);
	        	loadUIVersion(doc);
	        }else if(flag == 2){
	        	UIVersions.loadTasks(file);
	        }
	    }
	 private void loadUIVersion(Document doc){
		 	UIVersions.removeVersion();
	        Element root = doc.getRootElement();
	        for(Iterator i=root.elementIterator("ui");i.hasNext();){
	            Element node = (Element)i.next();
	            short id = Short.parseShort(node.attributeValue("id"));
	        	String name = node.attributeValue("name");
	        	int type = Integer.parseInt(node.attributeValue("type"));
	        	short version= Short.parseShort(node.attributeValue("version"));
	        	UIVersion uiVersion = new UIVersion(id, name, type, version);
	        	UIVersions.addUIVersion(uiVersion.name, uiVersion);
	        }
	    }

}
