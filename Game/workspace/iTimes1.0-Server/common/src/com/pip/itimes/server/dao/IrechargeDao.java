package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.Irecharge;

public class IrechargeDao extends BaseDao {
	public IrechargeDao() {
        super();
    }

    public void addIbuy(Irecharge irecharge) throws DataAccessException {
        makePersistent(irecharge);
    }
    
    public List getAllRecharge () throws DataAccessException {
    	String hql = "from Irecharge";
        List l = getList(hql);
        return l;
    }
}
