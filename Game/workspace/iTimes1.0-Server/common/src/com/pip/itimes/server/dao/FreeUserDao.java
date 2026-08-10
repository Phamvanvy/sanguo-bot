package com.pip.itimes.server.dao;

import java.util.*;

public class FreeUserDao extends BaseDao{
	public FreeUserDao(){
		super();
	}
	
	public List getAllFreeUser() throws DataAccessException{
		return getList("from FreeUser");
	}
}
