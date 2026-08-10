package com.pip.itimes.server.dao;

import java.util.List;

public class ChargePlanDao extends BaseDao{

	public ChargePlanDao(){
		super();
	}
	
	public List getAllChargePlan() throws DataAccessException{
		return getList("from ChargePlan");
	}
}
