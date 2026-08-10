package com.pip.itimes.server.world.ItemGroup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class PetDevelopTop {
	public static HashMap<String, PetDevelopTopData> attrRank[] = null;
	private static List<PetDevelopTopData> sortAttrData[];
	
	static{
		attrRank = new HashMap[4];
		for(int i=0; i<4; i++){
			attrRank[i] = new HashMap<String, PetDevelopTopData>();
		}
		sortAttrData = new ArrayList[4];
	}

	
	private static final Logger log = Logger.getLogger(PetDevelopTop.class);
	
	//排行榜文件
	private static final String PATH = "PetDevelopTop";
	
	public static final int MAX_TOP = 10;
	
	public static final int STRENGTH = 0;
	public static final int VITALITY = 1;
	public static final int AGILITY = 2;
	public static final int INTELLIGENCE = 3;
	
	public static final String ATTR_NAME[] = {
		"力量","体力","敏捷","智力"
	};
	
	public static void addPetDevelopData(int type, PetDevelopTopData pdd){
		if(type < 0 || type >= attrRank.length){
			return;
		}
		synchronized (attrRank[type]) {
			if(attrRank[type].containsKey("" + pdd.getPlayerID() + pdd.getPetID())){//表中存在玩家
				PetDevelopTopData p = attrRank[type].get("" + pdd.getPlayerID() + pdd.getPetID());
				p.setPlayerID(pdd.getPlayerID());
				p.setPlayername(pdd.getPlayername());
				p.setPlayerCamp(pdd.getPlayerCamp());
				p.setPetName(pdd.getPetName());
				p.setPetID(pdd.getPetID());
				p.setAttrValue(pdd.getAttrValue());
				sortAttrData[type] = sortplayer(attrRank[type]);
			}else{
				if(attrRank[type].size() >= MAX_TOP){
					PetDevelopTopData tempplayer = getPlayerMin(attrRank[type]);
					if(tempplayer != null){
						if(pdd.getAttrValue() < tempplayer.getAttrValue()){
							return;
						}
						attrRank[type].remove("" + tempplayer.getPlayerID() + tempplayer.getPetID());
					}
				}
				attrRank[type].put("" + pdd.getPlayerID() + pdd.getPetID(), pdd);
				sortAttrData[type] = sortplayer(attrRank[type]);
			}
			saveplayerData();
		}
	}
	
	//保存排行榜信息
	public static void saveplayerData(){
		try {
			synchronized (attrRank) {
				Document doc = DocumentHelper.createDocument();
				Element root = doc.addElement("PetDevelopTop");
				for(int i=0; i<attrRank.length; i++){
					String elementName = getTypeName(i);
					Element attrElement = root.addElement(elementName);
					for(PetDevelopTopData bbp : attrRank[i].values()){
						Element elItemTopData = attrElement.addElement("Data");
						elItemTopData.addAttribute("playerid", "" + bbp.getPlayerID());
						elItemTopData.addAttribute("playername", "" + bbp.getPlayername());
						elItemTopData.addAttribute("playercamp", "" + bbp.getPlayerCamp());
						elItemTopData.addAttribute("attrValue", "" + bbp.getAttrValue());
						elItemTopData.addAttribute("petName", "" + bbp.getPetName());
						elItemTopData.addAttribute("petID", "" + bbp.getPetID());
					};
				}
				try {
		        	String path = System.getProperty("user.dir") + "/" + PATH;
		        	File dir = new File(path);
		        	if(!dir.exists()){
		        		dir.mkdir();
		        	}
		        	File file = new File(PATH + "/" + PATH + ".xml");
		        	file.createNewFile();
					saveDocument(doc, new FileWriter(file));
					log.info("Save petDevelopTop ok");
				} catch (IOException e) {
					log.error(e, e);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	//获得排行榜中最小值的玩家
	public static PetDevelopTopData getPlayerMin(HashMap<String, PetDevelopTopData> attrRank){
		Iterator<PetDevelopTopData> iter = attrRank.values().iterator();
		PetDevelopTopData tempplayer = null;
		while(iter.hasNext()){	
			PetDevelopTopData currentplayer = iter.next();
			if(tempplayer == null){
				tempplayer = currentplayer;
			}else{	//删除等级最少的玩家
				if(currentplayer.getAttrValue() < tempplayer.getAttrValue()){
					tempplayer = currentplayer;
				}
			}
		}
		return tempplayer;
	}
	
	public static List<PetDevelopTopData> getTop(int type){
		return sortAttrData[type];
	}  
	
	//将表中数据排序
	public static List<PetDevelopTopData> sortplayer(HashMap<String, PetDevelopTopData> mpp){
		Iterator<PetDevelopTopData> iter = mpp.values().iterator();
		while(iter.hasNext()){
			PetDevelopTopData playervalue = iter.next();
			if(playervalue != null){
				List<PetDevelopTopData> sortTopData = new ArrayList<PetDevelopTopData>();
				sortTopData.add(new PetDevelopTopData(playervalue.getType(), playervalue.getPlayerID(),playervalue.getPlayername(),playervalue.getPlayerCamp(),playervalue.getAttrValue(), playervalue.getPetName(), playervalue.getPetID()));
				while(iter.hasNext()){
					PetDevelopTopData playerData = iter.next();
					if(playerData != null){
						int size = sortTopData.size();
						boolean insert = false;
						for(int i = 0;i<size;i++){
							PetDevelopTopData temp = sortTopData.get(i);
							if(playerData.getAttrValue() > temp.getAttrValue()){
								sortTopData.add(i, new PetDevelopTopData(playerData.getType(), playerData.getPlayerID(),playerData.getPlayername(),playerData.getPlayerCamp(),playerData.getAttrValue(), playerData.getPetName(), playerData.getPetID()));
								insert = true;
								break;
							}
						}
						if(!insert){
							sortTopData.add(new PetDevelopTopData(playerData.getType(), playerData.getPlayerID(),playerData.getPlayername(),playerData.getPlayerCamp(),playerData.getAttrValue(), playerData.getPetName(), playerData.getPetID()));
						}
					}
				}
				return sortTopData;
			}
		}
		return null;
	}
		
	//读取文件
	public static void loadfile(){
		synchronized (attrRank) {
			File file = new File(System.getProperty("user.dir") + "/" + PATH + "/" + PATH + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
		    		for(int i=0; i<4; i++){
		    			attrRank[i].clear();
		    			String elementName = getTypeName(i);
						Element attrRoot = root.element(elementName);
		    			for(Iterator data = attrRoot.elementIterator("Data"); data.hasNext();){
							Element elData = (Element)data.next();
							int playerid = Integer.parseInt(elData.attributeValue("playerid"));
							String playername = elData.attributeValue("playername");
							int camp = Integer.parseInt(elData.attributeValue("playercamp"));
							int attrValue = Integer.parseInt(elData.attributeValue("attrValue"));
							String petName = elData.attributeValue("petName");
							int petID = Integer.parseInt(elData.attributeValue("petID"));
							attrRank[i].put("" + playerid + "" + petID, new PetDevelopTopData(i, playerid, playername, camp, attrValue, petName, petID));
						}
		    			if(attrRank[i].size() > MAX_TOP){
		    				int removeCount = attrRank[i].size() - MAX_TOP;
		    				while(removeCount > 0){
		    					PetDevelopTopData tp = getPlayerMin(attrRank[i]);
		    					attrRank[i].remove(tp.getPlayerID());
		    					removeCount --;
		    				}
		    			}
		    			sortAttrData[i] = sortplayer(attrRank[i]);
		    		}
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
			
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
	
	public static String getTypeName(int type){
		switch(type){
		case VITALITY:
			return "Vitality";
		case AGILITY:
			return "Agility";
		case INTELLIGENCE:
			return "Intelligence";
		default:
			return "Strength";
		}
	}
	
}
