package com.pip.itimes.server.world.rabbitRace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.pip.itimes.server.world.ItemGroup.MagicPositionPlayer;

public class RabbitRaceTop {
	private static final Logger log = Logger.getLogger(RabbitRaceTop.class);
	private static final String RABBIT_RACE_WIN_PLAYER_DATA = "RabbitRaceWinPlayerData";
	public static final int MAX_TOP = 10;

	public static HashMap<Integer, WinPlayerTopData> playerHashMap = new HashMap<Integer, WinPlayerTopData>();

	// 排行list
	private static List<WinPlayerTopData> playerTopDataList = new ArrayList<WinPlayerTopData>();;

	// 获得排行榜中金钱最少玩家
	public static WinPlayerTopData getMinWinPlayerTopData(
			HashMap<Integer, WinPlayerTopData> topPlayerHashMap) {
		Iterator<WinPlayerTopData> iter = topPlayerHashMap.values().iterator();
		WinPlayerTopData minplayer = null;
		while (iter.hasNext()) {
			WinPlayerTopData currentplayer = iter.next();
			if (minplayer == null) {
				minplayer = currentplayer;
			} else {
				if (currentplayer.getPlayerMoney() < minplayer.getPlayerMoney()) {
					minplayer = currentplayer;
				}
			}
		}
		return minplayer;
	}

	// 读取
	public static void loadWinPlayerInfo() {
		synchronized (playerHashMap) {
			File file = new File(System.getProperty("user.dir") + "/"
					+ RABBIT_RACE_WIN_PLAYER_DATA + "/"
					+ RABBIT_RACE_WIN_PLAYER_DATA + ".xml");
			if (file.exists()) {
				try {
					SAXReader reader = new SAXReader();
					Document doc = reader.read(file);
					Element root = doc.getRootElement();
					for (Iterator data = root.elementIterator("Data"); data
							.hasNext();) {
						Element elData = (Element) data.next();
						int playerId = Integer.parseInt(elData
								.attributeValue("playerId"));
						String playerName = elData.attributeValue("playerName");
						int playerMoney = Integer.parseInt(elData
								.attributeValue("playerMoney"));

						playerHashMap.put(playerId, new WinPlayerTopData(
								playerId, playerName, playerMoney));
					}
					// if (playerHashMap.size() > MAX_TOP) {
					// int removeCount = playerHashMap.size() - MAX_TOP;
					// while (removeCount > 0) {
					// WinPlayerTopData mpp =
					// getMinWinPlayerTopData(playerHashMap);
					// playerHashMap.remove(mpp.getPlayerId());
					// removeCount--;
					// }
					// }
					playerTopDataList = sortPlayer(playerHashMap);
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		}
		log.info("RabbitRaceConfig:loadWinPlayerInfo()");
	}

	private static List<WinPlayerTopData> sortPlayer(
			HashMap<Integer, WinPlayerTopData> playerHashMap) {

		List<WinPlayerTopData> playerTopDataList = new ArrayList<WinPlayerTopData>();
		Iterator<WinPlayerTopData> iter = playerHashMap.values().iterator();

		while (iter.hasNext()) {
			WinPlayerTopData data = iter.next();

			int money = data.getPlayerMoney();

			if (playerTopDataList.isEmpty()) {
				playerTopDataList.add(data);
			} else {
				int listIndex = playerTopDataList.size();
				while (listIndex > 0
						&& money > playerTopDataList.get(listIndex - 1)
								.getPlayerMoney()) {
					listIndex--;
				}

				playerTopDataList.add(listIndex, data);

				if (playerTopDataList.size() > MAX_TOP) {
					playerTopDataList.remove(playerTopDataList.size() - 1);
				}
			}

		}

		return playerTopDataList;
	}

	// 保存
	public static void saveWinPlayerData() {
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("WinPlayerData");
			Iterator<WinPlayerTopData> iter = playerHashMap.values().iterator();
			while (iter.hasNext()) {
				WinPlayerTopData playerTopData = iter.next();
				Element elItemTopData = root.addElement("Data");
				elItemTopData.addAttribute("playerId",
						"" + playerTopData.getPlayerId());
				elItemTopData.addAttribute("playerName",
						"" + playerTopData.getPlayerName());
				elItemTopData.addAttribute("playerMoney",
						"" + playerTopData.getPlayerMoney());
			}

			try {
				String path = System.getProperty("user.dir") + "/"
						+ RABBIT_RACE_WIN_PLAYER_DATA;
				File dir = new File(path);
				if (!dir.exists()) {
					dir.mkdir();
				}
				File file = new File(path + "/" + RABBIT_RACE_WIN_PLAYER_DATA
						+ ".xml");
				file.createNewFile();
				saveDocument(doc, new FileWriter(file));
				log.info("Save RabbitRaceWinPlayerData ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
		log.info("RabbitRaceConfig:saveWinPlayerData()");
	}

	public static void saveDocument(Document doc, Writer w) {
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

	public static void addWinPlayerInfo(WinPlayerTopData mpp) {
		synchronized (playerHashMap) {
			if (playerHashMap.containsKey(mpp.getPlayerId())) {// 表中已有刷新
				WinPlayerTopData data = playerHashMap.get(mpp.getPlayerId());
				int money = mpp.getPlayerMoney() + data.getPlayerMoney();
				data.setPlayerId(mpp.getPlayerId());
				data.setPlayerName(mpp.getPlayerName());
				data.setPlayerMoney(money);
			} else {
				// if(playerHashMap.size() >= MAX_TOP){
				// WinPlayerTopData minlevelplayer =
				// getMinWinPlayerTopData(playerHashMap);
				// if(minlevelplayer != null){
				// if(mpp.getPlayerMagicExp() <
				// minlevelplayer.getPlayerMagicExp()){
				// return;
				// }
				// magictypeHashMap.remove(minlevelplayer.getPlayerID());
				// }
				// }
				// magictypeHashMap.put(mpp.getPlayerId(), mpp);
				playerHashMap.put(mpp.getPlayerId(), mpp);
			}
		}
	}

	public static List<WinPlayerTopData> getWinPlayerList() {
		return playerTopDataList;
	}

	public static void resetWinPlayerList() {
		playerTopDataList.clear();
		playerHashMap.clear();
		saveWinPlayerData();
		loadWinPlayerInfo();
	}

}
