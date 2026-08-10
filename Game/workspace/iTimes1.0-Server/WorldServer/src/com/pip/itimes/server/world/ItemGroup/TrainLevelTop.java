package com.pip.itimes.server.world.ItemGroup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.TrainGiftMessageLoader;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.MailService;

public class TrainLevelTop {
	
	private static HashMap<Integer,TrainPlayer> TrainLevelRank = new HashMap<Integer,TrainPlayer>(); //排行榜数据
	private static List<TrainPlayer> SortTrainPlayerData;//聚灵等级排行list
	private static final Logger log = Logger.getLogger(TrainLevelTop.class);
	public static final String TRAIN_PLAYER = "TrainRankingPlayerData";//聚灵等级排行文件
	public static final int MAX_TOP = 10;
	
	public static final int WEEK_DAY = 7;//一周时间
	public static final String PLAYER_USE_TRAINPOINT = "UseTrainPoint";//每周使用聚灵点排行文件
//	private static Map<Long, Map<Integer, TrainPlayer>> playerWeekUsePointData = new ConcurrentHashMap<Long, Map<Integer,TrainPlayer>>();
	public static ConcurrentHashMap<Integer, TrainPlayer> playerWeekUsePointData = new ConcurrentHashMap<Integer, TrainPlayer>(MAX_TOP);
	private static List<TrainPlayer> sortWeekTopData;	//添加聚灵点更新榜
	
	public static ConcurrentHashMap<Integer, TrainPlayer> playerUpWeekUsePointData = new ConcurrentHashMap<Integer, TrainPlayer>(MAX_TOP);
	private static List<TrainPlayer> sortUpWeekTopData;	//上周数据
	
	private static MailService mailService;
	
	public TrainLevelTop(MailService mailService){
		this.mailService = mailService;
	}
	
	//添加聚灵等级玩家信息
	public static void addPlayerTrainLevelInfo(TrainPlayer tp){
		synchronized (TrainLevelRank) {
			if(TrainLevelRank.containsKey(tp.getPlayerID())){//表中存在玩家
				TrainPlayer p = TrainLevelRank.get(tp.getPlayerID());
				if(p.getPlayerTrainLevel() > tp.getPlayerTrainLevel()){
					return;
				}
				p.setPlayerID(tp.getPlayerID());
				p.setPlayername(tp.getPlayername());
				p.setPlayerCamp(tp.getPlayerCamp());
				p.setPlayerTrainLevel(tp.getPlayerTrainLevel());
				p.setPlayerUseTrainPoint(tp.getPlayerUseTrainPoint());
				SortTrainPlayerData = sortplayer(TrainLevelRank);
			}else{
				if(TrainLevelRank.size() >= MAX_TOP){
					TrainPlayer tempplayer = getPlayerMin();
					if(tempplayer != null){
						if(tp.getPlayerTrainLevel() < tempplayer.getPlayerTrainLevel()){
							return;					
						}
						TrainLevelRank.remove(tempplayer.getPlayerID());	//超过10条记录删除聚灵总等级数最少的
					}
				}
				TrainLevelRank.put(tp.getPlayerID(), tp);
				SortTrainPlayerData = sortplayer(TrainLevelRank);
			}
			saveplayerData();
		}
	}
	
	/**
	 * 添加玩家的使用聚灵点数
	 */
	public static void addDayTrainpoint(TrainPlayer tp){
		synchronized (playerWeekUsePointData) {
			if(playerWeekUsePointData.containsKey(tp.getPlayerID())){
				TrainPlayer playerdata = playerWeekUsePointData.get(tp.getPlayerID());
				if(playerdata.getPlayerUseTrainPoint() == tp.getPlayerUseTrainPoint()){
					return;
				}
				playerdata.setPlayerUseTrainPoint(tp.getPlayerUseTrainPoint());
				sortWeekTopData = sortWeekPlayerTrainPointData(playerWeekUsePointData);
			}else{
				if(playerWeekUsePointData.size() >= MAX_TOP){
					TrainPlayer tempplayer = getPlayer2Min();
					if(tempplayer != null){
						if(tp.getPlayerUseTrainPoint() <= tempplayer.getPlayerUseTrainPoint()){
							return;					
						}
						playerWeekUsePointData.remove(tempplayer.getPlayerID());	//超过10条记录删除聚灵总等级数最少的
					}
				}
				playerWeekUsePointData.put(tp.getPlayerID(), tp);
				sortWeekTopData = sortWeekPlayerTrainPointData(playerWeekUsePointData);
			}
			save2File(false);
		}
	}
	
