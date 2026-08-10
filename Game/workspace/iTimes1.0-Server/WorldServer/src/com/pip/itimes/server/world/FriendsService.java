package com.pip.itimes.server.world;


import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.pip.itimes.server.bean.Friends;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.FriendsDao;

/**
 * @author sky
 * @version 1.0
 */
public class FriendsService {
	public static int FRIENDLIMIT = 10000;
    public static int FRIENDLEVEL1 = 4;
    public static int FRIENDLEVEL2 = 1;
    public static int FRIENDLEVEL3 = 1;
    private CacheManager cacheManager = CacheManager.create();
    
    private FriendsDao dao;
    private final Cache cache;
    //Cache cache = new Cache("AccountEntityCache", 5000, false, true, 0, 0);
    public FriendsService(FriendsDao dao) {
        this.dao = dao;
        this.cache = new Cache("FriendsCache", 7000, false, true, 0, 0);
        cacheManager.addCache(cache);
        //cache.put(new Element(Integer.valueOf(-1), null));
    }

    public void addFriend(Friends friends,int friendslevel,String friendsname) throws BuyException{
    	int friendplayerid = this.getfrienddoubleplayerid(friends.getPlayerid(), 1);
    	if (friendplayerid == -1){
    		cache.remove(friends.getPlayerid());
    		cache.put(new Element(Integer.valueOf(friends.getPlayerid()), friends));
    		try {
	            dao.addFriendid(friends);
	        } catch (DataAccessException ex) {
	            throw new BuyException("添加知己记录错误");
	        }
	        //把知己的信息也添加到表中
	        friendplayerid = this.getfrienddoubleplayerid(friends.getFriendplayerid(), 1);
	        if (friendplayerid < -1){
	        	Friends doublefriend = new Friends();
	        	doublefriend.setPlayerid(friends.getFriendplayerid());
	        	doublefriend.setPlayername(friendsname);
	        	doublefriend.setFriendplayerid(-1);
	        	doublefriend.setLevel(friendslevel);
	        	doublefriend.setImoney(0);
	        	doublefriend.setValid((byte)0);
    			try {
    	            dao.addFriend(doublefriend);
    	            cache.put(new Element(Integer.valueOf(doublefriend.getPlayerid()), doublefriend));
    	        } catch (DataAccessException ex) {
    	            throw new BuyException("添加知己记录错误");
    	        }
	        }
    	}else if (friendplayerid == -2){
	        try {
	            dao.addFriend(friends);
	            cache.put(new Element(Integer.valueOf(friends.getPlayerid()), friends));
	        } catch (DataAccessException ex) {
	            throw new BuyException("添加知己记录错误");
	        }
	        //把知己的信息也添加到表中
	        friendplayerid = this.getfrienddoubleplayerid(friends.getFriendplayerid(), 1);
	        if (friendplayerid < -1){
	        	Friends doublefriend = new Friends();
	        	doublefriend.setPlayerid(friends.getFriendplayerid());
	        	doublefriend.setPlayername(friendsname);
	        	doublefriend.setFriendplayerid(-1);
	        	doublefriend.setLevel(friendslevel);
	        	doublefriend.setImoney(0);
	        	doublefriend.setValid((byte)0);
    			try {
    	            dao.addFriend(doublefriend);
    	            cache.put(new Element(Integer.valueOf(doublefriend.getPlayerid()), doublefriend));
    	        } catch (DataAccessException ex) {
    	            throw new BuyException("添加知己记录错误");
    	        }
	        }
    	}
    }

    public int getImoney(int playerId) {
    	synchronized (cache) {
    		Friends friends = getFriendsFromCache(playerId);
			if (friends != null) {
				return friends.getImoney();
			}
			try {
	    		friends = dao.getFriends(playerId);
	        } catch (DataAccessException ex) {
	        	friends = null;
	        }
			if (friends != null) {
				cache.put(new Element(Integer.valueOf(friends.getPlayerid()), friends));
				return friends.getImoney();
			}else{
				return -2;
			}
			
		}
    }
    
    public Friends getFriends(int playerId) {
    	synchronized (cache) {
    		Friends friends = getFriendsFromCache(playerId);
			if (friends != null) {
				return friends;
			}
			try {
	    		friends = dao.getFriends(playerId);
	        } catch (DataAccessException ex) {
	        	friends = null;
	        }
			if (friends != null) {
				cache.put(new Element(Integer.valueOf(friends.getPlayerid()), friends));
				return friends;
			}else{
				return null;
			}
			
		}
    }
    public boolean canGetReborn(int playerId){
    	boolean flag = false;
    	int count = 0;
    	try {
    		count= dao.getRebornFriends(playerId);
        } catch (DataAccessException ex) {
        	ex.printStackTrace();
        }
        if(count >= 25){
        	flag = true;
        }
    	return flag;
    }
    
    protected Friends getFriendsFromCache(int playerId) {
		Element e = cache.get(Integer.valueOf(playerId));
		if (e == null)
			return null;
		return (Friends) e.getObjectValue();
	}
    
    
    public int getfriendplayerid(int playerId) {
    	synchronized (cache) {
    		Friends friends = getFriendsFromCache(playerId);
    		if (friends != null) {
				if (friends.getValid() == 0)
					return friends.getFriendplayerid();
				else
					return -2;
			}
			try {
	    		friends = dao.getFriends(playerId);
	        } catch (DataAccessException ex) {
	        	friends = null;
	        }
			if (friends != null) {
				cache.put(new Element(Integer.valueOf(friends.getPlayerid()), friends));
				if (friends.getValid() == 0)
					return friends.getFriendplayerid();
				else
					return -2;
			}else{
				return -2;
			}
		}
    	
    }
    
