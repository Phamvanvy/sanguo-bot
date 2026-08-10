package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.bean.Tong;
import com.pip.itimes.server.stage.PlayerCreditWapper;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import com.pip.itimes.server.stage.Friend;

import java.io.*;
import java.util.ArrayList;

public class PlayerDao extends BaseDao {
	protected static final Logger log = Logger.getLogger(PlayerDao.class);
	
    public PlayerDao() {
        super();
    }

    public void addPlayer(Player player) throws DataAccessException {
        makePersistent(player);
    }

    public Player getPlayerByName(String name) throws DataAccessException {
    	Player player = null;
    	try{
    		player = (Player)uniqueResult("from Player p where p.playerName='" +
                name + "' and p.valid=true");
    	}catch(Exception e){
    		log.info("getPlayerByName Exception name[" + name + "]");
    		throw new DataAccessException(e);
    	}
        return player;
    }

    public List<Player> getPlayerAdminValidByName(String name) throws DataAccessException {
        /*return (Player) uniqueResult("from Player p where p.playerName='" +
                                     name + "'");*/
    	 List l = getList("from Player p where p.playerName='" +
                 name + "' and p.valid=true");
         return l;
    }
    
    public List<Player> getPlayerAdminNoValidByName(String name) throws DataAccessException {
        /*return (Player) uniqueResult("from Player p where p.playerName='" +
                                     name + "'");*/
    	 List l = getList("from Player p where p.playerName='" +
                 name + "' and p.valid=false");
         return l;
    }
    
    public Player getPlayerById(int id) throws DataAccessException {
        return (Player) uniqueResult("from Player p where p.id=" + id + " and p.valid=true");
    }

    public List getPlayerList(int accountId) throws DataAccessException {
        return getList("from Player p where p.accountId=" + accountId+" and p.valid=true");
    }

    public Player getPlayerByNameAndAccountId(String name, int accountId) throws
            DataAccessException {
        try {
            Query query = getSession().createQuery(
                    "from Player p where p.playerName=:name and p.accountId=:accountId and p.valid=true");
            query.setString("name", name);
            query.setInteger("accountId", accountId);
            return (Player) query.uniqueResult();
        }
        finally{
            closeSession();
        }
//        return (Player) uniqueResult("from Player p where p.playerName='" +
//                                     name + "' and p.accountId=" + accountId +" and p.valid=true");
    }

    public int getPlayerId(String name) throws DataAccessException {
        Integer ret = (Integer) uniqueResult(
                "select p.id from Player p where p.playerName='" + name + "' and p.valid=true");
        if(ret!=null)
            return ret.intValue();
        return -1;
    }
    
    public String getPlayerName(int playerId) throws DataAccessException {
        String ret = (String) uniqueResult(
                "select p.playerName from Player p where id='" + playerId + "' and p.valid=true");
        if(ret!=null)
            return ret;
        return "";
    }
    //jwp add
    /**
     * @param playerId
     * @return 玩家的名称，等级，公会名称
     * @throws DataAccessException
     */
    public List getPlayerNameAndLevel(int playerId) throws DataAccessException {
    	String hql = "select t.playerName, t.level, t.tongName from Player t where t.id= " + playerId + " and valid = true";
        List l = getList(hql);
        return l;
    }
    //jwp addend
    public int getPlayerTongId(int playerId) throws DataAccessException{
        Integer ret = (Integer) uniqueResult("select p.tongId from Player p where p.id="+playerId);
        if(ret!=null)
            return ret.intValue();
        return -1;
    }

