package com.pip.server.account.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.server.account.bean.RecommendRequest;

public class RecommendRequestDAO extends GenericHibernateDAO<RecommendRequest,Integer> {
    public void add(RecommendRequest rr) {
        getSession().save(rr);
    }

    public RecommendRequest findByPhone(String phone) {
        String hql = "from RecommendRequest rr where rr.targetPhone = :phone and rr.validTime > :tnow order by rr.id desc";
        Session sess = getSession();
        Query query = sess.createQuery(hql);
        query.setParameter("phone", phone);
        query.setParameter("tnow", new Date());
        query.setMaxResults(1);
        List list = query.list();
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return (RecommendRequest)list.get(0);
        }
    }
}
