package com.pip.itimes.server.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.itimes.server.bean.Friends;
import com.pip.itimes.server.bean.Player;


public class FriendsDao extends BaseDao {
    public FriendsDao() {
        super();
    }
    public void addFriendid(Friends friends) throws DataAccessException {
    	Query query;
    	query = getSession().createQuery("update Friends t set t.friendplayerid = " + friends.getFriendplayerid() + 
    									",t.playername = '" + friends.getPlayername() + "' where t.playerid="+
    			friends.getPlayerid());
        query.executeUpdate();
    }
    
    public void addFriend(Friends friends) throws DataAccessException {
        makePersistent(friends);
    }
    //getFriends和getAllfriends 写一样的？看其来好似很麻烦啊
    
    public int getRebornFriends(int playerId) throws DataAccessException{
    	String hql= "from Friends t where t.friendplayerid="+
    				playerId + " and t.level >= 25 and valid = 0";
    	return getCount(hql);
    }
    
    public Friends getFriends(int playerId) throws DataAccessException{
    	return (Friends) uniqueResult("from Friends t where t.playerid="+
    				playerId);
    }
    
    public Friends getAllfriends(int playerId) throws DataAccessException{
    	return (Friends) uniqueResult("from Friends t where t.playerid="+
    				playerId);
    }
    
    public Friends getDoublefriends(int playerId) throws DataAccessException{
    	return (Friends) uniqueResult("from Friends t where t.playerid="+
    				playerId+" and t.valid = 0");
    }
    
    public Friends getDouble1friends(int playerId) throws DataAccessException{
    	return (Friends) uniqueResult("from Friends t where t.playerid="+
    				playerId);
    }
    
    public void killfriend(int playerId){
        Query query;
        
        query = getSession().createQuery("update Friends t set t.valid = 1 where t.playerid="+
    				playerId);
        query.executeUpdate();
    }
    
    public void lessimoney(int playerId,int imoney) {        
    	Query query;
    	query = getSession().createQuery("update Friends t set t.imoney = t.imoney - " + imoney + " where t.playerid="+
    				playerId);
        query.executeUpdate();
    }
    public void addimoney(int playerId,int imoney) {        
    	Query query;
    	query = getSession().createQuery("update Friends t set t.imoney = t.imoney + " + imoney + " where t.playerid="+
    				playerId);
        query.executeUpdate();
    }
    
    public void modifylevel(int playerId,int level) {        
    	Query query;
    	query = getSession().createQuery("update Friends t set t.level = " + level + " where t.playerid="+
    				playerId);
        query.executeUpdate();
    }
    public Friends[] getdownfriends(int playerId) throws DataAccessException{
    	List l = getList("from Friends t where t.friendplayerid=" + playerId + " and t.valid = 0");
    	Friends[] ret = new Friends[l.size()];
        l.toArray(ret);
        return ret;
    }
    public void modifyname(int playerId,String name) {        
    	Query query;
    	query = getSession().createQuery("update Friends t set t.playername = '" + name + "' where t.playerid="+
    				playerId);
        query.executeUpdate();
    }
}
