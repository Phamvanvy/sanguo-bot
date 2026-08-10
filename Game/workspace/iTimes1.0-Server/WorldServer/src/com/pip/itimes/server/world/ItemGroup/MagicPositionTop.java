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

public class MagicPositionTop {
	public static HashMap<Integer,MagicPositionPlayer> WaterMagicLevelRank = new HashMap<Integer,MagicPositionPlayer>(); //水元素排行榜数据
	public static HashMap<Integer,MagicPositionPlayer> SoilMagicLevelRank = new HashMap<Integer,MagicPositionPlayer>(); //土元素排行榜数据
	public static HashMap<Integer,MagicPositionPlayer> FireMagicLevelRank = new HashMap<Integer,MagicPositionPlayer>(); //火元素排行榜数据
	public static HashMap<Integer,MagicPositionPlayer> WindMagicLevelRank = new HashMap<Integer,MagicPositionPlayer>(); //风元素排行榜数据
	public static HashMap<Integer,MagicPositionPlayer> MindMagicLevelRank = new HashMap<Integer,MagicPositionPlayer>(); //精神元素排行榜数据
	
	//各法阵等级排行list
	private static List<MagicPositionPlayer> SortWaterMagicData;
	private static List<MagicPositionPlayer> SortSoilMagicData;
	private static List<MagicPositionPlayer> SortFireMagicData;
	private static List<MagicPositionPlayer> SortWindMagicData;
	private static List<MagicPositionPlayer> SortMindMagicData;
	
	
	private static final Logger log = Logger.getLogger(TrainLevelTop.class);
	
	//各法阵等级排行文件
	public static final String MAGIC_POSITION_WATER = "MagicPosWaterRankData";
	public static final String MAGIC_POSITION_SOIL = "MagicPosSoilRankData";
	public static final String MAGIC_POSITION_FIRE = "MagicPosFireRankData";
	public static final String MAGIC_POSITION_WIND = "MagicPosWindRankData";
	public static final String MAGIC_POSITION_MIND = "MagicPosMindRankData";
	
	
	public static final int MAX_TOP = 20;
	
	private static final int watermagic = 0;
	private static final int soilmagic = 1;
	private static final int firemagic = 2;
	private static final int windmagic = 3;
	private static final int mindmagic = 4;
	
	public static void addPlayerMagicLevelInfo(int magictype,MagicPositionPlayer mpp){
		if(magictype == watermagic){
			addMagicTypeInfo(WaterMagicLevelRank,mpp);
			SortWaterMagicData = sortplayer(WaterMagicLevelRank);
			saveMagicPosWaterData();
		}else if(magictype == soilmagic){
			addMagicTypeInfo(SoilMagicLevelRank,mpp);
			SortSoilMagicData = sortplayer(SoilMagicLevelRank);
			saveMagicPosSoilData();
		}else if(magictype == firemagic){
			addMagicTypeInfo(FireMagicLevelRank,mpp);
			SortFireMagicData = sortplayer(FireMagicLevelRank);
			saveMagicPosFireData();
		}else if(magictype == windmagic){
			addMagicTypeInfo(WindMagicLevelRank,mpp);
			SortWindMagicData = sortplayer(WindMagicLevelRank);
			saveMagicPosWindData();
		}else if(magictype == mindmagic){
			addMagicTypeInfo(MindMagicLevelRank,mpp);
			SortMindMagicData = sortplayer(MindMagicLevelRank);
			saveMagicPosMindData();
		}
	}
	
	public static void addMagicTypeInfo(HashMap<Integer, MagicPositionPlayer> magictypeHashMap,MagicPositionPlayer mpp){
		synchronized (magictypeHashMap) {
			if(magictypeHashMap.containsKey(mpp.getPlayerID())){//表中已有刷新
				MagicPositionPlayer m = magictypeHashMap.get(mpp.getPlayerID());
				if(m.getPlayerMagicExp() > mpp.getPlayerMagicExp()){
					return;
				}
				m.setPlayerID(mpp.getPlayerID());
				m.setPlayername(mpp.getPlayername());
				m.setPlayerCamp(mpp.getPlayerCamp());
				m.setPlayerMagicLevel(mpp.getPlayerMagicLevel());
				m.setPlayerMagicFloor(mpp.getPlayerMagicFloor());
				m.setPlayerMagicExp(mpp.getPlayerMagicExp());
			}else{
				if(magictypeHashMap.size() >= MAX_TOP){
					MagicPositionPlayer minlevelplayer = getMagicPosMinPlayer(magictypeHashMap);
					if(minlevelplayer != null){
						if(mpp.getPlayerMagicExp() < minlevelplayer.getPlayerMagicExp()){
							return;
						}
						magictypeHashMap.remove(minlevelplayer.getPlayerID());
					}
				}
				magictypeHashMap.put(mpp.getPlayerID(), mpp);
			}
		}
	}
	
