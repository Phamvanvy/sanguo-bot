package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.TongIsland;
import java.util.List;

public class TongIslandDao extends BaseDao {
    public TongIslandDao() {
    }

    public TongIsland[] getAll() throws DataAccessException{
        String hql = "from TongIsland t";
        List l = getList(hql);
        TongIsland[] ret = new TongIsland[l.size()];
        l.toArray(ret);
        return ret;
    }

    public void remove(TongIsland ti) throws DataAccessException{
        makeTransient(ti);
    }

    public void save(TongIsland ti) throws DataAccessException{
        makePersistent(ti);
    }
}
