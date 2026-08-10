package com.pip.itimes.server.world.lyrics;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class LoveLyricsLoader {
	public LoveLyricsLoader(File lyricsFile) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(lyricsFile);
        Element root = doc.getRootElement();
        loadLyrics(root);
        LoveLyricsConfig.resetTime();
        LoveLyricsConfig.reload();
    }
	
    private void loadLyrics(Element root){
    	Element start = root.element("Start");
    	LoveLyricsConfig.startYear = Integer.parseInt(start.attributeValue("Year"));
    	LoveLyricsConfig.startMonth = Integer.parseInt(start.attributeValue("Month"));
    	LoveLyricsConfig.startDay = Integer.parseInt(start.attributeValue("Day"));
    	Element end = root.element("End");
    	LoveLyricsConfig.endYear = Integer.parseInt(end.attributeValue("Year"));
    	LoveLyricsConfig.endMonth = Integer.parseInt(end.attributeValue("Month"));
    	LoveLyricsConfig.endDay = Integer.parseInt(end.attributeValue("Day"));
    	
    	Element day = root.element("Day");
    	LoveLyricsConfig.startHour = Integer.parseInt(day.attributeValue("StartHour"));
    	LoveLyricsConfig.startMinute = Integer.parseInt(day.attributeValue("StartMinute"));
    	LoveLyricsConfig.startSecond = Integer.parseInt(day.attributeValue("StartSecond"));
    	LoveLyricsConfig.endHour = Integer.parseInt(day.attributeValue("EndHour"));
    	LoveLyricsConfig.endMinute = Integer.parseInt(day.attributeValue("EndMinute"));
    	LoveLyricsConfig.endSecond = Integer.parseInt(day.attributeValue("EndSecond"));
    	LoveLyricsConfig.hourTime = Integer.parseInt(day.attributeValue("HourTime"));
        
        long minutemillisecond = 1000 * 60;
        LoveLyricsConfig.TIME = Integer.parseInt(day.attributeValue("ActionMinute")) * minutemillisecond;
        LoveLyricsConfig.HOURTIME = Integer.parseInt(day.attributeValue("ActionNext")) * minutemillisecond;
        LoveLyricsConfig.TIME5 = Integer.parseInt(day.attributeValue("ActionAd")) * minutemillisecond;
        
        LoveLyricsConfig.lyrics = new ArrayList<LoveLyric>();
        Element allLyrics = root.element("AllLyrics");
		for (Iterator<Element> sing = allLyrics.elementIterator("Sing"); sing.hasNext();) {
			Element el = (Element)sing.next();
			LoveLyric lyric = new LoveLyric();
			lyric.setSinger(el.attributeValue("Singer"));
			lyric.setName(el.attributeValue("Name"));
			lyric.setSysTip(el.attributeValue("SysTip"));
			ArrayList<String> tips = new ArrayList<String>();
			ArrayList<String> sexs = new ArrayList<String>();
			for (Iterator<Element> otherTip = el.elementIterator("OtherTip"); otherTip.hasNext();) {
				Element ot = (Element)otherTip.next();
				tips.add(ot.attributeValue("lyric"));
				sexs.add(ot.attributeValue("sex"));
			}
			String[] ots = new String[tips.size()];
			byte[] bsexs = new byte[sexs.size()];
			tips.toArray(ots);
			int index = 0;
			for(String sex : sexs){
				if(sex.equals("girl")){
					bsexs[index] = LoveLyric.SEX_GIRL;
				}else if(sex.equals("boygirl")){
					bsexs[index] = LoveLyric.SEX_BOYGIRL;
				}else{
					bsexs[index] = LoveLyric.SEX_BOY;
				}
				index ++;
			}
			lyric.setOtherTip(ots);
			lyric.setSex(bsexs);
			LoveLyricsConfig.lyrics.add(lyric);
		}
    }
}
