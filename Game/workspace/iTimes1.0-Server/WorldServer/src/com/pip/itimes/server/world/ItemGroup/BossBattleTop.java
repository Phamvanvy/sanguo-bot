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

public class BossBattleTop {
	
	private static final Logger log = Logger.getLogger(BossBattleTop.class);
	public static final String BOSS_BATTLE_PLAYER = "bossRankingPlayerData";		//玩家数据
	public static final int MAX_TOP = 10;
	private static HashMap<Integer,BossBattlePlayer> bossbattleRank = new HashMap<Integer,BossBattlePlayer>(MAX_TOP); //排行榜数据
	private static List<BossBattlePlayer> sortplayerData; 
	
	//增加玩家信息
	public static void addBossBattleInfo(BossBattlePlayer bbp){
		synchronized (bossbattleRank) {
			if(bossbattleRank.containsKey(bbp.getPlayerID())){	//排行榜中存在已有玩家id更新数据
				BossBattlePlayer tmp = bossbattleRank.get(bbp.getPlayerID());
				if(tmp.getPlayerTotalfloor() > bbp.getPlayerTotalfloor()){//已存在层数大于新的层数
					return;
				}else if(tmp.getPlayerTotalfloor() == bbp.getPlayerTotalfloor()){
					if(tmp.getPlayerRound() < bbp.getPlayerRound()){	//已存在的回合数小于新的回合数
						return;
					}
				}
				//刷新自身数据
				tmp.setPlayername(bbp.getPlayername());
				tmp.setPlayerLevel(bbp.getPlayerLevel());
				tmp.setPlayerCamp(bbp.getPlayerCamp());
				tmp.setPlayerTotalfloor(bbp.getPlayerTotalfloor());
				tmp.setPlayerRound(bbp.getPlayerRound());
				sortplayerData = sortplayer(bossbattleRank);
			}else{
				if(bossbattleRank.size() >= MAX_TOP){
					BossBattlePlayer tempplayer = getPlayer4Min();
					if(tempplayer != null){
						if(bbp.getPlayerTotalfloor() < tempplayer.getPlayerTotalfloor()){
							return;					//层数相同判断回合数，新加入玩家的回合数大于排行中最大回合数就不插入
						}else if(bbp.getPlayerTotalfloor() == tempplayer.getPlayerTotalfloor()){
							if(bbp.getPlayerRound() >= tempplayer.getPlayerRound()){
								return;
							}
						}
						bossbattleRank.remove(tempplayer.getPlayerID());	//超过10条记录删除总层数最少的
					}
				}
				bossbattleRank.put(bbp.getPlayerID(), bbp);
				sortplayerData = sortplayer(bossbattleRank);
			}
			saveplayerData();
		}
	}