    public int getallplayerid(int playerId) {
    	synchronized (cache) {
    		Friends friends = getFriendsFromCache(playerId);
			if (friends != null) {
				return friends.getFriendplayerid();
			}
			try {
	    		friends = dao.getAllfriends(playerId);
	        } catch (DataAccessException ex) {
	        	friends = null;
	        }
			if (friends != null) {
				cache.put(new Element(Integer.valueOf(friends.getPlayerid()), friends));
				return friends.getFriendplayerid();
			}else{
				return -2;
			}
		}
    }
    
    public int getfrienddoubleplayerid(int playerId,int valid) {
    	synchronized (cache) {
    		Friends friends = getFriendsFromCache(playerId);
			if (friends != null) {
				if (valid == 0)
					if (friends.getValid() == 0)
						return friends.getFriendplayerid();
					else
						return -2;
				else
					return friends.getFriendplayerid();
			}
			try {
				if (valid == 0)
					friends = dao.getDoublefriends(playerId);
				else
					friends = dao.getDouble1friends(playerId);
	        } catch (DataAccessException ex) {
	        	friends = null;
	        }
			if (friends != null) {
				cache.put(new Element(Integer.valueOf(friends.getPlayerid()), friends));
				return friends.getFriendplayerid();
			}else{
				return -2;
			}
			
		}
    }
    
    public void killfriend(int playerId,int level) throws DataAccessException{
    	synchronized (cache) {
	    	int friendplayerId = this.getfriendplayerid(playerId);
    		if (friendplayerId < 0){
    			Friends mine = getFriends(playerId);
    			//更新自己的数据
    			if (mine!=null){
	    			dao.modifylevel(playerId, level);
	    			mine.setLevel(level);
	    			//信息更新到cache
		    		cache.remove(playerId);
		    		cache.put(new Element(Integer.valueOf(mine.getPlayerid()), mine));
    			}
	    	}else{
	    		int tmpplayerid = this.getfrienddoubleplayerid(friendplayerId,1);//表中
	    		Friends friends = getFriends(friendplayerId);
	    		Friends mine = getFriends(playerId);
	    		if (level == -1){//删角色
	    			dao.killfriend(playerId);
	    			mine.setValid((byte)1);
	    			//删除下级关系
	    			Friends[] friendsdown = this.getdownfriends(playerId);
	    			if (friendsdown == null){
	            		
	            	}else if(friendsdown.length == 0){
	            		
	            	}else{
	    	        	for (int i = 0; i < friendsdown.length; i++) {
	    	        		dao.killfriend(friendsdown[i].getPlayerid());
	    	        		Friends friendtmp = this.getFriends(friendsdown[i].getPlayerid());
	    	        		friendtmp.setValid((byte)1);
	    	        		cache.remove(friendsdown[i].getPlayerid());
	    		    		cache.put(new Element(Integer.valueOf(friendtmp.getPlayerid()), friendtmp));
	    	            }
	            	}
	    		}else{
		    		if (friends.getLevel() <= level){
		    			//超过好友的级别了
		    			dao.killfriend(playerId);
		    			dao.modifylevel(playerId, level);
		    			mine.setValid((byte)1);
		    			mine.setLevel(level);
		    		}else{
		    			//更新自己的数据
		    			dao.modifylevel(playerId, level);
		    			mine.setLevel(level);
		    		}
	    		}
	    		//信息更新到cache
	    		cache.remove(playerId);
	    		cache.put(new Element(Integer.valueOf(mine.getPlayerid()), mine));
	    	}	
		}
    }
    
    public void addfriendimoney(int playerId,int imoney) {
    	dao.addimoney(playerId,imoney);
    	Friends friendstmp = this.getFriends(playerId);
    	friendstmp.setImoney(friendstmp.getImoney() + imoney);
    	//信息更新到cache
		cache.remove(playerId);
		cache.put(new Element(Integer.valueOf(playerId), friendstmp));
    }
    
    public void lessimoney(int playerId,int accountId,int imoney) {
        dao.lessimoney(playerId,imoney);
        Friends friendstmp = this.getFriends(playerId);
    	friendstmp.setImoney(friendstmp.getImoney() - imoney);
    	//信息更新到cache
		cache.remove(playerId);
		cache.put(new Element(Integer.valueOf(playerId), friendstmp));
    }
    
    public Friends[] getdownfriends(int playerId) throws DataAccessException {
		Friends[] friends = dao.getdownfriends(playerId);
		if (friends != null) {
			return friends;
		}else{
			return null;
		}
    }
    public void nameModified(String oldName,WorldPlayer player){
    	Friends mine = getFriends(player.getId());
    	if (mine!=null){
	    	mine.setPlayername(player.getPlayerName());
	    	cache.remove(player.getId());
			cache.put(new Element(Integer.valueOf(mine.getPlayerid()), mine));
	    	
	    	dao.modifyname(player.getId(),player.getPlayerName());
    	}
    }
    //mengjie add
    public void addfriendbyfriend(Friends player) throws BuyException{
    	try {
			dao.addFriend(player);
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			throw new BuyException("添加知己记录错误");
		}
    }
    public void Modifylevel(int playerid,int playerlevel){
    	dao.modifylevel(playerid,playerlevel);
    }
    public void Removecache(int playerid){
    	cache.remove(playerid);
    }
}
