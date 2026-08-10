package peony.service.account;

import java.util.List;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class FirstChargeDao extends GenericHibernateDAO<FirstCharge, Integer>{
	
//	public FirstCharge getFirstChargeByAccountId(int accountId){
//		return (FirstCharge) uniqueResult("from FirstCharge where accountid=?", accountId);
//	}
	
	public List<FirstCharge> getFirstChargeByAccountId(int accountId){
		return list("from FirstCharge where accountid=?", accountId);
	}
	
}
