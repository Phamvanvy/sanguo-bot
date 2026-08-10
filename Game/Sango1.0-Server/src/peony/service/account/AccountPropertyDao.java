package peony.service.account;

import java.util.List;

import com.pip.db.hibernateDAO.GenericHibernateDAO;


public class AccountPropertyDao extends GenericHibernateDAO<AccountProperty, Integer>{
	@SuppressWarnings("unchecked")
	public AccountProperty getAccountPropertyByAcc(int accountId){
		return (AccountProperty) uniqueResult("from AccountProperty a where a.accountId=?", accountId);
	}
	
	public List<AccountProperty> getAccountProperty(int accountId){
		return  list("from AccountProperty a where a.accountId=?", accountId);
	}
	
}
