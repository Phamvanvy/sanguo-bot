package com.pip.itimes.server.world.love7;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Love7Loader {
	public Love7Loader(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        loadLove(root);
    }
	
    private void loadLove(Element root){
    	synchronized (Love7Config.chats) {
    		for(int i=0; i<Love7Config.TYPE_MAX; i++){
	    		Element element = null;
	    		switch(i){
	    		case Love7Config.TYPE_GIRL2GIRL:
	    			element = root.element("Girl2Girl");
	    			break;
	    		case Love7Config.TYPE_BOY2BOY:
	    			element = root.element("Boy2Boy");
	    			break;
	    		case Love7Config.TYPE_BOY2GIRL:
	    			element = root.element("Boy2Girl");
	    			break;
	    		case Love7Config.TYPE_GIRL2BOY:
	    			element = root.element("Girl2Boy");
	    			break;
	    		}
	    		if(element != null){
	    			ArrayList<String> chat = new ArrayList<String>();
					for (Iterator<Element> chats = element.elementIterator("Chat"); chats.hasNext();) {
						Element em = chats.next();
						chat.add(em.attributeValue("value"));
					}
					Love7Config.chats[i] = chat;
	    		}else{
	    			Love7Config.chats[i] = null;
	    		}
    		}
    	}
    }
}
