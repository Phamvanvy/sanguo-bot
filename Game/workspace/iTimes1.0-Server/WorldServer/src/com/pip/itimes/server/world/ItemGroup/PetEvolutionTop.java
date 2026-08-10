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

public class PetEvolutionTop {
	public static HashMap<String, PetEvolutionTopData> evolution = new HashMap<String, PetEvolutionTopData>();
	private static List<PetEvolutionTopData> sortEvolution;

    private static final Logger log = Logger.getLogger(PetEvolutionTop.class);
	
	//排行榜文件
	private static final String PATH = "PetEvolutionTop";
	
	public static final int MAX_TOP = 10;
	
	public static void addPetEvolutionData(PetEvolutionTopData petd){
		
		synchronized (evolution) {
			if(evolution. size()== 0){
				evolution.put("" + petd.getPlayerId() + petd.getPetId(), petd);
				sortEvolution = sortEvolutionPlayer(evolution);
			}
			else if(evolution.containsKey("" + petd.getPlayerId() + petd.getPetId())){//表中存在玩家
				PetEvolutionTopData p = evolution.get("" + petd.getPlayerId() + petd.getPetId());
				p.setPlayerId(petd.getPlayerId());
				p.setPlayerName(petd.getPlayerName());
				p.setCamp(petd.getCamp());
				p.setPetName(petd.getPetName());
				p.setPetId(petd.getPetId());
				p.setEvoluitonName(petd.getEvoluitonName());
				p.setEvolutionLevel(petd.getEvolutionLevel());
				p.setEvolutionCurrentPoint(petd.getEvolutionCurrentPoint());
				p.setEvolutionPoint(petd.getEvolutionPoint());
				sortEvolution = sortEvolutionPlayer(evolution);
			}else{
				if(evolution.size() >= MAX_TOP){
					PetEvolutionTopData tempPlayer = getEvolutionPlayerMin(evolution);
					if(tempPlayer != null){
						if(petd.getEvolutionLevel() < tempPlayer.getEvolutionLevel()){
							return;
						}else if(petd.getEvolutionLevel() == tempPlayer.getEvolutionLevel()){
							if(petd.getEvolutionCurrentPoint() < tempPlayer.getEvolutionCurrentPoint()){
								return;
							}else{
								evolution.remove("" + tempPlayer.getPetId() + tempPlayer.getPetId());
							}
						}
						evolution.remove("" + tempPlayer.getPlayerId() + tempPlayer.getPetId());
					}
				}
				evolution.put("" + petd.getPlayerId() + petd.getPetId(), petd);
				sortEvolution = sortEvolutionPlayer(evolution);
			}
			
		}
		saveEvolutionPlayer();
	}
	//将表中数据排序
	public static List<PetEvolutionTopData> sortEvolutionPlayer(HashMap<String, PetEvolutionTopData> mpp){
		Iterator<PetEvolutionTopData> iter = mpp.values().iterator();
		while(iter.hasNext()){
			PetEvolutionTopData playerValue = iter.next();
			if(playerValue != null){
				List<PetEvolutionTopData> sortTopData = new ArrayList<PetEvolutionTopData>();
				sortTopData.add(playerValue);
				while(iter.hasNext()){
					PetEvolutionTopData playerData = iter.next();
					if(playerData != null){
						int size = sortTopData.size();
						boolean insert = false;
						for(int i = 0;i<size;i++){
							PetEvolutionTopData temp = sortTopData.get(i);
							if(playerData.getEvolutionLevel() > temp.getEvolutionLevel()){
								sortTopData.add(i, playerData);
								insert = true;
								break;
							}else if(playerData.getEvolutionLevel() == temp.getEvolutionLevel()){
								if(playerData.getEvolutionCurrentPoint() > temp.getEvolutionCurrentPoint()){
									sortTopData.add(i,playerData);
									insert = true;
									break;
								}
							}
						}
						if(!insert){
							sortTopData.add(new PetEvolutionTopData(playerData.getPlayerId(),playerData.getPlayerName(),playerData.getCamp(),playerData.getPetName(),
									playerData.getPetId(),playerData.getEvoluitonName(),playerData.getEvolutionLevel(),playerData.getEvolutionCurrentPoint(),playerData.getEvolutionPoint()));
						}
					}
				}
				return sortTopData;
			}
		}
		return null;
	}
	
