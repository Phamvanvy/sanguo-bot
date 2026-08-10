package com.pip.itimes.server.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.itimes.server.bean.ArenaTeam;

public class ArenaTeamDao extends BaseDao{

    public ArenaTeamDao() {
    }
    public void addArenaRecord(ArenaTeam arenateam) throws DataAccessException {
        makePersistent(arenateam);
    }
    public ArenaTeam findArenaTeam(int ownerid,int type) throws DataAccessException {
    	return (ArenaTeam) uniqueResult("from ArenaTeam t where t.owner = " + ownerid + " and t.Type = " + type + " and t.valid = 1");
    }
    public ArenaTeam findArenaTeamByArenaId(int ArenaId) throws DataAccessException {
    	return (ArenaTeam) uniqueResult("from ArenaTeam t where t.id = " + ArenaId + " and t.valid = 1");
    }
    public void updateArenaLevel(int ownerid,int point,int type){
        Query query;
        query = getSession().createQuery("update ArenaTeam t set t.arenalevel = t.arenalevel + " +point+" where t.owner="+
        		ownerid + " and t.valid = 1 and t.Type = "+type);
        query.executeUpdate();
    }
    public ArenaTeam[] getArenaTeamLevelOrder(int limit,int type) throws DataAccessException{
        String hql = "from ArenaTeam t where t.Type = "+type+" and t.valid = 1 order by t.arenalevel desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        ArenaTeam[] ret = new ArenaTeam[l.size()];
        l.toArray(ret);
        
        return ret;
    }
    public int getArenaTeamLevelowner(int arenateamid ,int arenateamlevel,int type) throws DataAccessException{
        String hql = "from ArenaTeam t where t.valid = 1 and t.Type = "+type+" and t.arenalevel > " + arenateamlevel + " or (t.arenalevel > " + arenateamlevel + " and t.id > " + arenateamid + ")";
        
        return getCount(hql);
    }
    public void deleteArenateam(int playerId,int type) throws DataAccessException{
    	Query query;
        query = getSession().createQuery("update ArenaTeam t set t.valid = 0 where t.owner="+
        		playerId + " and t.Type = "+type);
        query.executeUpdate();
    }
    public ArenaTeam getArenaTeamByName(String name) throws DataAccessException {
        return (ArenaTeam) uniqueResult("from ArenaTeam t where t.arenaname='" +
                                     name + "' and t.valid=true");
    }
}