	//验证周一零点
	public static long getWeekTimer(){
		Calendar cal = Calendar.getInstance();
		int week = cal.get(Calendar.DAY_OF_WEEK);
		if(week == Calendar.MONDAY){
		}else{
			if(week < Calendar.MONDAY){
				cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 6);
			}else{
				cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - (week - Calendar.MONDAY));
			}
		}
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	/**
	 * 将使用聚灵点排序放进sortWeekTopData中
	 * @param TrainPointMap
	 */
	private static List<TrainPlayer> sortWeekPlayerTrainPointData(Map<Integer, TrainPlayer> TrainPointMap){
		Iterator<TrainPlayer> iter = TrainPointMap.values().iterator();
		while(iter.hasNext()){
			TrainPlayer minValue = iter.next();
			if(minValue != null){
				List<TrainPlayer> sortTopData = new ArrayList<TrainPlayer>();
				sortTopData.add(new TrainPlayer(minValue.getPlayerID(),
						minValue.getPlayername(),minValue.getPlayerCamp(),
						minValue.getPlayerTrainLevel(),minValue.getPlayerUseTrainPoint()));
				while(iter.hasNext()){
					TrainPlayer playerdata = iter.next();
					if(playerdata != null){
						int size = sortTopData.size();
						boolean insert = false;
						for(int i=0; i<size; i++){
							TrainPlayer tempdata = sortTopData.get(i);
							if(playerdata.getPlayerUseTrainPoint() > tempdata.getPlayerUseTrainPoint()){
								sortTopData.add(i, new TrainPlayer(playerdata.getPlayerID(), 
										playerdata.getPlayername(),playerdata.getPlayerCamp(),
										playerdata.getPlayerTrainLevel(),playerdata.getPlayerUseTrainPoint()));
								insert = true;
								break;
							}
						}
						if(!insert){
							sortTopData.add(new TrainPlayer(playerdata.getPlayerID(), 
									playerdata.getPlayername(),playerdata.getPlayerCamp(),
									playerdata.getPlayerTrainLevel(),playerdata.getPlayerUseTrainPoint()));
						}
						if(sortTopData.size() > MAX_TOP){
							sortTopData.remove(sortTopData.size() - 1);
						}
					}
				}
				return sortTopData;
			}
		}
		return null;
	}
	
	private static final int UseTrainPointHigher = 0;//使用聚灵点最多的player
	
	/**
	 * 保存数据
	 */
	public static void save2File(boolean zeroup){
		long nowTimer = Utils.getTodayStart();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(nowTimer);
		if(zeroup && cal.getTimeInMillis() == getWeekTimer()){//是否是每周一
			//send award or clean weekdata
			log.info("PlayerUseTrainPoint Send Award or clean WeekData try");
			synchronized (playerWeekUsePointData) {
				List<TrainPlayer> playerTrainPointData = sortWeekPlayerTrainPointData(playerWeekUsePointData);
				if(playerTrainPointData != null && playerTrainPointData.size() > 0){
					TrainPlayer player = playerTrainPointData.get(UseTrainPointHigher); 
					if(player != null){
						IItemTemplate tmpitem = Items.getTemplate(TrainGiftMessageLoader.trainPointAward);
						byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
						mailService.sendMail(player.getPlayerID(), player.getPlayername(), -1, "系统", "聚灵奖励", "恭喜您在上一周的努力中聚灵等级增长达到第一名，获得特级聚灵碎片1个！", att, 0, true);
						log.info("player ID[" + player.getPlayerID() + "]giveitemid[" + TrainGiftMessageLoader.trainPointAward +"]");
					}
				}
				playerUpWeekUsePointData.clear();
				for(TrainPlayer bbp : playerWeekUsePointData.values()){
					playerUpWeekUsePointData.put(bbp.getPlayerID(), bbp);
				}
				sortUpWeekTopData = sortWeekPlayerTrainPointData(playerUpWeekUsePointData);
				playerWeekUsePointData.clear();
				sortWeekTopData = sortWeekPlayerTrainPointData(playerWeekUsePointData);
			}
		}
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("playerWeekUsePointData");
		synchronized (playerUpWeekUsePointData) {
			for(TrainPlayer bbp : playerUpWeekUsePointData.values()){
				Element elItemTopData = root.addElement("upWeekData");
				elItemTopData.addAttribute("playerid" , "" + bbp.getPlayerID());
				elItemTopData.addAttribute("playername", "" + bbp.getPlayername());
				elItemTopData.addAttribute("playercamp", "" + bbp.getPlayerCamp());
				elItemTopData.addAttribute("playertrainlevel", "" + bbp.getPlayerTrainLevel());
				elItemTopData.addAttribute("playerusetrainpoint", "" + bbp.getPlayerUseTrainPoint());
			};
		}
		synchronized (playerWeekUsePointData) {
			for(TrainPlayer bbp : playerWeekUsePointData.values()){
				Element elItemTopData = root.addElement("Data");
				elItemTopData.addAttribute("playerid" , "" + bbp.getPlayerID());
				elItemTopData.addAttribute("playername", "" + bbp.getPlayername());
				elItemTopData.addAttribute("playercamp", "" + bbp.getPlayerCamp());
				elItemTopData.addAttribute("playertrainlevel", "" + bbp.getPlayerTrainLevel());
				elItemTopData.addAttribute("playerusetrainpoint", "" + bbp.getPlayerUseTrainPoint());
			};
		}
		try {
			String path = System.getProperty("user.dir") + "/" + PLAYER_USE_TRAINPOINT;
			File dir = new File(path);
			if(!dir.exists()){
				dir.mkdir();
			}
			File file = new File(path + "/" + PLAYER_USE_TRAINPOINT + ".xml");
			file.createNewFile();
			saveDocument(doc, new FileWriter(file));
			log.info("Save TrainplayerUsePoint ok");
		} catch (IOException e) {
			log.error(e, e);
		}
    }
	
	public static void load2File(){
		 synchronized (playerWeekUsePointData) {
			File file = new File(System.getProperty("user.dir") + "/" + PLAYER_USE_TRAINPOINT + "/" + PLAYER_USE_TRAINPOINT + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
		    		for(Iterator data = root.elementIterator("upWeekData"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("playerid"));
						String playername = elData.attributeValue("playername");
						int camp = Integer.parseInt(elData.attributeValue("playercamp"));
						int level = Integer.parseInt(elData.attributeValue("playertrainlevel"));
						int usepoint = Integer.parseInt(elData.attributeValue("playerusetrainpoint"));
						playerUpWeekUsePointData.put(playerid, new TrainPlayer(playerid, playername,camp,level,usepoint));
					};
		    		for(Iterator data = root.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("playerid"));
						String playername = elData.attributeValue("playername");
						int camp = Integer.parseInt(elData.attributeValue("playercamp"));
						int level = Integer.parseInt(elData.attributeValue("playertrainlevel"));
						int usepoint = Integer.parseInt(elData.attributeValue("playerusetrainpoint"));
						playerWeekUsePointData.put(playerid, new TrainPlayer(playerid, playername,camp,level,usepoint));
					};
	    			if(playerWeekUsePointData.size() > MAX_TOP){
						int removeCount = playerWeekUsePointData.size() - MAX_TOP;
						while(removeCount > 0){
							TrainPlayer tp = getPlayer2Min();
							playerWeekUsePointData.remove(tp.getPlayerID());
							removeCount --;
						}
					}
	    			sortWeekTopData = sortWeekPlayerTrainPointData(playerWeekUsePointData);
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
		 }
	}
	
	
	//获得排行榜中最小等级玩家
	public static TrainPlayer getPlayerMin(){
		Iterator<TrainPlayer> iter = TrainLevelRank.values().iterator();
		TrainPlayer tempplayer = null;
		while(iter.hasNext()){	
			TrainPlayer currentplayer = iter.next();
			if(tempplayer == null){
				tempplayer = currentplayer;
			}else{	//删除等级最少的玩家
				if(currentplayer.getPlayerTrainLevel() < tempplayer.getPlayerTrainLevel()){
					tempplayer = currentplayer;
				}
			}
		}
		return tempplayer;
	}
	
	//获取排行榜中最小使用点数玩家
	public static TrainPlayer getPlayer2Min(){
		Iterator<TrainPlayer> iter = playerWeekUsePointData.values().iterator();
		TrainPlayer tempplayer = null;
		while(iter.hasNext()){	
			TrainPlayer currentplayer = iter.next();
			if(tempplayer == null){
				tempplayer = currentplayer;
			}else{	//删除等级最少的玩家
				if(currentplayer.getPlayerUseTrainPoint() < tempplayer.getPlayerUseTrainPoint()){
					tempplayer = currentplayer;
				}
			}
		}
		return tempplayer;
	}
	
	
	public static List<TrainPlayer> sortplayer(HashMap<Integer, TrainPlayer> tp){
		Iterator<TrainPlayer> iter = tp.values().iterator();
		while(iter.hasNext()){
			TrainPlayer playervalue = iter.next();
			if(playervalue != null){
				List<TrainPlayer> sortTopData = new ArrayList<TrainPlayer>();
				sortTopData.add(new TrainPlayer(playervalue.getPlayerID(),playervalue.getPlayername(),playervalue.getPlayerCamp(),playervalue.getPlayerTrainLevel(),playervalue.getPlayerUseTrainPoint()));
				while(iter.hasNext()){
					TrainPlayer playerData = iter.next();
					if(playerData != null){
						int size = sortTopData.size();
						boolean insert = false;
						for(int i = 0;i<size;i++){
							TrainPlayer temp = sortTopData.get(i);
							if(playerData.getPlayerTrainLevel() > temp.getPlayerTrainLevel()){
								sortTopData.add(i, new TrainPlayer(playerData.getPlayerID(),playerData.getPlayername(),playerData.getPlayerCamp(),playerData.getPlayerTrainLevel(),playervalue.getPlayerUseTrainPoint()));
								insert = true;
								break;
							}
						}
						if(!insert){
							sortTopData.add(new TrainPlayer(playerData.getPlayerID(),playerData.getPlayername(),playerData.getPlayerCamp(),playerData.getPlayerTrainLevel(),playervalue.getPlayerUseTrainPoint()));
						}
					}
				}
				return sortTopData;
			}
		}
		return null;
	}
	
	public static List<TrainPlayer> getplayerdata(){
		return SortTrainPlayerData;
	}
	
	public static List<TrainPlayer> getTopWeek(){
		if(sortUpWeekTopData == null){
			sortUpWeekTopData = sortWeekPlayerTrainPointData(playerUpWeekUsePointData);//取上周排行
			if(sortUpWeekTopData == null || sortUpWeekTopData.size() == 0){
				return sortWeekTopData;
			}
		}
		return sortUpWeekTopData;
	}
	
	//保存玩家信息
	public static void saveplayerData(){
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("TrainPlayer");
			for(TrainPlayer bbp : TrainLevelRank.values()){
				Element elItemTopData = root.addElement("Data");
				elItemTopData.addAttribute("playerid" , "" + bbp.getPlayerID());
				elItemTopData.addAttribute("playername", "" + bbp.getPlayername());
				elItemTopData.addAttribute("playercamp", "" + bbp.getPlayerCamp());
				elItemTopData.addAttribute("playertrainlevel", "" + bbp.getPlayerTrainLevel());
				elItemTopData.addAttribute("playerusetrainpoint", "" + bbp.getPlayerUseTrainPoint());
			};
			try {
	        	String path = System.getProperty("user.dir") + "/" + TRAIN_PLAYER;
	        	File dir = new File(path);
	        	if(!dir.exists()){
	        		dir.mkdir();
	        	}
	        	File file = new File(path + "/" + TRAIN_PLAYER + ".xml");
	        	file.createNewFile();
				saveDocument(doc, new FileWriter(file));
				log.info("Save TrainplayerInfo ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
		
	}
	
	//读取文件
	public static void loadfile(){
		synchronized (TrainLevelRank) {
			File file = new File(System.getProperty("user.dir") + "/" + TRAIN_PLAYER + "/" + TRAIN_PLAYER + ".xml");
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
						int level = Integer.parseInt(elData.attributeValue("playertrainlevel"));
						int usepoint = Integer.parseInt(elData.attributeValue("playerusetrainpoint"));
						TrainLevelRank.put(playerid, new TrainPlayer(playerid, playername,camp,level,usepoint));
					}
	    			if(TrainLevelRank.size() > MAX_TOP){
						int removeCount = TrainLevelRank.size() - MAX_TOP;
						while(removeCount > 0){
							TrainPlayer tp = getPlayerMin();
							TrainLevelRank.remove(tp.getPlayerID());
							removeCount --;
						}
					}
	    			SortTrainPlayerData = sortplayer(TrainLevelRank);
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
