package com.pip.server.account.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.server.account.bean.RecommendReward;

public class RecommendRewardDAO extends GenericHibernateDAO<RecommendReward,Integer> {
    public void add(RecommendReward rr) {
        getSession().save(rr);
    }
    
    public boolean hasRewarded(int accountID, int rewardCode) {
        String hql = "from RecommendReward rr where rr.guestID = " + accountID + 
            " and rr.rewardCode = " + rewardCode;
        Session sess = getSession();
        Query query = sess.createQuery(hql);
        List list = query.list();
        return (list != null && list.size() > 0);
    }

    public boolean hasRewarded(String phone, int rewardCode) {
        String hql = "from RecommendReward rr where rr.guestPhone = :phone" + 
            " and rr.rewardCode = " + rewardCode;
        Session sess = getSession();
        Query query = sess.createQuery(hql);
        query.setParameter("phone", phone);
        List list = query.list();
        return (list != null && list.size() > 0);
    }
}