	public static List<MagicPositionPlayer> getplayerMagicdata(int magictype){
		List<MagicPositionPlayer> currentList = null;
		if(magictype == watermagic){
			currentList = SortWaterMagicData;
		}else if(magictype == soilmagic){
			currentList = SortSoilMagicData;
		}else if(magictype == firemagic){
			currentList = SortFireMagicData;
		}else if(magictype == windmagic){
			currentList = SortWindMagicData;
		}else if(magictype == mindmagic){
			currentList = SortMindMagicData;
		}
		return currentList;
	}  
	
	//将表中数据排序
	public static List<MagicPositionPlayer> sortplayer(HashMap<Integer, MagicPositionPlayer> mpp){
		Iterator<MagicPositionPlayer> iter = mpp.values().iterator();
		while(iter.hasNext()){
			MagicPositionPlayer playervalue = iter.next();
			if(playervalue != null){
				List<MagicPositionPlayer> sortTopData = new ArrayList<MagicPositionPlayer>();
				sortTopData.add(new MagicPositionPlayer(playervalue.getPlayerID(),playervalue.getPlayername(),playervalue.getPlayerCamp(),playervalue.getPlayerMagicLevel(),playervalue.getPlayerMagicFloor(),playervalue.getPlayerMagicExp()));
				while(iter.hasNext()){
					MagicPositionPlayer playerData = iter.next();
					if(playerData != null){
						int size = sortTopData.size();
						boolean insert = false;
						for(int i = 0;i<size;i++){
							MagicPositionPlayer temp = sortTopData.get(i);
							if(playerData.getPlayerMagicExp() > temp.getPlayerMagicExp()){
								sortTopData.add(i, new MagicPositionPlayer(playerData.getPlayerID(),playerData.getPlayername(),playerData.getPlayerCamp(),playerData.getPlayerMagicLevel(),playerData.getPlayerMagicFloor(),playerData.getPlayerMagicExp()));
								insert = true;
								break;
							}
						}
						if(!insert){
							sortTopData.add(new MagicPositionPlayer(playerData.getPlayerID(),playerData.getPlayername(),playerData.getPlayerCamp(),playerData.getPlayerMagicLevel(),playerData.getPlayerMagicFloor(),playerData.getPlayerMagicExp()));
						}
					}
				}
				return sortTopData;
			}
		}
		return null;
	}
		
