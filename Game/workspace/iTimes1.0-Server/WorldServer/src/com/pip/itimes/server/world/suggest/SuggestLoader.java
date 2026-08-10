package com.pip.itimes.server.world.suggest;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;

import org.apache.commons.io.FilenameUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.world.Server;

public class SuggestLoader {

	public SuggestLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadSuggest(doc);
	}
	
	private void loadSuggest(Document doc) {
		Element root = doc.getRootElement();
		if(root == null)
			return;

		Suggest.suggest.clear();
		
		Iterator it = root.elementIterator("suggest");
		while(it.hasNext()) {
			Element e = (Element)it.next();
			Iterator et = e.elementIterator("map");
			while(et.hasNext()) {
				Element eM = (Element)et.next();
				String mapId = eM.attributeValue("id");
				String[] s =	mapId.split(",");
				int map[] = new int[s.length];
				for(int i = 0 ; i < s.length ; i ++) {
					map[i] = Integer.parseInt(s[i]);
				}
				Iterator el = eM.elementIterator("level");
				Vector level = new Vector();
				int begin;
				int end;
				String text;
				while(el.hasNext()) {
					Element eL = (Element)el.next();
					begin = Integer.parseInt(eL.attributeValue("begin"));
					end = Integer.parseInt(eL.attributeValue("end"));
					text = eL.attributeValue("suggest");
					level.add(new Object[] {begin,end,text});
				}
				Suggest.addSuggest(map,level);
			}
		}
		//适合的游戏地区加载
		Suggest.levelArea.removeAllElements();
		Iterator levelArea = root.elementIterator("suggestarea");
		while(levelArea.hasNext()){
			Element level = (Element)levelArea.next();
			Iterator area = level.elementIterator("areaid");
			while (area.hasNext()) {
				Element areaIds = (Element)area.next();
				int beginLevel = Integer.parseInt(areaIds.attributeValue("beginlevel"));
				int endLevel = Integer.parseInt(areaIds.attributeValue("endlevel"));
				Iterator areaName = areaIds.elementIterator("area");
				Vector areaIdVector = new Vector();
				while(areaName.hasNext()){
					Element areaNameElement = (Element) areaName.next();
					short areaid = Short.parseShort(areaNameElement.attributeValue("areaids"));
					String areaNames = areaNameElement.attributeValue("name");
					areaIdVector.add(new Object[]{areaid, areaNames});
				}
				Suggest.addLevelArea(new Object[]{beginLevel, endLevel, areaIdVector});
			}
			
		}
		
		//游戏公告加载
		Suggest.gameNotice.removeAllElements();
		Iterator gameNotices = root.elementIterator("gamenotice");
		while(gameNotices.hasNext()) {
			Element notics = (Element)gameNotices.next();
			Iterator noteticIterator = notics.elementIterator("notice");
			while (noteticIterator.hasNext()) {
				Element tip = (Element)noteticIterator.next();
				String  notice  = tip.attributeValue("text");
				Suggest.addGmameNotice(notice);
			}
		}
		//游戏小窍门加载
		Suggest.gameTip.removeAllElements();
		Iterator gameTip = root.elementIterator("gametips");
		while(gameTip.hasNext()) {
			Element tips = (Element)gameTip.next();
			Iterator tipiIterator = tips.elementIterator("tips");
			while (tipiIterator.hasNext()) {
				Element tip = (Element)tipiIterator.next();
				String  tipNotice  = tip.attributeValue("text");
				Suggest.addGmametip(tipNotice);
			}
		}
		//游戏可玩性介绍
		Suggest.gamePlay.removeAllElements();
		Suggest.gameContents.removeAllElements();
		Iterator gamePlays = root.elementIterator("gameplayers");
		while(gamePlays.hasNext()) {
			Element gamePlayer = (Element)gamePlays.next();
			Iterator gamePlayeriIterator = gamePlayer.elementIterator("gameplayer");
			while (gamePlayeriIterator.hasNext()) {
				Element gameElement = (Element)gamePlayeriIterator.next();
				String  gamePlayString  = gameElement.attributeValue("text");
				String  gamePlayerContent = gameElement.attributeValue("textcontent");
				Suggest.addGamePlay(gamePlayString);
				Suggest.addgameContents(gamePlayerContent);
			}
		}
		//游戏安全性介绍
		Suggest.gameSafeInfo.removeAllElements();
		Iterator gamesafts = root.elementIterator("gamesafts");
		while(gamesafts.hasNext()) {
			Element gameSaft = (Element)gamesafts.next();
			Iterator gameSaftiIterator = gameSaft.elementIterator("gamesaft");
			while (gameSaftiIterator.hasNext()) {
				Element gameSafeElement = (Element)gameSaftiIterator.next();
				String  gameSafeString  = gameSafeElement.attributeValue("text");
				Suggest.addGameSafeInfo(gameSafeString);
			}
		}
	}
}
