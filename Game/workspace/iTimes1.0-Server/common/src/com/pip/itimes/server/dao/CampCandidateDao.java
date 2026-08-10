package com.pip.itimes.server.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.itimes.server.bean.CampCandidate;

public class CampCandidateDao extends BaseDao{
    public List<CampCandidate> getAll(int camp) throws DataAccessException{
        try{
            Query query = getSession().createQuery("from CampCandidate where camp =:camp and valid = true");
            query.setInteger("camp", camp);
            return query.list();
        }finally{
            closeSession();
        }
    }
}
