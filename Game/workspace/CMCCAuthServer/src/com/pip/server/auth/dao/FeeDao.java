package com.pip.server.auth.dao;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;

import com.pip.server.auth.bean.Account;
import com.pip.server.auth.bean.Fee;

import java.util.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class FeeDao extends BaseDao{
    private static final Logger log = Logger.getLogger(FeeDao.class);
    public FeeDao() {
    }

    public Fee getFee(int id){
        try {
            return (Fee) getObject(Fee.class, new Integer(id));
        } catch (DataAccessException ex) {
            log.error(ex, ex);
            return null;
        }
    }

    public Fee getLatestFee(String channel) {
        try {
            String sql = "from Fee b where b.channel = '" + channel + "' order by b.createTime desc";
            java.util.List l = getLimitedList(sql, 0, 1);
            if (l == null || l.size() == 0) {
            	return null;
            } else {
            	return (Fee)l.get(0);
            }
        } catch (DataAccessException ex) {
            log.error(ex, ex);
            return null;
        }
    }
    
    public int getMonthSum(String channel) {
    	try {
            String sql = "select sum(b.amount) from Fee b where b.channel = '" + 
            	channel + "' and b.charged = true and b.finishTime >= ? and b.finishTime < ?";
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
            Query query = getSession().createQuery(sql);
            query.setDate(0, cal.getTime());
            cal.add(Calendar.MONTH, 1);
            query.setDate(1, cal.getTime());
            Long ret = (Long)query.uniqueResult();
            return ret.intValue();
        } catch (Exception ex) {
            log.error(ex, ex);
            return 0;
        } finally {
            closeSession();
        }
    }

    public void saveFee(Fee fee){
        try {
            super.makePersistent(fee);
        } catch (DataAccessException ex) {
            log.error(ex, ex);
        }
    }
}
