package com.pip.server.billing.f3g;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 服务器大事件（畅游）
 * @author lighthu
 */
public class GameEventManager {
	public static List<GameEvent> events = new ArrayList<GameEvent>();
	
	public static synchronized void addEvent(GameEvent evt) {
		events.add(evt);
	}
	
	public static synchronized List<GameEvent> getEvent(int[] servers, int limit, int[] types) {
		Set<Integer> serverIDs = new HashSet<Integer>();
		for (int i : servers) {
			serverIDs.add(i);
		}
		Set<Integer> typeIDs = new HashSet<Integer>();
		for (int i : types) {
			typeIDs.add(i);
		}
		List<GameEvent> ret = new ArrayList<GameEvent>();
		for (int i = 0; i < events.size(); i++) {
			GameEvent evt = events.get(i);
			if (serverIDs.contains(evt.serverID) && (typeIDs.size() == 0 || typeIDs.contains(evt.eventType))) {
				ret.add(0, evt);
				events.remove(i);
				i--;
				if (ret.size() >= limit) {
					break;
				}
			}
		}
		return ret;
	}
}
