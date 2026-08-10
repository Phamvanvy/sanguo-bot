package com.pip.itimes.server.world.love;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class LoveLoader {
	public LoveLoader(File lyricsFile) throws Exception{
        XMLConfiguration config = new XMLConfiguration(lyricsFile);
        loadChats(config);
    }
	
    private void loadChats(XMLConfiguration config){
    	SubnodeConfiguration chatConfig = config.configurationAt("Girl");
        int index = 1;
        try{
        	if(loveConfig.girlChats != null){
        		loveConfig.girlChats.clear();
        	}else{
        		loveConfig.girlChats = new ArrayList<String>();
        	}
	        while(true){
	        	String chat = chatConfig.getString("Chat" + index);
	        	if(chat == null) break;
	        	loveConfig.girlChats.add(chat);
	        	index++;
	        }
        }catch(Exception e){
        }
        chatConfig = config.configurationAt("Boy");
        index = 1;
        try{
        	if(loveConfig.boyChats != null){
        		loveConfig.boyChats.clear();
        	}else{
        		loveConfig.boyChats = new ArrayList<String>();
        	}
	        while(true){
	        	String chat = chatConfig.getString("Chat" + index);
	        	if(chat == null) break;
	        	loveConfig.boyChats.add(chat);
	        	index++;
	        }
        }catch(Exception e){
        }
    }
}
