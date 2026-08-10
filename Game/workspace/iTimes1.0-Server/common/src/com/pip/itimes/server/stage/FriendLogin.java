package com.pip.itimes.server.stage;

import java.util.Hashtable;
public class FriendLogin {
       
	private static Hashtable<Integer, Long> friendLoginTime = new Hashtable<Integer, Long>();
	
	public static void setFriendLoginTime(int id, long time){
		synchronized (friendLoginTime) {
			friendLoginTime.put(id, time);
		}
	}
	
	public static boolean isHave(int id){
		if(friendLoginTime.containsKey(id)){
			return true;
		}
		return false;
	}
	
	public static long getFrindLoginTime(int id){
		if(friendLoginTime.containsKey(id)){
			return friendLoginTime.get(id);
		}
		return 0;
	}
	
}
