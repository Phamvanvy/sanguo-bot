package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.Mercenary;

public class MercenaryDao extends BaseDao {
    public MercenaryDao() {
        super();
    }

    public void addMecenary(Mercenary mercenary) throws DataAccessException {
        makePersistent(mercenary);
    }

    public Mercenary getMercenaryByName(String name) throws DataAccessException {
        return (Mercenary) uniqueResult("from Mercenary m where m.playername='" +
                                     name + "' and m.valid=true");
    }
    
    public Mercenary getMercenaryById(int id) throws DataAccessException {
        return (Mercenary) uniqueResult("from Mercenary m where m.id=" + id + " and m.valid=true");
    }

    public int getMercenaryId(String name) throws DataAccessException {
        Integer ret = (Integer) uniqueResult(
                "select m.id from Mercenary m where m.playername='" + name + "' and m.valid=true");
        if(ret!=null)
            return ret.intValue();
        return -1;
    }
    
    public List<Mercenary> getPlayerShopMercenary() throws DataAccessException {
    	 List l = getList("from Mercenary m where m.profession=0 and m.state=0 and m.valid=true");
         return l;
    }
    
    public List<Mercenary> getPlayerMercenary() throws DataAccessException {
   	 List l = getList("from Mercenary m where m.state>=2 and m.state<=3 and m.valid=true");
        return l;
	}
    
    public List<Mercenary> getPlayerMercenary(String endtime) throws DataAccessException {
      	 List l = getList("from Mercenary m where m.createtime < '" + endtime + "' and m.state<>0 and m.valid=true");
           return l;
   	}
}
