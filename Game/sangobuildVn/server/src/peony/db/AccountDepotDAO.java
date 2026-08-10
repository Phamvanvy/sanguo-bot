package peony.db;


import peony.game.AccountDepot;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class AccountDepotDAO extends GenericHibernateDAO<AccountDepot, Integer>{
	
	public AccountDepot getAccountDepot(int accountId) {
		return (AccountDepot) uniqueResult("from AccountDepot a where a.accountId = ?",accountId);
	}
}
