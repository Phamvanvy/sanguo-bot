package com.pip.servermgr.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.pip.util.Utils;

public class Settings {
	private static File getFile() {
		String home = System.getProperty("user.home");
		return new File(home, "servermanagerclient.xml");
	}
	
	private static Document loadOrCreate() {
		try {
			return Utils.loadDOM(getFile());
		} catch (Exception e) {
		}
		Element elem = new Element("properties");
		return new Document(elem);
	}
	
	private static Element get(Document doc, String key) {
		List list = doc.getRootElement().getChildren("entry");
		for (int i = 0; i < list.size(); i++) {
			Element elem = (Element)list.get(i);
			if (key.equals(elem.getAttributeValue("name"))) {
				return elem;
			}
		}
		return null;
	}
	
	public static void set(String key, String value) {
		Document doc = loadOrCreate();
		Element elem = get(doc, key);
		if (elem == null) {
			elem = new Element("entry");
			elem.addAttribute("name", key);
			elem.addAttribute("value", value);
			doc.getRootElement().addContent(elem);
		} else {
			elem.getAttribute("value").setValue(value);
		}
		try {
			saveDOM(doc, getFile(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String get(String key) {
		Document doc = loadOrCreate();
		Element elem = get(doc, key);
		if (elem == null) {
			return null;
		} else {
			return elem.getAttributeValue("value");
		}
	}
	
	public static void saveDOM(Document doc, File file, boolean addSpace) throws Exception{
        FileOutputStream fos = null;
        String encoding = System.getProperty("pip_xml_encoding");
        if (encoding == null) {
        	encoding = "GBK";
        }
        try{
            XMLOutputter out;
            if (!addSpace) {
            	// 删除所有无用的空格
            	ArrayList stack = new ArrayList();
            	stack.add(doc.getRootElement());
            	while (stack.size() > 0) {
            		Object obj = stack.remove(0);
            		if (obj instanceof Element) {
            			Element element = (Element)obj;
            			List list = element.getMixedContent();
            			for (int i = 0; i < list.size(); i++) {
            				Object child = list.get(i);
            				if (child instanceof String && ((String)child).trim().isEmpty()) {
            					list.remove(i);
            					i--;
            				} else if (child instanceof Element) {
            					stack.add(child);
            				}
            			}
            		}
            	}
            }
            out = new XMLOutputter("    ", true, encoding);
            fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            out.output(doc, bos);
            bos.flush();
        }catch(Exception e){
            throw e;
        }finally{
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                }
            }
        }
    }
}
