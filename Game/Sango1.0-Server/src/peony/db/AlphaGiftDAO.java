package peony.db;

import peony.game.gift.AlphaGiftHistory;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class AlphaGiftDAO extends GenericHibernateDAO<AlphaGiftHistory, Integer> {

	public AlphaGiftHistory getAlphaGiftHistory(String name,String password){
		return (AlphaGiftHistory) uniqueResult(
				"from AlphaGiftHistory t where t.name = ? and t.password=?",
				name, password);
	}
}
