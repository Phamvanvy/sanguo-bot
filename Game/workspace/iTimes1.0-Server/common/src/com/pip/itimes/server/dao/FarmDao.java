package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.Farm;

public class FarmDao extends BaseDao{
	public FarmDao(){
	}
	
	public Farm getFarm(int playerid) throws DataAccessException{
        return (Farm)uniqueResult("from Farm f where f.playerid = " + playerid);
    }
	
	public List getAllFarm() throws DataAccessException{
        return getList("from Farm");
    }
	
	public List getAllFarmPlayerID() throws DataAccessException{
		return getList("select f.playerid from Farm f");
	}
	
	public void saveFarm(Farm farm) throws DataAccessException{
		makePersistent(farm);
	}
	
}
