package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.Treasure;



/**
 * @author Jeffrey
 * @version 1.0
 */
public class TreasureDao extends BaseDao{
    public TreasureDao() {
    }

    public List getTreasure(int playerId) throws DataAccessException{
        return getList("from Treasure t where t.playerId="+playerId);
    }

    public List getTreasure(int playerId,short mapId) throws DataAccessException{
        return getList("from Treasure t where t.playerId="+playerId+" and t.mapId="+mapId);
    }
    
    public List getTreasure(int playerId,int shovelId) throws DataAccessException{
        return getList("from Treasure t where t.playerId="+playerId+
        						" and (t.shovelId="+shovelId+" or t.shovelId=-1)");
    }
}
