package com.pip.itimes.server.world.love;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.pip.itimes.server.stage.ChristmasShowInfo;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;

public class loveConfig {
	static private Logger log = Logger.getLogger(loveConfig.class);
    
    public static Random rnd = new Random();
    
    public static ArrayList<String> boyChats = new ArrayList<String>();
    public static ArrayList<String> girlChats = new ArrayList<String>();
    
    public final static byte SEX_BOY = 0;
    public final static byte SEX_GIRL = 1;
    
    public static String getChat(byte sex, String playerName, String targerName){
    	ArrayList<String> chats = sex == SEX_BOY ? boyChats : girlChats;
    	if(chats == null || chats.size() == 0) return "°∞" + playerName + "°±∫Õ°∞" + targerName + "°±œ‡«◊œ‡∞Æ°£";
    	String chat = chats.get(Utils.getRandom(rnd, 0, chats.size() - 1));
    	chat = chat.replace("player", playerName);
    	chat = chat.replace("targer", targerName);
    	return chat;
    }
    
}