    public Friend[] getPlayerFriends(int playerId) throws DataAccessException{
        byte[] bytes = (byte[])uniqueResult("select p.friends from Player p where p.id="+playerId);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);
        try {
            byte count = dis.readByte();
        	List l = new ArrayList(count);
        	for (int i = 0; i < count; i++) {
        		int id = dis.readInt();
        		String name = dis.readUTF();
//                int favorite = 1;
        		int favorite = dis.readInt();
        		Friend friend = new Friend(id, name, favorite, 0);
        		l.add(friend);
        	}
        	Friend[] ret = new Friend[count];
        	l.toArray(ret);
        	return ret;
        } catch (IOException ex) {
            return new Friend[0];
        }
    }

    public int getPlayerId(String name,int accountid) throws DataAccessException{
        Integer ret = (Integer) uniqueResult(
                "select p.id from Player p where p.playerName='" + name + "' and p.valid=true and p.accountId="+accountid);
        if(ret!=null)
            return ret.intValue();
        return -1;
    }

    public Player[] getPlayerByAccountid(int accountId) throws DataAccessException{
        List l = getList("from Player p where p.accountId="+accountId+" and p.valid=true");
        Player[] ret = new Player[l.size()];
        l.toArray(ret);
        return ret;
    }

    public List getAllPlayers() throws DataAccessException{
        List l = getList("from Player");
        return l;
    }

    public PlayerCreditWapper[] getPlayerByCredit(int count) throws DataAccessException{
        List l = getLimitedList("select p.playerName,p.credit from Player p order by p.credit desc",0,count);
        PlayerCreditWapper[] ret = new PlayerCreditWapper[l.size()];
        for(int i=0;i<l.size();i++){
            Object[] o = (Object[])l.get(i);
            PlayerCreditWapper p = new PlayerCreditWapper();
            p.name = (String)o[0];
            p.credit = ((Integer)o[1]).intValue();
            ret[i] = p;
        }
        return ret;
    }

    public int getAccountIdByPlayerName(String name) throws DataAccessException{
        Integer ret = (Integer) uniqueResult(
                "select p.accountId from Player p where p.playerName='" + name + "' and p.valid=true");
        if(ret!=null)
            return ret.intValue();
        return -1;
    }
    
    public void killsAndSneaksDayEnd(){
        Query query;
        
        query = getSession().createQuery("update Player t set t.lastKills = t.kills, t.lastSneaks = t.sneaks");
        query.executeUpdate();
        
        query = getSession().createQuery("update Player t set t.kills = 0, t.sneaks = 0");
        query.executeUpdate();
    }
    
    public Player[] getPlayerLastKillsOrder(int limit) throws DataAccessException{
        String hql = "from Player t order by t.lastKills desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        Player[] ret = new Player[l.size()];
        l.toArray(ret);
        
        return ret;
    }
    
    public int getLastKillsOrder(Player player) throws DataAccessException{
        String hql = "from Player t where t.lastKills > " + player.getLastKills() + " or (t.lastKills = " + player.getLastKills() + " and t.id > " + player.getId() + ")";
        
        return getCount(hql);
    }
    
    public Player[] getPlayerLastSneaksOrder(int limit) throws DataAccessException{
        String hql = "from Player t order by t.lastSneaks desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        Player[] ret = new Player[l.size()];
        l.toArray(ret);
        
        return ret;
    }
    
    public int getLastSneaksOrder(Player player) throws DataAccessException{
        String hql = "from Player t where t.lastSneaks > " + player.getLastSneaks() + " or (t.lastSneaks = " + player.getLastSneaks() + " and t.id > " + player.getId() + ")";
        
        return getCount(hql);
    }
    
    public Player[] getPlayerarenaLevel1(int limit) throws DataAccessException{
        String hql = "from Player t where valid = 1 order by arenaLevel desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        Player[] ret = new Player[l.size()];
        l.toArray(ret);
        
        return ret;
    }
    public int getPlayerarenaLevelone1(int playerId,int Arenalevel) throws DataAccessException{
        String hql = "from Player t where valid = 1 and t.arenaLevel > " + Arenalevel + " or (t.arenaLevel > " + Arenalevel + " and t.id > " + playerId + ")";

        return getCount(hql);
    }
    public List getPlayerList_arena(int arenaId,int type) throws DataAccessException {
    	if (type == 1){
    		return getList("select id , playerName , arenaLevel from Player p where p.arenaV1Id=" + arenaId+" and p.valid=true");
    	}else if (type == 2){
    		return getList("select id , playerName , arenaLevel from Player p where p.arenaV2Id=" + arenaId+" and p.valid=true");
    	}else if (type == 3){
    		return getList("select id , playerName , arenaLevel from Player p where p.arenaV3Id=" + arenaId+" and p.valid=true");
    	}
        return null;
    }
    public void killArenateamPlayer(int arenaId,int type) throws DataAccessException{
    	Query query;
    	if (arenaId == 1){
    		query = getSession().createQuery("update Player p set p.arenaV1Id = -1 where p.arenaV1Id="+ arenaId);
    		query.executeUpdate();
    	}else if (arenaId == 2){
    		query = getSession().createQuery("update Player p set p.arenaV2Id = -1 where p.arenaV2Id="+ arenaId);
    		query.executeUpdate();
    	}else if (arenaId == 3){
    		query = getSession().createQuery("update Player p set p.arenaV3Id = -1 where p.arenaV3Id="+ arenaId);
    		query.executeUpdate();
    	}
    }
    public int getPlayerArenaAll(int playerId,int type) throws DataAccessException {
    	
    	Integer ret = null;
    	if (type == 1){
    		ret = (Integer) uniqueResult("select p.arenaV1Id from Player p where p.id=" + playerId+" and p.valid=true");
    	}else if (type == 2){
    		ret = (Integer) uniqueResult("select p.arenaV2Id from Player p where p.id=" + playerId+" and p.valid=true");
    	}else if (type == 3){
    		ret = (Integer) uniqueResult("select p.arenaV3Id from Player p where p.id=" + playerId+" and p.valid=true");
    	}
        if(ret!=null)
            return ret.intValue();
        return -1;
    }
    public void setPlayerArenalevel0(int playerId) throws DataAccessException {
    	Query query;
		query = getSession().createQuery("update Player p set p.arenaLevel = 0 where p.id="+ playerId);
		query.executeUpdate();
    	
    }	
    //启动时检索，
    public int getCampData(int type) throws DataAccessException {
    	
    	Long ret = null;

		ret = (Long) uniqueResult("select count(p.id) from Player p where p.camp = "+type+"");
        
		if(ret!=null)
            return ret.intValue();
        return -1;
    }
    
    public List getFriendsLastLoginTime(Friend friends[]) throws DataAccessException{
    	if(friends == null || friends.length == 0){
    		return null;
    	}
    	StringBuilder sb = new StringBuilder();
    	sb.append("select id, lastLoginTime from Player p where p.id in(");
    	int size = friends.length;
    	for(int i=0; i<size; i++){
    		sb.append(friends[i].getId());
    		if(i + 1 < size){
    			sb.append(",");
    		}
    	}
    	sb.append(")");
    	List list = getList(sb.toString());
    	return list;
    }
    
}
