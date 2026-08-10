package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.Battlefield;

public class BattlefieldDao extends BaseDao {
	public BattlefieldDao() {
        super();
    }

    public void addBattlefield (Battlefield battlefield) throws DataAccessException {
        makePersistent(battlefield);
    }
}
