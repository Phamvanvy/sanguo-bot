package com.pip.server.billing.chinarund;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class PayInfoDAO extends GenericHibernateDAO<PayInfo,Integer>{
	
	private static final String GETINFOBYPAYID = "from PayInfo p where p.payId=:payId";
	private static final String GETRECENTRECORDS = "from PayInfo p where p.accountId = :accountId and p.payTime > :payTime";
	private static final SimpleDateFormat payTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public void create(PayInfo payinfo){
		getSession().save(payinfo);
	}
	
	public PayInfo getInfoByPayId(String payId){
		Query query = getSession().createQuery(GETINFOBYPAYID);
		query.setParameter("payId", payId);
		return (PayInfo)query.uniqueResult();
	}
	
	public void update(PayInfo payinfo){
		getSession().update(payinfo);
	}
	
	public List<PayInfo> getRecentRecords(int accountID) {
		Date d = new Date(System.currentTimeMillis() - 3600000L);
		Query query = getSession().createQuery(GETRECENTRECORDS);
		query.setInteger("accountId", accountID);
		query.setString("payTime", payTimeFormat.format(d));
		return query.list();
	}
}
