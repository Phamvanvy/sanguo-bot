package com.pip.itimes.server.world.lyricsSystem;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class LyricsSystemConfig {
	public final static byte LYRIC_GETTYPE = 0;		//获取类型
	public final static byte LYRIC_GETLYRICS = 1;	//获取歌曲列表
	public final static byte LYRIC_SING	= 2;		//点歌
	public final static byte LYRIC_GETSERVERS = 3;	//获取服务器列表
	
	public final static byte LYRIC_CONTEXT = 1;	//歌曲发送内容
	public final static byte LYRIC_LYRIC = 2;	//歌词
	
	
	public final static int LYRIC_SING_ITEMID = 201371;	//点歌物品ID	先用双倍经验果代替
	public final static int LYRIC_SING_ITEMID2 = 201372;	//点歌物品ID	先用双倍经验果代替
	
	public static Hashtable<String, ArrayList<LyricData>> alllyrics = new Hashtable<String, ArrayList<LyricData>>();
	
	private static ConcurrentHashMap<Integer, LyricDataServer> singLyrics = new ConcurrentHashMap<Integer, LyricDataServer>();
	
	/**
	 * 获得类型个数
	 * @return
	 */
	public static int getTypeCount(){
		return alllyrics.size();
	}
	
	/**
	 * 获得所有类型的数值
	 * @return
	 */
	public static String[] getTypes(){
		String[] keys = new String[alllyrics.size()];
		alllyrics.keySet().toArray(keys);
		return keys;
	}
	
	/**
	 * 获得指定类型的歌词库
	 * @param type
	 * @return
	 */
	public static ArrayList<LyricData> getTypeLyrics(String type){
		if(alllyrics.containsKey(type)){
			return alllyrics.get(type);
		}
		return null;
	}
	
	/**
	 * 获取指定类型和位置 的歌曲
	 * @param type
	 * @param index
	 * @return
	 */
	public static LyricData getLyric(String type, int index){
		if(index < 0) return null;
		ArrayList<LyricData> lyrics = getTypeLyrics(type);
		if(lyrics == null || lyrics.size() < index){
			return null;
		}
		return lyrics.get(index);
	}
	
	public static void addSing(int playerid, LyricDataServer lds){
		singLyrics.put(playerid, lds);
	}
	
	public static LyricDataServer getSing(int playerid){
		return singLyrics.get(playerid);
	}
	
	public static void removeSing(int playerid){
		if(singLyrics.containsKey(playerid)){
			singLyrics.remove(playerid);
		}
	}
}
