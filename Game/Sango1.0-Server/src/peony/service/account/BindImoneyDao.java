package peony.service.account;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class BindImoneyDao extends GenericHibernateDAO<BindImoney, Integer> {

	public BindImoney getBindImoneyByAccountId(int accountId){
		return (BindImoney) uniqueResult("from BindImoney where accountid=?", accountId);
	}
	
}
