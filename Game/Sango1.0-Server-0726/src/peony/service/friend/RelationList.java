package peony.service.friend;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import peony.game.Actor;
import peony.game.Server;
import peony.service.player.ActorCacheService;

/**
 * 关联玩家列表。这个类被用于保存好友列表、黑名单、仇人录以及临时玩家表。
 * @author lighthu
 */
public class RelationList {
	public static final int LOCKED = 1;
	public static final int UNLOCKED = 0;
	
	// 玩家列表
	public Vector<Actor> players = new Vector<Actor>();
	// 玩家好友度/仇人度
	public Map<Integer, Integer> degrees = new ConcurrentHashMap<Integer, Integer>();
	// 玩家是否被锁定     1:锁定    0：未锁定
	public Map<Integer, Integer> isLocks = new ConcurrentHashMap<Integer, Integer>();
	/**
	 * 复制此玩家列表。
	 * @return
	 */
	@Override
	public RelationList clone() {
		RelationList ret = new RelationList();
		ret.players.addAll(players);
		ret.degrees.putAll(degrees);
		ret.isLocks.putAll(isLocks);
		return ret;
	}
	
	/**
	 * 从存储玩家列表的字符串中恢复。
	 * 每行格式为：玩家ID,玩家昵称,好友度
	 * @param data
	 */
	public void parse(String data) {
		ActorCacheService actorCacheService = Server.server
				.getServiceRegistry().getActorCacheService();
		try {
			players.clear();
			BufferedReader br = new BufferedReader(new StringReader(data));
			String line;
			while ((line = br.readLine()) != null) {
				try {
					String[] secs = line.split(",");
					if (secs.length != 4 && secs.length != 3) {
						throw new Exception("invalid relation data");
					}
					int playerID = Integer.parseInt(secs[0]);
					String playerName = secs[1];
					int playerDegree = Integer.parseInt(secs[2]);
					//默认为未锁定  
					int isLocked = UNLOCKED;
					if(secs.length == 4){
						isLocked = Integer.parseInt(secs[3]);
					}
					Actor actor = actorCacheService.add(playerID, playerName);
					if (actor != null && actor.exist == 1) {
						players.add(actor);
						degrees.put(playerID, playerDegree);
						isLocks.put(playerID, isLocked);
					}
				} catch (Exception e) {
				    e.printStackTrace();
				}
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * 处理的数据格式与parse相同，只为合服程序服务。
	 * 由于合服时没有相应的service，所以创建一些虚假的Actor，只为修改其中的id和name
	 * 如有修改parse函数，请一并修改此函数的相应部分
	 * @param data
	 */
	public void parseForMerge(String data){
        try {
            players.clear();
            BufferedReader br = new BufferedReader(new StringReader(data));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] secs = line.split(",");
                    if (secs.length != 4 && secs.length != 3) {
                        throw new Exception("invalid relation data");
                    }
                    int playerID = Integer.parseInt(secs[0]);
                    String playerName = secs[1];
                    int playerDegree = Integer.parseInt(secs[2]);
                    int isLocked = UNLOCKED;
					if(secs.length == 4){
						isLocked = Integer.parseInt(secs[3]);
					}
                    Actor actor = new Actor();
                    actor.id = playerID;
                    actor.name = playerName;
                    actor.exist = 1;

                    if (actor != null && actor.exist == 1) {
                        players.add(actor);
                        degrees.put(playerID, playerDegree);
                        isLocks.put(playerID, isLocked);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
        }
	}
	
	/**
	 * 转换为存储格式。
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		int count = players.size();
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				buf.append("\n");
			}
			Actor player = players.get(i);
			buf.append(player.id + "," + player.name + "," + degrees.get(player.id) + "," + isLocks.get(player.id));
		}
		return buf.toString();
	}
	
	/**
	 * 判断两个列表是否完全相同。
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RelationList)) {
			return false;
		}
		RelationList oo = (RelationList)o;
		int size = players.size();
		if (oo.players.size() == size) {
			for (int i = 0; i < size; i++) { 
				Actor actor1 = oo.players.get(i);
				Actor actor2 = players.get(i);
				if (actor1.id != actor2.id || oo.degrees.get(actor1.id) != degrees.get(actor2.id) || oo.isLocks.get(actor1.id) != isLocks.get(actor2.id)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 向列表中添加一个新玩家。新添加的玩家会出现在列表头，如果此玩家已经在列表中存在了，
	 * 则此玩家被移动到第一个。
	 * @param id
	 * @param name
	 */
	public void addPlayer(Actor actor, int degree) {
		int index = findPlayer(actor.id);
		if (index == -1) {
			players.add(0, actor);
		} else {
			actor = players.remove(index);
			players.add(0, actor);
		}
		degrees.put(actor.id, degree);
		isLocks.put(actor.id, UNLOCKED);
	}
	
	/**
	 * 裁剪玩家列表，以确保其数量不超过指定数目。
	 * @param maxCount 最大玩家数量
	 */
	public void truncate(int maxCount) {
		int size = players.size();
		if (size > maxCount) {
			for (int i = maxCount; i < size; i++) {
				degrees.remove(players.get(i).id);
				isLocks.remove(players.get(i).id);
			}
			while (players.size() > maxCount) {
				players.remove(maxCount);
			}
		}
	}
	
	/**
	 * 从列表中删除一个玩家。
	 * @param id
	 */
	public void removePlayer(int id) {
		int index = findPlayer(id);
		if (index != -1) {
			Actor actor = players.remove(index);
			degrees.remove(actor.id);
			isLocks.remove(actor.id);
		}
	}
	
	/*
	 * 在列表中查找某个玩家。
	 * @param id 玩家ID
	 * @return 玩家在列表中的索引，如果找不到返回-1.
	 */
	public int findPlayer(int id) {
		int size = players.size();
		for (int i = 0; i < size; i++) {
			if (id == players.get(i).id) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 判断一个玩家是否在列表中存在。
	 * @param id 玩家ID
	 * @return
	 */
	public boolean exists(int id) {
		return degrees.containsKey(id);
	}
	
	/**
	 * 获得列表中玩家数量。
	 * @return
	 */
	public int getCount() {
		return players.size();
	}
	
	/**
	 * 刷新玩家列表
	 */
	public void refreshPlayers(){
		Iterator<Actor> it = players.iterator();
		while(it.hasNext()){
			if(it.next().exist==0)
				it.remove();
		}
	}
	
	/**
	 * 取得指定索引位置的玩家。
	 * @param index
	 * @return
	 */
	public Actor getPlayerAt(int index) {
		return players.get(index);
	}
	
	/**
	 * 取得指定玩家的好友度。
	 * @param id 玩家ID
	 * @return
	 */
	public int getDegreeOfPlayer(int id) {
		try {
			return degrees.get(id);
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * 设置玩家好友度。
	 * @param id 玩家ID
	 * @param newValue
	 */
	public void setDegreeOfPlayer(int id, int newValue) {
		degrees.put(id, newValue);
	}
	
	/**
	 * 判断指定玩家是否被锁定
	 * @param id
	 * @return
	 */
	public boolean isLockedOfPlayer(int id){
		try{
			if(isLocks.get(id) == LOCKED){
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			return false;
		}
	}
	
	/**
	 * 锁定指定关系人
	 * @param id
	 */
	public void lockPlayer(int id){
		isLocks.put(id, LOCKED);
	}
	
	/**
	 * 解锁指定关系人
	 * @param id
	 */
	public void unLockPlayer(int id){
		isLocks.put(id, UNLOCKED);
	}
	
}
