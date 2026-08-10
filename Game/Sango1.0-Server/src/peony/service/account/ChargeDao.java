package peony.service.account;

import java.util.Date;
import java.util.List;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class ChargeDao extends GenericHibernateDAO<Charge, Integer> {

	@SuppressWarnings("unchecked")
	public List<Charge> getAllCharges(){
		return list("from Charge");
	}
	
	public List<Charge> getChargesAfter(Date date){
		return list("from Charge c where c.chargeTime>=?", date);
	}
	
	public int getAccumulateCharge(int account, Date startTime,Date endTime) {
		try {
			String hql = "select sum(money) from Charge c where c.accountId = ? and c.chargeTime > ? and c.chargeTime<=?";
			return ((Long) uniqueResult(hql, account, startTime,endTime)).intValue();
		} catch (Exception e) {
			return 0;
		}
	}
	
	public long getTotalCharges(int accountId){
		return ((Long) uniqueResult("select sun(money) from Charge c where accountId = ?", accountId)).longValue();
	}
	
	public int getTotalChargesAfter(int account, Date startTime) {
		try {
			String hql = "select sum(money) from Charge c where c.accountId = ? and c.chargeTime >= ?";
			return ((Long) uniqueResult(hql, account, startTime)).intValue();
		} catch (Exception e) {
			return 0;
		}
	}
	
}
