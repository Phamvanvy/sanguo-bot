package com.pip.itimes.server.dao;

import org.hibernate.Query;

import com.pip.itimes.server.bean.Camp;

public class CampDao extends BaseDao{
    public Camp getCamp(int camp) throws DataAccessException{
        try{
            Query query = getSession().createQuery("from Camp where camp = :camp and valid = true");
            query.setInteger("camp", camp);
            return (Camp)query.uniqueResult();
        }finally{
            closeSession();
        }
    }
    
    public int getCampPlayerCount(int camp) throws DataAccessException{
        try{
            return ((Long)uniqueResult("select count(*) from Player p where p.camp = " + camp + " and p.valid = true")).intValue();
        }finally{
            closeSession();
        }
    }
    
    public String getCampPool(int camp) throws DataAccessException{
    	try {
    		return (String)uniqueResult("from Player p where p.camp = " + camp + " and p.valid = true");
		}finally{
			closeSession();
		}
    }
    
    public void updateCampPool(int camp, String pool) throws DataAccessException{
    	try {
    		query("update p.pool set p.pool = " + pool + " from Player p where p.camp = " + camp + " and p.valid = true");
		}finally{
			closeSession();
		}
    }
    
}
