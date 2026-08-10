package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.Gift;


public class GiftDao extends BaseDao {
    public GiftDao() {
        super();
    }

    public void addGift(Gift gift) throws DataAccessException {
        makePersistent(gift);
    }
    
    public Gift getGift(int groupId, int playerId) throws DataAccessException{
        Gift ret = (Gift)uniqueResult("from Gift i where i.groupid = " + groupId + " and i.playerid = " + playerId);
        
        return ret;
    }
    
    public Gift[] getAllGift(int playerId) throws DataAccessException{
        List l = getList("from Gift i where i.playerid="+playerId+" order by modifytime desc");
        Gift[] ret = new Gift[l.size()];
        l.toArray(ret);
        
        return ret;
    }
}
