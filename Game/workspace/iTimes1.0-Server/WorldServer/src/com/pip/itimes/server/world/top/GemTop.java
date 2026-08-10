package com.pip.itimes.server.world.top;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.DiamondMosaic;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * @file GemTop.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-11-2
 **/
public class GemTop {
	private static final Logger log = Logger.getLogger(GemTop.class);
	
	public static final int levelPoint[] = {0, 0, 0, 10, 50, 250, 1250, 6250};
	
	public static boolean open = false;	//是否开启
	
	public static long endTime = 0;
	
	public static final int MAX_TOP = 10;
	
	public static ArrayList<GemData> tops = new ArrayList<GemData>();
	public static ConcurrentHashMap<Integer, GemData> topsData = new ConcurrentHashMap<Integer, GemData>();
	
	public static final String PATH = "Top";
	public static final String FILE = "GemTop";
	
	public static void init(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2022);
		cal.set(Calendar.MONTH, Calendar.NOVEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 13);
		cal.set(Calendar.HOUR_OF_DAY, 7);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		endTime = cal.getTimeInMillis();
		reset();
	}
	
	public static void reset(){
		if(System.currentTimeMillis() < endTime){
			open = true;
		}else{
			open = false;
		}
	}
	
	public static int calcValue(WorldPlayer player){
		int value = 0;
		Grid grids[] = player.getUseEquipments();
		value += getEquGemValue(grids);
		Pet pet = player.getPet();
		if(pet != null){
			grids = pet.getUsedEquipments();
			value += getEquGemValue(grids);
		}
		return value;
	}
	
	public static int getEquGemValue(Grid grids[]){
		int value = 0;
		for(Grid grid : grids){
			if(grid != null && grid.item != null){
				IEquipment equ = (IEquipment) grid.item;
				if(equ.getDataVesion() > 0){ 
        			for (byte d = 0; d < Utils.maxHolesEqu; d ++) {
        				DiamondMosaic diamondMosaic = equ.getDiamondMosaicRole(d);
        				if (diamondMosaic != null && diamondMosaic.getDiamondLevel() > 3) {
        					value += levelPoint[diamondMosaic.getDiamondLevel() - 1] * (equ.getDevelopAddCount()[d] + 1);
        				}
        			}
        		}
			}
		}
		return value;
	}
	
	public static void addPlayer(WorldPlayer player){
		if(!open) return;
		synchronized (topsData) {
			int value = calcValue(player);
			if(value <= 0) return;
			if(topsData.containsKey(player.getId())){//表中存在玩家
				GemData p = topsData.get(player.getId());
				//目前玩家更换之后 小于原先的数值 则不记录
				if(p.value >= value){
					return;
				}
				p.value = value;
				tops = sortplayer(topsData);
			}else{
				if(topsData.size() >= MAX_TOP){
					GemData tempplayer = getPlayerMin();
					if(tempplayer != null){
						if(value < tempplayer.value){
							return;					
						}
						topsData.remove(tempplayer.id);	//超过10条记录删除聚灵总等级数最少的
					}
				}
				GemData gd = new GemData(player.getId(), value, player.getPlayerName());
				topsData.put(gd.id, gd);
				tops = sortplayer(topsData);
			}
			saveplayerData();
		}
	}
	
	public static ArrayList<GemData> sortplayer(ConcurrentHashMap<Integer, GemData> gd){
		Iterator<GemData> iter = gd.values().iterator();
		while(iter.hasNext()){
			GemData playervalue = iter.next();
			if(playervalue != null){
				ArrayList<GemData> sortTopData = new ArrayList<GemData>();
				sortTopData.add(new GemData(playervalue.id, playervalue.value, playervalue.name));
				while(iter.hasNext()){
					GemData playerData = iter.next();
					if(playerData != null){
						int size = sortTopData.size();
						boolean insert = false;
						for(int i = 0;i<size;i++){
							GemData temp = sortTopData.get(i);
							if(playerData.value > temp.value){
								sortTopData.add(i, new GemData(playerData.id, playerData.value, playerData.name));
								insert = true;
								break;
							}
						}
						if(!insert){
							sortTopData.add(new GemData(playerData.id, playerData.value, playerData.name));
						}
					}
				}
				return sortTopData;
			}
		}
		return null;
	}
	
	public static GemData getPlayerMin(){
		Iterator<GemData> iter = topsData.values().iterator();
		GemData tempplayer = null;
		while(iter.hasNext()){	
			GemData currentplayer = iter.next();
			if(tempplayer == null){
				tempplayer = currentplayer;
			}else{	//删除等级最少的玩家
				if(currentplayer.value < tempplayer.value){
					tempplayer = currentplayer;
				}
			}
		}
		return tempplayer;
	}
	
	public static void saveplayerData(){
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("GemTop");
			for(GemData bbp : tops){
				Element elItemTopData = root.addElement("Data");
				elItemTopData.addAttribute("playerid" , "" + bbp.id);
				elItemTopData.addAttribute("playername", "" + bbp.name);
				elItemTopData.addAttribute("value", "" + bbp.value);
			};
			try {
	        	String path = System.getProperty("user.dir") + "/" + PATH;
	        	File dir = new File(path);
	        	if(!dir.exists()){
	        		dir.mkdir();
	        	}
	        	File file = new File(path + "/" + FILE + ".xml");
	        	file.createNewFile();
				saveDocument(doc, new FileWriter(file));
				log.info("Save GemTop ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
		
	}
	
	public static void loadfile(){
		synchronized (topsData) {
			File file = new File(System.getProperty("user.dir") + "/" + PATH + "/" + FILE + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
	    			for(Iterator data = root.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("playerid"));
						String playername = elData.attributeValue("playername");
						int value = Integer.parseInt(elData.attributeValue("value"));
						GemData gd = new GemData(playerid, value, playername);
						topsData.put(playerid, new GemData(playerid, value, playername));
					}
	    			if(topsData.size() > MAX_TOP){
						int removeCount = topsData.size() - MAX_TOP;
						while(removeCount > 0){
							GemData tp = getPlayerMin();
							topsData.remove(tp.id);
							removeCount --;
						}
					}
	    			tops = sortplayer(topsData);
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
	
	public static void writeTop(UWAPSegment seg){
		seg.writeShort((short) tops.size());
        for (int i = 0; i < tops.size(); i++) {
            seg.writeInt(i);
            GemData gd = tops.get(i);
            seg.writeString((i + 1) + ". " + gd.name + "的值:" + gd.value);
            seg.writeInt(Utils.CLR_WHITE);
        }
	}
}



