package com.pip.itimes.server.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.itimes.server.bean.CampQualification;

public class CampAuctionDao extends BaseDao{
    public List<CampQualification> getAll(int camp) throws DataAccessException{
        try{
            Query query = getSession().createQuery("from CampQualification where camp =:camp and valid = true order by total desc");
            query.setInteger("camp", camp);
            return query.list();
        }finally{
            closeSession();
        }
    }
}
