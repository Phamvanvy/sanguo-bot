package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Master;
import java.util.List;

public class MasterDao extends BaseDao{
    public MasterDao() {
    }

    public Master[] loadAllMasters(int state) throws DataAccessException{
        List l = getList("from Master where state="+state);
        Master[] ret = new Master[l.size()];
        l.toArray(ret);
        return ret;
    }
    
    public Master[] loadAllApprentices(int state, int masterid) throws DataAccessException {
    	List l = getList("from Master where state=" + state + " and masterid=" + masterid + " group by prenticeName");
    	Master[] ret = new Master[l.size()];
    	l.toArray(ret);
    	return ret;
    }

    public int getSuccessCount(int masterId) throws DataAccessException{
        return getCount("from Master where masterId="+masterId+" and state="+Master.SUCCESS);
    }
}
