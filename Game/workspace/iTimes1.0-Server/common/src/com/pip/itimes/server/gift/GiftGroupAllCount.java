package com.pip.itimes.server.gift;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * @file GiftGroupAllCount.java
 * @author zxyu
 * @version 1.0.0
 * @date 2013-1-15
 **/
public class GiftGroupAllCount {
	private static final Logger log = Logger.getLogger(GiftGroupAllCount.class);
	
	private final static String PATH = "GiftGroupAllCount";
	
	public static ConcurrentHashMap<Integer, GiftGroupData> allCounts = new ConcurrentHashMap<Integer, GiftGroupData>();
	
	static{
		loadfile();
	}
	
	public static boolean hasCount(int groupid, int giftid, int allCount){
		GiftGroupData data = allCounts.get(groupid << 4 | giftid);
		if(data != null){
			if(data.usecount >= allCount){
				return false;
			}
		}
		return true;
	}
	
	public static void addCount(int groupid, int giftid){
		synchronized (allCounts) {
			GiftGroupData data = allCounts.get(groupid << 4 | giftid);
			if(data != null){
				data.usecount ++;
			}else{
				data = new GiftGroupData();
				data.groupid = groupid;
				data.giftid = giftid;
				data.usecount = 1;
				allCounts.put(groupid << 4 | giftid, data);
			}
			saveFile();
		}
	}
	
	//保存排行榜信息
	public static void saveFile(){
		try {
			synchronized (allCounts) {
				Document doc = DocumentHelper.createDocument();
				Element root = doc.addElement(PATH);
				Element attrElement = root.addElement("AllCounts");
				for(GiftGroupData bbp : allCounts.values()){
					Element elItemTopData = attrElement.addElement("Data");
					elItemTopData.addAttribute("groupid", "" + bbp.groupid);
					elItemTopData.addAttribute("giftid", "" + bbp.giftid);
					elItemTopData.addAttribute("usecount", "" + bbp.usecount);
				};
				try {
		        	String path = System.getProperty("user.dir") + "/" + PATH;
		        	File dir = new File(path);
		        	if(!dir.exists()){
		        		dir.mkdir();
		        	}
		        	File file = new File(PATH + "/" + PATH + ".xml");
		        	file.createNewFile();
					saveDocument(doc, new FileWriter(file));
					log.info("Save GiftGroupAllCount ok");
				} catch (IOException e) {
					log.error(e, e);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	public static void saveDocument(Document doc, Writer w){
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");
        XMLWriter writer = new XMLWriter(w, format);
        try {
			writer.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			 try {
				writer.close();
			} catch (IOException e) {
			}
		}
    }
	
	//读取文件
	public static void loadfile(){
		synchronized (allCounts) {
			File file = new File(System.getProperty("user.dir") + "/" + PATH + "/" + PATH + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
		    		allCounts.clear();
					Element attrRoot = root.element("AllCounts");
	    			for(Iterator data = attrRoot.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int groupid = Integer.parseInt(elData.attributeValue("groupid"));
						int giftid = Integer.parseInt(elData.attributeValue("giftid"));
						int usecount = Integer.parseInt(elData.attributeValue("usecount"));
						GiftGroupData giftgroupdata = new GiftGroupData(); 
						giftgroupdata.groupid = groupid;
						giftgroupdata.giftid = giftid;
						giftgroupdata.usecount = usecount;
						allCounts.put(groupid << 4 | giftid, giftgroupdata);
					}
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
			
		}
		
	}
}
