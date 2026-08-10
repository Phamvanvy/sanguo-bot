package com.pip.itimes.server.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.itimes.server.bean.CampVote;

public class CampVoteDao extends BaseDao{
    public List<CampVote> getAll() throws DataAccessException{
        try{
            Query query = getSession().createQuery("from CampVote where valid = true");
            return query.list();
        }finally{
            closeSession();
        }
    }
}