	//排序
	public static List<BossBattlePlayer> sortplayer(HashMap<Integer, BossBattlePlayer> bbp){
		Iterator<BossBattlePlayer> iter = bbp.values().iterator();
		while(iter.hasNext()){
			BossBattlePlayer minValue = iter.next();
			if(minValue != null){
				List<BossBattlePlayer> sortTopData = new ArrayList<BossBattlePlayer>(MAX_TOP);
				sortTopData.add(new BossBattlePlayer(minValue.getPlayerID(), minValue.getPlayername(),
						minValue.getPlayerLevel(),minValue.getPlayerCamp(),minValue.getPlayerTotalfloor(),minValue.getPlayerRound()));
				while(iter.hasNext()){
					BossBattlePlayer itemTopData = iter.next();
					if(itemTopData != null){
						int size = sortTopData.size();
						boolean insert = false;
						for(int i=0; i<size; i++){
							BossBattlePlayer itemTopDataTemp = sortTopData.get(i);
							if(itemTopData.getPlayerTotalfloor() > itemTopDataTemp.getPlayerTotalfloor()){
								sortTopData.add(i, new BossBattlePlayer(itemTopData.getPlayerID(), itemTopData.getPlayername(),
										itemTopData.getPlayerLevel(),itemTopData.getPlayerCamp(),itemTopData.getPlayerTotalfloor(),itemTopData.getPlayerRound()));
								insert = true;
								break;
							}else if(itemTopData.getPlayerTotalfloor() == itemTopDataTemp.getPlayerTotalfloor()){
								if(itemTopData.getPlayerRound() < itemTopDataTemp.getPlayerRound()){
									sortTopData.add(i, new BossBattlePlayer(itemTopData.getPlayerID(), itemTopData.getPlayername(),
											itemTopData.getPlayerLevel(),itemTopData.getPlayerCamp(),itemTopData.getPlayerTotalfloor(),itemTopData.getPlayerRound()));
									insert = true;
									break;
								}
							}
						}
						if(!insert){
							sortTopData.add(new BossBattlePlayer(itemTopData.getPlayerID(), itemTopData.getPlayername(),
									itemTopData.getPlayerLevel(),itemTopData.getPlayerCamp(),itemTopData.getPlayerTotalfloor(),itemTopData.getPlayerRound()));
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
	
	
	public static List<BossBattlePlayer> getplayerdata(){
//		sortplayerData = sortplayer(bossbattleRank);
		return sortplayerData;
	} 
	
	//保存玩家信息
	public static void saveplayerData(){
		try {
				Document doc = DocumentHelper.createDocument();
				Element root = doc.addElement("bossBattlePlayer");
				for(BossBattlePlayer bbp : bossbattleRank.values()){
					Element elItemTopData = root.addElement("Data");
					elItemTopData.addAttribute("playerid" , "" + bbp.getPlayerID());
					elItemTopData.addAttribute("playername", "" + bbp.getPlayername());
					elItemTopData.addAttribute("playerlevel", "" + bbp.getPlayerLevel());
					elItemTopData.addAttribute("playercamp", "" + bbp.getPlayerCamp());
					elItemTopData.addAttribute("playerTotalfloor", "" + bbp.getPlayerTotalfloor());
					elItemTopData.addAttribute("playerRoundNum", "" + bbp.getPlayerRound());
				};
				try {
		        	String path = System.getProperty("user.dir") + "/" + BOSS_BATTLE_PLAYER;
		        	File dir = new File(path);
		        	if(!dir.exists()){
		        		dir.mkdir();
		        	}
		        	File file = new File(path + "/" + BOSS_BATTLE_PLAYER + ".xml");
		        	file.createNewFile();
					saveDocument(doc, new FileWriter(file));
					log.info("Save bossplayerInfo ok");
				} catch (IOException e) {
					log.error(e, e);
				}
		} catch (Exception e) {
			log.error(e, e);
		}
		
	}
	
	public static BossBattlePlayer getPlayer4Min(){
		Iterator<BossBattlePlayer> iter = bossbattleRank.values().iterator();
		BossBattlePlayer tempplayer = null;
		while(iter.hasNext()){	
			BossBattlePlayer currentplayer = iter.next();
			if(tempplayer == null){
				tempplayer = currentplayer;
			}else{	//删除层数最少的玩家
				if(currentplayer.getPlayerTotalfloor() < tempplayer.getPlayerTotalfloor()){
					tempplayer = currentplayer;
				}else if(currentplayer.getPlayerTotalfloor() == tempplayer.getPlayerTotalfloor()){
					if(currentplayer.getPlayerRound() > tempplayer.getPlayerRound()){  //如果层数相同删回合数最多的
						tempplayer = currentplayer;
					}
				}
			}
		}
		return tempplayer;
	}
	
	//读取文件
	public static void loadfile(){
		synchronized (bossbattleRank) {
			File file = new File(System.getProperty("user.dir") + "/" + BOSS_BATTLE_PLAYER + "/" + BOSS_BATTLE_PLAYER + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
//					for (Iterator iter = root.elementIterator("bossBattlePlayer"); iter.hasNext();) {
//						Element el = (Element)iter.next();
						//HashMap<Integer, BossBattlePlayer> itemTopDataMap = new HashMap<Integer, BossBattlePlayer>(MAX_TOP);
		    			for(Iterator data = root.elementIterator("Data"); data.hasNext();){
							Element elData = (Element)data.next();
							int playerid = Integer.parseInt(elData.attributeValue("playerid"));
							String playername = elData.attributeValue("playername");
							int level = Integer.parseInt(elData.attributeValue("playerlevel"));
							int camp = Integer.parseInt(elData.attributeValue("playercamp"));
							int totalfloor = Integer.parseInt(elData.attributeValue("playerTotalfloor"));
							int roundnum = Integer.parseInt(elData.attributeValue("playerRoundNum"));
							bossbattleRank.put(playerid, new BossBattlePlayer(playerid, playername,level,camp,totalfloor,roundnum));
						}
						if(bossbattleRank.size() > MAX_TOP){
							int removeCount = bossbattleRank.size() - MAX_TOP;
							while(removeCount > 0){
								BossBattlePlayer bbp = getPlayer4Min();
								bossbattleRank.remove(bbp.getPlayerID());
								removeCount --;
							}
						}
						sortplayerData = sortplayer(bossbattleRank);
						
//					}
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