	public static PetEvolutionTopData getEvolutionPlayerMin(HashMap<String, PetEvolutionTopData> evolution){
		Iterator<PetEvolutionTopData> iter = evolution.values().iterator();
		PetEvolutionTopData tempPlayer = null;
		while(iter.hasNext()){	
			PetEvolutionTopData currentPlayer = iter.next();
			if(tempPlayer == null){
				tempPlayer = currentPlayer;
			}else{	//删除等级最少的玩家
				if(currentPlayer.getEvolutionLevel() < tempPlayer.getEvolutionLevel()){
					tempPlayer = currentPlayer;
				}else if(currentPlayer.getEvolutionLevel() == tempPlayer.getEvolutionLevel()){
					if(currentPlayer.getEvolutionCurrentPoint() < tempPlayer.getEvolutionCurrentPoint()){
						tempPlayer = currentPlayer;
					}
				}
			}
		}
		return tempPlayer;
	}
	public static void saveEvolutionPlayer(){
		try {
			synchronized (evolution) {
				Document doc = DocumentHelper.createDocument();
				Element root = doc.addElement("PetEvolution");
				for(PetEvolutionTopData ep : evolution.values()){
					Element elItemTopData = root.addElement("Data");
					elItemTopData.addAttribute("playerId", "" + ep.getPlayerId());
					elItemTopData.addAttribute("playerName", "" + ep.getPlayerName());
					elItemTopData.addAttribute("camp", "" + ep.getCamp());
					elItemTopData.addAttribute("petName", "" + ep.getPetName());
					elItemTopData.addAttribute("petId", "" + ep.getPetId());
					elItemTopData.addAttribute("evolutionName", "" + ep.getEvoluitonName());
					elItemTopData.addAttribute("evolutionLevel", "" + ep.getEvolutionLevel());
					elItemTopData.addAttribute("evolutionCurrentPoint", "" + ep.getEvolutionCurrentPoint());
					elItemTopData.addAttribute("evolutionPoint", "" + ep.getEvolutionPoint());

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
					log.info("Save petEvolutionTop ok");
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
	public static void loadEvolutionFile(){
		synchronized (evolution) {
			File file = new File(System.getProperty("user.dir") + "/" + PATH + "/"  + PATH + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
	    			for(Iterator data = root.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerId = Integer.parseInt(elData.attributeValue("playerId"));
						String playerName = elData.attributeValue("playerName");
						int camp = Integer.parseInt(elData.attributeValue("camp"));
						String petName = elData.attributeValue("petName");
						int petId = Integer.parseInt(elData.attributeValue("petId"));
						String evolutionName = elData.attributeValue("evolutionName");
						int evolutionLevel = Integer.parseInt(elData.attributeValue("evolutionLevel"));
						int evolutionCurrentPoint = Integer.parseInt(elData.attributeValue("evolutionCurrentPoint"));
						int evolutionPoint = Integer.parseInt(elData.attributeValue("evolutionPoint"));
						evolution.put("" + playerId + "" + petId, new PetEvolutionTopData(playerId, playerName, camp,petName, petId,evolutionName,evolutionLevel,evolutionCurrentPoint,evolutionPoint));
					}
	    			if(evolution.size() > MAX_TOP){
	    				int removeCount = evolution.size() - MAX_TOP;
	    				while(removeCount > 0){
	    					PetEvolutionTopData tp = getEvolutionPlayerMin(evolution);
	    					evolution.remove("" + tp.getPlayerId() + tp.getPetId());
	    					removeCount --;
	    				}
	    			}
	    			sortEvolution = sortEvolutionPlayer(evolution);
	    		
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
			
		}
	}	
	public static List<PetEvolutionTopData> getEvolutionTop(){
		return sortEvolution;
	}
}
