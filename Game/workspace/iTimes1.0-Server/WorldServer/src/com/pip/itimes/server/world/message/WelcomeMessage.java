package com.pip.itimes.server.world.message;

import java.util.HashMap;

public class WelcomeMessage {
	public static HashMap<String, String> channelMessage = new HashMap<String, String>();
	
	public static HashMap<String, ChannelGiftData> channelGift = new HashMap<String, ChannelGiftData>();
	
	public static void addMessage(String key, String message){
		channelMessage.put(key, message);
	}
	
	public static String getMessage(String channel){
		if(channel == null || channel.equals("")) return null;
		if(channelMessage.containsKey(channel)){
			return channelMessage.get(channel);
		}
		return null;
	}
	
	public static void clearMessage(){
		channelMessage.clear();
	}
	
	public static void addChannelGift(String key, ChannelGiftData giftData){
		channelGift.put(key, giftData);
	}
	
	public static void clearGift(){
		channelGift.clear();
	}
	
	public static ChannelGiftData getChannelGift(String channel){
		if(channel == null || channel.equals("")) return null;
		if(channelGift.containsKey(channel)){
			return channelGift.get(channel);
		}
		return null;
	}
}
