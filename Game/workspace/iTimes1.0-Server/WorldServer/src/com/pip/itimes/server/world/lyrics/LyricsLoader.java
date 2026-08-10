package com.pip.itimes.server.world.lyrics;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class LyricsLoader {
	public LyricsLoader(File lyricsFile) throws Exception{
        XMLConfiguration config = new XMLConfiguration(lyricsFile);
        loadLyrics(config);
        LyricsConfig.resetTime();
        LyricsConfig.reload();
    }
	
    private void loadLyrics(XMLConfiguration config){
    	SubnodeConfiguration lyricsConfig = config.configurationAt("Start");
    	LyricsConfig.startYear = lyricsConfig.getInt("Year");
    	LyricsConfig.startMonth = lyricsConfig.getInt("Month");
    	LyricsConfig.startDay = lyricsConfig.getInt("Day");
        lyricsConfig = config.configurationAt("End");
        LyricsConfig.endYear = lyricsConfig.getInt("Year");
        LyricsConfig.endMonth = lyricsConfig.getInt("Month");
        LyricsConfig.endDay = lyricsConfig.getInt("Day");
        
        lyricsConfig = config.configurationAt("Day");
        LyricsConfig.startHour = lyricsConfig.getInt("StartHour");
        LyricsConfig.startMinute = lyricsConfig.getInt("StartMinute");
        LyricsConfig.startSecond = lyricsConfig.getInt("StartSecond");
        LyricsConfig.endHour = lyricsConfig.getInt("EndHour");
        LyricsConfig.endMinute = lyricsConfig.getInt("EndMinute");
        LyricsConfig.endSecond = lyricsConfig.getInt("EndSecond");
        
        LyricsConfig.hourTime = lyricsConfig.getInt("HourTime");
        
        long minutemillisecond = 1000 * 60;
        LyricsConfig.TIME = lyricsConfig.getInt("ActionMinute") * minutemillisecond;
        LyricsConfig.HOURTIME = lyricsConfig.getInt("ActionNext") * minutemillisecond;
        LyricsConfig.TIME5 = lyricsConfig.getInt("ActionAd") * minutemillisecond;
        
        
        lyricsConfig = config.configurationAt("AllLyrics");
        LyricsConfig.lyrics = new ArrayList<Lyric>();
        int index = 1;
        try{
	        while(true){
	        	SubnodeConfiguration sc = lyricsConfig.configurationAt("Sing" + index);
	        	if(sc == null) break;
	        	Lyric lyric = new Lyric();
	        	lyric.setSinger(sc.getString("Singer"));
	        	lyric.setName(sc.getString("Name"));
	        	lyric.setSysTip(sc.getString("SysTip"));
	        	String[] othertip = new String[LyricsConfig.hourTime];
	        	for(int i=0; i<othertip.length; i++){
	        		othertip[i] = sc.getString("OtherTip" + (i + 1));
	        	}
	        	lyric.setOtherTip(othertip);
	        	LyricsConfig.lyrics.add(lyric);
	        	index++;
	        }
        }catch(Exception e){
        }
    }
}
