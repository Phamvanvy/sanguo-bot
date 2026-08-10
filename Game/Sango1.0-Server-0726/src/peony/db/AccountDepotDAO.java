package peony.db;


import java.util.List;

import peony.game.AccountDepot;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class AccountDepotDAO extends GenericHibernateDAO<AccountDepot, Integer>{
	
	public AccountDepot getAccountDepot(int accountId) {
		return (AccountDepot) uniqueResult("from AccountDepot a where a.accountId = ?",accountId);
	}
	
	@SuppressWarnings("unchecked")
	public List<AccountDepot> getAccountDepots(int accountId){
		return list("from AccountDepot a where a.accountId = ?",accountId);
	}
	
}
