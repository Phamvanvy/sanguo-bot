package com.pip.itimes.server.world.love7;

import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

import com.pip.itimes.server.util.Utils;

public class Love7Config {
	static private Logger log = Logger.getLogger(Love7Config.class);
    
    public static Random random = new Random();
    
    public static final byte TYPE_MAX = 4;
    public static final byte TYPE_GIRL2GIRL = 0;
    public static final byte TYPE_GIRL2BOY = 1;
    public static final byte TYPE_BOY2BOY = 2;
    public static final byte TYPE_BOY2GIRL = 3;
    
	public static ArrayList<String> chats[] = new ArrayList[TYPE_MAX];
    
    public static String getChat(byte type, String playerName, String targerName){
    	if(type < 0 || type >= TYPE_MAX) return null;
    	ArrayList<String> chat = chats[type];
    	if(chat == null || chat.size() == 0) return "°∞" + playerName + "°±∫Õ°∞" + targerName + "°±œ‡«◊œ‡∞Æ°£";
    	String c = chat.get(Utils.getRandom(random, 0, chat.size() - 1));
    	c = c.replace("player", playerName);
    	c = c.replace("target", targerName);
    	return c;
    }
}
