package com.pip.itimes.server.world.lyricsSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class LyricsSystemLoader {
	public LyricsSystemLoader(File lyricsFile) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(lyricsFile);
        Element root = doc.getRootElement();
        loadLyrics(root);
    }
	
	private void loadLyrics(Element root){
		synchronized (LyricsSystemConfig.alllyrics) {
			LyricsSystemConfig.alllyrics.clear();
	        for (Iterator<Element> allLycrics = root.elementIterator("AllLyrics"); allLycrics.hasNext();) {
		        Element lycrics = allLycrics.next();
		        String type = lycrics.attributeValue("type");
		        ArrayList<LyricData> lstLycrics = new ArrayList<LyricData>();
				for (Iterator<Element> sing = lycrics.elementIterator("Sing"); sing.hasNext();) {
					Element el = (Element)sing.next();
					LyricData lyric = new LyricData();
					lyric.setSinger(el.attributeValue("Singer"));
					lyric.setName(el.attributeValue("Name"));
					ArrayList<String> tips = new ArrayList<String>();
					for (Iterator<Element> otherTip = el.elementIterator("OtherTip"); otherTip.hasNext();) {
						Element ot = (Element)otherTip.next();
						tips.add(ot.attributeValue("lyric"));
					}
					String[] ots = new String[tips.size()];
					tips.toArray(ots);
					lyric.setOtherTip(ots);
					lstLycrics.add(lyric);
				}
				LyricsSystemConfig.alllyrics.put(type, lstLycrics);
	        }
		}
    }
}
