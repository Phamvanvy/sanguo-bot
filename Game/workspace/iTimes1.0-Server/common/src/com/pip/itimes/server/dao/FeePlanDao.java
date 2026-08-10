package com.pip.itimes.server.dao;

import java.util.List;
import org.hibernate.*;

public class FeePlanDao extends BaseDao{
	
	public FeePlanDao(){
		super();
	}
	
	public List getAllFePlanDao() throws DataAccessException{
		return getList("from FeePlan");
	}
}
