package com.pip.server.account.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.server.account.bean.Fee;

public class FeeDAO extends GenericHibernateDAO<Fee,Integer> {
	
	protected final static String GETLASTESTFEE = "from Fee b where b.channel=:channel order by b.createTime desc";
	protected final static String GETMONTHSUM = "select sum(b.amount) from Fee b where b.channel =:channel and b.charged = true and b.finishTime >=:beginTime and b.finishTime < :endTime";
    protected final static String GETCHINARUNCHARGE = "select sum(b.amount) from Fee b where b.accountId = :id and b.charged = true and (b.channel like 'CHINARUN%' or b.channel like 'YEEPAY%')";
	
	public void add(Fee fee){
		getSession().save(fee);
	}
	
	public void update(Fee fee){
		getSession().update(fee);
	}
	
	public Fee getFee(int id){
		return (Fee)getSession().get(Fee.class, new Integer(id));
	}
	
    public Fee getLatestFee(String channel) {
    	Query query = getSession().createQuery(GETLASTESTFEE);
    	query.setString("channel", channel);
    	query.setFirstResult(0);
    	query.setMaxResults(1);
    	List l = query.list();
    	if(l==null||l.size()==0)
    		return null;
    	return (Fee)l.get(0);
    }
    
    public int getMonthSum(String channel) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
		Query query = getSession().createQuery(GETMONTHSUM);
		query.setString("channel", channel);
		query.setDate("beginTime", cal.getTime());
		cal.add(Calendar.MONTH, 1);
		query.setDate("endTime", cal.getTime());
		Long ret = (Long) query.uniqueResult();
		if(ret!=null)
			return ret.intValue();
		return 0;

	}    
    
    public int getChinarunCharge(int accountID) {
        Query query = getSession().createQuery(GETCHINARUNCHARGE);
        query.setInteger("id", accountID);
        Long ret = (Long) query.uniqueResult();
        if(ret!=null)
            return ret.intValue();
        return 0;
    }    
}
