package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Mate;
import java.util.List;

public class MateDao extends BaseDao{
    public MateDao() {
    }

    public Mate[] loadAllMates() throws DataAccessException{
        List l = getList("from Mate");
        Mate[] ret = new Mate[l.size()];
        l.toArray(ret);
        return ret;
    }

}
