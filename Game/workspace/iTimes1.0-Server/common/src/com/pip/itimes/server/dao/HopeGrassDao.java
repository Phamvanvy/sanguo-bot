package com.pip.itimes.server.dao;

import java.util.List;
import org.hibernate.Query;
import java.util.Date;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HopeGrassDao extends BaseDao{

    public HopeGrassDao() {
    }

    public List getHopeGrass(int playerId,short mapId,short x,short y) throws DataAccessException{
        return getList("from HopeGrass h where h.playerId="+playerId+" and h.x="+x+" and h.y="+y+" and h.mapId="+mapId);
    }

    public List getHopeGrass(short mapId,short x,short y,int grassType) throws DataAccessException{
        try{
            Query query = getSession().createQuery(
                    "from HopeGrass h where h.x=:x and h.y=:y and h.mapId=:mapId and h.grassType=:grassType");
            query.setInteger("x", x);
            query.setInteger("y", y);
            query.setInteger("mapId", mapId);
            query.setInteger("grassType",grassType);
//            query.setTimestamp("time", new Date());
            return query.list();
        }
        finally{
            closeSession();
        }
    }

}