	//获得排行榜中经验最少玩家
	public static MagicPositionPlayer getMagicPosMinPlayer(HashMap<Integer, MagicPositionPlayer> magictypeHashMap){
		Iterator<MagicPositionPlayer> iter = magictypeHashMap.values().iterator();
		MagicPositionPlayer minplayer = null;
		while(iter.hasNext()){
			MagicPositionPlayer currentplayer = iter.next();
			if(minplayer == null){
				minplayer = currentplayer;
			}else{
				if(currentplayer.getPlayerMagicExp() < minplayer.getPlayerMagicExp()){
					minplayer = currentplayer;
				}
			}
		}
		return minplayer;
	}
	
	
	//保存水阵眼信息
	public static void saveMagicPosWaterData(){
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("MagicPositionPlayer");
			for(MagicPositionPlayer mpp : WaterMagicLevelRank.values()){
				Element elItemTopData = root.addElement("Data");
				elItemTopData.addAttribute("playerid" , "" + mpp.getPlayerID());
				elItemTopData.addAttribute("playername", "" + mpp.getPlayername());
				elItemTopData.addAttribute("playercamp", "" + mpp.getPlayerCamp());
				elItemTopData.addAttribute("playermagiclevel", "" + mpp.getPlayerMagicLevel());
				elItemTopData.addAttribute("playermagicfloor", "" + mpp.getPlayerMagicFloor());
				elItemTopData.addAttribute("playermagicexp", "" + mpp.getPlayerMagicExp());
			};
			try {
	        	String path = System.getProperty("user.dir") + "/" + MAGIC_POSITION_WATER;
	        	File dir = new File(path);
	        	if(!dir.exists()){
	        		dir.mkdir();
	        	}
	        	File file = new File(path + "/" + MAGIC_POSITION_WATER + ".xml");
	        	file.createNewFile();
				saveDocument(doc, new FileWriter(file));
				log.info("Save MagicPositionWaterInfo ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	//保存土阵眼信息
	public static void saveMagicPosSoilData(){
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("MagicPositionPlayer");
			for(MagicPositionPlayer mpp : SoilMagicLevelRank.values()){
				Element elItemTopData = root.addElement("Data");
				elItemTopData.addAttribute("playerid" , "" + mpp.getPlayerID());
				elItemTopData.addAttribute("playername", "" + mpp.getPlayername());
				elItemTopData.addAttribute("playercamp", "" + mpp.getPlayerCamp());
				elItemTopData.addAttribute("playermagiclevel", "" + mpp.getPlayerMagicLevel());
				elItemTopData.addAttribute("playermagicfloor", "" + mpp.getPlayerMagicFloor());
				elItemTopData.addAttribute("playermagicexp", "" + mpp.getPlayerMagicExp());
			};
			try {
	        	String path = System.getProperty("user.dir") + "/" + MAGIC_POSITION_SOIL;
	        	File dir = new File(path);
	        	if(!dir.exists()){
	        		dir.mkdir();
	        	}
	        	File file = new File(path + "/" + MAGIC_POSITION_SOIL + ".xml");
	        	file.createNewFile();
				saveDocument(doc, new FileWriter(file));
				log.info("Save MagicPositionSoilInfo ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	//保存火阵眼信息
	public static void saveMagicPosFireData(){
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("MagicPositionPlayer");
			for(MagicPositionPlayer mpp : FireMagicLevelRank.values()){
				Element elItemTopData = root.addElement("Data");
				elItemTopData.addAttribute("playerid" , "" + mpp.getPlayerID());
				elItemTopData.addAttribute("playername", "" + mpp.getPlayername());
				elItemTopData.addAttribute("playercamp", "" + mpp.getPlayerCamp());
				elItemTopData.addAttribute("playermagiclevel", "" + mpp.getPlayerMagicLevel());
				elItemTopData.addAttribute("playermagicfloor", "" + mpp.getPlayerMagicFloor());
				elItemTopData.addAttribute("playermagicexp", "" + mpp.getPlayerMagicExp());
			};
			try {
	        	String path = System.getProperty("user.dir") + "/" + MAGIC_POSITION_FIRE;
	        	File dir = new File(path);
	        	if(!dir.exists()){
	        		dir.mkdir();
	        	}
	        	File file = new File(path + "/" + MAGIC_POSITION_FIRE + ".xml");
	        	file.createNewFile();
				saveDocument(doc, new FileWriter(file));
				log.info("Save MagicPositionFireInfo ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	//保存风阵眼信息
	public static void saveMagicPosWindData(){
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("MagicPositionPlayer");
			for(MagicPositionPlayer mpp : WindMagicLevelRank.values()){
				Element elItemTopData = root.addElement("Data");
				elItemTopData.addAttribute("playerid" , "" + mpp.getPlayerID());
				elItemTopData.addAttribute("playername", "" + mpp.getPlayername());
				elItemTopData.addAttribute("playercamp", "" + mpp.getPlayerCamp());
				elItemTopData.addAttribute("playermagiclevel", "" + mpp.getPlayerMagicLevel());
				elItemTopData.addAttribute("playermagicfloor", "" + mpp.getPlayerMagicFloor());
				elItemTopData.addAttribute("playermagicexp", "" + mpp.getPlayerMagicExp());
			};
			try {
	        	String path = System.getProperty("user.dir") + "/" + MAGIC_POSITION_WIND;
	        	File dir = new File(path);
	        	if(!dir.exists()){
	        		dir.mkdir();
	        	}
	        	File file = new File(path + "/" + MAGIC_POSITION_WIND + ".xml");
	        	file.createNewFile();
				saveDocument(doc, new FileWriter(file));
				log.info("Save MagicPositionWindInfo ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	//保存精神阵眼信息
	public static void saveMagicPosMindData(){
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("MagicPositionPlayer");
			for(MagicPositionPlayer mpp : MindMagicLevelRank.values()){
				Element elItemTopData = root.addElement("Data");
				elItemTopData.addAttribute("playerid" , "" + mpp.getPlayerID());
				elItemTopData.addAttribute("playername", "" + mpp.getPlayername());
				elItemTopData.addAttribute("playercamp", "" + mpp.getPlayerCamp());
				elItemTopData.addAttribute("playermagiclevel", "" + mpp.getPlayerMagicLevel());
				elItemTopData.addAttribute("playermagicfloor", "" + mpp.getPlayerMagicFloor());
				elItemTopData.addAttribute("playermagicexp", "" + mpp.getPlayerMagicExp());
			};
			try {
	        	String path = System.getProperty("user.dir") + "/" + MAGIC_POSITION_MIND;
	        	File dir = new File(path);
	        	if(!dir.exists()){
	        		dir.mkdir();
	        	}
	        	File file = new File(path + "/" + MAGIC_POSITION_MIND + ".xml");
	        	file.createNewFile();
				saveDocument(doc, new FileWriter(file));
				log.info("Save MagicPositionMindInfo ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	
	//读取水元素
	public static void loadWaterInfo(){
		synchronized (WaterMagicLevelRank) {
			WaterMagicLevelRank.clear();
			File file = new File(System.getProperty("user.dir") + "/" + MAGIC_POSITION_WATER + "/" + MAGIC_POSITION_WATER + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
	    			for(Iterator data = root.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("playerid"));
						String playername = elData.attributeValue("playername");
						int camp = Integer.parseInt(elData.attributeValue("playercamp"));
						int level = Integer.parseInt(elData.attributeValue("playermagiclevel"));
						int floor = Integer.parseInt(elData.attributeValue("playermagicfloor"));
						int exp = Integer.parseInt(elData.attributeValue("playermagicexp"));
						WaterMagicLevelRank.put(playerid, new MagicPositionPlayer(playerid, playername,camp,level,floor,exp));
					}
	    			if(WaterMagicLevelRank.size() > MAX_TOP){
						int removeCount = WaterMagicLevelRank.size() - MAX_TOP;
						while(removeCount > 0){
							MagicPositionPlayer mpp = getMagicPosMinPlayer(WaterMagicLevelRank);//删除表中最小等级玩家
							WaterMagicLevelRank.remove(mpp.getPlayerID());
							removeCount --;
						}
					}
	    			SortWaterMagicData = sortplayer(WaterMagicLevelRank);
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
		}
	}

	//读取土元素
	public static void loadSoilInfo(){
		synchronized (SoilMagicLevelRank) {
			SoilMagicLevelRank.clear();
			File file = new File(System.getProperty("user.dir") + "/" + MAGIC_POSITION_SOIL + "/" + MAGIC_POSITION_SOIL + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
	    			for(Iterator data = root.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("playerid"));
						String playername = elData.attributeValue("playername");
						int camp = Integer.parseInt(elData.attributeValue("playercamp"));
						int level = Integer.parseInt(elData.attributeValue("playermagiclevel"));
						int floor = Integer.parseInt(elData.attributeValue("playermagicfloor"));
						int exp = Integer.parseInt(elData.attributeValue("playermagicexp"));
						SoilMagicLevelRank.put(playerid, new MagicPositionPlayer(playerid, playername,camp,level,floor,exp));
					}
	    			if(SoilMagicLevelRank.size() > MAX_TOP){
						int removeCount = SoilMagicLevelRank.size() - MAX_TOP;
						while(removeCount > 0){
							MagicPositionPlayer mpp = getMagicPosMinPlayer(SoilMagicLevelRank);//删除表中最小等级玩家
							SoilMagicLevelRank.remove(mpp.getPlayerID());
							removeCount --;
						}
					}
	    			SortSoilMagicData = sortplayer(SoilMagicLevelRank);
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
		}
	}
	
	//读取火元素
	public static void loadFireInfo(){
		synchronized (FireMagicLevelRank) {
			FireMagicLevelRank.clear();
			File file = new File(System.getProperty("user.dir") + "/" + MAGIC_POSITION_FIRE + "/" + MAGIC_POSITION_FIRE + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
	    			for(Iterator data = root.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("playerid"));
						String playername = elData.attributeValue("playername");
						int camp = Integer.parseInt(elData.attributeValue("playercamp"));
						int level = Integer.parseInt(elData.attributeValue("playermagiclevel"));
						int floor = Integer.parseInt(elData.attributeValue("playermagicfloor"));
						int exp = Integer.parseInt(elData.attributeValue("playermagicexp"));
						FireMagicLevelRank.put(playerid, new MagicPositionPlayer(playerid, playername,camp,level,floor,exp));
					}
	    			if(FireMagicLevelRank.size() > MAX_TOP){
						int removeCount = FireMagicLevelRank.size() - MAX_TOP;
						while(removeCount > 0){
							MagicPositionPlayer mpp = getMagicPosMinPlayer(FireMagicLevelRank);//删除表中最小等级玩家
							FireMagicLevelRank.remove(mpp.getPlayerID());
							removeCount --;
						}
					}
	    			SortFireMagicData = sortplayer(FireMagicLevelRank);
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
		}
	}
	
	//读取风元素
	public static void loadWindInfo(){
		synchronized (WindMagicLevelRank) {
			WindMagicLevelRank.clear();
			File file = new File(System.getProperty("user.dir") + "/" + MAGIC_POSITION_WIND + "/" + MAGIC_POSITION_WIND + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
	    			for(Iterator data = root.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("playerid"));
						String playername = elData.attributeValue("playername");
						int camp = Integer.parseInt(elData.attributeValue("playercamp"));
						int level = Integer.parseInt(elData.attributeValue("playermagiclevel"));
						int floor = Integer.parseInt(elData.attributeValue("playermagicfloor"));
						int exp = Integer.parseInt(elData.attributeValue("playermagicexp"));
						WindMagicLevelRank.put(playerid, new MagicPositionPlayer(playerid, playername,camp,level,floor,exp));
					}
	    			if(WindMagicLevelRank.size() > MAX_TOP){
						int removeCount = WindMagicLevelRank.size() - MAX_TOP;
						while(removeCount > 0){
							MagicPositionPlayer mpp = getMagicPosMinPlayer(WindMagicLevelRank);//删除表中最小等级玩家
							WindMagicLevelRank.remove(mpp.getPlayerID());
							removeCount --;
						}
					}
	    			SortWindMagicData = sortplayer(WindMagicLevelRank);
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
		}
	}
	
	//读取精神元素
	public static void loadMindInfo(){
		synchronized (MindMagicLevelRank) {
			MindMagicLevelRank.clear();
			File file = new File(System.getProperty("user.dir") + "/" + MAGIC_POSITION_MIND + "/" + MAGIC_POSITION_MIND + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
	    			for(Iterator data = root.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("playerid"));
						String playername = elData.attributeValue("playername");
						int camp = Integer.parseInt(elData.attributeValue("playercamp"));
						int level = Integer.parseInt(elData.attributeValue("playermagiclevel"));
						int floor = Integer.parseInt(elData.attributeValue("playermagicfloor"));
						int exp = Integer.parseInt(elData.attributeValue("playermagicexp"));
						MindMagicLevelRank.put(playerid, new MagicPositionPlayer(playerid, playername,camp,level,floor,exp));
					}
	    			if(MindMagicLevelRank.size() > MAX_TOP){
						int removeCount = MindMagicLevelRank.size() - MAX_TOP;
						while(removeCount > 0){
							MagicPositionPlayer mpp = getMagicPosMinPlayer(MindMagicLevelRank);//删除表中最小等级玩家
							MindMagicLevelRank.remove(mpp.getPlayerID());
							removeCount --;
						}
					}
	    			SortMindMagicData = sortplayer(MindMagicLevelRank);
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
	
}
