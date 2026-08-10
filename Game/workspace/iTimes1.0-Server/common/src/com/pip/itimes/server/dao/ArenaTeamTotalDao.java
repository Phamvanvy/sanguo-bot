package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.ArenaTeamTotal;
import com.pip.itimes.server.bean.ArenaTeamTotalWorldWar;
import com.pip.itimes.server.bean.Bbs;

public class ArenaTeamTotalDao extends BaseDao{

    public ArenaTeamTotalDao() {
    }
    public void addArenaTeamTotal(ArenaTeamTotal arenaTeamTotal) throws DataAccessException {
        makePersistent(arenaTeamTotal);
    }
    public int getCount(ArenaTeamTotal arenaTeamTotal) throws DataAccessException{
        String hql = "from ArenaTeamTotal a where a.arenaid=" + arenaTeamTotal.getArenaid()+ 
        				" and a.serverid = '" + arenaTeamTotal.getServerid() + "'";
        return getCount(hql);
    }
    public void setArenaTeamTotal(ArenaTeamTotal arenaTeamTotal) throws DataAccessException{
        String hql = "update ArenaTeamTotal a set a.arenalevel="+arenaTeamTotal.getArenalevel()+
        				", a.updatetime='"+arenaTeamTotal.getUpdatetime()+
        				"' where a.arenaid=" + arenaTeamTotal.getArenaid()+ 
        				" and a.serverid = '" + arenaTeamTotal.getServerid() + "'";
        query(hql);
    }
    
    public ArenaTeamTotal[] getArenaTeamLevelOrder(int limit,int type) throws DataAccessException{
        String hql = "from ArenaTeamTotal t where t.Type = "+type+" order by t.arenalevel desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        ArenaTeamTotal[] ret = new ArenaTeamTotal[l.size()];
        l.toArray(ret);
        
        return ret;
    }
    public ArenaTeamTotal getArenaTeam(ArenaTeamTotal arenaTeamTotal) throws DataAccessException{
    	return (ArenaTeamTotal) uniqueResult("from ArenaTeamTotal a where a.arenaid=" + arenaTeamTotal.getArenaid()+ 
        				" and a.serverid = '" + arenaTeamTotal.getServerid() + "'");
    }
    public void deleteOwnerduplicate(ArenaTeamTotal arenaTeamTotal) throws DataAccessException {
        query("delete ArenaTeamTotal a where a.serverid= '" + arenaTeamTotal.getServerid() + "' and " +
        		"a.Type = " + arenaTeamTotal.getType() + " and a.ownerid = " + arenaTeamTotal.getOwnerid());
    }

    //worldwar add mengjie 
    public void addArenaTeamTotalWorldWar(ArenaTeamTotalWorldWar arenaTeamTotalWorldWar) throws DataAccessException {
        makePersistent(arenaTeamTotalWorldWar);
    }
    public ArenaTeamTotalWorldWar getWorldArenaTeam(int arenaId,String serverId) throws DataAccessException{
    	return (ArenaTeamTotalWorldWar) uniqueResult("from ArenaTeamTotalWorldWar a where a.arenaid=" + arenaId+ 
        				" and a.serverid = '" + serverId + "'");

    }
    public void deleteOwnerduplicateWorldWar(ArenaTeamTotalWorldWar arenaTeamTotalWorldWar) throws DataAccessException {
        query("delete ArenaTeamTotalWorldWar a where a.serverid= '" + arenaTeamTotalWorldWar.getServerid() + "' and " +
        		"a.Type = " + arenaTeamTotalWorldWar.getType() + " and a.ownerid = " + arenaTeamTotalWorldWar.getOwnerid());
    }
    public ArenaTeamTotalWorldWar[] getArenaTeamLevelOrderWorldWar(int limit,int type) throws DataAccessException{
        String hql = "from ArenaTeamTotalWorldWar t where t.Type = "+type+" order by t.arenalevel desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        ArenaTeamTotalWorldWar[] ret = new ArenaTeamTotalWorldWar[l.size()];
        l.toArray(ret);
        
        return ret;
    }
}
